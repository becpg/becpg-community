/*
 * 
 */
package fr.becpg.repo.web.scripts.debug;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.common.RepoConsts;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemProductType;
import fr.becpg.model.SystemState;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;

// TODO: Auto-generated Javadoc
/**
 * The Class RestoreArchivedNodeWebScript.
 *
 * @author querephi
 */
public class RestoreArchivedNodeWebScript extends AbstractWebScript
{	
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(RestoreArchivedNodeWebScript.class);
		
		//request parameter names
		/** The Constant PARAM_STORE_TYPE. */
		private static final String PARAM_STORE_TYPE = "store_type";
		
		/** The Constant PARAM_STORE_ID. */
		private static final String PARAM_STORE_ID = "store_id";
		
		/** The Constant PARAM_ID. */
		private static final String PARAM_ID = "id";
		
		/** The node service. */
		private NodeService nodeService;
		
		/** The search service. */
		private SearchService searchService;
		
		/** The file folder service. */
		private FileFolderService fileFolderService;
		
		/** The transaction service. */
		private TransactionService transactionService;
		
		private Repository repositoryHelper;
		
		private DictionaryDAO dictionaryDAO;
		
		private RepoService repoService;
			   					
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
		
		/**
		 * Sets the transaction service.
		 *
		 * @param transactionService the new transaction service
		 */
		public void setTransactionService(TransactionService transactionService) {
			this.transactionService = transactionService;
		}
				
		public void setRepositoryHelper(Repository repositoryHelper) {
			this.repositoryHelper = repositoryHelper;
		}
		
		public void setDictionaryDAO(DictionaryDAO dictionaryDAO) {
			this.dictionaryDAO = dictionaryDAO;
		}
		
		public void setRepoService(RepoService repoService) {
			this.repoService = repoService;
		}

