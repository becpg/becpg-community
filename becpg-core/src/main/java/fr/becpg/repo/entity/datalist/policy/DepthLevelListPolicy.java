/*
 * 
 */
package fr.becpg.repo.entity.datalist.policy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * The Class DepthLevelListPolicy.
 * 
 * @author querephi
 */
public class DepthLevelListPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy, 
NodeServicePolicies.OnAddAspectPolicy, 
NodeServicePolicies.OnDeleteNodePolicy {

	private static int DEFAULT_LEVEL = 1;
	private static final String QUERY_LIST_ITEMS_BY_FATHER = "+TYPE:\"%s\" +@bcpg\\:father:\"%s\"";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(DepthLevelListPolicy.class);

	/** The policy component. */
	private PolicyComponent policyComponent;

	/** The node service. */
	private NodeService nodeService;
	
	private BeCPGSearchService beCPGSearchService;

	/**
	 * Sets the policy component.
	 * 
	 * @param policyComponent
	 *            the new policy component
	 */
	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	/**
	 * Sets the node service.
	 * 
	 * @param nodeService
	 *            the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	/**
	 * Inits the.
	 */
	public void init() {
		logger.debug("Init DepthLevelListPolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, BeCPGModel.ASPECT_DEPTH_LEVEL,
				new JavaBehaviour(this, "onUpdateProperties"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME, BeCPGModel.ASPECT_DEPTH_LEVEL,
				new JavaBehaviour(this, "onAddAspect"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME, BeCPGModel.ASPECT_DEPTH_LEVEL,
				new JavaBehaviour(this, "onDeleteNode"));
	}

	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

		NodeRef beforeFather = (NodeRef) before.get(BeCPGModel.PROP_FATHER);
		NodeRef afterFather = (NodeRef) after.get(BeCPGModel.PROP_FATHER);
		
		logger.debug("before father: " + beforeFather);
		logger.debug("after father: " + afterFather);

		// has changed
		Integer level = null;
		boolean hasChanged = false;
		if (afterFather != null && !afterFather.equals(beforeFather)) {
				
			hasChanged = true;
			level = (Integer) nodeService.getProperty(afterFather, BeCPGModel.PROP_DEPTH_LEVEL);
			logger.debug("father level: " + level);
		}else{//father is null
			hasChanged = true;
		}
		
		if(hasChanged){			
			level = level == null ? DEFAULT_LEVEL : level+1;
			logger.debug("set property level: " + level);
			nodeService.setProperty(nodeRef, BeCPGModel.PROP_DEPTH_LEVEL, level);
			propagateLevel(nodeRef, level+1);
		}		
	}

	@Override
	public void onAddAspect(NodeRef nodeRef, QName aspect) {
		
		if(aspect.isMatch(BeCPGModel.ASPECT_DEPTH_LEVEL)){
			
			NodeRef father = (NodeRef) nodeService.getProperty(nodeRef, BeCPGModel.PROP_FATHER);
			
			if(father == null){
				nodeService.setProperty(nodeRef, BeCPGModel.PROP_DEPTH_LEVEL, DEFAULT_LEVEL);
			}
		}	
	}
	
	private void propagateLevel(NodeRef father, int level){
		
		List<NodeRef> listItems = getChildren(father);
		logger.debug("propagateLevel level: " + level + "listItems: " + listItems);
		
		for(NodeRef nodeRef : listItems){
			nodeService.setProperty(nodeRef, BeCPGModel.PROP_DEPTH_LEVEL, level);
			propagateLevel(nodeRef, level+1);
		}
	}

	@Override
	public void onDeleteNode(ChildAssociationRef childRef, boolean isNodeArchived) {
		
		logger.debug("onDeleteNode: " + childRef.getChildRef());
		List<NodeRef> listItems = getChildren(childRef.getChildRef());
		
		for(NodeRef nodeRef : listItems){			
			nodeService.deleteNode(nodeRef);
		}
	}
	
	private List<NodeRef> getChildren(NodeRef father){
		
		String query = String.format(QUERY_LIST_ITEMS_BY_FATHER, BeCPGModel.TYPE_ENTITYLIST_ITEM, father);				
		return beCPGSearchService.unProtLuceneSearch(query, null, RepoConsts.MAX_RESULTS_NO_LIMIT);
	}

}
