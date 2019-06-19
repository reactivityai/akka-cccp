package ai.reactivity.cccp.akka

import java.time.Instant

import ai.reactivity.cccp.akka.messages.in._
import ai.reactivity.cccp.akka.messages.out.{Error, Job, JobStatus}
import ai.reactivity.cccp.akka.state.{MonitorState, Process}
import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class Supervisor(resolver: Resolver, minRestartDelay: FiniteDuration) extends Actor with LazyLogging {

  implicit val system: ActorSystem = context.system
  implicit val ctx: ExecutionContext = context.dispatcher

  override def preStart(): Unit = {
    logger.info("Starting supervisor...")
    context.become(running(state = MonitorState.empty, subscribers = Set.empty))
  }

  override def receive: Receive = Actor.emptyBehavior

  def running(state: MonitorState, subscribers: Set[ActorRef]): Receive = {
    case Subscribe(actor) =>
      context.become(running(state, subscribers + actor))
    case Unsubscribe(actor) =>
      context.become(running(state, subscribers - actor))
    case Start(name, params) =>
      state.procs.find(name == _._2.job.name) match {
        case Some(_) =>
          val errMsg = s"Runner with same name already monitored: " + name
          val err = Error(Error.ALREADY_MONITORED, errMsg)
          logger.error(errMsg)
          subscribers.foreach(_ ! err)
        case None =>
          resolver.resolve(name, params) match {
            case Some(props) =>
              logger.info("Resolved runner: " + name)
              val nextPid = state.nextPid
              val actorName = "proc-" + name + "-" + nextPid
              val runner = context.actorOf(props, actorName)
              context.watch(runner)
              val job = Job.createNew(nextPid, name, params)
              val proc = Process(job, runner)
              val newState = state.copy(procs = state.procs + (job.pid -> proc))
              logger.info("Created job: " + job)
              subscribers.foreach(_ ! job)
              context.become(running(newState, subscribers))
            case None =>
              val errMsg = "Cannot resolve runner: " + name
              val err = Error(Error.NOT_FOUND, errMsg)
              subscribers.foreach(_ ! err)
          }
      }
    case Stop(id) =>
      state.procs.get(id) match {
        case Some(proc) =>
          logger.info("Stopping process: " + proc)
          system.stop(proc.runner)
          // removing the proc from monitoring to
          context.become(running(state.copy(procs = state.procs - id), subscribers))
        case None =>
          val errMsg = "Process not found: " + id
          val err = Error(Error.NOT_FOUND, errMsg)
          logger.warn(errMsg)
          subscribers.foreach(_ ! err)
      }
    case Restart(job) =>
      resolver.resolve(job) match {
        case Some(props) =>
          val actorName = "proc-" + job.name + "-" + job.pid // reusing same pid
          logger.info("Restarting: " + job)
          val runner = context.actorOf(props, actorName)
          context.watch(runner)
          val newJob = job.copy(updated = Instant.now(), status = JobStatus.Running)
          val newProc = Process(newJob, runner)
          val newState = state.copy(procs = state.procs + (newProc.job.pid -> newProc))
          context.become(running(newState, subscribers))
        case None =>
          logger.error("Could not resolve runner for $job!")
      }
    case Terminated(actor) =>
      val actorName = actor.path.name
      logger.info("Actor terminated: " + actorName)
      val pattern = """^.+\-(\d+)$""".r
      actorName match {
        case pattern(gr) =>
          val pid = gr.toInt
          state.procs.get(pid) match {
            case Some(proc) =>
              logger.info(s"Monitored proc found: $pid. Proceeding to restart...")
              val newJob = proc.job.copy(updated = Instant.now(), status = JobStatus.Restarting)
              val newProc = proc.copy(job = newJob)
              val newState = state.copy(procs = state.procs + (newJob.pid -> newProc))
              val elapsedTime = (newJob.updated.toEpochMilli - proc.job.updated.toEpochMilli).millis
              context.become(running(newState, subscribers))
              if (elapsedTime > minRestartDelay) {
                self ! Restart(newJob)
              } else {
                context.system.scheduler.scheduleOnce(minRestartDelay, self, Restart(newJob))
              }
            case None =>
              logger.warn("Unmonitored actor terminated: " + actorName + "; doing nothing.")
          }
        case _ =>
          logger.warn("Unknown child actor terminated: " + actorName)
      }
  }
}

object Supervisor {
  def props(resolver: Resolver, minRestartDelay: FiniteDuration): Props = Props(new Supervisor(resolver, minRestartDelay))
}