package fr.becpg.repo.batch;

public interface BatchStepListener {

	public void beforeStep();
	
	public void afterStep();
	
	public void onError(String lastErrorEntryId, String lastError);
}
