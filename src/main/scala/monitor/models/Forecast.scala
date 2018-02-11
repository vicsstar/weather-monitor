package monitor.models

import monitor.WeatherWatcher.Status

case class Forecast(
                     location: String,
                     temperatures: Seq[Temperature] = Nil,
                     monitoredTemperature: Double = 0d,
                     status: Option[Status] = None)
