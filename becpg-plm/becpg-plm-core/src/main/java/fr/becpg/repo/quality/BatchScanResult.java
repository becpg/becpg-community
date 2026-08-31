package fr.becpg.repo.quality;

import java.io.Serializable;

/**
 * Pure data structure representing the result of a batch scan operation.
 * Adheres to Clean Code principles.
 */
public class BatchScanResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private String scanStatus;
    private String allocationNodeRef;
    private String codeErp;
    private String productName;
    private String batchId;
    private String productType;
    private String stockNodeRef;

    public BatchScanResult() {}

    public BatchScanResult(String scanStatus) {
        this.scanStatus = scanStatus;
    }

    public String getScanStatus() {
        return scanStatus;
    }

    public void setScanStatus(String scanStatus) {
        this.scanStatus = scanStatus;
    }

    public String getAllocationNodeRef() {
        return allocationNodeRef;
    }

    public void setAllocationNodeRef(String allocationNodeRef) {
        this.allocationNodeRef = allocationNodeRef;
    }

    public String getCodeErp() {
        return codeErp;
    }

    public void setCodeErp(String codeErp) {
        this.codeErp = codeErp;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getStockNodeRef() {
        return stockNodeRef;
    }

    public void setStockNodeRef(String stockNodeRef) {
        this.stockNodeRef = stockNodeRef;
    }
}
