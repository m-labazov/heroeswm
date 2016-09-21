package ua.home.heroeswm.market.repository

import java.time.LocalTime

import akka.actor.{Actor, ActorRef}
import akka.actor.Actor.Receive
import reactivemongo.api.BSONSerializationPack.IdentityWriter
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{DefaultDB, MongoDriver}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, DefaultBSONHandlers}
import ua.home.configuration.ConfiguredContext
import ua.home.heroeswm.market.model.{ItemType, MarketItem}
import reactivemongo.bson._
import ua.home.heroeswm.market.repository.DataStorageActor.{LoadItemTypes, LoadItems, SaveItemTypes, SaveItems}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by Maksym on 6/12/2016.
  */
class DataStorageActor extends Actor with ConfiguredContext {
  import reactivemongo.bson._
  private val itemTypeCollectionName: String = "itemType"
  private val itemCollectionName: String = "item"

  implicit val itemWriter : ItemWriter = new ItemWriter
  implicit val itemTypeHandler : ItemTypeHandler = new ItemTypeHandler


  override def receive: Receive = {
    case SaveItemTypes(itemTypes) => saveItemType(itemTypes)
    case SaveItems(items) => saveItems(items)
    case LoadItemTypes => sender ! getItemTypes()
    case LoadItems(itemType) => sender ! getItems(itemType)
  }

  def getItems(itemType: String): Future[List[MarketItem]] = {
    val db: DefaultDB = getConfigured[DefaultDB]
    val result = db[BSONCollection](itemCollectionName).
      find(BSONDocument(
        "itemType" -> itemType
      )).
      cursor[MarketItem]().
      collect[List]()


    result
  }

  def getItemTypes(): Future[List[ItemType]] = {
    val db: DefaultDB = getConfigured[DefaultDB]
    val result = db[BSONCollection](itemTypeCollectionName).
      find(BSONDocument()).
      cursor[ItemType]().
      collect[List]()


    result
  }

  def saveItems(items :  Array[MarketItem]) = {
    for (item <- items) {
      val db: DefaultDB = getConfigured[DefaultDB]
      db[BSONCollection](itemCollectionName).insert(item)
    }
  }

  def saveItemType(itemTypes: List[ItemType]): Unit = {
    for (itemType <- itemTypes) {
      val db: DefaultDB = getConfigured[DefaultDB]
      db[BSONCollection](itemTypeCollectionName).insert(itemType)
    }
  }

  class ItemTypeHandler extends BSONDocumentReader[ItemType] with BSONDocumentWriter[ItemType] {
    override def read(bson: BSONDocument): ItemType = {
      val itemUrl: String = bson.get("itemUrl").get.asInstanceOf[BSONString].value
      val category: String = bson.get("category").get.asInstanceOf[BSONString].value
      val name: String = bson.get("name").get.asInstanceOf[BSONString].value
      new ItemType(category, name, itemUrl)
    }

    override def write(itemType: ItemType): BSONDocument =
      BSONDocument("category" -> itemType.category,
        "name" -> itemType.name,
        "itemUrl" -> itemType.url)
  }

  class ItemWriter extends BSONDocumentWriter[MarketItem] with BSONDocumentReader[MarketItem] {
    override def write(item: MarketItem): BSONDocument =
      BSONDocument("itemType" -> item.itemType,
        "price" -> item.price,
        "lotId" -> item.lotId,
        "name" -> item.name,
        "maxDurability" -> item.maxDurability,
        "currentDurability" -> item.currentDurability,
        "time" -> item.time.toString
      )

    override def read(bson: BSONDocument): MarketItem = {
      val itemType: String = bson.get("itemType").get.asInstanceOf[BSONString].value
      val price: Int = bson.get("price").get.asInstanceOf[BSONInteger].value
//      val currentDurability: Int = bson.get("currentDurability").get.asInstanceOf[BSONInteger].value
//      val maxDurability: Int = bson.get("maxDurability").get.asInstanceOf[BSONInteger].value
      val lotId: Int = bson.get("lotId").get.asInstanceOf[BSONInteger].value
//      val time: LocalTime = bson.get("time").get.asInstanceOf[BSONTimestamp].value
//      val name: String = bson.get("name").get.asInstanceOf[BSONString].value
      new MarketItem(itemType, price, currentDurability = 0, maxDurability = 0, name = itemType, lotId, time=LocalTime.now())
    }
  }
}
 object DataStorageActor {
   case class SaveItemTypes(itemTypes : List[ItemType])
   case class SaveItems(items: Array[MarketItem])
   case object LoadItemTypes
   case class LoadItems(itemType: String)
}
