package fr.becpg.olap;

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import fr.becpg.olap.InstanceManager.Instance;
import fr.becpg.olap.extractor.EntityToDBXmlVisitor;
import fr.becpg.olap.http.GetEntityCommand;
import fr.becpg.olap.http.ListEntitiesCommand;
import fr.becpg.olap.jdbc.JdbcConnectionManager;

/**
 * 
 * @author matthieu
 * 
 */
public class RemoteETLClient {

	private static Log logger = LogFactory.getLog(RemoteETLClient.class);

	private String remoteUser;

	private char[] remotePwd;

	private String serverUrl;

	public RemoteETLClient(String serverUrl, String remoteUser, char[] remotePwd) {
		super();
		this.remoteUser = remoteUser;
		this.remotePwd = remotePwd;
		this.serverUrl = serverUrl;
	}

	private EntityToDBXmlVisitor entityToDBXmlVisitor;

	public void setEntityToDBXmlVisitor(EntityToDBXmlVisitor entityToDBXmlVisitor) {
		this.entityToDBXmlVisitor = entityToDBXmlVisitor;
	}

	public void loadEntities(String query) {

		ListEntitiesCommand listEntitiesCommand = new ListEntitiesCommand(serverUrl, remoteUser, remotePwd);

		InputStream entitiesStream = null;
		try {
			entitiesStream = listEntitiesCommand.runCommand(query);

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
				loadEntity(nodeRef);
			}

		} catch (Exception e) {
			logger.error(e, e);
		} finally {
			IOUtils.closeQuietly(entitiesStream);
		}

	}

	private void loadEntity(String nodeRef) {
		logger.info("Read nodeRef:" + nodeRef);

		GetEntityCommand getEntityCommand = new GetEntityCommand(serverUrl, remoteUser, remotePwd);

		InputStream entityStream = null;
		try {
			entityStream = getEntityCommand.runCommand(nodeRef);
			entityToDBXmlVisitor.visit(entityStream);

		} catch (Exception e) {
			logger.error(e, e);
		} finally {
			IOUtils.closeQuietly(entityStream);
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
		InstanceManager instanceManager = new InstanceManager(jdbcConnectionManager);
		for (final Instance instance : instanceManager.getAllInstances()) {
			logger.info("Start importing from instance/tenant: " + instance.getId() + "/" + instance.getInstanceName() + "/" + instance.getTenantName());
			if (logger.isDebugEnabled()) {
				logger.debug(" - Login: " + instance.getTenantUser());
				logger.debug(" - Password: " + instance.getTenantPassword());
			}

			String query = " AND (@cm\\:created:[%s TO MAX] OR @cm\\:modified:[%s TO MAX])";

			RemoteETLClient remoteETLClient = new RemoteETLClient(instance.getInstanceUrl(), instance.getTenantUser(), instance.getTenantPassword().toCharArray());
			remoteETLClient.setEntityToDBXmlVisitor(new EntityToDBXmlVisitor(jdbcConnectionManager, instance));

			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

			String dateRange = "MIN";
			if (instance.getLastImport() != null) {
				dateRange = dateFormat.format(instance.getLastImport());
			}

			remoteETLClient.loadEntities(String.format(query, dateRange, dateRange));

			instanceManager.updateLastImportDate(instance);
			
		}

	}

}
