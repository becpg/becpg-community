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
import org.apache.http.client.HttpClient;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import fr.becpg.olap.extractor.EntityToDBXmlVisitor;
import fr.becpg.olap.http.GetEntityCommand;
import fr.becpg.olap.http.ListEntitiesCommand;

public class InstanceImporter {
	private static Log logger = LogFactory.getLog(InstanceImporter.class);

	private String serverUrl;

	public InstanceImporter(String serverUrl) {
		super();
		this.serverUrl = serverUrl;
	}

	private EntityToDBXmlVisitor entityToDBXmlVisitor;

	public void setEntityToDBXmlVisitor(EntityToDBXmlVisitor entityToDBXmlVisitor) {
		this.entityToDBXmlVisitor = entityToDBXmlVisitor;
	}

	public void loadEntities(String query, HttpClient client) throws XPathExpressionException, IOException, ParserConfigurationException, SAXException, DOMException,
			ParseException, SQLException {

		ListEntitiesCommand listEntitiesCommand = new ListEntitiesCommand(serverUrl);

		try (InputStream entitiesStream = listEntitiesCommand.runCommand(client, query)) {

			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(true); // never forget this!
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			Document doc = builder.parse(entitiesStream);

			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();

			int count = 1;
			String nodeRef = null;
			while ((nodeRef = (String) xpath.evaluate("//*[" + count + "]/@nodeRef", doc, XPathConstants.STRING)) != null && nodeRef.length() > 0) {
				count++;
				loadEntity(client, nodeRef);
			}

		}

	}

	private void loadEntity(HttpClient client, String nodeRef) throws IOException, DOMException, SAXException, ParserConfigurationException, ParseException, SQLException {
		logger.info("Import nodeRef:" + nodeRef);

		GetEntityCommand getEntityCommand = new GetEntityCommand(serverUrl);

		try (InputStream entityStream = getEntityCommand.runCommand(client, nodeRef)) {
			entityToDBXmlVisitor.visit(entityStream);

		}

	}
	

	public String buildQuery(Date lastImport) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

		String dateRange = "MIN";
		if (lastImport != null) {
			dateRange = dateFormat.format(lastImport);
		}

		logger.info("Import from :[ " + dateRange + " TO MAX ]");


		String query = " AND (@cm\\:created:[%s TO MAX] OR @cm\\:modified:[%s TO MAX])";
		
		return	String.format(query, dateRange, dateRange);
		 
	}

}
