package fr.becpg.repo.batch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchMonitor;
import org.alfresco.repo.batch.BatchMonitorEvent;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorkerAdaptor;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
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

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.audit.model.AuditScope;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.AuditPlugin;
import fr.becpg.repo.audit.plugin.impl.BatchAuditPlugin;
import fr.becpg.repo.audit.service.BeCPGAuditService;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.helper.SiteHelper;
import fr.becpg.repo.helper.json.JsonData;
import fr.becpg.repo.helper.json.JsonHelper;
import fr.becpg.repo.mail.BeCPGMailService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>BatchQueueServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("batchQueueService")
@SuppressWarnings("deprecation")
public class BatchQueueServiceImpl implements BatchQueueService, ApplicationListener<BatchMonitorEvent> {

	/** Constant <code>logger</code> */
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
	
	@Autowired
	private BeCPGCacheService beCPGCacheService;
	
	@Autowired
	private NodeService nodeService;

	@Autowired
	private NamespaceService namespaceService;
	
	@Autowired
	private BehaviourFilter policyBehaviourFilter;
	
	private BatchMonitor lastRunningBatch;
	
	private List<BatchCommand<?>> runningCommands = new CopyOnWriteArrayList<>();

	private Set<String> cancelledBatches = ConcurrentHashMap.newKeySet();
	
	private Deque<BatchCommand<?>> pausedCommands = new ConcurrentLinkedDeque<>();
	
	private Map<String, Set<NodeRef>> discardedEntries = new ConcurrentHashMap<>();
	
	private ReentrantLock batchQueueLock = new ReentrantLock();

	@Autowired(required = false)
	private BatchQueuePlugin[] batchQueuePlugins;
	
	/** Constant <code>CANCELLED="cancelled"</code> */
	private static final String CANCELLED = "cancelled";
	/** Constant <code>PERCENT_COMPLETED="percentCompleted"</code> */
	private static final String PERCENT_COMPLETED = "percentCompleted";
	/** Constant <code>STEPS_MAX="stepsMax"</code> */
	private static final String STEPS_MAX = "stepsMax";
	/** Constant <code>STEP_COUNT="stepCount"</code> */
	private static final String STEP_COUNT = "stepCount";
	/** Constant <code>MAX_DISCARDED_ENTRIES=1000</code> */
	private static final int MAX_DISCARDED_ENTRIES = 1000;

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
		if (isBatchInQueue(batchInfo)) {
			String label = I18NUtil.getMessage(batchInfo.getBatchDescId(), batchInfo.getEntityDescription());
			logger.warn("Same batch already in queue " + (label != null ? label : batchInfo.getBatchDescId()) + " (" + batchInfo.getBatchId() + ")");
		} else {
			if(logger.isInfoEnabled()) {
				logger.info("Batch " + batchInfo.getBatchId() + " added to execution queue");
			}
			threadPoolExecutor.execute(command);
			return true;
		}
		
