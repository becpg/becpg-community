package fr.becpg.repo.copy;

import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.jscript.BeCPGStateHelper;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * <p>BeCPGCopyPolicy class.</p>
 *
 * @author matthieu
 */
public class BeCPGCopyPolicy extends AbstractBeCPGPolicy implements CopyServicePolicies.OnCopyCompletePolicy {

	private static final String COPY_SUFFIX = "|copy";

	private static final String BRANCH_SUFFIX = "|branch";

	private static final String BRANCH_ONLY_SUFFIX = "|branchOnly";

	private static final Logger logger = LoggerFactory.getLogger(BeCPGCopyPolicy.class);

	private BeCPGCopyPlugin[] copyPlugins;

	private SystemConfigurationService systemConfigurationService;

	private EntityDictionaryService entityDictionaryService;

	private EntityService entityService;

	private NamespaceService namespaceService;

	private AssociationService associationService;

	/**
	 * <p>Setter for the field <code>copyPlugins</code>.</p>
	 *
	 * @param copyPlugins an array of {@link fr.becpg.repo.copy.BeCPGCopyPlugin} objects
	 */
	public void setCopyPlugins(BeCPGCopyPlugin[] copyPlugins) {
		this.copyPlugins = copyPlugins;
	}

	/**
	 * <p>Setter for the field <code>systemConfigurationService</code>.</p>
	 *
	 * @param systemConfigurationService a {@link fr.becpg.repo.system.SystemConfigurationService} object
	 */
	public void setSystemConfigurationService(SystemConfigurationService systemConfigurationService) {
		this.systemConfigurationService = systemConfigurationService;
	}

	/**
	 * <p>Setter for the field <code>entityDictionaryService</code>.</p>
	 *
	 * @param entityDictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object
	 */
	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	/**
	 * <p>Setter for the field <code>entityService</code>.</p>
	 *
	 * @param entityService a {@link fr.becpg.repo.entity.EntityService} object
	 */
	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	private List<String> typesToReset() {
		return systemConfigurationService.listValue("beCPG.copyOrBranch.typesToReset");
	}

	private List<String> propertiesToReset() {
		return systemConfigurationService.listValue("beCPG.copyOrBranch.propertiesToReset");
	}

