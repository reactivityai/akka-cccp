package ai.reactivity.cccp.akka.messages.in

import org.json4s.JsonAST.JValue

case class Start(name: String, params: JValue) extends MonitorCommand
