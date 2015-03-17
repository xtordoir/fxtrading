package com.impactopia.oanda.thrift

import com.impactopia.oanda.entities._
import org.joda.time.DateTime

import java.io.ByteArrayInputStream
import org.apache.thrift.transport._
import org.apache.thrift.protocol._
import kafka.serializer.Decoder
import kafka.utils.VerifiableProperties

object TPriceImplicits {
	implicit def TPrice2Price(tprice : TPrice) = 
                                    Price(tprice.instrument, new DateTime(tprice.timestamp), tprice.bid, tprice.ask)
	implicit def Price2TPrice(price : Price): TPrice = 
                                    TPrice(price.instrument, price.time.getMillis(), price.bid, price.ask)

	implicit def StreamTick2TPrice(price : StreamTick): TPrice = 
                                    TPrice(price.tick.instrument, price.tick.time.getMillis(), price.tick.bid, price.tick.ask)
}

class TPriceDecoder(props: VerifiableProperties = null) extends Decoder[TPrice] {
  def fromBytes(data: Array[Byte]): TPrice = {
    val is = new ByteArrayInputStream(data)
    val tprotocol = new TBinaryProtocol(new TIOStreamTransport(is))
    TPrice.decode(tprotocol)
  }
}

class PriceDecoder(props: VerifiableProperties = null) extends Decoder[Price] {
  import TPriceImplicits._
  def fromBytes(data: Array[Byte]): Price = {
    val is = new ByteArrayInputStream(data)
    val tprotocol = new TBinaryProtocol(new TIOStreamTransport(is))
    val price: Price = TPrice.decode(tprotocol)
    price
  }
}


object THeartbeatImplicits {
	implicit def THeartbeat2Heartbeat(thb: THeartbeat) =
	          HeartBeat(TimeOfHeartbeat(new DateTime(thb.timestamp)))
	implicit def Heartbeat2THeartbeat(hb: HeartBeat) = 
			THeartbeat(hb.heartbeat.time.getMillis())
}

class THeartbeatDecoder(props: VerifiableProperties = null) extends Decoder[THeartbeat] {
  def fromBytes(data: Array[Byte]): THeartbeat = {
    val is = new ByteArrayInputStream(data)
    val tprotocol = new TBinaryProtocol(new TIOStreamTransport(is))
    THeartbeat.decode(tprotocol)
  }
}

import com.impactopia.hff.trader._
import com.impactopia.hff._
object TradeAgentsImplicits {
	implicit def TPrice2Tick(t: TPrice) = Tick(new DateTime(t.timestamp), t.bid, t.ask)
	implicit def Tick2TPrice(t: Tick)(implicit instrument: String) = 
		TPrice(instrument, t.time.getMillis(), t.bid, t.ask)

/*	implicit def TOvershootTrader2OvershootTrader(t: TOvershootTrader) =
		OvershootTrader(t.exposure, t.tickMax, t.active,
	t.scale, t.tick0, t.p0, t.stopOS)

	implicit def OvershootTrader2TOvershootTrader(os: OvershootTrader)(implicit instrument: String) = 
		TOvershootTrader(os.exposure, os.tickMax, os.active, 
			os.scale, os.tick0, os.p0, os.stopOS)
*/
}