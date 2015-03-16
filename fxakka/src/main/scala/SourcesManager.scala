package com.impactopia

import PipsConverter._

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._

import akka.dispatch._

//import ch.olsen.fxheatmap.scala.Client
import ch.olsen.fxheatmap.scala._

class SourcesManager extends Actor {

	def actorRefFactory = context
	implicit def executionContext = actorRefFactory.dispatcher
    implicit val timeout: Timeout = 10.second
	
	val olsenSource = actorRefFactory.actorOf(Props(new OlsenFXHeatMap("xtordoir@yahoo.co.uk", "taxi2035", List[String]("EUR_USD"))), name = "sourcesActor")
	val tradesManager = actorRefFactory.actorOf(Props(new TradesManager), name = "tradesManagerActor")
	val accountManager = actorRefFactory.actorOf(Props(new AccountActor("/Users/mac/.fxtrade/config.properties")), name = "accountActor")
	def receive = {
		case "start" => context.system.scheduler.schedule(4.seconds, 4.seconds, self, "fetch")
		case "fetch" => {
			val hmdOptFut = olsenSource ? "fetch"
			hmdOptFut.onSuccess{
				case Some(res: HeatMapData) => {
					val ratio = 100*20.pips / 1.35
					val tradeFut = tradesManager ? res.get("EUR_USD", ratio)
					tradeFut.onSuccess{
						case Some(res: Future[Trade]) => {
							println("Trade ready for Action...")
							res foreach (ee => println(ee.currentAllocation))
							val trade = Await.result[Trade](res, 10.seconds)
							accountManager ! Tuple2(trade.features.pair, trade.currentAllocation)
							//println("Alloc in sourcesManager = " + Await.result[Trade](res, 10.seconds))
							// now push the Trade into AccountManager...
						}
						case elt => println("got the thind..." + elt)
					}
				}
			}
		}
		case "ping" => println("ping")
		case "trade" => {
			// first, we need the latest lambda
			val hmdOptFut = olsenSource ? "fetch"
			hmdOptFut.onSuccess{
				case Some(res: HeatMapData) => {
					val tradeFut = tradesManager ? TradeCase(
						TradeRisk.build("EUR_USD", 
                                price = 1.35000, 
                                threshold = 20 pips, 
                                stopAt = 65 pips, 
                                maxLoss = 500.0,
                                maxAlloc = 250000.0),
						 res.get("EUR_USD", 100*20.pips/1.35))
					tradeFut.onSuccess{
						case Some(res: Future[Trade]) => {
							println("New Trade ready for Action...")
							res foreach (ee => println(ee.currentAllocation))
							//println("Alloc in sourcesManager = " + Await.result[Trade](res, 10.seconds))
							val trade = Await.result[Trade](res, 10.seconds)
							accountManager ! Tuple2(trade.features.pair, trade.currentAllocation)
							// now push the Trade into AccountManager...
						}
						case elt => println("got the thind..." + elt)
					}
				}
			}
		}

	}

}

class OlsenFXHeatMap(username: String, password: String, pairs: List[String]) extends Actor {
	def actorRefFactory = context
	implicit def executionContext = actorRefFactory.dispatcher

	val client = Client(username, password)
	client.addPairs(pairs)
	client.login
	var hmd = client.fetch.getOrElse(null)
	self ! "start"

	def receive = {
		case "fetch" => {
			val hmdOpt = client.fetch
			hmdOpt match {
				case Some(nhmd: HeatMapData) => hmd = hmd.merge(nhmd)
				case _ => 
			}
			println(hmd.get("EUR_USD", 100*20.pips/1.35))
			sender ! Some(hmd)
//			sender ! "ping"
		//	client.fetch
		}
		case "ping" => println("ping")
	}
}

