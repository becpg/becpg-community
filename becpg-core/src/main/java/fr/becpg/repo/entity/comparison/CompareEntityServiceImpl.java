package fr.becpg.repo.entity.comparison;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.config.format.PropertyFormats;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.datalist.MultiLevelDataListService;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.helper.AttributeExtractorService;

/**
 * Compare several entities (properties, datalists and composite datalists).
 * 
 * @author querephi
 */
@Service("compareEntityService")
public class CompareEntityServiceImpl implements CompareEntityService {

	/** The logger. */
	private static final Log logger = LogFactory.getLog(CompareEntityServiceImpl.class);

	private static final String COMPARISON_SEPARATOR = " - ";

	@Autowired
	private NodeService nodeService;

	@Autowired
	private DictionaryService dictionaryService;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private AttributeExtractorService attributeExtractorService;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Autowired
	private EntityListDAO entityListDAO;

	@Autowired
	private MultiLevelDataListService multiLevelDataListService;

	@Override
	public List<CompareResultDataItem> compare(NodeRef entity1, List<NodeRef> entities, List<CompareResultDataItem> compareResult,
			Map<String, List<StructCompareResultDataItem>> structCompareResults) {

		Map<String, CompareResultDataItem> comparisonMap = new HashMap<>();
		int pos = 1;
		int nbEntities = entities.size() + 1;

		for (NodeRef entity : entities) {
			logger.debug("compare entity " + entity1 + " with entity " + entity);
			compareEntities(entity1, entity, nbEntities, pos, comparisonMap, structCompareResults);
			pos++;
		}

		for (CompareResultDataItem c : comparisonMap.values()) {
			compareResult.add(c);
		}

		return compareResult;
	}

	private void compareEntities(NodeRef entity1NodeRef, NodeRef entity2NodeRef, int nbEntities, int comparisonPosition,
			Map<String, CompareResultDataItem> comparisonMap, Map<String, List<StructCompareResultDataItem>> structCompareResults) {

		logger.debug("compare entity1NodeRef " + entity1NodeRef + " with " + entity2NodeRef);

		// compare entity properties
		compareNode(null, null, null, entity1NodeRef, entity2NodeRef, nbEntities, comparisonPosition, false, comparisonMap);

		// load datalists
		List<String> comparedDataLists = new ArrayList<>();
		List<NodeRef> dataLists1 = new ArrayList<>();
		NodeRef listsContainer1NodeRef = entityListDAO.getListContainer(entity1NodeRef);
		if (listsContainer1NodeRef != null) {
			dataLists1 = entityListDAO.getExistingListsNodeRef(listsContainer1NodeRef);
		}

		List<NodeRef> dataLists2 = new ArrayList<>();
		NodeRef listsContainer2NodeRef = entityListDAO.getListContainer(entity2NodeRef);
		if (listsContainer2NodeRef != null) {
			dataLists2 = entityListDAO.getExistingListsNodeRef(listsContainer2NodeRef);
		}

		for (NodeRef dataList1 : dataLists1) {

			QName dataListType = getDataListQName(dataList1);
			if (!BeCPGModel.TYPE_ENTITYLIST_ITEM.equals(dataListType)) {
				// structural comparison
				compareStructDatalist(entity1NodeRef, entity2NodeRef, dataListType, structCompareResults);

				// flat comparaison
				String dataListName1 = (String) nodeService.getProperty(dataList1, ContentModel.PROP_NAME);
				comparedDataLists.add(dataListName1);
				NodeRef dataList2NodeRef = null;
				if (dataLists2 != null) {
					for (NodeRef d : dataLists2) {
						String dataListName2 = (String) nodeService.getProperty(d, ContentModel.PROP_NAME);
						if (dataListName1.equals(dataListName2)) {
							dataList2NodeRef = d;
							break;
						}
					}
				}
				compareDataLists(dataListType, dataList1, dataList2NodeRef, nbEntities, comparisonPosition, comparisonMap);
			}
		}

		// compare dataLists2 that have not been compared
		if (dataLists2 != null) {
			for (NodeRef dataList2 : dataLists2) {

				String dataListName2 = (String) nodeService.getProperty(dataList2, ContentModel.PROP_NAME);

				if (!comparedDataLists.contains(dataListName2)) {

					QName dataListType = getDataListQName(dataList2);
					if (!BeCPGModel.TYPE_ENTITYLIST_ITEM.equals(dataListType)) {
						// structural comparison
						if (entityDictionaryService.isMultiLevelDataList(dataListType)) {
							compareStructDatalist(entity1NodeRef, entity2NodeRef, dataListType, structCompareResults);
						} else {
							comparedDataLists.add(dataListName2);
							compareDataLists(dataListType, null, dataList2, nbEntities, comparisonPosition, comparisonMap);
						}
					}
				}
			}
		}
	}

