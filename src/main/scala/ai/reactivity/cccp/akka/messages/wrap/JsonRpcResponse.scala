package ai.reactivity.cccp.akka.messages.wrap

case class JsonRpcResponse[T, E](jsonrpc: String = "2.0", result: Option[T], error: Option[JsonRpcError[E]], id: Option[Int])

object JsonRpcResponse {
  def result[T](value: T, id: Int): JsonRpcResponse[T, Unit] = JsonRpcResponse(result = Some(value), error = None, id = Some(id))
  def result[T](value: T): JsonRpcResponse[T, Unit] = JsonRpcResponse(result = Some(value), error = None, id = None)
  def error(code: Int, message: String, id: Int): JsonRpcResponse[Unit, Unit] = JsonRpcResponse(result = None, error = Some(JsonRpcError(code, message, None)), id = None)
  def error(code: Int, message: String): JsonRpcResponse[Unit, Unit] = JsonRpcResponse(result = None, error = Some(JsonRpcError(code, message, None)), id = None)
}
