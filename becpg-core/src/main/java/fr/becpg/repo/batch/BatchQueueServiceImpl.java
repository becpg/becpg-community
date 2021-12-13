package fr.becpg.repo.batch;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.Nullable;

import org.alfresco.repo.batch.BatchMonitor;
import org.alfresco.repo.batch.BatchMonitorEvent;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import fr.becpg.repo.mail.BeCPGMailService;

@Service("batchQueueService")
public class BatchQueueServiceImpl implements BatchQueueService, ApplicationListener<BatchMonitorEvent> {

	private static Log logger = LogFactory.getLog(BatchQueueServiceImpl.class);

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private BeCPGMailService beCPGMailService;

	@Autowired
	@Qualifier("batchThreadPoolExecutor")
	private ThreadPoolExecutor threadExecuter;

	@Autowired
	private TenantAdminService tenantAdminService;

	private BatchMonitor lastRunningBatch;

	@Override
	public <T> Boolean queueBatch(@NonNull BatchInfo batchInfo, @NonNull BatchProcessWorkProvider<T> workProvider,
			@NonNull BatchProcessWorker<T> processWorker, @Nullable BatchErrorCallback errorCallback) {
		logger.info("Batch " + batchInfo.getBatchId() + " added to execution queue");

		Runnable command = new BatchCommand<>(batchInfo, workProvider, processWorker, errorCallback);
		if (!threadExecuter.getQueue().contains(command)) {
			threadExecuter.execute(command);
		} else {
			String label = I18NUtil.getMessage(batchInfo.getBatchDescId());
			logger.warn("Same batch already in queue " + (label != null ? label : batchInfo.getBatchDescId()) + " (" + batchInfo.getBatchId() + ")");
		}

		return false;
	}

	@Override
	public BatchMonitor getLastRunningBatch() {
		return lastRunningBatch;
	}

	@Override
	public List<BatchInfo> getBatchesInQueue() {

		List<BatchInfo> batchInfos = new LinkedList<>();

		for (Runnable batch : threadExecuter.getQueue()) {
			if (batch instanceof BatchCommand) {
				batchInfos.add(((BatchCommand<?>) batch).getBatchInfo());
			}
		}

		return batchInfos;

	}

	@Override
	public boolean removeBatchFromQueue(String batchId) {
		BatchCommand<?> command = null;
		for (Runnable batch : threadExecuter.getQueue()) {
			if ((batch instanceof BatchCommand) && batchId.equals(((BatchCommand<?>) batch).getBatchId())) {
				command = (BatchCommand<?>) batch;
				break;
			}
		}

		if (command != null) {
			return threadExecuter.remove(command);
		}
		return false;

	}

	private class BatchCommand<T> implements Runnable {

		private String batchId;
		private BatchInfo batchInfo;
		private BatchProcessWorkProvider<T> workProvider;
		private BatchProcessWorker<T> processWorker;
		private BatchErrorCallback errorCallback;

		public BatchCommand(BatchInfo batchInfo, BatchProcessWorkProvider<T> workProvider, BatchProcessWorker<T> processWorker,
				BatchErrorCallback errorCallback) {
			super();
			this.batchInfo = batchInfo;
			this.batchId = batchInfo.getBatchId();
			this.workProvider = workProvider;
			this.processWorker = processWorker;
			this.errorCallback = errorCallback;
		}

		public BatchInfo getBatchInfo() {
			return batchInfo;
		}

		public String getBatchId() {
			return batchId;
		}

		@Override
		public void run() {

			BatchProcessor<T> batchProcessor = new BatchProcessor<>(batchInfo.getBatchDescId(), transactionService.getRetryingTransactionHelper(),
					workProvider, batchInfo.getWorkerThreads(), batchInfo.getBatchSize(), applicationEventPublisher, logger, 100);

			batchProcessor.process(runAsWrapper(processWorker), true);

			boolean hasError = batchProcessor.getTotalErrors() > 0;

			if (hasError && (this.errorCallback != null)) {

				AuthenticationUtil.runAs(() -> {
					return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

						this.errorCallback.run(batchProcessor.getLastErrorEntryId(), batchProcessor.getLastError());

						return null;
					}, true, false);
				}, batchInfo.getBatchUser());

			}

			if (Boolean.TRUE.equals(batchInfo.getNotifyByMail())) {

				AuthenticationUtil.runAs(() -> {
					return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

						beCPGMailService.sendMailOnAsyncAction(batchInfo.getBatchUser(), batchInfo.getMailAction(), batchInfo.getMailActionUrl(),
								hasError, batchProcessor.getEndTime().compareTo(batchProcessor.getStartTime()));

						return null;
					}, true, false);
				}, batchInfo.getBatchUser());
			}

			batchInfo.setIsCompleted(true);

		}

		private BatchProcessWorker<T> runAsWrapper(BatchProcessWorker<T> processWorker) {
			return new BatchProcessWorker<>() {

				@Override
				public String getIdentifier(T entry) {
					return processWorker.getIdentifier(entry);
				}

				@Override
				public void beforeProcess() throws Throwable {
					AuthenticationUtil.pushAuthentication();

					String username = batchInfo.getBatchUser();
					if (Boolean.TRUE.equals(batchInfo.getRunAsSystem())) {

						username = AuthenticationUtil.getSystemUserName();
						if (tenantAdminService.isEnabled()) {
							username = tenantAdminService.getDomainUser(username, tenantAdminService.getUserDomain(batchInfo.getBatchUser()));

						}

					}
					AuthenticationUtil.setFullyAuthenticatedUser(username);

					processWorker.beforeProcess();

				}

				@Override
				public void process(T entry) throws Throwable {

					processWorker.process(entry);
				}

				@Override
				public void afterProcess() throws Throwable {
					processWorker.afterProcess();
					AuthenticationUtil.popAuthentication();
				}

			};
		}

		/*
		 * Is important to keep only batchId in equals method
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = (prime * result) + getEnclosingInstance().hashCode();
			result = (prime * result) + Objects.hash(batchId);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if ((obj == null) || (getClass() != obj.getClass())) {
				return false;
			}
			BatchCommand<?> other = (BatchCommand<?>) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance())) {
				return false;
			}
			return Objects.equals(batchId, other.batchId);
		}

		private BatchQueueServiceImpl getEnclosingInstance() {
			return BatchQueueServiceImpl.this;
		}

	}

	@Override
	public void onApplicationEvent(BatchMonitorEvent event) {
		lastRunningBatch = event.getBatchMonitor();
	}

}
