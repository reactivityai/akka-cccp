package ai.reactivity.cccp.akka.messages.out

import java.time.Instant

import org.json4s.JsonAST.JValue

case class Job(pid: Int, name: String, params: JValue, created: Instant, updated: Instant, status: JobStatus.Value) extends MonitorResponse {
  override def toString: String = s"Job $name:$pid $status created:$created updated:$updated"
}

object Job {
  def createNew(pid: Int, name: String, params: JValue) = Job(pid, name, params, Instant.now(), Instant.now(), JobStatus.Running) // todo initializing
}