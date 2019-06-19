package ai.reactivity.cccp.akka

import ai.reactivity.cccp.akka.server.WsCommandServer
import ai.reactivity.cccp.util.RejectingResolver
import akka.actor.ActorSystem

import scala.concurrent.duration._

object Main extends App {
  val system = ActorSystem("cccp")
  val supervisor = system.actorOf(Supervisor.props(RejectingResolver, minRestartDelay = 1.seconds))
  system.actorOf(WsCommandServer.props(supervisor, host = "localhost", port = 7777))
}
