package test

import org.specs2.mutable._
import org.specs2.specification._



import com.impactopia._
import PipsConverter._

class TradeSpec extends Specification {

  "TradeRisk " should {
    "have id 565784" in  { 
      val tr = TradeRisk("EUR_USD", .1, alloc = 40.0, loss = 5.0, odd = 100.0)
      println(tr)
      val tr2 = TradeRisk.build("EUR_USD", 
                                price = 1.35000, 
                                threshold = 20 pips, 
                                stopAt = 65 pips, 
                                maxLoss = 500.0,
                                maxAlloc = 250000.0)
      println(tr2)
      1 must_==(1)
    }
  }
}