/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oanda.m;

/**
 *
 * @author xavier
 */
public class Summary {
    private String name;
    private String currency;
    private double balance;
    private double unrealizedPL;
    private double nav;
    private double PL;
    private double usedMargin;
    private double availableMargin;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the currency
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * @param currency the currency to set
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * @return the balance
     */
    public double getBalance() {
        return balance;
    }

    /**
     * @param balance the balance to set
     */
    public void setBalance(double balance) {
        this.balance = balance;
    }

    /**
     * @return the unrealizedPL
     */
    public double getUnrealizedPL() {
        return unrealizedPL;
    }

    /**
     * @param unrealizedPL the unrealizedPL to set
     */
    public void setUnrealizedPL(double unrealizedPL) {
        this.unrealizedPL = unrealizedPL;
    }

    /**
     * @return the nav
     */
    public double getNav() {
        return nav;
    }

    /**
     * @param nav the nav to set
     */
    public void setNav(double nav) {
        this.nav = nav;
    }

    /**
     * @return the PL
     */
    public double getPL() {
        return PL;
    }

    /**
     * @param PL the PL to set
     */
    public void setPL(double PL) {
        this.PL = PL;
    }

    /**
     * @return the usedMargin
     */
    public double getUsedMargin() {
        return usedMargin;
    }

    /**
     * @param usedMargin the usedMargin to set
     */
    public void setUsedMargin(double usedMargin) {
        this.usedMargin = usedMargin;
    }

    /**
     * @return the availableMargin
     */
    public double getAvailableMargin() {
        return availableMargin;
    }

    /**
     * @param availableMargin the availableMargin to set
     */
    public void setAvailableMargin(double availableMargin) {
        this.availableMargin = availableMargin;
    }
      

}
