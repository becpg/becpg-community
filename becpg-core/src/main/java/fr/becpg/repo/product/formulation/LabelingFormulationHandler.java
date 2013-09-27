/*
 * 
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ing.CompositeLabeling;
import fr.becpg.repo.product.data.ing.DeclarationFilter;
import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleType;
import fr.becpg.repo.product.data.spel.DeclarationFilterContext;
import fr.becpg.repo.product.data.spel.LabelingFormulaContext;
import fr.becpg.repo.product.data.spel.SpelHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.filters.EffectiveFilters;
import fr.becpg.repo.variant.filters.VariantFilters;

/**
 * @author matthieu
 */
@Service
public class LabelingFormulationHandler extends FormulationBaseHandler<ProductData> {

	/** The logger. */
	private static Log logger = LogFactory.getLog(LabelingFormulationHandler.class);

	private NodeService nodeService;

	protected AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	private NodeService mlNodeService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setMlNodeService(NodeService mlNodeService) {
		this.mlNodeService = mlNodeService;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		// no compo => no formulation
		if (!formulatedProduct.hasCompoListEl(EffectiveFilters.EFFECTIVE, VariantFilters.DEFAULT_VARIANT)) {
			logger.debug("no compo => no formulation");
			return true;
		}

		logger.debug("Calculate Ingredient Labeling");

		LabelingFormulaContext labelingFormulaContext = new LabelingFormulaContext(mlNodeService);

		//TODO add model only rules
		List<LabelingRuleListDataItem> labelingRuleLists = getLabelingRules(formulatedProduct);;
		
		
		// Apply before formula
		for (LabelingRuleListDataItem labelingRuleListDataItem : labelingRuleLists) {
			LabelingRuleType type = labelingRuleListDataItem.getLabelingRuleType();
			if (LabelingRuleType.Format.equals(type)) {
				labelingFormulaContext.formatText(labelingRuleListDataItem.getComponents(), labelingRuleListDataItem.getFormula());
			} else if (LabelingRuleType.Rename.equals(type)) {
				labelingFormulaContext.rename(labelingRuleListDataItem.getComponents(), labelingRuleListDataItem.getReplacements(), labelingRuleListDataItem.getFormula());
			} else if (LabelingRuleType.Locale.equals(type)) {
				labelingFormulaContext.addLocale(labelingRuleListDataItem.getFormula());
			} else if (LabelingRuleType.Detail.equals(type) || LabelingRuleType.Declare.equals(type) || LabelingRuleType.DoNotDeclare.equals(type)
					|| LabelingRuleType.Omit.equals(type) || LabelingRuleType.Group.equals(type)) {
				labelingFormulaContext.declare(labelingRuleListDataItem.getComponents(), labelingRuleListDataItem.getFormula(), DeclarationType.valueOf(type.toString()));
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("LabelingFormulaContext initialized : " + labelingFormulaContext.toString());
		}

		// Compute composite
		Composite<CompoListDataItem> compositeDefaultVariant = CompositeHelper.getHierarchicalCompoList(formulatedProduct.getCompoList(EffectiveFilters.ALL,
				VariantFilters.DEFAULT_VARIANT));

		List<CompositeLabeling> compositeLabelings = calculateILL(compositeDefaultVariant, labelingFormulaContext);

		// Store results
		labelingFormulaContext.setCompositeIngs(compositeLabelings);

		// Apply after formula
		for (LabelingRuleListDataItem labelingRuleListDataItem : labelingRuleLists) {
			LabelingRuleType type = labelingRuleListDataItem.getLabelingRuleType();
			if (LabelingRuleType.Aggregate.equals(type)) {
				// TODO
				// labelingFormulaContext.aggregate(labelingRuleListDataItem.getComponents(),
				// labelingRuleListDataItem.getFormula());
			} else if (LabelingRuleType.Combine.equals(type)) {
				// TODO
			}
		}

		List<IngLabelingListDataItem> retainNodes = new ArrayList<IngLabelingListDataItem>();

		ExpressionParser parser = new SpelExpressionParser();
		StandardEvaluationContext dataContext = new StandardEvaluationContext(labelingFormulaContext);

		for (LabelingRuleListDataItem labelingRuleListDataItem : labelingRuleLists) {
			if (LabelingRuleType.Render.equals(labelingRuleListDataItem.getLabelingRuleType())) {
				MLText label = new MLText();
				for (Locale locale : labelingFormulaContext.getLocales()) {
					Locale currentLocal = I18NUtil.getLocale();

					try {
						I18NUtil.setLocale(locale);

						Expression exp = parser.parseExpression(SpelHelper.formatFormula(labelingRuleListDataItem.getFormula()));
						String ret = exp.getValue(dataContext, String.class);
						if (logger.isDebugEnabled()) {
							logger.debug("Running renderFormula :" + labelingRuleListDataItem.getFormula()+" for locale :"+locale.toString());
							logger.debug(" - render value :" + ret);
						}
						label.addValue(locale, ret );
					} catch (Exception e) {
						labelingRuleListDataItem.setErrorLog(e.getLocalizedMessage());
						if (logger.isDebugEnabled()) {
							logger.info("Error in formula :" + SpelHelper.formatFormula(labelingRuleListDataItem.getFormula()), e);
						}
					} finally {
						I18NUtil.setLocale(currentLocal);
					}
				}
				retainNodes.add(getOrCreateILLDataItem(formulatedProduct, labelingRuleListDataItem.getNodeRef(), label));
			}
		}
		formulatedProduct.getLabelingListView().getIngLabelingList().retainAll(retainNodes);

		return true;
	}

	private List<LabelingRuleListDataItem> getLabelingRules(ProductData formulatedProduct) {
//      TODO 		
//		List<LabelingRuleListDataItem> ret = new LinkedList<>();
//		
//		if(formulatedProduct.getEntityTplRef() !=null){
//			List<RepositoryEntity> modelRules = alfrescoRepository.loadDataList(formulatedProduct.getEntityTplRef(), BeCPGModel.TYPE_LABELING_RULE_LIST,  BeCPGModel.TYPE_LABELING_RULE_LIST);
//			for (RepositoryEntity modelRule : modelRules) {
//				if(modelRule instanceof LabelingRuleListDataItem 
//						&& ((LabelingRuleListDataItem) modelRule).getIsManual()){
//					ret.add((LabelingRuleListDataItem)modelRule);
//				}
//			}	
//		}
//		ret.addAll(formulatedProduct.getLabelingListView().getLabelingRuleList());
//		return ret;
		
		return formulatedProduct.getLabelingListView().getLabelingRuleList();
	}

	private IngLabelingListDataItem getOrCreateILLDataItem(ProductData formulatedProduct, NodeRef key, MLText label) {

		IngLabelingListDataItem ill = null;
		for (IngLabelingListDataItem tmp : formulatedProduct.getLabelingListView().getIngLabelingList()) {
			if ((tmp.getGrp() == null && key == null) || (tmp.getGrp() != null && tmp.getGrp().equals(key))) {
				ill = tmp;
				break;
			}

		}

		if (ill == null) {
			ill = new IngLabelingListDataItem(null, key, label, Boolean.FALSE);
			formulatedProduct.getLabelingListView().getIngLabelingList().add(ill);
		} else if (!Boolean.TRUE.equals(ill.getIsManual())) {
			ill.setValue(label);
		}
		return ill;

	}

	//
	// private void applyFormulas(String[] labelingFormulas, ExpressionParser
	// parser, StandardEvaluationContext dataContext) {
	// for (String labelingFormula : labelingFormulas) {
	// Expression exp = parser.parseExpression(labelingFormula);
	// if (logger.isDebugEnabled()) {
	// logger.debug("Running labelingFormula :" + labelingFormula);
	// }
	// if (!exp.getValue(dataContext, Boolean.class)) {
	// logger.error("Error running :" + labelingFormula);
	// }
	// }
	//
	// }

	private List<CompositeLabeling> calculateILL(Composite<CompoListDataItem> composite, LabelingFormulaContext labelingFormulaContext) throws FormulateException {
		List<CompositeLabeling> ret = new ArrayList<>();

		CompositeLabeling defaultCompositeIng = new CompositeLabeling();

		for (Composite<CompoListDataItem> component : composite.getChildren()) {

			DeclarationType declarationType = getDeclarationType(component.getData(), null, labelingFormulaContext);
			
			if(logger.isDebugEnabled()){
				logger.debug("Declaration type :"+declarationType.toString()+" for :"+component.getData().getName());
			}

			if (component.isLeaf() || declarationType == DeclarationType.DoNotDeclare || declarationType == DeclarationType.Declare) {
				if(component.isLeaf()){
					defaultCompositeIng = calculateILLOfCompositeIng(defaultCompositeIng, component, labelingFormulaContext);
				} else {
					defaultCompositeIng = calculateILLOfCompositeIng(component, labelingFormulaContext);
				}

			} else {
				if (declarationType == DeclarationType.Detail) {
					defaultCompositeIng.add(calculateILLOfCompositeIng(component, labelingFormulaContext), true);

					// Calculate qtyRMUsed
					addQtyRMUsed(component.getData(), defaultCompositeIng);

				} else if (declarationType == DeclarationType.Group) {
					CompositeLabeling groupLabeling  = calculateILLOfCompositeIng(component, labelingFormulaContext);
					groupLabeling.setGroup(true);
					ret.add(groupLabeling);
				}

			}

		}

		if (!defaultCompositeIng.getIngList().isEmpty()) {
			ret.add(defaultCompositeIng);
		}

		return ret;
	}

	/**
	 * Calculate the labeling for a composite ingredient (DETAIL, GRP).
	 * 
	 * @param compoList
	 *            the compo list
	 * @param parentIndex
	 *            the parent index
	 * @param lastChild
	 *            the last child
	 * @return the composite ing
	 * @throws FormulateException
	 */
	private CompositeLabeling calculateILLOfCompositeIng(Composite<CompoListDataItem> composite, LabelingFormulaContext labelingFormulaContext) throws FormulateException {

		CompoListDataItem compoListDataItem = composite.getData();
		
		ProductData productData = (ProductData) alfrescoRepository.findOne(compoListDataItem.getProduct());
		
		CompositeLabeling compositeLabeling = new CompositeLabeling(productData) ;
		compositeLabeling.setQty(FormulationHelper.getQty(compoListDataItem));

		if (logger.isDebugEnabled()) {
			logger.debug("New compositeIng " + compositeLabeling.getLegalName(I18NUtil.getContentLocaleLang()) + " qtyUsed: " + compositeLabeling.getQty());
		}

		for (Composite<CompoListDataItem> child : composite.getChildren()) {
			// TODO manage more level
			compositeLabeling = calculateILLOfCompositeIng(compositeLabeling, child, labelingFormulaContext);
		}

		return compositeLabeling;
	}

	/**
	 * Add the ingredients labeling of the part in the composite ingredient.
	 * 
	 * @param parentIng
	 *            the parent ing
	 * @param compoListDataItem
	 *            the compo list data item
	 * @return the composite ing
	 * @throws FormulateException
	 */
	private CompositeLabeling calculateILLOfCompositeIng(CompositeLabeling parentIng, Composite<CompoListDataItem> component, LabelingFormulaContext labelingFormulaContext)
			throws FormulateException {

		CompoListDataItem compoListDataItem = component.getData();

		if (logger.isDebugEnabled()) {
			logger.debug("calculateILLOfCompositeIng: " + compoListDataItem.getQty() + " product "
					+ nodeService.getProperty(compoListDataItem.getProduct(), ContentModel.PROP_NAME));
		}

		ProductData productData = (ProductData) alfrescoRepository.findOne(compoListDataItem.getProduct());
		
		DeclarationType declarationType = getDeclarationType(compoListDataItem, null, labelingFormulaContext);
		boolean isDeclared = (declarationType == DeclarationType.DoNotDeclare) ? false : true;
		CompositeLabeling compositeLabeling = parentIng;

		// Calculate qtyRMUsed
		Double qty = FormulationHelper.getQtyInKg(compoListDataItem);

		// OMIT, DETAIL
		if (declarationType == DeclarationType.Omit) {
			return parentIng;// nothing to do...
		} else if (declarationType == DeclarationType.Detail) {

			compositeLabeling = new CompositeLabeling(productData);
			compositeLabeling.setQty(qty);
			compositeLabeling.setQtyRMUsed(qty);
			parentIng.add(compositeLabeling, isDeclared);

			logger.debug("Add detailed ing : " + compositeLabeling.getName() + " qty: " + qty);
		}

		if (productData.getIngList() != null && !productData.getIngList().isEmpty()) {

			addQtyRMUsed(compoListDataItem, parentIng);

			for (IngListDataItem ingListDataItem : productData.getIngList()) {

				// Look for ing
				declarationType = getDeclarationType(compoListDataItem, ingListDataItem, labelingFormulaContext);
				if (declarationType != DeclarationType.Omit) {

					NodeRef ingNodeRef = ingListDataItem.getIng();

					IngItem ingItem = (compositeLabeling.get(ingNodeRef, isDeclared) instanceof IngItem) ? (IngItem) compositeLabeling.get(ingNodeRef, isDeclared) : null;

					if (ingItem == null) {
						ingItem = (IngItem)alfrescoRepository.findOne(ingNodeRef);
						compositeLabeling.add(ingItem, isDeclared);
					}

					Double qtyPerc = ingListDataItem.getQtyPerc();

					if (qtyPerc == null) {
						ingItem.setQty(null);
					} else {
						// if one ingItem has null perc -> must be null
						if (ingItem.getQty() != null) {
							if (qty != null) {

								Double totalQtyIng = ingItem.getQty();

								Double valueToAdd = qty * qtyPerc / 100;
								totalQtyIng += valueToAdd;
								ingItem.setQty(totalQtyIng);

								if (logger.isDebugEnabled()) {
									logger.debug("ILL valueToAdd " + ingItem.getLegalName(I18NUtil.getContentLocaleLang()) + " " + valueToAdd);
								}
							}
						}
					}
				}

			}

		}
		return parentIng;

	}


	private DeclarationType getDeclarationType(CompoListDataItem compoListDataItem, IngListDataItem ingListDataItem, LabelingFormulaContext labelingFormulaContext) {

		if (ingListDataItem == null) {
			if (labelingFormulaContext.getNodeDeclarationFilters().containsKey(compoListDataItem.getProduct())) {
				DeclarationFilter declarationFilter = labelingFormulaContext.getNodeDeclarationFilters().get(compoListDataItem.getProduct());
				if (declarationFilter.getFormula() == null || labelingFormulaContext.matchFormule(declarationFilter.getFormula(), new DeclarationFilterContext(compoListDataItem, ingListDataItem))) {
					return declarationFilter.getDeclarationType();
				}
			}
		} else {

			if (labelingFormulaContext.getNodeDeclarationFilters().containsKey(ingListDataItem.getIng())) {
				DeclarationFilter declarationFilter = labelingFormulaContext.getNodeDeclarationFilters().get(ingListDataItem.getIng());
				if (declarationFilter.getFormula() == null || labelingFormulaContext.matchFormule(declarationFilter.getFormula(), new DeclarationFilterContext(compoListDataItem, ingListDataItem))) {
					return declarationFilter.getDeclarationType();
				}
			}

			for (DeclarationFilter declarationFilter : labelingFormulaContext.getDeclarationFilters()) {
				if (declarationFilter.getFormula() != null && labelingFormulaContext.matchFormule(declarationFilter.getFormula(), new DeclarationFilterContext(compoListDataItem, ingListDataItem))) {
					return declarationFilter.getDeclarationType();
				}
			}
		}

		return compoListDataItem.getDeclType();
	}

	private void addQtyRMUsed(CompoListDataItem compoListDataItem, CompositeLabeling compositeLabeling) throws FormulateException {

		Double qty = FormulationHelper.getQtyInKg(compoListDataItem);
		if (qty != null) {
			Double qtyRMUsed = compositeLabeling.getQtyRMUsed();
			qtyRMUsed += qty;
			compositeLabeling.setQtyRMUsed(qtyRMUsed);
		}
	}

}
