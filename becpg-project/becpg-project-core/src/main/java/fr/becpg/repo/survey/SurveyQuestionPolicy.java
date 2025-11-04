package fr.becpg.repo.survey;

import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdateNodePolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * <p>SurveyQuestionPolicy class.</p>
 *
 * @author matthieu
 */
public class SurveyQuestionPolicy extends AbstractBeCPGPolicy
		implements OnCreateNodePolicy, OnUpdateNodePolicy, BeforeDeleteNodePolicy {

	private BeCPGCacheService beCPGCacheService;

	/** {@inheritDoc} */
	@Override
	public void doInit() {
		policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, SurveyModel.TYPE_SURVEY_QUESTION,
				new JavaBehaviour(this, "onCreateNode"));
		policyComponent.bindClassBehaviour(OnUpdateNodePolicy.QNAME, SurveyModel.TYPE_SURVEY_QUESTION,
				new JavaBehaviour(this, "onUpdateNode"));
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

	/** {@inheritDoc} */
	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		beCPGCacheService.clearCache(SurveyService.CACHE_KEY);
	}

	/** {@inheritDoc} */
	@Override
	public void onUpdateNode(NodeRef nodeRef) {
		beCPGCacheService.clearCache(SurveyService.CACHE_KEY);
	}

	/** {@inheritDoc} */
	@Override
	public void beforeDeleteNode(NodeRef nodeRef) {
		beCPGCacheService.clearCache(SurveyService.CACHE_KEY);
	}

}
