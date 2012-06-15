package fr.becpg.repo.ecm.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ECMModel;
import fr.becpg.repo.BeCPGDao;
import fr.becpg.repo.data.hierarchicalList.AbstractComponent;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.ecm.ECOState;
import fr.becpg.repo.ecm.data.ChangeOrderData;
import fr.becpg.repo.ecm.data.ChangeOrderType;
import fr.becpg.repo.ecm.data.RevisionType;
import fr.becpg.repo.ecm.data.dataList.ChangeUnitDataItem;
import fr.becpg.repo.ecm.data.dataList.ReplacementListDataItem;
import fr.becpg.repo.ecm.data.dataList.SimulationListDataItem;
import fr.becpg.repo.ecm.data.dataList.WUsedListDataItem;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.productList.RequirementType;

/**
 * ECO DAO
 * @author quere
 *
 */
public class ChangeOrderDAOImpl implements BeCPGDao<ChangeOrderData>{
	
	private static Log logger = LogFactory.getLog(ChangeOrderDAOImpl.class);
	
	private NodeService nodeService;
	private EntityListDAO entityListDAO;
	private AssociationService associationService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}
	
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	@Override
	public NodeRef create(NodeRef parentNodeRef, ChangeOrderData ecoData) {
		
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		
		properties.put(ContentModel.PROP_NAME, ecoData.getName());
		properties.put(ECMModel.PROP_ECO_STATE, ecoData.getEcoState());		
		properties.put(ECMModel.PROP_ECO_TYPE, ecoData.getEcoType());
		
		NodeRef ecoNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, 
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(ecoData.getName())), 
								ECMModel.TYPE_ECO, properties).getChildRef();
		
		// calculated characts
		associationService.update(ecoNodeRef, ECMModel.ASSOC_CALCULATED_CHARACTS, ecoData.getCalculatedCharacts());
		
		createDataLists(ecoNodeRef, ecoData);
		
		return ecoNodeRef;
	}
	
	@Override
	public void update(NodeRef ecoNodeRef, ChangeOrderData ecoData) {
		
		nodeService.setProperty(ecoNodeRef, ContentModel.PROP_NAME, ecoData.getName());
		nodeService.setProperty(ecoNodeRef, ECMModel.PROP_ECO_STATE, ecoData.getEcoState());		
		nodeService.setProperty(ecoNodeRef, ECMModel.PROP_ECO_TYPE, ecoData.getEcoType());
		
		// calculated characts
		associationService.update(ecoNodeRef, ECMModel.ASSOC_CALCULATED_CHARACTS, ecoData.getCalculatedCharacts());
		
		createDataLists(ecoNodeRef, ecoData);		
	}

	@Override
	public ChangeOrderData find(NodeRef ecoNodeRef) {
		
		
		// properties		
		ECOState ecoState = null;
		String strEcoState = (String)nodeService.getProperty(ecoNodeRef, ECMModel.PROP_ECO_STATE);
		if(strEcoState != null){
			ecoState = ECOState.valueOf(strEcoState);
		}
		ChangeOrderType ecoType = null;
		String strECOType = (String)nodeService.getProperty(ecoNodeRef, ECMModel.PROP_ECO_TYPE);
		if(strECOType != null){
			ecoType = ChangeOrderType.valueOf(strECOType);
		}			

		// calculated characts
		List<AssociationRef> charactsAssocRefs = nodeService.getTargetAssocs(ecoNodeRef, ECMModel.ASSOC_CALCULATED_CHARACTS);
		List<NodeRef> calculatedCharacts = new ArrayList<NodeRef>(charactsAssocRefs.size());
		for(AssociationRef assocRef : charactsAssocRefs){
			calculatedCharacts.add(assocRef.getTargetRef());
		}
		
		ChangeOrderData ecoData = new ChangeOrderData(ecoNodeRef, 
										(String)nodeService.getProperty(ecoNodeRef, ContentModel.PROP_NAME),
										(String)nodeService.getProperty(ecoNodeRef, BeCPGModel.PROP_CODE),
										ecoState, 
										ecoType,
										calculatedCharacts);
		
		// datalists
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(ecoNodeRef);
		if(listContainerNodeRef != null){
			ecoData.setReplacementList(loadReplacementList(listContainerNodeRef));
			ecoData.setWUsedList(loadWUsedList(listContainerNodeRef));
			ecoData.setChangeUnitMap(loadChangeUnitMap(listContainerNodeRef));
			ecoData.setSimulationList(loadSimulationList(listContainerNodeRef));
		}
		
		return ecoData;
	}

	@Override
	public void delete(NodeRef ecoNodeRef) {
		
		nodeService.deleteNode(ecoNodeRef);		
	}

	private void createDataLists(NodeRef ecoNodeRef, ChangeOrderData ecoData){
		
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(ecoNodeRef);
		if(listContainerNodeRef == null){
			listContainerNodeRef = entityListDAO.createListContainer(ecoNodeRef);			
		}
		
		//replacementList
		createReplacementList(listContainerNodeRef, ecoData.getReplacementList());
		
		//wUsedList
		createWUsedList(listContainerNodeRef, ecoData.getWUsedList());
		
		//changeUnitList
		createChangeUnitList(listContainerNodeRef, ecoData.getChangeUnitMap());
		
		//simulationList
		createSimulationList(listContainerNodeRef, ecoData.getSimulationList());
	}
	/**
	 * Create/Update replacement items.
	 *
	 * @param listContainerNodeRef the list container node ref
	 * @param replacementList the replacement list
	 * @throws InvalidTypeException the invalid type exception
	 */
	private void createReplacementList(NodeRef listContainerNodeRef, List<ReplacementListDataItem> replacementList) throws InvalidTypeException
	{
		
		if(listContainerNodeRef != null)
		{  
			NodeRef replacementListNodeRef = entityListDAO.getList(listContainerNodeRef, ECMModel.TYPE_REPLACEMENTLIST);
			
			if(replacementList == null){
				//delete existing list
				if(replacementListNodeRef != null)
					nodeService.deleteNode(replacementListNodeRef);
			}
			else{    			
	    		//replacement list, create if needed	    		
	    		if(replacementListNodeRef == null)
	    		{		    						
		    		replacementListNodeRef = entityListDAO.createList(listContainerNodeRef, ECMModel.TYPE_REPLACEMENTLIST);
	    		}
			
	    		List<NodeRef> listItemNodeRefs = listItems(replacementListNodeRef, ECMModel.TYPE_REPLACEMENTLIST);
	    		
	    		//create temp list
	    		List<NodeRef> replacementListToTreat = new ArrayList<NodeRef>();
	    		for(ReplacementListDataItem replacementListDataItem : replacementList){
	    			replacementListToTreat.add(replacementListDataItem.getNodeRef());
	    		}
	    		
	    		//remove deleted nodes
	    		List<NodeRef> filesToUpdate = new ArrayList<NodeRef>();
	    		for(NodeRef listItemNodeRef : listItemNodeRefs){	    			
		    		
	    			if(!replacementListToTreat.contains(listItemNodeRef)){
	    				//delete
	    				nodeService.deleteNode(listItemNodeRef);
	    			}
	    			else{
	    				filesToUpdate.add(listItemNodeRef);
	    			}
	    		}
	    		
	    		//update or create nodes	  
	    		int sortIndex = 1;
	    		for(ReplacementListDataItem replacementListDataItem : replacementList)
	    		{    			
	    			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		    		properties.put(ECMModel.PROP_REVISION, replacementListDataItem.getRevision());
		    		
		    		properties.put(BeCPGModel.PROP_SORT, sortIndex);
		    		sortIndex++;
		    		
		    		if(filesToUpdate.contains(replacementListDataItem.getNodeRef())){
		    			//update
		    			nodeService.setProperties(replacementListDataItem.getNodeRef(), properties);		    			
		    		}
		    		else{
		    			//create
		    			ChildAssociationRef childAssocRef = nodeService.createNode(replacementListNodeRef, 
		    						ContentModel.ASSOC_CONTAINS, 
		    						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()), 
		    						ECMModel.TYPE_REPLACEMENTLIST, 
		    						properties);
		    			
			    		nodeService.createAssociation(childAssocRef.getChildRef(), replacementListDataItem.getSourceItem(), ECMModel.ASSOC_SOURCE_ITEM);
			    		nodeService.createAssociation(childAssocRef.getChildRef(), replacementListDataItem.getTargetItem(), ECMModel.ASSOC_TARGET_ITEM);
		    		}			    			    	
	    		}
			}
		}
	} 
	
	/**
	 * Create/Update WUsed items.
	 *
	 * @param listContainerNodeRef the list container node ref
	 * @param WUsedList the Wused list
	 * @throws InvalidTypeException the invalid type exception
	 */
	private void createWUsedList(NodeRef listContainerNodeRef, List<WUsedListDataItem> wUsedList) throws InvalidTypeException
	{
		
		if(listContainerNodeRef != null)
		{  
			NodeRef wUsedListNodeRef = entityListDAO.getList(listContainerNodeRef, ECMModel.TYPE_WUSEDLIST);
			
			if(wUsedList == null){
				//delete existing list
				if(wUsedListNodeRef != null)
					nodeService.deleteNode(wUsedListNodeRef);
			}
			else{    			
	    		//wUsed list, create if needed	    		
	    		if(wUsedListNodeRef == null)
	    		{		    						
		    		wUsedListNodeRef = entityListDAO.createList(listContainerNodeRef, ECMModel.TYPE_WUSEDLIST);
	    		}
			
	    		List<NodeRef> listItemNodeRefs = listItems(wUsedListNodeRef, ECMModel.TYPE_WUSEDLIST);
	    		
	    		//create temp list
	    		List<NodeRef> wUsedListToTreat = new ArrayList<NodeRef>();
	    		for(WUsedListDataItem wUsedListDataItem : wUsedList){
	    			wUsedListToTreat.add(wUsedListDataItem.getNodeRef());
	    		}
	    		
	    		//remove deleted nodes
	    		List<NodeRef> filesToUpdate = new ArrayList<NodeRef>();
	    		for(NodeRef listItemNodeRef : listItemNodeRefs){	    			
		    		
	    			if(!wUsedListToTreat.contains(listItemNodeRef)){
	    				//delete
	    				nodeService.deleteNode(listItemNodeRef);
	    			}
	    			else{
	    				filesToUpdate.add(listItemNodeRef);
	    			}
	    		}
	    		
	    		logger.debug("createWUsed, size: " + wUsedList.size());
	    		
	    		Composite<WUsedListDataItem> composite = WUsedListDataItem.getHierarchicalCompoList(wUsedList);
				int sortIndex = 1;
				createCompositeWUsedListItem(wUsedListNodeRef, composite, filesToUpdate, sortIndex);	    			    		
			}
		}
	} 
	
	//TODO : SAME CODE FOR COMPOLISTDATAITEM !!!	
	private int createCompositeWUsedListItem(NodeRef wUsedListNodeRef, Composite<WUsedListDataItem> composite, List<NodeRef> filesToUpdate, int sortIndex) {

		for(AbstractComponent<WUsedListDataItem> component : composite.getChildren()){
			
			WUsedListDataItem wUsedListDataItem = component.getData();
			
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
    		properties.put(BeCPGModel.PROP_DEPTH_LEVEL, wUsedListDataItem.getDepthLevel());
    		properties.put(ECMModel.PROP_WUL_IS_WUSED_IMPACTED, wUsedListDataItem.getIsWUsedImpacted());
    		properties.put(ECMModel.PROP_WUL_IMPACTED_DATALIST, wUsedListDataItem.getImpactedDataList());
    		
    		properties.put(BeCPGModel.PROP_SORT, sortIndex);
    		sortIndex++;
    		
    		if(filesToUpdate.contains(wUsedListDataItem.getNodeRef())){
    			//update
    			nodeService.setProperties(wUsedListDataItem.getNodeRef(), properties);		    			
    		}
    		else{
				//create
				ChildAssociationRef childAssocRef = nodeService.createNode(wUsedListNodeRef, 
							ContentModel.ASSOC_CONTAINS, 
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()), 
							ECMModel.TYPE_WUSEDLIST, 
							properties);
				
				wUsedListDataItem.setNodeRef(childAssocRef.getChildRef());
    		}
    		
    		associationService.update(wUsedListDataItem.getNodeRef(), ECMModel.ASSOC_WUL_LINK, wUsedListDataItem.getLink());
    		associationService.update(wUsedListDataItem.getNodeRef(), ECMModel.ASSOC_WUL_SOURCE_ITEM, wUsedListDataItem.getSourceItem());		    			
			
    		// store parentLevel if level > 1
			if (wUsedListDataItem.getDepthLevel() > 1) {
	
				WUsedListDataItem compositeCompoListDataItem = composite.getData();				
				nodeService.setProperty(wUsedListDataItem.getNodeRef(), BeCPGModel.PROP_PARENT_LEVEL, compositeCompoListDataItem.getNodeRef());
			}

			if (component instanceof Composite) {

				sortIndex = createCompositeWUsedListItem(wUsedListNodeRef, (Composite<WUsedListDataItem>) component, filesToUpdate, sortIndex);
			}
		}

		return sortIndex;
	}
	
	/**
	 * Create/Update ChangeUnit items.
	 *
	 * @param listContainerNodeRef the list container node ref
	 * @param WUsedList the Wused list
	 * @throws InvalidTypeException the invalid type exception
	 */
	private void createChangeUnitList(NodeRef listContainerNodeRef, Map<NodeRef, ChangeUnitDataItem> changeUnitMap) throws InvalidTypeException
	{
		
		if(listContainerNodeRef != null)
		{  
			NodeRef changeUnitListNodeRef = entityListDAO.getList(listContainerNodeRef, ECMModel.TYPE_CHANGEUNITLIST);
			
			if(changeUnitMap == null){
				//delete existing list
				if(changeUnitListNodeRef != null)
					nodeService.deleteNode(changeUnitListNodeRef);
			}
			else{    			
	    		//wUsed list, create if needed	    		
	    		if(changeUnitListNodeRef == null)
	    		{		    						
		    		changeUnitListNodeRef = entityListDAO.createList(listContainerNodeRef, ECMModel.TYPE_CHANGEUNITLIST);
	    		}
			
	    		List<NodeRef> listItemNodeRefs = listItems(changeUnitListNodeRef, ECMModel.TYPE_CHANGEUNITLIST);
	    		
	    		//create temp list
	    		List<NodeRef> changeUnitListToTreat = new ArrayList<NodeRef>();
	    		for(ChangeUnitDataItem changeUnitListDataItem : changeUnitMap.values()){
	    			changeUnitListToTreat.add(changeUnitListDataItem.getNodeRef());
	    		}
	    		
	    		//remove deleted nodes
	    		List<NodeRef> filesToUpdate = new ArrayList<NodeRef>();
	    		for(NodeRef listItemNodeRef : listItemNodeRefs){	    			
		    		
	    			if(!changeUnitListToTreat.contains(listItemNodeRef)){
	    				//delete
	    				nodeService.deleteNode(listItemNodeRef);
	    			}
	    			else{
	    				filesToUpdate.add(listItemNodeRef);
	    			}
	    		}
	    		
	    		logger.debug("createChangeUnit, size: " + changeUnitMap.values().size());
	    		
	    		//update or create nodes	  	    		
	    		for(ChangeUnitDataItem changeUnitListDataItem : changeUnitMap.values())
	    		{
	    			NodeRef changeUnitNodeRef = changeUnitListDataItem.getNodeRef();
	    			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		    		properties.put(ECMModel.PROP_CUL_REQ_DETAILS, changeUnitListDataItem.getReqDetails());
		    		properties.put(ECMModel.PROP_CUL_REQ_TYPE, changeUnitListDataItem.getReqType());
		    		properties.put(ECMModel.PROP_CUL_REVISION, changeUnitListDataItem.getRevision());
		    		properties.put(ECMModel.PROP_CUL_TREATED, changeUnitListDataItem.getTreated());
		    				    		
		    		if(filesToUpdate.contains(changeUnitNodeRef)){
		    			//update
		    			nodeService.setProperties(changeUnitNodeRef, properties);		    			
		    		}
		    		else{
		    			//create
		    			changeUnitNodeRef = nodeService.createNode(changeUnitListNodeRef, 
		    						ContentModel.ASSOC_CONTAINS, 
		    						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()), 
		    						ECMModel.TYPE_CHANGEUNITLIST, 
		    						properties).getChildRef();		    					    					    			
		    		}			    					    		
		    		
		    		associationService.update(changeUnitNodeRef, ECMModel.ASSOC_CUL_SOURCE_ITEM, changeUnitListDataItem.getSourceItem());
		    		associationService.update(changeUnitNodeRef, ECMModel.ASSOC_CUL_TARGET_ITEM, changeUnitListDataItem.getTargetItem());
		    		associationService.update(changeUnitNodeRef, ECMModel.ASSOC_CUL_SIMULATION_ITEM, changeUnitListDataItem.getSimulationItem());
	    		}
			}
		}
	} 
	
	/**
	 * Create/Update Simulation items.
	 *
	 * @param listContainerNodeRef the list container node ref
	 * @param SimulationList the Simationul list
	 * @throws InvalidTypeException the invalid type exception
	 */
	private void createSimulationList(NodeRef listContainerNodeRef, List<SimulationListDataItem> simulationList) throws InvalidTypeException
	{
		
		if(listContainerNodeRef != null)
		{  
			NodeRef simulationListNodeRef = entityListDAO.getList(listContainerNodeRef, ECMModel.TYPE_CALCULATEDCHARACTLIST);
			
			if(simulationList == null){
				//delete existing list
				if(simulationListNodeRef != null)
					nodeService.deleteNode(simulationListNodeRef);
			}
			else{    			
	    		//simulation list, create if needed	    		
	    		if(simulationListNodeRef == null)
	    		{		    						
		    		simulationListNodeRef = entityListDAO.createList(listContainerNodeRef, ECMModel.TYPE_CALCULATEDCHARACTLIST);
	    		}
				
	    		List<NodeRef> listItemNodeRefs = listItems(simulationListNodeRef, ECMModel.TYPE_CALCULATEDCHARACTLIST);
	    		
	    		//create temp list
	    		List<NodeRef> simulationListToTreat = new ArrayList<NodeRef>();
	    		for(SimulationListDataItem simulationListDataItem : simulationList){
	    			simulationListToTreat.add(simulationListDataItem.getNodeRef());
	    		}
	    		
	    		//remove deleted nodes
	    		List<NodeRef> filesToUpdate = new ArrayList<NodeRef>();
	    		for(NodeRef listItemNodeRef : listItemNodeRefs){	    			
		    		
	    			if(!simulationListToTreat.contains(listItemNodeRef)){
	    				//delete
	    				nodeService.deleteNode(listItemNodeRef);
	    			}
	    			else{
	    				filesToUpdate.add(listItemNodeRef);
	    			}
	    		}
	    		
	    		//update or create nodes	  
	    		for(SimulationListDataItem simulationListDataItem : simulationList)
	    		{    				    				    			
	    			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		    		properties.put(ECMModel.PROP_CCL_SOURCE_VALUE, simulationListDataItem.getSourceValue());
		    		properties.put(ECMModel.PROP_CCL_TARGET_VALUE, simulationListDataItem.getTargetValue());		    				    		
		    		
		    		if(filesToUpdate.contains(simulationListDataItem.getNodeRef())){
		    			//update
		    			nodeService.setProperties(simulationListDataItem.getNodeRef(), properties);		    			
		    		}
		    		else{
		    			//create
		    			ChildAssociationRef childAssocRef = nodeService.createNode(simulationListNodeRef, 
		    						ContentModel.ASSOC_CONTAINS, 
		    						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()), 
		    						ECMModel.TYPE_CALCULATEDCHARACTLIST, 
		    						properties);
		    					    			
		    			nodeService.createAssociation(childAssocRef.getChildRef(), simulationListDataItem.getSourceItem(), ECMModel.ASSOC_CCL_SOURCE_ITEM);
		    			nodeService.createAssociation(childAssocRef.getChildRef(), simulationListDataItem.getCharact(), ECMModel.ASSOC_CCL_CHARACT);
		    		}			    					    				    		
	    		}
			}
		}
	} 
	
	private List<ReplacementListDataItem> loadReplacementList(NodeRef listContainerNodeRef){
		
		List<ReplacementListDataItem> replacementList = null;
		
    	if(listContainerNodeRef != null)
    	{    		    		
    		NodeRef replacementListNodeRef = entityListDAO.getList(listContainerNodeRef, ECMModel.TYPE_REPLACEMENTLIST);
    		
    		if(replacementListNodeRef != null)
    		{    			
    			replacementList = new ArrayList<ReplacementListDataItem>();
				List<NodeRef> nodeRefs = listItems(replacementListNodeRef, ECMModel.TYPE_REPLACEMENTLIST);	    
				
	    		for(NodeRef nodeRef : nodeRefs)
		    	{	    			
	    			RevisionType revisionType = RevisionType.valueOf((String)nodeService.getProperty(nodeRef, ECMModel.PROP_REVISION));
		    		
		    		List<AssociationRef> sourceItemAssocRefs = nodeService.getTargetAssocs(nodeRef, ECMModel.ASSOC_SOURCE_ITEM);
		    		NodeRef sourceItemNodeRef = sourceItemAssocRefs.isEmpty() ? null : (sourceItemAssocRefs.get(0)).getTargetRef();
		    		
		    		List<AssociationRef> targetItemAssocRefs = nodeService.getTargetAssocs(nodeRef, ECMModel.ASSOC_TARGET_ITEM);
		    		NodeRef targetItemNodeRef = targetItemAssocRefs.isEmpty() ? null : (targetItemAssocRefs.get(0)).getTargetRef();
		    				    		
		    		ReplacementListDataItem replacementListDataItem = new ReplacementListDataItem(nodeRef, revisionType, sourceItemNodeRef, targetItemNodeRef);		    		
		    		replacementList.add(replacementListDataItem);
		    	}
    		}    		
    	}
    	
    	return replacementList;
	}
	
	private List<WUsedListDataItem> loadWUsedList(NodeRef listContainerNodeRef){
		
		List<WUsedListDataItem> wUsedList = null;
		
    	if(listContainerNodeRef != null)
    	{    		
    		NodeRef wUsedListNodeRef = entityListDAO.getList(listContainerNodeRef, ECMModel.TYPE_WUSEDLIST);
    		
    		if(wUsedListNodeRef != null)
    		{    			
    			wUsedList = new ArrayList<WUsedListDataItem>();
				List<NodeRef> nodeRefs = listItems(wUsedListNodeRef, ECMModel.TYPE_WUSEDLIST);
	    		
				logger.debug("loadWUsed, size: " + nodeRefs.size());
				
	    		for(NodeRef nodeRef : nodeRefs)
		    	{	
		    		List<AssociationRef> sourceItemAssocRefs = nodeService.getTargetAssocs(nodeRef, ECMModel.ASSOC_WUL_SOURCE_ITEM);
		    		NodeRef sourceItemNodeRef = sourceItemAssocRefs.isEmpty() ? null : (sourceItemAssocRefs.get(0)).getTargetRef();		    		
		    		
		    		List<AssociationRef> linkAssocRefs = nodeService.getTargetAssocs(nodeRef, ECMModel.ASSOC_WUL_LINK);
		    		NodeRef linkNodeRef = linkAssocRefs.isEmpty() ? null : (linkAssocRefs.get(0)).getTargetRef();
		    		
		    		WUsedListDataItem wUsedListDataItem = new WUsedListDataItem(nodeRef, 
		    																	(Integer)nodeService.getProperty(nodeRef, BeCPGModel.PROP_DEPTH_LEVEL),		    																	
		    																	(QName)nodeService.getProperty(nodeRef, ECMModel.PROP_WUL_IMPACTED_DATALIST),
		    																	(Boolean)nodeService.getProperty(nodeRef, ECMModel.PROP_WUL_IS_WUSED_IMPACTED),
		    																	linkNodeRef,
		    																	sourceItemNodeRef);
		    		wUsedList.add(wUsedListDataItem);
		    	}
    		}    		
    	}
    	
    	return wUsedList;
	}
	
	private Map<NodeRef, ChangeUnitDataItem> loadChangeUnitMap(NodeRef listContainerNodeRef){
		
		Map<NodeRef, ChangeUnitDataItem> changeUnitMap = null;
		
    	if(listContainerNodeRef != null)
    	{    		
    		NodeRef changeUnitListNodeRef = entityListDAO.getList(listContainerNodeRef, ECMModel.TYPE_CHANGEUNITLIST);
    		
    		if(changeUnitListNodeRef != null)
    		{    			
    			changeUnitMap = new LinkedHashMap<NodeRef, ChangeUnitDataItem>();
				List<NodeRef> nodeRefs = listItems(changeUnitListNodeRef, ECMModel.TYPE_CHANGEUNITLIST);
				
				logger.debug("loadChangeUnit, size: " + nodeRefs.size());
	    		
	    		for(NodeRef nodeRef : nodeRefs)
		    	{	
		    		List<AssociationRef> sourceItemAssocRefs = nodeService.getTargetAssocs(nodeRef, ECMModel.ASSOC_CUL_SOURCE_ITEM);
		    		NodeRef sourceItemNodeRef = sourceItemAssocRefs.isEmpty() ? null : (sourceItemAssocRefs.get(0)).getTargetRef();		    		
		    		
		    		List<AssociationRef> targetItemAssocRefs = nodeService.getTargetAssocs(nodeRef, ECMModel.ASSOC_CUL_TARGET_ITEM);
		    		NodeRef targetItemNodeRef = targetItemAssocRefs.isEmpty() ? null : (targetItemAssocRefs.get(0)).getTargetRef();
		    		
		    		List<AssociationRef> simulationItemAssocRefs = nodeService.getTargetAssocs(nodeRef, ECMModel.ASSOC_CUL_SIMULATION_ITEM);
		    		NodeRef simulationItemNodeRef = simulationItemAssocRefs.isEmpty() ? null : (simulationItemAssocRefs.get(0)).getTargetRef();
		    		
		    		RevisionType revision = null;
		    		String strRevision = (String)nodeService.getProperty(nodeRef, ECMModel.PROP_CUL_REVISION);
		    		if(strRevision != null){
		    			revision = RevisionType.valueOf(strRevision);
		    		}
		    		
		    		RequirementType reqType = null;
		    		String strReqType = (String)nodeService.getProperty(nodeRef, ECMModel.PROP_CUL_REQ_TYPE);
		    		if(strReqType != null){
		    			reqType = RequirementType.valueOf(strReqType);
		    		}
		    		
		    		logger.debug("revision: " + revision);
		    		
		    		ChangeUnitDataItem changeUnitDataItem = new ChangeUnitDataItem(nodeRef, 
		    												revision, 
		    												reqType, 
		    												(String)nodeService.getProperty(nodeRef, ECMModel.PROP_CUL_REQ_DETAILS), 
		    												(Boolean)nodeService.getProperty(nodeRef, ECMModel.PROP_CUL_TREATED), 
		    												sourceItemNodeRef, 
		    												targetItemNodeRef,
		    												simulationItemNodeRef);
		    		changeUnitMap.put(sourceItemNodeRef, changeUnitDataItem);
		    	}
    		}    		
    	}
    	
    	return changeUnitMap;
	}
	
	private List<SimulationListDataItem> loadSimulationList(NodeRef listContainerNodeRef){
		
		List<SimulationListDataItem> simList = null;
		
    	if(listContainerNodeRef != null)
    	{    		
    		NodeRef simulationListNodeRef = entityListDAO.getList(listContainerNodeRef, ECMModel.TYPE_CALCULATEDCHARACTLIST);
    		
    		if(simulationListNodeRef != null)
    		{    			
    			simList = new ArrayList<SimulationListDataItem>();
				List<NodeRef> nodeRefs = listItems(simulationListNodeRef, ECMModel.TYPE_CALCULATEDCHARACTLIST);
	    		
	    		for(NodeRef nodeRef : nodeRefs)
		    	{	
		    		List<AssociationRef> sourceAssocRefs = nodeService.getTargetAssocs(nodeRef, ECMModel.ASSOC_CCL_SOURCE_ITEM);
		    		NodeRef sourceNodeRef = sourceAssocRefs.isEmpty() ? null : (sourceAssocRefs.get(0)).getTargetRef();		    		
		    		
		    		List<AssociationRef> charactAssocRefs = nodeService.getTargetAssocs(nodeRef, ECMModel.ASSOC_CCL_CHARACT);
		    		NodeRef charactNodeRef = charactAssocRefs.isEmpty() ? null : (charactAssocRefs.get(0)).getTargetRef();
		    		
		    		SimulationListDataItem simListDataItem = new SimulationListDataItem(nodeRef,
		    																	sourceNodeRef,
		    																	charactNodeRef,		    																	
		    																	(Double)nodeService.getProperty(nodeRef, ECMModel.PROP_CCL_SOURCE_VALUE),
		    																	(Double)nodeService.getProperty(nodeRef, ECMModel.PROP_CCL_TARGET_VALUE));
		    		simList.add(simListDataItem);
		    	}
    		}    		
    	}
    	
    	return simList;
	}
	
	/*
	 * List the list items
	 */
	//TODO : refactor with ProdutDAOImpl
	private List<NodeRef> listItems(NodeRef parentNodeRef, QName listItemType){
		
		Set<QName> searchTypeQNames = new HashSet<QName>(1);
		searchTypeQNames.add(listItemType);
		
        // Do the query
        List<ChildAssociationRef> childAssocRefs = nodeService.getChildAssocs(parentNodeRef, searchTypeQNames);
        List<NodeRef> result = new ArrayList<NodeRef>(childAssocRefs.size());
        for (ChildAssociationRef assocRef : childAssocRefs){
        	
            result.add(assocRef.getChildRef());
        }
        // Done
        return result;		
    }
}
