package fr.becpg.repo.olap.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import fr.becpg.common.dom.DOMUtils;
/**
 * Store Chart infos
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public class OlapChart {
	
	private String queryName;
	private String queryId;
	private String mdx;
	private String cube;
	private String type;
	
	private static Log logger = LogFactory.getLog(OlapChart.class);

	public OlapChart(String queryName) {
		super();
		this.queryName = queryName;
	}


	public String getQueryName() {
		return queryName;
	}


	public String getQueryId() {
		return queryId;
	}


	public String getMdx() {
		return mdx;
	}


	public String getCube() {
		return cube;
	}


	public String getType() {
		return type;
	}


	/**
	 * Parse Xml response 
	 * @param buildQueryUrl
	 * @throws FactoryConfigurationError 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws MalformedURLException 
	 * @throws ParserConfigurationException 
	 * @throws TransformerException 
	 */

//	<?xml version="1.0" encoding="UTF-8"?>
//	<Query name="4B5AF0DE-4F20-6223-A9FB-1A2FDB5F3FBD" type="MDX" connection="foodmart" cube="[Sales Ragged]" catalog="FoodMart" schema="FoodMart">
//	  <MDX>SELECT
//	NON EMPTY {Hierarchize({[Store].[Store Country].Members})} ON COLUMNS,
//	NON EMPTY {Hierarchize({[Measures].[Grocery Sqft]})} ON ROWS
//	FROM [Store]</MDX>
//	</Query>
	public String load(String buildQueryUrl) throws MalformedURLException, SAXException, IOException, ParserConfigurationException, FactoryConfigurationError, TransformerException  {
		logger.debug("Get XML data query from:"+buildQueryUrl);
		
		InputStream is = new URL(buildQueryUrl).openStream();

		try {
		
			Document doc = DOMUtils.parse(is);
			if(doc!=null){
				Element queryEl = (Element) doc.getFirstChild();
				if(queryEl!=null){
					queryId = queryEl.getAttribute("name");
					cube = queryEl.getAttribute("cube");
					type = queryEl.getAttribute("type");
					mdx = DOMUtils.getElementText(queryEl,"MDX");
				}
				ByteArrayOutputStream buff = new ByteArrayOutputStream();
				try{
					DOMUtils.serialise(doc, buff);
					return new String(buff.toByteArray(),"UTF-8");
				} finally{
					IOUtils.closeQuietly(buff);
				}
			}
		} finally {
			IOUtils.closeQuietly(is);
		}
		
		return null;
		
	}

	

	public JSONObject toJSONObject() throws JSONException {
		JSONObject obj = new JSONObject();		
		obj.put("queryName",queryName);
		obj.put("queryId",queryId);
		//obj.put("mdx",mdx);
		obj.put("cube",cube);
		obj.put("type",type);
		return obj;
	}


	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}

	
	
	
}
