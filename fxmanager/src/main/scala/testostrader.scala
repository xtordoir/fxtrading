
import spray.client._

import scala.math._

import scala.concurrent.Future
import scala.concurrent.Await
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
import spray.httpx.unmarshalling.pimpHttpEntity

import spray.httpx.marshalling._

import com.impactopia.oanda.entities._
import com.impactopia.hff._

import com.impactopia.oanda.entities.MyJsonProtocol._

import spray.httpx.SprayJsonSupport._

import com.impactopia.oanda.client._
import scala.concurrent.duration._
import akka.util.Timeout

import akka.agent.Agent

import com.typesafe.config._

import com.impactopia.hff._
import com.impactopia.hff.trader._


object TestingOvershootTrader {
	def main(args: Array[String]) {

		implicit val timeout: Timeout = 10.second
		implicit val system = ActorSystem("oanda")
		import system.dispatcher


		val configFile: String = System.getProperty("user.home") + "/.oandaapi/oandaapi.conf"
		//val oandaActor = system.actorOf(Props(OandaActor(configFile)), name = "oanda")
		//println(system.settings)
		
		val oanda = new Oanda(configFile, system)

		val eurusd = Agent(Price())
		// the initial position is zero
		val eurusdposition = Agent(Position("EUR_USD", "buy", 728, 0.0))

		val overshoot = Agent(Overshoot("EUR_USD", 0.01))
		//oanda.getAccounts()
		val priceFut = oanda.getPrice("EUR_USD")
		val price0 = Await.result(priceFut, 100 seconds).right.getOrElse(Prices(List[Price]())).get("EUR_USD")		
		val trader = Agent( OvershootTrader(
			Tick(price0.get.time, price0.get.bid, price0.get.ask), 0.0005, 150000, 100, 10, 1) )


		system.scheduler.schedule(1000 milliseconds, 3000 milliseconds) {
		//system.scheduler.scheduleOnce(1000 milliseconds) {
			val ov = for (
				price <- oanda.getPrice("EUR_USD");

				tr <- trader alter (_.send(
					Tick(price.right.getOrElse(Prices(List[Price]())).get("EUR_USD").map(_.time).get, 
						price.right.getOrElse(Prices(List[Price]())).get("EUR_USD").map(_.bid).get, 
						price.right.getOrElse(Prices(List[Price]())).get("EUR_USD").map(_.ask).get)));
				positions <- oanda.getPositions(8477964);
				position <- eurusdposition alter ( x => positions.right.getOrElse(Positions(List[Position](Position()))).getOrElse("EUR_USD", Position()))

				) yield ( tr, position)
			//(overshoot.send(price.right.getOrElse(Positions(List[Position]())).map(_.get("EUR_USD"))))

			// now we trade ...
			

			ov.onComplete {
				case Success(x) => {
					val newPos = Position(if (x._1.exposure >=0) "buy" else "sell", "EUR_USD", math.abs(x._1.exposure).intValue, 0.0)
					val trr = TradeRequest(newPos, x._2)
					trr.map(oanda.postNewMarketOrder(8477964, _))

					println(x)
					println(trr)
				}
				case Failure(e) => println("An error has occured: " + e.getMessage)
			}

		}
		//system.shutdown;
	}
}
