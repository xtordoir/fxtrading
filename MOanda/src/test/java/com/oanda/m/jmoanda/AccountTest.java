/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oanda.m.jmoanda;

import com.oanda.m.Client;
import junit.framework.TestCase;
import java.util.Map.Entry;
/**
 *
 * @author xavier
 */
public class AccountTest extends TestCase {
    
    public AccountTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // TODO add test methods here. The name must begin with 'test'. For example:
    public void testHello() throws Exception {
        Client client = new Client();
        client.login();
        client.home();
        System.out.println(client.summary().getBalance() + "");
        for (Entry<String, String> e : client.getAccounts().entrySet()) {
            System.out.println("id:" + e.getKey() + "value:" + e.getValue());
        }
        System.out.println(client.getAccount());
        client.setAccount("5835725");
        client.home();
        System.out.println(client.getAccount());
        System.out.println(client.summary().getBalance() + "");
        client.logout();
    }

}