		/* (non-Javadoc)
		 * @see org.springframework.extensions.webscripts.WebScript#execute(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse)
		 */
		@Override
		public void execute(WebScriptRequest req, WebScriptResponse res) throws WebScriptException
	    {
	    	logger.debug("start restore archived node webscript");
	    	Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();	    	
	    	String storeType = templateArgs.get(PARAM_STORE_TYPE);
			String storeId = templateArgs.get(PARAM_STORE_ID);
			String nodeId = templateArgs.get(PARAM_ID);
	    	
			NodeRef nodeRef = new NodeRef(storeType, storeId, nodeId);
			
//			migrateAllergenList();
//			
//			migrateCompoList();
//			
//			migrateCostList();
//			
//			migrateIngLabelingList();
//			
//			migratePhysicoChemList();
//			
//			migrateProducts();
			
//			migrateMoveCharacts();
			
//			migrateIngList();
			
//			reloadModel();
			
//			migrateProductFolder();
			
//			migrateOrigins();
			
			migrateMissingMandatoryAspects();
			
//			migrateStringProductCodes();
			
	    }
		
//		private void migrateMoveCharacts(){
//						
//			NodeRef system_oldFolder = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, "System_old");
//			NodeRef allergens_oldFolder = nodeService.getChildByName(system_oldFolder, ContentModel.ASSOC_CONTAINS, "Allergens");
//			
//			NodeRef systemFolder = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, "Système");
//			NodeRef allergensFolder = nodeService.getChildByName(systemFolder, ContentModel.ASSOC_CONTAINS, "Allergènes");
//			
//			if(allergens_oldFolder != null && allergensFolder != null){
//				List<FileInfo> nodes = fileFolderService.listFiles(allergens_oldFolder);
//				
//				logger.debug("###migrate, move allergens: " + nodes.size());
//				
//				for(FileInfo node : nodes){
//					nodeService.moveNode(node.getNodeRef(), allergensFolder, ContentModel.ASSOC_CONTAINS, nodeService.getType(node.getNodeRef()));
//				}
//			}
//			
//			// Ings
//			NodeRef ings_oldFolder = nodeService.getChildByName(system_oldFolder, ContentModel.ASSOC_CONTAINS, "Ings");					
//			NodeRef ingsFolder = nodeService.getChildByName(systemFolder, ContentModel.ASSOC_CONTAINS, "Ingrédients");
//			
//			if(ings_oldFolder != null && ingsFolder != null){
//				List<FileInfo> nodes = fileFolderService.listFiles(ings_oldFolder);
//				
//				logger.debug("###migrate, move ings: " + nodes.size());
//				
//				for(FileInfo node : nodes){
//					nodeService.moveNode(node.getNodeRef(), ingsFolder, ContentModel.ASSOC_CONTAINS, nodeService.getType(node.getNodeRef()));
//				}
//			}
//			
//			// BioOrigins
//			NodeRef lists_oldFolder = nodeService.getChildByName(system_oldFolder, ContentModel.ASSOC_CONTAINS, "Lists");
//			NodeRef bioOrigins_oldFolder = nodeService.getChildByName(lists_oldFolder, ContentModel.ASSOC_CONTAINS, "BioOrigins");
//			NodeRef bioOriginsFolder = nodeService.getChildByName(systemFolder, ContentModel.ASSOC_CONTAINS, "Origines biologiques");
//			
//			if(bioOrigins_oldFolder != null && bioOriginsFolder != null){
//				List<FileInfo> nodes = fileFolderService.listFiles(bioOrigins_oldFolder);
//				
//				logger.debug("###migrate, move bioOrigins: " + nodes.size());
//				
//				for(FileInfo node : nodes){
//					nodeService.moveNode(node.getNodeRef(), bioOriginsFolder, ContentModel.ASSOC_CONTAINS, BeCPGModel.TYPE_BIO_ORIGIN);
//				}
//			}
//			
//			// GeoOrigins
//			NodeRef geoOrigins_oldFolder = nodeService.getChildByName(lists_oldFolder, ContentModel.ASSOC_CONTAINS, "GeoOrigins");
//			NodeRef geoOriginsFolder = nodeService.getChildByName(systemFolder, ContentModel.ASSOC_CONTAINS, "Origines géographiques");
//			
//			if(geoOrigins_oldFolder != null && geoOriginsFolder != null){
//				List<FileInfo> nodes = fileFolderService.listFiles(geoOrigins_oldFolder);
//				
//				logger.debug("###migrate, move geoOrigins: " + nodes.size());
//				
//				for(FileInfo node : nodes){
//					nodeService.moveNode(node.getNodeRef(), geoOriginsFolder, ContentModel.ASSOC_CONTAINS, BeCPGModel.TYPE_GEO_ORIGIN);
//				}
//			}
//		}
		
//		/**
//		 * Inits the code aspect.
//		 */
//		private void migrateAllergenList(){
//
//			List<NodeRef> productListItems = new ArrayList<NodeRef>();
//        	ResultSet resultSet = null;
//        	
//        	SearchParameters sp = new SearchParameters();
//            sp.addStore(RepoConsts.SPACES_STORE);
//            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
//            sp.setQuery("+TYPE:\"bcpg:allergenList\" ");	                         
//            //sp.addSort("@" + ContentModel.PROP_CREATED, true);
//            
//        	try{
//        		resultSet = searchService.query(sp);
//        		if(resultSet.length() > 0){
//        			productListItems = resultSet.getNodeRefs();        			
//        		}
//        	}	       
//        	catch(Exception e){
//        		logger.error("Failed to get productListItems", e);
//        	}
//        	finally{
//        		if(resultSet != null)
//        			resultSet.close();
//        	}        
//        	
//        	for(int cnt=0 ; cnt < productListItems.size() ; cnt++){
//        		
//        		final NodeRef nodeRef = productListItems.get(cnt);
//        		
//        		QName volQName = QName.createQName(BeCPGModel.BECPG_URI, "allergenListIntPresence");
//        		QName inVolQName = QName.createQName(BeCPGModel.BECPG_URI, "allergenListCrossContamination");
//        		QName volSourcesQName = QName.createQName(BeCPGModel.BECPG_URI, "allergenListLeadByIP");
//        		QName inVolSourcesQName = QName.createQName(BeCPGModel.BECPG_URI, "allergenListLeadByCC");
//        		Boolean vol = (Boolean)nodeService.getProperty(nodeRef, volQName);
//        		Boolean inVol = (Boolean)nodeService.getProperty(nodeRef, inVolQName);
//        		
//        		if(vol != null){
//        			logger.debug("migrate vol: " + nodeRef + " - vol: " + vol);
//        			nodeService.removeProperty(nodeRef, volQName);
//        			nodeService.setProperty(nodeRef, BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY, vol);
//        		}
//        		
//        		if(inVol != null){
//        			logger.debug("migrate inVol: " + nodeRef + " - vol: " + inVol);
//        			nodeService.removeProperty(nodeRef, inVolQName);
//        			nodeService.setProperty(nodeRef, BeCPGModel.PROP_ALLERGENLIST_INVOLUNTARY, inVol);
//        		}
//        		
//        		List<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, volSourcesQName);
//        		for(AssociationRef assocRef : assocRefs){
//        			nodeService.createAssociation(nodeRef, assocRef.getTargetRef(), BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY_SOURCES);
//        			nodeService.removeAssociation(nodeRef, assocRef.getTargetRef(), volSourcesQName);
//        		}
//        		
//        		assocRefs = nodeService.getTargetAssocs(nodeRef, inVolSourcesQName);
//        		for(AssociationRef assocRef : assocRefs){
//        			nodeService.createAssociation(nodeRef, assocRef.getTargetRef(), BeCPGModel.PROP_ALLERGENLIST_INVOLUNTARY_SOURCES);
//        			nodeService.removeAssociation(nodeRef, assocRef.getTargetRef(), inVolSourcesQName);
//        		}
//        		
//        	}	            	
//		}
//		
//		private void migrateIngLabelingList(){
//
//			List<NodeRef> productListItems = new ArrayList<NodeRef>();
//        	ResultSet resultSet = null;
//        	
//        	SearchParameters sp = new SearchParameters();
//            sp.addStore(RepoConsts.SPACES_STORE);
//            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
//            sp.setQuery("+TYPE:\"bcpg:ingLabelingList\" ");	                         
//            //sp.addSort("@" + ContentModel.PROP_CREATED, true);
//            
//        	try{
//        		resultSet = searchService.query(sp);
//        		if(resultSet.length() > 0){
//        			productListItems = resultSet.getNodeRefs();        			
//        		}
//        	}	       
//        	catch(Exception e){
//        		logger.error("Failed to get productListItems", e);
//        	}
//        	finally{
//        		if(resultSet != null)
//        			resultSet.close();
//        	}        
//        	
//        	for(int cnt=0 ; cnt < productListItems.size() ; cnt++){
//        		
//        		final NodeRef nodeRef = productListItems.get(cnt);
//        		
//        		QName illGrpQName = QName.createQName(BeCPGModel.BECPG_URI, "illGrp");
//        		String illGrp = (String)nodeService.getProperty(nodeRef, illGrpQName);
//        		
//        		if(illGrp != null){
//        			logger.debug("migrate illGrp: " + nodeRef + " - illGrp: " + illGrp);
//        			nodeService.removeProperty(nodeRef, illGrpQName);
//        			nodeService.setProperty(nodeRef, BeCPGModel.PROP_ILL_GRP, illGrp);
//        		}        		
//        	}	            	
//		}
//		
//		private void migrateCompoList(){
//
//			List<NodeRef> productListItems = new ArrayList<NodeRef>();
//        	ResultSet resultSet = null;
//        	
//        	SearchParameters sp = new SearchParameters();
//            sp.addStore(RepoConsts.SPACES_STORE);
//            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
//            sp.setQuery("+TYPE:\"bcpg:compoList\" ");	                         
//            //sp.addSort("@" + ContentModel.PROP_CREATED, true);
//            
//        	try{
//        		resultSet = searchService.query(sp);
//        		if(resultSet.length() > 0){
//        			productListItems = resultSet.getNodeRefs();        			
//        		}
//        	}	       
//        	catch(Exception e){
//        		logger.error("Failed to get productListItems", e);
//        	}
//        	finally{
//        		if(resultSet != null)
//        			resultSet.close();
//        	}        
//        	
//        	for(int cnt=0 ; cnt < productListItems.size() ; cnt++){
//        		
//        		final NodeRef nodeRef = productListItems.get(cnt);
//        		
//        		String compoListUnit = (String)nodeService.getProperty(nodeRef, BeCPGModel.PROP_COMPOLIST_UNIT);
//        		
//        		if(compoListUnit != null){
//        			if(compoListUnit.equals("U")){
//        				logger.debug("migrate compoListUnit: " + nodeRef + " - unit: " + compoListUnit);
//            			nodeService.setProperty(nodeRef, BeCPGModel.PROP_COMPOLIST_UNIT, "P");
//        			}
//        			else if(compoListUnit.isEmpty()){
//        				logger.debug("migrate compoListUnit: " + nodeRef + " - unit: " + compoListUnit);
//            			nodeService.setProperty(nodeRef, BeCPGModel.PROP_COMPOLIST_UNIT, "kg");
//        			}
//        			
//        		}        		
//        		
//        		QName compoListRoleQName = QName.createQName(BeCPGModel.BECPG_URI, "compoListRole");        		       		
//        		String compoListRole = (String)nodeService.getProperty(nodeRef, compoListRoleQName);
//        		
//        		if(compoListRole != null){
//        			
//        			if(compoListRole.equals("Role1")){
//        				compoListRole = "";
//        			}
//        			logger.debug("migrate compoListRole: " + nodeRef + " - role: " + compoListRole);
//        			nodeService.setProperty(nodeRef, BeCPGModel.PROP_COMPOLIST_DECL_GRP, compoListRole);
//        			nodeService.removeProperty(nodeRef, compoListRoleQName);
//        		}  
//        		
//        		QName compoListDeclTypeQName = QName.createQName(BeCPGModel.BECPG_URI, "compoListDeclaration");
//        		String compoListDeclType = (String)nodeService.getProperty(nodeRef, compoListDeclTypeQName);
//        		
//        		if(compoListDeclType != null){
//        			logger.debug("migrate compoListDeclType: " + nodeRef + " - DeclType: " + compoListDeclType);
//        			nodeService.setProperty(nodeRef, BeCPGModel.PROP_COMPOLIST_DECL_TYPE, compoListDeclType);
//        			nodeService.removeProperty(nodeRef, compoListDeclTypeQName);
//        		}  
//        	}	            	
//		}
//		
//		private void migrateCostList(){
//
//			List<NodeRef> productListItems = new ArrayList<NodeRef>();
//        	ResultSet resultSet = null;
//        	
//        	SearchParameters sp = new SearchParameters();
//            sp.addStore(RepoConsts.SPACES_STORE);
//            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
//            sp.setQuery("+TYPE:\"bcpg:costList\" ");	                         
//            //sp.addSort("@" + ContentModel.PROP_CREATED, true);
//            
//        	try{
//        		resultSet = searchService.query(sp);
//        		if(resultSet.length() > 0){
//        			productListItems = resultSet.getNodeRefs();        			
//        		}
//        	}	       
//        	catch(Exception e){
//        		logger.error("Failed to get productListItems", e);
//        	}
//        	finally{
//        		if(resultSet != null)
//        			resultSet.close();
//        	}        
//        	
//        	for(int cnt=0 ; cnt < productListItems.size() ; cnt++){
//        		
//        		final NodeRef nodeRef = productListItems.get(cnt);
//        		
//        		String costListUnit = (String)nodeService.getProperty(nodeRef, BeCPGModel.PROP_COSTLIST_UNIT);
//        		
//        		if(costListUnit != null && costListUnit.endsWith("/U")){
//        			logger.debug("migrate costListUnit: " + nodeRef + " - unit: " + costListUnit);
//        			nodeService.setProperty(nodeRef, BeCPGModel.PROP_COSTLIST_UNIT, costListUnit.replace("/U", "/P"));
//        		}        		
//        	}	            	
//		}
//		
//		private void migratePhysicoChemList(){
//
//			List<NodeRef> productListItems = new ArrayList<NodeRef>();
//        	ResultSet resultSet = null;
//        	
//        	SearchParameters sp = new SearchParameters();
//            sp.addStore(RepoConsts.SPACES_STORE);
//            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
//            sp.setQuery("+TYPE:\"bcpg:physicoChemList\" ");	                         
//            //sp.addSort("@" + ContentModel.PROP_CREATED, true);
//            
//        	try{
//        		resultSet = searchService.query(sp);
//        		if(resultSet.length() > 0){
//        			productListItems = resultSet.getNodeRefs();        			
//        		}
//        	}	       
//        	catch(Exception e){
//        		logger.error("Failed to get productListItems", e);
//        	}
//        	finally{
//        		if(resultSet != null)
//        			resultSet.close();
//        	}        
//        	
//        	for(int cnt=0 ; cnt < productListItems.size() ; cnt++){
//        		
//        		final NodeRef nodeRef = productListItems.get(cnt);
//        		
//        		String physicoChemListUnit = (String)nodeService.getProperty(nodeRef, BeCPGModel.PROP_PHYSICOCHEMLIST_UNIT);
//        		
//        		if(physicoChemListUnit != null && physicoChemListUnit.equals("U")){
//        			logger.debug("migrate physicoChemListUnit: " + nodeRef + " - unit: " + physicoChemListUnit);
//        			nodeService.setProperty(nodeRef, BeCPGModel.PROP_PHYSICOCHEMLIST_UNIT, "P");
//        		}        		
//        	}	            	
//		}
//		
//		private void migrateProducts(){
//
//			List<NodeRef> productListItems = new ArrayList<NodeRef>();
//        	ResultSet resultSet = null;
//        	
//        	SearchParameters sp = new SearchParameters();
//            sp.addStore(RepoConsts.SPACES_STORE);
//            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
//            sp.setQuery("+TYPE:\"bcpg:product\" -@cm\\:lockType:READ_ONLY_LOCK");	                         
//            //sp.addSort("@" + ContentModel.PROP_CREATED, true);
//            
//        	try{
//        		resultSet = searchService.query(sp);
//        		if(resultSet.length() > 0){
//        			productListItems = resultSet.getNodeRefs();        			
//        		}
//        	}	       
//        	catch(Exception e){
//        		logger.error("Failed to get productListItems", e);
//        	}
//        	finally{
//        		if(resultSet != null)
//        			resultSet.close();
//        	}        
//        	
//        	for(int cnt=0 ; cnt < productListItems.size() ; cnt++){
//        		
//        		final NodeRef nodeRef = productListItems.get(cnt);
//        		
//        		String productUnit = (String)nodeService.getProperty(nodeRef, BeCPGModel.PROP_PRODUCT_UNIT);
//        		
//        		if(productUnit != null && productUnit.equals("U")){
//        			logger.debug("migrate productUnit: " + nodeRef + " - unit: " + productUnit);
//        			nodeService.setProperty(nodeRef, BeCPGModel.PROP_PRODUCT_UNIT, "P");
//        		}        		
//        	}	            	
//		}
		
//		private void migrateProductFolder(){
//
//			List<NodeRef> productListItems = new ArrayList<NodeRef>();
//        	ResultSet resultSet = null;
//        	
//        	SearchParameters sp = new SearchParameters();
//            sp.addStore(RepoConsts.SPACES_STORE);
//            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
//            sp.setQuery("+TYPE:\"bcpg:productFolder\" ");	                         
//            //sp.addSort("@" + ContentModel.PROP_CREATED, true);
//            
//        	try{
//        		resultSet = searchService.query(sp);
//        		if(resultSet.length() > 0){
//        			productListItems = resultSet.getNodeRefs();        			
//        		}
//        	}	       
//        	catch(Exception e){
//        		logger.error("Failed to get productListItems", e);
//        	}
//        	finally{
//        		if(resultSet != null)
//        			resultSet.close();
//        	}        
//        	
//        	logger.debug("query: " + "+TYPE:\"bcpg:productFolder\" ");
//        	logger.debug("productFolder to migrate: " + productListItems.size());
//        	
//        	for(int cnt=0 ; cnt < productListItems.size() ; cnt++){
//        		
//        		final NodeRef nodeRef = productListItems.get(cnt);
//        		
//        		logger.debug("change type: " + nodeRef);
//        		nodeService.setType(nodeRef, BeCPGModel.TYPE_ENTITY_FOLDER);
//        		logger.debug("new type: " + nodeService.getType(nodeRef));
//        	}	            	
//		}
		
//		private void migrateIngList(){
//			
//			List<NodeRef> productListItems = new ArrayList<NodeRef>();
//        	ResultSet resultSet = null;
//        	
//        	SearchParameters sp = new SearchParameters();
//            sp.addStore(RepoConsts.SPACES_STORE);
//            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
//            sp.setQuery("+TYPE:\"bcpg:ingList\" ");	                         
//            //sp.addSort("@" + ContentModel.PROP_CREATED, true);
//            
//        	try{
//        		resultSet = searchService.query(sp);
//        		if(resultSet.length() > 0){
//        			productListItems = resultSet.getNodeRefs();        			
//        		}
//        	}	       
//        	catch(Exception e){
//        		logger.error("Failed to get productListItems", e);
//        	}
//        	finally{
//        		if(resultSet != null)
//        			resultSet.close();
//        	}        
//        	
//        	for(int cnt=0 ; cnt < productListItems.size() ; cnt++){
//        		
//        		final NodeRef nodeRef = productListItems.get(cnt);
//        		        		
//        		nodeService.setProperty(nodeRef, BeCPGModel.PROP_INGLIST_IS_IONIZED, false);        		        	
//        	}	            	
//		}
		
//		private void migrateOrigins(){
//			
//			List<NodeRef> productListItems = new ArrayList<NodeRef>();
//        	ResultSet resultSet = null;
//        	
//        	SearchParameters sp = new SearchParameters();
//            sp.addStore(RepoConsts.SPACES_STORE);
//            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
//            sp.setQuery(" +PATH:\"/app:company_home/cm:System/cm:BioOrigins/*\" +TYPE:\"bcpg:listValue\" ");	                         
//            
//            
//        	try{
//        		resultSet = searchService.query(sp);
//        		if(resultSet.length() > 0){
//        			productListItems = resultSet.getNodeRefs();        			
//        		}
//        	}	       
//        	catch(Exception e){
//        		logger.error("Failed to get productListItems", e);
//        	}
//        	finally{
//        		if(resultSet != null)
//        			resultSet.close();
//        	}        
//        	
//        	logger.debug("bio origins to migrate: " + productListItems.size());
//        	
//        	for(int cnt=0 ; cnt < productListItems.size() ; cnt++){
//        		
//        		final NodeRef nodeRef = productListItems.get(cnt);
//        		        		
//        		nodeService.setType(nodeRef, BeCPGModel.TYPE_BIO_ORIGIN);        		        	
//        	}	
//        	
//        	
//        	sp = new SearchParameters();
//            sp.addStore(RepoConsts.SPACES_STORE);
//            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
//        	sp.setQuery(" +PATH:\"/app:company_home/cm:System/cm:GeoOrigins/*\" +TYPE:\"bcpg:listValue\" ");	                         
//        	
//            
//        	try{
//        		resultSet = searchService.query(sp);
//        		if(resultSet.length() > 0){
//        			productListItems = resultSet.getNodeRefs();        			
//        		}
//        	}	       
//        	catch(Exception e){
//        		logger.error("Failed to get productListItems", e);
//        	}
//        	finally{
//        		if(resultSet != null)
//        			resultSet.close();
//        	}        
//        	
//        	logger.debug("geo origins to migrate: " + productListItems.size());
//        	
//        	for(int cnt=0 ; cnt < productListItems.size() ; cnt++){
//        		
//        		final NodeRef nodeRef = productListItems.get(cnt);
//        		        		
//        		nodeService.setType(nodeRef, BeCPGModel.TYPE_GEO_ORIGIN);        		        	
//        	}	
//		}
		
