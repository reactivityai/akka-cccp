package ai.reactivity.cccp.akka.messages.in

import akka.actor.ActorRef

case class Unsubscribe(actor: ActorRef) extends MonitorCommand
