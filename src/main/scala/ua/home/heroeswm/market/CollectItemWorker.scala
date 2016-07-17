package ua.home.heroeswm.market

import java.time.LocalTime

import akka.actor.{Actor, ActorRef, ActorSelection}
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import org.htmlcleaner.{ContentNode, HtmlCleaner, TagNode}
import ua.home.heroeswm.SystemAuthTokener
import ua.home.heroeswm.SystemAuthTokener.GetAuthToken
import ua.home.heroeswm.market.CollectItemWorker.Collect
import ua.home.heroeswm.market.MarketItemCollector.CollectedResult
import ua.home.heroeswm.market.model.{ItemType, MarketItem}
import ua.home.heroeswm.market.repository.DataStorageActor.SaveItems

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scalaj.http.{Http, HttpRequest, HttpResponse}
import akka.pattern.pipe
import com.sun.javafx.iio.ImageStorage.ImageType

/**
  * Created by Maksym on 6/2/2016.
  */
class CollectItemWorker extends Actor {
  val log = Logging(context.system, this)
  val itemTypePattern = ".*art_type=([0-9a-zA-Z_]+).*".r
  var i : Int = 0

  val storageActor: ActorSelection = context.actorSelection(ActorNames.STORAGE_ACTOR)
  val authTokener: ActorRef = SystemAuthTokener.getRef()

  implicit val timeout:Timeout = 15 second

  override def receive: Receive = {
    case Collect(itemType) => collect(itemType, sender)
  }
  def collect(itemType: ItemType, collector : ActorRef): Unit = {
    for (token <- authTokener ? GetAuthToken) {
      val itemTypePattern(name) = itemType.url
      val request: HttpRequest = Http("http://www.heroeswm.ru" + itemType.url).header("Cookie", token.asInstanceOf[String])
//      log.info("Item was loaded: " + itemUrl)
      val response: HttpResponse[String] = request.asString
      val cleanHtml: TagNode = new HtmlCleaner().clean(response.body)
      val items: Array[AnyRef] = cleanHtml.evaluateXPath("body/center/table/tbody/tr/td/table/tbody/tr/td/table/tbody/tr[@class='wb']")
      val result = items.map(getItem(itemType.url, name))
      log.info("{} items collected: {}", itemType, result.size)
      if (!result.isEmpty) {
        storageActor ! SaveItems(result)
      }
      collector ! CollectedResult(result)
    }
  }

  def getItem(itemUrl: String, itemType: String): (AnyRef) => MarketItem = {
    tagNode => {
      val tag = tagNode.asInstanceOf[TagNode]
      val price: String = getPrice(itemUrl, tag)
      val name = getChildByIdAndXPath(tag, "(td/table/tbody/tr/td)[2]", 1).toString
      //        log.info("name {} was received for {}", name, itemUrl)
      val lotId = tag.evaluateXPath("((td/table/tbody/tr/td)[2])/a/text()")(0).toString.replace("#", "")
      val priceInt: Int = Integer.parseInt(price.asInstanceOf[String].replace(",", ""))
      //        log.info("lotId {} was received for {}", lotId, itemUrl)
      val marketItem = new MarketItem(itemType, priceInt, 0, 0, name, lotId.toInt, LocalTime.now())
      marketItem
    }
  }

  def getPrice(itemUrl: String, tag: TagNode): String = {
    val price = getFirstChildByXPath(tag, "(td/table/tbody/tr/td/table[@border=\"0\"]/tbody/tr/td)[2]")
    if (price.isInstanceOf[String]) {
      log.debug("price {} was received for {}", price, itemUrl)
      price.toString
    } else if (price.isInstanceOf[ContentNode]) {
      val contentPrice = price.asInstanceOf[ContentNode].getContent
      log.debug("contentprice {} was received for {}", contentPrice, itemUrl)
      contentPrice
    } else {
      val alternativePrice = getFirstChildByXPath(tag, "(td/table/tbody/tr/td/table[@border=\"0\"]/tbody/tr/td)[last()]")
      log.debug("alternativeprice {} was received for {}", alternativePrice, itemUrl)
      alternativePrice.toString
    }
  }

  def getFirstChildByXPath(tag: TagNode, xPath: String): Any = {
    getChildByIdAndXPath(tag, xPath, 0)
  }
  def getChildByIdAndXPath(tag: TagNode, xPath: String, id: Int) : Any = {
    val tags: Array[AnyRef] = tag.evaluateXPath(xPath)
    if (tags.isEmpty) {
      log.info("xPath is bad {}", xPath)
    }
    val path: AnyRef = tags(0)
    if (path == null) {
      log.info("xPath is bad {}", xPath)
    }
    val node: TagNode = path.asInstanceOf[TagNode]
    val value: Any = node.getAllChildren.get(id)
    value
  }
}
object CollectItemWorker {
  case class Collect(itemUrl: ItemType)
}
