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
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.packaging.VariantPackagingData;
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
	public boolean process(ProductData formulatedProduct) {

		if (!(formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) || (formulatedProduct instanceof ProductSpecificationData))) {

			if (alfrescoRepository.hasDataList(formulatedProduct.getNodeRef(), PackModel.PACK_MATERIAL_LIST_TYPE)) {

				// no compo, no packagingList => no formulation
				if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) || (!formulatedProduct
						.hasCompoListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))
						&& !formulatedProduct
								.hasPackagingListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>())))) {

					calculateMaterialWeight(formulatedProduct);

					return true;
				}
				// CompoList
				Map<Pair<PackagingLevel, NodeRef>, Pair<BigDecimal, BigDecimal>> toUpdate = calculateMaterialOfComposition(formulatedProduct);

				// PackagingList
				for (PackagingListDataItem packagingItem : formulatedProduct.getPackagingList()) {
					calculateTareByMaterialItem(packagingItem, toUpdate, 1);
				}

				// Create/Update Packaging Material List
				if (formulatedProduct.getPackMaterialList() == null) {
					formulatedProduct.setPackMaterialList(new LinkedList<>());
				}

				List<PackMaterialListDataItem> toRemove = new ArrayList<>();
				for (PackMaterialListDataItem packmaterial : formulatedProduct.getPackMaterialList()) {
					Pair<PackagingLevel, NodeRef> key = new Pair<>(packmaterial.getPkgLevel(), packmaterial.getPmlMaterial());
					if (!toUpdate.containsKey(key)) {
						toRemove.add(packmaterial);
					} else {
						if (toUpdate.get(key).getFirst().doubleValue() != 0d) {
							packmaterial.setPmlWeight(toUpdate.get(key).getFirst().doubleValue());
							packmaterial.setPmlPerc(calculatePerc(formulatedProduct, key.getFirst(), toUpdate.get(key).getFirst()));
							packmaterial.setPmlRecycledPercentage(toUpdate.get(key).getSecond()
									.divide(toUpdate.get(key).getFirst(), MathContext.DECIMAL64).multiply(BigDecimal.valueOf(100d)).doubleValue());
							toUpdate.remove(key);
						}
					}
				}

				for (Map.Entry<Pair<PackagingLevel, NodeRef>, Pair<BigDecimal, BigDecimal>> entry : toUpdate.entrySet()) {
					if (entry.getValue().getFirst().doubleValue() != 0d) {
						formulatedProduct.getPackMaterialList().add(new PackMaterialListDataItem(entry.getKey().getSecond(),
								entry.getValue().getFirst().doubleValue(),
								calculatePerc(formulatedProduct, entry.getKey().getFirst(), entry.getValue().getFirst()), entry.getValue().getSecond()
										.divide(entry.getValue().getFirst(), MathContext.DECIMAL64).multiply(BigDecimal.valueOf(100d)).doubleValue(),
								entry.getKey().getFirst()));
					}
				}

				formulatedProduct.getPackMaterialList().removeAll(toRemove);

				// add detailable aspect
				for (PackMaterialListDataItem packMaterialListDataItem : formulatedProduct.getPackMaterialList()) {
					if (!packMaterialListDataItem.getAspects().contains(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM)) {
						packMaterialListDataItem.getAspects().add(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM);
					}

				}

			}
		}
		return true;
	}

	private Double calculatePerc(ProductData formulatedProduct, PackagingLevel packLevel, BigDecimal weight) {
		BigDecimal tare = null;
		VariantPackagingData variantPackagingData = formulatedProduct.getDefaultVariantPackagingData();
		if (PackagingLevel.Secondary.equals(packLevel)) {
			tare = variantPackagingData.getTareSecondary();
		} else if (PackagingLevel.Tertiary.equals(packLevel)) {
			tare = variantPackagingData.getTareTertiary();
		} else {
			tare = FormulationHelper.getTareInKg(formulatedProduct);
		}

		if (tare != null && tare.doubleValue() != 0d && weight != null) {
			return weight.divide(tare, MathContext.DECIMAL64).multiply(BigDecimal.valueOf(0.1d),
					MathContext.DECIMAL64).doubleValue();
		}
		return null;
	}

	private void calculateMaterialWeight(ProductData formulatedProduct) {
		BigDecimal tare = FormulationHelper.getTareInKg(formulatedProduct);

		if (tare != null && tare.doubleValue() != 0d) {

			for (PackMaterialListDataItem packMateriDataItem : formulatedProduct.getPackMaterialList()) {
				if (packMateriDataItem.getPmlPerc() != null) {

					BigDecimal plmWeight = tare.multiply(BigDecimal.valueOf(packMateriDataItem.getPmlPerc())).multiply(BigDecimal.valueOf(10d),
							MathContext.DECIMAL64);
					packMateriDataItem.setPmlWeight(plmWeight.doubleValue());
				}
			}

		}

	}

	private Map<Pair<PackagingLevel, NodeRef>, Pair<BigDecimal, BigDecimal>> calculateMaterialOfComposition(ProductData formulatedProduct) {

		Map<Pair<PackagingLevel, NodeRef>, Pair<BigDecimal, BigDecimal>> toUpdate = new HashMap<>();
		if (!Boolean.TRUE.equals(formulatedProduct.getDropPackagingOfComponents())) {

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
												.multiply(BigDecimal.valueOf(qtyUsed))
												.divide(BigDecimal.valueOf(compoProductQty), MathContext.DECIMAL64);
										BigDecimal pmlRecycledPercentage = BigDecimal.valueOf(
												packMateriDataItem.getPmlRecycledPercentage() != null ? packMateriDataItem.getPmlRecycledPercentage()
														: 0d)
												.multiply(plmWeight).divide(BigDecimal.valueOf(100d), MathContext.DECIMAL64);
										PackagingLevel pkgLevel = packMateriDataItem.getPkgLevel();
										
										if (pkgLevel == null && compoProduct.isRawMaterial()) {
											pkgLevel = PackagingLevel.Primary;
										}
										
										Pair<PackagingLevel, NodeRef> key = new Pair<>(pkgLevel,
												packMateriDataItem.getPmlMaterial());

										if (toUpdate.containsKey(key)) {
											BigDecimal newPlmWeight = toUpdate.get(key).getFirst().add(plmWeight);
											BigDecimal newPmlRecycledPercentage = toUpdate.get(key).getSecond().add(pmlRecycledPercentage);
											toUpdate.put(key, new Pair<>(newPlmWeight, newPmlRecycledPercentage));
										} else {
											toUpdate.put(key, new Pair<>(plmWeight, pmlRecycledPercentage));
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

	private void calculateTareByMaterialItem(PackagingListDataItem dataItem,
			Map<Pair<PackagingLevel, NodeRef>, Pair<BigDecimal, BigDecimal>> toUpdate, double subQty) {

		if (nodeService.getType(dataItem.getProduct()).equals(PLMModel.TYPE_PACKAGINGKIT)) {
			if ((dataItem.getQty() != null) && ProductUnit.P.equals(dataItem.getPackagingListUnit())) {
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

	private void calculateTareByMaterial(PackagingListDataItem dataItem, Map<Pair<PackagingLevel, NodeRef>, Pair<BigDecimal, BigDecimal>> toUpdate,
			double subQty) {

		// Keep materials of primary packaging (without packaging kit)
		if ((dataItem.getProduct() != null)) {

			PackagingMaterialData packagingProduct = (PackagingMaterialData) alfrescoRepository.findOne(dataItem.getProduct());

			BigDecimal tare = FormulationHelper.getTareInKg(dataItem, packagingProduct).multiply(BigDecimal.valueOf(subQty * 1000d));

			if (alfrescoRepository.hasDataList(packagingProduct, PackModel.PACK_MATERIAL_LIST_TYPE)
					&& (packagingProduct.getPackMaterialList() != null)) {

				for (PackMaterialListDataItem packMateriDataItem : packagingProduct.getPackMaterialList()) {
					if (packMateriDataItem.getPmlWeight() != null) {

						BigDecimal plmWeight = BigDecimal.valueOf(packMateriDataItem.getPmlWeight()).multiply(tare);

						BigDecimal productTare = FormulationHelper.getTareInKg(packagingProduct);
						if (productTare != null && productTare.doubleValue() != 0d) {
							plmWeight = plmWeight.divide(productTare.multiply(BigDecimal.valueOf(1000d)), MathContext.DECIMAL64);
						}
						BigDecimal pmlRecycledPercentage = BigDecimal
								.valueOf(packMateriDataItem.getPmlRecycledPercentage() != null ? packMateriDataItem.getPmlRecycledPercentage() : 0d)
								.multiply(plmWeight).divide(BigDecimal.valueOf(100d), MathContext.DECIMAL64);

						Pair<PackagingLevel, NodeRef> key = new Pair<>(dataItem.getPkgLevel(), packMateriDataItem.getPmlMaterial());

						if (toUpdate.containsKey(key)) {
							BigDecimal newPlmWeight = toUpdate.get(key).getFirst().add(plmWeight);
							BigDecimal newPmlRecycledPercentage = toUpdate.get(key).getSecond().add(pmlRecycledPercentage);
							toUpdate.put(key, new Pair<>(newPlmWeight, newPmlRecycledPercentage));
						} else {
							toUpdate.put(key, new Pair<>(plmWeight, pmlRecycledPercentage));
						}
					}
				}

			} else if ((packagingProduct.getPackagingMaterials() != null) && (!packagingProduct.getPackagingMaterials().isEmpty())) {

				BigDecimal tareByMaterial = tare.divide(BigDecimal.valueOf(packagingProduct.getPackagingMaterials().size()), MathContext.DECIMAL64);
				for (NodeRef packagingMaterial : packagingProduct.getPackagingMaterials()) {

					Pair<PackagingLevel, NodeRef> key = new Pair<>(dataItem.getPkgLevel(), packagingMaterial);

					if (toUpdate.containsKey(key)) {
						BigDecimal newPlmWeight = toUpdate.get(key).getFirst().add(tareByMaterial);
						BigDecimal newPmlRecycledPercentage = toUpdate.get(key).getSecond().add(BigDecimal.valueOf(0d));
						toUpdate.put(key, new Pair<>(newPlmWeight, newPmlRecycledPercentage));
					} else {
						toUpdate.put(key, new Pair<>(tareByMaterial, BigDecimal.valueOf(0d)));
					}
				}
			}
		}
	}
}
