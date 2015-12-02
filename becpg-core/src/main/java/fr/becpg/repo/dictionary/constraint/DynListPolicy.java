/*
 * 
 */
package fr.becpg.repo.dictionary.constraint;

import java.util.Set;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * @author matthieu
 */
public class DynListPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnDeleteNodePolicy,
		NodeServicePolicies.OnUpdateNodePolicy, NodeServicePolicies.OnCreateNodePolicy {

	private static final Log logger = LogFactory.getLog(DynListPolicy.class);

	
	BeCPGCacheService beCPGCacheService;
	
	public void setBeCPGCacheService(BeCPGCacheService beCPGCacheService) {
		this.beCPGCacheService = beCPGCacheService;
	}

	public void doInit() {
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME, BeCPGModel.TYPE_LIST_VALUE, new JavaBehaviour(this,
				"onDeleteNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdateNodePolicy.QNAME, BeCPGModel.TYPE_LIST_VALUE, new JavaBehaviour(this,
				"onUpdateNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, BeCPGModel.TYPE_LIST_VALUE, new JavaBehaviour(this,
				"onCreateNode"));
	}

	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		queueNode(childAssocRef.getChildRef());
	}

	@Override
	public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived) {
		queueNode(childAssocRef.getChildRef());
	}

	@Override
	public void onUpdateNode(NodeRef itemNodeRef) {
		queueNode(itemNodeRef);
	}
	
	@Override
	protected void doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		if(!pendingNodes.isEmpty()){
			logger.debug("Clean dynamicListConstraint cache");
			beCPGCacheService.clearCache(DynListConstraint.DYN_LIST_CACHE_NAME);
		}
	}

}
