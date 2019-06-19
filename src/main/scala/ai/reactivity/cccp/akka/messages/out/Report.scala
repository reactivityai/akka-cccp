package ai.reactivity.cccp.akka.messages.out

import java.time.Instant

import org.json4s.JsonAST.JValue

case class Report(origin: String, data: JValue, timestamp: Instant)