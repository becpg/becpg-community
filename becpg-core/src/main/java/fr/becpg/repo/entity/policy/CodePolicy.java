/*
 * 
 */
package fr.becpg.repo.entity.policy;

import java.util.Set;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.AutoNumService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * The Class CodePolicy.
 * 
 * @author querephi
 */
public class CodePolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnAddAspectPolicy {



	private static final Log logger = LogFactory.getLog(CodePolicy.class);

	private AutoNumService autoNumService;

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}


	public void setAutoNumService(AutoNumService autoNumService) {
		this.autoNumService = autoNumService;
	}

	/**
	 * Inits the.
	 */
	public void doInit() {
		logger.debug("Init CodePolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME, BeCPGModel.ASPECT_CODE, new JavaBehaviour(this, "onAddAspect"));

	}
	
	@Override
	public void onAddAspect(NodeRef nodeRef, QName type) {
		
		// need to queue it in order to wait cm:workingCopy aspect is added
		queueNode(nodeRef);
	}

	@Override
	protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		
		for (NodeRef nodeRef : pendingNodes) {
			if (isNotLocked(nodeRef) && !isWorkingCopyOrVersion(nodeRef) ) {

				autoNumService.getOrCreateBeCPGCode(nodeRef);
			}
		}
		return true;
	}
}
