package com.impactopia.hff.fx4j

import org.anormcypher._

import com.impactopia.hff.Tick
import com.impactopia.hff.trader.OvershootTrader

object Tick4j {

	def save(tick: Tick)(implicit instrument: String): Boolean = {
		val cyphStr = """
			CREATE (n:Price {instrument:{instrument},time:{time}, bid:{bid}, ask:{ask}})
		"""
		Cypher(cyphStr).on("instrument" -> instrument,
						"time" -> tick.time.getMillis(),
						"bid" -> tick.bid,
						"ask" -> tick.ask
							).execute()
	}

}

trait FX4j {



/*
def SendF(instrument: String)(price: Tick)(agent: OvershootTrader)(f: => String => Tick => OvershootTrader => OvershootTrader ) = 
f(instrument)(price)(agent)
*/
	def Init(agent: OvershootTrader,  instrument: String) = {
		OvershootTrader4j.saveNew(agent)(instrument)
	}

	def Send(agent: OvershootTrader, price: Tick)(implicit instrument: String): OvershootTrader = {
		val newagent = agent.send(price)
		(agent, newagent) match {
			case _ if (newagent.exposure != agent.exposure && newagent.exposure != 0) => {
				println("saving new state...")
				OvershootTrader4j.saveUpdate(newagent)(instrument)
				newagent
			}
			case _ if (newagent.exposure != agent.exposure && newagent.exposure == 0) => {
				println("closing position")
				OvershootTrader4j.saveClose(newagent)(instrument)
				newagent
			}
			case _ => {
				agent
			}
		}
	}
}

object OvershootTrader4j {

	def saveNew(agent: OvershootTrader)(implicit instrument: String): Boolean = {
		val cyStr = """
		CREATE (n:Price {instrument:{instrument},time:{time}, bid:{bid}, ask:{ask}})
		-[a:CREATE_AGENT {scale: {scale}, p0: {p0}, stopOS: {stopOS}}]->
		(y:Agent {exposure:{exposure}, active: {active}})
		<-[b: TickMax]-(nmax:Price {instrument:{instrument},time:{time}, bid:{bid}, ask:{ask}})
			
		"""
		Cypher(cyStr).on(
						"instrument" -> instrument,
						"time" -> agent.tick0.time.getMillis,
						"bid" -> agent.tick0.bid,
						"ask" -> agent.tick0.ask,
						"scale" -> agent.scale,
						"p0" -> agent.p0,
						"stopOS" -> agent.stopOS,
						"exposure" -> agent.exposure,
						"active" -> agent.active
		).execute()

	}

	def saveUpdate(agent: OvershootTrader)(implicit instrument: String): Boolean = {
		// find the agent
		val qStr = """
		MATCH (t0: Price)-[x:CREATE_AGENT]->(a:Agent)<-[tm:TickMax]-(tmax:Price) 
		WHERE t0.instrument = {instrument} AND t0.time = {time0} AND x.scale = {scale} 
		MATCH (a:Agent)-[*0..]->(b:Agent)
		WHERE NOT ((b)-->())
		CREATE (b)-[z:UPDATE]->(c:Agent {exposure:{exposure}, active: {active}})
		<-[zz:TickMax]-(tt:Price {instrument:{instrument},time:{time},bid:{bid},ask:{ask}})
		"""
		Cypher(qStr).on(
			"instrument" -> instrument,
			"time0" -> agent.tick0.time.getMillis(),
			"scale" -> agent.scale,
			"exposure" -> agent.exposure,
			"active" -> agent.active,
			"time" -> agent.tickMax.time.getMillis(),
			"bid" -> agent.tickMax.bid,
			"ask" -> agent.tickMax.ask
			)()//.headOption.map( elt => println( CypherRow.unapplySeq(elt)) )
			.collect {
				case CypherRow(i: BigDecimal) if i == 1 => println("FOUND AGENT!")//println(row[Int]("exposure"))
			}
			
			true
	}

	def saveClose(agent: OvershootTrader)(implicit instrument: String): Boolean = {
		// find the agent
		val qStr = """
		MATCH (t0: Price)-[x:CREATE_AGENT]->(a:Agent)<-[tm:TickMax]-(tmax:Price) 
		WHERE t0.instrument = {instrument} AND t0.time = {time0} AND x.scale = {scale} 
		MATCH (a:Agent)-[*0..]->(b:Agent)
		WHERE NOT ((b)-->())
		CREATE (b)-[z:CLOSE]->(c:Agent {exposure:{exposure}, active: {active}})
		<-[zz:TickClose]-(tt:Price {instrument:{instrument},time:{time},bid:{bid},ask:{ask}})
		"""
		Cypher(qStr).on(
			"instrument" -> instrument,
			"time0" -> agent.tick0.time.getMillis(),
			"scale" -> agent.scale,
			"exposure" -> agent.exposure,
			"active" -> false,
			"time" -> agent.tickMax.time.getMillis(),
			"bid" -> agent.tickMax.bid,
			"ask" -> agent.tickMax.ask
			)()//.headOption.map( elt => println( CypherRow.unapplySeq(elt)) )
			.collect {
				case CypherRow(i: BigDecimal) if i == 1 => println("FOUND AGENT!")//println(row[Int]("exposure"))
			}
			
			true
	}

}