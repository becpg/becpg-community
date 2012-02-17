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
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionServiceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.Pair;
import org.alfresco.util.VersionNumber;
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

	/** The Constant MSGID_ERR_NOT_FOUND. */
	private static final String MSGID_ERR_NOT_FOUND = "version_service.err_not_found";

	/** The Constant MSGID_ERR_NO_BRANCHES. */
	private static final String MSGID_ERR_NO_BRANCHES = "version_service.err_unsupported";

	private static final String ENTITIES_HISTORY_NAME = "entitiesHistory";

	private static final QName QNAME_ENTITIES_HISTORY = QName.createQName(BeCPGModel.BECPG_URI, ENTITIES_HISTORY_NAME);

	private static final String ENTITIES_HISTORY_XPATH = "/bcpg:entitiesHistory";

	/** The Constant VERSION_INITIAL. */
	private static final String VERSION_INITIAL = "1.0";

	/** The Constant VERSION_NAME_DELIMITER. */
	private static final String VERSION_NAME_DELIMITER = " v";

	/** The logger. */
	private static Log logger = LogFactory.getLog(EntityVersionServiceImpl.class);

	/** The node service. */
	private NodeService nodeService;

	/** The copy service. */
	private CopyService copyService;

	/** The person service. */
	private PersonService personService;

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
	 * Sets the person service.
	 * 
	 * @param personService
	 *            the new person service
	 */
	public void setPersonService(PersonService personService) {
		this.personService = personService;
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
	public NodeRef createVersion(NodeRef nodeRef, Map<String, Serializable> properties) {

		/*
		 * 1. FileFolderService.copy and CopyService.copy doesn't support cross
		 * copy stores : This operation is not supported by a version store
		 * implementation of the node service. 2. createNode doesn't work with
		 * version store : This operation is not supported by a version store
		 * implementation of the node service.
		 */

		logger.debug("createVersion");

		// Check properties
		VersionNumber newVersionNumber = null;
		String versionDescription = "";
		if (properties != null) {
			String versionLabel = (String) properties.get(BeCPGModel.PROP_VERSION_LABEL.toPrefixString());

			if (versionLabel != null && versionLabel != "") {
				newVersionNumber = new VersionNumber(versionLabel);
			}

			versionDescription = (String) properties.get(Version.PROP_DESCRIPTION);
		}

		VersionNumber currentVersion = null;
		NodeRef versionHistoryRef = getVersionHistoryNodeRef(nodeRef);

		if (versionHistoryRef == null) {
			logger.debug("createVersionHistory");
			versionHistoryRef = createVersionHistory(nodeRef);
			currentVersion = new VersionNumber(VERSION_INITIAL);

			// save initial version in version history
			createVersion(versionHistoryRef, nodeRef, currentVersion, versionDescription);
		} else {

			// Since we have an existing version history we should be able to
			// lookup
			// the current version
			Pair<Boolean, VersionNumber> result = getCurrentVersionImpl(versionHistoryRef, nodeRef);
			boolean headVersion = false;

			if (result != null) {
				currentVersion = result.getSecond();
				headVersion = result.getFirst();
			}

			if (currentVersion == null) {
				throw new VersionServiceException(MSGID_ERR_NOT_FOUND);
			}

			// Need to check that we are not about to create branch since this
			// is not currently supported
			if (!headVersion) {
				// belt-and-braces - remove extra check at some point
				// although child assocs should be in ascending time (hence
				// version creation) order
				List<NodeRef> versionHistory = buildVersionHistory(versionHistoryRef, nodeRef);
				NodeRef headVersionRef = versionHistory.get(versionHistory.size() - 1);
				String versionLabel = (String) this.nodeService.getProperty(nodeRef, BeCPGModel.PROP_VERSION_LABEL);
				String headVersionLabel = (String) this.nodeService.getProperty(headVersionRef,
						BeCPGModel.PROP_VERSION_LABEL);

				if (headVersionLabel.equals(versionLabel)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Belt-and-braces: current version does seem to be head version ["
								+ versionHistoryRef + ", " + nodeRef + "]");
					}
				} else {
					throw new VersionServiceException(MSGID_ERR_NO_BRANCHES);
				}
			}

		}

		if (newVersionNumber == null) {
			// incremet minor number
			int minorNb = currentVersion.getPart(1) + 1;
			newVersionNumber = new VersionNumber(currentVersion.getPart(0) + VERSION_DELIMITER + minorNb);
		} else {
			// Check new version number
			if (currentVersion != null) {

				if (currentVersion.getPart(0) > newVersionNumber.getPart(0)) {
					throw new VersionServiceException("New version must be bigger than current.");
				} else if (currentVersion.getPart(0) == newVersionNumber.getPart(0)) {
					if (currentVersion.getPart(1) > newVersionNumber.getPart(1)) {
						throw new VersionServiceException("New version must be bigger than current.");
					} else if (currentVersion.getPart(1) == newVersionNumber.getPart(1)) {

						throw new VersionServiceException("New version must be bigger than current.");
					} else {
						// OK
					}
				} else {
					// OK
				}
			}
		}

		Map<QName, Serializable> versionableProperties = new HashMap<QName, Serializable>();
		versionableProperties.put(BeCPGModel.PROP_VERSION_LABEL, newVersionNumber.toString());
		nodeService.addAspect(nodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSIONABLE, versionableProperties);
		
		// save new version in version history, change name to support several
		// children
		return createVersion(versionHistoryRef, nodeRef, newVersionNumber, versionDescription);
	}

	@Override
	public List<NodeRef> getVersionHistory(NodeRef entityNodeRef) {

		List<NodeRef> versionHistory = new ArrayList<NodeRef>();
		NodeRef versionHistoryRef = getVersionHistoryNodeRef(entityNodeRef);
		if (versionHistoryRef != null) {
			versionHistory = buildVersionHistory(versionHistoryRef, entityNodeRef);
		}

		return versionHistory;
	}

	@Override
	public List<VersionData> getVersionHistoryWithProperties(NodeRef entityNodeRef) {
		List<NodeRef> versionHistoryNodeRefs = getVersionHistory(entityNodeRef);

		List<VersionData> versionHistory = new ArrayList<VersionData>();

		for (NodeRef versionNodeRef : versionHistoryNodeRefs) {

			Map<QName, Serializable> versionProperties = nodeService.getProperties(versionNodeRef);
			String name = (String) versionProperties.get(ContentModel.PROP_NAME);
			String version = (String) versionProperties.get(BeCPGModel.PROP_VERSION_LABEL);
			name = name.replace(VERSION_NAME_DELIMITER + version, "");
			NodeRef personNodeRef = personService.getPerson((String) versionProperties
					.get(BeCPGModel.PROP_FROZEN_MODIFIER));
			Map<QName, Serializable> personProperties = nodeService.getProperties(personNodeRef);

			VersionData versionData = new VersionData(versionNodeRef, name, version,
					(String) versionProperties.get(BeCPGModel.PROP_VERSION_DESCRIPTION),
					(Date) versionProperties.get(BeCPGModel.PROP_FROZEN_MODIFIED),
					(String) personProperties.get(ContentModel.PROP_USERNAME),
					(String) personProperties.get(ContentModel.PROP_FIRSTNAME),
					(String) personProperties.get(ContentModel.PROP_LASTNAME),
					(Date)  versionProperties.get(BeCPGModel.PROP_START_EFFECTIVITY),
					(Date)  versionProperties.get(BeCPGModel.PROP_END_EFFECTIVITY));

			versionHistory.add(versionData);
		}

		return versionHistory;
	}
	
	/**
	 * Create the version.
	 * 
	 * @param versionHistoryNodeRef
	 *            the version history node ref
	 * @param entityNodeRef
	 *            the entity node ref
	 * @param versionNumber
	 *            the version number
	 * @param versionDescription
	 *            the version description
	 * @return the node ref
	 */
	private NodeRef createVersion(final NodeRef versionHistoryNodeRef, final NodeRef entityNodeRef,
			VersionNumber versionNumber, String versionDescription) {

		// disable policy to avoid code, folder initialization and report generation
		policyBehaviourFilter.disableBehaviour();
		NodeRef versionNodeRef = null;

		try {

			// Rights are checked by copyService during recursiveCopy
			versionNodeRef = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>() {
				@Override
				public NodeRef doWork() throws Exception {

					return copyService.copy(entityNodeRef, versionHistoryNodeRef, ContentModel.ASSOC_CONTAINS,
							ContentModel.ASSOC_CHILDREN, true);

				}
			}, AuthenticationUtil.getSystemUserName());

			/*-- Store version properties (version label, version description, frozen properties and entity properties not already stored) --*/
			Map<QName, Serializable> versionProperties = nodeService.getProperties(versionNodeRef);
			Map<QName, Serializable> entityProperties = nodeService.getProperties(entityNodeRef);						

			String name = entityProperties.get(ContentModel.PROP_NAME) + VERSION_NAME_DELIMITER + versionNumber;
			versionProperties.put(ContentModel.PROP_NAME, name);
			versionProperties.put(BeCPGModel.PROP_FROZEN_CREATOR, entityProperties.get(ContentModel.PROP_CREATOR));
			versionProperties.put(BeCPGModel.PROP_FROZEN_CREATED, entityProperties.get(ContentModel.PROP_CREATED));
			versionProperties.put(BeCPGModel.PROP_FROZEN_MODIFIER, entityProperties.get(ContentModel.PROP_MODIFIER));
			versionProperties.put(BeCPGModel.PROP_FROZEN_MODIFIED, entityProperties.get(ContentModel.PROP_MODIFIED));
			versionProperties.put(BeCPGModel.PROP_FROZEN_ACCESSED, entityProperties.get(ContentModel.PROP_ACCESSED));
			versionProperties.put(BeCPGModel.PROP_VERSION_LABEL, versionNumber);
			versionProperties.put(BeCPGModel.PROP_VERSION_DESCRIPTION, versionDescription);
			versionProperties.put(BeCPGModel.PROP_FROZEN_NODE_REF, entityNodeRef);
			versionProperties.put(BeCPGModel.PROP_FROZEN_NODE_DBID,
					nodeService.getProperty(entityNodeRef, ContentModel.PROP_NODE_DBID));
			
			nodeService.addAspect(versionNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION, versionProperties);
			
			updateEffectivity(entityNodeRef, versionNodeRef);					
			
		} finally {
			policyBehaviourFilter.enableBehaviour();
		}

		return versionNodeRef;
	}

	private void updateEffectivity(NodeRef entityNodeRef, NodeRef versionNodeRef) {
		Date newEffectivity = new Date();
		Date oldEffectivity = (Date) nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_START_EFFECTIVITY);
		if(oldEffectivity==null){
			oldEffectivity = newEffectivity;
		}
		nodeService.setProperty(versionNodeRef, BeCPGModel.PROP_START_EFFECTIVITY, oldEffectivity);
		nodeService.setProperty(versionNodeRef, BeCPGModel.PROP_END_EFFECTIVITY, newEffectivity);
		nodeService.setProperty(entityNodeRef, BeCPGModel.PROP_START_EFFECTIVITY, newEffectivity);
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
	private List<NodeRef> buildVersionHistory(NodeRef versionHistoryRef, NodeRef nodeRef) {

		List<ChildAssociationRef> versionAssocs = getVersionAssocs(versionHistoryRef, true);
		List<NodeRef> versionRefs = new ArrayList<NodeRef>();

		for (ChildAssociationRef versionAssoc : versionAssocs) {

			versionRefs.add(versionAssoc.getChildRef());
		}

		// sort versions by node id
		Collections.sort(versionRefs, new Comparator<NodeRef>() {

			@Override
			public int compare(NodeRef v1, NodeRef v2) {
				Date modifiedDateV1 = (Date) nodeService.getProperty(v1, BeCPGModel.PROP_FROZEN_MODIFIED);
				Date modifiedDateV2 = (Date) nodeService.getProperty(v2, BeCPGModel.PROP_FROZEN_MODIFIED);
				int result = modifiedDateV1.compareTo(modifiedDateV2);
				if (result == 0) {
					Long dbid1 = (Long) nodeService.getProperty(v1, ContentModel.PROP_NODE_DBID);
					Long dbid2 = (Long) nodeService.getProperty(v2, ContentModel.PROP_NODE_DBID);

					if (dbid1 != null && dbid2 != null) {
						result = dbid1.compareTo(dbid2);
					} else {
						result = 0;

						if (logger.isWarnEnabled()) {
							logger.warn("node-dbid property is missing for versions: " + v1.toString() + " or "
									+ v2.toString());
						}
					}
				}
				return result;
			}

		});

		return versionRefs;

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
	 * Gets current version of the passed node ref
	 * 
	 * This uses the version label as a mechanism for looking up the version
	 * node.
	 * 
	 * @param versionHistoryRef
	 *            the version history ref
	 * @param nodeRef
	 *            the node ref
	 * @return the current version impl
	 */
	private Pair<Boolean, VersionNumber> getCurrentVersionImpl(NodeRef versionHistoryRef, NodeRef nodeRef) {
		Pair<Boolean, VersionNumber> result = null;

		String versionLabel = (String) this.nodeService.getProperty(nodeRef, BeCPGModel.PROP_VERSION_LABEL);

		// note: resultant list is ordered by (a) explicit index and (b)
		// association creation time
		List<ChildAssociationRef> versionAssocs = getVersionAssocs(versionHistoryRef, false);

		// Current version should be head version (since no branching)
		int cnt = versionAssocs.size();
		for (int i = cnt; i > 0; i--) {
			ChildAssociationRef versionAssoc = versionAssocs.get(i - 1);

			String tempLabel = (String) nodeService.getProperty(versionAssoc.getChildRef(),
					BeCPGModel.PROP_VERSION_LABEL);
			
			if (tempLabel != null && tempLabel.equals(versionLabel) == true) {
				boolean headVersion = (i == cnt);

				if (!headVersion) {
					if (logger.isDebugEnabled()) {
						logger.debug("Unexpected: current version does not appear to be 1st version in the list  ["
								+ versionHistoryRef + ", " + nodeRef + "]");
					}
				}

				result = new Pair<Boolean, VersionNumber>(headVersion, new VersionNumber(versionLabel));
				break;
			}
		}

		return result;
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

}