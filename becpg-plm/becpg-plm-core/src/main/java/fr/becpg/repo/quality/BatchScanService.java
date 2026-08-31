package fr.becpg.repo.quality;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.QualityModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.quality.data.BatchData;
import fr.becpg.repo.quality.data.dataList.AllocationListDataItem;
import fr.becpg.repo.quality.data.dataList.StockListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.system.SystemConfigurationService;

@Service("batchScanService")
public class BatchScanService {

	private static final Log logger = LogFactory.getLog(BatchScanService.class);

	@Autowired
	private NodeService nodeService;
	@Autowired
	private EntityListDAO entityListDAO;
	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	@Autowired
	private SystemConfigurationService systemConfigurationService;

	public BatchScanResult scan(NodeRef batchNodeRef, String batchScanInput) {
		if (batchNodeRef == null || batchScanInput == null || batchScanInput.trim().isEmpty()) {
			return new BatchScanResult(false, false, "malformed", "message.qa-batch-scan.malformed");
		}

		BatchData batchData = (BatchData) alfrescoRepository.findOne(batchNodeRef);
		if (batchData == null) {
			return new BatchScanResult(false, false, "batch_not_found", "message.qa-batch-scan.batch_not_found");
		}

		String format = systemConfigurationService.confValue("beCPG.quality.scanner.format");
		if (format == null || format.trim().isEmpty()) {
			format = "bcpg:erpCode - qa:batchId";
		}

		String separator = " - ";
		boolean erpFirst = true;
		if (format.contains(" - ")) {
			separator = " - ";
			erpFirst = format.startsWith("bcpg:erpCode") || format.startsWith("prop_bcpg_erpCode");
		} else if (format.contains("-")) {
			separator = "-";
			erpFirst = format.startsWith("bcpg:erpCode") || format.startsWith("prop_bcpg_erpCode");
		}

		// 1. Check if input matches the configured format (contains separator)
		if (batchScanInput.contains(separator)) {
			String[] parts = batchScanInput.split(separator, 2);
			String codeErp = erpFirst ? parts[0].trim() : parts[1].trim();
			String batchId = erpFirst ? parts[1].trim() : parts[0].trim();

			if (codeErp.isEmpty()) {
				return new BatchScanResult(false, false, "malformed", "message.qa-batch-scan.malformed");
			}

			AllocationListDataItem allocation = findAllocation(batchData, codeErp);
			// Fallback swap if bi-directional format
			if (allocation == null && !batchId.isEmpty()) {
				allocation = findAllocation(batchData, batchId);
				if (allocation != null) {
					String tmp = codeErp;
					codeErp = batchId;
					batchId = tmp;
				}
			}

			if (allocation == null) {
				return new BatchScanResult(false, false, "product_not_found", "message.qa-batch-scan.product_not_found");
			}

			NodeRef productNodeRef = allocation.getProduct();

			// If batchId is blank (e.g. operator left prompt empty)
			if (batchId.isEmpty()) {
				StockListDataItem emptyStock = findOrCreateEmptyStock(productNodeRef);
				linkStock(allocation, emptyStock);
				BatchScanResult res = new BatchScanResult(true, true, "found", "Success");
				res.setCodeErp(codeErp);
				res.setProductName(getProductName(productNodeRef));
				res.setBatchId("");
				return res;
			}

			// Search for existing stock matching batchId
			StockListDataItem stockItem = findStockItem(productNodeRef, batchId);
			if (stockItem != null) {
				linkStock(allocation, stockItem);
				BatchScanResult res = new BatchScanResult(true, true, "found", "Success");
				res.setCodeErp(codeErp);
				res.setProductName(getProductName(productNodeRef));
				res.setBatchId(batchId);
				return res;
			} else {
				BatchScanResult res = new BatchScanResult(true, false, "stock_not_found", "message.qa-batch-scan.stock_not_found");
				res.setCodeErp(codeErp);
				res.setProductName(getProductName(productNodeRef));
				res.setBatchId(batchId);
				return res;
			}
		}

		// 2. Code does NOT match format (extract MP only)
		String codeErp = batchScanInput.trim();
		AllocationListDataItem allocation = findAllocation(batchData, codeErp);
		if (allocation == null) {
			return new BatchScanResult(false, false, "product_not_found", "message.qa-batch-scan.product_not_found");
		}

		NodeRef productNodeRef = allocation.getProduct();
		BatchScanResult res = new BatchScanResult(true, false, "lot_not_found", "message.qa-batch-scan.stock_not_found");
		res.setCodeErp(codeErp);
		res.setProductName(getProductName(productNodeRef));
		return res;
	}

