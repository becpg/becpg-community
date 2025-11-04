package fr.becpg.repo.project.admin.patch;

import java.util.List;

import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorkerAdaptor;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.admin.patch.AbstractBeCPGPatch;
import fr.becpg.repo.survey.SurveyModel;
import fr.becpg.repo.survey.helper.SurveyableEntityHelper;

/**
 * <p>SurveyQuestionGeneratePatch class.</p>
 *
 * @author matthieu
 */
public class SurveyQuestionGeneratePatch extends AbstractBeCPGPatch {

	/** Constant <code>TYPE_SURVEY_QUESTION</code> */
	protected static final QName TYPE_SURVEY_QUESTION = QName.createQName(SurveyModel.SURVEY_URI, "surveyQuestion");
	/** Constant <code>PROP_GENERATION_ENABLED</code> */
	protected static final QName PROP_GENERATION_ENABLED = QName.createQName(SurveyModel.SURVEY_URI, "generationEnabled");

	private BehaviourFilter policyBehaviourFilter;

	private RuleService ruleService;

	/**
	 * <p>Setter for the field <code>policyBehaviourFilter</code>.</p>
	 *
	 * @param policyBehaviourFilter a {@link org.alfresco.repo.policy.BehaviourFilter} object
	 */
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}
	
	/**
	 * <p>Setter for the field <code>ruleService</code>.</p>
	 *
	 * @param ruleService a {@link org.alfresco.service.cmr.rule.RuleService} object
	 */
	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}
	
	/** {@inheritDoc} */
	@Override
	protected String applyInternal() throws Exception {
		BatchProcessor<NodeRef> processor = createBatchTypeProcessor(TYPE_SURVEY_QUESTION, false);
		BatchProcessWorker<NodeRef> worker = new BatchProcessWorkerAdaptor<>() {
			@Override
			public void process(NodeRef questionNodeRef) throws Throwable {
				AuthenticationUtil.runAs(() -> {
					if (shouldEnableGeneration(questionNodeRef)) {
						try {
							policyBehaviourFilter.disableBehaviour();
							ruleService.disableRules();
							IntegrityChecker.setWarnInTransaction();
							nodeService.setProperty(questionNodeRef, PROP_GENERATION_ENABLED, true);
						} finally {
							policyBehaviourFilter.enableBehaviour();
							ruleService.enableRules();
						}
					}
					return null;
                }, AuthenticationUtil.getSystemUserName());  
			}

			@SuppressWarnings("unchecked")
			private boolean shouldEnableGeneration(NodeRef questionNodeRef) {
				String surveyListName = (String) nodeService.getProperty(questionNodeRef, SurveyModel.PROP_SURVEY_FS_SURVEY_LIST_NAME);
				if (surveyListName == null || !surveyListName.startsWith(SurveyableEntityHelper.SURVEY_LIST_BASE_NAME)) {
					return false;
				}
				List<String> linkedTypes = (List<String>) nodeService.getProperty(questionNodeRef, SurveyModel.PROP_SURVEY_FS_LINKED_TYPE);
				if (linkedTypes != null && !linkedTypes.isEmpty()) {
					return true;
				}
				List<AssociationRef> linkedCharacts = nodeService.getTargetAssocs(questionNodeRef, SurveyModel.ASSOC_SURVEY_FS_LINKED_CHARACT_REFS);
				if (linkedCharacts != null && !linkedCharacts.isEmpty()) {
					return true;
				}
				List<AssociationRef> linkedHierarchies = nodeService.getTargetAssocs(questionNodeRef, SurveyModel.ASSOC_SURVEY_FS_LINKED_HIERARCHY);
				if (linkedHierarchies != null && !linkedHierarchies.isEmpty()) {
					return true;
				}
				List<AssociationRef> linkedSubsidiaries = nodeService.getTargetAssocs(questionNodeRef, BeCPGModel.ASSOC_SUBSIDIARY_REF);
				if (linkedSubsidiaries != null && !linkedSubsidiaries.isEmpty()) {
					return true;
				}
				List<AssociationRef> linkedPlants = nodeService.getTargetAssocs(questionNodeRef, BeCPGModel.ASSOC_PLANTS);
				if (linkedPlants != null && !linkedPlants.isEmpty()) {
					return true;
				}
				return false;
			}
		};
		processor.processLong(worker, true);
		return "Patch applied successfully";
	}

}
