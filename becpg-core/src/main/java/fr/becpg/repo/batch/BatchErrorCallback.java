package fr.becpg.repo.batch;

/**
 * <p>BatchErrorCallback interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface BatchErrorCallback {

	/**
	 * <p>run.</p>
	 *
	 * @param lastErrorEntryId a {@link java.lang.String} object
	 * @param lastError a {@link java.lang.String} object
	 */
	public void run(String lastErrorEntryId, String lastError);
	
}
