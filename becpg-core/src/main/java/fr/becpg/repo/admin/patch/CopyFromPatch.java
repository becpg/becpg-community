package fr.becpg.repo.admin.patch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;

/**
 * Remove copy from assoc
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class CopyFromPatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(CopyFromPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.copyFromPatch.result";


	private BehaviourFilter policyBehaviourFilter;
	private RuleService ruleService;
	private DictionaryService dictionaryService;

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

		Set<QName> mapType = new HashSet<>();
		for (QName type : dictionaryService.getAllTypes()) {
			TypeDefinition typeDef = dictionaryService.getType(type);
			if (dictionaryService.isSubClass(typeDef.getName(), BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
				mapType.add(type);
			}
		}
		for (QName type : mapType) {
			doForType(type);
		}

		return I18NUtil.getMessage(MSG_SUCCESS);
	}

	private void doForType(QName type) {

		logger.info("Run patch for type: " + type);

		BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<>() {
			final List<NodeRef> result = new ArrayList<>();

			final long maxNodeId = nodeDAO.getMaxNodeId();

			long minSearchNodeId = 0;
			long maxSearchNodeId = INC;

			final Pair<Long, QName> val = qnameDAO.getQName(type);

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
						List<Long> nodeids = patchDAO.getNodesByTypeQNameId(typeQNameId, minSearchNodeId, maxSearchNodeId);

						for (Long nodeid : nodeids) {
							NodeRef.Status status = nodeDAO.getNodeIdStatus(nodeid);
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

		BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>("CopyFromPatch", transactionService.getRetryingTransactionHelper(),
				workProvider, BATCH_THREADS, BATCH_SIZE, applicationEventPublisher, logger, 10000);

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
				if (nodeService.exists(dataListNodeRef) && nodeService.hasAspect(dataListNodeRef, ContentModel.ASPECT_COPIEDFROM)) {
					AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
					policyBehaviourFilter.disableBehaviour();

					// Link the new node to the original, but ensure that we
					// only keep track of the last copy
					List<AssociationRef> originalAssocs = nodeService.getTargetAssocs(dataListNodeRef, ContentModel.ASSOC_ORIGINAL);
					for (AssociationRef originalAssoc : originalAssocs) {
						nodeService.removeAssociation(originalAssoc.getSourceRef(), originalAssoc.getTargetRef(), ContentModel.ASSOC_ORIGINAL);
					}

					nodeService.removeAspect(dataListNodeRef, ContentModel.ASPECT_COPIEDFROM);

				}
				ruleService.enableRules();
			}

		};

		// Now set the batch processor to work

		batchProcessor.processLong(worker, true);

	}


	/**
	 * <p>Setter for the field <code>policyBehaviourFilter</code>.</p>
	 *
	 * @param policyBehaviourFilter a {@link org.alfresco.repo.policy.BehaviourFilter} object.
	 */
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

}
