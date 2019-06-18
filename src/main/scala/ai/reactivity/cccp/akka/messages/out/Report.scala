package ai.reactivity.cccp.akka.messages.out

import java.time.Instant

import org.json4s.JsonAST.JValue

case class Report(data: JValue, timestamp: Instant)