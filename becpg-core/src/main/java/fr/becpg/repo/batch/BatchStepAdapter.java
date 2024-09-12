package fr.becpg.repo.batch;

/**
 * <p>BatchStepAdapter class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class BatchStepAdapter implements BatchStepListener {
	
	/** {@inheritDoc} */
	@Override
	public void beforeStep() {
		// default
	}
	
	/** {@inheritDoc} */
	@Override
	public void afterStep() {
		// default
	}
	
	/** {@inheritDoc} */
	@Override
	public void onError(String lastErrorEntryId, String lastError) {
		// default
	}
	
}
