package util.newURLStrategy

trait URLConsumer {

  def onNewURL(url: String): Unit

}

object PrinterConsumer extends URLConsumer {

  override def onNewURL(url: String): Unit = println(url)

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

