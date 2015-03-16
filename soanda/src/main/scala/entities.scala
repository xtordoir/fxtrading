package com.impactopia.oanda.entities

import java.util.Date
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

import spray.json._
import spray.json.DefaultJsonProtocol

trait Marshable {
//  def apply()
}
/***** TRADE API *****/
// Errors
case class APIError(code: Int, message: String, moreInfo: Option[String]) extends Marshable

object APIError extends Marshable{
  //def apply(code: String, message: String): APIError = APIError(code, message, None)
}

// ACCOUNT ENDPOINTS
case class Account(accountId: Int, accountName: String, balance: Double, unrealizedPl: Double,
  realizedPl: Double, marginUsed: Double, marginAvail: Double, openTrades: Int,
    openOrders: Int, marginRate: Double, accountCurrency: String)

object Account {
  def apply(): Account = Account(0,"", 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0.0, "")
}


case class AccountLabel(accountId: Int, accountName: String, accountCurrency: String, marginRate: Double, accountPropertyName: Option[List[String]])

case class Accounts(accounts: List[AccountLabel]) {
  def apply(index: Int): Option[AccountLabel] = {
    if (accounts.size  > index) {
      Some(accounts(index))
    } else {
      None
    }
  }
  def map[B](f: (AccountLabel) => B): Seq[B] = accounts.map(f)
  def size(): Int = accounts.size

}

object Accounts {
  def apply(): Accounts = Accounts(List[AccountLabel]())
}

case class AccountCredentials(username: String, password: String, accountId: Int)

// TRADE ENDPOINTS
case class Trade(id:Int, units: Int, side: String, instrument: String, 
  time: DateTime, price: Double, takeProfit: Double, stopLoss: Double, trailingStop: Double)
case class Trades(trades: List[Trade], nextPage: Option[String])

case class TradesQuery(maxId: Option[Int] = None, count : Option[Int] = None,
   instrument: Option[String] = None)

case class TradeParams(instrument: String, units: Int, side: String,
                        lowerBound: Option[Double] = None,
                        upperBound: Option[Double] = None,
                        takeProfit: Option[Double] = None,
                        stopLoss: Option[Double] = None,
                        trailingStop: Option[Int] = None)

case class TradeOpened( opened: Int,
                        updated: Int,
                        closed: Seq[Int],
                        interest: Seq[Int],
                        units: Int,
                        side: String,
                        instrument: String,
                        time: DateTime,
                        price: Double,
                        marginUsed: Double,
                        takeProfit: Double,
                        stopLoss: Double,
                        trailingStop: Double)
case class TradeClosed(id: Int, price: Double, instrument: String, profit: Double, side: String, time: DateTime)
// ORDER ENDPOINTS
case class Order(  id: Int, 
                    `type`: String, 
                    side: String, 
                    instrument: String, 
                    units: Int, 
                    time: DateTime, 
                    price: Double, 
                    stopLoss: Double, 
                    takeProfit: Double, 
                    expiry: DateTime, 
                    upperBound: Double, 
                    lowerBound: Double, 
                    trailingStop: Int,
                    ocaGroupId: Option[Int])

case class Orders(orders: List[Orders], nextPage: String)
case class OrderDeletes(id: Int, instrument: String, units: Int, side: String, price: Double, time: DateTime)

case class OrderTradeOpened(id: Option[Int], units: Option[Int], side: Option[String], takeProfit: Option[Double], 
  stopLoss: Option[Double], trailingStop: Option[Double])
object OrderTradeOpened {
  def apply(): OrderTradeOpened = OrderTradeOpened(None, None, None, None, None, None)
}
case class OrderTradeModified(id: Option[Int], units: Option[Int], side: Option[String])
object OrderTradeModified {
    def apply(): OrderTradeModified = OrderTradeModified(None, None, None)
}


case class TradeRequest(instrument: String, units: Int, side: String)
object TradeRequest {
  def apply(target: Position, current: Position): Option[TradeRequest] = {
    val d = target.signedUnits - current.signedUnits
    d match {
      case x if (d > 0) => Some(TradeRequest(target.instrument, math.abs(d), "buy"))
      case x if (d < 0) => Some(TradeRequest(target.instrument, math.abs(d), "sell"))
      case x if (d == 0) => None
    } 
  }
}
case class NewMarketOrder(  instrument: String, 
                            time: DateTime, 
                            price: Double, 
                            tradeOpened: OrderTradeOpened,
                            tradesClosed: List[OrderTradeModified],
                            tradeReduced: OrderTradeModified)
object NewMarketOrder {
  def apply(): NewMarketOrder = NewMarketOrder("", new DateTime(), 0.0, OrderTradeOpened(), List[OrderTradeModified](), OrderTradeModified())
}

// POSITIONS
case class Position(side: String, instrument: String, units: Int, avgPrice: Double) {
  def signedUnits: Int = side match {
    case "buy" => units
    case "sell" => -units
    case _ => 0
  }
}

object Position {
  def apply(): Position = Position("buy", "EUR_USD", 0, 0.0)
  def apply(instrument: String): Position = Position("buy", instrument, 0, 0.0)
}

