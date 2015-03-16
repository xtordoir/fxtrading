package com.impactopia.hff.trader

import com.impactopia.hff.Tick
/**
** *** state variables
*
* exposure: current position
* os: current overshoot (in units of lambda)
* tick: tick of last event
* avgPrice: average prive of position
* profit: profit from last event
* cumProfit: cummulated profit
*
** *** fixed parameters
* 
* side: 1 for long, -1 for short
* pi0: profit objective
* scale: lambda the scale
* gamma: position decrease factor ]0, 1]
* G: position variation function
* a0: initial average price
**/

case class CoastlineTrader2(override val exposure: Int, lastTick: Tick, active: Boolean,
	override val avgPrice: Double, override val realizedAvgPrice: Double, override val totalRealizedProfit: Double,
	scale: Double, gamma: Double, tick0: Tick, p0: Int
	) extends Trader {

	lazy val L = -(math.signum(exposure) * math.log(lastTick.price/tick0.price)/scale).intValue
	println("L value = " + L)
//	val avgPrice: Double = ???
//	val exposure: Int = ???
//	val realizedAvgPrice: Double = ???
//	val totalRealizedProfit: Double = ???
	// side is defined by the initial position
	override val side: Int = math.signum(p0)

	// this function returns the exposure shift for a given L value
	def GL(l: Int): Int = p0

	// this function returns the exposure shift from L up to l
	def GLacc(l0: Int, l1: Int): Int = {
		List.range(math.min(l0, l1)+1, math.max(l0,l1)+1).map(GL(_)).sum
	}

	def Gincr: Tick => Option[Int] = (newtick: Tick) => Some(p0)
	def Gdecr: Tick => Option[Int] = (newtick: Tick) => Some(-p0)

	// this function computes the exposure shift assuming a L -> L+1 or L -> L-1  or L -> L shift
	val G: Tick => Option[Int] = (newtick: Tick) => {

		if (newL(newtick) == L + 1 ) {
			// increase position...
			Some(GL(L+1))
		}
		else if (newL(newtick) == L - 1) {
			// decrease position
			Some(-GL(L))
		}
		else if (newL(newtick) == L) {
			// do nothing
			None
		} else {
			println("ooops shouldn't be here...")
			None
		} 
	}


	def eventProfitOnL(tick: com.impactopia.hff.Tick, delta: Int)(l0: Int, l1: Int): Double = {
		val g = GLacc(l0, l1)
		// loop on crossed Ls, get the corresponding Gear
		List.range(math.min(l0, l1)+1, math.max(l0,l1)+1).map(GL(_)).reverse.toList
		// addup the stuff with size of move (idex-based)
			.zipWithIndex.map(elt => (elt._2+1) * elt._1 * scale * tick.bid * gamma ).sum
		
	}
	val eventProfit: (com.impactopia.hff.Tick, Int) => Double = (tick: Tick, delta: Int) => {
		0.0
	}

	def newL(newtick: Tick): Int = -(math.signum(exposure) * math.log(newtick.price/tick0.price)/scale).intValue
	// the action of sending a tick involves a reccursive call
	// the reason is that an integer state controls the exposure and we need to deal with a
	// jump of several states
	def send(newtick: com.impactopia.hff.Tick): CoastlineTrader2 = {
		// the new L value, if different from previous one, there is an action to take!

		val currentProfit = (priceBar(newtick) - realizedAvgPrice)*exposure
		println( "current return = " + (priceBar(newtick) - realizedAvgPrice)*exposure )

		newL(newtick) - L match {
			case d: Int if (d > 0) => {
				val gd: Tick => Option[Int] = (tick: Tick) =>  Some(GLacc(L, L+d))
				val nst = newState(newtick, eventProfit, gd)
				println("")
				println(nst)
				println("")
				CoastlineTrader2(nst._1, newtick, true,
					nst._2, nst._3, nst._4,
					scale, gamma, tick0, p0)
			}
			case _ if (currentProfit > math.abs(p0*scale*2/3) ) => {
				// full take profit...
				CoastlineTrader2(0, newtick, false,
					0.0, 0.0, currentProfit,
					scale, gamma, tick0, p0)

			}			// if the decrease is larger than 1 (we go back...)
			case d: Int if (d < -1 && L > 1) => {
				println("DECREASE " + L + " for " + d )
				val gd: Tick => Option[Int] = (tick: Tick) => Some( (-gamma*GLacc(L-1, L-1 + d + 1)).intValue )

				val evtProfit: (Tick, Int) => Double = (tick: Tick, delta: Int) => {
					eventProfitOnL(tick, delta)(L-1, L-1+d+1)
				}
				val nst = newState(newtick, evtProfit, gd)
				println("")
				println(nst)
				println("")
				CoastlineTrader2(nst._1, newtick, true,
					nst._2, nst._3, nst._4,
					scale, gamma, tick0, p0)
			}
			case _ => this
		}
	}
}

