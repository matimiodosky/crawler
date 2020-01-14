package actors

import akka.actor.Actor
import messages.NewVertex
import util.Configuration
import org.jgrapht._
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.{DefaultDirectedGraph, DefaultEdge}

import scala.collection.mutable
import scala.jdk.CollectionConverters._


class GraphManager extends Actor {

  val graph: Graph[String, DefaultEdge] = new DefaultDirectedGraph[String, DefaultEdge](new DefaultEdge().getClass)


  override def receive: Receive = {


    case NewVertex(from, to) =>

      graph.addVertex(to)
      graph.addVertex(from)
      graph.addEdge(from, to)

      if (to.equals(Configuration.getConfig("endPage"))) {
        println("Found path!!!!")
        println(graph.vertexSet().size() + " pages parsed!!!")
        val dijskstra = new DijkstraShortestPath[String, DefaultEdge](graph)
        val argPaths = dijskstra.getPaths(Configuration.getConfig("initialPage"))
        argPaths
          .getPath(Configuration.getConfig("endPage"))
          .getVertexList
          .asScala
          .zipWithIndex
          .foreach(indexedElem => println(indexedElem._1+" "+indexedElem._2))

      }

  }

}
