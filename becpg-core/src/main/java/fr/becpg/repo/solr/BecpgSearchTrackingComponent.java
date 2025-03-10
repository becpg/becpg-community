package fr.becpg.repo.solr;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.solr.NodeParameters;
import org.alfresco.repo.solr.SOLRTrackingComponentImpl;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>BecpgSearchTrackingComponent class.</p>
 *
 * @author matthieu
 */
public class BecpgSearchTrackingComponent extends SOLRTrackingComponentImpl {

	private static Log logger = LogFactory.getLog(BecpgSearchTrackingComponent.class);


	private List<String> typesToExcludeFromSearch;


	private BeCPGQueryBuilder beCPGQueryBuilder;

	protected NamespaceService namespaceService;
	protected DictionaryService dictionaryService;

	/** {@inheritDoc} */
	@Override
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
		super.setNamespaceService(namespaceService);
	}

	/** {@inheritDoc} */
	@Override
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
		super.setDictionaryService(dictionaryService);
	}
	
	

	/**
	 * <p>Setter for the field <code>typesToExcludeFromSearch</code>.</p>
	 *
	 * @param typesToExcludeFromSearch a {@link java.util.List} object
	 */
	public void setTypesToExcludeFromSearch(List<String> typesToExcludeFromSearch) {
		this.typesToExcludeFromSearch = typesToExcludeFromSearch;
	}
	
	
	/**
	 * <p>Setter for the field <code>beCPGQueryBuilder</code>.</p>
	 *
	 * @param beCPGQueryBuilder a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object
	 */
	public void setBeCPGQueryBuilder(BeCPGQueryBuilder beCPGQueryBuilder) {
		this.beCPGQueryBuilder = beCPGQueryBuilder;
	}

	/** {@inheritDoc} */
	@Override
	public void init() {
	     Set<QName> typesToExclude = new HashSet<>();

		for (String qNameInString : typesToExcludeFromSearch) {
			if ((null != qNameInString) && !qNameInString.isEmpty()) {

				QName qname = QName.resolveToQName(namespaceService, qNameInString);
				if (dictionaryService.getType(qname) != null) {
					typesToExclude.add(qname);
					typesToExclude.addAll(dictionaryService.getSubTypes(qname, true));

				}
			}
		}
		beCPGQueryBuilder.setTypesToExcludeFromIndex(typesToExclude);
		
		super.init();
	}


	/** {@inheritDoc} */
	@Override
	public void getNodes(NodeParameters nodeParameters, NodeQueryCallback callback) {
		Set<QName> exludesTypes =  BeCPGQueryBuilder.getTypesExcludedFromIndex();
		
		if(exludesTypes!=null && !exludesTypes.isEmpty()) {
			if(nodeParameters.getExcludeNodeTypes()!=null) {
				nodeParameters.getExcludeNodeTypes().addAll(exludesTypes);
			} else {
				nodeParameters.setExcludeNodeTypes(exludesTypes);
			}

			if(logger.isDebugEnabled()) {
				logger.debug("Exclude types : "+nodeParameters.getExcludeNodeTypes());
			}
		}
		

		super.getNodes(nodeParameters, callback);
	}


}
