package fr.becpg.repo.product.comparison;

import org.alfresco.service.cmr.repository.NodeService;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.formulation.CostsCalculatingFormulationHandler;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.product.formulation.PackagingHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.model.BeCPGDataObject;

public class CurrentLevelQuantities {

	private Double qtyForProduct;
	private Double qtyForCost;
	private Double netQtyForCost;
	private Double lossRatio;
	private ProductData componentProductData;
	private CompoListDataItem compoListItem;

	private CurrentLevelQuantities() {}

	public CurrentLevelQuantities(AlfrescoRepository<BeCPGDataObject> alfrescoRepository, ProductData productData,
			PackagingListDataItem packagingListDataItem) {

		this.componentProductData = (ProductData) alfrescoRepository.findOne(packagingListDataItem.getProduct());
		this.lossRatio = packagingListDataItem.getLossPerc() != null ? packagingListDataItem.getLossPerc() : 0d;
		this.lossRatio = FormulationHelper.calculateLossPerc(productData.getProductLossPerc(), lossRatio);
		this.netQtyForCost = FormulationHelper.getNetQtyForCost(productData);
		this.qtyForCost = FormulationHelper.getQtyForCostByPackagingLevel(productData, packagingListDataItem, componentProductData);
		this.qtyForProduct = FormulationHelper.getQtyForProductByPackagingLevel(productData, packagingListDataItem, componentProductData);

	}

	public CurrentLevelQuantities(AlfrescoRepository<BeCPGDataObject> alfrescoRepository, 
			PackagingListDataItem packaginListDataItem, CurrentLevelQuantities currentLevelQuantities) {
		this(alfrescoRepository, currentLevelQuantities.getComponentProductData(), packaginListDataItem);
		this.lossRatio = FormulationHelper.calculateLossPerc(currentLevelQuantities.getLossRatio(), this.lossRatio);
		this.qtyForProduct = currentLevelQuantities.getQtyForProduct() * this.qtyForProduct;
		this.qtyForCost = (this.qtyForCost / this.netQtyForCost) * currentLevelQuantities.getQtyForCost();

	}

	public CurrentLevelQuantities(NodeService nodeService, AlfrescoRepository<BeCPGDataObject> alfrescoRepository,
			ProductData productData, ProcessListDataItem processListItem) {

		Double qty = processListItem.getQty() != null ? processListItem.getQty() : 0d;

		if ((qty == null) || (qty == 0d)) {
			qty = 1d;
		}

		if ((processListItem.getRateProduct() != null) && (processListItem.getRateProduct() != 0)) {
			qty /= processListItem.getRateProduct();
		}

		if (processListItem.getQtyResource() != null) {
			qty *= processListItem.getQtyResource();
		}

		if ((processListItem.getResource() != null) && nodeService.exists(processListItem.getResource())) {
			this.componentProductData = (ProductData) alfrescoRepository.findOne(processListItem.getResource());
		}

		this.lossRatio = processListItem.getLossPerc() != null ? processListItem.getLossPerc() : 0d;
		if (productData != null) {
			this.lossRatio = FormulationHelper.calculateLossPerc(productData.getProductLossPerc(), lossRatio);
			this.netQtyForCost = FormulationHelper.getNetQtyForCost(productData);
			this.qtyForCost = FormulationHelper.getQtyForCost(productData, null, processListItem);
		}
		this.qtyForProduct = qty;

	}

	public CurrentLevelQuantities(AlfrescoRepository<BeCPGDataObject> alfrescoRepository,
			PackagingHelper packagingHelper, CompoListDataItem compoListItem) {

		this.compoListItem = compoListItem;
		this.componentProductData = (ProductData) alfrescoRepository.findOne(compoListItem.getProduct());

		if (this.componentProductData.getDefaultVariantPackagingData() == null) {
			this.componentProductData.setDefaultVariantPackagingData(packagingHelper.getDefaultVariantPackagingData(this.componentProductData));
		}

		this.lossRatio = FormulationHelper.getComponentLossPerc(componentProductData, compoListItem);
		this.qtyForProduct = compoListItem.getQty() != null ? compoListItem.getQty() : 0d;
		this.qtyForCost = FormulationHelper.getQtyForCost(compoListItem, 0d, componentProductData,
				CostsCalculatingFormulationHandler.keepProductUnit);
		this.netQtyForCost = 1d;

	}

