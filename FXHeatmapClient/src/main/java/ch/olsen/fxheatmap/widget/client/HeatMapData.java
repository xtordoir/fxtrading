package ch.olsen.fxheatmap.widget.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Rates provider
 *
 * just hold the last rates and give them back
 */
public class HeatMapData implements Serializable {
	private static final long serialVersionUID = 1L;

	public static String imagesLocation = "images/";
	public static final int numbThresholds = 50;

	public Date date;
	public String dateUrlFormatted;
	public String ccyPairs[];
	public AllPriceInfosGwt priceInfos[];
	public double[][] rates;		//bid and ask
	public double[][] heatMap;


	public enum Ccy { 
		USD("us.png"), 
		JPY("jp.png"), 
		EUR("europeanunion.png"), 
		GBP("gb.png"), 
		CHF("ch.png"), 
		AUD("au.png"), 
		NZD("nz.png"),
		XAU("xau.png"),
		CAD("ca.png"),
		FXX("fxx.png");
		private Ccy(String flag) {this.flag = flag; }
		private final String flag;
		public String getFlag() {
			return imagesLocation+flag;
		}
	};

	public static enum CcyPair {
		AUD_CAD,AUD_JPY,AUD_NZD,AUD_USD,CAD_JPY,CHF_AUD,CHF_JPY,
		EUR_AUD,EUR_CAD,EUR_CHF,EUR_GBP,EUR_JPY,EUR_NZD,EUR_USD,
		GBP_AUD,GBP_CAD,GBP_CHF,GBP_JPY,GBP_USD,NZD_CAD,NZD_JPY,NZD_USD,
		USD_CAD,USD_CHF,USD_JPY,XAU_USD

		;
		public Ccy per;
		public Ccy exp;
		private CcyPair() {
			per = Ccy.valueOf(name().substring(0,3));
			exp = Ccy.valueOf(name().substring(4,7));
		}
		public boolean contains(Ccy ccy) {
			return per==ccy||exp==ccy;
		}
		public Ccy other(Ccy ccy) {
			return per==ccy?exp:per;
		}
		public static List<CcyPair> containing(Ccy ccy) {
			List<CcyPair> ret = new ArrayList<CcyPair>(CcyPair.values().length);
			if (ccy == Ccy.FXX){
				ret.add(CcyPair.AUD_USD);
				ret.add(CcyPair.EUR_JPY);
				ret.add(CcyPair.EUR_USD);
				ret.add(CcyPair.GBP_USD);
				ret.add(CcyPair.NZD_USD);
				ret.add(CcyPair.USD_CAD);
				ret.add(CcyPair.USD_CHF);
				ret.add(CcyPair.USD_JPY);
				ret.add(CcyPair.XAU_USD);
			}
			else {
				for ( CcyPair p : CcyPair.values() )
					if ( p.contains(ccy) )
						ret.add(p);
			}
			return ret;
		}
		public String invert() {
			return exp.name()+"_"+per.name();
		}}

	public static class AllPriceInfosGwt implements Serializable {
		private static final long serialVersionUID = 1L;
		public Date last;
		public double lastPrice;
		public LastPricesInfosGwt all[];

		public AllPriceInfosGwt() {
		}
		public AllPriceInfosGwt(LastPricesInfosGwt all[], Date last, double lastPrice) {
			this.all = all;
			this.last = last;
			this.lastPrice = lastPrice;
		}
	}

	public static class LastPricesInfosGwt implements Serializable {
		private static final long serialVersionUID = 1L;
		public Date lastExtTime;
		public double lastExtPrice;
//		public Date nextExtTime;		//not needed for now
		public double nextExtPrice;

		public LastPricesInfosGwt() {
		}

	}

	public HeatMapData cloneNoPriceInfos() {
		HeatMapData ret = new HeatMapData();
		ret.date = date;
		ret.dateUrlFormatted = dateUrlFormatted;
		ret.ccyPairs = ccyPairs;
		ret.rates = rates;
		ret.heatMap = heatMap;
		return ret;
	}

	@Override
	public int hashCode() {
		String key = date.toString();
		for ( String ccy : ccyPairs )
			key += ccy;
		return key.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if ( ! (other instanceof HeatMapData) )
			return false;
		return hashCode() == other.hashCode();
	}
	
	@Override
	public String toString() {
		String ret = date.toString()+"\n";
		for ( int n=0; n<ccyPairs.length; n++ ) {
			ret += ccyPairs[n] + " ";
			for ( int j=0; j<heatMap[n].length; j++ ) 
				ret +=  format(heatMap[n][j])+" ";
			ret += "\n";
		}
		return ret;
	}

	private String format(double d) {
		String ret = Double.toString(d);
		int idx = ret.indexOf('.');
		if ( idx >=0 && idx < ret.length()-2 )
			ret = ret.substring(0, idx+2);
		return ret;
	}

	public static String getCellColor(int pos, double val) {
		double max = (4+7*(double)pos/numbThresholds);
		String color;
		if ( val > 0 ) {
			//positive, blue colors
			if ( val > max )
				val = max;
			int RG = (int)(0xEE * (1.0-(double)val/max));
			String sRG = Integer.toHexString(RG).toUpperCase();
			if ( sRG.length() == 1 )
				sRG = "0"+sRG;
			else if ( sRG.length() > 2 )
				sRG = sRG.substring(sRG.length()-2);
			color = "#"+sRG+sRG+"FF";
		}
		else {
			//negative, red colors
			val = -val;
			if ( val > max )
				val = max;
			int GB = (int)(0xEE * (1.0-(double)val/max));
			String sGB = Integer.toHexString(GB).toUpperCase();
			if ( sGB.length() == 1 )
				sGB = "0"+sGB;
			else if ( sGB.length() > 2 )
				sGB = sGB.substring(sGB.length()-2);
			color = "#FF"+sGB+sGB;
			
		}
		return color;
	}



}
