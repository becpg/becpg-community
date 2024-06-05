package fr.becpg.repo.product.data;

import org.alfresco.service.cmr.repository.NodeService;

import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.formulation.CostsCalculatingFormulationHandler;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.product.formulation.PackagingHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * <p>CurrentLevelQuantities class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class CurrentLevelQuantities {

	private Double qtyForProduct;
	private Double qtyForCost;
	private Double netQtyForCost;
	private Double lossRatio;
	private ProductData componentProductData;
	private CompoListDataItem compoListItem;


	/**
	 * <p>Constructor for CurrentLevelQuantities.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param packagingListDataItem a {@link fr.becpg.repo.product.data.productList.PackagingListDataItem} object
	 */
	public CurrentLevelQuantities(AlfrescoRepository<BeCPGDataObject> alfrescoRepository, ProductData productData,
			PackagingListDataItem packagingListDataItem) {

		this.componentProductData = (ProductData) alfrescoRepository.findOne(packagingListDataItem.getProduct());
		this.lossRatio = packagingListDataItem.getLossPerc() != null ? packagingListDataItem.getLossPerc() : 0d;
		this.lossRatio = FormulationHelper.calculateLossPerc(productData.getProductLossPerc(), lossRatio);
		this.netQtyForCost = FormulationHelper.getNetQtyForCost(productData);
		this.qtyForCost = FormulationHelper.getQtyForCostByPackagingLevel(productData, packagingListDataItem, componentProductData);
		this.qtyForProduct = FormulationHelper.getQtyForProductByPackagingLevel(productData, packagingListDataItem, componentProductData);

	}

	/**
	 * <p>Constructor for CurrentLevelQuantities.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
	 * @param packaginListDataItem a {@link fr.becpg.repo.product.data.productList.PackagingListDataItem} object
	 * @param currentLevelQuantities a {@link fr.becpg.repo.product.data.CurrentLevelQuantities} object
	 */
	public CurrentLevelQuantities(AlfrescoRepository<BeCPGDataObject> alfrescoRepository, 
			PackagingListDataItem packaginListDataItem, CurrentLevelQuantities currentLevelQuantities) {
		this(alfrescoRepository, currentLevelQuantities.getComponentProductData(), packaginListDataItem);
		this.lossRatio = FormulationHelper.calculateLossPerc(currentLevelQuantities.getLossRatio(), this.lossRatio);
		this.qtyForProduct = currentLevelQuantities.getQtyForProduct() * this.qtyForProduct;
		this.qtyForCost = (this.qtyForCost / this.netQtyForCost) * currentLevelQuantities.getQtyForCost();

	}

	/**
	 * <p>Constructor for CurrentLevelQuantities.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param processListItem a {@link fr.becpg.repo.product.data.productList.ProcessListDataItem} object
	 */
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

	/**
	 * <p>Constructor for CurrentLevelQuantities.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
	 * @param packagingHelper a {@link fr.becpg.repo.product.formulation.PackagingHelper} object
	 * @param compoListItem a {@link fr.becpg.repo.product.data.productList.CompoListDataItem} object
	 */
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
				CostsCalculatingFormulationHandler.keepProductUnit());
		this.netQtyForCost = 1d;

	}

	/**
	 * <p>Constructor for CurrentLevelQuantities.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
	 * @param packagingHelper a {@link fr.becpg.repo.product.formulation.PackagingHelper} object
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param compoListItem a {@link fr.becpg.repo.product.data.productList.CompoListDataItem} object
	 */
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
				CostsCalculatingFormulationHandler.keepProductUnit());

	}

	/**
	 * <p>Constructor for CurrentLevelQuantities.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
	 * @param packagingHelper a {@link fr.becpg.repo.product.formulation.PackagingHelper} object
	 * @param compoListItem a {@link fr.becpg.repo.product.data.productList.CompoListDataItem} object
	 * @param currentLevelQuantities a {@link fr.becpg.repo.product.data.CurrentLevelQuantities} object
	 */
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

	/**
	 * <p>Constructor for CurrentLevelQuantities.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
	 * @param processListItem a {@link fr.becpg.repo.product.data.productList.ProcessListDataItem} object
	 * @param currentLevelQuantities a {@link fr.becpg.repo.product.data.CurrentLevelQuantities} object
	 */
	public CurrentLevelQuantities(NodeService nodeService, AlfrescoRepository<BeCPGDataObject> alfrescoRepository,
			ProcessListDataItem processListItem, CurrentLevelQuantities currentLevelQuantities) {
		this(nodeService, alfrescoRepository, currentLevelQuantities.getComponentProductData(), processListItem);
		this.lossRatio = FormulationHelper.calculateLossPerc(currentLevelQuantities.getLossRatio(), this.lossRatio);
		this.qtyForProduct = currentLevelQuantities.getQtyForProduct() * this.qtyForProduct;
		this.qtyForCost = (this.qtyForCost / this.netQtyForCost) * currentLevelQuantities.getQtyForCost();

	}

	/**
	 * <p>Getter for the field <code>qtyForProduct</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getQtyForProduct() {
		return qtyForProduct;
	}

	/**
	 * <p>Getter for the field <code>qtyForCost</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getQtyForCost() {
		return qtyForCost;
	}

	/**
	 * <p>Getter for the field <code>lossRatio</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getLossRatio() {
		return lossRatio;
	}

	/**
	 * <p>Getter for the field <code>componentProductData</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.ProductData} object
	 */
	public ProductData getComponentProductData() {
		return componentProductData;
	}

	/**
	 * <p>Getter for the field <code>compoListItem</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.productList.CompoListDataItem} object
	 */
	public CompoListDataItem getCompoListItem() {
		return compoListItem;
	}

	/**
	 * <p>Getter for the field <code>netQtyForCost</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getNetQtyForCost() {
		return netQtyForCost;
	}

}
