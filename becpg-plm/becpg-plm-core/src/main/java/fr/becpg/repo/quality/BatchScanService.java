package fr.becpg.repo.quality;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.QualityModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.quality.data.BatchData;
import fr.becpg.repo.quality.data.dataList.AllocationListDataItem;
import fr.becpg.repo.quality.data.dataList.StockListDataItem;
import fr.becpg.repo.quality.formulation.BatchFormulationHandler;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * Service for scanning and managing batch allocations and stocks.
 * Adheres to Clean Code principles and AGENTS.md conventions.
 */
@Service("batchScanService")
public class BatchScanService {

    private static final Log logger = LogFactory.getLog(BatchScanService.class);

    public static final String CONF_SCANNER_FORMAT = "beCPG.quality.batch.scanner.format";
    public static final String DEFAULT_SCANNER_FORMAT = "{bcpg:erpCode} - {qa:batchId}";

    public static final String STATUS_FOUND = "found";
    public static final String STATUS_MALFORMED = "malformed";
    public static final String STATUS_BATCH_NOT_FOUND = "batch_not_found";
    public static final String STATUS_PRODUCT_NOT_FOUND = "product_not_found";
    public static final String STATUS_LOT_NOT_FOUND = "lot_not_found";
    public static final String STATUS_STOCK_NOT_FOUND = "stock_not_found";

    private static final String ERP_CODE_KEY = "erpCode";
    private static final String BATCH_ID_KEY = "batchId";
    private static final String DEFAULT_SEPARATOR = " - ";
    private static final String HYPHEN_SEPARATOR = "-";

    @Autowired
    private NodeService nodeService;
    @Autowired
    private EntityListDAO entityListDAO;
    @Autowired
    private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
    @Autowired
    private SystemConfigurationService systemConfigurationService;
    @Autowired
    private BatchFormulationHandler batchFormulationHandler;

    private record ParsedBarcode(String codeErp, String batchId) {}
    private record AllocationMatch(AllocationListDataItem allocation, String codeErp, String batchId) {}

    /**
     * Initial scan method called with a barcode input.
     *
     * @param batchNodeRef the production batch node reference
     * @param batchScanInput the scanned barcode string
     * @return the scan result
     */
    public BatchScanResult scan(NodeRef batchNodeRef, String batchScanInput) {
        if (logger.isDebugEnabled()) {
            logger.debug("Scanning batch with input: " + batchScanInput);
        }

        if (batchNodeRef == null || batchScanInput == null || batchScanInput.trim().isEmpty()) {
            return new BatchScanResult(STATUS_MALFORMED);
        }

        BatchData batchData = (BatchData) alfrescoRepository.findOne(batchNodeRef);
        if (batchData == null) {
            return new BatchScanResult(STATUS_BATCH_NOT_FOUND);
        }

        String format = systemConfigurationService.confValue(CONF_SCANNER_FORMAT);
        ParsedBarcode parsed = parseBarcode(format, batchScanInput);
        if (parsed.codeErp().isEmpty()) {
            return new BatchScanResult(STATUS_MALFORMED);
        }

        return processParsedBarcode(batchData, parsed);
    }

    private BatchScanResult processParsedBarcode(BatchData batchData, ParsedBarcode parsed) {
        AllocationMatch match = resolveAllocation(batchData, parsed);
        if (match == null) {
            return new BatchScanResult(STATUS_PRODUCT_NOT_FOUND);
        }

        AllocationListDataItem allocation = match.allocation();
        String codeErp = match.codeErp();
        String batchId = match.batchId();
        NodeRef productNodeRef = allocation.getProduct();

        if (batchId.isEmpty()) {
            return createLotNotFoundResult(batchData, productNodeRef, allocation.getNodeRef(), codeErp);
        }

        return linkAndCreateResult(allocation, productNodeRef, codeErp, batchId);
    }

    private BatchScanResult linkAndCreateResult(AllocationListDataItem allocation, NodeRef productNodeRef, String codeErp, String batchId) {
        StockListDataItem stockItem = findStockItem(productNodeRef, batchId);
        if (stockItem != null) {
            linkStock(allocation, stockItem);
            BatchScanResult res = populateProductInfo(new BatchScanResult(STATUS_FOUND), productNodeRef, allocation.getNodeRef(), codeErp);
            res.setBatchId(batchId);
            res.setStockNodeRef(stockItem.getNodeRef().toString());
            return res;
        }

        BatchScanResult res = populateProductInfo(new BatchScanResult(STATUS_STOCK_NOT_FOUND), productNodeRef, allocation.getNodeRef(), codeErp);
        res.setBatchId(batchId);
        return res;
    }

    private AllocationMatch resolveAllocation(BatchData batchData, ParsedBarcode parsed) {
        String codeErp = parsed.codeErp();
        String batchId = parsed.batchId();
        AllocationListDataItem allocation = findAllocation(batchData, codeErp);

        if (allocation == null && !batchId.isEmpty()) {
            allocation = findAllocation(batchData, batchId);
            if (allocation != null) {
                return new AllocationMatch(allocation, batchId, codeErp);
            }
        }

        return allocation != null ? new AllocationMatch(allocation, codeErp, batchId) : null;
    }

