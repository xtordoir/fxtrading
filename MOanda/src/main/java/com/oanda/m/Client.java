/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oanda.m;

import com.oanda.m.Exception.ErrorException;
import com.oanda.m.Exception.NotLoggedException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author xavier
 */
public class Client {

    private Properties properties;
    private DefaultHttpClient httpclient = new DefaultHttpClient();
    private String home = "";

    private String username;
    private String password;

    private Map<String, String> accounts = new HashMap<String, String>();
    private String account = "";
    private String account_token = "";

    protected String getLoginURL() {
        return properties.getProperty("login.url")+"?"+properties.getProperty("login.query");
    }

    protected String getHome() {
        return home;
    }

    /**
     * Constructor read the default properties file to get URLs
     * @throws Exception
     */
    public Client() throws Exception {
        properties = new Properties();
        InputStream is = this.getClass().getResourceAsStream("config.practice.properties");
        properties.load(is);
        home = properties.getProperty("home.url");
    }
/**
 *
 * @param configFile
 * @throws Exception
 */
    public Client(File configFile) throws Exception {
        properties = new Properties();
        properties.load(new FileInputStream(configFile));
        home = properties.getProperty("home.url");
    }

    /**
     * Method for the login operation
     * @throws Exception
     */
    public void login() throws Exception {
        // FIRST GET THE LOGIN PAGE FOR COOKIES
        HttpGet httpget = new HttpGet(this.getLoginURL());
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            entity.consumeContent();
        }

