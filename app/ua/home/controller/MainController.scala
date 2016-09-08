package ua.home.controller

import java.time.LocalTime

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.Logging
import com.google.inject.Inject
import play.api.libs.concurrent.Akka
import play.api.mvc._
import ua.home.heroeswm.market.MarketAnalyzer.{DisplayAllTypes, DisplayItems}
import ua.home.heroeswm.market.{ActorNames, MarketAnalyzer}

import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import play.api.Logger
import play.api.libs.json._
import ua.home.heroeswm.market.model.{ItemType, MarketItem}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}


/**
  * Created by Maksym on 7/24/2016.
  */
class MainController (system: ActorSystem) extends Controller{

  implicit val timeout:Timeout = 30 second

//  val log = Logging(system, this)

  def hello = Action { system.actorSelection(ActorNames.STORAGE_ACTOR)
    Ok("abc")  }

  def getTypes = Action.async {
    val marketAnalyzer: ActorRef = system.actorOf(Props[MarketAnalyzer])
    for {
      resultFuture <- marketAnalyzer ? DisplayAllTypes
      result <- resultFuture.asInstanceOf[Future[List[ItemType]]]
    } yield {
      Logger.error("controller: " + result toString)
      Ok(Json.toJson(result))
    }
  }

  def getItems(itemType : String) = Action.async {
    val marketAnalyzer: ActorRef = system.actorOf(Props[MarketAnalyzer])
    for {
      resultFuture <- marketAnalyzer ? DisplayItems(itemType)
      result <- resultFuture.asInstanceOf[Future[List[MarketItem]]]
    } yield {
      Logger.error("controller: " + result toString)
      Ok(Json.toJson(result))
    }
  }

  implicit val itemTypeWrites : Writes[ItemType] = Writes[ItemType] { itemType => JsObject(Seq(
      "category" -> JsString(itemType.category),
      "name" -> JsString(itemType.name),
      "url" -> JsString(itemType.url)
    ))
  }

  implicit val itemTypeListFormat : Writes[List[ItemType]] = Writes[List[ItemType]] { list =>
    JsObject(Map(
      "types" -> JsArray(list.map(itemType => Json.toJson(itemType))
    )))
  }

  implicit val marketItemsWrites : Writes[MarketItem] = Writes[MarketItem] { marketItem => JsObject(Seq(
          "itemType" -> JsString(marketItem.itemType),
          "price" -> JsNumber(marketItem.price),
          "currentDurability" -> JsNumber(marketItem.currentDurability),
          "lotId" -> JsNumber(marketItem.lotId),
          "maxDurability" -> JsNumber(marketItem.maxDurability),
          "name" -> JsString(marketItem.name),
          "time" -> JsString(marketItem.time toString)
    ))
  }

  implicit val marketItemListFormat : Writes[List[MarketItem]] = Writes[List[MarketItem]] { list =>
    JsObject(Map(
      "items" -> JsArray(list.map(marketItem => Json.toJson(marketItem))
    )))
  }

}
