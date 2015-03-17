package com.impactopia.oanda.client

import scala.concurrent.Future
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor._

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


import spray.json.DefaultJsonProtocol
import spray.httpx.unmarshalling._
import spray.httpx.marshalling._
import spray.http._
import HttpCharsets._
import MediaTypes._


import com.typesafe.config._


object FailedStreaming

case class StreamingActor(accountId: Int, instruments: List[String], configFile: String) extends Actor {

  // holds the parsed config file
  object conf {
      lazy val root = ConfigFactory.parseFile(new java.io.File(configFile))
  } 
  private lazy val accessToken = conf.root.getString("practice.accessToken")
  private lazy val streamingName = conf.root.getString("practice.streaming")


	def actorRefFactory = context
	implicit val system = context.system
//implicit val system: ActorSystem = ActorSystem()
	//IO(Http) ! Http.Connect("https://stream-fxpractice.oanda.com", port = 443)
	IO(Http) ! Http.Connect("stream-fxpractice.oanda.com", 443, sslEncryption = true)
	def receive = {
    case "restart" => IO(Http) ! Http.Connect("stream-fxpractice.oanda.com", 443, sslEncryption = true)
		case Http.CommandFailed(x) => 
		println("command failed...")
		context.parent ! FailedStreaming

      case Http.Connected(_, _) =>
        println("Connected to streaming url:")
        val url = "/v1/prices?accountId=" + accountId + "&instruments=" + instruments.mkString("%2C")
        println(url)
        sender ! HttpRequest(GET, "/v1/prices?accountId=" + accountId + "&instruments=" + instruments.mkString("%2C")) ~> addHeader("Authorization", "Bearer " + accessToken)

      case ChunkedResponseStart(res) =>
        println("Starting reading chunks: " + res)

      case MessageChunk(body, ext) =>
        // split lines and send them back to manager
        body.asString.lines.toList.map(line => {
        //(new String(body)).lines.toList.map(line => {
          //println(line)
          val body = mkBody(line)
          val vv = body.as[StreamTick].right.getOrElse(
            body.as[HeartBeat].right.getOrElse(None)
          )
          context.parent ! vv
          }) 
      case ChunkedMessageEnd(ext, trailer) =>
        println("end: " + ext)
        context.parent ! FailedStreaming

      case m =>
        println("received unknown message " + m)
        context.parent ! FailedStreaming
    }

  def mkBody(line: String) = {
    HttpEntity(
      contentType = ContentType(`application/json`, `UTF-8`),
      string = line
    )
  }    


}


import com.impactopia.oanda.entities._

import MyJsonProtocol._
import spray.httpx.SprayJsonSupport._
import spray.util._


case class StreamingManager(accountId: Int, instruments: List[String], configFile: String) extends Actor {


	def actorRefFactory = context
	implicit val system = context.system

  val streamActor = context.actorOf(Props(new StreamingActor(accountId, instruments, configFile)))

  override def preStart() {
    context.watch(streamActor)
  }
	


	def receive = {

		case hb: HeartBeat => println(hb)

    case st: StreamTick => println(st)

    case FailedStreaming => streamActor ! "restart"

    case Terminated(`streamActor`) => println("streamActor terminated")
	}



}