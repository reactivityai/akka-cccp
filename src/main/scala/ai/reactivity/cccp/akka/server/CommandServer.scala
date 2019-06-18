package ai.reactivity.cccp.akka.server

import ai.reactivity.cccp.akka.messages.in.{Subscribe, Unsubscribe}
import ai.reactivity.cccp.util.JavaTimeSerializers
import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.http.scaladsl.model.ws
import akka.http.scaladsl.model.ws.{BinaryMessage, TextMessage}
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{Flow, Sink}
import org.json4s.jackson.Serialization
import org.json4s.{Formats, NoTypeHints}

class CommandServer extends Actor {

  implicit val formats: Formats = Serialization.formats(NoTypeHints) ++ JavaTimeSerializers.defaults

  implicit val system: ActorSystem = context.system
  implicit val mat: Materializer = ActorMaterializer()

  override def preStart(): Unit = {
    val wsService = Flow[ws.Message].mapConcat {
      case msg: TextMessage =>
        ???
      case bin: BinaryMessage =>
        bin.dataStream.runWith(Sink.ignore)
        Nil
    }

    context.become(running(Set.empty))
  }

  override def receive: Receive = Actor.emptyBehavior

  def running(subscribers: Set[ActorRef]): Receive = {
    case Subscribe(actor) =>
      context.become(running(subscribers + actor))
    case Unsubscribe(actor) =>
      context.become(running(subscribers - actor))
  }
}
