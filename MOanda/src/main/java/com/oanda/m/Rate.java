/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oanda.m;

/**
 *
 * @author xavier
 */
public class Rate {
    private String pair;
    private double bid;
    private double ask;

    public double spread() {
        return ask-bid;
    }
    public double price() {
        return (bid+ask)/2;
    }
    /**
     * @return the pair
     */
    public String getPair() {
        return pair;
    }

    /**
     * @param pair the pair to set
     */
    public void setPair(String pair) {
        this.pair = pair;
    }

    /**
     * @return the bid
     */
    public double getBid() {
        return bid;
    }

    /**
     * @param bid the bid to set
     */
    public void setBid(double bid) {
        this.bid = bid;
    }

    /**
     * @return the ask
     */
    public double getAsk() {
        return ask;
    }

    /**
     * @param ask the ask to set
     */
    public void setAsk(double ask) {
        this.ask = ask;
    }
    
}
