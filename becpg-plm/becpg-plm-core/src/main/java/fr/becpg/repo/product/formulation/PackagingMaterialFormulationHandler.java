package fr.becpg.repo.product.formulation;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackMaterialListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.variant.filters.VariantFilters;

public class PackagingMaterialFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(PackagingMaterialFormulationHandler.class);

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

		if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) 
				|| (formulatedProduct instanceof ProductSpecificationData)) {
			return true;
		}

		if (alfrescoRepository.hasDataList(formulatedProduct.getNodeRef(), PackModel.PACK_MATERIAL_LIST_TYPE)) {

			// no compo, no packagingList => no formulation
			if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL)
					|| (!formulatedProduct.hasCompoListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))
							|| !formulatedProduct
									.hasPackagingListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>())))) {
				logger.debug("no compoList, no packagingList => no formulation");
				return true;
			}
			// CompoList
			Map<NodeRef, BigDecimal> toUpdate = calculateMaterialOfComposition(formulatedProduct);

			// PackagingList
			for (PackagingListDataItem packagingItem : formulatedProduct.getPackagingList()) {
				calculateTareByMaterialItem(packagingItem, toUpdate, 1);
			}

			// Create/Update Packaging Material List
			if (formulatedProduct.getPackMaterialList() == null) {
				formulatedProduct.setPackMaterialList(new LinkedList<PackMaterialListDataItem>());
			}

			List<PackMaterialListDataItem> toRemove = new ArrayList<>();
			for (PackMaterialListDataItem packmaterial : formulatedProduct.getPackMaterialList()) {
				if (!toUpdate.containsKey(packmaterial.getPmlMaterial())) {
					toRemove.add(packmaterial);
				} else {
					packmaterial.setPmlWeight(toUpdate.get(packmaterial.getPmlMaterial()).doubleValue());
					toUpdate.remove(packmaterial.getPmlMaterial());
				}
			}

			for (Map.Entry<NodeRef, BigDecimal> entry : toUpdate.entrySet()) {
				formulatedProduct.getPackMaterialList().add(new PackMaterialListDataItem(entry.getKey(), entry.getValue().doubleValue()));
			}

			formulatedProduct.getPackMaterialList().removeAll(toRemove);

			// add detailable aspect
			for (PackMaterialListDataItem packMaterialListDataItem : formulatedProduct.getPackMaterialList()) {
				if (!packMaterialListDataItem.getAspects().contains(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM)) {
					packMaterialListDataItem.getAspects().add(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM);
				}
			}

		}
		return true;
	}

	private Map<NodeRef, BigDecimal> calculateMaterialOfComposition(ProductData formulatedProduct) {
		Map<NodeRef, BigDecimal> toUpdate = new HashMap<>();
		for (CompoListDataItem compoList : formulatedProduct
				.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
			if (compoList.getProduct() != null) {

				ProductData compoProduct = alfrescoRepository.findOne(compoList.getProduct());

				Double qtyUsed = FormulationHelper.getQtyInKg(compoList);
				ProductUnit compoListUnit = compoList.getCompoListUnit();

				if ((qtyUsed != null) && (qtyUsed > 0)) {
					if (compoProduct.getPackMaterialList() != null) {

						// get compoProduct qty
						Double compoProductQty = compoProduct.getQty();
						if (compoProductQty == null) {
							compoProductQty = 1d;
						}

						if (compoListUnit.isP()) {
							if ((compoProduct.getUnit() != null) && !compoProduct.getUnit().isP()) {
								compoProductQty = 1d;
							}

						} else if (compoListUnit.isWeight() || compoListUnit.isVolume()) {

							compoProductQty = FormulationHelper.getNetQtyInLorKg(compoProduct, 1d);
						}
						for (PackMaterialListDataItem packMateriDataItem : compoProduct.getPackMaterialList()) {
							if ((compoProductQty != null) && !compoProductQty.isNaN() && !compoProductQty.isInfinite() && (compoProductQty != 0d)) {
								BigDecimal plmWeight = new BigDecimal(packMateriDataItem.getPmlWeight(), MathContext.DECIMAL64)
										.multiply(new BigDecimal(qtyUsed)).divide(new BigDecimal(compoProductQty), MathContext.DECIMAL64);
								if (toUpdate.containsKey(packMateriDataItem.getNodeRef())) {
									BigDecimal newPlmWeight = toUpdate.get(packMateriDataItem.getPmlMaterial()).add(plmWeight);
									toUpdate.put(packMateriDataItem.getPmlMaterial(), newPlmWeight);
								} else {
									toUpdate.put(packMateriDataItem.getPmlMaterial(), plmWeight);
								}
							} else {
								logger.error("QtyUsed/CompoProductQty is NaN or 0 or infinite:" + qtyUsed + " " + compoProductQty + " for "
										+ compoList.getProduct());
							}
						}
					}
				}
			}
		}
		return toUpdate;
	}

	private void calculateTareByMaterialItem(PackagingListDataItem dataItem, Map<NodeRef, BigDecimal> toUpdate, int kitQty) {

		if (nodeService.getType(dataItem.getProduct()).equals(PLMModel.TYPE_PACKAGINGKIT)) {
			if ((ProductUnit.PP.equals(dataItem.getPackagingListUnit()) || ProductUnit.P.equals(dataItem.getPackagingListUnit()))
					&& (dataItem.getQty() != null)) {
				kitQty *= dataItem.getQty().intValue();
			}
			ProductData packagingKitData = alfrescoRepository.findOne(dataItem.getProduct());
			if (packagingKitData.hasPackagingListEl()) {
				for (PackagingListDataItem p : packagingKitData.getPackagingList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
					calculateTareByMaterialItem(p, toUpdate, kitQty);
				}
			}

		} else {
			calculateTareByMaterial(dataItem, toUpdate, kitQty);
		}
	}

	private void calculateTareByMaterial(PackagingListDataItem dataItem, Map<NodeRef, BigDecimal> toUpdate, int kitQty) {

		// Keep materials of primary packaging (without packaging kit)
		if ((dataItem.getProduct() != null) && (dataItem.getPkgLevel() != null) && PackagingLevel.Primary.equals(dataItem.getPkgLevel())) {

			BigDecimal tare = FormulationHelper.getTareInKg(dataItem, alfrescoRepository.findOne(dataItem.getProduct()))
					.multiply(new BigDecimal(kitQty));

			ProductUnit productUnit = alfrescoRepository.findOne(dataItem.getProduct()).getUnit();
			// Convert tare in Kg
			if (productUnit != null) {
				tare = tare.divide(new BigDecimal(productUnit.getUnitFactor()), 10, BigDecimal.ROUND_HALF_UP);
			}

			PackagingMaterialData packagingProduct = (PackagingMaterialData) alfrescoRepository.findOne(dataItem.getProduct());
			if ((packagingProduct.getPackagingMaterials() != null) && (packagingProduct.getPackagingMaterials().size() > 0)) {
				BigDecimal tareByMaterial = new BigDecimal(1000 / packagingProduct.getPackagingMaterials().size()).multiply(tare);
				for (NodeRef packagingMaterial : packagingProduct.getPackagingMaterials()) {
					if (toUpdate.containsKey(packagingMaterial)) {
						BigDecimal newTare = toUpdate.get(packagingMaterial).add(tareByMaterial);
						toUpdate.put(packagingMaterial, newTare);
					} else {
						toUpdate.put(packagingMaterial, tareByMaterial);
					}
				}
			}
		}
	}
}
