import java.io.{ByteArrayInputStream, InputStream}

import monitor.ForecastTemplatizer
import monitor.models.{Forecast, Temperature}
import org.specs2.mutable._

import scala.xml.Elem

class ForecastTemplatizerTest extends Specification {
  "ForecastTemplatizer" should {
    "add forecast" in {
      val forecast = Forecast(
        location = "Helsinki",
        temperatures = Seq(
          new Temperature(265, "2018-02-13"),
          new Temperature(218, "2018-02-14"),
          new Temperature(295, "2018-02-15"),
          new Temperature(222, "2018-02-16"),
          new Temperature(253, "2018-02-17")
        ),
        monitoredTemperature = -6d,
        Some("critical")
      )

      val templatizer = new ForecastTemplatizerStub().addForecast(forecast)
      val htmlContent = templatizer.render()

      htmlContent must contain ("2018-02-13")
      htmlContent must contain ("HELSINKI")
      htmlContent must contain ("CRITICAL")
    }
  }
}

class ForecastTemplatizerStub(override val forecasts: Seq[Elem] = Nil) extends ForecastTemplatizer(forecasts) {
  override val htmlInputStream: InputStream =
    new ByteArrayInputStream("<div>@forecasts</div>".getBytes)

  override def addForecast(forecast: Forecast): ForecastTemplatizerStub = {
    new ForecastTemplatizerStub(super.addForecast(forecast).forecasts)
  }
}
