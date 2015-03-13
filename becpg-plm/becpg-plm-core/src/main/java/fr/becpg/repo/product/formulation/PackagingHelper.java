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
	
	public VariantPackagingData getDefaultVariantPackagingData(ProductData productData){		
		NodeRef defaultVariantNodeRef = getDefaultVariant(productData);
		PackagingData packagingData = getPackagingData(productData, getDefaultVariant(productData));		
		return packagingData.getVariants().get(defaultVariantNodeRef);
	}
	
	public PackagingData getPackagingData(ProductData productData){		
		return getPackagingData(productData, getDefaultVariant(productData));
	}
	
	private PackagingData getPackagingData(ProductData productData, NodeRef defaultVariantNodeRef){
		PackagingData packagingData = new PackagingData(productData.getVariants());		
		for (PackagingListDataItem dataItem : productData.getPackagingList(EffectiveFilters.EFFECTIVE)) {
			loadPackagingItem(dataItem, packagingData, defaultVariantNodeRef);
		}		
		return packagingData;
	}
	
	private NodeRef getDefaultVariant(ProductData productData){
		NodeRef defaultVariantNodeRef = null;
		for (VariantData variantData : productData.getVariants()) {
			if (variantData.getIsDefaultVariant()) {
				defaultVariantNodeRef = variantData.getNodeRef();
			}
		}
		return defaultVariantNodeRef;
	}
	
	private void loadPackagingItem(PackagingListDataItem dataItem, PackagingData packagingData,
			NodeRef defaultVariantNodeRef) {

		if (nodeService.getType(dataItem.getProduct()).equals(PLMModel.TYPE_PACKAGINGKIT)) {
			loadPackagingKit(dataItem, packagingData, defaultVariantNodeRef);			
		} else {
			loadPackaging(dataItem, packagingData, defaultVariantNodeRef, dataItem.getVariants());
		}
	}

	private void loadPackaging(PackagingListDataItem dataItem, PackagingData packagingData,
		NodeRef defaultVariantNodeRef, List<NodeRef> currentVariants) {
		QName nodeType = nodeService.getType(dataItem.getProduct());

		if (nodeService.hasAspect(dataItem.getProduct(), PackModel.ASPECT_TARE)) {

			// Sum tare (don't take in account packagingKit)
			if (dataItem.getPkgLevel() != null && !PLMModel.TYPE_PACKAGINGKIT.equals(nodeType)) {

				BigDecimal tare = FormulationHelper.getTareInKg(dataItem, nodeService);

				if (dataItem.getPkgLevel().equals(PackagingLevel.Secondary)) {
					packagingData.addTareSecondary(currentVariants, tare);
				} else if (dataItem.getPkgLevel().equals(PackagingLevel.Tertiary)) {
					packagingData.addTareTertiary(currentVariants, tare);
				}
			}
		}

		if (nodeService.hasAspect(dataItem.getProduct(), PackModel.ASPECT_PALLET)) {
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
		}
	}

	// manage 2 level depth
	@SuppressWarnings("unchecked")
	private void loadPackagingKit(PackagingListDataItem dataItem, PackagingData packagingData, NodeRef defaultVariantNodeRef) {

		loadPackaging(dataItem, packagingData, defaultVariantNodeRef, dataItem.getVariants());
		ProductData packagingKitData = alfrescoRepository.findOne(dataItem.getProduct());

		for (PackagingListDataItem p : packagingKitData.getPackagingList(EffectiveFilters.EFFECTIVE)) {
			loadPackaging(p, packagingData, defaultVariantNodeRef, dataItem.getVariants());
		}
	}
}
