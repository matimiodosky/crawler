package actors.history

import akka.actor.Actor

abstract class History extends Actor {


}

object HistoryProvider {

  def getHistory(string: String): History = string match{
    case "inMemory" => InMemoryHistory()
    case "mongo" => MongoHistory()
  }
}