        // SECOND, POST THE LOGIN FORM
        HttpPost httpost = new HttpPost(this.getLoginURL());

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("username", properties.getProperty("username")));
        nvps.add(new BasicNameValuePair("password", properties.getProperty("password")));

        httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

        response = httpclient.execute(httpost);
        entity = response.getEntity();

        if (entity != null) {
            entity.consumeContent();
        }
        // failed if code 200 (return to login page)
        if (response.getStatusLine().getStatusCode() == 200) {
            throw (new Exception("Login failed"));
        }
        // code 500 for error
        if (response.getStatusLine().getStatusCode() == 500) {
            throw new ErrorException();
        }
        // code 302 for redirect to next page
        if (response.getStatusLine().getStatusCode() == 302) {
            for (Header header : response.getAllHeaders()) {
                if (header.getName().equalsIgnoreCase("location")) {
                    home = header.getValue();
                    break;
                }
            }
        }
    }

    public void logout() throws Exception {
        HttpGet httpget = new HttpGet(this.getHome() + "logout");
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            entity.consumeContent();
        }
    }

    public void home() throws NotLoggedException, IOException, ErrorException {
        final HttpParams params = new BasicHttpParams();
        HttpClientParams.setRedirecting(params, false);
        HttpGet httpget = new HttpGet(this.getHome());
        httpget.setParams(params);
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();
        String page = null;
        if (entity != null) {
            page = EntityUtils.toString(entity);
            entity.consumeContent();
        }       // code 500 for error
        if (response.getStatusLine().getStatusCode() == 500) {
            throw new ErrorException();
        }
        // code 302 for redirect to login means we are not logged
        if (response.getStatusLine().getStatusCode() == 302) {
            String redirect = response.getFirstHeader("location").getValue();
            if (redirect.startsWith(properties.getProperty("login.url"))) {
                throw new NotLoggedException();
            }
        }

        Document parse = Jsoup.parse(page);
        this.account = parse.select("#account").text();

    }

    public String getAccount() throws Exception {
        this.home();
        return this.account;
    }

    public Map<String, String> getAccounts() throws Exception {
        final HttpParams params = new BasicHttpParams();
        HttpClientParams.setRedirecting(params, false);
        HttpGet httpget = new HttpGet(this.getHome() + "account/change");
        httpget.setParams(params);
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();
       // code 302 for redirect to login means we are not logged
        if (response.getStatusLine().getStatusCode() == 302) {
            String redirect = response.getFirstHeader("location").getValue();
            if (redirect.startsWith(properties.getProperty("login.url"))) {
                throw new NotLoggedException();
            }
        }
        // code 500 for error
        if (response.getStatusLine().getStatusCode() == 500) {
            throw new ErrorException();
        }
        String page = null;
        if (entity != null) {
            page = EntityUtils.toString(entity);
            entity.consumeContent();
        }
        Document parse = Jsoup.parse(page);
        Elements elts = parse.select("input[name=account]");


        //System.out.println(account + " : " + currency);
        for (Element elt : elts) {
            String id = elt.attr("value");
            String label = parse.select("label[for=" + id  + "]").text();
            this.accounts.put(id, label);
        }
        this.account_token = parse.select("input[name=_token]").attr("value");
        return accounts;
    }

    public void setAccount(String id) throws Exception {
        this.getAccounts();
        if (! this.accounts.containsKey(id)) {
            System.err.println("sub-account " + id + " not found");
            return;
        }
        HttpPost httpost = new HttpPost(this.getHome() + "/account/change");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("_token", this.account_token));
        nvps.add(new BasicNameValuePair("account", id));

        httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
        HttpResponse response = httpclient.execute(httpost);
        HttpEntity entity = response.getEntity();

        String page = null;
        if (entity != null) {
            page = EntityUtils.toString(entity);
            entity.consumeContent();
        }
        //System.out.println(page);
                //System.out.println(response.getStatusLine());
        // failed if code 200 (return to login page)
        if (response.getStatusLine().getStatusCode() != 302) {
            throw (new Exception("Set account failed + " + response));
        }
    }

    public Summary summary() throws Exception {
        Summary summary = new Summary();
        final HttpParams params = new BasicHttpParams();
        HttpClientParams.setRedirecting(params, false);
        HttpGet httpget = new HttpGet(this.getHome() + "account/summary");
        httpget.setParams(params);
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();

        // code 302 for redirect to login means we are not logged
        if (response.getStatusLine().getStatusCode() == 302) {
            String redirect = response.getFirstHeader("location").getValue();
            if (redirect.startsWith(properties.getProperty("login.url"))) {
                throw new NotLoggedException();
            }
        }
        // code 500 for error
        if (response.getStatusLine().getStatusCode() == 500) {
            throw new ErrorException();
        }
        String page = null;
        if (entity != null) {
            page = EntityUtils.toString(entity);
            entity.consumeContent();
        }
        Document parse = Jsoup.parse(page);
        Elements elts = parse.select("tr");

        String account = "";
        double balance = 0, uPL = 0, nav = 0, PL = 0, usedMargin = 0, availMargin = 0;
        account = elts.first().select("th").text();
        String currency = elts.first().select("td").text();
        summary.setCurrency(currency);
        summary.setName(account);
        //System.out.println(account + " : " + currency);
        for (Element elt : elts) {
            if (elt.select("th").text().equals("Balance")) {
                balance = Double.valueOf(elt.select("td").text().replaceAll(",", ""));
                summary.setBalance(balance);
                //System.out.println("Balance = " + balance);
            } else if (elt.select("th").text().equals("Unrealized P&L")) {
                uPL = Double.valueOf(elt.select("td").text().replaceAll(",", ""));
                summary.setUnrealizedPL(uPL);
                //System.out.println("uPL = " + uPL);
            } else if (elt.select("th").text().equals("Net Asset Value")) {
                nav = Double.valueOf(elt.select("td").text().replaceAll(",", ""));
                summary.setNav(nav);
                //System.out.println("NAV = " + nav);
            } else if (elt.select("th").text().equals("Realized P&L")) {
                PL = Double.valueOf(elt.select("td").text().replaceAll(",", ""));
                summary.setPL(PL);
                //System.out.println("PL = " + PL);
            } else if (elt.select("th").text().equals("Margin Used")) {
                usedMargin = Double.valueOf(elt.select("td").text().replaceAll(",", ""));
                summary.setUsedMargin(usedMargin);
                //System.out.println("Used margin = " + usedMargin);
            } else if (elt.select("th").text().equals("Margin Available")) {
                availMargin = Double.valueOf(elt.select("td").text().replaceAll(",", ""));
                summary.setAvailableMargin(availMargin);
                //System.out.println("Available margin = " + availMargin);
            }
        }
        return summary;
    }

    public Map<String, Position> positions() throws Exception {
        Map<String, Position> ret = new HashMap<String, Position>();
        final HttpParams params = new BasicHttpParams();
        HttpClientParams.setRedirecting(params, false);
        HttpGet httpget = new HttpGet(this.getHome() + "position/view");
        httpget.setParams(params);
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();

        // code 302 for redirect to login means we are not logged
        if (response.getStatusLine().getStatusCode() == 302) {
            String redirect = response.getFirstHeader("location").getValue();
            if (redirect.startsWith(properties.getProperty("login.url"))) {
                throw new NotLoggedException();
            }
        }
        // code 500 for error
        if (response.getStatusLine().getStatusCode() == 500) {
            throw new ErrorException();
        }
        String page = null;
        if (entity != null) {
            page = EntityUtils.toString(entity);
            entity.consumeContent();
        }
        Document parse = Jsoup.parse(page);
        Element tbody = null;
        try {
            tbody = parse.getElementsByTag("tbody").first();
        
        Elements trs = tbody.getElementsByTag("tr");
        for (Element tr : trs) {
            Position position = new Position();
            Elements tds = tr.getElementsByTag("td");
            position.setPosition(tds.get(0).text());
            position.setPair(tds.get(1).text());
            position.setUnits(Long.valueOf(tds.get(2).text().replaceAll(",", "")));
            position.setProfit(Double.valueOf(tds.get(3).text().replaceAll(",", "").replaceAll(" x", "")));
            ret.put(position.getPair(), position);
            
        }
        } catch (Exception ee) {
            return ret;
        }
        return ret;
    }

    public void closePosition(String pair) throws NotLoggedException, IOException, Exception {
        List<Position> ret = new ArrayList<Position>();
        final HttpParams params = new BasicHttpParams();
        HttpClientParams.setRedirecting(params, false);
        HttpGet httpget = new HttpGet(this.getHome() + "/position/close/" + pair);
        httpget.setParams(params);
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();

        // code 302 for redirect to login means we are not logged
        if (response.getStatusLine().getStatusCode() == 302) {
            String redirect = response.getFirstHeader("location").getValue();
            if (redirect.startsWith(properties.getProperty("login.url"))) {
                throw new NotLoggedException();
            }
        }
        // code 500 for error
        if (response.getStatusLine().getStatusCode() == 500) {
            throw new ErrorException();
        }
        String page = null;
        if (entity != null) {
            page = EntityUtils.toString(entity);
            entity.consumeContent();
        }

        Document parse = Jsoup.parse(page);
        Element form = parse.select("form").first();
        Element tokenInput = form.select("input[name=_token]").first();
        String token = tokenInput.attr("value");
        //System.out.println(token);

        // SECOND, POST THE CLOSE POSITION FORM
        HttpPost httpost = new HttpPost(this.getHome() + "/position/close/" + pair);

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("_token", token));

        httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

        response = httpclient.execute(httpost);
        entity = response.getEntity();

        if (entity != null) {
            entity.consumeContent();
        }
        //System.out.println(response.getStatusLine());
        // failed if code 200 (return to login page)
        if (response.getStatusLine().getStatusCode() != 200) {
            //  throw (new Exception("Close positions failed"));
        }
    }

    public Map<String, Rate> rates() throws Exception {
        Map<String, Rate> ret = new HashMap<String, Rate>();
        final HttpParams params = new BasicHttpParams();
        HttpClientParams.setRedirecting(params, false);
        HttpGet httpget = new HttpGet(this.getHome() + "/rates");
        httpget.setParams(params);
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();

        // code 302 for redirect to login means we are not logged
        if (response.getStatusLine().getStatusCode() == 302) {
            String redirect = response.getFirstHeader("location").getValue();
            if (redirect.startsWith(properties.getProperty("login.url"))) {
                throw new NotLoggedException();
            }
        }
        // code 500 for error
        if (response.getStatusLine().getStatusCode() == 500) {
            throw new ErrorException();
        }
        String page = null;
        if (entity != null) {
            page = EntityUtils.toString(entity);
            entity.consumeContent();
        }

        Document parse = Jsoup.parse(page);
        Element tbody = parse.getElementsByTag("tbody").first();
        for (Element tr : tbody.getElementsByTag("tr")) {
            Elements tds = tr.getElementsByTag("td");
            if (tds.size() == 3) {
                Rate rate = new Rate();
                rate.setPair(tds.get(0).text());
                rate.setBid(Double.valueOf(tds.get(1).text()));
                rate.setAsk(Double.valueOf(tds.get(2).text()));
                ret.put(rate.getPair(), rate);
            }
        }
        return ret;
    }

    public long pairTradeUnits(String position, String pair, double fraction) throws Exception {
        final HttpParams params = new BasicHttpParams();
        HttpClientParams.setRedirecting(params, false);
        HttpGet httpget = new HttpGet(this.getHome() + "/trade/new?s=" + pair + "&p=" + position);
        httpget.setParams(params);
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();
        // code 302 for redirect to login means we are not logged
        if (response.getStatusLine().getStatusCode() == 302) {
            String redirect = response.getFirstHeader("location").getValue();
            if (redirect.startsWith(properties.getProperty("login.url"))) {
                throw new NotLoggedException();
            }
        }
        // code 500 for error
        if (response.getStatusLine().getStatusCode() == 500) {
            throw new ErrorException();
        }
        String page = null;
        if (entity != null) {
            page = EntityUtils.toString(entity);
            entity.consumeContent();
        }

        Elements form = Jsoup.parse(page).getElementsByTag("form");
        int units = Integer.valueOf(form.select("input[name=units]").first().attr("value"));
        String token = form.select("input[name=_token").attr("value");
        //System.out.println(units);
        units *= fraction;
        return units;
    }

    public TradeMsg tradeSetPosition(String position, String pair, long units) throws Exception {
        TradeMsg ret = null;
        long currentUnits = 0;
        System.out.println(position);
        System.out.println(pair);
        System.out.println(units);
        // Map<String, Position> curPoss = positions();
        //System.out.println(curPoss); 
        try {   
            currentUnits = this.positions().get(pair).getSignedUnits();
        } catch (Exception ee) {
            //ee.printStackTrace();
            System.out.println("No positions...");
            // do not trade !!! 
            //return new TradeMsg();
            //System.out.println(currentUnits + " UNITS (CURRENT)");
        }
        
        if ( (units > 0 && position.equals("S")) || (units < 0 && position.equals("L")) ) {
            units = -units;
        }

        String trade = units - currentUnits > 0 ? "L" : "S";
        long tradeUnits = Math.abs(units-currentUnits);
        if (tradeUnits == 0) {
            ret = new TradeMsg();
            ret.pair = pair;
            ret.position = "L";
            ret.price = 0.0;
            ret.units = 0;
            return ret;
        }
//        System.out.println(trade);
//        System.out.println("Target Units:\t" + units);
//        System.out.println("Current Units:\t" + currentUnits);
//        System.out.println("Trade Units:\t" + tradeUnits);

        final HttpParams params = new BasicHttpParams();
        HttpClientParams.setRedirecting(params, false);
        HttpGet httpget = new HttpGet(this.getHome() + "/trade/new?s=" + pair + "&p=" + trade);
        httpget.setParams(params);
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();

        // code 302 for redirect to login means we are not logged
        if (response.getStatusLine().getStatusCode() == 302) {
            String redirect = response.getFirstHeader("location").getValue();
            if (redirect.startsWith(properties.getProperty("login.url"))) {
                throw new NotLoggedException();
            }
        }
        // code 500 for error
        if (response.getStatusLine().getStatusCode() == 500) {
            throw new ErrorException();
        }
        String page = null;
        if (entity != null) {
            page = EntityUtils.toString(entity);
            entity.consumeContent();
        }

        Elements form = Jsoup.parse(page).getElementsByTag("form");
//        int units = Integer.valueOf(form.select("input[name=units]").first().attr("value"));
        String token = form.select("input[name=_token").attr("value");
 //       System.out.println(units);
 //       units *= fraction;
 //       System.out.println(units);
// SECOND, POST THE TRADE POSITION FORM
        HttpPost httpost = new HttpPost(this.getHome() + "/trade/new?s=" + pair + "&p=" + trade);
        HttpClientParams.setRedirecting(params, true);
        httpost.setParams(params);

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("units", "" + tradeUnits));
        nvps.add(new BasicNameValuePair("stop_loss", ""));
        nvps.add(new BasicNameValuePair("take_profit", ""));
        nvps.add(new BasicNameValuePair("trailing_stop", ""));
        nvps.add(new BasicNameValuePair("_token", token));
        nvps.add(new BasicNameValuePair("confirmed_ladder", "1"));

        httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

        response = httpclient.execute(httpost);
        entity = response.getEntity();
        //System.out.println(response.getStatusLine());
        entity.consumeContent();

        // failed if code 200 (return to login page)
        if (response.getStatusLine().getStatusCode() == 302) {
            String redirect = response.getFirstHeader("location").getValue();
            httpget = new HttpGet("https://m.fxtrade.com" + redirect);
            response = httpclient.execute(httpget);
            entity = response.getEntity();
            if (entity != null) {
                page = EntityUtils.toString(entity);
                entity.consumeContent();
            }

            Elements status = Jsoup.parse(page).select("div[id=msg]");
            //System.out.println(status.first().text());
            ret = new TradeMsg(status.first().text());
            //System.out.println(response.getStatusLine());
            //  throw (new Exception("Close positions failed"));
        }
        return ret;
    }

    public TradeMsg trade(String position, String pair, double fraction) throws Exception {
        TradeMsg ret = null;
        final HttpParams params = new BasicHttpParams();
        HttpClientParams.setRedirecting(params, false);
        HttpGet httpget = new HttpGet(this.getHome() + "/trade/new?s=" + pair + "&p=" + position);
        httpget.setParams(params);
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();

        // code 302 for redirect to login means we are not logged
        if (response.getStatusLine().getStatusCode() == 302) {
            String redirect = response.getFirstHeader("location").getValue();
            if (redirect.startsWith(properties.getProperty("login.url"))) {
                throw new NotLoggedException();
            }
        }
        // code 500 for error
        if (response.getStatusLine().getStatusCode() == 500) {
            throw new ErrorException();
        }
        String page = null;
        if (entity != null) {
            page = EntityUtils.toString(entity);
            entity.consumeContent();
        }

        Elements form = Jsoup.parse(page).getElementsByTag("form");
        int units = Integer.valueOf(form.select("input[name=units]").first().attr("value"));
        String token = form.select("input[name=_token").attr("value");
        //System.out.println(units);
        units *= fraction;
        //System.out.println(units);
// SECOND, POST THE TRADE POSITION FORM
        HttpPost httpost = new HttpPost(this.getHome() + "/trade/new?s=" + pair + "&p=" + position);
        HttpClientParams.setRedirecting(params, true);
        httpost.setParams(params);

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("units", "" + units));
        nvps.add(new BasicNameValuePair("stop_loss", ""));
        nvps.add(new BasicNameValuePair("take_profit", ""));
        nvps.add(new BasicNameValuePair("trailing_stop", ""));
        nvps.add(new BasicNameValuePair("_token", token));
        nvps.add(new BasicNameValuePair("confirmed_ladder", "1"));

        httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

        response = httpclient.execute(httpost);
        entity = response.getEntity();
        //System.out.println(response.getStatusLine());
        entity.consumeContent();

        // failed if code 200 (return to login page)
        if (response.getStatusLine().getStatusCode() == 302) {
            String redirect = response.getFirstHeader("location").getValue();
            httpget = new HttpGet("https://m.fxtrade.com" + redirect);
            response = httpclient.execute(httpget);
            entity = response.getEntity();
            if (entity != null) {
                page = EntityUtils.toString(entity);
                entity.consumeContent();
            }

            Elements status = Jsoup.parse(page).select("div[id=msg]");
            //System.out.println(status.first().text());
            ret = new TradeMsg(status.first().text());
            //System.out.println(response.getStatusLine());
            //  throw (new Exception("Close positions failed"));
        }
        return ret;
    }
}
