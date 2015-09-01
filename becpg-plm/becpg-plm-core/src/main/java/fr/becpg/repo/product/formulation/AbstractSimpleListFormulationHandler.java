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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.data.spel.SpelHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.model.ForecastValueDataItem;
import fr.becpg.repo.repository.model.FormulatedCharactDataItem;
import fr.becpg.repo.repository.model.MinMaxValueDataItem;
import fr.becpg.repo.repository.model.SimpleListDataItem;
import fr.becpg.repo.variant.filters.VariantFilters;

public abstract class AbstractSimpleListFormulationHandler<T extends SimpleListDataItem> extends FormulationBaseHandler<ProductData> {

	public static final String UNIT_SEPARATOR = "/";
	public static final String MESSAGE_UNDEFINED_CHARACT = "message.formulate.undefined.charact";

	private static final Log logger = LogFactory.getLog(AbstractSimpleListFormulationHandler.class);

	protected AlfrescoRepository<T> alfrescoRepository;

	protected EntityListDAO entityListDAO;

	protected NodeService nodeService;

	protected boolean transientFormulation = false;
	
	protected FormulaService formulaService;
	
	public void setFormulaService(FormulaService formulaService) {
		this.formulaService = formulaService;
	}

	public void setTransientFormulation(boolean transientFormulation) {
		this.transientFormulation = transientFormulation;
	}