case class CoastlineTrader(exposure: Double, tick: Tick, avgPrice: Double, profit: Double, cumProfit: Double,
	side: Int, pi0: Double, scale: Double, gamma: Double, G: Double => Double, a0: Double) {

	lazy val x = price(tick)
	lazy val xBar = priceBar(tick)
	lazy val os = (tick.price - a0)/a0/scale

	def send(newtick: Tick): CoastlineTrader = {
		// what is the new lambda?
		val newos = (newtick.price - a0)/a0/scale
		// is the new Overshoot the result of some events?
		// a positive value means Overshoot extension, a negative value means overshoot reduction
		val osShift =  - (newos.intValue - os.intValue) * math.signum(side)
		// the function to apply on each integer overshoot increase
		def runIncrease(x: Int, state: CoastlineTrader): CoastlineTrader = x match {
			case _ if (x >= 1) => {
				val incr = state.G(state.os.intValue - math.signum(side))
				val newExposure = state.exposure + incr
				val newAvgPrice = (state.avgPrice * state.exposure + price(tick) * incr) / newExposure
				val newProfit = state.profit
				val newCumProfit = state.cumProfit
				val newState = CoastlineTrader(newExposure, newtick, newAvgPrice, newProfit, newCumProfit,
					side, pi0, scale, gamma, G, a0)
				runIncrease(x-1, newState)
			}
			case 0 => state
		}
		// the function to apply on each integer overshoot decrease
		def runDecrease(x: Int, state: CoastlineTrader): CoastlineTrader = x match {
			case _ if (x >= -1) => {
				val incr = state.gamma * state.G(state.os.intValue)
				val newExposure = state.exposure - incr
				val newProfit = state.scale * incr * state.tick.bid
				val newAvgPrice = if (newExposure <= 0) { 0.0} else {
					(state.avgPrice * state.exposure + price(tick) * incr) / newExposure
				}
				val newCumProfit = state.cumProfit + newProfit
				val newState = CoastlineTrader(newExposure, newtick, newAvgPrice, newProfit, newCumProfit,
					side, pi0, scale, gamma, G, a0)
				runDecrease(x+1, newState)
			}
			case 0 => state
		}

		osShift match {
			case x: Int if (x >= 1) => {
				runIncrease(x, this)
			}
			case x : Int if (x <= -1) => {
				runDecrease(x, this)
			}
			case _ => this
		}
	}

	def priceBar(tick: Tick): Double = if (side > 0) tick.bid else tick.ask
	def price(tick: Tick): Double = if (side <= 0) tick.bid else tick.ask

}

/*
case class CoastlineTrader(exposure: Double, priceAverage: Double, overshoot: Double, length: Int, 
	profit: Double,
	gearIncrement: Double => Double,
	target: Double,
	gamma: Double, scale: Double, initOmega: Double, initPrice: Double) {

	def send(os: Double, price: Double): CoastlineTrader = {

		// if event extends overshoot
		if (os - overshoot >= 1) {
			val newExposure = exposure + gearIncrement(os)
			val newPriceAverage = (priceAverage * exposure + price * gearIncrement(os)) / newExposure
		}
		// if the event reduces overshoot
		else if (os - overshoot <= -1) {
			val newExposure = exposure - gamma * gearIncrement(overshoot)
			val newProfit = scale * gamma * gearIncrement(overshoot) * price + profit
			val newPriceAverage = ((priceAverage - price)*exposure - profit)/newExposure + price
		}

		CoastlineTrader(exposure, priceAverage, overshoot, length,
			gearIncrement,
			gamma, scale, initOmega, initPrice)
	}

}
*/