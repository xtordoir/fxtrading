/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.oanda.m;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author xavier
 */
public class Utils {

    public Properties loadProperties() throws IOException {
        Properties props = new Properties();
        InputStream is = this.getClass().getResourceAsStream("config.properties");
        System.out.println(is);
        props.load(is);
        return props;
    }



}
