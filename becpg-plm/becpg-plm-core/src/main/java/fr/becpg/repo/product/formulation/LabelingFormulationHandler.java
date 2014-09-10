/*
 * 
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
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
import fr.becpg.repo.variant.filters.VariantFilters;

/**
 * @author matthieu
 */
public class LabelingFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(LabelingFormulationHandler.class);

	private NodeService nodeService;

	protected AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	private NodeService mlNodeService;

	private NodeRef tmpDiluentNodeRef = new NodeRef("ings", "tempDiluent", "0");

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setMlNodeService(NodeService mlNodeService) {
		this.mlNodeService = mlNodeService;
	}

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

		ExpressionParser parser = new SpelExpressionParser();
		StandardEvaluationContext dataContext = new StandardEvaluationContext(labelingFormulaContext);

		List<LabelingRuleListDataItem> labelingRuleLists = getLabelingRules(formulatedProduct);

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
					Expression exp = parser.parseExpression(SpelHelper.formatFormula(labelingRuleListDataItem.getFormula()));
					exp.getValue(dataContext, String.class);
				} else if (!LabelingRuleType.Render.equals(type)) {
					labelingFormulaContext.addRule(labelingRuleListDataItem.getNodeRef(), labelingRuleListDataItem.getName(),
							labelingRuleListDataItem.getComponents(), labelingRuleListDataItem.getReplacements(),
							labelingRuleListDataItem.getLabel(), labelingRuleListDataItem.getFormula(), type);
				}
			}
		}

		List<CompoListDataItem> compoList = formulatedProduct.getCompoList(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT);

		// Compute composite
		Composite<CompoListDataItem> compositeDefaultVariant = CompositeHelper.getHierarchicalCompoList(compoList);

		CompositeLabeling compositeLabeling = calculateILLV2(new CompositeLabeling(), compositeDefaultVariant, labelingFormulaContext, 1d);

		if (logger.isTraceEnabled()) {
			logger.trace(" Before aggrate \n " + compositeLabeling.toString());
		}

		applyAggregateRules(compositeLabeling, labelingFormulaContext);

		if (logger.isTraceEnabled()) {
			logger.trace(" Before reorder \n " + compositeLabeling.toString());
		}

		reorderCompositeLabeling(compositeLabeling, true);

		if (logger.isDebugEnabled()) {
			logger.debug("\n" + compositeLabeling.toString());
		}

		List<IngLabelingListDataItem> retainNodes = new ArrayList<IngLabelingListDataItem>();
		if (!compositeLabeling.getIngList().isEmpty()) {

			// Store results
			labelingFormulaContext.setCompositeLabeling(compositeLabeling);

			for (LabelingRuleListDataItem labelingRuleListDataItem : labelingRuleLists) {
				if (LabelingRuleType.Render.equals(labelingRuleListDataItem.getLabelingRuleType())
						&& Boolean.TRUE.equals(labelingRuleListDataItem.getIsActive())) {

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
							String message = I18NUtil.getMessage("message.formulate.labelRule.error", labelingRuleListDataItem.getName(),
									e.getLocalizedMessage());
							formulatedProduct.getCompoListView().getReqCtrlList()
									.add(new ReqCtrlListDataItem(null, RequirementType.Tolerated, message, new ArrayList<NodeRef>()));

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

		} else {
			// Keep Manual ingList
			for (IngLabelingListDataItem tmp : formulatedProduct.getLabelingListView().getIngLabelingList()) {
				if (tmp.getManualValue() != null && !tmp.getManualValue().isEmpty()) {
					tmp.setValue(null);
					retainNodes.add(tmp);
				}
			}

		}
		formulatedProduct.getCompoListView().getReqCtrlList().addAll(labelingFormulaContext.getErrors());

		if(formulatedProduct.getLabelingListView().getIngLabelingList()!=null){
			formulatedProduct.getLabelingListView().getIngLabelingList().retainAll(retainNodes);
		}

		return true;
	}

	private List<LabelingRuleListDataItem> getLabelingRules(ProductData formulatedProduct) {
		List<LabelingRuleListDataItem> ret = new ArrayList<>();
		if (formulatedProduct.getLabelingListView().getLabelingRuleList() != null) {
			ret.addAll(formulatedProduct.getLabelingListView().getLabelingRuleList());
			if (formulatedProduct.getEntityTpl() != null && formulatedProduct.getEntityTpl().getLabelingListView().getLabelingRuleList() != null) {
				for (LabelingRuleListDataItem modelLabelingRuleListDataItem : formulatedProduct.getEntityTpl().getLabelingListView()
						.getLabelingRuleList()) {
					if (!modelLabelingRuleListDataItem.isSynchronisable() && Boolean.TRUE.equals(modelLabelingRuleListDataItem.getIsActive())) {
						ret.add(modelLabelingRuleListDataItem);
					}
				}
			}
		

		if (formulatedProduct.getProductSpecifications() != null) {
			for (ProductSpecificationData productSpecificationData : formulatedProduct.getProductSpecifications()) {
				for (LabelingRuleListDataItem modelLabelingRuleListDataItem : productSpecificationData.getLabelingRuleList()) {
					if (Boolean.TRUE.equals(modelLabelingRuleListDataItem.getIsActive())) {
						ret.add(modelLabelingRuleListDataItem);
					}
				}
			}
		}
		}

		return ret;
	}

	private void copyTemplateLabelingRuleList(ProductData formulatedProduct) {
		if (formulatedProduct.getEntityTpl() != null) {
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

	private void applyAggregateRules(CompositeLabeling parent, LabelingFormulaContext labelingFormulaContext) {
		if (!labelingFormulaContext.getAggregateRules().isEmpty()) {

			Map<NodeRef, AbstractLabelingComponent> toAdd = new HashMap<>();

			for (Iterator<Map.Entry<NodeRef, AbstractLabelingComponent>> iterator = parent.getIngList().entrySet().iterator(); iterator.hasNext();) {
				AbstractLabelingComponent component = (AbstractLabelingComponent) iterator.next().getValue();

				// Recur
				if (component instanceof CompositeLabeling) {
					applyAggregateRules((CompositeLabeling) component, labelingFormulaContext);
				}

				if (labelingFormulaContext.getAggregateRules().containsKey(component.getNodeRef())) {
					for (AggregateRule aggregateRule : labelingFormulaContext.getAggregateRules().get(component.getNodeRef())) {

						// Create a new
						if (!LabelingRuleType.Type.equals(aggregateRule.getLabelingRuleType())) {

							NodeRef aggregateRuleNodeRef = aggregateRule.getKey();

							if (toAdd.containsKey(aggregateRuleNodeRef) || aggregateRule.matchAll(parent.getIngList().values())) {

								Double qty = component.getQty();
								Double qtyVolumePerc = component.getVolumeQtyPerc();

								boolean is100Perc = aggregateRule.getQty() == null || aggregateRule.getQty() == 100d;
								// Add ing to group
								if (qty != null && aggregateRule.getQty() != null) {
									qty = qty * aggregateRule.getQty() / 100;
								}

								// Replacement
								if (aggregateRule.getReplacement() != null
										&& LabelingRuleType.DoNotDetails.equals(aggregateRule.getLabelingRuleType())) {
									AbstractLabelingComponent current = toAdd.containsKey(aggregateRuleNodeRef) ? toAdd.get(aggregateRuleNodeRef)
											: parent.getIngList().get(aggregateRuleNodeRef);

									if (current == null) {

										RepositoryEntity replacement = alfrescoRepository.findOne(aggregateRule.getReplacement());
										if (replacement instanceof IngItem) {
											current = (IngItem) replacement;
											current.setQty(0d);
											current.setVolumeQtyPerc(null);
											toAdd.put(aggregateRuleNodeRef, current);
										}
									}
									if (qty != null) {
										current.setQty(current.getQty() + qty);
									}

									if (qtyVolumePerc != null && current.getVolumeQtyPerc() != null) {
										current.setVolumeQtyPerc(current.getVolumeQtyPerc() + qtyVolumePerc);
									}

								} else {

									CompositeLabeling compositeLabeling = (CompositeLabeling) (toAdd.containsKey(aggregateRuleNodeRef) ? toAdd
											.get(aggregateRuleNodeRef) : parent.getIngList().get(aggregateRuleNodeRef));
									if (compositeLabeling == null) {

										compositeLabeling = new CompositeLabeling();
										compositeLabeling.setNodeRef(aggregateRuleNodeRef);
										compositeLabeling.setName(aggregateRule.getName());
										compositeLabeling.setLegalName(aggregateRule.getLabel());
										compositeLabeling.setDeclarationType(DeclarationType.valueOf(aggregateRule.getLabelingRuleType().toString()));
										if (logger.isTraceEnabled()) {
											logger.trace("Create new aggregate group :" + compositeLabeling.getLegalName(Locale.getDefault()));
										}

										// Add to current list
										toAdd.put(aggregateRuleNodeRef, compositeLabeling);
									}

									ReqCtrlListDataItem error = null;

									if (component instanceof CompositeLabeling
											&& DeclarationType.Declare.equals(((CompositeLabeling) component).getDeclarationType())) {
										for (AbstractLabelingComponent childComponent : ((CompositeLabeling) component).getIngList().values()) {
											Double subQty = null;
											if (qty != null && childComponent.getQty() != null) {
												subQty = childComponent.getQty() * qty / ((CompositeLabeling) component).getQtyRMUsed();
											}

											error = appendToAggregate(childComponent, compositeLabeling, aggregateRule, subQty,
													childComponent.getVolumeQtyPerc());
										}

									} else {
										error = appendToAggregate(component, compositeLabeling, aggregateRule, qty, qtyVolumePerc);
									}

									if (error != null) {

										if (parent.getNodeRef() != null && nodeService.exists(parent.getNodeRef())) {
											error.getSources().add(parent.getNodeRef());
										}
										if (logger.isDebugEnabled()) {
											logger.debug("Adding aggregate error " + error.toString());
										}
										labelingFormulaContext.getErrors().add(error);
									}

								}

								if (is100Perc) {
									iterator.remove();
								} else if (qty != null) {
									component.setQty(component.getQty() - qty);
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
			AggregateRule aggregateRule, Double qty, Double qtyVolumePerc) {
		AbstractLabelingComponent current = compositeLabeling.getIngList().get(component.getNodeRef());
		boolean is100Perc = aggregateRule.getQty() == null || aggregateRule.getQty() == 100d;

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
				current.setVolumeQtyPerc(null);
				if (logger.isTraceEnabled()) {
					logger.trace(" - Add new ing to aggregate  :" + current.getName());
				}
				compositeLabeling.add(current);
			}
		}

		if (qty != null) {
			if (current != null && current.getQty() != null) {
				if (logger.isTraceEnabled()) {
					logger.trace(" - Update aggregate ing qty  :" + current.getName() + " " + qty);
				}
				current.setQty(current.getQty() + qty);
			}

			if (current != null && current.getVolumeQtyPerc() != null && qtyVolumePerc != null) {
				current.setVolumeQtyPerc(current.getVolumeQtyPerc() + qtyVolumePerc);
			} else if (current != null) {
				current.setVolumeQtyPerc(qtyVolumePerc);
			}

			Double qtyRMUsed = compositeLabeling.getQtyRMUsed();

			qtyRMUsed += qty;
			compositeLabeling.setQtyRMUsed(qtyRMUsed);
			compositeLabeling.setQty(qtyRMUsed);

			if (compositeLabeling.getVolumeQtyPerc() != null && qtyVolumePerc != null) {
				compositeLabeling.setVolumeQtyPerc(compositeLabeling.getVolumeQtyPerc() + qtyVolumePerc);
			} else {
				compositeLabeling.setVolumeQtyPerc(qtyVolumePerc);
			}

		} else {
			if (current != null) {
				current.setQty(null);
				String message = I18NUtil.getMessage("message.formulate.labelRule.error.nullIng", current.getName());
				
				
				
				return new ReqCtrlListDataItem(null, RequirementType.Forbidden, message, new ArrayList<NodeRef>());
			}
		}

		return null;

	}

	// Move Group at top
	private List<CompositeLabeling> reorderCompositeLabeling(CompositeLabeling current, boolean isFirst) {
		List<CompositeLabeling> ret = new LinkedList<>();

		for (Iterator<Map.Entry<NodeRef, AbstractLabelingComponent>> iterator = current.getIngList().entrySet().iterator(); iterator.hasNext();) {
			AbstractLabelingComponent component = (AbstractLabelingComponent) iterator.next().getValue();
			if (component instanceof CompositeLabeling) {

				Double currQty = component.getQty();
				ret.addAll(reorderCompositeLabeling((CompositeLabeling) component, false));
				if (component.getQty() != currQty && component.getQty() == 0) {
					iterator.remove();
				} else {
					if (!isFirst) {
						if (((CompositeLabeling) component).isGroup()) {

							logger.trace("Found misplaced group :" + component.getName());
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

				double qtyRmUsedToremove = 0;

				for (CompositeLabeling childGroup : ret) {
					// Reset qty
					qtyRmUsedToremove += childGroup.getQty();
					childGroup.setQty(childGroup.getQty() * current.getQty() / current.getQtyRMUsed());

					logger.trace(" - Move child group to level n-1 :" + childGroup.getName() + " new qty " + childGroup.getQty());

				}
				current.setQty(current.getQty() - (qtyRmUsedToremove * current.getQty() / current.getQtyRMUsed()));
				current.setQtyRMUsed(current.getQtyRMUsed() - qtyRmUsedToremove);
			}

		} else {
			for (CompositeLabeling childGroup : ret) {
				current.add(childGroup);
			}
		}

		return ret;
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

	private CompositeLabeling calculateILLV2(CompositeLabeling ret, Composite<CompoListDataItem> composite,
			LabelingFormulaContext labelingFormulaContext, Double ratio) throws FormulateException {

		// Compute vTotal
		Double totalVolumeUsed = 0d;

		// Look for diluent
		for (Composite<CompoListDataItem> component : composite.getChildren()) {
			DeclarationType declarationType = getDeclarationType(component.getData(), null, labelingFormulaContext);
			if (nodeService.hasAspect(component.getData().getProduct(), PLMModel.ASPECT_DILUENT)) {

				ProductData productData = (ProductData) alfrescoRepository.findOne(component.getData().getProduct());

				if (DeclarationType.Detail.equals(declarationType) || DeclarationType.Group.equals(declarationType)
						|| DeclarationType.DoNotDetails.equals(declarationType)) {
					createDiluentAggregateRule(labelingFormulaContext, productData.getNodeRef(), productData.getLegalName());
				} else if (!DeclarationType.Omit.equals(declarationType)) {
					IngItem ingItem = (IngItem) alfrescoRepository.findOne(productData.getIngList().get(0).getIng());
					createDiluentAggregateRule(labelingFormulaContext, productData.getIngList().get(0).getIng(), ingItem.getLegalName());
				}
			}
			QName type = nodeService.getType(component.getData().getProduct());
			if (type != null
					&& (type.isMatch(PLMModel.TYPE_RAWMATERIAL) || type.isMatch(PLMModel.TYPE_SEMIFINISHEDPRODUCT) || type
							.isMatch(PLMModel.TYPE_FINISHEDPRODUCT)) && declarationType != DeclarationType.Omit) {

				Double vol = (Double) component.getData().getVolume();
				if (vol != null) {
					totalVolumeUsed += vol;
				}
			}

		}
		
		Map<String,ReqCtrlListDataItem> errors = new HashMap<String, ReqCtrlListDataItem>();

		for (Composite<CompoListDataItem> component : composite.getChildren()) {

			DeclarationType declarationType = getDeclarationType(component.getData(), null, labelingFormulaContext);

			if (!DeclarationType.Omit.equals(declarationType)) {

				List<AggregateRule> aggregateRules = getAggregateRules(component, composite.getChildren(), labelingFormulaContext);

				if (aggregateRules != null && logger.isTraceEnabled()) {
					logger.trace("Aggregate rule " + aggregateRules.toString() + " match ");
				}

				calculateILLV2(ret, component, labelingFormulaContext, aggregateRules, declarationType, totalVolumeUsed, ratio, errors);

			}

		}
		return ret;
	}

	private List<AggregateRule> getAggregateRules(Composite<CompoListDataItem> component, List<Composite<CompoListDataItem>> brothers,
			LabelingFormulaContext labelingFormulaContext) {

		List<AggregateRule> ret = new LinkedList<>();
		if (labelingFormulaContext.getAggregateRules().containsKey(component.getData().getProduct())) {
			for (AggregateRule aggregateRule : labelingFormulaContext.getAggregateRules().get(component.getData().getProduct())) {
				if (aggregateRule.matchAll(brothers) && !LabelingRuleType.Type.equals(aggregateRule.getLabelingRuleType())) {
					ret.add(aggregateRule);
				}
			}
		}

		return ret;
	}

	private CompositeLabeling calculateILLV2(CompositeLabeling parent, Composite<CompoListDataItem> composite,
			LabelingFormulaContext labelingFormulaContext, List<AggregateRule> aggregateRules,
			DeclarationType declarationType,
			Double totalVolumeUsed,
			Double ratio,
			 Map<String,ReqCtrlListDataItem> errors
			)
			throws FormulateException {
		CompoListDataItem compoListDataItem = composite.getData();

		NodeRef productNodeRef = compoListDataItem.getProduct();
		ProductData productData = (ProductData) alfrescoRepository.findOne(productNodeRef);

		// Calculate qtyRMUsed
		Double qty = FormulationHelper.getQtyInKg(compoListDataItem);
		if (qty != null && !(productData instanceof LocalSemiFinishedProductData)) {
			qty *= FormulationHelper.getYield(compoListDataItem) / 100;
		}

		qty = qty * ratio;
		
		Double volumeReconstitution = FormulationHelper.getVolumeReconstitution(compoListDataItem, nodeService);
		Double volumePerc = null;
		if (volumeReconstitution != null) {
			Double diluentVolume = (volumeReconstitution - compoListDataItem.getVolume());
			qty += diluentVolume;
			updateDiluentQty(parent, diluentVolume);
			volumePerc = volumeReconstitution / totalVolumeUsed;
		}

		boolean isMultiLevel = false;

		if (parent == null) {
			parent = new CompositeLabeling(productData);
			parent.setQty(qty);
			parent.setVolumeQtyPerc(volumePerc);
			if (logger.isTraceEnabled()) {
				logger.trace("+ Creating new group [" + parent.getLegalName(I18NUtil.getContentLocaleLang()) + "] qtyUsed: " + parent.getQty());
			}
		}

		CompositeLabeling compositeLabeling = parent;

		if (DeclarationType.Detail.equals(declarationType) || DeclarationType.Group.equals(declarationType)
				|| DeclarationType.DoNotDetails.equals(declarationType) || DeclarationType.Declare.equals(declarationType)  || !aggregateRules.isEmpty()) {

			// MultiLevel only if detail or group
			if (!DeclarationType.DoNotDetails.equals(declarationType)
					&& (productData instanceof SemiFinishedProductData || productData instanceof FinishedProductData)) {
				@SuppressWarnings("unchecked")
				Composite<CompoListDataItem> sfComposite = CompositeHelper.getHierarchicalCompoList(productData.getCompoList(EffectiveFilters.ALL,
						VariantFilters.DEFAULT_VARIANT));
				for (Composite<CompoListDataItem> sfChild : sfComposite.getChildren()) {
					sfChild.getData().setParent(compoListDataItem);
					composite.addChild(sfChild);
				}
				isMultiLevel = true;
			}

			if(!DeclarationType.Declare.equals(declarationType) || !aggregateRules.isEmpty()){
				AbstractLabelingComponent lc = parent.get(productData.getNodeRef());
				if (lc != null && lc instanceof CompositeLabeling) {
					compositeLabeling = (CompositeLabeling) lc;
					if (qty != null) {
	
						if (compositeLabeling.getQty() != null) {
							compositeLabeling.setQty(qty + compositeLabeling.getQty());
						}
	
						if (composite.isLeaf() && compositeLabeling.getQtyRMUsed() != null) {
							compositeLabeling.setQtyRMUsed(qty + compositeLabeling.getQtyRMUsed());
						}
	
					}
	
					if (volumeReconstitution != null && compositeLabeling.getVolumeQtyPerc() != null) {
						compositeLabeling.setVolumeQtyPerc(volumePerc + compositeLabeling.getVolumeQtyPerc());
					}
				} else {
					compositeLabeling = new CompositeLabeling(productData);
					compositeLabeling.setQty(qty);
					compositeLabeling.setVolumeQtyPerc(volumePerc);
					compositeLabeling.setDeclarationType(declarationType);
					if (composite.isLeaf()) {
						compositeLabeling.setQtyRMUsed(qty);
					}
	
					parent.add(compositeLabeling);
	
					if (logger.isTraceEnabled()) {
						logger.trace(" - Add detailed labeling component : " + compositeLabeling.getName() + " qty: " + qty);
					}
				}
			} 
			

		} else if (DeclarationType.Omit.equals(declarationType)) {
			return parent;
		}

		if (!DeclarationType.DoNotDetails.equals(declarationType) && !DeclarationType.DoNotDeclare.equals(declarationType)) {

			if (!isMultiLevel && productData.getIngList() != null && !productData.getIngList().isEmpty()) {
				
				Composite<IngListDataItem> compositeIngList = CompositeHelper.getHierarchicalCompoList(productData.getIngList());
				loadIngList(productData.getNodeRef(), compositeIngList, qty, volumePerc, labelingFormulaContext, compoListDataItem, compositeLabeling, errors);

			}

			Double computedRatio = 1d;
			if(DeclarationType.Declare.equals(declarationType) && isMultiLevel && qty!=null){
				Double qtyRmUsed = computeQtyRMUsed(composite,labelingFormulaContext);
				if(qtyRmUsed!=null){
					computedRatio =  qty /  qtyRmUsed;
				}
				if(logger.isTraceEnabled()){
					logger.trace("Declare ratio for :"+productData.getName()+ " "+ computedRatio+ " rMUsed:" + qtyRmUsed+" qty:"+qty);
				}
				
			}
			
			// Recur
			if (!composite.isLeaf()) {
				calculateILLV2(compositeLabeling, composite, labelingFormulaContext, computedRatio);
			}
		}

		if (!(DeclarationType.Declare.equals(declarationType) && (productData instanceof LocalSemiFinishedProductData || isMultiLevel))) {
			// Update parent qty
			if (qty != null) {
				parent.setQtyRMUsed(parent.getQtyRMUsed() + qty);
			}
		}

		return parent;

	}

	private Double computeQtyRMUsed(Composite<CompoListDataItem> composite, LabelingFormulaContext labelingFormulaContext) {
		Double qtyRMUsed = 0d;
		
		for (Composite<CompoListDataItem> component : composite.getChildren()) {

			DeclarationType declarationType = getDeclarationType(component.getData(), null, labelingFormulaContext);

			if (!DeclarationType.Omit.equals(declarationType)) {

				CompoListDataItem compoListDataItem = component.getData();

				NodeRef productNodeRef = compoListDataItem.getProduct();
				ProductData productData = (ProductData) alfrescoRepository.findOne(productNodeRef);
				
				Double qty = FormulationHelper.getQtyInKg(compoListDataItem);
				if (qty != null && !(productData instanceof LocalSemiFinishedProductData)) {
					qty *= FormulationHelper.getYield(compoListDataItem) / 100;
				}
				
				if(qtyRMUsed!=null && qty!=null){
					qtyRMUsed+=qty;
				} else {
					qtyRMUsed = null;
				}
			}

		}
		return qtyRMUsed;
	}

	private void loadIngList(NodeRef productNodeRef, Composite<IngListDataItem> compositeIngList, Double qty, Double volumePerc,
			LabelingFormulaContext labelingFormulaContext, CompoListDataItem compoListDataItem, CompositeLabeling compositeLabeling, Map<String,ReqCtrlListDataItem> errors) {

		 
		for (Composite<IngListDataItem> ingListItem : compositeIngList.getChildren()) {

			DeclarationType ingDeclarationType = getDeclarationType(compoListDataItem, ingListItem.getData(), labelingFormulaContext);

			if (!DeclarationType.Omit.equals(ingDeclarationType) && !DeclarationType.DoNotDeclare.equals(ingDeclarationType)) {

				NodeRef ingNodeRef = ingListItem.getData().getIng();
				IngItem ingLabelItem = (compositeLabeling.get(ingNodeRef) instanceof IngItem) ? (IngItem) compositeLabeling.get(ingNodeRef) : null;
				boolean isNew = true;

				if (ingLabelItem == null) {
					ingLabelItem = (IngItem) alfrescoRepository.findOne(ingNodeRef);

					if (!ingListItem.isLeaf()) {

						if (qty != null && ingListItem.getData().getQtyPerc() != null) {
							qty *= ingListItem.getData().getQtyPerc() / 100;
						} else {
							qty = null;
						}
						CompositeLabeling c = new CompositeLabeling();

						if (ingListItem.getData().getVolumeQtyPerc() != null) {
							c.setVolumeQtyPerc(ingListItem.getData().getVolumeQtyPerc() / 100);
						} else if (volumePerc != null && ingListItem.getData().getQtyPerc() != null) {
							c.setVolumeQtyPerc(volumePerc * ingListItem.getData().getQtyPerc() / 100);
						} else {
							c.setVolumeQtyPerc(volumePerc);
						}
						c.setNodeRef(ingLabelItem.getNodeRef());
						c.setName(ingLabelItem.getName());
						c.setLegalName(ingLabelItem.getLegalName());
						c.setDeclarationType(DeclarationType.Detail);
						c.setQty(qty);
						compositeLabeling.add(c);
						loadIngList(productNodeRef, ingListItem, qty, volumePerc, labelingFormulaContext, compoListDataItem, c, new HashMap<String, ReqCtrlListDataItem>());
					} else {
						compositeLabeling.add(ingLabelItem);
					}

					if (logger.isTraceEnabled()) {
						logger.trace("- Add new ing to current Label: " + ingLabelItem.getLegalName(I18NUtil.getContentLocaleLang()));
					}
					
					
				} else if (logger.isDebugEnabled()) {
					logger.trace("- Update ing value: " + ingLabelItem.getLegalName(I18NUtil.getContentLocaleLang()));
					isNew = false;
				}

				Double qtyPerc = ingListItem.getData().getQtyPerc();
				
				if (qtyPerc == null) {
					
					String message = I18NUtil.getMessage("message.formulate.labelRule.error.nullIng", ingLabelItem.getName());
					ReqCtrlListDataItem error = errors.get(message);
					if(error !=null){
						error.getSources().add(productNodeRef);
					} else {
						if (logger.isDebugEnabled()) {
							logger.debug("Store error for future qtyPerc is null "+ingLabelItem.getName());
						}
						error =  createError(ingLabelItem, productNodeRef);
						errors.put(message, error);
					}
					if(ingLabelItem.getQty()!=null && ! isNew){
						if (logger.isDebugEnabled()) {
							logger.debug("Adding aggregate error " + error.toString());
						}
						labelingFormulaContext.getErrors().add(error);
					}
					
					ingLabelItem.setQty(null);
					ingLabelItem.setVolumeQtyPerc(null);
				} else {
					// if one ingItem has null perc -> must be null
					if (ingLabelItem.getQty() != null && qty != null) {

						Double totalQtyIng = ingLabelItem.getQty();

						Double valueToAdd = qty * qtyPerc / 100;

						totalQtyIng += valueToAdd;
						ingLabelItem.setQty(totalQtyIng);

						if (ingListItem.getData().getVolumeQtyPerc() != null) {
							ingLabelItem.setVolumeQtyPerc(ingListItem.getData().getVolumeQtyPerc() / 100);
						} else if (ingLabelItem.getVolumeQtyPerc() != null && volumePerc != null) {
							ingLabelItem.setVolumeQtyPerc(volumePerc + ingLabelItem.getVolumeQtyPerc());
						} else {
							ingLabelItem.setVolumeQtyPerc(volumePerc);
						}

						if (logger.isTraceEnabled()) {
							logger.trace(" -- new qty to add :" + valueToAdd);
							logger.trace(" -- new volumePerc to add :" + volumePerc);
						}

					} else {
						
						
						String message = I18NUtil.getMessage("message.formulate.labelRule.error.nullIng", ingLabelItem.getName());
						ReqCtrlListDataItem error = errors.get(message);
						if(error !=null){
							if(qty==null){
								error.getSources().add(productNodeRef);
							}
							if(ingLabelItem.getQty() != qty){
								if (logger.isDebugEnabled()) {
									logger.debug("Adding aggregate error " + error.toString());
								}
								labelingFormulaContext.getErrors().add(error);
							}
						} else {
							if (logger.isDebugEnabled()) {
								logger.debug("Store error for future");
							}
							errors.put(message, createError(ingLabelItem, productNodeRef));
						}
						
						
						
						if(qty==null){
							ingLabelItem.setQty(null);
							ingLabelItem.setVolumeQtyPerc(null);
						}
						
					}

				}
				
			}

		}
	}

	private ReqCtrlListDataItem createError(IngItem ingItem, NodeRef productNodeRef) {
		String message = I18NUtil.getMessage("message.formulate.labelRule.error.nullIng", ingItem.getName());
		List<NodeRef> sourceNodeRefs =  new ArrayList<NodeRef>();
		if(productNodeRef!=null){
			sourceNodeRefs.add(productNodeRef);
		}
		return new ReqCtrlListDataItem(null, RequirementType.Forbidden, message, sourceNodeRefs);
		
	}

	private void updateDiluentQty(CompositeLabeling compositeLabeling, Double diluentVolume) {
		CompositeLabeling diluent = (CompositeLabeling) compositeLabeling.get(tmpDiluentNodeRef);
		if (diluent == null) {
			diluent = new CompositeLabeling();
			diluent.setNodeRef(tmpDiluentNodeRef);
			diluent.setName("diluent");
			diluent.setDeclarationType(DeclarationType.Detail);
			diluent.setQty(0d);
			compositeLabeling.add(diluent);

		}
		if (logger.isTraceEnabled()) {
			logger.trace(" -- new diluentVolume to add :" + diluentVolume);
		}
		diluent.setQty(diluent.getQty() - diluentVolume);
		compositeLabeling.setQtyRMUsed(compositeLabeling.getQtyRMUsed() - diluentVolume);

	}

	private void createDiluentAggregateRule(LabelingFormulaContext labelingFormulaContext, NodeRef diluentNodeRef, MLText name) {
		if (!labelingFormulaContext.getAggregateRules().containsKey(diluentNodeRef)) {
			labelingFormulaContext.addRule(diluentNodeRef, "diluentRule", Arrays.asList(diluentNodeRef, tmpDiluentNodeRef), null, name, null,
					LabelingRuleType.DoNotDetails);
		}

		if (logger.isTraceEnabled()) {
			logger.trace(" - Found diluent : " + diluentNodeRef);
		}
	}

	private DeclarationType getDeclarationType(CompoListDataItem compoListDataItem, IngListDataItem ingListDataItem,
			LabelingFormulaContext labelingFormulaContext) {

		if (ingListDataItem == null) {
			if (labelingFormulaContext.getNodeDeclarationFilters().containsKey(compoListDataItem.getProduct())) {
				DeclarationFilter declarationFilter = labelingFormulaContext.getNodeDeclarationFilters().get(compoListDataItem.getProduct());
				if (declarationFilter.getFormula() == null
						|| labelingFormulaContext.matchFormule(declarationFilter.getFormula(), new DeclarationFilterContext(compoListDataItem,
								ingListDataItem))) {

					if (logger.isTraceEnabled()) {
						logger.trace(" -- Found declType : " + declarationFilter.getDeclarationType() + " for "
								+ getName(compoListDataItem.getProduct()));
					}
					return declarationFilter.getDeclarationType();
				}
			}

		} else {

			if (labelingFormulaContext.getNodeDeclarationFilters().containsKey(ingListDataItem.getIng())) {
				DeclarationFilter declarationFilter = labelingFormulaContext.getNodeDeclarationFilters().get(ingListDataItem.getIng());
				if (declarationFilter.getFormula() == null
						|| labelingFormulaContext.matchFormule(declarationFilter.getFormula(), new DeclarationFilterContext(compoListDataItem,
								ingListDataItem))) {
					if (logger.isTraceEnabled()) {
						logger.trace(" -- Found declType : " + declarationFilter.getDeclarationType() + " for " + getName(ingListDataItem.getIng()));
					}
					return declarationFilter.getDeclarationType();
				}
			}

			for (DeclarationFilter declarationFilter : labelingFormulaContext.getDeclarationFilters()) {
				if (declarationFilter.getFormula() != null
						&& labelingFormulaContext.matchFormule(declarationFilter.getFormula(), new DeclarationFilterContext(compoListDataItem,
								ingListDataItem))) {
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
