package fr.becpg.repo.olap;

import java.io.IOException;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;

import fr.becpg.repo.olap.data.OlapChart;
import fr.becpg.repo.olap.data.OlapChartData;


/**
 * Act as a proxy to OLAP Engine
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public interface OlapService {

	List<OlapChart> retrieveOlapCharts();
	
	OlapChartData retrieveChartData(String olapQueryId) throws IOException, JSONException;

	String getCurrentOlapUserName();

	NodeRef getOlapQueriesFolder();

	List<OlapChart> retrieveOlapChartsFromSaiku();
	
	
}
