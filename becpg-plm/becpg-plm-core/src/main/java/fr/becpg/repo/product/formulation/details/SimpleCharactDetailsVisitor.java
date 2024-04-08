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
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.product.formulation.FormulatedQties;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.product.formulation.NutsCalculatingFormulationHandler;
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

	public interface SimpleCharactUnitProvider {
		public String provideUnit(SimpleCharactDataItem item);

	}

	/** {@inheritDoc} */
	@Override
	public CharactDetails visit(ProductData productData, List<NodeRef> dataListItems, Integer level) throws FormulateException {

		CharactDetails ret = createCharactDetails(dataListItems);

		if (level == null) {
			level = 0;
		}

		Double netQty = FormulationHelper.getNetQtyInLorKg(productData, FormulationHelper.DEFAULT_NET_WEIGHT);
		Double netWeight = FormulationHelper.getNetWeight(productData, FormulationHelper.DEFAULT_NET_WEIGHT);
		Double netVol = FormulationHelper.getNetVolume(productData, FormulationHelper.DEFAULT_NET_WEIGHT);

		visitRecur(productData, productData, ret, 0, level, netWeight, netVol, netQty);

		return ret;
	}

	/**
	 * <p>visitRecur.</p>
	 *
	 * @param subProductData a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param ret a {@link fr.becpg.repo.product.data.CharactDetails} object.
	 * @param currLevel a {@link java.lang.Integer} object.
	 * @param maxLevel a {@link java.lang.Integer} object.
	 * @param subWeight a {@link java.lang.Double} object.
	 * @param subVol a {@link java.lang.Double} object.
	 * @param netQty a {@link java.lang.Double} object.
	 * @return a {@link fr.becpg.repo.product.data.CharactDetails} object.
	 * @throws fr.becpg.repo.formulation.FormulateException if any.
	 */
	public CharactDetails visitRecur(ProductData rootProductData, ProductData subProductData, CharactDetails ret, Integer currLevel, Integer maxLevel, Double subWeight,
			Double subVol, Double netQty) throws FormulateException {

		if (!subProductData.isGeneric()
				&& subProductData.hasCompoListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {

			for (CompoListDataItem compoListDataItem : subProductData
					.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {

				//omit item if parent is omitted
				boolean omit = false;
				CompoListDataItem tmpCompoItem = compoListDataItem;
				while (tmpCompoItem != null && !omit) {
					omit = DeclarationType.Omit.equals(tmpCompoItem.getDeclType());
					tmpCompoItem = tmpCompoItem.getParent();
				}

				if (!omit && compoListDataItem != null) {

					Double weightUsed = FormulationHelper.getQtyInKg(compoListDataItem);
					Double netWeight = FormulationHelper.getNetWeight(subProductData, FormulationHelper.DEFAULT_NET_WEIGHT);
					if ((netWeight != 0d) && (subWeight != null)) {
						weightUsed = (weightUsed / netWeight) * subWeight;

					}

					ProductData partProduct = (ProductData) alfrescoRepository.findOne(compoListDataItem.getProduct());
					Double volUsed = FormulationHelper.getNetVolume(compoListDataItem, partProduct);
					Double netVol = FormulationHelper.getNetVolume(subProductData, FormulationHelper.DEFAULT_NET_WEIGHT);
					if ((volUsed != null) && (netVol != null) && (netVol != 0d) && (subVol != null)) {
						volUsed = (volUsed / netVol) * subVol;
					}

					ProductData compoListProduct = (ProductData) alfrescoRepository.findOne(compoListDataItem.getProduct());

					FormulatedQties qties = new FormulatedQties(weightUsed, volUsed, netQty, subWeight);

					visitPart(rootProductData, subProductData, compoListProduct, compoListDataItem.getNodeRef(), ret, qties, currLevel,null);
					if (((maxLevel < 0) || (currLevel < maxLevel))
							&& !entityDictionaryService.isMultiLevelLeaf(nodeService.getType(compoListDataItem.getProduct()))) {
						visitRecur(rootProductData, compoListProduct, ret, currLevel + 1, maxLevel, weightUsed, volUsed, netQty);
					}
				}
			}
		}

		return ret;
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
	 * @param parent a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param componentDataList a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param charactDetails a {@link fr.becpg.repo.product.data.CharactDetails} object.
	 * @param weightUsed a {@link java.lang.Double} object.
	 * @param volUsed a {@link java.lang.Double} object.
	 * @param netQtyInLorKg a {@link java.lang.Double} object.
	 * @param netWeight a {@link java.lang.Double} object.
	 * @param currLevel a {@link java.lang.Integer} object.
	 * @param unitProvider a {@link fr.becpg.repo.product.formulation.details.SimpleCharactDetailsVisitor.SimpleCharactUnitProvider} object.
	 * @throws fr.becpg.repo.formulation.FormulateException if any.
	 */
	protected void visitPart(ProductData rootProduct, ProductData formulatedProduct, ProductData partProduct, NodeRef componentDataList, CharactDetails charactDetails,
			FormulatedQties qties, Integer currLevel, SimpleCharactUnitProvider unitProvider) throws FormulateException {

		if (partProduct == null) {
			return;
		}

		if (!alfrescoRepository.hasDataList(partProduct, dataListType)) {
			logger.debug("no datalist for this product, exit. dataListType: " + dataListType + " entity: " + partProduct.getNodeRef());
			return;
		}

		List<SimpleCharactDataItem> simpleCharactDataList = alfrescoRepository.getList(partProduct, dataListType, dataListType);

		for (SimpleCharactDataItem simpleCharact : simpleCharactDataList) {
			if ((simpleCharact != null) && charactDetails.hasElement(simpleCharact.getCharactNodeRef())) {

				String unit = null;
				if (unitProvider != null) {
					unit = unitProvider.provideUnit(simpleCharact);
				}

				if ((unit == null) && (simpleCharact instanceof UnitAwareDataItem)) {
					unit = ((UnitAwareDataItem) simpleCharact).getUnit();
				}
				if (unit == null) {
					unit = provideUnit();
				}
				

				// calculate charact from qty or vol ?
				boolean formulateInVol = (partProduct.getUnit() != null) && partProduct.getUnit().isVolume();
				boolean forceWeight = false;

				if (simpleCharact instanceof PhysicoChemListDataItem) {
					if (FormulationHelper.isCharactFormulatedFromVol(nodeService, simpleCharact)) {
						formulateInVol = true;
					} else {
						formulateInVol = false;
						forceWeight = true;
					}
				} else if (simpleCharact instanceof NutListDataItem) {
					
					if(unit!=null) {
						unit = NutsCalculatingFormulationHandler.calculateUnit(rootProduct.getUnit(), rootProduct.getServingSizeUnit(), unit.split("/")[0]);
					}
					
					if (formulateInVol && (partProduct.getServingSizeUnit() != null) && partProduct.getServingSizeUnit().isWeight()) {
						if ((formulatedProduct.getServingSizeUnit() != null) && formulatedProduct.getServingSizeUnit().isWeight()) {
							formulateInVol = false;
							forceWeight = true;
						} else {
							formulateInVol = false;
						}
					}

				} else if (simpleCharact instanceof IngListDataItem) {
					formulateInVol = false;
					forceWeight = true;
				}

				// calculate charact from qty or vol ?
				Double qtyUsed = qties.getQtyUsed(formulateInVol);
				Double netQty = qties.getNetQty(forceWeight);

				// Calculate values
				if ((qtyUsed != null)) {
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
						
						if ((simpleCharact instanceof ForecastValueDataItem forecastValue) && !charactDetails.isMultiple()) {

							for (String forecastColumn : forecastValue.getForecastColumns()) {
								logger.debug("ForecastDataItem, " + forecastColumn + "=" + forecastValue.getForecastValue(forecastColumn));
								// add future and past values
								if (forecastValue.getForecastValue(forecastColumn) != null) {
									currentCharactDetailsValue
									.setForecastValue(forecastColumn, FormulationHelper.calculateValue(0d, qtyUsed, forecastValue.getForecastValue(forecastColumn), netQty));
								}
							}
						}

						if ((simpleCharact instanceof MinMaxValueDataItem) && !charactDetails.isMultiple()) {
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
						
						if (!charactDetails.isMultiple()) {
							provideAdditionalValues(rootProduct, formulatedProduct, simpleCharact, unit, qtyUsed, netQty, currentCharactDetailsValue);
						}
						
						charactDetails.addKeyValue(simpleCharact.getCharactNodeRef(), currentCharactDetailsValue);
					}
				}
			}
		}
	}

	protected String provideUnit() {
		return null;
	}

	protected void provideAdditionalValues(ProductData rootProduct, ProductData formulatedProduct, SimpleCharactDataItem simpleCharact, String unit, Double qtyUsed, Double netQty, CharactDetailsValue currentCharactDetailsValue) {
		// nothing by default
	}

	protected Double extractValue(ProductData formulatedProduct, ProductData partProduct, SimpleCharactDataItem simpleCharact) {
		return simpleCharact.getValue();
	}
}