	private QName getDataListQName(NodeRef listNodeRef) {
		return QName.createQName((String) nodeService.getProperty(listNodeRef, DataListModel.PROP_DATALISTITEMTYPE), namespaceService);
	}

	private void compareDataLists(QName dataListType, NodeRef dataList1NodeRef, NodeRef dataList2NodeRef, int nbEntities, int comparisonPosition,
			Map<String, CompareResultDataItem> comparisonMap) {

		QName pivotProperty = null;
		try {
			pivotProperty = entityDictionaryService.getDefaultPivotAssoc(dataListType);
		} catch (IllegalArgumentException e) {
			logger.debug(e);
		}

		if (pivotProperty != null) {

			logger.debug("pivotProperty: " + pivotProperty);

			// load characteristics
			List<NodeRef> dataListItems1 = new ArrayList<>();
			if (dataList1NodeRef != null) {
				dataListItems1 = entityListDAO.getListItems(dataList1NodeRef, dataListType);
			}

			List<NodeRef> dataListItems2 = new ArrayList<>();
			if (dataList2NodeRef != null) {
				dataListItems2 = entityListDAO.getListItems(dataList2NodeRef, dataListType);
			}

			// look for characteristics that are in both entity
			List<CharacteristicToCompare> characteristicsToCmp = new ArrayList<>();
			List<NodeRef> comparedCharact = new ArrayList<>();

			// composite datalist
			for (NodeRef dataListItem1 : dataListItems1) {

				NodeRef dataListItem2NodeRef = null;

				List<AssociationRef> target1Refs = nodeService.getTargetAssocs(dataListItem1, pivotProperty);
				NodeRef characteristicNodeRef = null;

				if (target1Refs.size() > 0) {
					characteristicNodeRef = (target1Refs.get(0)).getTargetRef();
					comparedCharact.add(characteristicNodeRef);

					for (NodeRef d : dataListItems2) {

						List<AssociationRef> target2Refs = nodeService.getTargetAssocs(d, pivotProperty);
						if (target2Refs.size() > 0) {

							NodeRef c = (target2Refs.get(0)).getTargetRef();
							if (characteristicNodeRef.equals(c)) {
								dataListItem2NodeRef = d;
								break;
							}
						}
					}
				}

				CharacteristicToCompare characteristicToCmp = new CharacteristicToCompare(null, characteristicNodeRef, dataListItem1,
						dataListItem2NodeRef);
				characteristicsToCmp.add(characteristicToCmp);
			}

			// compare charact that are in DL1
			for (NodeRef d : dataListItems2) {

				List<AssociationRef> target2Refs = nodeService.getTargetAssocs(d, pivotProperty);
				NodeRef characteristicNodeRef;

				if (target2Refs.size() > 0) {
					characteristicNodeRef = (target2Refs.get(0)).getTargetRef();

					if (!comparedCharact.contains(characteristicNodeRef)) {
						comparedCharact.add(characteristicNodeRef);
						CharacteristicToCompare characteristicToCmp = new CharacteristicToCompare(null, characteristicNodeRef, null, d);
						characteristicsToCmp.add(characteristicToCmp);
					}
				}
			}

			// compare properties of characteristics
			for (CharacteristicToCompare c : characteristicsToCmp) {
				compareNode(dataListType, c.getCharactPath(), c.getCharacteristic(), c.getNodeRef1(), c.getNodeRef2(), nbEntities,
						comparisonPosition, true, comparisonMap);
			}
		}
	}

