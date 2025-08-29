package fr.becpg.repo.project.admin.patch;

import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorkerAdaptor;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.admin.patch.AbstractBeCPGPatch;
import fr.becpg.repo.survey.SurveyModel;

public class SurveyQuestionGeneratePatch extends AbstractBeCPGPatch {

	protected static final QName TYPE_SURVEY_QUESTION = QName.createQName(SurveyModel.SURVEY_URI, "surveyQuestion");
	protected static final QName PROP_GENERATION_ENABLED = QName.createQName(SurveyModel.SURVEY_URI, "generationEnabled");

	private BehaviourFilter policyBehaviourFilter;

	private RuleService ruleService;

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}
	
	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}
	
	@Override
	protected String applyInternal() throws Exception {
		BatchProcessor<NodeRef> processor = createBatchTypeProcessor(TYPE_SURVEY_QUESTION, false);
		BatchProcessWorker<NodeRef> worker = new BatchProcessWorkerAdaptor<>() {
			@Override
			public void process(NodeRef entry) throws Throwable {
				AuthenticationUtil.runAs(() -> {
					try {
						policyBehaviourFilter.disableBehaviour();
						ruleService.disableRules();
						IntegrityChecker.setWarnInTransaction();
						nodeService.setProperty(entry, PROP_GENERATION_ENABLED, true);
					} finally {
						policyBehaviourFilter.enableBehaviour();
						ruleService.enableRules();
					}
					return null;
                }, AuthenticationUtil.getSystemUserName());  
			}
		};
		processor.processLong(worker, true);
		return "Patch applied successfully";
	}

}
