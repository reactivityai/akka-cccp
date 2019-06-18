package ai.reactivity.cccp.akka.messages.wrap

import org.json4s.JsonAST.JValue

case class JsonRpcRequest(method: String, params: JValue, destination: Option[String], id: Option[String])

