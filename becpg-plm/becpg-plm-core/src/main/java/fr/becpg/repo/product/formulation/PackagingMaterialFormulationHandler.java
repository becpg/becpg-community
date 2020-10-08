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

/**
 * <p>
 * PackagingMaterialFormulationHandler class.
 * </p>
 *
 * @author evelyne
 * @version $Id: $Id
 */
public class PackagingMaterialFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(PackagingMaterialFormulationHandler.class);

	private NodeService nodeService;

	private AlfrescoRepository<ProductData> alfrescoRepository;

	/**
	 * <p>
	 * Setter for the field <code>nodeService</code>.
	 * </p>
	 *
	 * @param nodeService
	 *            a {@link org.alfresco.service.cmr.repository.NodeService}
	 *            object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>
	 * Setter for the field <code>alfrescoRepository</code>.
	 * </p>
	 *
	 * @param alfrescoRepository
	 *            a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) || (formulatedProduct instanceof ProductSpecificationData)) {
			return true;
		}

		if (alfrescoRepository.hasDataList(formulatedProduct.getNodeRef(), PackModel.PACK_MATERIAL_LIST_TYPE)) {

			// no compo, no packagingList => no formulation
			if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL)
					|| (!formulatedProduct.hasCompoListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))
							&& !formulatedProduct
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
		if ((formulatedProduct.getDropPackagingOfComponents() == null) || !formulatedProduct.getDropPackagingOfComponents()) {

			for (CompoListDataItem compoList : formulatedProduct
					.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
				if (compoList.getProduct() != null) {

					ProductData compoProduct = alfrescoRepository.findOne(compoList.getProduct());
					if (compoProduct.getPackMaterialList() != null) {

						Double qtyUsed = compoList.getQtySubFormula();
						ProductUnit compoListUnit = compoList.getCompoListUnit();

						if ((qtyUsed != null) && (qtyUsed > 0)) {

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
								qtyUsed = FormulationHelper.getQtyInKg(compoList);
							}

							for (PackMaterialListDataItem packMateriDataItem : compoProduct.getPackMaterialList()) {
								if (packMateriDataItem.getPmlWeight() != null) {
									if ((compoProductQty != null) && !compoProductQty.isNaN() && !compoProductQty.isInfinite()
											&& (compoProductQty != 0d)) {
										BigDecimal plmWeight = BigDecimal.valueOf(packMateriDataItem.getPmlWeight())
												.multiply(BigDecimal.valueOf(qtyUsed)).divide(BigDecimal.valueOf(compoProductQty), MathContext.DECIMAL64);
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
			}
		}
		return toUpdate;
	}

	private void calculateTareByMaterialItem(PackagingListDataItem dataItem, Map<NodeRef, BigDecimal> toUpdate, double subQty) {

		if (nodeService.getType(dataItem.getProduct()).equals(PLMModel.TYPE_PACKAGINGKIT)) {
			if (dataItem.getQty()!=null &&  ProductUnit.P.equals(dataItem.getPackagingListUnit()) &&  PackagingLevel.Primary.equals(dataItem.getPkgLevel()) ) {
				subQty *= dataItem.getQty();
			}
			ProductData packagingKitData = alfrescoRepository.findOne(dataItem.getProduct());
			if (packagingKitData.hasPackagingListEl()) {
				for (PackagingListDataItem p : packagingKitData.getPackagingList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
					calculateTareByMaterialItem(p, toUpdate, subQty);
				}
			}

		} else {
			calculateTareByMaterial(dataItem, toUpdate, subQty);
		}
	}

	private void calculateTareByMaterial(PackagingListDataItem dataItem, Map<NodeRef, BigDecimal> toUpdate, double subQty) {

		// Keep materials of primary packaging (without packaging kit)
		if ((dataItem.getProduct() != null) &&  PackagingLevel.Primary.equals(dataItem.getPkgLevel())) {

			PackagingMaterialData packagingProduct = (PackagingMaterialData) alfrescoRepository.findOne(dataItem.getProduct());

			BigDecimal tare = FormulationHelper.getTareInKg(dataItem, packagingProduct).multiply(BigDecimal.valueOf(subQty*1000d));

			if (alfrescoRepository.hasDataList(packagingProduct, PackModel.PACK_MATERIAL_LIST_TYPE) 
					&& packagingProduct.getPackMaterialList() != null ) {

				for (PackMaterialListDataItem packMateriDataItem : packagingProduct.getPackMaterialList()) {
					if (packMateriDataItem.getPmlWeight() != null) {
						
						BigDecimal plmWeight = BigDecimal.valueOf(packMateriDataItem.getPmlWeight())
								.multiply(tare);
						
						
						BigDecimal productTare = FormulationHelper.getTareInKg(packagingProduct);
						if(productTare!=null) {
							 plmWeight = plmWeight.divide(productTare.multiply(BigDecimal.valueOf(1000d)));
						}
							
						if (toUpdate.containsKey(packMateriDataItem.getPmlMaterial())) {
							BigDecimal newPlmWeight = toUpdate.get(packMateriDataItem.getPmlMaterial()).add(plmWeight);
							toUpdate.put(packMateriDataItem.getPmlMaterial(), newPlmWeight);
						} else {
							toUpdate.put(packMateriDataItem.getPmlMaterial(), plmWeight);
						}
					}
				}

			} else if ((packagingProduct.getPackagingMaterials() != null) && (packagingProduct.getPackagingMaterials().size() > 0)) {


				BigDecimal tareByMaterial = tare.divide(BigDecimal.valueOf(packagingProduct.getPackagingMaterials().size()));
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
