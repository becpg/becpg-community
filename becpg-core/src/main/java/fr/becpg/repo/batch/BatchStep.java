package fr.becpg.repo.batch;

import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;

/**
 * <p>BatchStep class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class BatchStep<T> {

	protected BatchProcessWorkProvider<T> workProvider;
	
	protected BatchProcessWorker<T> processWorker;
	
	private BatchStepListener batchStepListener;
	
	private String stepDescId;
	
	private Boolean runAsSystem;
	
	private String batchUser;

	/*
	 * A step is run inside a transaction by default, which is what nearly every
	 * batch wants: the entry either lands whole or not at all.
	 *
	 * A step that spends most of its time outside the database - waiting on a
	 * remote server, for one - should say so. Held across such a wait, the
	 * transaction keeps its row locks and stops InnoDB from purging its undo
	 * records, and every other write on the instance pays for it. Such a step is
	 * responsible for its own transactions: short ones around the database work,
	 * none around the wait.
	 */
	private Boolean transactional = Boolean.TRUE;

	/**
	 * <p>Whether the step runs inside a transaction opened for it.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	public Boolean getTransactional() {
		return transactional;
	}

	/**
	 * <p>Setter for the field <code>transactional</code>.</p>
	 *
	 * @param transactional a {@link java.lang.Boolean} object
	 */
	public void setTransactional(Boolean transactional) {
		this.transactional = transactional;
	}
	
	/**
	 * <p>Getter for the field <code>batchUser</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getBatchUser() {
		return batchUser;
	}
	
	/**
	 * <p>Setter for the field <code>batchUser</code>.</p>
	 *
	 * @param batchUser a {@link java.lang.String} object
	 */
	public void setBatchUser(String batchUser) {
		this.batchUser = batchUser;
	}
	
	/**
	 * <p>Setter for the field <code>runAsSystem</code>.</p>
	 *
	 * @param runAsSystem a {@link java.lang.Boolean} object
	 */
	public void setRunAsSystem(Boolean runAsSystem) {
		this.runAsSystem = runAsSystem;
	}
	
	/**
	 * <p>Getter for the field <code>runAsSystem</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	public Boolean getRunAsSystem() {
		return runAsSystem;
	}
	
	/**
	 * <p>Setter for the field <code>stepDescId</code>.</p>
	 *
	 * @param descId a {@link java.lang.String} object
	 */
	public void setStepDescId(String descId) {
		this.stepDescId = descId;
	}
	
	/**
	 * <p>Getter for the field <code>stepDescId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getStepDescId() {
		return stepDescId;
	}
	
	/**
	 * <p>Getter for the field <code>workProvider</code>.</p>
	 *
	 * @return a {@link org.alfresco.repo.batch.BatchProcessWorkProvider} object
	 */
	public BatchProcessWorkProvider<T> getWorkProvider() {
		return workProvider;
	}
	
	/**
	 * <p>Setter for the field <code>workProvider</code>.</p>
	 *
	 * @param workProvider a {@link org.alfresco.repo.batch.BatchProcessWorkProvider} object
	 */
	public void setWorkProvider(BatchProcessWorkProvider<T> workProvider) {
		this.workProvider = workProvider;
	}
	
	/**
	 * <p>Getter for the field <code>processWorker</code>.</p>
	 *
	 * @return a {@link org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker} object
	 */
	public BatchProcessWorker<T> getProcessWorker() {
		return processWorker;
	}
	
	/**
	 * <p>Setter for the field <code>processWorker</code>.</p>
	 *
	 * @param processWorker a {@link org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker} object
	 */
	public void setProcessWorker(BatchProcessWorker<T> processWorker) {
		this.processWorker = processWorker;
	}

	/**
	 * <p>Getter for the field <code>batchStepListener</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.batch.BatchStepListener} object
	 */
	public BatchStepListener getBatchStepListener() {
		return batchStepListener;
	}

	/**
	 * <p>Setter for the field <code>batchStepListener</code>.</p>
	 *
	 * @param batchStepListener a {@link fr.becpg.repo.batch.BatchStepListener} object
	 */
	public void setBatchStepListener(BatchStepListener batchStepListener) {
		this.batchStepListener = batchStepListener;
	}

}
