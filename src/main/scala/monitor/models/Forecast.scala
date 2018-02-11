package monitor.models

import monitor.WeatherWatcher.Status

case class Forecast(
                     location: String,
                     temperatures: Seq[Temperature] = Nil,
                     monitoredTemperatures: Seq[Double] = Nil,
                     status: Option[Status] = None)

object Forecast {
  val LOG_FILENAME: String = "weather-watcher.log"
}
