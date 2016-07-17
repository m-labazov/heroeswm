package ua.home.heroeswm.market

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import ua.home.heroeswm.SystemAuthTokener
import ua.home.heroeswm.SystemAuthTokener.GetAuthToken
import ua.home.heroeswm.market.CategoryManager.GetItemTypes
import ua.home.heroeswm.market.CollectItemWorker.Collect
import ua.home.heroeswm.market.MarketItemCollector.{CollectItems, CollectedResult}
import ua.home.heroeswm.market.model.{ItemType, MarketItem}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * Created by Maksym on 5/16/2016.
  */
object MarketItemCollector {
  case object CollectItems
  case object CategoriesLoaded
  case class CollectedResult(items: Array[MarketItem])

}

class MarketItemCollector extends Actor{
  val log = Logging(context.system, this)

  val categoryManager: ActorRef = context.actorOf(Props[CategoryManager])
  val collectItemWorker: ActorRef = context.actorOf(Props[CollectItemWorker])

  implicit val timeout : Timeout = 15 second

  override def receive: Receive = {
    case CollectItems => collectItems()
    case CollectedResult(items) => log.error("We are here!")
    case _ => println("Wrong message!")
  }

  def collectItems() = {
    for { categories <- categoryManager ? GetItemTypes}{
      val categoryList = categories.asInstanceOf[List[ItemType]]
      if (categoryList.isEmpty) {
        log.info("Can't collect data, because there are no items")
      } else {
        categoryList.map(collectItemWorker ! Collect(_))
      }
    }
  }
}

