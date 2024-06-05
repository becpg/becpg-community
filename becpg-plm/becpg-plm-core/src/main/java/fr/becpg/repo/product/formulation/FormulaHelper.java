package fr.becpg.repo.product.formulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.formulation.spel.SpelFormulaService;
import fr.becpg.repo.formulation.spel.SpelHelper;
import fr.becpg.repo.helper.JsonFormulaHelper;
import fr.becpg.repo.helper.LargeTextHelper;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.AbstractProductDataView;
import fr.becpg.repo.product.data.AbstractScorableEntity;
import fr.becpg.repo.product.data.CompoListView;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.PackagingKitData;
import fr.becpg.repo.product.data.PackagingListView;
import fr.becpg.repo.product.data.ProductData;
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
import fr.becpg.repo.repository.model.CompositionDataItem;
import fr.becpg.repo.security.BeCPGAccessDeniedException;

/**
 * <p>FormulaHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Component
public class FormulaHelper {
	
	private static final Log logger = LogFactory.getLog(FormulaHelper.class);
	
	private static FormulaHelper instance;
	
	/**
	 * <p>Constructor for FormulaHelper.</p>
	 */
	public FormulaHelper() {
		instance = this;
	}
	
	@Autowired
	private AlfrescoRepository<ProductData> alfrescoRepository;
	
	@Autowired
	private NamespaceService namespaceService;
	
	@Autowired
	private SpelFormulaService formulaService;
	
	@Autowired
	private NodeService nodeService;
	
	/** Constant <code>DYN_COLUMN_SIZE=10</code> */
	public static final int DYN_COLUMN_SIZE = 10;
	/** Constant <code>DYN_COLUMN_NAME="bcpg:dynamicCharactColumn"</code> */
	public static final String DYN_COLUMN_NAME = "bcpg:dynamicCharactColumn";

	/** Constant <code>JSON_PATH_SEPARATOR="/"</code> */
	public static final String JSON_PATH_SEPARATOR = "/";

	/**
	 * <p>copyTemplateDynamicCharactList.</p>
	 *
	 * @param sourceList a {@link java.util.List} object
	 * @param targetList a {@link java.util.List} object
	 */
	public static void copyTemplateDynamicCharactList(List<DynamicCharactListItem> sourceList, List<DynamicCharactListItem> targetList) {

		if ((sourceList != null) && (targetList != null)) {
			for (DynamicCharactListItem sourceItem : sourceList) {
				if (sourceItem.getTitle() != null) {
					if (sourceItem.isSynchronisable()) {
						boolean isFound = false;
						for (DynamicCharactListItem targetItem : targetList) {
							// charact renamed
							if (sourceItem.getName().equals(targetItem.getName()) && sourceItem.getTitle()!=null && !sourceItem.getTitle().equals(targetItem.getTitle())) {
								targetItem.setTitle(sourceItem.getTitle());
							}
							if (sourceItem.getName().equals(targetItem.getName()) && sourceItem.getMlTitle()!=null && !sourceItem.getMlTitle().equals(targetItem.getMlTitle())) {
								targetItem.setMlTitle(sourceItem.getMlTitle());
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
	
	/**
	 * <p>computeFormula.</p>
	 *
	 * @param entity a {@link fr.becpg.repo.product.data.AbstractScorableEntity} object
	 * @param context a {@link org.springframework.expression.EvaluationContext} object
	 * @param view a {@link fr.becpg.repo.product.data.AbstractProductDataView} object
	 * @param execOrder a {@link fr.becpg.repo.product.data.productList.DynamicCharactExecOrder} object
	 */
	public static void computeFormula(AbstractScorableEntity entity, EvaluationContext context, AbstractProductDataView view, DynamicCharactExecOrder execOrder) {

		if (view.getDynamicCharactList() != null) {

			Set<QName> nullDynColumnNames = new HashSet<>(DYN_COLUMN_SIZE);
			for (int i = 1; i <= DYN_COLUMN_SIZE; i++) {
				nullDynColumnNames.add(QName.createQName(DYN_COLUMN_NAME + i, instance.namespaceService));
			}

			for (DynamicCharactListItem dynamicCharactListItem : view.getDynamicCharactList()) {
				if (execOrder != null && execOrder.equals(dynamicCharactListItem.getExecOrder())
						|| (execOrder == null || (DynamicCharactExecOrder.Defer.equals(dynamicCharactListItem.getExecOrder())
								&& !DynamicCharactExecOrder.Pre.equals(execOrder))
								&& !FormulationService.FAST_FORMULATION_CHAINID.equals(entity.getFormulationChainId()))) {
					try {
						if ((dynamicCharactListItem.getFormula() != null) && !dynamicCharactListItem.getFormula().isEmpty()) {
							if ((dynamicCharactListItem.getColumnName() != null) && !dynamicCharactListItem.getColumnName().isEmpty()) {

								QName columnName = QName.createQName(dynamicCharactListItem.getColumnName().replaceFirst("_", ":"), instance.namespaceService);

								String formula = SpelHelper.formatFormula(dynamicCharactListItem.getFormula());
								logger.debug("Column formula : " + formula + " (" + dynamicCharactListItem.getTitle() + ")");
								Expression exp = instance.formulaService.getSpelParser().parseExpression(formula);

								if (nullDynColumnNames.contains(columnName)) {
									nullDynColumnNames.remove(columnName);
								}
								for (CompositionDataItem dataListItem : view.getMainDataList()) {

									Double origQty = dataListItem.getQty();
									Double qtyPerProduct = getQtyPerProduct(entity, dataListItem);
									dataListItem.setQty(qtyPerProduct);

									StandardEvaluationContext dataContext = instance.formulaService.createDataListItemSpelContext(entity, dataListItem);
									Object value = null;
									try {
										value = exp.getValue(dataContext);
									} finally {
										if ((qtyPerProduct != null) && qtyPerProduct.equals(dataListItem.getQty())) {
											dataListItem.setQty(origQty);
										}
									}

									if (((dynamicCharactListItem.getMultiLevelFormula() != null)
													&& Boolean.TRUE.equals(dynamicCharactListItem.getMultiLevelFormula()))
											&& ((view instanceof CompoListView) || (view instanceof PackagingListView))
											&& ((dataListItem.getComponent() != null) && (PLMModel.TYPE_SEMIFINISHEDPRODUCT
													.equals(instance.nodeService.getType(dataListItem.getComponent()))
													|| PLMModel.TYPE_FINISHEDPRODUCT.equals(instance.nodeService.getType(dataListItem.getComponent()))
													|| PLMModel.TYPE_PACKAGINGKIT.equals(instance.nodeService.getType(dataListItem.getComponent()))))) {

										JSONObject jsonTree = extractJSONTree(entity, dataListItem, value, exp);
										String jsonValue = jsonTree.toString();
										
										if ((jsonValue.length() > LargeTextHelper.TEXT_SIZE_LIMIT)) {
											dataListItem.getExtraProperties().put(columnName, (Serializable) value);
											
											
											entity.getReqCtrlList()
													.add(ReqCtrlListDataItem.info()
															.withMessage(MLTextHelper.getI18NMessage(
																	"message.formulate.formula.toolong",
																	dynamicCharactListItem.getTitle()))
															.ofDataType(RequirementDataType.Formulation));											
											
										} else {
											dataListItem.getExtraProperties().put(columnName, jsonValue);
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
										Expression exp = instance.formulaService.getSpelParser().parseExpression(varFormulaMatcher.group(2));
										context.setVariable(varFormulaMatcher.group(1), exp.getValue(context));
									} else {
										logger.debug("Formula :  [" + dynamicCharactListItem.getTitle() + "] - " + formula);
										Expression exp = instance.formulaService.getSpelParser().parseExpression(formula);
										dynamicCharactListItem.setValue(exp.getValue(context));
									}
								}
								logger.debug(" - value :" + dynamicCharactListItem.getValue());
							}
						}

						if ((dynamicCharactListItem.getColumnName() != null) && !dynamicCharactListItem.getColumnName().isEmpty()
								&& Boolean.TRUE.equals(dynamicCharactListItem.getIsManual())) {

							QName columnName = QName.createQName(dynamicCharactListItem.getColumnName().replaceFirst("_", ":"), instance.namespaceService);
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

						
						entity.getReqCtrlList()
						.add(ReqCtrlListDataItem.forbidden()
								.withMessage(MLTextHelper.getI18NMessage(
										"message.formulate.formula.error",
										dynamicCharactListItem.getTitle(), e.getLocalizedMessage()))
								.ofDataType(RequirementDataType.Formulation).withSources(new ArrayList<>(Arrays.asList(entity.getNodeRef()))));

						if (logger.isDebugEnabled()) {
							logger.warn("Error in formula : [" + dynamicCharactListItem.getTitle() + "] - " + dynamicCharactListItem.getFormula());
							logger.trace(e, e);

						}
					}
				} else {

					if ((dynamicCharactListItem.getColumnName() != null) && !dynamicCharactListItem.getColumnName().isEmpty()) {
						QName columnName = QName.createQName(dynamicCharactListItem.getColumnName().replaceFirst("_", ":"), instance.namespaceService);
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
	
	private static JSONObject extractJSONTree(AbstractScorableEntity entity, CompositionDataItem dataListItem, Object value, Expression exp) throws JSONException {
		JSONObject jsonObject = new JSONObject();

		JSONArray subList = new JSONArray();

		String path = JSON_PATH_SEPARATOR + dataListItem.getNodeRef().getId();

		extractJSONSubList(entity, dataListItem, exp, path, subList, new HashSet<>());
		jsonObject.put(JsonFormulaHelper.JSON_SUB_VALUES, subList);
		jsonObject.put(JsonFormulaHelper.JSON_VALUE, value);

		return jsonObject;
	}
	
	private static void extractJSONSubList(AbstractScorableEntity entity, CompositionDataItem dataListItem, Expression exp, String path, JSONArray subList,
			Set<NodeRef> visited) throws JSONException {
		ProductData subProductData = instance.alfrescoRepository.findOne(dataListItem.getComponent());
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
		} else if (subProductData instanceof PackagingKitData && entity instanceof ProductData) {

			VariantPackagingData variantPackagingData = ((ProductData) entity).getDefaultVariantPackagingData();

			for (PackagingListDataItem subDataListItem : subProductData.getPackagingListView().getPackagingList()) {

				compositeList.add(subDataListItem);
				qtyList.add(subDataListItem.getQty());
				lossPercList.add(subDataListItem.getLossPerc());

				if (!PLMModel.TYPE_PACKAGINGKIT.equals(instance.nodeService.getType(subDataListItem.getComponent()))) {
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

				StandardEvaluationContext dataContext = instance.formulaService.createDataListItemSpelContext(entity, composite);

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
					if (PLMModel.TYPE_SEMIFINISHEDPRODUCT.equals(instance.nodeService.getType(dataListItem.getComponent()))
							|| PLMModel.TYPE_FINISHEDPRODUCT.equals(instance.nodeService.getType(dataListItem.getComponent()))
							|| PLMModel.TYPE_PACKAGINGKIT.equals(instance.nodeService.getType(dataListItem.getComponent()))) {

						visited.add(composite.getComponent());
						extractJSONSubList(entity, composite, exp, subPath, subList, visited);
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
	
	private static Double getQtyPerProduct(AbstractScorableEntity entity, CompositionDataItem dataListItem) {
		if (dataListItem instanceof PackagingListDataItem && entity instanceof ProductData) {
			VariantPackagingData variantPackagingData = ((ProductData) entity).getDefaultVariantPackagingData();

			if (ProductUnit.PP.equals(((PackagingListDataItem)dataListItem).getPackagingListUnit()) && (dataListItem.getQty() != null)
					 && dataListItem.getQty()!=0d) {
				return 1/ dataListItem.getQty();
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
	
}
