package com.impactopia

case class Pips(x: Double) {
    def pips = x/10000.0
    def of(p: Double) = pips / p
}
object Pips {
    implicit def apply(i: Int): Pips = Pips(i.doubleValue)
}

object PipsConverter {
    implicit def doubleToPips(x: Double): Pips = Pips(x)
}

import PipsConverter._
// TradeRisk contains RISK/REWARD INFO, NOT EXECUTION DETAILS

case class TradeRisk(pair: String, threshold: Double, alloc: Double, loss: Double, 
        odd: Double, p0: Double, profit: Double) { 
    override def toString(): String = {
        s"""pair: $pair
        Threshold: $threshold
        Initial Alloc: $p0
        Max Alloc: $alloc
        StopLoss Distance: """ + (Math.log(odd + 1.0) * threshold) + s""" %
        Loss: $loss
        Take Profit: $profit
        Odds: $odd
        """
    }
}

object TradeRisk {
    def apply(pair: String, threshold: Double, alloc: Double, loss: Double, 
        odd: Double): TradeRisk = {
        val elMax = odd + 1.0
        val p0alloc = alloc / elMax
        val p0loss = loss / threshold / (elMax - 1.0)
        val (np0, nloss, nalloc) = if (p0alloc < p0loss) { 
                (   p0alloc, 
                    threshold * p0alloc * (elMax - 1.0),
                    alloc
                )
            } else {
                (   p0loss,
                    loss,
                    p0loss * elMax
                    )
            }
        val nprofit = threshold * np0

        TradeRisk(pair, threshold, nalloc, nloss, odd, np0, nprofit)
    }

// this version counts in terms of money and price level
    def build(pair: String, price: Double, threshold: Double, stopAt: Double, maxLoss: Double, maxAlloc: Double) = {
        // odds is ratio between threshold and stop distance
        val odd = math.exp(stopAt/threshold)-1.0
        val thresholdRatio = threshold/price
        val p0alloc = maxAlloc / (odd+1.0)
        val p0loss = maxLoss / thresholdRatio / odd
        println(odd)
        println(100*thresholdRatio)
        println(p0alloc)
        println(p0loss)
        println( (10.0D).pips)
        TradeRisk(pair, thresholdRatio, maxAlloc, maxLoss, odd)
    }
}
// this class provides the money point of view
// a trade is defined by the money allocated to the trade
// the maximum loss
// the maximum distance to stop loss
// the threshold for take profit (in pips)

object TradeStatus {
    val PENDING = 1
    val OPENED = 2
    val PROFIT  = -3
    val STOPPED = -4
    val ABORTED = -5
}

// a Trade extends TradeRisk to incorporate:
// entry point, status and gear
// entry point (lambda at which creation was requested)
// tolerance for entry (delta between l and l0 to execute the opening)
// status
case class Trade(features: TradeRisk, lambda0: Double, lambdaExt: Double, delta: Double = 0.05, gear: Double = 0.0, status: Int = TradeStatus.PENDING) {
    def update(l: Double): Trade = {
        var (newLambdaExt, newGear, newStatus) = l match {
            case ll if (ll * lambda0 <= 0 && status == TradeStatus.PENDING) => {
                println("aborting trade")
                (lambdaExt, 0.0, TradeStatus.ABORTED)
            }
            case ll if (ll * lambda0 <= 0 && status == TradeStatus.OPENED) => {
                println("taking profit")
                (ll, 0.0, TradeStatus.PROFIT)
            }
            case ll if (status == TradeStatus.PENDING && ll * lambda0 > 0 &&
             math.abs(ll) - math.abs(lambda0) > -delta ) => {
                println("opening trade")
                (ll, -math.signum(ll)*1.0, TradeStatus.OPENED)
            }
            case ll if (status > 0 && math.abs(ll) > math.abs(lambda0) && math.abs(ll) > math.abs(lambdaExt) &&
             math.exp(math.abs(ll)-math.abs(lambda0)) <= (features.odd + 1.0) )  => {
                println("adjusting trade gear")
                println( status )
                println( ll )
                println( lambda0 )
                println( math.exp(math.abs(ll)-math.abs(lambda0)) )
                println( (features.odd + 1.0) )
                (ll, g(ll, lambda0), TradeStatus.OPENED)
            }
            case ll if (status > 0 && math.abs(ll) > math.abs(lambda0) &&
             math.exp(math.abs(ll)-math.abs(lambda0)) > (features.odd + 1.0) )  => {
                println("stopping trade")
                println( status )
                println( ll )
                println( lambda0 )
                println( math.exp(math.abs(ll)-math.abs(lambda0)) )
                println( (features.odd + 1.0) )
                (lambdaExt, 0.0, TradeStatus.STOPPED)
            }
            case ll if (status < 0) => (lambdaExt, 0.0, status)
            case _ => (lambdaExt, gear, status)
        }
        Trade(features, lambda0, newLambdaExt, delta, newGear, newStatus)
    }
    def g(ll: Double, l0: Double): Double = {
        if (l0 < 0) {
            gear.max(math.exp(math.abs(ll)-math.abs(l0)))
        } else {
            gear.min(- math.exp(math.abs(ll)-math.abs(l0)))
        }
    }

    def currentAllocation(): Double = gear*features.p0

}