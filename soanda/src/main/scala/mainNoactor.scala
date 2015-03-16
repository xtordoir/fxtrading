
import spray.client._

import scala.math._

import scala.concurrent.Future
import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorSystem
import scala.util.{Success, Failure}
import java.util.{Date, Locale}

//import akka.actor._
import spray.http._
import spray.client.pipelining._
import spray.can.Http
import spray.http._
import spray.client.pipelining._

//import spray.can.client.HttpClient
//import spray.client.HttpConduit
//import HttpConduit._
import akka.io.IO
import akka.pattern.ask

import spray.io._
import spray.util._
//import spray.http._

import HttpMethods._
//import spray.httpx.unmarshalling.pimpHttpEntity

import spray.httpx.marshalling._

import com.impactopia.oanda.entities._

import com.impactopia.oanda.entities.MyJsonProtocol._

import spray.httpx.SprayJsonSupport._

import com.impactopia.oanda.client._
import scala.concurrent.duration._
import akka.util.Timeout

import akka.agent.Agent

import com.typesafe.config._
/*
object conf {
    //load the conf
  lazy val root = ConfigFactory.load(getClass.getClassLoader);
}
*/

case class StreamMsg(line: String)



object TestStream extends App {

  implicit val system = ActorSystem()
  import system.dispatcher

  val configFile: String = System.getProperty("user.home") + "/.oandaapi/oandaapi.conf"

  val streamActor = system.actorOf(Props(new StreamingManager(8477964, List("EUR_USD", "EUR_CAD"), configFile)), name = "streamer")
}


object RunMarketOrder {
	def main(args: Array[String]) {
		implicit val timeout: Timeout = 10.second
		implicit val system = ActorSystem("oanda")
		import system.dispatcher

		val configFile: String = System.getProperty("user.home") + "/.oandaapi/oandaapi.conf"
		//val oandaActor = system.actorOf(Props(OandaActor(configFile)), name = "oanda")
		//println(system.settings)
		
		val oanda = new Oanda(configFile, system)
		val tradeFut = oanda.postNewMarketOrder(8477964, "EUR_USD", 2000, "sell")	
		tradeFut.onComplete {
			case Success(succ) => println(succ); system.shutdown
			case Failure(t) => println("An error has occured: " + t.getMessage)	; system.shutdown				
		}	
	}
}

