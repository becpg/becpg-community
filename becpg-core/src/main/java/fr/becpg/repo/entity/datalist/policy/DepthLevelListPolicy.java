/*
 * 
 */
package fr.becpg.repo.entity.datalist.policy;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.datalist.DataListSortService;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.helper.LuceneHelper.Operator;
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
	private static int MAX_LEVEL = 256;
	private static final String QUERY_LIST_ITEMS = "+PARENT:\"%s\"";
	
	private static Log logger = LogFactory.getLog(DepthLevelListPolicy.class);

	private PolicyComponent policyComponent;

	private NodeService nodeService;
	
	private BeCPGSearchService beCPGSearchService;

	private DataListSortService dataListSortService;

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	public void setDataListSortService(DataListSortService dataListSortService) {
		this.dataListSortService = dataListSortService;
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

		NodeRef beforeParentLevel = (NodeRef) before.get(BeCPGModel.PROP_PARENT_LEVEL);
		NodeRef afterParentLevel = (NodeRef) after.get(BeCPGModel.PROP_PARENT_LEVEL);
		
		if(logger.isDebugEnabled()){
			logger.debug("###DepthLevel onUpdateProperties on " + tryGetName(nodeRef));
		}
		
		// has changed ?
		boolean hasChanged = false;
		if (afterParentLevel != null && !afterParentLevel.equals(beforeParentLevel)) {				
			hasChanged = true;
		}else if(beforeParentLevel != null && !beforeParentLevel.equals(afterParentLevel)){//parentLevel is null
			hasChanged = true;
		}
		else{
			hasChanged = false;
		}
		
		if(hasChanged){				
			calculateDepthLevel(nodeRef, afterParentLevel);
		}		
	}

	@Override
	public void onAddAspect(NodeRef nodeRef, QName aspect) {
		
		if(aspect.isMatch(BeCPGModel.ASPECT_DEPTH_LEVEL)){			
			NodeRef parentLevel = (NodeRef) nodeService.getProperty(nodeRef, BeCPGModel.PROP_PARENT_LEVEL);			
			calculateDepthLevel(nodeRef, parentLevel);
		}	
	}
	
	@Override
	public void onDeleteNode(ChildAssociationRef childRef, boolean isNodeArchived) {
		
		List<NodeRef> listItems = getChildren(childRef.getParentRef(), childRef.getChildRef());
		
		for(NodeRef nodeRef : listItems){			
			nodeService.deleteNode(nodeRef);
		}
	}
	
	private void calculateDepthLevel(NodeRef nodeRef, NodeRef parentLevel){
	
		Integer level = null;

		if(parentLevel !=null){
			level = (Integer) nodeService.getProperty(parentLevel, BeCPGModel.PROP_DEPTH_LEVEL);
			level++;
		}
		else{
			level = DEFAULT_LEVEL;
		}		
		
		NodeRef listContainer = nodeService.getPrimaryParent(nodeRef).getParentRef();			
		if(logger.isDebugEnabled()){
			logger.debug("set property level: " + level + " - name: " + tryGetName(nodeRef));
		}		
		
		nodeService.setProperty(nodeRef, BeCPGModel.PROP_DEPTH_LEVEL, level);
		propagateLevel(listContainer, nodeRef, level+1);
		
		// update sort of currentNodeRef		
		dataListSortService.calculateNextSort(listContainer, nodeRef, parentLevel);	
	}
	
	private void propagateLevel(NodeRef listContainer, NodeRef parentLevel, int level){
		
		if(level>MAX_LEVEL){
			logger.error("Cyclic parentLevel level");
			return; 
		}
		
		List<NodeRef> listItems = getChildren(listContainer, parentLevel);
		if(logger.isDebugEnabled()){
			logger.debug("propagateLevel level: " + level + "listItems.size(): " + listItems.size());
		}
		
		for(NodeRef nodeRef : listItems){
			if(!nodeRef.equals(parentLevel)){
				nodeService.setProperty(nodeRef, BeCPGModel.PROP_DEPTH_LEVEL, level);
				propagateLevel(listContainer, nodeRef, level+1);
			}
		}
	}

	
	
	/*
	 * Get the children of the children of the parent
	 */
	private List<NodeRef> getChildren(NodeRef listContainer, NodeRef parentLevel){
						
		return beCPGSearchService.unProtLuceneSearch(getQueryByParentLevel(listContainer, parentLevel), LuceneHelper.getSort(BeCPGModel.PROP_SORT, true), RepoConsts.MAX_RESULTS_NO_LIMIT);
	}
	
	/*
	 * Get the query that return children of parent
	 */
	private String getQueryByParentLevel(NodeRef listContainer, NodeRef parentLevel){
		
		String query = String.format(QUERY_LIST_ITEMS, listContainer);
		if(parentLevel == null){
			query += LuceneHelper.getCondIsNullValue(BeCPGModel.PROP_PARENT_LEVEL, Operator.AND);
		}
		else{
			query += LuceneHelper.getCondEqualValue(BeCPGModel.PROP_PARENT_LEVEL, parentLevel.toString(), Operator.AND);
		}
		
		return query;
	}
	
	
	
	/*
	 * Debug function used to get the name of the product stored in the compoList
	 */
	private String tryGetName(NodeRef nodeRef){
		
		List<AssociationRef> compoAssocRefs = nodeService.getTargetAssocs(nodeRef, BeCPGModel.ASSOC_COMPOLIST_PRODUCT);
		NodeRef part = compoAssocRefs.size() > 0 ? (compoAssocRefs.get(0)).getTargetRef() : null;
		
		return part != null ? (String)nodeService.getProperty(part, ContentModel.PROP_NAME) : (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
	}
}
