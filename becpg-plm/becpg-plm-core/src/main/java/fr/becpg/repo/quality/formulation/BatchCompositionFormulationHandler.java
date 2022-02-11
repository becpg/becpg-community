package fr.becpg.repo.quality.formulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.alfresco.error.ExceptionStackUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
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
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.formulation.spel.SpelFormulaService;
import fr.becpg.repo.formulation.spel.SpelHelper;
import fr.becpg.repo.helper.JsonFormulaHelper;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.AbstractProductDataView;
import fr.becpg.repo.product.data.CompoListView;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.PackagingListView;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.formulation.FormulaFormulationHandler;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.quality.data.BatchData;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.repository.model.CompositionDataItem;
import fr.becpg.repo.security.BeCPGAccessDeniedException;
import fr.becpg.repo.variant.filters.VariantFilters;

public class BatchCompositionFormulationHandler extends FormulationBaseHandler<BatchData> {

	private static Log logger = LogFactory.getLog(BatchCompositionFormulationHandler.class);

	private AlfrescoRepository<ProductData> alfrescoRepository;

	private NodeService nodeService;

	private NamespaceService namespaceService;

	private SpelFormulaService formulaService;

	public void setFormulaService(SpelFormulaService formulaService) {
		this.formulaService = formulaService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}
	
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	@Override
	public boolean process(BatchData batchData) {

		if (!(batchData.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL))) {

			Double batchQty = batchData.getBatchQty();

			if (batchQty == null) {
				batchQty = 1d;
			}

			if ((batchData.getUnit() != null) && (batchData.getUnit().isVolume() || batchData.getUnit().isWeight())) {
				batchQty = batchQty / batchData.getUnit().getUnitFactor();
			}

			if ((batchData.getProduct() != null) && !batchData.hasCompoListEl()) {

				ProductData productData = batchData.getProduct();

				Double productNetWeight = FormulationHelper.getNetWeight(productData, FormulationHelper.DEFAULT_NET_WEIGHT);

				// 500 product of 5 Kg
				
				Double ratio = 1d;
				if(batchData.getUnit() != null && batchData.getUnit().isP()) {
					ratio = batchQty;
				} else if(batchData.getUnit() != null && batchData.getUnit().isPerc()) {
					ratio = batchQty / 100;
				} else {
					ratio = batchQty / productNetWeight;
				}

				for (CompoListDataItem compoListItem : productData
						.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {

					CompoListDataItem toAdd = new CompoListDataItem(compoListItem);
					toAdd.setParentNodeRef(null);
					toAdd.setNodeRef(null);
					toAdd.setVariants(new ArrayList<>());
					toAdd.setDeclType(DeclarationType.DoNotDetails);
					if (toAdd.getQtySubFormula() != null) {
						toAdd.setQtySubFormula(toAdd.getQtySubFormula() * ratio);
					}
					batchData.getCompoList().add(toAdd);
				}

			}

			Composite<CompoListDataItem> compositeAll = CompositeHelper.getHierarchicalCompoList(batchData.getCompoList());

			// calculate on every item	
			visitQtyChildren(batchQty, compositeAll);

			copyTemplateDynamicCharactLists(batchData);

			StandardEvaluationContext context = formulaService.createEntitySpelContext(batchData);

			computeFormula(batchData, formulaService.getSpelParser(), context, batchData.getCompoListView());

		}

		return true;
	}

	private void visitQtyChildren(Double parentQty, Composite<CompoListDataItem> composite) throws FormulateException {

		for (Composite<CompoListDataItem> component : composite.getChildren()) {

			Double qtyInKg = calculateQtyInKg(component.getData());
			if (logger.isDebugEnabled()) {
				logger.debug("qtySubFormula: " + qtyInKg + " parentQty: " + parentQty);
			}
			if (qtyInKg != null) {

				// take in account percentage
				if (ProductUnit.Perc.equals(component.getData().getCompoListUnit()) && (parentQty != null) && !parentQty.equals(0d)) {
					qtyInKg = (qtyInKg * parentQty) / 100;
				}

				component.getData().setQty(qtyInKg);
			}

			// calculate children
			if (!component.isLeaf()) {

				// take in account percentage
				if (ProductUnit.Perc.equals(component.getData().getCompoListUnit()) && (parentQty != null) && !parentQty.equals(0d)) {

					visitQtyChildren(parentQty, component);

					// no yield but calculate % of composite
					Double compositePerc = 0d;
					boolean isUnitPerc = true;
					for (Composite<CompoListDataItem> child : component.getChildren()) {
						compositePerc += child.getData().getQtySubFormula();
						isUnitPerc = isUnitPerc && ProductUnit.Perc.equals(child.getData().getCompoListUnit());
						if (!isUnitPerc) {
							break;
						}
					}
					if (isUnitPerc) {
						component.getData().setQtySubFormula(compositePerc);
						component.getData().setQty((compositePerc * parentQty) / 100);
					}
				} else {
					visitQtyChildren(component.getData().getQty(), component);
				}
			}
		}
	}

