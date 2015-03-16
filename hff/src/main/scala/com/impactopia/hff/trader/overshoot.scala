package com.impactopia.hff.trader

import com.impactopia.hff.Tick


case class OvershootTrader(override val exposure: Int, tickMax: Tick, active: Boolean,
	override val avgPrice: Double, override val realizedAvgPrice: Double, override val totalRealizedProfit: Double,
	scale: Double, tick0: Tick, p0: Int, stopOS: Double)  extends Trader {

	lazy val overshoot = math.log(tickMax.price/tick0.price)/scale
	override val side: Int = math.signum(p0)

	// the profit generated from a position reduction is always
	// a complte position close
	// the profit is then
	override val eventProfit: (Tick, Int) => Double = (newtick: Tick, delta: Int) => {
		println("eventProfit " + newtick + " " + delta)
		-delta * (priceBar(newtick) - avgPrice)
	}

	override
	val G:  Tick => Option[Int] = (newtick: Tick) => {
		val nos = math.log(newtick.price/tick0.price)/scale
		// check if this tick reverses the overshoot
		// if abs reverse overshoot is larger than 1 and of opposite direction
		val ros = math.log(newtick.price/tickMax.price)/scale
		println("    OS " + overshoot)
		println("New OS " + nos)
		println("Rev OS " + ros)
		if (math.abs(ros) > 1.0 && math.signum(exposure) * ros > 0) {
			// we close position, full reversal
			println("FULL PROFIT")
			Some(-exposure)
			//Some(0)
		} 
		// if the overshoot extends below stop
		else if (math.abs(nos) > math.abs(overshoot) && math.signum(exposure) * nos < 0 && math.abs(nos) < stopOS ) {
			// we extend the postion
			val newExposure = (p0 * math.exp(math.abs(nos))).intValue
			println("EXTEND")

			Some(newExposure - exposure)
			//Some(newExposure)
		}
		// if the overshoot extends beyond stop
		else if (math.abs(nos) > math.abs(overshoot) && math.signum(exposure) * nos < 0 && math.abs(nos) >= stopOS) {
			// we close the position stop
			println("STOP")
			Some(-exposure)
			//Some(0)
		}
		// else do nothing
		else {
			println("DO NOTHING")
			None
		}
	}
	
	override
	def send(newtick: Tick): OvershootTrader = {

		println( (priceBar(newtick) - realizedAvgPrice)*exposure )
		val nst = newState(newtick)
		println("")
		println(nst)
		println("")
		println("")
		println("")
		nst match {
			case (newExposure, newA, newRA, newP) if (newExposure == 0) => {
				println("CLOSED POSITION")
				OvershootTrader(0, tickMax, false, 
 				newA, newRA, newP,
				scale, tick0, p0, stopOS)
			}
			case (newExposure, newA, newRA, newP) if (newExposure != exposure) => {
				println("CHANGED POSITION")
				OvershootTrader(newExposure, newtick, true, 
				newA, newRA, newP,
				scale, tick0, p0, stopOS)
			}
			case _ => this
		}
	}
}

object OvershootTrader {
	/** 
	* the Overshoot trade is initialized with:
	* tick0: the tick
	* scale: the scale
	***/
	def apply(tick0: Tick, scale: Double, alloc: Double, maxLoss: Double, odd: Double, side: Int): OvershootTrader = {
		val stopOS = math.log(odd + 1)
		val exposure = math.signum(side) * math.min( math.abs(maxLoss/(scale*(1-math.exp(stopOS)))) , alloc/math.exp(stopOS)).intValue
		OvershootTrader(exposure, tick0, true, 
			price(tick0, side), price(tick0, side), 0.0,
			scale, tick0, exposure, stopOS)
	}
	def price(tick: Tick, side: Int): Double = if (side > 0) tick.ask else tick.bid
	def priceBar(tick: Tick, side: Int): Double = if (side > 0) tick.bid else tick.ask

}








/**
* state of an overshoot trade (side is encoded in exposure sign):
*
* exposure: gear
* maxOS: maximum overshoot (corresponding to gear)
* maxTick: Tick of maximum
* 
* scale: scale
* alloc: maximm allocation
* maxLoss: maximum loss
* odd: Profit / loss odds
* os0: initial overshoot
* tick0: initial Tick
**/
case class OvershootTraderOLD(exposure: Double, tickMax: Tick, active: Boolean,
	scale: Double, tick0: Tick, p0: Double, stopOS: Double)  {

	lazy val overshoot = math.log(tickMax.price/tick0.price)/scale

	def send(newtick: Tick): OvershootTraderOLD = {
		val nos = math.log(newtick.price/tick0.price)/scale
		// check if this tick reverses the overshoot
		// if abs reverse overshoot is larger than 1 and of opposite direction
		val ros = math.log(newtick.price/tickMax.price)/scale
		println("    OS " + overshoot)
		println("New OS " + nos)
		println("Rev OS " + ros)
		if (math.abs(ros) > 1.0 && math.signum(exposure) * ros > 0) {
			// we close position, full reversal
			OvershootTraderOLD(0, tickMax, false, scale, tick0, p0, stopOS)
		} 
		// if the overshoot extends below stop
		else if (math.abs(nos) > math.abs(overshoot) && math.signum(exposure) * nos < 0 && math.abs(nos) < stopOS ) {
			// we extend the postion
			val newExposure = p0 * math.exp(math.abs(nos))
			OvershootTraderOLD(newExposure, newtick, true, scale, tick0, p0, stopOS)
		}
		// if the overshoot extends beyond stop
		else if (math.abs(nos) > math.abs(overshoot) && math.signum(exposure) * nos < 0 && math.abs(nos) >= stopOS) {
			// we close the position stop
			OvershootTraderOLD(0, tickMax, false, scale, tick0, p0, stopOS)
		}
		// else do nothing
		else {
			this
		}
	}
}

object OvershootTraderOLD {
	/** 
	* the Overshoot trade is initialized with:
	* tick0: the tick
	* scale: the scale
	***/
	def apply(tick0: Tick, scale: Double, alloc: Double, maxLoss: Double, odd: Double, side: Int): OvershootTraderOLD = {
		val stopOS = math.log(odd + 1)
		val exposure = math.signum(side) * math.min( math.abs(maxLoss/(scale*(1-math.exp(stopOS)))) , alloc/math.exp(stopOS))
		OvershootTraderOLD(exposure, tick0, true, scale, tick0, exposure, stopOS)
	}

}
