package ai.reactivity.cccp.util

import ai.reactivity.cccp.akka.Resolver
import akka.actor.Props
import org.json4s.JsonAST

object RejectingResolver extends Resolver {
  override def resolve(name: String, params: JsonAST.JValue): Option[Props] = None
}
