package ch.olsen.fxheatmap.scala

import java.util.Date

case class Client(client: ch.olsen.fxheatmap.rpcclient.Client) {

	def login() = {
		client.login
	}

	def addPair(pair: String) = {
		client.addPair(pair)
	}
	def addPairs(pairs: Seq[String]) = {
		pairs.map(elt => client.addPair(elt))
	}

	def fetch(): Option[HeatMapData] = {
		val hmd = client.fetch
		// if there is a time lage, we need to relogin, and we fail the fetch
		if ( math.abs( hmd.date.getTime - (new Date()).getTime) > 1000L*60L*10L) {
			login
			None
		} else {
			Some(hmd)
		}
	}

}

object Client {
	def apply(file: String): Client = {
		Client(new ch.olsen.fxheatmap.rpcclient.Client(new java.io.File(file)))
	}

	def apply(username: String, password: String): Client = {
		Client(new ch.olsen.fxheatmap.rpcclient.Client(username, password))
	}
}