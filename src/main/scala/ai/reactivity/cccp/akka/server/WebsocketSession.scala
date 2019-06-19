package ai.reactivity.cccp.akka.server

import akka.http.scaladsl.model.ws
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source, SourceQueueWithComplete}
import akka.{Done, NotUsed}

import scala.concurrent.Future

class WebsocketSession(onRequest: (WebsocketSession, ws.Message) => Unit, onDisconnect: WebsocketSession => Unit) {

  private var maybeResponseQueue: Option[SourceQueueWithComplete[ws.Message]] = None

  val responseSrc: Source[ws.Message, SourceQueueWithComplete[ws.Message]] = Source.queue[ws.Message](4, OverflowStrategy.backpressure).mapMaterializedValue(q => {
    this.maybeResponseQueue = Some(q); q // what a hack!
  })

  val requestSink: Sink[ws.Message, Future[Done]] = Sink.foreach(onRequest(this, _))

  val flow: Flow[ws.Message, ws.Message, NotUsed] = Flow.fromSinkAndSource(requestSink, responseSrc).alsoTo(Sink.onComplete(_ => onDisconnect(this)))

  def send(response: ws.Message): Unit = maybeResponseQueue.foreach(_.offer(response))
}
