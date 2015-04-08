package fr.becpg.repo.product.formulation;

import java.math.BigDecimal;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.PackagingListUnit;
import fr.becpg.repo.product.data.packaging.PackagingData;
import fr.becpg.repo.product.data.packaging.VariantPackagingData;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.variant.model.VariantData;

@Service
public class PackagingHelper {

	private static Log logger = LogFactory.getLog(PackagingHelper.class);

	@Autowired
	private NodeService nodeService;

	@Autowired
	protected AlfrescoRepository<ProductData> alfrescoRepository;

	public VariantPackagingData getDefaultVariantPackagingData(ProductData productData) {
		PackagingData packagingData = getPackagingData(productData);
		return packagingData.getVariants().get(getDefaultVariant(productData));
	}

	@SuppressWarnings("unchecked")
	public PackagingData getPackagingData(ProductData productData) {
		PackagingData packagingData = new PackagingData(productData.getVariants());
		for (PackagingListDataItem dataItem : productData.getPackagingList(EffectiveFilters.EFFECTIVE)) {
			loadPackagingItem(dataItem, packagingData);
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

	private void loadPackagingItem(PackagingListDataItem dataItem, PackagingData packagingData) {

		if (nodeService.getType(dataItem.getProduct()).equals(PLMModel.TYPE_PACKAGINGKIT)) {
			loadPackagingKit(dataItem, packagingData);
		} else {
			loadPackaging(dataItem, packagingData, dataItem.getVariants());
		}
	}

	private void loadPackaging(PackagingListDataItem dataItem, PackagingData packagingData, List<NodeRef> currentVariants) {
		QName nodeType = nodeService.getType(dataItem.getProduct());

		// Sum tare (don't take in account packagingKit)
		if (dataItem.getPkgLevel() != null && !PLMModel.TYPE_PACKAGINGKIT.equals(nodeType)) {

			BigDecimal tare = FormulationHelper.getTareInKg(dataItem, nodeService);

			if (PackagingLevel.Secondary.equals(dataItem.getPkgLevel())) {

				packagingData.addTareSecondary(currentVariants, tare);
			} else if (PackagingLevel.Tertiary.equals(dataItem.getPkgLevel())) {
				packagingData.addTareTertiary(currentVariants, tare);
			}
		}

		if (nodeService.hasAspect(dataItem.getProduct(), PackModel.ASPECT_PALLET) && PackagingLevel.Secondary.equals(dataItem.getPkgLevel())
				&& PackagingListUnit.PP.equals(dataItem.getPackagingListUnit()) && PLMModel.TYPE_PACKAGINGKIT.equals(nodeType)) {
			logger.debug("load pallet aspect ");

			// product per box and boxes per pallet
			if (dataItem.getQty() != null) {
				logger.debug("setProductPerBoxes " + dataItem.getQty().intValue());
				packagingData.setProductPerBoxes(currentVariants, dataItem.getQty().intValue());
			}
			Integer palletBoxesPerPallet = (Integer) nodeService.getProperty(dataItem.getProduct(), PackModel.PROP_PALLET_BOXES_PER_PALLET);
			if (palletBoxesPerPallet != null) {
				packagingData.setBoxesPerPallet(currentVariants, palletBoxesPerPallet);
			}
			packagingData.setPalletNumberOnGround(currentVariants,
					(Integer) nodeService.getProperty(dataItem.getProduct(), PackModel.PROP_PALLET_NUMBER_ON_GROUND));
		}
	}

	// manage 2 level depth
	@SuppressWarnings("unchecked")
	private void loadPackagingKit(PackagingListDataItem dataItem, PackagingData packagingData) {

		loadPackaging(dataItem, packagingData, dataItem.getVariants());
		ProductData packagingKitData = alfrescoRepository.findOne(dataItem.getProduct());

		for (PackagingListDataItem p : packagingKitData.getPackagingList(EffectiveFilters.EFFECTIVE)) {
			loadPackaging(p, packagingData, dataItem.getVariants());
		}
	}
}
