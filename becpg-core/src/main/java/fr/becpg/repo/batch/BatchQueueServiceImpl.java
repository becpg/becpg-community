package fr.becpg.repo.batch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;

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

import fr.becpg.repo.audit.model.AuditScope;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.impl.BatchAuditPlugin;
import fr.becpg.repo.audit.service.BeCPGAuditService;
import fr.becpg.repo.mail.BeCPGMailService;

/**
 * <p>BatchQueueServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
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
	@Qualifier("batchThreadPoolExecutorMap")
	private Map<String, ThreadPoolExecutor> threadExecutorMap;

	@Autowired
	private TenantAdminService tenantAdminService;
	
	@Autowired
	private BeCPGAuditService beCPGAuditService;
	
	private BatchMonitor lastRunningBatch;
	
	private AtomicReference<BatchCommand<?>> runningCommand = new AtomicReference<>();

	private Set<String> cancelledBatches = ConcurrentHashMap.newKeySet();
	
	private Deque<BatchCommand<?>> pausedCommands = new ConcurrentLinkedDeque<>();
	
	private static final String CANCELLED = "cancelled";
	private static final String PERCENT_COMPLETED = "percentCompleted";
	private static final String STEPS_MAX = "stepsMax";
	private static final String STEP_COUNT = "stepCount";

	/** {@inheritDoc} */
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
	
	/** {@inheritDoc} */
	@Override
	public <T> Boolean queueBatch(@NonNull BatchInfo batchInfo, @NonNull List<BatchStep<T>> batchSteps) {
		return queueBatch(batchInfo, batchSteps, null);
	}
	
	/** {@inheritDoc} */
	@Override
	public <T> Boolean queueBatch(@NonNull BatchInfo batchInfo, @NonNull List<BatchStep<T>> batchSteps, BatchClosingHook closingHook) {
		
		if (tenantAdminService.isEnabled()) {
			String currentDomain = tenantAdminService.getCurrentUserDomain();
			
			if (!TenantService.DEFAULT_DOMAIN.equals(currentDomain)) {
				batchInfo.setBatchId(batchInfo.getBatchId() + " - " + currentDomain);
			}
		}
		
		cancelledBatches.remove(batchInfo.getBatchId());
		
		Runnable command = new BatchCommand<>(batchInfo, batchSteps, closingHook);
		ThreadPoolExecutor threadPoolExecutor = threadExecutorMap.get(Integer.toString(batchInfo.getPriority()));
		if (!threadPoolExecutor.getQueue().contains(command) && !command.equals(runningCommand.get())) {
			if(logger.isInfoEnabled()) {
				logger.info("Batch " + batchInfo.getBatchId() + " added to execution queue");
			}
			threadPoolExecutor.execute(command);
		} else {
			String label = I18NUtil.getMessage(batchInfo.getBatchDescId(), batchInfo.getEntityDescription());
			logger.warn("Same batch already in queue " + (label != null ? label : batchInfo.getBatchDescId()) + " (" + batchInfo.getBatchId() + ")");
		}
		
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public String getRunningBatchInfo() {
		if (runningCommand.get() != null) {
			return buildJsonBatchInfo(runningCommand.get().batchInfo).toString();
		}
		return null;
	}
	
	private JSONObject buildJsonBatchInfo(BatchInfo batchInfo) throws JSONException {
		JSONObject json = new JSONObject();
			
		String entityDescription = null;
		
		if (batchInfo.getEntityDescription() != null) {
			entityDescription = batchInfo.getEntityDescription();
		}
		
		if (batchInfo.getCurrentStep() != null && batchInfo.getTotalSteps() != null) {
			json.put(STEP_COUNT, batchInfo.getCurrentStep());
			json.put(STEPS_MAX, batchInfo.getTotalSteps());
		}
		
		json.put(BatchInfo.BATCH_ID, batchInfo.getBatchId());
		json.put(BatchInfo.BATCH_USER, batchInfo.getBatchUser());
		
		String descriptionLabel = I18NUtil.getMessage(batchInfo.getBatchDescId(), entityDescription);
		
		if (batchInfo.getStepDescId() != null) {
			descriptionLabel += " - " + I18NUtil.getMessage(batchInfo.getStepDescId());
		}
		
		json.put(BatchInfo.BATCH_DESC_ID, descriptionLabel != null ? descriptionLabel : batchInfo.getBatchDescId());
		
		if (batchInfo.isCancelled()) {
			json.put(CANCELLED, true);
		}
		
		if (pausedCommands.stream().anyMatch(c -> c.getBatchId().equals(batchInfo.getBatchId()))) {
			json.put("paused", true);
		}
		
		if (batchInfo.getCurrentItem() != null && batchInfo.getTotalItems() != null && batchInfo.getTotalItems() != 0) {
			json.put("currentItem", batchInfo.getCurrentItem());
			json.put("totalItems", batchInfo.getTotalItems());
			json.put(PERCENT_COMPLETED, 100 * batchInfo.getCurrentItem() / batchInfo.getTotalItems());
		} else {
			json.put(PERCENT_COMPLETED, 0);
		}
		
		return json;
	}
	
	/** {@inheritDoc} */
	@Override
	public BatchMonitor getLastRunningBatch() {
		return lastRunningBatch;
	}
	
	/** {@inheritDoc} */
	@Override
	public List<String> getBatchesInQueue() {

		List<String> batchInfos = new ArrayList<>();
		
		for (Entry<String, ThreadPoolExecutor> entry : threadExecutorMap.entrySet()) {
			String priority = entry.getKey();
			ThreadPoolExecutor poolExecutor = entry.getValue();
			Iterator<BatchCommand<?>> it = pausedCommands.descendingIterator();
			while (it.hasNext()) {
				BatchCommand<?> pausedBatch = it.next();
				if (Integer.toString(pausedBatch.getBatchInfo().getPriority()).equals(priority)) {
					batchInfos.add(buildJsonBatchInfo(pausedBatch.getBatchInfo()).toString());
					break;
				}
			}
			for (Runnable batch : poolExecutor.getQueue()) {
				if (batch instanceof BatchCommand) {
					batchInfos.add(buildJsonBatchInfo(((BatchCommand<?>) batch).getBatchInfo()).toString());
				}
			}
		}
		
		return batchInfos;

	}

	/** {@inheritDoc} */
	@Override
	public boolean removeBatchFromQueue(String batchId) {
		
		if (pausedCommands.stream().anyMatch(c -> c.getBatchId().equals(batchId))) {
			cancelBatch(batchId);
		}
		
		BatchCommand<?> command = findCommandInQueue(batchId);
		
		if (command != null) {
			return threadExecutorMap.get(Integer.toString(command.getBatchInfo().getPriority())).remove(command);
		}
		
		return false;

	}

	private BatchCommand<?> findCommandInQueue(String batchId) {
		for (ThreadPoolExecutor executor : threadExecutorMap.values()) {
			for (Runnable batch : executor.getQueue()) {
				if ((batch instanceof BatchCommand) && batchId.equals(((BatchCommand<?>) batch).getBatchId())) {
					return (BatchCommand<?>) batch;
				}
			}
		}
		return null;
	}
	
	/** {@inheritDoc} */
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
		private BatchClosingHook closingHook;
		private AuditScope auditScope;
		
		public BatchCommand(BatchInfo batchInfo, List<BatchStep<T>> batchSteps, BatchClosingHook closingHook) {
			super();
			this.batchInfo = batchInfo;
			this.batchId = batchInfo.getBatchId();
			this.batchSteps = batchSteps;
			this.closingHook = closingHook;
		}

		public BatchInfo getBatchInfo() {
			return batchInfo;
		}

		public String getBatchId() {
			return batchId;
		}

		@Override
		public void run() {
			
			if (runningCommand.get() != null) {
				if (runningCommand.get().getBatchInfo().getPriority() < this.getBatchInfo().getPriority()) {
					pausedCommands.push(this);
					if (logger.isInfoEnabled()) {
						logger.info("Batch '" + this.getBatchId() + "' is waiting for '" + runningCommand.get().getBatchId() + "' to finish");
					}
					checkPausedCommand();
				} else {
					pausedCommands.push(runningCommand.get());
					if (logger.isInfoEnabled()) {
						logger.info("Batch '" + runningCommand.get().getBatchId() + "' is paused because '" + this.getBatchId() + "' started");
					}
					runningCommand.set(this);
				}
			} else {
				runningCommand.set(this);
			}
			
			try (AuditScope scope = beCPGAuditService.startAudit(AuditType.BATCH)) {
				boolean hasError = false;

				Date startTime = new Date();
				
				this.auditScope = scope;

				int totalItems = 0;
				int totalErrors = 0;

				auditScope.putAttribute(BatchAuditPlugin.BATCH_USER, batchInfo.getBatchUser());
				auditScope.putAttribute(BatchAuditPlugin.BATCH_ID, batchInfo.getBatchId());
				auditScope.putAttribute(BatchAuditPlugin.IS_COMPLETED, false);

				Integer stepCount = batchSteps.size() > 1 ? 1 : null;

				for (BatchStep<T> batchStep : batchSteps) {

					if (batchStep.getBatchStepListener() != null) {

						pushAndSetBatchAuthentication(batchStep);

						transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
							batchStep.getBatchStepListener().beforeStep();
							return true;
						}, false, true);

						AuthenticationUtil.popAuthentication();

					}
					
					batchInfo.setCurrentItem(0);
					batchInfo.setTotalItems((int) batchStep.getWorkProvider().getTotalEstimatedWorkSizeLong());

					batchInfo.setStepDescId(batchStep.getStepDescId());
					if (stepCount != null) {
						batchInfo.setCurrentStep(stepCount);
						batchInfo.setTotalSteps(batchSteps.size());
						stepCount++;
					}
					
					BatchProcessor<T> batchProcessor = new BatchProcessor<>(batchInfo.toJson().toString(),
							transactionService.getRetryingTransactionHelper(),
							getNextWorkWrapper(batchStep.getWorkProvider()), batchInfo.getWorkerThreads(),
							batchInfo.getBatchSize(), applicationEventPublisher, logger, 100);

					batchProcessor.processLong(runAsWrapper(batchStep), true);

					totalItems += batchProcessor.getTotalResultsLong();
					totalErrors += batchProcessor.getTotalErrorsLong();

					if (batchProcessor.getTotalErrorsLong() > 0 && batchStep.getBatchStepListener() != null) {

						hasError = true;

						AuthenticationUtil
								.runAs(() -> transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
									batchStep.getBatchStepListener().onError(batchProcessor.getLastErrorEntryId(),
											batchProcessor.getLastError());
									return null;

							}, false, true), batchInfo.getBatchUser());

					}
					if (batchStep.getBatchStepListener() != null) {

						pushAndSetBatchAuthentication(batchStep);

						transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
							batchStep.getBatchStepListener().afterStep();
							return true;
						}, false, true);

						AuthenticationUtil.popAuthentication();

					}
				}

				if (closingHook != null) {

					pushAndSetBatchAuthentication(null);

					transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
						closingHook.run();
						return true;
					}, false, true);

					AuthenticationUtil.popAuthentication();

				}

				batchInfo.setIsCompleted(true);

				auditScope.putAttribute(BatchAuditPlugin.TOTAL_ITEMS, totalItems);
				auditScope.putAttribute(BatchAuditPlugin.TOTAL_ERRORS, totalErrors);
				auditScope.putAttribute(BatchAuditPlugin.IS_COMPLETED, true);

				if (Boolean.TRUE.equals(batchInfo.getNotifyByMail())) {

					Date endTime = new Date();
					
					boolean finalHasError = hasError;

					int secondsBetween = (int) ((endTime.getTime() - startTime.getTime()) / 1000);

					AuthenticationUtil
							.runAs(() -> transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

								beCPGMailService.sendMailOnAsyncAction(batchInfo.getBatchUser(),
										batchInfo.getMailAction(), batchInfo.getMailActionUrl(), !finalHasError,
										secondsBetween, batchInfo.getEntityDescription());

								return null;
					}, false, true), batchInfo.getBatchUser());
				}

				batchInfo.setIsCompleted(true);

			} finally {
				if (cancelledBatches.contains(batchId)) {
					cancelledBatches.remove(batchId);
				}
				if (pausedCommands.contains(this)) {
					pausedCommands.remove(this);
				}
				if (runningCommand.get() == this) {
					runningCommand.set(null);
				}
				if (!pausedCommands.isEmpty()) {
					runningCommand.set(pausedCommands.pop());
					if (logger.isInfoEnabled()) {
						logger.info("Resume batch: " + ((BatchCommand<?>) runningCommand.get()).getBatchId());
					}
				}
			}
		}

		private void pushAndSetBatchAuthentication(BatchStep<T> batchStep) {
			AuthenticationUtil.pushAuthentication();
			
			Boolean runAsSystem = batchStep != null && batchStep.getRunAsSystem() != null ? batchStep.getRunAsSystem() : batchInfo.getRunAsSystem();
			String batchUser = batchStep != null && batchStep.getBatchUser() != null ? batchStep.getBatchUser() : batchInfo.getBatchUser();
			
			if (Boolean.TRUE.equals(runAsSystem)) {
				if (tenantAdminService.isEnabled()) {
					if (AuthenticationUtil.getSystemUserName().equals(batchUser)) {
						batchUser = tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), batchInfo.getTenant());
					} else {
						batchUser = tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantAdminService.getUserDomain(batchUser));
					}
				} else {
					batchUser = AuthenticationUtil.getSystemUserName();
				}
			}
			AuthenticationUtil.setFullyAuthenticatedUser(batchUser);
		}

		private BatchProcessWorkProvider<T> getNextWorkWrapper(BatchProcessWorkProvider<T> workProvider) {
			return new BatchProcessWorkProvider<T>() {

				@Override
				public int getTotalEstimatedWorkSize() {
					return (int) workProvider.getTotalEstimatedWorkSizeLong();
				}
				
				@Override
				public long getTotalEstimatedWorkSizeLong() {
					return getTotalEstimatedWorkSize();
				}
				
				@Override
				public Collection<T> getNextWork() {
					return workProvider.getNextWork();
				}
				
			};
		}

		private BatchProcessWorker<T> runAsWrapper(BatchStep<T> batchStep) {
			return new BatchProcessWorker<>() {

				@Override
				public String getIdentifier(T entry) {
					return batchStep.getProcessWorker().getIdentifier(entry);
				}

				@Override
				public void beforeProcess() throws Throwable {
					pushAndSetBatchAuthentication(batchStep);
					batchStep.getProcessWorker().beforeProcess();
				}

				@Override
				public void process(T entry) throws Throwable {
					if (cancelledBatches.contains(batchId)) {
						if (logger.isDebugEnabled()) {
							logger.debug("Skip entry '" + entry + "' as batch : '" + batchId + "' was cancelled");
						}
						BatchCommand.this.getBatchInfo().setCancelled(true);
						auditScope.disable();
						return;
					}
					checkPausedCommand();
					batchStep.getProcessWorker().process(entry);
					batchInfo.setCurrentItem(batchInfo.getCurrentItem() + 1);
				}


				@Override
				public void afterProcess() throws Throwable {
					batchStep.getProcessWorker().afterProcess();
					AuthenticationUtil.popAuthentication();
				}

			};
		}

		private void checkPausedCommand() {
			while (pausedCommands.contains(this) && !cancelledBatches.contains(this.getBatchId())) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					logger.error("error while pausing command", e);
				}
			}
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
			
			return Objects.equals(batchId, other.batchId) && Objects.equals(batchInfo.getEntityDescription(), other.batchInfo.getEntityDescription());
		}

		private BatchQueueServiceImpl getEnclosingInstance() {
			return BatchQueueServiceImpl.this;
		}

	}

	/** {@inheritDoc} */
	@Override
	public void onApplicationEvent(BatchMonitorEvent event) {
		if (event.getBatchMonitor().getProcessName() != null && event.getBatchMonitor().getProcessName().contains("batchId")) {
			lastRunningBatch = event.getBatchMonitor();
		}
	}
	
}
