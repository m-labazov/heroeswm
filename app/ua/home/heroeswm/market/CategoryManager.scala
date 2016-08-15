package ua.home.heroeswm.market

import java.util

import akka.actor.{Actor, ActorRef, ActorSelection, Props}
import akka.actor.Actor.Receive
import akka.event.Logging
import org.htmlcleaner.{ContentNode, HtmlCleaner, TagNode}
import ua.home.heroeswm.market.repository.DataStorageActor

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.collection.mutable
import scala.xml.{Elem, NodeSeq, XML}
import scalaj.http.{Http, HttpResponse}
import akka.pattern.ask
import akka.util.Timeout
import ua.home.heroeswm.SystemAuthTokener
import ua.home.heroeswm.SystemAuthTokener.GetAuthToken
import ua.home.heroeswm.market.CategoryManager.GetItemTypes
import ua.home.heroeswm.market.model.{ItemType, MarketItem}
import ua.home.heroeswm.market.repository.DataStorageActor.{LoadItemTypes, SaveItemTypes}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by Maksym on 5/23/2016.
  */
class CategoryManager extends Actor {
  val log = Logging(context.system, this)
  val storageActor: ActorSelection = context.actorSelection(ActorNames.STORAGE_ACTOR)
  val authTokener: ActorRef = SystemAuthTokener.getRef()
//  val storageActor = context.actorOf(Props[DataStorageActor])

  val itemCategoryPattern = ".*cat=([0-9a-zA-Z_]+)&.*".r
  val itemNamePattern = ".*art_type=([0-9a-zA-Z_]+).*".r

  implicit val timeout : Timeout = 15 second

  def receive: Receive = {
    case GetItemTypes => getItemTypes(sender)
  }

  def getItemTypes(sender: ActorRef): Unit = {

    val itemTypesResponse: Future[Any] = storageActor ? LoadItemTypes
    for {itemTypesFuture <- itemTypesResponse
         itemTypes <- itemTypesFuture.asInstanceOf[Future[List[String]]]
    } {
      if (itemTypes.isEmpty) {
        for {token <- authTokener ? GetAuthToken} {
          val itemsList: List[ItemType] = getCategoryList(token.toString)
          storageActor ! SaveItemTypes(itemsList)
          sender ! itemsList
        }
      } else {
        sender ! itemTypes
      }
    }
//    sender ! List()
  }

  def getCategoryList(token: String) : List[ItemType] = {
    val response: HttpResponse[String] = Http("http://www.heroeswm.ru/auction.php").header("Cookie", token).asString
    if (response.code != 200) {
      log.info("There are problems with authentication")
    }
    val cleanHtml: TagNode = new HtmlCleaner().clean(response.body)
    val scripts: Array[AnyRef] = cleanHtml.evaluateXPath("""body/center/table/tbody/tr/td/table/tbody/tr/td/script""")
    val itemsList = scripts
      .map(_.asInstanceOf[TagNode].getAllChildren.asScala.toList)
      .flatten
      .map(_.asInstanceOf[ContentNode].getContent)
      .filter(url => url.startsWith("""<a href="/""") && url.contains("art_type"))
      .map(ahref => {val url = ahref.replace("""<a href="""","").replace("""">""","")
            val itemCategoryPattern(category) = url
            val itemNamePattern(name) = url
            new ItemType(category, name, url)
      })
      .toList

    log.info("Different items were downloaded: {}", itemsList.size)
    itemsList
  }
}
object CategoryManager {
  case object GetItemTypes
}
