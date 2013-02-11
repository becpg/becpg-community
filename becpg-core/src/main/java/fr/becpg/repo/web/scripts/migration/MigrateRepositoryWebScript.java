/*
 * 
 */
package fr.becpg.repo.web.scripts.migration;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.person.PersonServiceImpl;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.version.BeCPGVersionMigrator;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.migration.BeCPGSystemFolderMigrator;
import fr.becpg.repo.migration.EntityFolderMigrator;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * The Class MigrateRepositoryWebScript.
 *
 * @author querephi
 */
public class MigrateRepositoryWebScript extends AbstractWebScript
{	
	private static final String PARAM_ACTION = "action";
	private static final String PARAM_NODEREF = "nodeRef";
	private static final String PARAM_OLD_USERNAME = "oldUserName";
	private static final String PARAM_NEW_USERNAME = "newUserName";
	private static final String PARAM_PAGINATION = "pagination";
	
	private static final String ACTION_MIGRATE_SYSTEM_FOLDER = "systemFolder";
	private static final String ACTION_MIGRATE_FIX_PRODUCT_HIERARCHY = "fixProductHierarchy";
	private static final String ACTION_MIGRATE_PROPERTY = "property";
	private static final String ACTION_MIGRATE_VERSION = "version";
	private static final String ACTION_DELETE_MODEL = "deleteModel";
	private static final String ACTION_RENAME_USER = "renameUser";
	private static final String ACTION_MIGRATE_ENTITY_FOLDER = "entityFolder";
	private static final String ACTION_MIGRATE_CLASSIFY_PRODUCT = "classifyProduct";	
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(MigrateRepositoryWebScript.class);
				
	

	/** The search service. */
	private BeCPGSearchService beCPGSearchService;
	
	
	private BehaviourFilter policyBehaviourFilter;
	
	private NodeService mlNodeService;
		   					
	private BeCPGVersionMigrator beCPGVersionMigrator;
	
	private BeCPGSystemFolderMigrator beCPGSystemFolderMigrator;
	
	private PersonService personService;
	
	private NodeService nodeService;
	
	private EntityFolderMigrator entityFolderMigrator;
	
	private ProductService productService;
	
