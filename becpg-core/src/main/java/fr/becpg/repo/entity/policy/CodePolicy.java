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
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * The Class CodePolicy.
 * 
 * @author querephi
 */
public class CodePolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnAddAspectPolicy {



	private static Log logger = LogFactory.getLog(CodePolicy.class);

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
	protected void doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		
		for (NodeRef nodeRef : pendingNodes) {
			if (isNotLocked(nodeRef) && !isWorkingCopyOrVersion(nodeRef) ) {

				// check code is already taken. If yes : this object is
				// a copy of an
				// existing node
				String code = (String) nodeService.getProperty(nodeRef, BeCPGModel.PROP_CODE);
				boolean generateCode = true;
				QName typeQName = nodeService.getType(nodeRef);

				if (code != null && !code.isEmpty()) {
					
					//private static final String QUERY_NODE_BY_CODE = " +TYPE:\"%s\" +@bcpg\\:code:\"%s\"  -@sys\\:node-uuid:\"%s\" ";
					generateCode = BeCPGQueryBuilder
							.createQuery()
							.ofType(typeQName)
							.andProp(BeCPGModel.PROP_CODE,code)
							.excludeVersions()
							.singleValue()!=null;
				}

				// generate a new code
				if (generateCode) {
					String c = autoNumService.getAutoNumValue(typeQName, BeCPGModel.PROP_CODE);
					nodeService.setProperty(nodeRef, BeCPGModel.PROP_CODE, c);
				} else {
					// store autoNum in db
					autoNumService.createOrUpdateAutoNumValue(typeQName, BeCPGModel.PROP_CODE, code);
				}
			}
		}		
	}
}
