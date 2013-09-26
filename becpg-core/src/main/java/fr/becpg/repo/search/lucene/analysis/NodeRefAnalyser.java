package fr.becpg.repo.search.lucene.analysis;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Set;

import org.alfresco.repo.search.impl.lucene.analysis.AlfrescoStandardFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ISOLatin1AccentFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.util.ApplicationContextHelper;

public class NodeRefAnalyser extends Analyzer {
	private static Log logger = LogFactory.getLog(NodeRefAnalyser.class);
	private static String STOP_WORD = " . ";

	@SuppressWarnings("rawtypes")
	private Set stopSet;

	/**
	 * An array containing some common English words that are usually not useful
	 * for searching.
	 */
	public static final String[] STOP_WORDS = { ".", " ", " . " };

	/** Builds an analyzer. */
	public NodeRefAnalyser() {
		this(STOP_WORDS);
	}

	/** Builds an analyzer with the given stop words. */
	public NodeRefAnalyser(String[] stopWords) {
		stopSet = StopFilter.makeStopSet(stopWords);
	}

	/**
	 * Constructs a {@link StandardTokenizer} filtered by a
	 * {@link StandardFilter}, a {@link LowerCaseFilter} and a
	 * {@link StopFilter}.
	 */
	public TokenStream tokenStream(String fieldName, Reader reader) {

		reader = extractDisplayName(fieldName, reader);

		TokenStream result = new StandardTokenizer(reader);
		result = new AlfrescoStandardFilter(result);
		result = new LowerCaseFilter(result);
		result = new StopFilter(result, stopSet);
		result = new ISOLatin1AccentFilter(result);
		return result;
		
//		return new NodeRefTokenFilter(reader, true);
	}

	private Reader extractDisplayName(String fieldName, Reader reader) {

		// org.alfresco.error.AlfrescoRuntimeException: 02070001 Patch
		// patch.migrateVersionStore has been configured with
		// requiresTransaction set to false but is being called in a transaction
		// ApplicationContext applicationContext =
		// org.alfresco.util.ApplicationContextHelper.getApplicationContext();

		NodeService nodeService = (NodeService) ApplicationContextHelper.getApplicationContext().getBean("nodeService");

		QName fieldQName = QName.createQName(fieldName.substring(1));

		if (BeCPGModel.PROP_PRODUCT_HIERARCHY1.equals(fieldQName)
				|| BeCPGModel.PROP_PRODUCT_HIERARCHY2.equals(fieldQName)
				|| ProjectModel.PROP_PROJECT_HIERARCHY1.equals(fieldQName)
				|| ProjectModel.PROP_PROJECT_HIERARCHY2.equals(fieldQName)) {
			String strNodeRef = "";
			int r;

			try {
				while ((r = reader.read()) != -1) {
					strNodeRef += (char) r;
				}
			} catch (IOException e) {
				logger.error("Failed to read nodeRef", e);
			}

			if (!strNodeRef.isEmpty() && NodeRef.isNodeRef(strNodeRef)) {
				NodeRef nodeRef = new NodeRef(strNodeRef);

				String name = (String) nodeService.getProperty(nodeRef, BeCPGModel.PROP_LKV_VALUE);

				strNodeRef = name + STOP_WORD + strNodeRef;

				if (logger.isTraceEnabled()) {
					logger.trace("NodeRef analyser for fieldName: " + fieldName + " new value is : " + strNodeRef);
				}
			}

			reader = new StringReader(strNodeRef);
		}

		return reader;
	}
}