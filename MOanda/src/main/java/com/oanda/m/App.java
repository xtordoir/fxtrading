package com.oanda.m;

import com.fxtrade.PositionManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {

        
        PositionManager trader = new PositionManager();
        trader.setPosition("S");
        trader.factor = 0.001;
        trader.maxN = 5;
        trader.pair = "EUR/USD";
        trader.thres = 0.002;
        trader.run();
        
        
        /*
        Client client = new Client();
        client.login();
        client.home();
        client.tradeSetPosition("S", "EUR/USD", -10);
        client.logout();
         * 
         */
        /*
        client.summary();
        Map<String, Position> positions = client.positions();
        for (Entry<String, Position> pos : positions.entrySet()) {
            System.out.println(pos.getValue().getPosition() + "\t" + pos.getValue().getPair() +
                    "\t" + pos.getValue().getUnits() + "\t" +
                    pos.getValue().getProfit());
        }
        client.closePosition("EUR/USD");

        Map<String, Rate> rates = client.rates();
        for (String pair : rates.keySet()) {
            Rate rate = rates.get(pair);
            System.out.println(rate.getPair() + "\t" +
                    rate.getBid() + "\t" +
                    rate.getAsk() + "\t" +
                    rate.spread()*10000/rate.getBid()
                    );
        }
        //client.trade("S", "EUR/USD", 0.0005);
        client.logout();
         *
         */
    }


}
