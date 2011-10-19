package fr.becpg.repo.olap;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.json.JSONException;
import org.xml.sax.SAXException;

import fr.becpg.repo.olap.data.OlapChart;
import fr.becpg.repo.olap.data.OlapChartData;


/**
 * Act as a proxy to OLAP Engine
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public interface OlapService {

	List<OlapChart> retrieveOlapCharts() throws IOException, JSONException, SAXException, ParserConfigurationException, FactoryConfigurationError, TransformerException;
	
	OlapChartData retrieveChartData(String olapQueryId) throws IOException, JSONException;
	
	
}