		private void migrateMissingMandatoryAspects(){

			List<NodeRef> productListItems = new ArrayList<NodeRef>();
        	ResultSet resultSet = null;
        	
        	SearchParameters sp = new SearchParameters();
            sp.addStore(RepoConsts.SPACES_STORE);
            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
            sp.setQuery(" TYPE:\"bcpg:finishedProduct\"  -@cm\\:lockType:READ_ONLY_LOCK");	                         
            
        	try{
        		resultSet = searchService.query(sp);
        		if(resultSet.length() > 0){
        			productListItems = resultSet.getNodeRefs();        			
        		}
        	}	       
        	catch(Exception e){
        		logger.error("Failed to get productListItems", e);
        	}
        	finally{
        		if(resultSet != null)
        			resultSet.close();
        	}        
        	        	
        	logger.debug("productFolder to migrate: " + productListItems.size());
        	
        	for(int cnt=0 ; cnt < productListItems.size() ; cnt++){
        		
        		final NodeRef nodeRef = productListItems.get(cnt);
        		
        		nodeService.addAspect(nodeRef, BeCPGModel.ASPECT_CLIENTS, null);
        		nodeService.addAspect(nodeRef, BeCPGModel.ASPECT_PRODUCT_MICROBIO_CRITERIA, null);
        	}	  
        	
        	sp = new SearchParameters();
            sp.addStore(RepoConsts.SPACES_STORE);
            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
            sp.setQuery(" TYPE:\"bcpg:condSalesUnit\" -@cm\\:lockType:READ_ONLY_LOCK");	                         
            
        	try{
        		resultSet = searchService.query(sp);
        		if(resultSet.length() > 0){
        			productListItems = resultSet.getNodeRefs();        			
        		}
        	}	       
        	catch(Exception e){
        		logger.error("Failed to get productListItems", e);
        	}
        	finally{
        		if(resultSet != null)
        			resultSet.close();
        	}        
        	        	
        	logger.debug("productFolder to migrate: " + productListItems.size());
        	
        	for(int cnt=0 ; cnt < productListItems.size() ; cnt++){
        		
        		final NodeRef nodeRef = productListItems.get(cnt);
        		
        		nodeService.addAspect(nodeRef, BeCPGModel.ASPECT_CLIENTS, null);
        	}
        	
        	sp = new SearchParameters();
            sp.addStore(RepoConsts.SPACES_STORE);
            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
            sp.setQuery(" TYPE:\"bcpg:packagingMaterial\" TYPE:\"bcpg:rawMaterial\" -@cm\\:lockType:READ_ONLY_LOCK");	                         
            
        	try{
        		resultSet = searchService.query(sp);
        		if(resultSet.length() > 0){
        			productListItems = resultSet.getNodeRefs();        			
        		}
        	}	       
        	catch(Exception e){
        		logger.error("Failed to get productListItems", e);
        	}
        	finally{
        		if(resultSet != null)
        			resultSet.close();
        	}        
        	        	
        	logger.debug("productFolder to migrate: " + productListItems.size());
        	QName supplierAssocQName = QName.createQName(BeCPGModel.BECPG_URI, "supplierAssoc");
        	QName supplierAspectQName = QName.createQName(BeCPGModel.BECPG_URI, "supplierAspect");
        	
        	for(int cnt=0 ; cnt < productListItems.size() ; cnt++){
        		
        		final NodeRef nodeRef = productListItems.get(cnt);
        		
        		if(!nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_SUPPLIERS)){
        			
        			logger.debug("Add aspect suppliers");
        			nodeService.addAspect(nodeRef, BeCPGModel.ASPECT_SUPPLIERS, null);
        			
        			if(nodeService.hasAspect(nodeRef, supplierAspectQName)){
        			
        				logger.debug("copy suppliers");
        				
        				List<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, supplierAssocQName);
            			
            			for(AssociationRef assocRef : assocRefs){
            				nodeService.createAssociation(nodeRef, assocRef.getTargetRef(), BeCPGModel.ASSOC_SUPPLIERS);
            			}
            			
            			nodeService.removeAspect(nodeRef, supplierAspectQName);
        			}        			
        		}
        		
        	}
		}
		
