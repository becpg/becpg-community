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
	private static int MAX_LEVEL = 25;
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

		NodeRef beforeFather = (NodeRef) before.get(BeCPGModel.PROP_FATHER);
		NodeRef afterFather = (NodeRef) after.get(BeCPGModel.PROP_FATHER);
		
		if(logger.isDebugEnabled()){
			logger.debug("nodeRef: " + nodeRef + " name " + nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
			if(beforeFather!=null){
				logger.debug("before father: " + beforeFather + " name " + nodeService.getProperty(beforeFather, ContentModel.PROP_NAME));
			}
			if(afterFather!=null){
				logger.debug("after father: " + afterFather+" name " + nodeService.getProperty(afterFather, ContentModel.PROP_NAME));
			}
		}

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
			
			NodeRef listContainer = nodeService.getPrimaryParent(nodeRef).getParentRef();			
			level = level == null ? DEFAULT_LEVEL : level+1;
			logger.debug("set property level: " + level + " - node: " + nodeRef);
			nodeService.setProperty(nodeRef, BeCPGModel.PROP_DEPTH_LEVEL, level);
			propagateLevel(listContainer, nodeRef, level+1);
			
			// sort sibling nodes			
			List<NodeRef> listItems = getSiblingNodes(listContainer, afterFather, nodeRef);
			logger.debug("listItems.size(): " + listItems.size() + " - " + listItems);
			
			if(listItems.size()>0){
				
				Integer sort = (Integer)nodeService.getProperty(listItems.get(0), BeCPGModel.PROP_SORT);
				logger.debug("sort: " + sort + " - name " + nodeService.getProperty(listItems.get(0), ContentModel.PROP_NAME));				
				listItems = getChildrenBySort(listContainer, sort);
				
				// update sort of currentNodeRef
				sort++;
				nodeService.setProperty(nodeRef, BeCPGModel.PROP_SORT, sort);
				
				// update nodes after currentNodeRef
				for(NodeRef listItem : listItems){					
					sort++;
					nodeService.setProperty(listItem, BeCPGModel.PROP_SORT, sort);
				}
			}		
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
	
	private void propagateLevel(NodeRef listContainer, NodeRef father, int level){
		
		if(level>MAX_LEVEL){
			logger.error("Cyclic father level");
			return; 
		}
		
		List<NodeRef> listItems = getChildren(listContainer, father);
		logger.debug("propagateLevel level: " + level + "listItems: " + listItems);
		
		for(NodeRef nodeRef : listItems){
			if(!nodeRef.equals(father)){
				nodeService.setProperty(nodeRef, BeCPGModel.PROP_DEPTH_LEVEL, level);
				propagateLevel(listContainer, nodeRef, level+1);
			}
		}
	}

	@Override
	public void onDeleteNode(ChildAssociationRef childRef, boolean isNodeArchived) {
		
		logger.debug("onDeleteNode: " + childRef.getChildRef());
		List<NodeRef> listItems = getChildren(childRef.getParentRef(), childRef.getChildRef());
		
		for(NodeRef nodeRef : listItems){			
			nodeService.deleteNode(nodeRef);
		}
	}
	
	private List<NodeRef> getChildren(NodeRef listContainer, NodeRef father){
						
		return beCPGSearchService.unProtLuceneSearch(getQueryByFather(listContainer, father), null, RepoConsts.MAX_RESULTS_NO_LIMIT);
	}
	
	private String getQueryByFather(NodeRef listContainer, NodeRef father){
		
		String query = String.format(QUERY_LIST_ITEMS, listContainer);
		if(father == null){
			query += LuceneHelper.getCondIsNullValue(BeCPGModel.PROP_FATHER, Operator.AND);
		}
		else{
			query += LuceneHelper.getCondEqualValue(BeCPGModel.PROP_FATHER, father.toString(), Operator.AND);
		}
		
		return query;
	}

	private List<NodeRef> getSiblingNodes(NodeRef listContainer, NodeRef father, NodeRef nodeRef){
		
		String query = getQueryByFather(listContainer, father);
		query += LuceneHelper.getCondEqualID(nodeRef, Operator.NOT);
		return beCPGSearchService.unProtLuceneSearch(query, null, RepoConsts.MAX_RESULTS_NO_LIMIT);
	}

	private List<NodeRef> getChildrenBySort(NodeRef listContainer, Integer sort){
		
		String query = String.format(QUERY_LIST_ITEMS_BY_SORT, listContainer, sort);
		return beCPGSearchService.unProtLuceneSearch(query, null, RepoConsts.MAX_RESULTS_NO_LIMIT);
	}
}
