/*
 * 
 */
package fr.becpg.repo.web.scripts.migration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.person.PersonServiceImpl;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.google.common.collect.Lists;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.version.BeCPGVersionMigrator;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.migration.MigrationService;
import fr.becpg.repo.migration.impl.BeCPGSystemFolderMigrator;
import fr.becpg.repo.migration.impl.EntityFolderMigrator;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * The Class MigrateRepositoryWebScript.
 * 
 * @author querephi
 */
public class MigrateRepositoryWebScript extends AbstractWebScript {	
	
	private static final String PARAM_ACTION = "action";
	private static final String PARAM_NODEREF = "nodeRef";
	private static final String PARAM_OLD_USERNAME = "oldUserName";
	private static final String PARAM_NEW_USERNAME = "newUserName";	

	private static final String ACTION_MIGRATE_SYSTEM_FOLDER = "systemFolder";
	private static final String ACTION_MIGRATE_FIX_PRODUCT_HIERARCHY = "fixProductHierarchy";
	private static final String ACTION_MIGRATE_VERSION = "version";
	private static final String ACTION_DELETE_MODEL = "deleteModel";
	private static final String ACTION_RENAME_USER = "renameUser";
	private static final String ACTION_MIGRATE_ENTITY_FOLDER = "entityFolder";
	private static final String ACTION_MIGRATE_CLASSIFY_PRODUCT = "classifyProduct";
	private static final String ACTION_DELETE_UNUSED_INGS = "deleteUnusedIngs";
	
	private static final String ACTION_REMOVE_ASPECT = "removeAspect";
	private static final String ACTION_ADD_MANDATORY_ASPECT = "addMandatoryAspect";
	private static final String PARAM_TYPE = "type";
	private static final String PARAM_ASPECT = "aspect";
	
	private static final String ACTION_MIGRATE_ASSOC = "migrateAssociation";
	private static final String PARAM_CLASS_QNAME = "classQName";
	private static final String PARAM_SOURCE_ASSOC = "sourceAssoc";
	private static final String PARAM_TARGET_ASSOC = "targetAssoc";
	
	private static final String ACTION_MIGRATE_PROP = "migrateProperty";
	private static final String PARAM_SOURCE_PROP = "sourceProp";
	private static final String PARAM_TARGET_PROP = "targetProp";

	private static final String ACTION_ADD_NUT_FACTS_METHODS = "addNutFactsMethods";
	private static final String ACTION_CREATE_GEN_RAWMATERIAL = "createGenRawMaterial";
	
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

	private TenantAdminService tenantAdminService;
	
	private NamespaceService namespaceService;	
	
	private MigrationService migrationService;
	
	protected AlfrescoRepository<ProductData> alfrescoRepository;
	
	protected TransactionService transactionService;
	
	protected AssociationService associationService;

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

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
	
