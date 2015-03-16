package ch.olsen.fxheatmap.rpcclient;

import ch.olsen.fxheatmap.widget.client.HeatMapData;

public class App {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		Client client = new Client("__MY_EMAIL__","__PASSWORD__");
		client.login();
		
		client.addPair("EUR_USD");
		client.addPair("USD_CAD");
		client.addPair("NZD_USD");
		
		HeatMapData hmd = client.fetch();
		System.out.println(hmd);

	}
}
