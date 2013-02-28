package fr.becpg.repo.olap;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;

import fr.becpg.repo.olap.data.OlapChart;
import fr.becpg.repo.olap.data.OlapChartData;


/**
 * Act as a proxy to OLAP Engine
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public interface OlapService {

	List<OlapChart> retrieveOlapCharts()  throws JSONException, IOException;
	
	OlapChartData retrieveChartData(String olapQueryId) throws IOException, JSONException;

	String getCurrentOlapUserName();
	
	
}
