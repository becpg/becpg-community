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
import org.alfresco.repo.rule.RuntimeRuleService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.repo.version.common.VersionImpl;
import org.alfresco.repo.version.common.versionlabel.SerialVersionLabelPolicy;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.coci.CheckOutCheckInServiceException;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.cache.BeCPGCacheDataProviderCallBack;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * Store the entity version history in the SpacesStore otherwise we cannot use
 * lucene query and datalists don't work so we cannot get them.
 * 
 * @author querephi
 */
/**
 * 
 * checkOut - node is CP by versionService - create 1rst version by coping
 * oldNode 1.0 - mv file and datalist and variant - manual modification checkIn
 * - copy working copy to history 1.1 - mv file and datalist to oldNode -
 * versionService merge oldNode
 * 
 */
@Service("entityVersionService")
public class EntityVersionServiceImpl implements EntityVersionService {

	private static final QName QNAME_ENTITIES_HISTORY = QName.createQName(BeCPGModel.BECPG_URI, RepoConsts.ENTITIES_HISTORY_NAME);

	private static final String KEY_ENTITIES_HISTORY = "EntitiesHistory";
	private static final String MSG_ERR_NOT_AUTHENTICATED = "coci_service.err_not_authenticated";
	private static final String MSG_INITIAL_VERSION = "create_version.initial_version";
	private static final String MSG_WORKING_COPY_LABEL = "coci_service.working_copy_label";
	private static final String MSG_ERR_ALREADY_WORKING_COPY = "coci_service.err_workingcopy_checkout";
	private static final String MSG_ALREADY_CHECKEDOUT = "coci_service.err_already_checkedout";

	private static final String EXTENSION_CHARACTER = ".";

	private final static Log logger = LogFactory.getLog(EntityVersionServiceImpl.class);

	@Autowired
	private NodeService nodeService;

	@Autowired
	private CopyService copyService;

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

	@Autowired
	private LockService lockService;

	@Autowired
	private RepoService repoService;
	
	@Autowired
	private CheckOutCheckInService checkOutCheckInService;
	
	@Autowired
	private EntityActivityService entityActivityService;

	@Autowired
	@Qualifier("ruleService")
	private RuntimeRuleService ruleService;

	@Autowired(required = false)
	private EntityVersionPlugin[] entityVersionPlugins;

	@Override
	public NodeRef createVersionAndCheckin(final NodeRef origNodeRef, final NodeRef workingCopyNodeRef, Map<String, Serializable> versionProperties) {
		return internalCreateVersionAndCheckin(origNodeRef, workingCopyNodeRef, versionProperties, false);
	}

	@Override
	public NodeRef doCheckOut(final NodeRef origNodeRef, final NodeRef workingCopyNodeRef) {

		logger.debug("checkOutDataListAndFiles");
		// Create initialVersion
		createInitialVersion(origNodeRef);

		// Copy entity datalists (rights are checked by copyService during
		// recursiveCopy)
		AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
			@Override
			public Void doWork() throws Exception {

				try {
					policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
					policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);
					entityListDAO.copyDataLists(origNodeRef, workingCopyNodeRef, true);
					entityService.moveFiles(origNodeRef, workingCopyNodeRef);
				} finally {
					policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);
					policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
				}