	/** {@inheritDoc} */
	@Override
	public void doInit() {
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME, ContentModel.TYPE_CMOBJECT,
				new JavaBehaviour(this, "getCopyCallback"));
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyCompletePolicy.QNAME, ContentModel.TYPE_CMOBJECT,
				new JavaBehaviour(this, "onCopyComplete"));
	}

	/** {@inheritDoc} */
	@Override
	public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails) {
		return new DefaultCopyBehaviourCallback() {
			@Override
			public boolean getMustCopy(QName classQName, CopyDetails currentCopyDetails) {
				String nodeRefType = entityDictionaryService.toPrefixString(classQName);
				for (String typeToReset : typesToReset()) {
					if (typeToReset.equals(nodeRefType)) {
						if (logger.isDebugEnabled()) {
							logger.debug("Denying copy for node {} of type {}", currentCopyDetails.getSourceNodeRef(), nodeRefType);
						}
						return false;
					}
					if (!shouldCopyFromPlugins(typeToReset, currentCopyDetails)) {
						return false;
					}
					if (typeToReset.endsWith(BRANCH_SUFFIX) || typeToReset.endsWith(BRANCH_ONLY_SUFFIX)) {
						typeToReset = typeToReset.replace(BRANCH_ONLY_SUFFIX, "").replace(BRANCH_SUFFIX, "");
						NodeRef entityNodeRef = entityService.getEntityNodeRef(currentCopyDetails.getSourceNodeRef(), classQName);
						if (entityNodeRef != null && BeCPGStateHelper.isOnBranchEntity(entityNodeRef)
								&& !shouldCopy(typeToReset, nodeRefType, currentCopyDetails)) {
							return false;
						}
					} else if (typeToReset.endsWith(COPY_SUFFIX)) {
						typeToReset = typeToReset.replace(COPY_SUFFIX, "");
						NodeRef targetNodeRef = currentCopyDetails.isTargetNodeIsNew() ? currentCopyDetails.getTargetParentNodeRef() : currentCopyDetails.getTargetNodeRef();
						NodeRef entityNodeRef = entityService.getEntityNodeRef(targetNodeRef, null);
						if (entityNodeRef != null && BeCPGStateHelper.isOnCopyEntity(entityNodeRef)
								&& !shouldCopy(typeToReset, nodeRefType, currentCopyDetails)) {
							return false;
						}
					}
				}
				return true;
			}
		};
	}

	/** {@inheritDoc} */
	@Override
	public void onCopyComplete(QName classRef, NodeRef sourceNodeRef, NodeRef targetNodeRef, boolean copyToNewNode, Map<NodeRef, NodeRef> copyMap) {
		CopyContext context = new CopyContext(classRef, sourceNodeRef, targetNodeRef);
		for (String propertyToReset : propertiesToReset()) {
			handleResetProperty(context, propertyToReset);
		}
	}

	private boolean shouldCopyFromPlugins(String typeToReset, CopyDetails copyDetails) {
		if (copyPlugins == null) {
			return true;
		}
		for (BeCPGCopyPlugin plugin : copyPlugins) {
			if (!plugin.shouldCopy(typeToReset, copyDetails)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Denying copy for node {} of typeToReset {} from plugin {}", copyDetails.getSourceNodeRef(), typeToReset,
							plugin.getClass().getSimpleName());
				}
				return false;
			}
		}
		return true;
	}

	private boolean shouldCopy(String typeToReset, String nodeRefType, CopyDetails copyDetails) {
		if (nodeRefType.equals(typeToReset)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Denying copy for node {} of typeToReset {}", copyDetails.getSourceNodeRef(), nodeRefType);
			}
			return false;
		}
		return shouldCopyFromPlugins(typeToReset, copyDetails);
	}

	private void handleResetProperty(CopyContext context, String propertyToReset) {
		String propertyName = extractPropertyName(propertyToReset, context);
		if (propertyName == null) {
			return;
		}

		boolean onlyDuringBranch = false;
		boolean onlyDuringBranchOrMerge = false;
		boolean onlyDuringCopy = false;
		if (propertyName.endsWith(BRANCH_SUFFIX)) {
			propertyName = propertyName.replace(BRANCH_SUFFIX, "");
			onlyDuringBranchOrMerge = true;
		} else if (propertyName.endsWith(BRANCH_ONLY_SUFFIX)) {
			propertyName = propertyName.replace(BRANCH_ONLY_SUFFIX, "");
			onlyDuringBranch = true;
		} else if (propertyName.endsWith(COPY_SUFFIX)) {
			propertyName = propertyName.replace(COPY_SUFFIX, "");
			onlyDuringCopy = true;
		}

		QName propQName = QName.createQName(propertyName, namespaceService);
		boolean isProp = entityDictionaryService.getProperty(propQName) != null;
		boolean isAssoc = entityDictionaryService.getAssociation(propQName) != null;
		if (!isProp && !isAssoc) {
			logger.warn("Property or association {} configured in 'propertiesToReset' does not exist", propQName);
			return;
		}

		if (isProp && nodeService.getProperty(context.getTargetRef(), propQName) == null) {
			return;
		}
		if (isAssoc && associationService.getTargetAssocs(context.getTargetRef(), propQName).isEmpty()) {
			return;
		}

		if (onlyDuringBranch && !BeCPGStateHelper.isOnBranchEntity(context.getEntitySourceNodeRef())) {
			return;
		}
		if (onlyDuringCopy && !BeCPGStateHelper.isOnCopyEntity(context.getEntityTargetNodeRef())) {
			return;
		}
		if (onlyDuringBranchOrMerge && BeCPGStateHelper.isOnCopyEntity(context.getEntityTargetNodeRef())) {
			return;
		}

		if (!BeCPGStateHelper.isOnMergeEntity(context.getEntitySourceNodeRef())) {
			if (isProp) {
				if (logger.isDebugEnabled()) {
					logger.debug("Resetting property {} on copied node {}", propQName, context.getTargetRef());
				}
				if (nodeService.getProperty(context.getTargetRef(), propQName) != null) {
					nodeService.setProperty(context.getTargetRef(), propQName, null);
				}
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("Resetting association {} on copied node {}", propQName, context.getTargetRef());
				}
				associationService.update(context.getTargetRef(), propQName, List.of());
			}
		}
	}

	private String extractPropertyName(String propertyToReset, CopyContext context) {
		if (propertyToReset.contains("|")) {
			String[] split = propertyToReset.split("\\|");
			boolean isNested = split.length > 2 || (!propertyToReset.endsWith(BRANCH_SUFFIX) && !propertyToReset.endsWith(BRANCH_ONLY_SUFFIX)
					&& !propertyToReset.endsWith(COPY_SUFFIX));
			if (isNested) {
				String requiredType = split[0];
				if (!requiredType.equals(context.getNodeShortType())) {
					return null;
				}
				return propertyToReset.substring(requiredType.length() + 1);
			}
		} else if (!context.isEntity()) {
			return null;
		}
		return propertyToReset;
	}

	private class CopyContext {
		private QName sourceClassQName;
		private NodeRef sourceRef;
		private NodeRef targetRef;
		private NodeRef entitySourceNodeRef;
		private NodeRef entityTargetNodeRef;
		private Boolean isEntity;
		private String shortType;

		CopyContext(QName sourceClassQName, NodeRef sourceRef, NodeRef targetRef) {
			this.sourceClassQName = sourceClassQName;
			this.sourceRef = sourceRef;
			this.targetRef = targetRef;
		}

		NodeRef getTargetRef() {
			return targetRef;
		}

		NodeRef getEntitySourceNodeRef() {
			if (entitySourceNodeRef == null) {
				if (isEntity()) {
					entitySourceNodeRef = sourceRef;
				} else {
					entitySourceNodeRef = entityService.getEntityNodeRef(sourceRef, sourceClassQName);
				}
			}
			return entitySourceNodeRef;
		}

		NodeRef getEntityTargetNodeRef() {
			if (entityTargetNodeRef == null) {
				if (isEntity()) {
					entityTargetNodeRef = targetRef;
				} else {
					entityTargetNodeRef = entityService.getEntityNodeRef(targetRef, sourceClassQName);
				}
			}
			return entityTargetNodeRef;
		}

		boolean isEntity() {
			if (isEntity == null) {
				isEntity = entityDictionaryService.isSubClass(sourceClassQName, BeCPGModel.TYPE_ENTITY_V2);
			}
			return isEntity;
		}

		String getNodeShortType() {
			if (shortType == null) {
				shortType = entityDictionaryService.toPrefixString(sourceClassQName);
			}
			return shortType;
		}
	}

}
