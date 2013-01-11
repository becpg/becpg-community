/*
 * 
 */
package fr.becpg.repo.entity.version;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.cache.BeCPGCacheDataProviderCallBack;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * Store the entity version history in the SpacesStore otherwise we cannot use
 * lucene query and datalists don't work so we cannot get them.
 * 
 * @author querephi
 */
@Service
public class EntityVersionServiceImpl implements EntityVersionService {
	private static final String ENTITIES_HISTORY_NAME = "entitiesHistory";

	private static final QName QNAME_ENTITIES_HISTORY = QName.createQName(BeCPGModel.BECPG_URI, ENTITIES_HISTORY_NAME);

	private static final String ENTITIES_HISTORY_XPATH = "/bcpg:entitiesHistory";
	private static final String KEY_ENTITIES_HISTORY = "EntitiesHistory";
	

	/** The Constant VERSION_NAME_DELIMITER. */
	private static final String VERSION_NAME_DELIMITER = " v";

	/** The logger. */
	private static Log logger = LogFactory.getLog(EntityVersionServiceImpl.class);

	/** The node service. */
	private NodeService nodeService;

	/** The copy service. */
	private CopyService copyService;

	/** The search service. */
	private BeCPGSearchService beCPGSearchService;
	
	private BeCPGCacheService beCPGCacheService;

	
	private EntityListDAO entityListDAO;
	
	/**
	 * Sets the node service.
	 * 
	 * @param nodeService
	 *            the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * Sets the copy service.
	 * 
	 * @param copyService
	 *            the new copy service
	 */
	public void setCopyService(CopyService copyService) {
		this.copyService = copyService;
	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}
	
	public void setBeCPGCacheService(BeCPGCacheService beCPGCacheService) {
		this.beCPGCacheService = beCPGCacheService;
	}

	
	
	
	
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	@Override
	public NodeRef createVersionAndCheckin(final NodeRef origNodeRef, final NodeRef workingCopyNodeRef) {


		logger.debug("createEntityVersion: " + origNodeRef);

		final NodeRef versionHistoryRef = getVersionHistoryNodeRef(origNodeRef);

		NodeRef versionNodeRef = null;

		// Rights are checked by copyService during recursiveCopy
		versionNodeRef = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>() {
				@Override
			public NodeRef doWork() throws Exception {
			    // Non recursive copy
				NodeRef nodeRef =  copyService.copy(origNodeRef, versionHistoryRef, ContentModel.ASSOC_CONTAINS,
							ContentModel.ASSOC_CHILDREN,false);
				//Move origNodeRef DataList to version
				entityListDAO.moveDataLists(origNodeRef, nodeRef);
				//Move workingCopyNodeRef DataList to origNodeRef
				entityListDAO.moveDataLists(workingCopyNodeRef, origNodeRef);
				return nodeRef;

			}
		 }, AuthenticationUtil.getSystemUserName());

		Map<QName, Serializable> versionProperties = nodeService.getProperties(versionNodeRef);

		String name = nodeService.getProperty(origNodeRef, ContentModel.PROP_NAME) + VERSION_NAME_DELIMITER
				+ nodeService.getProperty(origNodeRef, ContentModel.PROP_VERSION_LABEL);
		versionProperties.put(ContentModel.PROP_NAME, name);
		nodeService.addAspect(versionNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION, versionProperties);

