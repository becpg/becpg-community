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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.product.formulation;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.data.hierarchicalList.CompositeDataItem;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.formulation.spel.SpelFormulaService;
import fr.becpg.repo.formulation.spel.SpelHelper;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.model.ForecastValueDataItem;
import fr.becpg.repo.repository.model.FormulatedCharactDataItem;
import fr.becpg.repo.repository.model.MinMaxValueDataItem;
import fr.becpg.repo.repository.model.SimpleListDataItem;
import fr.becpg.repo.repository.model.VariantAwareDataItem;
import fr.becpg.repo.variant.filters.VariantFilters;
import fr.becpg.repo.variant.model.VariantData;

/**
 * <p>Abstract AbstractSimpleListFormulationHandler class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class AbstractSimpleListFormulationHandler<T extends SimpleListDataItem> extends FormulationBaseHandler<ProductData> {

	/** Constant <code>UNIT_SEPARATOR="/"</code> */
	public static final String UNIT_SEPARATOR = "/";
	/** Constant <code>MESSAGE_UNDEFINED_CHARACT="message.formulate.undefined.charact"</code> */
	protected static final String MESSAGE_UNDEFINED_CHARACT = "message.formulate.undefined.charact";

	private static final Log logger = LogFactory.getLog(AbstractSimpleListFormulationHandler.class);

	protected AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	protected EntityListDAO entityListDAO;

	protected NodeService nodeService;

	protected NodeService mlNodeService;

	protected boolean transientFormulation = false;

	protected SpelFormulaService formulaService;

	/**
	 * <p>Setter for the field <code>formulaService</code>.</p>
	 *
	 * @param formulaService a {@link fr.becpg.repo.formulation.spel.SpelFormulaService} object.
	 */
	public void setFormulaService(SpelFormulaService formulaService) {
		this.formulaService = formulaService;
	}

	/**
	 * <p>Setter for the field <code>transientFormulation</code>.</p>
	 *
	 * @param transientFormulation a boolean.
	 */
	public void setTransientFormulation(boolean transientFormulation) {
		this.transientFormulation = transientFormulation;
	}

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/**
	 * <p>Setter for the field <code>entityListDAO</code>.</p>
	 *
	 * @param entityListDAO a {@link fr.becpg.repo.entity.EntityListDAO} object.
	 */
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
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
	 * <p>Setter for the field <code>mlNodeService</code>.</p>
	 *
	 * @param mlNodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setMlNodeService(NodeService mlNodeService) {
		this.mlNodeService = mlNodeService;
	}

	/**
	 * <p>createNewInstance.</p>
	 *
	 * @return a T object.
	 * @throws java.lang.InstantiationException if any.
	 * @throws java.lang.IllegalAccessException if any.
	 * @throws java.lang.IllegalArgumentException if any.
	 * @throws java.lang.reflect.InvocationTargetException if any.
	 * @throws java.lang.NoSuchMethodException if any.
	 * @throws java.lang.SecurityException if any.
	 */
	public T createNewInstance() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return getInstanceClass().getDeclaredConstructor().newInstance();
	}

	/**
	 * <p>getInstanceClass.</p>
	 *
	 * @return a {@link java.lang.Class} object.
	 */
	protected abstract Class<T> getInstanceClass();

	/**
	 * <p>accept.</p>
	 *
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @return a boolean.
	 */
	protected abstract boolean accept(ProductData productData);

	/**
	 * <p>getDataListVisited.</p>
	 *
	 * @param partProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @return a {@link java.util.List} object.
	 */
	protected abstract List<T> getDataListVisited(ProductData partProduct);

	/**
	 * <p>getMandatoryCharacts.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param componentType a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link java.util.Map} object.
	 */
	protected abstract Map<NodeRef, List<NodeRef>> getMandatoryCharacts(ProductData formulatedProduct, QName componentType);

	/**
	 * <p>getRequirementDataType.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.constraints.RequirementDataType} object.
	 */
	protected abstract RequirementDataType getRequirementDataType();

	/**
	 * <p>getMandatoryCharactsFromList.</p>
	 *
	 * @param simpleListDataList a {@link java.util.List} object.
	 * @return a {@link java.util.Map} object.
	 */
	protected Map<NodeRef, List<NodeRef>> getMandatoryCharactsFromList(List<T> simpleListDataList) {
		Map<NodeRef, List<NodeRef>> mandatoryCharacts = new HashMap<>(simpleListDataList.size());
		for (SimpleListDataItem sl : simpleListDataList) {
			if (isCharactFormulated(sl)) {
				mandatoryCharacts.put(sl.getCharactNodeRef(), new ArrayList<>());
			}
		}
		return mandatoryCharacts;
	}

	/**
	 * <p>formulateSimpleList.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param simpleListDataList a {@link java.util.List} object.
	 * @param isFormulatedProduct a boolean.
	 * @throws fr.becpg.repo.formulation.FormulateException if any.
	 */
	protected void formulateSimpleList(ProductData formulatedProduct, List<T> simpleListDataList, boolean isFormulatedProduct) {
		logger.debug("formulateSimpleList");
		cleanSimpleList(simpleListDataList, isFormulatedProduct);

		synchronizeTemplate(formulatedProduct, simpleListDataList);

		if (isFormulatedProduct) {
			visitChildren(formulatedProduct, simpleListDataList,
					FormulationHelper.getNetQtyInLorKg(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT), null);
			for (VariantData variant : formulatedProduct.getVariants()) {
				visitChildren(formulatedProduct, simpleListDataList,
						FormulationHelper.getNetQtyInLorKg(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT), variant);
			}
		}

	}

	/**
	 * <p>cleanSimpleList.</p>
	 *
	 * @param simpleListDataList a {@link java.util.List} object.
	 * @param isFormulatedProduct a boolean.
	 */
	protected void cleanSimpleList(List<T> simpleListDataList, boolean isFormulatedProduct) {

		if ((simpleListDataList != null) && isFormulatedProduct) {
			simpleListDataList.forEach(sl -> {
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
					if (sl instanceof VariantAwareDataItem) {
						for (int i = 1; i <= VariantAwareDataItem.VARIANT_COLUMN_SIZE; i++) {
							((VariantAwareDataItem) sl).setValue(null, VariantAwareDataItem.VARIANT_COLUMN_NAME + i);
						}
					}

					// add detailable aspect
					if (!sl.getAspects().contains(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM)) {
						sl.getAspects().add(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM);
					}
				}
			});
		}
	}

	/**
	 * <p>visitChildren.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param simpleListDataList a {@link java.util.List} object.
	 * @param netQtyInLorKg a {@link java.lang.Double} object.
	 * @param variant a {@link fr.becpg.repo.variant.model.VariantData} object.
	 * @throws fr.becpg.repo.formulation.FormulateException if any.
	 */
	protected void visitChildren(ProductData formulatedProduct, List<T> simpleListDataList, Double netQtyInLorKg, VariantData variant) {

		Map<NodeRef, Double> totalQtiesValue = new HashMap<>();

		boolean isGenericRawMaterial = formulatedProduct instanceof RawMaterialData;

		Double netWeight = FormulationHelper.getNetWeight(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT);

		if (formulatedProduct.hasCompoListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE),
				(variant != null ? new VariantFilters<>(variant.getNodeRef()) : new VariantFilters<>())))) {
			Map<NodeRef, List<NodeRef>> mandatoryCharacts = getMandatoryCharacts(formulatedProduct, PLMModel.TYPE_RAWMATERIAL);
			for (CompoListDataItem compoItem : formulatedProduct.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE),
					(variant != null ? new VariantFilters<>(variant.getNodeRef()) : new VariantFilters<>())))) {
				Double weight = FormulationHelper.getQtyInKg(compoItem);
				// omit item if parent is omitted
				boolean omit = false;
				CompoListDataItem tmpCompoItem = compoItem;
				while ((tmpCompoItem != null) && !omit) {
					omit = DeclarationType.Omit.equals(tmpCompoItem.getDeclType());
					tmpCompoItem = tmpCompoItem.getParent();
				}

				if ((weight != null) && !omit  && compoItem!=null) {

					ProductData partProduct = (ProductData) alfrescoRepository.findOne(compoItem.getProduct());
					Double vol = FormulationHelper.getNetVolume(compoItem, partProduct);

					visitPart(formulatedProduct, partProduct, simpleListDataList, weight, vol, netQtyInLorKg, netWeight, mandatoryCharacts,
							totalQtiesValue, isGenericRawMaterial, variant);
				}
			}
			addReqCtrlList(formulatedProduct.getReqCtrlList(), mandatoryCharacts, getRequirementDataType());

		}

		// Case Generic MP
		if (isGenericRawMaterial) {
			formulateGenericRawMaterial(simpleListDataList, totalQtiesValue, netQtyInLorKg);
		}

	}

	/**
	 * <p>formulateGenericRawMaterial.</p>
	 *
	 * @param simpleListDataList a {@link java.util.List} object.
	 * @param totalQtiesValue a {@link java.util.Map} object.
	 * @param netQty a {@link java.lang.Double} object.
	 */
	protected void formulateGenericRawMaterial(List<T> simpleListDataList, Map<NodeRef, Double> totalQtiesValue, Double netQty) {
		if (logger.isDebugEnabled()) {
			logger.debug("Case generic MP adjust value to total");
		}
		for (SimpleListDataItem newSimpleListDataItem : simpleListDataList) {
			if (totalQtiesValue.containsKey(newSimpleListDataItem.getCharactNodeRef())) {
				Double totalQty = totalQtiesValue.get(newSimpleListDataItem.getCharactNodeRef());
				if ((newSimpleListDataItem.getValue() != null) && (netQty != null) && (totalQty != null) && (totalQty != 0d)) {
					newSimpleListDataItem.setValue((newSimpleListDataItem.getValue() * netQty) / totalQty);
				}
			}
		}
	}

	/**
	 * <p>addReqCtrlList.</p>
	 *
	 * @param reqCtrlList a {@link java.util.List} object.
	 * @param mandatoryCharacts a {@link java.util.Map} object.
	 * @param dataType a {@link fr.becpg.repo.product.data.constraints.RequirementDataType} object.
	 */
	protected void addReqCtrlList(List<ReqCtrlListDataItem> reqCtrlList, Map<NodeRef, List<NodeRef>> mandatoryCharacts,
			RequirementDataType dataType) {

		// ReqCtrlList
		for (Map.Entry<NodeRef, List<NodeRef>> mandatoryCharact : mandatoryCharacts.entrySet()) {
			if ((mandatoryCharact.getValue() != null) && !mandatoryCharact.getValue().isEmpty()) {

				reqCtrlList.add(new ReqCtrlListDataItem(null, RequirementType.Tolerated,
						MLTextHelper.getI18NMessage(MESSAGE_UNDEFINED_CHARACT,
								mlNodeService.getProperty(mandatoryCharact.getKey(), BeCPGModel.PROP_CHARACT_NAME)),
						mandatoryCharact.getKey(), mandatoryCharact.getValue(), dataType));
			}
		}
	}

	/**
	 * <p>visitPart.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param partProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param simpleListDataList a {@link java.util.List} object.
	 * @param weightUsed a {@link java.lang.Double} object.
	 * @param volUsed a {@link java.lang.Double} object.
	 * @param netQtyInLorKg a {@link java.lang.Double} object.
	 * @param netWeight a {@link java.lang.Double} object.
	 * @param mandatoryCharacts a {@link java.util.Map} object.
	 * @param totalQtiesValue a {@link java.util.Map} object.
	 * @param isGenericRawMaterial a boolean.
	 * @param variant a {@link fr.becpg.repo.variant.model.VariantData} object.
	 * @throws fr.becpg.repo.formulation.FormulateException if any.
	 */
	protected void visitPart(ProductData formulatedProduct, ProductData partProduct, List<T> simpleListDataList, Double weightUsed, Double volUsed,
			Double netQtyInLorKg, Double netWeight, Map<NodeRef, List<NodeRef>> mandatoryCharacts, Map<NodeRef, Double> totalQtiesValue,
			boolean isGenericRawMaterial, VariantData variant) {

		if (!(partProduct instanceof LocalSemiFinishedProductData)) {

			List<T> componentSimpleListDataList = getDataListVisited(partProduct);

			if ((componentSimpleListDataList == null) || componentSimpleListDataList.isEmpty()) {

				logger.debug("simpleListDataList  is null or empty");

				mandatoryCharacts.keySet().forEach(charactNodeRef -> 
					addMissingMandatoryCharact(mandatoryCharacts, charactNodeRef, partProduct.getNodeRef()));
			} else {

				simpleListDataList.forEach(newSimpleListDataItem -> {
					if ((newSimpleListDataItem.getCharactNodeRef() != null) && isCharactFormulated(newSimpleListDataItem)) {

						boolean formulateInVol = (partProduct.getUnit() != null) && partProduct.getUnit().isVolume();
						boolean forceWeight = false;

						if (newSimpleListDataItem instanceof PhysicoChemListDataItem) {
							if (FormulationHelper.isCharactFormulatedFromVol(nodeService, newSimpleListDataItem)) {
								formulateInVol = true;
							} else {
								formulateInVol = false;
								forceWeight = true;
							}
						} else if(newSimpleListDataItem instanceof  NutListDataItem  && ((NutListDataItem)newSimpleListDataItem).getUnit()!=null
								 &&((NutListDataItem)newSimpleListDataItem).getUnit().endsWith(NutListDataItem.UNIT_PER100G) ) {
							formulateInVol = false;
							forceWeight = true;
						}

						// calculate charact from qty or vol ?
						Double qtyUsed = formulateInVol ? volUsed : weightUsed;
						Double netQty = forceWeight ? netWeight : netQtyInLorKg;

						// look for charact in component
						SimpleListDataItem slDataItem = componentSimpleListDataList.stream()
								.filter(s -> newSimpleListDataItem.getCharactNodeRef().equals(s.getCharactNodeRef())).findFirst().orElse(null);

						// is it a mandatory charact ?
						if ((slDataItem == null) || (slDataItem.getValue() == null)) {
							if (!(slDataItem instanceof MinMaxValueDataItem) || ((((MinMaxValueDataItem) slDataItem).getMaxi() == null)
									&& (((MinMaxValueDataItem) slDataItem).getMini() == null))) {
								addMissingMandatoryCharact(mandatoryCharacts, newSimpleListDataItem.getCharactNodeRef(), partProduct.getNodeRef());
							}
						}

						// Calculate values
						if ((slDataItem != null) && (qtyUsed != null)) {

							calculate(formulatedProduct, partProduct, newSimpleListDataItem, slDataItem, qtyUsed, netQty, isGenericRawMaterial,
									variant);

							if ((totalQtiesValue != null) && (slDataItem.getValue() != null)) {
								Double currentQty = totalQtiesValue.get(newSimpleListDataItem.getCharactNodeRef());
								if (currentQty == null) {
									currentQty = 0d;
								}
								totalQtiesValue.put(newSimpleListDataItem.getCharactNodeRef(), currentQty + qtyUsed);
							}
						}
					}
				});

			}
		}
	}

	/**
	 * <p>calculate.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param partProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param newSimpleListDataItem a {@link fr.becpg.repo.repository.model.SimpleListDataItem} object.
	 * @param slDataItem a {@link fr.becpg.repo.repository.model.SimpleListDataItem} object.
	 * @param qtyUsed a {@link java.lang.Double} object.
	 * @param netQty a {@link java.lang.Double} object.
	 * @param isGenericRawMaterial a boolean.
	 * @param variant a {@link fr.becpg.repo.variant.model.VariantData} object.
	 */
	protected void calculate(ProductData formulatedProduct, ProductData partProduct, SimpleListDataItem newSimpleListDataItem,
			SimpleListDataItem slDataItem, Double qtyUsed, Double netQty, boolean isGenericRawMaterial, VariantData variant) {

		Double formulatedValue;
		if (newSimpleListDataItem instanceof FormulatedCharactDataItem) {
			formulatedValue = ((FormulatedCharactDataItem) newSimpleListDataItem).getFormulatedValue();
		} else {
			formulatedValue = newSimpleListDataItem.getValue();
		}

		Double newValue = formulatedValue != null ? formulatedValue : 0d;
		Double value = extractValue(formulatedProduct, partProduct, slDataItem);
		if ((variant != null) && (newSimpleListDataItem instanceof VariantAwareDataItem)) {
			formulatedValue = ((VariantAwareDataItem) newSimpleListDataItem).getValue(variant);
			if (value != null) {
				((VariantAwareDataItem) newSimpleListDataItem).setValue(FormulationHelper.calculateValue(formulatedValue, qtyUsed, value, netQty),
						variant);
			}
		} else if (variant == null) {
			if (value != null) {
				newSimpleListDataItem.setValue(FormulationHelper.calculateValue(formulatedValue, qtyUsed, value, netQty));
			} else {
				value = 0d;
			}
			if (slDataItem instanceof MinMaxValueDataItem) {
				Double newMini = ((MinMaxValueDataItem) newSimpleListDataItem).getMini();
				if(newSimpleListDataItem instanceof NutListDataItem) {
					newMini = ((NutListDataItem) newSimpleListDataItem).getFormulatedMini();
				}
				
				
				Double miniValue = ((MinMaxValueDataItem) slDataItem).getMini();
				Double newMaxi = ((MinMaxValueDataItem) newSimpleListDataItem).getMaxi();
				
				if(newSimpleListDataItem instanceof NutListDataItem) {
					newMaxi = ((NutListDataItem) newSimpleListDataItem).getFormulatedMaxi();
				}
				
				Double maxiValue = ((MinMaxValueDataItem) slDataItem).getMaxi();
				if (isGenericRawMaterial) {
					if ((miniValue != null) && ((newMini == null) || ((newMini != null) && (newMini > miniValue)))) {
						((MinMaxValueDataItem) newSimpleListDataItem).setMini(miniValue);
					}
					if ((maxiValue != null) && ((newMaxi == null) || ((newMaxi != null) && (newMaxi < maxiValue)))) {
						((MinMaxValueDataItem) newSimpleListDataItem).setMaxi(maxiValue);
					}
				} else {
					if ((newMini != null) || (miniValue != null)) {
						((MinMaxValueDataItem) newSimpleListDataItem).setMini(FormulationHelper.calculateValue(newMini != null ? newMini : newValue,
								qtyUsed, miniValue != null ? miniValue : value, netQty));
					}
					if ((newMaxi != null) || (maxiValue != null)) {
						((MinMaxValueDataItem) newSimpleListDataItem).setMaxi(FormulationHelper.calculateValue(newMaxi != null ? newMaxi : newValue,
								qtyUsed, maxiValue != null ? maxiValue : value, netQty));
					}
				}
			}

			if (newSimpleListDataItem instanceof ForecastValueDataItem) {
				((ForecastValueDataItem) newSimpleListDataItem)
						.setPreviousValue(FormulationHelper.calculateValue(((ForecastValueDataItem) newSimpleListDataItem).getPreviousValue(),
								qtyUsed, ((ForecastValueDataItem) slDataItem).getPreviousValue(), netQty));
				((ForecastValueDataItem) newSimpleListDataItem)
						.setFutureValue(FormulationHelper.calculateValue(((ForecastValueDataItem) newSimpleListDataItem).getFutureValue(), qtyUsed,
								((ForecastValueDataItem) slDataItem).getFutureValue(), netQty));
			}

			if (logger.isDebugEnabled()) {
				logger.debug("valueToAdd = qtyUsed * value : " + qtyUsed + " * " + slDataItem.getValue());
				if (newSimpleListDataItem.getNodeRef() != null) {
					logger.debug("charact: " + nodeService.getProperty(newSimpleListDataItem.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME)
							+ " - newValue : " + newSimpleListDataItem.getValue());
					if (newSimpleListDataItem instanceof MinMaxValueDataItem) {
						logger.debug("charact: " + nodeService.getProperty(newSimpleListDataItem.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME)
								+ " - newMini : " + ((MinMaxValueDataItem) newSimpleListDataItem).getMini());
						logger.debug("charact: " + nodeService.getProperty(newSimpleListDataItem.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME)
								+ " - newMaxi : " + ((MinMaxValueDataItem) newSimpleListDataItem).getMaxi());
					}
					if (newSimpleListDataItem instanceof ForecastValueDataItem) {
						logger.debug("charact: " + nodeService.getProperty(newSimpleListDataItem.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME)
								+ " - previousValue : " + ((ForecastValueDataItem) newSimpleListDataItem).getPreviousValue());
						logger.debug("charact: " + nodeService.getProperty(newSimpleListDataItem.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME)
								+ " - futureValue : " + ((ForecastValueDataItem) newSimpleListDataItem).getFutureValue());
					}
				}
			}
		}
	}

	/**
	 * <p>extractValue.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param partProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param slDataItem a {@link fr.becpg.repo.repository.model.SimpleListDataItem} object.
	 * @return a {@link java.lang.Double} object.
	 */
	protected Double extractValue(ProductData formulatedProduct, ProductData partProduct, SimpleListDataItem slDataItem) {

		return slDataItem.getValue();
	}

	/**
	 * <p>addMissingMandatoryCharact.</p>
	 *
	 * @param mandatoryCharacts a {@link java.util.Map} object.
	 * @param charactNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param componentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	protected void addMissingMandatoryCharact(Map<NodeRef, List<NodeRef>> mandatoryCharacts, NodeRef charactNodeRef, NodeRef componentNodeRef) {
		if (mandatoryCharacts.containsKey(charactNodeRef)) {
			List<NodeRef> sources = mandatoryCharacts.computeIfAbsent(charactNodeRef, k -> new ArrayList<>());
			if (!sources.contains(componentNodeRef)) {
				sources.add(componentNodeRef);
			}

		}
	}

	/**
	 * <p>isCharactFormulated.</p>
	 *
	 * @param sl a {@link fr.becpg.repo.repository.model.SimpleListDataItem} object.
	 * @return a boolean.
	 */
	protected boolean isCharactFormulated(SimpleListDataItem sl) {
		return (sl.getIsManual() == null) || !sl.getIsManual();
	}

	/**
	 * <p>findSimpleListDataItem.</p>
	 *
	 * @param simpleList a {@link java.util.List} object.
	 * @param charactNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a T object.
	 */
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

	/**
	 * <p>computeFormulatedList.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param formulatedCharactDataItems a {@link java.util.List} object.
	 * @param propFormula a {@link org.alfresco.service.namespace.QName} object.
	 * @param errorKey a {@link java.lang.String} object.
	 * @param <U> a U object.
	 */
	protected <U extends FormulatedCharactDataItem> void computeFormulatedList(ProductData formulatedProduct, List<U> formulatedCharactDataItems,
			QName propFormula, String errorKey) {

		if (formulatedCharactDataItems != null) {

			ExpressionParser parser = formulaService.getSpelParser();
			StandardEvaluationContext context = formulaService.createEntitySpelContext(formulatedProduct);

			for (FormulatedCharactDataItem formulatedCharactDataItem : formulatedCharactDataItems) {
				String error = null;
				formulatedCharactDataItem.setIsFormulated(false);
				formulatedCharactDataItem.setErrorLog(null);
				if (((formulatedCharactDataItem.getIsManual() == null) || !formulatedCharactDataItem.getIsManual())
						&& (formulatedCharactDataItem.getCharactNodeRef() != null)) {

					String formulaText = (String) nodeService.getProperty(formulatedCharactDataItem.getCharactNodeRef(), propFormula);
					if ((formulaText != null) && !formulaText.trim().isBlank()) {
						try {

							formulatedCharactDataItem.setIsFormulated(true);

							String[] formulas = SpelHelper.formatMTFormulas(formulaText);
							for (String formula : formulas) {

								Matcher varFormulaMatcher = SpelHelper.formulaVarPattern.matcher(formula);
								if (varFormulaMatcher.matches()) {
									Expression exp = parser.parseExpression(varFormulaMatcher.group(2));
									context.setVariable(varFormulaMatcher.group(1), exp.getValue(context));
								} else {
									Expression exp = parser.parseExpression(formula);
									Object ret = exp.getValue(context);
									if ((ret == null) || (ret instanceof Double) || (ret instanceof Integer)) {
										formulatedCharactDataItem.setValue((Double) ret);

										if (formula.contains(".value") && (formulatedCharactDataItem instanceof MinMaxValueDataItem)) {
											try {
												exp = parser.parseExpression(formula.replace(".value", ".mini"));
												((MinMaxValueDataItem) formulatedCharactDataItem).setMini((Double) exp.getValue(context));
												exp = parser.parseExpression(formula.replace(".value", ".maxi"));
												((MinMaxValueDataItem) formulatedCharactDataItem).setMaxi((Double) exp.getValue(context));
											} catch (Exception e) {
												((MinMaxValueDataItem) formulatedCharactDataItem).setMaxi(null);
												((MinMaxValueDataItem) formulatedCharactDataItem).setMini(null);
												if (logger.isDebugEnabled()) {
													logger.debug("Error in formula :" + formula, e);
												}
											}
										}

										if (formula.contains(".value") && (formulatedCharactDataItem instanceof ForecastValueDataItem)) {
											try {
												exp = parser.parseExpression(formula.replace(".value", ".futureValue"));
												((ForecastValueDataItem) formulatedCharactDataItem).setFutureValue((Double) exp.getValue(context));
												exp = parser.parseExpression(formula.replace(".value", ".previousValue"));
												((ForecastValueDataItem) formulatedCharactDataItem)
														.setPreviousValue(((Double) exp.getValue(context)));
											} catch (Exception e) {
												((ForecastValueDataItem) formulatedCharactDataItem).setFutureValue(null);
												((ForecastValueDataItem) formulatedCharactDataItem).setPreviousValue(null);
												if (logger.isDebugEnabled()) {
													logger.debug("Error in formula :" + formula, e);
												}
											}
										}

										if (formula.contains(".value") && (formulatedCharactDataItem instanceof VariantAwareDataItem)) {
											try {
												for (int i = 1; i <= VariantAwareDataItem.VARIANT_COLUMN_SIZE; i++) {
													exp = parser.parseExpression(formula.replace(".value(",
															".variantValue(\"" + VariantAwareDataItem.VARIANT_COLUMN_NAME + i + "\","));
													if (((VariantAwareDataItem) formulatedCharactDataItem)
															.getValue(VariantAwareDataItem.VARIANT_COLUMN_NAME + i) != null) {
														((VariantAwareDataItem) formulatedCharactDataItem).setValue((Double) exp.getValue(context),
																VariantAwareDataItem.VARIANT_COLUMN_NAME + i);
													}
												}
											} catch (Exception e) {
												if (logger.isDebugEnabled()) {
													logger.debug("Error in formula :" + formula, e);
												}
											}
										}

									} else {
										error = I18NUtil.getMessage("message.formulate.formula.incorrect.type.double", Locale.getDefault());
									}

								}
							}

						} catch (Exception e) {
							error = e.getLocalizedMessage();
							if (logger.isDebugEnabled()) {
								logger.debug("Error in formula :" + SpelHelper.formatFormula(formulaText), e);
							}
						}
					}
				}

				if (error != null) {
					formulatedCharactDataItem.setValue(null);
					formulatedCharactDataItem.setErrorLog(error);

					ReqCtrlListDataItem rclDataItem = new ReqCtrlListDataItem(null, RequirementType.Tolerated,
							MLTextHelper.getI18NMessage(errorKey, mlNodeService.getProperty(formulatedCharactDataItem.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME), error),
							formulatedCharactDataItem.getCharactNodeRef(), new ArrayList<>(), getRequirementDataType());
					formulatedProduct.getReqCtrlList().add(rclDataItem);
				}

			}
		}

	}

	/**
	 * Copy missing item from template
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param simpleListDataList a {@link java.util.List} object.
	 */
	@SuppressWarnings("unchecked")
	protected void synchronizeTemplate(ProductData formulatedProduct, List<T> simpleListDataList) {
		if ((formulatedProduct.getEntityTpl() != null) && !formulatedProduct.getEntityTpl().equals(formulatedProduct)) {

			List<T> templateSimpleListDataList = getDataListVisited(formulatedProduct.getEntityTpl());

			for (T tsl : templateSimpleListDataList) {
				if (tsl.getCharactNodeRef() != null) {
					boolean isFound = false;
					for (T sl : simpleListDataList) {
						if (tsl.getCharactNodeRef().equals(sl.getCharactNodeRef())) {
							isFound = true;

							if ((sl instanceof CompositeDataItem) && (tsl instanceof CompositeDataItem)) {
								if (((CompositeDataItem<T>) tsl).getParent() != null) {
									((CompositeDataItem<T>) sl).setParent(findParentByCharactName(simpleListDataList,
											((CompositeDataItem<T>) tsl).getParent().getCharactNodeRef()));
								} else {
									((CompositeDataItem<T>) sl).setParent(null);
								}
							}

							break;
						}
					}
					if (!isFound) {
						T toAdd = (T) tsl.clone();
						toAdd.setName(null);
						toAdd.setNodeRef(null);
						toAdd.setParentNodeRef(null);

						if (toAdd instanceof CompositeDataItem) {
							if (((CompositeDataItem<T>) toAdd).getParent() != null) {
								((CompositeDataItem<T>) toAdd).setParent(
										findParentByCharactName(simpleListDataList, ((CompositeDataItem<T>) toAdd).getParent().getCharactNodeRef()));
							}
						}

						simpleListDataList.add(toAdd);
					}

				}
			}

			// check sorting
			int lastSort = 0;
			for (T sl : simpleListDataList) {
				if (sl.getCharactNodeRef() != null) {
					boolean isFound = false;

					for (T tsl : templateSimpleListDataList) {
						if (sl.getCharactNodeRef().equals(tsl.getCharactNodeRef())) {
							isFound = true;
							lastSort = tsl.getSort() * 100;
							sl.setSort(lastSort);
						}
					}

					if (!isFound) {
						sl.setSort(++lastSort);
					}
				}
			}

		}
	}

	/**
	 * <p>findParentByCharactName.</p>
	 *
	 * @param simpleListDataList a {@link java.util.List} object.
	 * @param charactNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a T object.
	 */
	protected T findParentByCharactName(List<T> simpleListDataList, NodeRef charactNodeRef) {
		for (T listItem : simpleListDataList) {
			if ((listItem.getCharactNodeRef() != null) && listItem.getCharactNodeRef().equals(charactNodeRef)) {
				return listItem;
			}
		}
		return null;
	}

}
