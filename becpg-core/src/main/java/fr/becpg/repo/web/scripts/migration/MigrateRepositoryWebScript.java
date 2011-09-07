/*
 * 
 */
package fr.becpg.repo.web.scripts.migration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.common.RepoConsts;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.QualityModel;
import fr.becpg.repo.helper.RepoService;

/**
 * The Class MigrateRepositoryWebScript.
 *
 * @author querephi
 */
public class MigrateRepositoryWebScript extends AbstractWebScript
{	
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(MigrateRepositoryWebScript.class);
				
	private static final String PARAM_ACTION = "action";
	private static final String VALUE_ACTION_MIGRATE_PRODUCTCODE = "migrateProductCode";
	private static final String VALUE_ACTION_MIGRATE_AUTONUM = "migrateAutoNum";
	private static final String VALUE_ACTION_MIGRATE_ENTITYLISTS = "migrateEntityLists";
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The search service. */
	private SearchService searchService;
	
	/** The file folder service. */
	private FileFolderService fileFolderService;
	
	private Repository repositoryHelper;
	
	private RepoService repoService;
	
	private BehaviourFilter policyBehaviourFilter;
	
	private DictionaryService dictionaryService;
		   					
	/**
	 * Sets the node service.
	 *
	 * @param nodeService the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	/**
	 * Sets the search service.
	 *
	 * @param searchService the new search service
	 */
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}
	
	/**
	 * Sets the file folder service.
	 *
	 * @param fileFolderService the new file folder service
	 */
	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}
	
	public void setRepositoryHelper(Repository repositoryHelper) {
		this.repositoryHelper = repositoryHelper;
	}
	
	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}
	
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}
	
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws WebScriptException
    {
    	logger.debug("start restore archived node webscript");
    	Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();	    	
    	String action = templateArgs.get(PARAM_ACTION);
		
    	if(action == null){
    		logger.error("action cannot be null");
    	}
    	else if(action.equals(VALUE_ACTION_MIGRATE_PRODUCTCODE)){
    		
    		// migration productCode
    		QName productCodeQName = QName.createQName(BeCPGModel.BECPG_URI, "productCode");
    		migrateProperty(" +@bcpg\\:productCode:* ", productCodeQName, BeCPGModel.PROP_CODE);    		
    	} 
    	else if(action.equals(VALUE_ACTION_MIGRATE_AUTONUM)){
    		
    		// migrate autoNum
    		migrateAutoNum();
    	}
    	else if(action.equals(VALUE_ACTION_MIGRATE_ENTITYLISTS)){
    		
    		// migrate dataLists
    		migrateDataLists();
    		
    		// migrate productLists
    		migrateProductLists();
    		
    	} 
    }
	
	private void migrateProperty(String query, QName oldProperty, QName newProperty){

		logger.info("migrateProductCode");
		
		List<NodeRef> items = new ArrayList<NodeRef>();
    	ResultSet resultSet = null;
    	
    	SearchParameters sp = new SearchParameters();
        sp.addStore(RepoConsts.SPACES_STORE);
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(query);	                         
        
    	try{
    		resultSet = searchService.query(sp);
    		if(resultSet.length() > 0){
    			items = resultSet.getNodeRefs();        			
    		}
    	}	       
    	catch(Exception e){
    		logger.error("Failed to get items", e);
    	}
    	finally{
    		if(resultSet != null)
    			resultSet.close();
    	}        
    	
    	logger.info("items to migrate: " + items.size());    	
    	
    	policyBehaviourFilter.disableAllBehaviours();
    	        	
    	try{
    		
    		for(int cnt=0 ; cnt < items.size() ; cnt++){
        		
        		final NodeRef nodeRef = items.get(cnt);        		
        		Serializable value = nodeService.getProperty(nodeRef, oldProperty);
        		
        		if(value != null){
        			logger.info("change property: " + oldProperty + " - value: " + value);
        			nodeService.setProperty(nodeRef, newProperty, value);
        			nodeService.removeProperty(nodeRef, oldProperty);
        		}        		   	
        	}
    	}
    	finally{
    		policyBehaviourFilter.enableAllBehaviours();
    	}    		      
	}	
	
	private void migrateAutoNum(){

		logger.info("migrateAutoNum");
		
		List<NodeRef> items = new ArrayList<NodeRef>();
    	ResultSet resultSet = null;
    	
    	SearchParameters sp = new SearchParameters();
        sp.addStore(RepoConsts.SPACES_STORE);
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(" +TYPE:\"bcpg:autoNum\" ");	                         
        
    	try{
    		resultSet = searchService.query(sp);
    		if(resultSet.length() > 0){
    			items = resultSet.getNodeRefs();        			
    		}
    	}	       
    	catch(Exception e){
    		logger.error("Failed to get items", e);
    	}
    	finally{
    		if(resultSet != null)
    			resultSet.close();
    	}        
    	
    	logger.info("items to migrate: " + items.size());    	
    	
    	policyBehaviourFilter.disableAllBehaviours();
    	        	
    	try{
    		
    		for(int cnt=0 ; cnt < items.size() ; cnt++){
        		
        		final NodeRef nodeRef = items.get(cnt);        		
        		String name = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
        		
        		logger.info("start to migrate autoNum, name: " + name);
        		
        		String[] arrValues = name.split(" - ");
        		
        		String classLocalName = arrValues[0];
        		String propertyLocalName = arrValues[1];
        		
        		if(classLocalName != null && propertyLocalName != null){
        			
        			QName classQName = QName.createQName(BeCPGModel.BECPG_URI, classLocalName);
        			
        			TypeDefinition typeDef = dictionaryService.getType(classQName);
        			
        			if(typeDef == null){        			        			
        				
        				// try quality
        				classQName = QName.createQName(QualityModel.QUALITY_URI, classLocalName);
        				
        				typeDef = dictionaryService.getType(classQName);
        				
        				if(typeDef == null){
        					
        					// try sva
            				classQName = QName.createQName("http://www.bcpg.fr/model/sva/1.0", classLocalName);
            				
            				typeDef = dictionaryService.getType(classQName);
            				
            				if(typeDef == null){
            					classQName = null;
            					logger.error("Failed to find type: " + classLocalName);
            				}
        				}        				
        			}
        			
        			QName propertyQName = QName.createQName(BeCPGModel.BECPG_URI, propertyLocalName);
        			PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
        			
        			if(propertyDef == null){
        				propertyQName = null;
    					logger.error("Failed to find property: " + propertyLocalName);
    				}
        			
        			if(classQName != null && propertyQName != null){
        				
        				logger.info("change autoNum, name: " + name);
        				nodeService.setProperty(nodeRef, BeCPGModel.PROP_AUTO_NUM_CLASS_NAME, classQName);
        				nodeService.setProperty(nodeRef, BeCPGModel.PROP_AUTO_NUM_PROPERTY_NAME, propertyQName);
        			}        		
        		}        		
        	}
    	}
    	finally{
    		policyBehaviourFilter.enableAllBehaviours();
    	}    		      
	}
	
	private void migrateProductLists(){

		logger.info("migrateProductLists");
		
		List<NodeRef> items = new ArrayList<NodeRef>();
    	ResultSet resultSet = null;
    	
    	SearchParameters sp = new SearchParameters();
        sp.addStore(RepoConsts.SPACES_STORE);
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(" +ASPECT:\"bcpg:productListsAspect\" ");	                         
        
    	try{
    		resultSet = searchService.query(sp);
    		if(resultSet.length() > 0){
    			items = resultSet.getNodeRefs();        			
    		}
    	}	       
    	catch(Exception e){
    		logger.error("Failed to get items", e);
    	}
    	finally{
    		if(resultSet != null)
    			resultSet.close();
    	}        
    	
    	logger.info("items to migrate: " + items.size());
    	QName productListsAssocQName = QName.createQName(BeCPGModel.BECPG_URI, "productLists");
    	QName productListsAspectQName = QName.createQName(BeCPGModel.BECPG_URI, "productListsAspect");    	
    	
    	policyBehaviourFilter.disableAllBehaviours();
    	        	
    	try{
    		
    		for(int cnt=0 ; cnt < items.size() ; cnt++){
        		
        		final NodeRef nodeRef = items.get(cnt);
        		
        		// add entityListsAspect
        		if(!nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_ENTITYLISTS)){
        			nodeService.addAspect(nodeRef, BeCPGModel.ASPECT_ENTITYLISTS, null);
        		}
        		
        		// productLists
        		NodeRef listContainerNodeRef = nodeService.getChildByName(nodeRef, productListsAssocQName, RepoConsts.CONTAINER_DATALISTS);
        		
        		if(listContainerNodeRef != null){
        			logger.info("move listContainerNodeRef of node: " + nodeRef);
        			nodeService.moveNode(listContainerNodeRef, nodeRef, BeCPGModel.ASSOC_ENTITYLISTS, BeCPGModel.ASSOC_ENTITYLISTS);
        		}    
        		
        		// remove productListsAspect
        		if(nodeService.hasAspect(nodeRef, productListsAspectQName)){
        			nodeService.removeAspect(nodeRef, productListsAspectQName);
        		}
        	}
    	}
    	finally{
    		policyBehaviourFilter.enableAllBehaviours();
    	}    		      
	}	
	
	private void migrateDataLists(){

		logger.info("migrateDataLists");
		
		List<NodeRef> items = new ArrayList<NodeRef>();
    	ResultSet resultSet = null;
    	
    	SearchParameters sp = new SearchParameters();
        sp.addStore(RepoConsts.SPACES_STORE);
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(" +ASPECT:\"bcpg:dataListsAspect\" ");	                         
        
    	try{
    		resultSet = searchService.query(sp);
    		if(resultSet.length() > 0){
    			items = resultSet.getNodeRefs();        			
    		}
    	}	       
    	catch(Exception e){
    		logger.error("Failed to get items", e);
    	}
    	finally{
    		if(resultSet != null)
    			resultSet.close();
    	}        
    	
    	logger.info("items to migrate: " + items.size());
    	QName dataListsAssocQName = QName.createQName(BeCPGModel.BECPG_URI, "dataLists");
    	QName productListsAspectQName = QName.createQName(BeCPGModel.BECPG_URI, "productListsAspect");
    	QName dataListsAspectQName = QName.createQName(BeCPGModel.BECPG_URI, "dataListsAspect");
    	
    	policyBehaviourFilter.disableAllBehaviours();
    	        	
    	try{
    		
    		for(int cnt=0 ; cnt < items.size() ; cnt++){
        		
        		final NodeRef nodeRef = items.get(cnt);
        		
        		logger.info("node: " + nodeRef);
        		
        		// add entityListsAspect
        		if(!nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_ENTITYLISTS)){
        			nodeService.addAspect(nodeRef, BeCPGModel.ASPECT_ENTITYLISTS, null);
        		}
        		
        		// dataLists
        		NodeRef listContainerNodeRef = nodeService.getChildByName(nodeRef, dataListsAssocQName, RepoConsts.CONTAINER_DATALISTS);
        		
        		if(listContainerNodeRef != null){
        			
        			// remove residual productListAspect
        			if(nodeService.hasAspect(nodeRef, productListsAspectQName)){
        				logger.info("remove residual productListAspect of node: " + nodeRef);
        				nodeService.removeAspect(nodeRef, productListsAspectQName);
        			}
        			
        			logger.info("move listContainerNodeRef of node: " + nodeRef);
        			nodeService.moveNode(listContainerNodeRef, nodeRef, BeCPGModel.ASSOC_ENTITYLISTS, BeCPGModel.ASSOC_ENTITYLISTS);
        		}   
        		
        		// remove dataListsAspect
        		if(nodeService.hasAspect(nodeRef, dataListsAspectQName)){
        			nodeService.removeAspect(nodeRef, dataListsAspectQName);
        		}
        	}
    	}
    	finally{
    		policyBehaviourFilter.enableAllBehaviours();
    	}    		      
	}	
}
