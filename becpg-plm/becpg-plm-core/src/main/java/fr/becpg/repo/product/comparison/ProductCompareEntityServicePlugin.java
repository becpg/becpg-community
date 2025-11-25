package fr.becpg.repo.product.comparison;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.MPMModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.comparison.CompareResultDataItem;
import fr.becpg.repo.entity.comparison.StructCompareResultDataItem;
import fr.becpg.repo.entity.datalist.MultiLevelDataListService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.product.formulation.PackagingHelper;
import fr.becpg.repo.product.helper.AllocationHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * <p>ProductCompareEntityServicePlugin class.</p>
 *
 * @author kevin
 * @version $Id: $Id
 */
@Service
public class ProductCompareEntityServicePlugin extends DefaultCompareEntityServicePlugin {

	@Autowired
	public ProductCompareEntityServicePlugin(
			@Qualifier("alfrescoRepository") AlfrescoRepository<BeCPGDataObject> alfrescoRepository,
			@Qualifier("packagingHelper") PackagingHelper packagingHelper,
			@Qualifier("nodeService") NodeService nodeService,
			@Qualifier("dictionaryService") DictionaryService dictionaryService,
			@Qualifier("namespaceService") NamespaceService namespaceService,
			@Qualifier("associationService") AssociationService associationService,
			@Qualifier("attributeExtractorService") AttributeExtractorService attributeExtractorService,
			@Qualifier("entityDictionaryService") EntityDictionaryService entityDictionaryService,
			@Qualifier("entityListDAO") EntityListDAO entityListDAO,
			@Qualifier("multiLevelDataListService") MultiLevelDataListService multiLevelDataListService,
			@Qualifier("fileFolderService") FileFolderService fileFolderService,
			@Qualifier("systemConfigurationService") SystemConfigurationService systemConfigurationService) {
		super(alfrescoRepository, packagingHelper, nodeService, dictionaryService, namespaceService, associationService,
				attributeExtractorService, entityDictionaryService, entityListDAO, multiLevelDataListService,
				fileFolderService, systemConfigurationService);
	}

	

