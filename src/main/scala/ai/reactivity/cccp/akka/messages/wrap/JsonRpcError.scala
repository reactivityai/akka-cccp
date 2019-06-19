package ai.reactivity.cccp.akka.messages.wrap

case class JsonRpcError[T](code: Int, message: String, data: Option[T])