	public void setAlfrescoRepository(AlfrescoRepository<T> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public T createNewInstance() throws InstantiationException, IllegalAccessException {
		return getInstanceClass().newInstance();
	}

	protected abstract Class<T> getInstanceClass();

	protected abstract QName getDataListVisited();

	protected abstract Map<NodeRef, List<NodeRef>> getMandatoryCharacts(ProductData formulatedProduct, QName componentType);

	protected Map<NodeRef, List<NodeRef>> getMandatoryCharactsFromList(List<T> simpleListDataList) {
		Map<NodeRef, List<NodeRef>> mandatoryCharacts = new HashMap<>(simpleListDataList.size());
		for (SimpleListDataItem sl : simpleListDataList) {
			if (isCharactFormulated(sl)) {
				mandatoryCharacts.put(sl.getCharactNodeRef(), new ArrayList<NodeRef>());
			}
		}
		return mandatoryCharacts;
	}

	protected void formulateSimpleList(ProductData formulatedProduct, List<T> simpleListDataList) throws FormulateException {
		logger.debug("formulateSimpleList");

		if (simpleListDataList != null) {
			for (SimpleListDataItem sl : simpleListDataList) {
				// reset value if formulated
				if (isCharactFormulated(sl)) {
					sl.setValue(null);

					if (sl instanceof MinMaxValueDataItem) {
						((MinMaxValueDataItem) sl).setMini(null);
						((MinMaxValueDataItem) sl).setMaxi(null);
					}
					if (sl instanceof ForecastValueDataItem) {
						((ForecastValueDataItem) sl).setPreviousValue(null);
						((ForecastValueDataItem) sl).setFutureValue(null);
					}

					// add detailable aspect
					if (!sl.getAspects().contains(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM)) {
						sl.getAspects().add(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM);
					}
				}
			}
		}

		copyProductTemplateList(formulatedProduct, simpleListDataList);

		visitChildren(formulatedProduct, simpleListDataList);

		// sort
		// sort(simpleListDataList);
	}

	protected void visitChildren(ProductData formulatedProduct, List<T> simpleListDataList) throws FormulateException {

		Map<NodeRef, Double> totalQtiesValue = new HashMap<>();

		Double netQty = FormulationHelper.getNetQtyInLorKg(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT);

		if (formulatedProduct.hasCompoListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {

			Map<NodeRef, List<NodeRef>> mandatoryCharacts = getMandatoryCharacts(formulatedProduct, PLMModel.TYPE_RAWMATERIAL);

			for (CompoListDataItem compoItem : formulatedProduct.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
				Double weight = FormulationHelper.getQtyInKg(compoItem);
				Double vol = FormulationHelper.getNetVolume(compoItem, nodeService);

				if (weight != null) {
					visitPart(compoItem.getProduct(), simpleListDataList, weight, vol, netQty, mandatoryCharacts, totalQtiesValue, formulatedProduct instanceof RawMaterialData);
				}
			}

			addReqCtrlList(formulatedProduct.getCompoListView().getReqCtrlList(), mandatoryCharacts);
		}

		// Case Generic MP
		if (formulatedProduct instanceof RawMaterialData) {
			formulateGenericRawMaterial(simpleListDataList, totalQtiesValue, netQty);
		}

	}

	protected void formulateGenericRawMaterial(List<T> simpleListDataList, Map<NodeRef, Double> totalQtiesValue, Double netQty) {
		if (logger.isDebugEnabled()) {
			logger.debug("Case generic MP adjust value to total");
		}
		for (SimpleListDataItem newSimpleListDataItem : simpleListDataList) {
			if (totalQtiesValue.containsKey(newSimpleListDataItem.getCharactNodeRef())) {
				Double totalQty = totalQtiesValue.get(newSimpleListDataItem.getCharactNodeRef());
				if (newSimpleListDataItem.getValue() != null) {
					newSimpleListDataItem.setValue(newSimpleListDataItem.getValue() * netQty / totalQty);
				}
			}
		}
	}

	protected void addReqCtrlList(List<ReqCtrlListDataItem> reqCtrlList, Map<NodeRef, List<NodeRef>> mandatoryCharacts) {

		// ReqCtrlList
		for (Map.Entry<NodeRef, List<NodeRef>> mandatoryCharact : mandatoryCharacts.entrySet()) {
			if (mandatoryCharact.getValue() != null && !mandatoryCharact.getValue().isEmpty()) {
				String message = I18NUtil.getMessage(MESSAGE_UNDEFINED_CHARACT, nodeService.getProperty(mandatoryCharact.getKey(), BeCPGModel.PROP_CHARACT_NAME));

				reqCtrlList.add(new ReqCtrlListDataItem(null, RequirementType.Tolerated, message, mandatoryCharact.getKey(), mandatoryCharact.getValue()));
			}
		}
	}

	/**
	 * Visit part.
	 * 
	 * @param valueCount
	 *
	 */
	protected void visitPart(NodeRef componentNodeRef, List<T> simpleListDataList, Double weightUsed, Double volUsed, Double netQty, Map<NodeRef, List<NodeRef>> mandatoryCharacts,
			Map<NodeRef, Double> totalQtiesValue, boolean isGenericRawMaterial) throws FormulateException {

		if (!PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT.equals(nodeService.getType(componentNodeRef))) {

			List<? extends SimpleListDataItem> componentSimpleListDataList = alfrescoRepository.loadDataList(componentNodeRef, getDataListVisited(), getDataListVisited());

			if (!alfrescoRepository.hasDataList(componentNodeRef, getDataListVisited()) || componentSimpleListDataList.isEmpty()) {

				logger.debug("simpleListDataList " + getDataListVisited() + " is null or empty");
				for (NodeRef charactNodeRef : mandatoryCharacts.keySet()) {
					addMissingMandatoryCharact(mandatoryCharacts, charactNodeRef, componentNodeRef);
				}
				return;
			} else {

				for (SimpleListDataItem newSimpleListDataItem : simpleListDataList) {
					if (newSimpleListDataItem.getCharactNodeRef() != null && isCharactFormulated(newSimpleListDataItem)) {

						// calculate charact from qty or vol ?
						Double qtyUsed = isCharactFormulatedFromVol(newSimpleListDataItem) || FormulationHelper.isProductUnitLiter(FormulationHelper.getProductUnit(componentNodeRef, nodeService)) ? volUsed
								: weightUsed;

						// look for charact in component
						SimpleListDataItem slDataItem = null;
						for (SimpleListDataItem s : componentSimpleListDataList) {
							if (newSimpleListDataItem.getCharactNodeRef() != null && s.getCharactNodeRef() != null && newSimpleListDataItem.getCharactNodeRef().equals(s.getCharactNodeRef())) {
								slDataItem = s;
								break;
							}
						}

						// is it a mandatory charact ?
						if (slDataItem == null || (slDataItem.getValue() == null)) {

							if (slDataItem instanceof MinMaxValueDataItem) {
								if (((MinMaxValueDataItem) slDataItem).getMaxi() == null && ((MinMaxValueDataItem) slDataItem).getMini() == null) {
									addMissingMandatoryCharact(mandatoryCharacts, newSimpleListDataItem.getCharactNodeRef(), componentNodeRef);
								}
							} else {
								addMissingMandatoryCharact(mandatoryCharacts, newSimpleListDataItem.getCharactNodeRef(), componentNodeRef);
							}
						}

						// Calculate values
						if (slDataItem != null && qtyUsed != null) {

							calculate(newSimpleListDataItem, slDataItem, qtyUsed, netQty, isGenericRawMaterial);

							if (totalQtiesValue != null && slDataItem.getValue() != null) {
								Double currentQty = totalQtiesValue.get(newSimpleListDataItem.getCharactNodeRef());
								if (currentQty == null) {
									currentQty = 0d;
								}
								totalQtiesValue.put(newSimpleListDataItem.getCharactNodeRef(), currentQty + qtyUsed);
							}
						}
					}
				}
			}
		}
	}

	protected void calculate(SimpleListDataItem newSimpleListDataItem, SimpleListDataItem slDataItem, Double qtyUsed, Double netQty, boolean isGenericRawMaterial) {
		// newValue, newMini, newMaxi : usefull to know if one mini or maxi is
		// filled on 1 raw material

		Double formulatedValue = 0d;
		if (newSimpleListDataItem instanceof FormulatedCharactDataItem) {
			formulatedValue = ((FormulatedCharactDataItem) newSimpleListDataItem).getFormulatedValue();
		} else {
			formulatedValue = newSimpleListDataItem.getValue();
		}

		Double newValue = formulatedValue != null ? formulatedValue : 0d;
		Double value = slDataItem.getValue();
		if (value != null) {
			newSimpleListDataItem.setValue(FormulationHelper.calculateValue(formulatedValue, qtyUsed, slDataItem.getValue(), netQty));
		} else {
			value = 0d;
		}
		if (slDataItem instanceof MinMaxValueDataItem) {
			if (isGenericRawMaterial) {
				if (((MinMaxValueDataItem) slDataItem).getMini() != null
						&& (((MinMaxValueDataItem) newSimpleListDataItem).getMini() == null || (((MinMaxValueDataItem) newSimpleListDataItem).getMini() != null && ((MinMaxValueDataItem) newSimpleListDataItem)
								.getMini() > ((MinMaxValueDataItem) slDataItem).getMini()))) {
					((MinMaxValueDataItem) newSimpleListDataItem).setMini(((MinMaxValueDataItem) slDataItem).getMini());
				}
				if (((MinMaxValueDataItem) slDataItem).getMaxi() != null
						&& (((MinMaxValueDataItem) newSimpleListDataItem).getMaxi() == null || (((MinMaxValueDataItem) newSimpleListDataItem).getMaxi() != null && ((MinMaxValueDataItem) newSimpleListDataItem)
								.getMaxi() < ((MinMaxValueDataItem) slDataItem).getMaxi()))) {
					((MinMaxValueDataItem) newSimpleListDataItem).setMaxi(((MinMaxValueDataItem) slDataItem).getMaxi());
				}
			} else {
				Double newMini = ((MinMaxValueDataItem) newSimpleListDataItem).getMini() != null ? ((MinMaxValueDataItem) newSimpleListDataItem).getMini() : newValue;
				Double newMaxi = ((MinMaxValueDataItem) newSimpleListDataItem).getMaxi() != null ? ((MinMaxValueDataItem) newSimpleListDataItem).getMaxi() : newValue;
				Double miniValue = ((MinMaxValueDataItem) slDataItem).getMini() != null ? ((MinMaxValueDataItem) slDataItem).getMini() : value;
				Double maxiValue = ((MinMaxValueDataItem) slDataItem).getMaxi() != null ? ((MinMaxValueDataItem) slDataItem).getMaxi() : value;
				if (miniValue < value || newMini < newValue || ((MinMaxValueDataItem) newSimpleListDataItem).getMini() != null) {
					((MinMaxValueDataItem) newSimpleListDataItem).setMini(FormulationHelper.calculateValue(newMini, qtyUsed, miniValue, netQty));
				}
				if (maxiValue > value || newMaxi > newValue || ((MinMaxValueDataItem) newSimpleListDataItem).getMaxi() != null) {
					((MinMaxValueDataItem) newSimpleListDataItem).setMaxi(FormulationHelper.calculateValue(newMaxi, qtyUsed, maxiValue, netQty));
				}
			}
		}

		if (newSimpleListDataItem instanceof ForecastValueDataItem) {
			((ForecastValueDataItem) newSimpleListDataItem).setPreviousValue(FormulationHelper.calculateValue(((ForecastValueDataItem) newSimpleListDataItem).getPreviousValue(), qtyUsed,
					((ForecastValueDataItem) slDataItem).getPreviousValue(), netQty));
			((ForecastValueDataItem) newSimpleListDataItem).setFutureValue(FormulationHelper.calculateValue(((ForecastValueDataItem) newSimpleListDataItem).getFutureValue(), qtyUsed,
					((ForecastValueDataItem) slDataItem).getFutureValue(), netQty));
		}

		if (logger.isDebugEnabled()) {
			logger.debug("valueToAdd = qtyUsed * value : " + qtyUsed + " * " + slDataItem.getValue());
			if (newSimpleListDataItem.getNodeRef() != null) {
				logger.debug("charact: " + nodeService.getProperty(newSimpleListDataItem.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME) + " - newValue : " + newSimpleListDataItem.getValue());
				if (newSimpleListDataItem instanceof MinMaxValueDataItem) {
					logger.debug("charact: " + nodeService.getProperty(newSimpleListDataItem.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME) + " - newMini : "
							+ ((MinMaxValueDataItem) newSimpleListDataItem).getMini());
					logger.debug("charact: " + nodeService.getProperty(newSimpleListDataItem.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME) + " - newMaxi : "
							+ ((MinMaxValueDataItem) newSimpleListDataItem).getMaxi());
				}
				if (newSimpleListDataItem instanceof ForecastValueDataItem) {
					logger.debug("charact: " + nodeService.getProperty(newSimpleListDataItem.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME) + " - previousValue : "
							+ ((ForecastValueDataItem) newSimpleListDataItem).getPreviousValue());
					logger.debug("charact: " + nodeService.getProperty(newSimpleListDataItem.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME) + " - futureValue : "
							+ ((ForecastValueDataItem) newSimpleListDataItem).getFutureValue());
				}
			}
		}
	}

	protected void addMissingMandatoryCharact(Map<NodeRef, List<NodeRef>> mandatoryCharacts, NodeRef charactNodeRef, NodeRef componentNodeRef) {
		if (mandatoryCharacts.containsKey(charactNodeRef)) {
			List<NodeRef> sources = mandatoryCharacts.get(charactNodeRef);
			if (sources == null) {
				sources = mandatoryCharacts.put(charactNodeRef, new ArrayList<NodeRef>());
			}
			if (!sources.contains(componentNodeRef)) {
				sources.add(componentNodeRef);
			}

		}
	}

	protected boolean isCharactFormulated(SimpleListDataItem sl) {
		return sl.getIsManual() == null || !sl.getIsManual();
	}

	protected boolean isCharactFormulatedFromVol(SimpleListDataItem sl) {
		return false;
	}

	protected T findSimpleListDataItem(List<T> simpleList, NodeRef charactNodeRef) {
		if (charactNodeRef != null) {
			for (T s : simpleList) {
				if (charactNodeRef.equals(s.getCharactNodeRef())) {
					return s;
				}
			}
		}
		return null;
	}
	

	protected <U extends FormulatedCharactDataItem> void computeFormulatedList(ProductData formulatedProduct, List<U> formulatedCharactDataItems, QName propFormula, String errorKey) {
    	
		if (formulatedCharactDataItems != null) {
			
			ExpressionParser parser = new SpelExpressionParser();
			StandardEvaluationContext context = formulaService.createEvaluationContext(formulatedProduct);
			
			for (FormulatedCharactDataItem formulatedCharactDataItem : formulatedCharactDataItems) {
				String error = null;
				formulatedCharactDataItem.setIsFormulated(false);
				formulatedCharactDataItem.setErrorLog(null);
				if ((formulatedCharactDataItem.getIsManual() == null || !formulatedCharactDataItem.getIsManual()) && formulatedCharactDataItem.getCharactNodeRef() != null) {

					String formula = (String) nodeService.getProperty(formulatedCharactDataItem.getCharactNodeRef(), propFormula );
					if (formula != null && formula.length() > 0) {
						try {
							formulatedCharactDataItem.setIsFormulated(true);							
							formula = SpelHelper.formatFormula(formula);

							Expression exp = parser.parseExpression(formula);
							Object ret = exp.getValue(context);
							if (ret instanceof Double) {
								formulatedCharactDataItem.setValue((Double) ret);

								if (formula.contains(".value") && formulatedCharactDataItem instanceof MinMaxValueDataItem) {
									try {
										exp = parser.parseExpression(formula.replace(".value", ".mini"));
										((MinMaxValueDataItem) formulatedCharactDataItem).setMini((Double) exp.getValue(context));
										exp = parser.parseExpression(formula.replace(".value", ".maxi"));
										((MinMaxValueDataItem)formulatedCharactDataItem).setMaxi((Double) exp.getValue(context));
									} catch (Exception e) {
										((MinMaxValueDataItem)formulatedCharactDataItem).setMaxi(null);
										((MinMaxValueDataItem)formulatedCharactDataItem).setMini(null);
										if (logger.isDebugEnabled()) {
											logger.debug("Error in formula :" + formula, e);
										}
									}
								}

							} else {
								error = I18NUtil.getMessage("message.formulate.formula.incorrect.type.double",
										Locale.getDefault());
							}

						} catch (Exception e) {
							error = e.getLocalizedMessage();							
							if (logger.isDebugEnabled()) {
								logger.debug("Error in formula :" + SpelHelper.formatFormula(formula), e);
							}
						}
					}
				}

				if (error != null) {
					formulatedCharactDataItem.setValue(null);
					formulatedCharactDataItem.setErrorLog(error);
					String message = I18NUtil.getMessage(errorKey, Locale.getDefault(),
							nodeService.getProperty(formulatedCharactDataItem.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME), error);
					formulatedProduct.getCompoListView().getReqCtrlList()
							.add(new ReqCtrlListDataItem(null, RequirementType.Tolerated, message,formulatedCharactDataItem.getCharactNodeRef(), new ArrayList<NodeRef>()));
				}

			}
		}

	}

	

	/**
	 * Copy missing item from template
	 * 
	 * @param formulatedProduct
	 * @param simpleListDataList
	 */
	protected void copyProductTemplateList(ProductData formulatedProduct, List<T> simpleListDataList) {

		if (formulatedProduct.getEntityTpl() != null && !formulatedProduct.getEntityTpl().equals(formulatedProduct)) {

			// TODO do not use loadDataList
			List<T> templateSimpleListDataList = alfrescoRepository.loadDataList(formulatedProduct.getEntityTpl().getNodeRef(), getDataListVisited(), getDataListVisited());

			for (T tsl : templateSimpleListDataList) {
				if (tsl.getCharactNodeRef() != null) {
					boolean isFound = false;
					for (T sl : simpleListDataList) {
						if (tsl.getCharactNodeRef().equals(sl.getCharactNodeRef())) {
							isFound = true;
							break;
						}
					}
					if (!isFound) {
						tsl.setNodeRef(null);
						tsl.setParentNodeRef(null);
						simpleListDataList.add(tsl);
					}

				}
			}

		}
	}
}
