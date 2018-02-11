package monitor

import java.io.InputStream
import monitor.models.Forecast

import scala.xml.Elem

case class ForecastTemplatizer(forecasts: Seq[Elem] = Nil) {
  val htmlInputStream: InputStream = ClassLoader.
    getSystemClassLoader.getResourceAsStream("log-viewer.html")

  def addForecast(forecast: Forecast): ForecastTemplatizer = {
    val monitoredTemp =
      <span class="temp">
        {forecast.monitoredTemperature.formatted("%.1f")}
        <sup>o</sup>c <strong><small>LIMIT</small></strong>
      </span>

    val forecastedTemps = forecast.temperatures.map { temp =>
      <div class="temp">
        <span>{temp.asCelsius.formatted("%.1f")}<sup>o</sup>c</span>
        <label>{temp.dateString}</label>
      </div>
    }

    val elem =
      <div class={"forecast " + forecast.status.get.toLowerCase}>
        <span class="city">{forecast.location.toUpperCase}</span>
        <div class="body">
          <div class="temps">
            {forecastedTemps}
          </div>
          <div class="monitored-temps">
            {monitoredTemp}
          </div>
        </div>
        <span class="status">{forecast.status.get.toUpperCase}</span>
      </div>

    copy(forecasts = forecasts :+ elem)
  }

  def render(): String = {
    try {
      val htmlContent = scala.io.Source.fromInputStream(htmlInputStream).mkString

      htmlContent.replaceFirst(
        "@forecasts",
        forecasts.map(_.toString).mkString
      )
    } catch {
      case e: Exception =>
        e.printStackTrace()
        ""
    } finally {
      if (htmlInputStream != null) {
        htmlInputStream.close()
      }
    }
  }
}
