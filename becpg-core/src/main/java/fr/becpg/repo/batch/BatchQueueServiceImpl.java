package fr.becpg.repo.batch;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.Nullable;

import org.alfresco.repo.batch.BatchMonitor;
import org.alfresco.repo.batch.BatchMonitorEvent;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
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

	private static final Log logger = LogFactory.getLog(BatchQueueServiceImpl.class);

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private BeCPGMailService beCPGMailService;

	@Autowired
	@Qualifier("batchThreadPoolExecutor")
	private ThreadPoolExecutor threadExecutor;

	@Autowired
	private TenantAdminService tenantAdminService;

	private BatchMonitor lastRunningBatch;

	private Set<String> cancelledBatches = new HashSet<>();

	@Override
	public <T> Boolean queueBatch(@NonNull BatchInfo batchInfo, @NonNull BatchProcessWorkProvider<T> workProvider,
			@NonNull BatchProcessWorker<T> processWorker, @Nullable BatchErrorCallback errorCallback) {

		BatchStep<T> batchStep = new BatchStep<>();

		batchStep.setWorkProvider(workProvider);
		batchStep.setProcessWorker(processWorker);

		BatchStepListener batchStepListener = new BatchStepAdapter() {
			@Override
			public void onError(String lastErrorEntryId, String lastError) {
				if (errorCallback != null) {
					errorCallback.run(lastErrorEntryId, lastError);
				}
			}
		};

		batchStep.setBatchStepListener(batchStepListener);

		return queueBatch(batchInfo, Arrays.asList(batchStep));

	}

	@Override
	public <T> Boolean queueBatch(@NonNull BatchInfo batchInfo, @NonNull List<BatchStep<T>> batchSteps) {

		if (tenantAdminService.isEnabled()) {
			String currentDomain = tenantAdminService.getCurrentUserDomain();

			if (!TenantService.DEFAULT_DOMAIN.equals(currentDomain)) {
				batchInfo.setBatchId(batchInfo.getBatchId() + " - " + currentDomain);
			}
		}

		cancelledBatches.remove(batchInfo.getBatchId());

		Runnable command = new BatchCommand<>(batchInfo, batchSteps);
		if (!threadExecutor.getQueue().contains(command)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Batch " + batchInfo.getBatchId() + " added to execution queue");
			}
			threadExecutor.execute(command);
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

		for (Runnable batch : threadExecutor.getQueue()) {
			if (batch instanceof BatchCommand) {
				batchInfos.add(((BatchCommand<?>) batch).getBatchInfo());
			}
		}

		return batchInfos;

	}

	@Override
	public boolean removeBatchFromQueue(String batchId) {

		BatchCommand<?> command = findCommandInQueue(batchId);

		if (command != null) {
			return threadExecutor.remove(command);
		}

		return false;

	}

	private BatchCommand<?> findCommandInQueue(String batchId) {
		for (Runnable batch : threadExecutor.getQueue()) {
			if ((batch instanceof BatchCommand) && batchId.equals(((BatchCommand<?>) batch).getBatchId())) {
				return (BatchCommand<?>) batch;
			}
		}
		return null;
	}

	@Override
	public boolean cancelBatch(String batchId) {

		if (findCommandInQueue(batchId) == null) {
			return cancelledBatches.add(batchId);
		}

		return false;
	}

	public class BatchCommand<T> implements Runnable {

		private String batchId;
		private BatchInfo batchInfo;
		private List<BatchStep<T>> batchSteps;

		public BatchCommand(BatchInfo batchInfo, List<BatchStep<T>> batchSteps) {
			super();
			this.batchInfo = batchInfo;
			this.batchId = batchInfo.getBatchId();
			this.batchSteps = batchSteps;
		}

		public BatchInfo getBatchInfo() {
			return batchInfo;
		}

		public String getBatchId() {
			return batchId;
		}

		@Override
		public void run() {

			boolean hasError = false;

			Date startTime = null;
			Date endTime = null;

			for (BatchStep<T> batchStep : batchSteps) {
				try {
					if (batchStep.getBatchStepListener() != null) {

						AuthenticationUtil.pushAuthentication();

						String username = batchInfo.getBatchUser();
						if (Boolean.TRUE.equals(batchInfo.getRunAsSystem())) {

							username = AuthenticationUtil.getSystemUserName();
							if (tenantAdminService.isEnabled()) {
								username = tenantAdminService.getDomainUser(username, tenantAdminService.getUserDomain(batchInfo.getBatchUser()));

							}

						}
						AuthenticationUtil.setFullyAuthenticatedUser(username);

						transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
							batchStep.getBatchStepListener().beforeStep();
							return true;
						}, false, true);

						AuthenticationUtil.popAuthentication();

					}

					JSONObject jsonBatch = new JSONObject();

					jsonBatch.put("batchId", batchInfo.getBatchId());
					jsonBatch.put("batchDescId", batchInfo.getBatchDescId());
					jsonBatch.put("batchUser", batchInfo.getBatchUser());
					jsonBatch.put("entityDescription", batchInfo.getEntityDescription());

					BatchProcessor<T> batchProcessor = new BatchProcessor<>(jsonBatch.toString(), transactionService.getRetryingTransactionHelper(),
							getNextWorkwrapper(batchStep.getWorkProvider()), batchInfo.getWorkerThreads(), batchInfo.getBatchSize(),
							applicationEventPublisher, logger, 100);

					batchProcessor.process(runAsWrapper(batchStep.getProcessWorker()), true);

					if (startTime == null) {
						startTime = batchProcessor.getStartTime();
					}

					endTime = batchProcessor.getEndTime();

					if (batchProcessor.getTotalErrors() > 0 && batchStep.getBatchStepListener() != null) {

						hasError = true;

						AuthenticationUtil.runAs(() -> transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
							batchStep.getBatchStepListener().onError(batchProcessor.getLastErrorEntryId(), batchProcessor.getLastError());
							return null;

						}, true, false), batchInfo.getBatchUser());

					}
					if (batchStep.getBatchStepListener() != null) {

						AuthenticationUtil.pushAuthentication();

						String username = batchInfo.getBatchUser();
						if (Boolean.TRUE.equals(batchInfo.getRunAsSystem())) {

							username = AuthenticationUtil.getSystemUserName();
							if (tenantAdminService.isEnabled()) {
								username = tenantAdminService.getDomainUser(username, tenantAdminService.getUserDomain(batchInfo.getBatchUser()));

							}

						}
						AuthenticationUtil.setFullyAuthenticatedUser(username);

						transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
							batchStep.getBatchStepListener().afterStep();
							return true;
						}, false, true);

						AuthenticationUtil.popAuthentication();

					}

				} catch (JSONException e) {
					logger.error("Failed to fill JSON information", e);
				}

			}

			if (Boolean.TRUE.equals(batchInfo.getNotifyByMail())) {

				final boolean finalHasError = hasError;
				final Date finalEndTime = endTime;
				final Date finalStartTime = startTime;

				int secondsBetween = (int) ((finalEndTime.getTime() - finalStartTime.getTime()) / 1000);

				AuthenticationUtil.runAs(() -> transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

					beCPGMailService.sendMailOnAsyncAction(batchInfo.getBatchUser(), batchInfo.getMailAction(), batchInfo.getMailActionUrl(),
							!finalHasError, secondsBetween, batchInfo.getEntityDescription());

					return null;
				}, true, false), batchInfo.getBatchUser());
			}

			batchInfo.setIsCompleted(true);

			cancelledBatches.remove(batchId);

		}

		private BatchProcessWorkProvider<T> getNextWorkwrapper(BatchProcessWorkProvider<T> workProvider) {
			return new BatchProcessWorkProvider<T>() {

				@Override
				public int getTotalEstimatedWorkSize() {
					return workProvider.getTotalEstimatedWorkSize();
				}

				@Override
				public Collection<T> getNextWork() {
					if (cancelledBatches.contains(batchId)) {
						if (logger.isDebugEnabled()) {
							logger.debug("Stop providing next work for batch '" + batchId + "' as it was cancelled");
						}
						return Collections.emptyList();
					}
					return workProvider.getNextWork();
				}

			};
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

					if (cancelledBatches.contains(batchId)) {
						if (logger.isDebugEnabled()) {
							logger.debug("Skip entry '" + entry + "' as batch : '" + batchId + "' was cancelled");
						}
						return;
					}

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
		if (event.getBatchMonitor().getProcessName() != null && event.getBatchMonitor().getProcessName().contains("batchId")) {
			lastRunningBatch = event.getBatchMonitor();
		}
	}

}