
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



object MainNoActorClass {
	def main(args: Array[String]) {

		implicit val timeout: Timeout = 10.second
		implicit val system = ActorSystem("oanda")
		import system.dispatcher

object PositionComputer {
	def get(): Future[Position] = {
		Future(Position("EUR_USD", "buy", 729, 0.0))
	}
}

		val configFile: String = System.getProperty("user.home") + "/.oandaapi/oandaapi.conf"
		//val oandaActor = system.actorOf(Props(OandaActor(configFile)), name = "oanda")
		//println(system.settings)
		
		val oanda = new Oanda(configFile, system)

		val eurusd = Agent(Price())
		// the initial position is zero
		val eurusdposition = Agent(Position("EUR_USD", "buy", 728, 0.0))

		val overshoot = Agent(Overshoot("EUR_USD", 0.01))
		//oanda.getAccounts()

		/*
		val posFut = oanda.getPositions(8477964)
		posFut.onSuccess {
			case x => println(x)
		}
	*/
		def OSinitializer(instrument: String, scale: Double): Option[Overshoot] = {
			val tms = List[String]("S5", "S15", "M1", "M5", "M15", "H1", "H4", "D", "W", "M")
			def run(tms: List[String]): Option[Overshoot] = tms match {
				case head :: tail => {
					val cndlFut = oanda.getCandles(instrument, head)
					val cndl =  Await.result(cndlFut, 100 seconds).right.get
					Overshoot.init(scale, cndl) match {
						case Some(os: Overshoot) => Some(os)
						case None => run(tail)
					}
				}
				case Nil => None
			}
			run(tms)			
		}

		//println(OSinitializer("EUR_USD", 1))
		
		def OSSInitializer(instrument: String, scales: List[Double]): Overshoots = {
			val tms = List[String]("S5", "S15", "M1", "M5", "M15", "H1", "H4", "D", "W", "M")
			val scs = scales.sortBy( x => x)
			def run(tms: List[String], scales: List[Double], done: List[Option[Overshoot]]): Overshoots = (tms, scales) match {
				// there remains stuff to do...
				case (head :: tail, scHead :: scTail) => {
					println("RUNNING " + head + " FOR " + scales)
					val cndlFut = oanda.getCandles(instrument, head)
					val cndl =  Await.result(cndlFut, 100 seconds).right.get
					val newdone = scales.map( scale => {
						Overshoot.init(scale, cndl) match {
							case Some(os: Overshoot) => Some(os)
							case None => None
						}
					}).collect(_ match {
						case Some(os: Overshoot) => Some(os)
						})
					run(tail, scales.drop(newdone.size), newdone ++ done)
				}
				// whe hit the end of a list...
				case (Nil, x) => Overshoots(done.collect {case Some(os: Overshoot) => os}.sortBy( x => x.scale).reverse)
				case (x, Nil) => Overshoots(done.collect {case Some(os: Overshoot) => os}.sortBy( x => x.scale).reverse)
			}
			run(tms, scs, List[Option[Overshoot]]())
		}
		println(OSSInitializer("EUR_USD", List[Double](1, 0.5, 0.25, 0.125, 0.06) ))

//		system.scheduler.schedule(1000 milliseconds, 3000 milliseconds) {
		system.scheduler.scheduleOnce(1000 milliseconds) {
			val ov = for (
				price <- oanda.getPrice("EUR_USD");
				oos <- overshoot alter (_.send(price.right.getOrElse(Prices(List[Price]())).get("EUR_USD")))
				) yield ( oos )
			//(overshoot.send(price.right.getOrElse(Positions(List[Position]())).map(_.get("EUR_USD"))))

			ov.onComplete {
				case Success(x) => println(x)
				case Failure(e) => println("An error has occured: " + e.getMessage)
			}
/****
			val tickpos = for (
				price <- oanda.getPrice("EUR_USD");
				prevposition <- eurusdposition.future;
				position <- oanda.getPositions(8477964);
				nextposition <- PositionComputer.get
			) yield (price.right.map(_.get("EUR_USD")), prevposition, position.right.map(_.getOrElse("EUR_USD", Position("EUR_USD") )), nextposition)

			tickpos.onComplete {
				case Success(x) => {
					println(x);
					x match {
						case (Right(Some(p : Price)), prev: Position, Right(current: Position), next: Position) => {
							if (prev.units != current.units) {
								println("PROBLEM WITH UNREGISTETED TRANSACTIONS")
							} else if (current.units != next.units) {
								println("TRADING TO SET POSITION FROM " + current.units + " TO " + next.units)
							} else {
								println("NOTHING TO DO...")
							}
						}
						case y => println(y)
					}
					println(x)
					system.shutdown;
				}
  				case Failure(t) => println("An error has occured: " + t.getMessage);system.shutdown;
			}
			**/
		}
		system.shutdown;
	}
}
