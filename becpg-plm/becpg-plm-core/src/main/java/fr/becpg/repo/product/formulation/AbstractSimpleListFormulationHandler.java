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
import java.math.BigDecimal;
import java.math.MathContext;
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
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeDataItem;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.formulation.spel.SpelFormulaService;
import fr.becpg.repo.formulation.spel.SpelHelper;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.model.CompositionDataItem;
import fr.becpg.repo.repository.model.ForecastValueDataItem;
import fr.becpg.repo.repository.model.FormulatedCharactDataItem;
import fr.becpg.repo.repository.model.MinMaxValueDataItem;
import fr.becpg.repo.repository.model.SimpleListDataItem;
import fr.becpg.repo.repository.model.SourceableDataItem;
import fr.becpg.repo.repository.model.VariantAwareDataItem;
import fr.becpg.repo.system.SystemConfigurationService;
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
	
	protected AssociationService associationService;
	

	
	protected SystemConfigurationService systemConfigurationService;
	
	/**
	 * <p>Setter for the field <code>systemConfigurationService</code>.</p>
	 *
	 * @param systemConfigurationService a {@link fr.becpg.repo.system.SystemConfigurationService} object
	 */
	public void setSystemConfigurationService(SystemConfigurationService systemConfigurationService) {
		this.systemConfigurationService = systemConfigurationService;
	}

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
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
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
	 * <p>cleanSimpleList.</p>
	 *
	 * @param simpleListDataList a {@link java.util.List} object.
	 * @param isFormulatedProduct a boolean.
	 * @param toRemove a {@link java.util.List} object
	 */
	protected void cleanSimpleList(List<T> simpleListDataList, boolean isFormulatedProduct, List<T> toRemove) {

		if ((simpleListDataList != null) && isFormulatedProduct) {
			simpleListDataList.forEach(sl -> {
				// reset value if formulated
				if (isCharactFormulated(sl)) {
					sl.setValue(null);

					if (sl instanceof MinMaxValueDataItem minMaxValueDataItem) {
						minMaxValueDataItem.setMini(null);
						minMaxValueDataItem.setMaxi(null);
					}
					if (sl instanceof ForecastValueDataItem forecastValueDataItem) {
						for (String forecastColumn : forecastValueDataItem.getForecastColumns()) {
							forecastValueDataItem.setForecastValue(forecastColumn, null);
						}
					}
					if (sl instanceof VariantAwareDataItem variantAwareDataItem) {
						for (int i = 1; i <= VariantAwareDataItem.VARIANT_COLUMN_SIZE; i++) {
							variantAwareDataItem.setValue(null, VariantAwareDataItem.VARIANT_COLUMN_NAME + i);
						}
					}

					if (sl instanceof SourceableDataItem sourceableDataItem) {
						sourceableDataItem.getSources().clear();
					}
					
					// add detailable aspect
					if (!sl.getAspects().contains(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM)) {
						sl.getAspects().add(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM);
					}
					
					toRemove.add(sl);
				}
			});
		}
	}

	/**
	 * <p>formulateSimpleList.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param simpleListDataList a {@link java.util.List} object.
	 * @param isFormulatedProduct a boolean.
	 * @throws fr.becpg.repo.formulation.FormulateException if any.
	 * @param simpleListQtyProvider a {@link fr.becpg.repo.product.formulation.SimpleListQtyProvider} object
	 */
	protected void formulateSimpleList(ProductData formulatedProduct, List<T> simpleListDataList, SimpleListQtyProvider simpleListQtyProvider,
			boolean isFormulatedProduct) {
		
		List<T> toRemove = new ArrayList<>();

		cleanSimpleList( simpleListDataList, isFormulatedProduct, toRemove);
		synchronizeTemplate(formulatedProduct, simpleListDataList, toRemove);

		if (isFormulatedProduct) {
			visitComposition(formulatedProduct, simpleListDataList, simpleListQtyProvider, null, toRemove);
			visitPackaging(formulatedProduct, simpleListDataList, simpleListQtyProvider, null, toRemove);
			visitProcess(formulatedProduct, simpleListDataList, simpleListQtyProvider, null, toRemove);

			for (VariantData variant : formulatedProduct.getVariants()) {
				visitComposition(formulatedProduct, simpleListDataList, simpleListQtyProvider, variant, toRemove);
				visitPackaging(formulatedProduct, simpleListDataList, simpleListQtyProvider, variant, toRemove);
				visitProcess(formulatedProduct, simpleListDataList, simpleListQtyProvider, variant, toRemove);
			}
			
		   simpleListDataList.removeAll(toRemove);
			
		}

	}

	/**
	 * <p>visitComposition.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param simpleListDataList a {@link java.util.List} object
	 * @param qtyProvider a {@link fr.becpg.repo.product.formulation.SimpleListQtyProvider} object
	 * @param variant a {@link fr.becpg.repo.variant.model.VariantData} object
	 * @param toRemove a {@link java.util.List} object
	 */
	protected void visitComposition(ProductData formulatedProduct, List<T> simpleListDataList, SimpleListQtyProvider qtyProvider,
			VariantData variant,List<T> toRemove ) {
		NodeRef variantNodeRef = variant != null ? variant.getNodeRef() : null;

		if (formulatedProduct.hasCompoListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE),
				(variant != null ? new VariantFilters<>(variantNodeRef) : new VariantFilters<>())))) {

			/*
			 * Composition
			 */
			Map<NodeRef, List<NodeRef>> mandatoryCharacts1 = getMandatoryCharacts(formulatedProduct, PLMModel.TYPE_RAWMATERIAL);

			Composite<CompoListDataItem> composite = CompositeHelper
					.getHierarchicalCompoList(formulatedProduct.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE),
							(variant != null ? new VariantFilters<>(variantNodeRef) : new VariantFilters<>()))));
			visitCompoListChildren(formulatedProduct, composite, simpleListDataList, formulatedProduct.getProductLossPerc(), qtyProvider,
					mandatoryCharacts1, variant, true, toRemove );

			addReqCtrlList(formulatedProduct.getReqCtrlList(), mandatoryCharacts1, getRequirementDataType());

		}

	}

	private void visitCompoListChildren(ProductData formulatedProduct, Composite<CompoListDataItem> composite, List<T> simpleListDataList,
			Double parentLossRatio, SimpleListQtyProvider qtyProvider, Map<NodeRef, List<NodeRef>> mandatoryCharacts, VariantData variant,
			boolean isFormulatedProduct,List<T> toRemove ) {

		Map<NodeRef, Double> totalQtiesInKg = new HashMap<>();
		for (Composite<CompoListDataItem> component : composite.getChildren()) {
			CompoListDataItem compoListDataItem = component.getData();
			if (compoListDataItem != null) {
				ProductData componentProduct = (ProductData) alfrescoRepository.findOne(compoListDataItem.getProduct());
				if (!Boolean.TRUE.equals(qtyProvider.omitElement(compoListDataItem))) {
					if (!component.isLeaf()) {

						// take in account the loss perc
						Double lossPerc = FormulationHelper.getComponentLossPerc(componentProduct, compoListDataItem);
						Double newLossPerc = FormulationHelper.calculateLossPerc(parentLossRatio, lossPerc);
						if (logger.isDebugEnabled()) {
							logger.debug("parentLossRatio: " + parentLossRatio + " - lossPerc: " + lossPerc + " - newLossPerc: " + newLossPerc);
						}

						// calculate children
						Composite<CompoListDataItem> c = component;
						visitCompoListChildren(formulatedProduct, c, simpleListDataList, newLossPerc, qtyProvider, mandatoryCharacts, variant, false, toRemove);

					} else {

						FormulatedQties qties = new FormulatedQties(qtyProvider.getQty(compoListDataItem, parentLossRatio, componentProduct),
								qtyProvider.getVolume(compoListDataItem, parentLossRatio, componentProduct), qtyProvider.getNetQty(variant),
								qtyProvider.getNetWeight(variant));

						if (qties.isNotNull()) {
							visitPart(formulatedProduct,compoListDataItem , componentProduct, simpleListDataList, qties, mandatoryCharacts, totalQtiesInKg,
									FormulationHelper.getQtyInKg(compoListDataItem), variant, toRemove);
						}

					}
				}
			}
		}
		// Case Generic MP
		if (isFormulatedProduct && formulatedProduct.isGeneric()) {

			Double netQtyForGeneric = qtyProvider.getNetQty(variant);
			if (ProductUnit.P.equals(formulatedProduct.getUnit())) {
				netQtyForGeneric = FormulationHelper.getNetQtyInLorKg(formulatedProduct,variant, FormulationHelper.DEFAULT_NET_WEIGHT);
			}

			formulateGenericRawMaterial(simpleListDataList, totalQtiesInKg, netQtyForGeneric);
		}
	}

	/**
	 * <p>visitPackaging.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param simpleListDataList a {@link java.util.List} object
	 * @param qtyProvider a {@link fr.becpg.repo.product.formulation.SimpleListQtyProvider} object
	 * @param variant a {@link fr.becpg.repo.variant.model.VariantData} object
	 * @param toRemove a {@link java.util.List} object
	 */
	protected void visitPackaging(ProductData formulatedProduct, List<T> simpleListDataList, SimpleListQtyProvider qtyProvider, VariantData variant, List<T> toRemove ) {

		if (formulatedProduct.hasPackagingListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE),
				(variant != null ? new VariantFilters<>(variant.getNodeRef()) : new VariantFilters<>())))) {

			/*
			 * PackagingList
			 */
			Map<NodeRef, List<NodeRef>> mandatoryCharacts2 = getMandatoryCharacts(formulatedProduct, PLMModel.TYPE_PACKAGINGMATERIAL);

			for (PackagingListDataItem packagingListDataItem : formulatedProduct
					.getPackagingList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE),
							(variant != null ? new VariantFilters<>(variant.getNodeRef()) : new VariantFilters<>())))) {

				if ((packagingListDataItem.getProduct() != null)
						&& ((packagingListDataItem.getIsRecycle() == null) || !packagingListDataItem.getIsRecycle())) {

					ProductData partProduct = (ProductData) alfrescoRepository.findOne(packagingListDataItem.getProduct());

					Double qty = qtyProvider.getQty(packagingListDataItem, partProduct);

					FormulatedQties qties = new FormulatedQties(qty, qty, qtyProvider.getNetQty(variant), qtyProvider.getNetWeight(variant));
					
					if (qties.isNotNull()) {
						visitPart(formulatedProduct, packagingListDataItem, partProduct, simpleListDataList, qties, mandatoryCharacts2, null, null, variant, toRemove);
					}
				}
			}

			addReqCtrlList(formulatedProduct.getReqCtrlList(), mandatoryCharacts2, getRequirementDataType());
		}

	}

	/**
	 * <p>visitProcess.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param simpleListDataList a {@link java.util.List} object
	 * @param qtyProvider a {@link fr.becpg.repo.product.formulation.SimpleListQtyProvider} object
	 * @param variant a {@link fr.becpg.repo.variant.model.VariantData} object
	 * @param toRemove a {@link java.util.List} object
	 */
	protected void visitProcess(ProductData formulatedProduct, List<T> simpleListDataList, SimpleListQtyProvider qtyProvider, VariantData variant, List<T> toRemove ) {

		if (formulatedProduct.hasProcessListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE),
				(variant != null ? new VariantFilters<>(variant.getNodeRef()) : new VariantFilters<>())))) {
			/*
			 * ProcessList
			 */

			Double netQtyForCost = qtyProvider.getNetQty(variant);

			Map<NodeRef, List<NodeRef>> mandatoryCharacts3 = getMandatoryCharacts(formulatedProduct, PLMModel.TYPE_RESOURCEPRODUCT);
			for (ProcessListDataItem processListDataItem : formulatedProduct
					.getProcessList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE),
							(variant != null ? new VariantFilters<>(variant.getNodeRef()) : new VariantFilters<>())))) {

				Double qty = qtyProvider.getQty(processListDataItem,variant);

				if ((processListDataItem.getResource() != null) && (qty != null)) {
					if (ProductUnit.P.equals(processListDataItem.getUnit()) && ProductUnit.P.equals(formulatedProduct.getUnit())) {
						netQtyForCost = FormulationHelper.QTY_FOR_PIECE;
					}

					ProductData partProduct = (ProductData) alfrescoRepository.findOne(processListDataItem.getResource());

					FormulatedQties qties = new FormulatedQties(qty, null, netQtyForCost, null);
					if (qties.isNotNull()) {
						visitPart(formulatedProduct, processListDataItem , partProduct, simpleListDataList, qties, mandatoryCharacts3, null, null, variant, toRemove);
					}
				}
			}

			addReqCtrlList(formulatedProduct.getReqCtrlList(), mandatoryCharacts3, getRequirementDataType());
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
				if ((netQty != null) && (totalQty != null) && (totalQty != 0d)) {

					Double value = null;
					
					if (newSimpleListDataItem instanceof FormulatedCharactDataItem formulatedCharactDataItem) {
						value = formulatedCharactDataItem.getFormulatedValue();
					} else {
						value = newSimpleListDataItem.getValue();
					}
					
					if ((value != null)) {
						newSimpleListDataItem.setValue(BigDecimal.valueOf(value)
								.multiply(BigDecimal.valueOf(netQty).divide(BigDecimal.valueOf(totalQty), MathContext.DECIMAL64)).doubleValue());
					}

					if (newSimpleListDataItem instanceof ForecastValueDataItem forecastValueDataItem) {
						for (String forecastColumn : forecastValueDataItem.getForecastColumns()) {
							if (forecastValueDataItem.getForecastValue(forecastColumn) != null) {
								forecastValueDataItem.setForecastValue(forecastColumn,
										BigDecimal.valueOf(forecastValueDataItem.getForecastValue(forecastColumn))
												.multiply(BigDecimal.valueOf(netQty).divide(BigDecimal.valueOf(totalQty), MathContext.DECIMAL64))
												.doubleValue());
							}
						}
					}
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

				reqCtrlList.add( ReqCtrlListDataItem.build().ofType( RequirementType.Tolerated)
					.withMessage(MLTextHelper.getI18NMessage(MESSAGE_UNDEFINED_CHARACT,
							mlNodeService.getProperty(mandatoryCharact.getKey(), BeCPGModel.PROP_CHARACT_NAME)))
					.withCharact(mandatoryCharact.getKey())
					.withSources( mandatoryCharact.getValue())
					.ofDataType(dataType));
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
	private void visitPart(ProductData formulatedProduct, CompositionDataItem compositionDataItem, ProductData partProduct, List<T> simpleListDataList, FormulatedQties qties,
			Map<NodeRef, List<NodeRef>> mandatoryCharacts, Map<NodeRef, Double> totalQtiesValue, Double totalQtyUsed, VariantData variant, List<T> toRemove ) {

		if (!(partProduct instanceof LocalSemiFinishedProductData)) {

			List<T> componentSimpleListDataList = getDataListVisited(partProduct);

			if ((componentSimpleListDataList == null) || componentSimpleListDataList.isEmpty()) {

				logger.debug("simpleListDataList  is null or empty");

				mandatoryCharacts.keySet()
						.forEach(charactNodeRef -> addMissingMandatoryCharact(mandatoryCharacts, charactNodeRef, partProduct.getNodeRef()));
			} else {
				
				
				componentSimpleListDataList.forEach(componentSimpleListDataItem -> {
					if(shouldPropagate(compositionDataItem,componentSimpleListDataItem.getCharactNodeRef(), propagateModeEnable(formulatedProduct))) {
						// look for charact in formulatedProduct
						SimpleListDataItem slDataItem = simpleListDataList.stream()
								.filter(s -> componentSimpleListDataItem.getCharactNodeRef().equals(s.getCharactNodeRef())).findFirst().orElse(null);
						if(slDataItem == null) {
							if( extractValue(formulatedProduct, partProduct, componentSimpleListDataItem)!=null) {
								simpleListDataList.add( newSimpleListDataItem(componentSimpleListDataItem.getCharactNodeRef()));
							}
						} else {
							toRemove.remove(slDataItem);
						}
					}
					
				});
				

				simpleListDataList.forEach(newSimpleListDataItem -> {
					if ((newSimpleListDataItem.getCharactNodeRef() != null) && isCharactFormulated(newSimpleListDataItem)) {
						
						if(!shouldPropagate(compositionDataItem,newSimpleListDataItem.getCharactNodeRef(), propagateModeEnable(formulatedProduct))) {
						     toRemove.remove(newSimpleListDataItem);
						}

						// look for charact in component
						SimpleListDataItem slDataItem = componentSimpleListDataList.stream()
								.filter(s -> newSimpleListDataItem.getCharactNodeRef().equals(s.getCharactNodeRef())).findFirst().orElse(null);

						// is it a mandatory charact ?
						if ((slDataItem == null) || (slDataItem.getValue() == null)) {
							if (!(slDataItem instanceof MinMaxValueDataItem minMaxValueDataItem) || (((minMaxValueDataItem).getMaxi() == null)
									&& ((minMaxValueDataItem).getMini() == null))) {
								addMissingMandatoryCharact(mandatoryCharacts, newSimpleListDataItem.getCharactNodeRef(), partProduct.getNodeRef());
							}
						}

						if (slDataItem != null) {

							boolean formulateInVol = partProduct.isLiquid();
							boolean forceWeight = false;

							if (newSimpleListDataItem instanceof PhysicoChemListDataItem) {
								if (FormulationHelper.isCharactFormulatedFromVol(nodeService, newSimpleListDataItem)) {
									formulateInVol = true;
								} else {
									formulateInVol = false;
									forceWeight = true;
								}
							} else if (newSimpleListDataItem instanceof NutListDataItem) {
								if ((partProduct.getUnit() != null) && partProduct.getUnit().isVolume() && (partProduct.getServingSizeUnit() != null)
										&& partProduct.getServingSizeUnit().isWeight()) {
									if ((formulatedProduct.getServingSizeUnit() != null) && formulatedProduct.getServingSizeUnit().isWeight()) {
										forceWeight = true;
									}
								}
							}

							// calculate charact from qty or vol ?
							Double qtyUsed = qties.getQtyUsed(formulateInVol);
							Double netQty = qties.getNetQty(forceWeight);

							// Calculate values
							if ((qtyUsed != null)) {

								calculate(formulatedProduct, partProduct, newSimpleListDataItem, slDataItem, qtyUsed, netQty, variant);

								if ((totalQtiesValue != null) && (slDataItem.getValue() != null) && totalQtyUsed != null) {
									Double currentQty = totalQtiesValue.get(newSimpleListDataItem.getCharactNodeRef());
									if (currentQty == null) {
										currentQty = 0d;
									}

									totalQtiesValue.put(newSimpleListDataItem.getCharactNodeRef(), currentQty + totalQtyUsed);
								}
							}
						}
					}
				});

			}
		}
	}

	/**
	 * <p>propagateModeEnable.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @return a boolean
	 */
	protected abstract boolean propagateModeEnable(ProductData formulatedProduct);

	/**
	 * <p>newSimpleListDataItem.</p>
	 *
	 * @param charactNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a T object
	 */
	protected abstract T newSimpleListDataItem(NodeRef charactNodeRef);

	/**
	 * <p>calculate.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param partProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param qtyUsed a {@link java.lang.Double} object.
	 * @param netQty a {@link java.lang.Double} object.
	 * @param variant a {@link fr.becpg.repo.variant.model.VariantData} object.
	 * @param calculatedListItem a {@link fr.becpg.repo.repository.model.SimpleListDataItem} object
	 * @param visitedListItem a {@link fr.becpg.repo.repository.model.SimpleListDataItem} object
	 */
	protected void calculate(ProductData formulatedProduct, ProductData partProduct, SimpleListDataItem calculatedListItem,
			SimpleListDataItem visitedListItem, Double qtyUsed, Double netQty, VariantData variant) {

		Double formulatedValue;
		if (calculatedListItem instanceof FormulatedCharactDataItem) {
			formulatedValue = ((FormulatedCharactDataItem) calculatedListItem).getFormulatedValue();
		} else {
			formulatedValue = calculatedListItem.getValue();
		}

		Double newValue = formulatedValue != null ? formulatedValue : 0d;
		Double value = extractValue(formulatedProduct, partProduct, visitedListItem);
		if ((variant != null) && (calculatedListItem instanceof VariantAwareDataItem)) {
			formulatedValue = ((VariantAwareDataItem) calculatedListItem).getValue(variant);
			if (value != null) {
				((VariantAwareDataItem) calculatedListItem).setValue(FormulationHelper.calculateValue(formulatedValue, qtyUsed, value, netQty),
						variant);
			}
		} else if (variant == null) {
			if (value != null) {
				calculatedListItem.setValue(FormulationHelper.calculateValue(formulatedValue, qtyUsed, value, netQty));
			} else {
				value = 0d;
			}
			if (visitedListItem instanceof MinMaxValueDataItem) {
				Double newMini = ((MinMaxValueDataItem) calculatedListItem).getMini();
				if (calculatedListItem instanceof NutListDataItem) {
					newMini = ((NutListDataItem) calculatedListItem).getFormulatedMini();
				}

				Double miniValue = ((MinMaxValueDataItem) visitedListItem).getMini();
				Double newMaxi = ((MinMaxValueDataItem) calculatedListItem).getMaxi();

				if (calculatedListItem instanceof NutListDataItem) {
					newMaxi = ((NutListDataItem) calculatedListItem).getFormulatedMaxi();
				}

				Double maxiValue = ((MinMaxValueDataItem) visitedListItem).getMaxi();
				if (formulatedProduct.isGeneric()) {
					if ((miniValue != null) && ((newMini == null) || ((newMini != null) && (newMini > miniValue)))) {
						((MinMaxValueDataItem) calculatedListItem).setMini(miniValue);
					}
					if ((maxiValue != null) && ((newMaxi == null) || ((newMaxi != null) && (newMaxi < maxiValue)))) {
						((MinMaxValueDataItem) calculatedListItem).setMaxi(maxiValue);
					}
				} else {
					if ((newMini != null) || (miniValue != null)) {
						((MinMaxValueDataItem) calculatedListItem).setMini(FormulationHelper.calculateValue(newMini != null ? newMini : newValue,
								qtyUsed, miniValue != null ? miniValue : value, netQty));
					}
					if ((newMaxi != null) || (maxiValue != null)) {
						((MinMaxValueDataItem) calculatedListItem).setMaxi(FormulationHelper.calculateValue(newMaxi != null ? newMaxi : newValue,
								qtyUsed, maxiValue != null ? maxiValue : value, netQty));
					}
				}
			}

			if (calculatedListItem instanceof ForecastValueDataItem forecastListItem) {
				for (String forecastColumn : forecastListItem.getForecastColumns()) {
					forecastListItem.setForecastValue(forecastColumn,
							FormulationHelper.calculateValue(forecastListItem.getForecastValue(forecastColumn), qtyUsed,
									((ForecastValueDataItem) visitedListItem).getForecastValue(forecastColumn), netQty));
				}
			}

			if (logger.isDebugEnabled()) {
				logger.debug(partProduct.getName() + " - valueToAdd = qtyUsed * value : " + qtyUsed + " * " + visitedListItem.getValue());
				if (calculatedListItem.getNodeRef() != null) {
					logger.debug("charact: " + nodeService.getProperty(calculatedListItem.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME)
							+ " - newValue : " + calculatedListItem.getValue());
					if (calculatedListItem instanceof MinMaxValueDataItem) {
						logger.debug("charact: " + nodeService.getProperty(calculatedListItem.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME)
								+ " - newMini : " + ((MinMaxValueDataItem) calculatedListItem).getMini());
						logger.debug("charact: " + nodeService.getProperty(calculatedListItem.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME)
								+ " - newMaxi : " + ((MinMaxValueDataItem) calculatedListItem).getMaxi());
					}
					if (calculatedListItem instanceof ForecastValueDataItem forecastListItem) {
						for (String forecastColumn : forecastListItem.getForecastColumns()) {
							logger.debug("charact: " + nodeService.getProperty(calculatedListItem.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME)
							+ " - " + forecastColumn + " : " + forecastListItem.getForecastValue(forecastColumn));
						}
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

										if (formula.contains(".value") && (formulatedCharactDataItem instanceof ForecastValueDataItem forecastListItem)) {
											for (String forecastColumn : forecastListItem.getForecastColumns()) {
												try {
													String forecastAccessor = forecastListItem.getForecastAccessor(forecastColumn);
													exp = parser.parseExpression(formula.replace(".value", "." + forecastAccessor));
													forecastListItem.setForecastValue(forecastColumn, (Double) exp.getValue(context));
												} catch (Exception e) {
													forecastListItem.setForecastValue(forecastColumn, null);
													if (logger.isDebugEnabled()) {
														logger.debug("Error in formula :" + formula, e);
													}
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

					formulatedProduct.getReqCtrlList().add(ReqCtrlListDataItem.build()
							.ofType( RequirementType.Tolerated)
							.withMessage(MLTextHelper.getI18NMessage(errorKey,
									mlNodeService.getProperty(formulatedCharactDataItem.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME), error))
							.withCharact(formulatedCharactDataItem.getCharactNodeRef()).ofDataType(getRequirementDataType()));
				}

			}
		}

	}

	/**
	 * Copy missing item from template
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param simpleListDataList a {@link java.util.List} object.
	 * @param toRemove a {@link java.util.List} object
	 */
	@SuppressWarnings("unchecked")
	protected void synchronizeTemplate(ProductData formulatedProduct, List<T> simpleListDataList, List<T> toRemove) {
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
							toRemove.remove(sl);
							break;
						}
					}
					if (!isFound) {
						T toAdd = (T) tsl.copy();
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
	
	
	/**
	 * <p>shouldPropagate.</p>
	 *
	 * @param compositionDataItem a {@link fr.becpg.repo.repository.model.CompositionDataItem} object
	 * @param charactNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param defaultValue a boolean
	 * @return a boolean
	 */
	protected boolean shouldPropagate(CompositionDataItem compositionDataItem, NodeRef charactNodeRef, boolean defaultValue) {
		
		if (compositionDataItem.getAspects().contains(PLMModel.ASPECT_PROPAGATE_UP) && (compositionDataItem.getNodeRef() != null)
				&& (charactNodeRef != null)) {
			List<NodeRef> propagatedCharacts = associationService.getTargetAssocs(compositionDataItem.getNodeRef(), PLMModel.ASSOC_PROPAGATED_CHARACTS);		
			return propagatedCharacts.isEmpty() || propagatedCharacts.contains(charactNodeRef);
		} else if(charactNodeRef!=null && Boolean.TRUE.equals(nodeService.getProperty(charactNodeRef, PLMModel.PROP_IS_CHARACT_PROPAGATE_UP))) {
			return true;
		}	
		return defaultValue;
	}

}
