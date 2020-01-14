package actors

import akka.actor.Actor
import messages.NewVertex

class Graph extends Actor{

  var graph: List[(String, String)] = List()

  override def receive: Receive = {

    case NewVertex(from, to) => {
      if(to.equals("https://es.wikipedia.org/wiki/Cuba")){
        println("llegue a cuba")
      }
      if(graph.size > 1000){
//        println("pase los mil")
      }
      graph = (from, to) :: graph
    }

  }

}
