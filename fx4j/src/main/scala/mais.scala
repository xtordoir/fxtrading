
import com.impactopia.hff.Tick
import com.impactopia.hff.trader.OvershootTrader
import org.joda.time.DateTime

//case class Tick(instrument: String, time: Int, bid: Double, ask: Double)

case class Agent(exposure: Int, active: Boolean, tickMax: Tick, 
	scale: Double, tick0: Tick, p0: Int, stopOS: Double)

import com.impactopia.hff.fx4j._

object Main extends App with FX4j{
	
	import org.anormcypher._

	import com.impactopia.hff.fx4j._


	val t0 = Tick(new DateTime(), 1.3500, 1.3501)
	val a0 = OvershootTrader(t0, 0.0005, 150000, 100, 10, 1)

	val t1 = Tick(new DateTime(), 1.3498, 1.3499)
	val t2 = Tick(new DateTime(), 1.3496, 1.3497)
	val t3 = Tick(new DateTime(), 1.3493, 1.3494)
	val t4 = Tick(new DateTime(), 1.3483, 1.3484)

	implicit val instrument = "EUR_USD"

//	val s = Tick4j.save(t0)
//	val s = OvershootTrader4j.saveNew(a0)

	Init(a0, instrument)

	val a1 = Send(a0, t1)
	val a2 = Send(a1, t2)
	val a3 = Send(a2, t3)
	val a4 = Send(a3, t4)
	val a5 = Send(a4, t2)
	//val s2 = Send(s1, t1)

	//OvershootTrader4j.saveUpdate(a0)

/*
	SendF(instrument)(t0)(a0) { instrument: String => tick: Tick => agent: OvershootTrader => {
		val nagent = agent.send(tick)
		//save(agent, nagent)(instrument)
		nagent
		} 
	}
*/
//	println(s)
/*	val tmax = Tick("EUR_USD", 1, 1.3515, 1.3516)

	val agent = Agent(1000, true, t0, 0.01, t0, 1000, 1.2)

	val cyphStr = """
		create(n:Price {instrument:{instrument},time:{time}, bid:{bid}, ask:{ask}})
		"""
	Cypher(cyphStr).on("instrument" -> t0.instrument,
						"time" -> t0.time,
						"bid" -> t0.bid,
						"ask" -> t0.ask
							).execute()

	// create a full path...
	val pathStr = """
		create (n:Price {instrument:{instrument},time:{time}, bid:{bid}, ask:{ask}})
		-[a:CREATE_AGENT {scale: {scale}, p0: {p0}, stopOS: {stopOS}}]->
		(y:Agent {exposure:{exposure}, active: {active}})
	""" 

	val res = Cypher(pathStr).on(
						"instrument" -> "EUR_USD",
						"time" -> t0.time,
						"bid" -> t0.bid,
						"ask" -> t0.ask,
						"scale" -> agent.scale,
						"p0" -> agent.p0,
						"stopOS" -> agent.stopOS,
						"exposure" -> 1000,
						"active" -> true
		).execute()

	println(res)

	// create some test nodes
	Cypher("""create (anorm {name:"AnormCypher"}), (test {name:"Test"})""").execute()

	Cypher("""create (n:Price {instrument:{instrument},time:{time}, bid:{bid}, ask:{ask}})""")
	.on("instrument" -> "EUR_USD", "time" -> 0, "bid" -> 1.35000, "ask" -> 1.35010).execute()



	// a simple query
	val req = Cypher("start n=node(*) return COALESCE(n.name, \"\") AS name")

	// get a stream of results back
	val stream = req()

	// get the results and put them into a list
	stream.map(row => {row[String]("name")}).toList.map(println(_))
*/
}