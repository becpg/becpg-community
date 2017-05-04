package fr.becpg.repo.product.policy;

import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.PLMWorkflowModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.entity.policy.CodePolicy;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

public class ProductPolicy extends AbstractBeCPGPolicy implements CopyServicePolicies.OnCopyCompletePolicy {

	private static final Log logger = LogFactory.getLog(CodePolicy.class);

	/**
	 * Inits the.
	 */
	@Override
	public void doInit() {
		logger.debug("Init ProductPolicy...");
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyCompletePolicy.QNAME, PLMModel.ASPECT_PRODUCT,
				new JavaBehaviour(this, "onCopyComplete"));

	}

	@Override
	public void onCopyComplete(QName classRef, NodeRef sourceNodeRef, NodeRef destinationRef, boolean copyToNewNode, Map<NodeRef, NodeRef> copyMap) {

		if (policyBehaviourFilter.isEnabled(ContentModel.ASPECT_AUDITABLE)
				&& policyBehaviourFilter.isEnabled(sourceNodeRef, ContentModel.ASPECT_AUDITABLE) && !isWorkingCopyOrVersion(destinationRef)
				&& !isWorkingCopyOrVersion(sourceNodeRef)

		) {
			nodeService.setProperty(destinationRef, PLMModel.PROP_PRODUCT_STATE, SystemState.Simulation);
			nodeService.setProperty(destinationRef, PLMModel.PROP_ERP_CODE, null);
			if (nodeService.hasAspect(destinationRef, PLMWorkflowModel.ASPECT_PRODUCT_VALIDATION_ASPECT)) {
				nodeService.removeAspect(destinationRef, PLMWorkflowModel.ASPECT_PRODUCT_VALIDATION_ASPECT);
			}
			// Allow to determine if is a branch or a version
			if (!policyBehaviourFilter.isEnabled(BeCPGModel.ASPECT_ENTITY_BRANCH)) {
				if (nodeService.hasAspect(destinationRef, BeCPGModel.ASPECT_ENTITY_BRANCH)) {
					nodeService.removeAspect(destinationRef, BeCPGModel.ASPECT_ENTITY_BRANCH);
				}
			}
		}

	}

}