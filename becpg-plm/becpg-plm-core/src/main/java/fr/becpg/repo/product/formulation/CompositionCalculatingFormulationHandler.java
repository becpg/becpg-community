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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.variant.filters.VariantFilters;
import fr.becpg.repo.variant.model.VariantData;

/**
 * <p>CompositionCalculatingFormulationHandler class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class CompositionCalculatingFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(CompositionCalculatingFormulationHandler.class);


	private AlfrescoRepository<ProductData> alfrescoRepository;

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		logger.debug("Composition calculating visitor");

		if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL)
				|| formulatedProduct instanceof ProductSpecificationData) {
			return true;
		}

		// no compo => no formulation
		if (!formulatedProduct.hasCompoListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
			logger.debug("no compo => no formulation");
			return true;
		}


		Composite<CompoListDataItem> compositeAll = CompositeHelper.getHierarchicalCompoList(formulatedProduct.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE)));

		// Variants
		List<VariantData> variants = formulatedProduct.getVariants();

		if ((variants == null) || variants.isEmpty()) {
			variants = new ArrayList<>();

		} else {
			for (VariantData variantData : formulatedProduct.getVariants()) {

				variantData.reset();

				if (Boolean.TRUE.equals(variantData.getIsDefaultVariant())) {
					formulatedProduct.setDefaultVariantData(variantData);
				}
			}

		}

		// A least one variant Data
		if (formulatedProduct.getDefaultVariantData() == null) {
			formulatedProduct.setDefaultVariantData(new VariantData());
			variants.add(formulatedProduct.getDefaultVariantData());
		}

		visitVariantData(variants, compositeAll, formulatedProduct, formulatedProduct.getProductLossPerc());


		Double qtyUsed = formulatedProduct.getDefaultVariantData().getRecipeQtyUsed();

		formulatedProduct.setRecipeQtyUsed(qtyUsed);

		formulatedProduct.setRecipeQtyUsedWithLossPerc(formulatedProduct.getDefaultVariantData().getRecipeQtyUsedWithLossPerc());


		Double netWeight = formulatedProduct.getNetWeight();
		
		Double manualYield = formulatedProduct.getManualYield();
		
		if(manualYield!=null && manualYield!=0d  && (qtyUsed != null) && (qtyUsed != 0d)) {
			formulatedProduct.setYield(manualYield);
	   } else if ((netWeight != null) && (qtyUsed != null) && (qtyUsed != 0d)) {
			formulatedProduct.setYield((100 * netWeight) / qtyUsed);
		} else {
			formulatedProduct.setYield(null);
		}

        // Product-level volume metrics from default variant aggregates
        Double volumeUsed = formulatedProduct.getDefaultVariantData().getRecipeVolumeUsed();
        formulatedProduct.setRecipeVolumeUsed(volumeUsed);

        Double netVolume = FormulationHelper.getNetVolume(formulatedProduct, null, null);
        if ((netVolume != null) && (volumeUsed != null) && (volumeUsed != 0d)) {
            formulatedProduct.setYieldVolume((100 * netVolume) / volumeUsed);
        }

		// generic raw material
		if (formulatedProduct instanceof RawMaterialData rawMaterialData) {
			calculateAttributesOfGenericRawMaterial(rawMaterialData, compositeAll);
		} else {
			Double vol = netVolume != null ? netVolume : volumeUsed;
			Double weight = netWeight != null ? netWeight : qtyUsed;
			if ((weight != null) && (weight != 0d) && (vol != null) && (vol != 0d)) {
				formulatedProduct.setDensity(weight / vol);
			} else {
				formulatedProduct.setDensity(null);
			}
		}

		return true;
	}

	private void visitVariantData(List<VariantData> variants, Composite<CompoListDataItem> composite, ProductData formulatedProduct, Double parentLossRatio) {

		for (Composite<CompoListDataItem> component : composite.getChildren()) {

			if (!DeclarationType.Omit.equals(component.getData().getDeclType())) {
				
				CompoListDataItem compoListDataItem = component.getData();
				ProductData componentProduct = alfrescoRepository.findOne(compoListDataItem.getProduct());
	
				Double lossPerc = FormulationHelper.calculateLossPerc(parentLossRatio,FormulationHelper.getComponentLossPerc(componentProduct, compoListDataItem));

				if (!component.isLeaf()) {
					visitVariantData(variants, component, formulatedProduct, lossPerc);
				} else {
					for (VariantData variantData : variants) {

						if ((component.getData().getVariants() == null) || component.getData().getVariants().contains(variantData.getNodeRef())) {
							Double recipeQtyUsedWithLossPerc = ((FormulationHelper.getQtyInKg(compoListDataItem)
									* FormulationHelper.getYield(component.getData()) * (1 + (lossPerc / 100))) / 100);

							Double recipeQtyUsed = (FormulationHelper.getQtyInKg(compoListDataItem)
									* FormulationHelper.getYield(component.getData())) / 100;
							
							if(formulatedProduct.getManualYield()!=null && formulatedProduct.getManualYield()!=0d) {
								recipeQtyUsed = (recipeQtyUsed * 100) / formulatedProduct.getManualYield();
								recipeQtyUsedWithLossPerc = (recipeQtyUsedWithLossPerc * 100) / formulatedProduct.getManualYield();
							}
							
							
							Double recipeVolumeUsed = FormulationHelper.getNetVolume(compoListDataItem, componentProduct);
							if(recipeVolumeUsed == null) {
								recipeVolumeUsed = 0d;
							}
							
							recipeQtyUsedWithLossPerc += variantData.getRecipeQtyUsedWithLossPerc();
							recipeQtyUsed += variantData.getRecipeQtyUsed();
							recipeVolumeUsed += variantData.getRecipeVolumeUsed();

							variantData.setRecipeQtyUsedWithLossPerc(recipeQtyUsedWithLossPerc);
							variantData.setRecipeQtyUsed(recipeQtyUsed);
							variantData.setRecipeVolumeUsed(recipeVolumeUsed);
						}

					}

				}
			}
		}

	}
	
	private void calculateAttributesOfGenericRawMaterial(RawMaterialData rawMaterialData, Composite<CompoListDataItem> composite) {
		List<NodeRef> supplierNodeRefs = new ArrayList<>();
		List<NodeRef> plantNodeRefs = new ArrayList<>();
		List<NodeRef> supplierPlantNodeRefs = new ArrayList<>();
		List<NodeRef> geoOriginsNodeRefs = new ArrayList<>();
		Double density = 0d;
		int rawMaterialsWithDensity = 0;
		for (Composite<CompoListDataItem> component : composite.getChildren()) {
			if ((component.getData().getQtySubFormula() != null) && !component.getData().getQtySubFormula().isNaN()
					&& !component.getData().getQtySubFormula().isInfinite() && (component.getData().getQtySubFormula() > 0)) {
				ProductData productData = alfrescoRepository.findOne(component.getData().getProduct());

				if (productData instanceof RawMaterialData componentRawMaterialData) {
					for (NodeRef supplierNodeRef : componentRawMaterialData.getSuppliers()) {
						if (!supplierNodeRefs.contains(supplierNodeRef)) {
							supplierNodeRefs.add(supplierNodeRef);
						}
					}
					for (NodeRef plantNodeRef : productData.getPlants()) {
						if (!plantNodeRefs.contains(plantNodeRef)) {
							plantNodeRefs.add(plantNodeRef);
						}
					}

					for (NodeRef originGeoNodeRef : componentRawMaterialData.getGeoOrigins()) {
						if (!geoOriginsNodeRefs.contains(originGeoNodeRef)) {
							geoOriginsNodeRefs.add(originGeoNodeRef);
						}
					}

					for (NodeRef supplierPlantNodeRef : componentRawMaterialData.getSupplierPlants()) {
						if (!supplierPlantNodeRefs.contains(supplierPlantNodeRef)) {
							supplierPlantNodeRefs.add(supplierPlantNodeRef);
						}
					}

					if ((productData.getDensity() != null) && !productData.getDensity().isNaN() && !productData.getDensity().isInfinite()) {
						density += productData.getDensity();
						rawMaterialsWithDensity++;
					}
				}
			}
		}
		if ((density != 0d) && (rawMaterialsWithDensity != 0)) {
			rawMaterialData.setDensity(density / rawMaterialsWithDensity);
		}

		rawMaterialData.setSuppliers(supplierNodeRefs);
		rawMaterialData.setPlants(plantNodeRefs);
		rawMaterialData.setSupplierPlants(supplierPlantNodeRefs);
		rawMaterialData.setGeoOrigins(geoOriginsNodeRefs);
	}

}
