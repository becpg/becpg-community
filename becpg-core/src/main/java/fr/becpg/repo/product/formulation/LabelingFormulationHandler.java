/*
 * 
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
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
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.data.productList.RequirementType;
import fr.becpg.repo.product.data.spel.DeclarationFilterContext;
import fr.becpg.repo.product.data.spel.LabelingFormulaContext;
import fr.becpg.repo.product.data.spel.SpelHelper;
import fr.becpg.repo.product.data.spel.LabelingFormulaContext.AggregateRule;
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

		// TODO add model only rules
		List<LabelingRuleListDataItem> labelingRuleLists = getLabelingRules(formulatedProduct);

		// Apply before formula
		for (LabelingRuleListDataItem labelingRuleListDataItem : labelingRuleLists) {
			LabelingRuleType type = labelingRuleListDataItem.getLabelingRuleType();
			if (LabelingRuleType.Format.equals(type)) {
				labelingFormulaContext.formatText(labelingRuleListDataItem.getComponents(), labelingRuleListDataItem.getFormula());
			} else if (LabelingRuleType.Rename.equals(type)) {
				labelingFormulaContext.rename(labelingRuleListDataItem.getComponents(), labelingRuleListDataItem.getReplacements(), labelingRuleListDataItem.getLabel() ,labelingRuleListDataItem.getFormula());
			} else if (LabelingRuleType.Locale.equals(type)) {
				labelingFormulaContext.addLocale(labelingRuleListDataItem.getFormula());
			} else if (LabelingRuleType.Detail.equals(type) || LabelingRuleType.Declare.equals(type) || LabelingRuleType.DoNotDeclare.equals(type)
					|| LabelingRuleType.Omit.equals(type) || LabelingRuleType.Group.equals(type)) {
				labelingFormulaContext.declare(labelingRuleListDataItem.getComponents(), labelingRuleListDataItem.getFormula(), DeclarationType.valueOf(type.toString()));
			} else if (LabelingRuleType.Aggregate.equals(type)) {
				labelingFormulaContext.aggregate(labelingRuleListDataItem.getComponents(), labelingRuleListDataItem.getReplacements(), labelingRuleListDataItem.getLabel(), labelingRuleListDataItem.getFormula());
			} 
		}

		// Compute composite
		Composite<CompoListDataItem> compositeDefaultVariant = CompositeHelper.getHierarchicalCompoList(formulatedProduct.getCompoList(EffectiveFilters.ALL,
				VariantFilters.DEFAULT_VARIANT));

		CompositeLabeling compositeLabeling = calculateILLV2(compositeDefaultVariant, labelingFormulaContext);

		if (logger.isDebugEnabled()) {
			logger.debug(compositeLabeling.toString());
		}

		List<IngLabelingListDataItem> retainNodes = new ArrayList<IngLabelingListDataItem>();
		if (!compositeLabeling.getIngList().isEmpty()) {

			// Store results
			labelingFormulaContext.setCompositeLabeling(compositeLabeling);

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
								logger.debug("Running renderFormula :" + labelingRuleListDataItem.getFormula() + " for locale :" + locale.toString());
								logger.debug(" - render value :" + ret);
							}
							label.addValue(locale, ret);
						} catch (Exception e) {
							String message = I18NUtil.getMessage("message.formulate.labelRule.error", labelingRuleListDataItem.getName(), e.getLocalizedMessage());
							formulatedProduct.getCompoListView().getReqCtrlList().add(new ReqCtrlListDataItem(null, RequirementType.Tolerated, message, new ArrayList<NodeRef>()));

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
		}
		formulatedProduct.getLabelingListView().getIngLabelingList().retainAll(retainNodes);

		return true;
	}

	private List<LabelingRuleListDataItem> getLabelingRules(ProductData formulatedProduct) {
		// TODO
		// List<LabelingRuleListDataItem> ret = new LinkedList<>();
		//
		// if(formulatedProduct.getEntityTplRef() !=null){
		// List<RepositoryEntity> modelRules =
		// alfrescoRepository.loadDataList(formulatedProduct.getEntityTplRef(),
		// BeCPGModel.TYPE_LABELING_RULE_LIST,
		// BeCPGModel.TYPE_LABELING_RULE_LIST);
		// for (RepositoryEntity modelRule : modelRules) {
		// if(modelRule instanceof LabelingRuleListDataItem
		// && ((LabelingRuleListDataItem) modelRule).getIsManual()){
		// ret.add((LabelingRuleListDataItem)modelRule);
		// }
		// }
		// }
		// ret.addAll(formulatedProduct.getLabelingListView().getLabelingRuleList());
		// return ret;

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


	private CompositeLabeling calculateILLV2(Composite<CompoListDataItem> composite, LabelingFormulaContext labelingFormulaContext) throws FormulateException {

		CompositeLabeling ret = new CompositeLabeling();

		for (Composite<CompoListDataItem> component : composite.getChildren()) {

			DeclarationType declarationType = getDeclarationType(component.getData(), null, labelingFormulaContext);

			if (!DeclarationType.Omit.equals(declarationType)) {
				calculateILLV2(ret, component, labelingFormulaContext, declarationType);
			}

		}
		return ret;
	}

	private CompositeLabeling calculateILLV2(CompositeLabeling parent, Composite<CompoListDataItem> composite, LabelingFormulaContext labelingFormulaContext,
			DeclarationType declarationType) throws FormulateException {
		CompoListDataItem compoListDataItem = composite.getData();

		ProductData productData = (ProductData) alfrescoRepository.findOne(compoListDataItem.getProduct());

		// Calculate qtyRMUsed
		Double qty = FormulationHelper.getQtyInKg(compoListDataItem);

		if (parent == null) {
			parent = new CompositeLabeling(productData);
			parent.setQty(qty);
			if (logger.isDebugEnabled()) {
				logger.debug("+ Creating new group [" + parent.getLegalName(I18NUtil.getContentLocaleLang()) + "] qtyUsed: " + parent.getQty());
			}
		}

		CompositeLabeling compositeLabeling = parent;

		if (DeclarationType.Detail.equals(declarationType) || DeclarationType.Group.equals(declarationType)) {
			compositeLabeling = new CompositeLabeling(productData);
			compositeLabeling.setQty(qty);
			compositeLabeling.setGroup(DeclarationType.Group.equals(declarationType));
			if (composite.isLeaf()) {
				compositeLabeling.setQtyRMUsed(qty);
			}
			parent.add(compositeLabeling);

			if (logger.isDebugEnabled()) {
				logger.debug(" - Add detailed labeling component : " + compositeLabeling.getName() + " qty: " + qty);
			}

		} else if (DeclarationType.Omit.equals(declarationType)) {
			return parent;
		}

		if (productData.getIngList() != null && !productData.getIngList().isEmpty()) {

			for (IngListDataItem ingListDataItem : productData.getIngList()) {

				DeclarationType ingDeclarationType = getDeclarationType(compoListDataItem, ingListDataItem, labelingFormulaContext);

				if (!DeclarationType.Omit.equals(ingDeclarationType) && !DeclarationType.DoNotDeclare.equals(ingDeclarationType)) {

					AggregateRule aggregateRule  = labelingFormulaContext.getAggregateRules().get(ingListDataItem.getIng());
					
					NodeRef ingNodeRef = aggregateRule!=null && aggregateRule.getReplacement()!=null? aggregateRule.getReplacement() : ingListDataItem.getIng();

					IngItem ingItem = (compositeLabeling.get(ingNodeRef) instanceof IngItem) ? (IngItem) compositeLabeling.get(ingNodeRef) : null;

					if (ingItem == null) {
						ingItem = (IngItem) alfrescoRepository.findOne(ingNodeRef);
						compositeLabeling.add(ingItem);
						if (logger.isDebugEnabled()) {
							logger.debug("- Add new ing to current Label" + ingItem.getLegalName(I18NUtil.getContentLocaleLang()));
						}
					} else if (logger.isDebugEnabled()) {
						logger.debug("- Update ing value" + ingItem.getLegalName(I18NUtil.getContentLocaleLang()));
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
									logger.debug(" -- new qty to add :" + valueToAdd);
								}
							}
						}
					}
				}

			}

		}

		// Recur
		if (!composite.isLeaf()) {
			for (Composite<CompoListDataItem> component : composite.getChildren()) {

				if (!DeclarationType.Omit.equals(declarationType)) {
					calculateILLV2(compositeLabeling, component, labelingFormulaContext, getDeclarationType(component.getData(), null, labelingFormulaContext));
				}
			}
		}

		// Update parent qty
		if (qty != null) {
			Double qtyRMUsed = parent.getQtyRMUsed();
			qtyRMUsed += qty;
			parent.setQtyRMUsed(qtyRMUsed);
		}

		return parent;

	}

	private DeclarationType getDeclarationType(CompoListDataItem compoListDataItem, IngListDataItem ingListDataItem, LabelingFormulaContext labelingFormulaContext) {

		if (ingListDataItem == null) {
			if (labelingFormulaContext.getNodeDeclarationFilters().containsKey(compoListDataItem.getProduct())) {
				DeclarationFilter declarationFilter = labelingFormulaContext.getNodeDeclarationFilters().get(compoListDataItem.getProduct());
				if (declarationFilter.getFormula() == null
						|| labelingFormulaContext.matchFormule(declarationFilter.getFormula(), new DeclarationFilterContext(compoListDataItem, ingListDataItem))) {

					logger.debug(" -- Found declType : " + declarationFilter.getDeclarationType() + " for " + getName(compoListDataItem.getProduct()));
					return declarationFilter.getDeclarationType();
				}
			}
		} else {

			if (labelingFormulaContext.getNodeDeclarationFilters().containsKey(ingListDataItem.getIng())) {
				DeclarationFilter declarationFilter = labelingFormulaContext.getNodeDeclarationFilters().get(ingListDataItem.getIng());
				if (declarationFilter.getFormula() == null
						|| labelingFormulaContext.matchFormule(declarationFilter.getFormula(), new DeclarationFilterContext(compoListDataItem, ingListDataItem))) {
					logger.debug(" -- Found declType : " + declarationFilter.getDeclarationType() + " for " + getName(ingListDataItem.getIng()));
					return declarationFilter.getDeclarationType();
				}
			}

			for (DeclarationFilter declarationFilter : labelingFormulaContext.getDeclarationFilters()) {
				if (declarationFilter.getFormula() != null
						&& labelingFormulaContext.matchFormule(declarationFilter.getFormula(), new DeclarationFilterContext(compoListDataItem, ingListDataItem))) {
					logger.debug(" -- Found declType : " + declarationFilter.getDeclarationType() + " for " + getName(ingListDataItem.getIng()));
					return declarationFilter.getDeclarationType();
				}
			}
		}

		logger.debug(" -- Found declType : " + compoListDataItem.getDeclType() + " for default " + getName(compoListDataItem.getProduct()));
		return compoListDataItem.getDeclType();
	}

	private String getName(NodeRef nodeRef) {
		return (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
	}

}
