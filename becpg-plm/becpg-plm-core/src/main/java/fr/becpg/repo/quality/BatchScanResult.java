package fr.becpg.repo.quality;

import java.io.Serializable;

public class BatchScanResult implements Serializable {
	private static final long serialVersionUID = 1L;

	private boolean productFound;
	private boolean batchIdFound;
	private String codeErp;
	private String productName;
	private String batchId;
	private String scanStatus;
	private String msgText;

	public BatchScanResult() {}

	public BatchScanResult(boolean productFound, boolean batchIdFound, String scanStatus, String msgText) {
		this.productFound = productFound;
		this.batchIdFound = batchIdFound;
		this.scanStatus = scanStatus;
		this.msgText = msgText;
	}

	public boolean isProductFound() {
		return productFound;
	}

	public void setProductFound(boolean productFound) {
		this.productFound = productFound;
	}

	public boolean isBatchIdFound() {
		return batchIdFound;
	}

	public void setBatchIdFound(boolean batchIdFound) {
		this.batchIdFound = batchIdFound;
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

	public String getScanStatus() {
		return scanStatus;
	}

	public void setScanStatus(String scanStatus) {
		this.scanStatus = scanStatus;
	}

	public String getMsgText() {
		return msgText;
	}

	public void setMsgText(String msgText) {
		this.msgText = msgText;
	}
}