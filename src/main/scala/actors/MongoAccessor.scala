package actors

import org.mongodb.scala.{Completed, Document, MongoClient, MongoClientSettings, MongoCollection, MongoDatabase, Observer, ServerAddress}
import scala.jdk.CollectionConverters._


trait MongoAccessor {

  val mongoClient: MongoClient = MongoClient()

  val settings: MongoClientSettings = MongoClientSettings.builder()
    .applyToClusterSettings(b => b.hosts(List(new ServerAddress("localhost")).asJava))
    .build()

  val database: MongoDatabase = mongoClient.getDatabase("crawler")

  val collection: MongoCollection[Document] = database.getCollection("visited")

  val noOpObserver: Observer[Completed] = new Observer[Completed] {

    override def onNext(result: Completed): Unit = {
      //      println(result)
    }

    override def onError(e: Throwable): Unit = {
      //      println(e)
    }

    override def onComplete(): Unit = {
      //      println("completed")
    }
  }
}