    private ParsedBarcode parseBarcode(String format, String input) {
        String effectiveFormat = (format != null && !format.isBlank()) ? format : DEFAULT_SCANNER_FORMAT;
        String separator = extractSeparator(effectiveFormat);
        boolean erpFirst = isErpFirst(effectiveFormat);

        if (separator != null && !separator.isEmpty() && input.contains(separator)) {
            String[] parts = input.split(Pattern.quote(separator), 2);
            return createParsedBarcode(parts[0].trim(), parts[1].trim(), erpFirst);
        }

        return new ParsedBarcode(input.trim(), "");
    }

    private ParsedBarcode createParsedBarcode(String part1, String part2, boolean erpFirst) {
        return erpFirst ? new ParsedBarcode(part1, part2) : new ParsedBarcode(part2, part1);
    }

    private String extractSeparator(String format) {
        Matcher matcher = Pattern.compile("\\{([^}]+)\\}").matcher(format);
        List<int[]> matchPositions = new ArrayList<>();
        while (matcher.find()) {
            matchPositions.add(new int[] { matcher.start(), matcher.end() });
        }
        if (matchPositions.size() >= 2) {
            return format.substring(matchPositions.get(0)[1], matchPositions.get(1)[0]);
        }
        if (format.contains(DEFAULT_SEPARATOR)) {
            return DEFAULT_SEPARATOR;
        } else if (format.contains(HYPHEN_SEPARATOR)) {
            return HYPHEN_SEPARATOR;
        }
        return DEFAULT_SEPARATOR;
    }

    private boolean isErpFirst(String format) {
        Matcher matcher = Pattern.compile("\\{([^}]+)\\}").matcher(format);
        if (matcher.find()) {
            String firstPlaceholder = matcher.group(1);
            return firstPlaceholder.contains(ERP_CODE_KEY);
        }
        int erpIdx = format.indexOf(ERP_CODE_KEY);
        int batchIdx = format.indexOf(BATCH_ID_KEY);
        if (erpIdx != -1 && batchIdx != -1) {
            return erpIdx < batchIdx;
        }
        return true;
    }

    private BatchScanResult createLotNotFoundResult(BatchData batchData, NodeRef productNodeRef, NodeRef allocationNodeRef, String codeErp) {
        BatchScanResult res = populateProductInfo(new BatchScanResult(STATUS_LOT_NOT_FOUND), productNodeRef, allocationNodeRef, codeErp);
        StockListDataItem firstStock = getFirstStockItem(batchData, productNodeRef);
        if (firstStock != null) {
            res.setStockNodeRef(firstStock.getNodeRef().toString());
            res.setBatchId(firstStock.getBatchId());
        }
        return res;
    }

    private BatchScanResult populateProductInfo(BatchScanResult res, NodeRef productNodeRef, NodeRef allocationNodeRef, String codeErp) {
        res.setAllocationNodeRef(allocationNodeRef != null ? allocationNodeRef.toString() : null);
        res.setCodeErp(codeErp);
        if (productNodeRef != null) {
            res.setProductName(getProductName(productNodeRef));
            res.setProductType(nodeService.getType(productNodeRef).getLocalName());
        }
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
        List<NodeRef> currentStockRefs = allocation.getStockListItems() != null
                ? new ArrayList<>(allocation.getStockListItems())
                : new ArrayList<>();
        if (!currentStockRefs.contains(stock.getNodeRef())) {
            currentStockRefs.add(stock.getNodeRef());
            allocation.setStockListItems(currentStockRefs);
            alfrescoRepository.save(allocation);
        }
    }

    private StockListDataItem findStockItem(NodeRef productNodeRef, String batchId) {
        if (productNodeRef == null || batchId == null || batchId.isBlank()) {
            return null;
        }

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

    private StockListDataItem getFirstStockItem(BatchData batchData, NodeRef productNodeRef) {
        if (productNodeRef == null) {
            return null;
        }

        if (batchData != null && batchFormulationHandler != null) {
            ProductData productData = (ProductData) alfrescoRepository.findOne(productNodeRef);
            if (productData != null) {
                List<StockListDataItem> filteredStocks = batchFormulationHandler.extractFilteredStockList(batchData, productData, new ArrayList<>());
                if (filteredStocks != null && !filteredStocks.isEmpty()) {
                    return filteredStocks.get(0);
                }
            }
        }

        NodeRef productListContainer = entityListDAO.getListContainer(productNodeRef);
        if (productListContainer == null) {
            return null;
        }

        NodeRef stockList = entityListDAO.getList(productListContainer, QualityModel.TYPE_STOCK_LIST);
        if (stockList == null) {
            return null;
        }

        List<NodeRef> stockItems = entityListDAO.getListItems(stockList, QualityModel.TYPE_STOCK_LIST);
        if (stockItems != null && !stockItems.isEmpty()) {
            for (NodeRef stockItemNodeRef : stockItems) {
                StockListDataItem stockItem = (StockListDataItem) alfrescoRepository.findOne(stockItemNodeRef);
                if (stockItem != null) {
                    return stockItem;
                }
            }
        }
        return null;
    }

    private String getProductName(NodeRef productNodeRef) {
        if (productNodeRef != null) {
            String name = (String) nodeService.getProperty(productNodeRef, ContentModel.PROP_NAME);
            if (name != null) {
                return name;
            }
        }
        return "";
    }
}
