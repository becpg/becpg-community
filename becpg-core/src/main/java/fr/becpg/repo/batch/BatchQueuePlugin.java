package fr.becpg.repo.batch;

import org.alfresco.service.cmr.repository.NodeRef;

public interface BatchQueuePlugin {

	void onRetryBatchError(NodeRef entry, String batchId);

}
