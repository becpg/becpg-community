package fr.becpg.repo.product.formulation;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.GS1Model;
import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.packaging.PackagingData;
import fr.becpg.repo.product.data.packaging.VariantPackagingData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.variant.filters.VariantFilters;
import fr.becpg.repo.variant.model.VariantData;

/**
 * <p>PackagingHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class PackagingHelper implements InitializingBean {

	private static final Log logger = LogFactory.getLog(PackagingHelper.class);

	private static PackagingHelper instance = null;

	/** {@inheritDoc} */
	@Override
	public void afterPropertiesSet() {
		instance = this;
	}

	private PackagingHelper() {
		// Make creation private
	}

	@Autowired
	private NodeService nodeService;

	@Autowired
	protected AlfrescoRepository<ProductData> alfrescoRepository;

	/**
	 * <p>getDefaultVariantPackagingData.</p>
	 *
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @return a {@link fr.becpg.repo.product.data.packaging.VariantPackagingData} object.
	 */
	public VariantPackagingData getDefaultVariantPackagingData(ProductData productData) {
		PackagingData packagingData = getPackagingData(productData);
		return packagingData.getVariants().get(getDefaultVariant(productData));
	}

	/**
	 * <p>getPackagingData.</p>
	 *
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @return a {@link fr.becpg.repo.product.data.packaging.PackagingData} object.
	 */
	public PackagingData getPackagingData(ProductData productData) {
		PackagingData packagingData = new PackagingData(productData.getVariants());
		if (productData.hasPackagingListEl()) {
			for (PackagingListDataItem dataItem : productData.getPackagingList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
				if(dataItem.getProduct()!=null) {
					loadPackagingItem(dataItem, packagingData, dataItem.getVariants(), 1d);
				}
			}
		}

		for (VariantPackagingData variantPackagingData : packagingData.getVariants().values()) {
			if (productData.getAspects().contains(GS1Model.ASPECT_MEASURES_ASPECT)) {
				if (variantPackagingData.isManualPrimary()) {

					variantPackagingData.setWidth((Double) nodeService.getProperty(productData.getNodeRef(), GS1Model.PROP_WIDTH));
					variantPackagingData.setHeight((Double) nodeService.getProperty(productData.getNodeRef(), GS1Model.PROP_HEIGHT));
					variantPackagingData.setDepth((Double) nodeService.getProperty(productData.getNodeRef(), GS1Model.PROP_DEPTH));
				}
				if (variantPackagingData.isManualSecondary()) {
					variantPackagingData.setSecondaryWidth((Double) nodeService.getProperty(productData.getNodeRef(), GS1Model.PROP_SECONDARY_WIDTH));
					variantPackagingData
							.setSecondaryHeight((Double) nodeService.getProperty(productData.getNodeRef(), GS1Model.PROP_SECONDARY_HEIGHT));
					variantPackagingData.setSecondaryDepth((Double) nodeService.getProperty(productData.getNodeRef(), GS1Model.PROP_SECONDARY_DEPTH));
				}

			}
			if (variantPackagingData.isManualTertiary()) {
				variantPackagingData.setTertiaryWidth((Double) nodeService.getProperty(productData.getNodeRef(), GS1Model.PROP_TERTIARY_WIDTH));
				variantPackagingData.setTertiaryDepth((Double) nodeService.getProperty(productData.getNodeRef(), GS1Model.PROP_TERTIARY_DEPTH));
			}
			
			if(variantPackagingData.isManualPalletInformations()) {
				variantPackagingData
				.setProductPerBoxes((Integer) nodeService.getProperty(productData.getNodeRef(), PackModel.PROP_PALLET_PRODUCTS_PER_BOX));
		
				if (productData.getAspects().contains(PackModel.ASPECT_PALLET)) {
					extractPalletInformations(productData.getNodeRef(), variantPackagingData);
				}
			}
			
		}

		return packagingData;
	}

	private NodeRef getDefaultVariant(ProductData productData) {
		NodeRef defaultVariantNodeRef = null;
		if (productData.getVariants() != null) {
			for (VariantData variantData : productData.getVariants()) {
				if (Boolean.TRUE.equals(variantData.getIsDefaultVariant())) {
					defaultVariantNodeRef = variantData.getNodeRef();
				}
			}
		}
		return defaultVariantNodeRef;
	}

	private void loadPackagingItem(PackagingListDataItem dataItem, PackagingData packagingData,  List<NodeRef> currentVariants , double subQty) {

		if (nodeService.getType(dataItem.getProduct()).equals(PLMModel.TYPE_PACKAGINGKIT)) {
			loadPackagingKit(dataItem, packagingData, currentVariants, subQty);
		} else {
			loadPackaging(dataItem, packagingData, currentVariants, subQty);
		}
	}

	private void loadPackaging(PackagingListDataItem dataItem, PackagingData packagingData, List<NodeRef> currentVariants, double subQty) {
		QName nodeType = nodeService.getType(dataItem.getProduct());

		// Sum tare (don't take in account packagingKit)
		if ((dataItem.getPkgLevel() != null) && !PLMModel.TYPE_PACKAGINGKIT.equals(nodeType) && (dataItem.getProduct() != null)) {
			for (VariantPackagingData variantPackagingData : packagingData.getVariantPackagingData(currentVariants)) {
				BigDecimal tare = FormulationHelper.getTareInKg(dataItem, alfrescoRepository.findOne(dataItem.getProduct())).multiply( BigDecimal.valueOf(subQty));

				if (PackagingLevel.Primary.equals(dataItem.getPkgLevel())) {
					variantPackagingData.addTarePrimary(tare);

					if (Boolean.TRUE.equals(dataItem.getIsMaster())) {

						variantPackagingData.setManualPrimary(false);
						variantPackagingData.setWidth((Double) nodeService.getProperty(dataItem.getProduct(), PackModel.PROP_WIDTH));
						variantPackagingData.setHeight((Double) nodeService.getProperty(dataItem.getProduct(), PackModel.PROP_HEIGHT));
						variantPackagingData.setDepth((Double) nodeService.getProperty(dataItem.getProduct(), PackModel.PROP_LENGTH));
						variantPackagingData.setPackagingTypeCode((String) nodeService.getProperty(dataItem.getProduct(), GS1Model.PROP_PACKAGING_TYPE_CODE));
						variantPackagingData
								.setPackagingTermsAndConditionsCode((String) nodeService.getProperty(dataItem.getProduct(), GS1Model.PROP_PACKAGINGTERMSANSCONDITION_CODE));
						
					}

				} else if (PackagingLevel.Secondary.equals(dataItem.getPkgLevel())) {
					variantPackagingData.addTareSecondary(tare);

					if (Boolean.TRUE.equals(dataItem.getIsMaster())) {

						variantPackagingData.setManualSecondary(false);
						variantPackagingData
								.setSecondaryWidth((Double) nodeService.getProperty(dataItem.getProduct(), PackModel.PROP_WIDTH));
						variantPackagingData
								.setSecondaryHeight((Double) nodeService.getProperty(dataItem.getProduct(), PackModel.PROP_HEIGHT));
						variantPackagingData
								.setSecondaryDepth((Double) nodeService.getProperty(dataItem.getProduct(), PackModel.PROP_LENGTH));
						variantPackagingData.setSecondaryPackagingTypeCode((String) nodeService.getProperty(dataItem.getProduct(), GS1Model.PROP_PACKAGING_TYPE_CODE));

					}

				} else if (PackagingLevel.Tertiary.equals(dataItem.getPkgLevel())) {
					
					if (Boolean.TRUE.equals(dataItem.getIsMaster())) {
						variantPackagingData.setManualTertiary(false);
						variantPackagingData.setTertiaryWidth((Double) nodeService.getProperty(dataItem.getProduct(), PackModel.PROP_WIDTH));
						variantPackagingData.setTertiaryDepth((Double) nodeService.getProperty(dataItem.getProduct(), PackModel.PROP_LENGTH));
						variantPackagingData.setTertiaryPackagingTypeCode((String) nodeService.getProperty(dataItem.getProduct(), GS1Model.PROP_PACKAGING_TYPE_CODE));
					}
				
					variantPackagingData.addTareTertiary(tare);
				}
			}
		}

		if (nodeService.hasAspect(dataItem.getProduct(), PackModel.ASPECT_PALLET) && PackagingLevel.Secondary.equals(dataItem.getPkgLevel())
				&& ProductUnit.PP.equals(dataItem.getPackagingListUnit()) && PLMModel.TYPE_PACKAGINGKIT.equals(nodeType)) {
			logger.debug("load pallet aspect ");
			for (VariantPackagingData variantPackagingData : packagingData.getVariantPackagingData(currentVariants)) {
				variantPackagingData.setManualPalletInformations(false);

				// product per box and boxes per pallet
				if (dataItem.getQty() != null) {
					logger.debug("setProductPerBoxes " + dataItem.getQty().intValue());
					variantPackagingData.setProductPerBoxes(dataItem.getQty().intValue());
				}

				extractPalletInformations(dataItem.getProduct(), variantPackagingData);
				
				if (Boolean.TRUE.equals(dataItem.getIsMaster())) {
					variantPackagingData.setManualTertiary(false);
					variantPackagingData.setTertiaryWidth((Double) nodeService.getProperty(dataItem.getProduct(), GS1Model.PROP_TERTIARY_WIDTH));
					variantPackagingData.setTertiaryDepth((Double) nodeService.getProperty(dataItem.getProduct(), GS1Model.PROP_TERTIARY_DEPTH));
				}

			}

		}
	}


	private void extractPalletInformations(NodeRef product, VariantPackagingData variantPackagingData) {

		variantPackagingData.setPalletLayers((Integer) nodeService.getProperty(product, PackModel.PROP_PALLET_LAYERS));
		variantPackagingData.setBoxesPerPallet((Integer) nodeService.getProperty(product, PackModel.PROP_PALLET_BOXES_PER_PALLET));
		variantPackagingData.setPalletBoxesPerLastLayer((Integer) nodeService.getProperty(product, PackModel.PROP_PALLET_BOXES_PER_LAST_LAYER));
		variantPackagingData.setPalletStackingMaxWeight((Double) nodeService.getProperty(product, PackModel.PROP_PALLET_STACKING_MAX_WEIGHT));
		variantPackagingData.setPalletBoxesPerLayer((Integer) nodeService.getProperty(product, PackModel.PROP_PALLET_BOXES_PER_LAYER));
		variantPackagingData.setPalletHeight((Double) nodeService.getProperty(product, PackModel.PROP_PALLET_HEIGHT));
		variantPackagingData.setPalletNumberOnGround((Integer) nodeService.getProperty(product, PackModel.PROP_PALLET_NUMBER_ON_GROUND));
		variantPackagingData.setPalletTypeCode((String) nodeService.getProperty(product, GS1Model.PROP_PALLET_TYPE_CODE));
		variantPackagingData
				.setPlatformTermsAndConditionsCode((String) nodeService.getProperty(product, GS1Model.PROP_PLATFORMTERMSANSCONDITION_CODE));

	}

	// manage 2 level depth
	private void loadPackagingKit(PackagingListDataItem dataItem, PackagingData packagingData,  List<NodeRef> currentVariants, double subQty) {

		if (dataItem.getQty()!=null &&  ProductUnit.P.equals(dataItem.getPackagingListUnit()) ) {
			subQty *= dataItem.getQty();
		}
		
		loadPackaging(dataItem, packagingData, currentVariants, subQty);
		ProductData packagingKitData = alfrescoRepository.findOne(dataItem.getProduct());
		if (packagingKitData.hasPackagingListEl()) {
			for (PackagingListDataItem p : packagingKitData.getPackagingList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
				loadPackagingItem(p,packagingData, currentVariants, subQty);
			}
		}
	}

	/**
	 * <p>flatPackagingList.</p>
	 *
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @return a {@link java.util.List} object.
	 */
	public static List<PackagingListDataItem> flatPackagingList(ProductData productData) {
		return instance.flatPackagingList(productData, 1d);
	}

	private List<PackagingListDataItem> flatPackagingList(ProductData productData, Double subQty) {
		List<PackagingListDataItem> ret = new ArrayList<>();
		for (PackagingListDataItem dataItem : productData.getPackagingList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
			if (dataItem.getProduct() != null) {

				QName nodeType = nodeService.getType(dataItem.getProduct());

				if (!PLMModel.TYPE_PACKAGINGKIT.equals(nodeType)) {

					PackagingListDataItem item = dataItem.copy();
					item.setQty(item.getQty() * subQty);

					ret.add(item);
				} else {
					ProductData subProduct = alfrescoRepository.findOne(dataItem.getProduct());
					
					if(!(nodeService.hasAspect(dataItem.getProduct(), PackModel.ASPECT_PALLET) && PackagingLevel.Secondary.equals(dataItem.getPkgLevel())
								&& ProductUnit.PP.equals(dataItem.getPackagingListUnit()))	) {
						subQty *= FormulationHelper.getQty(dataItem);
					}
					
					
					ret.addAll(flatPackagingList(subProduct, subQty));
				}
			}

		}

		for (CompoListDataItem compoList : productData
				.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
			if (compoList.getProduct() != null) {
				QName nodeType = nodeService.getType(compoList.getProduct());

				if (!PLMModel.TYPE_RAWMATERIAL.equals(nodeType)) {

					ProductData subProduct = alfrescoRepository.findOne(compoList.getProduct());

					ProductUnit compoListUnit = compoList.getCompoListUnit();
					Double qty = compoList.getQtySubFormula();
	

					if ((subProduct != null) && (compoListUnit != null) && (qty != null)) {

						Double productQty = subProduct.getQty();
						if (productQty == null) {
							productQty = 1d;
						}

						if (compoListUnit.isP()) {
							if ((subProduct.getUnit() != null) && !subProduct.getUnit().isP()) {
								productQty = 1d;
							}

						} else if (compoListUnit.isWeight() || compoListUnit.isVolume()) {

							productQty = FormulationHelper.getNetQtyInLorKg(subProduct, 1d);
							qty = FormulationHelper.getQtyInKg(compoList);

						}

						if ((qty != null) && !qty.isNaN() && !qty.isInfinite() && (productQty != null) && !productQty.isNaN()
								&& !productQty.isInfinite() && (productQty != 0d)) {

							subQty *= qty / productQty;
						}

					}

					ret.addAll(flatPackagingList(subProduct, subQty));
				}
			}
		}

		return ret;

	}

}
