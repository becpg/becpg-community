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
 * @version $Id: $Id
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

	/**
	 * <p>Setter for the field <code>ruleService</code>.</p>
	 *
	 * @param ruleService a {@link org.alfresco.service.cmr.rule.RuleService} object.
	 */
	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}

	/** {@inheritDoc} */
	@Override
	protected String applyInternal() throws Exception {

		BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<>() {
			final List<NodeRef> result = new ArrayList<>();

			final long maxNodeId = getNodeDAO().getMaxNodeId();

			long minSearchNodeId = 0;

			final Pair<Long, QName> val = getQnameDAO().getQName(PackModel.ASPECT_PM_MATERIAL);

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
						List<Long> nodeids = getPatchDAO().getNodesByAspectQNameId(typeQNameId, minSearchNodeId, minSearchNodeId + INC);

						for (Long nodeid : nodeids) {
							NodeRef.Status status = getNodeDAO().getNodeIdStatus(nodeid);
							if (!status.isDeleted()) {
								result.add(status.getNodeRef());
							}
						}
						minSearchNodeId = minSearchNodeId + INC;
					}
				}

				return result;
			}
		};

		BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>("PMMaterialPatch", transactionService.getRetryingTransactionHelper(),
				workProvider, BATCH_THREADS, BATCH_SIZE, applicationEventPublisher, logger, 500);

		BatchProcessWorker<NodeRef> worker = new BatchProcessWorker<>() {

			List<NodeRef> materials = new ArrayList<>();

			@Override
			public void afterProcess() throws Throwable {
				//Do nothing

			}

			@Override
			public void beforeProcess() throws Throwable {
				AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
				materials = BeCPGQueryBuilder.createQuery().selectNodesByPath(nodeService.getRootNode(RepoConsts.SPACES_STORE),
						"/app:company_home/" + AbstractBeCPGQueryBuilder.encodePath("/System/Lists/bcpg:entityLists/pmMaterials") + "/*");

			}

			@Override
			public String getIdentifier(NodeRef entry) {
				return entry.toString();
			}

			@SuppressWarnings("unchecked")
			@Override
			public void process(NodeRef dataListNodeRef) throws Throwable {
				ruleService.disableRules();

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
				ruleService.enableRules();
			}

		};
		batchProcessor.processLong(worker, true);
		return I18NUtil.getMessage(MSG_SUCCESS);

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
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object.
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

}
