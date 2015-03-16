import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor._

import scala.concurrent.duration._

import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import org.apache.thrift.transport._
import org.apache.thrift.protocol._

import com.impactopia.oanda.client._
import com.impactopia.oanda.thrift._
import com.impactopia.oanda.entities._

import TPriceImplicits._

import kafka.consumer._

object Main extends App {

	def writer(data: Array[Byte]): Unit = {
		val is = new ByteArrayInputStream(data)
		val tprotocol = new TBinaryProtocol(new TIOStreamTransport(is))
		val price: Price = TPrice.decode(tprotocol)
		println(price)
	} 

	val cons = new KafkaConsumer("ticks", "ticksfeedReader", "localhost:2181")
	cons.read(writer)
	cons.close()
}

