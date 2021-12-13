package fr.becpg.repo.batch;

public interface BatchErrorCallback {

	public void run(String lastErrorEntryId, String lastError);
	
}
