package fr.becpg.repo.project.admin.patch;

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
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.admin.patch.AbstractBeCPGPatch;

/**
 * Add missing aspect to task
 * @author matthieu
 *
 */
public class ProjectRulesPatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(ProjectRulesPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.projet.projectRulesPatch.result";
	


	private NodeDAO nodeDAO;
	private PatchDAO patchDAO;
	private QNameDAO qnameDAO;
	private RuleService ruleService;

	private final int batchThreads = 3;
	private final int batchSize = 40;
	private final long count = batchThreads * batchSize;

	@Override
	protected String applyInternal() throws Exception {

			AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

			BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<NodeRef>() {
				final List<NodeRef> result = new ArrayList<>();

				final long maxNodeId = getPatchDAO().getMaxAdmNodeID();

				long minSearchNodeId = 1;
				long maxSearchNodeId = count;

				final Pair<Long, QName> val = getQnameDAO().getQName(ProjectModel.TYPE_PROJECT);

				public int getTotalEstimatedWorkSize() {
					return result.size();
				}

				public Collection<NodeRef> getNextWork() {
					if (val != null) {
						Long typeQNameId = val.getFirst();

						result.clear();

						while (result.isEmpty() && minSearchNodeId < maxNodeId) {
							
							
							List<Long> nodeids = getPatchDAO().getNodesByTypeQNameId(typeQNameId, minSearchNodeId, maxSearchNodeId);

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

			BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>("ProjectRulesPatch",
					transactionService.getRetryingTransactionHelper(), workProvider, batchThreads, batchSize, applicationEventPublisher, logger, 1000);

			BatchProcessWorker<NodeRef> worker = new BatchProcessWorker<NodeRef>() {

				public void afterProcess() throws Throwable {
				}

				public void beforeProcess() throws Throwable {
				}

				public String getIdentifier(NodeRef entry) {
					return entry.toString();
				}

				public void process(NodeRef projectNodeRef) throws Throwable {
					
					AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
					
					if (nodeService.exists(projectNodeRef) ) {
						AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
					
						for(Rule rule : ruleService.getRules(projectNodeRef, false)){
							logger.info("Found a rule on project: "+ rule);
							
							if(rule.getTitle().equals(I18NUtil.getMessage("project.activity.rule.inbound.title"))
									|| rule.getTitle().equals(I18NUtil.getMessage("project.activity.rule.outbound.title"))
											|| rule.getTitle().equals(I18NUtil.getMessage("project.activity.rule.update.title"))){
										ruleService.removeRule(projectNodeRef, rule);
										logger.info(" - deleting rule");
									}
						}
						
					} else {
						logger.warn("projectNodeRef doesn't exist : " + projectNodeRef);
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


	public RuleService getRuleService() {
		return ruleService;
	}

	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}

}
