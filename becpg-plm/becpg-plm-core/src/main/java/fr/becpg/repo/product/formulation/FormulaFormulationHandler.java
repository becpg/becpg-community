/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG.
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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.product.formulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.helper.JsonFormulaHelper;
import fr.becpg.repo.product.data.AbstractProductDataView;
import fr.becpg.repo.product.data.CompoListView;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.PackagingKitData;
import fr.becpg.repo.product.data.PackagingListView;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.PackagingListUnit;
import fr.becpg.repo.product.data.productList.CompositionDataItem;
import fr.becpg.repo.product.data.productList.DynamicCharactExecOrder;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.spel.FormulaFormulationContext;
import fr.becpg.repo.product.data.spel.SpelHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.security.BeCPGAccessDeniedException;

/**
 * Use Spring EL to parse formula and compute value
 *
 * @author matthieu
 *
 */
public class FormulaFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(FormulaFormulationHandler.class);

	public static final int DYN_COLUMN_SIZE = 10;
	public static final String DYN_COLUMN_NAME = "bcpg:dynamicCharactColumn";

	private AlfrescoRepository<ProductData> alfrescoRepository;

	private NodeService nodeService;

	private NamespaceService namespaceService;

	private FormulaService formulaService;

	private DynamicCharactExecOrder execOrder = DynamicCharactExecOrder.Post;

	public void setExecOrder(DynamicCharactExecOrder execOrder) {
		this.execOrder = execOrder;
	}

	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setFormulaService(FormulaService formulaService) {
		this.formulaService = formulaService;
	}

	@Override
	public boolean process(ProductData productData) throws FormulateException {

		if (productData.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL)) {
			return true;
		}

		if (DynamicCharactExecOrder.Pre.equals(execOrder)) {
			copyTemplateDynamicCharactLists(productData);
		}

		ExpressionParser parser = new SpelExpressionParser();
		StandardEvaluationContext context = formulaService.createEvaluationContext(productData);

		for (AbstractProductDataView view : productData.getViews()) {
			computeFormula(productData, parser, context, view);
		}

		if (DynamicCharactExecOrder.Post.equals(execOrder)) {
			computeNutrientProfile(productData, parser, context);
		}

		return true;
	}

	private void computeNutrientProfile(ProductData productData, ExpressionParser parser, StandardEvaluationContext context) {
		if (productData.getNutrientProfile() != null) {
			String scoreformula = (String) nodeService.getProperty(productData.getNutrientProfile(), PLMModel.PROP_NUTRIENT_PROFILE_SCORE_FORMULA);
			if ((scoreformula != null) && (scoreformula.length() > 0)) {
				try {
					productData.setNutrientScore(null);
					productData.setNutrientClass(null);
					Expression exp = parser.parseExpression(SpelHelper.formatFormula(scoreformula));
					Object ret = exp.getValue(context);
					if (ret instanceof Number) {
						productData.setNutrientScore(Double.valueOf(ret.toString()));
						String classformula = (String) nodeService.getProperty(productData.getNutrientProfile(),
								PLMModel.PROP_NUTRIENT_PROFILE_CLASS_FORMULA);
						if ((classformula != null) && (classformula.length() > 0)) {
							exp = parser.parseExpression(SpelHelper.formatFormula(classformula));
							productData.setNutrientClass((String) exp.getValue(context));
						}
					} else {
						productData.setNutrientClass(I18NUtil.getMessage("message.formulate.formula.incorrect.nutrientProfile",
								I18NUtil.getMessage("message.formulate.formula.incorrect.type.double", Locale.getDefault()), Locale.getDefault()));
					}
				} catch (Exception e) {
					productData.setNutrientClass(
							I18NUtil.getMessage("message.formulate.formula.incorrect.nutrientProfile", e.getLocalizedMessage(), Locale.getDefault()));
					if (logger.isDebugEnabled()) {
						logger.debug("Error in formula :" + SpelHelper.formatFormula(scoreformula), e);
					}
				}
			}

		}

	}

	private void computeFormula(ProductData productData, ExpressionParser parser, EvaluationContext context, AbstractProductDataView view) {

		if (view.getDynamicCharactList() != null) {

			Set<QName> nullDynColumnNames = new HashSet<>(DYN_COLUMN_SIZE);
			for (int i = 1; i <= DYN_COLUMN_SIZE; i++) {
				nullDynColumnNames.add(QName.createQName(DYN_COLUMN_NAME + i, namespaceService));
			}

			for (DynamicCharactListItem dynamicCharactListItem : view.getDynamicCharactList()) {
				if (execOrder.equals(dynamicCharactListItem.getExecOrder())) {
					try {
						if ((dynamicCharactListItem.getFormula() != null) && !dynamicCharactListItem.getFormula().isEmpty()) {
							String formula = SpelHelper.formatFormula(dynamicCharactListItem.getFormula());
							logger.debug("Parse formula : " + formula + " (" + dynamicCharactListItem.getName() + ")");
							Expression exp = parser.parseExpression(formula);

							if ((dynamicCharactListItem.getColumnName() != null) && !dynamicCharactListItem.getColumnName().isEmpty()) {
								QName columnName = QName.createQName(dynamicCharactListItem.getColumnName().replaceFirst("_", ":"), namespaceService);
								if (nullDynColumnNames.contains(columnName)) {
									nullDynColumnNames.remove(columnName);
								}
								for (CompositionDataItem dataListItem : view.getMainDataList()) {
									StandardEvaluationContext dataContext = new StandardEvaluationContext(
											new FormulaFormulationContext(alfrescoRepository, productData, dataListItem));
									formulaService.registerCustomFunctions(dataContext);
									Object value = exp.getValue(dataContext);

									if (!L2CacheSupport.isCacheOnlyEnable()
											&& ((dynamicCharactListItem.getMultiLevelFormula() != null)
													&& Boolean.TRUE.equals(dynamicCharactListItem.getMultiLevelFormula()))
											&& ((view instanceof CompoListView) || (view instanceof PackagingListView))
											&& ((dataListItem.getComponent() != null) && (PLMModel.TYPE_SEMIFINISHEDPRODUCT
													.equals(nodeService.getType(dataListItem.getComponent()))
													|| PLMModel.TYPE_FINISHEDPRODUCT.equals(nodeService.getType(dataListItem.getComponent()))
													|| PLMModel.TYPE_PACKAGINGKIT.equals(nodeService.getType(dataListItem.getComponent()))))) {

										JSONObject jsonTree = extractJSONTree(productData, dataListItem, value, exp);
										dataListItem.getExtraProperties().put(columnName, jsonTree.toString());
										if (logger.isDebugEnabled()) {
											logger.debug("JsonTree :" + value);
										}
									} else {
										dataListItem.getExtraProperties().put(columnName, (Serializable) value);
										logger.debug("Value :" + value);
									}
								}
								dynamicCharactListItem.setValue(null);
							} else {
								dynamicCharactListItem.setValue(exp.getValue(context));
								logger.debug("Value :" + dynamicCharactListItem.getValue());
							}
						}
// #1939 Do not reset as the user can modify the value						
//						} else {
//							dynamicCharactListItem.setValue(null);
//						}
						dynamicCharactListItem.setErrorLog(null);
					} catch (Exception e) {
						if ((e.getCause() != null) && (e.getCause().getCause() instanceof BeCPGAccessDeniedException)) {
							dynamicCharactListItem.setValue("#AccessDenied");
						} else {
							dynamicCharactListItem.setValue("#Error");
						}
						dynamicCharactListItem.setErrorLog(e.getLocalizedMessage());

						if (logger.isDebugEnabled()) {
							logger.debug("Error in formula :" + dynamicCharactListItem.getFormula() + " (" + dynamicCharactListItem.getName() + ")",
									e);
						}
					}
				} else {

					if ((dynamicCharactListItem.getColumnName() != null) && !dynamicCharactListItem.getColumnName().isEmpty()) {
						QName columnName = QName.createQName(dynamicCharactListItem.getColumnName().replaceFirst("_", ":"), namespaceService);
						if (nullDynColumnNames.contains(columnName)) {
							nullDynColumnNames.remove(columnName);
						}
					}
				}

			}

			// remove null columns
			for (QName nullDynColumnName : nullDynColumnNames) {
				for (CompositionDataItem dataListItem : view.getMainDataList()) {
					dataListItem.getExtraProperties().put(nullDynColumnName, null);
				}
			}
		}

	}

	private JSONObject extractJSONTree(ProductData productData, CompositionDataItem dataListItem, Object value, Expression exp) throws JSONException {
		JSONObject jsonObject = new JSONObject();

		JSONArray subList = new JSONArray();

		String path = "/" + dataListItem.getNodeRef().getId();

		extractJSONSubList(productData, dataListItem, exp, path, subList);

		if (subList.length() > 0) {
			jsonObject.put(JsonFormulaHelper.JSON_SUB_VALUES, subList);
		}

		jsonObject.put(JsonFormulaHelper.JSON_VALUE, value);
		jsonObject.put(JsonFormulaHelper.JSON_DISPLAY_VALUE, JsonFormulaHelper.formatValue(value));

		return jsonObject;
	}

	private void extractJSONSubList(ProductData productData, CompositionDataItem dataListItem, Expression exp, String path, JSONArray subList)
			throws JSONException {
		ProductData subProductData = alfrescoRepository.findOne(dataListItem.getComponent());
		List<Pair<CompositionDataItem, Double>> compositeList = new ArrayList<>();
		if ((subProductData instanceof FinishedProductData) || (subProductData instanceof SemiFinishedProductData)) {
			for (CompositionDataItem subDataListItem : subProductData.getCompoListView().getCompoList()) {
				Pair<CompositionDataItem, Double> pair = new Pair<>(subDataListItem, subDataListItem.getQty());
				if (FormulationHelper.getNetWeight(subProductData, FormulationHelper.DEFAULT_NET_WEIGHT) != 0) {
					subDataListItem.setQty((dataListItem.getQty() * subDataListItem.getQty())
							/ FormulationHelper.getNetWeight(subProductData, FormulationHelper.DEFAULT_NET_WEIGHT));
				}
				compositeList.add(pair);
			}
		} else if (subProductData instanceof PackagingKitData) {
			for (CompositionDataItem subDataListItem : subProductData.getPackagingListView().getPackagingList()) {
				Double listQty = dataListItem.getQty();
				Double subListQty = subDataListItem.getQty();
				Pair<CompositionDataItem, Double> pair = new Pair<>(subDataListItem, subDataListItem.getQty());

				if ((subDataListItem instanceof PackagingListDataItem) && (((PackagingListDataItem) subDataListItem).getPackagingListUnit() != null)
						&& PackagingListUnit.PP.equals(((PackagingListDataItem) subDataListItem).getPackagingListUnit())) {
					subListQty = 1 / subListQty;
				}
				if ((dataListItem instanceof PackagingListDataItem) && (((PackagingListDataItem) dataListItem).getPackagingListUnit() != null)
						&& PackagingListUnit.PP.equals(((PackagingListDataItem) dataListItem).getPackagingListUnit())) {
					listQty = 1 / listQty;
				}
				subDataListItem.setQty(listQty * subListQty);
				compositeList.add(pair);
			}
		}
		for (Pair<CompositionDataItem, Double> pair : compositeList) {
			try {
				JSONObject subObject = new JSONObject();
				
				
				StandardEvaluationContext dataContext = new StandardEvaluationContext(
						new FormulaFormulationContext(alfrescoRepository, productData, pair.getFirst()));
	
				String subPath = path + "/" + pair.getFirst().getNodeRef().getId();
	
				formulaService.registerCustomFunctions(dataContext);
				Object subValue = exp.getValue(dataContext);
				subObject.put(JsonFormulaHelper.JSON_VALUE, subValue);
				subObject.put(JsonFormulaHelper.JSON_DISPLAY_VALUE, JsonFormulaHelper.formatValue(subValue));
				subObject.put(JsonFormulaHelper.JSON_PATH, subPath);
				subList.put(subObject);
	
				if (PLMModel.TYPE_SEMIFINISHEDPRODUCT.equals(nodeService.getType(dataListItem.getComponent()))
						|| PLMModel.TYPE_FINISHEDPRODUCT.equals(nodeService.getType(dataListItem.getComponent()))
						|| PLMModel.TYPE_PACKAGINGKIT.equals(nodeService.getType(dataListItem.getComponent()))) {
					extractJSONSubList(productData, pair.getFirst(), exp, subPath, subList);
				}
			} finally {
				// Reset QTY
				pair.getFirst().setQty(pair.getSecond());
			}
		}
	}

	/**
	 * Copy missing item from template
	 *
	 * @param formulatedProduct
	 * @param simpleListDataList
	 */
	private void copyTemplateDynamicCharactLists(ProductData formulatedProduct) {
		if ((formulatedProduct.getEntityTpl() != null) && !formulatedProduct.getEntityTpl().equals(formulatedProduct)) {
			ProductData templateProductData = formulatedProduct.getEntityTpl();

			copyTemplateDynamicCharactList(templateProductData.getCompoListView().getDynamicCharactList(),
					formulatedProduct.getCompoListView().getDynamicCharactList());
			copyTemplateDynamicCharactList(templateProductData.getPackagingListView().getDynamicCharactList(),
					formulatedProduct.getPackagingListView().getDynamicCharactList());
			copyTemplateDynamicCharactList(templateProductData.getProcessListView().getDynamicCharactList(),
					formulatedProduct.getProcessListView().getDynamicCharactList());
		}
	}

	protected void copyTemplateDynamicCharactList(List<DynamicCharactListItem> sourceList, List<DynamicCharactListItem> targetList) {

		for (DynamicCharactListItem sourceItem : sourceList) {
			if (sourceItem.getTitle() != null) {
				if (sourceItem.isSynchronisable()) {
					boolean isFound = false;
					for (DynamicCharactListItem targetItem : targetList) {
						// charact renamed
						if (sourceItem.getName().equals(targetItem.getName()) && !sourceItem.getTitle().equals(targetItem.getTitle())) {
							targetItem.setTitle(sourceItem.getTitle());
						}
						// update formula
						if (sourceItem.getTitle().equals(targetItem.getTitle())) {
							if ((targetItem.getIsManual() == null) || !targetItem.getIsManual()) {
								targetItem.setFormula(sourceItem.getFormula());
								targetItem.setColumnName(sourceItem.getColumnName());
								targetItem.setGroupColor(sourceItem.getGroupColor());
								targetItem.setSynchronisableState(sourceItem.getSynchronisableState());
								targetItem.setExecOrder(sourceItem.getExecOrder());
								targetItem.setMultiLevelFormula(sourceItem.getMultiLevelFormula());
							}
							isFound = true;
							break;
						}
					}
					if (!isFound) {
						sourceItem.setNodeRef(null);
						sourceItem.setParentNodeRef(null);
						targetList.add(sourceItem);
					}
				} else {
					sourceItem.setNodeRef(null);
					sourceItem.setParentNodeRef(null);
					sourceItem.setTransient(true);
					targetList.add(sourceItem);
				}
			}
		}
	}
}
