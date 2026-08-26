/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG. 
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
import java.io.Serializable;
import java.net.MalformedURLException;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.common.dom.DOMUtils;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.MLTextHelper;

/**
 * Store Chart infos
 *
 * @author "Matthieu Laborie"
 * @version $Id: $Id
 */
public class OlapChart {

	private NodeRef nodeRef;
	private String fileName;
	private String olapQueryId;
	private String queryName;
	private String queryId;
	private String mdx;
	private String cube;
	private String type;
	private String xml;

	/** Constant <code>logger</code> */
	/** Extension of a saved OLAP query document. */
	public static final String SAIKU_EXTENSION = ".saiku";

	private static final Log logger = LogFactory.getLog(OlapChart.class);

	/**
	 * <p>Constructor for OlapChart.</p>
	 *
	 * @param fileInfo a {@link org.alfresco.service.cmr.model.FileInfo} object.
	 */
	public OlapChart(FileInfo fileInfo) {
		super();
		this.olapQueryId = (String) fileInfo.getProperties().get(BeCPGModel.PROP_OLAP_QUERY_ID);
		this.fileName = readFileName(fileInfo);
		this.queryName = readQueryName(fileInfo);
		this.nodeRef = fileInfo.getNodeRef();
	}

	/**
	 * Reads the identity of a stored chart.
	 *
	 * <p>#24931: a resource shipped by beCPG is identified by {@code bcpg:olapQueryId}, which no
	 * translation touches, while its name carries the label the user reads. A query the user saved
	 * from Saiku has no such id, so its name remains its identity, as it was before.
	 *
	 * @param fileInfo the stored document
	 * @return the name every reference resolves on, extension included
	 */
	private String readFileName(FileInfo fileInfo) {
		if (olapQueryId == null) {
			return fileInfo.getName();
		}
		return olapQueryId + extensionOf(fileInfo.getName());
	}

	/**
	 * Reads the display label of a stored chart, in the locale of the caller.
	 *
	 * <p>The label falls back to the name without its extension. Strip it from the end only: a
	 * plain replace turns "Sales.saikudash" into "Salesdash", which is neither a usable label nor
	 * a name any caller can map back to the stored file.
	 *
	 * @param fileInfo the stored document
	 * @return the label to display
	 */
	private static String readQueryName(FileInfo fileInfo) {
		Serializable title = fileInfo.getProperties().get(ContentModel.PROP_TITLE);
		if (title instanceof MLText mlText) {
			String value = MLTextHelper.getClosestValue(mlText, I18NUtil.getLocale());
			if ((value != null) && !value.isBlank()) {
				return value;
			}
		} else if ((title instanceof String value) && !value.isBlank()) {
			return value;
		}

		return stripExtension(fileInfo.getName());
	}

	private static String stripExtension(String name) {
		int dot = name.lastIndexOf('.');
		return dot > 0 ? name.substring(0, dot) : name;
	}

	private static String extensionOf(String name) {
		int dot = name.lastIndexOf('.');
		return dot > 0 ? name.substring(dot) : "";
	}

	/**
	 * <p>Getter for the field <code>fileName</code>.</p>
	 *
	 * @return the stored file name, extension included
	 */
	public String getFileName() {
		return fileName;
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
		return (olapQueryId != null) ? olapQueryId : queryId;
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

		// #24931: a query re-saved from the Saiku 4.8 workspace is stored as JSON, not as the
		// Saiku 2.x XML this method was written for. Parsing it as XML throws, the caller skips the
		// chart, and it silently disappears from the beCPG BI dashlet. The document is still passed
		// on verbatim to the OLAP server, which accepts both forms, so only the few attributes read
		// here need a second reading.
		if (isJsonQuery(xml)) {
			loadFromJson(xml);
			return;
		}

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

	private static boolean isJsonQuery(String content) {
		return (content != null) && content.trim().startsWith("{");
	}

	/**
	 * Reads the few attributes this class exposes from a Saiku 4.8 query document.
	 *
	 * @param json the query as stored by the 4.8 workspace
	 * @throws JSONException if the document is not readable
	 */
	private void loadFromJson(String json) throws JSONException {
		JSONObject root = new JSONObject(json);
		queryId = root.optString("name", null);
		type = root.optString("type", null);
		mdx = root.optString("mdx", null);
		JSONObject cubeObject = root.optJSONObject("cube");
		if (cubeObject != null) {
			cube = cubeObject.optString("name", null);
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
		obj.put("fileName", fileName);
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
