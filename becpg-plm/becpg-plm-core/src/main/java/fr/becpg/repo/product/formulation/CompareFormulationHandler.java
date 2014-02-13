/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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

import fr.becpg.config.format.PropertyFormats;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.CompareHelper;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.AbstractProductDataView;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompositionDataItem;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.data.productList.RequirementType;
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

	private static Log logger = LogFactory.getLog(CompareFormulationHandler.class);

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

		if (!L2CacheSupport.isCacheOnlyEnable()
				&& (productData.getAspects().contains(BeCPGModel.ASPECT_COMPARE_WITH) || nodeService.hasAspect(productData.getNodeRef(), BeCPGModel.ASPECT_COMPARE_WITH))) {
			L2CacheSupport.doInCacheContext(new Action() {

				@Override
				public void run() {

					List<ProductData> toCompareWithProductDatas = getComparableProductDatas(productData);
					for (AbstractProductDataView view : productData.getViews()) {

						Map<DynamicCharactListItem, JSONArray> dynamicCharactToTreat = new HashMap<>();
						Map<Pair<CompositionDataItem, QName>, JSONArray> dynamicColumnToTreat = new HashMap<>();
						try {
							for (ProductData toCompareWith : toCompareWithProductDatas) {

								if (logger.isDebugEnabled()) {
									logger.debug("Comparing : " + productData.getName() + " with " + toCompareWith.getName() + " for view " + view.getClass().getName());
								}

								for (DynamicCharactListItem dynamicCharactListItem : view.getDynamicCharactList()) {
									DynamicCharactListItem toCompareDynamicCharactListItem = getMatchingCharact(dynamicCharactListItem, getMatchingView(toCompareWith, view)
											.getDynamicCharactList());
									if (toCompareDynamicCharactListItem != null) {

										if (logger.isDebugEnabled()) {
											logger.debug(" - Found matching charact to compare: ");
											logger.debug(" - " + toCompareDynamicCharactListItem.toString());
										}

										// DynamicColumns
										if (dynamicCharactListItem.getColumnName() != null && !dynamicCharactListItem.getColumnName().isEmpty()) {
											QName columnName = QName.createQName(dynamicCharactListItem.getColumnName().replaceFirst("_", ":"), namespaceService);
											int pos = 0;
											Set<CompositionDataItem> cache = new HashSet<>();
											for (CompositionDataItem dataListItem : view.getMainDataList()) {
												CompositionDataItem toCompareWithCompositionDataItem = getMatchingCompositionDataItem(dataListItem, columnName,
														getMatchingView(toCompareWith, view).getMainDataList(), cache, pos);
												if (toCompareWithCompositionDataItem != null) {
													JSONArray values = dynamicColumnToTreat.get(new Pair<>(dataListItem, columnName));
													if (values == null) {
														values = new JSONArray();
														values.put(getJSONValue(toCompareWith, dataListItem.getExtraProperties().get(columnName)));
													}
													values.put(getJSONValue(toCompareWith, toCompareWithCompositionDataItem.getExtraProperties().get(columnName)));
													dynamicColumnToTreat.put(new Pair<>(dataListItem, columnName), values);
												}
												pos++;
											}
											// DynamicCharacts
										} else {
											JSONArray values = dynamicCharactToTreat.get(dynamicCharactListItem);
											if (values == null) {
												values = new JSONArray();
												values.put(getJSONValue(toCompareWith, dynamicCharactListItem.getValue()));
											}
											values.put(getJSONValue(toCompareWith, toCompareDynamicCharactListItem.getValue()));
											dynamicCharactToTreat.put(dynamicCharactListItem, values);
										}
									}
								}

							}

							// Set definitive value
							for (Map.Entry<DynamicCharactListItem, JSONArray> entry : dynamicCharactToTreat.entrySet()) {

								JSONObject jsonObject = new JSONObject();
								jsonObject.put(CompareHelper.JSON_COMP_ITEMS, entry.getValue());

								entry.getKey().setValue(jsonObject.toString());
							}

							for (Map.Entry<Pair<CompositionDataItem, QName>, JSONArray> entry : dynamicColumnToTreat.entrySet()) {

								JSONObject jsonObject = new JSONObject();
								jsonObject.put(CompareHelper.JSON_COMP_ITEMS, entry.getValue());

								entry.getKey().getFirst().getExtraProperties().put(entry.getKey().getSecond(), jsonObject.toString());
							}
						} catch (JSONException e) {
							logger.error(e, e);
						}

					}
				}

			}, true);
		}
		return true;
	}

	PropertyFormats propertyFormats = new PropertyFormats(true);

	private JSONObject getJSONValue(ProductData toCompareWith, Object value) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(CompareHelper.JSON_COMP_ITEM_NODEREF, toCompareWith.getNodeRef());
		jsonObject.put("name", toCompareWith.getName());
		jsonObject.put(CompareHelper.JSON_COMP_VALUE, value);
		jsonObject.put("itemType", nodeService.getType(toCompareWith.getNodeRef()).toPrefixString(namespaceService));
		jsonObject.put("displayValue", formatValue(value));
		String siteId = attributeExtractorService.extractSiteId(toCompareWith.getNodeRef());
		if (siteId != null) {
			jsonObject.put("siteId", siteId);
		}
		return jsonObject;
	}

	private Object formatValue(Object v) {
		if (v != null && (v instanceof Double || v instanceof Float)) {

			if (propertyFormats.getDecimalFormat() != null) {
				return propertyFormats.getDecimalFormat().format(v);
			} else {
				return v.toString();
			}
		}
		return v;
	}

	private AbstractProductDataView getMatchingView(ProductData productData, AbstractProductDataView view) {
		for (AbstractProductDataView tmp : productData.getViews()) {
			if (tmp.getClass().getName().equals(view.getClass().getName())) {
				return tmp;
			}
		}
		throw new IllegalStateException("No Matching view");
	}

	private CompositionDataItem getMatchingCompositionDataItem(CompositionDataItem dataListItem, QName columnName, List<? extends CompositionDataItem> compositionDataItems,
			Set<CompositionDataItem> cache, int currentPos) {
		// branches

		CompositionDataItem ret = null;
		int tmpPos = 0;
		int posDiff = 1000;
		for (CompositionDataItem tmp : compositionDataItems) {
			//Skip if already match
			if (!cache.contains(tmp)) {
				// Same NodeRef Same Pos
				if (Objects.equals(tmp.getProduct(), dataListItem.getProduct()) ) {
					// We break as no better match
					if(tmpPos == currentPos) {
						ret = tmp;
						break;
					} else if (Math.abs(tmpPos-currentPos)<posDiff){
						posDiff = Math.abs(tmpPos-currentPos);
						ret = tmp;
					}
				}
				
				if(approxMatch(dataListItem.getProduct(), tmp.getProduct()) && Math.abs(tmpPos-currentPos)<posDiff) {
					posDiff = Math.abs(tmpPos-currentPos);
					ret = tmp;
				}	
			}
			tmpPos++;
		}

		if (ret != null) {
			cache.add(ret);
			if(Objects.equals(ret.getExtraProperties().get(columnName), dataListItem.getExtraProperties().get(columnName))) {
				//Match but same value
				 ret = null;
			}
		}

		return ret;
	}

	private boolean approxMatch(NodeRef refProductNodeRef, NodeRef toCompareProductNoRef) {
		//TODO test fuzzy match
		NodeRef copiedFromRef = associationService.getTargetAssoc(refProductNodeRef, ContentModel.ASSOC_ORIGINAL);
		NodeRef copiedFromComp = associationService.getTargetAssoc(toCompareProductNoRef, ContentModel.ASSOC_ORIGINAL);
		
		return (copiedFromRef!=null && copiedFromRef.equals(toCompareProductNoRef)) ||
				(copiedFromComp!=null && copiedFromComp.equals(refProductNodeRef)) ||
				nodeService.getProperty(refProductNodeRef, ContentModel.PROP_NAME).equals(nodeService.getProperty(toCompareProductNoRef, ContentModel.PROP_NAME));
	}

	private DynamicCharactListItem getMatchingCharact(DynamicCharactListItem dynamicCharactListItem, List<DynamicCharactListItem> dynamicCharactList) {
		for (DynamicCharactListItem tmp : dynamicCharactList) {
			if (Objects.equals(tmp.getFormula(), dynamicCharactListItem.getFormula())) {
				if (!Objects.equals(tmp.getValue(), dynamicCharactListItem.getValue())
						|| (dynamicCharactListItem.getColumnName() != null && !dynamicCharactListItem.getColumnName().isEmpty() && Objects.equals(
								dynamicCharactListItem.getColumnName(), tmp.getColumnName()))) {
					return tmp;
				}
			}
		}
		return null;
	}

	private List<ProductData> getComparableProductDatas(ProductData productData) {

		List<NodeRef> compareWithEntities = associationService.getTargetAssocs(productData.getNodeRef(), BeCPGModel.ASSOC_COMPARE_WITH_ENTITIES);
		List<ProductData> ret = new ArrayList<>();
		if (compareWithEntities != null && !compareWithEntities.isEmpty()) {

			for (NodeRef entityNodeRef : compareWithEntities) {
				ProductData tmpData = alfrescoRepository.findOne(entityNodeRef);
				try {
					ret.add(productService.formulate(tmpData));
				} catch (FormulateException e) {
					logger.warn(e, e);
					String message = I18NUtil.getMessage("message.formulate.comparewith.formulate.entity.error", Locale.getDefault(), tmpData.getName());
					productData.getCompoListView().getReqCtrlList().add(new ReqCtrlListDataItem(null, RequirementType.Tolerated, message, new ArrayList<NodeRef>()));
				}
			}

		}
		return ret;
	}

}
