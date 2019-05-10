package ai.reactivity.cccp.akka.messages.in

import ai.reactivity.cccp.akka.messages.out.Job

case class Restart(job: Job) extends MonitorCommand