	/**
	 * Check if raw material extraction is enabled via system configuration.
	 *
	 * @return true if raw material extraction is enabled
	 */
	private boolean extractRawMaterialEnabled() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.product.compare.extractRawMaterial"));
	}

	/** {@inheritDoc} */
	@Override
	public void compareEntities(NodeRef entity1NodeRef, NodeRef entity2NodeRef, int nbEntities, int comparisonPosition,
			Map<String, CompareResultDataItem> comparisonMap, Map<String, List<StructCompareResultDataItem>> structCompareResults) {
		
		// Call parent implementation for standard comparison
		super.compareEntities(entity1NodeRef, entity2NodeRef, nbEntities, comparisonPosition, comparisonMap, structCompareResults);
		
		// Extract and compare raw materials if enabled
		if (extractRawMaterialEnabled()) {
			compareRawMaterials(entity1NodeRef, entity2NodeRef, nbEntities, comparisonPosition, comparisonMap);
		}
	}
	
	/** {@inheritDoc} */
	@Override
	protected void multiLevelComparison(QName dataListType, String charactName, String pivotKey, NodeRef entity1NodeRef,
			NodeRef entity2NodeRef, int nbEntities, int comparisonPosition, Map<String, CompareResultDataItem> comparisonMap,
			double[] totalQty) {
		if (dataListType.equals(PLMModel.TYPE_COMPOLIST)) {
			addCompoListProps(comparisonMap, dataListType, charactName, pivotKey, entity1NodeRef, nbEntities, comparisonPosition, totalQty, null, false, 0, 1);
			addCompoListProps(comparisonMap, dataListType, charactName, pivotKey, entity2NodeRef, nbEntities, comparisonPosition, totalQty, null, true, 1, 1);
		} else if (dataListType.equals(PLMModel.TYPE_PACKAGINGLIST)) {
			addPackagingListProps(comparisonMap, dataListType, charactName, pivotKey, entity1NodeRef, nbEntities, comparisonPosition, totalQty, null, false, 0, 1);
			addPackagingListProps(comparisonMap, dataListType, charactName, pivotKey, entity2NodeRef, nbEntities, comparisonPosition, totalQty, null, true, 1, 1);
		} else if (dataListType.equals(MPMModel.TYPE_PROCESSLIST)) {
			addProcessListProps(comparisonMap, dataListType, charactName, pivotKey, entity1NodeRef, nbEntities, comparisonPosition, totalQty, null, false, 0, 1);
			addProcessListProps(comparisonMap, dataListType, charactName, pivotKey, entity2NodeRef, nbEntities, comparisonPosition, totalQty, null, true, 1, 1);
		} 
	}

	private void addCompoListProps(Map<String, CompareResultDataItem> comparisonMap, QName dataListType, String charactName, 
			String pivotKey, NodeRef entityNodeRef, int nbEntities, int comparisonPosition, double[] totalQty, 
			ProductData baseProduct, boolean swap, int position, int level) {

		if (level > 20) {
			//Avoid infinite loop
			return;
		}
		
		if (entityNodeRef != null) {
			// qtyForProduct for currently compared product
			CompoListDataItem item = (CompoListDataItem) alfrescoRepository.findOne(entityNodeRef);
			QName entityType = nodeService.getType(item.getProduct());
			ProductData product = (ProductData) alfrescoRepository.findOne(item.getProduct());
			Double qtyForProduct = null;
			String value = "";

			if ((level == 1) && (baseProduct == null)) {
				NodeRef compoListNodeRef = nodeService.getPrimaryParent(entityNodeRef).getParentRef();
				NodeRef entityListsNodeRef = nodeService.getPrimaryParent(compoListNodeRef).getParentRef();
				NodeRef productNodeRef = nodeService.getPrimaryParent(entityListsNodeRef).getParentRef();
				baseProduct = (ProductData) alfrescoRepository.findOne(productNodeRef);
			}

			String previousQtyForProductStr =
				getCurrentValue(comparisonMap, dataListType, charactName, PLMModel.PROP_COMPOLIST_QTY_FOR_PRODUCT, position);
			
			if (previousQtyForProductStr != null) {
				qtyForProduct = Double.parseDouble(previousQtyForProductStr);
			}

			if (qtyForProduct != null) {
				if (entityType.equals(PLMModel.TYPE_FINISHEDPRODUCT) || entityType.equals(PLMModel.TYPE_LOGISTICUNIT)) {
					extractProps(comparisonMap, dataListType, charactName, charactName, PLMModel.PROP_COMPOLIST_QTY_PERC_FOR_PRODUCT,
							Double.toString(qtyForProduct / totalQty[position] * 100), nbEntities, comparisonPosition, swap);
				} else if (entityType.equals(PLMModel.TYPE_SEMIFINISHEDPRODUCT)) {
					extractProps(comparisonMap, dataListType, charactName, pivotKey, PLMModel.PROP_COMPOLIST_QTY_PERC_FOR_SF,
							Double.toString(qtyForProduct / totalQty[position] * 100), nbEntities, comparisonPosition, swap);
				}
			}

			value = String.valueOf(level);
			extractProps(comparisonMap, dataListType, charactName, pivotKey, BeCPGModel.PROP_DEPTH_LEVEL,
					value, nbEntities, comparisonPosition, swap);

			value = item.getCompoListUnit() == null ? "" : item.getCompoListUnit().toString();
			extractProps(comparisonMap, dataListType, charactName, pivotKey, PLMModel.PROP_COMPOLIST_UNIT,
					value, nbEntities, comparisonPosition, swap);

			Double qtySubFormula = item.getQtySubFormula();
			value = qtySubFormula == null ? "" : Double.toString(qtySubFormula);
			extractProps(comparisonMap, dataListType, charactName, pivotKey, PLMModel.PROP_COMPOLIST_QTY_SUB_FORMULA,
					value, nbEntities, comparisonPosition, swap);

			// Extract props from datalist items
			for (CompoListDataItem compoItem : product.getCompoList()) {
				addCompoListProps(comparisonMap, dataListType, attributeExtractorService.extractPropName(compoItem.getProduct()), compoItem.getNodeRef().toString(),
						compoItem.getNodeRef(), nbEntities, comparisonPosition, totalQty, baseProduct, swap, position, level + 1);
			}
		}
	}

	private void addPackagingListProps(Map<String, CompareResultDataItem> comparisonMap, QName dataListType, String charactName, 
			String pivotKey, NodeRef entityNodeRef, int nbEntities, int comparisonPosition, double[] totalQty, 
			ProductData baseProduct, boolean swap, int position, int level) {

		if (level > 20) {
			//Avoid infinite loop
			return;
		}

		if (entityNodeRef != null) {
			PackagingListDataItem item = (PackagingListDataItem) alfrescoRepository.findOne(entityNodeRef);
			QName entityType = nodeService.getType(item.getProduct());
			ProductData product = (ProductData) alfrescoRepository.findOne(item.getProduct());
			Double qtyForProduct = null;
			String value = "";

			if ((level == 1) && (baseProduct == null)) {
				NodeRef packagingListNodeRef = nodeService.getPrimaryParent(entityNodeRef).getParentRef();
				NodeRef entityListsNodeRef = nodeService.getPrimaryParent(packagingListNodeRef).getParentRef();
				NodeRef productNodeRef = nodeService.getPrimaryParent(entityListsNodeRef).getParentRef();
				baseProduct = (ProductData) alfrescoRepository.findOne(productNodeRef);
			}

			String previousQtyForProductStr =
				getCurrentValue(comparisonMap, dataListType, charactName, PLMModel.PROP_PACKAGINGLIST_QTY_FOR_PRODUCT, position);
			
			if (previousQtyForProductStr != null) {
				qtyForProduct = Double.parseDouble(previousQtyForProductStr);
			}

			if (qtyForProduct != null) {
				if (entityType.equals(PLMModel.TYPE_FINISHEDPRODUCT) || entityType.equals(PLMModel.TYPE_LOGISTICUNIT)) {
					extractProps(comparisonMap, dataListType, charactName, charactName, PLMModel.PROP_PACKAGINGLIST_QTY_PERC_FOR_PRODUCT,
							Double.toString(qtyForProduct / totalQty[position] * 100), nbEntities, comparisonPosition, swap);
				} else if (entityType.equals(PLMModel.TYPE_SEMIFINISHEDPRODUCT)) {
					extractProps(comparisonMap, dataListType, charactName, charactName, PLMModel.PROP_PACKAGINGLIST_QTY_PERC_FOR_SF,
							Double.toString(qtyForProduct / totalQty[position] * 100), nbEntities, comparisonPosition, swap);
				}
			}

			value = String.valueOf(level);
			extractProps(comparisonMap, dataListType, charactName, pivotKey, BeCPGModel.PROP_DEPTH_LEVEL,
					value, nbEntities, comparisonPosition, swap);

			value = item.getPackagingListUnit().toString();
			extractProps(comparisonMap, dataListType, charactName, pivotKey, PLMModel.PROP_PACKAGINGLIST_UNIT,
					value, nbEntities, comparisonPosition, swap);

			// Extract props from datalist items
			for (PackagingListDataItem packagingItem : product.getPackagingList()) {
				addPackagingListProps(comparisonMap, dataListType, attributeExtractorService.extractPropName(packagingItem.getProduct()), packagingItem.getNodeRef().toString(),
						packagingItem.getNodeRef(), nbEntities, comparisonPosition, totalQty, baseProduct, swap, position, level + 1);
			}
		}
	}

	private void addProcessListProps(Map<String, CompareResultDataItem> comparisonMap, QName dataListType, String charactName, 
			String pivotKey, NodeRef entityNodeRef, int nbEntities, int comparisonPosition, double[] totalQty, 
			ProductData baseProduct, boolean swap, int position, int level) {

		if (level > 20) {
			//Avoid infinite loop
			return;
		}

		if (entityNodeRef != null) {
			ProcessListDataItem item = (ProcessListDataItem) alfrescoRepository.findOne(entityNodeRef);
			if (item.getResource() != null) {
				QName entityType = nodeService.getType(item.getResource());
				ProductData product = (ProductData) alfrescoRepository.findOne(item.getResource());
				Double qtyForProduct = null;
				String value = "";
				
				if ((level == 1) && (baseProduct == null)) {
					NodeRef processListNodeRef = nodeService.getPrimaryParent(entityNodeRef).getParentRef();
					NodeRef entityListsNodeRef = nodeService.getPrimaryParent(processListNodeRef).getParentRef();
					NodeRef productNodeRef = nodeService.getPrimaryParent(entityListsNodeRef).getParentRef();
					baseProduct = (ProductData) alfrescoRepository.findOne(productNodeRef);
				}
				
				String previousQtyForProductStr =
						getCurrentValue(comparisonMap, dataListType, charactName, MPMModel.PROP_PL_QTY_FOR_PRODUCT, position);
				
				if (previousQtyForProductStr != null) {
					qtyForProduct = Double.parseDouble(previousQtyForProductStr);
				}
				
				if (qtyForProduct != null) {
					if (entityType.equals(PLMModel.TYPE_RESOURCEPRODUCT)) {
						extractProps(comparisonMap, dataListType, charactName, charactName, MPMModel.PROP_PL_QTY_PERC_FOR_PRODUCT,
								Double.toString(qtyForProduct / totalQty[position] * 100), nbEntities, comparisonPosition, swap);
					} else if (entityType.equals(PLMModel.TYPE_FINISHEDPRODUCT) || entityType.equals(PLMModel.TYPE_LOGISTICUNIT)) {
						extractProps(comparisonMap, dataListType, charactName, charactName, MPMModel.PROP_PL_QTY_PERC_FOR_PRODUCT,
								Double.toString(qtyForProduct / totalQty[position] * 100), nbEntities, comparisonPosition, swap);
					} else if (entityType.equals(PLMModel.TYPE_SEMIFINISHEDPRODUCT)) {
						extractProps(comparisonMap, dataListType, charactName, charactName, MPMModel.PROP_PL_QTY_PERC_FOR_SF,
								Double.toString(qtyForProduct / totalQty[position] * 100), nbEntities, comparisonPosition, swap);
					}
				}
				value = String.valueOf(level);
				extractProps(comparisonMap, dataListType, charactName, pivotKey, BeCPGModel.PROP_DEPTH_LEVEL,
						value, nbEntities, comparisonPosition, swap);
				
				value = item.getUnit() == null ? "" : item.getUnit().toString();
				extractProps(comparisonMap, dataListType, charactName, pivotKey, MPMModel.PROP_PL_UNIT,
						value, nbEntities, comparisonPosition, swap);
				
				// Extract props from datalist items
				for (ProcessListDataItem processItem : product.getProcessList()) {
					addProcessListProps(comparisonMap, dataListType, attributeExtractorService.extractPropName(processItem.getProduct()), processItem.getNodeRef().toString(),
							processItem.getNodeRef(), nbEntities, comparisonPosition, totalQty, baseProduct, swap, position, level + 1);
				}
			}
		}
	}


	
	private void extractProps(Map<String, CompareResultDataItem> comparisonMap, QName dataListType, String charactName, 
			String pivotKey, QName property, String value, int nbEntities, int comparisonPosition, boolean swap) {

		boolean isDiff = isDifferent(comparisonMap, dataListType, pivotKey, property, value);
		addComparisonDataItem(comparisonMap, dataListType, charactName, pivotKey, property,
				(!swap ? value : null), (!swap ? null : value), nbEntities, comparisonPosition, isDiff);
	}

	private String getCurrentValue(Map<String, CompareResultDataItem> comparisonMap, QName dataListType,
			String pivotKey, QName property, int comparisonPosition) {
		String key = String.format("%s-%s-%s", dataListType, pivotKey, property);
		CompareResultDataItem comparisonDataItem = comparisonMap.get(key);
		
		if (comparisonDataItem != null) {
			return comparisonDataItem.getValues()[comparisonPosition];
		}
		return null;
	}

	private boolean isDifferent(Map<String, CompareResultDataItem> comparisonMap, QName dataListType, String pivotKey, 
			QName property, String value) {
		String key = String.format("%s-%s-%s", dataListType, pivotKey, property);
		CompareResultDataItem comparisonDataItem = comparisonMap.get(key);
		
		String firstValue = null;
		if (comparisonDataItem != null) {
			firstValue = comparisonDataItem.getValues()[0];
		}
		return !value.equals(firstValue);
	}

	/**
	 * Compare raw materials between two product entities.
	 * Extracts raw materials from both entities, matches them by NodeRef, and adds comparison data.
	 *
	 * @param entity1NodeRef the first product entity node reference (always at position 0)
	 * @param entity2NodeRef the second product entity node reference (at comparisonPosition)
	 * @param nbEntities number of entities being compared
	 * @param comparisonPosition position of entity2 in comparison (1+ for second, third, etc.)
	 * @param comparisonMap the comparison map to populate
	 */
	private void compareRawMaterials(NodeRef entity1NodeRef, NodeRef entity2NodeRef, int nbEntities, int comparisonPosition,
			Map<String, CompareResultDataItem> comparisonMap) {
		
		// Extract raw materials from entity1 (position 0)
		Map<NodeRef, Double> rawMaterials1 = extractRawMaterialsFromEntity(entity1NodeRef);
		Double totalQty1 = rawMaterials1.values().stream().mapToDouble(Double::doubleValue).sum();
		ProductData productData1 = entity1NodeRef != null ? (ProductData) alfrescoRepository.findOne(entity1NodeRef) : null;
		Double productNetWeight1 = productData1 != null ? FormulationHelper.getNetWeight(productData1, FormulationHelper.DEFAULT_NET_WEIGHT) : 0d;
		
		// Extract raw materials from entity2 (at comparisonPosition)
		Map<NodeRef, Double> rawMaterials2 = extractRawMaterialsFromEntity(entity2NodeRef);
		Double totalQty2 = rawMaterials2.values().stream().mapToDouble(Double::doubleValue).sum();
		ProductData productData2 = entity2NodeRef != null ? (ProductData) alfrescoRepository.findOne(entity2NodeRef) : null;
		Double productNetWeight2 = productData2 != null ? FormulationHelper.getNetWeight(productData2, FormulationHelper.DEFAULT_NET_WEIGHT) : 0d;
		
		// Collect raw materials from entity1 and compare with entity2
		Set<NodeRef> processedRawMaterials = new HashSet<>();
		List<NodeRef> sortedRawMaterials1 = new ArrayList<>(rawMaterials1.keySet());
		Collections.sort(sortedRawMaterials1, (r1, r2) -> rawMaterials1.get(r2).compareTo(rawMaterials1.get(r1)));
		
		// Process raw materials from entity1 (may or may not exist in entity2)
		for (NodeRef rawMaterialNodeRef : sortedRawMaterials1) {
			processedRawMaterials.add(rawMaterialNodeRef);
			compareRawMaterial(rawMaterialNodeRef, rawMaterials1, rawMaterials2, totalQty1, totalQty2, 
					productNetWeight1, productNetWeight2, nbEntities, comparisonPosition, comparisonMap);
		}
		
		// Process raw materials that are only in entity2 (added items)
		List<NodeRef> sortedRawMaterials2 = new ArrayList<>(rawMaterials2.keySet());
		Collections.sort(sortedRawMaterials2, (r1, r2) -> rawMaterials2.get(r2).compareTo(rawMaterials2.get(r1)));
		
		for (NodeRef rawMaterialNodeRef : sortedRawMaterials2) {
			if (!processedRawMaterials.contains(rawMaterialNodeRef)) {
				compareRawMaterial(rawMaterialNodeRef, rawMaterials1, rawMaterials2, totalQty1, totalQty2, 
						productNetWeight1, productNetWeight2, nbEntities, comparisonPosition, comparisonMap);
			}
		}
	}
	
	/**
	 * Compare a single raw material between two entities.
	 */
	private void compareRawMaterial(NodeRef rawMaterialNodeRef, Map<NodeRef, Double> rawMaterials1, 
			Map<NodeRef, Double> rawMaterials2, Double totalQty1, Double totalQty2,
			Double productNetWeight1, Double productNetWeight2, int nbEntities, int comparisonPosition,
			Map<String, CompareResultDataItem> comparisonMap) {
		
		if (rawMaterialNodeRef != null) {
			String rawMaterialName = attributeExtractorService.extractPropName(rawMaterialNodeRef);
			String pivotKey = rawMaterialNodeRef.toString();
			
			// Get quantities for both entities
			Double qty1 = rawMaterials1.getOrDefault(rawMaterialNodeRef, 0d);
			Double qty2 = rawMaterials2.getOrDefault(rawMaterialNodeRef, 0d);
			
			// Calculate percentages relative to total raw materials
			Double qtyPerc1 = (100 * qty1) / (totalQty1 != 0d ? totalQty1 : 1d);
			Double qtyPerc2 = (100 * qty2) / (totalQty2 != 0d ? totalQty2 : 1d);
			
			// Check if values are different
			boolean isDifferent = !Double.toString(qtyPerc1).equals(Double.toString(qtyPerc2));
			
			// Add percentage values for both entities in a single call
			addComparisonDataItem(comparisonMap, PLMModel.TYPE_RAWMATERIAL, rawMaterialName, pivotKey, 
					PLMModel.PROP_COMPOLIST_QTY, Double.toString(qtyPerc1), Double.toString(qtyPerc2), 
					nbEntities, comparisonPosition, isDifferent);
			
			// Calculate and add quantity per product (percentage of product net weight)
			String qtyForProduct1Str = "";
			if (productNetWeight1 != 0d) {
				Double qtyForProduct1 = (100 * qty1) / productNetWeight1;
				qtyForProduct1Str = Double.toString(qtyForProduct1);
			}
			
			String qtyForProduct2Str = "";
			if (productNetWeight2 != 0d) {
				Double qtyForProduct2 = (100 * qty2) / productNetWeight2;
				qtyForProduct2Str = Double.toString(qtyForProduct2);
			}
			
			boolean isDifferentForProduct = !qtyForProduct1Str.equals(qtyForProduct2Str);
			addComparisonDataItem(comparisonMap, PLMModel.TYPE_RAWMATERIAL, rawMaterialName, pivotKey,
					PLMModel.PROP_COMPOLIST_QTY_FOR_PRODUCT, qtyForProduct1Str, qtyForProduct2Str, 
					nbEntities, comparisonPosition, isDifferentForProduct);
		}
	}
	
	/**
	 * Extract raw materials from a product entity.
	 *
	 * @param entityNodeRef the product entity node reference
	 * @return map of raw material NodeRef to quantity
	 */
	private Map<NodeRef, Double> extractRawMaterialsFromEntity(NodeRef entityNodeRef) {
		Map<NodeRef, Double> rawMaterials = new HashMap<>();
		
		if (entityNodeRef == null) {
			return rawMaterials;
		}
		
		ProductData productData = (ProductData) alfrescoRepository.findOne(entityNodeRef);
		if (productData == null) {
			return rawMaterials;
		}
		
		Double productNetWeight = FormulationHelper.getNetWeight(productData, FormulationHelper.DEFAULT_NET_WEIGHT);
		return AllocationHelper.extractAllocations(productData, rawMaterials, productNetWeight, alfrescoRepository);
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean applyTo(QName entityType) {
		return entityType.equals(PLMModel.TYPE_FINISHEDPRODUCT) || entityType.equals(PLMModel.TYPE_SEMIFINISHEDPRODUCT) || entityType.equals(PLMModel.TYPE_LOGISTICUNIT);
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean isDefault() {
		return false;
	}

}
