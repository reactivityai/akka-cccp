package ai.reactivity.cccp.akka.messages.wrap

case class JsonRpcRequest[T](method: String, params: T, id: Option[String])

