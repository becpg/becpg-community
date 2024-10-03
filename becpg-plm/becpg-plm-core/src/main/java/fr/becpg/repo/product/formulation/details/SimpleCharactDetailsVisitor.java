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
package fr.becpg.repo.product.formulation.details;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.CharactDetailsVisitor;
import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.CharactDetailsValue;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.formulation.FormulatedQties;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.model.ForecastValueDataItem;
import fr.becpg.repo.repository.model.MinMaxValueDataItem;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;
import fr.becpg.repo.repository.model.UnitAwareDataItem;
import fr.becpg.repo.variant.filters.VariantFilters;

/**
 * <p>SimpleCharactDetailsVisitor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class SimpleCharactDetailsVisitor implements CharactDetailsVisitor {

	private static final Log logger = LogFactory.getLog(SimpleCharactDetailsVisitor.class);

	protected AlfrescoRepository<? extends RepositoryEntity> alfrescoRepository;

	protected NodeService nodeService;

	protected EntityDictionaryService entityDictionaryService;

	protected QName dataListType;

	/**
	 * <p>Setter for the field <code>entityDictionaryService</code>.</p>
	 *
	 * @param entityDictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object.
	 */
	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<? extends RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/** {@inheritDoc} */
	@Override
	public void setDataListType(QName dataListType) {
		this.dataListType = dataListType;
	}

	/** {@inheritDoc} */
	@Override
	public CharactDetails visit(ProductData productData, List<NodeRef> dataListItems, Integer maxLevel) throws FormulateException {

		CharactDetails ret = createCharactDetails(dataListItems);

		if (maxLevel == null) {
			maxLevel = 0;
		}

		Double netQty = FormulationHelper.getNetQtyInLorKg(productData, FormulationHelper.DEFAULT_NET_WEIGHT);
		Double netWeight = FormulationHelper.getNetWeight(productData, FormulationHelper.DEFAULT_NET_WEIGHT);
		Double netVol = FormulationHelper.getNetVolume(productData, FormulationHelper.DEFAULT_NET_WEIGHT);

		CharactDetailsVisitorContext context = new CharactDetailsVisitorContext(productData, maxLevel, ret);
		
		visitRecur(context, productData, 0, netWeight, netVol, netQty);

		return ret;
	}
	
	/**
	 * <p>areDetailsApplicable.</p>
	 *
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object
	 * @return a boolean
	 */
	protected boolean areDetailsApplicable(ProductData productData) {
		return productData.isRawMaterial() || !productData.isGeneric();
	}

	/**
	 * <p>visitRecur.</p>
	 *
	 * @param context a {@link fr.becpg.repo.product.formulation.details.CharactDetailsVisitorContext} object
	 * @param subProductData a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param currLevel a {@link java.lang.Integer} object
	 * @param parentNetWeight a {@link java.lang.Double} object
	 * @param parentVoume a {@link java.lang.Double} object
	 * @param parentQuantity a {@link java.lang.Double} object
	 * @return a {@link fr.becpg.repo.product.data.CharactDetails} object
	 * @throws fr.becpg.repo.formulation.FormulateException if any.
	 */
	public CharactDetails visitRecur(CharactDetailsVisitorContext context, ProductData subProductData, Integer currLevel, Double parentNetWeight,
			Double parentVoume, Double parentQuantity) throws FormulateException {
		
		if (areDetailsApplicable(subProductData)
				&& subProductData.hasCompoListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
			
			for (CompoListDataItem compoListDataItem : subProductData
					.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
				
				if (compoListDataItem != null && !omitItem(compoListDataItem)) {
					
					Double compoListWeight = computeCompoListWeight(subProductData, parentNetWeight, compoListDataItem);
					Double compoListVol = computeCompoListVol(subProductData, parentVoume, compoListDataItem);
					ProductData compoListProduct = (ProductData) alfrescoRepository.findOne(compoListDataItem.getProduct());
					
					FormulatedQties qties = new FormulatedQties(compoListWeight, compoListVol, parentQuantity, parentNetWeight);
					visitPart(context, subProductData, compoListProduct, compoListDataItem.getNodeRef(), qties, currLevel);
					
					if (shouldVisitNextLevel(currLevel, context.getMaxLevel(), compoListDataItem)) {
						visitRecur(context, compoListProduct, currLevel + 1, compoListWeight, compoListVol, parentQuantity);
					}
				}
			}
		}
		
		return context.getCharactDetails();
	}

	/**
	 * <p>shouldVisitNextLevel.</p>
	 *
	 * @param currLevel a {@link java.lang.Integer} object
	 * @param maxLevel a {@link java.lang.Integer} object
	 * @param compoListDataItem a {@link fr.becpg.repo.product.data.productList.CompoListDataItem} object
	 * @return a boolean
	 */
	protected boolean shouldVisitNextLevel(Integer currLevel, Integer maxLevel, CompoListDataItem compoListDataItem) {
		return ((maxLevel < 0) || (currLevel < maxLevel))
				&& !entityDictionaryService.isMultiLevelLeaf(nodeService.getType(compoListDataItem.getProduct()));
	}

	private Double computeCompoListVol(ProductData subProductData, Double subVol, CompoListDataItem compoListDataItem) {
		ProductData partProduct = (ProductData) alfrescoRepository.findOne(compoListDataItem.getProduct());
		Double volUsed = FormulationHelper.getNetVolume(compoListDataItem, partProduct);
		Double netVol = FormulationHelper.getNetVolume(subProductData, FormulationHelper.DEFAULT_NET_WEIGHT);
		if ((volUsed != null) && (netVol != null) && (netVol != 0d) && (subVol != null)) {
			volUsed = (volUsed / netVol) * subVol;
		}
		return volUsed;
	}

	private Double computeCompoListWeight(ProductData subProductData, Double parentProductNetWeight, CompoListDataItem compoListDataItem) {
		Double compoListWeight = FormulationHelper.getQtyInKg(compoListDataItem);
		Double compoProductNetWeight = FormulationHelper.getNetWeight(subProductData, FormulationHelper.DEFAULT_NET_WEIGHT);
		if ((compoProductNetWeight != 0d) && (parentProductNetWeight != null)) {
			compoListWeight = (compoListWeight / compoProductNetWeight) * parentProductNetWeight;
		}
		return compoListWeight;
	}

	private boolean omitItem(CompoListDataItem compoListDataItem) {
		//omit item if parent is omitted
		while (compoListDataItem != null) {
			if (DeclarationType.Omit.equals(compoListDataItem.getDeclType())) {
				return true;
			}
			compoListDataItem = compoListDataItem.getParent();
		}
		return false;
	}

	/**
	 * <p>createCharactDetails.</p>
	 *
	 * @param dataListItems a {@link java.util.List} object.
	 * @return a {@link fr.becpg.repo.product.data.CharactDetails} object.
	 */
	protected CharactDetails createCharactDetails(List<NodeRef> dataListItems) {

		List<NodeRef> tmp = new ArrayList<>();
		if (dataListItems != null) {
			for (NodeRef dataListItem : dataListItems) {
				
				SimpleCharactDataItem o = (SimpleCharactDataItem) alfrescoRepository.findOne(dataListItem);
					if (o != null) {
						tmp.add(o.getCharactNodeRef());
				}
			}
		}

		return new CharactDetails(tmp);
	}

	/**
	 * <p>visitPart.</p>
	 *
	 * @param componentDataList a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param currLevel a {@link java.lang.Integer} object.
	 * @throws fr.becpg.repo.formulation.FormulateException if any.
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param partProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param qties a {@link fr.becpg.repo.product.formulation.FormulatedQties} object
	 * @param context a {@link fr.becpg.repo.product.formulation.details.CharactDetailsVisitorContext} object
	 */
	protected void visitPart(CharactDetailsVisitorContext context, ProductData formulatedProduct, ProductData partProduct, NodeRef componentDataList, FormulatedQties qties,
			Integer currLevel) throws FormulateException {

		if (partProduct == null) {
			return;
		}

		if (!alfrescoRepository.hasDataList(partProduct, dataListType)) {
			logger.debug("no datalist for this product, exit. dataListType: " + dataListType + " entity: " + partProduct.getNodeRef());
			return;
		}

		List<SimpleCharactDataItem> simpleCharactDataList = alfrescoRepository.getList(partProduct, dataListType, dataListType);

		for (SimpleCharactDataItem simpleCharact : simpleCharactDataList) {
			if ((simpleCharact != null) && context.getCharactDetails().hasElement(simpleCharact.getCharactNodeRef())) {

				String unit = provideUnit(context, simpleCharact);

				// calculate charact from qty or vol ?
				boolean formulateInVol = shouldFormulateInVolume(context, partProduct, simpleCharact);
				boolean forceWeight = shouldForceWeight(context, partProduct, simpleCharact);

				Double qtyUsed = qties.getQtyUsed(formulateInVol);
				Double netQty = qties.getNetQty(forceWeight);

				if ((qtyUsed != null)) {
					calculateCharactDetailsValues(context, formulatedProduct, partProduct, componentDataList, currLevel, simpleCharact, unit, qtyUsed,
							netQty);
				}
			}
		}
	}

	private void calculateCharactDetailsValues(CharactDetailsVisitorContext context, ProductData formulatedProduct, ProductData partProduct,
			NodeRef componentDataList, Integer currLevel, SimpleCharactDataItem simpleCharact, String unit, Double qtyUsed, Double netQty) {
		Double value = FormulationHelper.calculateValue(0d, qtyUsed, extractValue(formulatedProduct, partProduct, simpleCharact), netQty);

		CharactDetailsValue currentCharactDetailsValue = null;

		if ((value != null) && (simpleCharact.shouldDetailIfZero() || (value != 0d))) {

			if (logger.isDebugEnabled()) {
				logger.debug("Add new charact detail. Charact: "
						+ nodeService.getProperty(simpleCharact.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME) + " - entityNodeRef: "
						+ partProduct.getName() + " - netQty: " + netQty + " - qty: " + qtyUsed + " - value: " + value);
			}

			currentCharactDetailsValue = new CharactDetailsValue(formulatedProduct.getNodeRef(), partProduct.getNodeRef(),
					componentDataList, value, currLevel, unit);
			
			if ((simpleCharact instanceof ForecastValueDataItem forecastValue) && !context.getCharactDetails().isMultiple()) {

				for (String forecastColumn : forecastValue.getForecastColumns()) {
					logger.debug("ForecastDataItem, " + forecastColumn + "=" + forecastValue.getForecastValue(forecastColumn));
					// add future and past values
					if (forecastValue.getForecastValue(forecastColumn) != null) {
						currentCharactDetailsValue
						.setForecastValue(forecastColumn, FormulationHelper.calculateValue(0d, qtyUsed, forecastValue.getForecastValue(forecastColumn), netQty));
					}
				}
			}

			if ((simpleCharact instanceof MinMaxValueDataItem) && !context.getCharactDetails().isMultiple()) {
				MinMaxValueDataItem minMaxValue = (MinMaxValueDataItem) simpleCharact;
				minMaxValue.setMaxi(FormulationHelper.flatPercValue(minMaxValue.getMaxi(), unit));
				minMaxValue.setMini(FormulationHelper.flatPercValue(minMaxValue.getMini(), unit));

				logger.debug("minMaxValue, prev=" + minMaxValue.getMini() + ", maxi=" + minMaxValue.getMaxi());
				// add future and past values
				if (minMaxValue.getMini() != null) {
					currentCharactDetailsValue.setMini(FormulationHelper.calculateValue(0d, qtyUsed, minMaxValue.getMini(), netQty));
				}

				if (minMaxValue.getMaxi() != null) {
					currentCharactDetailsValue.setMaxi(FormulationHelper.calculateValue(0d, qtyUsed, minMaxValue.getMaxi(), netQty));
				}
			}
			
			if (!context.getCharactDetails().isMultiple()) {
				provideAdditionalValues(context.getRootProductData(), formulatedProduct, simpleCharact, unit, qtyUsed, netQty, currentCharactDetailsValue);
			}
			
			context.getCharactDetails().addKeyValue(simpleCharact.getCharactNodeRef(), currentCharactDetailsValue);
		}
	}

	/**
	 * <p>shouldForceWeight.</p>
	 *
	 * @param context a {@link fr.becpg.repo.product.formulation.details.CharactDetailsVisitorContext} object
	 * @param partProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param simpleCharact a {@link fr.becpg.repo.repository.model.SimpleCharactDataItem} object
	 * @return a boolean
	 */
	protected boolean shouldForceWeight(CharactDetailsVisitorContext context, ProductData partProduct, SimpleCharactDataItem simpleCharact) {
		return false;
	}

	/**
	 * <p>shouldFormulateInVolume.</p>
	 *
	 * @param context a {@link fr.becpg.repo.product.formulation.details.CharactDetailsVisitorContext} object
	 * @param partProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param simpleCharact a {@link fr.becpg.repo.repository.model.SimpleCharactDataItem} object
	 * @return a boolean
	 */
	protected boolean shouldFormulateInVolume(CharactDetailsVisitorContext context, ProductData partProduct, SimpleCharactDataItem simpleCharact) {
		return (partProduct.getUnit() != null) && partProduct.getUnit().isVolume();
	}

	/**
	 * <p>provideUnit.</p>
	 *
	 * @param context a {@link fr.becpg.repo.product.formulation.details.CharactDetailsVisitorContext} object
	 * @param simpleCharact a {@link fr.becpg.repo.repository.model.SimpleCharactDataItem} object
	 * @return a {@link java.lang.String} object
	 */
	protected String provideUnit(CharactDetailsVisitorContext context, SimpleCharactDataItem simpleCharact) {
		if (simpleCharact instanceof UnitAwareDataItem unitAwareDataItem) {
			return unitAwareDataItem.getUnit();
		}
		return null;
	}

	/**
	 * <p>provideAdditionalValues.</p>
	 *
	 * @param rootProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param simpleCharact a {@link fr.becpg.repo.repository.model.SimpleCharactDataItem} object
	 * @param unit a {@link java.lang.String} object
	 * @param qtyUsed a {@link java.lang.Double} object
	 * @param netQty a {@link java.lang.Double} object
	 * @param currentCharactDetailsValue a {@link fr.becpg.repo.product.data.CharactDetailsValue} object
	 */
	protected void provideAdditionalValues(ProductData rootProduct, ProductData formulatedProduct, SimpleCharactDataItem simpleCharact, String unit, Double qtyUsed, Double netQty, CharactDetailsValue currentCharactDetailsValue) {
		// nothing by default
	}

	/**
	 * <p>extractValue.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param partProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param simpleCharact a {@link fr.becpg.repo.repository.model.SimpleCharactDataItem} object
	 * @return a {@link java.lang.Double} object
	 */
	protected Double extractValue(ProductData formulatedProduct, ProductData partProduct, SimpleCharactDataItem simpleCharact) {
		return simpleCharact.getValue();
	}
}
