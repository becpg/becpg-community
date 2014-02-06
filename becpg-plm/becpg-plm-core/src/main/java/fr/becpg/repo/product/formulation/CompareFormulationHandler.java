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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.server.support.query.CmisQlExtParser_CmisBaseGrammar.null_predicate_return;
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
										for (CompositionDataItem dataListItem : view.getMainDataList()) {
											CompositionDataItem toCompareWithCompositionDataItem = getMatchingCompositionDataItem(dataListItem, columnName,
													getMatchingView(toCompareWith, view).getMainDataList());
											if (toCompareWithCompositionDataItem != null) {
												JSONArray values = dynamicColumnToTreat.get(new Pair<>(dataListItem, columnName));
												if (values == null) {
													values = new JSONArray();
													values.put(formatValue(dataListItem.getExtraProperties().get(columnName)));
												}
												values.put(formatValue(toCompareWithCompositionDataItem.getExtraProperties().get(columnName)));
												dynamicColumnToTreat.put(new Pair<>(dataListItem, columnName), values);
											}
										}
										// DynamicCharacts
									} else {
										JSONArray values = dynamicCharactToTreat.get(dynamicCharactListItem);
										if (values == null) {
											values = new JSONArray();
											values.put(formatValue(dynamicCharactListItem.getValue()));
										}
										values.put(formatValue(toCompareDynamicCharactListItem.getValue()));
										dynamicCharactToTreat.put(dynamicCharactListItem, values);
									}
								}
							}

						}

						try {

							// Set definitive value
							for (Map.Entry<DynamicCharactListItem, JSONArray> entry : dynamicCharactToTreat.entrySet()) {

								JSONObject jsonObject = new JSONObject();
								jsonObject.put("comp", entry.getValue());

								entry.getKey().setValue(jsonObject.toString());
							}

							for (Map.Entry<Pair<CompositionDataItem, QName>, JSONArray> entry : dynamicColumnToTreat.entrySet()) {

								JSONObject jsonObject = new JSONObject();
								jsonObject.put("comp", entry.getValue());

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

	private CompositionDataItem getMatchingCompositionDataItem(CompositionDataItem dataListItem, QName columnName, List<? extends CompositionDataItem> compositionDataItems) {
		// TODO should be better matching base on sort and approching name or branches
		for (CompositionDataItem tmp : compositionDataItems) {
			if (Objects.equals(tmp.getProduct(), dataListItem.getProduct())
					&& !Objects.equals(tmp.getExtraProperties().get(columnName), dataListItem.getExtraProperties().get(columnName))) {
				return tmp;
			}
		}
		return null;
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
