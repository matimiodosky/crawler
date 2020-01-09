package actors

import java.io.{File, PrintWriter}

import akka.actor.{Actor, ActorRef}
import messages.{DumpRequest, DumpResponse, Tick}

case class Dumper(crawler: ActorRef) extends Actor {

  override def receive: Receive = {

    case Tick() => crawler ! DumpRequest()

    case DumpResponse(urls) =>
      println("Dumping...")
      val file = new File("dump_" + System.currentTimeMillis())
      val out = new PrintWriter(file)
      urls.foreach(out.println)
      out.close()
      println("Dumped...")
  }

}
