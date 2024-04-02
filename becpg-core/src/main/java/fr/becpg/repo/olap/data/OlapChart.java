/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
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
 *
 * @author "Matthieu Laborie"
 * @version $Id: $Id
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

	/**
	 * <p>Constructor for OlapChart.</p>
	 *
	 * @param fileInfo a {@link org.alfresco.service.cmr.model.FileInfo} object.
	 */
	public OlapChart(FileInfo fileInfo) {
		super();
		this.queryName = fileInfo.getName().replace(".saiku", "");
		this.nodeRef = fileInfo.getNodeRef();
	}

	/**
	 * <p>Constructor for OlapChart.</p>
	 *
	 * @param queryName a {@link java.lang.String} object.
	 */
	public OlapChart(String queryName) {
		this.queryName = queryName;
	}

	/**
	 * <p>Getter for the field <code>queryName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getQueryName() {
		return queryName;
	}

	/**
	 * <p>Getter for the field <code>queryId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getQueryId() {
		return queryId;
	}

	/**
	 * <p>Getter for the field <code>mdx</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getMdx() {
		return mdx;
	}

	/**
	 * <p>Getter for the field <code>cube</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getCube() {
		return cube;
	}

	/**
	 * <p>Getter for the field <code>type</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getType() {
		return type;
	}

	/**
	 * <p>Getter for the field <code>xml</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getXml() {
		return xml;
	}

	/**
	 * <p>Getter for the field <code>nodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef getNodeRef() {
		return nodeRef;
	}

	/**
	 * Parse Xml response
	 *
	 * @param xml a {@link java.lang.String} object.
	 * @throws java.net.MalformedURLException if any.
	 * @throws org.xml.sax.SAXException if any.
	 * @throws java.io.IOException if any.
	 * @throws javax.xml.parsers.ParserConfigurationException if any.
	 * @throws javax.xml.parsers.FactoryConfigurationError if any.
	 * @throws javax.xml.transform.TransformerException if any.
	 * @throws org.json.JSONException if any.
	 */

	// <?xml version="1.0" encoding="UTF-8"?>
	// <Query name="4B5AF0DE-4F20-6223-A9FB-1A2FDB5F3FBD" type="MDX"
	// connection="foodmart" cube="[Sales Ragged]" catalog="FoodMart"
	// schema="FoodMart">
	// <MDX>SELECT
	// NON EMPTY {Hierarchize({[Store].[Store Country].Members})} ON COLUMNS,
	// NON EMPTY {Hierarchize({[Measures].[Grocery Sqft]})} ON ROWS
	// FROM [Store]</MDX>
	// </Query>
	public void load(String xml) throws MalformedURLException, SAXException, IOException, ParserConfigurationException, FactoryConfigurationError, TransformerException, JSONException {
		logger.trace("Get XML data query from xml" + xml);
		this.xml = xml;

		try (InputStream is = new ByteArrayInputStream(xml.getBytes())) {

			Document doc = DOMUtils.parse(is);
			if (doc != null) {
				Element queryEl = (Element) doc.getFirstChild();
				if (queryEl != null) {
					queryId = queryEl.getAttribute("name");
					cube = queryEl.getAttribute("cube");
					type = queryEl.getAttribute("type");
					mdx = DOMUtils.getElementText(queryEl, "MDX");
				}
			}

		}
	}

	/**
	 * <p>toJSONObject.</p>
	 *
	 * @return a {@link org.json.JSONObject} object.
	 * @throws org.json.JSONException if any.
	 */
	public JSONObject toJSONObject() throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("queryName", queryName);
		obj.put("queryId", queryId);
		obj.put("cube", cube);
		obj.put("type", type);
		obj.put("noderef", nodeRef);
		return obj;
	}

	/**
	 * <p>Setter for the field <code>queryId</code>.</p>
	 *
	 * @param queryId a {@link java.lang.String} object.
	 */
	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}

}
