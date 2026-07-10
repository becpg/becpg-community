package fr.becpg.repo.quality.web.scripts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.QualityModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.quality.data.BatchData;
import fr.becpg.repo.quality.data.dataList.AllocationListDataItem;
import fr.becpg.repo.quality.data.dataList.StockListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

public class ScanBatchWebScript extends DeclarativeWebScript {

	private static final Log logger = LogFactory.getLog(ScanBatchWebScript.class);

	private NodeService nodeService;
	private EntityListDAO entityListDAO;
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		Map<String, Object> model = new HashMap<>();
		model.put("scanStatus", "malformed");
		model.put("msgText", "");

		String batchNodeRefStr = req.getParameter("nodeRef");
		String scanInput = null;

		try {
			String content = req.getContent().getContent();
			if (content != null && !content.isEmpty()) {
				ObjectMapper mapper = new ObjectMapper();
				Map<String, Object> jsonMap = mapper.readValue(content, Map.class);
				if (jsonMap != null && jsonMap.containsKey("prop_qa_batchScannerInput")) {
					scanInput = (String) jsonMap.get("prop_qa_batchScannerInput");
				}
			}
		} catch (Exception e) {
			logger.error("Error parsing JSON content", e);
		}

		if (batchNodeRefStr == null || scanInput == null || scanInput.trim().isEmpty()) {
			status.setCode(Status.STATUS_BAD_REQUEST);
			status.setMessage("message.qa-batch-scan.malformed");
			model.put("scanStatus", "malformed");
			model.put("msgText", "message.qa-batch-scan.malformed");
			return model;
		}

		NodeRef batchNodeRef = new NodeRef(batchNodeRefStr);
		if (!nodeService.exists(batchNodeRef)) {
			status.setCode(Status.STATUS_NOT_FOUND);
			status.setMessage("message.qa-batch-scan.batch_not_found");
			model.put("scanStatus", "batch_not_found");
			model.put("msgText", "message.qa-batch-scan.batch_not_found");
			return model;
		}

		try {
			executeScan(batchNodeRef, scanInput);
			model.put("scanStatus", "found");
			model.put("msgText", "Success");
		} catch (IllegalArgumentException e) {
			String msg = e.getMessage();
			if ("Batch not found".equals(msg)) {
				status.setCode(Status.STATUS_NOT_FOUND);
				status.setMessage("message.qa-batch-scan.batch_not_found");
				model.put("scanStatus", "batch_not_found");
				model.put("msgText", "message.qa-batch-scan.batch_not_found");
			} else if ("Product not found".equals(msg)) {
				status.setCode(Status.STATUS_NOT_FOUND);
				status.setMessage("message.qa-batch-scan.product_not_found");
				model.put("scanStatus", "product_not_found");
				model.put("msgText", "message.qa-batch-scan.product_not_found");
			} else if ("Stock not found".equals(msg)) {
				status.setCode(Status.STATUS_NOT_FOUND);
				status.setMessage("message.qa-batch-scan.stock_not_found");
				model.put("scanStatus", "stock_not_found");
				model.put("msgText", "message.qa-batch-scan.stock_not_found");
			} else {
				status.setCode(Status.STATUS_BAD_REQUEST);
				status.setMessage("message.qa-batch-scan.malformed");
				model.put("scanStatus", "malformed");
				model.put("msgText", "message.qa-batch-scan.malformed");
			}
		}

		return model;
	}

	public void executeScan(NodeRef batchNodeRef, String scanInput) {
		if (batchNodeRef == null || scanInput == null || scanInput.trim().isEmpty()) {
			throw new IllegalArgumentException("Malformed scan input");
		}

		String part1 = null;
		String part2 = null;
		if (scanInput.contains(" - ")) {
			String[] parts = scanInput.split(" - ");
			if (parts.length >= 2) {
				part1 = parts[0].trim();
				part2 = parts[1].trim();
			}
		} else if (scanInput.contains("-")) {
			String[] parts = scanInput.split("-");
			if (parts.length >= 2) {
				part1 = parts[0].trim();
				part2 = parts[1].trim();
			}
		}

		if (part1 == null || part2 == null || part1.isEmpty() || part2.isEmpty()) {
			throw new IllegalArgumentException("Malformed scan input");
		}

		BatchData batchData = (BatchData) alfrescoRepository.findOne(batchNodeRef);
		if (batchData == null) {
			throw new IllegalArgumentException("Batch not found");
		}

		// Try Order A: part1 = codeErp, part2 = batchId
		AllocationListDataItem matchingAllocation = null;
		StockListDataItem matchingStock = null;

		if (batchData.getAllocationList() != null) {
			for (AllocationListDataItem allocationItem : batchData.getAllocationList()) {
				NodeRef productNodeRef = allocationItem.getProduct();
				if (productNodeRef != null) {
					String productErpCode = (String) nodeService.getProperty(productNodeRef, BeCPGModel.PROP_ERP_CODE);
					if (productErpCode != null && productErpCode.equalsIgnoreCase(part1)) {
						StockListDataItem stock = findStockItem(productNodeRef, part2);
						if (stock != null) {
							matchingAllocation = allocationItem;
							matchingStock = stock;
							break;
						}
					}
				}
			}
		}

		// Try Order B: part2 = codeErp, part1 = batchId
		if (matchingAllocation == null) {
			if (batchData.getAllocationList() != null) {
				for (AllocationListDataItem allocationItem : batchData.getAllocationList()) {
					NodeRef productNodeRef = allocationItem.getProduct();
					if (productNodeRef != null) {
						String productErpCode = (String) nodeService.getProperty(productNodeRef, BeCPGModel.PROP_ERP_CODE);
						if (productErpCode != null && productErpCode.equalsIgnoreCase(part2)) {
							StockListDataItem stock = findStockItem(productNodeRef, part1);
							if (stock != null) {
								matchingAllocation = allocationItem;
								matchingStock = stock;
								break;
							}
						}
					}
				}
			}
		}

		if (matchingAllocation == null || matchingStock == null) {
			boolean erpMatchPart1 = hasAllocationForErp(batchData, part1);
			boolean erpMatchPart2 = hasAllocationForErp(batchData, part2);
			if (erpMatchPart1 || erpMatchPart2) {
				throw new IllegalArgumentException("Stock not found");
			} else {
				throw new IllegalArgumentException("Product not found");
			}
		}

		List<NodeRef> currentStockRefs = matchingAllocation.getStockListItems();
		if (currentStockRefs == null) {
			currentStockRefs = new ArrayList<>();
		}
		if (!currentStockRefs.contains(matchingStock.getNodeRef())) {
			currentStockRefs.add(matchingStock.getNodeRef());
			matchingAllocation.setStockListItems(currentStockRefs);
			alfrescoRepository.save(matchingAllocation);
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

	private boolean hasAllocationForErp(BatchData batchData, String codeErp) {
		if (batchData.getAllocationList() != null) {
			for (AllocationListDataItem allocationItem : batchData.getAllocationList()) {
				NodeRef productNodeRef = allocationItem.getProduct();
				if (productNodeRef != null) {
					String productErpCode = (String) nodeService.getProperty(productNodeRef, BeCPGModel.PROP_ERP_CODE);
					if (productErpCode != null && productErpCode.equalsIgnoreCase(codeErp)) {
						return true;
					}
				}
			}
		}
		return false;
	}
}