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

import java.math.BigDecimal;
import java.util.Arrays;

import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.GS1Model;
import fr.becpg.model.PackModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.TareUnit;
import fr.becpg.repo.product.data.packaging.VariantPackagingData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.variant.filters.VariantFilters;

public class TareFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(TareFormulationHandler.class);

	private NodeService nodeService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		logger.debug("Tare visitor");

		// no compo => no formulation
		if (!formulatedProduct.hasCompoListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))
				&& !formulatedProduct.hasPackagingListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
			logger.debug("no compoList, no packagingList => no formulation");
			return true;
		}

		// Tare
		BigDecimal tarePrimary = calculateTareOfComposition(formulatedProduct);
		tarePrimary = tarePrimary.add(calculateTareOfPackaging(formulatedProduct));
		formulatedProduct.setTareUnit(TareUnit.kg);

		if (formulatedProduct.getAspects().contains(GS1Model.ASPECT_MEASURES_ASPECT)) {

			BigDecimal netWeightPrimary = new BigDecimal(
					FormulationHelper.getNetWeight(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT).toString());

			BigDecimal weightPrimary = tarePrimary.add(netWeightPrimary);

			formulatedProduct.getExtraProperties().put(GS1Model.PROP_WEIGHT, weightPrimary.doubleValue());

			formulatedProduct.getExtraProperties().put(GS1Model.PROP_SECONDARY_WEIGHT, null);
			formulatedProduct.getExtraProperties().put(GS1Model.PROP_SECONDARY_NET_WEIGHT, null);
			formulatedProduct.getExtraProperties().put(GS1Model.PROP_TERTIARY_WEIGHT, null);
			formulatedProduct.getExtraProperties().put(GS1Model.PROP_TERTIARY_NET_WEIGHT, null);

			VariantPackagingData variantPackagingData = formulatedProduct.getDefaultVariantPackagingData();

			if (variantPackagingData.getProductPerBoxes() != null) {

				BigDecimal tareSecondary = tarePrimary.multiply(new BigDecimal(variantPackagingData.getProductPerBoxes()))
						.add(variantPackagingData.getTareSecondary());

				BigDecimal netWeightSecondary = netWeightPrimary.multiply(new BigDecimal(variantPackagingData.getProductPerBoxes()));
				BigDecimal weightSecondary = tareSecondary.add(netWeightSecondary);

				formulatedProduct.getExtraProperties().put(GS1Model.PROP_SECONDARY_WEIGHT, weightSecondary.doubleValue());
				formulatedProduct.getExtraProperties().put(GS1Model.PROP_SECONDARY_NET_WEIGHT, netWeightSecondary.doubleValue());

				if (variantPackagingData.getBoxesPerPallet() != null) {

					BigDecimal tareTertiary = tareSecondary.multiply(new BigDecimal(variantPackagingData.getBoxesPerPallet()))
							.add(variantPackagingData.getTareTertiary());
					BigDecimal netWeightTertiary = netWeightSecondary.multiply(new BigDecimal(variantPackagingData.getBoxesPerPallet()));

					formulatedProduct.getExtraProperties().put(GS1Model.PROP_TERTIARY_WEIGHT, tareTertiary.add(netWeightTertiary));
					formulatedProduct.getExtraProperties().put(GS1Model.PROP_TERTIARY_NET_WEIGHT, netWeightTertiary.doubleValue());
				}
			}

		}

		if (tarePrimary.doubleValue() < 1) {
			logger.debug("Calculating tare in g: " + tarePrimary);
			tarePrimary = tarePrimary.multiply(new BigDecimal(1000d));
			formulatedProduct.setTareUnit(TareUnit.g);
		}
		formulatedProduct.setTare(tarePrimary.doubleValue());

		return true;
	}

	private BigDecimal calculateTareOfComposition(ProductData formulatedProduct) {
		BigDecimal totalTare = new BigDecimal(0d);
		for (CompoListDataItem compoList : formulatedProduct
				.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
			totalTare = totalTare.add(FormulationHelper.getTareInKg(compoList, nodeService));
		}
		return totalTare;
	}

	private BigDecimal calculateTareOfPackaging(ProductData formulatedProduct) {
		BigDecimal totalTare = new BigDecimal(0d);
		for (PackagingListDataItem packList : formulatedProduct
				.getPackagingList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
			// take in account only primary
			PackagingLevel level = PackagingLevel.Primary;
			if (nodeService.hasAspect(formulatedProduct.getNodeRef(), PackModel.ASPECT_PALLET)) {
				level = PackagingLevel.Secondary;
			}

			if ((packList.getPkgLevel() != null) && packList.getPkgLevel().equals(level)) {
				totalTare = totalTare.add(FormulationHelper.getTareInKg(packList, nodeService));
			}
		}
		return totalTare;
	}

}
