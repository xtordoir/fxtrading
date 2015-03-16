package com.impactopia

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Future
import scala.concurrent.duration._

//import ch.olsen.fxheatmap.scala.Client
import ch.olsen.fxheatmap.scala._

case class TradeCase(risk: TradeRisk, lambda: (Double, Double))

class TradesManager extends Actor {

	def actorRefFactory = context
	implicit def executionContext = actorRefFactory.dispatcher
    implicit val timeout: Timeout = 10.second

	var tradeActor: Option[ActorRef] = None

	def receive = {
		case t: TradeCase => {
			tradeActor = Some(actorRefFactory.actorOf(Props(
			new TradeActor(t.risk, t.lambda)), name = "tradeActor"))
			sender ! Some((tradeActor.get ? "get").mapTo[Trade])
		}

		case lambda: (Double, Double) => tradeActor match {
			case Some(act: ActorRef) => {
				val tt = for (trade <- (act ? lambda).mapTo[Trade]) yield (trade)
				println(tt)
				sender ! Some(tt)
			}
			case None => 
		}
	}

}

// an actor to manage the lifetime of a trade
// 1. creation (PENDING)
// 2. execution (OPENED)
// 3. life: adjusting gear
// 4. execution (CLOSED)
// 5. shutdown
class TradeActor(risk: TradeRisk, lambda: (Double, Double)) extends Actor {
	var trade: Trade = Trade(risk, lambda._2, lambda._2)
	trade = trade.update(lambda._1)
	println(trade)


	def threshold = risk.threshold

	def receive = {
		case lambda: (Double, Double) => {
			val t = trade.update(lambda._1)
			trade = t
			println(trade)
			println("Current lambda diff(0-ext):" + (trade.lambdaExt - trade.lambda0))
			println("Current Position:" + trade.currentAllocation)
			sender ! trade
		}
		case "get" => {
			sender ! trade
		}
	} 
}