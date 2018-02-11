package monitor.models

class Temperature(val kelvin: Double, val dateString: String) {
  /**
    * Converts kelvin temperature to celsius.
    * @return Double, the temperature in celsius.
    */
  lazy val asCelsius: Double = kelvin - 273.15
}