	private AllocationListDataItem findAllocation(BatchData batchData, String codeErp) {
		if (batchData.getAllocationList() != null) {
			for (AllocationListDataItem allocationItem : batchData.getAllocationList()) {
				NodeRef productNodeRef = allocationItem.getProduct();
				if (productNodeRef != null) {
					String productErpCode = (String) nodeService.getProperty(productNodeRef, BeCPGModel.PROP_ERP_CODE);
					if (productErpCode != null && productErpCode.equalsIgnoreCase(codeErp)) {
						return allocationItem;
					}
				}
			}
		}
		return null;
	}

	private void linkStock(AllocationListDataItem allocation, StockListDataItem stock) {
		List<NodeRef> currentStockRefs = allocation.getStockListItems();
		if (currentStockRefs == null) {
			currentStockRefs = new ArrayList<>();
		}
		if (!currentStockRefs.contains(stock.getNodeRef())) {
			currentStockRefs.add(stock.getNodeRef());
			allocation.setStockListItems(currentStockRefs);
			alfrescoRepository.save(allocation);
		}
	}

	private StockListDataItem findStockItem(NodeRef productNodeRef, String batchId) {
		NodeRef productListContainer = entityListDAO.getListContainer(productNodeRef);
		if (productListContainer == null) {
			return null;
		}

		NodeRef stockList = entityListDAO.getList(productListContainer, QualityModel.TYPE_STOCK_LIST);
		if (stockList == null) {
			return null;
		}

		List<NodeRef> stockItems = entityListDAO.getListItems(stockList, QualityModel.TYPE_STOCK_LIST);
		if (stockItems != null) {
			for (NodeRef stockItemNodeRef : stockItems) {
				StockListDataItem stockItem = (StockListDataItem) alfrescoRepository.findOne(stockItemNodeRef);
				if (stockItem != null) {
					String stockBatchId = stockItem.getBatchId();
					if (stockBatchId != null && stockBatchId.equalsIgnoreCase(batchId)) {
						return stockItem;
					}
				}
			}
		}
		return null;
	}

	private StockListDataItem findOrCreateEmptyStock(NodeRef productNodeRef) {
		NodeRef productListContainer = entityListDAO.getListContainer(productNodeRef);
		if (productListContainer == null) {
			productListContainer = entityListDAO.createListContainer(productNodeRef);
		}

		NodeRef stockList = entityListDAO.getList(productListContainer, QualityModel.TYPE_STOCK_LIST);
		if (stockList == null) {
			stockList = entityListDAO.createList(productListContainer, QualityModel.TYPE_STOCK_LIST);
		}

		List<NodeRef> stockItems = entityListDAO.getListItems(stockList, QualityModel.TYPE_STOCK_LIST);
		if (stockItems != null) {
			for (NodeRef stockItemNodeRef : stockItems) {
				StockListDataItem stockItem = (StockListDataItem) alfrescoRepository.findOne(stockItemNodeRef);
				if (stockItem != null) {
					String stockBatchId = stockItem.getBatchId();
					if (stockBatchId == null || stockBatchId.trim().isEmpty() || "SANS LOT".equalsIgnoreCase(stockBatchId)) {
						return stockItem;
					}
				}
			}
		}

		StockListDataItem emptyStock = new StockListDataItem();
		emptyStock.setProduct(productNodeRef);
		emptyStock.setBatchId("");
		emptyStock.setBatchQty(0d);
		return (StockListDataItem) alfrescoRepository.create(stockList, emptyStock);
	}

	private String getProductName(NodeRef productNodeRef) {
		if (productNodeRef != null) {
			String name = (String) nodeService.getProperty(productNodeRef, org.alfresco.model.ContentModel.PROP_NAME);
			if (name != null) {
				return name;
			}
		}
		return "";
	}
}