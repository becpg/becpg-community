/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.olap.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
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
	
	private NodeRef nodeRef;
	private String queryName;
	private String queryId;
	private String mdx;
	private String cube;
	private String type;
	private String xml;
	
	
	private static final Log logger = LogFactory.getLog(OlapChart.class);

	public OlapChart(FileInfo fileInfo) {
		super();
		this.queryName = fileInfo.getName().replace(".saiku", "");
		this.nodeRef = fileInfo.getNodeRef();
	}


	public OlapChart(String queryName) {
		// TODO Auto-generated constructor stub
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
	
	


	public String getXml() {
		return xml;
	}
	
	


	public NodeRef getNodeRef() {
		return nodeRef;
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
	 * @throws JSONException 
	 */

//	<?xml version="1.0" encoding="UTF-8"?>
//	<Query name="4B5AF0DE-4F20-6223-A9FB-1A2FDB5F3FBD" type="MDX" connection="foodmart" cube="[Sales Ragged]" catalog="FoodMart" schema="FoodMart">
//	  <MDX>SELECT
//	NON EMPTY {Hierarchize({[Store].[Store Country].Members})} ON COLUMNS,
//	NON EMPTY {Hierarchize({[Measures].[Grocery Sqft]})} ON ROWS
//	FROM [Store]</MDX>
//	</Query>
	public void load(String xml) throws MalformedURLException, SAXException, IOException, ParserConfigurationException, FactoryConfigurationError, TransformerException, JSONException  {
		logger.debug("Get XML data query from xml"+xml);
		this.xml = xml;
		
		InputStream is = new ByteArrayInputStream(xml.getBytes());
		
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
			}
		} 
		
		finally {
			IOUtils.closeQuietly(is);
		}
	}

	

	public JSONObject toJSONObject() throws JSONException {
		JSONObject obj = new JSONObject();		
		obj.put("queryName",queryName);
		obj.put("queryId",queryId);
		//obj.put("mdx",mdx);
		obj.put("cube",cube);
		obj.put("type",type);
		obj.put("noderef", nodeRef);
		return obj;
	}


	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}

	
	
}
