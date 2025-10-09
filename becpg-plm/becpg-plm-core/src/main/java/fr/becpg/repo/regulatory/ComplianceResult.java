package fr.becpg.repo.regulatory;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Valentin
 */
public class ComplianceResult {

	private String batchId;
	
	private Status status;
	
	@JsonIgnore
	private RegulatoryContext context;
	
	public RegulatoryContext getContext() {
		return context;
	}
	
	public void setContext(RegulatoryContext context) {
		this.context = context;
	}
	
	public String getBatchId() {
		return batchId;
	}
	
	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
	enum Status {
		STARTED,
		PENDING,
		FINISHED,
		NOT_APPLICABLE,
		UP_TO_DATE,
	}
	
}
