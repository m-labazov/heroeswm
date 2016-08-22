package ua.home.heroeswm.market.repository

import akka.actor.{Actor, ActorRef}
import akka.actor.Actor.Receive
import reactivemongo.api.BSONSerializationPack.IdentityWriter
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{DefaultDB, MongoDriver}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, DefaultBSONHandlers}
import ua.home.configuration.ConfiguredContext
import ua.home.heroeswm.market.model.{ItemType, MarketItem}
import reactivemongo.bson._
import ua.home.heroeswm.market.repository.DataStorageActor.{LoadItemTypes, SaveItemTypes, SaveItems}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by Maksym on 6/12/2016.
  */
class DataStorageActor extends Actor with ConfiguredContext {
  import reactivemongo.bson._
  private val itemtype: String = "itemType"

  override def receive: Receive = {
    case SaveItemTypes(itemTypes) => saveItemType(itemTypes)
    case SaveItems(items) => saveItems(items)
    case LoadItemTypes => sender ! getItemTypes()
  }

  def getItemTypes(): Future[List[ItemType]] = {
    val db: DefaultDB = getConfigured[DefaultDB]
    val result = db[BSONCollection](itemtype).
      find(BSONDocument()).
      cursor[ItemType]().
      collect[List]()


    result
  }

  def saveItems(items :  Array[MarketItem]) = {
    for (item <- items) {
      val db: DefaultDB = getConfigured[DefaultDB]
      db[BSONCollection]("item").insert(item)
    }
  }

  def saveItemType(itemTypes: List[ItemType]): Unit = {
    for (itemType <- itemTypes) {
      val db: DefaultDB = getConfigured[DefaultDB]
      db[BSONCollection](itemtype).insert(itemType)
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

  class ItemWriter extends BSONDocumentWriter[MarketItem] {
    override def write(item: MarketItem): BSONDocument =
      BSONDocument(itemtype -> item.itemType,
        "price" -> item.price,
        "lotId" -> item.lotId)
  }
  implicit val itemWriter : ItemWriter = new ItemWriter
  implicit val urlHeandler : ItemTypeHandler = new ItemTypeHandler
}
 object DataStorageActor {
   case class SaveItemTypes(itemTypes : List[ItemType])
   case class SaveItems(items: Array[MarketItem])
   case object LoadItemTypes
   case object LoadObjects
}
