package fr.becpg.repo.regulatory;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * <p>ComplianceResult class.</p>
 *
 * @author Valentin
 */
public class ComplianceResult {

	private String batchId;
	
	private Status status;
	
	@JsonIgnore
	private RegulatoryContext context;
	
	/**
	 * <p>Getter for the field <code>context</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.regulatory.RegulatoryContext} object
	 */
	public RegulatoryContext getContext() {
		return context;
	}
	
	/**
	 * <p>Setter for the field <code>context</code>.</p>
	 *
	 * @param context a {@link fr.becpg.repo.regulatory.RegulatoryContext} object
	 */
	public void setContext(RegulatoryContext context) {
		this.context = context;
	}
	
	/**
	 * <p>Getter for the field <code>batchId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getBatchId() {
		return batchId;
	}
	
	/**
	 * <p>Setter for the field <code>batchId</code>.</p>
	 *
	 * @param batchId a {@link java.lang.String} object
	 */
	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}
	
	/**
	 * <p>Getter for the field <code>status</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.regulatory.ComplianceResult.Status} object
	 */
	public Status getStatus() {
		return status;
	}
	
	/**
	 * <p>Setter for the field <code>status</code>.</p>
	 *
	 * @param status a {@link fr.becpg.repo.regulatory.ComplianceResult.Status} object
	 */
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
