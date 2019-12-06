package fr.becpg.repo.product.formulation;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedList;
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

@Service
public class PackagingHelper implements InitializingBean {

	private static final Log logger = LogFactory.getLog(PackagingHelper.class);

	private static PackagingHelper INSTANCE = null;

	@Override
	public void afterPropertiesSet() {
		INSTANCE = this;

	}

	private PackagingHelper() {
		// Make creation private
	}

	@Autowired
	private NodeService nodeService;

	@Autowired
	protected AlfrescoRepository<ProductData> alfrescoRepository;

	public VariantPackagingData getDefaultVariantPackagingData(ProductData productData) {
		PackagingData packagingData = getPackagingData(productData);
		return packagingData.getVariants().get(getDefaultVariant(productData));
	}

	public PackagingData getPackagingData(ProductData productData) {
		PackagingData packagingData = new PackagingData(productData.getVariants());
		if (productData.hasPackagingListEl()) {
			for (PackagingListDataItem dataItem : productData.getPackagingList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
				loadPackagingItem(dataItem, packagingData, dataItem.getVariants());
			}
		}

		for (VariantPackagingData variantPackagingData : packagingData.getVariants().values()) {
			if (productData.getAspects().contains(GS1Model.ASPECT_MEASURES_ASPECT)) {
				if (variantPackagingData.isManualPrimary()) {

					variantPackagingData.setWidth((Float) nodeService.getProperty(productData.getNodeRef(), GS1Model.PROP_WIDTH));
					variantPackagingData.setHeight((Float) nodeService.getProperty(productData.getNodeRef(), GS1Model.PROP_HEIGHT));
					variantPackagingData.setDepth((Float) nodeService.getProperty(productData.getNodeRef(), GS1Model.PROP_DEPTH));
				}
				if (variantPackagingData.isManualSecondary()) {
					variantPackagingData.setSecondaryWidth((Float) nodeService.getProperty(productData.getNodeRef(), GS1Model.PROP_SECONDARY_WIDTH));
					variantPackagingData
							.setSecondaryHeight((Float) nodeService.getProperty(productData.getNodeRef(), GS1Model.PROP_SECONDARY_HEIGHT));
					variantPackagingData.setSecondaryDepth((Float) nodeService.getProperty(productData.getNodeRef(), GS1Model.PROP_SECONDARY_DEPTH));
				}

			}
			if (variantPackagingData.isManualTertiary()) {

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
				if (variantData.getIsDefaultVariant()) {
					defaultVariantNodeRef = variantData.getNodeRef();
				}
			}
		}
		return defaultVariantNodeRef;
	}

	private void loadPackagingItem(PackagingListDataItem dataItem, PackagingData packagingData,  List<NodeRef> currentVariants ) {

		if (nodeService.getType(dataItem.getProduct()).equals(PLMModel.TYPE_PACKAGINGKIT)) {
			loadPackagingKit(dataItem, packagingData, currentVariants);
		} else {
			loadPackaging(dataItem, packagingData, currentVariants);
		}
	}

