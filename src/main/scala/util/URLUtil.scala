package util

import java.net.URI

object URLUtil {

  def getHost(url: String): String = {
    try {
      val uri = new URI(url)
      val domain = uri.getHost
      if (domain.startsWith("www.")) domain.substring(4) else domain
    } catch {
      case _: Exception => ""
    }
  }

}
