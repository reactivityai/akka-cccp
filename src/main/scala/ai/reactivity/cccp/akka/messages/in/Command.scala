package ai.reactivity.cccp.akka.messages.in

import org.json4s.JsonAST.JValue

case class Command(data: JValue)