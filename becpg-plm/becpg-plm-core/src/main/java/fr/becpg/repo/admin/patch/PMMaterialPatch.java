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
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.search.impl.AbstractBeCPGQueryBuilder;

/**
 * Copy prop value of cm:name in bcpg:lvValue to support mlText
 *
 * @author matthieu
 *
 */
public class PMMaterialPatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(PMMaterialPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.pmMaterialPatch.result";

	private static final QName PM_MATERIAL = QName.createQName(PackModel.PACK_URI, "pmMaterial");

	private NodeDAO nodeDAO;
	private PatchDAO patchDAO;
	private QNameDAO qnameDAO;
	private BehaviourFilter policyBehaviourFilter;
	private RuleService ruleService;
	private AssociationService associationService;

	private final int batchThreads = 3;
	private final int batchSize = 40;
	private final long count = batchThreads * batchSize;

	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}

	@Override
	protected String applyInternal() throws Exception {

		BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<NodeRef>() {
			final List<NodeRef> result = new ArrayList<>();

			final long maxNodeId = getPatchDAO().getMaxAdmNodeID();

			long minSearchNodeId = 0;

			final Pair<Long, QName> val = getQnameDAO().getQName(PackModel.ASPECT_PM_MATERIAL);

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
						List<Long> nodeids = getPatchDAO().getNodesByAspectQNameId(typeQNameId, minSearchNodeId, minSearchNodeId + count);

						for (Long nodeid : nodeids) {
							NodeRef.Status status = getNodeDAO().getNodeIdStatus(nodeid);
							if (!status.isDeleted()) {
								result.add(status.getNodeRef());
							}
						}
						minSearchNodeId = minSearchNodeId + count;
					}
				}

				return result;
			}
		};

		BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>("IngTypePatch", transactionService.getRetryingTransactionHelper(), workProvider,
				batchThreads, batchSize, applicationEventPublisher, logger, 500);

		BatchProcessWorker<NodeRef> worker = new BatchProcessWorker<NodeRef>() {

			List<NodeRef> materials = new ArrayList<>();

			@Override
			public void afterProcess() throws Throwable {
				ruleService.enableRules();

			}

			@Override
			public void beforeProcess() throws Throwable {

				materials = BeCPGQueryBuilder.createQuery().selectNodesByPath(nodeService.getRootNode(RepoConsts.SPACES_STORE),
						"/app:company_home/" + AbstractBeCPGQueryBuilder.encodePath("/System/Lists/bcpg:entityLists/pmMaterials") + "/*");

				ruleService.disableRules();
			}

			@Override
			public String getIdentifier(NodeRef entry) {
				return entry.toString();
			}

			@SuppressWarnings("unchecked")
			@Override
			public void process(NodeRef dataListNodeRef) throws Throwable {
				if (nodeService.exists(dataListNodeRef)) {
					AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
					policyBehaviourFilter.disableBehaviour();
					List<String> pmMaterials = (List<String>) nodeService.getProperty(dataListNodeRef, PM_MATERIAL);
					if (pmMaterials != null) {
						List<NodeRef> assocPmMaterial = new ArrayList<>();
						for (String pmMaterial : pmMaterials) {
							for (NodeRef listValueNodeRef : materials) {

								String key = (String) nodeService.getProperty(listValueNodeRef, BeCPGModel.PROP_LV_VALUE);
								String code = (String) nodeService.getProperty(listValueNodeRef, BeCPGModel.PROP_LV_CODE);
								if ((pmMaterial != null) && (pmMaterial.equals(key) || pmMaterial.equals(code))) {
									assocPmMaterial.add(listValueNodeRef);

								}

							}

						}

						associationService.update(dataListNodeRef, PackModel.ASSOC_PM_MATERIAL, assocPmMaterial);

					}

				} else {
					logger.warn("node doesn't exist : " + dataListNodeRef);
				}
			}

		};
		batchProcessor.process(worker, true);
		return I18NUtil.getMessage(MSG_SUCCESS);

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

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	
}
