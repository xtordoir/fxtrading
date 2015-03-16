/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oanda.m;

/**
 *
 * @author xavier
 */
public class TradeMsg {

    public String position;
    public String pair;
    public long units;
    public Double price;

    TradeMsg() {
    }

    TradeMsg(String msg) throws Exception {
        String tokens[] = msg.split("[ ]+");
        if (tokens.length < 5) {
            throw (new Exception("wrong format"));
        }
        if (tokens[0].equals("Sold")) {
            position = "S";
        } else {
            position = "L";
        }
        units = Long.valueOf(tokens[1].replaceAll(",", ""));
        pair = tokens[2];
        price = Double.valueOf(tokens[4].replaceAll(",", ""));
    }
}