		return false;
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean isBatchInQueue(BatchInfo batchInfo) {
		String batchId = batchInfo.getBatchId();
		if (runningCommands.stream().anyMatch(c -> c.getBatchId().equals(batchId))) {
			logger.debug("Batch is running: " + batchId);
			return true;
		}
		if (pausedCommands.stream().anyMatch(c -> c.getBatchId().equals(batchId))) {
			logger.debug("Batch is paused: " + batchId);
			return true;
		}
		ThreadPoolExecutor threadPoolExecutor = threadExecutorMap.get(Integer.toString(batchInfo.getPriority()));
		if (threadPoolExecutor.getQueue().stream().map(c -> (BatchCommand<?>) c).anyMatch(c -> c.getBatchId().equals(batchId))) {
			logger.debug("Batch is in queue: " + batchId);
			return true;
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isBatchCompleted(BatchInfo batchInfo) {
		if (!Boolean.TRUE.equals(batchInfo.getIsCompleted())) {
			return false;
		}
		String batchId = batchInfo.getBatchId();
		return runningCommands.stream().noneMatch(c -> c.getBatchId().equals(batchId));
	}

	/** {@inheritDoc} */
	@Override
	public String getRunningBatchInfo() {
    BatchCommand<?> current = getRunningCommand();
    if (current != null) {
      BatchInfo info = current.getBatchInfo();
      // If the batch has already completed but cleanup hasn't cleared runningCommand yet,
      // report no running batch to avoid transient race conditions in tests/clients.
      if (Boolean.TRUE.equals(info.getIsCompleted())) {
        return null;
      }
      return buildJsonBatchInfo(info).toString();
    }
    return null;
  }
	
	/**
	 * <p>buildJsonBatchInfo.</p>
	 *
	 * @param batchInfo a {@link fr.becpg.repo.batch.BatchInfo} object
	 * @return a {@link org.json.JSONObject} object
	 * @throws org.json.JSONException if any.
	 */
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
	
	/**
	 * <p>getRunningCommand.</p>
	 *
	 * @return a {@link fr.becpg.repo.batch.BatchQueueServiceImpl.BatchCommand} object
	 */
	private BatchCommand<?> getRunningCommand() {
		if (runningCommands.isEmpty()) {
			return null;
		}
		return runningCommands.get(runningCommands.size() - 1);
	}
	
	/**
	 * <p>pollNextPausedCommand.</p>
	 *
	 * Removes and returns the paused batch that must run next: the most prioritary one, and among
	 * batches of equal priority the one paused first, so that a repeatedly preempted batch cannot be
	 * starved by more recently paused ones.
	 *
	 * @return a {@link fr.becpg.repo.batch.BatchQueueServiceImpl.BatchCommand} object
	 */
	private BatchCommand<?> pollNextPausedCommand() {
		BatchCommand<?> nextCommand = null;
		
		for (BatchCommand<?> pausedCommand : pausedCommands) {
			if ((nextCommand == null) || (pausedCommand.getBatchInfo().getPriority() < nextCommand.getBatchInfo().getPriority())) {
				nextCommand = pausedCommand;
			}
		}
		
		if (nextCommand != null) {
			pausedCommands.remove(nextCommand);
		}
		
		return nextCommand;
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

	/**
	 * <p>findCommandInQueue.</p>
	 *
	 * @param batchId a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.batch.BatchQueueServiceImpl.BatchCommand} object
	 */
	private BatchCommand<?> findCommandInQueue(String batchId) {
		for (ThreadPoolExecutor executor : threadExecutorMap.values()) {
			for (Runnable batch : executor.getQueue()) {
				if (batch instanceof BatchCommand<?> batchCommand && batchId.equals(batchCommand.getBatchId())) {
					return batchCommand;
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
			
			pushAndSetBatchAuthentication(null);
			
			batchQueueLock.lock();
			try {
				BatchCommand<?> currentRunningCommand = getRunningCommand();
				if (currentRunningCommand != null) {
					if (currentRunningCommand.getBatchInfo().getPriority() < this.getBatchInfo().getPriority()) {
						pauseCommand(this);
						if (logger.isInfoEnabled()) {
							logger.info("Batch '" + this.getBatchId() + "' is waiting for '" + currentRunningCommand.getBatchId() + "' to finish");
						}
					} else {
						pauseCommand(currentRunningCommand);
						if (logger.isInfoEnabled()) {
							logger.info("Batch '" + currentRunningCommand.getBatchId() + "' is paused because '" + this.getBatchId() + "' started");
						}
						runningCommands.add(this);
					}
				} else {
					runningCommands.add(this);
				}
			} finally {
				batchQueueLock.unlock();
			}
			
			try (AuditScope scope = beCPGAuditService.startAudit(AuditType.BATCH)) {
				boolean hasError = false;

				Date startTime = new Date();
				
				this.auditScope = scope;

				int totalItems = 0;
				int totalErrors = 0;

				auditScope.putAttribute(BatchAuditPlugin.BATCH_USER, batchInfo.getBatchUser());
				auditScope.putAttribute(BatchAuditPlugin.BATCH_ID, batchInfo.getBatchId());
				auditScope.putAttribute(AuditPlugin.IS_COMPLETED, false);

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
					
					StepOutcome outcome = Boolean.FALSE.equals(batchStep.getTransactional()) ? runStepOutsideTransaction(batchStep)
							: runStepInTransaction(batchStep);

					totalItems += outcome.items();
					totalErrors += outcome.errors();

					if (outcome.errors() > 0) {
						hasError = true;
						if (batchStep.getBatchStepListener() != null) {
							AuthenticationUtil
							.runAs(() -> transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
								batchStep.getBatchStepListener().onError(outcome.lastErrorEntryId(),
										outcome.lastError());
								return null;
								
							}, false, true), batchInfo.getBatchUser());
						}
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
					transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
						closingHook.run();
						return true;
					}, false, true);
				}

				batchInfo.setIsCompleted(true);

				auditScope.putAttribute(BatchAuditPlugin.TOTAL_ITEMS, totalItems);
				auditScope.putAttribute(BatchAuditPlugin.TOTAL_ERRORS, totalErrors);
				auditScope.putAttribute(AuditPlugin.IS_COMPLETED, true);

				if (Boolean.TRUE.equals(batchInfo.getNotifyByMail())) {

					Date endTime = new Date();
					
					boolean finalHasError = hasError;

					int secondsBetween = (int) ((endTime.getTime() - startTime.getTime()) / 1000);

					logger.info("batch '" + batchInfo.getBatchId() + "' took " + secondsBetween + " seconds to complete");
					
					AuthenticationUtil
							.runAs(() -> transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

								beCPGMailService.sendMailOnAsyncAction(batchInfo.getBatchUser(),
										batchInfo.getMailAction(), batchInfo.getMailActionUrl(), !finalHasError,
										secondsBetween, batchInfo.getEntityDescription());

								return null;
					}, false, true), batchInfo.getBatchUser());
				}

			} finally {
				batchQueueLock.lock();
				try {
					if (cancelledBatches.contains(batchId)) {
						cancelledBatches.remove(batchId);
					}
					if (pausedCommands.contains(this)) {
						pausedCommands.remove(this);
					}
					if (runningCommands.contains(this)) {
						runningCommands.remove(this);
					}
					if (!pausedCommands.isEmpty()) {
						BatchCommand<?> nextCommand = pollNextPausedCommand();
						if (nextCommand != null) {
							if (cancelledBatches.contains(nextCommand.getBatchId())) {
								// The cancellation stays registered: the worker of the batch reads it to skip
								// the entries it has left, and clears it itself once the batch has ended.
								nextCommand.getBatchInfo().setCancelled(true);
								if (logger.isInfoEnabled()) {
									logger.info("Cancelled paused batch: " + nextCommand.getBatchId());
								}
							} else if (logger.isInfoEnabled()) {
								logger.info("Resume batch: " + nextCommand.getBatchId());
							}
							runningCommands.remove(nextCommand);
							runningCommands.add(nextCommand);
						}
					}
				} finally {
					batchQueueLock.unlock();
				}
			}
		}

