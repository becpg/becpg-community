package fr.becpg.repo.entity.version;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.model.ImapModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.forum.CommentService;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.rule.RuntimeRuleService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.repo.version.common.VersionImpl;
import org.alfresco.repo.version.common.VersionUtil;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.AssociationExistsException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.cmr.view.ExporterCrawlerParameters;
import org.alfresco.service.cmr.view.ExporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.BeCPGModel.EntityFormat;
import fr.becpg.model.BeCPGPermissions;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.audit.helper.StopWatchSupport;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.batch.BatchPriority;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.batch.EntityListBatchProcessWorkProvider;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityFormatService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.entity.remote.RemoteParams;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.jscript.BeCPGStateHelper;
import fr.becpg.repo.jscript.BeCPGStateHelper.ActionStateContext;
import fr.becpg.repo.report.entity.EntityReportService;
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
 * @version $Id: $Id
 */
@Service("entityVersionService")
public class EntityVersionServiceImpl implements EntityVersionService {

	private static final QName QNAME_ENTITIES_HISTORY = QName.createQName(BeCPGModel.BECPG_URI, RepoConsts.ENTITIES_HISTORY_NAME);

	private static final String KEY_ENTITIES_HISTORY = "EntitiesHistory";
	private static final String MSG_INITIAL_VERSION = "create_version.initial_version";
	private static final Log logger = LogFactory.getLog(EntityVersionServiceImpl.class);

	@Autowired
	private NodeService nodeService;

	@Autowired
	@Qualifier("mtAwareNodeService")
	private NodeService dbNodeService;

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
	private BehaviourFilter policyBehaviourFilter;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private RepoService repoService;

	@Autowired
	private EntityActivityService entityActivityService;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private CommentService commentService;

	@Autowired
	private ContentService contentService;

	@Autowired
	@Qualifier("exporterComponent")
	private ExporterService exporterService;

	@Autowired
	@Qualifier("ruleService")
	private RuntimeRuleService ruleService;

	@Autowired(required = false)
	private EntityVersionPlugin[] entityVersionPlugins;

	@Autowired
	private EntityFormatService entityFormatService;

	@Autowired
	private LockService lockService;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Autowired
	private EntityReportService entityReportService;

	@Autowired
	private BatchQueueService batchQueueService;
	
	@Autowired
	private IntegrityChecker integrityChecker;
	
	@Autowired
	private NamespaceService namespaceService;

	/** {@inheritDoc} */
	@Override
	public void cancelCheckOut(final NodeRef origNodeRef, final NodeRef workingCopyNodeRef) {

		AuthenticationUtil.runAsSystem(() -> {

			// move files
			try {
				policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
				entityService.moveFiles(workingCopyNodeRef, origNodeRef);
			} finally {
				policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
			}

			return null;

		});

		// Delete initialversion
		if ((versionService.getVersionHistory(origNodeRef) == null) || (versionService.getVersionHistory(origNodeRef).getAllVersions().size() == 1)) {
			logger.debug("Deleting initial version");
			deleteVersionHistory(origNodeRef);
		}

	}

	/** {@inheritDoc} */
	@Override
	public void afterCancelCheckOut(NodeRef entityNodeRef) {
		if ((versionService.getVersionHistory(entityNodeRef) == null)
				|| (versionService.getVersionHistory(entityNodeRef).getAllVersions().size() == 1)) {
			nodeService.removeAspect(entityNodeRef, ContentModel.ASPECT_VERSIONABLE);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void updateLastVersionLabel(final NodeRef entityNodeRef, final String versionLabel) {

		// Create first version if needed
		createInitialVersion(entityNodeRef);

		Version currentVersion = versionService.getCurrentVersion(entityNodeRef);
		if (currentVersion != null) {
			NodeRef versionNodeRef = getEntityVersion(currentVersion);
			dbNodeService.setProperty(versionNodeRef, Version2Model.PROP_QNAME_VERSION_LABEL, versionLabel);

			//Old version type
			NodeRef entityVersion = getEntityVersion(currentVersion);
			if (entityVersion != null) {
				nodeService.setProperty(entityVersion, BeCPGModel.PROP_VERSION_LABEL, versionLabel);
			}

			nodeService.setProperty(entityNodeRef, ContentModel.PROP_VERSION_LABEL, versionLabel);
		}
	}

	//Old version type
	private void updateEntitiesHistory(NodeRef origNodeRef, NodeRef impactOnlyNodeRef) {
		List<AssociationRef> assocRefs = nodeService.getSourceAssocs(origNodeRef, RegexQNamePattern.MATCH_ALL);

		List<EntityVersion> versions = getAllVersions(origNodeRef);

		if ((versions != null) && (!versions.isEmpty())) {

			int index = 0;

			if ((versions.size() > 1) && (impactOnlyNodeRef != null)) {
				index = 1;
			}

			NodeRef versionNodeRef = versions.get(index).getEntityVersionNodeRef();

			if (StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.equals(versionNodeRef.getStoreRef())) {
				for (AssociationRef assocRef : assocRefs) {
					policyBehaviourFilter.disableBehaviour(assocRef.getSourceRef(), ContentModel.ASPECT_AUDITABLE);
					try {

						if ((assocRef != null) && (assocRef.getTargetRef() != null) && !assocRef.getTargetRef().equals(versionNodeRef)) {
							NodeRef versionEntityNodeRef = entityService.getEntityNodeRef(assocRef.getSourceRef(),
									nodeService.getType(assocRef.getSourceRef()));

							if ((versionEntityNodeRef != null) && nodeService.hasAspect(versionEntityNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)) {

								NodeRef entityNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, (String) nodeService
										.getProperty(nodeService.getPrimaryParent(versionEntityNodeRef).getParentRef(), ContentModel.PROP_NAME));

								if (nodeService.exists(entityNodeRef) && nodeService.exists(versionNodeRef)
										&& ((impactOnlyNodeRef == null) || impactOnlyNodeRef.equals(entityNodeRef))) {

									String entityVersionLabel = (String) nodeService.getProperty(versionEntityNodeRef, BeCPGModel.PROP_VERSION_LABEL);
									String versionLabel = (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_VERSION_LABEL);

									if ((entityVersionLabel != null) && !entityVersionLabel.equals(versionLabel)) {

										nodeService.removeAssociation(assocRef.getSourceRef(), assocRef.getTargetRef(), assocRef.getTypeQName());
										nodeService.createAssociation(assocRef.getSourceRef(), versionNodeRef, assocRef.getTypeQName());
									}
								}
							}
						}
					} catch (AssociationExistsException e) {
						logger.error("Cannot update assoc : " + assocRef);
					} finally {
						policyBehaviourFilter.enableBehaviour(assocRef.getSourceRef(), ContentModel.ASPECT_AUDITABLE);
					}
				}
			}
		}

	}

	private void removeRemovedAssociation(NodeRef sourceCopy, NodeRef targetCopy) {

		/*
		 * Extending DefaultCopyBehaviourCallback doesn't work since we must implement it for every aspect
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

	/** {@inheritDoc} */
	@Override
	public void createInitialVersion(NodeRef entityNodeRef) {
		createInitialVersion(entityNodeRef, null);
	}

	@Override
	public void createInitialVersion(NodeRef entityNodeRef, Date effectiveDate) {
		internalCreateInitialVersion(entityNodeRef, effectiveDate);
	}

	private NodeRef internalCreateInitialVersion(NodeRef entityNodeRef, Date newEffectivity) {
		if (!nodeService.hasAspect(entityNodeRef, ContentModel.ASPECT_VERSIONABLE)) {
			// Create the initial-version
			Map<String, Serializable> versionProperties = new HashMap<>(1);
			versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE, VersionType.MAJOR);

			if (logger.isDebugEnabled()) {
				logger.debug("Create initial version : " + I18NUtil.getMessage(MSG_INITIAL_VERSION));
			}

			versionProperties.put(Version.PROP_DESCRIPTION, I18NUtil.getMessage(MSG_INITIAL_VERSION));

			Map<QName, Serializable> aspectProperties = new HashMap<>();
			aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
			nodeService.addAspect(entityNodeRef, ContentModel.ASPECT_VERSIONABLE, aspectProperties);

			String manualVersionLabel = (String) nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_MANUAL_VERSION_LABEL);

			NodeRef versionNode = internalCreateVersion(entityNodeRef, versionProperties, newEffectivity, manualVersionLabel, true);

			// we need to retrieve the AUDITABLE properties because Version2ServiceImpl only freezes these properties
			nodeService.setProperty(versionNode, ContentModel.PROP_CREATED, nodeService.getProperty(entityNodeRef, ContentModel.PROP_CREATED));
			nodeService.setProperty(versionNode, ContentModel.PROP_CREATOR, nodeService.getProperty(entityNodeRef, ContentModel.PROP_CREATOR));
			nodeService.setProperty(versionNode, ContentModel.PROP_MODIFIED, nodeService.getProperty(entityNodeRef, ContentModel.PROP_MODIFIED));
			nodeService.setProperty(versionNode, ContentModel.PROP_MODIFIER, nodeService.getProperty(entityNodeRef, ContentModel.PROP_MODIFIER));
			nodeService.setProperty(versionNode, ContentModel.PROP_ACCESSED, nodeService.getProperty(entityNodeRef, ContentModel.PROP_ACCESSED));

			return versionNode;
		}

		return null;
	}

