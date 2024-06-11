package fr.becpg.repo.batch;

/**
 * <p>BatchStepListener interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface BatchStepListener {

	/**
	 * <p>beforeStep.</p>
	 */
	public void beforeStep();
	
	/**
	 * <p>afterStep.</p>
	 */
	public void afterStep();
	
	/**
	 * <p>onError.</p>
	 *
	 * @param lastErrorEntryId a {@link java.lang.String} object
	 * @param lastError a {@link java.lang.String} object
	 */
	public void onError(String lastErrorEntryId, String lastError);
}