object MainNoActorClass {
	def main(args: Array[String]) {

		implicit val timeout: Timeout = 10.second
		implicit val system = ActorSystem("oanda")
		import system.dispatcher

		val configFile: String = System.getProperty("user.home") + "/.oandaapi/oandaapi.conf"
		//val oandaActor = system.actorOf(Props(OandaActor(configFile)), name = "oanda")
		//println(system.settings)
		
		val oanda = new Oanda(configFile, system)

		val eurusd = Agent(0.0)

		oanda.getAccounts()

		val posFut = oanda.getPositions(8477964)
		posFut.onSuccess {
			case x => println(x)
		}
		system.scheduler.scheduleOnce(1000 milliseconds) {
//		system.scheduler.schedule(1000 milliseconds, 10000 milliseconds) {
			println("sending message")

			val pfut = oanda.getPrice("EUR_USD")
			println(pfut)
//			val pfut = ask(oandaActor,"EUR_USD").mapTo[Future[Either[APIError,Prices]]]
/////			val inside = pfut.flatMap(elt => elt)
			/*for (
				l1 <- pfut.flatMap(elt => elt)
				) yield (l2)*/
			//println("" + inside.map(_.right.map(_.toMap).map(_("EUR_USD"))))
/////			val deeper = inside.map(_.right.map(_.toMap).map(_("EUR_USD")))

			val deeper = pfut.map(_.right.map(_.toMap).map(_("EUR_USD")))
			//eurusd.send()

			deeper.onSuccess {
  				case Right(x: Price) => {
  					println("GOT:" + x.bid)
  					eurusd.send(x.bid)
  					println("HAV:" + eurusd.get)
  				}
			}

		}
		//oanda.shutdown

		//oanda.testError
/*		oanda.testError.onComplete {
			case Success(succ) => println(succ)
			case Failure(t) => println("An error has occured: " + t.getMessage)				
		}
*/
//		oanda ! Accounts
  
//  		oanda.getAccounts2().map(elt => println("RESP2 = " + elt))
//oanda.shutdown()
//system stop system.actorSelection("/*")

	//oanda.streaming("EUR_USD", 8477964)

/*
	oanda.getPrice("EUR_USD").onSuccess {
  		case Right(price: Prices) => {
  			println(price.toMap("EUR_USD"))
  		}
	}

	oanda.getAccounts().onSuccess {
  		case Right(accounts: Accounts) => {
  			val id = accounts(0).map(_.accountId).getOrElse(0)
  			println(id)
  			acc(id, oanda.shutdown)
  		}
	}

	def acc(id: Int, f: () => Unit = () => Unit) = {
		oanda.getAccount(id).onSuccess {
			case Right(account: Account) => {
				println(account)
				oanda.shutdown
			}
		}
	}
	*/
	//val account
	//oanda.shutdown


/*
		oanda.getAccounts().onComplete {
			case Success(succ) => {
				println(succ)
				//oanda.shutdown
				//oanda.getAccounts2().map(elt => println("RESP2 = " + elt))
				//oanda.shutdown
				//oanda.shutdown()
				system.shutdown()
			}
			case Failure(t) => {
				println("An error has occured: " + t.getMessage)
				//oanda.shutdown	
			}			
		}
*/

//		IO(Http).ask(Http.CloseAll)(1.second).await
        //system.shutdown()

		//system.actorSelection("/*") ! akka.actor.PoisonPill
		
//		system.shutdown
		/*
		oanda.getAccount(565784).onComplete {
			case Success(res) => println(res)
			case Failure(t) => println("An error has occured: " + t.getMessage)	
		}
		*/
/*
		oanda.getAccounts("sephew").onComplete {
			case Success(res) => println(res)
			case Failure(t) => println("An error has occured: " + t.getMessage)
		}
*/		
//		oanda.getTrades(565784, TradesQuery(Some(10000), Some(10), Some("EUR_USD"))).onComplete {
	/*
		oanda.getTrades(565784).onComplete {
			case Success(res) => println(res)
			case Failure(t) => println("getTrades error has occured: " + t.getMessage)	
		}
		*/
/*		oanda.trade(565784, "EUR_USD", 4, "sell").onComplete {
			case Success(res) => println(res)
			case Failure(t) => println("An error has occured: " + t.getMessage)	
		}
*/
/*
		oanda.trade(565784, TradeParams("EUR_USD", 1, "sell", None, None, None, None, Some(1000))).onComplete {
			case Success(res) => println(res)
			case Failure(t) => println("An error has occured: " + t.getMessage)	
		}
*/
		/*
		oanda.createAccount("EUR").onComplete {
			case Success(res) => println(res)
			case Failure(t) => println("An error has occured: " + t.getMessage)	
		}
		*/
/*
		oanda.getInstruments().onComplete {
 			case Success(res) => {
  				println(res)
  			}
  			case Failure(t) => println("An error has occured: " + t.getMessage)			
		}
*/

/*
		system.scheduler.schedule(0 milliseconds, 4000 milliseconds) {
			oanda.getQuote(List[String]("EUR_USD")).onComplete {
 				case Success(res) => {
  					println(res)
  				}
  				case Failure(t) => println("getQuote error has occured: " + t.getMessage)			
			}
		}
*/

/*
		oanda.getInstrumentsPrice(List("EUR_USD", "EUR_CAD")).onComplete {
 			case Success(res) => {
  				println(res)
  			}
  			case Failure(t) => println("An error has occured: " + t.getMessage)			
		}
*/
/*
		oanda.getCandles().onComplete {
 			case Success(res) => {
  				println(res)
  			}
  			case Failure(t) => println("An error has occured: " + t.getMessage)						
		}
*/

	}
}
