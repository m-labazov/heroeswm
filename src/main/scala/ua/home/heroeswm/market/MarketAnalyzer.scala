package ua.home.heroeswm.market

import akka.actor.{Actor, ActorRef, Props}
import ua.home.heroeswm.market.MarketAnalyzer.Init
import ua.home.heroeswm.market.MarketItemCollector.CollectItems

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * Created by Maksym on 5/16/2016.
  */
class MarketAnalyzer() extends Actor{

  val collector: ActorRef = context.actorOf(Props[MarketItemCollector], "collector")
  override def receive: Receive = {
    case Init => context.system.scheduler.schedule(0 seconds, 5 minutes, collector, CollectItems)
//      collector ! CollectItems
    case _ => println("Unknown command: ")
  }
}

object MarketAnalyzer {
  case object Init
}
