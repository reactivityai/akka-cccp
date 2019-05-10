package ai.reactivity.cccp.akka.state

import ai.reactivity.cccp.akka.messages.out.Job
import akka.actor.ActorRef

case class Process(job: Job, runner: ActorRef)
