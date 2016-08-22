import akka.actor.Props
import controllers.{Assets, AsyncController, CountController, HomeController}
import play.api.ApplicationLoader.Context
import play.api.cache.EhCacheComponents
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext, Configuration, LoggerConfigurator}
import reactivemongo.api.MongoDriver
import ua.home.configuration.Configurator
import ua.home.controller.MainController
import ua.home.heroeswm.SystemAuthTokener
import ua.home.heroeswm.market.MarketAnalyzer.Init
import ua.home.heroeswm.market.{ActorNames, MarketAnalyzer}
import ua.home.heroeswm.market.repository.DataStorageActor

import router.Routes

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

class AppLoader extends ApplicationLoader {

  implicit val ec: ExecutionContext = play.api.libs.concurrent.Execution.defaultContext

  override def load(context: Context): Application = {
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment)
    }


    val components: AppComponents = new AppComponents(context)

    components.application
  }

}

class AppComponents(context: Context)(implicit val ec: ExecutionContext) extends BuiltInComponentsFromContext(context)
  with EhCacheComponents {

  val config = context.initialConfiguration.underlying

  implicit val scheduler = actorSystem.scheduler

  class Application extends Configurator {
    configure {
      val driver = new MongoDriver
      val connection = driver.connection(List("localhost"))
      val db = connection.database("heroeswm")
      Await.result(db, 10 seconds)
    }
  }
  new Application
  actorSystem.actorOf(Props[DataStorageActor], ActorNames.STORAGE_ACTOR_NAME)
  actorSystem.actorOf(Props[SystemAuthTokener], ActorNames.AUTH_ACTOR_NAME)
  // default Actor constructor
  //  val collector: ActorRef = system.actorOf(Props[MarketItemCollector])
  val marketAnalyzer = actorSystem.actorOf(Props[MarketAnalyzer])

  val collectData = context.initialConfiguration.getBoolean("heroeswn.collect.data")

  if (collectData.getOrElse(false)) {
    marketAnalyzer ! Init
  }

  lazy val homeController = new HomeController
  lazy val countController = new CountController(services.AtomicCounter)
  lazy val asyncController = new AsyncController(actorSystem)
  lazy val mainController = new MainController(actorSystem)

  lazy val assetsController: Assets = new Assets(httpErrorHandler)

  // order matters - should be the same as routes file
  lazy val router = new Routes(
    httpErrorHandler,
    homeController,
    countController,
    asyncController,
    assetsController,
    mainController)

}