//		private void migrateStringProductCodes(){
//
//			StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "version2Store");
//			
//			final NodeRef storeNodeRef = nodeService.getRootNode(storeRef);
//			
//			List<ChildAssociationRef> versionHistoryNodes = nodeService.getChildAssocs(storeNodeRef, Version2Model.CHILD_QNAME_VERSION_HISTORIES, RegexQNamePattern.MATCH_ALL);
//			
//			// get version history folders
//			for(ChildAssociationRef childAssociationRef : versionHistoryNodes){
//			
//				NodeRef versionHistoryNodeRef = childAssociationRef.getChildRef();
//				logger.debug("versionHistoryNode: " + versionHistoryNodeRef);
//				
//				List<ChildAssociationRef> versionNodes = nodeService.getChildAssocs(versionHistoryNodeRef, Version2Model.CHILD_QNAME_VERSIONS, RegexQNamePattern.MATCH_ALL);
//			
//				// get version nodes
//				for(ChildAssociationRef childAssociationRef2 : versionNodes){
//				
//					NodeRef versionNodeRef = childAssociationRef2.getChildRef();
//					logger.debug("versionNode: " + versionNodeRef);
//					QName type = nodeService.getType(versionNodeRef);
//					SystemProductType systemProductType = SystemProductType.valueOf(type);
//					
//					if(!systemProductType.equals(SystemProductType.Unknown)){
//					
//						Serializable code = nodeService.getProperty(versionNodeRef, BeCPGModel.PROP_CODE);
//						
//						if(code != null && code instanceof String){
//							
//							logger.debug("version code is a String: " + code);
//							nodeService.removeProperty(versionNodeRef, BeCPGModel.PROP_CODE);
//						}
//					}
//				}
//			}
//			
//		}	
		
		private void reloadModel(){
		
			dictionaryDAO.reset();			
		}
		
}
