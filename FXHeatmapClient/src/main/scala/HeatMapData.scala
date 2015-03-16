package ch.olsen.fxheatmap.scala

import scala.collection.JavaConverters._
import java.util.Date

case class PairHeatMapData(pair: String, os: List[(Double, Double)]) {

    def merge(data: PairHeatMapData): PairHeatMapData = {
        val newExtr = os.zip(data.os).map(elt => {
        	// if not a direction change and decrease, we keep old extremum
        	if (elt._1._2 * elt._2._2 > 0 && math.abs(elt._1._2) > math.abs(elt._2._2)) {
        		(elt._2._1, elt._1._2)
        	} else { // we take new data
        		(elt._2._1, elt._2._2)
        	}
        	})
        PairHeatMapData(pair, newExtr)
    }
}

object PairHeatMapData {

	val maxT = 4.5D
    val minT = 0.02D
    val nCols = 50

    def indexOf(t: Double): Int = {
    	val tt = t match {
    		case x if (x > maxT) => maxT
    		case x if (x < minT) => minT
    		case x => x
    	}
    	val xN = nCols - math.round(nCols*math.log(tt / minT) / math.log(maxT / minT))
    	val ret = xN match {
    		case x if (x >= nCols) => nCols - 1
    		case x => x.intValue  		
    	}
    	//println(t + " => " + ret)
    	ret
    }

    def tOf(i: Int): Double = {
    	0.0
    }
}

case class HeatMapData(hmd: Map[String, PairHeatMapData], date: Date = new Date) {
	def get(pair: String, t: Double): (Double, Double) = {
		hmd.get(pair) match {
			case Some(p) => p.os(PairHeatMapData.indexOf(t))
			case None => (0.0, 0.0)
		}
	}
    def merge(newmap: HeatMapData): HeatMapData = {
        HeatMapData( newmap.hmd.map(elt => (elt._1 -> hmd.get(elt._1).get.merge(elt._2))) )
    }

    def apply(pair: String): Option[PairHeatMapData] = {
        hmd.get(pair)
    }
}

object HeatMapData {

	implicit def converter(hmd: ch.olsen.fxheatmap.widget.client.HeatMapData): HeatMapData = {
		//println(hmd.date)
		val cies: List[String] = java.util.Arrays.asList(hmd.ccyPairs: _*).asScala.toList
		val ciesOss = cies.zipWithIndex.map{ case (cy, i) => {
			(cy -> java.util.Arrays.asList(hmd.heatMap(i): _*).asScala.toList.reverse)
			}}
		val pairsHMD:Map[String, PairHeatMapData] = ciesOss.map(elt => {
			val hmd = elt._2.map(ee => (ee, ee))
			(elt._1 -> PairHeatMapData(elt._1, hmd))
			}).toMap
        
		HeatMapData(pairsHMD)
	}
}