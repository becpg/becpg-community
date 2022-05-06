package fr.becpg.repo.batch;

public class BatchStepAdapter implements BatchStepListener {
	
	@Override
	public void beforeStep() {
		// default
	}
	
	@Override
	public void afterStep() {
		// default
	}
	
	@Override
	public void onError(String lastErrorEntryId, String lastError) {
		// default
	}
	
}
