package monitor

import java.io.File

import monitor.models.{Forecast, Temperature}

object ForecastLogReader {
  val userHome = sys.env("HOME")
  lazy val logFile: File = new File(userHome, Forecast.LOG_FILENAME)

  def read(): Seq[Forecast] = {
    val temperatureLog = scala.io.Source.fromFile(logFile).mkString

    temperatureLog.split("\n").map { forecastString =>
      forecastString.split('|').toList match {
        case location :: temps :: monitoredTemps :: alertStatus :: Nil =>
          Forecast(
            location,
            temps.split(",").map { temp =>
              val t = temp.split(" ")
              new Temperature(t(0).toDouble + 273.15, t(1))
            },
            monitoredTemps.split(",").map(_.toDouble),
            Some(alertStatus)
          )
      }
    }
  }
}