	@Override
	public void compareStructDatalist(NodeRef entity1NodeRef, NodeRef entity2NodeRef, QName datalistType,
			Map<String, List<StructCompareResultDataItem>> structCompareResults) {

		// load the 2 datalists
		QName pivotProperty = entityDictionaryService.getDefaultPivotAssoc(datalistType);

		MultiLevelListData listData1 = loadCompositeDataList(entity1NodeRef, datalistType);
		MultiLevelListData listData2 = loadCompositeDataList(entity2NodeRef, datalistType);

		if(logger.isDebugEnabled()){
			logger.debug("listData1 " + listData1);
			logger.debug("listData2 " + listData2);
		}
		CompositeComparableItem compositeItem1 = new CompositeComparableItem(0, null, null);
		loadComparableItems(compositeItem1, listData1);

		CompositeComparableItem compositeItem2 = new CompositeComparableItem(0, null, null);
		loadComparableItems(compositeItem2, listData2);

		List<StructCompareResultDataItem> structComparisonList = new LinkedList<>();
		structCompareCompositeDataLists(datalistType, pivotProperty, structComparisonList, compositeItem1, compositeItem2);

		String comparison = nodeService.getProperty(entity1NodeRef, ContentModel.PROP_NAME) + COMPARISON_SEPARATOR
				+ nodeService.getProperty(entity2NodeRef, ContentModel.PROP_NAME) + COMPARISON_SEPARATOR
				+ dictionaryService.getType(datalistType).getTitle(dictionaryService);

		structCompareResults.put(comparison, structComparisonList);
	}

	private MultiLevelListData loadCompositeDataList(NodeRef entityNodeRef, QName datalistType) {
		DataListFilter dataListFilter = new DataListFilter();
		dataListFilter.setDataType(datalistType);
		dataListFilter.setFilterId(DataListFilter.ALL_FILTER);
		dataListFilter.setEntityNodeRefs(Collections.singletonList(entityNodeRef));
		dataListFilter.updateMaxDepth(-1);
		return multiLevelDataListService.getMultiLevelListData(dataListFilter);
	}

	private void loadComparableItems(CompositeComparableItem compositeItem, MultiLevelListData listData) {

		for (Entry<NodeRef, MultiLevelListData> entry : listData.getTree().entrySet()) {
			NodeRef nodeRef = entry.getKey();

			// TODO generic should be able to combine several properties (ie:
			// EAN, Funtion,...)
			String pivot = (String) nodeService.getProperty(entry.getValue().getEntityNodeRef(), BeCPGModel.PROP_LEGAL_NAME);			
			if (pivot == null) {
				pivot = entry.getValue().getEntityNodeRef().toString();
			}
			CompositeComparableItem c = new CompositeComparableItem(entry.getValue().getDepth(), pivot, nodeRef);
			addComparableItem(compositeItem, pivot, c);
			loadComparableItems(c, entry.getValue());
		}
	}

	private void addComparableItem(CompositeComparableItem compositeItem, String pivot, AbstractComparableItem c) {
		String key = pivot;
		if (compositeItem.get(pivot) != null) {
			int keyCnt = 1;
			while (compositeItem.get(pivot + keyCnt) != null) {
				keyCnt++;
			}
			key += keyCnt;
		}
		compositeItem.add(key, c);
	}

