/*
 * 
 */
package fr.becpg.repo.entity.datalist.policy;

import java.io.Serializable;
import java.util.HashMap;
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
	private static final String QUERY_LIST_ITEMS_BY_SORT = "+PARENT:\"%s\" AND +@bcpg\\:sort:[%s TO MAX]";
	
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
		calculateNextSort(listContainer, nodeRef, parentLevel);	
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
	 * Get all children of the list container 
	 */
	private List<NodeRef> getChildren(NodeRef listContainer){
		
		return beCPGSearchService.unProtLuceneSearch(String.format(QUERY_LIST_ITEMS, listContainer), getSort(BeCPGModel.PROP_SORT, true), RepoConsts.MAX_RESULTS_NO_LIMIT);
	}
	
	/*
	 * Get the children of the children of the parent
	 */
	private List<NodeRef> getChildren(NodeRef listContainer, NodeRef parentLevel){
						
		return beCPGSearchService.unProtLuceneSearch(getQueryByParentLevel(listContainer, parentLevel), getSort(BeCPGModel.PROP_SORT, true), RepoConsts.MAX_RESULTS_NO_LIMIT);
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
	
	private Map<String, Boolean> getSort(QName field, boolean asc) {

		Map<String, Boolean> sort = new HashMap<String, Boolean>();
		sort.put("@" + field, asc);

		return sort;
	}

	/*
	 * Get the last sibling node of the level
	 */
	private NodeRef getLastSiblingNode(NodeRef listContainer, NodeRef parentLevel, NodeRef nodeRef){
		
		String query = getQueryByParentLevel(listContainer, parentLevel);
		query += LuceneHelper.getCondEqualID(nodeRef, Operator.NOT);
		List<NodeRef> listItems = beCPGSearchService.unProtLuceneSearch(query, getSort(BeCPGModel.PROP_SORT, false), RepoConsts.MAX_RESULTS_SINGLE_VALUE);
		return listItems.size()>0 ? listItems.get(0) : null;
	}

	/*
	 * look for another node that has already the sort value
	 */
	private NodeRef getSortedNode(NodeRef listContainer, Integer sort, NodeRef nodeRef){
		
		String query = String.format(QUERY_LIST_ITEMS_BY_SORT, listContainer, sort);
		query += LuceneHelper.getCondEqualValue(BeCPGModel.PROP_SORT, sort.toString(), Operator.AND);
		query += LuceneHelper.getCondEqualID(nodeRef, Operator.NOT);
		List<NodeRef> listItems = beCPGSearchService.unProtLuceneSearch(query, getSort(BeCPGModel.PROP_SORT, false), RepoConsts.MAX_RESULTS_SINGLE_VALUE);
		return listItems.size()>0 ? listItems.get(0) : null;
	}
	
	/*
	 * Calculate the next sort value of the node. May do a rebuild of the sort index
	 */
	private void calculateNextSort(NodeRef listContainer, NodeRef nodeRef, NodeRef parentLevel){
					
		// sibling nodes			
		NodeRef lastSiblingNode = getLastSiblingNode(listContainer, parentLevel, nodeRef);			
		Integer sort = null;
		
		if(lastSiblingNode != null){
			
			sort = (Integer)nodeService.getProperty(lastSiblingNode, BeCPGModel.PROP_SORT);
			if(logger.isDebugEnabled()){
				logger.debug("last sibling node: " + tryGetName(lastSiblingNode) + " - sort: " + sort);
			}				
		}
		else{
			//first node of level
			if(parentLevel != null){
				sort = (Integer)nodeService.getProperty(parentLevel, BeCPGModel.PROP_SORT);
				
				if(logger.isDebugEnabled()){
					logger.debug("first node of level, parent: " + tryGetName(parentLevel) + " - sort: " + sort);
				}
			}//first node of list
			else{
				if(logger.isDebugEnabled()){
					logger.debug("first node of list - sort: " + sort);
				}
			}
		}
		
		// calculate next sort
		Integer nextSort = RepoConsts.SORT_DEFAULT_STEP;
		if(sort == null){
			nextSort = RepoConsts.SORT_DEFAULT_STEP;
		}
		else{
			if(parentLevel == null){
				nextSort = sort + RepoConsts.SORT_DEFAULT_STEP;
			}
			else{
				nextSort = sort + RepoConsts.SORT_INSERTING_STEP;
			}
		}
		
		//is next free ?
		NodeRef sortedNodeRef = getSortedNode(listContainer, nextSort, nodeRef);			
		
		if(sortedNodeRef != null){
			if(logger.isDebugEnabled()){
				logger.info("### REBUILD INDEX - " + tryGetName(nodeRef) + " next sort " + nextSort + " is taken by: " + tryGetName(sortedNodeRef));
			}
			
			// rebuild index
			List<NodeRef> listItems = getChildren(listContainer);
			NodeRef lastNode = lastSiblingNode;
			int newSort = RepoConsts.SORT_DEFAULT_STEP;
			for(NodeRef listItem : listItems){
				if(!listItem.equals(nodeRef)){
					logger.debug("set property of " + tryGetName(listItem) +
							" sort " + nodeService.getProperty(listItem, BeCPGModel.PROP_SORT) + 
							" new sort " + newSort);
					nodeService.setProperty(listItem, BeCPGModel.PROP_SORT, newSort);
					newSort = newSort + RepoConsts.SORT_DEFAULT_STEP;
					
					//is it last node before the current one ?
					if(listItem.equals(lastNode)){
						NodeRef tmpNode = getLastSiblingNode(listContainer, lastNode, nodeRef);
						if(tmpNode != null){
							logger.debug("tmpNode " + tryGetName(tmpNode) +
									" sort " + nodeService.getProperty(tmpNode, BeCPGModel.PROP_SORT));
							lastNode = tmpNode;
						}
						else{
							logger.debug("set property of " + tryGetName(nodeRef) +
									" sort " + nodeService.getProperty(nodeRef, BeCPGModel.PROP_SORT) + 
									" new sort " + newSort);
							nodeService.setProperty(nodeRef, BeCPGModel.PROP_SORT, newSort);
							newSort = newSort + RepoConsts.SORT_DEFAULT_STEP;
						}
					}
				}
			}
			
			// call again function after index rebuilt is NOT OK
			//calculateNextSort(listContainer, nodeRef, parentLevel);
			nextSort = (Integer)nodeService.getProperty(sortedNodeRef, BeCPGModel.PROP_SORT) + RepoConsts.SORT_INSERTING_STEP;			
		}
		else{
			if(logger.isDebugEnabled()){
				logger.debug("update sort of currentNodeRef: sort: " + sort + " -nextSort : " + nextSort + " - node: " + tryGetName(nodeRef));
			}
			nodeService.setProperty(nodeRef, BeCPGModel.PROP_SORT, nextSort);
		}			
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
