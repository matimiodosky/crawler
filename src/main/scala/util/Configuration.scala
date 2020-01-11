package util

import java.io.FileInputStream
import java.util.Properties

object Configuration {

  val prop = new Properties()
  prop.load(new FileInputStream("application.properties"))

  def getConfig(config: String): String = prop.getProperty(config)

}
