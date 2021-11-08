/*
 * 
 */
package fr.becpg.repo.dictionary.constraint;

import java.util.Set;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * <p>DynListPolicy class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class DynListPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnDeleteNodePolicy,
		NodeServicePolicies.OnUpdateNodePolicy, NodeServicePolicies.OnCreateNodePolicy {

	private static final Log logger = LogFactory.getLog(DynListPolicy.class);


	private BeCPGCacheService beCPGCacheService;
	
	private NamespaceService namespaceService;
	
	/**
	 * <p>Setter for the field <code>beCPGCacheService</code>.</p>
	 *
	 * @param beCPGCacheService a {@link fr.becpg.repo.cache.BeCPGCacheService} object.
	 */
	public void setBeCPGCacheService(BeCPGCacheService beCPGCacheService) {
		this.beCPGCacheService = beCPGCacheService;
	}
	
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * <p>doInit.</p>
	 */
	public void doInit() {
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME, BeCPGModel.TYPE_LIST_VALUE, new JavaBehaviour(this,
				"onDeleteNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdateNodePolicy.QNAME, BeCPGModel.TYPE_LIST_VALUE, new JavaBehaviour(this,
				"onUpdateNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, BeCPGModel.TYPE_LIST_VALUE, new JavaBehaviour(this,
				"onCreateNode"));
	}

	/** {@inheritDoc} */
	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		queueNode(childAssocRef.getChildRef());
	}

	/** {@inheritDoc} */
	@Override
	public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived) {
		
		if (DynListConstraint.getPathRegistry().contains(nodeService.getPath(childAssocRef.getParentRef()).toPrefixString(namespaceService))) {
			throw new IllegalStateException(I18NUtil.getMessage("message.constraint.list-value.delete.forbidden"));
		}
		
		queueNode(childAssocRef.getChildRef());
	}

	/** {@inheritDoc} */
	@Override
	public void onUpdateNode(NodeRef itemNodeRef) {
		queueNode(itemNodeRef);
	}
	
	/** {@inheritDoc} */
	@Override
	protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		if(!pendingNodes.isEmpty()){
			logger.debug("Clean dynamicListConstraint cache");
			beCPGCacheService.clearCache(DynListConstraint.class.getName());
		}
		return true;
	}
	
}
