/*
 * 
 */
package fr.becpg.repo.web.scripts.migration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
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
import fr.becpg.model.ReportModel;
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
	private static final String PARAM_PAGINATION = "pagination";
	private static final String VALUE_ACTION_MIGRATE_PROPERTIES = "migrateProperties";
	private static final String VALUE_ACTION_MIGRATE_AUTONUM = "migrateAutoNum";
	private static final String VALUE_ACTION_MIGRATE_ENTITYLISTS = "migrateEntityLists";
	private static final String VALUE_ACTION_MIGRATE_VERSIONHISTORY = "migrateVersionHistory";
	
	private static final String ENTITIES_HISTORY_XPATH = "/bcpg:entitiesHistory";
	private static final String PRODUCTS_HISTORY_XPATH = "/bcpg:productsHistory";
	private static final String ENTITIES_HISTORY_NAME = "entitiesHistory";
    private static final QName QNAME_ENTITIES_HISTORY  = QName.createQName(BeCPGModel.BECPG_URI, ENTITIES_HISTORY_NAME);
	
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
    	logger.debug("start migration");
    	Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();	    	
    	String action = templateArgs.get(PARAM_ACTION);
    	String pagination = templateArgs.get(PARAM_PAGINATION);
    	Integer iPagination = (pagination != null && !pagination.isEmpty()) ? Integer.parseInt(pagination) : null;
		
    	if(action == null){
    		logger.error("action cannot be null");
    	}
    	else if(action.equals(VALUE_ACTION_MIGRATE_PROPERTIES)){
    		
    		// remove old aspect
    		QName reportNodeAspectQName = QName.createQName(ReportModel.REPORT_URI, "reportNodeAspect");
    		removeAspect(iPagination, " +ASPECT:\"rep:reportNodeAspect\" ", reportNodeAspectQName);
    		
    		// migration productCode
    		QName productCodeQName = QName.createQName(BeCPGModel.BECPG_URI, "productCode");
    		migrateProperty(iPagination, " +@bcpg\\:productCode:* ", productCodeQName, BeCPGModel.PROP_CODE);
    		
    		// migration productReportModified
    		QName productReportModifiedQName = QName.createQName(BeCPGModel.BECPG_URI, "productReportModified");
    		migrateProperty(iPagination, " +@bcpg\\:productReportModified:* ", productReportModifiedQName, ReportModel.PROP_REPORT_ENTITY_GENERATED);
    		
    	} 
    	else if(action.equals(VALUE_ACTION_MIGRATE_AUTONUM)){
    		
    		// migrate autoNum
    		migrateAutoNum(iPagination);
    	}
    	else if(action.equals(VALUE_ACTION_MIGRATE_ENTITYLISTS)){
    		
    		// migrate dataLists
    		migrateDataLists(iPagination);
    		
    		// migrate productLists
    		migrateProductLists(iPagination);
    		
    	} 
    	else if(action.equals(VALUE_ACTION_MIGRATE_VERSIONHISTORY)){
    		
    		// migrate version history
    		migrateVersionHistory();
    	}
    }
	
	private void migrateProperty(Integer iPagination, String query, QName oldProperty, QName newProperty){

		logger.info("migrateProperty");
		
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
    		int maxCnt = iPagination != null && iPagination < items.size() ? iPagination : items.size();
    		for(int cnt=0 ; cnt < maxCnt ; cnt++){
        		
        		final NodeRef nodeRef = items.get(cnt);        		
        		Serializable value = nodeService.getProperty(nodeRef, oldProperty);
        		
        		if(value != null){
        			logger.info("node: " + nodeRef + " - change property: " + oldProperty + " - value: " + value);
        			nodeService.setProperty(nodeRef, newProperty, value);
        			nodeService.removeProperty(nodeRef, oldProperty);
        		}        		   	
        	}
    	}
    	finally{
    		policyBehaviourFilter.enableAllBehaviours();
    	}    		      
	}	
	
	private void migrateAutoNum(Integer iPagination){

		logger.info("migrateProperty");
		
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
    		int maxCnt = iPagination != null && iPagination < items.size() ? iPagination : items.size();
    		for(int cnt=0 ; cnt < maxCnt ; cnt++){
        		
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
	
	private void migrateProductLists(Integer iPagination){

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
    		int maxCnt = iPagination != null && iPagination < items.size() ? iPagination : items.size();
    		for(int cnt=0 ; cnt < maxCnt ; cnt++){
        		
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
	
	private void migrateDataLists(Integer iPagination){

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
    		int maxCnt = iPagination != null && iPagination < items.size() ? iPagination : items.size();
    		for(int cnt=0 ; cnt < maxCnt ; cnt++){
        		
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
	
	private void removeAspect(Integer iPagination, String query, QName aspect){

		logger.info("removeAspect");
		
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
    		int maxCnt = iPagination != null && iPagination < items.size() ? iPagination : items.size();
    		for(int cnt=0 ; cnt < maxCnt ; cnt++){
        		
        		final NodeRef nodeRef = items.get(cnt);        		        		  
        		
        		// remove aspect
        		if(nodeService.hasAspect(nodeRef, aspect)){
        			nodeService.removeAspect(nodeRef, aspect);
        		}
        	}
    	}
    	finally{
    		policyBehaviourFilter.enableAllBehaviours();
    	}    		      
	}
	
	private void migrateVersionHistory(){
		
		ResultSet resultSet = null;
    	NodeRef entitiesHistoryNodeRef = null;
    	NodeRef productsHistoryNodeRef = null;
    	
    	try{
    		resultSet = searchService.query(RepoConsts.SPACES_STORE, SearchService.LANGUAGE_XPATH, ENTITIES_HISTORY_XPATH);
    		if(resultSet.length() > 0){
    			entitiesHistoryNodeRef = resultSet.getNodeRef(0);
    		}
    	}	       
    	catch(Exception e){
    		logger.error("Failed to get entitysHistory", e);
    	}
    	finally{
    		if(resultSet != null)
    			resultSet.close();
    	}  
    	
    	try{
    		resultSet = searchService.query(RepoConsts.SPACES_STORE, SearchService.LANGUAGE_XPATH, PRODUCTS_HISTORY_XPATH);
    		if(resultSet.length() > 0){
    			productsHistoryNodeRef = resultSet.getNodeRef(0);
    		}
    	}	       
    	catch(Exception e){
    		logger.error("Failed to get entitysHistory", e);
    	}
    	finally{
    		if(resultSet != null)
    			resultSet.close();
    	}  
    	
    	if(productsHistoryNodeRef != null){
    		
    		// create entities history if needed
    		if(entitiesHistoryNodeRef == null){    		
    			
        		final NodeRef storeNodeRef = nodeService.getRootNode(RepoConsts.SPACES_STORE);
        		
        		entitiesHistoryNodeRef = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>(){
    	            @Override
    				public NodeRef doWork() throws Exception
    	            {                                	                
            			//create folder
            			logger.info("create folder 'EntitysHistory'");
            			HashMap<QName, Serializable> props = new HashMap<QName, Serializable>();
            	        props.put(ContentModel.PROP_NAME, ENTITIES_HISTORY_NAME);
            	        NodeRef nodeRef = nodeService.createNode(storeNodeRef, ContentModel.ASSOC_CHILDREN, QNAME_ENTITIES_HISTORY, ContentModel.TYPE_FOLDER, props).getChildRef();
    	        		
    	                return nodeRef;
    	                
    	            }
    	        }, AuthenticationUtil.getSystemUserName());   
        	}
    		
    		List<FileInfo> versionHistoryFolders = fileFolderService.listFolders(productsHistoryNodeRef);
    		
    		for(FileInfo versionHistoryFolder : versionHistoryFolders){
    			
    			logger.info("migrate folder: " + versionHistoryFolder.getNodeRef() + " -name: " + versionHistoryFolder.getName());    			
    			NodeRef vhNodeRef = nodeService.getChildByName(entitiesHistoryNodeRef, ContentModel.ASSOC_CONTAINS, versionHistoryFolder.getName());
    			
    			if(vhNodeRef == null){
    				logger.info("move folder");
    				nodeService.moveNode(versionHistoryFolder.getNodeRef(), entitiesHistoryNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_FOLDER);
    			}
    			else{
    				
    				logger.info("move versions");    				
    				List<FileInfo> versionFolders = fileFolderService.listFiles(versionHistoryFolder.getNodeRef());
    				
    				for(FileInfo versionFolder : versionFolders){
    				
    					logger.info("move version: " + versionFolder.getNodeRef());    					
    					nodeService.moveNode(versionFolder.getNodeRef(), vhNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_FOLDER);
    				}
    			}
    		}
    	}
	}
}
