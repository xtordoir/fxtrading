package com.impactopia

import akka.actor.Actor
import akka.actor.ActorRef
import scala.collection.immutable.Set

import com.oanda.m.scala.Client


class AccountActor(configFile: String) extends Actor {

//	val client = new Client(new java.io.File(configFile))
	val client = Client(configFile)
	client.login
	client.home

	def receive = {
        case (pair: String, position: Double) => client.tradeSetPosition(pair, position.longValue)
	}
}

case class GetTicks
case class GetTicksToMe(me: ActorRef)
case class Shutdown