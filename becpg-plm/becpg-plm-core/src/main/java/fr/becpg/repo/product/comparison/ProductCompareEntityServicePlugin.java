package fr.becpg.repo.product.comparison;

import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.MPMModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.comparison.CompareResultDataItem;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;

/**
 * <p>ProductCompareEntityServicePlugin class.</p>
 *
 * @author kevin
 * @version $Id: $Id
 */
@Service
public class ProductCompareEntityServicePlugin extends DefaultCompareEntityServicePlugin {

	
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

			value = item.getCompoListUnit().toString();
			extractProps(comparisonMap, dataListType, charactName, pivotKey, PLMModel.PROP_COMPOLIST_UNIT,
					value, nbEntities, comparisonPosition, swap);

			value = Double.toString(item.getQtySubFormula());
			extractProps(comparisonMap, dataListType, charactName, pivotKey, PLMModel.PROP_COMPOLIST_QTY_SUB_FORMULA,
					value, nbEntities, comparisonPosition, swap);

			// Extract props from datalist items
			for (CompoListDataItem compoItem : product.getCompoList()) {
				ProductData itemProduct = (ProductData) alfrescoRepository.findOne(compoItem.getProduct());
				addCompoListProps(comparisonMap, dataListType, itemProduct.getName(), compoItem.getNodeRef().toString(),
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
				ProductData itemProduct = (ProductData) alfrescoRepository.findOne(packagingItem.getProduct());
				addPackagingListProps(comparisonMap, dataListType, itemProduct.getName(), packagingItem.getNodeRef().toString(),
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

			value = item.getUnit().toString();
			extractProps(comparisonMap, dataListType, charactName, pivotKey, MPMModel.PROP_PL_UNIT,
					value, nbEntities, comparisonPosition, swap);

			// Extract props from datalist items
			for (ProcessListDataItem processItem : product.getProcessList()) {
				ProductData itemProduct = (ProductData) alfrescoRepository.findOne(processItem.getProduct());
				addProcessListProps(comparisonMap, dataListType, itemProduct.getName(), processItem.getNodeRef().toString(),
						processItem.getNodeRef(), nbEntities, comparisonPosition, totalQty, baseProduct, swap, position, level + 1);
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

	/** {@inheritDoc} */
	@Override
	public boolean isComparableProperty(QName qName, boolean isDataList) {
		return super.isComparableProperty(qName, isDataList);
	}

	/** {@inheritDoc} */
	@Override
	public boolean applyTo(QName entityType) {
		return entityType.equals(PLMModel.TYPE_FINISHEDPRODUCT) || entityType.equals(PLMModel.TYPE_SEMIFINISHEDPRODUCT) || entityType.equals(PLMModel.TYPE_LOGISTICUNIT);
	}
	
	@Override
	public boolean isDefault() {
		return false;
	}

}
