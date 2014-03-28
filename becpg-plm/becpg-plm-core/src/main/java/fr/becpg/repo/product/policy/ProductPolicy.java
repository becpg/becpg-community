package fr.becpg.repo.product.policy;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.namespace.QName;
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
			NodeServicePolicies.OnCreateNodePolicy{

	private static Log logger = LogFactory.getLog(ProductPolicy.class);

	@Override
	public void doInit() {
		logger.debug("Init ProductPolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, PLMModel.TYPE_PRODUCT,
				new JavaBehaviour(this, "onCreateNode"));
	}

	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		
		nodeService.setProperty(childAssocRef.getChildRef(), PLMModel.PROP_PRODUCT_STATE, SystemState.Simulation);
		
		if (nodeService.hasAspect(childAssocRef.getChildRef(), PLMWorkflowModel.ASPECT_PRODUCT_VALIDATION_ASPECT)) {
			nodeService.removeAspect(childAssocRef.getChildRef(), PLMWorkflowModel.ASPECT_PRODUCT_VALIDATION_ASPECT);
		}
	}		
}
