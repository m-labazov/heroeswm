package ua.home.heroeswm

import java.net.HttpCookie

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import ua.home.heroeswm.SystemAuthTokener.{GetAuthToken, Init, InvalidateAuthToken}

import scalaj.http.{Http, HttpRequest, HttpResponse}

/**
  * Created by Maksym on 5/16/2016.
  */
class SystemAuthTokener extends Actor {
  self ! Init
  var token: Option[String] = Some("__utma=80519182.2134292720.1463320188.1469367989.1471785485.28; __utmz=80519182.1463320188.1.1.utmccn=(direct)|utmcsr=(direct)|utmcmd=(none); _ym_uid=1463320189383481326; hwm_reg_cache=eeb3bb172e9ea729310455d34acc04a5; hwm_reg_user_key=4bf28779f8af70baa062a6b3254b04f9; PHPSESSID=26ffdcda26cbd9fa2c61897268265231; __utmc=80519182; __utmb=80519182; _ym_isad=2; pl_id=95329; duration=13380; sid=74ded8444cc8aa5c1849f7d58cb2e441; l_obj_c=f77f4f5d9165a40cdbc8e1177eebd87d; l_obj=19")
  val log = Logging(context.system, this)

  override def receive: Receive = {
    case Init => SystemAuthTokener.ref = self
    case GetAuthToken => sender ! token.getOrElse(authenticate)
    case InvalidateAuthToken => token = Option.empty
    case _ => log.error("Wrong message")
  }

  def authenticate: String = {

//    val form: HttpRequest = Http("http://www.heroeswm.ru/login.php").postForm(Seq("login" -> "Rendal", "pass" -> "72918666"))
//    log.info("Authentication request returned parameters: [{}]", form.asString)
//    val form2: HttpRequest = Http("http://www.heroeswm.ru/auction.php?cat=cuirass&sort=0&art_type=hauberk")
//    log.info("Response body: {}", form2.header("Cookie", token.get).asString)
//    val value: String = form.asString.cookies.filter(_.getName == "sid")(0).getValue
//    token = Some(value)
//    value
    "token"
  }
}
object SystemAuthTokener {
  def getRef(): ActorRef = ref
  private var ref : ActorRef = null

  case object GetAuthToken
  case object InvalidateAuthToken
  case class AuthToken(token : String)
  case object Init
}