	private Repository repository;

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}
	
	public void setMlNodeService(NodeService mlNodeService) {
		this.mlNodeService = mlNodeService;
	}
	
	public void setBeCPGVersionMigrator(BeCPGVersionMigrator beCPGVersionMigrator) {
		this.beCPGVersionMigrator = beCPGVersionMigrator;
	}
	
	public void setBeCPGSystemFolderMigrator(BeCPGSystemFolderMigrator beCPGSystemFolderMigrator) {
		this.beCPGSystemFolderMigrator = beCPGSystemFolderMigrator;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setEntityFolderMigrator(EntityFolderMigrator entityFolderMigrator) {
		this.entityFolderMigrator = entityFolderMigrator;
	}
	
	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws WebScriptException
    {
    	logger.debug("start migration");
    	Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();	    	
    	
    	String action = templateArgs.get(PARAM_ACTION);
    	String pagination = req.getParameter(PARAM_PAGINATION);
    	Integer iPagination = (pagination != null && !pagination.isEmpty()) ? Integer.parseInt(pagination) : null;
		
    	if(ACTION_MIGRATE_PROPERTY.equals(action)){
    		// migration ingMLName
    		QName ingMLNameQName = QName.createQName(BeCPGModel.BECPG_URI, "ingMLName");
    		migrateProperty(iPagination, " +TYPE:\"bcpg:ing\" ", ingMLNameQName, BeCPGModel.PROP_LEGAL_NAME, mlNodeService);
    	}
    	else if(ACTION_MIGRATE_VERSION.equals(action)){
    		
    		migrationVersion();
    	}
    	else if(ACTION_DELETE_MODEL.equals(action)){
    		NodeRef modelNodeRef = new NodeRef( req.getParameter(PARAM_NODEREF));
    		deleteModel(modelNodeRef);
    	} else if(ACTION_MIGRATE_SYSTEM_FOLDER.equals(action)){
    		beCPGSystemFolderMigrator.migrate();
    	} else if(ACTION_MIGRATE_FIX_PRODUCT_HIERARCHY.equals(action)){
    		beCPGSystemFolderMigrator.fixDeletedHierarchies();
    	} else if(ACTION_RENAME_USER.equals(action)){
    		String oldUserName = req.getParameter(PARAM_OLD_USERNAME);
    		String newUserName = req.getParameter(PARAM_NEW_USERNAME);
    		if(oldUserName!=null && !oldUserName.isEmpty() && newUserName!=null && !newUserName.isEmpty()){
    			renameUser(oldUserName, newUserName);
    		}    		
    	} else if(ACTION_MIGRATE_ENTITY_FOLDER.equals(action)){
    		try{
    			policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
    			entityFolderMigrator.migrate();
    		}
    		finally{
    			policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
    		}    		
    	}
    	else if(ACTION_MIGRATE_CLASSIFY_PRODUCT.equals(action)){
    		try{
    			policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
    			
    			// search for entities to migrate
    			List<NodeRef> productsNodeRef = beCPGSearchService.search("+TYPE:\"bcpg:product\" -ASPECT:\"bcpg:compositeVersionable\" ", null, RepoConsts.MAX_RESULTS_UNLIMITED, SearchService.LANGUAGE_LUCENE);
    			
    			for (NodeRef productNodeRef : productsNodeRef){
    				productService.classifyProduct(repository.getCompanyHome(), productNodeRef);
    			}    			
    		}
    		finally{
    			policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
    		}    		
    	}
    	else{
    		logger.error("Unknown action" + action);
    	}
    	
    }
	
	private void migrateProperty(Integer iPagination, String query, QName oldProperty, QName newProperty, NodeService nodeService){

		logger.info("migrateProperty");
		
		List<NodeRef> items =  beCPGSearchService.luceneSearch(query);

    	logger.info("items to migrate: " + items.size());    	
    	
    	int maxCnt = iPagination != null && iPagination < items.size() ? iPagination : items.size();
		for(int cnt=0 ; cnt < maxCnt ; cnt++){
    		
			NodeRef nodeRef = items.get(cnt);
    		
	    	policyBehaviourFilter.disableBehaviour(nodeRef);
	    	        	
	    	try{
	    		        		
        		Serializable value = nodeService.getProperty(nodeRef, oldProperty);
        		
        		if(value != null){
        			logger.info("node: " + nodeRef + " - change property: " + oldProperty + " - value: " + value);
        			nodeService.setProperty(nodeRef, newProperty, value);
        			nodeService.removeProperty(nodeRef, oldProperty);
        		}        		   	
	    	}
	    	finally{
	    		policyBehaviourFilter.enableBehaviour(nodeRef);
	    	}
		}
	}
	
	private void migrationVersion(){
		
		beCPGVersionMigrator.migrateVersionHistory();
	}
	
	private void deleteModel(NodeRef modelNodeRef){

		logger.info("deleteModel");		
    	policyBehaviourFilter.disableBehaviour(modelNodeRef);
    	        	
    	try{
    		        		
    		mlNodeService.deleteNode(modelNodeRef);      		   	
    	}
    	finally{
    		policyBehaviourFilter.enableBehaviour(modelNodeRef);
    	}
	}
	
	private void renameUser(String oldUsername, String newUsername)
    {
		logger.info("\""+oldUsername+"\" --> \""+newUsername+"\""); 
        try
        {
            NodeRef person = personService.getPerson(oldUsername, false);
            
            // Allow us to update the username just like the LDAP process
            AlfrescoTransactionSupport.bindResource(PersonServiceImpl.KEY_ALLOW_UID_UPDATE, Boolean.TRUE);

            // Update the username property which will result in a PersonServiceImpl.onUpdateProperties call
            // on commit.
            nodeService.setProperty(person, ContentModel.PROP_USERNAME, newUsername);
        }
        catch (NoSuchPersonException e)
        {
            logger.error("User does not exist: "+oldUsername);
        }
    }
}
