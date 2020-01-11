package util

trait URLConsumer {

  def onNewURL(url: String): Unit

}

object PrinterConsumer extends URLConsumer {

  import URLUtil.getHost
  override def onNewURL(url: String): Unit = println(getHost(url) + " " + url)

}

object NoOpConsumer extends URLConsumer {

  override def onNewURL(url: String): Unit = {}

}

object URLConsumerProvider {

  def getConsumer(string: String): URLConsumer = string match{

    case "print" => PrinterConsumer
    case "noOperation" => NoOpConsumer

  }

}

