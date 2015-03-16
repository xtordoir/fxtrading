/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fxtrade;

import com.oanda.m.Client;
import com.oanda.m.Position;
import com.oanda.m.Rate;
import com.oanda.m.TradeMsg;
import java.util.Map;

/**
 *
 * @author xavier
 */
public class PositionManager {

    private Client moanda;
    public double basePrice;
    public double thres;
    public double maxN;
    public double factor;
    private int direction;
    public String pair;

    public void run() throws Exception {
        moanda = new Client();
        moanda.login();
        Map<String, Rate> rates = moanda.rates();
        basePrice = (rates.get(pair).getAsk() + rates.get(pair).getBid()) / 2;
        double g = this.gear(rates.get(pair).price());
        long units = 0;
        try {
            units = moanda.positions().get(pair).getSignedUnits();
        } catch (Exception ee) {
        }
        long baseUnits = moanda.pairTradeUnits(this.getPosition(), pair, factor);
        long expectedUnits = Math.round(g * baseUnits);
        String tradeSL = "L";
        if (expectedUnits < units) {
            tradeSL = "S";
        }
        TradeMsg mess;
        if (expectedUnits != units) {
            System.out.println();
            mess = moanda.tradeSetPosition(this.getPosition(), pair, expectedUnits);
            System.out.println("Trade: " + this.getPosition() + " " + pair);
            System.out.println(mess.position + " " + mess.units + " " + mess.pair + " @" + mess.price);
        }

        boolean flag = true;
        while (flag) {
            Thread.sleep(10000);
            rates = moanda.rates();
            g = this.gear(rates.get(pair).price());
            units = 0;
            try {
                units = moanda.positions().get(pair).getSignedUnits();
            } catch (Exception ee) {
            }

            //long baseUnits = moanda.pairTradeUnits(this.getPosition(), pair, factor);
            expectedUnits = Math.round(g * baseUnits);
            System.out.println("Expected units @ " + rates.get(pair).price() + " = " + expectedUnits + " Operation :"
                    + (expectedUnits - units));
            if (expectedUnits != units) {
                mess = moanda.tradeSetPosition(this.getPosition(), pair, expectedUnits);
                System.out.println("Trade: " + this.getPosition() + " " + pair);
                System.out.println(mess.position + " " + mess.units + " " + mess.pair + " @" + mess.price);
            }
        }


        moanda.logout();
    }

    protected String getPosition() {
        return direction > 0 ? "L" : "S";
    }

    public void setPosition(String position) throws Exception {
        if (position.equals("L")) {
            direction = 1;
        } else if (position.equals("S")) {
            direction = -1;
        } else {
            throw (new Exception("Position must be L or S"));
        }
    }

    protected double gear(double price) {
        double gear = 1.0;
        if (direction * (price - basePrice) > 0) {
            gear = direction;
        } else if (Math.abs(price - basePrice) / thres / basePrice < maxN) {
            gear = direction * Math.exp(Math.log(2) * Math.abs(price - basePrice) / thres);
        } else {
            gear = maxN;
        }
        return gear;
    }

    protected double clientGear() throws Exception {
        Map<String, Position> positions = moanda.positions();
        long units = positions.get(pair).getUnits();
        return units;
    }
}
