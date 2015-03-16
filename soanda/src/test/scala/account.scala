/*
package test

import org.specs2.mutable._
import org.specs2.specification._

import akka.actor.Actor
import akka.actor.ActorSystem
import scala.concurrent.Future
import scala.concurrent.Await
import scala.util.Success
import scala.concurrent._
import scala.concurrent.duration._


import com.impactopia.oanda.client.Oanda
import com.impactopia.oanda.entities._

  class AccountSpec extends Specification {
    implicit val system = ActorSystem()
    import system.dispatcher

    object oanda extends Outside[Oanda] with Scope {
      // prepare a valid HttpRequest
      def outside: Oanda = new Oanda
    }

    "oanda account" should {
      "have id 565784" in oanda { (client: Oanda) => {
        val accFut = client.getAccount(565784)
        Await.result(accFut, Duration(10000, "millis"))
        val Success(acc: Account) = accFut.value.get       
        565784 must_==(acc.accountId)
      }
        

      }
    }
  }
  */