package com.oanda.m.scala

object Rate {
	implicit def rate(r: com.oanda.m.Rate) = Rate(r.getPair(), r.getBid(), r.getAsk())
}
case class Rate(pair: String, bid: Double, ask: Double) {
	def spread(): Double = ask - bid
	def price(): Double = (bid+ask)/2
}

object Position {
	implicit def position(p: com.oanda.m.Position) = 
		Position(p.getPosition, p. getPair, p.getUnits, p.getProfit)
}

case class Position(position: String, pair: String, units: Long, profit: Double) {
	def getSignedUnits():Long = if (position.equals("S")) -units else units
}
 
object Summary {
	implicit def summary(s: com.oanda.m.Summary) = 
	Summary(s.getName(), s.getCurrency, s.getBalance, s.getUnrealizedPL,
		s.getNav, s.getPL, s.getUsedMargin, s.getAvailableMargin)
}
case class Summary(name: String, currency: String, balance: Double,
	unrealizedPL: Double, nav: Double, PL: Double, usedMargin: Double,
	availableMargin: Double)


object TradeMsg {
	implicit def trademsg(t: com.oanda.m.TradeMsg): TradeMsg = 
		TradeMsg(t.position, t.pair, t.units, t.price)
}
case class TradeMsg(position: String, pair: String, units: Long,
	price: Double)

//import com.oanda.m.{Client, Position, Rate, Summary}
import scala.collection.JavaConversions._

object Client {
	def apply(file: String): Client = {
		Client(new com.oanda.m.Client(new java.io.File(file)))
	}
}

case class Client(client: com.oanda.m.Client) {

	def login() = client.login()
	def logout() = client.logout()
	def home() = client.home()
	
	def rates(): scala.collection.mutable.Map[String, Rate] = {
		client.rates() map (e => {
			val rr: Rate = e._2
			(e._1, rr)
		}
			)
	}

	def getAccounts(): scala.collection.mutable.Map[String, String] = {
		client.getAccounts		
	}
	def setAccount(account: String) = {
		client.setAccount(account)
	}
	def summary():Summary = {
		client.summary
	}
	def positions(): scala.collection.mutable.Map[String, Position] = {
		client.positions() map (e => {
			val pp: Position = e._2
			(e._1, pp)
			})
	}

	def closePosition(pair: String) = {
		client.closePosition(pair.replaceAll("_", "/"))
	}
	def pairTradeUnits(position: String, pair: String, fraction: Double): Long = {
		client.pairTradeUnits(position, pair.replaceAll("_", "/"), fraction)
	}

	def tradeSetPosition(position: String, pair: String, units: Long): TradeMsg = {
		client.tradeSetPosition(position, pair.replaceAll("_", "/"), units)
	}
	def tradeSetPosition(pair: String, units: Long): TradeMsg = {
		val pStr = if (units >= 0) "L" else "S"
		tradeSetPosition(pStr, pair.replaceAll("_", "/"), math.abs(units.longValue))
	}

	def trade(position: String, pair: String, fraction: Double): TradeMsg = {
		client.trade(position, pair.replaceAll("_", "/"), fraction)
	}
}