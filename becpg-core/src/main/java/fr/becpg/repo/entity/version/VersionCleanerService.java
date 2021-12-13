package fr.becpg.repo.entity.version;

public interface VersionCleanerService {
	
	public static final int MAX_PROCESSED_NODES = 5;

	public boolean cleanVersions(int maxProcessedNdoes);

	public void cleanVersionStore();
	
}
