/*
 * 
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.ing.AbstractLabelingComponent;
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
import fr.becpg.repo.product.data.spel.LabelingFormulaContext.AggregateRule;
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

	private static final Log logger = LogFactory.getLog(LabelingFormulationHandler.class);

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

		copyTemplateLabelingRuleList(formulatedProduct);

		logger.debug("Calculate Ingredient Labeling: ");

		LabelingFormulaContext labelingFormulaContext = new LabelingFormulaContext(mlNodeService, alfrescoRepository);

		List<LabelingRuleListDataItem> labelingRuleLists = getLabelingRules(formulatedProduct);

		// Apply before formula
		for (LabelingRuleListDataItem labelingRuleListDataItem : labelingRuleLists) {
			if (Boolean.TRUE.equals(labelingRuleListDataItem.getIsActive())) {
				LabelingRuleType type = labelingRuleListDataItem.getLabelingRuleType();
				if (LabelingRuleType.Format.equals(type)) {
					labelingFormulaContext.formatText(labelingRuleListDataItem.getComponents(), labelingRuleListDataItem.getFormula());
				} else if (LabelingRuleType.Rename.equals(type)) {
					labelingFormulaContext.rename(labelingRuleListDataItem.getComponents(), labelingRuleListDataItem.getReplacements(), labelingRuleListDataItem.getLabel(),
							labelingRuleListDataItem.getFormula());
				} else if (LabelingRuleType.Locale.equals(type)) {
					labelingFormulaContext.addLocale(labelingRuleListDataItem.getFormula());
				} else if (!LabelingRuleType.Render.equals(type)){
					labelingFormulaContext.addRule(labelingRuleListDataItem.getComponents(), labelingRuleListDataItem.getReplacements(), labelingRuleListDataItem.getLabel(),
							labelingRuleListDataItem.getFormula(), type);
				}
			}
		}

		// Compute composite
		Composite<CompoListDataItem> compositeDefaultVariant = CompositeHelper.getHierarchicalCompoList(formulatedProduct.getCompoList(EffectiveFilters.ALL,
				VariantFilters.DEFAULT_VARIANT));

		CompositeLabeling compositeLabeling = calculateILLV2(compositeDefaultVariant, labelingFormulaContext);

		applyAggregateRules(compositeLabeling, labelingFormulaContext, true);

		if (logger.isDebugEnabled()) {
			logger.debug(" - " + compositeLabeling.toString());
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
		List<LabelingRuleListDataItem> ret = new ArrayList<>(formulatedProduct.getLabelingListView().getLabelingRuleList());
		if (formulatedProduct.getEntityTpl() != null) {
			for (LabelingRuleListDataItem modelLabelingRuleListDataItem : formulatedProduct.getEntityTpl().getLabelingListView().getLabelingRuleList()) {
				if (!Boolean.TRUE.equals(modelLabelingRuleListDataItem.getIsManual())) {
					ret.add(modelLabelingRuleListDataItem);
				}
			}
		}

		return ret;
	}

	private void copyTemplateLabelingRuleList(ProductData formulatedProduct) {
		if (formulatedProduct.getEntityTpl() != null) {
			for (LabelingRuleListDataItem modelLabelingRuleListDataItem : formulatedProduct.getEntityTpl().getLabelingListView().getLabelingRuleList()) {
				if (Boolean.TRUE.equals(modelLabelingRuleListDataItem.getIsManual())) {
					boolean contains = false;
					for (LabelingRuleListDataItem labelingRuleListDataItem : formulatedProduct.getLabelingListView().getLabelingRuleList()) {
						if (labelingRuleListDataItem.getName().equals(modelLabelingRuleListDataItem.getName())) {
							contains = true;
							break;
						}
					}
					if (!contains) {
						modelLabelingRuleListDataItem.setNodeRef(null);
						modelLabelingRuleListDataItem.setParentNodeRef(null);
						formulatedProduct.getLabelingListView().getLabelingRuleList().add(modelLabelingRuleListDataItem);
					}

				}
			}

		}

	}

	private void applyAggregateRules(CompositeLabeling parent, LabelingFormulaContext labelingFormulaContext, boolean firstLevel) {
		if (!labelingFormulaContext.getAggregateRules().isEmpty()) {

			Map<NodeRef, AbstractLabelingComponent> toAdd = new HashMap<>();

			for (Iterator<Map.Entry<NodeRef, AbstractLabelingComponent>> iterator = parent.getIngList().entrySet().iterator(); iterator.hasNext();) {
				AbstractLabelingComponent component = (AbstractLabelingComponent) iterator.next().getValue();

				AggregateRule aggregateRule = labelingFormulaContext.getAggregateRules().get(component.getNodeRef());

				// Create a new
				if (aggregateRule != null && !LabelingRuleType.Type.equals(aggregateRule.getLabelingRuleType())) {

					NodeRef aggregateRuleNodeRef = aggregateRule.getKey();

					if (toAdd.containsKey(aggregateRuleNodeRef) || aggregateRule.matchAll(parent.getIngList().values())) {

						Double qty = component.getQty();

						boolean is100Perc = aggregateRule.getQty() == null || aggregateRule.getQty() == 100d;
						// Add ing to group
						if (qty != null && aggregateRule.getQty() != null) {
							qty = qty * aggregateRule.getQty() / 100;
						}

						// Replacement
						if (aggregateRule.getReplacement() != null && LabelingRuleType.DoNotDetails.equals(aggregateRule.getLabelingRuleType())) {
							AbstractLabelingComponent current = toAdd.containsKey(aggregateRuleNodeRef) ? toAdd.get(aggregateRuleNodeRef) : parent.getIngList().get(
									aggregateRuleNodeRef);

							if (current == null) {

								RepositoryEntity replacement = alfrescoRepository.findOne(aggregateRule.getReplacement());
								if (replacement instanceof IngItem) {
									current = (IngItem) replacement;
									current.setQty(0d);
									toAdd.put(aggregateRuleNodeRef, current);
								}
							}
							if (qty != null) {
								current.setQty(current.getQty() + qty);
							}

						} else {

							CompositeLabeling compositeLabeling = (CompositeLabeling) (toAdd.containsKey(aggregateRuleNodeRef) ? toAdd.get(aggregateRuleNodeRef) : parent
									.getIngList().get(aggregateRuleNodeRef));
							if (compositeLabeling == null) {

								compositeLabeling = new CompositeLabeling();
								compositeLabeling.setNodeRef(aggregateRuleNodeRef);
								compositeLabeling.setLegalName(aggregateRule.getLabel());
								compositeLabeling.setGroup(firstLevel && LabelingRuleType.Group.equals(aggregateRule.getLabelingRuleType()));
								if (logger.isTraceEnabled()) {
									logger.trace("Create new aggregate group :" + compositeLabeling.getLegalName(Locale.getDefault()));
								}

								// Add to current list
								toAdd.put(aggregateRuleNodeRef, compositeLabeling);
							}
							AbstractLabelingComponent current = compositeLabeling.getIngList().get(component.getNodeRef());

							if (!LabelingRuleType.DoNotDetails.equals(aggregateRule.getLabelingRuleType())) {
								if (current == null) {
									if (!is100Perc) {
										if (component instanceof CompositeLabeling) {
											current = new CompositeLabeling((CompositeLabeling) component);
										} else {
											current = new IngItem((IngItem) component);
										}
									} else {
										current = component;
									}

									current.setQty(0d);
									if (logger.isTraceEnabled()) {
										logger.trace(" - Add new ing to aggregate  :" + current.getName());
									}
									compositeLabeling.add(current);
								}
							}

							if (qty != null) {
								if (current != null) {
									if (logger.isTraceEnabled()) {
										logger.trace(" - Update aggregate ing qty  :" + current.getName() + " " + qty);
									}
									current.setQty(current.getQty() + qty);
								}
								Double qtyRMUsed = compositeLabeling.getQtyRMUsed();
								qtyRMUsed += qty;
								compositeLabeling.setQtyRMUsed(qtyRMUsed);
								compositeLabeling.setQty(qtyRMUsed);
							}

						}

						if (is100Perc) {
							iterator.remove();
						} else if (qty != null) {
							component.setQty(component.getQty() - qty);
						}

					}
				}
				// Recur
				if (component instanceof CompositeLabeling) {
					applyAggregateRules((CompositeLabeling) component, labelingFormulaContext, false);
				}

			}

			for (AbstractLabelingComponent ing : toAdd.values()) {
				parent.add(ing);
			}

		}

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

		NodeRef productNodeRef = compoListDataItem.getProduct();

		// Calculate qtyRMUsed
		Double qty = FormulationHelper.getQtyInKg(compoListDataItem);

		ProductData productData = (ProductData) alfrescoRepository.findOne(productNodeRef);

		boolean isMultiLevel = false;

		if (parent == null) {
			parent = new CompositeLabeling(productData);
			parent.setQty(qty);
			if (logger.isTraceEnabled()) {
				logger.trace("+ Creating new group [" + parent.getLegalName(I18NUtil.getContentLocaleLang()) + "] qtyUsed: " + parent.getQty());
			}
		}

		CompositeLabeling compositeLabeling = parent;

		if (DeclarationType.Detail.equals(declarationType) || DeclarationType.Group.equals(declarationType) || DeclarationType.DoNotDetails.equals(declarationType)) {

			// MultiLevel only if detail or group
			if (!DeclarationType.DoNotDetails.equals(declarationType) && (productData instanceof SemiFinishedProductData || productData instanceof FinishedProductData)) {
				@SuppressWarnings("unchecked")
				Composite<CompoListDataItem> sfComposite = CompositeHelper.getHierarchicalCompoList(productData.getCompoList(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT));
				for (Composite<CompoListDataItem> sfChild : sfComposite.getChildren()) {
					sfChild.getData().setParent(compoListDataItem);
					composite.addChild(sfChild);
				}
				isMultiLevel = true;
			}

			compositeLabeling = new CompositeLabeling(productData);
			compositeLabeling.setQty(qty);
			compositeLabeling.setGroup(DeclarationType.Group.equals(declarationType));
			if (composite.isLeaf()) {
				compositeLabeling.setQtyRMUsed(qty);
			}
			parent.add(compositeLabeling);

			if (logger.isTraceEnabled()) {
				logger.trace(" - Add detailed labeling component : " + compositeLabeling.getName() + " qty: " + qty);
			}

		} else if (DeclarationType.Omit.equals(declarationType)) {
			return parent;
		}

		if (!DeclarationType.DoNotDetails.equals(declarationType) && !DeclarationType.DoNotDeclare.equals(declarationType)) {

			if (!isMultiLevel && productData.getIngList() != null && !productData.getIngList().isEmpty()) {

				for (IngListDataItem ingListDataItem : productData.getIngList()) {

					DeclarationType ingDeclarationType = getDeclarationType(compoListDataItem, ingListDataItem, labelingFormulaContext);

					if (!DeclarationType.Omit.equals(ingDeclarationType) && !DeclarationType.DoNotDeclare.equals(ingDeclarationType)) {

						NodeRef ingNodeRef = ingListDataItem.getIng();
						IngItem ingItem = (compositeLabeling.get(ingNodeRef) instanceof IngItem) ? (IngItem) compositeLabeling.get(ingNodeRef) : null;

						if (ingItem == null) {
							ingItem = (IngItem) alfrescoRepository.findOne(ingNodeRef);

							if (ingListDataItem.getIngListSubIng().size() > 0) {
								for (NodeRef subIng : ingListDataItem.getIngListSubIng()) {
									ingItem.getSubIngs().add((IngItem) alfrescoRepository.findOne(subIng));
								}

							}

							compositeLabeling.add(ingItem);
							if (logger.isTraceEnabled()) {
								logger.trace("- Add new ing to current Label: " + ingItem.getLegalName(I18NUtil.getContentLocaleLang()));
							}
						} else if (logger.isDebugEnabled()) {
							logger.trace("- Update ing value: " + ingItem.getLegalName(I18NUtil.getContentLocaleLang()));
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

									if (logger.isTraceEnabled()) {
										logger.trace(" -- new qty to add :" + valueToAdd);
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
		}

		if (!(DeclarationType.Declare.equals(declarationType) && productData instanceof LocalSemiFinishedProductData)) {
			// Update parent qty
			if (qty != null) {
				Double qtyRMUsed = parent.getQtyRMUsed();
				qtyRMUsed += qty;
				parent.setQtyRMUsed(qtyRMUsed);
			}
		}

		return parent;

	}

	private DeclarationType getDeclarationType(CompoListDataItem compoListDataItem, IngListDataItem ingListDataItem, LabelingFormulaContext labelingFormulaContext) {

		if (ingListDataItem == null) {
			if (labelingFormulaContext.getNodeDeclarationFilters().containsKey(compoListDataItem.getProduct())) {
				DeclarationFilter declarationFilter = labelingFormulaContext.getNodeDeclarationFilters().get(compoListDataItem.getProduct());
				if (declarationFilter.getFormula() == null
						|| labelingFormulaContext.matchFormule(declarationFilter.getFormula(), new DeclarationFilterContext(compoListDataItem, ingListDataItem))) {

					if (logger.isTraceEnabled()) {
						logger.trace(" -- Found declType : " + declarationFilter.getDeclarationType() + " for " + getName(compoListDataItem.getProduct()));
					}
					return declarationFilter.getDeclarationType();
				}
			}
		} else {

			if (labelingFormulaContext.getNodeDeclarationFilters().containsKey(ingListDataItem.getIng())) {
				DeclarationFilter declarationFilter = labelingFormulaContext.getNodeDeclarationFilters().get(ingListDataItem.getIng());
				if (declarationFilter.getFormula() == null
						|| labelingFormulaContext.matchFormule(declarationFilter.getFormula(), new DeclarationFilterContext(compoListDataItem, ingListDataItem))) {
					if (logger.isTraceEnabled()) {
						logger.trace(" -- Found declType : " + declarationFilter.getDeclarationType() + " for " + getName(ingListDataItem.getIng()));
					}
					return declarationFilter.getDeclarationType();
				}
			}

			for (DeclarationFilter declarationFilter : labelingFormulaContext.getDeclarationFilters()) {
				if (declarationFilter.getFormula() != null
						&& labelingFormulaContext.matchFormule(declarationFilter.getFormula(), new DeclarationFilterContext(compoListDataItem, ingListDataItem))) {
					if (logger.isTraceEnabled()) {
						logger.trace(" -- Found declType : " + declarationFilter.getDeclarationType() + " for " + getName(ingListDataItem.getIng()));
					}
					return declarationFilter.getDeclarationType();
				}
			}
		}

		if (logger.isTraceEnabled()) {
			logger.trace(" -- Found declType : " + compoListDataItem.getDeclType() + " for default " + getName(compoListDataItem.getProduct()));
		}
		return compoListDataItem.getDeclType();
	}

	private String getName(NodeRef nodeRef) {
		return (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
	}

}
