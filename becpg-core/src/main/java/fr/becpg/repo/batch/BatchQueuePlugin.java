package fr.becpg.repo.batch;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>BatchQueuePlugin interface.</p>
 *
 * @author matthieu
 */
public interface BatchQueuePlugin {

	/**
	 * <p>onRetryBatchError.</p>
	 *
	 * @param entry a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param batchId a {@link java.lang.String} object
	 */
	void onRetryBatchError(NodeRef entry, String batchId);

}
