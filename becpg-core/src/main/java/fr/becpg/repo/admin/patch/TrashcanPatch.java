package fr.becpg.repo.admin.patch;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * <p>TrashcanPatch class.</p>
 *
 * @author matthieu
 */
public class TrashcanPatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(TrashcanPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.trashcanPatch.result";

	private Duration keepPeriod;
	
	/**
	 * <p>Setter for the field <code>keepPeriod</code>.</p>
	 *
	 * @param keepPeriod a {@link java.lang.String} object
	 */
	public void setKeepPeriod(String keepPeriod) {
		this.keepPeriod = Duration.parse(keepPeriod)
				// add 1 day to avoid undesired deletions as TrashcanCleaner runs every day
				.plusDays(1)
				;
	}
	
	/** {@inheritDoc} */
	@Override
	protected String applyInternal() throws Exception {

		BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<>() {
			final List<NodeRef> result = new ArrayList<>();

			final long maxNodeId = getNodeDAO().getMaxNodeId();

			long minSearchNodeId = 0;
			long maxSearchNodeId = INC;

			final Pair<Long, QName> val = getQnameDAO().getQName(ContentModel.TYPE_CONTENT);

			@Override
			public int getTotalEstimatedWorkSize() {
				return result.size();
			}

			@Override
			public long getTotalEstimatedWorkSizeLong() {
				return getTotalEstimatedWorkSize();
			}

			@Override
			public Collection<NodeRef> getNextWork() {
				if (val != null) {
					Long typeQNameId = val.getFirst();

					result.clear();

					while (result.isEmpty() && (minSearchNodeId < maxNodeId)) {
						List<Long> nodeids = getPatchDAO().getNodesByTypeQNameId(typeQNameId, minSearchNodeId, maxSearchNodeId);

						for (Long nodeid : nodeids) {
							NodeRef.Status status = getNodeDAO().getNodeIdStatus(nodeid);
							if (!status.isDeleted() && StoreRef.STORE_REF_ARCHIVE_SPACESSTORE.equals(status.getNodeRef().getStoreRef())) {
								result.add(status.getNodeRef());
							}
						}
						minSearchNodeId = minSearchNodeId + INC;
						maxSearchNodeId = maxSearchNodeId + INC;
					}
				}

				return result;
			}
		};

		BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>("TrashcanPatch", transactionService.getRetryingTransactionHelper(),
				workProvider, BATCH_THREADS, BATCH_SIZE, applicationEventPublisher, logger, 1000);

		BatchProcessWorker<NodeRef> worker = new BatchProcessWorker<>() {

			@Override
			public void afterProcess() throws Throwable {
				//Do nothing

			}

			@Override
			public void beforeProcess() throws Throwable {
				//Do nothing
			}

			@Override
			public String getIdentifier(NodeRef entry) {
				return entry.toString();
			}

			@Override
			public void process(NodeRef nodeRef) throws Throwable {
				if (nodeService.exists(nodeRef)) {
					AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
					String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
					if (name.startsWith("entity_deleted_") && olderThanDaysToKeep(nodeRef)) {
						nodeService.deleteNode(nodeRef);
					}
				}
			}

		};

		// Now set the batch processor to work

		batchProcessor.processLong(worker, true);

		return I18NUtil.getMessage(MSG_SUCCESS);
	}
	
	private boolean olderThanDaysToKeep(NodeRef node) {
		Date archivedDate = (Date) nodeService.getProperty(node, ContentModel.PROP_ARCHIVED_DATE);
		long archivedDateValue = 0;
		if (archivedDate != null) {
			archivedDateValue = archivedDate.getTime();
		}

		ZonedDateTime before = Instant.ofEpochMilli(System.currentTimeMillis()).minus(keepPeriod).atZone(ZoneId.of("UTC"));
		return Instant.ofEpochMilli(archivedDateValue).atZone(ZoneId.of("UTC")).isBefore(before);
	}

}
