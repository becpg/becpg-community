package fr.becpg.repo.batch;

import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;

public class BatchStep<T> {

	private BatchProcessWorkProvider<T> workProvider;
	
	private BatchProcessWorker<T> processWorker;
	
	private BatchStepListener batchStepListener;
	
	private String stepDescId;
	
	public void setStepDescId(String descId) {
		this.stepDescId = descId;
	}
	
	public String getStepDescId() {
		return stepDescId;
	}
	
	public BatchProcessWorkProvider<T> getWorkProvider() {
		return workProvider;
	}
	
	public void setWorkProvider(BatchProcessWorkProvider<T> workProvider) {
		this.workProvider = workProvider;
	}
	
	public BatchProcessWorker<T> getProcessWorker() {
		return processWorker;
	}
	
	public void setProcessWorker(BatchProcessWorker<T> processWorker) {
		this.processWorker = processWorker;
	}

	public BatchStepListener getBatchStepListener() {
		return batchStepListener;
	}

	public void setBatchStepListener(BatchStepListener batchStepListener) {
		this.batchStepListener = batchStepListener;
	}
	
}
