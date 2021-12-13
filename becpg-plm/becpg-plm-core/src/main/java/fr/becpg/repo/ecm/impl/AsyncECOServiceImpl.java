package fr.becpg.repo.ecm.impl;

import java.util.Arrays;

import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.batch.EntityListBatchProcessWorkProvider;
import fr.becpg.repo.ecm.AsyncECOService;
import fr.becpg.repo.ecm.ECOService;

/**
 * <p>
 * AsyncECOServiceImpl class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("asyncECOService")
public class AsyncECOServiceImpl implements AsyncECOService {

	private static final Log logger = LogFactory.getLog(AsyncECOServiceImpl.class);
	private static final String ASYNC_ACTION_URL_PREFIX = "page/entity-data-lists?list=changeUnitList&nodeRef=%s";

	@Autowired
	private ECOService ecoService;

	@Autowired
	private BatchQueueService batchQueueService;

	/** {@inheritDoc} */
	@Override
	public void applyAsync(NodeRef ecoNodeRef) {
		runAsync(ecoNodeRef, true);
	}

	/** {@inheritDoc} */
	@Override
	public void doSimulationAsync(NodeRef ecoNodeRef) {
		runAsync(ecoNodeRef, false);
	}

	private void runAsync(NodeRef ecoNodeRef, boolean apply) {

		boolean ret = ecoService.setInProgress(ecoNodeRef);

		if (ret) {

			BatchInfo batchInfo = new BatchInfo(String.format(apply ? "simulateECO-%s" : "applyECO-%s", ecoNodeRef.getId()),
					apply ? "becpg.batch.eco.apply" : "becpg.batch.eco.simulate");
			batchInfo.enableNotifyByMail(apply ? "eco.apply" : "eco.simulate", String.format(ASYNC_ACTION_URL_PREFIX, ecoNodeRef.toString()));
			batchInfo.setWorkerThreads(1);
			batchInfo.setBatchSize(1);

			batchQueueService.queueBatch(batchInfo, new EntityListBatchProcessWorkProvider<>(Arrays.asList(ecoNodeRef)),
					new BatchProcessor.BatchProcessWorkerAdaptor<>() {

						@Override
						public void process(NodeRef entry) throws Throwable {
							if (apply) {
								ecoService.apply(ecoNodeRef);
							} else {
								ecoService.doSimulation(ecoNodeRef);
							}
						}

					}, (erroEntryId, error) -> ecoService.setInError(ecoNodeRef, error));
		} else {
			logger.warn("ECO already InProgress:" + ecoNodeRef);
		}

	}

}
