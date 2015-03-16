package com.impactopia.oanda.client

import scala.concurrent.Future
import akka.actor.ActorSystem
import akka.actor.Props

import spray.http._
import spray.client.pipelining._
import spray.can.Http
import spray.http._
import spray.client.pipelining._


import spray.io._
import spray.http._
import MediaTypes._
import HttpMethods._
//import spray.httpx.unmarshalling.pimpHttpEntity
import com.impactopia.oanda.entities._
import com.impactopia.oanda.entities.MyJsonProtocol._
import spray.httpx.SprayJsonSupport._
import spray.util._

//import spray.client.HttpConduit
import  spray.client._
import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._
import akka.actor.{ ActorRefFactory, ActorRef }
import akka.util.Timeout
import akka.pattern.ask
import akka.io.IO
import spray.httpx.{ ResponseTransformation, RequestBuilding }
import spray.can.Http
import spray.util.actorSystem
import spray.http._





import com.typesafe.config._



case class Oanda(configFile: String, implicit val system: ActorSystem) {
   
	// holds the parsed config file
	object conf {
  		lazy val root = ConfigFactory.parseFile(new java.io.File(configFile))
	}	

	type HToRFut = HttpRequest => Future[HttpResponse]

	// end point base
	//val targetURI = "http://api-sandbox.oanda.com"
	private lazy val targetURI = conf.root.getString("practice.url")
	private lazy val accessToken = conf.root.getString("practice.accessToken")
	private lazy val streamingURI = conf.root.getString("practice.streaming")

	val port = 80

	val `application/json` = 
		MediaTypes.register(MediaType.custom(
    		mainType = "application",
    		subType = "json"))

	val `application/x-www-form-urlencoded` = 
		MediaTypes.register(MediaType.custom(
    		mainType = "application",
    		subType = "x-www-form-urlencoded"))

	import system.dispatcher

	def shutdown(): Unit = {
    	IO(Http).ask(Http.CloseAll)(4.second).await
    	system.shutdown()
  	}

	type HToFut[T] = HttpRequest => Future[T]
	type EitherFut[T] = Future[Either[APIError,T]]

//	implicit def pipeline[T]:  HToFut[T] = sendReceive ~> unmarshal[T]
//	implicit def pipeline[APIError]:  HToFut[APIError] = sendReceive ~> unmarshal[APIError]
	implicit def pipelineAccount: HToFut[Account] = sendReceive ~> unmarshal[Account]

    implicit def pipeline: HToRFut = addHeader("Authorization", "Bearer " + accessToken) ~> sendReceive 

// ACCOUNT ENDPOINTS
    def getAccounts(): EitherFut[Accounts] = {
    	pipeRequest[Accounts](Accounts.apply, HttpRequest(method = GET, uri = targetURI + "/v1/accounts"))(pipeline)
    }

	def getAccount(account_id: Int): EitherFut[Account] = {
    	pipeRequest[Account](Account.apply, HttpRequest(method = GET, uri = targetURI + "/v1/accounts/" + account_id))(pipeline)
    }

	def getPositions(account_id: Int): EitherFut[Positions] = {
		pipeRequest[Positions](Positions.apply, HttpRequest(method = GET, uri = targetURI + "/v1/accounts/" + account_id + "/positions"))(pipeline)
	}

	// Prices
	def getPrice(instrument: String): EitherFut[Prices] = {
    	pipeRequest[Prices](Prices.apply, HttpRequest(method = GET, uri = targetURI + "/v1/prices?instruments=" + instrument))(pipeline)
	}
	def getQuote(instruments: List[String]): EitherFut[Prices] = {
    	pipeRequest[Prices](Prices.apply, HttpRequest(method = GET, uri = targetURI + "/v1/prices?instruments=" + instruments.mkString(",")))(pipeline)
	}
	def getCandles(instrument: String, granularity: String = "S5"): EitherFut[Candles[Candle]] = {
		pipeRequest[Candles[Candle]](Candles.apply[Candle], 
			HttpRequest(method = GET, uri = targetURI + "/v1/candles?instrument=" + instrument + "&granularity=" + granularity + "&count=5000&candleFormat=midpoint"))(pipeline)
	}

	def postNewMarketOrder(account_id: Int, instrument: String, units: Int, side: String): EitherFut[NewMarketOrder] = {		
		val fmap = Map(
			"instrument" -> instrument,
			"units" -> units.toString,
			"side" -> side,
			"type" -> "market"
			)
		pipeRequest[NewMarketOrder](NewMarketOrder.apply, Post(uri = targetURI + "/v1/accounts/" + account_id + "/orders", FormData(fmap)))(pipeline)
	}

	def postNewMarketOrder(account_id: Int, trade: TradeRequest): EitherFut[NewMarketOrder] =
		postNewMarketOrder(account_id, trade.instrument, trade.units, trade.side)

	def streaming(instrument: String = "EUR_USD", accountId: Int) = {
		val request = HttpRequest(method = GET, uri = streamingURI + "/v1/quote?accountId=" + accountId + "&instruments=" + instrument)
		println(request)
		val resp = pipeline(request)
		println(resp)
		resp.map (response => {
			println(response)
	    	response.status.isSuccess match {
    	        case true    => println(response)
    	        case other   => println(response)
    	    }
    	}
    	)  
	}
//	def getSomething(account_id : Int)(implicit pipeline:HToFut[Account]): Future[Account] = {
//		pipeline(HttpRequest(method = GET, uri = "/v1/accounts/" + account_id + "/trades"))
//	}

	/*
	// Instruments methods
	def getInstruments(visibilityAll: Boolean = false)(implicit pipeline:HToFut[Instruments]): Future[Instruments] = {
		val qstr = if (visibilityAll) "?visibility=all" else "" 
		pipeline(HttpRequest(method = GET, uri = "/v1/instruments" + qstr))
	}
*/
/********* TRADING API *********/

	def test = {
		import scala.util.{Success, Failure}
		pipeline(HttpRequest(method = GET, uri = targetURI + "/v1/accounts")).onComplete{
			case Success(res) => println(res.entity.asString)
			case Failure(t) => println("An error has occured IN TEST: " + t.getMessage)		
		}		
	}

	def testError: Future[Either[APIError,Accounts]] = {
		pipeRequest[Accounts](Accounts(), HttpRequest(method = GET, uri = targetURI + "/v1/accos"))(pipeline)
	}


// ACCOUNT ENDPOINTS

/*
	def getAccount(account_id: Int): Future[Account] = {
		val pipeline: HttpRequest => Future[Account] = sendReceive ~> unmarshal[Account]
		pipeline(HttpRequest(method = GET, uri = targetURI + "/v1/accounts/" + account_id))
	}
*/
	def getAccounts(username: String): Future[Accounts] = {
		val pipeline: HttpRequest => Future[Accounts] = sendReceive ~> unmarshal[Accounts]
		pipeline(HttpRequest(method = GET, uri = targetURI + "/v1/accounts?username=" + username))
	}

	def createAccount(currency: String = "USD"): Future[AccountCredentials] = {
//		val qstr = "currency=" + currency 
		val pipeline: HttpRequest => Future[AccountCredentials] = sendReceive ~> unmarshal[AccountCredentials]
//		pipeline(HttpRequest(method = POST, uri = "/v1/accounts",
//		 entity = HttpBody(`application/x-www-form-urlencoded`, qstr)))

		pipeline(Post(targetURI + "/v1/accounts" , FormData(Map("currency" -> currency))))
	}
// TRADE ENDPOINTS
	def getTrades(account_id: Int, query: TradesQuery): Future[Trades] = getTrades(account_id, Some(query))
	def getTrades(account_id: Int, query: Option[TradesQuery] = None): Future[Trades] = {
		//val pipeline: HttpRequest => Future[Trades] = sendReceive(conduit) ~> unmarshal[Trades]
		//pipeline(HttpRequest(method = GET, uri = "/v1/accounts/" + account_id + "/trades"))
		val qst = query match {
			case None => ""
			case Some(TradesQuery(maxId, count, instrument)) => {
				val maxCl = maxId.map { "maxId=" + _ }
				val countCl = count.map { "count=" + _ }
				val instrumentCl = instrument.map { "instrument=" + _ }
				(maxCl, countCl, instrumentCl) match {
					case (None, None, None) => ""
					case tuple => {
						"?" +
						tuple.productIterator.collect {
							case Some(s: String) => s
						}.toList.mkString("&")
					}
				}
			}				
		}
		//println(qst)
		getTrades(account_id)(qst)
	}
	def getTrades(account_id: Int, ids: Seq[Int]): Future[Trades] = {
		val qst = "?ids=" + ids.mkString(",")
		getTrades(account_id)(qst)
	}

	private def getTrades(account_id: Int)(queryString: String): Future[Trades] = {
		val pipeline: HttpRequest => Future[Trades] = sendReceive ~> unmarshal[Trades]
		pipeline(HttpRequest(method = GET, uri = targetURI + "/v1/accounts/" + account_id + "/trades" + queryString))
	}

	def getTrade(account_id: Int, trade_id: Int): Future[Trade] = {
		val pipeline: HttpRequest => Future[Trade] = sendReceive ~> unmarshal[Trade]
		pipeline(HttpRequest(method = GET, uri = targetURI + "/v1/accounts/" + account_id + "/trades" + trade_id))	
	}

	def trade(account_id: Int, params: TradeParams): Future[TradeOpened] = {
		val pipeline: HttpRequest => Future[TradeOpened] = sendReceive ~> unmarshal[TradeOpened]
		val fmap = Map[String, String](
			"instrument" -> params.instrument,
			"units" -> params.units.toString,
			"side" -> params.side,
			"lowerBound" -> params.lowerBound.getOrElse("").toString,
			"upperBound" -> params.upperBound.getOrElse("").toString,
			"takeProfit" -> params.takeProfit.getOrElse("").toString,
			"stopLoss" -> params.stopLoss.getOrElse("").toString,
			"trailingStop" -> params.trailingStop.getOrElse("").toString
			).iterator.collect {
			case (k, v) if (!v.equals("")) => (k -> v)
			//case e => println(e)
		}.toMap

		pipeline(Post(targetURI + "/v1/accounts/" + account_id + "/trades", FormData(fmap)))
	}

	def trade(account_id: Int, instrument: String, units: Int, side: String): Future[TradeOpened] = {
		val pipeline: HttpRequest => Future[TradeOpened] = sendReceive ~> unmarshal[TradeOpened]
  		val data = Map("instrument" -> "EUR_USD", "units" -> units.toString, 
  			"side" -> side)
	    val formdata = FormData(data)
	    pipeline(Post(targetURI + "/v1/accounts/" + account_id + "/trades", formdata))
	}

	def changeTrade(account_id: Int, trade_id: Int, takeProfit: Option[Double], stopLoss: Option[Double], trailingStop: Option[Int]): Future[Trade] = {
		val pipeline: HttpRequest => Future[Trade] = sendReceive ~> unmarshal[Trade]
		val fmap = Map[String, Option[Any]](
			"takeProfit" -> takeProfit,
			"stopLoss" -> stopLoss,
			"trailingStop" -> trailingStop
			).iterator.collect {
			case (k, Some(v: Any)) => (k -> v.toString)
			//case e => println(e)
		}.toMap

	    pipeline(Put(targetURI + "/v1/accounts/" + account_id + "/trades/" + trade_id, FormData(fmap)))
	}

	def closeTrade(account_id: Int, trade_id: Int): Future[TradeClosed] = {
		val pipeline: HttpRequest => Future[TradeClosed] = sendReceive ~> unmarshal[TradeClosed]
		pipeline(Delete(targetURI + "/v1/accounts/" + account_id + "/trades/" + trade_id))
	}





/*
// RATES ENDPOINTS
	def getInstruments(visibilityAll: Boolean = false)
	(implicit pipeline:HToFut[Instruments] = sendReceive(conduit) ~> unmarshal[Instruments]): Future[Instruments] = {
		val qstr = if (visibilityAll) "?visibility=all" else "" 
		pipeline(HttpRequest(method = GET, uri = "/v1/instruments" + qstr))
	}
*/
	// Prices
	/*
	def getPrice(instrument: String): Future[Price] = {
		val pipeline: HttpRequest => Future[Price] = sendReceive ~> unmarshal[Price]
		pipeline(Get(targetURI + "/v1/quote?instrument=" + instrument))
	}
	
	def getQuote(instruments: List[String]): Future[Prices] = {
		val pipeline: HttpRequest => Future[Prices] = sendReceive ~> unmarshal[Prices]
		pipeline(Get(targetURI + "/v1/quote?instruments=" + instruments.mkString(",")))
	}
*/
	/* Partial implementation, options and candle formats not supported */
	/*
	def getCandles(): Future[Candles[Candle]] = {
		val pipeline: HttpRequest => Future[Candles[Candle]] = sendReceive(conduit) ~> unmarshal[Candles[Candle]]
		pipeline(HttpRequest(method = GET, uri = "/v1/instruments/EUR_USD/candles?count=2"))
	}
	*/

	import spray.httpx.unmarshalling._
    private def eunmarshal[T: Unmarshaller](response: HttpResponse): T = {
        response.entity.as[T] match {
          case Right(value) => value
          case Left(error)  => throw new PipelineException(error.toString)
        }
    }
    private def mswitch[T](kind:T)(response: HttpResponse): Either[APIError, T] = kind match {
    	case x: Accounts => Right(eunmarshal[Accounts](response).asInstanceOf[T])
    	case x: Account => Right(eunmarshal[Account](response).asInstanceOf[T])
    	case x: Price => Right(eunmarshal[Price](response).asInstanceOf[T])
    	case x: Prices => Right(eunmarshal[Prices](response).asInstanceOf[T])
     	case x: Position => Right(eunmarshal[Position](response).asInstanceOf[T])
    	case x: Positions => Right(eunmarshal[Positions](response).asInstanceOf[T])
     	case x: Candle => Right(eunmarshal[Candle](response).asInstanceOf[T])
    	case x: Candles[Candle] => Right(eunmarshal[Candles[Candle]](response).asInstanceOf[T])
    	case x: NewMarketOrder => Right(eunmarshal[NewMarketOrder](response).asInstanceOf[T])
    	case x: APIError => {
    		val un = eunmarshal[APIError](response)
    		Right(un.asInstanceOf[T])
    	}
    	case other    => Left(eunmarshal[APIError](response))
    }

    def pipeRequest[T](kind: T, request: HttpRequest)(pipeline: HttpRequest => Future[HttpResponse]): Future[Either[APIError, T]] = {
    	//val response: Future[HttpResponse] = pipeline(request)
    	//println(request)
    	pipeline(request).map (response => 
	    	response.status.isSuccess match {
    	        case true    => /*println("SUCCESS"); println(response); */mswitch(kind)(response)
    	        case other   => /*println(other); println(response); */Left(eunmarshal[APIError](response))
    	    }
    	)  
    }

}
