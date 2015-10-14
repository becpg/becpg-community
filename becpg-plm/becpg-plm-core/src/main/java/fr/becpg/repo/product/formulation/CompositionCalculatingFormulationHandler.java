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
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.variant.filters.VariantFilters;

public class CompositionCalculatingFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(CompositionCalculatingFormulationHandler.class);

	private NodeService nodeService;

	private AlfrescoRepository<ProductData> alfrescoRepository;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		logger.debug("Composition calculating visitor");
		
		if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL)) {
			return true;
		}

		// no compo => no formulation
		if (!formulatedProduct.hasCompoListEl(new VariantFilters<>())) {
			logger.debug("no compo => no formulation");
			return true;
		}
		
		Double netWeight = formulatedProduct.getNetWeight();
		Composite<CompoListDataItem> compositeAll = CompositeHelper.getHierarchicalCompoList(formulatedProduct.getCompoList());
		Composite<CompoListDataItem> compositeDefaultVariant = CompositeHelper.getHierarchicalCompoList(formulatedProduct.getCompoList(new VariantFilters<>()));

		// Yield
		visitYieldChildren(formulatedProduct, compositeDefaultVariant);

		Double qtyUsed = calculateQtyUsed(compositeDefaultVariant);
		formulatedProduct.setRecipeQtyUsed(qtyUsed);
		if (netWeight != null && qtyUsed != null && qtyUsed != 0d) {
			formulatedProduct.setYield(100 * netWeight / qtyUsed);
		}

		// Volume
		Double volumeUsed = calculateVolumeFromChildren(compositeDefaultVariant);
		formulatedProduct.setRecipeVolumeUsed(volumeUsed);
		Double netVolume = FormulationHelper.getNetVolume(formulatedProduct);
		if (netVolume != null && volumeUsed != 0d) {
			formulatedProduct.setYieldVolume(100 * netVolume / volumeUsed);
		}

		// generic raw material
		if (formulatedProduct instanceof RawMaterialData) {
			calculateAttributesOfGenericRawMaterial((RawMaterialData) formulatedProduct, compositeAll);
		} else {
			Double vol = netVolume != null ? netVolume : volumeUsed;
			Double weight = netWeight != null ? netWeight : qtyUsed;
			if (weight != null && weight != 0d && vol != null && vol != 0d) {
				formulatedProduct.setDensity(weight / vol);
			}
			else{
				formulatedProduct.setDensity(null);
			}
		}

		return true;
	}

	private void visitYieldChildren(ProductData formulatedProduct, Composite<CompoListDataItem> composite) throws FormulateException {

		for (Composite<CompoListDataItem> component : composite.getChildren()) {

			// calculate children
			if (!component.isLeaf()) {

				// take in account percentage
				if (component.getData().getCompoListUnit() != null && component.getData().getCompoListUnit().equals(CompoListUnit.Perc)) {

					visitYieldChildren(formulatedProduct, component);
					component.getData().setYieldPerc(null);
				} else {
					visitYieldChildren(formulatedProduct, component);

					// Yield
					component.getData().setYieldPerc(calculateYield(component));
				}
			}
		}
	}

	private Double calculateYield(Composite<CompoListDataItem> composite) throws FormulateException {

		Double yieldPerc = 100d;

		// qty Used in the sub formula
		Double qtyUsed = 0d;
		for (Composite<CompoListDataItem> component : composite.getChildren()) {

			Double qty = component.getData().getQty();
			if (qty != null) {
				// water can be taken in account on Raw Material
				if (component.isLeaf()) {
					qtyUsed += qty * FormulationHelper.getYield(component.getData()) / 100;
				} else {
					qtyUsed += qty;
				}
			}
		}

		// qty after process
		if (composite.getData().getQty() != null && qtyUsed != 0) {
			yieldPerc = composite.getData().getQty() / qtyUsed * 100;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("component: " + nodeService.getProperty(composite.getData().getProduct(), ContentModel.PROP_NAME) + " qtyAfterProcess: "
					+ composite.getData().getQty() + " - qtyUsed: " + qtyUsed + " yieldPerc: " + yieldPerc);
		}

		return yieldPerc;
	}

	private Double calculateQtyUsed(Composite<CompoListDataItem> composite) throws FormulateException {

		Double qty = 0d;

		for (Composite<CompoListDataItem> component : composite.getChildren()) {

			if (!component.isLeaf()) {
				// calculate children
				qty += calculateQtyUsed(component);
			} else {
				if (component.getData().getQty() != null) {
					qty += component.getData().getQty() * FormulationHelper.getYield(component.getData()) / 100;
				}
			}
		}
		return qty;
	}

	private Double calculateVolumeFromChildren(Composite<CompoListDataItem> composite) throws FormulateException {

		Double volume = 0d;

		for (Composite<CompoListDataItem> component : composite.getChildren()) {

			Double value;

			if (!component.isLeaf()) {
				// calculate children
				value = calculateVolumeFromChildren(component);
				component.getData().setVolume(value);

			} else {
				value = FormulationHelper.getNetVolume(component.getData(), nodeService);
				component.getData().setVolume(value);
			}
			volume += (value != null ? value : 0d);
		}
		return volume;
	}

	private void calculateAttributesOfGenericRawMaterial(RawMaterialData rawMaterialData, Composite<CompoListDataItem> composite) {
		List<NodeRef> supplierNodeRefs = new ArrayList<>();
		List<NodeRef> plantNodeRefs = new ArrayList<>();
		List<NodeRef> supplierPlantNodeRefs = new ArrayList<>();
		Double density = 0d;
		int rawMaterialsWithDensity = 0;
		for (Composite<CompoListDataItem> component : composite.getChildren()) {
			ProductData productData = alfrescoRepository.findOne(component.getData().getProduct());
			if (productData instanceof RawMaterialData) {
				for (NodeRef supplierNodeRef : ((RawMaterialData) productData).getSuppliers()) {
					if (!supplierNodeRefs.contains(supplierNodeRef)) {
						supplierNodeRefs.add(supplierNodeRef);
					}
				}
				for (NodeRef plantNodeRef : productData.getPlants()) {
					if (!plantNodeRefs.contains(plantNodeRef)) {
						plantNodeRefs.add(plantNodeRef);
					}
				}
				for (NodeRef supplierPlantNodeRef : ((RawMaterialData) productData).getSupplierPlants()) {
					if (!supplierPlantNodeRefs.contains(supplierPlantNodeRef)) {
						supplierPlantNodeRefs.add(supplierPlantNodeRef);
					}
				}

				if (productData.getDensity() != null) {
					density += productData.getDensity();
					rawMaterialsWithDensity++;
				}
			}

		}
		if (density != 0d && rawMaterialsWithDensity != 0) {
			rawMaterialData.setDensity(density / rawMaterialsWithDensity);
		}

		rawMaterialData.setSuppliers(supplierNodeRefs);
		rawMaterialData.setPlants(plantNodeRefs);
		rawMaterialData.setSupplierPlants(supplierPlantNodeRefs);
	}

}
