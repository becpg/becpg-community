package fr.becpg.repo.entity.version;

public interface VersionCleanerService {
	
	public static final int MAX_PROCESSED_NODES = 100;

	public boolean cleanVersions(int maxProcessedNdoes);
	
}
