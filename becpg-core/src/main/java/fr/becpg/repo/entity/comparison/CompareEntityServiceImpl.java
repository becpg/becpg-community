/*
 * 
 */
package fr.becpg.repo.entity.comparison;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.config.format.PropertyFormats;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AttributeExtractorService;

// TODO: Auto-generated Javadoc
/**
 * Compare several entities (properties, datalists and composite datalists).
 *
 * @author querephi
 */
public class CompareEntityServiceImpl implements CompareEntityService {		
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(CompareEntityServiceImpl.class);
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The dictionary service. */
	private DictionaryService dictionaryService;	
	
	/** The namespace service. */
	private NamespaceService namespaceService;
	
	private AttributeExtractorService attributeExtractorService;
	
	private EntityDictionaryService entityDictionaryService;
	
	private EntityListDAO entityListDAO;
			
	/**
	 * Sets the node service.
	 *
	 * @param nodeService the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	/**
	 * Sets the dictionary service.
	 *
	 * @param dictionaryService the new dictionary service
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}
	
	
	
	
	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	/**
	 * Sets the namespace service.
	 *
	 * @param namespaceService the new namespace service
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}
	

	public void setAttributeExtractorService(AttributeExtractorService attributeExtractorService) {
		this.attributeExtractorService = attributeExtractorService;
	}


	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	/* (non-Javadoc)
	 * @see fr.becpg.repo.entity.comparison.CompareEntitieService#compare(org.alfresco.service.cmr.repository.NodeRef, java.util.List)
	 */
	@Override
	public List<CompareResultDataItem> compare(NodeRef entity1,
			List<NodeRef> entities) {
		
		Map<String, CompareResultDataItem> comparisonMap = new HashMap<String, CompareResultDataItem>();		
		int pos = 1;
		int nbEntities = entities.size() + 1;		
		
		for(NodeRef entity : entities){
			compareEntities(entity1, entity, nbEntities,  pos, comparisonMap);
			pos++;
		}
		
		List<CompareResultDataItem> compareResult = new ArrayList<CompareResultDataItem>();
		for(CompareResultDataItem c : comparisonMap.values())
			compareResult.add(c);
		
		return compareResult;
	}
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.ef, BeCPGModel.PROP_VERSION_LABEL);
					versionLabel = versionLabel == null ? VERSION_INITIAL : versionLabel;entity.comparison.CompareEntitieService#compareStructDatalist(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, org.alfresco.service.namespace.QName)
	 */
	@Override
	public List<StructCompareResultDataItem> compareStructDatalist(NodeRef entity1NodeRef, NodeRef entity2NodeRef, QName datalistType, QName pivotProperty) {
		
		List<StructCompareResultDataItem> structComparisonList = new LinkedList<StructCompareResultDataItem>();
		
		// load the 2 datalists		
		List<NodeRef> dataListItems1 = null;
		List<NodeRef> dataListItems2 = null;
		
		NodeRef listsContainer1NodeRef = entityListDAO.getListContainer(entity1NodeRef);
		if(listsContainer1NodeRef != null){
			NodeRef list1NodeRef = entityListDAO.getList(listsContainer1NodeRef, datalistType);
			
			if(list1NodeRef != null){
				dataListItems1 = entityListDAO.getListItems(list1NodeRef, datalistType);
			}
		}
		
		NodeRef listsContainer2NodeRef = entityListDAO.getListContainer(entity2NodeRef);
		if(listsContainer2NodeRef != null){
			NodeRef list2NodeRef = entityListDAO.getList(listsContainer2NodeRef, datalistType);
			
			if(list2NodeRef != null){
				dataListItems2 = entityListDAO.getListItems(list2NodeRef, datalistType);
			}
		}
		
		// load the 2 composite datalists
		CompositeComparableItem compositeItem1 = new CompositeComparableItem(0, null, null);
		loadCompositeDataList(dataListItems1, pivotProperty, 1, 0, compositeItem1);
		
		CompositeComparableItem compositeItem2 = new CompositeComparableItem(0, null, null);
		loadCompositeDataList(dataListItems2, pivotProperty, 1, 0, compositeItem2);
		
		structCompareCompositeDataLists(datalistType, pivotProperty, structComparisonList, compositeItem1, compositeItem2);		
		
		return structComparisonList;
	}
	
	
	/**
	 * Compare 2 entities.
	 *
	 * @param entity1NodeRef the entity1 node ref
	 * @param entity2NodeRef the entity2 node ref
	 * @param nbEntities the nb entities
	 * @param comparisonPosition the comparison position
	 * @param comparisonMap the comparison map
	 */
	private void compareEntities(NodeRef entity1NodeRef, NodeRef entity2NodeRef, int nbEntities, int comparisonPosition, Map<String, CompareResultDataItem> comparisonMap){
		
		//compare entity properties		
		compareNode(null, null, null, entity1NodeRef, entity2NodeRef, nbEntities, comparisonPosition, false, comparisonMap);
		
		//load datalists
		List<String> comparedDataLists = new ArrayList<String>();
		List<NodeRef> dataLists1 = new ArrayList<NodeRef>();    	
		NodeRef listsContainer1NodeRef = entityListDAO.getListContainer(entity1NodeRef);
		if(listsContainer1NodeRef != null){
			dataLists1 = entityListDAO.getExistingListsNodeRef(listsContainer1NodeRef);
		}
		
		List<NodeRef> dataLists2 = new ArrayList<NodeRef>();		
		NodeRef listsContainer2NodeRef = entityListDAO.getListContainer(entity2NodeRef);
		if(listsContainer2NodeRef != null){
			dataLists2 = entityListDAO.getExistingListsNodeRef(listsContainer2NodeRef);
		}
    	
    	
    	for(NodeRef dataList1 : dataLists1){
    		    		
    		String dataListType1 = (String)nodeService.getProperty(dataList1, DataListModel.PROP_DATALISTITEMTYPE);
    		comparedDataLists.add(dataListType1);
    		
    		// look for dataList2
			NodeRef dataList2NodeRef = null;
			if(dataLists2 != null){
				for(NodeRef d : dataLists2){
					String dataListType2 = (String)nodeService.getProperty(d, DataListModel.PROP_DATALISTITEMTYPE);
					if(dataListType1.equals(dataListType2)){
						dataList2NodeRef = d;
					}
				}
			}
			
			logger.debug("datalist type str: " + dataListType1);
			
			compareDataLists(QName.createQName(dataListType1, namespaceService), dataList1, dataList2NodeRef, nbEntities, comparisonPosition, comparisonMap);
    	}
    	
    	//compare dataLists2 that have not been compared
    	for(NodeRef dataList2 : dataLists2){
    		
    		String dataListType2 = (String)nodeService.getProperty(dataList2, DataListModel.PROP_DATALISTITEMTYPE);
    		
    		if(comparedDataLists.contains(dataListType2)){
    			continue;
    		}
    		
    		comparedDataLists.add(dataListType2);
			logger.debug("datalist type str: " + dataListType2);
			
			compareDataLists(QName.createQName(dataListType2, namespaceService), null, dataList2, nbEntities, comparisonPosition, comparisonMap);
    	}
	}
	
	/**
	 * Compare 2 entity data lists.
	 *
	 * @param entityList the entity list
	 * @param dataList1NodeRef the data list1 node ref
	 * @param dataList2NodeRef the data list2 node ref
	 * @param nbEntities the nb entities
	 * @param comparisonPosition the comparison position
	 * @param comparisonMap the comparison map
	 */
	private void compareDataLists(QName entityList, NodeRef dataList1NodeRef, NodeRef dataList2NodeRef, int nbEntities, int comparisonPosition, Map<String, CompareResultDataItem> comparisonMap){
		
		QName pivotProperty = entityDictionaryService.getDefaultPivotAssoc(entityList);
		boolean isCompositeDL = false;
		
		logger.debug("pivotProperty: " + pivotProperty);					
		
		//load characteristics
		List<NodeRef> dataListItems1 = new ArrayList<NodeRef>();
		if(dataList1NodeRef != null){
			dataListItems1 = entityListDAO.getListItems(dataList1NodeRef, entityList);
		}
		
		List<NodeRef> dataListItems2 = new ArrayList<NodeRef>();		
		if(dataList2NodeRef != null){
			dataListItems2 = entityListDAO.getListItems(dataList2NodeRef, entityList);
		}
		
		
		// look for characteristics that are in both entity
		List<CharacteristicToCompare> characteristicsToCmp = new ArrayList<CharacteristicToCompare>();
		List<NodeRef> comparedCharact = new ArrayList<NodeRef>();
		
		// composite datalist
		if(isCompositeDL){		
			
			CompositeComparableItem compositeItem1 = new CompositeComparableItem(0, null, null);
			loadCompositeDataList(dataListItems1, pivotProperty, 1, 0, compositeItem1);
			
			CompositeComparableItem compositeItem2 = new CompositeComparableItem(0, null, null);
			loadCompositeDataList(dataListItems2, pivotProperty, 1, 0, compositeItem2);
			
			List<NodeRef> charactPath = new ArrayList<NodeRef>();
			compareCompositeDataLists(charactPath, characteristicsToCmp, compositeItem1, compositeItem2);
		}
		// not a composite datalist
		else{
			
			for(NodeRef dataListItem1 : dataListItems1){
				
				NodeRef dataListItem2NodeRef = null;							
				
				List<AssociationRef> target1Refs = nodeService.getTargetAssocs(dataListItem1, pivotProperty);
				NodeRef characteristicNodeRef = null;
				
				if(target1Refs.size() > 0){
					characteristicNodeRef = (target1Refs.get(0)).getTargetRef();
					comparedCharact.add(characteristicNodeRef);
					
					for(NodeRef d : dataListItems2){
						
						List<AssociationRef> target2Refs = nodeService.getTargetAssocs(d, pivotProperty);
						if(target2Refs.size() > 0){
							
							NodeRef c = (target2Refs.get(0)).getTargetRef();
							if(characteristicNodeRef.equals(c)){
								dataListItem2NodeRef = d;
								break;	
							}							
						}
						else{
							// continue to look for the good dataListItem2
						}
					}
				}
				
				CharacteristicToCompare characteristicToCmp = new CharacteristicToCompare(null, characteristicNodeRef, dataListItem1, dataListItem2NodeRef);
				characteristicsToCmp.add(characteristicToCmp);
			}
			
			//compare charact that are in DL1
			for(NodeRef d : dataListItems2){									
				
				List<AssociationRef> target2Refs = nodeService.getTargetAssocs(d, pivotProperty);
				NodeRef characteristicNodeRef = null;
				
				if(target2Refs.size() > 0){
					characteristicNodeRef = (target2Refs.get(0)).getTargetRef();
					
					if(!comparedCharact.contains(characteristicNodeRef)){					
						comparedCharact.add(characteristicNodeRef);
						CharacteristicToCompare characteristicToCmp = new CharacteristicToCompare(null, characteristicNodeRef, null, d);
						characteristicsToCmp.add(characteristicToCmp);
					}
				}			
			}
		}
		
		
		// compare properties of characteristics
		for(CharacteristicToCompare c : characteristicsToCmp){			
			compareNode(entityList, c.getCharactPath(), c.getCharacteristic(), c.getNodeRef1(), c.getNodeRef2(), nbEntities, comparisonPosition, true, comparisonMap);
		}
	}
	
	/**
	 * Load a composite datalist.
	 *
	 * @param dataListItems the data list items
	 * @param pivotProperty the pivot property
	 * @param depthLevel the depth level
	 * @param position the position
	 * @param compositeItem the composite item
	 */
	private void loadCompositeDataList(List<NodeRef> dataListItems, QName pivotProperty, int depthLevel, int position, CompositeComparableItem compositeItem){
		
		int cnt = position;
		
		if(dataListItems == null){
			return; // nothing to do
		}			
		
		while(cnt < dataListItems.size()){
			
			NodeRef nodeRef = dataListItems.get(cnt);
			Integer l = (Integer)nodeService.getProperty(nodeRef, BeCPGModel.PROP_DEPTH_LEVEL);						
			
			if(l!=null && depthLevel == l){					
				
				List<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, pivotProperty);
	    		String pivot = assocRefs.get(0).getTargetRef().toString();
	    		
	    		if(logger.isDebugEnabled()){
	    			logger.debug("loadCompositeDataList: nodeRef: " + nodeRef + " - targetAssoc: " +  nodeService.getProperty(assocRefs.get(0).getTargetRef(), ContentModel.PROP_NAME));
	    		}
				
				// has a child ?
				boolean isComposite = false;
				if(cnt+1 < dataListItems.size()){
					
					NodeRef nextNodeRef = dataListItems.get(cnt + 1);
					Integer nextDepthLevel = (Integer)nodeService.getProperty(nextNodeRef, BeCPGModel.PROP_DEPTH_LEVEL);
					
					if(depthLevel < nextDepthLevel)						
						isComposite = true;
				}
				
				AbstractComparableItem c;
				if(isComposite){					
					c = new CompositeComparableItem(depthLevel, pivot, nodeRef);					
					loadCompositeDataList(dataListItems, pivotProperty, depthLevel + 1, cnt + 1, (CompositeComparableItem)c);
				}
				else{
					c = new ComparableItem(depthLevel, (String)nodeService.getProperty(nodeRef, pivotProperty), nodeRef);
				}
			
				String key = pivot;
				if(compositeItem.get(pivot) != null){
					int keyCnt = 1;
					while(compositeItem.get(pivot + keyCnt) != null){
						keyCnt++;
					}
					key += keyCnt;
				}
				compositeItem.add(key, c);				
			}
			else if (depthLevel < l){
				// nothing to do since we treat children before
			} 	
			else if(depthLevel > l){
				// exit
				return;
			}
			
			cnt++;
		}
	}
	
	/**
	 * Compare 2 composite datalists.
	 *
	 * @param charactPath the charact path
	 * @param characteristicsToCmp the characteristics to cmp
	 * @param compositeItem1 the composite item1
	 * @param compositeItem2 the composite item2
	 */
	private void compareCompositeDataLists(List<NodeRef> charactPath, List<CharacteristicToCompare> characteristicsToCmp, CompositeComparableItem compositeItem1, CompositeComparableItem compositeItem2){
		
		if(compositeItem1 != null){
			for(String key : compositeItem1.getItemList().keySet()){
				
				AbstractComparableItem c1 = compositeItem1.get(key);
				AbstractComparableItem c2 = compositeItem2 == null ? null : compositeItem2.get(key);
				NodeRef nodeRef2 = c2 == null ? null : c2.getNodeRef();
				
				// key is nodeRef + counter
				NodeRef charactNodeRef = new NodeRef(key.substring(0, 60));				
				CharacteristicToCompare characteristicToCmp = new CharacteristicToCompare(charactPath, charactNodeRef, c1.getNodeRef(), nodeRef2);
				characteristicsToCmp.add(characteristicToCmp);
				
				CompositeComparableItem tempCompositeItem1 = null;
				CompositeComparableItem tempCompositeItem2 = null;			
				if(c1 instanceof CompositeComparableItem){
					tempCompositeItem1 = (CompositeComparableItem)c1;
				}			
				if(c2 instanceof CompositeComparableItem){
					tempCompositeItem2 = (CompositeComparableItem)c2;
				}
				
				List<NodeRef> tempCharactPath = new ArrayList<NodeRef>(charactPath);				
				tempCharactPath.add(charactNodeRef);
				compareCompositeDataLists(tempCharactPath, characteristicsToCmp, tempCompositeItem1, tempCompositeItem2);
			}
		}
		
		if(compositeItem2 != null){
			for(String key : compositeItem2.getItemList().keySet()){
				
				AbstractComparableItem c2 = compositeItem2.get(key);
				
				if((compositeItem1 == null) || (compositeItem1 != null && compositeItem1.get(key) == null)){
										
					// key is nodeRef + counter
					NodeRef charactNodeRef = new NodeRef(key.substring(0, 60));	
					CharacteristicToCompare characteristicToCmp = new CharacteristicToCompare(charactPath, charactNodeRef, null, c2.getNodeRef());
					characteristicsToCmp.add(characteristicToCmp);
					
					if(c2 instanceof CompositeComparableItem){
						List<NodeRef> tempCharactPath = new ArrayList<NodeRef>(charactPath);						
						tempCharactPath.add(charactNodeRef);
						compareCompositeDataLists(tempCharactPath, characteristicsToCmp, null, (CompositeComparableItem)c2);
					}
				}			
			}
		}
				
	}
	
	/**
	 * Do a structural comparison of the 2 composite datalists.
	 *
	 * @param entityListType the entity list type
	 * @param pivotProperty the pivot property
	 * @param strucComparisonList the struc comparison list
	 * @param compositeItem1 the composite item1
	 * @param compositeItem2 the composite item2
	 */
	private void structCompareCompositeDataLists(QName entityListType, QName pivotProperty, List<StructCompareResultDataItem> strucComparisonList, CompositeComparableItem compositeItem1, CompositeComparableItem compositeItem2){
		
		if(compositeItem1 != null){
			for(String key : compositeItem1.getItemList().keySet()){
				
				AbstractComparableItem c1 = compositeItem1.get(key);
				AbstractComparableItem c2 = compositeItem2 == null ? null : compositeItem2.get(key);
				NodeRef nodeRef1 = c1.getNodeRef();
				NodeRef nodeRef2 = c2 == null ? null : c2.getNodeRef();
				
				if(logger.isDebugEnabled()){
					logger.debug("structCompareCompositeDataLists: nodeRef1: " + nodeRef1 + " - nodeRef2: " + nodeRef2);
				}
				
				StructCompareOperator operator = StructCompareOperator.Equal;										
				
				Map<String, CompareResultDataItem> comparisonMap = new HashMap<String, CompareResultDataItem>();
				compareNode(entityListType, null, null, nodeRef1, nodeRef2, 2, 1, true, comparisonMap);
				
				if(comparisonMap.size() > 0){
					operator = StructCompareOperator.Modified;
				}
					
				// get Properties
				Map<QName, String> properties1 = new HashMap<QName, String>();
				Map<QName, String> properties2 = new HashMap<QName, String>();
				for(String key2 : comparisonMap.keySet()){											
					
					CompareResultDataItem c = comparisonMap.get(key2);
					properties1.put(c.getProperty(), c.getValues().get(0));
					properties2.put(c.getProperty(), c.getValues().get(1));
					
					// replaced ?
					if(pivotProperty.getLocalName().equals(c.getProperty().getLocalName())){
						operator = StructCompareOperator.Replaced;
					}
				}
				
				//removed
				if(nodeRef2 == null){
					operator = StructCompareOperator.Removed;
				}
				
				StructCompareResultDataItem structComparison = new StructCompareResultDataItem(entityListType, c1.getDepthLevel(), operator, nodeRef1, nodeRef2, properties1, properties2);
				strucComparisonList.add(structComparison);
				
				CompositeComparableItem tempCompositeItem1 = null;
				CompositeComparableItem tempCompositeItem2 = null;			
				if(c1 instanceof CompositeComparableItem){
					tempCompositeItem1 = (CompositeComparableItem)c1;
				}			
				if(c2 instanceof CompositeComparableItem){
					tempCompositeItem2 = (CompositeComparableItem)c2;
				}
				
				structCompareCompositeDataLists(entityListType, pivotProperty, strucComparisonList, tempCompositeItem1, tempCompositeItem2);
			}
		}
		
		if(compositeItem2 != null){
			for(String key : compositeItem2.getItemList().keySet()){
				
				AbstractComparableItem c2 = compositeItem2.get(key);
				
				if((compositeItem1 == null) || (compositeItem1 != null && compositeItem1.get(key) == null)){
					
					// get Properties
					Map<String, CompareResultDataItem> comparisonMap = new HashMap<String, CompareResultDataItem>();
					compareNode(entityListType, null, null, null, c2.getNodeRef(), 2, 1, true, comparisonMap);
					Map<QName, String> properties1 = new HashMap<QName, String>();
					Map<QName, String> properties2 = new HashMap<QName, String>();
					for(String key2 : comparisonMap.keySet()){											
						
						CompareResultDataItem c = comparisonMap.get(key2);
						properties2.put(c.getProperty(), c.getValues().get(1));											
					}
					
					StructCompareResultDataItem structComparison = new StructCompareResultDataItem(entityListType, c2.getDepthLevel(), StructCompareOperator.Added, null, c2.getNodeRef(), properties1, properties2);
					strucComparisonList.add(structComparison);
					
					if(c2 instanceof CompositeComparableItem){
						structCompareCompositeDataLists(entityListType, pivotProperty, strucComparisonList, null, (CompositeComparableItem)c2);
					}
				}			
			}
		}
				
	}
	
	/**
	 * Compare the properties and the associations of the same characteristic.
	 *
	 * @param entityList the entity list
	 * @param charactPath the charact path
	 * @param characteristic the characteristic
	 * @param nodeRef1 the node ref1
	 * @param nodeRef2 the node ref2
	 * @param nbEntities the nb entities
	 * @param comparisonPosition the comparison position
	 * @param isDataList the is data list
	 * @param comparisonMap the comparison map
	 */
	private void compareNode(QName entityList, List<NodeRef> charactPath, NodeRef characteristic, NodeRef nodeRef1, NodeRef nodeRef2, int nbEntities, int comparisonPosition, boolean isDataList, Map<String, CompareResultDataItem> comparisonMap){
		
		
		/*
		 * 		Compare properites
		 */
		
		PropertyFormats propertyFormats = new PropertyFormats(false);
		Map<QName, Serializable> properties1 = nodeRef1 == null ? new HashMap<QName, Serializable>() : nodeService.getProperties(nodeRef1);
		Map<QName, Serializable> properties2 = nodeRef2 == null ? new HashMap<QName, Serializable>() : nodeService.getProperties(nodeRef2);
		
		for(QName propertyQName : properties1.keySet()){
						
			if(isCompareableProperty(propertyQName, isDataList)){
			
				Serializable oValue1 = properties1.get(propertyQName);
				Serializable oValue2 = null;
				
				if(properties2.containsKey(propertyQName)){
					oValue2 = properties2.get(propertyQName);
				}				
				
				compareValues(entityList, charactPath, characteristic, propertyQName, oValue1, oValue2, nbEntities, comparisonPosition, comparisonMap, propertyFormats);
			}						
		}

		//look for properties2 that are not in properties1
		for(QName propertyQName : properties2.keySet()){
			
			if(!properties1.containsKey(propertyQName) && isCompareableProperty(propertyQName, isDataList)){
				compareValues(entityList, charactPath, characteristic, propertyQName, null,  properties2.get(propertyQName), nbEntities, comparisonPosition, comparisonMap, propertyFormats);
			}
		}		
		
		/*
		 * 		Compare associations
		 */
		
		List<AssociationRef> associations1 = nodeRef1 == null ? new ArrayList<AssociationRef>() : nodeService.getTargetAssocs(nodeRef1, RegexQNamePattern.MATCH_ALL);
		List<AssociationRef> associations2 = nodeRef2 == null ? new ArrayList<AssociationRef>() : nodeService.getTargetAssocs(nodeRef2, RegexQNamePattern.MATCH_ALL);
		
		Map<QName, List<NodeRef>> associations1Sorted = new HashMap<QName, List<NodeRef>>();
		Map<QName, List<NodeRef>> associations2Sorted = new HashMap<QName, List<NodeRef>>();
		
		// load associations of nodeRef 1
		for(AssociationRef assocRef : associations1){
			
			QName qName = assocRef.getTypeQName();
			NodeRef targetNodeRef = assocRef.getTargetRef();
			
			if(isCompareableProperty(qName, isDataList)){
			
				if(associations1Sorted.containsKey(qName)){
					List<NodeRef> nodeRefs = associations1Sorted.get(qName);
					if(!nodeRefs.contains(assocRef.getTargetRef())){
						nodeRefs.add(targetNodeRef);
					}					
				}
				else{
					List<NodeRef> nodeRefs = new ArrayList<NodeRef>();
					nodeRefs.add(targetNodeRef);
					associations1Sorted.put(qName, nodeRefs	);
				}				
			}
		}

		// load associations of nodeRef 1		
		for(AssociationRef assocRef : associations2){
			
			QName qName = assocRef.getTypeQName();
			NodeRef targetNodeRef = assocRef.getTargetRef();
			
			if(isCompareableProperty(qName, isDataList)){
			
				if(associations2Sorted.containsKey(qName)){
					List<NodeRef> nodeRefs = associations2Sorted.get(qName);
					if(!nodeRefs.contains(assocRef.getTargetRef())){
						nodeRefs.add(targetNodeRef);
					}					
				}
				else{
					List<NodeRef> nodeRefs = new ArrayList<NodeRef>();
					nodeRefs.add(targetNodeRef);
					associations2Sorted.put(qName, nodeRefs	);
				}				
			}
		}
		
		for(QName propertyQName : associations1Sorted.keySet()){
			
			if(isCompareableProperty(propertyQName, isDataList)){
			
				List<NodeRef> nodeRefs1 = associations1Sorted.get(propertyQName);
				List<NodeRef> nodeRefs2 = null;
				
				boolean isDifferent = false;
				if(associations2Sorted.containsKey(propertyQName)){
					nodeRefs2 = associations2Sorted.get(propertyQName);
					
					for(NodeRef nodeRef : nodeRefs1){
						if(!nodeRefs2.contains(nodeRef)){
							isDifferent = true;
						}
					}
				}
				else{
					isDifferent = true;
				}
								
				if(isDifferent){
					compareAssocs(entityList, charactPath, characteristic, propertyQName, nodeRefs1, nodeRefs2, nbEntities, comparisonPosition, comparisonMap);							
				}
			}						
		}

		//look for properties2 that are not in properties1
		for(QName propertyQName : associations2Sorted.keySet()){
			
			if(!associations1Sorted.containsKey(propertyQName) && isCompareableProperty(propertyQName, isDataList)){
				compareAssocs(entityList, charactPath, characteristic, propertyQName, null,  associations2Sorted.get(propertyQName), nbEntities, comparisonPosition, comparisonMap);
			}
		}
		
		
	}
	
	/**
	 * Compare the associations of a node.
	 *
	 * @param entityList the entity list
	 * @param charactPath the charact path
	 * @param characteristic the characteristic
	 * @param propertyQName the property q name
	 * @param nodeRefs1 the node refs1
	 * @param nodeRefs2 the node refs2
	 * @param nbEntities the nb entities
	 * @param comparisonPosition the comparison position
	 * @param comparisonMap the comparison map
	 */
	private void compareAssocs(QName entityList, List<NodeRef> charactPath, NodeRef characteristic, QName propertyQName, List<NodeRef> nodeRefs1, List<NodeRef> nodeRefs2, int nbEntities, int comparisonPosition, Map<String, CompareResultDataItem> comparisonMap){
		
		String strValue1 = null;
		String strValue2 = null;			
		
		if(nodeRefs1 != null){
			for(NodeRef nodeRef : nodeRefs1){
				
				if(strValue1 == null){
					strValue1 = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
				}
				else{
					strValue1 += RepoConsts.LABEL_SEPARATOR + (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
				}						
			}
		}
			
		if(nodeRefs2 != null){
			for(NodeRef nodeRef : nodeRefs2){
				
				if(strValue2 == null){
					strValue2 = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
				}
				else{
					strValue2 += RepoConsts.LABEL_SEPARATOR + (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
				}						
			}
		}
		
		String key = String.format("%s-%s-%s", entityList, characteristic, propertyQName);		
		CompareResultDataItem comparisonDataItem = comparisonMap.get(key);
		
		if(comparisonDataItem == null){
			List<String> values = new ArrayList<String>(nbEntities);
			values.add(0, strValue1);
			values.add(comparisonPosition, strValue2);
			comparisonDataItem = new CompareResultDataItem(entityList, charactPath, characteristic, propertyQName, values);
			comparisonMap.put(key, comparisonDataItem);
		}
		else{
				comparisonDataItem.getValues().add(comparisonPosition, strValue2);
		}		
		
	}
		
	/**
	 * Compare 2 values of the same property.
	 *
	 * @param entityList the entity list
	 * @param charactPath the charact path
	 * @param characteristic the characteristic
	 * @param propertyQName the property q name
	 * @param oValue1 the o value1
	 * @param oValue2 the o value2
	 * @param nbEntities the nb entities
	 * @param comparisonPosition the comparison position
	 * @param comparisonMap the comparison map
	 */
	private void compareValues(QName entityList, List<NodeRef> charactPath, NodeRef characteristic, QName propertyQName, Serializable oValue1, Serializable oValue2, int nbEntities, int comparisonPosition, Map<String, CompareResultDataItem> comparisonMap, PropertyFormats propertyFormats){
		
		PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
		
		//some system properties are not found (versionDescription, frozenModifier, etc...)
		if(propertyDef == null){
			return;
		}		
		
		// Are properties differents ?
		if(oValue1 != null){
								
			if(oValue2 != null){
				if(propertyDef.getDataType().toString().equals(DataTypeDefinition.DOUBLE.toString())  || 
						propertyDef.getDataType().toString().equals(DataTypeDefinition.FLOAT.toString()) ||
						propertyDef.getDataType().toString().equals(DataTypeDefinition.DATE.toString()) ||
						propertyDef.getDataType().toString().equals(DataTypeDefinition.DATETIME.toString())){
			
					String tempValue1 = attributeExtractorService.getStringValue(propertyDef, oValue1, propertyFormats);					
					String tempValue2 = attributeExtractorService.getStringValue(propertyDef, oValue2, propertyFormats);
					
					if(tempValue1.equals(tempValue2)){
						return;
					}
				}
				else if(oValue1.equals(oValue2)){
					return;
				}				
			}
		}
		else if(oValue2 == null){
			return;
		}
		
		String strValue1 = attributeExtractorService.getStringValue(propertyDef, oValue1, propertyFormats);					
		String strValue2 = attributeExtractorService.getStringValue(propertyDef, oValue2, propertyFormats);
		String key = String.format("%s-%s-%s", entityList, characteristic, propertyDef.getName());	
		CompareResultDataItem comparisonDataItem = comparisonMap.get(key);				
				
		if(comparisonDataItem == null){
			List<String> values = new ArrayList<String>(nbEntities);
			values.add(0, strValue1);
			values.add(comparisonPosition, strValue2);
			comparisonDataItem = new CompareResultDataItem(entityList, charactPath, characteristic, propertyQName, values);
			comparisonMap.put(key, comparisonDataItem);
		}
		else{
				comparisonDataItem.getValues().add(comparisonPosition, strValue2);
		}			
	}
	
	/**
	 * Test if the property must be compared.
	 *
	 * @param qName the q name
	 * @param isDataList the is data list
	 * @return true, if is compareable property
	 */
	private boolean isCompareableProperty(QName qName, boolean isDataList){
		
		boolean isCompareable = true;
		
		if(qName.equals(ContentModel.PROP_NODE_REF) || 
				qName.equals(ContentModel.PROP_NODE_DBID) ||
				qName.equals(ContentModel.PROP_NODE_UUID) ||
				qName.equals(ContentModel.PROP_STORE_IDENTIFIER) ||
				qName.equals(ContentModel.PROP_STORE_NAME) ||
				qName.equals(ContentModel.PROP_STORE_PROTOCOL) ||
				qName.equals(ContentModel.PROP_CONTENT) ||
				qName.equals(ContentModel.PROP_VERSION_LABEL) ||
				qName.equals(ContentModel.PROP_AUTO_VERSION) ||
				qName.equals(ContentModel.PROP_AUTO_VERSION_PROPS) ||
				qName.equals(ContentModel.ASSOC_ORIGINAL) ||
				//system properties
				qName.equals(BeCPGModel.PROP_PARENT_LEVEL) ||
				qName.equals(BeCPGModel.PROP_START_EFFECTIVITY) ||
				qName.equals(BeCPGModel.PROP_END_EFFECTIVITY) ||
				qName.equals(ReportModel.PROP_REPORT_ENTITY_GENERATED) ||
				qName.equals(ReportModel.ASSOC_REPORTS) ||
				qName.equals(BeCPGModel.PROP_VERSION_LABEL)){
			
			isCompareable = false;
		}
		
		if(isDataList && isCompareable){
			if(qName.equals(ContentModel.PROP_NAME) ||
				qName.equals(ContentModel.PROP_CREATOR) ||
				qName.equals(ContentModel.PROP_CREATED) ||
				qName.equals(ContentModel.PROP_MODIFIER) ||
				qName.equals(ContentModel.PROP_MODIFIED) ||
				qName.equals(BeCPGModel.PROP_SORT)){
				
				isCompareable = false;
			}
		}
		
		return isCompareable;
	}

}
