package com.impactopia.hff.trader

import com.impactopia.hff.Tick


/**
* A trader is an agent that is started and is sent ticks
**/
abstract trait Trader {
	// exposure is the position (in units), signed
	val exposure: Int
	// realized average
    val realizedAvgPrice: Double
    // average price of open position
    val avgPrice: Double
    // total realized profit
    val totalRealizedProfit: Double

	// Gminus/Gplus is the amount of exposure reduction/increase on a tick
	// G sign is such that new exposure is exposure + G
	val G: Tick => Option[Int]

	// This function computes the profit resulting in partial
	// closing of exposure
	val eventProfit: (Tick, Int) => Double	
	val side: Int = 1

	// compute a new (exposure, avgPrice, realizedAvgPrice, totalRealizedProfit)
	def newState(tick: Tick, evtProfit: (Tick, Int) => Double = eventProfit, gfunc: Tick => Option[Int] = G): (Int, Double, Double, Double) = gfunc(tick) match {
		// exposure reduction
		case Some(x: Int) if (x * 1.0*exposure < 0 && exposure + x != 0) => {
			println("reduction: " + x + " of " + exposure)
			//val eventProfit = x * tick.bid
			val newExposure = exposure + x
			println("event profit = " + evtProfit(tick, x))
			val newAvgPrice = ( (avgPrice - priceBar(tick)) * exposure - evtProfit(tick, x)) / newExposure + priceBar(tick)
			val newTotalRealizedProfit = evtProfit(tick, x) + totalRealizedProfit
			val newRealizedAvgPrice = ((newAvgPrice - priceBar(tick))*newExposure + newTotalRealizedProfit)/newExposure + priceBar(tick)
			(newExposure, newAvgPrice, newRealizedAvgPrice, newTotalRealizedProfit)
		}
		// exposure increase
		case Some(x: Int) if (x * 1.0*exposure > 0) => {
			println("increase: " + x + " of " + exposure)
			//val eventProfit = 0
			val newExposure = exposure + x
			val newAvgPrice = ( avgPrice * exposure + price(tick) * x ) / newExposure
			val newRealizedAvgPrice = ((newAvgPrice - priceBar(tick))*newExposure + totalRealizedProfit)/newExposure + priceBar(tick)
			(newExposure, newAvgPrice, newRealizedAvgPrice, totalRealizedProfit)
		}
		// close position
		case Some(x: Int) if (exposure != 0 && exposure + x == 0) => {
			//val eventProfit = x * tick.bid
			val newExposure = exposure + x
			val newAvgPrice = 0.0
			val newTotalRealizedProfit = evtProfit(tick, x) + totalRealizedProfit
			val newRealizedAvgPrice = 0.0
			(newExposure, newAvgPrice, newRealizedAvgPrice, newTotalRealizedProfit)
		}
		case _ => (exposure, avgPrice, realizedAvgPrice, totalRealizedProfit)
	}

	def send(newtick: Tick): Trader

	def profit(tick: Tick): Double = (priceBar(tick) - realizedAvgPrice) * exposure

	def priceBar(tick: Tick): Double = if (side > 0) tick.bid else tick.ask
	def price(tick: Tick): Double = if (side > 0) tick.ask else tick.bid

}

