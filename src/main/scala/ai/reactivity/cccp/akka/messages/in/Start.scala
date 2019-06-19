package ai.reactivity.cccp.akka.messages.in

import org.json4s.JsonAST.JValue

case class Start(service: String, params: JValue) extends MonitorRequest
