package fr.becpg.repo.admin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import fr.becpg.repo.entity.EntityDictionaryService;

/**
 * <p>AssociationIndexerService class.</p>
 *
 * @author matthieu
 */
@Service
public class AssociationIndexerService {

	private static final Log logger = LogFactory.getLog(AssociationIndexerService.class);

	/** Constant <code>BATCH_THREADS=4</code> */
	protected static final int BATCH_THREADS = 4;
	/** Constant <code>BATCH_SIZE=50</code> */
	protected static final int BATCH_SIZE = 50;
	/** Constant <code>INC=BATCH_THREADS * BATCH_SIZE * 1L</code> */
	protected static final long INC = BATCH_THREADS * BATCH_SIZE * 1L;

	@Autowired
	private BehaviourFilter policyBehaviourFilter;
	@Autowired
	private RuleService ruleService;
	@Autowired
	private IntegrityChecker integrityChecker;
	@Autowired
	private EntityDictionaryService entityDictionaryService;
	@Autowired
	private NodeDAO nodeDAO;
	@Autowired
	private PatchDAO patchDAO;
	@Autowired
	private QNameDAO qnameDAO;
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	@Autowired
	private TransactionService transactionService;
	@Autowired
	private NodeService nodeService;

	/**
	 * <p>reindexAssocs.</p>
	 *
	 * @param sourceName a {@link org.alfresco.service.namespace.QName} object
	 * @param assocQName a {@link org.alfresco.service.namespace.QName} object
	 */
	public void reindexAssocs(QName sourceName, QName assocQName) {
		if (entityDictionaryService.getAssocIndexQName(assocQName) == null) {
			logger.error("indexQName does not exist for assoc: " + assocQName);
		}
		if (entityDictionaryService.getType(sourceName) != null) {
			applyToType(sourceName, assocQName);
			for (QName subType : entityDictionaryService.getSubTypes(sourceName, true)) {
				applyToType(subType, assocQName);
			}
		} else if (entityDictionaryService.getAspect(sourceName) != null) {
			logger.info("reindex assoc for: " + sourceName);
			BatchProcessor<NodeRef> batchProcessor = createBatchAspectProcessor(sourceName);
			integrityChecker.setEnabled(false);
			try {
				batchProcessor.processLong(getPatchWorker(assocQName), true);
			} finally {
				integrityChecker.setEnabled(true);
			}
		} else {
			throw new IllegalStateException("sourceName does not exist: " + sourceName);
		}
	}

	private void applyToType(QName toApplyType, QName assocQName) {
		logger.info("reindex assoc for: " + toApplyType);
		BatchProcessor<NodeRef> batchProcessor = createBatchTypeProcessor(toApplyType);
		integrityChecker.setEnabled(false);
		try {
			batchProcessor.processLong(getPatchWorker(assocQName), true);
		} finally {
			integrityChecker.setEnabled(true);
		}
	}

	/**
	 * <p>createBatchTypeProcessor.</p>
	 *
	 * @param type a {@link org.alfresco.service.namespace.QName} object
	 * @return a {@link org.alfresco.repo.batch.BatchProcessor} object
	 */
	protected BatchProcessor<NodeRef> createBatchTypeProcessor(QName type) {
		BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<>() {
			private final long maxNodeId = nodeDAO.getMaxNodeId();
			private final Pair<Long, QName> typeQNamePair = qnameDAO.getQName(type);
			private final List<NodeRef> result = new ArrayList<>();
			private long minSearchNodeId = 0;
			private long maxSearchNodeId = BATCH_SIZE;

			@Override
			public Collection<NodeRef> getNextWork() {
				result.clear();

				if (typeQNamePair == null) {
					return result;
				}

				Long typeQNameId = typeQNamePair.getFirst();

				while (result.isEmpty() && (minSearchNodeId < maxNodeId)) {
					List<Long> nodeIds = patchDAO.getNodesByTypeQNameId(typeQNameId, minSearchNodeId, maxSearchNodeId);
					result.addAll(nodeIds.stream().map(nodeDAO::getNodeIdStatus).filter(status -> !status.isDeleted()).map(NodeRef.Status::getNodeRef)
							.filter(Objects::nonNull).toList());
					minSearchNodeId += BATCH_SIZE;
					maxSearchNodeId += BATCH_SIZE;
				}

				return result;
			}

			@Override
			public int getTotalEstimatedWorkSize() {
				return result.size();
			}

			@Override
			public long getTotalEstimatedWorkSizeLong() {
				return getTotalEstimatedWorkSize();
			}
		};

		return new BatchProcessor<>(getClass().getSimpleName(), transactionService.getRetryingTransactionHelper(), workProvider, BATCH_THREADS,
				BATCH_SIZE, applicationEventPublisher, logger, 500);
	}

