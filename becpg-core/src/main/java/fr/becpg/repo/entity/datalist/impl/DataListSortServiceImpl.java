package fr.becpg.repo.entity.datalist.impl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.datalist.DataListSortService;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.helper.LuceneHelper.Operator;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * 
 * @author matthieu, philippe
 * 
 */
@Service
public class DataListSortServiceImpl implements DataListSortService {

	private static Log logger = LogFactory.getLog(DataListSortServiceImpl.class);

	private static int DEFAULT_LEVEL = 1;

	private NodeService nodeService;

	private BeCPGSearchService beCPGSearchService;
	
	private BehaviourFilter policyBehaviourFilter;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	@Override
	public void computeDepthAndSort(Set<NodeRef> nodeRefs) {		
		
		NodeRef prevLastChild = null;
		NodeRef prevParentLevel = null;
		
		NodeRef prevListContainer = null;
		int sort = RepoConsts.SORT_DEFAULT_STEP - RepoConsts.SORT_INSERTING_STEP;
		
		HashSet<NodeRef> pendingNodeRefs = new HashSet<NodeRef>(nodeRefs);
		
		for(NodeRef nodeRef : nodeRefs){
			
			QName dataType = nodeService.getType(nodeRef);
			NodeRef listContainer = nodeService.getPrimaryParent(nodeRef).getParentRef();

			// depthLevel manage sort
			if (nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL)) {
				NodeRef parentLevel = (NodeRef) nodeService.getProperty(nodeRef, BeCPGModel.PROP_PARENT_LEVEL);
				
				// cycle detection
				if(nodeRef.equals(parentLevel)){
					
					logger.error("Cannot select itself as parent, otherwise we get a cycle. nodeRef: " + nodeRef);
				}
				else{

					if (logger.isDebugEnabled()) {
						logger.debug("computeDepthAndSort for :" + tryGetName(nodeRef));
					}
					
					// #351 : we avoid lucene queries
					if(prevParentLevel == null || !prevParentLevel.equals(parentLevel)){
						prevParentLevel = parentLevel;
						prevLastChild = getLastChildOfLevel(dataType, listContainer, parentLevel, nodeRef);
					}					

					insertAfter(dataType, listContainer, prevLastChild, nodeRef, pendingNodeRefs);
					prevLastChild = nodeRef;
				}			
			} else {

				// #351 : we avoid lucene queries
				if(prevListContainer == null || !prevListContainer.equals(listContainer)){					
					prevListContainer = listContainer;
					NodeRef prevSiblingNode = getLastChild(dataType, null, listContainer, nodeRef, false);
					if(prevSiblingNode != null){
						Integer s = (Integer)nodeService.getProperty(prevSiblingNode, BeCPGModel.PROP_SORT);
						if(s!=null){
							sort = s;							
						}
					}					
				}
				
				sort += RepoConsts.SORT_INSERTING_STEP;
				nodeService.setProperty(nodeRef, BeCPGModel.PROP_SORT, sort);				
			}
			
			pendingNodeRefs.remove(nodeRef);
		}
	}

	@Override
	public void insertAfter(NodeRef selectedNodeRef, NodeRef nodeRef) {
		
		QName dataType = nodeService.getType(nodeRef);
		NodeRef parentLevel = (NodeRef) nodeService.getProperty(nodeRef, BeCPGModel.PROP_PARENT_LEVEL);

		// Put at same level
		if(parentLevel != null || nodeService.getProperty(selectedNodeRef, BeCPGModel.PROP_PARENT_LEVEL) != null){
			setProperty(selectedNodeRef, BeCPGModel.PROP_PARENT_LEVEL, parentLevel);
		}		

		NodeRef listContainer = nodeService.getPrimaryParent(selectedNodeRef).getParentRef();
		insertAfter(dataType, listContainer, nodeRef, selectedNodeRef, new HashSet<NodeRef>());
	}

	private void insertAfter(QName dataType, NodeRef listContainer, NodeRef siblingNode, NodeRef nodeRef, HashSet<NodeRef> pendingNodeRefs) {

		logger.debug("insertAfter");				
		
		Integer level = null;
		NodeRef parentLevel = null;
		Integer sort = null;
		boolean isDepthList = nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL);
		logger.debug("isDepthList: " + isDepthList);
		
		if (isDepthList) {

			parentLevel = (NodeRef) nodeService.getProperty(nodeRef, BeCPGModel.PROP_PARENT_LEVEL);

			if (parentLevel != null) {

				level = (Integer) nodeService.getProperty(parentLevel, BeCPGModel.PROP_DEPTH_LEVEL);
				level++;
			} else {
				level = DEFAULT_LEVEL;
			}
		}

		if (siblingNode != null) {

			if(isDepthList){
			
				NodeRef lastChild = getLastChild(dataType, siblingNode, listContainer, nodeRef, isDepthList);
				sort = (Integer) nodeService.getProperty(lastChild, BeCPGModel.PROP_SORT);
				//sibling node can be after lastchild (drag n drop)
				Integer siblingSort = (Integer) nodeService.getProperty(siblingNode, BeCPGModel.PROP_SORT);
				if(siblingSort != null && siblingSort > sort){
					sort = siblingSort;
				}				
			}
			else{
				sort = (Integer) nodeService.getProperty(siblingNode, BeCPGModel.PROP_SORT);
			}			

		} else {
			// first node of level
			if (parentLevel != null) {
				sort = (Integer) nodeService.getProperty(parentLevel, BeCPGModel.PROP_SORT);

				if (logger.isDebugEnabled()) {
					logger.debug("first node of level, parent: " + tryGetName(parentLevel) + " - sort: " + sort);
				}
			}// first node of list
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("first node of list - sort: " + sort);
				}
			}
		}

		// calculate next sort
		Integer nextSort = RepoConsts.SORT_DEFAULT_STEP;
		if (sort == null) {
			nextSort = RepoConsts.SORT_DEFAULT_STEP;
		} else {
			nextSort = sort + RepoConsts.SORT_INSERTING_STEP;
		}

		// is next free ?
		NodeRef sortedNodeRef = getSortedNode(dataType, listContainer, nextSort, nodeRef);

		if (sortedNodeRef != null) {
			if (logger.isDebugEnabled()) {
				logger.debug(" nextSort not available : " + nextSort + " - node: " + tryGetName(nodeRef) + " - taken by: " + tryGetName(sortedNodeRef));
			}
			fixSortableList(dataType, listContainer);
			insertAfter(dataType, listContainer, siblingNode, nodeRef, pendingNodeRefs);
		} else {			

			setProperty(nodeRef, BeCPGModel.PROP_SORT, nextSort);
			
			if (isDepthList) {
			
				// start search before setProperty, otherwise it duplicates nodeRef in lucene index !!!
				List<NodeRef> listItems = getChildren(dataType, listContainer, nodeRef, true);
				
				setProperty(nodeRef, BeCPGModel.PROP_DEPTH_LEVEL, level);
				NodeRef prevNode = nodeRef;
				
				for (NodeRef tmp : listItems) {
					logger.debug("start call insertAfter: " + tryGetName(tmp));
					if(!pendingNodeRefs.contains(tmp)){
						insertAfter(dataType, listContainer, prevNode, tmp, pendingNodeRefs);
					}					
					prevNode = tmp;
				}
			}			
		}
	}

	private void fixSortableList(QName dataType, NodeRef listContainer) {

		logger.info("###FixSortableList. parentNodeRef: " + listContainer + "");
    	
		String query = LuceneHelper.getCondParent(listContainer, null);
		query += LuceneHelper.getCondIsNullValue(BeCPGModel.PROP_SORT, Operator.NOT);		
		
		List<NodeRef> listItems = luceneSearchByType(dataType, query, LuceneHelper.getSort(BeCPGModel.PROP_SORT, true), RepoConsts.MAX_RESULTS_256);
		int newSort = RepoConsts.SORT_DEFAULT_STEP;
		
		for (NodeRef listItem : listItems) {
			setProperty(listItem, BeCPGModel.PROP_SORT, newSort);
			newSort = newSort + RepoConsts.SORT_DEFAULT_STEP;
		}		
	}

	@Override
	public NodeRef getLastChild(NodeRef nodeRef) {
		
		if (!nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL)) {
			// depthLevel manage sort
			return null;
		}
		
		QName dataType = nodeService.getType(nodeRef);
		NodeRef parentLevel = (NodeRef) nodeService.getProperty(nodeRef, BeCPGModel.PROP_PARENT_LEVEL);
		NodeRef listContainer = nodeService.getPrimaryParent(nodeRef).getParentRef();

		return getLastChild(dataType, parentLevel, listContainer, nodeRef, true);
	}

	private NodeRef getLastChild(QName dataType, NodeRef destNodeRef, NodeRef listContainer, NodeRef nodeRef, boolean isDepthList) {
		
		/*
		 * Special case for drag & drop:
		 * 	- 1.1
		 * 	- 	2.1	//destNodeRef
		 * 	- 1.2
		 * 	- 1.3  	//nodeRef
		 * 	- 	2.2
		 * 	- 	2.3
		 * 
		 * We drag 1.3 on 2.1. Last child found is 2.3 but it should not be after 1.2 so we need to look for 1.2 to know when level stops
		 */				
		
		String stopSortCond = "MAX";
		Integer startSort = null;
		
		if(destNodeRef != null){
			
			logger.debug("getLastChild of " + tryGetName(destNodeRef));					
			startSort = (Integer)nodeService.getProperty(destNodeRef, BeCPGModel.PROP_SORT);		
			Integer level = (Integer)nodeService.getProperty(destNodeRef, BeCPGModel.PROP_DEPTH_LEVEL);
			
			
			if(startSort != null && level != null){
							
				String query = LuceneHelper.getCondParent(listContainer, null); 
				query += LuceneHelper.getCondMinMax(BeCPGModel.PROP_SORT, String.valueOf(startSort+1), "MAX", Operator.AND);
				query += LuceneHelper.getCondIsNullValue(BeCPGModel.PROP_SORT, Operator.NOT);
				query += LuceneHelper.getCondMinMax(BeCPGModel.PROP_DEPTH_LEVEL, "1", Integer.toString(level), Operator.AND);				
				
				List<NodeRef> listItems = luceneSearchByType(dataType, query, LuceneHelper.getSort(BeCPGModel.PROP_SORT, true), RepoConsts.MAX_RESULTS_SINGLE_VALUE);				
				if(logger.isDebugEnabled()){
					logger.debug(" - startSort: " + startSort + " - query: " + query + " size: " + listItems.size());
				}
				if(!listItems.isEmpty()){ 
					Integer stopSort = (Integer)nodeService.getProperty(listItems.get(0), BeCPGModel.PROP_SORT);
					logger.debug("stopSort: " + stopSort);				
					if(stopSort != null && startSort < stopSort){
						stopSortCond = stopSort.toString();
					}
				}
			}
		}
		
						
		logger.debug("startSort: " + startSort + " - stopCond: " + stopSortCond);

		String query = getQueryByParentLevel(listContainer, destNodeRef,isDepthList);
		query += LuceneHelper.getCondEqualID(nodeRef, Operator.NOT);		
		query += LuceneHelper.getCondMinMax(BeCPGModel.PROP_SORT, startSort!= null ? Integer.toString(startSort+1) : "1", stopSortCond, Operator.AND);
		
		List<NodeRef> listItems = luceneSearchByType(dataType, query, LuceneHelper.getSort(BeCPGModel.PROP_SORT, false), RepoConsts.MAX_RESULTS_SINGLE_VALUE);
		if (!listItems.isEmpty() ) {
			if(isDepthList){
				destNodeRef = getLastChild(dataType, listItems.get(0), listContainer, nodeRef, isDepthList);
				if(destNodeRef == null){
					destNodeRef = listItems.get(0);
				}
			} else {
				destNodeRef = listItems.get(0);
			}		
		} 
		return destNodeRef;
	}

	/*
	 * Get the last sibling node of the level
	 */
	private NodeRef getLastChildOfLevel(QName dataType, NodeRef listContainer, NodeRef parentLevel, NodeRef nodeRef) {

		String query = getQueryByParentLevel(listContainer, parentLevel,true);
		query += LuceneHelper.getCondEqualID(nodeRef, Operator.NOT);
		query += LuceneHelper.getCondIsNullValue(BeCPGModel.PROP_SORT, Operator.NOT);
		
		List<NodeRef> listItems = luceneSearchByType(dataType, query, LuceneHelper.getSort(BeCPGModel.PROP_SORT, false), RepoConsts.MAX_RESULTS_SINGLE_VALUE);
		return listItems!=null && !listItems.isEmpty() ? listItems.get(0) : null; 
	}
	
	private NodeRef getNextSiblingNode(QName dataType, NodeRef listContainer, NodeRef nodeRef, boolean moveUp) {

		Integer level = (Integer) nodeService.getProperty(nodeRef, BeCPGModel.PROP_DEPTH_LEVEL);
		Integer sort = (Integer) nodeService.getProperty(nodeRef, BeCPGModel.PROP_SORT);	
		
		String startSort = moveUp ? "MIN" : Integer.toString(sort+1);
		String endSort = moveUp ? Integer.toString(sort-1) : "MAX";		
		String query = LuceneHelper.getCondParent(listContainer, null); 
		query += LuceneHelper.getCondMinMax(BeCPGModel.PROP_SORT,startSort, endSort, Operator.AND);		
		if(level != null){
			query += LuceneHelper.getCondEqualValue(BeCPGModel.PROP_DEPTH_LEVEL, level.toString(), Operator.AND);
		}				
		
		List<NodeRef> destListItems = luceneSearchByType(dataType, query, 
										LuceneHelper.getSort(BeCPGModel.PROP_SORT, !moveUp), 
										RepoConsts.MAX_RESULTS_SINGLE_VALUE);
		
		return destListItems!=null && !destListItems.isEmpty() ? destListItems.get(0):null;
	}
	
	/*
	 * look for another node that has already the sort value
	 */
	private NodeRef getSortedNode(QName dataType, NodeRef listContainer, Integer sort, NodeRef nodeRef) {

		String query = LuceneHelper.getCondParent(listContainer, null); 
		query += LuceneHelper.getCondMinMax(BeCPGModel.PROP_SORT,String.valueOf(sort), "MAX", Operator.AND);
		query += LuceneHelper.getCondEqualValue(BeCPGModel.PROP_SORT, sort.toString(), Operator.AND);
		query += LuceneHelper.getCondEqualID(nodeRef, Operator.NOT);		
		
		List<NodeRef> listItems = luceneSearchByType(dataType, query, LuceneHelper.getSort(BeCPGModel.PROP_SORT, false), RepoConsts.MAX_RESULTS_SINGLE_VALUE);
		return listItems!=null && !listItems.isEmpty() ? listItems.get(0) : null;
	}
	
	/*
	 * Get the children of the children of the parent
	 */
	private List<NodeRef> getChildren(QName dataType, NodeRef listContainer, NodeRef parentLevel, boolean isDepthList) {

		return luceneSearchByType(dataType, getQueryByParentLevel(listContainer, parentLevel,isDepthList), LuceneHelper.getSort(BeCPGModel.PROP_SORT, true),
				RepoConsts.MAX_RESULTS_256);
	}

	/*
	 * Get the query that return children of parent
	 */
	private String getQueryByParentLevel(NodeRef listContainer, NodeRef parentLevel, boolean isDepthList) {

		String query = LuceneHelper.getCondParent(listContainer, null);		
		
		if (parentLevel == null) {
			if(isDepthList){
				query += LuceneHelper.getCondIsNullValue(BeCPGModel.PROP_PARENT_LEVEL, Operator.AND);
			}
		} else {
			query += LuceneHelper.getCondEqualValue(BeCPGModel.PROP_PARENT_LEVEL, parentLevel.toString(), Operator.AND);
		}

		return query;
	}

	/*
	 * Debug function used to get the name of the product stored in the
	 * compoList
	 */
	private String tryGetName(NodeRef nodeRef) {
		
		if(nodeRef == null){
			return null;
		}
		
		List<AssociationRef> compoAssocRefs = nodeService.getTargetAssocs(nodeRef, BeCPGModel.ASSOC_COMPOLIST_PRODUCT);
		NodeRef part = compoAssocRefs!=null &&  !compoAssocRefs.isEmpty() ? (compoAssocRefs.get(0)).getTargetRef() : null;

		return part != null ? (String) nodeService.getProperty(part, ContentModel.PROP_NAME) : (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
	}
	
	@Override
	public void deleteChildrens(NodeRef listContainer, NodeRef nodeRef) {

		List<NodeRef> listItems = getChildren(null, listContainer, nodeRef,true);

		for (NodeRef tmp : listItems) {
			if(nodeService.exists(tmp)){
				nodeService.deleteNode(tmp);
			}			
		}
	}

	@Override
	public void move(NodeRef nodeRef, boolean moveUp) {
		
		QName dataType = nodeService.getType(nodeRef);
		NodeRef listContainer = nodeService.getPrimaryParent(nodeRef).getParentRef();
		Integer sort = (Integer) nodeService.getProperty(nodeRef, BeCPGModel.PROP_SORT);		
		
		logger.debug("node: " + tryGetName(nodeRef));
		logger.debug("moveUp: " + moveUp);		

		// look for the right destNode (before or after sibling)							
		NodeRef destNodeRef = getNextSiblingNode(dataType, listContainer, nodeRef, moveUp);
		
		if(destNodeRef ==null){
			// cannot swap
			logger.debug("Cannot swap.");
		}				
		else{
						
			/*
			 *  Calculate children lists first, then update sort otherwise we get the second time all children			
			 */
		
			NodeRef lastChild = getLastChild(dataType, nodeRef, listContainer, null, true);								
			Integer lastSort = (Integer) nodeService.getProperty(lastChild, BeCPGModel.PROP_SORT);
			String query = LuceneHelper.getCondParent(listContainer, null); 
			query += LuceneHelper.getCondMinMax(BeCPGModel.PROP_SORT, String.valueOf(sort+1), String.valueOf(lastSort), Operator.AND);
			
			List<NodeRef> children = luceneSearchByType(dataType, query, 
																	LuceneHelper.getSort(BeCPGModel.PROP_SORT, true), 
																	RepoConsts.MAX_RESULTS_256);
						
			Integer destSort = (Integer) nodeService.getProperty(destNodeRef, BeCPGModel.PROP_SORT);
			NodeRef lastDestChild = getLastChild(dataType, destNodeRef, listContainer, null, true);
			Integer lastDestSort = (Integer) nodeService.getProperty(lastDestChild, BeCPGModel.PROP_SORT);
			query = LuceneHelper.getCondParent(listContainer, null); 
			query += LuceneHelper.getCondMinMax(BeCPGModel.PROP_SORT, String.valueOf(destSort+1), String.valueOf(lastDestSort), Operator.AND);			
			
			List<NodeRef> destChildren = luceneSearchByType(dataType, query, 
																	LuceneHelper.getSort(BeCPGModel.PROP_SORT, true), 
																	RepoConsts.MAX_RESULTS_256);
			
			// udpate sort of nodeRef and children
			int newSort = destSort;				
			for(NodeRef listItem : children){
				newSort++;
				setProperty(listItem, BeCPGModel.PROP_SORT, newSort);
			}
			
			// update sort of destNodeRef and children
			newSort = sort;			
			for(NodeRef listItem : destChildren){
				newSort++;
				setProperty(listItem, BeCPGModel.PROP_SORT, newSort);
			}					
			
			// swap parent
			setProperty(nodeRef, BeCPGModel.PROP_SORT, destSort);
			setProperty(destNodeRef, BeCPGModel.PROP_SORT, sort);
		}		
	}
	
	private void setProperty(NodeRef nodeRef, QName property, Serializable value){
		
		try {

			policyBehaviourFilter.disableBehaviour(nodeRef, BeCPGModel.ASPECT_SORTABLE_LIST);
			policyBehaviourFilter.disableBehaviour(nodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL);
			
			if (logger.isDebugEnabled()) {
				logger.debug("set property " + property + " with value " + value + " for node " + tryGetName(nodeRef));
			}
						
			nodeService.setProperty(nodeRef, property, value);
			
		} finally {
			policyBehaviourFilter.enableBehaviour(nodeRef, BeCPGModel.ASPECT_SORTABLE_LIST);
			policyBehaviourFilter.enableBehaviour(nodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL);
		}
	}
	
	private List<NodeRef> luceneSearchByType(QName dataType, String query, Map<String, Boolean> sort, int maxResult){
		
		//We need dataType since some lists can have several types in the same container like composition
		query += dataType != null ? LuceneHelper.getCond(LuceneHelper.getCondType(dataType), Operator.AND) : "";
		return beCPGSearchService.luceneSearch(query, sort,	maxResult);
	}

}