	private void structCompareCompositeDataLists(QName entityListType, QName pivotProperty, List<StructCompareResultDataItem> strucComparisonList,
			CompositeComparableItem compositeItem1, CompositeComparableItem compositeItem2) {		
		
		if (compositeItem1 != null) {
			for (String key : compositeItem1.getItemList().keySet()) {

				AbstractComparableItem c1 = compositeItem1.get(key);
				AbstractComparableItem c2 = compositeItem2 == null ? null : compositeItem2.get(key);
				NodeRef nodeRef1 = c1.getNodeRef();
				NodeRef nodeRef2 = c2 == null ? null : c2.getNodeRef();				
				
				StructCompareOperator operator = StructCompareOperator.Equal;

				Map<String, CompareResultDataItem> comparisonMap = new TreeMap<>();
				compareNode(entityListType, null, null, nodeRef1, nodeRef2, 2, 1, true, comparisonMap);

				if (logger.isDebugEnabled()) {
					logger.debug("structCompareCompositeDataLists: nodeRef1: " + nodeRef1 + " - nodeRef2: " + nodeRef2 + " pivotProperty: " + pivotProperty);
					logger.trace(" comparisonMap: " + comparisonMap);
				}				

				// get Properties
				Map<QName, String> properties1 = new TreeMap<>();
				Map<QName, String> properties2 = new TreeMap<>();
				for (CompareResultDataItem c : comparisonMap.values()) {

					if (c.isDifferent()) {
						if (operator.equals(StructCompareOperator.Equal)) {
							operator = StructCompareOperator.Modified;
						}
						if (pivotProperty.getLocalName().equals(c.getProperty().getLocalName())) {
							operator = StructCompareOperator.Replaced;
						}
						else{
							// we don't include pivot in properties
							properties1.put(c.getProperty(), c.getValues().get(0));
							properties2.put(c.getProperty(), c.getValues().get(1));
						}
					}
				}

				// removed
				if (nodeRef2 == null) {
					operator = StructCompareOperator.Removed;
				}
				
				// we display only changes
				if (!StructCompareOperator.Equal.equals(operator)) {

					StructCompareResultDataItem structComparison = new StructCompareResultDataItem(entityListType, c1.getDepthLevel(), operator,
							pivotProperty, nodeRef1, nodeRef2, properties1, properties2);
					strucComparisonList.add(structComparison);

					CompositeComparableItem tempCompositeItem1 = null;
					CompositeComparableItem tempCompositeItem2 = null;
					if (c1 instanceof CompositeComparableItem) {
						tempCompositeItem1 = (CompositeComparableItem) c1;
					}
					if (c2 instanceof CompositeComparableItem) {
						tempCompositeItem2 = (CompositeComparableItem) c2;
					}

					structCompareCompositeDataLists(entityListType, pivotProperty, strucComparisonList, tempCompositeItem1, tempCompositeItem2);
				}

			}
		}

		if (compositeItem2 != null) {
			for (String key : compositeItem2.getItemList().keySet()) {

				AbstractComparableItem c2 = compositeItem2.get(key);

				if ((compositeItem1 == null) || (compositeItem1 != null && compositeItem1.get(key) == null)) {

					// get Properties
					Map<String, CompareResultDataItem> comparisonMap = new HashMap<>();
					compareNode(entityListType, null, null, null, c2.getNodeRef(), 2, 1, true, comparisonMap);

					Map<QName, String> properties1 = new HashMap<>();
					Map<QName, String> properties2 = new HashMap<>();
					for (String key2 : comparisonMap.keySet()) {

						CompareResultDataItem c = comparisonMap.get(key2);
						properties2.put(c.getProperty(), c.getValues().get(1));
					}

					if (logger.isDebugEnabled()) {
						logger.debug("structCompareCompositeDataLists: c2.getNodeRef(): " + c2.getNodeRef() + " pivotProperty: " + pivotProperty);
						logger.trace(" comparisonMap: " + comparisonMap);
					}

					StructCompareResultDataItem structComparison = new StructCompareResultDataItem(entityListType, c2.getDepthLevel(),
							StructCompareOperator.Added, pivotProperty, null, c2.getNodeRef(), properties1, properties2);
					strucComparisonList.add(structComparison);

					if (c2 instanceof CompositeComparableItem) {
						structCompareCompositeDataLists(entityListType, pivotProperty, strucComparisonList, null, (CompositeComparableItem) c2);
					}
				}
			}
		}

	}

