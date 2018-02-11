package monitor.utils

import java.util.Properties

class Config(resourceFile: String) {
  val properties = new Properties()
  properties.load(ClassLoader.getSystemResourceAsStream(resourceFile))

  def get(key: String): String = properties.getProperty(key)
}