	private void loadPackaging(PackagingListDataItem dataItem, PackagingData packagingData, List<NodeRef> currentVariants) {
		QName nodeType = nodeService.getType(dataItem.getProduct());

		// Sum tare (don't take in account packagingKit)
		if ((dataItem.getPkgLevel() != null) && !PLMModel.TYPE_PACKAGINGKIT.equals(nodeType) && (dataItem.getProduct() != null)) {
			for (VariantPackagingData variantPackagingData : packagingData.getVariantPackagingData(currentVariants)) {
				BigDecimal tare = FormulationHelper.getTareInKg(dataItem, alfrescoRepository.findOne(dataItem.getProduct()));

				if (PackagingLevel.Primary.equals(dataItem.getPkgLevel())) {
					variantPackagingData.addTarePrimary(tare);

					if (Boolean.TRUE.equals(dataItem.getIsMaster())) {

						variantPackagingData.setManualPrimary(false);
						variantPackagingData.setWidth(parseFloat((Double) nodeService.getProperty(dataItem.getProduct(), PackModel.PROP_WIDTH)));
						variantPackagingData.setHeight(parseFloat((Double) nodeService.getProperty(dataItem.getProduct(), PackModel.PROP_HEIGHT)));
						variantPackagingData.setDepth(parseFloat((Double) nodeService.getProperty(dataItem.getProduct(), PackModel.PROP_LENGTH)));

					}

				} else if (PackagingLevel.Secondary.equals(dataItem.getPkgLevel())) {
					variantPackagingData.addTareSecondary(tare);

					if (Boolean.TRUE.equals(dataItem.getIsMaster())) {

						variantPackagingData.setManualSecondary(false);
						variantPackagingData
								.setSecondaryWidth(parseFloat((Double) nodeService.getProperty(dataItem.getProduct(), PackModel.PROP_WIDTH)));
						variantPackagingData
								.setSecondaryHeight(parseFloat((Double) nodeService.getProperty(dataItem.getProduct(), PackModel.PROP_HEIGHT)));
						variantPackagingData
								.setSecondaryDepth(parseFloat((Double) nodeService.getProperty(dataItem.getProduct(), PackModel.PROP_LENGTH)));

					}

				} else if (PackagingLevel.Tertiary.equals(dataItem.getPkgLevel())) {
					variantPackagingData.addTareTertiary(tare);
				}
			}
		}

		if (nodeService.hasAspect(dataItem.getProduct(), PackModel.ASPECT_PALLET) && PackagingLevel.Secondary.equals(dataItem.getPkgLevel())
				&& ProductUnit.PP.equals(dataItem.getPackagingListUnit()) && PLMModel.TYPE_PACKAGINGKIT.equals(nodeType)) {
			logger.debug("load pallet aspect ");
			for (VariantPackagingData variantPackagingData : packagingData.getVariantPackagingData(currentVariants)) {
				variantPackagingData.setManualTertiary(false);

				// product per box and boxes per pallet
				if (dataItem.getQty() != null) {
					logger.debug("setProductPerBoxes " + dataItem.getQty().intValue());
					variantPackagingData.setProductPerBoxes(dataItem.getQty().intValue());
				}

				extractPalletInformations(dataItem.getProduct(), variantPackagingData);

			}

		}
	}

	private Float parseFloat(Double value) {
		if (value != null) {
			return value.floatValue();
		}
		return null;
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

		variantPackagingData.setTertiaryWidth((Float) nodeService.getProperty(product, GS1Model.PROP_TERTIARY_WIDTH));
		variantPackagingData.setTertiaryDepth((Float) nodeService.getProperty(product, GS1Model.PROP_TERTIARY_DEPTH));

	}

	// manage 2 level depth
	private void loadPackagingKit(PackagingListDataItem dataItem, PackagingData packagingData,  List<NodeRef> currentVariants) {

		loadPackaging(dataItem, packagingData, currentVariants);
		ProductData packagingKitData = alfrescoRepository.findOne(dataItem.getProduct());
		if (packagingKitData.hasPackagingListEl()) {
			for (PackagingListDataItem p : packagingKitData.getPackagingList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
				loadPackagingItem(p,packagingData, currentVariants);
			}
		}
	}

	public static List<PackagingListDataItem> flatPackagingList(ProductData productData) {
		return INSTANCE.flatPackagingList(productData, 1d);
	}

	private List<PackagingListDataItem> flatPackagingList(ProductData productData, Double subQty) {
		List<PackagingListDataItem> ret = new LinkedList<>();
		for (PackagingListDataItem dataItem : productData.getPackagingList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
			if (dataItem.getProduct() != null) {

				QName nodeType = nodeService.getType(dataItem.getProduct());

				if (!PLMModel.TYPE_PACKAGINGKIT.equals(nodeType)) {

					PackagingListDataItem item = dataItem.clone();
					item.setQty(item.getQty() * subQty);

					ret.add(item);
				} else {
					ProductData subProduct = alfrescoRepository.findOne(dataItem.getProduct());
					
					if(!(nodeService.hasAspect(dataItem.getProduct(), PackModel.ASPECT_PALLET) && PackagingLevel.Secondary.equals(dataItem.getPkgLevel())
								&& ProductUnit.PP.equals(dataItem.getPackagingListUnit()))	) {
						subQty *= FormulationHelper.getQty(dataItem, subProduct);
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