	private Double calculateQtyInKg(CompoListDataItem compoListDataItem) {
		Double qty = compoListDataItem.getQtySubFormula();
		ProductUnit compoListUnit = compoListDataItem.getCompoListUnit();

		ProductData componentProductData = alfrescoRepository.findOne(compoListDataItem.getProduct());

		if ((qty != null) && (compoListUnit != null)) {

			Double unitFactor = compoListUnit.getUnitFactor();

			if (compoListUnit.isWeight()) {
				return qty / unitFactor;
			} else if (compoListUnit.isP()) {

				Double productQty = FormulationHelper.QTY_FOR_PIECE;

				if ((componentProductData.getUnit() != null) && componentProductData.getUnit().isP() && (componentProductData.getQty() != null)) {
					productQty = componentProductData.getQty();
				}

				return (FormulationHelper.getNetWeight(componentProductData, FormulationHelper.DEFAULT_NET_WEIGHT) * qty) / productQty;

			} else if (compoListUnit.isVolume()) {

				qty = qty / unitFactor;

				Double overrun = compoListDataItem.getOverrunPerc();
				if (compoListDataItem.getOverrunPerc() == null) {
					overrun = FormulationHelper.DEFAULT_OVERRUN;
				}

				Double density = componentProductData.getDensity();
				if ((density == null) || density.equals(0d)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Cannot calculate qty since density is null or equals to 0");
					}
				} else {
					return (qty * density * 100) / (100 + overrun);

				}

			}
			return qty;
		}

