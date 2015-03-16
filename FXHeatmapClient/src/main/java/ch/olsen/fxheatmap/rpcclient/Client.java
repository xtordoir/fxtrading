package ch.olsen.fxheatmap.rpcclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.CookieManager;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import ch.olsen.fxheatmap.widget.client.FxHeatMapServiceIfc;
import ch.olsen.fxheatmap.widget.client.HeatMapData;
import ch.olsen.fxheatmap.widget.client.HeatMapData.CcyPair;

import com.gdevelop.gwt.syncrpc.SyncProxy;

public class Client {

	private Set<String> pairs;
	
	private Properties properties;
	private CookieManager userSession;
	private FxHeatMapServiceIfc hmService;
	
	public Client() throws Exception {
        properties = new Properties();
        InputStream is = this.getClass().getResourceAsStream("config.properties");
        properties.load(is);
        
        pairs = new HashSet<String>();
    }
	public Client(String email, String password) {
		properties = new Properties();
		properties.setProperty("username", email);
		properties.setProperty("password", password);
		pairs = new HashSet<String>();
	}
    public Client(File configFile) throws Exception {
        properties = new Properties();
        properties.load(new FileInputStream(configFile));
    }

    public void addPair(String pair) {
		pairs.add(pair);
	}
	public void addPairs(Collection<String> pairs) {
		this.pairs.addAll(pairs);
	}

	public void login() throws Exception {
		userSession = LoginUtils.loginOlsen(properties.getProperty("username"),
				properties.getProperty("password"));
		hmService = (FxHeatMapServiceIfc)SyncProxy.newProxyInstance(FxHeatMapServiceIfc.class,
		        "https://tools.olseninvest.com/ois/fxheatmap/", ""
		        ,"07C018938EC913D486A3357BF70F8DD5",
		        userSession);
	}
	
	public HeatMapData fetch() {
		Set<CcyPair> instruments = new HashSet<CcyPair>();
		for (String pair : pairs) {
			instruments.add(CcyPair.valueOf(pair));
		}
		return hmService.getHeatMapData(instruments, null);
	}
}