	private void compareNode(QName dataListType, List<NodeRef> charactPath, NodeRef characteristic, NodeRef nodeRef1, NodeRef nodeRef2,
			int nbEntities, int comparisonPosition, boolean isDataList, Map<String, CompareResultDataItem> comparisonMap) {

		/*
		 * Compare properites
		 */

		PropertyFormats propertyFormats = new PropertyFormats(false);
		Map<QName, Serializable> properties1 = nodeRef1 == null ? new TreeMap<QName, Serializable>() : nodeService.getProperties(nodeRef1);
		Map<QName, Serializable> properties2 = nodeRef2 == null ? new TreeMap<QName, Serializable>() : nodeService.getProperties(nodeRef2);

		for (QName propertyQName : properties1.keySet()) {
			
			if (isCompareableProperty(propertyQName, isDataList)) {

				Serializable oValue1 = properties1.get(propertyQName);
				Serializable oValue2 = null;

				if (properties2.containsKey(propertyQName)) {
					oValue2 = properties2.get(propertyQName);
				}

				compareValues(dataListType, charactPath, characteristic, propertyQName, oValue1, oValue2, nbEntities, comparisonPosition,
						comparisonMap, propertyFormats);
			}
		}

		// look for properties2 that are not in properties1
		for (QName propertyQName : properties2.keySet()) {

			if (!properties1.containsKey(propertyQName) && isCompareableProperty(propertyQName, isDataList)) {
				compareValues(dataListType, charactPath, characteristic, propertyQName, null, properties2.get(propertyQName), nbEntities,
						comparisonPosition, comparisonMap, propertyFormats);
			}
		}

		/*
		 * Compare associations
		 */

		List<AssociationRef> associations1 = nodeRef1 == null ? new ArrayList<AssociationRef>() : nodeService.getTargetAssocs(nodeRef1,
				RegexQNamePattern.MATCH_ALL);
		List<AssociationRef> associations2 = nodeRef2 == null ? new ArrayList<AssociationRef>() : nodeService.getTargetAssocs(nodeRef2,
				RegexQNamePattern.MATCH_ALL);

		Map<QName, List<NodeRef>> associations1Sorted = new HashMap<>();
		Map<QName, List<NodeRef>> associations2Sorted = new HashMap<>();

		// load associations of nodeRef 1
		for (AssociationRef assocRef : associations1) {

			QName qName = assocRef.getTypeQName();
			NodeRef targetNodeRef = assocRef.getTargetRef();

			if (isCompareableProperty(qName, isDataList)) {

				if (associations1Sorted.containsKey(qName)) {
					List<NodeRef> nodeRefs = associations1Sorted.get(qName);
					if (!nodeRefs.contains(assocRef.getTargetRef())) {
						nodeRefs.add(targetNodeRef);
					}
				} else {
					List<NodeRef> nodeRefs = new ArrayList<>();
					nodeRefs.add(targetNodeRef);
					associations1Sorted.put(qName, nodeRefs);
				}
			}
		}

		// load associations of nodeRef 1
		for (AssociationRef assocRef : associations2) {

			QName qName = assocRef.getTypeQName();
			NodeRef targetNodeRef = assocRef.getTargetRef();

			if (isCompareableProperty(qName, isDataList)) {

				if (associations2Sorted.containsKey(qName)) {
					List<NodeRef> nodeRefs = associations2Sorted.get(qName);
					if (!nodeRefs.contains(assocRef.getTargetRef())) {
						nodeRefs.add(targetNodeRef);
					}
				} else {
					List<NodeRef> nodeRefs = new ArrayList<>();
					nodeRefs.add(targetNodeRef);
					associations2Sorted.put(qName, nodeRefs);
				}
			}
		}

		for (QName propertyQName : associations1Sorted.keySet()) {

			if (isCompareableProperty(propertyQName, isDataList)) {

				List<NodeRef> nodeRefs1 = associations1Sorted.get(propertyQName);
				List<NodeRef> nodeRefs2 = null;

				boolean isDifferent = false;
				if (associations2Sorted.containsKey(propertyQName)) {
					nodeRefs2 = associations2Sorted.get(propertyQName);

					for (NodeRef nodeRef : nodeRefs1) {
						if (!nodeRefs2.contains(nodeRef)) {
							isDifferent = true;
						}
					}
				} else {
					isDifferent = true;
				}

				compareAssocs(dataListType, charactPath, characteristic, propertyQName, nodeRefs1, nodeRefs2, nbEntities, comparisonPosition,
						comparisonMap, isDifferent);
			}
		}

		// look for properties2 that are not in properties1
		for (QName propertyQName : associations2Sorted.keySet()) {

			if (!associations1Sorted.containsKey(propertyQName) && isCompareableProperty(propertyQName, isDataList)) {
				compareAssocs(dataListType, charactPath, characteristic, propertyQName, null, associations2Sorted.get(propertyQName), nbEntities,
						comparisonPosition, comparisonMap, true);
			}
		}

	}

