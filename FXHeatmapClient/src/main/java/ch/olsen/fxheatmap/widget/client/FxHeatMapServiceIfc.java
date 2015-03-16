package ch.olsen.fxheatmap.widget.client;


import java.util.Set;

import ch.olsen.fxheatmap.widget.client.HeatMapData.CcyPair;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("service/")
public interface FxHeatMapServiceIfc extends RemoteService {

	HeatMapData getHeatMapData(Set<CcyPair> instrument, String time);
    HeatMapData getHeatMapData2(String time);
	}
