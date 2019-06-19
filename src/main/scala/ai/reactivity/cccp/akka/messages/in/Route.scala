package ai.reactivity.cccp.akka.messages.in

case class Route[T](destination: Int, message: T) extends MonitorRequest