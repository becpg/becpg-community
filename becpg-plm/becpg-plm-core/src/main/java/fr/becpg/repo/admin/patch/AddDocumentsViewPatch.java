package fr.becpg.repo.admin.patch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityTplService;

/**
 * Add document view
 *
 * @author matthieu
 *
 */
public class AddDocumentsViewPatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(AddDocumentsViewPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.plm.addDocumentsViewPatch.result";

	private NodeDAO nodeDAO;
	private PatchDAO patchDAO;
	private QNameDAO qnameDAO;
	private RuleService ruleService;
	private EntityTplService entityTplService;

	public void setEntityTplService(EntityTplService entityTplService) {
		this.entityTplService = entityTplService;
	}

	private final int batchThreads = 3;
	private final int batchSize = 40;
	private final long count = 10000;

	@Override
	protected String applyInternal() throws Exception {

		AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

		doForType(PLMModel.ASPECT_PRODUCT, true);
		doForType(PLMModel.TYPE_CLIENT, false);
		doForType(PLMModel.TYPE_SUPPLIER, false);
		doForType(ProjectModel.TYPE_PROJECT, false);

		return I18NUtil.getMessage(MSG_SUCCESS);
	}

	private void doForType(final QName type, boolean isAspect) {
		BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<NodeRef>() {
			final List<NodeRef> result = new ArrayList<>();

			final long maxNodeId = getPatchDAO().getMaxAdmNodeID();

			long minSearchNodeId = 0;
			long maxSearchNodeId = count;

			final Pair<Long, QName> val = getQnameDAO().getQName(type);

			@Override
			public int getTotalEstimatedWorkSize() {
				return result.size();
			}

			@Override
			public Collection<NodeRef> getNextWork() {
				if (val != null) {
					Long typeQNameId = val.getFirst();

					result.clear();

					while (result.isEmpty() && (minSearchNodeId < maxNodeId)) {

						List<Long> nodeids = null;

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
						minSearchNodeId = minSearchNodeId + count;
						maxSearchNodeId = maxSearchNodeId + count;
					}
				}

				return result;
			}
		};

		BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>("AddDocumentsViewPatch", transactionService.getRetryingTransactionHelper(),
				workProvider, batchThreads, batchSize, applicationEventPublisher, logger, 1000);

		BatchProcessWorker<NodeRef> worker = new BatchProcessWorker<NodeRef>() {

			@Override
			public void afterProcess() throws Throwable {
				ruleService.enableRules();
			}

			@Override
			public void beforeProcess() throws Throwable {
				ruleService.disableRules();
			}

			@Override
			public String getIdentifier(NodeRef entry) {
				return entry.toString();
			}

			@Override
			public void process(NodeRef entityNodeRef) throws Throwable {

				AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

				if (nodeService.exists(entityNodeRef)) {
					if (nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)
							|| nodeService.getTargetAssocs(entityNodeRef, BeCPGModel.ASSOC_ENTITY_TPL_REF).isEmpty()) {
						logger.debug("Create views on entity " + entityNodeRef);
						entityTplService.createView(entityNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_DOCUMENTS);
					}
				} else {
					logger.warn("entityNodeRef doesn't exist : " + entityNodeRef);
				}

			}

		};

		batchProcessor.process(worker, true);

	}

	public NodeDAO getNodeDAO() {
		return nodeDAO;
	}

	public void setNodeDAO(NodeDAO nodeDAO) {
		this.nodeDAO = nodeDAO;
	}

	public PatchDAO getPatchDAO() {
		return patchDAO;
	}

	public void setPatchDAO(PatchDAO patchDAO) {
		this.patchDAO = patchDAO;
	}

	public QNameDAO getQnameDAO() {
		return qnameDAO;
	}

	public void setQnameDAO(QNameDAO qnameDAO) {
		this.qnameDAO = qnameDAO;
	}

	public RuleService getRuleService() {
		return ruleService;
	}

	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}

}
