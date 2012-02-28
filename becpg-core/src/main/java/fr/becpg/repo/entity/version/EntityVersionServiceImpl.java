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
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;

/**
 * Store the entity version history in the SpacesStore otherwise we cannot use
 * lucene query and datalists don't work so we cannot get them.
 * 
 * @author querephi
 */
public class EntityVersionServiceImpl implements EntityVersionService {
	private static final String ENTITIES_HISTORY_NAME = "entitiesHistory";

	private static final QName QNAME_ENTITIES_HISTORY = QName.createQName(BeCPGModel.BECPG_URI, ENTITIES_HISTORY_NAME);

	private static final String ENTITIES_HISTORY_XPATH = "/bcpg:entitiesHistory";

	/** The Constant VERSION_NAME_DELIMITER. */
	private static final String VERSION_NAME_DELIMITER = " v";

	/** The logger. */
	private static Log logger = LogFactory.getLog(EntityVersionServiceImpl.class);

	/** The node service. */
	private NodeService nodeService;

	/** The copy service. */
	private CopyService copyService;

	/** The search service. */
	private SearchService searchService;

	/** The entitys history node ref. */
	private NodeRef entitiesHistoryNodeRef;

	private BehaviourFilter policyBehaviourFilter;

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

	/**
	 * Sets the search service.
	 * 
	 * @param searchService
	 *            the new search service
	 */
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	@Override
	public NodeRef createEntityVersion(final NodeRef nodeRef, Version version) {

		/*
		 * 1. FileFolderService.copy and CopyService.copy doesn't support cross
		 * copy stores : This operation is not supported by a version store
		 * implementation of the node service. 2. createNode doesn't work with
		 * version store : This operation is not supported by a version store
		 * implementation of the node service.
		 */

		logger.debug("createEntityVersion: " + nodeRef);

		final NodeRef versionHistoryRef = getVersionHistoryNodeRef(nodeRef);
		QName type = nodeService.getType(nodeRef);

		// disable policy to avoid code, folder initialization and report
		// generation
		policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_CODE);
		policyBehaviourFilter.disableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
		policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
		policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_VERSIONABLE);
		// doesn't work, need to disable current class, subclass of entity, better than disableBehaviour()
		//policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITY);
		policyBehaviourFilter.disableBehaviour(type);

		NodeRef versionNodeRef = null;

		try {

			// Rights are checked by copyService during recursiveCopy
			versionNodeRef = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>() {
				@Override
				public NodeRef doWork() throws Exception {

					return copyService.copy(nodeRef, versionHistoryRef, ContentModel.ASSOC_CONTAINS,
							ContentModel.ASSOC_CHILDREN, true);

				}
			}, AuthenticationUtil.getSystemUserName());

			if (nodeService.hasAspect(versionNodeRef, ContentModel.ASPECT_CHECKED_OUT)) {
				nodeService.removeAspect(versionNodeRef, ContentModel.ASPECT_CHECKED_OUT);
			}

			Map<QName, Serializable> versionProperties = nodeService.getProperties(versionNodeRef);
			String name = nodeService.getProperty(nodeRef, ContentModel.PROP_NAME) + VERSION_NAME_DELIMITER
					+ nodeService.getProperty(nodeRef, ContentModel.PROP_VERSION_LABEL);
			versionProperties.put(ContentModel.PROP_NAME, name);
			nodeService.addAspect(versionNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION, versionProperties);

			updateEffectivity(nodeRef, versionNodeRef);

		} finally {
			policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_CODE);
			policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_FINISHEDPRODUCT);
			policyBehaviourFilter.enableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
			policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
			policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_VERSIONABLE);
			// doesn't work, need to disable current class, subclass of entity, better than disableBehaviour()
			//policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITY);
			policyBehaviourFilter.enableBehaviour(type);

		}

		return versionNodeRef;
	}

	private void updateEffectivity(NodeRef entityNodeRef, NodeRef versionNodeRef) {
		Date newEffectivity = new Date();
		Date oldEffectivity = (Date) nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_START_EFFECTIVITY);
		if (oldEffectivity == null) {
			oldEffectivity = newEffectivity;
		}
		nodeService.setProperty(versionNodeRef, BeCPGModel.PROP_START_EFFECTIVITY, oldEffectivity);
		nodeService.setProperty(versionNodeRef, BeCPGModel.PROP_END_EFFECTIVITY, newEffectivity);
		nodeService.setProperty(entityNodeRef, BeCPGModel.PROP_START_EFFECTIVITY, newEffectivity);
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

		// does entitys history folder exist ?
		if (getEntitysHistoryFolder() == null) {
			createEntitysHistoryFolder();
		}

		ChildAssociationRef childAssocRef = nodeService.createNode(getEntitysHistoryFolder(),
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

		if (getEntitysHistoryFolder() != null) {
			vhNodeRef = nodeService.getChildByName(getEntitysHistoryFolder(), ContentModel.ASSOC_CONTAINS,
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
	private NodeRef getEntitysHistoryFolder() {
		if (entitiesHistoryNodeRef == null) {

			ResultSet resultSet = null;

			try {
				resultSet = searchService.query(RepoConsts.SPACES_STORE, SearchService.LANGUAGE_XPATH,
						ENTITIES_HISTORY_XPATH);
				if (resultSet.length() > 0) {
					entitiesHistoryNodeRef = resultSet.getNodeRef(0);
				}
			} catch (Exception e) {
				logger.error("Failed to get entitysHistory", e);
			} finally {
				if (resultSet != null)
					resultSet.close();
			}
		}

		return entitiesHistoryNodeRef;
	}

	/**
	 * Create the entitys history folder node where we store entity versions
	 * (create it if it doesn't exist).
	 */
	private void createEntitysHistoryFolder() {
		if (entitiesHistoryNodeRef == null) {

			final NodeRef storeNodeRef = nodeService.getRootNode(RepoConsts.SPACES_STORE);

			AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
				@Override
				public Boolean doWork() throws Exception {
					// create folder
					logger.debug("create folder 'EntitysHistory'");
					HashMap<QName, Serializable> props = new HashMap<QName, Serializable>();
					props.put(ContentModel.PROP_NAME, ENTITIES_HISTORY_NAME);
					entitiesHistoryNodeRef = nodeService.createNode(storeNodeRef, ContentModel.ASSOC_CHILDREN,
							QNAME_ENTITIES_HISTORY, ContentModel.TYPE_FOLDER, props).getChildRef();

					return null;

				}
			}, AuthenticationUtil.getSystemUserName());
		}
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

			if (version.getVersionLabel().equals(
					nodeService.getProperty(versionNodeRef, ContentModel.PROP_VERSION_LABEL))) {
				return versionNodeRef;
			}
		}

		logger.error("Failed to find entity version. version: " + version.getVersionedNodeRef());
		return null;
	}

}