package monitor

import java.io._
import java.net.{HttpURLConnection, ServerSocket, SocketTimeoutException, URL}

import akka.actor.Props
import monitor.models.{Forecast, Temperature}
import play.api.libs.json.{JsArray, Json}

import scala.concurrent.duration.DurationInt
import scala.concurrent.ExecutionContext.Implicits.global
import utils.{Config, Constants}

object WeatherWatcher extends App with Constants {
  type Status = String

  val config = new Config("config.properties")
  val apiKey = config.get("api_key")
  val periodicCheckTime = config.get("periodic_check_time").toInt

  lazy val readForecasts: Seq[Forecast] = {
    val stdin = scala.io.StdIn

    val locations = stdin.
      readLine("Enter list of locations, separated by commas: ").
      split(",").
      map(loc => Forecast(loc.trim))

    locations.map { forecast =>
      val temperatureString = stdin.readLine(
        s"For ${forecast.location}, enter the temperature limit to listen for (IN CELSIUS): ")

      forecast.copy(monitoredTemperature = temperatureString.trim.toDouble)
    }
  }

  def fetchAndAnalyzeForecasts(forecasts: Seq[Forecast]): Seq[Forecast] = {
    forecasts.map { forecast =>
      //          val forecastData = get(s"http://api.openweathermap.org/data/2.5/forecast?q=$location&mode=json&appid=$apiKey")
      val is = ClassLoader.getSystemClassLoader.getResourceAsStream("sample-forecast.json")
      val forecastData = Json.parse(scala.io.Source.fromInputStream(is).mkString)

      val temperatureList = (forecastData \\ "list").map {
        case JsArray(values) => values map { value =>
          val temperature = (value \ "main" \ "temp").get.toString.toDouble
          val date = (value \ "dt_txt").get.as[String].split(" ")(0)

          new Temperature(temperature, date)
        }
        case _ => Nil
      }.filter(_.nonEmpty).
        flatten

      // analyze the data collected.
      val _temps = temperatureList.groupBy(_.dateString).map {
        // get the lowest of the temperature readings for each day.
        case (_, temps) => temps.minBy(_.asCelsius)
      }.toSeq.sortBy(_.dateString)

      // check if we have a critical weather alert for this forecast.
      val critical = _temps.map(_.asCelsius)
        .exists(t1 => t1 <= forecast.monitoredTemperature)

      forecast.copy(
        temperatures = _temps,
        status = Some(if(critical) "critical" else "normal")
      )
    }
  }

  def saveToLogFile(forecasts: Seq[Forecast]): Unit = {
    val userHome: String = sys.env("HOME")
    val logFile = new File(userHome, LOG_FILENAME)
    val logWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(logFile)))

    try {
      forecasts.foreach {
        case Forecast(location, temperatures, monitoredTemperature, alertStatus) =>
          logWriter.println(
            s"$location|" +
            s"${temperatures.map(t => s"${t.asCelsius.formatted("%.1f")} ${t.dateString}").mkString(",")}|" +
            s"$monitoredTemperature|" +
            s"${alertStatus.get}")
      }
    } finally {
      if (logWriter != null) {
        logWriter.flush()
        logWriter.close()
      }
    }
  }

  def begin(): Unit = {
    val startTime = System.nanoTime()

    try {
      saveToLogFile(
        fetchAndAnalyzeForecasts(
          readForecasts
        )
      )
    } catch {
      case _: IOException =>
      case _: SocketTimeoutException =>
        val stopTime = System.nanoTime()
        println(s"The weather service timed out after ${(stopTime - startTime) / 1000000} seconds.")
      case _: Exception => println("An unknown error occurred.")
    }
  }

  @throws[IOException]("if an I/O or network error occurred.")
  @throws[SocketTimeoutException]("if the network connection times out.")
  def get(url: String, requestMethod: String = "GET"): String = {
    val connection = new URL(url).openConnection().asInstanceOf[HttpURLConnection]
    connection.setConnectTimeout(10000)
    connection.setReadTimeout(10000)
    connection.setRequestMethod(requestMethod)

    val inputStream = connection.getInputStream
    val content = scala.io.Source.fromInputStream(inputStream).mkString

    if (inputStream != null) inputStream.close()

    content
  }

  def spinUpWebServer(): Unit = {
    val port = config.get("port").toInt
    val serverSocket = new ServerSocket(port)

    val actorSystem = akka.actor.ActorSystem.apply("weather-server-actor-system")
    println(s"Web Server is listening at port $port")
    println("Visit this address on your browser - http://127.0.0.1:9000")

    val task = actorSystem.scheduler.scheduleOnce(0.millis,
      actorSystem.actorOf(Props[StaticWebServer]),
      WaitForConnection(serverSocket, actorSystem))

    actorSystem.registerOnTermination(() => {
      task.cancel()
      serverSocket.close()
      actorSystem.terminate()
      println("Web server terminated.")
    })
  }

  def schedulePeriodicChecker(): Unit = {
    // run every 30 minutes.
    actorSystem.scheduler.schedule(0.millis, periodicCheckTime.minutes, () => {
      begin()
    })
  }

  val actorSystem = akka.actor.ActorSystem.apply("main-scheduler")

  actorSystem.scheduler.scheduleOnce(0.millis, () => {
    spinUpWebServer()
    schedulePeriodicChecker()
  })
}
