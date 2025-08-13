/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.product.formulation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.JsonFormulaHelper;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.AbstractProductDataView;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.repo.regulatory.RequirementDataType;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.repository.model.CompositionDataItem;

/**
 * Compare product with each other
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class CompareFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(CompareFormulationHandler.class);

	private AssociationService associationService;

	private ProductService productService;

	private NamespaceService namespaceService;

	private NodeService nodeService;

	private AttributeExtractorService attributeExtractorService;

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object.
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	/**
	 * <p>Setter for the field <code>productService</code>.</p>
	 *
	 * @param productService a {@link fr.becpg.repo.product.ProductService} object.
	 */
	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * <p>Setter for the field <code>attributeExtractorService</code>.</p>
	 *
	 * @param attributeExtractorService a {@link fr.becpg.repo.helper.AttributeExtractorService} object.
	 */
	public void setAttributeExtractorService(AttributeExtractorService attributeExtractorService) {
		this.attributeExtractorService = attributeExtractorService;
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(final ProductData productData) {

		if (productData.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL)) {
			return true;
		}

		if (!L2CacheSupport.isCacheOnlyEnable() && (productData.getAspects().contains(BeCPGModel.ASPECT_COMPARE_WITH)
				|| ((productData.getNodeRef() != null) && nodeService.hasAspect(productData.getNodeRef(), BeCPGModel.ASPECT_COMPARE_WITH)))) {
			L2CacheSupport.doInCacheOnly(() -> {
				List<ProductData> comparedProductDatas = getComparableProductDatas(productData);
				for (AbstractProductDataView view : productData.getViews()) {
					if (!comparedProductDatas.isEmpty()) {
						Map<DynamicCharactListItem, JSONArray> dynamicCharactResults = new HashMap<>();
						Map<Pair<CompositionDataItem, QName>, JSONArray> dynamicColumnResults = new HashMap<>();
						processComparison(productData, comparedProductDatas, view, dynamicCharactResults, dynamicColumnResults);
						applyResults(dynamicCharactResults, dynamicColumnResults);
					} else {
						for (CompositionDataItem dataListItem : view.getMainDataList()) {
							dataListItem.getExtraProperties().put(PLMModel.PROP_COMPARE_WITH_DYN_COLUMN, null);
						}
					}
				}
			});
		}
		return true;
	}

	private void processComparison(final ProductData referenceProduct, List<ProductData> comparedProducts, AbstractProductDataView view,
			Map<DynamicCharactListItem, JSONArray> dynamicCharactResults,
			Map<Pair<CompositionDataItem, QName>, JSONArray> dynamicColumnResults) {
		try {
			for (ProductData comparedProduct : comparedProducts) {
				if (logger.isDebugEnabled()) {
					logger.debug("Comparing : " + referenceProduct.getName() + " with " + comparedProduct.getName() + " for view "
							+ view.getClass().getName());
				}
				compareDynamicColumn(comparedProduct, PLMModel.PROP_COMPARE_WITH_DYN_COLUMN, view, dynamicColumnResults);
				compareDynamicCharacts(comparedProduct, view, dynamicCharactResults, dynamicColumnResults);
			}
		} catch (JSONException e) {
			logger.error(e, e);
		}
	}

	private void compareDynamicColumn(ProductData comparedProduct, QName columnName, AbstractProductDataView view,
			Map<Pair<CompositionDataItem, QName>, JSONArray> dynamicColumnResults) {
		int pos = 0;
		Set<CompositionDataItem> cache = new HashSet<>();
		for (CompositionDataItem referenceItem : view.getMainDataList()) {
			CompositionDataItem comparedItem = findMatchingItem(referenceItem, getMatchingView(comparedProduct, view).getMainDataList(), cache, pos);
			if (comparedItem != null) {
				compareItems(referenceItem, comparedItem, columnName, comparedProduct, dynamicColumnResults);
			}
			pos++;
		}
	}

	private void compareItems(CompositionDataItem referenceItem, CompositionDataItem comparedItem, QName columnName,
			ProductData comparedProduct, Map<Pair<CompositionDataItem, QName>, JSONArray> dynamicColumnResults) {
		JSONArray itemComparisonArray = dynamicColumnResults.get(new Pair<>(referenceItem, columnName));
		if (itemComparisonArray == null) {
			itemComparisonArray = new JSONArray();
			JSONObject referenceValue = extractCompoItemValue(referenceItem, columnName, null);
			itemComparisonArray.put(referenceValue);
		}
		JSONObject comparedValue = extractCompoItemValue(comparedItem, columnName, comparedProduct);
		itemComparisonArray.put(comparedValue);
		dynamicColumnResults.put(new Pair<>(referenceItem, columnName), itemComparisonArray);
	}

	private void compareDynamicCharacts(ProductData comparedProduct, AbstractProductDataView view,
			Map<DynamicCharactListItem, JSONArray> dynamicCharactToTreat,
			Map<Pair<CompositionDataItem, QName>, JSONArray> dynamicColumnToTreat) {
		
		Map<String, Integer> duplicatedColumnsMap = getDuplicatedColumnLastIndexMap(view);

		for (int i = 0; i < view.getDynamicCharactList().size(); ++i) {
			DynamicCharactListItem dynamicCharactListItem = view.getDynamicCharactList().get(i);

			// if this column is duplicated and not the last occurrence, we don't compare
			boolean shouldCompare = (dynamicCharactListItem.getColumnName() == null)
					|| "".equals(dynamicCharactListItem.getColumnName())
					|| !duplicatedColumnsMap.containsKey(dynamicCharactListItem.getColumnName())
					|| (i == duplicatedColumnsMap.get(dynamicCharactListItem.getColumnName()));

			if (shouldCompare) {
				if (dynamicCharactListItem.getFormula() != null && !dynamicCharactListItem.getFormula().isEmpty()) {
					
					DynamicCharactListItem toCompareDynamicCharactListItem = getMatchingCharact(dynamicCharactListItem,
							getMatchingView(comparedProduct, view).getDynamicCharactList());
					
					if (toCompareDynamicCharactListItem != null) {
						
						if (logger.isDebugEnabled()) {
							logger.debug(" - Found matching charact to compare: ");
							logger.debug(" - " + toCompareDynamicCharactListItem.toString());
						}
						
						// DynamicColumns
						if ((dynamicCharactListItem.getColumnName() != null) && !dynamicCharactListItem.getColumnName().isEmpty()) {
							
							QName columnName = QName.createQName(dynamicCharactListItem.getColumnName().replaceFirst("_", ":"), namespaceService);
							
							compareDynamicColumn(comparedProduct, columnName, view, dynamicColumnToTreat);
							
							// DynamicCharacts
						} else {
							JSONArray itemComparisonArray = dynamicCharactToTreat.get(dynamicCharactListItem);
							if (itemComparisonArray == null) {
								itemComparisonArray = new JSONArray();
								JSONObject refInfo = new JSONObject();
								refInfo.put(JsonFormulaHelper.JSON_VALUE, dynamicCharactListItem.getValue());
								itemComparisonArray.put(refInfo);
							}
							JSONObject comparedInfo = new JSONObject();
							comparedInfo.put(JsonFormulaHelper.JSON_VALUE, toCompareDynamicCharactListItem.getValue());
							appendProductInfo(comparedInfo, comparedProduct);
							itemComparisonArray.put(comparedInfo);
							dynamicCharactToTreat.put(dynamicCharactListItem, itemComparisonArray);
						}
					}
				}
			}
		}
	}

	private Map<String, Integer> getDuplicatedColumnLastIndexMap(AbstractProductDataView view) {
		// map each duplicated column to its last occurrence in the dynamicCharactList

		Map<String, Integer> duplicateDynCharactColumnsMap = new HashMap<>();

		Map<String, Long> columnNameOccurrences = view.getDynamicCharactList().stream()
				.filter(charact -> charact.getColumnName() != null).map(DynamicCharactListItem::getColumnName)
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

		columnNameOccurrences.forEach((column, occurrences) -> {
			if (occurrences > 1) {
				duplicateDynCharactColumnsMap.put(column, view.getDynamicCharactList().stream()
						.map(DynamicCharactListItem::getColumnName).collect(Collectors.toList()).lastIndexOf(column));
			}
		});
		return duplicateDynCharactColumnsMap;
	}

	private void applyResults(Map<DynamicCharactListItem, JSONArray> dynamicCharactResults, Map<Pair<CompositionDataItem, QName>, JSONArray> dynamicColumnResults) {
		for (Map.Entry<DynamicCharactListItem, JSONArray> entry : dynamicCharactResults.entrySet()) {
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.put(JsonFormulaHelper.JSON_COMP_ITEMS, entry.getValue());
			
			entry.getKey().setValue(jsonObject.toString());
		}
		
		for (Map.Entry<Pair<CompositionDataItem, QName>, JSONArray> entry : dynamicColumnResults.entrySet()) {
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.put(JsonFormulaHelper.JSON_COMP_ITEMS, entry.getValue());
			
			entry.getKey().getFirst().getExtraProperties().put(entry.getKey().getSecond(), jsonObject.toString());
		}
	}

	private JSONObject extractCompoItemValue(CompositionDataItem dataListItem, QName columnName, ProductData associatedProduct) {

		JSONObject jsonObject = new JSONObject();
		
		if (PLMModel.PROP_COMPARE_WITH_DYN_COLUMN.equals(columnName)) {
			if (dataListItem instanceof CompoListDataItem) {
				jsonObject.put(JsonFormulaHelper.JSON_VALUE, ((CompoListDataItem) dataListItem).getQtySubFormula());
			} else {
				jsonObject.put(JsonFormulaHelper.JSON_VALUE, dataListItem.getQty());
			}
			if (dataListItem.getNodeRef() != null) {
				jsonObject.put("itemNodeRef", dataListItem.getNodeRef());
			}
		} else {
			Serializable extraPropertyValue = dataListItem.getExtraProperties().get(columnName);
			if (extraPropertyValue != null) {
				try {
					JSONObject extraProperty = new JSONObject(extraPropertyValue.toString());
					jsonObject.put(JsonFormulaHelper.JSON_VALUE, extraProperty.get("value"));
					if (extraProperty.has("sub")) {
						
						JSONArray subs = extraProperty.getJSONArray("sub");
						JSONArray newSubs = new JSONArray();
						
						for (int i = 0; i < subs.length(); i++) {
							JSONObject subItem = subs.getJSONObject(i);
							if (associatedProduct != null) {
								appendProductInfo(subItem, associatedProduct);
							}
							newSubs.put(subItem);
						}
						
						jsonObject.put("sub", newSubs);
					} else {
						jsonObject.put(JsonFormulaHelper.JSON_VALUE, extraPropertyValue);
					}
				} catch (JSONException e) {
					jsonObject.put(JsonFormulaHelper.JSON_VALUE, extraPropertyValue);
				}
			}
		}
		
		if (associatedProduct != null) {
			appendProductInfo(jsonObject, associatedProduct);
		}

		return jsonObject;
	}

	private void appendProductInfo(JSONObject jsonObject, ProductData productData) {
		if (productData != null) {
			jsonObject.put(JsonFormulaHelper.JSON_NODEREF, productData.getNodeRef());
			jsonObject.put("name", productData.getName());
			jsonObject.put("itemType", nodeService.getType(productData.getNodeRef()).toPrefixString(namespaceService));
			String siteId = attributeExtractorService.extractSiteId(productData.getNodeRef());
			if (siteId != null) {
				jsonObject.put("siteId", siteId);
			}
		}
	}

	private AbstractProductDataView getMatchingView(ProductData productData, AbstractProductDataView view) {
		for (AbstractProductDataView tmp : productData.getViews()) {
			if (tmp.getClass().isAssignableFrom(view.getClass())) {
				return tmp;
			}
		}
		throw new IllegalStateException("No Matching view");
	}

	private CompositionDataItem findMatchingItem(CompositionDataItem dataListItem,
			List<? extends CompositionDataItem> compositionDataItems, Set<CompositionDataItem> cache, int currentPos) {
		// branches

		CompositionDataItem ret = null;
		int tmpPos = 0;
		int posDiff = 1000;
		for (CompositionDataItem tmp : compositionDataItems) {
			// Skip if already match
			if (!cache.contains(tmp)) {
				// Same NodeRef Same Pos
				if (Objects.equals(tmp.getComponent(), dataListItem.getComponent())) {
					// We break as no better match
					if (tmpPos == currentPos) {
						ret = tmp;
						break;
					} else if (Math.abs(tmpPos - currentPos) < posDiff) {
						posDiff = Math.abs(tmpPos - currentPos);
						ret = tmp;
					}
				}

				if (approxMatch(dataListItem.getComponent(), tmp.getComponent()) && (Math.abs(tmpPos - currentPos) < posDiff)) {
					posDiff = Math.abs(tmpPos - currentPos);
					ret = tmp;
				}
			}
			tmpPos++;
		}

		if (ret != null) {
			cache.add(ret);
		}

		return ret;
	}

	private boolean approxMatch(NodeRef refProductNodeRef, NodeRef toCompareProductNoRef) {

		if ((refProductNodeRef != null) && (toCompareProductNoRef != null)) {
			NodeRef copiedFromRef = associationService.getTargetAssoc(refProductNodeRef, ContentModel.ASSOC_ORIGINAL);
			NodeRef copiedFromComp = associationService.getTargetAssoc(toCompareProductNoRef, ContentModel.ASSOC_ORIGINAL);

			return ((copiedFromRef != null) && copiedFromRef.equals(toCompareProductNoRef))
					|| ((copiedFromComp != null) && copiedFromComp.equals(refProductNodeRef))
					|| nodeService.getProperty(refProductNodeRef, ContentModel.PROP_NAME)
							.equals(nodeService.getProperty(toCompareProductNoRef, ContentModel.PROP_NAME));
		}
		return false;
	}

	private DynamicCharactListItem getMatchingCharact(DynamicCharactListItem dynamicCharactListItem,
			List<DynamicCharactListItem> dynamicCharactList) {
		for (DynamicCharactListItem tmp : dynamicCharactList) {
			if ((dynamicCharactListItem.getFormula() != null) && Objects.equals(tmp.getFormula(), dynamicCharactListItem.getFormula())) {
				if ((dynamicCharactListItem.getColumnName() == null) || dynamicCharactListItem.getColumnName().isEmpty()
						|| ((dynamicCharactListItem.getColumnName() != null) && !dynamicCharactListItem.getColumnName().isEmpty()
								&& Objects.equals(dynamicCharactListItem.getColumnName(), tmp.getColumnName()))) {
					return tmp;
				}
			}
		}
		return null;
	}

	private List<ProductData> getComparableProductDatas(ProductData productData) {

		List<ProductData> compareWithEntities = productData.getCompareWithEntities();
		List<ProductData> ret = new ArrayList<>();
		if ((compareWithEntities != null) && !compareWithEntities.isEmpty()) {

			for (ProductData tmpData : compareWithEntities) {
				try {
					ret.add(productService.formulate(tmpData));
				} catch (FormulateException e) {
					logger.warn(e, e);
					productData.getReqCtrlList()
							.add(RequirementListDataItem.tolerated()
									.withMessage(
											MLTextHelper.getI18NMessage("message.formulate.comparewith.formulate.entity.error", tmpData.getName()))
									.ofDataType(RequirementDataType.Nutrient));
				}
			}

		}
		return ret;
	}

}
