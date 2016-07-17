package ua.home.heroeswm

import akka.actor.{ActorSystem, Props}
import reactivemongo.api.MongoDriver
import ua.home.configuration.Configurator
import ua.home.heroeswm.market.MarketAnalyzer.Init
import ua.home.heroeswm.market.repository.DataStorageActor
import ua.home.heroeswm.market.{ActorNames, MarketAnalyzer}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * Created by Maksym on 5/16/2016.
  */
object MainClass extends App {
  val system = ActorSystem("HeroeswmMarket")

  class Application extends Configurator {
    configure {
      val driver = new MongoDriver
      val connection = driver.connection(List("localhost"))
      val db = connection.database("heroeswm")
      Await.result(db, 10 seconds)
    }
  }
  new Application
  system.actorOf(Props[DataStorageActor], ActorNames.STORAGE_ACTOR_NAME)
  system.actorOf(Props[SystemAuthTokener], ActorNames.AUTH_ACTOR_NAME)
  // default Actor constructor
//  val collector: ActorRef = system.actorOf(Props[MarketItemCollector])
  val marketAnalyzer = system.actorOf(Props[MarketAnalyzer])
  marketAnalyzer ! Init

}