	private void compareAssocs(QName dataListType, List<NodeRef> charactPath, NodeRef characteristic, QName propertyQName, List<NodeRef> nodeRefs1,
			List<NodeRef> nodeRefs2, int nbEntities, int comparisonPosition, Map<String, CompareResultDataItem> comparisonMap, boolean isDifferent) {

		String strValue1 = null;
		String strValue2 = null;

		if (nodeRefs1 != null) {
			for (NodeRef nodeRef : nodeRefs1) {

				if (strValue1 == null) {
					strValue1 = attributeExtractorService.extractPropName(nodeRef);
				} else {
					strValue1 += RepoConsts.LABEL_SEPARATOR + attributeExtractorService.extractPropName(nodeRef);
				}
			}
		}

		if (nodeRefs2 != null) {
			for (NodeRef nodeRef : nodeRefs2) {

				if (strValue2 == null) {
					strValue2 = attributeExtractorService.extractPropName(nodeRef);
				} else {
					strValue2 += RepoConsts.LABEL_SEPARATOR + attributeExtractorService.extractPropName(nodeRef);
				}
			}
		}

		addComparisonDataItem(comparisonMap, dataListType, charactPath, characteristic, propertyQName, strValue1, strValue2, nbEntities,
				comparisonPosition, isDifferent);
	}

	private void compareValues(QName dataListType, List<NodeRef> charactPath, NodeRef characteristic, QName propertyQName, Serializable oValue1,
			Serializable oValue2, int nbEntities, int comparisonPosition, Map<String, CompareResultDataItem> comparisonMap,
			PropertyFormats propertyFormats) {

		PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
		boolean isDifferent = true;
		// some system properties are not found (versionDescription,
		// frozenModifier, etc...)
		if (propertyDef == null) {
			logger.info("Property Def doesn't exists for: "+propertyQName);
			return;
		}

		// Are properties differents ?
		if (oValue1 != null) {

			if (oValue2 != null) {
				if (propertyDef.getDataType().toString().equals(DataTypeDefinition.DOUBLE.toString())
						|| propertyDef.getDataType().toString().equals(DataTypeDefinition.FLOAT.toString())
						|| propertyDef.getDataType().toString().equals(DataTypeDefinition.DATE.toString())
						|| propertyDef.getDataType().toString().equals(DataTypeDefinition.DATETIME.toString())) {

					String tempValue1 = attributeExtractorService.getStringValue(propertyDef, oValue1, propertyFormats);
					String tempValue2 = attributeExtractorService.getStringValue(propertyDef, oValue2, propertyFormats);

					if (tempValue1.equals(tempValue2)) {
						isDifferent = false;
					}
				} else if (oValue1.equals(oValue2)) {
					isDifferent = false;
				}
			}
		} else if (oValue2 == null) {
			isDifferent = false;
		}

		String strValue1 = attributeExtractorService.extractPropertyForReport(propertyDef, oValue1, propertyFormats, true);
		String strValue2 = attributeExtractorService.extractPropertyForReport(propertyDef, oValue2, propertyFormats, true);

		addComparisonDataItem(comparisonMap, dataListType, charactPath, characteristic, propertyQName, strValue1, strValue2, nbEntities,
				comparisonPosition, isDifferent);
	}

