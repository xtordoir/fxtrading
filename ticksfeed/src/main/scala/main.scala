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
import THeartbeatImplicits._


trait KafkaSender {
	val kafkaIP = System.getenv("KAFKA_IP")
	val tickprod = new kafka.producer.KafkaProducer("ticks", s"${kafkaIP}:9092")
	val hbprod = new kafka.producer.KafkaProducer("heartbeats", s"${kafkaIP}:9092")
}

object ConsoleSender {
	def send(bytes: Array[Byte], nul: Any):Unit  = {
		println(bytes)
	}
}
trait ConsoleSender {
	val prod = ConsoleSender
}

case class StreamingManager2(accountId: Int, instruments: List[String], configFile: String) extends Actor with KafkaSender {


	def actorRefFactory = context
	implicit val system = context.system
	implicit def executionContext = actorRefFactory.dispatcher
	val streamActor = context.actorOf(Props(new StreamingActor(accountId, instruments, configFile)))

	var backoff = 500
	def extendBackoff() {
		backoff = if (backoff < 60000) backoff * 2 else backoff
	}

	override def preStart() {
    	context.watch(streamActor)
  	}
	
	def receive = {

	case hb: HeartBeat => {
		println(hb)
		backoff = 1000
		val os = new ByteArrayOutputStream()
		val tpotocol = new TBinaryProtocol(new TIOStreamTransport(os))
    	THeartbeat.encode(hb, tpotocol)
    	println(s"sending ${os.toByteArray()} to " + hbprod)
    	hbprod.send(os.toByteArray(), null)
	}

    case st: StreamTick => {
    	println(st)
		val os = new ByteArrayOutputStream()
		val tpotocol = new TBinaryProtocol(new TIOStreamTransport(os))
    	TPrice.encode(st, tpotocol)
    	println(s"sending ${os.toByteArray()} to " + tickprod)
    	tickprod.send(os.toByteArray(), null)
	}

	// restart with exponential backoff
    case FailedStreaming => {
    	extendBackoff()
    	println("Restart in " + backoff + " ms")
    	context.system.scheduler.scheduleOnce(backoff milliseconds, streamActor, "restart")

    }

    case Terminated(`streamActor`) => println("streamActor terminated")
	}



}

object Main extends App {

  implicit val system = ActorSystem()
  import system.dispatcher

  val configFile: String = System.getProperty("user.home") + "/.oandaapi/oandaapi.conf"

  val streamActor = system.actorOf(Props(new StreamingManager2(8477964, List("EUR_USD"), configFile)), name = "streamer")
}
/*
object MainNoKafka extends App {

  implicit val system = ActorSystem()
  import system.dispatcher

  val configFile: String = System.getProperty("user.home") + "/.oandaapi/oandaapi.conf"

  val streamActor = system.actorOf(Props(new StreamingManager(8477964, List("EUR_USD"), configFile)), name = "streamer")
}
*/

