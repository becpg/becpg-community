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
package fr.becpg.olap;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import fr.becpg.olap.extractor.EntityToDBXmlVisitor;
import fr.becpg.tools.http.GetEntityCommand;
import fr.becpg.tools.http.ListEntitiesCommand;

public class InstanceImporter {
	private static final Log logger = LogFactory.getLog(InstanceImporter.class);

	private final String serverUrl;

	public InstanceImporter(String serverUrl) {
		super();
		this.serverUrl = serverUrl;
	}

	private EntityToDBXmlVisitor entityToDBXmlVisitor;

	public void setEntityToDBXmlVisitor(EntityToDBXmlVisitor entityToDBXmlVisitor) {
		this.entityToDBXmlVisitor = entityToDBXmlVisitor;
	}

	public void loadEntities(String query, CloseableHttpClient client, HttpClientContext context) throws XPathExpressionException, IOException, ParserConfigurationException, SAXException,
			DOMException, ParseException, SQLException {

		ListEntitiesCommand listEntitiesCommand = new ListEntitiesCommand(serverUrl);

		try (CloseableHttpResponse resp = listEntitiesCommand.runCommand(client, context, query)) {
			HttpEntity entity = resp.getEntity();
			if (entity != null) {
				try (InputStream entitiesStream = entity.getContent()) {

					DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
					domFactory.setNamespaceAware(true); // never forget this!
					DocumentBuilder builder = domFactory.newDocumentBuilder();
					Document doc = builder.parse(entitiesStream);

					XPathFactory factory = XPathFactory.newInstance();
					XPath xpath = factory.newXPath();

					int count = 1;
					String nodeRef;
					while ((nodeRef = (String) xpath.evaluate("//*[" + count + "]/@nodeRef", doc, XPathConstants.STRING)) != null && nodeRef.length() > 0) {
						count++;
						try {
							loadEntity(client, context, nodeRef);
						} catch (Exception e) {
							logger.error("Invalid Xml for " + nodeRef + " skipping node " + e.getMessage());
							if (logger.isDebugEnabled()) {
								logger.debug(e, e);
							}
						}
					}
				}
			}

		}

	}

	private void loadEntity(CloseableHttpClient client, HttpClientContext context, String nodeRef) throws IOException, DOMException, SAXException, ParserConfigurationException, ParseException,
			SQLException {
		logger.info("Import nodeRef:" + nodeRef);

		GetEntityCommand getEntityCommand = new GetEntityCommand(serverUrl);
		try (CloseableHttpResponse resp = getEntityCommand.runCommand(client, context, nodeRef)) {
			HttpEntity entity = resp.getEntity();
			if (entity != null) {
				try (InputStream entityStream = entity.getContent()) {
					entityToDBXmlVisitor.visit(entityStream);

				}
			}
		}

	}

	public String buildQuery(Date lastImport) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

		String dateRange = "MIN";
		if (lastImport != null) {
			dateRange = dateFormat.format(lastImport);
		}

		logger.info("Import from :[ " + dateRange + " TO MAX ]");

		String query = "@cm\\:created:[%s TO MAX] OR @cm\\:modified:[%s TO MAX]";

		return String.format(query, dateRange, dateRange);

	}

}
