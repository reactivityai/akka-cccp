package ai.reactivity.cccp.akka.server

import ai.reactivity.cccp.akka.messages.in._
import ai.reactivity.cccp.akka.messages.out.MonitorResponse
import ai.reactivity.cccp.akka.messages.wrap.{JsonRpcRequest, JsonRpcResponse}
import ai.reactivity.cccp.util.JavaTimeSerializers
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws.{BinaryMessage, TextMessage, UpgradeToWebSocket}
import akka.stream.scaladsl.Sink
import com.typesafe.scalalogging.LazyLogging
import org.json4s.JsonAST.JValue
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._
import org.json4s.{Formats, NoTypeHints}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

class WsCommandServer(supervisor: ActorRef, host: String, port: Int) extends Actor with LazyLogging {

  implicit val formats: Formats = Serialization.formats(NoTypeHints) ++ JavaTimeSerializers.defaults

  implicit val system: ActorSystem = context.system

  var bindingFuture: Future[Http.ServerBinding] = _

  override def preStart(): Unit = {

    implicit val executionContext: ExecutionContext = ExecutionContext.global

    val requestHandler: HttpRequest => HttpResponse = {
      case req@HttpRequest(HttpMethods.GET, Uri.Path("/"), _, _, _) =>
        req.header[UpgradeToWebSocket] match {
          case Some(upgrade) =>
            val session = new WebsocketSession(onRequest = { (s, msg) =>
              val decoded = decodeRequest(msg)
              decoded.onComplete {
                case Success(x) => self ! x
                case Failure(e) =>
                  val decodeError = JsonRpcResponse.error(-500,"Request decode error: " + e.getMessage)
                  sendTo(s, decodeError)
              }
            }, onDisconnect = self ! Disconnected(_))
            self ! Connected(session)
            upgrade.handleMessages(session.flow)
          case None =>
            HttpResponse(400, entity = "Not a valid websocket request!")
        }
      case r: HttpRequest =>
        r.discardEntityBytes()
        HttpResponse(404, entity = "Unknown resource!")
    }

    supervisor ! Subscribe(self)

    bindingFuture = Http().bindAndHandleSync(requestHandler, interface = host, port = port)
    logger.info(s"WS server online, $host:$port...")
    context.become(running(subscribers = Set.empty, sessions = Set.empty))
  }

  override def receive: Receive = Actor.emptyBehavior

  def running(subscribers: Set[ActorRef], sessions: Set[WebsocketSession]): Receive = {
    case Subscribe(actor) =>
      context.become(running(subscribers + actor, sessions))
    case Unsubscribe(actor) =>
      context.become(running(subscribers - actor, sessions))
    case Connected(session) =>
      context.become(running(subscribers, sessions + session))
    case Disconnected(session) =>
      context.become(running(subscribers, sessions - session))
    case req: MonitorRequest =>
      supervisor ! req
    case res: MonitorResponse =>
      val encoded = encodeResponse(res)
      sessions.foreach(_.send(encoded))
  }

  override def postStop(): Unit = {
    implicit val ctx: ExecutionContextExecutor = context.dispatcher
    supervisor ! Unsubscribe(self)
    bindingFuture.flatMap(_.unbind())
  }

  private def decodeRequest(request: ws.Message)(implicit ctx: ExecutionContext): Future[MonitorRequest] = request match {
    case msg: TextMessage =>
      val content = msg.toStrict(5.seconds)
      logger.info("Got content: " + content)
      content.map { c =>
        val text = c.text
        val json = parse(text, useBigDecimalForDouble = false, useBigIntForLong = false)
        val jsonRpcRequest = json.extract[JsonRpcRequest[JValue]]
        jsonRpcRequest.method match {
          case "start" =>
            jsonRpcRequest.params.extract[Start]
          case "stop" =>
            jsonRpcRequest.params.extract[Stop]
          case "route" =>
            jsonRpcRequest.params.extract[Route[JsonRpcRequest[JValue]]]
        }
      }
    case bin: BinaryMessage =>
      bin.dataStream.runWith(Sink.ignore)
      Future.failed(new UnsupportedOperationException("Binary messages not supported"))
  }

  private def encodeResponse(response: MonitorResponse): ws.Message = {
    val jsonRpcResponse = JsonRpcResponse.result(response)
    val encoded = write(jsonRpcResponse)
    TextMessage.Strict(text = encoded)
  }

  private def sendTo(session: WebsocketSession, response: AnyRef): Unit = {
    val encoded = write(response)
    val msg = TextMessage.Strict(text = encoded)
    session.send(msg)
  }
}

object WsCommandServer {
  def props(supervisor: ActorRef, host: String, port: Int): Props = Props(new WsCommandServer(supervisor, host, port))
}