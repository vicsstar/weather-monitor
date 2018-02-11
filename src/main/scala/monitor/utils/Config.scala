package monitor.utils

import java.util.Properties

class Config(resourceFile: String) {
  val properties = new Properties()
  properties.load(getClass.getResourceAsStream(resourceFile))

  def get(key: String): String = properties.getProperty(key)
}
