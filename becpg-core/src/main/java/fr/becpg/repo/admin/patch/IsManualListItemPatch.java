package fr.becpg.repo.admin.patch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * IsManualListItemPatch
 *
 * @author quere
 * @version $Id: $Id
 */
public class IsManualListItemPatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(IsManualListItemPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.isManualListItemPatch.result";

	private NodeDAO nodeDAO;
	private PatchDAO patchDAO;
	private QNameDAO qnameDAO;
	private BehaviourFilter policyBehaviourFilter;
	private RuleService ruleService;
	private DictionaryService dictionaryService;
	private IntegrityChecker integrityChecker;

	/**
	 * <p>Setter for the field <code>ruleService</code>.</p>
	 *
	 * @param ruleService a {@link org.alfresco.service.cmr.rule.RuleService} object.
	 */
	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}

	/**
	 * <p>Setter for the field <code>dictionaryService</code>.</p>
	 *
	 * @param dictionaryService a {@link org.alfresco.service.cmr.dictionary.DictionaryService} object.
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	/** {@inheritDoc} */
	@Override
	protected String applyInternal() throws Exception {

		HashMap<QName, List<PropertyDefinition>> mapAspect = new HashMap<>();
		for (QName aspect : dictionaryService.getAllAspects()) {
			AspectDefinition aspectDef = dictionaryService.getAspect(aspect);
			if (!aspectDef.getName().equals(ContentModel.ASPECT_AUDITABLE) && !aspectDef.getName().equals(ContentModel.ASPECT_REFERENCEABLE)) {
				List<PropertyDefinition> propDefs = getEnforcedProps(aspectDef.getProperties().values());
				if (!propDefs.isEmpty()) {
					logger.debug("add aspect " + aspect + " propDefs " + propDefs);
					mapAspect.put(aspect, propDefs);
				}
			}
		}
		HashMap<QName, List<PropertyDefinition>> mapType = new HashMap<>();
		for (QName type : dictionaryService.getAllTypes()) {
			TypeDefinition typeDef = dictionaryService.getType(type);
			List<PropertyDefinition> propDefs = getEnforcedProps(typeDef.getProperties().values());
			if (!propDefs.isEmpty()) {
				logger.debug("add type " + type + " propDefs " + propDefs);
				mapType.put(type, propDefs);
			}
		}
		for (QName aspect : mapAspect.keySet()) {
			logger.debug("aspect " + aspect);
			doForAspect(aspect, mapAspect, mapType, true);
		}

		for (QName type : mapType.keySet()) {
			logger.debug("type " + type);
			doForAspect(type, mapAspect, mapType, true);
		}

		patchCurrentUSerSize();

		return I18NUtil.getMessage(MSG_SUCCESS);
	}

	private void patchCurrentUSerSize() {

		BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<>() {
			final List<NodeRef> result = new ArrayList<>();

			final long maxNodeId = getNodeDAO().getMaxNodeId();

			long minSearchNodeId = 0;
			long maxSearchNodeId = INC;

			final Pair<Long, QName> val = getQnameDAO().getQName(ContentModel.TYPE_PERSON);

			@Override
			public int getTotalEstimatedWorkSize() {
				return result.size();
			}

			@Override
			public long getTotalEstimatedWorkSizeLong() {
				return getTotalEstimatedWorkSize();
			}

			@Override
			public Collection<NodeRef> getNextWork() {
				if (val != null) {
					Long typeQNameId = val.getFirst();

					result.clear();

					while (result.isEmpty() && (minSearchNodeId < maxNodeId)) {

						List<Long> nodeids = getPatchDAO().getNodesByTypeQNameId(typeQNameId, minSearchNodeId, maxSearchNodeId);

						for (Long nodeid : nodeids) {
							NodeRef.Status status = getNodeDAO().getNodeIdStatus(nodeid);
							if (!status.isDeleted()) {
								result.add(status.getNodeRef());
							}
						}
						minSearchNodeId = minSearchNodeId + INC;
						maxSearchNodeId = maxSearchNodeId + INC;
					}
				}

				return result;
			}
		};

		BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>("CostParentLevelPatch", transactionService.getRetryingTransactionHelper(),
				workProvider, BATCH_THREADS, BATCH_SIZE, applicationEventPublisher, logger, 1000);

		BatchProcessWorker<NodeRef> worker = new BatchProcessWorker<>() {

			@Override
			public void afterProcess() throws Throwable {
				//Do nothing
			}

			@Override
			public void beforeProcess() throws Throwable {
				//Do nothing
			}

			@Override
			public String getIdentifier(NodeRef entry) {
				return entry.toString();
			}

			@Override
			public void process(NodeRef personNodeRef) throws Throwable {
				ruleService.disableRules();
				if (nodeService.exists(personNodeRef)) {
					if (nodeService.getProperty(personNodeRef, ContentModel.PROP_SIZE_CURRENT) == null) {
						nodeService.setProperty(personNodeRef, ContentModel.PROP_SIZE_CURRENT, 0);
					}
				} else {
					logger.warn("personNodeRef doesn't exist : " + personNodeRef);
				}
				ruleService.enableRules();
			}

		};

		integrityChecker.setEnabled(false);
		try {
			batchProcessor.processLong(worker, true);
		} finally {
			integrityChecker.setEnabled(true);
		}

	}

	private List<PropertyDefinition> getEnforcedProps(Collection<PropertyDefinition> propertyDefs) {
		List<PropertyDefinition> propDefs = new ArrayList<>();
		for (PropertyDefinition propDef : propertyDefs) {
			if (propDef.isMandatoryEnforced() && (propDef.getDefaultValue() != null) && !propDef.getName().equals(ContentModel.PROP_NAME)) {
				propDefs.add(propDef);
			}
		}
		return propDefs;
	}

	private void doForAspect(final QName aspect, final Map<QName, List<PropertyDefinition>> mapAspect,
			final Map<QName, List<PropertyDefinition>> mapTypes, final boolean isAspect) {
		BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<>() {
			final List<NodeRef> result = new ArrayList<>();

			final long maxNodeId = getNodeDAO().getMaxNodeId();

			long minSearchNodeId = 1;
			long maxSearchNodeId = INC;

			final Pair<Long, QName> val = getQnameDAO().getQName(aspect);

			@Override
			public int getTotalEstimatedWorkSize() {
				return result.size();
			}

			@Override
			public long getTotalEstimatedWorkSizeLong() {
				return getTotalEstimatedWorkSize();
			}

			@Override
			public Collection<NodeRef> getNextWork() {
				if (val != null) {
					Long typeQNameId = val.getFirst();

					result.clear();

					while (result.isEmpty() && (minSearchNodeId < maxNodeId)) {
						List<Long> nodeids;
						if (isAspect) {
							nodeids = getPatchDAO().getNodesByAspectQNameId(typeQNameId, minSearchNodeId, maxSearchNodeId);
						} else {
							nodeids = getPatchDAO().getNodesByTypeQNameId(typeQNameId, minSearchNodeId, maxSearchNodeId);
						}

						for (Long nodeid : nodeids) {
							NodeRef.Status status = getNodeDAO().getNodeIdStatus(nodeid);
							if (!status.isDeleted()) {
								result.add(status.getNodeRef());
							}
						}
						minSearchNodeId = minSearchNodeId + INC;
						maxSearchNodeId = maxSearchNodeId + INC;
					}
				}

				return result;
			}
		};

		BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>("IsManualListItemPatch", transactionService.getRetryingTransactionHelper(),
				workProvider, BATCH_THREADS, BATCH_SIZE, applicationEventPublisher, logger, 1000);

		BatchProcessWorker<NodeRef> worker = new BatchProcessWorker<>() {

			@Override
			public void afterProcess() throws Throwable {

				//Do nothing

			}

			@Override
			public void beforeProcess() throws Throwable {

				//Do nothing
			}

			@Override
			public String getIdentifier(NodeRef entry) {
				return entry.toString();
			}

			@Override
			public void process(NodeRef dataListNodeRef) throws Throwable {
				ruleService.disableRules();
				if (nodeService.exists(dataListNodeRef)) {
					AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
					policyBehaviourFilter.disableBehaviour();
					for (QName aspect : mapAspect.keySet()) {
						if (nodeService.hasAspect(dataListNodeRef, aspect)) {
							updatePropertyDefs(dataListNodeRef, mapAspect.get(aspect));
						}
					}

					if (isAspect) {
						QName type = nodeService.getType(dataListNodeRef);
						if (mapTypes.containsKey(type)) {
							updatePropertyDefs(dataListNodeRef, mapTypes.get(type));
						}
					}

				} else {
					logger.warn("dataListNodeRef doesn't exist : " + dataListNodeRef);
				}
				ruleService.enableRules();
			}

		};

		// Now set the batch processor to work

		integrityChecker.setEnabled(false);
		try {
			batchProcessor.processLong(worker, true);
		} finally {
			integrityChecker.setEnabled(true);
		}
	}

	private void updatePropertyDefs(NodeRef dataListNodeRef, Collection<PropertyDefinition> propertyDefs) {
		for (PropertyDefinition propDef : propertyDefs) {
			//logger.info("propDef " + propDef.getName());
			if (nodeService.getProperty(dataListNodeRef, propDef.getName()) == null) {
				nodeService.setProperty(dataListNodeRef, propDef.getName(), propDef.getDefaultValue());
				logger.debug("set enforced prop " + propDef.getName() + " default value " + propDef.getDefaultValue());
			}
		}
	}

	/**
	 * <p>Getter for the field <code>nodeDAO</code>.</p>
	 *
	 * @return a {@link org.alfresco.repo.domain.node.NodeDAO} object.
	 */
	public NodeDAO getNodeDAO() {
		return nodeDAO;
	}

	/**
	 * <p>Setter for the field <code>nodeDAO</code>.</p>
	 *
	 * @param nodeDAO a {@link org.alfresco.repo.domain.node.NodeDAO} object.
	 */
	public void setNodeDAO(NodeDAO nodeDAO) {
		this.nodeDAO = nodeDAO;
	}

	/**
	 * <p>Getter for the field <code>patchDAO</code>.</p>
	 *
	 * @return a {@link org.alfresco.repo.domain.patch.PatchDAO} object.
	 */
	public PatchDAO getPatchDAO() {
		return patchDAO;
	}

	/**
	 * <p>Setter for the field <code>patchDAO</code>.</p>
	 *
	 * @param patchDAO a {@link org.alfresco.repo.domain.patch.PatchDAO} object.
	 */
	public void setPatchDAO(PatchDAO patchDAO) {
		this.patchDAO = patchDAO;
	}

	/**
	 * <p>Getter for the field <code>qnameDAO</code>.</p>
	 *
	 * @return a {@link org.alfresco.repo.domain.qname.QNameDAO} object.
	 */
	public QNameDAO getQnameDAO() {
		return qnameDAO;
	}

	/**
	 * <p>Setter for the field <code>qnameDAO</code>.</p>
	 *
	 * @param qnameDAO a {@link org.alfresco.repo.domain.qname.QNameDAO} object.
	 */
	public void setQnameDAO(QNameDAO qnameDAO) {
		this.qnameDAO = qnameDAO;
	}

	/**
	 * <p>Setter for the field <code>policyBehaviourFilter</code>.</p>
	 *
	 * @param policyBehaviourFilter a {@link org.alfresco.repo.policy.BehaviourFilter} object.
	 */
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	/**
	 * <p>Getter for the field <code>integrityChecker</code>.</p>
	 *
	 * @return a {@link org.alfresco.repo.node.integrity.IntegrityChecker} object.
	 */
	public IntegrityChecker getIntegrityChecker() {
		return integrityChecker;
	}

	/**
	 * <p>Setter for the field <code>integrityChecker</code>.</p>
	 *
	 * @param integrityChecker a {@link org.alfresco.repo.node.integrity.IntegrityChecker} object.
	 */
	public void setIntegrityChecker(IntegrityChecker integrityChecker) {
		this.integrityChecker = integrityChecker;
	}

}
