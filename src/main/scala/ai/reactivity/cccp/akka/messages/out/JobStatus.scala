package ai.reactivity.cccp.akka.messages.out

object JobStatus extends Enumeration  {
  val Initializing, Running, Restarting = Value
}