		/**
		 * Moves a command to the paused queue. A paused command leaves the running list, so that
		 * the running list only ever names the batch that is actually progressing, and it is never
		 * queued twice, so that its own completion is enough to clear it from the paused queue.
		 */
		private void pauseCommand(BatchCommand<?> command) {
			runningCommands.remove(command);
			if (!pausedCommands.contains(command)) {
				pausedCommands.addLast(command);
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

		/** What a step reported, whichever way it was run. */
		private record StepOutcome(long items, long errors, String lastErrorEntryId, String lastError) {
		}

		/** The default: Alfresco's BatchProcessor opens a transaction around every entry. */
		private StepOutcome runStepInTransaction(BatchStep<T> batchStep) {
			BatchProcessor<T> batchProcessor = new BatchProcessor<>(batchInfo.toJson().toString(),
					transactionService.getRetryingTransactionHelper(),
					getNextWorkWrapper(batchStep.getWorkProvider()), batchInfo.getWorkerThreads(),
					batchInfo.getBatchSize(), applicationEventPublisher, logger, 100);

			batchProcessor.processLong(runAsWrapper(batchStep), true);

			return new StepOutcome(batchProcessor.getTotalResultsLong(), batchProcessor.getTotalErrorsLong(),
					batchProcessor.getLastErrorEntryId(), batchProcessor.getLastError());
		}

		/**
		 * Runs the step without opening a transaction around it, for work that waits
		 * on something other than the database and must not hold a transaction while
		 * it does. The worker takes care of its own transactions.
		 *
		 * Everything the transactional path offers is kept: the same wrapper, so the
		 * same authentication, pause and cancellation handling and the same progress
		 * counter; one entry at a time, so a pause is honoured between entries; and an
		 * entry that throws is counted and logged without stopping the rest, as
		 * BatchProcessor does.
		 */
		private StepOutcome runStepOutsideTransaction(BatchStep<T> batchStep) {
			BatchProcessWorker<T> worker = runAsWrapper(batchStep);
			BatchProcessWorkProvider<T> workProvider = getNextWorkWrapper(batchStep.getWorkProvider());

			long items = 0;
			long errors = 0;
			String lastErrorEntryId = null;
			String lastError = null;

			for (Collection<T> batch = workProvider.getNextWork(); (batch != null) && !batch.isEmpty(); batch = workProvider
					.getNextWork()) {
				for (T entry : batch) {
					try {
						worker.beforeProcess();
						try {
							worker.process(entry);
							items++;
						} finally {
							worker.afterProcess();
						}
					} catch (Throwable t) { //NOSONAR - an entry must not take the batch down with it
						errors++;
						lastErrorEntryId = worker.getIdentifier(entry);
						lastError = asReportedByBatchProcessor(t);
						logger.error("Batch '" + batchId + "' failed on entry '" + lastErrorEntryId + "'", t);
					}
				}
			}

			return new StepOutcome(items, errors, lastErrorEntryId, lastError);
		}

		/** Same shape as BatchProcessor.getLastError, so listeners see one thing. */
		private String asReportedByBatchProcessor(Throwable t) {
			StringWriter buff = new StringWriter(1024);
			try (PrintWriter out = new PrintWriter(buff)) {
				t.printStackTrace(out);
			}
			return buff.toString();
		}

		private BatchProcessWorker<T> runAsWrapper(BatchStep<T> batchStep) {
			return new BatchProcessWorker<>() {

				@Override
				public String getIdentifier(T entry) {
					return batchStep.getProcessWorker().getIdentifier(entry);
				}

				@Override
				public void beforeProcess() throws Throwable {
					checkPausedCommand();
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
			try {
                while (pausedCommands.contains(this)) {
                    if (cancelledBatches.contains(this.getBatchId())) {
                    	pausedCommands.remove(this);
                        break;
                    }
                    Thread.sleep(1000);
                }
                if (cancelledBatches.contains(this.getBatchId())) {
                	BatchCommand.this.getBatchInfo().setCancelled(true);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
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
			
			return Objects.equals(batchId, other.batchId);
		}

		private BatchQueueServiceImpl getEnclosingInstance() {
			return BatchQueueServiceImpl.this;
		}

	}
	
	/** {@inheritDoc} */
	@Override
	public String getBatchesInError() {
		Map<String, Set<NodeRef>> batchNodesMap = getBatchErrorsMap();
	    JsonData result = JsonHelper.createJsonArray();
	    
	    for (Map.Entry<String, Set<NodeRef>> entry : batchNodesMap.entrySet()) {
	    	JsonData json = JsonHelper.createJsonObject();
	    	String batchErrorId = entry.getKey();
	    	String[] split = batchErrorId.split("\\|");
	    	String batchShortId = split[0];
	    	String batchDescKey = split.length > 1 ? split[1] : batchErrorId;
	    	String batchDesc = I18NUtil.getMessage(batchDescKey);
	    	if (batchDesc == null || batchDesc.isBlank()) {
	    		batchDesc = batchDescKey;
	    	}
	    	json.put("batchId", batchErrorId);
	    	json.put("batchShortId", batchShortId);
	    	json.put("batchDesc", batchDesc);
	    	json.put("numberOfNodes", entry.getValue().size());
	    	result.put(json);
	    }
	    
	    return result.toString();
	}

	/**
	 * <p>getBatchErrorsMap.</p>
	 *
	 * @return a {@link java.util.Map} object
	 */
	private Map<String, Set<NodeRef>> getBatchErrorsMap() {
		return beCPGCacheService.getFromCache(
	        BatchQueueServiceImpl.class.getName(),
	        "batchesInError",
	        () -> {
	            Map<String, Set<NodeRef>> map = new HashMap<>();
	            processBatchErrors(
	                BeCPGQueryBuilder.createQuery()
	                    .withAspect(BeCPGModel.ASPECT_BATCH_ERROR)
	                    .maxResults(RepoConsts.MAX_RESULTS_UNLIMITED)
	                    .list(),
	                map
	            );
	            processBatchErrors(
	                BeCPGQueryBuilder.createQuery()
	                    .withAspect(BeCPGModel.ASPECT_BATCH_ERROR)
	                    .inStore(RepoConsts.VERSION_STORE)
	                    .maxResults(RepoConsts.MAX_RESULTS_UNLIMITED)
	                    .list(),
	                map
	            );
	            return map;
	        }
	    );
	}

	/**
	 * <p>processBatchErrors.</p>
	 *
	 * @param nodeRefs a {@link java.util.List} object
	 * @param batchNodesMap a {@link java.util.Map} object
	 */
	@SuppressWarnings("unchecked")
	private void processBatchErrors(
	        List<NodeRef> nodeRefs,
	        Map<String, Set<NodeRef>> batchNodesMap) {

	    for (NodeRef nodeRef : nodeRefs) {
	        List<String> batchErrorIds = (List<String>) nodeService.getProperty(nodeRef, BeCPGModel.PROP_BATCH_ERROR_IDS);
	        if (batchErrorIds != null && !batchErrorIds.isEmpty()) {
	            for (String batchErrorId : batchErrorIds) {
	                batchNodesMap.computeIfAbsent(batchErrorId, k -> new HashSet<>()).add(nodeRef);
	            }
	        } else {
	            String errorLogs = (String) nodeService.getProperty(nodeRef, BeCPGModel.PROP_BATCH_ERROR_LOGS);
	            if (errorLogs != null && !errorLogs.isBlank()) {
	                try {
	                    JsonData jsonData = JsonHelper.read(errorLogs);
	                    Iterator<String> fieldNames = jsonData.fieldNames();
	                    while (fieldNames.hasNext()) {
	                        String batchErrorId = fieldNames.next();
	                        batchNodesMap.computeIfAbsent(batchErrorId, k -> new HashSet<>()).add(nodeRef);
	                    }
	                } catch (Exception e) {
	                    logger.warn("Cannot parse batch error logs for entry '" + nodeRef + "': " + e.getMessage());
	                }
	            }
	        }
	    }
	}
	
	/** {@inheritDoc} */
	@Override
	public BatchInfo retryBatchInError(String batchErrorId) {
		if (batchErrorId == null || batchErrorId.isBlank()) {
			return null;
		}
		discardedEntries.remove(batchErrorId);
		Set<NodeRef> nodes = getBatchErrorsMap().get(batchErrorId);
		List<NodeRef> nodeRefs = nodes != null ? new ArrayList<>(nodes) : Collections.emptyList();
		return createAndQueueRetryBatch(batchErrorId, "becpg.batch.retry." + batchErrorId, nodeRefs);
	}

	/** {@inheritDoc} */
	@Override
	public BatchInfo retryBatchEntryInError(String batchErrorId, NodeRef nodeRef) {
		if (batchErrorId == null || batchErrorId.isBlank() || nodeRef == null) {
			return null;
		}
		Set<NodeRef> discarded = discardedEntries.get(batchErrorId);
		if (discarded != null) {
			discarded.remove(nodeRef);
			if (discarded.isEmpty()) {
				discardedEntries.remove(batchErrorId);
			}
		}
		return createAndQueueRetryBatch(batchErrorId, "becpg.batch.retry." + batchErrorId + "." + nodeRef.getId(), List.of(nodeRef));
	}

	private BatchInfo createAndQueueRetryBatch(String batchErrorId, String queueBatchId, List<NodeRef> nodeRefs) {
		if (batchErrorId == null || nodeRefs == null || nodeRefs.isEmpty()) {
			return null;
		}
		String[] split = batchErrorId.split("\\|");
		String batchDesc = split.length > 1 ? I18NUtil.getMessage(split[1]) : split[0];
		if (batchDesc == null) {
			batchDesc = split.length > 1 ? split[1] : split[0];
		}
		BatchInfo batchInfo = new BatchInfo(queueBatchId, "becpg.batch.retry", batchDesc);
		BatchStep<NodeRef> batchStep = new BatchStep<>();
		batchStep.setWorkProvider(new EntityListBatchProcessWorkProvider<>(nodeRefs));
		batchStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<>() {
			@Override
			public void process(NodeRef entry) throws Throwable {
				clearBatchError(entry, batchErrorId);
				if (batchQueuePlugins != null) {
					for (BatchQueuePlugin plugin : batchQueuePlugins) {
						plugin.onRetryBatchError(entry, split[0]);
					}
				}
			}
		});
		queueBatch(batchInfo, List.of(batchStep));
		return batchInfo;
	}
	
	/** {@inheritDoc} */
	@Override
	public BatchStep<NodeRef> createBatchStepWithErrorHandling(BatchInfo batchInfo, List<NodeRef> list, BatchProcessWorker<NodeRef> processor) {
		return createBatchStepWithErrorHandling(batchInfo, list, processor, null);
	}
	
	/** {@inheritDoc} */
	@Override
	public BatchStep<NodeRef> createBatchStepWithErrorHandling(BatchInfo batchInfo, List<NodeRef> list, BatchProcessWorker<NodeRef> processor, BiConsumer<NodeRef, Throwable> errorHandler) {
		return new BatchStepWithErrorHandling(batchInfo, list, processor, errorHandler);
	}

	private class BatchStepWithErrorHandling extends BatchStep<NodeRef> {

		private BatchStepWithErrorHandling(BatchInfo batchInfo, List<NodeRef> list, BatchProcessWorker<NodeRef> processor, BiConsumer<NodeRef, Throwable> errorHandler) {
			String batchFullId = batchInfo.getBatchId() + "|" + batchInfo.getBatchDescId();
			workProvider = new EntityListBatchProcessWorkProvider<>(list);
			processWorker = createErrorHandlingProcessWorker(batchInfo, batchFullId, processor, errorHandler);
		}

		private BatchProcessWorkerAdaptor<NodeRef> createErrorHandlingProcessWorker(BatchInfo batchInfo, String batchFullId,
				BatchProcessWorker<NodeRef> processor, BiConsumer<NodeRef, Throwable> errorHandler) {
			return new BatchProcessor.BatchProcessWorkerAdaptor<>() {
				@Override
				public void process(NodeRef entry) throws Throwable {
					if (isDiscardedEntry(batchFullId, entry)) {
						return;
					}
					if (!hasBatchError(entry, batchFullId)) {
						try {
							processor.process(entry);
						} catch (Throwable e) {
							if (RetryingTransactionHelper.extractRetryCause(e) != null) {
								logger.debug("Retrying the formulation for " + entry + " due to exception: " + e.getMessage());
								throw e; // Re-throw to trigger retry
							}
							policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
							logger.error("Error processing entry '" + entry + "' for batch : '" + batchInfo.getBatchId(), e);
							markEntryInError(batchFullId, entry, e);
							if (errorHandler != null) {
								errorHandler.accept(entry, e);
							}
						}
					} else {
						if (logger.isDebugEnabled()) {
							logger.debug("skip entry '" + entry + "' from batch '" + batchInfo.getBatchId() + "' as it is marked as failed");
						}
					}
				}
			};
		}
	}
	
	/**
	 * <p>isDiscardedEntry.</p>
	 *
	 * An entry that cannot be flagged in error on the node itself has to be excluded by the queue,
	 * otherwise it would fail again on every run of the batch and reschedule it endlessly.
	 *
	 * @param batchFullId a {@link java.lang.String} object
	 * @param entry a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a boolean
	 */
	private boolean isDiscardedEntry(String batchFullId, NodeRef entry) {
		if (!nodeService.exists(entry)) {
			if (logger.isDebugEnabled()) {
				logger.debug("skip entry '" + entry + "' from batch '" + batchFullId + "' as it does not exist anymore");
			}
			return true;
		}
		
		Set<NodeRef> batchDiscardedEntries = discardedEntries.get(batchFullId);
		
		if ((batchDiscardedEntries != null) && batchDiscardedEntries.contains(entry)) {
			if (logger.isDebugEnabled()) {
				logger.debug("skip entry '" + entry + "' from batch '" + batchFullId + "' as it could not be flagged as failed");
			}
			return true;
		}
		
		return false;
	}
	
	/**
	 * <p>discardEntry.</p>
	 *
	 * @param batchFullId a {@link java.lang.String} object
	 * @param entry a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	private void discardEntry(String batchFullId, NodeRef entry) {
		Set<NodeRef> batchDiscardedEntries = discardedEntries.computeIfAbsent(batchFullId, k -> ConcurrentHashMap.newKeySet());
		
		if (batchDiscardedEntries.size() < MAX_DISCARDED_ENTRIES) {
			batchDiscardedEntries.add(entry);
		}
	}
	
	/**
	 * <p>markEntryInError.</p>
	 *
	 * Flags the entry as failed so that it is skipped on the next runs of the batch. When the flag
	 * cannot be written, the entry is discarded in memory instead, otherwise the batch would be
	 * rescheduled endlessly on it.
	 *
	 * @param batchFullId a {@link java.lang.String} object
	 * @param entry a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param throwable a {@link java.lang.Throwable} object
	 * @throws java.lang.Exception if the flag cannot be written and the transaction has to be retried
	 */
	@SuppressWarnings("unchecked")
	private void markEntryInError(String batchFullId, NodeRef entry, Throwable throwable) throws Exception {
		try {
			if (!nodeService.hasAspect(entry, BeCPGModel.ASPECT_BATCH_ERROR)) {
				nodeService.addAspect(entry, BeCPGModel.ASPECT_BATCH_ERROR, null);
			}

			// 1. Maintain indexed batchErrorIds for fast search / exclusion queries (e.g. EntityReportJob)
			List<String> errorIds = (List<String>) nodeService.getProperty(entry, BeCPGModel.PROP_BATCH_ERROR_IDS);
			List<String> newErrorIds = errorIds == null ? new ArrayList<>() : new ArrayList<>(errorIds);
			if (!newErrorIds.contains(batchFullId)) {
				newErrorIds.add(batchFullId);
				nodeService.setProperty(entry, BeCPGModel.PROP_BATCH_ERROR_IDS, (Serializable) newErrorIds);
			}

			// 2. Maintain batchErrorLogs for structured error messages
			String errorMsg = extractErrorMessage(throwable);
			updateBatchErrorLogs(entry, batchFullId, errorMsg != null ? errorMsg : "Error");

			beCPGCacheService.clearCache(BatchQueueServiceImpl.class.getName());
		} catch (Exception e) {
			if (RetryingTransactionHelper.extractRetryCause(e) != null) {
				throw e;
			}
			logger.warn("Cannot flag entry '" + entry + "' as failed for batch '" + batchFullId + "', discarding it: " + e.getMessage());
			discardEntry(batchFullId, entry);
		}
	}

	@SuppressWarnings("unchecked")
	private boolean hasBatchError(NodeRef entry, String batchFullId) {
		List<String> batchErrorIds = (List<String>) nodeService.getProperty(entry, BeCPGModel.PROP_BATCH_ERROR_IDS);
		if (batchErrorIds != null && !batchErrorIds.isEmpty()) {
			return batchErrorIds.contains(batchFullId);
		}
		String existingJson = (String) nodeService.getProperty(entry, BeCPGModel.PROP_BATCH_ERROR_LOGS);
		if (existingJson != null && !existingJson.isBlank()) {
			try {
				JsonData jsonData = JsonHelper.read(existingJson);
				return jsonData.has(batchFullId);
			} catch (Exception e) {
				logger.debug("Cannot read batch error logs for entry " + entry, e);
			}
		}
		return false;
	}

	private Throwable getRootCause(Throwable throwable) {
		Throwable rootCause = throwable;
		int depth = 0;
		while (rootCause.getCause() != null && rootCause.getCause() != rootCause && depth < 20) {
			rootCause = rootCause.getCause();
			depth++;
		}
		return rootCause;
	}

	private String extractErrorMessage(Throwable throwable) {
		if (throwable == null) {
			return null;
		}
		Throwable rootCause = getRootCause(throwable);
		String message = rootCause.getMessage();
		if (message == null || message.trim().isEmpty()) {
			message = throwable.getMessage();
		}
		if (message == null || message.trim().isEmpty()) {
			message = rootCause.getClass().getSimpleName();
		}
		return message;
	}

	private void updateBatchErrorLogs(NodeRef entry, String batchFullId, String errorMsg) {
		String existingJson = (String) nodeService.getProperty(entry, BeCPGModel.PROP_BATCH_ERROR_LOGS);
		JsonData jsonData = (existingJson != null && !existingJson.isBlank()) ? JsonHelper.read(existingJson) : JsonHelper.createJsonObject();
		jsonData.put(batchFullId, errorMsg);
		nodeService.setProperty(entry, BeCPGModel.PROP_BATCH_ERROR_LOGS, jsonData.toString());
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	public void clearBatchError(NodeRef entry, String batchFullId) {
		if (entry == null || batchFullId == null || !nodeService.exists(entry)) {
			return;
		}
		boolean hasRemainingLogs = false;
		String existingJson = (String) nodeService.getProperty(entry, BeCPGModel.PROP_BATCH_ERROR_LOGS);
		if (existingJson != null && !existingJson.isBlank()) {
			try {
				JsonData jsonData = JsonHelper.read(existingJson);
				if (jsonData.has(batchFullId)) {
					jsonData.remove(batchFullId);
					if (jsonData.isEmpty()) {
						nodeService.removeProperty(entry, BeCPGModel.PROP_BATCH_ERROR_LOGS);
					} else {
						nodeService.setProperty(entry, BeCPGModel.PROP_BATCH_ERROR_LOGS, jsonData.toString());
						hasRemainingLogs = true;
					}
				} else if (!jsonData.isEmpty()) {
					hasRemainingLogs = true;
				}
			} catch (Exception e) {
				logger.warn("Cannot update batch error logs for entry '" + entry + "': " + e.getMessage(), e);
			}
		}
		boolean hasRemainingIds = false;
		List<String> errorIds = (List<String>) nodeService.getProperty(entry, BeCPGModel.PROP_BATCH_ERROR_IDS);
		if (errorIds != null) {
			List<String> newIds = errorIds.stream().filter(id -> !id.equals(batchFullId)).toList();
			if (newIds.isEmpty()) {
				nodeService.removeProperty(entry, BeCPGModel.PROP_BATCH_ERROR_IDS);
			} else {
				nodeService.setProperty(entry, BeCPGModel.PROP_BATCH_ERROR_IDS, (Serializable) newIds);
				hasRemainingIds = true;
			}
		}
		if (!hasRemainingLogs && !hasRemainingIds) {
			if (nodeService.hasAspect(entry, BeCPGModel.ASPECT_BATCH_ERROR)) {
				nodeService.removeAspect(entry, BeCPGModel.ASPECT_BATCH_ERROR);
			}
		}
		beCPGCacheService.clearCache(BatchQueueServiceImpl.class.getName());
	}

	private String getBatchErrorFromNode(NodeRef entry, String batchFullId) {
		String existingJson = (String) nodeService.getProperty(entry, BeCPGModel.PROP_BATCH_ERROR_LOGS);
		if (existingJson != null && !existingJson.isBlank()) {
			try {
				JsonData jsonData = JsonHelper.read(existingJson);
				if (jsonData.has(batchFullId)) {
					return jsonData.get(batchFullId).getString();
				}
			} catch (Exception e) {
				logger.debug("Cannot read batch error logs for entry " + entry, e);
			}
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public String viewErrors(String batchId) {
		return viewErrors(batchId, 0, -1);
	}

	/** {@inheritDoc} */
	@Override
	public String viewErrors(String batchId, int offset, int limit) {
		JsonData root = JsonHelper.createJsonObject();
		if (batchId == null || batchId.isBlank()) {
			root.put("total", 0);
			root.put("offset", offset);
			root.put("limit", limit);
			root.put("entities", JsonHelper.createJsonArray());
			return root.toString();
		}
		Map<String, Set<NodeRef>> batchErrorsMap = getBatchErrorsMap();
		String[] split = batchId.split("\\|");
		root.put("batchId", batchId);
		String batchDesc = split.length > 1 ? I18NUtil.getMessage(split[1]) : batchId;
		if (batchDesc == null) {
			batchDesc = split.length > 1 ? split[1] : batchId;
		}
		root.put("batchDesc", batchDesc);

		JsonData entitiesArray = JsonHelper.createJsonArray();
		Set<NodeRef> errorNodes = batchErrorsMap.get(batchId);
		int total = 0;
		if (errorNodes != null) {
			List<NodeRef> existingNodes = new ArrayList<>();
			for (NodeRef nodeRef : errorNodes) {
				if (nodeService.exists(nodeRef)) {
					existingNodes.add(nodeRef);
				}
			}
			total = existingNodes.size();

			int fromIndex = Math.max(0, offset);
			int toIndex = limit > 0 ? Math.min(fromIndex + limit, total) : total;
			if (fromIndex < total) {
				List<NodeRef> pagedNodes = existingNodes.subList(fromIndex, toIndex);
				for (NodeRef errorNodeRef : pagedNodes) {
					JsonData json = JsonHelper.createJsonObject();
					json.put("nodeRef", errorNodeRef.toString());
					String name = (String) nodeService.getProperty(errorNodeRef, ContentModel.PROP_NAME);
					json.put("name", name != null ? name : errorNodeRef.getId());

					String code = (String) nodeService.getProperty(errorNodeRef, BeCPGModel.PROP_CODE);
					if (code != null) {
						json.put("code", code);
					}

					QName typeQName = nodeService.getType(errorNodeRef);
					json.put("type", typeQName.toPrefixString(namespaceService));

					String siteId = null;
					try {
						Path path = nodeService.getPath(errorNodeRef);
						if (path != null) {
							siteId = SiteHelper.extractSiteId(path.toPrefixString(namespaceService));
						}
					} catch (Exception e) {
						logger.debug("Cannot extract site id for entry " + errorNodeRef, e);
					}
					if (siteId != null) {
						json.put("siteId", siteId);
					}

					String errorMsg = getBatchErrorFromNode(errorNodeRef, batchId);
					if (errorMsg != null) {
						json.put("error", errorMsg);
					}

					entitiesArray.put(json);
				}
			}
		}
		root.put("total", total);
		root.put("offset", offset);
		root.put("limit", limit);
		root.put("entities", entitiesArray);
		return root.toString();
	}
	
	/** {@inheritDoc} */
	@Override
	public void onApplicationEvent(BatchMonitorEvent event) {
		if (event.getBatchMonitor().getProcessName() != null && event.getBatchMonitor().getProcessName().contains("batchId")) {
			lastRunningBatch = event.getBatchMonitor();
		}
	}
	
}
