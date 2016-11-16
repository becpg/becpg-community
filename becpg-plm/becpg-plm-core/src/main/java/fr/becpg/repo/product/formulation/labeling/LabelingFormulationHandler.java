/*
 *
 */
package fr.becpg.repo.product.formulation.labeling;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.AllergenType;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.LabelingRuleType;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.ing.AbstractLabelingComponent;
import fr.becpg.repo.product.data.ing.CompositeLabeling;
import fr.becpg.repo.product.data.ing.DeclarationFilter;
import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.data.spel.DeclarationFilterContext;
import fr.becpg.repo.product.data.spel.SpelHelper;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.product.formulation.labeling.LabelingFormulaContext.AggregateRule;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.variant.filters.VariantFilters;

/**
 * @author matthieu
 */
public class LabelingFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(LabelingFormulationHandler.class);

	private NodeService nodeService;

	protected AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	private NodeService mlNodeService;

	private AssociationService associationService;

	private boolean ingsCalculatingWithYield = false;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setMlNodeService(NodeService mlNodeService) {
		this.mlNodeService = mlNodeService;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setIngsCalculatingWithYield(boolean ingsCalculatingWithYield) {
		this.ingsCalculatingWithYield = ingsCalculatingWithYield;
	}

	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		if ((formulatedProduct.getReformulateCount() != null)
				&& !formulatedProduct.getReformulateCount().equals(formulatedProduct.getCurrentReformulateCount())) {
			logger.debug("Skip labeling in reformulateCount " + formulatedProduct.getCurrentReformulateCount());
			return true;
		}

		// no compo => no formulation
		if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL)
				|| !formulatedProduct.hasCompoListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
			logger.debug("no compo => no formulation - " + formulatedProduct.getName());
			return true;
		}

		copyTemplateLabelingRuleList(formulatedProduct);

		Map<String, List<LabelingRuleListDataItem>> labelingRuleListsByGroup = getLabelingRules(formulatedProduct);

		List<IngLabelingListDataItem> retainNodes = new ArrayList<>();

		boolean shouldSkip = true;

		// Keep Manual ingList
		for (IngLabelingListDataItem tmp : formulatedProduct.getLabelingListView().getIngLabelingList()) {
			if ((tmp.getManualValue() != null) && !tmp.getManualValue().isEmpty()) {
				tmp.setValue(null);
				retainNodes.add(tmp);
			}
		}

		for (Map.Entry<String, List<LabelingRuleListDataItem>> labelingRuleListsGroup : labelingRuleListsByGroup.entrySet()) {

			logger.debug("Calculate Ingredient Labeling for group : " + labelingRuleListsGroup.getKey() + " - " + formulatedProduct.getName());

			LabelingFormulaContext labelingFormulaContext = new LabelingFormulaContext(mlNodeService, associationService, alfrescoRepository);

			ExpressionParser parser = new SpelExpressionParser();
			StandardEvaluationContext dataContext = new StandardEvaluationContext(labelingFormulaContext);

			List<LabelingRuleListDataItem> labelingRuleLists = labelingRuleListsGroup.getValue();

			// Apply before formula
			for (LabelingRuleListDataItem labelingRuleListDataItem : labelingRuleLists) {
				if (Boolean.TRUE.equals(labelingRuleListDataItem.getIsActive())) {
					LabelingRuleType type = labelingRuleListDataItem.getLabelingRuleType();
					if (LabelingRuleType.Format.equals(type)) {
						labelingFormulaContext.formatText(labelingRuleListDataItem.getComponents(), labelingRuleListDataItem.getFormula());
					} else if (LabelingRuleType.Rename.equals(type)) {
						labelingFormulaContext.rename(labelingRuleListDataItem.getComponents(), labelingRuleListDataItem.getReplacements(),
								labelingRuleListDataItem.getLabel(), labelingRuleListDataItem.getFormula());
					} else if (LabelingRuleType.Locale.equals(type)) {
						labelingFormulaContext.addLocale(labelingRuleListDataItem.getFormula());
					} else if (LabelingRuleType.Prefs.equals(type)) {
						try {
							Expression exp = parser.parseExpression(SpelHelper.formatFormula(labelingRuleListDataItem.getFormula()));
							exp.getValue(dataContext, String.class);

						} catch (Exception e) {
							String message = I18NUtil.getMessage("message.formulate.labelRule.error", labelingRuleListDataItem.getName(),
									e.getLocalizedMessage());
							formulatedProduct.getReqCtrlList().add(new ReqCtrlListDataItem(null, RequirementType.Tolerated, message, null,
									new ArrayList<NodeRef>(), RequirementDataType.Labelling));
							if (logger.isInfoEnabled()) {
								logger.info("Error in formula :" + SpelHelper.formatFormula(labelingRuleListDataItem.getFormula()), e);
							}
						}
					} else if (!LabelingRuleType.Render.equals(type)) {
						labelingFormulaContext.addRule(labelingRuleListDataItem.getNodeRef(), labelingRuleListDataItem.getName(),
								labelingRuleListDataItem.getComponents(), labelingRuleListDataItem.getReplacements(),
								labelingRuleListDataItem.getLabel(), labelingRuleListDataItem.getFormula(), type);
					} else if (LabelingRuleType.Render.equals(type)) {
						shouldSkip = false;
					}
				}
			}

			if (shouldSkip) {
				logger.debug("No render rule for " + formulatedProduct.getName() + " skipping ");
				return true;
			}

			List<CompoListDataItem> compoList = formulatedProduct.getCompoList(new VariantFilters<>());

			// Compute composite
			Composite<CompoListDataItem> compositeDefaultVariant = CompositeHelper.getHierarchicalCompoList(compoList);

			CompositeLabeling compositeLabeling = visitCompositeLabeling(new CompositeLabeling(CompositeLabeling.ROOT), compositeDefaultVariant,
					labelingFormulaContext, 1d, formulatedProduct.getYield(), formulatedProduct.getRecipeQtyUsed(), true);

			if (logger.isTraceEnabled()) {
				logger.trace(" Before aggrate \n " + compositeLabeling.toString());
			}

			applyAggregateRules(compositeLabeling, labelingFormulaContext, true, false);

			if (logger.isTraceEnabled()) {
				logger.trace(" Before aggregate legalName \n " + compositeLabeling.toString());
			}

			aggregateLegalName(compositeLabeling, labelingFormulaContext, true);

			if (logger.isTraceEnabled()) {
				logger.trace(" Before reorder \n " + compositeLabeling.toString());
			}

			reorderCompositeLabeling(compositeLabeling, true);

			if (logger.isDebugEnabled()) {
				logger.debug("\n" + compositeLabeling.toString());
			}

			if (!compositeLabeling.getIngList().isEmpty()) {

				if (logger.isTraceEnabled()) {
					logger.trace(" Create merged composite labeling");
				}

				CompositeLabeling mergeCompositeLabeling = mergeCompositeLabeling(compositeLabeling, labelingFormulaContext);

				// Store results
				labelingFormulaContext.setCompositeLabeling(compositeLabeling);

				labelingFormulaContext.setMergedLblCompositeContext(mergeCompositeLabeling);

				extractAllergens(labelingFormulaContext, formulatedProduct);

				for (LabelingRuleListDataItem labelingRuleListDataItem : labelingRuleLists) {
					if (LabelingRuleType.Render.equals(labelingRuleListDataItem.getLabelingRuleType())
							&& Boolean.TRUE.equals(labelingRuleListDataItem.getIsActive())) {

						MLText label = new MLText();
						
						Set<Locale> locales  =  labelingFormulaContext.getLocales();
						
						if(locales.isEmpty()){
							locales.add(new Locale(Locale.getDefault().getLanguage()));
						}
								
						for (Locale locale : locales) {
							Locale currentLocal = I18NUtil.getLocale();

							try {
								I18NUtil.setLocale(locale);

								Expression exp = parser.parseExpression(SpelHelper.formatFormula(labelingRuleListDataItem.getFormula()));
								String ret = exp.getValue(dataContext, String.class);
								if (logger.isDebugEnabled()) {
									logger.debug(
											"Running renderFormula :" + labelingRuleListDataItem.getFormula() + " for locale :" + locale.toString());
									logger.debug(" - render value :" + ret);
								}
								label.addValue(locale, ret);
							} catch (Exception e) {
								String message = I18NUtil.getMessage("message.formulate.labelRule.error", labelingRuleListDataItem.getName(),
										e.getLocalizedMessage());
								formulatedProduct.getReqCtrlList().add(new ReqCtrlListDataItem(null, RequirementType.Tolerated, message, null,
										new ArrayList<NodeRef>(), RequirementDataType.Labelling));

								if (logger.isInfoEnabled()) {
									logger.info("Error in formula :" + SpelHelper.formatFormula(labelingRuleListDataItem.getFormula()), e);
								}
							} finally {
								I18NUtil.setLocale(currentLocal);
							}
						}

						// Create logs
						String log = labelingFormulaContext
								.createJsonLog(labelingRuleListDataItem.getFormula().replace(" ", "").contains("render(false)"));

						retainNodes.add(getOrCreateILLDataItem(formulatedProduct, labelingRuleListDataItem.getNodeRef(), label, log));
					}
				}

			}

			formulatedProduct.getReqCtrlList().addAll(labelingFormulaContext.getErrors());

		}

		if (formulatedProduct.getLabelingListView().getIngLabelingList() != null) {
			formulatedProduct.getLabelingListView().getIngLabelingList().retainAll(retainNodes);
		}

		return true;
	}

	private void extractAllergens(LabelingFormulaContext labelingFormulaContext, ProductData productData) {
		for (AllergenListDataItem allergenListDataItem : productData.getAllergenList()) {
			if (allergenListDataItem.getVoluntary()) {
				if (AllergenType.Major.toString().equals(nodeService.getProperty(allergenListDataItem.getAllergen(), PLMModel.PROP_ALLERGEN_TYPE))) {
					labelingFormulaContext.getAllergens().add(allergenListDataItem.getAllergen());
				}
			}
		}

	}

	private void aggregateLegalName(CompositeLabeling parent, LabelingFormulaContext labelingFormulaContext, boolean multiLevel) {

		Map<String, List<AbstractLabelingComponent>> componentsByName = new HashMap<>();

		for (AbstractLabelingComponent component : parent.getIngList().values()) {
			List<AbstractLabelingComponent> tmp = new ArrayList<>();
			String name = labelingFormulaContext.getLegalIngName(component);
			if ((name != null) && !name.isEmpty()) {
				if (componentsByName.containsKey(name)) {
					tmp = componentsByName.get(name);
				}
				tmp.add(component);

				componentsByName.put(name, tmp);
			}
			if (multiLevel && (component instanceof CompositeLabeling)) {
				aggregateLegalName((CompositeLabeling) component, labelingFormulaContext, multiLevel);
			}
		}

		for (List<AbstractLabelingComponent> toAggregate : componentsByName.values()) {

			AbstractLabelingComponent prev = null;
			for (AbstractLabelingComponent component : toAggregate) {
				if (prev == null) {
					prev = component;
				} else {
					// Same type same declaration
					if ((prev instanceof CompositeLabeling) && (component instanceof CompositeLabeling)
							&& (((CompositeLabeling) prev).getDeclarationType() != null)
							&& ((CompositeLabeling) prev).getDeclarationType().equals(((CompositeLabeling) component).getDeclarationType())
							&& !((CompositeLabeling) prev).isGroup()) {

						merge(prev, component);
						parent.remove(component.getNodeRef());
						
						if(labelingFormulaContext.getToApplyThresholdItems().contains(component.getNodeRef())){
							labelingFormulaContext.getToApplyThresholdItems().add(prev.getNodeRef());
						}

					} else if ((prev instanceof CompositeLabeling) && (component instanceof IngItem)
							&& DeclarationType.DoNotDetails.equals(((CompositeLabeling) prev).getDeclarationType())
							&& ((IngItem) component).getSubIngs().isEmpty()) {

						merge(prev, component);
						parent.remove(component.getNodeRef());
						
						if(labelingFormulaContext.getToApplyThresholdItems().contains(component.getNodeRef())){
							labelingFormulaContext.getToApplyThresholdItems().add(prev.getNodeRef());
						}

					} else if ((prev instanceof IngItem) && (component instanceof CompositeLabeling)
							&& DeclarationType.DoNotDetails.equals(((CompositeLabeling) component).getDeclarationType())
							&& ((IngItem) prev).getSubIngs().isEmpty()) {

						merge(component, prev);
						parent.remove(prev.getNodeRef());
						
						if(labelingFormulaContext.getToApplyThresholdItems().contains(prev.getNodeRef())){
							labelingFormulaContext.getToApplyThresholdItems().add(component.getNodeRef());
						}
						
						prev = component;
						

					} else {
						// DO nothing
						prev = component;
					}

				}

			}

		}

	}

	private void merge(AbstractLabelingComponent prev, AbstractLabelingComponent component) {
		if ((prev != null) && (component != null)) {
			if ((prev.getQty() != null) && (component.getQty() != null)) {
				prev.setQty(prev.getQty() + component.getQty());
			} else {
				prev.setQty(null);
			}
			if ((prev.getVolume() != null) && (component.getVolume() != null)) {
				prev.setVolume(prev.getVolume() + component.getVolume());
			} else {
				prev.setVolume(null);
			}

			if ((prev instanceof CompositeLabeling) && (component instanceof CompositeLabeling)) {
				if ((((CompositeLabeling) prev).getQtyTotal() != null) && (((CompositeLabeling) component).getQtyTotal() != null)) {
					((CompositeLabeling) prev).setQtyTotal(((CompositeLabeling) prev).getQtyTotal() + ((CompositeLabeling) component).getQtyTotal());
				} else {
					((CompositeLabeling) prev).setQtyTotal(null);
				}
				if ((((CompositeLabeling) prev).getVolumeTotal() != null) && (((CompositeLabeling) component).getVolumeTotal() != null)) {
					((CompositeLabeling) prev)
							.setVolumeTotal(((CompositeLabeling) prev).getVolumeTotal() + ((CompositeLabeling) component).getVolumeTotal());
				} else {
					((CompositeLabeling) prev).setVolumeTotal(null);
				}
				for(AbstractLabelingComponent  ing : ((CompositeLabeling) component).getIngList().values()){
					if(((CompositeLabeling) prev).getIngList().containsKey(ing.getNodeRef())){
						merge(((CompositeLabeling) prev).getIngList().get(ing.getNodeRef()),ing);
					} else {
						((CompositeLabeling) prev).add(ing);
					}
				}
				
			}
			

			prev.getAllergens().addAll(component.getAllergens());
			
			
			
		}
	

	}

	private CompositeLabeling mergeCompositeLabeling(CompositeLabeling lblCompositeContext, LabelingFormulaContext labelingFormulaContext) {
		CompositeLabeling merged = new CompositeLabeling();
		merged.setQtyTotal(lblCompositeContext.getQtyTotal());
		merged.setVolumeTotal(lblCompositeContext.getVolumeTotal());

		// Start adding all the components
		for (AbstractLabelingComponent component : lblCompositeContext.getIngList().values()) {
			if (!labelingFormulaContext.isGroup(component)) {
				merged.add(component.clone());
			}
		}

		// Then merge
		for (AbstractLabelingComponent component : lblCompositeContext.getIngList().values()) {
			if (labelingFormulaContext.isGroup(component)) {
				CompositeLabeling compositeLabeling = (CompositeLabeling) component;
				for (AbstractLabelingComponent subComponent : compositeLabeling.getIngList().values()) {

					Double qty = labelingFormulaContext.computeQtyPerc(compositeLabeling, subComponent,
							compositeLabeling.getQty() != null ? compositeLabeling.getQty() : 1d);
					Double volume = labelingFormulaContext.computeVolumePerc(compositeLabeling, subComponent,
							compositeLabeling.getVolume() != null ? compositeLabeling.getVolume() : 1d);

					AbstractLabelingComponent toMerged = merged.get(subComponent.getNodeRef());

					if (toMerged == null) {
						AbstractLabelingComponent clonedSubComponent = subComponent.clone();
						clonedSubComponent.setQty(qty);
						clonedSubComponent.setVolume(volume);
						merged.add(clonedSubComponent);
					} else {

						if ((qty != null) && (toMerged.getQty() != null)) {
							toMerged.setQty(toMerged.getQty() + qty);
						}
						if ((volume != null) && (toMerged.getVolume() != null)) {
							toMerged.setVolume(toMerged.getVolume() + volume);
						}

						toMerged.getAllergens().addAll(subComponent.getAllergens());

						// TODO else add warning
					}
				}
			}
		}

		// Level 1 only
		applyAggregateRules(merged, labelingFormulaContext, false, true);

		aggregateLegalName(merged, labelingFormulaContext, false);

		return merged;

	}

	private Map<String, List<LabelingRuleListDataItem>> getLabelingRules(ProductData formulatedProduct) {
		Map<String, List<LabelingRuleListDataItem>> ret = new HashMap<>();
		if (formulatedProduct.getLabelingListView().getLabelingRuleList() != null) {

			for (LabelingRuleListDataItem entityLabelingRulList : formulatedProduct.getLabelingListView().getLabelingRuleList()) {
				addLabelingRule(ret, entityLabelingRulList);
			}

			if ((formulatedProduct.getEntityTpl() != null) && !formulatedProduct.getEntityTpl().equals(formulatedProduct)
					&& (formulatedProduct.getEntityTpl().getLabelingListView().getLabelingRuleList() != null)) {
				for (LabelingRuleListDataItem modelLabelingRuleListDataItem : formulatedProduct.getEntityTpl().getLabelingListView()
						.getLabelingRuleList()) {
					if (!modelLabelingRuleListDataItem.isSynchronisable() && Boolean.TRUE.equals(modelLabelingRuleListDataItem.getIsActive())) {
						addLabelingRule(ret, modelLabelingRuleListDataItem);
					}
				}
			}

			if (formulatedProduct.getProductSpecifications() != null) {
				for (ProductSpecificationData productSpecificationData : formulatedProduct.getProductSpecifications()) {
					for (LabelingRuleListDataItem modelLabelingRuleListDataItem : productSpecificationData.getLabelingRuleList()) {
						if (Boolean.TRUE.equals(modelLabelingRuleListDataItem.getIsActive())) {
							addLabelingRule(ret, modelLabelingRuleListDataItem);
						}
					}
				}
			}
		}
		return ret;
	}

	private void addLabelingRule(Map<String, List<LabelingRuleListDataItem>> ret, LabelingRuleListDataItem labelingRule) {
		String group = labelingRule.getGroup();
		if ((group == null) || group.isEmpty()) {
			group = LabelingRuleListDataItem.DEFAULT_LABELING_GROUP;
		}
		List<LabelingRuleListDataItem> tmp = ret.get(group);
		if (tmp == null) {
			tmp = new ArrayList<>();
		}
		tmp.add(labelingRule);
		ret.put(group, tmp);
	}

	private void copyTemplateLabelingRuleList(ProductData formulatedProduct) {
		if ((formulatedProduct.getEntityTpl() != null) && !formulatedProduct.getEntityTpl().equals(formulatedProduct)) {
			for (LabelingRuleListDataItem modelLabelingRuleListDataItem : formulatedProduct.getEntityTpl().getLabelingListView()
					.getLabelingRuleList()) {
				if (modelLabelingRuleListDataItem.isSynchronisable()) {
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

	private void applyAggregateRules(CompositeLabeling parent, LabelingFormulaContext labelingFormulaContext, boolean recur, boolean mergedLabelling) {

		if (!labelingFormulaContext.getAggregateRules().isEmpty()) {

			Map<NodeRef, AbstractLabelingComponent> toAdd = new HashMap<>();
			Map<NodeRef, AbstractLabelingComponent> ingList = parent.getIngList();

			for (Iterator<Map.Entry<NodeRef, AbstractLabelingComponent>> iterator = ingList.entrySet().iterator(); iterator.hasNext();) {
				AbstractLabelingComponent component = iterator.next().getValue();

				// Recur
				if (recur && (component instanceof CompositeLabeling)) {
					applyAggregateRules((CompositeLabeling) component, labelingFormulaContext, recur, mergedLabelling);
				}

				if (labelingFormulaContext.getAggregateRules().containsKey(component.getNodeRef())) {
					for (AggregateRule aggregateRule : labelingFormulaContext.getAggregateRules().get(component.getNodeRef())) {

						// Create a new
						if (!LabelingRuleType.Type.equals(aggregateRule.getLabelingRuleType()) && !(mergedLabelling && LabelingRuleType.Group.equals(aggregateRule.getLabelingRuleType()))) {

							NodeRef aggregateRuleNodeRef = aggregateRule.getKey();

							if ((aggregateRule.getReplacement() == null) || !aggregateRule.getReplacement().equals(component.getNodeRef())) {

								if (toAdd.containsKey(aggregateRuleNodeRef) || aggregateRule.matchAll(ingList.values(), recur)) {

									Double qty = component.getQty();
									Double volume = component.getVolume();
									Set<NodeRef> allergens = component.getAllergens();

									boolean is100Perc = (aggregateRule.getQty() == null) || (aggregateRule.getQty() == 100d);
									// Add ing to group
									if (aggregateRule.getQty() != null) {
										if (qty != null) {
											qty = (qty * aggregateRule.getQty()) / 100;
										}

										if (volume != null) {
											volume = (volume * aggregateRule.getQty()) / 100;
										}
									}

									if (logger.isTraceEnabled()) {
										logger.trace("Adding :" + getName(component) + "  to aggregate rule, qty " + qty + " is100Perc " + is100Perc);
									}

									// Replacement
									if ((aggregateRule.getReplacement() != null)
											&& LabelingRuleType.DoNotDetails.equals(aggregateRule.getLabelingRuleType())) {
										AbstractLabelingComponent current = toAdd.containsKey(aggregateRuleNodeRef) ? toAdd.get(aggregateRuleNodeRef)
												: ingList.get(aggregateRuleNodeRef);

										if (current == null) {

											RepositoryEntity replacement = alfrescoRepository.findOne(aggregateRule.getReplacement());
											if (replacement instanceof IngItem) {
												current = new IngItem((IngItem) replacement);
												current.setQty(0d);
												current.setVolume(0d);

												if (logger.isTraceEnabled()) {
													logger.trace("Create new aggregate replacement :" + getName(current));
												}
											} else {
												logger.warn("Invalid replacement :" + aggregateRule.getReplacement());
											}
										}

										if (current != null) {

											if (!toAdd.containsKey(aggregateRuleNodeRef)) {
												// if(parent.getIngList().get(aggregateRuleNodeRef)!=null){
												// current.setQty(0d);
												// current.setVolume(0d);
												// }
												toAdd.put(aggregateRuleNodeRef, current);
											}

											if (qty != null && current.getQty()!=null) {
												current.setQty(current.getQty() + qty);
											}

											if (volume != null && current.getVolume()!=null) {
												current.setVolume(current.getVolume() + volume);
											}

											current.getAllergens().addAll(allergens);

										}

									} else {

										// TODO Caused by:
										// java.lang.ClassCastException:
										// fr.becpg.repo.product.data.ing.IngItem
										// cannot
										// be cast to
										// fr.becpg.repo.product.data.ing.CompositeLabeling

										CompositeLabeling compositeLabeling = (CompositeLabeling) (toAdd.containsKey(aggregateRuleNodeRef)
												? toAdd.get(aggregateRuleNodeRef) : ingList.get(aggregateRuleNodeRef));
										if (compositeLabeling == null) {

											compositeLabeling = new CompositeLabeling();
											compositeLabeling.setNodeRef(aggregateRuleNodeRef);
											compositeLabeling.setName(aggregateRule.getName());
											compositeLabeling.setLegalName(aggregateRule.getLabel());
											compositeLabeling
													.setDeclarationType(DeclarationType.valueOf(aggregateRule.getLabelingRuleType().toString()));
											if (logger.isTraceEnabled()) {
												logger.trace("Create new aggregate group :" + compositeLabeling.getLegalName(Locale.getDefault()));
											}

										}

										if (!toAdd.containsKey(aggregateRuleNodeRef)) {
											compositeLabeling.setQty(0d);
											compositeLabeling.setVolume(0d);
											toAdd.put(aggregateRuleNodeRef, compositeLabeling);
										}

										ReqCtrlListDataItem error = null;

										if ((component instanceof CompositeLabeling)
												&& DeclarationType.Declare.equals(((CompositeLabeling) component).getDeclarationType())) {
											for (AbstractLabelingComponent childComponent : ((CompositeLabeling) component).getIngList().values()) {
												Double subQty = null;
												Double subVolume = null;
												if ((qty != null) && (childComponent.getQty() != null)) {
													subQty = (childComponent.getQty() * qty) / ((CompositeLabeling) component).getQtyTotal();
												}

												if ((volume != null) && (childComponent.getVolume() != null)) {
													subVolume = (childComponent.getVolume() * volume)
															/ ((CompositeLabeling) component).getVolumeTotal();
												}

												error = appendToAggregate(childComponent, compositeLabeling, aggregateRule, subQty, subVolume,
														childComponent.getAllergens());
											}

										} else {
											error = appendToAggregate(component, compositeLabeling, aggregateRule, qty, volume, allergens);
										}

										if (error != null) {

											if ((parent.getNodeRef() != null) && nodeService.exists(parent.getNodeRef())) {
												if (!error.getSources().contains(parent.getNodeRef())) {
													error.getSources().add(parent.getNodeRef());
												}
											}
											if (logger.isDebugEnabled()) {
												logger.debug("Adding aggregate error " + error.toString());
											}
											labelingFormulaContext.getErrors().add(error);
										}

									}

									if (is100Perc) {
										if (logger.isTraceEnabled()) {
											logger.trace("Aggregate rule remove : " + getName(component));
										}
										iterator.remove();
										break;
									} else if (qty != null && component.getQty()!=null) {
										component.setQty(component.getQty() - qty);
										if (volume != null && component.getVolume()!=null) {
											component.setVolume(component.getVolume() - volume);
										}
									}

								}
							}
						}
					}

				}
			}

			for (AbstractLabelingComponent ing : toAdd.values()) {
				parent.add(ing);
			}

		}

	}

	private ReqCtrlListDataItem appendToAggregate(AbstractLabelingComponent component, CompositeLabeling compositeLabeling,
			AggregateRule aggregateRule, Double qty, Double volume, Set<NodeRef> allergens) {
		AbstractLabelingComponent current = compositeLabeling.getIngList().get(component.getNodeRef());
		boolean is100Perc = (aggregateRule.getQty() == null) || (aggregateRule.getQty() == 100d);

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
				current.setVolume(0d);
				if (logger.isTraceEnabled()) {
					logger.trace(" - Add new ing to aggregate  :" + getName(current));
				}

				if (current instanceof CompositeLabeling && DeclarationType.Group.equals(((CompositeLabeling) current).getDeclarationType())) {
					if (logger.isTraceEnabled()) {
						logger.trace(" - Downgrade group to Detail");
					}
					((CompositeLabeling) current).setDeclarationType(DeclarationType.Detail);
				}

				compositeLabeling.add(current);
			}
		}

		if (qty != null) {
			if (current != null) {

				if (current.getQty() != null) {
					if (logger.isTraceEnabled()) {
						logger.trace(" - Update aggregate ing qty  :" + getName(current) + " " + qty);
					}
					current.setQty(current.getQty() + qty);
				}
				if ((current.getVolume() != null) && (volume != null)) {
					if (logger.isTraceEnabled()) {
						logger.trace(" - Update aggregate ing volume  :" + getName(current) + " " + volume);
					}
					current.setVolume(current.getVolume() + volume);
				}

				current.getAllergens().addAll(allergens);

			}

			Double qtyTotal = compositeLabeling.getQtyTotal();
			qtyTotal += qty;
			compositeLabeling.setQtyTotal(qtyTotal);
			compositeLabeling.setQty(qtyTotal);

			if (volume != null) {
				Double volumeTotal = compositeLabeling.getVolumeTotal();
				volumeTotal += volume;
				compositeLabeling.setVolumeTotal(volumeTotal);
				compositeLabeling.setVolume(volume);
			}

		} else {
			if (current != null) {
				current.setQty(null);
				current.setVolume(null);
				String message = I18NUtil.getMessage("message.formulate.labelRule.error.nullIng", getName(current));

				return new ReqCtrlListDataItem(null, RequirementType.Forbidden, message, null, new ArrayList<NodeRef>(),
						RequirementDataType.Labelling);
			}
		}

		return null;
	}

	// Move Group at top
	private List<CompositeLabeling> reorderCompositeLabeling(CompositeLabeling current, boolean isFirst) {
		List<CompositeLabeling> ret = new LinkedList<>();

		for (Iterator<Map.Entry<NodeRef, AbstractLabelingComponent>> iterator = current.getIngList().entrySet().iterator(); iterator.hasNext();) {
			AbstractLabelingComponent component = iterator.next().getValue();
			if (component instanceof CompositeLabeling) {

				Double currQty = component.getQty();
				ret.addAll(reorderCompositeLabeling((CompositeLabeling) component, false));
				if (!Objects.equals(component.getQty(), currQty) && (component.getQty() == 0)) {
					iterator.remove();
				} else {
					if (!isFirst) {
						if (((CompositeLabeling) component).isGroup()) {

							logger.trace("Found misplaced group :" + getName(component));
							// Remove from current
							iterator.remove();

							ret.add((CompositeLabeling) component);

						}
					}
				}
			}

		}
		if (!isFirst) {

			if (!ret.isEmpty()) {

				double qtyTotalToremove = 0;
				double volumeTotalToremove = 0;

				for (CompositeLabeling childGroup : ret) {
					// Reset qty

					qtyTotalToremove += childGroup.getQty();
					childGroup.setQty((childGroup.getQty() * current.getQty()) / current.getQtyTotal());

					volumeTotalToremove += childGroup.getVolume();
					childGroup.setVolume((childGroup.getVolume() * current.getVolume()) / current.getVolumeTotal());

					if (logger.isTraceEnabled()) {
						logger.trace(" - Move child group to level n-1 :" + getName(childGroup) + " new qty " + childGroup.getQty() + " new vol "
								+ childGroup.getVolume());
					}

				}
				current.setQty(current.getQty() - ((qtyTotalToremove * current.getQty()) / current.getQtyTotal()));
				current.setQtyTotal(current.getQtyTotal() - qtyTotalToremove);

				current.setVolume(current.getVolume() - ((volumeTotalToremove * current.getVolume()) / current.getVolumeTotal()));
				current.setVolumeTotal(current.getVolumeTotal() - volumeTotalToremove);

			}

		} else {
			for (CompositeLabeling childGroup : ret) {
				current.add(childGroup);
			}
		}

		return ret;
	}

	private IngLabelingListDataItem getOrCreateILLDataItem(ProductData formulatedProduct, NodeRef key, MLText label, String log) {

		IngLabelingListDataItem ill = null;
		for (IngLabelingListDataItem tmp : formulatedProduct.getLabelingListView().getIngLabelingList()) {
			if (((tmp.getGrp() == null) && (key == null)) || ((tmp.getGrp() != null) && tmp.getGrp().equals(key))) {
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

		ill.setLogValue(log);

		return ill;

	}

	private CompositeLabeling visitCompositeLabeling(CompositeLabeling parent, Composite<CompoListDataItem> parentComposite,
			LabelingFormulaContext labelingFormulaContext, Double ratio, Double yield, Double recipeQtyUsed, boolean computeReconstitution)
			throws FormulateException {

		Map<String, ReqCtrlListDataItem> errors = new HashMap<>();

		for (Composite<CompoListDataItem> composite : parentComposite.getChildren()) {

			CompoListDataItem compoListDataItem = composite.getData();

			DeclarationType declarationType = getDeclarationType(compoListDataItem, null, labelingFormulaContext);

			if (!DeclarationType.Omit.equals(declarationType)) {

				
				List<AggregateRule> aggregateRules = getAggregateRules(composite, parentComposite.getChildren(), labelingFormulaContext);

				NodeRef productNodeRef = compoListDataItem.getProduct();
				ProductData productData = (ProductData) alfrescoRepository.findOne(productNodeRef);
				
				
				if (logger.isTraceEnabled()) {
					logger.info("#########  Parse :"+ productData.getName() );
					
					if ((aggregateRules != null) && !aggregateRules.isEmpty()) {
						logger.trace(aggregateRules.toString() + " match ");
					}
					
				}

				// Calculate qty
				Double qty = FormulationHelper.getQtyInKg(compoListDataItem);

				if ((qty != null) && !(productData instanceof LocalSemiFinishedProductData)) {
					qty *= FormulationHelper.getYield(compoListDataItem) / 100;
				}

				if (qty != null) {
					qty *= LabelingFormulaContext.PRECISION_FACTOR;
				}
				
				Double waterLost = 0d;
				if ((ingsCalculatingWithYield || labelingFormulaContext.isIngsLabelingWithYield()) && (qty != null) && (yield != null)
						&& (yield != 100d) && (recipeQtyUsed != null) && nodeService.hasAspect(productNodeRef, PLMModel.ASPECT_WATER)) {
					waterLost = (1 - (yield / 100d)) * recipeQtyUsed * LabelingFormulaContext.PRECISION_FACTOR;


					if (logger.isTraceEnabled()) {
						logger.trace("Detected water lost: " + waterLost + " for qty :" + qty);
					}
					
					labelingFormulaContext.getToApplyThresholdItems().add(productNodeRef);

					qty -= waterLost;
				}

				if ((qty != null) && (ratio != null)) {
					qty *= ratio;
					waterLost *= ratio;
				}

				// Calculate volume
				Double volume = FormulationHelper.getNetVolume(compoListDataItem, nodeService);
				if (volume == null) {
					volume = 0d;
				}

				if ((volume != null) && !(productData instanceof LocalSemiFinishedProductData)) {
					volume *= FormulationHelper.getYield(compoListDataItem) / 100;

				}

				if (volume != null) {
					volume *= LabelingFormulaContext.PRECISION_FACTOR;
				}

				if ((volume != null) && (ratio != null)) {
					volume *= ratio;
				}

				// Reconstitution
				if (nodeService.hasAspect(productNodeRef, PLMModel.ASPECT_RECONSTITUTABLE)) {
					Double reconstitionRate = (Double) nodeService.getProperty(productNodeRef, PLMModel.PROP_RECONSTITUTION_RATE);
					if (reconstitionRate != null) {
						NodeRef diluentNodeRef = associationService.getTargetAssoc(productNodeRef, PLMModel.ASSOC_DILUENT_REF);
						NodeRef targetNodeRef = associationService.getTargetAssoc(productNodeRef, PLMModel.ASSOC_TARGET_RECONSTITUTION_REF);
						if ((diluentNodeRef != null) && (targetNodeRef != null)) {
							if (logger.isTraceEnabled()) {
								logger.trace("Found reconstitution rate for " + productData.getName() + " (" + reconstitionRate + ")");
							}

							// Override declaration type
							declarationType = DeclarationType.DoNotDetails;

							labelingFormulaContext.getReconstituableDataItems()
									.add(new ReconstituableDataItem(productNodeRef, reconstitionRate,
											(Integer) nodeService.getProperty(productNodeRef, PLMModel.PROP_RECONSTITUTION_PRIORITY), diluentNodeRef,
											targetNodeRef));
							
							
							labelingFormulaContext.getToApplyThresholdItems().add(diluentNodeRef);
							labelingFormulaContext.getToApplyThresholdItems().add(targetNodeRef);

						} else {
							logger.warn("Diluent or Target ing is null for: " + productData.getName());
						}
					} else {
						logger.warn("No reconstitution rate on: " + productData.getName());
					}
				}

				boolean isMultiLevel = false;

				if (parent == null) {
					parent = new CompositeLabeling(productData);

					fillAllergens(parent, productData);

					parent.setQty(qty);
					parent.setVolume(volume);
					if (logger.isTraceEnabled()) {
						logger.trace(
								"+ Creating new group [" + parent.getLegalName(I18NUtil.getContentLocaleLang()) + "] qtyUsed: " + parent.getQty());
					}
				}

				CompositeLabeling compositeLabeling = parent;

				if (!DeclarationType.DoNotDeclare.equals(declarationType) || !aggregateRules.isEmpty()) {

					// MultiLevel only if detail or group
					if (!DeclarationType.DoNotDetails.equals(declarationType)
							&& ((productData instanceof SemiFinishedProductData) || (productData instanceof FinishedProductData))) {
						Composite<CompoListDataItem> sfComposite = CompositeHelper
								.getHierarchicalCompoList(productData.getCompoList(new VariantFilters<>()));

						for (Composite<CompoListDataItem> sfChild : sfComposite.getChildren()) {
							CompoListDataItem clone = sfChild.getData().clone();
							clone.setParent(compoListDataItem);
							sfChild.setData(clone);
							composite.addChild(sfChild);
						}
						isMultiLevel = true;
					}

					if (!DeclarationType.Declare.equals(declarationType) || !aggregateRules.isEmpty()) {

						AbstractLabelingComponent lc = parent.get(productData.getNodeRef());
						if ((lc != null) && (lc instanceof CompositeLabeling)) {
							compositeLabeling = (CompositeLabeling) lc;
							if (qty != null) {

								if (compositeLabeling.getQty() != null) {
									compositeLabeling.setQty(qty + compositeLabeling.getQty());
								}

								if (composite.isLeaf() && (compositeLabeling.getQtyTotal() != null)) {
									if (logger.isTraceEnabled()) {
										logger.trace("Add to totalQty: " + applyYield(qty + waterLost, yield, labelingFormulaContext) + " yield: "
												+ yield);

									}

									compositeLabeling.setQtyTotal(
											applyYield(qty + waterLost, yield, labelingFormulaContext) + compositeLabeling.getQtyTotal());
								}

							}

							if (volume != null) {
								if (compositeLabeling.getVolume() != null) {
									compositeLabeling.setVolume(volume + compositeLabeling.getVolume());
								}

								if (composite.isLeaf() && (compositeLabeling.getVolumeTotal() != null)) {
									compositeLabeling
											.setVolumeTotal(applyYield(volume, yield, labelingFormulaContext) + compositeLabeling.getVolumeTotal());
								}
							}

						} else {
							compositeLabeling = new CompositeLabeling(productData);

							fillAllergens(compositeLabeling, productData);

							compositeLabeling.setQty(qty);
							compositeLabeling.setVolume(volume);
							compositeLabeling.setDeclarationType(declarationType);
							if (composite.isLeaf()) {
								if (logger.isTraceEnabled()) {
									logger.trace("Set totalQty: " + applyYield(qty + waterLost, yield, labelingFormulaContext) + " yield: " + yield);

								}
								compositeLabeling.setQtyTotal(applyYield(qty + waterLost, yield, labelingFormulaContext));
								compositeLabeling.setVolumeTotal(applyYield(volume, yield, labelingFormulaContext));
							}

							parent.add(compositeLabeling);

							if (logger.isTraceEnabled()) {
								logger.trace(" - Add detailed labeling component : " + getName(compositeLabeling) + " qty: " + qty);
							}
						}
					}

				}

				if (!DeclarationType.DoNotDetails.equals(declarationType) && !DeclarationType.DoNotDeclare.equals(declarationType)) {

					if (!isMultiLevel && (productData.getIngList() != null) && !productData.getIngList().isEmpty()) {

						loadIngList(productData, CompositeHelper.getHierarchicalCompoList(productData.getIngList()), qty, volume,
								labelingFormulaContext, compoListDataItem, compositeLabeling, errors);

					}

					Double computedRatio = 1d;
					if (DeclarationType.Declare.equals(declarationType) && isMultiLevel && (qty != null)) {
						Double qtyTotal = FormulationHelper.getNetWeight(productData, FormulationHelper.DEFAULT_NET_WEIGHT);

						if ((qtyTotal != null) && (qtyTotal != 0d)) {
							computedRatio = qty / (qtyTotal * LabelingFormulaContext.PRECISION_FACTOR);
						}
						if (logger.isTraceEnabled()) {
							logger.trace("Declare ratio for :" + productData.getName() + " " + computedRatio + " total:" + qtyTotal + " qty:" + qty);
						}

					} else if (DeclarationType.Declare.equals(declarationType) && (productData instanceof LocalSemiFinishedProductData)) {
						computedRatio = ratio;
					}

					// Recur
					if (!composite.isLeaf()) {

						Double recurYield = yield;
						Double recurRecipeQtyUsed = recipeQtyUsed;
						if (!(productData instanceof LocalSemiFinishedProductData) && !DeclarationType.Group.equals(declarationType)) {
							recurYield = computeYield(productData);

							if ((yield != null) && (yield != 100d) && (recurYield != null)) {
								recurYield = recurYield * (yield / 100);
							}

							recurRecipeQtyUsed = productData.getRecipeQtyUsed();
						} else if(!DeclarationType.Declare.equals(declarationType)){
							recurYield = 100d;
						}

						if (logger.isTraceEnabled()) {
							logger.trace(" -- Recur call " + productData.getName() + " yield " + productData.getYield() + " ratio " + ratio);
							logger.trace(" -- Recur yield " + recurYield + " recur recipeQtyUsed " + recurRecipeQtyUsed);
						}

						visitCompositeLabeling(compositeLabeling, composite, labelingFormulaContext, computedRatio, recurYield, recurRecipeQtyUsed,
								!parent.equals(compositeLabeling));
					}
				}

				if (!(DeclarationType.Declare.equals(declarationType) && aggregateRules.isEmpty()
						&& ((productData instanceof LocalSemiFinishedProductData) || isMultiLevel))) {
					// Update parent qty
					if (qty != null) {

						if (logger.isTraceEnabled()) {
							logger.trace(
									"Add to parent totalQty: " + applyYield(qty + waterLost, yield, labelingFormulaContext) + " yield: " + yield);

						}

						parent.setQtyTotal(parent.getQtyTotal() + applyYield(qty + waterLost, yield, labelingFormulaContext));
					}

					if (volume != null) {
						parent.setVolumeTotal(parent.getVolumeTotal() + applyYield(volume, yield, labelingFormulaContext));
					}
				}

			}

		}

		if (computeReconstitution) {
			applyReconstitution(parent, labelingFormulaContext.getReconstituableDataItems());
			labelingFormulaContext.getReconstituableDataItems().clear();
		}

		return parent;
	}

	private void fillAllergens(CompositeLabeling compositeLabeling, ProductData productData) {
		for (AllergenListDataItem allergenListDataItem : productData.getAllergenList()) {

			if (allergenListDataItem.getVoluntary()) {
				if (AllergenType.Major.toString().equals(nodeService.getProperty(allergenListDataItem.getAllergen(), PLMModel.PROP_ALLERGEN_TYPE))) {
					compositeLabeling.getAllergens().add(allergenListDataItem.getAllergen());
				}
			}
		}

	}

	private Double computeYield(ProductData productData) {
		Double qtyUsed = productData.getRecipeQtyUsed();
		Double netWeight = productData.getNetWeight();

		if ((netWeight != null) && (qtyUsed != null) && (qtyUsed != 0d)) {
			return (100 * netWeight) / qtyUsed;
		}

		return 100d;
	}

	private Double applyYield(Double qty, Double yield, LabelingFormulaContext labelingFormulaContext) {
		if ((ingsCalculatingWithYield || labelingFormulaContext.isIngsLabelingWithYield()) && (qty != null) && (yield != null)) {
			return (qty * yield) / 100d;
		}
		return qty;
	}

	private String getName(AbstractLabelingComponent component) {
		if (component instanceof IngItem) {
			return ((IngItem) component).getCharactName();
		}
		return component.getName();
	}

	private void applyReconstitution(CompositeLabeling parent, List<ReconstituableDataItem> reconstituableDataItems) {

		if (!reconstituableDataItems.isEmpty()) {
			if (logger.isTraceEnabled()) {
				logger.trace(" Before reconstitution \n " + parent.toString());
			}

			reconstituableDataItems.stream().sorted((e1, e2) -> Integer.compare(e2.getPriority(), e1.getPriority())).forEach(reconstituableData -> {

				AbstractLabelingComponent productLabelItem = parent.get(reconstituableData.getProductNodeRef());
				if ((productLabelItem != null) && (productLabelItem.getQty() != null)) {

					AbstractLabelingComponent ingLabelItem = parent.get(reconstituableData.getDiluentIngNodeRef());

					if ((ingLabelItem != null) && (ingLabelItem.getQty() != null)) {

						BigDecimal rate = new BigDecimal(reconstituableData.getRate());
						BigDecimal productQty = new BigDecimal(productLabelItem.getQty());
						BigDecimal ingQty = new BigDecimal(ingLabelItem.getQty());
						BigDecimal ingVol = new BigDecimal(ingLabelItem.getVolume());
						BigDecimal productVol = new BigDecimal(productLabelItem.getVolume());

						BigDecimal diluentQty = productQty.multiply(rate).subtract(productQty);

						BigDecimal realDiluentQty = ingQty.min(diluentQty);

						BigDecimal realDiluentQtyRatio = diluentQty.equals(BigDecimal.ZERO) ? (new BigDecimal(1d))
								: realDiluentQty.divide(diluentQty, 10, BigDecimal.ROUND_HALF_UP);

						BigDecimal realQty = realDiluentQty.add(productQty.multiply(realDiluentQtyRatio)).divide(rate, 10, BigDecimal.ROUND_HALF_UP);

						ingLabelItem.setQty(ingQty.subtract(realDiluentQty).doubleValue());
						productLabelItem.setQty(productQty.subtract(realQty).doubleValue());

						BigDecimal diluentVolume = rate.multiply(productVol).subtract(productVol);

						BigDecimal readlDiluentvolume = ingVol.min(diluentVolume);
						BigDecimal readlDiluentvolumeRatio = diluentVolume.equals(BigDecimal.ZERO) ? (new BigDecimal(1d))
								: readlDiluentvolume.divide(diluentQty, 10, BigDecimal.ROUND_HALF_UP);

						BigDecimal realVol = readlDiluentvolume.add(productVol.multiply(readlDiluentvolumeRatio)).divide(rate, 10,
								BigDecimal.ROUND_HALF_UP);

						ingLabelItem.setVolume(ingVol.subtract(readlDiluentvolume).doubleValue());
						productLabelItem.setVolume(productVol.subtract(realVol).doubleValue());

						IngItem targetLabelItem = (IngItem) parent.get(reconstituableData.getTargetIngNodeRef());
						if (targetLabelItem == null) {
							targetLabelItem = new IngItem((IngItem) alfrescoRepository.findOne(reconstituableData.getTargetIngNodeRef()));
							targetLabelItem.setQty(0d);
							targetLabelItem.setVolume(0d);
							parent.add(targetLabelItem);
						}

						targetLabelItem.setQty(new BigDecimal(targetLabelItem.getQty()).add(realQty).add(realDiluentQty).doubleValue());
						targetLabelItem.setVolume(new BigDecimal(targetLabelItem.getVolume()).add(realVol).add(readlDiluentvolume).doubleValue());

						if (logger.isTraceEnabled()) {
							logger.trace("Applying reconstitution:" + getName(productLabelItem) + " with " + getName(ingLabelItem) + " to "
									+ getName(targetLabelItem));
							logger.trace(" - rate: " + reconstituableData.getRate());
							logger.trace(" - diluentQty: " + diluentQty.doubleValue());
							logger.trace(" - realDiluentQty: " + realDiluentQty.doubleValue());
							logger.trace(" - realDiluentQtyRatio: " + realDiluentQtyRatio.doubleValue());
							logger.trace(" - realQty: " + realQty.doubleValue());
							logger.trace(" - diluent quantity: " + ingLabelItem.getQty());
							logger.trace(" - orig quantity: " + productLabelItem.getQty());
							logger.trace(" - new quantity: " + targetLabelItem.getQty());
						}

						Double TRESHOLD = 0.00001d;

						if (targetLabelItem.getQty() < TRESHOLD) {
							parent.getIngList().remove(targetLabelItem.getNodeRef());
						}

						if (productLabelItem.getQty() < TRESHOLD) {
							parent.getIngList().remove(productLabelItem.getNodeRef());
						}

						if (ingLabelItem.getQty() < TRESHOLD) {
							parent.getIngList().remove(ingLabelItem.getNodeRef());
						}

					}
				} else {
					logger.warn("No productLabelItem found for reconstituable: " + reconstituableData.toString());
				}
			});
		}

	}

	private List<AggregateRule> getAggregateRules(Composite<CompoListDataItem> component, List<Composite<CompoListDataItem>> brothers,
			LabelingFormulaContext labelingFormulaContext) {
		if (labelingFormulaContext.getAggregateRules().containsKey(component.getData().getProduct())) {
			return labelingFormulaContext.getAggregateRules().get(component.getData().getProduct()).stream()
					.filter(r -> r.matchAll(brothers) && !LabelingRuleType.Type.equals(r.getLabelingRuleType())).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	private void loadIngList(ProductData product, Composite<IngListDataItem> compositeIngList, Double qty, Double volume,
			LabelingFormulaContext labelingFormulaContext, CompoListDataItem compoListDataItem, CompositeLabeling compositeLabeling,
			Map<String, ReqCtrlListDataItem> errors) {
		
		boolean applyThreshold = false;
		if(nodeService.hasAspect(product.getNodeRef(), PLMModel.ASPECT_WATER)) {
			applyThreshold = true;
		}
		

		for (Composite<IngListDataItem> ingListItem : compositeIngList.getChildren()) {

			DeclarationType ingDeclarationType = getDeclarationType(compoListDataItem, ingListItem.getData(), labelingFormulaContext);

			if (!DeclarationType.Omit.equals(ingDeclarationType) && !DeclarationType.DoNotDeclare.equals(ingDeclarationType)) {

				NodeRef ingNodeRef = ingListItem.getData().getIng();
				IngItem ingLabelItem = (IngItem) compositeLabeling.get(ingNodeRef);
				boolean isNew = true;

				if (ingLabelItem == null) {
					ingLabelItem = new IngItem((IngItem) alfrescoRepository.findOne(ingNodeRef));

					compositeLabeling.add(ingLabelItem);
					
					if(applyThreshold){
						labelingFormulaContext.getToApplyThresholdItems().add(ingNodeRef);
					}

					if (logger.isTraceEnabled()) {
						logger.trace("- Add new ing " + getName(ingLabelItem) + " to current Label " + getName(compositeLabeling));
					}

				} else if (logger.isDebugEnabled()) {
					logger.trace("- Update ing value: " + ingLabelItem.getLegalName(I18NUtil.getContentLocaleLang()));
					isNew = false;
				}

				if (!ingListItem.isLeaf()) {
					// Only one level of subIngs
					for (Composite<IngListDataItem> subIngListItem : ingListItem.getChildren()) {

						DeclarationType subIngItemDeclarationType = getDeclarationType(compoListDataItem, subIngListItem.getData(),
								labelingFormulaContext);
						if (!DeclarationType.Omit.equals(subIngItemDeclarationType)
								&& !DeclarationType.DoNotDeclare.equals(subIngItemDeclarationType)) {

							IngItem subIngItem = new IngItem((IngItem) alfrescoRepository.findOne(subIngListItem.getData().getIng()));

							if (subIngListItem.getData().getQtyPerc() != null) {
								subIngItem.setQty(subIngListItem.getData().getQtyPerc() / 100);
								subIngItem.setVolume(subIngListItem.getData().getQtyPerc() / 100);
							} else {
								subIngItem.setQty(null);
								subIngItem.setVolume(null);
							}
							
							if (product.getAllergenList() != null) {
								for (AllergenListDataItem allergenListDataItem : product.getAllergenList()) {
									if (allergenListDataItem.getVoluntary() && allergenListDataItem.getVoluntarySources().contains(subIngItem.getNodeRef())) {
										if (AllergenType.Major.toString()
												.equals(nodeService.getProperty(allergenListDataItem.getAllergen(), PLMModel.PROP_ALLERGEN_TYPE))) {
											subIngItem.getAllergens().add(allergenListDataItem.getAllergen());
										}
									}
								}
							}
							

							if (ingLabelItem.getSubIngs().stream()
									.filter(i -> (labelingFormulaContext.getLegalIngName(i) != null)
											&& labelingFormulaContext.getLegalIngName(i).equals(labelingFormulaContext.getLegalIngName(subIngItem)))
									.count() < 1) {
								logger.trace("Adding subIng: " + subIngItem.getCharactName());
								ingLabelItem.getSubIngs().add(subIngItem);
							} else {
								logger.trace("Merge subIng: " + subIngItem.getCharactName());
								ingLabelItem.getSubIngs().stream().filter(i -> (labelingFormulaContext.getLegalIngName(i) != null)
										&& labelingFormulaContext.getLegalIngName(i).equals(labelingFormulaContext.getLegalIngName(subIngItem)))

										.forEach(i -> {
											if ((i.getQty() != null) && (subIngItem.getQty() != null)) {
												i.setQty(i.getQty() + subIngItem.getQty());
											} else {
												// TODO add warning
												i.setQty(null);
											}
											if ((i.getVolume() != null) && (subIngItem.getVolume() != null)) {
												i.setVolume(i.getVolume() + subIngItem.getVolume());
											} else {
												// TODO add warning
												i.setVolume(null);
											}
											
											i.getAllergens().addAll(subIngItem.getAllergens());

										});
							}
						}

					}
				}

				if (product.getAllergenList() != null) {
					for (AllergenListDataItem allergenListDataItem : product.getAllergenList()) {
						if (allergenListDataItem.getVoluntary() && allergenListDataItem.getVoluntarySources().contains(ingNodeRef)) {
							if (AllergenType.Major.toString()
									.equals(nodeService.getProperty(allergenListDataItem.getAllergen(), PLMModel.PROP_ALLERGEN_TYPE))) {
								ingLabelItem.getAllergens().add(allergenListDataItem.getAllergen());
							}
						}
					}
				}

				Double qtyPerc = ingListItem.getData().getQtyPerc();

				if (qtyPerc == null) {

					String message = I18NUtil.getMessage("message.formulate.labelRule.error.nullIng", getName(ingLabelItem));
					ReqCtrlListDataItem error = errors.get(message);
					if (error != null) {
						if (!error.getSources().contains(product.getNodeRef())) {
							error.getSources().add(product.getNodeRef());
						}
					} else {
						if (logger.isDebugEnabled()) {
							logger.debug("Store error for future qtyPerc is null " + getName(ingLabelItem));
						}
						error = createError(ingLabelItem, product.getNodeRef());
						errors.put(message, error);
					}
					if ((ingLabelItem.getQty() != null) && !isNew) {
						if (logger.isDebugEnabled()) {
							logger.debug("Adding aggregate error " + error.toString());
						}
						labelingFormulaContext.getErrors().add(error);
					}

					ingLabelItem.setQty(null);
					ingLabelItem.setVolume(null);
				} else {

					// if one ingItem has null perc -> must be null
					if ((ingLabelItem.getQty() != null) && (qty != null)) {

						ingLabelItem.setQty(ingLabelItem.getQty() + ((qty * qtyPerc) / 100));
						if (logger.isTraceEnabled()) {
							logger.trace(" -- new qty to add to " + getName(ingLabelItem) + ": " + ((qty * qtyPerc) / 100));
						}

						if ((ingLabelItem.getVolume() != null) && (volume != null)) {
							ingLabelItem.setVolume(ingLabelItem.getVolume() + ((volume * qtyPerc) / 100));
							logger.trace(" -- new volume to add: " + ((volume * qtyPerc) / 100));
						}

					} else {

						String message = I18NUtil.getMessage("message.formulate.labelRule.error.nullIng", getName(ingLabelItem));
						ReqCtrlListDataItem error = errors.get(message);
						if (error != null) {
							if (qty == null) {
								if (!error.getSources().contains(product.getNodeRef())) {
									error.getSources().add(product.getNodeRef());
								}
							}
							if (!Objects.equals(ingLabelItem.getQty(), qty)) {
								if (logger.isDebugEnabled()) {
									logger.debug("Adding aggregate error " + error.toString());
								}
								labelingFormulaContext.getErrors().add(error);
							}
						} else {
							if (logger.isDebugEnabled()) {
								logger.debug("Store error for future");
							}
							errors.put(message, createError(ingLabelItem, product.getNodeRef()));
						}

						if (qty == null) {
							ingLabelItem.setQty(null);
							ingLabelItem.setVolume(null);
						}

					}

				}

			}

		}
	}

	private ReqCtrlListDataItem createError(AbstractLabelingComponent ingItem, NodeRef productNodeRef) {
		String message = I18NUtil.getMessage("message.formulate.labelRule.error.nullIng", getName(ingItem));
		List<NodeRef> sourceNodeRefs = new ArrayList<>();
		if (productNodeRef != null) {
			sourceNodeRefs.add(productNodeRef);
		}
		return new ReqCtrlListDataItem(null, RequirementType.Forbidden, message, null, sourceNodeRefs, RequirementDataType.Labelling);

	}

	private DeclarationType getDeclarationType(CompoListDataItem compoListDataItem, IngListDataItem ingListDataItem,
			LabelingFormulaContext labelingFormulaContext) {

		if (ingListDataItem == null) {
			if (labelingFormulaContext.getNodeDeclarationFilters().containsKey(compoListDataItem.getProduct())) {
				DeclarationFilter declarationFilter = labelingFormulaContext.getNodeDeclarationFilters().get(compoListDataItem.getProduct());
				if ((declarationFilter.getFormula() == null) || labelingFormulaContext.matchFormule(declarationFilter.getFormula(),
						new DeclarationFilterContext(compoListDataItem, ingListDataItem))) {

					if (logger.isTraceEnabled()) {
						logger.trace(" -- Found declType : " + declarationFilter.getDeclarationType() + " for "
								+ nodeService.getProperty(compoListDataItem.getProduct(), ContentModel.PROP_NAME));
					}
					return declarationFilter.getDeclarationType();
				}
			}

		} else {

			if (labelingFormulaContext.getNodeDeclarationFilters().containsKey(ingListDataItem.getIng())) {
				DeclarationFilter declarationFilter = labelingFormulaContext.getNodeDeclarationFilters().get(ingListDataItem.getIng());
				if ((declarationFilter.getFormula() == null) || labelingFormulaContext.matchFormule(declarationFilter.getFormula(),
						new DeclarationFilterContext(compoListDataItem, ingListDataItem))) {
					if (logger.isTraceEnabled()) {
						logger.trace(" -- Found declType : " + declarationFilter.getDeclarationType() + " for "
								+ nodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_CHARACT_NAME));
					}
					return declarationFilter.getDeclarationType();
				}
			}

			for (DeclarationFilter declarationFilter : labelingFormulaContext.getDeclarationFilters()) {
				if ((declarationFilter.getFormula() != null) && labelingFormulaContext.matchFormule(declarationFilter.getFormula(),
						new DeclarationFilterContext(compoListDataItem, ingListDataItem))) {
					if (logger.isTraceEnabled()) {
						logger.trace(" -- Found declType : " + declarationFilter.getDeclarationType() + " for "
								+ nodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_CHARACT_NAME));
					}
					return declarationFilter.getDeclarationType();
				}
			}

		}

		if (logger.isTraceEnabled()) {
			logger.trace(" -- Found declType : " + compoListDataItem.getDeclType() + " for default "
					+ nodeService.getProperty(compoListDataItem.getProduct(), ContentModel.PROP_NAME));
		}
		return compoListDataItem.getDeclType();
	}

}
