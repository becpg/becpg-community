/*
 *
 */
package fr.becpg.repo.product.formulation.labeling;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

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

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.GS1Model;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.formulation.spel.SpelFormulaService;
import fr.becpg.repo.formulation.spel.SpelHelper;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.LargeTextHelper;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.AllergenType;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.LabelingRuleType;
import fr.becpg.repo.product.data.constraints.PlaceOfActivityTypeCode;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.ing.CompositeLabeling;
import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.ing.LabelingComponent;
import fr.becpg.repo.product.data.meat.MeatContentData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.data.spel.LabelingFormulaFilterContext;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.system.SystemConfigurationService;
import fr.becpg.repo.variant.filters.VariantFilters;

/**
 * <p>
 * LabelingFormulationHandler class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class LabelingFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(LabelingFormulationHandler.class);

	private static final String NULL_ING_ERROR = "message.formulate.labelRule.error.nullIng";

	private NodeService nodeService;

	protected AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	private NodeService mlNodeService;

	private AssociationService associationService;

	private SpelFormulaService formulaService;

	private SystemConfigurationService systemConfigurationService;

	public void setSystemConfigurationService(SystemConfigurationService systemConfigurationService) {
		this.systemConfigurationService = systemConfigurationService;
	}

	private boolean ingsCalculatingWithYield() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.formulation.ingsCalculatingWithYield"));
	}

	/**
	 * <p>
	 * Setter for the field <code>nodeService</code>.
	 * </p>
	 *
	 * @param nodeService
	 *            a {@link org.alfresco.service.cmr.repository.NodeService}
	 *            object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>
	 * Setter for the field <code>alfrescoRepository</code>.
	 * </p>
	 *
	 * @param alfrescoRepository
	 *            a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/**
	 * <p>
	 * Setter for the field <code>mlNodeService</code>.
	 * </p>
	 *
	 * @param mlNodeService
	 *            a {@link org.alfresco.service.cmr.repository.NodeService}
	 *            object.
	 */
	public void setMlNodeService(NodeService mlNodeService) {
		this.mlNodeService = mlNodeService;
	}

	/**
	 * <p>
	 * Setter for the field <code>associationService</code>.
	 * </p>
	 *
	 * @param associationService
	 *            a {@link fr.becpg.repo.helper.AssociationService} object.
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	/**
	 * <p>
	 * Setter for the field <code>formulaService</code>.
	 *
	 * @param formulaService
	 *            a {@link fr.becpg.repo.formulation.spel.SpelFormulaService}
	 *            object.
	 */
	public void setFormulaService(SpelFormulaService formulaService) {
		this.formulaService = formulaService;
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData formulatedProduct) {

		if ((formulatedProduct.getReformulateCount() != null)
				&& !formulatedProduct.getReformulateCount().equals(formulatedProduct.getCurrentReformulateCount())) {
			logger.debug("Skip labeling in reformulateCount " + formulatedProduct.getCurrentReformulateCount());
			return true;
		}

		// no compo => no formulation
		if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) || ((formulatedProduct.getLabelingListView() == null)
				&& !alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_INGLABELINGLIST))) {
			logger.debug("no labelingListView => no formulation - " + formulatedProduct.getName());
			return true;
		}

		copyTemplateLabelingRuleList(formulatedProduct);

		Map<String, List<LabelingRuleListDataItem>> labelingRuleListsByGroup = getLabelingRules(formulatedProduct);

		List<IngLabelingListDataItem> retainNodes = new ArrayList<>();

		boolean shouldSkip = true;

		// Keep Manual ingList
		if (formulatedProduct.getLabelingListView().getIngLabelingList() != null) {
			for (IngLabelingListDataItem tmp : formulatedProduct.getLabelingListView().getIngLabelingList()) {
				boolean isEmpty = true;

				if ((tmp.getManualValue() != null) && !tmp.getManualValue().isEmpty()) {
					for (String trad : tmp.getManualValue().values()) {
						if ((trad != null) && !trad.isEmpty()) {
							isEmpty = false;
						}
					}

					if (!isEmpty) {
						tmp.setValue(null);
						retainNodes.add(tmp);
					}
				}

				if (isEmpty) {
					tmp.setManualValue(null);
				}
			}
		}

		int sortOrder = 0;

		for (Map.Entry<String, List<LabelingRuleListDataItem>> labelingRuleListsGroup : labelingRuleListsByGroup.entrySet()) {

			logger.debug("Calculate Ingredient Labeling for group : " + labelingRuleListsGroup.getKey() + " - " + formulatedProduct.getName());

			LabelingFormulaContext labelingFormulaContext = new LabelingFormulaContext(mlNodeService, associationService, alfrescoRepository,
					formulaService);

			labelingFormulaContext.setIngsLabelingWithYield(ingsCalculatingWithYield());

			ExpressionParser parser = new SpelExpressionParser();
			StandardEvaluationContext dataContext = formulaService.createCustomSpelContext(formulatedProduct, labelingFormulaContext);

			List<LabelingRuleListDataItem> labelingRuleLists = labelingRuleListsGroup.getValue();

			// Apply Prefs first
			Comparator<LabelingRuleListDataItem> customComparator = (item1, item2) -> {
				LabelingRuleType type1 = item1.getLabelingRuleType();
				LabelingRuleType type2 = item2.getLabelingRuleType();

				// Compare based on the desired order
				if (LabelingRuleType.Prefs.equals(type1) && !LabelingRuleType.Prefs.equals(type2)) {
					return -1; // item1 comes before item2
				} else if (!LabelingRuleType.Prefs.equals(type1) && LabelingRuleType.Prefs.equals(type2)) {
					return 1; // item2 comes before item1
				} else {
					return 0; // They have the same type or neither is Prefs
				}
			};

			// Sort the list using the custom comparator
			Collections.sort(labelingRuleLists, customComparator);

			// Apply before formula
			int groupSortOrder = 0;
			for (LabelingRuleListDataItem labelingRuleListDataItem : labelingRuleLists) {
				if (Boolean.TRUE.equals(labelingRuleListDataItem.getIsActive())) {
					LabelingRuleType type = labelingRuleListDataItem.getLabelingRuleType();
					if (LabelingRuleType.Prefs.equals(type)) {
						try {
							Expression exp = parser.parseExpression(SpelHelper.formatFormula(labelingRuleListDataItem.getFormula()));
							exp.getValue(dataContext, String.class);

						} catch (Exception e) {
							formulatedProduct.getReqCtrlList()
									.add(new ReqCtrlListDataItem(
											null, RequirementType.Tolerated, MLTextHelper.getI18NMessage("message.formulate.labelRule.error",
													labelingRuleListDataItem.getName(), e.getLocalizedMessage()),
											null, new ArrayList<>(), RequirementDataType.Labelling));
							if (logger.isDebugEnabled()) {
								logger.warn("Error label rule formula : [" + labelingRuleListDataItem.getName() + "] - "
										+ SpelHelper.formatFormula(labelingRuleListDataItem.getFormula()), e);
							}
						}
					} else if (!LabelingRuleType.Render.equals(type)) {
						labelingFormulaContext.addRule(labelingRuleListDataItem.getNodeRef(), labelingRuleListDataItem.getName(),
								labelingRuleListDataItem.getComponents(), labelingRuleListDataItem.getReplacements(),
								labelingRuleListDataItem.getLabel(), labelingRuleListDataItem.getFormula(), type,
								labelingRuleListDataItem.getLocales(), groupSortOrder++);

					} else {
						shouldSkip = false;
					}

				}
			}

			if (shouldSkip) {
				logger.debug("No render rule for " + formulatedProduct.getName() + " skipping ");
				continue;
			}

			List<CompoListDataItem> compoList = new ArrayList<>();

			if ((formulatedProduct.isGeneric()) || Boolean.TRUE.equals(formulatedProduct.getIsIngListManual())
					|| !formulatedProduct.hasCompoListEl()) {
				compoList.add(new CompoListDataItem(null, null, 1d, 1d, ProductUnit.kg, 0d, DeclarationType.Declare, formulatedProduct.getNodeRef()));
			} else {
				compoList = formulatedProduct.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()));
			}

			// Compute composite
			Composite<CompoListDataItem> compositeDefaultVariant = CompositeHelper.getHierarchicalCompoList(compoList);

			CompositeLabeling compositeLabeling = new CompositeLabeling(CompositeLabeling.ROOT);

			visitCompoList(compositeLabeling, compositeDefaultVariant, labelingFormulaContext, BigDecimal.valueOf(1d),
					labelingFormulaContext.getYield() != null ? labelingFormulaContext.getYield()
							: (labelingFormulaContext.isUseSecondaryYield() ? formulatedProduct.getSecondaryYield() : formulatedProduct.getYield()),
					true);

			if (logger.isTraceEnabled()) {
				logger.trace(" Before aggrate \n " + compositeLabeling.toString());
			}

			applyAggregateRules(compositeLabeling, labelingFormulaContext, true, false);

			if (logger.isTraceEnabled()) {
				logger.trace(" Before aggregate legalName \n " + compositeLabeling.toString());
			}

			aggregateLegalName(compositeLabeling, labelingFormulaContext, true);

			mergeAtEndLabeling(compositeLabeling, labelingFormulaContext, new HashMap<>(), true);

			if (logger.isTraceEnabled()) {
				logger.trace(" Before reorder \n " + compositeLabeling.toString());
			}

			applyMeatContentRules(formulatedProduct, compositeLabeling, labelingFormulaContext);

			reorderCompositeLabeling(labelingFormulaContext, compositeLabeling, true);

			if (logger.isDebugEnabled()) {
				logger.debug("\n" + compositeLabeling.toString());
			}

			if (!compositeLabeling.getIngList().isEmpty()) {

				CompositeLabeling mergeCompositeLabeling = mergeCompositeLabeling(compositeLabeling, labelingFormulaContext);

				if (logger.isTraceEnabled()) {
					logger.trace(" Create merged composite labeling\n " + mergeCompositeLabeling.toString());
				}

				// Store results
				labelingFormulaContext.setCompositeLabeling(compositeLabeling);

				labelingFormulaContext.setMergedLblCompositeContext(mergeCompositeLabeling);

				extractAllergens(labelingFormulaContext, formulatedProduct);

				for (LabelingRuleListDataItem labelingRuleListDataItem : labelingRuleLists) {
					if (LabelingRuleType.Render.equals(labelingRuleListDataItem.getLabelingRuleType())
							&& Boolean.TRUE.equals(labelingRuleListDataItem.getIsActive())) {

						String log = "";
						MLText label = new MLText();
						if ((labelingRuleListDataItem.getFormula() != null) && !labelingRuleListDataItem.getFormula().trim().isEmpty()) {
							Set<Locale> locales = (labelingRuleListDataItem.getLocales() != null) && !labelingRuleListDataItem.getLocales().isEmpty()
									? MLTextHelper.extractLocales(labelingRuleListDataItem.getLocales())
									: labelingFormulaContext.getLocales();

							if (locales.isEmpty()) {
								locales.add(new Locale(Locale.getDefault().getLanguage()));
							}

							for (Locale locale : locales) {
								Locale currentLocal = I18NUtil.getLocale();
								Locale currentContentLocal = I18NUtil.getContentLocale();
								try {
									I18NUtil.setLocale(locale);
									I18NUtil.setContentLocale(null);

									labelingFormulaContext.setLocale(locale);

									String ret = "";

									String[] formulas = SpelHelper.formatMTFormulas(labelingRuleListDataItem.getFormula());
									for (String formula : formulas) {

										Matcher varFormulaMatcher = SpelHelper.formulaVarPattern.matcher(formula);
										if (varFormulaMatcher.matches()) {
											logger.debug(
													"Variable formula : " + varFormulaMatcher.group(2) + " (" + varFormulaMatcher.group(1) + ")");
											Expression exp = parser.parseExpression(varFormulaMatcher.group(2));
											dataContext.setVariable(varFormulaMatcher.group(1), exp.getValue(dataContext));
										} else {
											Expression exp = parser.parseExpression(formula);
											ret = exp.getValue(dataContext, String.class);
										}
									}

									if (logger.isDebugEnabled()) {
										logger.debug("Running renderFormula :" + labelingRuleListDataItem.getFormula() + " for locale :"
												+ locale.toString());
										logger.debug(" - render value :" + ret);
									}
									label.addValue(locale, ret);

								} catch (Exception e) {
									formulatedProduct.getReqCtrlList()
											.add(new ReqCtrlListDataItem(null, RequirementType.Tolerated,
													MLTextHelper.getI18NMessage("message.formulate.labelRule.error",
															labelingRuleListDataItem.getName(), e.getLocalizedMessage()),
													null, new ArrayList<>(), RequirementDataType.Labelling));

									if (logger.isDebugEnabled()) {
										logger.warn("Error in formula : (" + labelingRuleListDataItem.getNodeRef() + ")"
												+ SpelHelper.formatFormula(labelingRuleListDataItem.getFormula()), e);
									}
								} finally {
									I18NUtil.setLocale(currentLocal);
									I18NUtil.setContentLocale(currentContentLocal);
								}
							}

							// Create logs
							log = labelingFormulaContext
									.createJsonLog(labelingRuleListDataItem.getFormula().replace(" ", "").contains("render(false)"));
						}

						retainNodes.addAll(getOrCreateILLDataItems(formulatedProduct, labelingRuleListDataItem.getNodeRef(), label, log,
								labelingFormulaContext, sortOrder));

						sortOrder = sortOrder + 50;
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
		if (productData.getAllergenList() != null) {
			for (AllergenListDataItem allergenListDataItem : productData.getAllergenList()) {
				NodeRef allergen = allergenListDataItem.getAllergen();
				if (Boolean.TRUE.equals(allergenListDataItem.getVoluntary())) {
					if (AllergenType.Major.toString().equals(nodeService.getProperty(allergen, PLMModel.PROP_ALLERGEN_TYPE))) {
						appendAllergen(labelingFormulaContext.getAllergens(), allergen, allergenListDataItem.getQtyPerc());
					}
				} else if (Boolean.TRUE.equals(allergenListDataItem.getInVoluntary())) {
					if (AllergenType.Major.toString().equals(nodeService.getProperty(allergen, PLMModel.PROP_ALLERGEN_TYPE))) {
						appendAllergen(labelingFormulaContext.getInVolAllergens(), allergen, allergenListDataItem.getQtyPerc());
						for (NodeRef inVoluntarySource : allergenListDataItem.getInVoluntarySources()) {
							QName inVoluntarySourceType = nodeService.getType(inVoluntarySource);

							if (PLMModel.TYPE_RAWMATERIAL.equals(inVoluntarySourceType)) {
								appendAllergen(labelingFormulaContext.getInVolAllergensRawMaterial(), allergen, allergenListDataItem.getQtyPerc());
							} else if (PLMModel.TYPE_RESOURCEPRODUCT.equals(inVoluntarySourceType)) {
								appendAllergen(labelingFormulaContext.getInVolAllergensProcess(), allergen, allergenListDataItem.getQtyPerc());
							}
						}
					}
				}
			}
		}
	}

	private void appendAllergen(Map<NodeRef, Double> toAppendTo, NodeRef allergen, Double qtyPerc) {
		Double qty = qtyPerc;

		if (toAppendTo.containsKey(allergen) && (qty != null) && (toAppendTo.get(allergen) != null)) {
			qty += toAppendTo.get(allergen);
		}

		toAppendTo.put(allergen, qty);

	}

	private void applyMeatContentRules(ProductData formulatedProduct, CompositeLabeling parent, LabelingFormulaContext labelingFormulaContext) {

		if (!labelingFormulaContext.getMeatContentRules().isEmpty() && !formulatedProduct.getMeatContents().isEmpty()) {

			Set<CompositeLabeling> toAdd = new HashSet<>();

			for (MeatContentRule meatContentRule : labelingFormulaContext.getMeatContentRules()) {

				MeatContentData meatContentData = formulatedProduct.getMeatContents().get(meatContentRule.getMeatType());
				if ((meatContentData != null) && (meatContentData.getMeatContent() != null) && (meatContentData.getMeatContent() < 100)) {
					CompositeLabeling fatReplacement = null;

					if (meatContentRule.getFatReplacement() != null) {
						fatReplacement = parent.getIngList().get(meatContentRule.getFatReplacement());
						if (fatReplacement == null) {
							RepositoryEntity replacement = alfrescoRepository.findOne(meatContentRule.getFatReplacement());
							if (replacement instanceof IngItem) {
								fatReplacement = new IngItem((IngItem) replacement);
								fatReplacement.setQties(0d);

								if (logger.isTraceEnabled()) {
									logger.trace("Create new fat replacement :" + getName(fatReplacement));
								}
							} else {
								logger.warn("Invalid replacement :" + meatContentRule.getFatReplacement());
							}
						}
					}

					CompositeLabeling ctReplacement = null;

					if (meatContentRule.getCtReplacement() != null) {
						ctReplacement = parent.getIngList().get(meatContentRule.getCtReplacement());
						if (ctReplacement == null) {
							RepositoryEntity replacement = alfrescoRepository.findOne(meatContentRule.getCtReplacement());
							if (replacement instanceof IngItem) {
								ctReplacement = new IngItem((IngItem) replacement);
								ctReplacement.setQties(0d);

								if (logger.isTraceEnabled()) {
									logger.trace("Create new collagen replacement :" + getName(ctReplacement));
								}
							} else {
								logger.warn("Invalid replacement :" + meatContentRule.getCtReplacement());
							}
						}
					}

					for (CompositeLabeling component : parent.getIngList().values()) {
						if (component.getNodeRef().equals(meatContentRule.getComponent())) {

							if ((ctReplacement != null) && (fatReplacement != null)) {
								if (component.getQty() != null) {
									ctReplacement.setQty(component.getQty() * (meatContentData.getExCTPerc() / 100d));
									fatReplacement.setQty(component.getQty() * (meatContentData.getExFatPerc() / 100d));
								}
								if (component.getQtyWithYield() != null) {
									ctReplacement.setQtyWithYield(component.getQtyWithYield() * (meatContentData.getExCTPerc() / 100d));
									fatReplacement.setQtyWithYield(component.getQtyWithYield() * (meatContentData.getExFatPerc() / 100d));
								}

								if (component.getVolume() != null) {
									ctReplacement.setVolume(component.getVolume() * (meatContentData.getExCTPerc() / 100d));
									fatReplacement.setVolume(component.getVolume() * (meatContentData.getExFatPerc() / 100d));
								}

								if (component.getVolumeWithYield() != null) {
									ctReplacement.setVolumeWithYield(component.getVolumeWithYield() * (meatContentData.getExCTPerc() / 100d));
									fatReplacement.setVolumeWithYield(component.getVolumeWithYield() * (meatContentData.getExFatPerc() / 100d));
								}

								toAdd.add(ctReplacement);
							} else if (fatReplacement != null) {
								if (component.getQty() != null) {
									fatReplacement.setQty(component.getQty() * (1d - (meatContentData.getMeatContent() / 100d)));
								}
								if (component.getQtyWithYield() != null) {
									fatReplacement.setQtyWithYield(component.getQtyWithYield() * (1d - (meatContentData.getMeatContent() / 100d)));
								}
								if (component.getVolume() != null) {
									fatReplacement.setVolume(component.getVolume() * (1d - (meatContentData.getMeatContent() / 100d)));
								}
								if (component.getVolumeWithYield() != null) {
									fatReplacement
											.setVolumeWithYield(component.getVolumeWithYield() * (1d - (meatContentData.getMeatContent() / 100d)));
								}
							}

							if (component.getQty() != null) {
								component.setQty(component.getQty() * (meatContentData.getMeatContent() / 100d));
							}
							if (component.getQtyWithYield() != null) {
								component.setQtyWithYield(component.getQtyWithYield() * (meatContentData.getMeatContent() / 100d));
							}
							if (component.getVolume() != null) {
								component.setVolume(component.getVolume() * (meatContentData.getMeatContent() / 100d));
							}
							if (component.getVolumeWithYield() != null) {
								component.setVolumeWithYield(component.getVolumeWithYield() * (meatContentData.getMeatContent() / 100d));
							}
							if (fatReplacement != null) {
								toAdd.add(fatReplacement);
							}

							if (fatReplacement != null) {

								copyAttributes(fatReplacement, component);
							}

						} else {
							applyMeatContentRules(formulatedProduct, component, labelingFormulaContext);
						}
					}
				}
			}

			for (CompositeLabeling tmp : toAdd) {
				parent.add(tmp);
			}

		}

	}

	private void aggregateLegalName(CompositeLabeling parent, LabelingFormulaContext labelingFormulaContext, boolean multiLevel) {

		Map<String, List<CompositeLabeling>> componentsByName = new HashMap<>();

		for (CompositeLabeling component : parent.getIngList().values()) {
			List<CompositeLabeling> tmp = new ArrayList<>();
			String name = labelingFormulaContext.getLegalIngName(component);
			if ((name != null) && !name.isEmpty()) {
				if (componentsByName.containsKey(name)) {
					tmp = componentsByName.get(name);
				}
				tmp.add(component);

				componentsByName.put(name, tmp);
			}
			if (multiLevel) {
				aggregateLegalName(component, labelingFormulaContext, multiLevel);
			}
		}

		for (List<CompositeLabeling> toAggregate : componentsByName.values()) {

			CompositeLabeling prev = null;
			for (CompositeLabeling component : toAggregate) {
				if (prev == null) {
					prev = component;
				} else {
					if (((prev.getIngType() == null) || (component.getIngType() == null)) || prev.getIngType().equals(component.getIngType())) {

						merge(prev, component);
						parent.remove(component.getNodeRef());

						if (labelingFormulaContext.getToApplyThresholdItems().contains(component.getNodeRef())) {
							labelingFormulaContext.getToApplyThresholdItems().add(prev.getNodeRef());
						}

					}

				}

			}

		}

	}

	private void mergeAtEndLabeling(CompositeLabeling parent, LabelingFormulaContext labelingFormulaContext,
			Map<String, List<CompositeLabeling>> componentsByName, boolean isRoot) {

		List<CompositeLabeling> toRemove = new ArrayList<>();

		for (CompositeLabeling component : parent.getIngListAtEnd().values()) {
			List<CompositeLabeling> tmp = new ArrayList<>();
			String name = labelingFormulaContext.getLegalIngName(component);
			if ((name != null) && !name.isEmpty()) {
				if (componentsByName.containsKey(name)) {
					tmp = componentsByName.get(name);
				}
				tmp.add(component);
				componentsByName.put(name, tmp);
			}
			if (!isRoot) {
				toRemove.add(component);
			}

		}

		for (CompositeLabeling component : parent.getIngList().values()) {
			DeclarationType declarationType = component.getDeclarationType();

			boolean isGroup = DeclarationType.Group.equals(declarationType) || DeclarationType.Kit.equals(declarationType);

			if (!isGroup) {
				mergeAtEndLabeling(component, labelingFormulaContext, componentsByName, isGroup);
			} else {
				mergeAtEndLabeling(component, labelingFormulaContext, new HashMap<>(), isGroup);
			}
		}

		if (isRoot) {
			for (List<CompositeLabeling> toAggregate : componentsByName.values()) {

				CompositeLabeling prev = null;
				for (CompositeLabeling component : toAggregate) {

					if (prev == null) {
						prev = component;
					} else {
						merge(prev, component);
					}

					if (parent.getAtEnd(component.getNodeRef()) == null) {
						parent.addAtEnd(component);
					}
				}
			}
		} else {
			toRemove.forEach(ing -> parent.removeAtEnd(ing.getNodeRef()));
		}

	}

	private void merge(CompositeLabeling prev, CompositeLabeling component) {
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

			if ((prev.getQtyTotal() != null) && (component.getQtyTotal() != null)) {
				prev.setQtyTotal(prev.getQtyTotal() + component.getQtyTotal());
			} else {
				prev.setQtyTotal(null);
			}
			if ((prev.getVolumeTotal() != null) && (component.getVolumeTotal() != null)) {
				prev.setVolumeTotal(prev.getVolumeTotal() + component.getVolumeTotal());
			} else {
				prev.setVolumeTotal(null);
			}

			if ((prev.getQtyWithYield() != null) && (component.getQtyWithYield() != null)) {
				prev.setQtyWithYield(prev.getQtyWithYield() + component.getQtyWithYield());
			} else {
				prev.setQtyWithYield(null);
			}
			if ((prev.getVolumeWithYield() != null) && (component.getVolumeWithYield() != null)) {
				prev.setVolumeWithYield(prev.getVolumeWithYield() + component.getVolumeWithYield());
			} else {
				prev.setVolumeWithYield(null);
			}

			for (CompositeLabeling ing : component.getIngList().values()) {
				if (prev.getIngList().containsKey(ing.getNodeRef())) {
					merge(prev.getIngList().get(ing.getNodeRef()), ing);
				} else {
					prev.add(ing);
				}
			}

			if (((prev.getPluralLegalName() == null) || prev.getPluralLegalName().isEmpty())
					&& ((component.getPluralLegalName() != null) && !component.getPluralLegalName().isEmpty())) {
				prev.setPluralLegalName(component.getPluralLegalName());
			}

			if ((prev.getIngType() == null)) {
				if ((component.getIngType() != null)) {
					prev.setIngType(component.getIngType());
				}
			}

			prev.setPlural(true);

			copyAttributes(prev, component);
		}
	}

	private void copyAttributes(CompositeLabeling prev, CompositeLabeling component) {

		prev.getAllergens().addAll(component.getAllergens());
		prev.getBioOrigins().addAll(component.getBioOrigins());
		prev.getFootNotes().addAll(component.getFootNotes());

		for (Map.Entry<PlaceOfActivityTypeCode, Set<NodeRef>> entry : component.getGeoOriginsByPlaceOfActivity().entrySet()) {
			if (prev.getGeoOriginsByPlaceOfActivity().containsKey(entry.getKey())) {
				prev.getGeoOriginsByPlaceOfActivity().get(entry.getKey()).addAll(entry.getValue());
			} else {
				prev.getGeoOriginsByPlaceOfActivity().put(entry.getKey(), entry.getValue());
			}

		}

	}

	private CompositeLabeling mergeCompositeLabeling(CompositeLabeling lblCompositeContext, LabelingFormulaContext labelingFormulaContext) {
		CompositeLabeling merged = new CompositeLabeling();
		merged.setQtyTotal(lblCompositeContext.getQtyTotal());
		merged.setVolumeTotal(lblCompositeContext.getVolumeTotal());

		// Start adding all the components
		for (CompositeLabeling component : lblCompositeContext.getIngList().values()) {
			if (!labelingFormulaContext.isGroup(component)) {
				merged.add(component.createCopy());
			}
		}

		// Then merge
		for (CompositeLabeling component : lblCompositeContext.getIngList().values()) {
			if (labelingFormulaContext.isGroup(component)) {
				CompositeLabeling compositeLabeling = component;
				for (CompositeLabeling subComponent : compositeLabeling.getIngList().values()) {

					Double qty = computeQtyPerc(labelingFormulaContext, compositeLabeling, subComponent, compositeLabeling.getQty());
					Double volume = computeVolumePerc(labelingFormulaContext, compositeLabeling, subComponent, compositeLabeling.getVolume());

					Double qtyWithYield = computeQtyPerc(labelingFormulaContext, compositeLabeling, subComponent,
							compositeLabeling.getQtyWithYield());
					Double volumeWithYield = computeVolumePerc(labelingFormulaContext, compositeLabeling, subComponent,
							compositeLabeling.getVolumeWithYield());

					CompositeLabeling toMerged = merged.get(subComponent.getNodeRef());

					if (toMerged == null) {
						CompositeLabeling clonedSubComponent = subComponent.createCopy();
						clonedSubComponent.setQty(qty);
						clonedSubComponent.setVolume(volume);
						clonedSubComponent.setQtyWithYield(qtyWithYield);
						clonedSubComponent.setVolumeWithYield(volumeWithYield);

						merged.add(clonedSubComponent);
					} else {

						if ((qty != null) && (toMerged.getQty() != null)) {
							toMerged.setQty(toMerged.getQty() + qty);
						}
						if ((volume != null) && (toMerged.getVolume() != null)) {
							toMerged.setVolume(toMerged.getVolume() + volume);
						}

						if ((qtyWithYield != null) && (toMerged.getQtyWithYield() != null)) {
							toMerged.setQtyWithYield(toMerged.getQtyWithYield() + qtyWithYield);
						}
						if ((volumeWithYield != null) && (toMerged.getVolumeWithYield() != null)) {
							toMerged.setVolumeWithYield(toMerged.getVolumeWithYield() + volumeWithYield);
						}

						copyAttributes(toMerged, subComponent);

					}
				}
			}
		}

		// Level 1 only
		applyAggregateRules(merged, labelingFormulaContext, false, true);

		aggregateLegalName(merged, labelingFormulaContext, false);

		return merged;

	}

	private Double computeVolumePerc(LabelingFormulaContext labelingFormulaContext, CompositeLabeling parent, CompositeLabeling component,
			Double ratio) {
		BigDecimal ret = labelingFormulaContext.computeVolumePerc(parent, component,
				ratio != null ? BigDecimal.valueOf(ratio) : LabelingFormulaContext.DEFAULT_RATIO);
		return ret != null ? ret.doubleValue() : null;
	}

	private Double computeQtyPerc(LabelingFormulaContext labelingFormulaContext, CompositeLabeling parent, CompositeLabeling component,
			Double ratio) {
		BigDecimal ret = labelingFormulaContext.computeQtyPerc(parent, component,
				ratio != null ? BigDecimal.valueOf(ratio) : LabelingFormulaContext.DEFAULT_RATIO);
		return ret != null ? ret.doubleValue() : null;
	}

	private Map<String, List<LabelingRuleListDataItem>> getLabelingRules(ProductData formulatedProduct) {
		Map<String, List<LabelingRuleListDataItem>> ret = new TreeMap<>((s1, s2) -> s1.compareTo(s2));
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

		if (ret.size() > 1) {
			List<LabelingRuleListDataItem> defaultRules = ret.get(LabelingRuleListDataItem.DEFAULT_LABELING_GROUP);
			if (defaultRules != null) {
				for (Map.Entry<String, List<LabelingRuleListDataItem>> labelingRuleListsGroup : ret.entrySet()) {
					labelingRuleListsGroup.getValue().addAll(defaultRules);
				}

				ret.remove(LabelingRuleListDataItem.DEFAULT_LABELING_GROUP);
			}

		}

		return ret;
	}

	private void addLabelingRule(Map<String, List<LabelingRuleListDataItem>> ret, LabelingRuleListDataItem labelingRule) {
		if ((labelingRule.getGroups() != null) && !labelingRule.getGroups().isEmpty()) {
			for (String group : labelingRule.getGroups()) {
				if ((group == null) || group.isEmpty()) {
					group = LabelingRuleListDataItem.DEFAULT_LABELING_GROUP;
				}
				List<LabelingRuleListDataItem> tmp = ret.get(group);
				if (tmp == null) {
					tmp = new LinkedList<>();
				}
				tmp.add(labelingRule);
				ret.put(group, tmp);
			}
		} else {
			String group = LabelingRuleListDataItem.DEFAULT_LABELING_GROUP;
			List<LabelingRuleListDataItem> tmp = ret.get(group);
			if (tmp == null) {
				tmp = new LinkedList<>();
			}
			tmp.add(labelingRule);
			ret.put(group, tmp);
		}
	}

	private void copyTemplateLabelingRuleList(ProductData formulatedProduct) {
		if ((formulatedProduct.getEntityTpl() != null) && !formulatedProduct.getEntityTpl().equals(formulatedProduct)
				&& (formulatedProduct.getEntityTpl().getLabelingListView().getLabelingRuleList() != null)) {
			for (LabelingRuleListDataItem modelLabelingRuleListDataItem : formulatedProduct.getEntityTpl().getLabelingListView()
					.getLabelingRuleList()) {
				if (modelLabelingRuleListDataItem.isSynchronisable()) {
					boolean contains = false;
					if (formulatedProduct.getLabelingListView().getLabelingRuleList() != null) {
						for (LabelingRuleListDataItem labelingRuleListDataItem : formulatedProduct.getLabelingListView().getLabelingRuleList()) {
							if (labelingRuleListDataItem.getName().equals(modelLabelingRuleListDataItem.getName())) {
								contains = true;
								if ((labelingRuleListDataItem.getIsManual() == null)
										|| !Boolean.TRUE.equals(labelingRuleListDataItem.getIsManual())) {
									labelingRuleListDataItem.update(modelLabelingRuleListDataItem);
								}
								break;
							}
						}
					} else {
						formulatedProduct.getLabelingListView().setLabelingRuleList(new LinkedList<>());
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

	private void applyAggregateRules(CompositeLabeling parent, LabelingFormulaContext labelingFormulaContext, boolean recur,
			boolean mergedLabelling) {

		if (!labelingFormulaContext.getAggregateRules().isEmpty()) {

			Map<NodeRef, CompositeLabeling> toAdd = new HashMap<>();
			Map<NodeRef, CompositeLabeling> ingList = parent.getIngList();

			for (Iterator<Map.Entry<NodeRef, CompositeLabeling>> iterator = ingList.entrySet().iterator(); iterator.hasNext();) {
				CompositeLabeling component = iterator.next().getValue();

				// Recur
				if (recur) {
					applyAggregateRules(component, labelingFormulaContext, recur, mergedLabelling);
				}

				if (labelingFormulaContext.getAggregateRules().containsKey(component.getNodeRef())) {
					for (AggregateRule aggregateRule : labelingFormulaContext.getAggregateRules().get(component.getNodeRef())) {

						// Create a new
						if (!LabelingRuleType.Type.equals(aggregateRule.getLabelingRuleType())
								&& !(mergedLabelling && LabelingRuleType.Group.equals(aggregateRule.getLabelingRuleType()))) {

							NodeRef aggregateRuleNodeRef = aggregateRule.getKey();

							if ((aggregateRule.getReplacement() == null) || !aggregateRule.getReplacement().equals(component.getNodeRef())) {

								if (toAdd.containsKey(aggregateRuleNodeRef) || aggregateRule.matchAll(ingList.values(), recur)) {

									Double qty = component.getQty();
									Double volume = component.getVolume();
									Double qtyWithYield = component.getQtyWithYield();
									Double volumeWithYield = component.getVolumeWithYield();

									boolean is100Perc = (aggregateRule.getQty() == null) || (aggregateRule.getQty() == 100d);
									// Add ing to group
									if (aggregateRule.getQty() != null) {
										if (qty != null) {
											qty = (qty * aggregateRule.getQty()) / 100;
										}

										if (volume != null) {
											volume = (volume * aggregateRule.getQty()) / 100;
										}

										if (qtyWithYield != null) {
											qtyWithYield = (qtyWithYield * aggregateRule.getQty()) / 100;
										}

										if (volumeWithYield != null) {
											volumeWithYield = (volumeWithYield * aggregateRule.getQty()) / 100;
										}
									}

									if (logger.isTraceEnabled()) {
										logger.trace("Adding :" + getName(component) + "  to aggregate rule, qty " + qty + " is100Perc " + is100Perc);
									}

									CompositeLabeling current = toAdd.containsKey(aggregateRuleNodeRef) ? toAdd.get(aggregateRuleNodeRef)
											: ingList.get(aggregateRuleNodeRef);

									// Replacement
									if ((aggregateRule.getReplacement() != null)
											&& LabelingRuleType.DoNotDetails.equals(aggregateRule.getLabelingRuleType())) {

										if (current == null) {
											RepositoryEntity replacement = alfrescoRepository.findOne(aggregateRule.getReplacement());
											if (replacement instanceof IngItem) {
												current = new IngItem((IngItem) replacement);
												current.setQties(0d);

												if (logger.isTraceEnabled()) {
													logger.trace("Create new aggregate replacement :" + getName(current));
												}
											} else {
												logger.warn("Invalid replacement :" + aggregateRule.getReplacement());
											}

										}

										if (current != null) {

											if (ingList.containsKey(aggregateRuleNodeRef)) {
												current.setPlural(true);
											}

											if (!toAdd.containsKey(aggregateRuleNodeRef)) {
												toAdd.put(aggregateRuleNodeRef, current);
											} else {
												current.setPlural(true);
											}

											if ((qty != null) && (current.getQty() != null)) {
												current.setQty(current.getQty() + qty);
											}

											if ((volume != null) && (current.getVolume() != null)) {
												current.setVolume(current.getVolume() + volume);
											}

											if ((qtyWithYield != null) && (current.getQtyWithYield() != null)) {
												current.setQtyWithYield(current.getQtyWithYield() + qtyWithYield);
											}

											if ((volumeWithYield != null) && (current.getVolumeWithYield() != null)) {
												current.setVolumeWithYield(current.getVolumeWithYield() + volumeWithYield);
											}

											copyAttributes(current, component);

										}

									} else {

										CompositeLabeling compositeLabeling = null;
										if (current != null) {
											compositeLabeling = current;
										}

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
											compositeLabeling.setQties(0d);
											toAdd.put(aggregateRuleNodeRef, compositeLabeling);
										}

										ReqCtrlListDataItem error = null;

										if (DeclarationType.Declare.equals(component.getDeclarationType())) {
											for (CompositeLabeling childComponent : component.getIngList().values()) {
												Double subQty = null;
												Double subVolume = null;
												Double subQtyWithYield = null;
												Double subVolumeWithYield = null;
												if ((qty != null) && (childComponent.getQty() != null)) {
													subQty = BigDecimal.valueOf(childComponent.getQty()).multiply(BigDecimal.valueOf(qty), LabelingFormulaContext.PRECISION)
															.divide(BigDecimal.valueOf(component.getQtyTotal()), LabelingFormulaContext.PRECISION)
															.doubleValue();
												}

												if ((qtyWithYield != null) && (childComponent.getQtyWithYield() != null)) {
													subQtyWithYield = BigDecimal.valueOf(childComponent.getQtyWithYield())
															.multiply(BigDecimal.valueOf(qtyWithYield),LabelingFormulaContext.PRECISION).divide(BigDecimal.valueOf(component.getQtyTotal()),
																	LabelingFormulaContext.PRECISION)
															.doubleValue();
												}

												if ((volume != null) && (childComponent.getVolume() != null)) {
													subVolume = BigDecimal.valueOf(childComponent.getVolume()).multiply(BigDecimal.valueOf(volume),LabelingFormulaContext.PRECISION)
															.divide(BigDecimal.valueOf(component.getVolumeTotal()), LabelingFormulaContext.PRECISION)
															.doubleValue();

												}

												if ((volumeWithYield != null) && (childComponent.getVolumeWithYield() != null)) {
													subVolumeWithYield = BigDecimal.valueOf(childComponent.getVolumeWithYield())
															.multiply(BigDecimal.valueOf(volumeWithYield),LabelingFormulaContext.PRECISION).divide(BigDecimal.valueOf(component.getVolumeTotal()),
																	LabelingFormulaContext.PRECISION)
															.doubleValue();

												}

												error = appendToAggregate(childComponent, compositeLabeling, aggregateRule, subQty, subVolume,
														subQtyWithYield, subVolumeWithYield);
											}

										} else {
											error = appendToAggregate(component, compositeLabeling, aggregateRule, qty, volume, qtyWithYield,
													volumeWithYield);
										}

										if (error != null) {

											if ((parent.getNodeRef() != null) && nodeService.exists(parent.getNodeRef())) {
												error.addSource(parent.getNodeRef());
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
									} else {

										if ((qty != null) && (component.getQty() != null)) {
											component.setQty(component.getQty() - qty);
										}
										if ((volume != null) && (component.getVolume() != null)) {
											component.setVolume(component.getVolume() - volume);
										}

										if ((qtyWithYield != null) && (component.getQtyWithYield() != null)) {
											component.setQtyWithYield(component.getQtyWithYield() - qtyWithYield);
										}
										if ((volumeWithYield != null) && (component.getVolumeWithYield() != null)) {
											component.setVolumeWithYield(component.getVolumeWithYield() - volumeWithYield);
										}
									}

								}
							}
						}
					}

				}
			}

			for (CompositeLabeling ing : toAdd.values()) {
				parent.add(ing);
			}

		}

	}

	private ReqCtrlListDataItem appendToAggregate(CompositeLabeling component, CompositeLabeling compositeLabeling, AggregateRule aggregateRule,
			Double qty, Double volume, Double qtyWithYield, Double volumeWithYield) {
		CompositeLabeling current = compositeLabeling.getIngList().get(component.getNodeRef());
		boolean is100Perc = (aggregateRule.getQty() == null) || (aggregateRule.getQty() == 100d);

		if (!LabelingRuleType.DoNotDetails.equals(aggregateRule.getLabelingRuleType())) {
			if (current == null) {
				if (!is100Perc) {
					current = component.createCopy();
				} else {
					current = component;
				}
				current.setQties(0d);
				if (logger.isTraceEnabled()) {
					logger.trace(" - Add new ing to aggregate  :" + getName(current));
				}

				if (DeclarationType.Group.equals(current.getDeclarationType())) {
					if (logger.isTraceEnabled()) {
						logger.trace(" - Downgrade group to Detail");
					}
					current.setDeclarationType(DeclarationType.Detail);
				}

				compositeLabeling.add(current);
			}
		}

		if (current != null) {

			if ((qty != null) && (current.getQty() != null)) {
				if (logger.isTraceEnabled()) {
					logger.trace(" - Update aggregate ing qty  :" + getName(current) + " " + qty);
				}
				current.setQty(current.getQty() + qty);
			} else {
				current.setQty(null);
			}

			if ((current.getVolume() != null) && (volume != null)) {
				if (logger.isTraceEnabled()) {
					logger.trace(" - Update aggregate ing volume  :" + getName(current) + " " + volume);
				}
				current.setVolume(current.getVolume() + volume);
			} else {
				current.setVolume(null);
			}

			if ((qtyWithYield != null) && (current.getQtyWithYield() != null)) {
				if (logger.isTraceEnabled()) {
					logger.trace(" - Update aggregate ing qtyWithYield  :" + getName(current) + " " + qtyWithYield);
				}
				current.setQtyWithYield(current.getQtyWithYield() + qtyWithYield);
			} else {
				current.setQtyWithYield(null);
			}

			if ((current.getVolumeWithYield() != null) && (volumeWithYield != null)) {
				if (logger.isTraceEnabled()) {
					logger.trace(" - Update aggregate ing volumeWithYield  :" + getName(current) + " " + volumeWithYield);
				}
				current.setVolumeWithYield(current.getVolumeWithYield() + volumeWithYield);
			} else {
				current.setVolumeWithYield(null);
			}

			copyAttributes(current, component);

		} else {
			copyAttributes(compositeLabeling, component);
		}

		if ((qty != null) && (compositeLabeling.getQtyTotal() != null) && qtyWithYield!=null) {
			compositeLabeling.setQty(qty + compositeLabeling.getQtyTotal().doubleValue());
			compositeLabeling.setQtyWithYield(qtyWithYield + compositeLabeling.getQtyTotal().doubleValue());
			compositeLabeling.setQtyTotal(compositeLabeling.getQty());

		}

		if ((volume != null) && (compositeLabeling.getVolumeTotal() != null) && volumeWithYield!=null) {
			compositeLabeling.setVolume(volume + compositeLabeling.getVolumeTotal().doubleValue());
			compositeLabeling.setVolumeWithYield(volumeWithYield + compositeLabeling.getVolumeTotal().doubleValue());
			compositeLabeling.setVolumeTotal(compositeLabeling.getVolume());

		}

		if ((qty == null) && (current != null)) {
			return new ReqCtrlListDataItem(null, RequirementType.Forbidden, MLTextHelper.getI18NMessage(NULL_ING_ERROR, getName(current)), null,
					new ArrayList<>(), RequirementDataType.Labelling);
		}

		return null;
	}

	// Move Group at top
	private List<CompositeLabeling> reorderCompositeLabeling(LabelingFormulaContext context, CompositeLabeling current, boolean isFirst) {
		List<CompositeLabeling> ret = new LinkedList<>();

		for (Iterator<Map.Entry<NodeRef, CompositeLabeling>> iterator = current.getIngList().entrySet().iterator(); iterator.hasNext();) {
			CompositeLabeling component = iterator.next().getValue();
			Double currQty = context.isIngsLabelingWithYield() ? component.getQtyWithYield() : component.getQty();
			ret.addAll(reorderCompositeLabeling(context, component, false));
			if (context.isIngsLabelingWithYield() ? !Objects.equals(component.getQtyWithYield(), currQty) && (component.getQtyWithYield() == 0)
					: !Objects.equals(component.getQty(), currQty) && (component.getQty() == 0)) {
				iterator.remove();
			} else {
				if (!isFirst) {
					if (component.isGroup()) {

						logger.trace("Found misplaced group :" + getName(component));
						// Remove from current
						iterator.remove();

						ret.add(component);

					}
				}
			}

		}
		if (!isFirst) {

			if (!ret.isEmpty()) {

				double qtyTotalToremove = 0;
				double volumeTotalToremove = 0;

				double qtyTotalToremoveWithYield = 0;
				double volumeTotalToremoveWithYield = 0;

				for (CompositeLabeling childGroup : ret) {
					// Reset qty

					if(childGroup.getQty()!=null) {
						qtyTotalToremove += childGroup.getQty();
						childGroup.setQty((childGroup.getQty() * current.getQty()) / current.getQtyTotal().doubleValue());
					}
					
					if(childGroup.getVolume()!=null) {
						volumeTotalToremove += childGroup.getVolume();
						childGroup.setVolume((childGroup.getVolume() * current.getVolume()) / current.getVolumeTotal().doubleValue());
					}

					if(childGroup.getQtyWithYield()!=null) {
						qtyTotalToremoveWithYield += childGroup.getQtyWithYield();
						childGroup.setQtyWithYield((childGroup.getQtyWithYield() * current.getQtyWithYield()) / current.getQtyTotal().doubleValue());
					}

					if(childGroup.getVolumeWithYield()!=null) {
						volumeTotalToremoveWithYield += childGroup.getVolumeWithYield();
						childGroup.setVolumeWithYield(
								(childGroup.getVolumeWithYield() * current.getVolumeWithYield()) / current.getVolumeTotal().doubleValue());
					}

					if (logger.isTraceEnabled()) {
						logger.trace(" - Move child group to level n-1 :" + getName(childGroup) + " new qty " + childGroup.getQty() + " new vol "
								+ childGroup.getVolume());
					}
				}
				current.setQty(current.getQty() - ((qtyTotalToremove * current.getQty()) / current.getQtyTotal().doubleValue()));

				current.setVolume(current.getVolume() - ((volumeTotalToremove * current.getVolume()) / current.getVolumeTotal().doubleValue()));

				current.setQtyWithYield(
						current.getQtyWithYield() - ((qtyTotalToremoveWithYield * current.getQtyWithYield()) / current.getVolumeTotal().doubleValue()));

				current.setVolumeWithYield(
						current.getVolumeWithYield() - ((volumeTotalToremoveWithYield * current.getVolumeWithYield()) / current.getQtyTotal().doubleValue()));

				current.setVolumeTotal(current.getVolumeTotal()- volumeTotalToremove);
				current.setQtyTotal(current.getQtyTotal() - qtyTotalToremove);

			}

		} else {
			for (CompositeLabeling childGroup : ret) {
				current.add(childGroup);
			}
		}

		return ret;
	}

	private List<IngLabelingListDataItem> getOrCreateILLDataItems(ProductData formulatedProduct, NodeRef key, MLText label, String log,
			LabelingFormulaContext labelingFormulaContext, int sortOrder) {
		List<IngLabelingListDataItem> ret = new ArrayList<>();

		if (labelingFormulaContext.isLabelingByLanguage()) {
			List<Locale> langs = new LinkedList<>();
			for (Locale orderedLocale : labelingFormulaContext.availableLocales) {
				if (label.containsKey(orderedLocale)) {
					langs.add(orderedLocale);
				}
			}

			for (Locale lang : langs) {
				MLText newLabel = new MLText();
				newLabel.addValue(MLTextHelper.getNearestLocale(Locale.getDefault()), label.getValue(lang));
				ret.add(getOrCreateILLDataItem(formulatedProduct, key, newLabel, log, MLTextHelper.localeKey(lang), sortOrder++));
			}
		} else {
			ret.add(getOrCreateILLDataItem(formulatedProduct, key, label, log, null, sortOrder));
		}
		return ret;

	}

	IngLabelingListDataItem getOrCreateILLDataItem(ProductData formulatedProduct, NodeRef key, MLText label, String log, String lang, int sortOrder) {

		IngLabelingListDataItem ill = null;
		for (IngLabelingListDataItem tmp : formulatedProduct.getLabelingListView().getIngLabelingList()) {
			if (((tmp.getGrp() == null) && (key == null)) || (((tmp.getGrp() != null) && tmp.getGrp().equals(key))
					&& ((lang == null) || ((tmp.getLocales() != null) && tmp.getLocales().contains(lang))))) {
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

		if (lang != null) {
			ill.setLocales(Arrays.asList(lang));
		} else {
			ill.setLocales(null);
		}

		// Limit to 50ko (Max 64k)
		if ((log != null) && (log.length() > LargeTextHelper.TEXT_SIZE_LIMIT)) {
			log = "{\"children\":[{\"cssClass\":\"error\",\"name\":\"error-too-long\",\"legal\":\"Details cannot be display, data too long\"}],\"name\":\"root\",\"legal\":\"root\"}";
		}

		ill.setLogValue(log);
		ill.setSort(sortOrder);

		if (!ill.getAspects().contains(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM)) {
			ill.getAspects().add(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM);
		}

		return ill;

	}

	private void visitCompoList(CompositeLabeling parent, Composite<CompoListDataItem> parentComposite, LabelingFormulaContext labelingFormulaContext,
			final BigDecimal ratio, final Double currYield, final boolean apply) {

		Map<String, ReqCtrlListDataItem> errors = new HashMap<>();

		for (Composite<CompoListDataItem> composite : parentComposite.getChildren()) {

			Double calculatedYield = currYield != null ? currYield : 100d;
			CompoListDataItem compoListDataItem = composite.getData();
			DeclarationType declarationType = getDeclarationType(compoListDataItem, null, labelingFormulaContext);

			if (!DeclarationType.Omit.equals(declarationType)) {

				List<AggregateRule> aggregateRules = getAggregateRules(composite, parentComposite.getChildren(), labelingFormulaContext);

				NodeRef productNodeRef = compoListDataItem.getProduct();
				ProductData productData = (ProductData) alfrescoRepository.findOne(productNodeRef);

				if (logger.isTraceEnabled()) {
					logger.info("#########  Parse :" + productData.getName());
					logger.info("Calculated yield :" + calculatedYield);
					if ((aggregateRules != null) && !aggregateRules.isEmpty()) {
						logger.trace(aggregateRules.toString() + " match ");
					}
				}

				boolean isLocalSemiFinished = productData.isLocalSemiFinished();

				// Calculate qty
				Double qty = FormulationHelper.getQtyInKg(compoListDataItem);
				Double volume = FormulationHelper.getNetVolume(compoListDataItem, productData);

				Double componentYield = FormulationHelper.getYield(compoListDataItem);

				if (qty != null && ratio != null) {
					qty = BigDecimal.valueOf(qty).multiply(ratio, LabelingFormulaContext.PRECISION)
							.doubleValue();

				}

				if (volume != null && ratio != null) {
					volume = BigDecimal.valueOf(volume).multiply(ratio,LabelingFormulaContext.PRECISION)
							.doubleValue();
				}

				Double qtyWithYield = qty != null && !DeclarationType.Group.equals(declarationType) ? BigDecimal.valueOf(qty)
						.multiply(BigDecimal.valueOf(100d),LabelingFormulaContext.PRECISION)
						.divide(BigDecimal.valueOf(calculatedYield), LabelingFormulaContext.PRECISION).doubleValue()
						: qty;
				Double volumeWithYield = volume != null && !DeclarationType.Group.equals(declarationType) ? BigDecimal.valueOf(volume)
						.multiply(BigDecimal.valueOf(100d),LabelingFormulaContext.PRECISION)
						.divide(BigDecimal.valueOf(calculatedYield), LabelingFormulaContext.PRECISION).doubleValue()
						: volume;

				if (!isLocalSemiFinished) {
					if (qty != null && componentYield != null) {
						qty = BigDecimal.valueOf(qty).multiply(BigDecimal.valueOf(componentYield),LabelingFormulaContext.PRECISION)
								.divide(BigDecimal.valueOf(100d), LabelingFormulaContext.PRECISION).doubleValue();
					}

					if (volume != null && componentYield != null) {
						volume = BigDecimal.valueOf(volume).multiply(BigDecimal.valueOf(componentYield),LabelingFormulaContext.PRECISION)
								.divide(BigDecimal.valueOf(100d), LabelingFormulaContext.PRECISION).doubleValue();
					}
				}

				//Water loss
				if ((qty != null) && (calculatedYield != null) && (calculatedYield.doubleValue() != 100d)
						&& nodeService.hasAspect(productNodeRef, PLMModel.ASPECT_WATER)) {

					if (logger.isTraceEnabled()) {
						logger.trace("Detected water lost");
					}

					// Override declaration type
					declarationType = DeclarationType.DoNotDetails;

					if (labelingFormulaContext.getEvaporatedDataItems().isEmpty()) {
						labelingFormulaContext.getEvaporatedDataItems().add(new EvaporatedDataItem(productNodeRef, 100d));
					}

					qtyWithYield = qty;
					volumeWithYield = qty;

					labelingFormulaContext.getToApplyThresholdItems().add(productNodeRef);

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

					fillAllergensAndGeos(parent, productData);

					parent.setQty(qty);
					parent.setVolume(volume);

					parent.setQtyWithYield(qtyWithYield);
					parent.setVolumeWithYield(volumeWithYield);
					if (logger.isTraceEnabled()) {
						logger.trace(
								"+ Creating new group [" + parent.getLegalName(I18NUtil.getContentLocaleLang()) + "] qtyUsed: " + parent.getQty());
					}
				}

				CompositeLabeling compositeLabeling = parent;

				if (!DeclarationType.DoNotDeclare.equals(declarationType) || !aggregateRules.isEmpty()) {

					if (!isLocalSemiFinished) {
						// MultiLevel only if detail or group
						if ((!DeclarationType.DoNotDetails.equals(declarationType) || !DeclarationType.DoNotDetailsAtEnd.equals(declarationType))
								&& (!productData.isGeneric() && !Boolean.TRUE.equals(productData.getIsIngListManual()))) {
							Composite<CompoListDataItem> sfComposite = CompositeHelper.getHierarchicalCompoList(productData
									.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>())));
							if ((sfComposite.getChildren() != null) && !sfComposite.getChildren().isEmpty()) {
								for (Composite<CompoListDataItem> sfChild : sfComposite.getChildren()) {
									//	CompoListDataItem clone = sfChild.getData().copy();
									//clone.setParent(compoListDataItem);
									//sfChild.setData(clone);
									composite.addChild(sfChild);
								}
								isMultiLevel = true;
							}
						}

						if (!isMultiLevel && (DeclarationType.Declare.equals(declarationType))) {
							// Case show ings and is empty use legalName instead #2558
							if (((productData.getIngList() == null) || productData.getIngList().isEmpty())) {
								declarationType = DeclarationType.DoNotDetails;
							} else {
								// Case all ingredients are omit
								boolean shouldOmit = true;
								for (IngListDataItem ingListItem : productData.getIngList()) {
									DeclarationType ingDeclarationType = getDeclarationType(compoListDataItem, ingListItem, labelingFormulaContext);

									if (!DeclarationType.Omit.equals(ingDeclarationType)) {
										shouldOmit = false;
										break;
									}
								}
								if (shouldOmit) {
									if (logger.isDebugEnabled()) {
										logger.debug("Only omitted ingredients, omit the raw material");
									}
									declarationType = DeclarationType.Omit;
								}

							}
						}
					}

					if (!DeclarationType.Omit.equals(declarationType)) {

						if (!DeclarationType.Declare.equals(declarationType) || !aggregateRules.isEmpty()) {

							CompositeLabeling lc = null;
							if (DeclarationType.DoNotDetailsAtEnd.equals(declarationType)) {
								lc = parent.getAtEnd(productData.getNodeRef());
							} else {
								lc = parent.get(productData.getNodeRef());
							}

							if ((lc != null)) {
								compositeLabeling = lc;
								compositeLabeling.setPlural(true);

								appendQtiesToLabeling(compositeLabeling, qty, qtyWithYield, volume, volumeWithYield, true, composite.isLeaf());

							} else {
								compositeLabeling = new CompositeLabeling(productData);

								fillAllergensAndGeos(compositeLabeling, productData);

								compositeLabeling.getFootNotes().addAll(extractFootNotes(compoListDataItem, null, labelingFormulaContext));

								compositeLabeling.setDeclarationType(declarationType);

								appendQtiesToLabeling(compositeLabeling, qty, qtyWithYield, volume, volumeWithYield, true, composite.isLeaf());

								if (composite.isLeaf()) {

									if (logger.isTraceEnabled()) {
										logger.trace("Set total qty (" + compositeLabeling.getName() + "): " + qty + " volume:" + volume);
									}
									compositeLabeling.setQtyTotal(qty );
									compositeLabeling.setVolumeTotal(volume );

									if (qty != null && qtyWithYield != null && !qty.equals(qtyWithYield)) {
										compositeLabeling.setEvaporatedQty(qtyWithYield - qty);
									}

									if (volumeWithYield != null && volume != null && !volume.equals(volumeWithYield)) {
										compositeLabeling.setEvaporatedVolume(volumeWithYield - volume);
									}

								}

							}
							if (DeclarationType.DoNotDetailsAtEnd.equals(declarationType)) {
								parent.addAtEnd(compositeLabeling);
							} else {
								parent.add(compositeLabeling);
							}

							if (logger.isTraceEnabled()) {
								logger.trace(" - Add detailed labeling component : " + getName(compositeLabeling) + " qty: " + qty + " qtyWithYield: "
										+ qtyWithYield);
							}
						}

					}
				}
				if (!DeclarationType.Omit.equals(declarationType)) {

					if (!DeclarationType.DoNotDetails.equals(declarationType) && !DeclarationType.DoNotDetailsAtEnd.equals(declarationType)
							&& !DeclarationType.DoNotDeclare.equals(declarationType)) {

						if (!isMultiLevel && (productData.getIngList() != null) && !productData.getIngList().isEmpty()) {

							visitIngList(compositeLabeling, productData, CompositeHelper.getHierarchicalCompoList(productData.getIngList()), null,
									qty, volume, qtyWithYield, volumeWithYield, labelingFormulaContext, compoListDataItem, errors);
						}

						BigDecimal computedRatio = BigDecimal.valueOf(1d);
						if (DeclarationType.Declare.equals(declarationType)) {
							if (isMultiLevel && (qty != null) && !isLocalSemiFinished) {

								Double qtyTotal = FormulationHelper.getQtyInKgFromComposition(productData, null,
										FormulationHelper.DEFAULT_NET_WEIGHT);

								if ((qtyTotal != null) && (qtyTotal != 0d)) {
									computedRatio = BigDecimal.valueOf(qty).divide(BigDecimal.valueOf(qtyTotal),
											LabelingFormulaContext.PRECISION);
								}

								if (logger.isTraceEnabled()) {
									logger.trace("Declare ratio for :" + productData.getName() + " " + computedRatio + " total:" + qtyTotal + " qty:"
											+ qty);
								}

							} else if (isLocalSemiFinished) {
								computedRatio = ratio;
							}
						}

						// Recur
						if (!composite.isLeaf()) {

							BigDecimal recurYield = BigDecimal.valueOf(calculatedYield);

							if (componentYield != 100d) {
								recurYield = recurYield.multiply(BigDecimal.valueOf(componentYield),LabelingFormulaContext.PRECISION).divide(BigDecimal.valueOf(100d),
										LabelingFormulaContext.PRECISION);
							}

							if (!isLocalSemiFinished) {

								recurYield = productData.getYield() != null ? BigDecimal.valueOf(productData.getYield()) : BigDecimal.valueOf(100d);

								if ((calculatedYield != null) && (calculatedYield != 100d)) {
									recurYield = recurYield.multiply(BigDecimal.valueOf(calculatedYield),LabelingFormulaContext.PRECISION).divide(BigDecimal.valueOf(100d),
											LabelingFormulaContext.PRECISION);
								}

								if ((componentYield != null) && (componentYield != 100d)) {
									recurYield = recurYield.multiply(BigDecimal.valueOf(componentYield),LabelingFormulaContext.PRECISION).divide(BigDecimal.valueOf(100d),
											LabelingFormulaContext.PRECISION);
								}

							}

							if (logger.isTraceEnabled()) {
								logger.trace(" -- Recur call " + productData.getName() + " yield " + recurYield + " ratio " + ratio);
								logger.trace(" --- prevCalculatedYield " + calculatedYield + " componentYield " + componentYield);
							}

							visitCompoList(compositeLabeling, composite, labelingFormulaContext, computedRatio,
									recurYield != null ? recurYield.doubleValue() : null, !parent.equals(compositeLabeling));
						}
					}

					if (!(DeclarationType.Declare.equals(declarationType) && aggregateRules.isEmpty() && (isLocalSemiFinished || isMultiLevel))) {
						// Update parent qty
						if (!DeclarationType.DoNotDetailsAtEnd.equals(declarationType)) {
							appendQtiesToLabeling(parent, qty, qtyWithYield, volume, volumeWithYield, false, true);
						}

					}
				}

				if (logger.isTraceEnabled()) {
					logger.info("#########  End :" + productData.getName());
				}

			}

		}

		if (apply)

		{
			applyReconstitution(labelingFormulaContext, parent, labelingFormulaContext.getReconstituableDataItems());
			labelingFormulaContext.getReconstituableDataItems().clear();
			applyEvaporation(parent, labelingFormulaContext.getEvaporatedDataItems());
			labelingFormulaContext.getEvaporatedDataItems().clear();

		}
	}

	private void appendQtiesToLabeling(CompositeLabeling compositeLabeling, Double qty, Double qtyWithYield, Double volume, Double volumeWithYield,
			boolean addToQty, boolean addToTotal) {
		if (addToQty) {
			if ((qty != null) && (compositeLabeling.getQty() != null)) {
				compositeLabeling.setQty(qty + compositeLabeling.getQty());
			} else {
				compositeLabeling.setQty(null);
			}
			if ((qtyWithYield != null) && (compositeLabeling.getQtyWithYield() != null)) {
				compositeLabeling.setQtyWithYield(qtyWithYield + compositeLabeling.getQtyWithYield());
			} else {
				compositeLabeling.setQtyWithYield(null);
			}
			if ((volume != null) && (compositeLabeling.getVolume() != null)) {
				compositeLabeling.setVolume(volume + compositeLabeling.getVolume());
			} else {
				compositeLabeling.setVolume(null);
			}
			if ((volumeWithYield != null) && (compositeLabeling.getVolumeWithYield() != null)) {
				compositeLabeling.setVolumeWithYield(volumeWithYield + compositeLabeling.getVolumeWithYield());
			} else {
				compositeLabeling.setVolumeWithYield(null);
			}
		}

		if (addToTotal) {

			if ((qty != null) && (compositeLabeling.getQtyTotal() != null)) {
				if (logger.isTraceEnabled()) {
					logger.trace("Add to totalQty (" + compositeLabeling.getName() + "): " + qty);
				}
				compositeLabeling.setQtyTotal(compositeLabeling.getQtyTotal() + qty);
			}

			if ((volume != null) && (compositeLabeling.getVolumeTotal() != null)) {
				if (logger.isTraceEnabled()) {
					logger.trace("Add to totalVolume ( " + compositeLabeling.getName() + "): " + +volume);
				}
				compositeLabeling.setVolumeTotal(compositeLabeling.getVolumeTotal()+qty);
			}

			if (qty != null && qtyWithYield != null && !qty.equals(qtyWithYield) && compositeLabeling.getEvaporatedQty() != null) {
				BigDecimal evaporatedQty = BigDecimal.valueOf(qtyWithYield).subtract(BigDecimal.valueOf(qty));
				if (logger.isTraceEnabled()) {
					logger.trace("Add to evaporate qty: " + evaporatedQty.doubleValue());
				}
				compositeLabeling.setEvaporatedQty(evaporatedQty.add(BigDecimal.valueOf(compositeLabeling.getEvaporatedQty())).doubleValue());
			}

			if (volumeWithYield != null && volume != null && !volume.equals(volumeWithYield) && compositeLabeling.getEvaporatedVolume() != null) {

				BigDecimal evaporatedVolume = BigDecimal.valueOf(volumeWithYield).subtract(BigDecimal.valueOf(volume));

				if (logger.isTraceEnabled()) {
					logger.trace("Add to evaporate volume: " + evaporatedVolume);

				}
				compositeLabeling
						.setEvaporatedVolume(evaporatedVolume.add(BigDecimal.valueOf(compositeLabeling.getEvaporatedVolume())).doubleValue());

			}

		}
	}


	private void fillAllergensAndGeos(CompositeLabeling compositeLabeling, ProductData productData) {

		for (AllergenListDataItem allergenListDataItem : productData.getAllergenList()) {

			if (Boolean.TRUE.equals(allergenListDataItem.getVoluntary())) {
				if (AllergenType.Major.toString().equals(nodeService.getProperty(allergenListDataItem.getAllergen(), PLMModel.PROP_ALLERGEN_TYPE))) {
					compositeLabeling.getAllergens().add(allergenListDataItem.getAllergen());
				}
			}
		}

		if (productData.getGeoOrigins() != null) {
			addGeo(compositeLabeling, productData.getGeoOrigins(), PlaceOfActivityTypeCode.LAST_PROCESSING);
		}

	}

	private String getName(CompositeLabeling component) {
		if (component instanceof IngItem) {
			return ((IngItem) component).getCharactName();
		}
		return component.getName();
	}

	private void applyEvaporation(CompositeLabeling parent, List<EvaporatedDataItem> evaporatedDataItems) {
		if (!evaporatedDataItems.isEmpty()) {
			if (logger.isTraceEnabled()) {
				logger.trace(" Before applyEvaporation \n " + parent.toString());
			}
			Double evaporatingQty = parent.getEvaporatedQty();
			Double evaporatingVolume = parent.getEvaporatedVolume();

			evaporatedDataItems.stream().forEach(evaporatedDataItem -> {

				CompositeLabeling productLabelItem = parent.get(evaporatedDataItem.getProductNodeRef());
				if ((productLabelItem != null) && (productLabelItem.getQtyWithYield() != null)) {

					if (logger.isDebugEnabled()) {
						logger.debug("Apply water lost : qty " + ((evaporatingQty * evaporatedDataItem.getRate()) / 100d) + " over "
								+ parent.getQtyTotal());

					}
					productLabelItem.setQtyWithYield(productLabelItem.getQty() - ((evaporatingQty * evaporatedDataItem.getRate()) / 100d));

				}

				if ((productLabelItem != null) && (productLabelItem.getVolumeWithYield() != null)) {

					if (logger.isDebugEnabled()) {
						logger.debug("Apply water lost : vol " + ((evaporatingVolume * evaporatedDataItem.getRate()) / 100d) + " over "
								+ parent.getVolumeTotal());

					}
					productLabelItem.setVolumeWithYield(productLabelItem.getVolume() - ((evaporatingVolume * evaporatedDataItem.getRate()) / 100d));

				}
			});

		}

	}

	private void applyReconstitution(LabelingFormulaContext context, CompositeLabeling parent, List<ReconstituableDataItem> reconstituableDataItems) {

		if (!reconstituableDataItems.isEmpty()) {
			if (logger.isTraceEnabled()) {
				logger.trace(" Before reconstitution \n " + parent.toString());
			}

			reconstituableDataItems.stream().sorted((e1, e2) -> Integer.compare(e2.getPriority(), e1.getPriority())).forEach(reconstituableData -> {

				CompositeLabeling productLabelItem = parent.get(reconstituableData.getProductNodeRef());
				if ((productLabelItem != null) && (productLabelItem.getQty() != null)) {

					CompositeLabeling ingLabelItem = parent.get(reconstituableData.getDiluentIngNodeRef());

					if ((ingLabelItem != null) && (ingLabelItem.getQty() != null) && (reconstituableData.getRate() != 0d)) {

						BigDecimal rate = BigDecimal.valueOf(reconstituableData.getRate());

						BigDecimal productQty = BigDecimal.valueOf(productLabelItem.getQty());
						BigDecimal ingQty = BigDecimal.valueOf(ingLabelItem.getQty());
						BigDecimal diluentQty = productQty.multiply(rate,LabelingFormulaContext.PRECISION).subtract(productQty);
						BigDecimal realDiluentQty = ingQty.min(diluentQty);
						BigDecimal realDiluentQtyRatio = (diluentQty.compareTo(BigDecimal.ZERO) == 0) ? (BigDecimal.valueOf(1d))
								: realDiluentQty.divide(diluentQty, LabelingFormulaContext.PRECISION);
						BigDecimal realQty = realDiluentQty.add(productQty.multiply(realDiluentQtyRatio,LabelingFormulaContext.PRECISION)).divide(rate, LabelingFormulaContext.PRECISION);
						ingLabelItem.setQty(ingQty.subtract(realDiluentQty).doubleValue());
						productLabelItem.setQty(productQty.subtract(realQty).doubleValue());

						BigDecimal productQtyWithYield = BigDecimal.valueOf(productLabelItem.getQtyWithYield());
						BigDecimal ingQtyWithYield = BigDecimal.valueOf(ingLabelItem.getQtyWithYield());
						BigDecimal diluentQtyWithYield = productQtyWithYield.multiply(rate,LabelingFormulaContext.PRECISION).subtract(productQtyWithYield);
						BigDecimal realDiluentQtyWithYield = ingQtyWithYield.min(diluentQtyWithYield);
						BigDecimal realDiluentQtyRatioWithYield = (diluentQtyWithYield.compareTo(BigDecimal.ZERO) == 0) ? (BigDecimal.valueOf(1d))
								: realDiluentQtyWithYield.divide(diluentQtyWithYield, LabelingFormulaContext.PRECISION);
						BigDecimal realQtyWithYield = realDiluentQtyWithYield.add(productQtyWithYield.multiply(realDiluentQtyRatioWithYield,LabelingFormulaContext.PRECISION))
								.divide(rate, LabelingFormulaContext.PRECISION);
						ingLabelItem.setQtyWithYield(ingQtyWithYield.subtract(realDiluentQtyWithYield).doubleValue());
						productLabelItem.setQtyWithYield(productQtyWithYield.subtract(realQtyWithYield).doubleValue());

						BigDecimal ingVol = BigDecimal.valueOf(ingLabelItem.getVolume());
						BigDecimal productVol = BigDecimal.valueOf(productLabelItem.getVolume());
						BigDecimal diluentVolume = rate.multiply(productVol,LabelingFormulaContext.PRECISION).subtract(productVol);
						BigDecimal readlDiluentvolume = ingVol.min(diluentVolume);
						BigDecimal readlDiluentvolumeRatio = (diluentVolume.compareTo(BigDecimal.ZERO) == 0) ? (BigDecimal.valueOf(1d))
								: readlDiluentvolume.divide(diluentVolume, LabelingFormulaContext.PRECISION);
						BigDecimal realVol = readlDiluentvolume.add(productVol.multiply(readlDiluentvolumeRatio,LabelingFormulaContext.PRECISION)).divide(rate,
								LabelingFormulaContext.PRECISION);
						ingLabelItem.setVolume(ingVol.subtract(readlDiluentvolume).doubleValue());
						productLabelItem.setVolume(productVol.subtract(realVol).doubleValue());

						BigDecimal ingVolWithYield = BigDecimal.valueOf(ingLabelItem.getVolume());
						BigDecimal productVolWithYield = BigDecimal.valueOf(productLabelItem.getVolume());
						BigDecimal diluentVolumeWithYield = rate.multiply(productVolWithYield,LabelingFormulaContext.PRECISION).subtract(productVolWithYield);
						BigDecimal readlDiluentvolumeWithYield = ingVol.min(diluentVolumeWithYield);
						BigDecimal readlDiluentvolumeRatioWithYield = (diluentVolumeWithYield.compareTo(BigDecimal.ZERO) == 0)
								? (BigDecimal.valueOf(1d))
								: readlDiluentvolumeWithYield.divide(diluentVolumeWithYield, LabelingFormulaContext.PRECISION);
						BigDecimal realVolWithYield = readlDiluentvolumeWithYield.add(productVolWithYield.multiply(readlDiluentvolumeRatioWithYield,LabelingFormulaContext.PRECISION))
								.divide(rate, LabelingFormulaContext.PRECISION);
						ingLabelItem.setVolumeWithYield(ingVolWithYield.subtract(readlDiluentvolumeWithYield).doubleValue());
						productLabelItem.setVolumeWithYield(productVolWithYield.subtract(realVolWithYield).doubleValue());

						IngItem targetLabelItem = (IngItem) parent.get(reconstituableData.getTargetIngNodeRef());
						if (targetLabelItem == null) {
							targetLabelItem = new IngItem((IngItem) alfrescoRepository.findOne(reconstituableData.getTargetIngNodeRef()));
							targetLabelItem.setQty(0d);
							targetLabelItem.setVolume(0d);
							targetLabelItem.setQtyWithYield(0d);
							targetLabelItem.setVolumeWithYield(0d);
							parent.add(targetLabelItem);
						}

						targetLabelItem.setQty(BigDecimal.valueOf(targetLabelItem.getQty()).add(realQty).add(realDiluentQty).doubleValue());
						targetLabelItem.setVolume(BigDecimal.valueOf(targetLabelItem.getVolume()).add(realVol).add(readlDiluentvolume).doubleValue());
						targetLabelItem.setQtyWithYield(BigDecimal.valueOf(targetLabelItem.getQtyWithYield()).add(realQtyWithYield)
								.add(realDiluentQtyWithYield).doubleValue());
						targetLabelItem.setVolumeWithYield(BigDecimal.valueOf(targetLabelItem.getVolumeWithYield()).add(realVolWithYield)
								.add(readlDiluentvolumeWithYield).doubleValue());

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

						if (targetLabelItem.getQty() < context.getQtyPrecisionThreshold()) {
							parent.getIngList().remove(targetLabelItem.getNodeRef());
						}

						if (productLabelItem.getQty() < context.getQtyPrecisionThreshold()) {
							parent.getIngList().remove(productLabelItem.getNodeRef());
						}

						if (ingLabelItem.getQty() < context.getQtyPrecisionThreshold()) {
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

	private CompositeLabeling visitIngList(CompositeLabeling parent, ProductData product, Composite<IngListDataItem> compositeIngList,
			Double omitQtyPerc, Double qty, Double volume, Double qtyWithYield, Double volumeWithYield, LabelingFormulaContext labelingFormulaContext,
			CompoListDataItem compoListDataItem, Map<String, ReqCtrlListDataItem> errors) {

		boolean applyThreshold = false;
		if (nodeService.hasAspect(product.getNodeRef(), PLMModel.ASPECT_WATER)) {
			applyThreshold = true;
		}

		List<Composite<IngListDataItem>> toAddIngListItem = new ArrayList<>();

		// Only first level of subIng
		if (omitQtyPerc == null) {
			omitQtyPerc = 0d;
		}

		for (Composite<IngListDataItem> ingListItem : compositeIngList.getChildren()) {

			DeclarationType ingDeclarationType = getDeclarationType(compoListDataItem, ingListItem.getData(), labelingFormulaContext);

			if (!DeclarationType.Omit.equals(ingDeclarationType)) {
				toAddIngListItem.add(ingListItem);
			} else if (DeclarationType.Omit.equals(ingDeclarationType)) {
				Double qtyPerc = ingListItem.getData().getQtyPerc();
				if (qtyPerc != null) {

					if (logger.isTraceEnabled()) {
						logger.trace("Removing ingredient " + ingListItem.getData().getName() + " qtyPerc " + qtyPerc);
					}

					omitQtyPerc += qtyPerc;
				}
			}
		}

		if (!toAddIngListItem.isEmpty()) {
			omitQtyPerc /= toAddIngListItem.size();
		}

		for (Composite<IngListDataItem> ingListItem : toAddIngListItem) {

			DeclarationType ingDeclarationType = getDeclarationType(compoListDataItem, ingListItem.getData(), labelingFormulaContext);
			if (!DeclarationType.Omit.equals(ingDeclarationType) && !DeclarationType.DoNotDeclare.equals(ingDeclarationType)) {

				if (DeclarationType.Declare.equals(ingDeclarationType) && !ingListItem.isLeaf()
						&& hasVisibleSubIng(compoListDataItem, ingListItem, labelingFormulaContext)) {
					logger.debug("Declaring ingredient: ");
					visitIngList(parent, product, ingListItem, omitQtyPerc, qty, volume, qtyWithYield, volumeWithYield, labelingFormulaContext,
							compoListDataItem, errors);
				} else {

					if (DeclarationType.Declare.equals(ingDeclarationType)) {
						ingDeclarationType = DeclarationType.DoNotDetails;
					}

					NodeRef ingNodeRef = ingListItem.getData().getIng();
					IngItem ingLabelItem = (IngItem) parent.get(ingNodeRef);
					boolean isNew = true;

					if (ingLabelItem == null) {
						ingLabelItem = new IngItem((IngItem) alfrescoRepository.findOne(ingNodeRef));
						ingLabelItem.getPluralParents().add(ingListItem.getData().getNodeRef());
						ingLabelItem.setDeclarationType(ingDeclarationType);

						parent.add(ingLabelItem);

						if (applyThreshold) {
							labelingFormulaContext.getToApplyThresholdItems().add(ingNodeRef);
						}

						if (logger.isTraceEnabled()) {
							logger.trace("- Add new ing " + getName(ingLabelItem) + " to current Label " + getName(parent));
						}

						if (nodeService.hasAspect(ingNodeRef, PLMModel.ASPECT_WATER)) {

							if (logger.isTraceEnabled()) {
								logger.trace("Detected water lost");
							}

							if (labelingFormulaContext.getEvaporatedDataItems().isEmpty()) {
								labelingFormulaContext.getEvaporatedDataItems().add(new EvaporatedDataItem(ingNodeRef, 100d));
							}

							labelingFormulaContext.getToApplyThresholdItems().add(ingNodeRef);

						}

					} else {
						if (logger.isTraceEnabled()) {
							logger.trace("- Update ing value: " + ingLabelItem.getLegalName(I18NUtil.getContentLocaleLang()));
						}
						isNew = false;
						if (!ingLabelItem.getPluralParents().contains(ingListItem.getData().getNodeRef())) {
							ingLabelItem.getPluralParents().add(ingListItem.getData().getNodeRef());
							ingLabelItem.setPlural(true);
						}
					}

					if (product.getAllergenList() != null) {
						for (AllergenListDataItem allergenListDataItem : product.getAllergenList()) {
							if (Boolean.TRUE.equals(allergenListDataItem.getVoluntary())
									&& (allergenListDataItem.getVoluntarySources().contains(ingNodeRef)
											|| (DeclarationType.DoNotDetails.equals(ingDeclarationType)
													&& allergenMatchSubIngs(allergenListDataItem.getVoluntarySources(), ingListItem)))) {
								if (AllergenType.Major.toString()
										.equals(nodeService.getProperty(allergenListDataItem.getAllergen(), PLMModel.PROP_ALLERGEN_TYPE))) {
									ingLabelItem.getAllergens().add(allergenListDataItem.getAllergen());
								}
							}
						}
					}

					boolean hasGeoTransfo = false;
					if ((ingListItem.getData().getGeoTransfo() != null) && !ingListItem.getData().getGeoTransfo().isEmpty()) {
						hasGeoTransfo = addGeo(ingLabelItem, ingListItem.getData().getGeoTransfo(), PlaceOfActivityTypeCode.LAST_PROCESSING);
					}

					if (ingListItem.getData().getGeoOrigin() != null) {
						addGeo(ingLabelItem, ingListItem.getData().getGeoOrigin(),
								hasGeoTransfo ? PlaceOfActivityTypeCode.EMPTY : PlaceOfActivityTypeCode.LAST_PROCESSING);

					}

					if (ingListItem.getData().getBioOrigin() != null) {
						ingLabelItem.getBioOrigins().addAll(ingListItem.getData().getBioOrigin());
					}

					// Fix #15932
					//					if (product.getGeoOrigins() != null) {
					//						addGeo(ingLabelItem, product.getGeoOrigins(), PlaceOfActivityTypeCode.LAST_PROCESSING);
					//					}

					ingLabelItem.getFootNotes().addAll(extractFootNotes(compoListDataItem, ingListItem.getData(), labelingFormulaContext));

					Double qtyPerc = ingListItem.getData().getQtyPerc();

					if (qtyPerc == null) {

						String message = I18NUtil.getMessage(NULL_ING_ERROR, getName(ingLabelItem));
						ReqCtrlListDataItem error = errors.get(message);
						if (error != null) {
							error.addSource(product.getNodeRef());
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

						ingLabelItem.setQties(null);
						ingLabelItem.setQtyTotal(null);
						ingLabelItem.setVolumeTotal(null);
					} else {

						qtyPerc += omitQtyPerc;

						updateIfNotNull(ingLabelItem.getQty(), qty, qtyPerc, ingLabelItem::setQty, "qty");
						updateIfNotNull(ingLabelItem.getVolume(), volume, qtyPerc, ingLabelItem::setVolume, "volume");

						if ((ingLabelItem.getQty() != null) && (qty != null)) {
							ingLabelItem.setQtyTotal(ingLabelItem.getQty());
						}
						if ((ingLabelItem.getVolume() != null) && (volume != null)) {
							ingLabelItem.setVolumeTotal(ingLabelItem.getVolume());
						}

						updateIfNotNull(ingLabelItem.getQtyWithYield(), qtyWithYield, qtyPerc, ingLabelItem::setQtyWithYield, "qtyWithYield");
						updateIfNotNull(ingLabelItem.getVolumeWithYield(), volumeWithYield, qtyPerc, ingLabelItem::setVolumeWithYield,
								"volumeWithYield");

						if ((ingLabelItem.getQty() == null) && (qty == null)) {

							String message = I18NUtil.getMessage(NULL_ING_ERROR, getName(ingLabelItem));
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
								ingLabelItem.setQties(null);
								ingLabelItem.setQtyTotal(null);
								ingLabelItem.setVolumeTotal(null);
							}

						}
					}

					// Sub ings
					if (!ingListItem.isLeaf() && !DeclarationType.DoNotDeclare.equals(ingDeclarationType)
							&& !DeclarationType.DoNotDetails.equals(ingDeclarationType)) {
						if (logger.isTraceEnabled()) {
							logger.trace(
									" -- Adding subings " + ingListItem.getChildren().size() + " to current " + ingLabelItem.getIngList().size());
						}

						visitIngList(ingLabelItem, product, ingListItem, omitQtyPerc, qty, volume, qty, volume, labelingFormulaContext,
								compoListDataItem, errors);

					} else if (DeclarationType.Detail.equals(ingDeclarationType) && ingLabelItem.getIngList().isEmpty()) {
						ingLabelItem.setDeclarationType(DeclarationType.DoNotDetails);
					}

				}
			}
		}

		return parent;
	}

	private void updateIfNotNull(Double oldValue, Double newValue, Double qtyPerc, Consumer<Double> updateFunction, String name) {
		if (oldValue != null && newValue != null) {
			BigDecimal valueToAdd = BigDecimal.valueOf(newValue).multiply(BigDecimal.valueOf(qtyPerc),LabelingFormulaContext.PRECISION).divide(BigDecimal.valueOf(100d),
					LabelingFormulaContext.PRECISION);
			if (logger.isTraceEnabled()) {
				logger.trace(" -- new value to add: " + valueToAdd.doubleValue() + " - to :" + name);
			}
			updateFunction.accept(valueToAdd.add(BigDecimal.valueOf(oldValue))
					.doubleValue());
		}
	}

	private boolean allergenMatchSubIngs(List<NodeRef> voluntarySources, Composite<IngListDataItem> ingListItem) {
		if ((voluntarySources != null) && !voluntarySources.isEmpty() && !ingListItem.isLeaf()) {
			for (Composite<IngListDataItem> child : ingListItem.getChildren()) {
				if (voluntarySources.contains(child.getData().getIng()) || allergenMatchSubIngs(voluntarySources, child)) {
					return true;
				}

			}

		}
		return false;
	}

	private Boolean addGeo(LabelingComponent labelingComponent, List<NodeRef> geoOrigins, PlaceOfActivityTypeCode defaultActivity) {

		boolean added = false;
		for (NodeRef geo : geoOrigins) {

			@SuppressWarnings("unchecked")
			List<String> placeOfActivityProps = (List<String>) nodeService.getProperty(geo, GS1Model.PROP_PRODUCT_ACTIVITY_TYPE_CODE);

			if ((placeOfActivityProps != null) && !placeOfActivityProps.isEmpty()) {
				for (String placeOfActivityProp : placeOfActivityProps) {
					if (!placeOfActivityProp.isBlank()) {
						labelingComponent.getGeoOriginsByPlaceOfActivity()
								.computeIfAbsent(PlaceOfActivityTypeCode.valueOf(placeOfActivityProp), (a) -> new HashSet<>()).add(geo);
						added = true;
					}
				}

			} else {
				added = true;
				labelingComponent.getGeoOriginsByPlaceOfActivity().computeIfAbsent(defaultActivity, (a) -> new HashSet<>()).add(geo);
			}

		}
		return added;

	}

	private boolean hasVisibleSubIng(CompoListDataItem compoListDataItem, Composite<IngListDataItem> ingListItem,
			LabelingFormulaContext labelingFormulaContext) {
		for (Composite<IngListDataItem> subIngListItem : ingListItem.getChildren()) {

			DeclarationType ingDeclarationType = getDeclarationType(compoListDataItem, subIngListItem.getData(), labelingFormulaContext);
			if (!DeclarationType.Omit.equals(ingDeclarationType) && !DeclarationType.DoNotDeclare.equals(ingDeclarationType)) {
				return true;
			}

		}

		return false;
	}

	private ReqCtrlListDataItem createError(CompositeLabeling ingItem, NodeRef productNodeRef) {
		List<NodeRef> sourceNodeRefs = new ArrayList<>();
		if (productNodeRef != null) {
			sourceNodeRefs.add(productNodeRef);
		}
		return new ReqCtrlListDataItem(null, RequirementType.Forbidden, MLTextHelper.getI18NMessage(NULL_ING_ERROR, getName(ingItem)), null,
				sourceNodeRefs, RequirementDataType.Labelling);

	}

	private DeclarationType getDeclarationType(CompoListDataItem compoListDataItem, IngListDataItem ingListDataItem,
			LabelingFormulaContext labelingFormulaContext) {

		if (ingListDataItem == null) {
			if (labelingFormulaContext.getNodeDeclarationFilters().containsKey(compoListDataItem.getProduct())) {
				for (DeclarationFilterRule declarationFilter : labelingFormulaContext.getNodeDeclarationFilters()
						.get(compoListDataItem.getProduct())) {
					if (!declarationFilter.isThreshold() && ((declarationFilter.getFormula() == null) || labelingFormulaContext
							.matchFormule(declarationFilter, new LabelingFormulaFilterContext(formulaService, compoListDataItem, ingListDataItem)))) {

						if (logger.isTraceEnabled()) {
							logger.trace(" -- Found declType : " + declarationFilter.getDeclarationType() + " for "
									+ nodeService.getProperty(compoListDataItem.getProduct(), ContentModel.PROP_NAME));
						}
						return declarationFilter.getDeclarationType();
					}
				}
			}

		} else {

			if (labelingFormulaContext.getNodeDeclarationFilters().containsKey(ingListDataItem.getIng())) {
				for (DeclarationFilterRule declarationFilter : labelingFormulaContext.getNodeDeclarationFilters().get(ingListDataItem.getIng())) {
					if (!declarationFilter.isThreshold() && ((declarationFilter.getFormula() == null) || labelingFormulaContext
							.matchFormule(declarationFilter, new LabelingFormulaFilterContext(formulaService, compoListDataItem, ingListDataItem)))) {
						if (logger.isTraceEnabled()) {
							logger.trace(" -- Found declType : " + declarationFilter.getDeclarationType() + " for "
									+ nodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_CHARACT_NAME));
						}
						return declarationFilter.getDeclarationType();
					}
				}
			}

			for (DeclarationFilterRule declarationFilter : labelingFormulaContext.getDeclarationFilters()) {
				if (!declarationFilter.isThreshold() && (declarationFilter.getFormula() != null) && labelingFormulaContext
						.matchFormule(declarationFilter, new LabelingFormulaFilterContext(formulaService, compoListDataItem, ingListDataItem))) {
					if (logger.isTraceEnabled()) {
						logger.trace(" -- Found declType : " + declarationFilter.getDeclarationType() + " for "
								+ nodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_CHARACT_NAME));
					}
					return declarationFilter.getDeclarationType();
				}
			}

			DeclarationType declType = ingListDataItem.getDeclType();
			if ((declType != null)) {
				if (logger.isTraceEnabled()) {
					logger.trace(" -- Found declType : " + declType + " for "
							+ nodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_CHARACT_NAME));
				}

				return declType;
			}

			if (logger.isTraceEnabled()) {
				logger.trace(" -- Default declType : " + DeclarationType.Detail + " for default "
						+ nodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_CHARACT_NAME));
			}

			return DeclarationType.Detail;
		}

		if (logger.isTraceEnabled()) {

			logger.trace(" -- Found declType : " + compoListDataItem.getDeclType() + " for default "
					+ nodeService.getProperty(compoListDataItem.getProduct(), ContentModel.PROP_NAME));

		}
		return compoListDataItem.getDeclType();
	}

	private Set<FootNoteRule> extractFootNotes(CompoListDataItem compoListDataItem, IngListDataItem ingListDataItem,
			LabelingFormulaContext labelingFormulaContext) {

		Set<FootNoteRule> footNoteRules = new HashSet<>();

		if (ingListDataItem == null) {
			if (labelingFormulaContext.getNodeFootNoteRules().containsKey(compoListDataItem.getProduct())) {
				for (FootNoteRule footNoteRule : labelingFormulaContext.getNodeFootNoteRules().get(compoListDataItem.getProduct())) {
					if ((((footNoteRule.getFormula() == null) || footNoteRule.getFormula().isBlank()) || labelingFormulaContext
							.matchFormule(footNoteRule, new LabelingFormulaFilterContext(formulaService, compoListDataItem, ingListDataItem)))) {

						if (logger.isTraceEnabled()) {
							logger.trace(" -- Found footNote : " + footNoteRule.getRuleName() + " for "
									+ nodeService.getProperty(compoListDataItem.getProduct(), ContentModel.PROP_NAME));
						}
						labelingFormulaContext.getFootNotes().add(footNoteRule);
						footNoteRules.add(footNoteRule);
					}
				}
			}

		} else {

			if (labelingFormulaContext.getNodeFootNoteRules().containsKey(ingListDataItem.getIng())) {
				for (FootNoteRule footNoteRule : labelingFormulaContext.getNodeFootNoteRules().get(ingListDataItem.getIng())) {
					if ((((footNoteRule.getFormula() == null) || footNoteRule.getFormula().isBlank()) || labelingFormulaContext
							.matchFormule(footNoteRule, new LabelingFormulaFilterContext(formulaService, compoListDataItem, ingListDataItem)))) {

						if (logger.isTraceEnabled()) {
							logger.trace(" -- Found footNote : " + footNoteRule.getRuleName() + " for "
									+ nodeService.getProperty(compoListDataItem.getProduct(), ContentModel.PROP_NAME));
						}
						labelingFormulaContext.getFootNotes().add(footNoteRule);
						footNoteRules.add(footNoteRule);
					}
				}
			}
		}

		for (FootNoteRule footNoteRule : labelingFormulaContext.getFootNoteRules()) {
			if ((((footNoteRule.getFormula() == null) || footNoteRule.getFormula().isBlank()) || labelingFormulaContext.matchFormule(footNoteRule,
					new LabelingFormulaFilterContext(formulaService, compoListDataItem, ingListDataItem)))) {

				if (logger.isTraceEnabled()) {
					logger.trace(" -- Found footNote : " + footNoteRule.getRuleName() + " for "
							+ nodeService.getProperty(compoListDataItem.getProduct(), ContentModel.PROP_NAME));
				}
				labelingFormulaContext.getFootNotes().add(footNoteRule);
				footNoteRules.add(footNoteRule);
			}
		}

		return footNoteRules;
	}

}