case class Positions(positions: List[Position]) {
  def toMap: Map[String, Position] = {
    positions.map(elt => (elt.instrument -> elt)).toMap
  }

  def get(instrument: String): Option[Position] = {
    positions.find(elt => elt.instrument == instrument)
  }
  def getOrElse(instrument: String, default: => Position): Position = get(instrument) match {
    case Some(p: Position) => p
    case None => default
  }
}

object Positions {
  def apply(): Positions = Positions(List[Position]())
}


// RATES ENDPOINTS
case class Instrument(instrument: String, displayName: String, pip: String, maxTradeUnits: Int)
case class Instruments(instruments: List[Instrument])

case class Price(instrument: String, time: DateTime, bid: Double, ask: Double) {
  def price: Double = (bid+ask)/2
}

object Price {
  def apply(): Price = Price("", new DateTime, 0.0, 0.0)
}
case class Prices(prices: List[Price]) {
  def toMap: Map[String, Price] = {
    prices.map(elt => (elt.instrument -> elt)).toMap
  }
  def get(instrument: String): Option[Price] = {
    prices.find(elt => elt.instrument == instrument)
  }
}
object Prices {
  def apply(): Prices = Prices(List[Price]())
}

case class Candle(time: DateTime, openMid: Double, highMid: Double, lowMid: Double, closeMid: Double, volume: Int, complete: Boolean)
object Candle {
  def apply(): Candle = Candle(new DateTime(), 0.0, 0.0, 0.0, 0.0, 0, true)
}
case class Candles[T](instrument: String, granularity: String, candles: List[T])

object Candles {
  def apply[T](): Candles[T] = Candles[T]("EUR_USD", "S5", List[T]())
}

//case class StreamTick(instrument: String, time: DateTime, bid: Double, ask: Double)
case class StreamTick(tick: Price)
case class TimeOfHeartbeat(time: DateTime)
case class HeartBeat(heartbeat: TimeOfHeartbeat)



object MyJsonProtocol extends DefaultJsonProtocol {
  val dateFormatter = ISODateTimeFormat.dateTime()
  val pdateFormatter = ISODateTimeFormat.dateTime()

  implicit object DateTimeJsonFormat extends RootJsonFormat[DateTime] {
    def write(d: DateTime) = JsString(dateFormatter.print(d))

    def read(value: JsValue) = value match {
      case JsString(d) => {
        val dSSS = if (d.matches(""".*:\d\d[Z]""")) {d.replaceAll("""Z""", ".000Z")} else {d}
        dateFormatter.parseDateTime(dSSS)
      }
      case _ => deserializationError("Date expected")
    }
  }  

  implicit val ErrorFormat = jsonFormat3(APIError.apply)

  implicit val InstrumentFormat = jsonFormat4(Instrument)
  implicit val InstrumentsFormat = jsonFormat(Instruments, "instruments")
  implicit val PriceFormat = jsonFormat4(Price.apply)
  implicit val PricesFormat = jsonFormat1(Prices.apply)

  implicit val CandleFormat = jsonFormat7(Candle.apply)
  implicit val CandlesFormat = jsonFormat(Candles.apply[Candle], "instrument", "granularity", "candles")

  implicit val AccountFormat = jsonFormat11(Account.apply)
  implicit val AccountLFormat = jsonFormat5(AccountLabel)

  implicit val AccountsFormat = jsonFormat1(Accounts.apply)

  implicit val AccountCredentialsFormat = jsonFormat3(AccountCredentials)

  implicit val tradeFormat  = jsonFormat9(Trade)
  implicit val tradesFormat = jsonFormat(Trades, "trades", "nextPage")
  implicit val tradeActionFormat  = jsonFormat13(TradeOpened)
  implicit val tradeCloseFormat  = jsonFormat6(TradeClosed)

  implicit val positionFormat  = jsonFormat4(Position.apply)
  implicit val positionsFormat = jsonFormat1(Positions.apply)

  implicit val tradeReducedFormat = jsonFormat3(OrderTradeModified.apply)
  implicit val orderTradeOpened = jsonFormat6(OrderTradeOpened.apply)
  implicit val neworderFormat = jsonFormat6(NewMarketOrder.apply)

  implicit val streamTickFormat = jsonFormat1(StreamTick)
  implicit val timeOfHeartBeatFormat = jsonFormat1(TimeOfHeartbeat)
  implicit val heartBeatFormat = jsonFormat1(HeartBeat)
//  implicit val fooFormat: JsonFormat[Foo] = lazyFormat(jsonFormat(Foo, "i", "foo"))
//  implicit val InstrumentsFormat: JsonFormat[Instruments] = lazyFormat(jsonFormat(Foo, "i", "foo"))
}

/*
object OandaObj {
	def getInstruments(implicit conduit: akka.actor.ActorRef): Future[Instruments] = {
		val instrumentPipe: HttpRequest => Future[Instruments] = (
 			sendReceive(conduit)
			~> unmarshal[Instruments]
		)
		instrumentPipe(HttpRequest(method = GET, uri = "/v1/instruments"))
	}
}
*/