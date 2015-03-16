
import spray.json._
import DefaultJsonProtocol._ 

/*class Color(val name: String, val red: Int, val green: Int, val blue: Int)

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit object ColorJsonFormat extends RootJsonFormat[Color] {
    def write(c: Color) =
      JsArray(JsString(c.name), JsNumber(c.red), JsNumber(c.green), JsNumber(c.blue))

    def read(value: JsValue) = value match {
      case JsArray(JsString(name) :: JsNumber(red) :: JsNumber(green) :: JsNumber(blue) :: Nil) =>
        new Color(name, red.toInt, green.toInt, blue.toInt)
      case _ => deserializationError("Color expected")
    }
  }
}

import MyJsonProtocol._

val json = Color("CadetBlue", 95, 158, 160).toJson
val color = json.convertTo[Color]

*/
import org.joda.time.DateTime

case class OrderbookAt(datetime: DateTime)
case class Orderbook(books: Seq[OrderbookAt])

object MyJsonProtocol extends DefaultJsonProtocol {
	implicit object OrderbookJsonFormat extends RootJsonFormat[Orderbook] {
		def write(c: Orderbook) =
			JsObject(Map[String, JsValue]())
		   //JsArray(JsString(c.name), JsNumber(c.red), JsNumber(c.green), JsNumber(c.blue))

    def read(value: JsValue) = value match {
    	case JsObject(mp: Map[String, JsValue]) if (mp.size >= 1) =>
    	    mp.keys.map(elt => {println(elt)
    	     println(elt.toInt)})
    		val books = mp.keys.map(k => 1000*k.toLong).toList.sortBy(x=>x).map(elt => OrderbookAt(new DateTime(elt))).toList
    		new Orderbook(books)
      case _ => deserializationError("Orderbook expected")
    }
  }	
}

import scala.io.Source

object Test {
   def main(args: Array[String]) {
      println("Following is the content read:" )
      val samplefile = this.getClass().getResource("/sample.json").toURI
      val jsonStr = Source.fromFile(samplefile).mkString("")
      //.foreach{ 
      //   print 
      //}
      println(jsonStr)
      import MyJsonProtocol._
      val orderbook = JsonParser(jsonStr).convertTo[Orderbook]
      println(orderbook)
   }
}