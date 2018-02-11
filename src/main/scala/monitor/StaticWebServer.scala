package monitor

import java.io.{OutputStreamWriter, PrintWriter}
import java.net.{ServerSocket, Socket}

import akka.actor.{Actor, ActorSystem, Props}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

/**
  * This static web server was built just for this purpose.
  * The requirements for the coding challenge needs only a single static page rendered.
  * No need to use too many dependencies at this point.
  */
class StaticWebServer extends Actor {

  override def receive: Receive = {
    case WaitForConnection(serverSocket, actorSystem) =>
      val socket = serverSocket.accept()

      actorSystem.scheduler.scheduleOnce(0.millis,
        actorSystem.actorOf(Props(RequestHandler)), HandleConnection(socket))

      self ! WaitForConnection(serverSocket, actorSystem)
    case _ =>
  }
}

object RequestHandler extends Actor with Headers {
  override def receive: Receive = {
    case HandleConnection(socket: Socket) => handleRequest(socket)
    case _ =>
  }

  def handleRequest(socket: Socket): Unit = {
    val reader = scala.io.Source
      .fromInputStream(socket.getInputStream)
      .bufferedReader()

    if (reader.readLine().contains("GET / ")) {
      implicit val writer: PrintWriter =
        new PrintWriter(new OutputStreamWriter(socket.getOutputStream))

      try {
        val forecasts = ForecastLogReader.read()

        val templatizer = ForecastTemplatizer()
        // add all forecasted and monitored temperatures.
        val template = forecasts.foldRight(templatizer)(
          (forecast, _templatizer) => _templatizer.addForecast(forecast)
        )

        // render the templatizer's HTML template after interpolating its data.
        val htmlBody = template.render()
        setHeaders(htmlBody.length)
        writer.println(htmlBody)
        writer.flush()
      } finally {
        if (writer != null) writer.close()
        socket.close()
      }
    } else {
      if (reader != null) reader.close()
      socket.close()
    }
  }
}

trait Headers {
  def setHeaders(contentLength: Int)(implicit writer: PrintWriter): Unit = {
    writer.println("HTTP/1.1 200 OK")
    writer.println("Content-Type: text/html")
    writer.println(s"Content-Length: $contentLength")
    writer.println()
  }
}

case class WaitForConnection(serverSocket: ServerSocket, actorSystem: ActorSystem)
case class HandleConnection(socket: Socket)
