package fr.becpg.repo.product.policy;

import org.alfresco.repo.coci.CheckOutCheckInServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.PLMModel;
import fr.becpg.model.PLMWorkflowModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * Policies of product
 * 
 * @author quere
 * 
 */
public class ProductPolicy extends AbstractBeCPGPolicy implements 
		CheckOutCheckInServicePolicies.OnCheckOut{

	private static final Log logger = LogFactory.getLog(ProductPolicy.class);

	@Override
	public void doInit() {
		logger.debug("Init ProductPolicy...");
		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.OnCheckOut.QNAME, PLMModel.TYPE_PRODUCT,
				new JavaBehaviour(this, "onCheckOut"));
	}

	@Override
	public void onCheckOut(NodeRef workingCopyNodeRef) {
		nodeService.setProperty(workingCopyNodeRef, PLMModel.PROP_PRODUCT_STATE, SystemState.Simulation);
		
		if (nodeService.hasAspect(workingCopyNodeRef, PLMWorkflowModel.ASPECT_PRODUCT_VALIDATION_ASPECT)) {
			nodeService.removeAspect(workingCopyNodeRef, PLMWorkflowModel.ASPECT_PRODUCT_VALIDATION_ASPECT);
		}		
	}
	
}
