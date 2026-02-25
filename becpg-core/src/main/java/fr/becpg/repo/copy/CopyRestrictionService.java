package fr.becpg.repo.copy;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.jscript.BeCPGStateHelper;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * <p>CopyRestrictionService class.</p>
 *
 * @author matthieu
 */
@Service
public class CopyRestrictionService {

	private static final String COPY_SUFFIX = "|copy";

	private static final String BRANCH_SUFFIX = "|branch";

	private static final String BRANCH_ONLY_SUFFIX = "|branchOnly";

	private static final Logger logger = LoggerFactory.getLogger(CopyRestrictionService.class);

	@Autowired
	private CopyRestrictionPlugin[] copyRestrictionPlugins;

	@Autowired
	private SystemConfigurationService systemConfigurationService;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Autowired
	private EntityService entityService;
	
	@Autowired
	private NamespaceService namespaceService;
	
	@Autowired
	private AssociationService associationService;
	
	@Autowired
	private NodeService nodeService;

	private List<String> typesToReset() {
		return systemConfigurationService.listValue("beCPG.copyOrBranch.typesToReset");
	}
	
	private List<String> propertiesToReset() {
		return systemConfigurationService.listValue("beCPG.copyOrBranch.propertiesToReset");
	}

	/**
	 * <p>shouldCopyNodeRef.</p>
	 *
	 * @param classRef a {@link org.alfresco.service.namespace.QName} object
	 * @param sourceNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param targetNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a boolean
	 */
	public boolean shouldCopyNodeRef(QName classRef, NodeRef sourceNodeRef, NodeRef targetNodeRef) {
		String nodeRefType = entityDictionaryService.toPrefixString(classRef);
		for (String typeToReset : typesToReset()) {
			if (typeToReset.equals(nodeRefType)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Denying copy for node {} of type {}", sourceNodeRef, nodeRefType);
				}
				return false;
			}
			if (!shouldCopyFromPlugins(classRef, sourceNodeRef, targetNodeRef, typeToReset)) {
				return false;
			}
			if (typeToReset.endsWith(BRANCH_SUFFIX) || typeToReset.endsWith(BRANCH_ONLY_SUFFIX)) {
				String type = typeToReset.replace(BRANCH_ONLY_SUFFIX, "").replace(BRANCH_SUFFIX, "");
				NodeRef entityNodeRef = entityService.getEntityNodeRef(sourceNodeRef, classRef);
				if (BeCPGStateHelper.isOnBranchEntity(entityNodeRef) 
						&& !shouldCopy(classRef, type, nodeRefType, sourceNodeRef, targetNodeRef)) {
					return false;
				}
			} else if (typeToReset.endsWith(COPY_SUFFIX)) {
				String type = typeToReset.replace(COPY_SUFFIX, "");
				NodeRef entityNodeRef = entityService.getEntityNodeRef(sourceNodeRef, classRef);
				if (!BeCPGStateHelper.isOnBranchEntity(entityNodeRef) && !BeCPGStateHelper.isOnMergeEntity(entityNodeRef)
						&& !shouldCopy(classRef, type, nodeRefType, sourceNodeRef, targetNodeRef)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * <p>handlePropertiesToReset.</p>
	 *
	 * @param classRef a {@link org.alfresco.service.namespace.QName} object
	 * @param sourceNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param targetNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public void handlePropertiesToReset(QName classRef, NodeRef sourceNodeRef, NodeRef targetNodeRef) {
		CopyContext context = new CopyContext(classRef, sourceNodeRef, targetNodeRef);
		for (String propertyToReset : propertiesToReset()) {
			handleResetProperty(context, propertyToReset);
		}
	}

	private boolean shouldCopyFromPlugins(QName sourceClassQName, NodeRef sourceNodeRef, NodeRef targetNodeRef, String type) {
		for (CopyRestrictionPlugin plugin : copyRestrictionPlugins) {
			if (!plugin.shouldCopy(sourceClassQName, sourceNodeRef, targetNodeRef, type)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Denying copy for node {} of type {} from plugin {}", sourceNodeRef, type,
							plugin.getClass().getSimpleName());
				}
				return false;
			}
		}
		return true;
	}
	
	private boolean shouldCopy(QName classRef, String type, String nodeRefType, NodeRef sourceNodeRef, NodeRef targetNodeRef) {
		if (nodeRefType.equals(type)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Denying copy for node {} of type {}", sourceNodeRef, nodeRefType);
			}
			return false;
		}
		if (!shouldCopyFromPlugins(classRef, sourceNodeRef, targetNodeRef, type)) {
			return false;
		}
		return true;
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
		
		if ((isProp && nodeService.getProperty(context.getTargetRef(), propQName) == null)) {
			return;
		}
		if ((isAssoc && associationService.getTargetAssocs(context.getTargetRef(), propQName).isEmpty())) {
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
