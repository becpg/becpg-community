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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.product.formulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;

import org.alfresco.error.ExceptionStackUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.formulation.spel.SpelFormulaService;
import fr.becpg.repo.formulation.spel.SpelHelper;
import fr.becpg.repo.formulation.spel.SpelHelper.SpelShortcut;
import fr.becpg.repo.helper.JsonFormulaHelper;
import fr.becpg.repo.helper.LargeTextHelper;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.AbstractProductDataView;
import fr.becpg.repo.product.data.CompoListView;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.PackagingKitData;
import fr.becpg.repo.product.data.PackagingListView;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.packaging.VariantPackagingData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.DynamicCharactExecOrder;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.repository.model.CompositionDataItem;
import fr.becpg.repo.security.BeCPGAccessDeniedException;

/**
 * Use Spring EL to parse formula and compute value
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class FormulaFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(FormulaFormulationHandler.class);

	/** Constant <code>DYN_COLUMN_SIZE=10</code> */
	public static final int DYN_COLUMN_SIZE = 10;
	/** Constant <code>DYN_COLUMN_NAME="bcpg:dynamicCharactColumn"</code> */
	public static final String DYN_COLUMN_NAME = "bcpg:dynamicCharactColumn";

	/** Constant <code>JSON_PATH_SEPARATOR="/"</code> */
	public static final String JSON_PATH_SEPARATOR = "/";

	static {
		SpelHelper
				.registerShortcut(new SpelShortcut("cost\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]", "costList.^[cost.toString() == '$1']"));
		SpelHelper
		.registerShortcut(new SpelShortcut("lca\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]", "lcaList.^[lca.toString() == '$1']"));
		SpelHelper.registerShortcut(new SpelShortcut("nut\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]", "nutList.^[nut.toString() == '$1']"));
		SpelHelper.registerShortcut(
				new SpelShortcut("allergen\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]", "allergenList.^[allergen.toString() == '$1']"));
		SpelHelper.registerShortcut(new SpelShortcut("ing\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]", "ingList.^[ing.toString() == '$1']"));
		SpelHelper.registerShortcut(
				new SpelShortcut("organo\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]", "organoList.^[organo.toString() == '$1']"));
		SpelHelper.registerShortcut(
				new SpelShortcut("physico\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]", "physicoChemList.^[physicoChem.toString() == '$1']"));
		SpelHelper.registerShortcut(
				new SpelShortcut("microbio\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]", "microbioList.^[microbio.toString() == '$1']"));
		SpelHelper.registerShortcut(new SpelShortcut("compo\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]",
				"compoListView.compoList.^[product.toString() == '$1']"));
		SpelHelper.registerShortcut(new SpelShortcut("process\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]",
				"processListView.processList.^[resource.toString() == '$1']"));
		SpelHelper.registerShortcut(
				new SpelShortcut("resParam\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]", "resourceParamList.^[param.toString() == '$1']"));
		SpelHelper.registerShortcut(new SpelShortcut("pack\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]",
				"packagingListView.packagingList.^[product.toString() == '$1']"));
		SpelHelper.registerShortcut(new SpelShortcut("packaging\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]",
				"packagingListView.packagingList.^[product.toString() == '$1']"));
		SpelHelper.registerShortcut(new SpelShortcut("compoVar\\['(.*?)'\\]", "compoListView.dynamicCharactList.^[title == '$1']?.value"));
		SpelHelper.registerShortcut(new SpelShortcut("packVar\\['(.*?)'\\]", "packagingListView.dynamicCharactList.^[title == '$1']?.value"));
		SpelHelper.registerShortcut(new SpelShortcut("packagingVar\\['(.*?)'\\]", "packagingListView.dynamicCharactList.^[title == '$1']?.value"));
		SpelHelper.registerShortcut(new SpelShortcut("processVar\\['(.*?)'\\]", "processListView.dynamicCharactList.^[title == '$1']?.value"));
		SpelHelper.registerShortcut(
				new SpelShortcut("labelClaim\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]", "labelClaimList.^[labelClaim.toString() == '$1']"));
		SpelHelper.registerShortcut(new SpelShortcut("labeling\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]",
				"labelingListView.ingLabelingList.^[grp.toString() == '$1']"));
	}

	private AlfrescoRepository<ProductData> alfrescoRepository;

	private NodeService nodeService;

	private NamespaceService namespaceService;

	private SpelFormulaService formulaService;

	private DynamicCharactExecOrder execOrder = DynamicCharactExecOrder.Post;

	/**
	 * <p>Setter for the field <code>execOrder</code>.</p>
	 *
	 * @param execOrder a {@link fr.becpg.repo.product.data.productList.DynamicCharactExecOrder} object.
	 */
	public void setExecOrder(DynamicCharactExecOrder execOrder) {
		this.execOrder = execOrder;
	}

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
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
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>formulaService</code>.</p>
	 *
	 * @param formulaService a {@link fr.becpg.repo.formulation.spel.SpelFormulaService} object.
	 */
	public void setFormulaService(SpelFormulaService formulaService) {
		this.formulaService = formulaService;
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData productData) {

		if (!(productData.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) || (productData instanceof ProductSpecificationData))) {

			if (DynamicCharactExecOrder.Pre.equals(execOrder)) {
				copyTemplateDynamicCharactLists(productData);
			}

			StandardEvaluationContext context = formulaService.createEntitySpelContext(productData);

			for (AbstractProductDataView view : productData.getViews()) {
				computeFormula(productData, formulaService.getSpelParser(), context, view);
			}
		}
		return true;
	}

	private void computeFormula(ProductData productData, ExpressionParser parser, EvaluationContext context, AbstractProductDataView view) {

		if (view.getDynamicCharactList() != null) {

			Set<QName> nullDynColumnNames = new HashSet<>(DYN_COLUMN_SIZE);
			for (int i = 1; i <= DYN_COLUMN_SIZE; i++) {
				nullDynColumnNames.add(QName.createQName(DYN_COLUMN_NAME + i, namespaceService));
			}

			for (DynamicCharactListItem dynamicCharactListItem : view.getDynamicCharactList()) {
				if (execOrder.equals(dynamicCharactListItem.getExecOrder())
						|| (DynamicCharactExecOrder.Defer.equals(dynamicCharactListItem.getExecOrder())
								&& !DynamicCharactExecOrder.Pre.equals(execOrder)
								&& !FormulationService.FAST_FORMULATION_CHAINID.equals(productData.getFormulationChainId()))) {
					try {
						if ((dynamicCharactListItem.getFormula() != null) && !dynamicCharactListItem.getFormula().isEmpty()) {
							if ((dynamicCharactListItem.getColumnName() != null) && !dynamicCharactListItem.getColumnName().isEmpty()) {

								QName columnName = QName.createQName(dynamicCharactListItem.getColumnName().replaceFirst("_", ":"), namespaceService);

								String formula = SpelHelper.formatFormula(dynamicCharactListItem.getFormula());
								logger.debug("Column formula : " + formula + " (" + dynamicCharactListItem.getTitle() + ")");
								Expression exp = parser.parseExpression(formula);

								if (nullDynColumnNames.contains(columnName)) {
									nullDynColumnNames.remove(columnName);
								}
								for (CompositionDataItem dataListItem : view.getMainDataList()) {

									Double origQty = dataListItem.getQty();
									Double qtyPerProduct = getQtyPerProduct(productData, dataListItem);
									dataListItem.setQty(qtyPerProduct);

									StandardEvaluationContext dataContext = formulaService.createDataListItemSpelContext(productData, dataListItem);
									Object value = null;
									try {
										value = exp.getValue(dataContext);
									} finally {
										if ((qtyPerProduct != null) && qtyPerProduct.equals(dataListItem.getQty())) {
											dataListItem.setQty(origQty);
										}
									}

									if (!L2CacheSupport.isCacheOnlyEnable()
											&& ((dynamicCharactListItem.getMultiLevelFormula() != null)
													&& Boolean.TRUE.equals(dynamicCharactListItem.getMultiLevelFormula()))
											&& ((view instanceof CompoListView) || (view instanceof PackagingListView))
											&& ((dataListItem.getComponent() != null) && (PLMModel.TYPE_SEMIFINISHEDPRODUCT
													.equals(nodeService.getType(dataListItem.getComponent()))
													|| PLMModel.TYPE_FINISHEDPRODUCT.equals(nodeService.getType(dataListItem.getComponent()))
													|| PLMModel.TYPE_PACKAGINGKIT.equals(nodeService.getType(dataListItem.getComponent()))))) {

										JSONObject jsonTree = extractJSONTree(productData, dataListItem, value, exp);
										String jsonValue = jsonTree.toString();

										if ((jsonValue.length() > LargeTextHelper.TEXT_SIZE_LIMIT)) {
											dataListItem.getExtraProperties().put(columnName, (Serializable) value);

											productData.getReqCtrlList()
													.add(ReqCtrlListDataItem.info().withMessage(MLTextHelper
															.getI18NMessage("message.formulate.formula.toolong", dynamicCharactListItem.getTitle()))
															.ofDataType(RequirementDataType.Formulation));


										} else {
											dataListItem.getExtraProperties().put(columnName, jsonTree.toString());
											if (logger.isDebugEnabled()) {
												logger.debug(" -- json tree value:" + value);
											}
										}
									} else {
										dataListItem.getExtraProperties().put(columnName, (Serializable) value);
										logger.debug(" - value :" + value);
									}
								}

								dynamicCharactListItem.setValue(null);

							} else {

								String[] formulas = SpelHelper.formatMTFormulas(dynamicCharactListItem.getFormula());
								for (String formula : formulas) {

									Matcher varFormulaMatcher = SpelHelper.formulaVarPattern.matcher(formula);
									if (varFormulaMatcher.matches()) {
										logger.debug("Variable formula : [" + dynamicCharactListItem.getTitle() + "] - " + varFormulaMatcher.group(2)
												+ " (" + varFormulaMatcher.group(1) + ")");
										Expression exp = parser.parseExpression(varFormulaMatcher.group(2));
										context.setVariable(varFormulaMatcher.group(1), exp.getValue(context));
									} else {
										logger.debug("Formula :  [" + dynamicCharactListItem.getTitle() + "] - " + formula);
										Expression exp = parser.parseExpression(formula);
										dynamicCharactListItem.setValue(exp.getValue(context));
									}
								}
								logger.debug(" - value :" + dynamicCharactListItem.getValue());
							}
						}

						if ((dynamicCharactListItem.getColumnName() != null) && !dynamicCharactListItem.getColumnName().isEmpty()
								&& Boolean.TRUE.equals(dynamicCharactListItem.getIsManual())) {

							QName columnName = QName.createQName(dynamicCharactListItem.getColumnName().replaceFirst("_", ":"), namespaceService);
							if (nullDynColumnNames.contains(columnName)) {
								nullDynColumnNames.remove(columnName);
							}

						}

						dynamicCharactListItem.setErrorLog(null);
					} catch (Exception e) {
						Throwable validCause = ExceptionStackUtil.getCause(e, RetryingTransactionHelper.RETRY_EXCEPTIONS);
						if (validCause != null) {
							throw (RuntimeException) validCause;
						}
						if ((e.getCause() != null) && (e.getCause().getCause() instanceof BeCPGAccessDeniedException)) {
							dynamicCharactListItem.setValue("#AccessDenied");
						} else {
							dynamicCharactListItem.setValue("#Error");
						}
						dynamicCharactListItem.setErrorLog(e.getLocalizedMessage());

						productData.getReqCtrlList()
								.add(ReqCtrlListDataItem.info()
										.withMessage(MLTextHelper.getI18NMessage("message.formulate.formula.error", dynamicCharactListItem.getTitle(),
												e.getLocalizedMessage()))
										.withSources(Arrays.asList(productData.getNodeRef())).ofDataType(RequirementDataType.Formulation));


						if (logger.isDebugEnabled()) {
							logger.warn("Error in formula : [" + dynamicCharactListItem.getTitle() + "] - " + dynamicCharactListItem.getFormula());
							logger.trace(e, e);

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

	private Double getQtyPerProduct(ProductData productData, CompositionDataItem dataListItem) {
		if (dataListItem instanceof PackagingListDataItem) {
			VariantPackagingData variantPackagingData = productData.getDefaultVariantPackagingData();

			if (ProductUnit.PP.equals(((PackagingListDataItem) dataListItem).getPackagingListUnit()) && (dataListItem.getQty() != null)
					&& dataListItem.getQty() != 0d) {
				return 1 / dataListItem.getQty();
			}

			if ((variantPackagingData != null) && (dataListItem.getQty() != null)) {
				if (PackagingLevel.Secondary.equals(((PackagingListDataItem) dataListItem).getPkgLevel())
						&& (variantPackagingData.getProductPerBoxes() != null)) {
					return dataListItem.getQty() / variantPackagingData.getProductPerBoxes();
				}

				if (PackagingLevel.Tertiary.equals(((PackagingListDataItem) dataListItem).getPkgLevel())
						&& (variantPackagingData.getProductPerPallet() != null)) {
					return dataListItem.getQty() / variantPackagingData.getProductPerPallet();
				}
			}
		}
		return dataListItem.getQty();
	}

	private JSONObject extractJSONTree(ProductData productData, CompositionDataItem dataListItem, Object value, Expression exp) throws JSONException {
		JSONObject jsonObject = new JSONObject();

		JSONArray subList = new JSONArray();

		String path = JSON_PATH_SEPARATOR + dataListItem.getNodeRef().getId();

		if (dataListItem instanceof CompoListDataItem && ((CompoListDataItem) dataListItem).getParent() != null) {
			path = JSON_PATH_SEPARATOR + ((CompoListDataItem) dataListItem).getParent().getNodeRef().getId() + path;
		}

		extractJSONSubList(productData, dataListItem, exp, path, subList, new HashSet<>());
		jsonObject.put(JsonFormulaHelper.JSON_SUB_VALUES, subList);
		jsonObject.put(JsonFormulaHelper.JSON_VALUE, value);

		return jsonObject;
	}

	private void extractJSONSubList(ProductData productData, CompositionDataItem dataListItem, Expression exp, String path, JSONArray subList,
			Set<NodeRef> visited) throws JSONException {
		ProductData subProductData = alfrescoRepository.findOne(dataListItem.getComponent());
		List<CompositionDataItem> compositeList = new ArrayList<>();
		List<Double> qtyList = new ArrayList<>();
		List<Double> lossPercList = new ArrayList<>();
		if ((subProductData instanceof FinishedProductData) || (subProductData instanceof SemiFinishedProductData)) {
			for (CompositionDataItem subDataListItem : subProductData.getCompoListView().getCompoList()) {
				compositeList.add(subDataListItem);
				qtyList.add(subDataListItem.getQty());
				lossPercList.add(subDataListItem.getLossPerc());

				if (FormulationHelper.getNetWeight(subProductData, FormulationHelper.DEFAULT_NET_WEIGHT) != 0) {
					subDataListItem.setQty((dataListItem.getQty() * subDataListItem.getQty())
							/ FormulationHelper.getNetWeight(subProductData, FormulationHelper.DEFAULT_NET_WEIGHT));
				}

				subDataListItem.setLossPerc((((1 + ((dataListItem.getLossPerc() != null ? dataListItem.getLossPerc() : 0) / 100))
						* (1 + ((subDataListItem.getLossPerc() != null ? subDataListItem.getLossPerc() : 0) / 100))) - 1) * 100);
			}
		} else if (subProductData instanceof PackagingKitData) {

			VariantPackagingData variantPackagingData = productData.getDefaultVariantPackagingData();

			for (PackagingListDataItem subDataListItem : subProductData.getPackagingListView().getPackagingList()) {

				compositeList.add(subDataListItem);
				qtyList.add(subDataListItem.getQty());
				lossPercList.add(subDataListItem.getLossPerc());

				if (!PLMModel.TYPE_PACKAGINGKIT.equals(nodeService.getType(subDataListItem.getComponent()))) {
					if ((variantPackagingData != null) && (subDataListItem.getQty() != null)) {
						if (PackagingLevel.Secondary.equals(subDataListItem.getPkgLevel()) && (variantPackagingData.getProductPerBoxes() != null)) {
							subDataListItem.setQty(subDataListItem.getQty() / variantPackagingData.getProductPerBoxes());
						}

						if (PackagingLevel.Tertiary.equals(subDataListItem.getPkgLevel()) && (variantPackagingData.getProductPerPallet() != null)) {
							subDataListItem.setQty(subDataListItem.getQty() / variantPackagingData.getProductPerPallet());
						}
					}
				}

				if ((dataListItem instanceof PackagingListDataItem) && (((PackagingListDataItem) dataListItem).getPackagingListUnit() != null)
						&& !ProductUnit.PP.equals(((PackagingListDataItem) dataListItem).getPackagingListUnit()) && (dataListItem.getQty() != null)
						&& (subDataListItem.getQty() != null)) {

					subDataListItem.setQty(dataListItem.getQty() * subDataListItem.getQty());
				}

				subDataListItem.setLossPerc((((1 + ((dataListItem.getLossPerc() != null ? dataListItem.getLossPerc() : 0) / 100))
						* (1 + ((subDataListItem.getLossPerc() != null ? subDataListItem.getLossPerc() : 0) / 100))) - 1) * 100);
			}
		}
		for (int i = 0; i < compositeList.size(); i++) {
			CompositionDataItem composite = compositeList.get(i);
			try {
				JSONObject subObject = new JSONObject();

				StandardEvaluationContext dataContext = formulaService.createDataListItemSpelContext(productData, composite);

				String subPath = path + JSON_PATH_SEPARATOR;

				if (composite instanceof CompoListDataItem) {
					if ((((CompoListDataItem) composite).getParent() != null) && (((CompoListDataItem) composite).getParent().getNodeRef() != null)) {
						subPath += ((CompoListDataItem) composite).getParent().getNodeRef().getId() + JSON_PATH_SEPARATOR;
					}
				}

				subPath += composite.getNodeRef().getId();

				Object subValue = exp.getValue(dataContext);
				subObject.put(JsonFormulaHelper.JSON_VALUE, subValue);
				subObject.put(JsonFormulaHelper.JSON_PATH, subPath);
				subList.put(subObject);
				if (!visited.contains(composite.getComponent())) {
					if (PLMModel.TYPE_SEMIFINISHEDPRODUCT.equals(nodeService.getType(dataListItem.getComponent()))
							|| PLMModel.TYPE_FINISHEDPRODUCT.equals(nodeService.getType(dataListItem.getComponent()))
							|| PLMModel.TYPE_PACKAGINGKIT.equals(nodeService.getType(dataListItem.getComponent()))) {

						visited.add(composite.getComponent());
						extractJSONSubList(productData, composite, exp, subPath, subList, visited);
						visited.remove(composite.getComponent());
					}
				}
			} finally {
				// Reset
				composite.setQty(qtyList.get(i));
				composite.setLossPerc(lossPercList.get(i));
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

		if (formulatedProduct.getProductSpecifications() != null) {
			for (ProductSpecificationData specData : formulatedProduct.getProductSpecifications()) {
				copyTemplateDynamicCharactList(specData.getDynamicCharactList(), formulatedProduct.getCompoListView().getDynamicCharactList());
			}
		}

	}

	/**
	 * <p>copyTemplateDynamicCharactList.</p>
	 *
	 * @param sourceList a {@link java.util.List} object.
	 * @param targetList a {@link java.util.List} object.
	 */
	protected void copyTemplateDynamicCharactList(List<DynamicCharactListItem> sourceList, List<DynamicCharactListItem> targetList) {

		if ((sourceList != null) && (targetList != null)) {
			for (DynamicCharactListItem sourceItem : sourceList) {
				if (sourceItem.getTitle() != null) {

					Optional<DynamicCharactListItem> existingItem = targetList.stream().filter(item -> sourceItem.getTitle().equals(item.getTitle()) 
							&&( sourceItem.isColumn().equals(item.isColumn())) )
							.findFirst();

					if (!existingItem.isPresent()) {
						existingItem = targetList.stream().filter(item -> sourceItem.getName().equals(item.getName())).findFirst();
					}

					if (existingItem.isPresent() && sourceItem.isSynchronisable()) {
						updateItem(existingItem.get(), sourceItem);
					} else {
						sourceItem.setNodeRef(null);
						sourceItem.setParentNodeRef(null);
						sourceItem.setTransient(!sourceItem.isSynchronisable());
						targetList.add(sourceItem);
					}
				}
			}

			targetList.sort(Comparator.comparing(DynamicCharactListItem::getSort, Comparator.nullsFirst(Comparator.naturalOrder())));
		}

	}

	private void updateItem(DynamicCharactListItem targetItem, DynamicCharactListItem sourceItem) {

		targetItem.setTitle(sourceItem.getTitle());
		targetItem.setMlTitle(sourceItem.getMlTitle());
		targetItem.setSort(sourceItem.getSort());

		if (!Boolean.TRUE.equals(targetItem.getIsManual())) {
			targetItem.setName(sourceItem.getName());
			targetItem.setFormula(sourceItem.getFormula());
			targetItem.setColumnName(sourceItem.getColumnName());
			targetItem.setGroupColor(sourceItem.getGroupColor());
			targetItem.setColor(sourceItem.getColor());
			targetItem.setSynchronisableState(sourceItem.getSynchronisableState());
			targetItem.setExecOrder(sourceItem.getExecOrder());
			targetItem.setMultiLevelFormula(sourceItem.getMultiLevelFormula());
		}
	}
}
