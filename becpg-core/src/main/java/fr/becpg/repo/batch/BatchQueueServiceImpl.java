package fr.becpg.repo.batch;

import java.util.ArrayList;
import java.util.Date;
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
		if(logger.isDebugEnabled()) {
			logger.debug("Batch " + batchInfo.getBatchId() + " added to execution queue");
		}

		List<BatchStep<T>> batchSteps = new ArrayList<>();
		
		BatchStep<T> batchStep = new BatchStep<>();
		
		batchStep.setWorkProvider(workProvider);
		batchStep.setProcessWorker(processWorker);
		
		BatchStepListener batchStepListener = new BatchStepAdapter() {
			@Override
			public void onError(String lastErrorEntryId, String lastError) {
				errorCallback.run(lastErrorEntryId, lastError);
			}
		};
		
		batchStep.setBatchStepListener(batchStepListener);
		
		batchSteps.add(batchStep);
		
		Runnable command = new BatchCommand<>(batchInfo, batchSteps);
		
		if (!threadExecuter.getQueue().contains(command)) {
			threadExecuter.execute(command);
		} else {
			String label = I18NUtil.getMessage(batchInfo.getBatchDescId());
			logger.warn("Same batch already in queue " + (label != null ? label : batchInfo.getBatchDescId()) + " (" + batchInfo.getBatchId() + ")");
		}

		return false;
	}
	
	@Override
	public <T> Boolean queueBatch(@NonNull BatchInfo batchInfo, @NonNull List<BatchStep<T>> batchSteps) {
		if(logger.isDebugEnabled()) {
			logger.debug("Batch " + batchInfo.getBatchId() + " added to execution queue");
		}
		
		Runnable command = new BatchCommand<>(batchInfo, batchSteps);
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

				BatchProcessor<T> batchProcessor = new BatchProcessor<>(batchInfo.getBatchId(),
						transactionService.getRetryingTransactionHelper(), batchStep.getWorkProvider(),
						batchInfo.getWorkerThreads(), batchInfo.getBatchSize(), applicationEventPublisher, logger, 100);

				batchProcessor.processLong(runAsWrapper(batchStep.getProcessWorker()), true);

				if (startTime == null) {
					startTime = batchProcessor.getStartTime();
				}
				
				endTime = batchProcessor.getEndTime();
				
				if (batchProcessor.getTotalErrorsLong() > 0 && batchStep.getBatchStepListener() != null) {

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
			}
			
			if (Boolean.TRUE.equals(batchInfo.getNotifyByMail())) {

				final boolean finalHasError = hasError;
				final Date finalEndTime = endTime;
				final Date finalStartTime = startTime;
				
				AuthenticationUtil.runAs(() -> {
					return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

						beCPGMailService.sendMailOnAsyncAction(batchInfo.getBatchUser(), batchInfo.getMailAction(), batchInfo.getMailActionUrl(),
								finalHasError, finalEndTime.compareTo(finalStartTime));

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
