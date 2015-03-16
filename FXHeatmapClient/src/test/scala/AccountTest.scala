import ch.olsen.fxheatmap.scala._

import junit.framework.TestCase

/**
 *
 * @author xavier
 */

class AccountScalaTest extends TestCase {
    


    // TODO add test methods here. The name must begin with 'test'. For example:
    def testHello() = {
        val client = Client("xtordoir@yahoo.co.uk","taxi2035")
        client.login
        
        client.addPair("EUR_USD");
        
        val hmd = client.fetch();
        System.out.println(hmd.get("EUR_USD", 4.5D));
        System.out.println(hmd.get("EUR_USD", 3.2D));
        System.out.println(hmd.get("EUR_USD", 1.6D));
        System.out.println(hmd.get("EUR_USD", 0.8D));
        System.out.println(hmd.get("EUR_USD", 0.4D));
        System.out.println(hmd.get("EUR_USD", 0.2D));
        System.out.println(hmd.get("EUR_USD", 0.1D));
        System.out.println(hmd.get("EUR_USD", 0.05D));
        System.out.println(hmd.get("EUR_USD", 0.02D));
        System.out.println(hmd.get("EUR_USD", 1.0));
    }

}