		return FormulationHelper.DEFAULT_COMPONANT_QUANTITY;
	}

	private void computeFormula(BatchData batchData, ExpressionParser parser, EvaluationContext context, AbstractProductDataView view) {

		if (view.getDynamicCharactList() != null) {

			Set<QName> nullDynColumnNames = new HashSet<>(FormulaFormulationHandler.DYN_COLUMN_SIZE);
			for (int i = 1; i <= FormulaFormulationHandler.DYN_COLUMN_SIZE; i++) {
				nullDynColumnNames.add(QName.createQName(FormulaFormulationHandler.DYN_COLUMN_NAME + i, namespaceService));
			}

			for (DynamicCharactListItem dynamicCharactListItem : view.getDynamicCharactList()) {
				if (!FormulationService.FAST_FORMULATION_CHAINID.equals(batchData.getFormulationChainId())) {
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
									Double qtyPerProduct = dataListItem.getQty();
									dataListItem.setQty(qtyPerProduct);

									StandardEvaluationContext dataContext = formulaService.createDataListItemSpelContext(batchData, dataListItem);
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

										JSONObject jsonTree = extractJSONTree(batchData, dataListItem, value, exp);
										dataListItem.getExtraProperties().put(columnName, jsonTree.toString());
										if (logger.isDebugEnabled()) {
											logger.debug(" -- json tree value:" + value);
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
						
						batchData.getReqCtrlList()
						.add(new ReqCtrlListDataItem(
								null, RequirementType.Forbidden, MLTextHelper.getI18NMessage("message.formulate.formula.error",
										dynamicCharactListItem.getTitle(), e.getLocalizedMessage()),
								null, new ArrayList<>(), RequirementDataType.Formulation));


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

	private JSONObject extractJSONTree(BatchData batchData, CompositionDataItem dataListItem, Object value, Expression exp) throws JSONException {
		JSONObject jsonObject = new JSONObject();

		JSONArray subList = new JSONArray();

		String path = FormulaFormulationHandler.JSON_PATH_SEPARATOR + dataListItem.getNodeRef().getId();

		extractJSONSubList(batchData, dataListItem, exp, path, subList);
		jsonObject.put(JsonFormulaHelper.JSON_SUB_VALUES, subList);
		jsonObject.put(JsonFormulaHelper.JSON_VALUE, value);

		return jsonObject;
	}

	private void extractJSONSubList(BatchData batchData, CompositionDataItem dataListItem, Expression exp, String path, JSONArray subList)
			throws JSONException {
		ProductData subBatchData = alfrescoRepository.findOne(dataListItem.getComponent());
		List<CompositionDataItem> compositeList = new ArrayList<>();
		List<Double> qtyList = new ArrayList<>();
		List<Double> lossPercList = new ArrayList<>();
		for (CompositionDataItem subDataListItem : subBatchData.getCompoListView().getCompoList()) {
			compositeList.add(subDataListItem);
			qtyList.add(subDataListItem.getQty());
			lossPercList.add(subDataListItem.getLossPerc());

			if (FormulationHelper.getNetWeight(subBatchData, FormulationHelper.DEFAULT_NET_WEIGHT) != 0) {
				subDataListItem.setQty((dataListItem.getQty() * subDataListItem.getQty())
						/ FormulationHelper.getNetWeight(subBatchData, FormulationHelper.DEFAULT_NET_WEIGHT));
			}

			subDataListItem.setLossPerc((((1 + ((dataListItem.getLossPerc() != null ? dataListItem.getLossPerc() : 0) / 100))
					* (1 + ((subDataListItem.getLossPerc() != null ? subDataListItem.getLossPerc() : 0) / 100))) - 1) * 100);
		}

		for (int i = 0; i < compositeList.size(); i++) {
			CompositionDataItem composite = compositeList.get(i);
			try {
				JSONObject subObject = new JSONObject();

				StandardEvaluationContext dataContext = formulaService.createDataListItemSpelContext(batchData, composite);

				String subPath = path + FormulaFormulationHandler.JSON_PATH_SEPARATOR + composite.getNodeRef().getId();

				Object subValue = exp.getValue(dataContext);
				subObject.put(JsonFormulaHelper.JSON_VALUE, subValue);
				subObject.put(JsonFormulaHelper.JSON_PATH, subPath);
				subList.put(subObject);

				if (PLMModel.TYPE_SEMIFINISHEDPRODUCT.equals(nodeService.getType(dataListItem.getComponent()))
						|| PLMModel.TYPE_FINISHEDPRODUCT.equals(nodeService.getType(dataListItem.getComponent()))
						|| PLMModel.TYPE_PACKAGINGKIT.equals(nodeService.getType(dataListItem.getComponent()))) {
					extractJSONSubList(batchData, composite, exp, subPath, subList);
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
	private void copyTemplateDynamicCharactLists(BatchData formulatedProduct) {
		if ((formulatedProduct.getEntityTpl() != null) && !formulatedProduct.getEntityTpl().equals(formulatedProduct)) {
			BatchData templateBatchData = formulatedProduct.getEntityTpl();

			copyTemplateDynamicCharactList(templateBatchData.getCompoListView().getDynamicCharactList(),
					formulatedProduct.getCompoListView().getDynamicCharactList());

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
					if (sourceItem.isSynchronisable()) {
						boolean isFound = false;
						for (DynamicCharactListItem targetItem : targetList) {
							// charact renamed
							if (sourceItem.getName().equals(targetItem.getName()) && !sourceItem.getTitle().equals(targetItem.getTitle())) {
								targetItem.setTitle(sourceItem.getTitle());
							}
							// update formula
							if (sourceItem.getName().equals(targetItem.getName())) {

								targetItem.setName(sourceItem.getName());
								targetItem.setTitle(sourceItem.getTitle());
								targetItem.setSort(sourceItem.getSort());
								if (!Boolean.TRUE.equals(targetItem.getIsManual())) {
									targetItem.setFormula(sourceItem.getFormula());
									targetItem.setColumnName(sourceItem.getColumnName());
									targetItem.setGroupColor(sourceItem.getGroupColor());
									targetItem.setColor(sourceItem.getColor());
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

			targetList.sort((o1, o2) -> {
				if ((o1.getSort() == null) && (o2.getSort() == null)) {
					return 0;
				}
				if ((o1.getSort() == null) && (o2.getSort() != null)) {
					return -1;
				}
				if ((o2.getSort() == null) && (o1.getSort() != null)) {
					return 1;
				}
				return o1.getSort().compareTo(o2.getSort());
			});
		}
	}
	
	

}
