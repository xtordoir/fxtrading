
import akka.actor.{ActorSystem, Props}
import scala.concurrent.duration._
import akka.actor._
import akka.dispatch._

import com.impactopia.AccountActor
import com.impactopia._
import com.oanda.m.Rate

import scala.collection.JavaConversions._

case class StartStreamer

//type Rates = Map[String, Rate]

case class EURUSDStreamer extends Actor {
	//import context.dispatcher
	def actorRefFactory = context
	implicit def executionContext = actorRefFactory.dispatcher

	def receive = {
		case StartStreamer => {
			val actorRef = actorRefFactory.actorOf(Props(new AccountActor("/Users/mac/.fxtrade/config.properties")), name = "accountActor")
			println("StartStreamer: ")
			//context.system.scheduler.scheduleOnce(4.seconds, actorRef, GetTicksToMe(self))
			context.system.scheduler.schedule(500.millisecond, 4.seconds, actorRef, GetTicksToMe(self))
		}
		case rates: scala.collection.mutable.Map[String, Rate] => {
			//val r: scala.collection.mutable.Map[String, Rate] = rates
			println()
			rates map (e => println(e._1 + "\t" + e._2))
		}
		case elt @ _ => {
			println("anything..." + elt)
		}
	}
}

object FXAkka extends App {

	implicit val system = ActorSystem()
	import system.dispatcher

//	val accountActor = system.actorOf(Props(new AccountActor("/Users/mac/.fxtrade/config.properties")), name = "accountActor")
//	val eurusdStreamer = system.actorOf(Props(EURUSDStreamer()), name = "eurusdStreamer")

//	eurusdStreamer ! StartStreamer
	val sources = system.actorOf(Props(new SourcesManager()), name = "sourcesActor")

	sources ! "start"
	system.scheduler.scheduleOnce(1.second) { sources ! "trade"}

/*
	import ch.olsen.fxheatmap.scala.Client
	val client = Client("xtordoir@yahoo.co.uk", "taxi2035")
	client.login
	client.addPair("EUR_USD")
	var hmd = client.fetch()
	system.scheduler.schedule(500.millisecond, 4.seconds) {
		hmd = hmd.merge(client.fetch())
		println(hmd.get("EUR_USD", 0.02D))
	}
*/
	//system.scheduler.scheduleOnce(600.second) {system.shutdown()}
}

class ShutDown extends Actor {
	def receive = {
		case _ => context.system.shutdown()
	}
}