	public void setTenantAdminService(TenantAdminService tenantAdminService) {
		this.tenantAdminService = tenantAdminService;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}
	
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setMigrationService(MigrationService migrationService) {
		this.migrationService = migrationService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	public void doMigrateEntityFolderInMt() {
		PropertyCheck.mandatory(this, "tenantAdminService", tenantAdminService);
		/*
		 * Ensure transactionality and the correct authentication
		 */
		AuthenticationUtil.runAs(new RunAsWork<Object>() {
			public Object doWork() throws Exception {

				if (tenantAdminService.isEnabled()) {
					for (final Tenant tenant : tenantAdminService.getAllTenants()) {
						AuthenticationUtil.runAs(new RunAsWork<Object>() {
							public Object doWork() throws Exception {
								logger.info("Migrate entity folder for tenant: "+tenant.getTenantDomain());
								
								entityFolderMigrator.migrate();							

								return null;
							}

						}, tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenant.getTenantDomain()));
					}

				} else {
				  logger.info("Migrate entity folder for in non-tenant environment");
				  entityFolderMigrator.migrate();
				}
				return null;
			}
		}, AuthenticationUtil.getSystemUserName());

	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws WebScriptException {
		logger.debug("start migration");
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();

		String action = templateArgs.get(PARAM_ACTION);		

		if (ACTION_MIGRATE_VERSION.equals(action)) {

			migrationVersion();
		} else if (ACTION_DELETE_MODEL.equals(action)) {
			NodeRef modelNodeRef = new NodeRef(req.getParameter(PARAM_NODEREF));
			deleteModel(modelNodeRef);
		} else if (ACTION_MIGRATE_SYSTEM_FOLDER.equals(action)) {
			beCPGSystemFolderMigrator.migrate();
		} else if (ACTION_MIGRATE_FIX_PRODUCT_HIERARCHY.equals(action)) {
			beCPGSystemFolderMigrator.fixDeletedHierarchies();
		} else if (ACTION_RENAME_USER.equals(action)) {
			String oldUserName = req.getParameter(PARAM_OLD_USERNAME);
			String newUserName = req.getParameter(PARAM_NEW_USERNAME);
			if (oldUserName != null && !oldUserName.isEmpty() && newUserName != null && !newUserName.isEmpty()) {
				renameUser(oldUserName, newUserName);
			}
		} else if (ACTION_MIGRATE_ENTITY_FOLDER.equals(action)) {
			doMigrateEntityFolderInMt();
		} else if (ACTION_MIGRATE_CLASSIFY_PRODUCT.equals(action)) {
			try {
				policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);

				// search for entities to migrate
				List<NodeRef> productsNodeRef = beCPGSearchService.luceneSearch("+TYPE:\"bcpg:product\" -ASPECT:\"bcpg:compositeVersionable\" ", RepoConsts.MAX_RESULTS_UNLIMITED);

				for (NodeRef productNodeRef : productsNodeRef) {
					productService.classifyProduct(repository.getCompanyHome(), productNodeRef);
				}
			} finally {
				policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
			}
		} else if(ACTION_ADD_MANDATORY_ASPECT.equals(action)){
			String type = req.getParameter(PARAM_TYPE);
			String aspect = req.getParameter(PARAM_ASPECT);
			if(type != null && aspect != null){				
				try {
					policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
					migrationService.addMandatoryAspectInMt(QName.createQName(type, namespaceService), QName.createQName(aspect, namespaceService));
				} finally {
					policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
				}				
			}
			else{
				logger.error("Missing param for action " + action + " type " + type + " aspect " + aspect);
			}
		} else if(ACTION_REMOVE_ASPECT.equals(action)){
			String type = req.getParameter(PARAM_TYPE);
			String aspect = req.getParameter(PARAM_ASPECT);
			if(type != null && aspect != null){	
				try {
					policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
					migrationService.removeAspectInMt(QName.createQName(type, namespaceService), QName.createQName(aspect, namespaceService));
				} finally {
					policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
				}							
			}
			else{
				logger.error("Missing param for action " + action + " type " + type + " aspect " + aspect);
			}
		} else if(ACTION_MIGRATE_ASSOC.equals(action)){
			String classQName = req.getParameter(PARAM_CLASS_QNAME);
			String sourceAssoc = req.getParameter(PARAM_SOURCE_ASSOC);
			String targetAssoc = req.getParameter(PARAM_TARGET_ASSOC);
			if(classQName != null && sourceAssoc != null && targetAssoc != null){		
				try {
					policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
					migrationService.migrateAssociationInMt(QName.createQName(classQName, namespaceService), QName.createQName(sourceAssoc, namespaceService), QName.createQName(targetAssoc, namespaceService));
				} finally {
					policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
				}						
			}
			else{
				logger.error("Missing param for action " + action + " classQName " + classQName + " sourceAssoc " + sourceAssoc + " targetAssoc " + targetAssoc);
			}
		}  else if(ACTION_MIGRATE_PROP.equals(action)){
			String classQName = req.getParameter(PARAM_CLASS_QNAME);
			String sourceProp = req.getParameter(PARAM_SOURCE_PROP);
			String targetProp = req.getParameter(PARAM_TARGET_PROP);
			if(classQName != null && sourceProp != null && targetProp != null){	
				try {
					policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
					migrationService.migratePropertyInMt(QName.createQName(classQName, namespaceService), QName.createQName(sourceProp, namespaceService), QName.createQName(targetProp, namespaceService));
				} finally {
					policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
				}				
			}
			else{
				logger.error("Missing param for action " + action + " classQName " + classQName + " sourceProp " + sourceProp + " targetProp " + targetProp);
			}
		} else if(ACTION_ADD_NUT_FACTS_METHODS.equals(action)){
			
			String [] supplierNames = {"Table Ciqual 2012", "Table USDA"};
			String [] nutFactsMethods = {"CIQUAL 2012", "USDA"};
			
			for(int i=0 ; i<supplierNames.length ; i++){
				String query = LuceneHelper.mandatory(LuceneHelper.getCondType(BeCPGModel.TYPE_SUPPLIER)) +
						LuceneHelper.mandatory(LuceneHelper.getCondEqualValue(ContentModel.PROP_NAME, supplierNames[i]));
				
				final String nutFactsMethod = nutFactsMethods[i];
				List<NodeRef> results = beCPGSearchService.luceneSearch(query);
				if(!results.isEmpty()){					
					NodeRef supplierNodeRef = results.get(0);
					List<AssociationRef> assocRefs = nodeService.getSourceAssocs(supplierNodeRef, BeCPGModel.ASSOC_SUPPLIERS);
					logger.info("Migrate table " + supplierNames[i] + " size: " + assocRefs.size());
					
					for (final List<AssociationRef> batchList : Lists.partition(assocRefs, 50)) {
						transactionService.getRetryingTransactionHelper().doInTransaction(
								new RetryingTransactionCallback<Boolean>() {
									public Boolean execute() throws Exception {

										for(AssociationRef assocRef : batchList){
											ProductData productData = alfrescoRepository.findOne(assocRef.getSourceRef());						
											for(NutListDataItem n : productData.getNutList()){
												n.setMethod(nutFactsMethod);
											}						
											alfrescoRepository.save(productData);
										}
										
										return true;
									}
								}, false, true);
					}
					
					
				}
			}	
			
			
			
		}
		else if(ACTION_CREATE_GEN_RAWMATERIAL.equals(action)){
			
			String query = LuceneHelper.mandatory(LuceneHelper.getCondType(BeCPGModel.TYPE_RAWMATERIAL));
			List<NodeRef> rawMaterialNodeRefs = beCPGSearchService.luceneSearch(query, LuceneHelper.getSort(BeCPGModel.PROP_ERP_CODE));
			Map<String, List<NodeRef>> rawMaterialsGroupByERPCode = new HashMap<String, List<NodeRef>>();
			
			for(NodeRef rawMaterialNodeRef : rawMaterialNodeRefs){	
				
				String erpCode = (String)nodeService.getProperty(rawMaterialNodeRef, BeCPGModel.PROP_ERP_CODE);
				
				List<NodeRef> subList = rawMaterialsGroupByERPCode.get(erpCode);
				if(subList == null){
					subList = new ArrayList<NodeRef>();
					rawMaterialsGroupByERPCode.put(erpCode, subList);
				}
				subList.add(rawMaterialNodeRef);				
			}
			
			for(Map.Entry<String, List<NodeRef>> kv : rawMaterialsGroupByERPCode.entrySet()){
				
				if(kv.getValue().size()>1){
					
					NodeRef rawMaterialNodeRef = kv.getValue().get(0);
					ProductData rawMaterialData = alfrescoRepository.findOne(rawMaterialNodeRef);
					RawMaterialData genRawMaterialData = new RawMaterialData();
					genRawMaterialData.setName(kv.getKey() + "-GEN");
					genRawMaterialData.setHierarchy1(rawMaterialData.getHierarchy1());
					genRawMaterialData.setHierarchy2(rawMaterialData.getHierarchy2());
					
//					List<NodeRef> supplierNodeRefs = new ArrayList<NodeRef>(kv.getValue().size());
					
					List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>(kv.getValue().size());
					for(NodeRef rmNodeRef : kv.getValue()){
						Double subQty = new Double(100 / kv.getValue().size());						
						compoList.add(new CompoListDataItem(null, null, null, subQty, CompoListUnit.Perc, null, DeclarationType.Declare, rmNodeRef));
						
						List<NodeRef> rawMaterialSupplierNodeRefs = associationService.getTargetAssocs(rmNodeRef, BeCPGModel.ASSOC_SUPPLIERS);
//						supplierNodeRefs.addAll(rawMaterialSupplierNodeRefs);
						
						// generate a new ERP
						String newERPCode = kv.getKey();						
						for(NodeRef rawMaterialSupplierNodeRef : rawMaterialSupplierNodeRefs){
							newERPCode += "-" + (String)nodeService.getProperty(rawMaterialSupplierNodeRef, BeCPGModel.PROP_ERP_CODE);
						}
						nodeService.setProperty(rmNodeRef, BeCPGModel.PROP_ERP_CODE, newERPCode);						
					}								
					
					genRawMaterialData.getCompoListView().setCompoList(compoList);
					ProductData productData = alfrescoRepository.create(repository.getCompanyHome(), genRawMaterialData);
					nodeService.setProperty(productData.getNodeRef(), BeCPGModel.PROP_ERP_CODE, kv.getKey());
//					associationService.update(genProductData.getNodeRef(), BeCPGModel.ASSOC_SUPPLIERS, supplierNodeRefs);					
				}
			}
		}
		else if(ACTION_DELETE_UNUSED_INGS.equals(action)){
			
			String query = LuceneHelper.mandatory(LuceneHelper.getCondType(BeCPGModel.TYPE_ING));
			List<NodeRef> ingNodeRefs = beCPGSearchService.luceneSearch(query, RepoConsts.MAX_RESULTS_UNLIMITED);
			
			for(NodeRef ingNodeRef : ingNodeRefs){	
				
				List<AssociationRef> ingAssocRefs = nodeService.getSourceAssocs(ingNodeRef, BeCPGModel.ASSOC_INGLIST_ING);
				List<AssociationRef> inVolAssocRefs = nodeService.getSourceAssocs(ingNodeRef, BeCPGModel.ASSOC_ALLERGENLIST_INVOLUNTARY_SOURCES);
				List<AssociationRef> volAssocRefs = nodeService.getSourceAssocs(ingNodeRef, BeCPGModel.ASSOC_ALLERGENLIST_VOLUNTARY_SOURCES);
				
				if(ingAssocRefs.isEmpty() && inVolAssocRefs.isEmpty() && volAssocRefs.isEmpty() && nodeService.exists(ingNodeRef)){
					logger.info("Delete ing : " + nodeService.getProperty(ingNodeRef, ContentModel.PROP_NAME));
					nodeService.deleteNode(ingNodeRef);
				}							
			}
			
		}
		else {
			logger.error("Unknown action" + action);
		}

	}

	private void migrationVersion() {

		beCPGVersionMigrator.migrateVersionHistory();
	}

	private void deleteModel(NodeRef modelNodeRef) {

		logger.info("deleteModel");
		policyBehaviourFilter.disableBehaviour(modelNodeRef);

		try {

			mlNodeService.deleteNode(modelNodeRef);
		} finally {
			policyBehaviourFilter.enableBehaviour(modelNodeRef);
		}
	}

	private void renameUser(String oldUsername, String newUsername) {
		logger.info("\"" + oldUsername + "\" --> \"" + newUsername + "\"");
		try {
			NodeRef person = personService.getPerson(oldUsername, false);

			// Allow us to update the username just like the LDAP process
			AlfrescoTransactionSupport.bindResource(PersonServiceImpl.KEY_ALLOW_UID_UPDATE, Boolean.TRUE);

			// Update the username property which will result in a
			// PersonServiceImpl.onUpdateProperties call
			// on commit.
			nodeService.setProperty(person, ContentModel.PROP_USERNAME, newUsername);
		} catch (NoSuchPersonException e) {
			logger.error("User does not exist: " + oldUsername);
		}
	}
}
