package fr.becpg.repo.entity.version;

/**
 * <p>VersionCleanerService interface.</p>
 *
 * @author matthieu
 */
public interface VersionCleanerService {
	
	/** Constant <code>MAX_PROCESSED_NODES=5</code> */
	public static final int MAX_PROCESSED_NODES = 5;

	/**
	 * <p>cleanVersions.</p>
	 *
	 * @param maxProcessedNdoes a int
	 * @param path a {@link java.lang.String} object
	 * @return a boolean
	 */
	public boolean cleanVersions(int maxProcessedNdoes, String path);

	/**
	 * <p>cleanVersionStore.</p>
	 */
	public void cleanVersionStore();

}
