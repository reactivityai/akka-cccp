package ai.reactivity.cccp.akka.messages.in

import akka.actor.ActorRef

case class Subscribe(actor: ActorRef) extends MonitorRequest
