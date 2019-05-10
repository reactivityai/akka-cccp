package ai.reactivity.cccp.akka.messages.out

case class Error(code: Int, message: String) extends MonitorResponse

object Error {
  val SOURCE_ALREADY_MONITORED = 606
  val SOURCE_NOT_FOUND = 404
  val NOT_IMPLEMENTED = 500
}