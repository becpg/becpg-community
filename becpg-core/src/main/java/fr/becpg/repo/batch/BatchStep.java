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

	private BatchProcessWorkProvider<T> workProvider;
	
	private BatchProcessWorker<T> processWorker;
	
	private BatchStepListener batchStepListener;
	
	private String stepDescId;
	
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
