package fr.becpg.repo.survey;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.survey.data.SurveyQuestion;

public class SurveyQuestionPolicy extends AbstractBeCPGPolicy
		implements OnCreateNodePolicy, OnUpdatePropertiesPolicy, BeforeDeleteNodePolicy {

	private static final String CACHE_KEY = SurveyQuestion.class.getName();

	private BeCPGCacheService beCPGCacheService;

	@Override
	public void doInit() {
		policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, SurveyModel.TYPE_SURVEY_QUESTION,
				new JavaBehaviour(this, "onCreateNode"));
		policyComponent.bindClassBehaviour(OnUpdatePropertiesPolicy.QNAME, SurveyModel.TYPE_SURVEY_QUESTION,
				new JavaBehaviour(this, "onUpdateProperties"));
		policyComponent.bindClassBehaviour(BeforeDeleteNodePolicy.QNAME, SurveyModel.TYPE_SURVEY_QUESTION,
				new JavaBehaviour(this, "beforeDeleteNode"));
	}

	/**
	 * <p>
	 * Setter for the field <code>beCPGCacheService</code>.
	 * </p>
	 *
	 * @param beCPGCacheService a {@link fr.becpg.repo.cache.BeCPGCacheService}
	 *                          object
	 */
	public void setBeCPGCacheService(BeCPGCacheService beCPGCacheService) {
		this.beCPGCacheService = beCPGCacheService;
	}

	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		beCPGCacheService.clearCache(CACHE_KEY);
	}

	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		beCPGCacheService.clearCache(CACHE_KEY);
	}

	@Override
	public void beforeDeleteNode(NodeRef nodeRef) {
		beCPGCacheService.clearCache(CACHE_KEY);
	}

}
