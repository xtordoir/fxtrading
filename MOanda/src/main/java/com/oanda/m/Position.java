/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oanda.m;

/**
 *
 * @author xavier
 */
public class Position {
    private String position;
    private String pair;
    private long units;
    private double profit;

    public long getSignedUnits() {
        return position.equals("S") ? -units : units;
    }
    /**
     * @return the LS
     */
    public String getPosition() {
        return position;
    }

    /**
     * @param LS the LS to set
     */
    public void setPosition(String LS) {
        this.position = LS;
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
     * @return the units
     */
    public long getUnits() {
        return units;
    }

    /**
     * @param units the units to set
     */
    public void setUnits(long units) {
        this.units = units;
    }

    /**
     * @return the profit
     */
    public double getProfit() {
        return profit;
    }

    /**
     * @param profit the profit to set
     */
    public void setProfit(double profit) {
        this.profit = profit;
    }
    
}
