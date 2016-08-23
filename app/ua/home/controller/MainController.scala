package ua.home.controller

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.Logging
import com.google.inject.Inject
import play.api.libs.concurrent.Akka
import play.api.mvc._
import ua.home.heroeswm.market.MarketAnalyzer.DisplayAllTypes
import ua.home.heroeswm.market.{ActorNames, MarketAnalyzer}

import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import play.api.Logger
import play.api.libs.json._
import ua.home.heroeswm.market.model.ItemType

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

  implicit val itemTypeReads : Writes[ItemType] = Writes[ItemType] { itemType => JsObject(Seq(
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

//  implicit def listWrites[T](implicit fmt: Writes[T]): Writes[List[T]] = new Writes[List[T]] {
//    def writes(ts: List[T]) = JsArray(ts.map(t => toJson(t)(fmt)))
//  }

//  implicit object ItemTypeFormat extends Format[ItemType] {
//    def reads(json: JsValue) : ItemType = new ItemType(
//      (json \ "category").as[String],
//      (json \ "name").as[String],
//      (json \ "url").as[String]
//    )
//
//    def writes(itemType: ItemType) = JsObject(Seq(
//      "category" -> JsString(itemType.category),
//      "name" -> JsString(itemType.name),
//      "url" -> JsString(itemType.url)
//    ))
//  }

}
