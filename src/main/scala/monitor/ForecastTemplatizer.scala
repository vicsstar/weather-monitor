package monitor

import java.net.URL

import monitor.models.Forecast

import scala.xml.Elem

case class ForecastTemplatizer(
                                forecasts: Seq[Elem] = Nil,
                                watchedTemperatures: Seq[Elem] = Nil) {
  lazy val htmlFile: URL = ClassLoader.
    getSystemClassLoader.getResource("log-viewer.html")

  def addWatchedTemperature(temp: Double): ForecastTemplatizer = {
    val elem = <span class="temp">{temp.formatted("%.2f")}<sup>o</sup>c</span>
    copy(watchedTemperatures = watchedTemperatures :+ elem)
  }

  def addForecast(forecast: Forecast): ForecastTemplatizer = {
    val temps = forecast.temperatures.map { temp =>
      <div class="temp">
        <span>{temp.asCelsius.formatted("%.1f")}<sup>o</sup>c</span>
        <label>{temp.dateString}</label>
      </div>
    }

    val elem =
      <div class={"forecast " + forecast.status.get.toLowerCase}>
        <span class="city">{forecast.location.toUpperCase}</span>
        <div class="body">
          {temps}
        </div>
        <span class="status">{forecast.status.get.toUpperCase}</span>
      </div>

    copy(forecasts = forecasts :+ elem)
  }

  def render(): String = {
    val inputStream = htmlFile.openStream()

    try {
      val htmlContent = scala.io.Source.fromInputStream(inputStream).mkString

      htmlContent.replaceFirst(
        "@monitoredTemps",
        watchedTemperatures.map(_.toString).mkString
      ).replaceFirst(
        "@forecasts",
        forecasts.map(_.toString).mkString
      )
    } catch {
      case _: Exception => ""
    } finally {
      if (inputStream != null) {
        inputStream.close()
      }
    }
  }
}
