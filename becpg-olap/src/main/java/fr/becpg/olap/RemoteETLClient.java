package fr.becpg.olap;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import fr.becpg.olap.extractor.EntityToDBXmlVisitor;
import fr.becpg.olap.http.GetEntityCommand;
import fr.becpg.olap.http.ListEntitiesCommand;
import fr.becpg.olap.jdbc.JdbcConnectionManager;

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


	public static void main(String[] args) {

		// Option help = new Option( "help", "print this message" );
		// Option projecthelp = new Option( "projecthelp",
		// "print project help information" );
		// Option version = new Option( "version",
		// "print the version information and exit" );
		// Option quiet = new Option( "quiet", "be extra quiet" );
		// Option verbose = new Option( "verbose", "be extra verbose" );
		// Option debug = new Option( "debug", "print debugging information" );
		// Option emacs = new Option( "emacs",
		// "produce logging information without adornments" );
		//
		// Option logfile = OptionBuilder.withArgName( "file" )
		// .hasArg()
		// .withDescription( "use given file for log" )
		// .create( "logfile" );
		//
		// Options options = new Options();
		//
		// options.addOption( help );
		// options.addOption( projecthelp );
		// options.addOption( version );
		// options.addOption( quiet );
		// options.addOption( verbose );
		// options.addOption( debug );
		// options.addOption( emacs );
		// options.addOption( logfile );
		//
		// // create the parser
		// CommandLineParser parser = new GnuParser();
		// try {
		// // parse the command line arguments
		// CommandLine line = parser.parse( options, args );
		// }
		// catch( ParseException exp ) {
		// // oops, something went wrong
		// System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
		// }

		JdbcConnectionManager jdbcConnectionManager = new JdbcConnectionManager("becpg","becpg", "jdbc:mysql://localhost:3306/becpg_dev");
		
		RemoteETLClient remoteETLClient = new RemoteETLClient("http://localhost:8080/alfresco/service", "admin@agrostis.biz", "becpg".toCharArray());
		remoteETLClient.setEntityToDBXmlVisitor(new EntityToDBXmlVisitor(jdbcConnectionManager));

		remoteETLClient.loadEntities("+TYPE:bcpg\\:finishedProduct");

	}

}
