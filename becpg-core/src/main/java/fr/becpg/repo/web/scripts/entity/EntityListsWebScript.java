/*
 * 
 */
package fr.becpg.repo.web.scripts.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.model.SecurityModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.SiteHelper;
import fr.becpg.repo.policy.BeCPGPolicyHelper;
import fr.becpg.repo.security.SecurityService;

/**
 * The Class ProductListsWebScript.
 * 
 * @author querephi
 */
public class EntityListsWebScript extends DeclarativeWebScript {

	// request parameter names
	/** The Constant PARAM_STORE_TYPE. */
	private static final String PARAM_STORE_TYPE = "store_type";

	/** The Constant PARAM_STORE_ID. */
	private static final String PARAM_STORE_ID = "store_id";

	/** The Constant PARAM_ACL_MODE. */
	private static final String PARAM_ACL_MODE = "aclMode";

	/** The Constant PARAM_ID. */
	private static final String PARAM_ID = "id";
	// model key names
	/** The Constant MODEL_KEY_NAME_PRODUCT. */
	private static final String MODEL_KEY_NAME_ENTITY = "entity";

	/** The Constant MODEL_KEY_NAME_CONTAINER. */
	private static final String MODEL_KEY_NAME_CONTAINER = "container";

	/** The Constant MODEL_KEY_NAME_LISTS. */
	private static final String MODEL_KEY_NAME_LISTS = "lists";

	/** The Constant MODEL_HAS_WRITE_PERMISSION. */
	private static final String MODEL_HAS_WRITE_PERMISSION = "hasWritePermission";

	/** the Constant MODEL_KEY_ACL_TYPE **/
	private static final String MODEL_KEY_ACL_TYPE = "aclType";

	/** The Constant MODEL_WUSED_LIST. */
	private static final String MODEL_WUSED_LIST = "wUsedList";
	
	private static final String MODEL_PROP_KEY_LIST_TYPES = "listTypes";

	private static final String MODEL_KEY_NAME_ENTITY_PATH = "entityPath";

	/** The logger. */
	private static Log logger = LogFactory.getLog(EntityListsWebScript.class);

	/** The node service. */
	private NodeService nodeService;

	/** The security service. */
	private SecurityService securityService;

	private EntityListDAO entityListDAO;

	private EntityTplService entityTplService;

	private NamespaceService namespaceService;

	private TransactionService transactionService;
	
	private DictionaryService dictionaryService;
	
	private AuthorityService authorityService;
	
	private PermissionService permissionService;
	
