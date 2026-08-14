package fr.becpg.repo.product.formulation;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
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

/**
 * <p>
 * PackagingMaterialFormulationHandler class.
 * </p>
 *
 * @author evelyne
 * @version $Id: $Id
 */
public class PackagingMaterialFormulationHandler extends FormulationBaseHandler<ProductData> {

	/** Constant <code>logger</code> */
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
				if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL)
						|| (!formulatedProduct.hasCompoListEl(FormulationFilters.EFFECTIVE_VARIANT_COMPO)
								&& !formulatedProduct.hasPackagingListEl(FormulationFilters.EFFECTIVE_VARIANT_PACKAGING))) {

					calculateMaterialWeight(formulatedProduct);

					return true;
				}
				// CompoList
				Map<Pair<PackagingLevel, NodeRef>, Pair<BigDecimal, BigDecimal>> toUpdate = calculateMaterialOfComposition(formulatedProduct);

				// PackagingList
				if (formulatedProduct.getPackagingList(FormulationFilters.EFFECTIVE_VARIANT_PACKAGING) != null) {
					for (PackagingListDataItem packagingItem : formulatedProduct.getPackagingList(FormulationFilters.EFFECTIVE_VARIANT_PACKAGING)) {
						calculateTareByMaterialItem(packagingItem, toUpdate, 1);
					}
				}

				// Create/Update Packaging Material List
				if (formulatedProduct.getPackMaterialList() == null) {
					formulatedProduct.setPackMaterialList(new ArrayList<>());
				}

				List<PackMaterialListDataItem> toRemove = new ArrayList<>();
				for (PackMaterialListDataItem packmaterial : formulatedProduct.getPackMaterialList()) {
					if (Boolean.TRUE.equals(packmaterial.getIsManual())) {
						toUpdate.remove(new Pair<>(packmaterial.getPkgLevel(), packmaterial.getPmlMaterial()));
						continue;
					}
					Pair<PackagingLevel, NodeRef> key = new Pair<>(packmaterial.getPkgLevel(), packmaterial.getPmlMaterial());
					if (!toUpdate.containsKey(key) || toUpdate.get(key).getFirst().doubleValue() == 0d) {
						toRemove.add(packmaterial);
					} else {
						packmaterial.setPmlWeight(toUpdate.get(key).getFirst().doubleValue());
						packmaterial.setPmlPerc(calculatePerc(formulatedProduct, key.getFirst(), toUpdate.get(key).getFirst()));
						packmaterial.setPmlRecycledPercentage(toUpdate.get(key).getSecond()
								.divide(toUpdate.get(key).getFirst(), MathContext.DECIMAL64).multiply(BigDecimal.valueOf(100d)).doubleValue());
						toUpdate.remove(key);
					}
				}

				for (Map.Entry<Pair<PackagingLevel, NodeRef>, Pair<BigDecimal, BigDecimal>> entry : toUpdate.entrySet()) {
					if (entry.getValue().getFirst().doubleValue() != 0d) {

						formulatedProduct.getPackMaterialList()
								.add(PackMaterialListDataItem.build().withMaterial(entry.getKey().getSecond())
										.withWeight(entry.getValue().getFirst().doubleValue())
										.withPerc(calculatePerc(formulatedProduct, entry.getKey().getFirst(), entry.getValue().getFirst()))
										.withRecycledPerc(entry.getValue().getSecond().divide(entry.getValue().getFirst(), MathContext.DECIMAL64)
												.multiply(BigDecimal.valueOf(100d)).doubleValue())
										.withPkgLevel(entry.getKey().getFirst()));
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

	/**
	 * <p>calculatePerc.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param packLevel a {@link fr.becpg.repo.product.data.constraints.PackagingLevel} object
	 * @param weight a {@link java.math.BigDecimal} object
	 * @return a {@link java.lang.Double} object
	 */
	private Double calculatePerc(ProductData formulatedProduct, PackagingLevel packLevel, BigDecimal weight) {
		BigDecimal tare = null;
		VariantPackagingData variantPackagingData = formulatedProduct.getDefaultVariantPackagingData();
		if (PackagingLevel.Secondary.equals(packLevel)) {
			tare = variantPackagingData.getTareSecondary();
		} else if (PackagingLevel.Tertiary.equals(packLevel)) {
			tare = variantPackagingData.getTareTertiary();
		} else if (PackagingLevel.Inner.equals(packLevel)) {
			tare = variantPackagingData.getTareInner();
		} else {
			tare = FormulationHelper.getTareInKg(formulatedProduct);
		}

		if (tare != null && tare.doubleValue() != 0d && weight != null) {
			return weight.divide(tare, MathContext.DECIMAL64).multiply(BigDecimal.valueOf(0.1d), MathContext.DECIMAL64).doubleValue();
		}
		return null;
	}

	/**
	 * <p>calculateMaterialWeight.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 */
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

	/**
	 * <p>calculateMaterialOfComposition.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @return a {@link java.util.Map} object
	 */
	private Map<Pair<PackagingLevel, NodeRef>, Pair<BigDecimal, BigDecimal>> calculateMaterialOfComposition(ProductData formulatedProduct) {

		Map<Pair<PackagingLevel, NodeRef>, Pair<BigDecimal, BigDecimal>> toUpdate = new HashMap<>();
		if (!Boolean.TRUE.equals(formulatedProduct.getDropPackagingOfComponents())) {

			if (formulatedProduct.getCompoList(FormulationFilters.EFFECTIVE_VARIANT_COMPO) != null) {
				for (CompoListDataItem compoList : formulatedProduct.getCompoList(FormulationFilters.EFFECTIVE_VARIANT_COMPO)) {
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

								if (compoListUnit != null && compoListUnit.isP()) {
									if ((compoProduct.getUnit() != null) && !compoProduct.getUnit().isP()) {
										compoProductQty = 1d;
									}

								} else if (compoListUnit != null && (compoListUnit.isWeight() || compoListUnit.isVolume())) {
									compoProductQty = FormulationHelper.getNetWeight(compoProduct, 1d);
									qtyUsed = FormulationHelper.getQtyInKg(compoList);
								}

								for (PackMaterialListDataItem packMateriDataItem : compoProduct.getPackMaterialList()) {
									if (packMateriDataItem.getPmlWeight() != null) {
										if ((compoProductQty != null) && !compoProductQty.isNaN() && !compoProductQty.isInfinite()
												&& (compoProductQty != 0d)) {

											BigDecimal plmWeight = BigDecimal.valueOf(packMateriDataItem.getPmlWeight())
													.multiply(BigDecimal.valueOf(qtyUsed))
													.divide(BigDecimal.valueOf(compoProductQty), MathContext.DECIMAL64);
											BigDecimal pmlRecycledPercentage = BigDecimal
													.valueOf(packMateriDataItem.getPmlRecycledPercentage() != null
															? packMateriDataItem.getPmlRecycledPercentage()
															: 0d)
													.multiply(plmWeight).divide(BigDecimal.valueOf(100d), MathContext.DECIMAL64);
											PackagingLevel pkgLevel = packMateriDataItem.getPkgLevel();

											if (pkgLevel == null && compoProduct.isRawMaterial()) {
												pkgLevel = PackagingLevel.Primary;
											}

											Pair<PackagingLevel, NodeRef> key = new Pair<>(pkgLevel, packMateriDataItem.getPmlMaterial());

											if (toUpdate.containsKey(key)) {
												BigDecimal newPlmWeight = toUpdate.get(key).getFirst().add(plmWeight);
												BigDecimal newPmlRecycledPercentage = toUpdate.get(key).getSecond().add(pmlRecycledPercentage);
												toUpdate.put(key, new Pair<>(newPlmWeight, newPmlRecycledPercentage));
											} else {
												toUpdate.put(key, new Pair<>(plmWeight, pmlRecycledPercentage));
											}
										} else {
											logger.error("QtyUsed/CompoProductQty is NaN or 0 or infinite:" + qtyUsed + " " + compoProductQty
													+ " for " + compoList.getProduct());
										}
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

	/**
	 * <p>calculateTareByMaterialItem.</p>
	 * <p>
	 * Recycled packaging is skipped: such a packaging is not destroyed by the product, so it
	 * contributes neither to the costs (already handled by the simple list handlers) nor to the
	 * packaging materials (see #35780). The check is done here so that it applies to the packaging
	 * list of the product as well as to the content of a packaging kit.
	 *
	 * @param dataItem a {@link fr.becpg.repo.product.data.productList.PackagingListDataItem} object
	 * @param toUpdate a {@link java.util.Map} object
	 * @param subQty a double
	 */
	private void calculateTareByMaterialItem(PackagingListDataItem dataItem,
			Map<Pair<PackagingLevel, NodeRef>, Pair<BigDecimal, BigDecimal>> toUpdate, double subQty) {

		if ((dataItem.getProduct() == null) || Boolean.TRUE.equals(dataItem.getIsRecycle())) {
			return;
		}

		if (nodeService.getType(dataItem.getProduct()).equals(PLMModel.TYPE_PACKAGINGKIT)) {
			if ((dataItem.getQty() != null) && ProductUnit.P.equals(dataItem.getPackagingListUnit())) {
				subQty *= dataItem.getQty();
			}
			ProductData packagingKitData = alfrescoRepository.findOne(dataItem.getProduct());
			if (packagingKitData.hasPackagingListEl()) {
				for (PackagingListDataItem p : packagingKitData.getPackagingList(FormulationFilters.EFFECTIVE_VARIANT_PACKAGING)) {
					calculateTareByMaterialItem(p, toUpdate, subQty);
				}
			}

		} else {
			calculateTareByMaterial(dataItem, toUpdate, subQty);
		}
	}

	/**
	 * <p>calculateTareByMaterial.</p>
	 *
	 * @param dataItem a {@link fr.becpg.repo.product.data.productList.PackagingListDataItem} object
	 * @param toUpdate a {@link java.util.Map} object
	 * @param subQty a double
	 */
	private void calculateTareByMaterial(PackagingListDataItem dataItem, Map<Pair<PackagingLevel, NodeRef>, Pair<BigDecimal, BigDecimal>> toUpdate,
			double subQty) {

		// Keep materials of primary packaging (without packaging kit)
		if ((dataItem.getProduct() != null)) {

			PackagingMaterialData packagingProduct = (PackagingMaterialData) alfrescoRepository.findOne(dataItem.getProduct());

			BigDecimal tare = FormulationHelper.getTareInKg(dataItem, packagingProduct).multiply(BigDecimal.valueOf(subQty * 1000d));

			if (hasPackMaterialList(packagingProduct)) {

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

	/**
	 * <p>Tells whether the packaging details its materials in a packMaterialList.</p>
	 *
	 * The entity template of a packaging material always creates the datalist, so its mere existence
	 * says nothing: only a filled list takes precedence over the <code>pack:pmMaterialRefs</code>
	 * associations.
	 *
	 * @param packagingProduct a {@link fr.becpg.repo.product.data.PackagingMaterialData} object
	 * @return true when the packaging has at least one packMaterialList line
	 */
	private boolean hasPackMaterialList(PackagingMaterialData packagingProduct) {
		return (packagingProduct.getPackMaterialList() != null) && !packagingProduct.getPackMaterialList().isEmpty();
	}
}
