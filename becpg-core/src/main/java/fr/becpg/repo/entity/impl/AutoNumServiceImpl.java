/*
 * 
 */
package fr.becpg.repo.entity.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.common.RepoConsts;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.AutoNumService;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;

// TODO: Auto-generated Javadoc
/**
 * The Class AutoNumServiceImpl.
 *
 * @author querephi
 */
public class AutoNumServiceImpl implements AutoNumService {

	/** The Constant NAME. */
	private static final String NAME = "%s - %s";
	
	/** The Constant DEFAULT_AUTO_NUM. */
	private static final long DEFAULT_AUTO_NUM = 1;
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(AutoNumServiceImpl.class);
	
	/** The search service. */
	private SearchService searchService;
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The repo service. */
	private RepoService repoService;
	
	/** The repository helper. */
	private Repository repositoryHelper;
	
	/**
	 * Sets the search service.
	 *
	 * @param searchService the new search service
	 */
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}
	
	/**
	 * Sets the node service.
	 *
	 * @param nodeService the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	/**
	 * Sets the repo service.
	 *
	 * @param repoService the new repo service
	 */
	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}
	
	/**
	 * Sets the repository helper.
	 *
	 * @param repositoryHelper the new repository helper
	 */
	public void setRepositoryHelper(Repository repositoryHelper) {
		this.repositoryHelper = repositoryHelper;
	}
	
	/**
	 * Get the next autoNum value.
	 *
	 * @param className the class name
	 * @param propertyName the property name
	 * @return the auto num value
	 */	
	@Override
	public long getAutoNumValue(QName className, QName propertyName) {
		
		long autoNumValue = DEFAULT_AUTO_NUM;
		NodeRef autoNumNodeRef = getAutoNumNodeRef(className, propertyName);
		
        // get value store in db
        if(autoNumNodeRef != null){
        	Long v = (Long)nodeService.getProperty(autoNumNodeRef, BeCPGModel.PROP_AUTO_NUM_VALUE);
        	if(v != null){
        		autoNumValue = v.longValue();
        		autoNumValue++;
        	}        	
        	
        	// update autonum node in db
        	nodeService.setProperty(autoNumNodeRef, BeCPGModel.PROP_AUTO_NUM_VALUE, autoNumValue);
        }
        else{
        	// create autonum node in db
        	createAutoNum(className, propertyName, autoNumValue);
        }
        
		return autoNumValue;
	}
	
	/**
	 * Decrease the AutoNum value and update db.
	 *
	 * @param className the class name
	 * @param propertyName the property name
	 * @return the long
	 */
	@Override
	public long decreaseAutoNumValue(QName className, QName propertyName) {
		
		long autoNumValue = DEFAULT_AUTO_NUM;
		NodeRef autoNumNodeRef = getAutoNumNodeRef(className, propertyName);
		
		// get value store in db
        if(autoNumNodeRef != null){
        	Long v = (Long)nodeService.getProperty(autoNumNodeRef, BeCPGModel.PROP_AUTO_NUM_VALUE);
        	if(v != null){
        		autoNumValue = v.longValue();
        		autoNumValue--;
        	}        	
        	
        	// update autonum node in db
        	nodeService.setProperty(autoNumNodeRef, BeCPGModel.PROP_AUTO_NUM_VALUE, autoNumValue);
        }
        
		return autoNumValue;
	}
	
	/**
	 * Store AutoNum value in db.
	 *
	 * @param className the class name
	 * @param propertyName the property name
	 * @param autoNumValue the auto num value
	 */
	@Override
	public void createOrUpdateAutoNumValue(QName className, QName propertyName,
			long autoNumValue) {
		
		NodeRef autoNumNodeRef = getAutoNumNodeRef(className, propertyName);
		
        if(autoNumNodeRef != null){
        	nodeService.setProperty(autoNumNodeRef, BeCPGModel.PROP_AUTO_NUM_VALUE, autoNumValue);
        }
        else{
        	createAutoNum(className, propertyName, autoNumValue);
        }
	}
	
	/**
	 * Delete the AutoNum object in db.
	 *
	 * @param className the class name
	 * @param propertyName the property name
	 */
	@Override
	public void deleteAutoNumValue(QName className, QName propertyName) {
		
		NodeRef autoNumNodeRef = getAutoNumNodeRef(className, propertyName);
		if(autoNumNodeRef != null){
			nodeService.deleteNode(autoNumNodeRef);
		}		
	}
	
	/**
	 * Create AutoNum value in db.
	 *
	 * @param className the class name
	 * @param propertyName the property name
	 * @param autoNumValue the auto num value
	 * @return the node ref
	 */
	private NodeRef createAutoNum(QName className, QName propertyName, long autoNumValue) {
		
		NodeRef systemNodeRef = repoService.createFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
    	NodeRef autoNumFolderNodeRef = repoService.createFolderByPath(systemNodeRef, RepoConsts.PATH_AUTO_NUM, TranslateHelper.getTranslatedPath(RepoConsts.PATH_AUTO_NUM));
    	
    	String name = String.format(NAME, className.getLocalName(), propertyName.getLocalName());
    	Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
    	properties.put(ContentModel.PROP_NAME, name);
    	properties.put(BeCPGModel.PROP_AUTO_NUM_CLASS_NAME, className);
    	properties.put(BeCPGModel.PROP_AUTO_NUM_PROPERTY_NAME, propertyName);
    	properties.put(BeCPGModel.PROP_AUTO_NUM_VALUE, autoNumValue);
    	return nodeService.createNode(autoNumFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), BeCPGModel.TYPE_AUTO_NUM, properties).getChildRef();    	    	
	}
	
	/**
	 * get AutoNum value from db.
	 *
	 * @param className the class name
	 * @param propertyName the property name
	 * @return the auto num node ref
	 */
	private NodeRef getAutoNumNodeRef(QName className, QName propertyName) {
		
		NodeRef autoNumNodeRef = null;
		
		SearchParameters sp = new SearchParameters();
        sp.addStore(RepoConsts.SPACES_STORE);
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(String.format(RepoConsts.QUERY_AUTONUM, className, propertyName));	                
        
        ResultSet resultSet =null;
        
        try{
	        resultSet = searchService.query(sp);
			
	        logger.debug("resultSet.length() : " + resultSet.length());
	        if (resultSet.length() != 0){
	        	autoNumNodeRef = resultSet.getNodeRef(0); 
	        }	        
        }
        finally{
        	if(resultSet != null)
        		resultSet.close();
        }
        
        return autoNumNodeRef;
	}	
}