	private AssociationService associationService;	
	
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setEntityTplService(EntityTplService entityTplService) {
		this.entityTplService = entityTplService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	/**
	 * Suggest values according to query
	 * 
	 * url : /becpg/entitylists/node/{store_type}/{store_id}/{id}.
	 * 
	 * @param req
	 *            the req
	 * @param status
	 *            the status
	 * @param cache
	 *            the cache
	 * @return the map
	 */
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		String storeType = templateArgs.get(PARAM_STORE_TYPE);
		String storeId = templateArgs.get(PARAM_STORE_ID);
		String nodeId = templateArgs.get(PARAM_ID);
		String aclMode = req.getParameter(PARAM_ACL_MODE);

		logger.debug("entityListsWebScript executeImpl()");

		List<NodeRef> listsNodeRef = new ArrayList<NodeRef>();
		final NodeRef nodeRef = new NodeRef(storeType, storeId, nodeId);
		NodeRef listContainerNodeRef = null;
		QName nodeType = nodeService.getType(nodeRef);
		boolean hasWritePermission = authorityService.isAdminAuthority(AuthenticationUtil.getRunAsUser());//admin can delete entity lists
		boolean skipFilter = false;
		String wUsedList = null;

		Map<String, Object> model = new HashMap<String, Object>();
		// We get datalist for a given aclGroup
		if (aclMode != null && SecurityModel.TYPE_ACL_GROUP.equals(nodeType)) {
			logger.debug("We want to get datalist for current ACL entity");
			String aclType = (String) nodeService.getProperty(nodeRef, SecurityModel.PROP_ACL_GROUP_NODE_TYPE);
			QName aclTypeQname = QName.createQName( aclType, namespaceService);
			model.put(MODEL_KEY_ACL_TYPE, aclType);

			NodeRef templateNodeRef = entityTplService.getEntityTpl(aclTypeQname);
			if (templateNodeRef != null) {
				listContainerNodeRef = entityListDAO.getListContainer(templateNodeRef);
				if (listContainerNodeRef == null) {
					listContainerNodeRef = entityListDAO.createListContainer(templateNodeRef);
				}
			} else {
				logger.error("Cannot get templateNodeRef for type : " + aclType);
			}
			skipFilter = true;
		}
		// We get datalist for entityTpl
		else if ((nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_ENTITYLISTS) && nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_ENTITY_TPL)) || 
				BeCPGModel.TYPE_SYSTEM_ENTITY.equals(nodeType)) {

			listContainerNodeRef = entityListDAO.getListContainer(nodeRef);
			if (listContainerNodeRef == null) {
				listContainerNodeRef = entityListDAO.createListContainer(nodeRef);
			}
	
			// Add types that can be added
	        Set<ClassDefinition> classDefinitions = new HashSet<ClassDefinition>();
			Collection <QName> entityListTypes = dictionaryService.getSubTypes(BeCPGModel.TYPE_ENTITYLIST_ITEM, true);
			
	        for(QName entityListType: entityListTypes)
		    {	
	        	if(!BeCPGModel.TYPE_ENTITYLIST_ITEM.equals(entityListType) /*&& !BeCPGModel.TYPE_PRODUCTLIST_ITEM.equals(entityListType)*/){
	        		classDefinitions.add(dictionaryService.getClass(entityListType));
	        	}
		    }
	    	
	    	model.put(MODEL_PROP_KEY_LIST_TYPES, classDefinitions);
	    	
			hasWritePermission = true;
			skipFilter = true;
		}
		// We get datalist for entity
		else {

			NodeRef entityTplNodeRef = associationService.getTargetAssoc(nodeRef, BeCPGModel.ASSOC_ENTITY_TPL_REF);
			if(entityTplNodeRef == null){
				entityTplNodeRef = entityTplService.getEntityTpl(nodeType);
			}

			if (entityTplNodeRef != null) {
				
				final NodeRef templateNodeRef = entityTplNodeRef;
				// Redmine #59 : copy missing datalists as admin, otherwise, if
				// a datalist is added in product template, users cannot see
				// datalists of valid products
				RunAsWork<Object> actionRunAs = new RunAsWork<Object>() {
					@Override
					public Object doWork() throws Exception {
						RetryingTransactionCallback<Object> actionCallback = new RetryingTransactionCallback<Object>() {
							@Override
							public Object execute() {

								StopWatch watch = null;
								if (logger.isDebugEnabled()) {
									watch = new StopWatch();
									watch.start();
								}
								try {
									BeCPGPolicyHelper.enableCopyBehaviourForTransaction();
										
									entityListDAO.copyDataLists(templateNodeRef, nodeRef, false);

								} finally {
									BeCPGPolicyHelper.disableCopyBehaviourForTransaction();
								}
								
								if (logger.isDebugEnabled()) {
									watch.stop();
									logger.debug("copyDataLists executed in  "
											+ watch.getTotalTimeSeconds() + " seconds - templateNodeRef "
											+  templateNodeRef);
								}
								
								return null;
							}
						};
						return transactionService.getRetryingTransactionHelper().doInTransaction(actionCallback);
					}
				};
				AuthenticationUtil.runAs(actionRunAs, AuthenticationUtil.getAdminUserName());

				
			}

			listContainerNodeRef = entityListDAO.getListContainer(nodeRef);
		}

		if (listContainerNodeRef != null) {

			listsNodeRef = entityListDAO.getExistingListsNodeRef(listContainerNodeRef);
		}

		// filter list with perms
		if (!skipFilter) {
			Iterator<NodeRef> it = listsNodeRef.iterator();
			while (it.hasNext()) {
				NodeRef temp = (NodeRef) it.next();
				String dataListType = (String) nodeService.getProperty(temp, DataListModel.PROP_DATALISTITEMTYPE);
				int access_mode = securityService.computeAccessMode(nodeType, dataListType);

				if( SecurityService.NONE_ACCESS == access_mode){
					if (logger.isTraceEnabled()) {
						logger.trace("Don't display dataList:" + dataListType);
					}
					it.remove();
				}
			}
		}
		
		String path = nodeService.getPath(nodeRef).toPrefixString(namespaceService);
		String displayPath = this.nodeService.getPath(nodeRef).toDisplayPath(nodeService, permissionService);

		String retPath = SiteHelper.extractDisplayPath(path,displayPath);

		model.put(MODEL_KEY_NAME_ENTITY_PATH, retPath);
		model.put(MODEL_KEY_NAME_ENTITY, nodeRef);
		model.put(MODEL_KEY_NAME_CONTAINER, listContainerNodeRef);
		model.put(MODEL_HAS_WRITE_PERMISSION, hasWritePermission);
		model.put(MODEL_WUSED_LIST, wUsedList);
		model.put(MODEL_KEY_NAME_LISTS, listsNodeRef);

		return model;
	}

}