	/**
	 * <p>createBatchAspectProcessor.</p>
	 *
	 * @param type a {@link org.alfresco.service.namespace.QName} object
	 * @return a {@link org.alfresco.repo.batch.BatchProcessor} object
	 */
	protected BatchProcessor<NodeRef> createBatchAspectProcessor(QName type) {
		BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<>() {
			private final long maxNodeId = nodeDAO.getMaxNodeId();
			private final Pair<Long, QName> typeQNamePair = qnameDAO.getQName(type);
			private final List<NodeRef> result = new ArrayList<>();
			private long minSearchNodeId = 0;
			private long maxSearchNodeId = BATCH_SIZE;

			@Override
			public Collection<NodeRef> getNextWork() {
				result.clear();

				if (typeQNamePair == null) {
					return result;
				}

				Long typeQNameId = typeQNamePair.getFirst();

				while (result.isEmpty() && (minSearchNodeId < maxNodeId)) {
					List<Long> nodeIds = patchDAO.getNodesByAspectQNameId(typeQNameId, minSearchNodeId, maxSearchNodeId);
					result.addAll(nodeIds.stream().map(nodeDAO::getNodeIdStatus).filter(status -> !status.isDeleted()).map(NodeRef.Status::getNodeRef)
							.filter(Objects::nonNull).toList());
					minSearchNodeId += BATCH_SIZE;
					maxSearchNodeId += BATCH_SIZE;
				}

				return result;
			}

			@Override
			public int getTotalEstimatedWorkSize() {
				return result.size();
			}

			@Override
			public long getTotalEstimatedWorkSizeLong() {
				return getTotalEstimatedWorkSize();
			}
		};

		return new BatchProcessor<>(getClass().getSimpleName(), transactionService.getRetryingTransactionHelper(), workProvider, BATCH_THREADS,
				BATCH_SIZE, applicationEventPublisher, logger, 500);
	}

	private BatchProcessWorker<NodeRef> getPatchWorker(QName assocQName) {
		return new BatchProcessWorker<NodeRef>() {
			public void afterProcess() throws Throwable {
				//Do Nothing
			}

			public void beforeProcess() throws Throwable {
				//Do Nothing
			}

			public String getIdentifier(NodeRef entry) {
				return entry.toString();
			}

			@SuppressWarnings("unchecked")
			public void process(NodeRef nodeRef) throws Throwable {
				ruleService.disableRules();
				if (nodeService.exists(nodeRef)) {
					AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
					policyBehaviourFilter.disableBehaviour();
					QName indexQName = entityDictionaryService.getAssocIndexQName(assocQName);
					for (AssociationRef assoc : nodeService.getTargetAssocs(nodeRef, assocQName)) {
						ArrayList<NodeRef> values = (ArrayList<NodeRef>) nodeService.getProperty(nodeRef, indexQName);
						if (values == null) {
							values = new ArrayList<>();
						}
						if (!values.contains(assoc.getTargetRef())) {
							values.add(assoc.getTargetRef());
						}
						nodeService.setProperty(nodeRef, indexQName, values);
					}
				} else {
					logger.warn("nodeRef doesn't exist : " + nodeRef);
				}
				ruleService.enableRules();
			}
		};
	}
}
