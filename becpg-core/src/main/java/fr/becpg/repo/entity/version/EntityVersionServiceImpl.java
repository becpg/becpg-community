package fr.becpg.repo.entity.version;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.repo.version.common.VersionImpl;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.cache.BeCPGCacheDataProviderCallBack;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * Store the entity version history in the SpacesStore otherwise we cannot use
 * lucene query and datalists don't work so we cannot get them.
 * 
 * @author querephi
 */
@Service("entityVersionService")
public class EntityVersionServiceImpl implements EntityVersionService {

	private static final String ENTITIES_HISTORY_NAME = "entitiesHistory";

	private static final QName QNAME_ENTITIES_HISTORY = QName.createQName(BeCPGModel.BECPG_URI, ENTITIES_HISTORY_NAME);

	private static final String ENTITIES_HISTORY_XPATH = "/bcpg:entitiesHistory";
	private static final String KEY_ENTITIES_HISTORY = "EntitiesHistory";

	private static final String MSG_ERR_NOT_AUTHENTICATED = "coci_service.err_not_authenticated";

	private static Log logger = LogFactory.getLog(EntityVersionServiceImpl.class);

	@Autowired
	private NodeService nodeService;

	@Autowired
	private CopyService copyService;

	@Autowired
	private BeCPGSearchService beCPGSearchService;

	@Autowired
	private BeCPGCacheService beCPGCacheService;

	@Autowired
	private EntityListDAO entityListDAO;

	@Autowired
	private EntityService entityService;

	@Autowired
	private VersionService versionService;

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private BehaviourFilter policyBehaviourFilter;

	@Autowired
	private PermissionService permissionService;
	
	@Autowired
	private AssociationService associationService;

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

			String name = nodeService.getProperty(origNodeRef, ContentModel.PROP_NAME) + RepoConsts.VERSION_NAME_DELIMITER + versionLabel;
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

			if (!ContentModel.ASSOC_WORKING_COPY_LINK.equals(targetAssocRef.getTypeQName())) {

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

			NodeRef branchFromNodeRef = getBranchFromNodeRef(entityNodeRef);
			for (Version version : versionHistory.getAllVersions()) {
				NodeRef entityVersionNodeRef = getEntityVersion(versionAssocs, version);
				if (entityVersionNodeRef != null) {
					EntityVersion entityVersion = new EntityVersion(version,entityNodeRef,  entityVersionNodeRef,branchFromNodeRef);
					if(RepoConsts.INITIAL_VERSION.equals(version.getVersionLabel())) {
						entityVersion.setCreatedDate((Date)nodeService.getProperty(entityNodeRef, ContentModel.PROP_CREATED));
					}
					
					entityVersions.add(entityVersion);
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

	@Override
	public List<EntityVersion> getAllVersionAndBranches(NodeRef entityNodeRef) {
		List<EntityVersion> ret = new LinkedList<>();
		for (NodeRef branchNodeRef : getAllVersionBranches(entityNodeRef)) {
			List<EntityVersion> entityVersions = getAllVersions(branchNodeRef);
			
			if (!entityVersions.isEmpty()) {
				for (EntityVersion entityVersion : getAllVersions(branchNodeRef)) {
					ret.add(entityVersion);
				}
			} else {
				Map<String, Serializable> propsMap = new HashMap<String, Serializable>();

				propsMap.put(Version2Model.PROP_FROZEN_MODIFIED, nodeService.getProperty(branchNodeRef, ContentModel.PROP_CREATED));
				propsMap.put(Version2Model.PROP_FROZEN_MODIFIER, nodeService.getProperty(branchNodeRef, ContentModel.PROP_CREATOR));
				propsMap.put(VersionBaseModel.PROP_VERSION_LABEL, RepoConsts.INITIAL_VERSION);

				EntityVersion initialVersion = new EntityVersion(new VersionImpl(propsMap, branchNodeRef),branchNodeRef,  branchNodeRef, getBranchFromNodeRef(branchNodeRef));
				ret.add(initialVersion);
			}
		}
		
		Collections.sort(ret, new Comparator<EntityVersion>() {

			@Override
			public int compare(EntityVersion o1, EntityVersion o2) {
				Date d1 = (Date) o1.getFrozenModifiedDate();
				Date d2 = (Date) o2.getFrozenModifiedDate();
				return (d1 == d2) ? 0 : d2 == null ? -1 : d2.compareTo(d1);
			}

		});

		return ret;
	}

	
	private NodeRef getBranchFromNodeRef(NodeRef branchNodeRef) {
		return associationService.getTargetAssoc(branchNodeRef, BeCPGModel.ASSOC_BRANCH_FROM_ENTITY);
	}

	@Override
	public List<NodeRef> getAllVersionBranches(NodeRef entityNodeRef) {

		NodeRef primaryParentNodeRef = entityNodeRef;

		// Look for primary parent
		NodeRef tmp = null;

		do {
			tmp = associationService.getTargetAssoc(primaryParentNodeRef, BeCPGModel.ASSOC_BRANCH_FROM_ENTITY);
			if (tmp != null) {
				primaryParentNodeRef = tmp;
			}
		} while (tmp != null );

		List<NodeRef> ret = new LinkedList<>();
		ret.add(primaryParentNodeRef);
		ret.addAll(getAllChildVersionBranches(primaryParentNodeRef));

		Collections.sort(ret, new Comparator<NodeRef>() {

			@Override
			public int compare(NodeRef o1, NodeRef o2) {
				Date d1 = (Date) nodeService.getProperty(o1, ContentModel.PROP_CREATED);
				Date d2 = (Date) nodeService.getProperty(o2, ContentModel.PROP_CREATED);
				return (d1 == d2) ? 0 : d2 == null ? -1 : d2.compareTo(d1);
			}

		});

		return ret;
	}

	/**
	 * @param tmpNodeRef
	 * @return
	 */
	private List<NodeRef> getAllChildVersionBranches(NodeRef entityNodeRef) {

		List<NodeRef> ret = new LinkedList<>();
		// Look for childs
		for (AssociationRef associationRef : nodeService.getSourceAssocs(entityNodeRef, BeCPGModel.ASSOC_BRANCH_FROM_ENTITY)) {
			if (!nodeService.hasAspect(associationRef.getSourceRef(), BeCPGModel.ASPECT_COMPOSITE_VERSION)) {
				NodeRef tmpNodeRef = associationRef.getSourceRef();
				if (!ret.contains(tmpNodeRef)) {
					ret.add(tmpNodeRef);
					ret.addAll(getAllChildVersionBranches(tmpNodeRef));
				}
			}
		}

		return ret;
	}

	private String getVersionLabel(NodeRef origNodeRef, Map<String, Serializable> versionProperties, boolean isInitialVersion) {

		QName classRef = nodeService.getType(origNodeRef);
		Version preceedingVersion = versionService.getCurrentVersion(origNodeRef);

		String versionLabel = RepoConsts.INITIAL_VERSION;
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
