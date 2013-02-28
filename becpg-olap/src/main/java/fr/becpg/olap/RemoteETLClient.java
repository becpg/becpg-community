package fr.becpg.olap;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import fr.becpg.olap.InstanceManager.Instance;
import fr.becpg.olap.extractor.EntityToDBXmlVisitor;
import fr.becpg.olap.http.GetEntityCommand;
import fr.becpg.olap.http.ListEntitiesCommand;
import fr.becpg.olap.jdbc.JdbcConnectionManager;
import fr.becpg.olap.jdbc.JdbcConnectionManager.JdbcConnectionManagerCallBack;

/**
 * 
 * @author matthieu
 * 
 */
public class RemoteETLClient {

	private static Log logger = LogFactory.getLog(RemoteETLClient.class);


	private String serverUrl;

	public RemoteETLClient(String serverUrl) {
		super();
		this.serverUrl = serverUrl;
	}

	

	private EntityToDBXmlVisitor entityToDBXmlVisitor;

	public void setEntityToDBXmlVisitor(EntityToDBXmlVisitor entityToDBXmlVisitor) {
		this.entityToDBXmlVisitor = entityToDBXmlVisitor;
	}

	public void loadEntities(String query, HttpClient client) throws XPathExpressionException, IOException, ParserConfigurationException, SAXException, DOMException, ParseException, SQLException {

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

	public static void main(String[] args) throws Exception {

		Options options = new Options();

		options.addOption("file", true, "Load properties file");

		CommandLineParser parser = new GnuParser();
		CommandLine cmd = parser.parse(options, args);
		Properties props = new Properties();
		if (cmd.hasOption("file")) {
			props.load(new FileInputStream(cmd.getOptionValue("file")));
		} else {
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("jdbc.properties"));
		}

		final JdbcConnectionManager jdbcConnectionManager = new JdbcConnectionManager((String) props.get("jdbc.user"), (String) props.get("jdbc.password"),
				(String) props.get("jdbc.url"));
		final InstanceManager instanceManager = new InstanceManager();
		instanceManager.setJdbcConnectionManager(jdbcConnectionManager);
		for (final Instance instance : instanceManager.getAllInstances()) {
			logger.info("Start importing from instance/tenant: " + instance.getId() + "/" + instance.getInstanceName() + "/" + instance.getTenantName());
			if (logger.isDebugEnabled()) {
				logger.debug(" - Login: " + instance.getTenantUser());
				logger.debug(" - Password: " + instance.getTenantPassword());
			}
			JdbcConnectionManager.doInTransaction(jdbcConnectionManager, new JdbcConnectionManagerCallBack() {

				@Override
				public void execute(JdbcConnectionManager jdbcConnectionManager) throws Exception {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

					String dateRange = "MIN";
					if (instance.getLastImport() != null) {
						dateRange = dateFormat.format(instance.getLastImport());
					}

					logger.info("Import from :[ " + dateRange + " TO MAX ]");

					instanceManager.createBatch(instance);

					String query = " AND (@cm\\:created:[%s TO MAX] OR @cm\\:modified:[%s TO MAX])";

					RemoteETLClient remoteETLClient = new RemoteETLClient(instance.getInstanceUrl());

					remoteETLClient.setEntityToDBXmlVisitor(new EntityToDBXmlVisitor(jdbcConnectionManager, instance));

					remoteETLClient.loadEntities(String.format(query, dateRange, dateRange), instanceManager.createInstanceSession(instance));

				}
			});

		}

	}

}
