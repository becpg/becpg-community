/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG.
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
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.GS1Model;
import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.PackagingListUnit;
import fr.becpg.repo.product.data.constraints.TareUnit;
import fr.becpg.repo.product.data.packaging.PackagingData;
import fr.becpg.repo.product.data.packaging.VariantPackagingData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.variant.filters.VariantFilters;

public class TareFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(TareFormulationHandler.class);

	@Autowired
	protected AlfrescoRepository<ProductData> alfrescoRepository;

	private NodeService nodeService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		logger.debug("Tare visitor");

		// no compo => no formulation
		if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL)
				|| (!formulatedProduct.hasCompoListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))
						&& !formulatedProduct
								.hasPackagingListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>())))) {
			logger.debug("no compoList, no packagingList => no formulation");
			return true;
		}

		// Tare
		BigDecimal tarePrimary = calculateTareOfComposition(formulatedProduct);
		tarePrimary = tarePrimary.add(calculateTareOfPackaging(formulatedProduct));

		BigDecimal netWeightPrimary = new BigDecimal(
				FormulationHelper.getNetWeight(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT).toString());

		BigDecimal weightPrimary = tarePrimary.add(netWeightPrimary);

		formulatedProduct.setWeightPrimary(weightPrimary.doubleValue());

		VariantPackagingData variantPackagingData = formulatedProduct.getDefaultVariantPackagingData();

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

		if (formulatedProduct.getAspects().contains(GS1Model.ASPECT_MEASURES_ASPECT)) {
			formulatedProduct.getExtraProperties().put(GS1Model.PROP_WEIGHT, formulatedProduct.getWeightPrimary());
			formulatedProduct.getExtraProperties().put(GS1Model.PROP_SECONDARY_WEIGHT, formulatedProduct.getWeightSecondary());
			formulatedProduct.getExtraProperties().put(GS1Model.PROP_SECONDARY_NET_WEIGHT, formulatedProduct.getNetWeightSecondary());
			formulatedProduct.getExtraProperties().put(GS1Model.PROP_TERTIARY_WEIGHT, formulatedProduct.getWeightTertiary());
			formulatedProduct.getExtraProperties().put(GS1Model.PROP_TERTIARY_NET_WEIGHT, formulatedProduct.getNetWeightTertiary());
		}

		formulatedProduct.setTareUnit(TareUnit.kg);

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
			totalTare = totalTare.add(FormulationHelper.getTareInKg(compoList, alfrescoRepository));
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

			if (nodeService.hasAspect(packList.getProduct(), PackModel.ASPECT_PALLET) && PackagingLevel.Secondary.equals(packList.getPkgLevel())
					&& PackagingListUnit.PP.equals(packList.getPackagingListUnit())
					&& PLMModel.TYPE_PACKAGINGKIT.equals(nodeService.getType(packList.getComponent()))) {
				ProductData kitData = alfrescoRepository.findOne(packList.getProduct());
				BigDecimal kitTare = new BigDecimal(0d);

				PackagingData kitPackagingData = new PackagingData(kitData.getVariants());
				if (kitData.hasPackagingListEl()) {
					for (PackagingListDataItem kitPackagingDataItem : kitData.getPackagingList()) {
						if (PackagingLevel.Primary.equals(kitPackagingDataItem.getPkgLevel())) {
							BigDecimal kitPkgDataTare = FormulationHelper.getTareInKg(kitPackagingDataItem, alfrescoRepository);
							kitPackagingData.addTarePrimary(kitPackagingDataItem.getVariants(), kitPkgDataTare);

							kitTare = kitTare.add(kitPkgDataTare);
						}

					}

				}

				totalTare = totalTare.add(kitTare);
			}

			if ((packList.getPkgLevel() != null) && packList.getPkgLevel().equals(level)) {
				totalTare = totalTare.add(FormulationHelper.getTareInKg(packList, alfrescoRepository));
			}
		}
		return totalTare;
	}

}