		updateVersionEffectivity(origNodeRef, versionNodeRef);
		return versionNodeRef;
	}

	private void updateVersionEffectivity(NodeRef entityNodeRef, NodeRef versionNodeRef) {
		Date newEffectivity = new Date();
		Date oldEffectivity = (Date) nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_START_EFFECTIVITY);
		if (oldEffectivity == null) {
			oldEffectivity = newEffectivity;
		}
		nodeService.setProperty(versionNodeRef, BeCPGModel.PROP_START_EFFECTIVITY, oldEffectivity);
		nodeService.setProperty(versionNodeRef, BeCPGModel.PROP_END_EFFECTIVITY, newEffectivity);
	}

	/**
	 * Gets the version assocs.
	 * 
	 * @param versionHistoryRef
	 *            the version history ref
	 * @param preLoad
	 *            the pre load
	 * @return the version assocs
	 */
	private List<ChildAssociationRef> getVersionAssocs(NodeRef versionHistoryRef, boolean preLoad) {

		return nodeService.getChildAssocs(versionHistoryRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL,
				preLoad);
	}

	/**
	 * Creates a new version history node, applying the root version aspect is
	 * required.
	 * 
	 * @param nodeRef
	 *            the node ref
	 * @return the version history node reference
	 */
	private NodeRef createVersionHistory(NodeRef nodeRef) {
		long start = System.currentTimeMillis();

		HashMap<QName, Serializable> props = new HashMap<QName, Serializable>();
		props.put(ContentModel.PROP_NAME, nodeRef.getId());

		ChildAssociationRef childAssocRef = nodeService.createNode(getEntitiesHistoryFolder(),
				ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, nodeRef.getId()), ContentModel.TYPE_FOLDER,
				props);

		if (logger.isTraceEnabled()) {
			logger.trace("created version history nodeRef: " + childAssocRef.getChildRef() + " for " + nodeRef + " in "
					+ (System.currentTimeMillis() - start) + " ms");
		}

		return childAssocRef.getChildRef();
	}

	/**
	 * Gets a reference to the version history node for a given 'real' node.
	 * 
	 * @param nodeRef
	 *            a node reference
	 * @return a reference to the version history node, null of none
	 */
	private NodeRef getVersionHistoryNodeRef(NodeRef nodeRef) {
		NodeRef vhNodeRef = null;

		if (getEntitiesHistoryFolder() != null) {
			vhNodeRef = nodeService.getChildByName(getEntitiesHistoryFolder(), ContentModel.ASSOC_CONTAINS,
					nodeRef.getId());
		}

		if (vhNodeRef == null) {
			logger.debug("createVersionHistory");
			vhNodeRef = createVersionHistory(nodeRef);
		}

		return vhNodeRef;
	}

	/**
	 * Get the entitys history folder node where we store entity versions.
	 * 
	 * @return the entitys history folder
	 */
	@Override
	public NodeRef getEntitiesHistoryFolder() {
		
		return beCPGCacheService.getFromCache(EntityVersionServiceImpl.class.getName(), KEY_ENTITIES_HISTORY , new BeCPGCacheDataProviderCallBack<NodeRef>() {

			@Override
			public NodeRef getData() {
				
				NodeRef entitiesHistoryNodeRef = null;
				List<NodeRef> resultSet = null;

				try {
					resultSet = beCPGSearchService.search(ENTITIES_HISTORY_XPATH, null, RepoConsts.MAX_RESULTS_SINGLE_VALUE, SearchService.LANGUAGE_XPATH);
					
					if (!resultSet.isEmpty()) {
						entitiesHistoryNodeRef = resultSet.get(0);
					}
					else{
						
						// create folder
						final NodeRef storeNodeRef = nodeService.getRootNode(RepoConsts.SPACES_STORE);

						return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>() {
							@Override
							public NodeRef doWork() throws Exception {								
								logger.debug("create folder 'EntitysHistory'");
								HashMap<QName, Serializable> props = new HashMap<QName, Serializable>();
								props.put(ContentModel.PROP_NAME, ENTITIES_HISTORY_NAME);
								return nodeService.createNode(storeNodeRef, ContentModel.ASSOC_CHILDREN,
										QNAME_ENTITIES_HISTORY, ContentModel.TYPE_FOLDER, props).getChildRef();
							}
						}, AuthenticationUtil.getSystemUserName());	
					}
				} catch (Exception e) {
					logger.error("Failed to get entitysHistory", e);
				}
				
				return entitiesHistoryNodeRef;
			}			
		});		
	}

	@Override
	public void deleteVersionHistory(NodeRef entityNodeRef) {
		NodeRef versionHistoryRef = getVersionHistoryNodeRef(entityNodeRef);
		if (versionHistoryRef != null) {
			nodeService.deleteNode(versionHistoryRef);
		}

	}

	@Override
	public NodeRef getEntityVersion(Version version) {
		
		NodeRef versionHistoryNodeRef = getVersionHistoryNodeRef(version.getVersionedNodeRef());
		List<ChildAssociationRef> versionAssocs = getVersionAssocs(versionHistoryNodeRef, false);

		for (ChildAssociationRef versionAssoc : versionAssocs) {

			NodeRef versionNodeRef = versionAssoc.getChildRef();
			String entityVersionLabel = (String)nodeService.getProperty(versionNodeRef, ContentModel.PROP_VERSION_LABEL);
			logger.debug("versionLabel: " + version.getVersionLabel() + " - entityVersionLabel: " + entityVersionLabel);
			
			if (version.getVersionLabel().equals(entityVersionLabel)) {
				return versionNodeRef;
			}
		}

		logger.error("Failed to find entity version. version: " + version.getFrozenStateNodeRef());
		return null;
	}



}