				return null;

			}
		});

		// Set contributor permission for user to edit datalists
		String userName = getUserName();
		permissionService.setPermission(workingCopyNodeRef, userName, PermissionService.CONTRIBUTOR, true);

		if (entityVersionPlugins != null) {
			for (EntityVersionPlugin entityVersionPlugin : entityVersionPlugins) {
				entityVersionPlugin.doAfterCheckout(origNodeRef, workingCopyNodeRef);
			}
		}

		return workingCopyNodeRef;
	}

	@Override
	public void cancelCheckOut(final NodeRef origNodeRef, final NodeRef workingCopyNodeRef) {

		AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<NodeRef>() {
			@Override
			public NodeRef doWork() throws Exception {

				// move files
				try {
					policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
					entityService.moveFiles(workingCopyNodeRef, origNodeRef);
				} finally {
					policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
				}

				return null;

			}
		});

		// Delete initialversion
		if (versionService.getVersionHistory(origNodeRef) == null || versionService.getVersionHistory(origNodeRef).getAllVersions().size() == 1) {
			logger.debug("Deleting initial version");
			deleteVersionHistory(origNodeRef);
		}

	}

	@Override
	public void afterCancelCheckOut(NodeRef entityNodeRef) {
		if (versionService.getVersionHistory(entityNodeRef) == null || versionService.getVersionHistory(entityNodeRef).getAllVersions().size() == 1) {
			nodeService.removeAspect(entityNodeRef, ContentModel.ASPECT_VERSIONABLE);
		}

	}

	private NodeRef internalCreateVersionAndCheckin(final NodeRef origNodeRef, final NodeRef workingCopyNodeRef,
			Map<String, Serializable> versionProperties, boolean createAlfrescoVersion) {
		StopWatch watch = new StopWatch();

		if (logger.isDebugEnabled()) {
			watch.start();
			logger.debug("createEntityVersion: " + origNodeRef + " versionProperties: " + versionProperties);
		}

		try {
			NodeRef versionHistoryRef = getVersionHistoryNodeRef(origNodeRef);
			boolean isInitialVersion = versionHistoryRef == null ? true : false;

			if (!isInitialVersion && entityVersionPlugins != null && !createAlfrescoVersion) {
				for (EntityVersionPlugin entityVersionPlugin : entityVersionPlugins) {
					entityVersionPlugin.doBeforeCheckin(origNodeRef, workingCopyNodeRef);
				}
			}

			if (versionHistoryRef == null) {
				versionHistoryRef = createVersionHistory(getEntitiesHistoryFolder(), origNodeRef);
			}
			final NodeRef finalVersionHistoryRef = versionHistoryRef;
			NodeRef versionNodeRef;

			// Rights are checked by copyService during recursiveCopy
			versionNodeRef = AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<NodeRef>() {
				@Override
				public NodeRef doWork() throws Exception {

					try {
						policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);

						// version is a copy of working copy or orig for 1st
						// version
						NodeRef nodeToVersionNodeRef = workingCopyNodeRef != null ? workingCopyNodeRef : origNodeRef;

						// Recursive copy
						NodeRef versionNodeRef = copyService.copy(nodeToVersionNodeRef, finalVersionHistoryRef, ContentModel.ASSOC_CONTAINS,
								ContentModel.ASSOC_CHILDREN, true);

						if (workingCopyNodeRef != null) {
							((RuleService) ruleService).disableRules(workingCopyNodeRef);

							// remove assoc (copy used to checkin doesn't do it)
							removeRemovedAssociation(workingCopyNodeRef, origNodeRef);

							// Move workingCopyNodeRef DataList to origNodeRef
							entityService.deleteDataLists(origNodeRef, true);
							entityListDAO.moveDataLists(workingCopyNodeRef, origNodeRef);
							// Move files to origNodeRef
							entityService.deleteFiles(origNodeRef, true);
							// Remove rules
							ChildAssociationRef ruleChildAssocRef = ruleService.getSavedRuleFolderAssoc(origNodeRef);
							if (ruleChildAssocRef != null) {
								if (ruleChildAssocRef.isPrimary()) {
									logger.debug("remove primary rule of entity " + origNodeRef);
									nodeService.deleteNode(ruleChildAssocRef.getChildRef());
								} else {
									logger.debug("remove secondary rule of entity " + origNodeRef);
									nodeService.removeSecondaryChildAssociation(ruleChildAssocRef);
								}
							}
							entityService.moveFiles(workingCopyNodeRef, origNodeRef);
							// delete files that are not moved (ie: Documents)
							// otherwise
							// checkin copy them and fails since they already
							// exits
							entityService.deleteFiles(workingCopyNodeRef, true);
						}

						return versionNodeRef;

					} finally {
						policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
					}

				}
			});

			// Map<QName, Serializable> versionProperties =
			// nodeService.getProperties(versionNodeRef);
			String versionLabel = getVersionLabel(origNodeRef, versionProperties, isInitialVersion, createAlfrescoVersion);

			String name = nodeService.getProperty(origNodeRef, ContentModel.PROP_NAME) + RepoConsts.VERSION_NAME_DELIMITER + versionLabel;
			Map<QName, Serializable> aspectProperties = new HashMap<>(2);
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

		for (AssociationRef targetAssocRef : targetAssocRefs) {

			if (!ContentModel.ASSOC_WORKING_COPY_LINK.equals(targetAssocRef.getTypeQName())) {

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

	@Override
	public void createInitialVersion(NodeRef entityNodeRef) {
		if (getVersionHistoryNodeRef(entityNodeRef) == null) {
			// Create the initial-version
			Map<String, Serializable> versionProperties = new HashMap<>(1);
			versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);

			if (logger.isDebugEnabled()) {
				logger.debug("Create initial version : " + I18NUtil.getMessage(MSG_INITIAL_VERSION));
			}

			versionProperties.put(Version.PROP_DESCRIPTION, I18NUtil.getMessage(MSG_INITIAL_VERSION));

			if (nodeService.hasAspect(entityNodeRef, ContentModel.ASPECT_VERSIONABLE)) {
				createVersionAndCheckin(entityNodeRef, null, versionProperties);
			} else {
				Map<QName, Serializable> aspectProperties = new HashMap<>();
				aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
				nodeService.addAspect(entityNodeRef, ContentModel.ASPECT_VERSIONABLE, aspectProperties);
				createVersion(entityNodeRef, versionProperties);
			}

		}
	}

	@Override
	public NodeRef createVersion(final NodeRef origNodeRef, Map<String, Serializable> versionProperties) {
		return internalCreateVersionAndCheckin(origNodeRef, null, versionProperties, true);

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

		return beCPGCacheService.getFromCache(EntityVersionService.class.getName(), KEY_ENTITIES_HISTORY,
				new BeCPGCacheDataProviderCallBack<NodeRef>() {

					@Override
					public NodeRef getData() {

						NodeRef entitiesHistoryNodeRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(
								nodeService.getRootNode(RepoConsts.SPACES_STORE), RepoConsts.ENTITIES_HISTORY_XPATH);
						try {
							if (entitiesHistoryNodeRef == null) {

								// create folder
								final NodeRef storeNodeRef = nodeService.getRootNode(RepoConsts.SPACES_STORE);

								return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<NodeRef>() {
									@Override
									public NodeRef doWork() throws Exception {
										HashMap<QName, Serializable> props = new HashMap<>();
										props.put(ContentModel.PROP_NAME, RepoConsts.ENTITIES_HISTORY_NAME);
										NodeRef n = nodeService.createNode(storeNodeRef, ContentModel.ASSOC_CHILDREN, QNAME_ENTITIES_HISTORY,
												ContentModel.TYPE_FOLDER, props).getChildRef();

										logger.debug("create folder 'EntitiesHistory' " + n + " - " + nodeService.exists(n));

										return n;
									}
								});
							}
						} catch (Exception e) {
							if (e instanceof ConcurrencyFailureException) {
								throw (ConcurrencyFailureException) e;
							}
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
			try {
				policyBehaviourFilter.disableBehaviour();
				nodeService.addAspect(versionHistoryRef, ContentModel.ASPECT_TEMPORARY, null);
				nodeService.deleteNode(versionHistoryRef);
			} finally {
				policyBehaviourFilter.enableBehaviour();
			}
		}
	}
	
	
	@Override
	public void deleteEntityVersion(Version version) {
		NodeRef entityVersion = getEntityVersion(getVersionAssocs(version.getVersionedNodeRef()), version);
		if (entityVersion != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("delete entityVersion " + entityVersion);
			}
			nodeService.addAspect(entityVersion, ContentModel.ASPECT_TEMPORARY, null);
			nodeService.deleteNode(entityVersion);
		}
	}

	@Override
	public NodeRef getEntityVersion(Version version) {
		return getEntityVersion(getVersionAssocs(version.getVersionedNodeRef()), version);
	}

	@Override
	public List<EntityVersion> getAllVersions(NodeRef entityNodeRef) {

		List<EntityVersion> entityVersions = new ArrayList<>();
		if( !nodeService.hasAspect(entityNodeRef, ContentModel.ASPECT_WORKING_COPY)){
			VersionHistory versionHistory = versionService.getVersionHistory(entityNodeRef);
	
			if (versionHistory != null) {
				List<ChildAssociationRef> versionAssocs = getVersionAssocs(entityNodeRef);
	
				NodeRef branchFromNodeRef = getBranchFromNodeRef(entityNodeRef);
				for (Version version : versionHistory.getAllVersions()) {
					NodeRef entityVersionNodeRef = getEntityVersion(versionAssocs, version);
					if (entityVersionNodeRef != null) {
						EntityVersion entityVersion = new EntityVersion(version, entityNodeRef, entityVersionNodeRef, branchFromNodeRef);
						if (RepoConsts.INITIAL_VERSION.equals(version.getVersionLabel())) {
							entityVersion.setCreatedDate((Date) nodeService.getProperty(entityNodeRef, ContentModel.PROP_CREATED));
						}
	
						entityVersions.add(entityVersion);
					}
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
		List<NodeRef> versionRefs = new ArrayList<>();

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
				Map<String, Serializable> propsMap = new HashMap<>();
				propsMap.put(Version2Model.PROP_FROZEN_MODIFIED, nodeService.getProperty(branchNodeRef, ContentModel.PROP_CREATED));
				propsMap.put(Version2Model.PROP_FROZEN_MODIFIER, nodeService.getProperty(branchNodeRef, ContentModel.PROP_CREATOR));
				propsMap.put(VersionBaseModel.PROP_VERSION_LABEL, RepoConsts.INITIAL_VERSION);
				propsMap.put(Version.PROP_DESCRIPTION, nodeService.getProperty(branchNodeRef, ContentModel.PROP_DESCRIPTION));

				EntityVersion initialVersion = new EntityVersion(new VersionImpl(propsMap, branchNodeRef), branchNodeRef, branchNodeRef,
						getBranchFromNodeRef(branchNodeRef));
				ret.add(initialVersion);
			}
		}

		Collections.sort(ret, new Comparator<EntityVersion>() {

			@Override
			public int compare(EntityVersion o1, EntityVersion o2) {
				Date d1 = o1.getFrozenModifiedDate();
				Date d2 = o2.getFrozenModifiedDate();
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
		NodeRef tmp;

		do {
			tmp = associationService.getTargetAssoc(primaryParentNodeRef, BeCPGModel.ASSOC_BRANCH_FROM_ENTITY);
			if (tmp != null) {
				primaryParentNodeRef = tmp;
			}
		} while (tmp != null);

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

	private String getVersionLabel(NodeRef origNodeRef, Map<String, Serializable> versionProperties, boolean isInitialVersion,
			boolean createNewVersion) {
		String versionLabel = RepoConsts.INITIAL_VERSION;
		if (createNewVersion) {
			Version newVersion = versionService.createVersion(origNodeRef, versionProperties);
			versionLabel = newVersion.getVersionLabel();
		} else {

			QName classRef = nodeService.getType(origNodeRef);
			Version preceedingVersion = versionService.getCurrentVersion(origNodeRef);

			if (!isInitialVersion) {

				if (preceedingVersion == null) {

					Map<String, Serializable> propsMap = new HashMap<>();
					propsMap.put(VersionBaseModel.PROP_VERSION_LABEL, RepoConsts.INITIAL_VERSION);
					propsMap.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);

					preceedingVersion = new VersionImpl(propsMap, origNodeRef);
				}
				// Default the version label to the SerialVersionLabelPolicy
				SerialVersionLabelPolicy defaultVersionLabelPolicy = new SerialVersionLabelPolicy();
				versionLabel = defaultVersionLabelPolicy.calculateVersionLabel(classRef, preceedingVersion, versionProperties);

			}

			if (logger.isDebugEnabled()) {
				logger.debug("new versionLabel: " + versionLabel + " - preceedingVersion: " + preceedingVersion);
			}
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
			logger.trace("created version history nodeRef: " + childAssocRef.getChildRef() + " for " + nodeRef + " in " + watch.getTotalTimeSeconds()
					+ " s");
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

	@Override
	public NodeRef mergeBranch(NodeRef branchNodeRef, NodeRef branchToNodeRef, VersionType versionType, String description){
	
		if (branchToNodeRef == null) {
		  branchToNodeRef = associationService.getTargetAssoc(branchNodeRef, BeCPGModel.ASSOC_AUTO_MERGE_TO);
		}
		if(branchToNodeRef != null){
			
			prepareBranchBeforeMerge(branchNodeRef, branchToNodeRef);
	
			Map<String, Serializable> properties = new HashMap<>();
			properties.put(VersionBaseModel.PROP_VERSION_TYPE, versionType);
			properties.put(Version.PROP_DESCRIPTION, description);
			
			entityActivityService.postMergeBranchActivity(branchNodeRef, branchToNodeRef, versionType, description );
			
			return checkOutCheckInService.checkin(branchNodeRef, properties);
		}
		return null;
	}
	
	
	private void prepareBranchBeforeMerge(NodeRef branchNodeRef, NodeRef branchToNodeRef) {
		if (nodeService.hasAspect(branchToNodeRef, ContentModel.ASPECT_CHECKED_OUT)) {
			throw new CheckOutCheckInServiceException(MSG_ALREADY_CHECKEDOUT);
		}

		// Make sure we are not checking out a working copy node
		if (nodeService.hasAspect(branchNodeRef, ContentModel.ASPECT_WORKING_COPY)) {
			throw new CheckOutCheckInServiceException(MSG_ERR_ALREADY_WORKING_COPY);
		}

		// Create initialVersion if needed
		createInitialVersion(branchToNodeRef);

		// It is not enough to check LockUtils.isLockedOrReadOnly in case when
		// the same user does offline and online edit (for instance in two open
		// browsers). In this case we get
		// set ContentModel.ASPECT_LOCKABLE and LockType.WRITE_LOCK. So, here we
		// have to check following
		LockStatus lockStatus = lockService.getLockStatus(branchToNodeRef);
		if (lockStatus != LockStatus.NO_LOCK && lockStatus != LockStatus.LOCK_EXPIRED) {
			throw new NodeLockedException(branchToNodeRef);
		}

		policyBehaviourFilter.disableBehaviour(branchNodeRef, ContentModel.ASPECT_AUDITABLE);
		try {

			// Apply the lock aspect if required
			if (!nodeService.hasAspect(branchToNodeRef, ContentModel.ASPECT_LOCKABLE)) {
				nodeService.addAspect(branchToNodeRef, ContentModel.ASPECT_LOCKABLE, null);
			}

			// Get the user
			final String userName = getUserName();

			((RuleService) ruleService).disableRuleType(RuleType.UPDATE);
			try {

				String copyName = (String) this.nodeService.getProperty(branchToNodeRef, ContentModel.PROP_NAME);
				String workingCopyLabel = I18NUtil.getMessage(MSG_WORKING_COPY_LABEL);
				copyName = createWorkingCopyName(copyName, workingCopyLabel);

				// Apply the working copy aspect to the working copy
				Map<QName, Serializable> workingCopyProperties = new HashMap<>(1);
				workingCopyProperties.put(ContentModel.PROP_WORKING_COPY_OWNER, userName);
				workingCopyProperties.put(ContentModel.PROP_WORKING_COPY_LABEL, workingCopyLabel);
				nodeService.addAspect(branchNodeRef, ContentModel.ASPECT_WORKING_COPY, workingCopyProperties);
				nodeService.addAspect(branchNodeRef, ContentModel.ASPECT_LOCKABLE, null);
				nodeService.addAspect(branchToNodeRef, ContentModel.ASPECT_CHECKED_OUT, null);
				nodeService.createAssociation(branchToNodeRef, branchNodeRef, ContentModel.ASSOC_WORKING_COPY_LINK);

				// Set beCPG CODE
				nodeService.setProperty(branchNodeRef, BeCPGModel.PROP_CODE, nodeService.getProperty(branchToNodeRef, BeCPGModel.PROP_CODE));
				nodeService.setProperty(branchNodeRef, ContentModel.PROP_NAME, copyName);

				// Remove branchForm as it's merge
				nodeService.removeAspect(branchNodeRef, BeCPGModel.ASPECT_ENTITY_BRANCH);

				// Deattach other branches
				List<NodeRef> sources = associationService.getSourcesAssocs(branchNodeRef, BeCPGModel.ASSOC_BRANCH_FROM_ENTITY);
				for (NodeRef sourceNodeRef : sources) {
					policyBehaviourFilter.disableBehaviour(sourceNodeRef, ContentModel.ASPECT_AUDITABLE);
					try {
						nodeService.removeAspect(sourceNodeRef, BeCPGModel.ASPECT_ENTITY_BRANCH);
					} finally {
						policyBehaviourFilter.enableBehaviour(sourceNodeRef, ContentModel.ASPECT_AUDITABLE);
					}
				}
				
				
				//Update all association refering to this branch to point to branchToNodeRef
				List<AssociationRef> assocRefs = nodeService.getSourceAssocs(branchNodeRef, null);
				
				for (AssociationRef assocRef : assocRefs) {
					policyBehaviourFilter.disableBehaviour(assocRef.getSourceRef(), ContentModel.ASPECT_AUDITABLE);
					try {
						nodeService.removeAssociation(assocRef.getSourceRef(), assocRef.getTargetRef(), assocRef.getTypeQName());
						nodeService.createAssociation(assocRef.getSourceRef(), branchToNodeRef, assocRef.getTypeQName());
					} finally {
						policyBehaviourFilter.enableBehaviour(assocRef.getSourceRef(), ContentModel.ASPECT_AUDITABLE);
					}
				}
				

			} finally {
				((RuleService) ruleService).enableRuleType(RuleType.UPDATE);
			}

			// Lock the original node
			lockService.lock(branchToNodeRef, LockType.READ_ONLY_LOCK);

		} finally {
			policyBehaviourFilter.enableBehaviour(branchToNodeRef, ContentModel.ASPECT_AUDITABLE);
		}

	}

	@Override
	public NodeRef createBranch(NodeRef entityNodeRef, NodeRef parentRef) {

		String newEntityName = repoService.getAvailableName(parentRef, (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME));
		NodeRef branchNodeRef = entityService.createOrCopyFrom(entityNodeRef, parentRef, nodeService.getType(entityNodeRef), newEntityName);
		if (nodeService.hasAspect(entityNodeRef, ContentModel.ASPECT_VERSIONABLE)) {
			nodeService.setProperty(branchNodeRef, BeCPGModel.PROP_BRANCH_FROM_VERSION_LABEL,
					nodeService.getProperty(entityNodeRef, ContentModel.PROP_VERSION_LABEL));
		} else {
			nodeService.setProperty(branchNodeRef, BeCPGModel.PROP_BRANCH_FROM_VERSION_LABEL, RepoConsts.INITIAL_VERSION);
		}
		nodeService.setAssociations(branchNodeRef, BeCPGModel.ASSOC_BRANCH_FROM_ENTITY, Collections.singletonList(entityNodeRef));
		return branchNodeRef;
	}

	/**
	 * Create a working copy name using the given fileName and workingCopyLabel.
	 * The label will be inserted before the file extension (if present), or
	 * else appended to the name (in either case a space is prepended to the
	 * workingCopyLabel).
	 * <p>
	 * Examples, where workingCopyLabel is "wc":
	 * <p>
	 * "Myfile.txt" becomes "Myfile wc.txt", "Myfile" becomes "Myfile wc".
	 * <p>
	 * In the event that fileName is empty or null, the workingCopyLabel is used
	 * for the new working copy name
	 * <p>
	 * Example: "" becomes "wc".
	 * 
	 * @param name
	 * @param workingCopyLabel
	 * @return
	 */
	public static String createWorkingCopyName(String name, final String workingCopyLabel) {
		if (workingCopyLabel != null && workingCopyLabel.length() != 0) {
			if (name != null && name.length() != 0) {
				int index = name.lastIndexOf(EXTENSION_CHARACTER);
				if (index > 0) {
					// Insert the working copy label before the file extension
					name = name.substring(0, index) + " " + workingCopyLabel + name.substring(index);
				} else {
					// Simply append the working copy label onto the end of the
					// existing name
					name = name + " " + workingCopyLabel;
				}
			} else {
				name = workingCopyLabel;
			}
		} else {
			throw new IllegalArgumentException("workingCopyLabel is null or empty");
		}

		return name;
	}

}
