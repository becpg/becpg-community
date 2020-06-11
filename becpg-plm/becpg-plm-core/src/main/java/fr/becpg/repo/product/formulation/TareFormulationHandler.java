/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG.
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

import java.math.BigDecimal;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.GS1Model;
import fr.becpg.model.PackModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.TareUnit;
import fr.becpg.repo.product.data.packaging.VariantPackagingData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.variant.filters.VariantFilters;

public class TareFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(TareFormulationHandler.class);

	@Autowired
	protected AlfrescoRepository<ProductData> alfrescoRepository;

	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		logger.debug("Tare visitor");

		// no compo => no formulation
		if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL)
				|| (formulatedProduct instanceof ProductSpecificationData)
				|| (!formulatedProduct.hasCompoListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))
						&& !formulatedProduct
								.hasPackagingListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>())))) {
			logger.debug("no compoList, no packagingList => no formulation");
			return true;
		}

		// Do not calculate Tare on RawMaterialData
		if (formulatedProduct instanceof RawMaterialData) {
			return true;
		}

		// Tare
		BigDecimal tarePrimary = calculateTareOfComposition(formulatedProduct);
		VariantPackagingData variantPackagingData = formulatedProduct.getDefaultVariantPackagingData();

		if (variantPackagingData != null) {
			tarePrimary = tarePrimary.add(variantPackagingData.getTarePrimary());
		}

		BigDecimal netWeightPrimary = new BigDecimal(
				FormulationHelper.getNetWeight(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT).toString());

		BigDecimal weightPrimary = tarePrimary.add(netWeightPrimary);

		formulatedProduct.setWeightPrimary(weightPrimary.doubleValue());

		if ((variantPackagingData != null) && (variantPackagingData.getProductPerBoxes() != null)) {

			BigDecimal tareSecondary = tarePrimary.multiply(new BigDecimal(variantPackagingData.getProductPerBoxes()))
					.add(variantPackagingData.getTareSecondary());

			BigDecimal netWeightSecondary = netWeightPrimary.multiply(new BigDecimal(variantPackagingData.getProductPerBoxes()));
			BigDecimal weightSecondary = tareSecondary.add(netWeightSecondary);
			formulatedProduct.setWeightSecondary(weightSecondary.doubleValue());
			formulatedProduct.setNetWeightSecondary(netWeightSecondary.doubleValue());

			if (variantPackagingData.getBoxesPerPallet() != null) {

				BigDecimal tareTertiary = tareSecondary.multiply(new BigDecimal(variantPackagingData.getBoxesPerPallet()))
						.add(variantPackagingData.getTareTertiary());

				BigDecimal netWeightTertiary = netWeightSecondary.multiply(new BigDecimal(variantPackagingData.getBoxesPerPallet()));
				BigDecimal weightTertiary = tareTertiary.add(netWeightTertiary);
				formulatedProduct.setWeightTertiary(weightTertiary.doubleValue());
				formulatedProduct.setNetWeightTertiary(netWeightTertiary.doubleValue());

			}
		}

		if (formulatedProduct instanceof FinishedProductData || formulatedProduct.getAspects().contains(GS1Model.ASPECT_MEASURES_ASPECT)) {

			if (variantPackagingData != null) {
				formulatedProduct.getExtraProperties().put(GS1Model.PROP_WEIGHT, formulatedProduct.getWeightPrimary());
				
				if (!variantPackagingData.isManualPrimary()) {
					formulatedProduct.getExtraProperties().put(GS1Model.PROP_WIDTH, variantPackagingData.getWidth());
					formulatedProduct.getExtraProperties().put(GS1Model.PROP_DEPTH, variantPackagingData.getDepth());
					formulatedProduct.getExtraProperties().put(GS1Model.PROP_HEIGHT, variantPackagingData.getHeight());
				}
				
				formulatedProduct.getExtraProperties().put(GS1Model.PROP_SECONDARY_WEIGHT, formulatedProduct.getWeightSecondary());
				formulatedProduct.getExtraProperties().put(GS1Model.PROP_SECONDARY_NET_WEIGHT, formulatedProduct.getNetWeightSecondary());
				
				if (!variantPackagingData.isManualSecondary()) {
					formulatedProduct.getExtraProperties().put(GS1Model.PROP_SECONDARY_WIDTH, variantPackagingData.getSecondaryWidth());
					formulatedProduct.getExtraProperties().put(GS1Model.PROP_SECONDARY_DEPTH, variantPackagingData.getSecondaryDepth());
					formulatedProduct.getExtraProperties().put(GS1Model.PROP_SECONDARY_HEIGHT, variantPackagingData.getSecondaryHeight());
				}
				
				formulatedProduct.getExtraProperties().put(GS1Model.PROP_TERTIARY_NET_WEIGHT, formulatedProduct.getNetWeightTertiary());
				formulatedProduct.getExtraProperties().put(GS1Model.PROP_TERTIARY_WEIGHT, formulatedProduct.getWeightTertiary());
				
				if (!variantPackagingData.isManualTertiary()) {
					formulatedProduct.getExtraProperties().put(GS1Model.PROP_TERTIARY_WIDTH, variantPackagingData.getTertiaryWidth());
					formulatedProduct.getExtraProperties().put(GS1Model.PROP_TERTIARY_DEPTH, variantPackagingData.getTertiaryDepth());
					formulatedProduct.getExtraProperties().put(GS1Model.PROP_PALLET_TYPE_CODE, variantPackagingData.getPalletTypeCode());
					formulatedProduct.getExtraProperties().put(GS1Model.PROP_PLATFORMTERMSANSCONDITION_CODE, variantPackagingData.getPlatformTermsAndConditionsCode());
				

					if (formulatedProduct instanceof FinishedProductData || formulatedProduct.getAspects().contains(PackModel.ASPECT_PALLET)) {
						
						formulatedProduct.getExtraProperties().put(PackModel.PROP_PALLET_PRODUCTS_PER_BOX, variantPackagingData.getProductPerBoxes());

						formulatedProduct.getExtraProperties().put(PackModel.PROP_PALLET_LAYERS, variantPackagingData.getPalletLayers());
						formulatedProduct.getExtraProperties().put(PackModel.PROP_PALLET_BOXES_PER_LAYER,
								variantPackagingData.getPalletBoxesPerLayer());
						formulatedProduct.getExtraProperties().put(PackModel.PROP_PALLET_BOXES_PER_PALLET, variantPackagingData.getBoxesPerPallet());
						formulatedProduct.getExtraProperties().put(PackModel.PROP_PALLET_STACKING_MAX_WEIGHT,
								variantPackagingData.getPalletStackingMaxWeight());
						formulatedProduct.getExtraProperties().put(PackModel.PROP_PALLET_BOXES_PER_LAST_LAYER,
								variantPackagingData.getPalletBoxesPerLastLayer());
						formulatedProduct.getExtraProperties().put(PackModel.PROP_PALLET_NUMBER_ON_GROUND,
								variantPackagingData.getPalletNumberOnGround());
						formulatedProduct.getExtraProperties().put(PackModel.PROP_PALLET_HEIGHT, variantPackagingData.getPalletHeight());

					}
				}
			}
		}

		if ((formulatedProduct.getUnit() != null) && formulatedProduct.getUnit().isLb()) {
			formulatedProduct.setTareUnit(TareUnit.lb);
			Double tareInLb = ProductUnit.kgToLb(tarePrimary.doubleValue());
			if (tareInLb < 1) {
				tareInLb = ProductUnit.lbToOz(tareInLb);
				formulatedProduct.setTareUnit(TareUnit.oz);
			}

			formulatedProduct.setTare(tareInLb);
		} else {

			formulatedProduct.setTareUnit(TareUnit.kg);

			if (tarePrimary.doubleValue() < 1) {
				tarePrimary = tarePrimary.multiply(BigDecimal.valueOf(1000d));
				formulatedProduct.setTareUnit(TareUnit.g);
			}
			formulatedProduct.setTare(tarePrimary.doubleValue());

		}
		return true;
	}

	private BigDecimal calculateTareOfComposition(ProductData formulatedProduct) {
		BigDecimal totalTare = BigDecimal.valueOf(0d);
		if(logger.isDebugEnabled()){
			logger.debug("calculateTareOfComposition dropPackagingOfComponents: " + formulatedProduct.getDropPackagingOfComponents());
		}
		if(formulatedProduct.getDropPackagingOfComponents() == null || !formulatedProduct.getDropPackagingOfComponents()){
			for (CompoListDataItem compoList : formulatedProduct
					.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
				if (compoList.getProduct() != null) {
					totalTare = totalTare.add(FormulationHelper.getTareInKg(compoList, alfrescoRepository.findOne(compoList.getProduct())));
				}
			}
		}
		return totalTare;
	}

}
