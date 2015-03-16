package com.impactopia.hff

import com.impactopia.oanda.entities._

case class Overshoot(instrument: String, scale: Double, direction: Int, prevExt: Double, ext: Double, 
		maxOS: Double, extDist: Double) {

	def send(price: Price): Overshoot = {

		val os = 100*(price.price - prevExt)/prevExt/scale
		val eDist = 100*(price.price - ext)/ext/scale
		// if reversal...
		if (os*eDist < 0 && math.abs(eDist) > 1.0) {
			Overshoot(instrument, scale, -direction, ext, price.price, 1.0, 0.0)
		} else {
			//println(math.abs(os) + " vs " + maxOS)
			//println(math.abs(eDist) + " vs " + extDist)

			val (newExt, newMaxOS) = if (math.abs(os) > maxOS) { (price.price, math.abs(os)) } else { (ext, maxOS)}
			val newExtDist = math.abs(100*(price.price - newExt)/ext/scale)
			//println(prevExt + " " + newExt+ " " + newMaxOS+ " " + newExtDist)
			Overshoot(instrument, scale, direction, prevExt, newExt, newMaxOS, newExtDist)
		}
	}

	def send(priceOpt: Option[Price]): Overshoot = priceOpt match {
		case Some(price: Price) => send(price)
		case None => this
	}

}
object Overshoot {
	def apply(instrument: String, scale: Double): Overshoot = Overshoot(instrument, scale,
		1, 1.0, 1.0, 0.0, 0.0)

	def apply(scale: Double, price: Price): Overshoot = Overshoot(price.instrument, scale,
		1, price.price, price.price, 0.0, 0.0)

	def init(scale: Double, candles: Candles[Candle]): Option[Overshoot] = {

		// the tuple is (min, max, direction, 
		def run(scale: Double, tup: Tuple4[Double, Double, Int, Int], candles: Seq[Candle]): Tuple3[Double, Double, Int] = candles match {
			// explicit reversal with new
			case head :: tail if (tup._3 == 1 && 100*(tup._2 - head.closeMid)/tup._2 > scale) => (tup._1, tup._2, -1)
			case head :: tail if (tup._3 == -1 && 100*(head.closeMid - tup._1)/tup._1 > scale) => (tup._1, tup._2, 1)
			// new minimum
			case head :: tail if (head.lowMid < tup._1 && head.highMid < tup._2) => run(scale, (head.lowMid, tup._2, -1, 0), tail)
			// new maximum
			case head :: tail if (head.highMid > tup._2 && head.lowMid > tup._1) => run(scale, (tup._1, head.highMid, 1, 0), tail)
			// concurrent new max and min...
			case head :: tail if (head.highMid > tup._2 && head.lowMid < tup._1) => run(scale, (head.lowMid, head.highMid, 0, 0), tail)
			// nothing...
			case head :: tail => run(scale, tup, tail)
			case Nil => (tup._1, tup._2, 0)
		}
		val llast = candles.candles.last
		println(llast)
		val tup = run(scale, (llast.lowMid, llast.highMid, 0, 0), candles.candles.reverse)
		println(tup)
		tup match {
			case (low: Double, high: Double, i: Int) if (i == 1) => 
				Some(Overshoot("EUR_USD", scale, i, low, high, 100*(high-low)/low/scale, 100*(high-llast.closeMid)/high/scale))
			case (low: Double, high: Double, i: Int) if (i == -1) => 
				Some(Overshoot("EUR_USD", scale, i, low, high, 100*(high-low)/low/scale, 100*(llast.closeMid-low)/low/scale))
			case _ => None
		}
		
	}
}
// used to store a sequence of overshoot (or a heatmap row...)
case class Overshoots(overshoots: Seq[Overshoot]) {
	def send(price: Price): Overshoots = Overshoots(overshoots.map(_.send(price)))
	def send(priceOpt: Option[Price]): Overshoots = Overshoots(overshoots.map(_.send(priceOpt)))
}

object Overshoots {
}