	@Override
	public void createInitialVersionWithProps(NodeRef entityNodeRef, Map<QName, Serializable> before) {

		NodeRef versionNode = internalCreateInitialVersion(entityNodeRef, null);

		if (versionNode != null) {
			String name = (String) nodeService.getProperty(versionNode, ContentModel.PROP_NAME);

			nodeService.setProperties(versionNode, before);

			nodeService.setProperty(versionNode, ContentModel.PROP_NAME, name);
			nodeService.setProperty(versionNode, Version2Model.PROP_QNAME_VERSION_LABEL, RepoConsts.INITIAL_VERSION);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * Gets a reference to the version history node for a given 'real' node.
	 */
	@Override
	public  NodeRef getVersionHistoryNodeRef(NodeRef nodeRef, boolean shouldCreate) {
		NodeRef vhNodeRef = null;
		if (nodeRef != null) {
			final NodeRef entitiesHistoryFolder = getEntitiesHistoryFolder();

			vhNodeRef = nodeService.getChildByName(entitiesHistoryFolder, ContentModel.ASSOC_CONTAINS, nodeRef.getId());

			if (vhNodeRef == null) {
				vhNodeRef = nodeService.getChildByName(entitiesHistoryFolder, ContentModel.ASSOC_CHILDREN, nodeRef.getId());
			}

			if (shouldCreate && vhNodeRef == null && AlfrescoTransactionSupport.getTransactionReadState() == TxnReadState.TXN_READ_WRITE) {
				return AuthenticationUtil.runAsSystem(() -> {
					Map<QName, Serializable> props = new HashMap<>();
					props.put(ContentModel.PROP_NAME, nodeRef.getId());

					return nodeService
							.createNode(entitiesHistoryFolder, ContentModel.ASSOC_CONTAINS,
									QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, nodeRef.getId()), ContentModel.TYPE_FOLDER, props)
							.getChildRef();

				});
			}

		}
		return vhNodeRef;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Get the entitys history folder node where we store entity versions.
	 */
	@Override
	@NonNull
	public NodeRef getEntitiesHistoryFolder() {

		return beCPGCacheService.getFromCache(EntityVersionService.class.getName(), KEY_ENTITIES_HISTORY, () -> {

			final NodeRef storeNodeRef = nodeService.getRootNode(RepoConsts.SPACES_STORE);

			NodeRef entitiesHistoryNodeRef = nodeService.getChildByName(storeNodeRef, ContentModel.ASSOC_CONTAINS, RepoConsts.ENTITIES_HISTORY_NAME);

			//Backward compatibility
			if (entitiesHistoryNodeRef == null) {
				entitiesHistoryNodeRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(storeNodeRef, RepoConsts.ENTITIES_HISTORY_XPATH);
			}

			if (entitiesHistoryNodeRef == null && AlfrescoTransactionSupport.getTransactionReadState() == TxnReadState.TXN_READ_WRITE) {
				return AuthenticationUtil.runAsSystem(() -> {
					HashMap<QName, Serializable> props = new HashMap<>();
					props.put(ContentModel.PROP_NAME, RepoConsts.ENTITIES_HISTORY_NAME);
					NodeRef n = nodeService
							.createNode(storeNodeRef, ContentModel.ASSOC_CONTAINS, QNAME_ENTITIES_HISTORY, ContentModel.TYPE_FOLDER, props)
							.getChildRef();

					logger.debug("create folder 'EntitiesHistory' " + n + " - " + nodeService.exists(n));

					return n;
				});
			}
			return entitiesHistoryNodeRef;
		}, true);
	}

	/** {@inheritDoc} */
	@Override
	public void deleteVersionHistory(NodeRef entityNodeRef) {
		VersionHistory versionHistory = versionService.getVersionHistory(entityNodeRef);
		if (versionHistory != null) {
			Collection<Version> versions = versionHistory.getAllVersions();

			for (Version version : versions) {
				NodeRef versionNode = getEntityVersion(version);

				NodeRef extractedVersion = findExtractedVersion(versionNode);

				if (extractedVersion != null) {
					nodeService.addAspect(extractedVersion, ContentModel.ASPECT_TEMPORARY, null);
					nodeService.deleteNode(extractedVersion);
				}
			}
		}
		NodeRef versionHistoryRef = getVersionHistoryNodeRef(entityNodeRef, false);
		if (versionHistoryRef != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("delete versionHistoryRef " + versionHistoryRef);
			}
			nodeService.addAspect(versionHistoryRef, ContentModel.ASPECT_TEMPORARY, null);
			nodeService.deleteNode(versionHistoryRef);
		}
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	public NodeRef getEntityVersion(Version version) {
		return VersionUtil.convertNodeRef(version.getFrozenStateNodeRef());
	}

	/** {@inheritDoc} */
	@Override
	public List<EntityVersion> getAllVersions(NodeRef entityNodeRef) {

		List<EntityVersion> entityVersions = new LinkedList<>();
		if (!nodeService.hasAspect(entityNodeRef, ContentModel.ASPECT_WORKING_COPY)
				&& !nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)) {
			VersionHistory versionHistory = versionService.getVersionHistory(entityNodeRef);

			if (versionHistory != null) {
				List<ChildAssociationRef> versionAssocs = getVersionAssocs(entityNodeRef);

				NodeRef branchFromNodeRef = getBranchFromNodeRef(entityNodeRef);

				Optional<Version> lowestVersion = versionHistory.getAllVersions().stream()
						.min(((o1, o2) -> o1.getVersionLabel().compareTo(o2.getVersionLabel())));

				for (Version version : versionHistory.getAllVersions()) {
					NodeRef entityVersionNodeRef = getEntityVersion(versionAssocs, version);
					EntityVersion entityVersion = null;
					if (entityVersionNodeRef != null && !nodeService.hasAspect(entityVersionNodeRef, ContentModel.ASPECT_TEMPORARY)) {
						entityVersion = new EntityVersion(version, entityNodeRef, entityVersionNodeRef, branchFromNodeRef);
					} else {
						entityVersion = new EntityVersion(version, entityNodeRef, getEntityVersion(version), branchFromNodeRef);
					}
					if (RepoConsts.INITIAL_VERSION.equals(version.getVersionLabel()) || lowestVersion.isPresent() && version == lowestVersion.get()) {
						entityVersion.setCreatedDate((Date) nodeService.getProperty(entityNodeRef, ContentModel.PROP_CREATED));
					}
					entityVersions.add(entityVersion);
				}
			}
		}

		return entityVersions;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Get the versions sort by date and node-ide.
	 */
	@Override
	public List<NodeRef> buildVersionHistory(NodeRef versionHistoryRef, NodeRef nodeRef) {

		List<ChildAssociationRef> versionAssocs = getVersionAssocs(versionHistoryRef, true);
		List<NodeRef> versionRefs = new LinkedList<>();

		for (ChildAssociationRef versionAssoc : versionAssocs) {

			versionRefs.add(versionAssoc.getChildRef());
		}

		// sort versions by node id
		Collections.sort(versionRefs, (v1, v2) -> {
			Date modifiedDateV1 = (Date) nodeService.getProperty(v1, ContentModel.PROP_CREATED);
			Date modifiedDateV2 = (Date) nodeService.getProperty(v2, ContentModel.PROP_CREATED);
			int result = modifiedDateV1.compareTo(modifiedDateV2);
			if (result == 0) {
				Long dbid1 = (Long) nodeService.getProperty(v1, ContentModel.PROP_NODE_DBID);
				Long dbid2 = (Long) nodeService.getProperty(v2, ContentModel.PROP_NODE_DBID);

				if ((dbid1 != null) && (dbid2 != null)) {
					result = dbid1.compareTo(dbid2);
				} else {
					result = 0;

					if (logger.isWarnEnabled()) {
						logger.warn("node-dbid property is missing for versions: " + v1.toString() + " or " + v2.toString());
					}
				}
			}
			return result;
		});

		return versionRefs;
	}

	/** {@inheritDoc} */
	@Override
	public List<EntityVersion> getAllVersionAndBranches(NodeRef entityNodeRef) {
		List<EntityVersion> ret = new LinkedList<>();
		for (NodeRef branchNodeRef : getAllVersionBranches(entityNodeRef)) {
			List<EntityVersion> entityVersions = getAllVersions(branchNodeRef);

			if (!entityVersions.isEmpty()) {
				for (EntityVersion entityVersion : entityVersions) {
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

		Collections.sort(ret, (o1, o2) -> {
			Date d1 = o1.getFrozenModifiedDate();
			Date d2 = o2.getFrozenModifiedDate();
			return (d1 == d2) ? 0 : d2 == null ? -1 : d2.compareTo(d1);
		});

		return ret;
	}

	private NodeRef getBranchFromNodeRef(NodeRef branchNodeRef) {
		return associationService.getTargetAssoc(branchNodeRef, BeCPGModel.ASSOC_BRANCH_FROM_ENTITY);
	}

	/** {@inheritDoc} */
	@Override
	public List<NodeRef> getAllVersionBranches(NodeRef entityNodeRef) {

		NodeRef primaryParentNodeRef = entityNodeRef;

		// Look for primary parent
		NodeRef tmp;

		List<NodeRef> ret = new LinkedList<>();
		if (primaryParentNodeRef != null) {
			int maxDeep = 0;
			do {
				tmp = associationService.getTargetAssoc(primaryParentNodeRef, BeCPGModel.ASSOC_BRANCH_FROM_ENTITY);

				if (tmp != null) {
					primaryParentNodeRef = tmp;
				}

				maxDeep++;
			} while (tmp != null && maxDeep < 100);

			if (maxDeep >= 100) {
				logger.error("Infinite branch cycle in : " + primaryParentNodeRef);
			}

			ret.add(primaryParentNodeRef);
			ret.addAll(getAllChildVersionBranches(primaryParentNodeRef));

			Collections.sort(ret, (o1, o2) -> {
				Date d1 = (Date) nodeService.getProperty(o1, ContentModel.PROP_CREATED);
				Date d2 = (Date) nodeService.getProperty(o2, ContentModel.PROP_CREATED);
				return (d1 == d2) ? 0 : d2 == null ? -1 : d2.compareTo(d1);
			});
		}

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
			if (!isVersion(associationRef.getSourceRef())
					&& !nodeService.hasAspect(associationRef.getSourceRef(), BeCPGModel.ASPECT_COMPOSITE_VERSION)) {
				NodeRef tmpNodeRef = associationRef.getSourceRef();
				if (!ret.contains(tmpNodeRef)) {
					ret.add(tmpNodeRef);
					if (!entityNodeRef.equals(tmpNodeRef)) {
						ret.addAll(getAllChildVersionBranches(tmpNodeRef));
					}
				}
			}
		}

		return ret;
	}

	private List<ChildAssociationRef> getVersionAssocs(NodeRef entityNodeRef) {
		NodeRef versionHistoryNodeRef = getVersionHistoryNodeRef(entityNodeRef, false);
		return versionHistoryNodeRef != null ? getVersionAssocs(versionHistoryNodeRef, false) : new ArrayList<>();
	}

	private NodeRef getEntityVersion(List<ChildAssociationRef> versionAssocs, Version version) {

		for (ChildAssociationRef versionAssoc : versionAssocs) {

			NodeRef versionNodeRef = versionAssoc.getChildRef();
			String entityVersionLabel = (String) nodeService.getProperty(versionNodeRef, BeCPGModel.PROP_VERSION_LABEL);

			if (version.getVersionLabel().equals(entityVersionLabel)) {
				logger.debug("versionNodeRef:" + versionNodeRef + " - versionLabel: " + version.getVersionLabel() + " - entityVersionLabel: "
						+ entityVersionLabel);
				return versionNodeRef;
			}
		}

		return null;
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

	/** {@inheritDoc} */
	@Override
	public NodeRef mergeBranch(NodeRef branchNodeRef, Date newEffectivity) {

		String versionType = (String) nodeService.getProperty(branchNodeRef, BeCPGModel.PROP_AUTO_MERGE_VERSIONTYPE);
		if (versionType == null) {
			versionType = VersionType.MINOR.toString();
		}
		Boolean impactWused = (Boolean) nodeService.getProperty(branchNodeRef, BeCPGModel.PROP_AUTO_MERGE_IMPACTWUSED);
		if (impactWused == null) {
			impactWused = false;
		}
		String description = (String) nodeService.getProperty(branchNodeRef, BeCPGModel.PROP_AUTO_MERGE_COMMENTS);
		if (description == null) {
			description = "";
		}

		NodeRef newEntityNodeRef = internalMergeBranch(branchNodeRef, null, VersionType.valueOf(versionType), description, impactWused, false,
				newEffectivity);

		if (impactWused) {
			impactWUsed(newEntityNodeRef, VersionType.valueOf(versionType), description, newEffectivity);
		}

		return newEntityNodeRef;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef mergeBranch(NodeRef branchNodeRef, NodeRef branchToNodeRef, VersionType versionType, String description) {
		return mergeBranch(branchNodeRef, branchToNodeRef, versionType, description, false, false);
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef mergeBranch(NodeRef branchNodeRef, NodeRef branchToNodeRef, VersionType versionType, String description, boolean impactWused,
			boolean rename) {
		return internalMergeBranch(branchNodeRef, branchToNodeRef, versionType, description, impactWused, rename, new Date());
	}

	private NodeRef internalMergeBranch(NodeRef branchNodeRef, NodeRef branchToNodeRef, VersionType versionType, String description,
			boolean impactWused, boolean rename, Date newEffectivity) {

		if (branchToNodeRef == null) {
			branchToNodeRef = associationService.getTargetAssoc(branchNodeRef, BeCPGModel.ASSOC_AUTO_MERGE_TO);
		}

		if ((permissionService.hasPermission(branchToNodeRef, BeCPGPermissions.MERGE_ENTITY) == AccessStatus.ALLOWED) && (branchToNodeRef != null)) {

			boolean mlAware = 	MLPropertyInterceptor.setMLAware(true);
			try(ActionStateContext state = BeCPGStateHelper.onMergeEntity(branchToNodeRef, versionType) ){

				final NodeRef internalBranchToNodeRef = branchToNodeRef;

				state.addToState(branchNodeRef);

				return StopWatchSupport.build().logger(logger).scopeName(branchToNodeRef.toString()).run(() ->
					AuthenticationUtil.runAsSystem(() ->
						transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
							
							try {
								((RuleService) ruleService).disableRules();
								
								policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
								policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_ENTITY_BRANCH);
								policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);
							policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_UNDELETABLE_ASPECT);
								policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
								policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_VERSIONABLE);
								policyBehaviourFilter.disableBehaviour(ImapModel.ASPECT_IMAP_CONTENT);
								
								internalCreateInitialVersion(internalBranchToNodeRef, newEffectivity);
								
								String manualVersionLabelFrom = (String) nodeService.getProperty(branchNodeRef, BeCPGModel.PROP_MANUAL_VERSION_LABEL);
								
								/**
								 *
							  1 - Prepare branch
								 */
								
								String finalBranchName = rename ? (String) this.nodeService.getProperty(branchNodeRef, ContentModel.PROP_NAME) : (String) this.nodeService.getProperty(internalBranchToNodeRef, ContentModel.PROP_NAME);
								
								nodeService.addAspect(branchNodeRef, ContentModel.ASPECT_LOCKABLE, null);
								
								// Set beCPG CODE
								nodeService.setProperty(branchNodeRef, BeCPGModel.PROP_CODE,
										nodeService.getProperty(internalBranchToNodeRef, BeCPGModel.PROP_CODE));
								
								// Remove branchForm as it's merge
								nodeService.removeAspect(branchNodeRef, BeCPGModel.ASPECT_ENTITY_BRANCH);
								
								NodeRef branchFromNodeRef = null;
								
								if (nodeService.hasAspect(internalBranchToNodeRef, BeCPGModel.ASPECT_ENTITY_BRANCH)) {
									branchFromNodeRef = associationService.getTargetAssoc(internalBranchToNodeRef, BeCPGModel.ASSOC_BRANCH_FROM_ENTITY);
									nodeService.removeAspect(branchNodeRef, BeCPGModel.ASPECT_ENTITY_BRANCH);
								}
								
								nodeService.removeProperty(internalBranchToNodeRef, BeCPGModel.PROP_MANUAL_VERSION_LABEL);
								
								// Deattach other branches
								List<NodeRef> sources = associationService.getSourcesAssocs(branchNodeRef, BeCPGModel.ASSOC_BRANCH_FROM_ENTITY);
								for (NodeRef sourceNodeRef : sources) {
									associationService.update(sourceNodeRef, BeCPGModel.ASSOC_BRANCH_FROM_ENTITY, internalBranchToNodeRef);
									
									if (nodeService.hasAspect(internalBranchToNodeRef, ContentModel.ASPECT_VERSIONABLE)) {
										nodeService.setProperty(sourceNodeRef, BeCPGModel.PROP_BRANCH_FROM_VERSION_LABEL,
												nodeService.getProperty(internalBranchToNodeRef, ContentModel.PROP_VERSION_LABEL));
									} else {
										nodeService.setProperty(sourceNodeRef, BeCPGModel.PROP_BRANCH_FROM_VERSION_LABEL, RepoConsts.INITIAL_VERSION);
									}
									
								}
								
								for (EntityVersionPlugin entityVersionPlugin : entityVersionPlugins) {
									entityVersionPlugin.doBeforeCheckin(internalBranchToNodeRef, branchNodeRef);
								}
								
								/**
								 * Merge branch
								 */
								
								// remove assoc (copy used to checkin doesn't do it)
								
								//TODO Matthieu needed why?
								removeRemovedAssociation(branchNodeRef, internalBranchToNodeRef);
								
								// Remove comments
								mergeComments(branchNodeRef, internalBranchToNodeRef);
								
								entityActivityService.mergeActivities(internalBranchToNodeRef, branchNodeRef);
								
								// Remove rules
								ChildAssociationRef ruleChildAssocRef = ruleService.getSavedRuleFolderAssoc(internalBranchToNodeRef);
								if (ruleChildAssocRef != null) {
									if (ruleChildAssocRef.isPrimary()) {
										logger.debug("remove primary rule of entity " + internalBranchToNodeRef);
										nodeService.deleteNode(ruleChildAssocRef.getChildRef());
									} else {
										logger.debug("remove secondary rule of entity " + internalBranchToNodeRef);
										nodeService.removeSecondaryChildAssociation(ruleChildAssocRef);
									}
								}
								
								// Move workingCopyNodeRef DataList to origNodeRef
								entityService.deleteDataLists(internalBranchToNodeRef, true);
								entityService.deleteFiles(internalBranchToNodeRef, true);
								
								try {
									entityListDAO.moveDataLists(branchNodeRef, internalBranchToNodeRef);
									entityService.moveFiles(branchNodeRef, internalBranchToNodeRef);
								} catch (DuplicateChildNodeNameException e) {
									// This will be rare, but it's not impossible.
									// We have to retry the operation.
									throw new ConcurrencyFailureException("DuplicateChildNodeNameException during mergeBranch");
								}
								
								// delete files that are not moved (ie: Documents)
								// otherwise
								// checkin copy them and fails since they already
								// exits
								entityService.deleteFiles(branchNodeRef, true);
								
								//Add aspect to avoid rename during copy
								nodeService.addAspect(branchNodeRef, ContentModel.ASPECT_WORKING_COPY, null);
								
								Date createdDate = (Date) nodeService.getProperty(internalBranchToNodeRef, ContentModel.PROP_CREATED);
								String versionLabel = (String) nodeService.getProperty(internalBranchToNodeRef, ContentModel.PROP_VERSION_LABEL);
								
								// Copy the contents of the working copy onto the original
								this.copyService.copy(branchNodeRef, internalBranchToNodeRef);
								
								// reset the original createdDate and versionLabel
								nodeService.setProperty(internalBranchToNodeRef, ContentModel.PROP_CREATED, createdDate);
								nodeService.setProperty(internalBranchToNodeRef, ContentModel.PROP_VERSION_LABEL, versionLabel);
								
								if (branchFromNodeRef != null) {
									associationService.update(internalBranchToNodeRef, BeCPGModel.ASSOC_BRANCH_FROM_ENTITY, branchFromNodeRef);
								}
								
								/**
								 * Create alfresco version
								 */
								Map<String, Serializable> versionProperties = new HashMap<>();
								versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE, versionType);
								versionProperties.put(Version.PROP_DESCRIPTION, description);
								if (impactWused) {
									versionProperties.put(EntityVersionPlugin.POST_UPDATE_HISTORY_NODEREF, null);
								}
								
								VersionHistory originalVersionHistory = versionService.getVersionHistory(internalBranchToNodeRef);
								
								if (originalVersionHistory != null) {
									Version lastVersion = originalVersionHistory.getVersion(versionLabel);
									if (lastVersion != null) {
										NodeRef lastVersionNodeRef = getEntityVersion(lastVersion);
										dbNodeService.setProperty(lastVersionNodeRef, BeCPGModel.PROP_END_EFFECTIVITY, newEffectivity);
									}
								}
								
								StopWatchSupport.addCheckpoint("before internalCreateVersion");
								
								internalCreateVersion(internalBranchToNodeRef, versionProperties, newEffectivity, manualVersionLabelFrom, false);
								
								if (rename) {
									Version currentVersion = versionService.getCurrentVersion(internalBranchToNodeRef);
									dbNodeService.setProperty(getEntityVersion(currentVersion), ContentModel.PROP_NAME, finalBranchName);
								}
								
								/**
								 * Post create alfresco version
								 */
								
								// Update all association refering to this branch to point to
								// branchToNodeRef
								updateBranchAssoc(branchNodeRef, internalBranchToNodeRef);
								
								//TODO remove that when all version has been converted
								// Update also version of the node
								VersionHistory versionHistory = versionService.getVersionHistory(branchNodeRef);
								
								if (versionHistory != null) {
									List<ChildAssociationRef> versionAssocs = getVersionAssocs(branchNodeRef);
									
									for (Version version : versionHistory.getAllVersions()) {
										NodeRef entityVersionNodeRef = getEntityVersion(versionAssocs, version);
										if (entityVersionNodeRef != null && !isVersion(entityVersionNodeRef)) {
											updateBranchAssoc(entityVersionNodeRef, internalBranchToNodeRef);
										}
									}
								}
								
								entityActivityService.postMergeBranchActivity(branchNodeRef, internalBranchToNodeRef, versionType, description);
								
								nodeService.removeAspect(internalBranchToNodeRef, ContentModel.ASPECT_CHECKED_OUT);
								
								
								
								// Delete the working copy
								
								nodeService.addAspect(branchNodeRef, ContentModel.ASPECT_TEMPORARY, null);
								nodeService.deleteNode(branchNodeRef);
								
								
								/**
								 * After working copy deletion
								 */
								//Fire rules once for entity
								((RuleService) ruleService).enableRules();
								
								nodeService.setProperty(internalBranchToNodeRef, ContentModel.PROP_NAME, finalBranchName);
								
								associationService.removeAllCacheAssocs(internalBranchToNodeRef);
								
								if (nodeService.hasAspect(internalBranchToNodeRef, BeCPGModel.ASPECT_EFFECTIVITY)) {
									nodeService.setProperty(internalBranchToNodeRef, BeCPGModel.PROP_START_EFFECTIVITY, newEffectivity);
									nodeService.removeProperty(internalBranchToNodeRef, BeCPGModel.PROP_END_EFFECTIVITY);
								}
								
								nodeService.setProperty(internalBranchToNodeRef, ContentModel.PROP_MODIFIED, new Date());
								
								generateReportsAsync(internalBranchToNodeRef);
								
								StopWatchSupport.addCheckpoint("after internalCreateVersion");
								
								return internalBranchToNodeRef;
								
							} finally {
								((RuleService) ruleService).enableRules();
								policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
								policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_ENTITY_BRANCH);
								policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);
							policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_UNDELETABLE_ASPECT);
								policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
								policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_VERSIONABLE);
								policyBehaviourFilter.enableBehaviour(ImapModel.ASPECT_IMAP_CONTENT);
								
							}
							
						}, false, false)
					));
				
			} finally {
				MLPropertyInterceptor.setMLAware(mlAware);
			}

		}
		return null;
	}

	private void generateReportsAsync(final NodeRef internalBranchToNodeRef) {
		String entityDescription = nodeService.getProperty(internalBranchToNodeRef, BeCPGModel.PROP_CODE) + " "
				+ nodeService.getProperty(internalBranchToNodeRef, ContentModel.PROP_NAME);

		BatchInfo batchInfo = new BatchInfo(String.format("generateReports-%s", Calendar.getInstance().getTimeInMillis()),
				"becpg.batch.entity.generateReports", entityDescription);
		batchInfo.setRunAsSystem(true);
		batchInfo.setPriority(BatchPriority.LOW);
		
		BatchProcessWorkProvider<NodeRef> workProvider = new EntityListBatchProcessWorkProvider<>(List.of(internalBranchToNodeRef));

		BatchProcessWorker<NodeRef> processWorker = new BatchProcessor.BatchProcessWorkerAdaptor<>() {

			@Override
			public void process(NodeRef entityNodeRef) throws Throwable {
				entityReportService.generateReports(entityNodeRef);
			}
		};

		batchQueueService.queueBatch(batchInfo, workProvider, processWorker, null);
	}

	private NodeRef convertNodeAndWhereUsed(NodeRef notConvertedNode) {

		for (NodeRef source : associationService.getSourcesAssocs(notConvertedNode, QName.createQName(BeCPGModel.BECPG_URI, "compoListProduct"))) {
			NodeRef datalistFolder = nodeService.getPrimaryParent(source).getParentRef();
			NodeRef entitylistFolder = nodeService.getPrimaryParent(datalistFolder).getParentRef();
			NodeRef parentProduct = nodeService.getPrimaryParent(entitylistFolder).getParentRef();

			if (nodeService.hasAspect(parentProduct, BeCPGModel.ASPECT_COMPOSITE_VERSION)
					&& !nodeService.hasAspect(parentProduct, BeCPGModel.ASPECT_ENTITY_FORMAT)
					&& !nodeService.hasAspect(parentProduct, ContentModel.ASPECT_TEMPORARY)) {
				convertNodeAndWhereUsed(parentProduct);
			}
		}

		for (NodeRef source : associationService.getSourcesAssocs(notConvertedNode,
				QName.createQName(BeCPGModel.BECPG_URI, "packagingListProduct"))) {
			NodeRef datalistFolder = nodeService.getPrimaryParent(source).getParentRef();
			NodeRef entitylistFolder = nodeService.getPrimaryParent(datalistFolder).getParentRef();
			NodeRef parentProduct = nodeService.getPrimaryParent(entitylistFolder).getParentRef();

			if (nodeService.hasAspect(parentProduct, BeCPGModel.ASPECT_COMPOSITE_VERSION)
					&& !nodeService.hasAspect(parentProduct, BeCPGModel.ASPECT_ENTITY_FORMAT)
					&& !nodeService.hasAspect(parentProduct, ContentModel.ASPECT_TEMPORARY)) {
				convertNodeAndWhereUsed(parentProduct);
			}
		}

		logger.info("converting " + nodeService.getProperty(notConvertedNode, ContentModel.PROP_NAME));

		final NodeRef finalNotConvertedNode = notConvertedNode;

		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			String versionLabel = (String) dbNodeService.getProperty(finalNotConvertedNode, BeCPGModel.PROP_VERSION_LABEL);
			NodeRef parentNode = dbNodeService.getPrimaryParent(finalNotConvertedNode).getParentRef();
			String parentName = (String) dbNodeService.getProperty(parentNode, ContentModel.PROP_NAME);
			NodeRef originalNode = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, parentName);
			VersionHistory versionHistory = dbNodeService.exists(originalNode) ? versionService.getVersionHistory(originalNode) : null;

			if (versionHistory != null) {
				NodeRef versionNode = new NodeRef(StoreRef.PROTOCOL_WORKSPACE, Version2Model.STORE_ID,
						versionHistory.getVersion(versionLabel).getFrozenStateNodeRef().getId());

				transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
					exportEntityToVersion(finalNotConvertedNode, versionNode);
					return null;
				}, false, false);

				return versionNode;
			}

			return null;
		}, false, true);
		
	}
	
	@Override
	public NodeRef convertVersion(NodeRef nodeRef) {
		
		Set<NodeRef> oldVersionWUsed = findOldVersionWUsed(nodeRef, new HashSet<>(), null, 2, new AtomicInteger(0), null);
		
		if (oldVersionWUsed.size() > 1) {
			String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
			if (logger.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder();
				sb.append("Couldn't convert entity '" + name + "' because it is used by not converted entities :");
				for (NodeRef convertibleAncestor : oldVersionWUsed) {
					if (!convertibleAncestor.equals(nodeRef)) {
						String relativeName = (String) nodeService.getProperty(convertibleAncestor, ContentModel.PROP_NAME);
						sb.append(" '" + relativeName + "' ");
					}
				}
				logger.debug(sb.toString());
			}
			return null;
		}
		
		for (NodeRef innerEntity : getInnerEntities(nodeRef)) {
			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
				moveToImportToDoFolder(innerEntity);
				return null;
			}, false, true);
		}
		
		String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
		String versionLabel = (String) dbNodeService.getProperty(nodeRef, BeCPGModel.PROP_VERSION_LABEL);
		if (versionLabel == null) {
			versionLabel = (String) dbNodeService.getProperty(nodeRef, ContentModel.PROP_VERSION_LABEL);
		}
		if (versionLabel == null) {
			String[] splitted = name.split(RepoConsts.VERSION_NAME_DELIMITER);
			versionLabel = splitted[splitted.length - 1];
		}
		
		NodeRef parentNode = dbNodeService.getPrimaryParent(nodeRef).getParentRef();
		String parentName = (String) dbNodeService.getProperty(parentNode, ContentModel.PROP_NAME);
		NodeRef originalNode = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, parentName);
		if (!nodeService.exists(originalNode)) {
			originalNode = nodeService.getTargetAssocs(nodeRef, ContentModel.ASSOC_ORIGINAL).get(0).getTargetRef();
		}
		
		VersionHistory versionHistory = dbNodeService.exists(originalNode) ? versionService.getVersionHistory(originalNode) : null;
		if (versionHistory != null) {
			NodeRef versionNode = VersionUtil.convertNodeRef(versionHistory.getVersion(versionLabel).getFrozenStateNodeRef());
			exportEntityToVersion(nodeRef, versionNode);
		}
		return null;
	}
	
	private void moveToImportToDoFolder(NodeRef toMove) {
		NodeRef originalParent = nodeService.getPrimaryParent(toMove).getParentRef();
		String parentName = (String) nodeService.getProperty(originalParent, ContentModel.PROP_NAME);
		
		NodeRef rootNode = nodeService.getRootNode(RepoConsts.SPACES_STORE);
		NodeRef importToDoNodeRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(rootNode,RemoteEntityService.FULL_PATH_IMPORT_TO_DO);
		
		NodeRef newParent = nodeService.getChildByName(importToDoNodeRef, ContentModel.ASSOC_CONTAINS, parentName);
		
		if (newParent == null) {
			newParent = nodeService.createNode(importToDoNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_FOLDER).getChildRef();
			nodeService.setProperty(newParent, ContentModel.PROP_NAME, parentName);
		}
		
		repoService.moveEntity(toMove, newParent);
	}
	
	@Override
	public Set<NodeRef> findOldVersionWUsed(NodeRef sourceEntity) {
		return findOldVersionWUsed(sourceEntity, new HashSet<>(), null, -1, new AtomicInteger(0), null);
	}
	
	@Override
	public Set<NodeRef> findOldVersionWUsed(NodeRef sourceEntity, Set<NodeRef> visited,
			final List<NodeRef> ignoredItems, final int maxProcessedNodes, AtomicInteger currentCount, String path) {
		Set<NodeRef> ret = new LinkedHashSet<>();
		if ((maxProcessedNodes >= 0 && currentCount.get() >= maxProcessedNodes) || visited.contains(sourceEntity) || !nodeService.exists(sourceEntity)) {
			return ret;
		}
		visited.add(sourceEntity);
		Set<NodeRef> refs = new HashSet<>();
		List<AssociationRef> assocRefs = nodeService.getSourceAssocs(sourceEntity, RegexQNamePattern.MATCH_ALL);
		List<ChildAssociationRef> parentRefs = nodeService.getParentAssocs(sourceEntity);
		
		for (AssociationRef assocRef : assocRefs) {
			refs.add(assocRef.getSourceRef());
		}
		for (ChildAssociationRef parentRef : parentRefs) {
			refs.add(parentRef.getParentRef());
		}
		
		for (NodeRef ref : refs) {
			NodeRef entitySource = entityService.getEntityNodeRef(ref, BeCPGModel.TYPE_ENTITY_V2);
			if (entitySource != null) {
				if (logger.isTraceEnabled()) {
					logger.trace("findConvertibleRelatives from " + sourceEntity + "to " + entitySource);
				}
				Set<NodeRef> oldVersionWUsed = findOldVersionWUsed(entitySource, visited, ignoredItems, maxProcessedNodes, currentCount, path);
				if (ignoredItems != null) {
					oldVersionWUsed.removeAll(ignoredItems);
				}
				ret.addAll(oldVersionWUsed);
			}
		}
		
		if (ignoredItems == null || !ignoredItems.contains(sourceEntity)) {
			boolean isOldVersion = entityDictionaryService.isSubClass(nodeService.getType(sourceEntity), BeCPGModel.TYPE_ENTITY_V2)
					&& !sourceEntity.getStoreRef().getProtocol().contains(VersionBaseModel.STORE_PROTOCOL)
					&& !sourceEntity.getStoreRef().getIdentifier().contains(Version2Model.STORE_ID);
			if (isOldVersion) {
				isOldVersion = nodeService.hasAspect(sourceEntity, BeCPGModel.ASPECT_COMPOSITE_VERSION);
				
				if (!isOldVersion && path != null) {
					isOldVersion = nodeService.getPath(sourceEntity).toPrefixString(namespaceService).contains(path);
				}
			}
			if (isOldVersion) {
				ret.add(sourceEntity);
				currentCount.addAndGet(1);
				if (logger.isTraceEnabled()) {
					logger.trace("found " + currentCount.get() + " items out of " + maxProcessedNodes);
				}
			}
		}
		return ret;
	}
	
	private Set<NodeRef> getInnerEntities(NodeRef node) {
		
		Set<NodeRef> result = new HashSet<>();
		
		for (NodeRef assocNodeRef : associationService.getChildAssocs(node, ContentModel.ASSOC_CONTAINS)) {
			if (entityDictionaryService.isSubClass(nodeService.getType(assocNodeRef), BeCPGModel.TYPE_ENTITY_V2)) {
				result.add(assocNodeRef);
			}
			result.addAll(getInnerEntities(assocNodeRef));
		}
		
		return result;
	}
	
	private void exportEntityToVersion(final NodeRef entityNodeRef, final NodeRef versionNodeRef) {

		StopWatchSupport.build().logger(logger).scopeName("convert entity version").run(() -> {
			integrityChecker.setEnabled(false);
			
			try {
				transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
					ExporterCrawlerParameters crawlerParameters = new ExporterCrawlerParameters();
					Location exportFrom = new Location(entityNodeRef);
					crawlerParameters.setExportFrom(exportFrom);
					crawlerParameters.setCrawlSelf(true);
					crawlerParameters.setExcludeChildAssocs(new QName[] { RenditionModel.ASSOC_RENDITION, ForumModel.ASSOC_DISCUSSION,
							BeCPGModel.ASSOC_ENTITYLISTS, ContentModel.ASSOC_RATINGS });
					crawlerParameters.setExcludeNamespaceURIs(Arrays.asList(ReportModel.TYPE_REPORT.getNamespaceURI()).toArray(new String[0]));
					exporterService.exportView(new VersionExporter(entityNodeRef, versionNodeRef, dbNodeService, entityDictionaryService), crawlerParameters, null);
					return null;
				}, false, true);
				
				StopWatchSupport.addCheckpoint("export version");
				
				transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
					String fromName = (String) dbNodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);
					dbNodeService.setProperty(versionNodeRef, ContentModel.PROP_NAME, fromName);
					String versionLabel = (String) dbNodeService.getProperty(versionNodeRef, BeCPGModel.PROP_VERSION_LABEL);
					dbNodeService.setProperty(versionNodeRef, ContentModel.PROP_VERSION_LABEL, versionLabel);
					String generateEntityData = entityFormatService.generateEntityData(entityNodeRef, EntityFormat.JSON);
					entityFormatService.updateEntityFormat(versionNodeRef, EntityFormat.JSON, generateEntityData);
					return null;
				}, false, true);
				
				StopWatchSupport.addCheckpoint("update format");
				
				transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
					NodeRef documentsFolder = nodeService.getChildByName(versionNodeRef, ContentModel.ASSOC_CONTAINS, "Documents");
					List<NodeRef> reports = associationService.getTargetAssocs(entityNodeRef, ReportModel.ASSOC_REPORTS);
					List<NodeRef> reportCopyList = reports.stream().map(n -> copyReport(documentsFolder, n)).toList();
					associationService.update(versionNodeRef, ReportModel.ASSOC_REPORTS, reportCopyList);
					return null;
				}, false, true);
				
				StopWatchSupport.addCheckpoint("copy reports");
				
				deleteNodeRef(entityNodeRef);
				
				StopWatchSupport.addCheckpoint("delete node");
			} finally {
				integrityChecker.setEnabled(true);
			}
			return null;
		});
		

	}
	
	private NodeRef copyReport(NodeRef parentFolder, NodeRef reportNodeRef) {
		
		String reportName = (String) nodeService.getProperty(reportNodeRef, ContentModel.PROP_NAME);

		Map<QName, Serializable> props = new HashMap<>();
		props.put(ContentModel.PROP_NAME, reportName);

		NodeRef reportCopy = nodeService.createNode(parentFolder, ContentModel.ASSOC_CONTAINS,
				ContentModel.ASSOC_CONTAINS, ReportModel.TYPE_REPORT, props).getChildRef();

		ContentReader reader = contentService.getReader(reportNodeRef, ContentModel.PROP_CONTENT);
		ContentWriter writer = contentService.getWriter(reportCopy, ContentModel.PROP_CONTENT, true);
		writer.setEncoding(reader.getEncoding());
		writer.setMimetype("application/pdf");

		writer.putContent(reader);

		return reportCopy;
	}
	
	private void deleteNodeRef(final NodeRef originalNodeRef) {
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			
			dbNodeService.addAspect(originalNodeRef, ContentModel.ASPECT_TEMPORARY, null);
			
			List<NodeRef> links = getFileLinks(originalNodeRef);
			
			for (NodeRef link : links) {
				if (nodeService.getProperties(link).containsKey(ContentModel.PROP_LINK_DESTINATION) && nodeService.getProperty(link, ContentModel.PROP_LINK_DESTINATION) == null) {
					policyBehaviourFilter.disableBehaviour(link);
					nodeService.deleteNode(link);
				}
			}
			
			dbNodeService.deleteNode(originalNodeRef);
			return null;
			
		}, false, true);
	}

	private List<NodeRef> getFileLinks(NodeRef parent) {
		List<NodeRef> links = new ArrayList<>();
		
		if (ApplicationModel.TYPE_FILELINK.equals(nodeService.getType(parent))) {
			links.add(parent);
		} else {
			for (NodeRef child : associationService.getChildAssocs(parent, ContentModel.ASSOC_CONTAINS)) {
				links.addAll(getFileLinks(child));
			}
		}
		
		return links;
	}
	@Override
	public NodeRef revertVersion(NodeRef versionNodeRef) throws IllegalAccessException {

		boolean notConverted = nodeService.hasAspect(versionNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)
				&& !nodeService.hasAspect(versionNodeRef, BeCPGModel.ASPECT_ENTITY_FORMAT)
				&& !nodeService.hasAspect(versionNodeRef, ContentModel.ASPECT_TEMPORARY);

		if (notConverted) {
			final NodeRef finalNode = versionNodeRef;
			versionNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
				return convertNodeAndWhereUsed(finalNode);
			}, false, true);
		}

		String parentName = (String) nodeService.getProperty(nodeService.getPrimaryParent(versionNodeRef).getParentRef(), ContentModel.PROP_NAME);

		NodeRef entityNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, parentName);

		if (!nodeService.exists(entityNodeRef)) {
			throw new IllegalAccessException("Parent nodeRef doesn't exist : " + entityNodeRef);
		}

		NodeRef newBranch = createEmptyBranch(entityNodeRef, nodeService.getPrimaryParent(entityNodeRef).getParentRef());

		String entityJsonString = entityFormatService.getEntityData(versionNodeRef);

		Date createdDate = (Date) nodeService.getProperty(newBranch, ContentModel.PROP_CREATED);

		JSONObject json = new JSONObject(entityJsonString);

		String name = (String) dbNodeService.getProperty(versionNodeRef, ContentModel.PROP_NAME) + "~";

		((JSONObject) json.get("entity")).put("cm:name", name);

		((JSONObject) ((JSONObject) json.get("entity")).get("attributes")).put("cm:name", name);

		entityFormatService.createOrUpdateEntityFromJson(newBranch, json.toString());

		nodeService.setProperty(newBranch, ContentModel.PROP_CREATED, createdDate);

		nodeService.removeAspect(newBranch, ContentModel.ASPECT_VERSIONABLE);
		nodeService.removeAspect(newBranch, BeCPGModel.ASPECT_COMPOSITE_VERSION);

		return newBranch;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef createVersion(NodeRef entityNodeRef, Map<String, Serializable> versionProperties) {
		return createVersion(entityNodeRef, versionProperties, null);
	}

	@Override
	public NodeRef createVersion(final NodeRef entityNodeRef, Map<String, Serializable> versionProperties, Date effectiveDate) {
		NodeRef versionNodeRef = internalCreateVersion(entityNodeRef, versionProperties, effectiveDate, null, false);
		generateReportsAsync(entityNodeRef);
		return versionNodeRef;
	}
	
	private NodeRef internalCreateVersion(final NodeRef entityNodeRef, Map<String, Serializable> versionProperties, Date newEffectivity, String manualVersionLabel, boolean isInitialVersion) {
		if (nodeService.hasAspect(entityNodeRef, ContentModel.ASPECT_VERSIONABLE)) {

			// Set effectivity
			if (newEffectivity == null) {
				newEffectivity = new Date();
			}
			
			Date oldEffectivity = (Date) nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_START_EFFECTIVITY);
			if (oldEffectivity == null) {
				oldEffectivity = newEffectivity;
			}
			
			if (oldEffectivity.compareTo(newEffectivity) > 0) {
				newEffectivity = oldEffectivity;
			}
			
			final Date finalNewEffectivity = newEffectivity;
			final Date finalOldEffectivity = oldEffectivity;
			
			return StopWatchSupport.build().logger(logger).run(() -> {
				
				if (isInitialVersion) {
					nodeService.setProperty(entityNodeRef, BeCPGModel.PROP_START_EFFECTIVITY, finalOldEffectivity);
					nodeService.setProperty(entityNodeRef, BeCPGModel.PROP_END_EFFECTIVITY, finalNewEffectivity);
				} else {
					nodeService.setProperty(entityNodeRef, BeCPGModel.PROP_START_EFFECTIVITY, finalNewEffectivity);
				}
				
				// create the version node
				Version newVersion = versionService.createVersion(entityNodeRef, versionProperties);

				Map<String, Object> extraParams = new HashMap<>();

				if (isInitialVersion) {
					extraParams.put(RemoteParams.PARAM_IS_INITIAL_VERSION, true);
				}
				
				extraParams.put(RemoteParams.PARAM_APPEND_REPORT_PROPS, true);

				// extract the JSON data of the current node
				String jsonData = entityFormatService.generateEntityData(entityNodeRef, EntityFormat.JSON, extraParams);
				
				NodeRef versionNode = getEntityVersion(newVersion);

				// add child assocs to versions
				ExporterCrawlerParameters crawlerParameters = new ExporterCrawlerParameters();

				Location exportFrom = new Location(entityNodeRef);
				crawlerParameters.setExportFrom(exportFrom);

				crawlerParameters.setCrawlSelf(true);
				crawlerParameters.setExcludeChildAssocs(new QName[] { RenditionModel.ASSOC_RENDITION, ForumModel.ASSOC_DISCUSSION,
						BeCPGModel.ASSOC_ENTITYLISTS, ContentModel.ASSOC_RATINGS });

				crawlerParameters.setExcludeNamespaceURIs(Arrays.asList(ReportModel.TYPE_REPORT.getNamespaceURI()).toArray(new String[0]));

				exporterService.exportView(new VersionExporter(entityNodeRef, versionNode, dbNodeService, entityDictionaryService), crawlerParameters,
						null);
				
				entityFormatService.updateEntityFormat(versionNode, EntityFormat.JSON, jsonData);
				
				String versionLabel = newVersion.getVersionLabel();

				if (manualVersionLabel != null && !manualVersionLabel.isBlank()) {
					versionLabel = manualVersionLabel;
					dbNodeService.setProperty(entityNodeRef, ContentModel.PROP_VERSION_LABEL, manualVersionLabel);
					dbNodeService.setProperty(versionNode, ContentModel.PROP_VERSION_LABEL, manualVersionLabel);
					dbNodeService.setProperty(versionNode, Version2Model.PROP_QNAME_VERSION_LABEL, manualVersionLabel);
				}

				dbNodeService.setProperty(versionNode, BeCPGModel.PROP_VERSION_LABEL, versionLabel);

				String name = dbNodeService.getProperty(versionNode, ContentModel.PROP_NAME) + RepoConsts.VERSION_NAME_DELIMITER + versionLabel;
				dbNodeService.setProperty(versionNode, ContentModel.PROP_NAME, name);

				dbNodeService.setProperty(versionNode, Version2Model.PROP_QNAME_FROZEN_MODIFIED, new Date());
				
				
				if (nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_EFFECTIVITY)) {
					nodeService.setProperty(entityNodeRef, BeCPGModel.PROP_START_EFFECTIVITY, finalNewEffectivity == null ? new Date() : finalNewEffectivity);
					nodeService.removeProperty(entityNodeRef, BeCPGModel.PROP_END_EFFECTIVITY);
				}

				if ((versionProperties != null) && versionProperties.containsKey(EntityVersionPlugin.POST_UPDATE_HISTORY_NODEREF)) {
					NodeRef postUpdateHistoryNodeRef = (NodeRef) versionProperties.get(EntityVersionPlugin.POST_UPDATE_HISTORY_NODEREF);
					if (postUpdateHistoryNodeRef != null) {
						updateEntitiesHistory(postUpdateHistoryNodeRef, entityNodeRef);
					}
					
				} else {
					updateEntitiesHistory(entityNodeRef, null);
				}
				
				entityActivityService.postVersionActivity(entityNodeRef, newVersion.getVersionedNodeRef(), versionLabel);
				
				return versionNode;
					
			});

		} else {
			logger.info("Should create initial version first");
		}

		return null;
	}

	private void updateBranchAssoc(NodeRef branchNodeRef, NodeRef branchToNodeRef) {

		List<AssociationRef> assocRefs = nodeService.getSourceAssocs(branchNodeRef, RegexQNamePattern.MATCH_ALL);

		for (AssociationRef assocRef : assocRefs) {
			policyBehaviourFilter.disableBehaviour(assocRef.getSourceRef(), ContentModel.ASPECT_AUDITABLE);
			try {
				if ((assocRef.getTargetRef() != null) && !assocRef.getTargetRef().equals(branchToNodeRef)
						&& !ContentModel.ASSOC_WORKING_COPY_LINK.equals(assocRef.getTypeQName())) {

					nodeService.removeAssociation(assocRef.getSourceRef(), assocRef.getTargetRef(), assocRef.getTypeQName());
					nodeService.createAssociation(assocRef.getSourceRef(), branchToNodeRef, assocRef.getTypeQName());

				}
			} catch (AssociationExistsException e) {
				// DO Nothing
			} finally {
				policyBehaviourFilter.enableBehaviour(assocRef.getSourceRef(), ContentModel.ASPECT_AUDITABLE);
			}
		}

	}

	private void mergeComments(NodeRef branchNodeRef, NodeRef branchToNodeRef) {
		PagingResults<NodeRef> comments = commentService.listComments(branchNodeRef, new PagingRequest(5000, null));
		if (comments != null) {
			for (NodeRef commentNodeRef : comments.getPage()) {
				NodeRef newComment = null;
				boolean mlAware = MLPropertyInterceptor.setMLAware(false);

				try {

					MLPropertyInterceptor.setMLAware(false);
					ContentReader reader = contentService.getReader(commentNodeRef, ContentModel.PROP_CONTENT);
					String comment = reader.getContentString();
					newComment = commentService.createComment(branchToNodeRef,
							(String) nodeService.getProperty(commentNodeRef, ContentModel.PROP_TITLE), comment, false);

					policyBehaviourFilter.disableBehaviour(newComment, ContentModel.ASPECT_AUDITABLE);
					nodeService.setProperty(newComment, ContentModel.PROP_CREATED,
							nodeService.getProperty(commentNodeRef, ContentModel.PROP_CREATED));
					nodeService.setProperty(newComment, ContentModel.PROP_CREATOR,
							nodeService.getProperty(commentNodeRef, ContentModel.PROP_CREATOR));
					nodeService.setProperty(newComment, ContentModel.PROP_MODIFIED,
							nodeService.getProperty(commentNodeRef, ContentModel.PROP_MODIFIED));
					commentService.deleteComment(commentNodeRef);
				} finally {
					MLPropertyInterceptor.setMLAware(mlAware);
					if (newComment != null) {
						policyBehaviourFilter.enableBehaviour(newComment, ContentModel.ASPECT_AUDITABLE);
					}
				}
			}
		}
	}

	private NodeRef createEmptyBranch(NodeRef entityNodeRef, NodeRef parentRef) {

		return StopWatchSupport.build().logger(logger).scopeName(entityNodeRef.toString()).run(() -> {
			boolean mlAware = MLPropertyInterceptor.setMLAware(true);
			try {
				
				return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
					
					// Only for transaction do not reenable it
					policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
					policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);
					policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_ENTITY_BRANCH);
					policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
					
					String newEntityName = repoService.getAvailableName(parentRef,
							(String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME), true);
					
					NodeRef branchNodeRef = nodeService.createNode(parentRef, ContentModel.ASSOC_CONTAINS,	QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(newEntityName)), nodeService.getType(entityNodeRef))
							.getChildRef();
					
					if (nodeService.hasAspect(entityNodeRef, ContentModel.ASPECT_VERSIONABLE)) {
						nodeService.setProperty(branchNodeRef, BeCPGModel.PROP_BRANCH_FROM_VERSION_LABEL,
								nodeService.getProperty(entityNodeRef, ContentModel.PROP_VERSION_LABEL));
					} else {
						nodeService.setProperty(branchNodeRef, BeCPGModel.PROP_BRANCH_FROM_VERSION_LABEL, RepoConsts.INITIAL_VERSION);
					}
					
					nodeService.setAssociations(branchNodeRef, BeCPGModel.ASSOC_BRANCH_FROM_ENTITY, Collections.singletonList(entityNodeRef));
					
					return branchNodeRef;
					
				}, false, false);
				
			} finally {
				MLPropertyInterceptor.setMLAware(mlAware);
			}
		});
		
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef createBranch(NodeRef entityNodeRef, NodeRef parentRef) {

		return StopWatchSupport.build().logger(logger).scopeName(entityNodeRef.toString()).run(() -> {
			
			boolean mlAware = MLPropertyInterceptor.setMLAware(true);
			try (ActionStateContext state = BeCPGStateHelper.onBranchEntity(entityNodeRef)) {
				
				return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
					
					// Only for transaction do not reenable it
					policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
					policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);
					policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_ENTITY_BRANCH);
					policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
					
					String newEntityName = repoService.getAvailableName(parentRef,
							(String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME), true);
					
					NodeRef branchNodeRef = null;
					
					try {
						branchNodeRef = entityService.createOrCopyFrom(entityNodeRef, parentRef, nodeService.getType(entityNodeRef), newEntityName);
						StopWatchSupport.addCheckpoint("createOrCopyFrom");
						state.addToState(branchNodeRef);
					} catch (AssociationExistsException e) {
						// This will be rare, but it's not impossible.
						// We have to retry the operation.
						throw new ConcurrencyFailureException("Association already exists for this noderef : " + entityNodeRef);
					}
					
					if (nodeService.hasAspect(entityNodeRef, ContentModel.ASPECT_VERSIONABLE)) {
						nodeService.setProperty(branchNodeRef, BeCPGModel.PROP_BRANCH_FROM_VERSION_LABEL,
								nodeService.getProperty(entityNodeRef, ContentModel.PROP_VERSION_LABEL));
					} else {
						nodeService.setProperty(branchNodeRef, BeCPGModel.PROP_BRANCH_FROM_VERSION_LABEL, RepoConsts.INITIAL_VERSION);
					}
					nodeService.setProperty(branchNodeRef, ContentModel.PROP_CREATED, new Date());
					nodeService.setProperty(branchNodeRef, ContentModel.PROP_MODIFIED, new Date());
					nodeService.setProperty(branchNodeRef, ContentModel.PROP_CREATOR, AuthenticationUtil.getFullyAuthenticatedUser());
					nodeService.setProperty(branchNodeRef, ContentModel.PROP_MODIFIER, AuthenticationUtil.getFullyAuthenticatedUser());
					
					nodeService.setAssociations(branchNodeRef, BeCPGModel.ASSOC_BRANCH_FROM_ENTITY, Collections.singletonList(entityNodeRef));
					
					nodeService.removeProperty(branchNodeRef, BeCPGModel.PROP_MANUAL_VERSION_LABEL);
					
					StopWatchSupport.addCheckpoint("setProperties");
					
					return branchNodeRef;
					
				}, false, false);
				
			} finally {
				MLPropertyInterceptor.setMLAware(mlAware);
			}
		});
	}

	/** {@inheritDoc} */
	@Override
	public void impactWUsed(NodeRef entityNodeRef, VersionType versionType, String description, Date effectiveDate) {
		if (entityVersionPlugins != null) {
			for (EntityVersionPlugin entityVersionPlugin : entityVersionPlugins) {
				entityVersionPlugin.impactWUsed(entityNodeRef, versionType, description, effectiveDate);
			}
		}

	}

	@Override
	public boolean isVersion(NodeRef nodeRef) {
		return nodeRef.getStoreRef().getProtocol().contains(VersionBaseModel.STORE_PROTOCOL)
				|| nodeRef.getStoreRef().getIdentifier().contains(Version2Model.STORE_ID);
	}

	@Override
	public NodeRef extractVersion(NodeRef versionNodeRef) {

		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef extractedVersion = findExtractedVersion(versionNodeRef);

			if (extractedVersion == null || !nodeService.exists(extractedVersion)) {

				extractedVersion = createExtractedVersion(versionNodeRef);

			}
			return extractedVersion;
		}, false, false);

	}

	private NodeRef findExtractedVersion(final NodeRef versionNodeRef) {

		NodeRef versionHistoryRef = getVersionHistoryNodeRef(versionNodeRef,false);

		final String versionLabel = (String) dbNodeService.getProperty(versionNodeRef, Version2Model.PROP_QNAME_VERSION_LABEL);

		if (versionHistoryRef != null) {
			List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(versionHistoryRef);

			for (ChildAssociationRef childAssoc : childAssocs) {

				String version = (String) nodeService.getProperty(childAssoc.getChildRef(), BeCPGModel.PROP_VERSION_LABEL);

				if (versionLabel.equals(version)) {
					return childAssoc.getChildRef();
				}
			}
		}

		return null;
	}

	private NodeRef createExtractedVersion(final NodeRef versionNodeRef) {

		try {

			final String versionLabel = (String) dbNodeService.getProperty(versionNodeRef, Version2Model.PROP_QNAME_VERSION_LABEL);

			NodeRef versionHistoryRef = getVersionHistoryNodeRef(versionNodeRef, true);

			((RuleService) ruleService).disableRules();

			policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
			policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_ENTITY_BRANCH);
			policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);
			policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
			policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_VERSIONABLE);
			policyBehaviourFilter.disableBehaviour(ImapModel.ASPECT_IMAP_CONTENT);
			policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_ENTITY_TPL_REF);

			// create the temporary mirror node in EntitiesHistory folder

			String entityJson = entityFormatService.getEntityData(versionNodeRef);

			Map<QName, Serializable> props = new HashMap<>();
			props.put(ContentModel.PROP_NAME, versionNodeRef.getId());

			ChildAssociationRef childAssoc = nodeService.createNode(versionHistoryRef, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, versionNodeRef.getId()), dbNodeService.getType(versionNodeRef), props);

			NodeRef extractedVersion = childAssoc.getChildRef();

			ExporterCrawlerParameters crawlerParameters = new ExporterCrawlerParameters();

			Location exportFrom = new Location(versionNodeRef);
			crawlerParameters.setExportFrom(exportFrom);

			crawlerParameters.setCrawlSelf(true);
			crawlerParameters
					.setExcludeChildAssocs(new QName[] { QName.createQName(Version2Model.NAMESPACE_URI, VersionBaseModel.CHILD_VERSIONED_ASSOCS),
							RenditionModel.ASSOC_RENDITION, ForumModel.ASSOC_DISCUSSION, BeCPGModel.ASSOC_ENTITYLISTS, ContentModel.ASSOC_RATINGS });

			crawlerParameters.setExcludeNamespaceURIs(Arrays.asList(ReportModel.TYPE_REPORT.getNamespaceURI()).toArray(new String[0]));

			// reconstructs the folder hierarchy
			exporterService.exportView(new VersionExporter(versionNodeRef, extractedVersion, nodeService, entityDictionaryService), crawlerParameters,
					null);

			entityFormatService.createOrUpdateEntityFromJson(extractedVersion, entityJson);

			if (lockService.isLocked(extractedVersion)) {
				lockService.unlock(extractedVersion);
			}

			String name = nodeService.getProperty(extractedVersion, ContentModel.PROP_NAME) + RepoConsts.VERSION_NAME_DELIMITER + versionLabel;
			Map<QName, Serializable> versionAspectProperties = new HashMap<>(2);
			versionAspectProperties.put(ContentModel.PROP_NAME, name);
			versionAspectProperties.put(BeCPGModel.PROP_VERSION_LABEL, versionLabel);
			nodeService.addAspect(extractedVersion, BeCPGModel.ASPECT_COMPOSITE_VERSION, versionAspectProperties);

			nodeService.setProperty(extractedVersion, ContentModel.PROP_VERSION_LABEL, versionLabel);

			// MNT-11911 fix, add ASPECT_INDEX_CONTROL and property that not create indexes for search and not visible files/folders at 'My Documents' dashlet
			Map<QName, Serializable> aspectProperties = new HashMap<>(2);
			aspectProperties.put(ContentModel.PROP_IS_INDEXED, Boolean.FALSE);
			aspectProperties.put(ContentModel.PROP_IS_CONTENT_INDEXED, Boolean.FALSE);
			nodeService.addAspect(extractedVersion, ContentModel.ASPECT_INDEX_CONTROL, aspectProperties);

			// add temporary aspect in order to delete the node later with VersionCleanerJob
			nodeService.addAspect(extractedVersion, ContentModel.ASPECT_TEMPORARY, null);

			return extractedVersion;

		} finally {
			((RuleService) ruleService).enableRules();
			policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
			policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_ENTITY_BRANCH);
			policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);
			policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
			policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_VERSIONABLE);
			policyBehaviourFilter.enableBehaviour(ImapModel.ASPECT_IMAP_CONTENT);
			policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_ENTITY_TPL_REF);
		}
	}

}