	private void addComparisonDataItem(Map<String, CompareResultDataItem> comparisonMap, QName dataListType, List<NodeRef> charactPath,
			NodeRef characteristic, QName propertyQName, String strValue1, String strValue2, int nbEntities, int comparisonPosition,
			boolean isDifferent) {

		String key = String.format("%s-%s-%s", dataListType, characteristic, propertyQName);
		CompareResultDataItem comparisonDataItem = comparisonMap.get(key);

		if (comparisonDataItem == null) {
			List<String> values = new ArrayList<>(nbEntities);
			values.add(strValue1);
			values.add(strValue2);
			comparisonDataItem = new CompareResultDataItem(dataListType, charactPath, characteristic, propertyQName, values);
			comparisonMap.put(key, comparisonDataItem);
		} else {
			comparisonDataItem.getValues().add(comparisonPosition, strValue2);
		}

		if (isDifferent) {
			comparisonDataItem.setDifferent(isDifferent);
		}
	}

	private boolean isCompareableProperty(QName qName, boolean isDataList) {

		boolean isCompareable = true;

		if (qName.equals(ContentModel.PROP_NODE_REF) || qName.equals(ContentModel.PROP_NODE_DBID) || qName.equals(ContentModel.PROP_NODE_UUID)
				|| qName.equals(ContentModel.PROP_STORE_IDENTIFIER) || qName.equals(ContentModel.PROP_STORE_NAME)
				|| qName.equals(ContentModel.PROP_STORE_PROTOCOL) || qName.equals(ContentModel.PROP_CONTENT)
				|| qName.equals(ContentModel.PROP_VERSION_LABEL) || qName.equals(ContentModel.PROP_AUTO_VERSION)
				|| qName.equals(ContentModel.PROP_AUTO_VERSION_PROPS)
				|| qName.equals(ContentModel.ASSOC_ORIGINAL)
				|| qName.equals(ForumModel.PROP_COMMENT_COUNT)
				||
				// system properties
				qName.equals(BeCPGModel.PROP_PARENT_LEVEL) || qName.equals(BeCPGModel.PROP_START_EFFECTIVITY)
				|| qName.equals(BeCPGModel.PROP_END_EFFECTIVITY) || qName.equals(ReportModel.PROP_REPORT_ENTITY_GENERATED)
				|| qName.equals(ReportModel.ASSOC_REPORTS) || qName.equals(BeCPGModel.PROP_VERSION_LABEL) || qName.equals(BeCPGModel.PROP_COLOR)
				// TODO plugin
				|| qName.getLocalName().contains("dynamicCharactColumn") || qName.getLocalName().contains("compareWithDynColumn")) {

			isCompareable = false;
		}

		if (isDataList && isCompareable) {
			if (qName.equals(ContentModel.PROP_NAME) || qName.equals(ContentModel.PROP_CREATOR) || qName.equals(ContentModel.PROP_CREATED)
					|| qName.equals(ContentModel.PROP_MODIFIER) || qName.equals(ContentModel.PROP_MODIFIED) || qName.equals(BeCPGModel.PROP_SORT)) {

				isCompareable = false;
			}
		}
		
		return isCompareable;
	}

}
