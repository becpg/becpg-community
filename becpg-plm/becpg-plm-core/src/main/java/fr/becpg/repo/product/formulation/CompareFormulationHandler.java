/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

import com.google.gdata.util.common.base.Pair;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.JsonFormulaHelper;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.AbstractProductDataView;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompositionDataItem;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.repository.L2CacheSupport.Action;

/**
 * Compare product with each other
 *
 * @author matthieu
 *
 */
public class CompareFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(CompareFormulationHandler.class);

	private AssociationService associationService;

	private AlfrescoRepository<ProductData> alfrescoRepository;

	private ProductService productService;

	private NamespaceService namespaceService;

	private NodeService nodeService;

	private AttributeExtractorService attributeExtractorService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setAttributeExtractorService(AttributeExtractorService attributeExtractorService) {
		this.attributeExtractorService = attributeExtractorService;
	}

	@Override
	public boolean process(final ProductData productData) throws FormulateException {

		if (productData.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL)) {
			return true;
		}

		if (!L2CacheSupport.isCacheOnlyEnable() && (productData.getAspects().contains(BeCPGModel.ASPECT_COMPARE_WITH)
				|| ((productData.getNodeRef() != null) && nodeService.hasAspect(productData.getNodeRef(), BeCPGModel.ASPECT_COMPARE_WITH)))) {
			L2CacheSupport.doInCacheContext(new Action() {

				@Override
				public void run() {

					List<ProductData> toCompareWithProductDatas = getComparableProductDatas(productData);
					for (AbstractProductDataView view : productData.getViews()) {

						if ((toCompareWithProductDatas != null) && !toCompareWithProductDatas.isEmpty()) {

							Map<DynamicCharactListItem, JSONArray> dynamicCharactToTreat = new HashMap<>();
							Map<Pair<CompositionDataItem, QName>, JSONArray> dynamicColumnToTreat = new HashMap<>();
							try {
								for (ProductData toCompareWith : toCompareWithProductDatas) {

									if (logger.isDebugEnabled()) {
										logger.debug("Comparing : " + productData.getName() + " with " + toCompareWith.getName() + " for view "
												+ view.getClass().getName());
									}

									addCompareValueColumn(PLMModel.PROP_COMPARE_WITH_DYN_COLUMN, view, toCompareWith, dynamicColumnToTreat, true);

									for (DynamicCharactListItem dynamicCharactListItem : view.getDynamicCharactList()) {
										if (!Boolean.TRUE.equals(dynamicCharactListItem.getMultiLevelFormula())
												&& dynamicCharactListItem.getFormula()!= null && !dynamicCharactListItem.getFormula().isEmpty()) {

											DynamicCharactListItem toCompareDynamicCharactListItem = getMatchingCharact(dynamicCharactListItem,
													getMatchingView(toCompareWith, view).getDynamicCharactList());
											if (toCompareDynamicCharactListItem != null) {

												if (logger.isDebugEnabled()) {
													logger.debug(" - Found matching charact to compare: ");
													logger.debug(" - " + toCompareDynamicCharactListItem.toString());
												}

												// DynamicColumns
												if ((dynamicCharactListItem.getColumnName() != null)
														&& !dynamicCharactListItem.getColumnName().isEmpty()) {
													QName columnName = QName.createQName(
															dynamicCharactListItem.getColumnName().replaceFirst("_", ":"), namespaceService);

													addCompareValueColumn(columnName, view, toCompareWith, dynamicColumnToTreat, false);

													// DynamicCharacts
												} else {
													JSONArray values = dynamicCharactToTreat.get(dynamicCharactListItem);
													if (values == null) {
														values = new JSONArray();
														values.put(getJSONValue(toCompareWith, dynamicCharactListItem.getValue(), null));
													}
													values.put(getJSONValue(toCompareWith, toCompareDynamicCharactListItem.getValue(), null));
													dynamicCharactToTreat.put(dynamicCharactListItem, values);
												}
											}
										}
									}

								}

								// Set definitive value
								for (Map.Entry<DynamicCharactListItem, JSONArray> entry : dynamicCharactToTreat.entrySet()) {

									JSONObject jsonObject = new JSONObject();
									jsonObject.put(JsonFormulaHelper.JSON_COMP_ITEMS, entry.getValue());

									entry.getKey().setValue(jsonObject.toString());
								}

								for (Map.Entry<Pair<CompositionDataItem, QName>, JSONArray> entry : dynamicColumnToTreat.entrySet()) {

									JSONObject jsonObject = new JSONObject();
									jsonObject.put(JsonFormulaHelper.JSON_COMP_ITEMS, entry.getValue());

									entry.getKey().getFirst().getExtraProperties().put(entry.getKey().getSecond(), jsonObject.toString());
								}
							} catch (JSONException e) {
								logger.error(e, e);
							}
						} else {
							for (CompositionDataItem dataListItem : view.getMainDataList()) {
								dataListItem.getExtraProperties().put(PLMModel.PROP_COMPARE_WITH_DYN_COLUMN, null);
							}

						}
					}
				}

				private void addCompareValueColumn(QName columnName, AbstractProductDataView view, ProductData toCompareWith,
						Map<Pair<CompositionDataItem, QName>, JSONArray> dynamicColumnToTreat, boolean isQty) throws JSONException {
					int pos = 0;
					Set<CompositionDataItem> cache = new HashSet<>();
					for (CompositionDataItem dataListItem : view.getMainDataList()) {
						CompositionDataItem toCompareWithCompositionDataItem = getMatchingCompositionDataItem(dataListItem, isQty ? null : columnName,
								getMatchingView(toCompareWith, view).getMainDataList(), cache, pos);
						if (toCompareWithCompositionDataItem != null) {
							JSONArray values = dynamicColumnToTreat.get(new Pair<>(dataListItem, columnName));
							if (values == null) {
								values = new JSONArray();
								values.put(getJSONValue(toCompareWith, dataListItem, columnName, isQty));
							}
							values.put(getJSONValue(toCompareWith, toCompareWithCompositionDataItem, columnName, isQty));
							dynamicColumnToTreat.put(new Pair<>(dataListItem, columnName), values);
						}
						pos++;
					}

				}

			}, true);
		}
		return true;
	}

	private JSONObject getJSONValue(ProductData toCompareWith, CompositionDataItem dataListItem, QName columnName, boolean isQty)
			throws JSONException {

		Object value = getValue(dataListItem, columnName, isQty);

		if (isQty) {
			return getJSONValue(toCompareWith, value, dataListItem.getNodeRef());
		} else {
			return getJSONValue(toCompareWith, value, null);
		}

	}

	private JSONObject getJSONValue(ProductData toCompareWith, Object value, NodeRef itemNodeRef) throws JSONException {
		JSONObject jsonObject = new JSONObject();

		jsonObject.put(JsonFormulaHelper.JSON_NODEREF, toCompareWith.getNodeRef());
		jsonObject.put("name", toCompareWith.getName());

		if (itemNodeRef != null) {
			jsonObject.put("itemNodeRef", itemNodeRef);
		}

		jsonObject.put(JsonFormulaHelper.JSON_VALUE, value);
		jsonObject.put(JsonFormulaHelper.JSON_DISPLAY_VALUE, JsonFormulaHelper.formatValue(value));
		jsonObject.put("itemType", nodeService.getType(toCompareWith.getNodeRef()).toPrefixString(namespaceService));
		String siteId = attributeExtractorService.extractSiteId(toCompareWith.getNodeRef());
		if (siteId != null) {
			jsonObject.put("siteId", siteId);
		}

		return jsonObject;
	}

	private Object getValue(CompositionDataItem dataListItem, QName columnName, boolean isQty) {
		if (isQty) {
			if (dataListItem instanceof CompoListDataItem) {
				return ((CompoListDataItem) dataListItem).getQtySubFormula();
			}
			return dataListItem.getQty();
		}
		return dataListItem.getExtraProperties().get(columnName);
	}

	private AbstractProductDataView getMatchingView(ProductData productData, AbstractProductDataView view) {
		for (AbstractProductDataView tmp : productData.getViews()) {
			if (tmp.getClass().getName().equals(view.getClass().getName())) {
				return tmp;
			}
		}
		throw new IllegalStateException("No Matching view");
	}

	private CompositionDataItem getMatchingCompositionDataItem(CompositionDataItem dataListItem, QName columnName,
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
			// if ((columnName != null) &&
			// Objects.equals(ret.getExtraProperties().get(columnName),
			// dataListItem.getExtraProperties().get(columnName))) {
			// // Match but same value
			// ret = null;
			// }
		}

		return ret;
	}

	private boolean approxMatch(NodeRef refProductNodeRef, NodeRef toCompareProductNoRef) {
		// TODO test fuzzy match
		NodeRef copiedFromRef = associationService.getTargetAssoc(refProductNodeRef, ContentModel.ASSOC_ORIGINAL);
		NodeRef copiedFromComp = associationService.getTargetAssoc(toCompareProductNoRef, ContentModel.ASSOC_ORIGINAL);

		return ((copiedFromRef != null) && copiedFromRef.equals(toCompareProductNoRef))
				|| ((copiedFromComp != null) && copiedFromComp.equals(refProductNodeRef))
				|| nodeService.getProperty(refProductNodeRef, ContentModel.PROP_NAME)
						.equals(nodeService.getProperty(toCompareProductNoRef, ContentModel.PROP_NAME));
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

		List<NodeRef> compareWithEntities = associationService.getTargetAssocs(productData.getNodeRef(), BeCPGModel.ASSOC_COMPARE_WITH_ENTITIES);
		List<ProductData> ret = new LinkedList<>();
		if ((compareWithEntities != null) && !compareWithEntities.isEmpty()) {

			for (NodeRef entityNodeRef : compareWithEntities) {
				ProductData tmpData = alfrescoRepository.findOne(entityNodeRef);
				try {
					ret.add(productService.formulate(tmpData));
				} catch (FormulateException e) {
					logger.warn(e, e);
					String message = I18NUtil.getMessage("message.formulate.comparewith.formulate.entity.error", Locale.getDefault(),
							tmpData.getName());
					productData.getReqCtrlList().add(new ReqCtrlListDataItem(null, RequirementType.Tolerated, message, null,
							new ArrayList<NodeRef>(), RequirementDataType.Nutrient));
				}
			}

		}
		return ret;
	}

}
