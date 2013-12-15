/*
 * 
 */
package fr.becpg.repo.entity.version;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.version.common.versionlabel.SerialVersionLabelPolicy;
import org.alfresco.service.cmr.coci.CheckOutCheckInServiceException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.cache.BeCPGCacheDataProviderCallBack;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityService;
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

	private static final String MSG_ERR_NOT_AUTHENTICATED = "coci_service.err_not_authenticated";

	/** The Constant VERSION_NAME_DELIMITER. */
	private static final String VERSION_NAME_DELIMITER = " v";
	private static final String INITIAL_VERSION = "1.0";

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

	private EntityService entityService;

	private VersionService versionService;

	private AuthenticationService authenticationService;

	private BehaviourFilter policyBehaviourFilter;

	private PermissionService permissionService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

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

	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	public void setVersionService(VersionService versionService) {
		this.versionService = versionService;
	}

	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}


	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	@Override
	public NodeRef createVersion(final NodeRef nodeRef, Map<String, Serializable> versionProperties) {
		return internalCreateVersionAndCheckin(nodeRef, null, versionProperties);
	}

	@Override
	public NodeRef createVersionAndCheckin(final NodeRef origNodeRef, final NodeRef workingCopyNodeRef, Map<String, Serializable> versionProperties) {

		return internalCreateVersionAndCheckin(origNodeRef, workingCopyNodeRef, versionProperties);
	}

	@Override
	public NodeRef checkOutDataListAndFiles(final NodeRef origNodeRef, final NodeRef workingCopyNodeRef) {
		

		
		// Copy entity datalists (rights are checked by copyService during
		// recursiveCopy)
		AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {
			@Override
			public Void doWork() throws Exception {

				try {
					policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);
					entityListDAO.copyDataLists(origNodeRef, workingCopyNodeRef, true);
					entityService.moveFiles(origNodeRef, workingCopyNodeRef);
				} finally {
					policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);
				}

				return null;

			}
		}, AuthenticationUtil.getSystemUserName());

		// Set contributor permission for user to edit datalists
		String userName = getUserName();
		permissionService.setPermission(workingCopyNodeRef, userName, PermissionService.CONTRIBUTOR, true);

		return workingCopyNodeRef;
	}

	@Override
	public void cancelCheckOut(final NodeRef origNodeRef, final NodeRef workingCopyNodeRef) {

		AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>() {
			@Override
			public NodeRef doWork() throws Exception {

				// move files
				entityService.moveFiles(workingCopyNodeRef, origNodeRef);
				return null;

			}
		}, AuthenticationUtil.getSystemUserName());

	}

	private NodeRef internalCreateVersionAndCheckin(final NodeRef origNodeRef, final NodeRef workingCopyNodeRef, Map<String, Serializable> versionProperties) {
		StopWatch watch = new StopWatch();

		if (logger.isDebugEnabled()) {
			watch.start();
			logger.debug("createEntityVersion: " + origNodeRef + " versionProperties: " + versionProperties);
		}

		try {
			NodeRef versionHistoryRef = getVersionHistoryNodeRef(origNodeRef);
			boolean isInitialVersion = versionHistoryRef == null ? true : false;
			if (versionHistoryRef == null) {
				versionHistoryRef = createVersionHistory(getEntitiesHistoryFolder(), origNodeRef);
			}
			final NodeRef finalVersionHistoryRef = versionHistoryRef;
			NodeRef versionNodeRef = null;

			// Rights are checked by copyService during recursiveCopy
			versionNodeRef = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>() {
				@Override
				public NodeRef doWork() throws Exception {

					// version is a copy of working copy or orig for 1st version
					NodeRef nodeToVersionNodeRef = workingCopyNodeRef != null ? workingCopyNodeRef : origNodeRef;

					// Recursive copy
					NodeRef versionNodeRef = copyService.copy(nodeToVersionNodeRef, finalVersionHistoryRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN, true);

					// entityListDAO.copyDataLists(nodeToVersionNodeRef,
					// nodeRef,
					// false);
					// entityService.copyFiles(nodeToVersionNodeRef, nodeRef);

					if (workingCopyNodeRef != null) {

						// remove assoc (copy used to checkin doesn't do it)
						removeRemovedAssociation(workingCopyNodeRef, origNodeRef);

						// Move workingCopyNodeRef DataList to origNodeRef
						entityService.deleteDataLists(origNodeRef, true);
						entityListDAO.moveDataLists(workingCopyNodeRef, origNodeRef);
						// Move files to origNodeRef
						entityService.deleteFiles(origNodeRef, true);
						entityService.moveFiles(workingCopyNodeRef, origNodeRef);
						// delete files that are not moved (ie: Documents)
						// otherwise
						// checkin copy them and fails since they already exits
						entityService.deleteFiles(workingCopyNodeRef, true);
					}

					return versionNodeRef;

				}
			}, AuthenticationUtil.getSystemUserName());

			// Map<QName, Serializable> versionProperties =
			// nodeService.getProperties(versionNodeRef);
			String versionLabel = getVersionLabel(origNodeRef, versionProperties, isInitialVersion);

			String name = nodeService.getProperty(origNodeRef, ContentModel.PROP_NAME) + VERSION_NAME_DELIMITER + versionLabel;
			Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>(2);
			aspectProperties.put(ContentModel.PROP_NAME, name);
			aspectProperties.put(BeCPGModel.PROP_VERSION_LABEL, versionLabel);
			nodeService.addAspect(versionNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION, aspectProperties);

			updateVersionEffectivity(origNodeRef, versionNodeRef);


			return versionNodeRef;

		} finally {
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug("internalCreateVersionAndCheckin run in  " + watch.getTotalTimeSeconds() + " s");
			}
		}
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

	private void removeRemovedAssociation(NodeRef sourceCopy, NodeRef targetCopy) {

		/*
		 * Extending DefaultCopyBehaviourCallback doesn't work since we must
		 * implement it for every aspect
		 */

		List<AssociationRef> sourceAssocRefs = nodeService.getTargetAssocs(sourceCopy, RegexQNamePattern.MATCH_ALL);
		List<AssociationRef> targetAssocRefs = nodeService.getTargetAssocs(targetCopy, RegexQNamePattern.MATCH_ALL);

		// don't copy/remove theses assocs
		List<QName> assocs = new ArrayList<QName>(0);
		// assocs.add(ReportModel.ASSOC_REPORTS);

		List<QName> childAssocs = new ArrayList<QName>(2);
		childAssocs.add(ContentModel.ASSOC_CHILDREN);
		childAssocs.add(BeCPGModel.ASSOC_ENTITYLISTS);

		for (AssociationRef targetAssocRef : targetAssocRefs) {

			if (!ContentModel.ASSOC_WORKING_COPY_LINK.equals(targetAssocRef.getTypeQName())	) {

				if (!assocs.contains(targetAssocRef.getTypeQName())) {

					boolean removeAssoc = true;
					if (targetAssocRef.getTargetRef() != null) {
						for (AssociationRef sourceAssocRef : sourceAssocRefs) {
							if (targetAssocRef.getTargetRef().equals(sourceAssocRef.getTargetRef())) {
								removeAssoc = false;
								break;
							}
						}
					}

					if (removeAssoc) {
						logger.debug("Remove association sourceRef : " + targetCopy + " targetRef: " + targetAssocRef.getTargetRef() + " assocType: "
								+ targetAssocRef.getTypeQName());
						nodeService.removeAssociation(targetCopy, targetAssocRef.getTargetRef(), targetAssocRef.getTypeQName());
					}
				}
			}

		}

	}

	/**
	 * Gets a reference to the version history node for a given 'real' node.
	 * 
	 * @param nodeRef
	 *            a node reference
	 * @return a reference to the version history node, null of none
	 */
	@Override
	public NodeRef getVersionHistoryNodeRef(NodeRef nodeRef) {
		NodeRef vhNodeRef = null;
		NodeRef entitiesHistoryFolder = getEntitiesHistoryFolder();
		if (entitiesHistoryFolder != null) {
			vhNodeRef = nodeService.getChildByName(entitiesHistoryFolder, ContentModel.ASSOC_CONTAINS, nodeRef.getId());
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

		return beCPGCacheService.getFromCache(EntityVersionService.class.getName(), KEY_ENTITIES_HISTORY, new BeCPGCacheDataProviderCallBack<NodeRef>() {

			@Override
			public NodeRef getData() {

				NodeRef entitiesHistoryNodeRef = null;
				List<NodeRef> resultSet = null;

				try {
					resultSet = beCPGSearchService.search(ENTITIES_HISTORY_XPATH, null, RepoConsts.MAX_RESULTS_SINGLE_VALUE, SearchService.LANGUAGE_XPATH);

					if (!resultSet.isEmpty()) {
						entitiesHistoryNodeRef = resultSet.get(0);
					} else {

						// create folder
						final NodeRef storeNodeRef = nodeService.getRootNode(RepoConsts.SPACES_STORE);

						return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>() {
							@Override
							public NodeRef doWork() throws Exception {
								HashMap<QName, Serializable> props = new HashMap<QName, Serializable>();
								props.put(ContentModel.PROP_NAME, ENTITIES_HISTORY_NAME);
								NodeRef n = nodeService.createNode(storeNodeRef, ContentModel.ASSOC_CHILDREN, QNAME_ENTITIES_HISTORY, ContentModel.TYPE_FOLDER, props)
										.getChildRef();

								logger.debug("create folder 'EntitysHistory' " + n + " - " + nodeService.exists(n));

								return n;
							}
						}, AuthenticationUtil.getSystemUserName());
					}
				} catch (Exception e) {
					logger.error("Failed to get entitysHistory", e);
				}

				return entitiesHistoryNodeRef;
			}
		}, true);
	}

	@Override
	public void deleteVersionHistory(NodeRef entityNodeRef) {
		NodeRef versionHistoryRef = getVersionHistoryNodeRef(entityNodeRef);
		if (versionHistoryRef != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("delete versionHistoryRef " + versionHistoryRef);
			}
			nodeService.deleteNode(versionHistoryRef);			
		}
	}

	@Override
	public NodeRef getEntityVersion(Version version) {
		return getEntityVersion(getVersionAssocs(version.getVersionedNodeRef()), version);
	}

	@Override
	public List<EntityVersion> getAllVersions(NodeRef entityNodeRef) {

		List<EntityVersion> entityVersions = new ArrayList<EntityVersion>();
		VersionHistory versionHistory = versionService.getVersionHistory(entityNodeRef);

		if (versionHistory != null) {
			List<ChildAssociationRef> versionAssocs = getVersionAssocs(entityNodeRef);

			for (Version version : versionHistory.getAllVersions()) {
				NodeRef entityVersionNodeRef = getEntityVersion(versionAssocs, version);
				if (entityVersionNodeRef != null) {
					entityVersions.add(new EntityVersion(version, entityVersionNodeRef));
				}
			}
		}

		return entityVersions;
	}

	/**
	 * Get the versions sort by date and node-ide.
	 * 
	 * @param versionHistoryRef
	 *            the version history ref
	 * @param nodeRef
	 *            the node ref
	 * @return the list
	 */
	@Override
	public List<NodeRef> buildVersionHistory(NodeRef versionHistoryRef, NodeRef nodeRef) {

		List<ChildAssociationRef> versionAssocs = getVersionAssocs(versionHistoryRef, true);
		List<NodeRef> versionRefs = new ArrayList<NodeRef>();

		for (ChildAssociationRef versionAssoc : versionAssocs) {

			versionRefs.add(versionAssoc.getChildRef());
		}

		// sort versions by node id
		Collections.sort(versionRefs, new Comparator<NodeRef>() {

			@Override
			public int compare(NodeRef v1, NodeRef v2) {
				Date modifiedDateV1 = (Date) nodeService.getProperty(v1, ContentModel.PROP_CREATED);
				Date modifiedDateV2 = (Date) nodeService.getProperty(v2, ContentModel.PROP_CREATED);
				int result = modifiedDateV1.compareTo(modifiedDateV2);
				if (result == 0) {
					Long dbid1 = (Long) nodeService.getProperty(v1, ContentModel.PROP_NODE_DBID);
					Long dbid2 = (Long) nodeService.getProperty(v2, ContentModel.PROP_NODE_DBID);

					if (dbid1 != null && dbid2 != null) {
						result = dbid1.compareTo(dbid2);
					} else {
						result = 0;

						if (logger.isWarnEnabled()) {
							logger.warn("node-dbid property is missing for versions: " + v1.toString() + " or " + v2.toString());
						}
					}
				}
				return result;
			}

		});

		return versionRefs;

	}

	private String getVersionLabel(NodeRef origNodeRef, Map<String, Serializable> versionProperties, boolean isInitialVersion) {

		QName classRef = nodeService.getType(origNodeRef);
		Version preceedingVersion = versionService.getCurrentVersion(origNodeRef);

		String versionLabel = INITIAL_VERSION;
		if (!isInitialVersion) {
			// Default the version label to the SerialVersionLabelPolicy
			SerialVersionLabelPolicy defaultVersionLabelPolicy = new SerialVersionLabelPolicy();
			versionLabel = defaultVersionLabelPolicy.calculateVersionLabel(classRef, preceedingVersion, versionProperties);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("new versionLabel: " + versionLabel + " - preceedingVersion: " + preceedingVersion);
		}

		return versionLabel;
	}

	private List<ChildAssociationRef> getVersionAssocs(NodeRef entityNodeRef) {
		NodeRef versionHistoryNodeRef = getVersionHistoryNodeRef(entityNodeRef);
		return versionHistoryNodeRef != null ? getVersionAssocs(versionHistoryNodeRef, false) : new ArrayList<ChildAssociationRef>();
	}

	private NodeRef getEntityVersion(List<ChildAssociationRef> versionAssocs, Version version) {

		for (ChildAssociationRef versionAssoc : versionAssocs) {

			NodeRef versionNodeRef = versionAssoc.getChildRef();
			String entityVersionLabel = (String) nodeService.getProperty(versionNodeRef, BeCPGModel.PROP_VERSION_LABEL);
			logger.debug("versionLabel: " + version.getVersionLabel() + " - entityVersionLabel: " + entityVersionLabel);

			if (version.getVersionLabel().equals(entityVersionLabel)) {
				return versionNodeRef;
			}
		}

		logger.error("Failed to find entity version. version: " + version.getFrozenStateNodeRef() + " versionLabel: " + version.getVersionLabel());
		return null;
	}

	/**
	 * Creates a new version history node, applying the root version aspect is
	 * required.
	 * 
	 * @param nodeRef
	 *            the node ref
	 * @return the version history node reference
	 */
	private NodeRef createVersionHistory(NodeRef entitiesHistoryFolder, NodeRef nodeRef) {
		StopWatch watch = new StopWatch();
		if (logger.isDebugEnabled()) {
			watch.start();
		}

		Map<QName, Serializable> props = new HashMap<>();
		props.put(ContentModel.PROP_NAME, nodeRef.getId());

		ChildAssociationRef childAssocRef = nodeService.createNode(entitiesHistoryFolder, ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, nodeRef.getId()), ContentModel.TYPE_FOLDER, props);

		if (logger.isTraceEnabled()) {

			watch.stop();
			logger.trace("created version history nodeRef: " + childAssocRef.getChildRef() + " for " + nodeRef + " in " + watch.getTotalTimeSeconds() + " s");
		}

		return childAssocRef.getChildRef();

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

		return nodeService.getChildAssocs(versionHistoryRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL, preLoad);
	}

	/**
	 * Gets the authenticated users node reference
	 * 
	 * @return the users node reference
	 */
	private String getUserName() {
		String un = this.authenticationService.getCurrentUserName();
		if (un != null) {
			return un;
		} else {
			throw new CheckOutCheckInServiceException(MSG_ERR_NOT_AUTHENTICATED);
		}
	}

}