	public CurrentLevelQuantities(AlfrescoRepository<BeCPGDataObject> alfrescoRepository,
			PackagingHelper packagingHelper, ProductData productData, CompoListDataItem compoListItem) {

		this.compoListItem = compoListItem;
		this.componentProductData = (ProductData) alfrescoRepository.findOne(compoListItem.getProduct());

		if (this.componentProductData.getDefaultVariantPackagingData() == null) {
			this.componentProductData.setDefaultVariantPackagingData(packagingHelper.getDefaultVariantPackagingData(this.componentProductData));
		}

		this.lossRatio = FormulationHelper.calculateLossPerc(productData.getProductLossPerc() != null ? productData.getProductLossPerc() : 0d,
				FormulationHelper.getComponentLossPerc(componentProductData, compoListItem));
		this.qtyForProduct = compoListItem.getQty() != null ? compoListItem.getQty() : 0d;
		this.netQtyForCost = FormulationHelper.getNetQtyForCost(productData);
		this.qtyForCost = FormulationHelper.getQtyForCost(compoListItem, productData.getProductLossPerc(), componentProductData,
				CostsCalculatingFormulationHandler.keepProductUnit);

	}

	public CurrentLevelQuantities(AlfrescoRepository<BeCPGDataObject> alfrescoRepository,
			PackagingHelper packagingHelper, CompoListDataItem compoListItem, CurrentLevelQuantities currentLevelQuantities) {

		this(alfrescoRepository, packagingHelper, currentLevelQuantities.getComponentProductData(), compoListItem);
		this.lossRatio = FormulationHelper.calculateLossPerc(currentLevelQuantities.getLossRatio(), this.lossRatio);
		this.qtyForProduct = currentLevelQuantities.getQtyForProduct() * this.qtyForProduct;
		this.qtyForCost = (this.qtyForCost / this.netQtyForCost) * currentLevelQuantities.getQtyForCost();

		Double currentNetWeight = FormulationHelper.getNetWeight(currentLevelQuantities.getComponentProductData(),
				FormulationHelper.DEFAULT_NET_WEIGHT);
		if ((currentNetWeight != 0)) {
			this.qtyForProduct = this.qtyForProduct / currentNetWeight;
		} else {
			this.qtyForProduct = 0d;
		}

	}

	public CurrentLevelQuantities(NodeService nodeService, AlfrescoRepository<BeCPGDataObject> alfrescoRepository,
			ProcessListDataItem processListItem, CurrentLevelQuantities currentLevelQuantities) {
		this(nodeService, alfrescoRepository, currentLevelQuantities.getComponentProductData(), processListItem);
		this.lossRatio = FormulationHelper.calculateLossPerc(currentLevelQuantities.getLossRatio(), this.lossRatio);
		this.qtyForProduct = currentLevelQuantities.getQtyForProduct() * this.qtyForProduct;
		this.qtyForCost = (this.qtyForCost / this.netQtyForCost) * currentLevelQuantities.getQtyForCost();

	}

	public Double getQtyForProduct() {
		return qtyForProduct;
	}

	public Double getQtyForCost() {
		return qtyForCost;
	}

	public Double getLossRatio() {
		return lossRatio;
	}

	public ProductData getComponentProductData() {
		return componentProductData;
	}

	public CompoListDataItem getCompoListItem() {
		return compoListItem;
	}

	public Double getNetQtyForCost() {
		return netQtyForCost;
	}

}