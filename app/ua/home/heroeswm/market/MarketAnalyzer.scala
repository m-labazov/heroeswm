package ua.home.heroeswm.market

import akka.actor.{Actor, ActorRef, ActorSelection, Props}
import akka.event.Logging
import ua.home.heroeswm.market.MarketAnalyzer.{DisplayItems, DisplayAllTypes, Init}
import ua.home.heroeswm.market.MarketItemCollector.CollectItems
import ua.home.heroeswm.market.repository.DataStorageActor.{LoadItemTypes, LoadItems}
import akka.pattern.{ask, pipe}
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * Created by Maksym on 5/16/2016.
  */
class MarketAnalyzer() extends Actor{

  implicit val timeout:Timeout = 15 second

  val log = Logging(context.system, this)

  val storageActor: ActorSelection = context.actorSelection(ActorNames.STORAGE_ACTOR)
  val collector: ActorRef = context.actorOf(Props[MarketItemCollector], "collector")
  override def receive: Receive = {
    case Init => context.system.scheduler.schedule(0 seconds, 5 minutes, collector, CollectItems)
//      collector ! CollectItems
    case DisplayItems(itemType) => pipe(storageActor ? LoadItems(itemType)) to sender
    case DisplayAllTypes => pipe(storageActor ? LoadItemTypes) to sender
    case _ => println("Unknown command: ")
  }
}

object MarketAnalyzer {
  case object Init
  case class DisplayItems(itemType: String)
  case object DisplayAllTypes


}
