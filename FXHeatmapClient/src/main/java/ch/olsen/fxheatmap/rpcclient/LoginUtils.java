package ch.olsen.fxheatmap.rpcclient;

import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.gdevelop.gwt.syncrpc.Utils;
import com.google.gwt.user.client.rpc.StatusCodeException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class LoginUtils {
	private static final String LOGIN_URL = "https://tools.olseninvest.com:443/ois/web/login/";
	private static final String LOGIN_GOTO = "/web/myaccount";

    static TrustManager[] trustAllCerts = new TrustManager[]{
        new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {return null;}
            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType){}
            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType){}
        }
    };


	public static CookieManager loginOlsen(String email, String password) throws Exception {
		
		CookieHandler oldCookieHandler = CookieHandler.getDefault();
		try {
			CookieManager cookieManager = new CookieManager(null,
					CookiePolicy.ACCEPT_ALL);
			CookieHandler.setDefault(cookieManager);
			URL url = new URL(LoginUtils.LOGIN_URL);
			email = URLEncoder.encode(email, "UTF-8");
			password = URLEncoder.encode(password, "UTF-8");
			String requestData = "username=" + email + 
					"&password=" + password + 
					"&goto=" + LoginUtils.LOGIN_GOTO;

        	SSLContext sc = SSLContext.getInstance("SSL");
	        sc.init(null, trustAllCerts, new java.security.SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
	        connection.setDoInput(true);
	        connection.setDoOutput(true);
	        connection.setInstanceFollowRedirects(false);
	        connection.setRequestMethod("POST");
	        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        connection.setRequestProperty("Content-Length", "" + requestData.length());
	        
	        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
	        writer.write(requestData);
	        writer.flush();
	        writer.close();
	        
	        int statusCode = connection.getResponseCode();
	        //System.out.println(statusCode);
	        // a code 200 means login failed (OK code for login page...)
	        if (statusCode == HttpURLConnection.HTTP_OK) {
	        	throw new Exception("Login failed");
	        }
	        // anything that is not a redirect mean login is a failure also
	        else if (statusCode != HttpURLConnection.HTTP_MOVED_TEMP) {
	        	String responseText = Utils.getResposeText(connection);
	        	throw new StatusCodeException(statusCode, responseText);
	        }
	        return cookieManager;
		} finally {
			 CookieHandler.setDefault(oldCookieHandler);
		}
		

	}
}



