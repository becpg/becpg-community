package fr.becpg.util;

import java.util.concurrent.locks.ReentrantLock;

import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ConcurrentReferenceHashMap;

/**
 * <p>MutexFactory class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Component
public class MutexFactory {

	/** Constant <code>LOCK_TIMEOUT</code> */
	private static final Long LOCK_TIMEOUT = 5 * 60 * 1000L;

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(MutexFactory.class);

	private ConcurrentReferenceHashMap<String, ReentrantLock> memoryMap;
	private ConcurrentReferenceHashMap<String, JobLockWrapper> clusterMap;

	@Autowired
	private JobLockService jobLockService;

	/**
	 * <p>Constructor for MutexFactory.</p>
	 */
	public MutexFactory() {
		this.memoryMap = new ConcurrentReferenceHashMap<>();
		this.clusterMap = new ConcurrentReferenceHashMap<>();
	}

	/**
	 * <p>getMutex.</p>
	 *
	 * Returns a memory-only lock for entity-level synchronization.
	 * Use this for entity-specific locks (formulate-{nodeId}) to avoid:
	 * - Deadlock in beforeCommit during init-repo
	 * - Pollution of alf_lock_resource with dynamic QNames
	 *
	 * @param key a {@link java.lang.String} object
	 * @return a {@link java.util.concurrent.locks.ReentrantLock} object
	 */
	public ReentrantLock getMutex(String key) {
		return this.memoryMap.compute(key, (k, v) -> v == null ? new ReentrantLock() : v);
	}

	/**
	 * <p>getClusterMutex.</p>
	 *
	 * Returns a cluster-aware lock using JobLockService for global job synchronization.
	 * Use this for global job locks (projectformulationjob, entityactivityjob, etc.)
	 * to ensure only one node in the cluster executes the job at a time.
	 *
	 * @param key a {@link java.lang.String} object (used for the ReentrantLock map)
	 * @param lockName a {@link java.lang.String} object (used as QName local name for JobLockService)
	 * @return a {@link java.util.concurrent.locks.ReentrantLock} object
	 */
	public ReentrantLock getClusterMutex(String key, String lockName) {
		return this.clusterMap.compute(key, (k, v) -> v == null ? new JobLockWrapper(k, lockName) : v);
	}

	/**
	 * <p>removeMutex.</p>
	 *
	 * @param key a {@link java.lang.String} object
	 * @param value a {@link java.lang.Object} object
	 */
	public void removeMutex(String key, Object value) {
		this.memoryMap.remove(key, value);
		this.clusterMap.remove(key, value);
	}

	private class JobLockWrapper extends ReentrantLock {

		private static final long serialVersionUID = 1L;
		private String lockToken;
		private QName lockQName;

		public JobLockWrapper(String key, String lockName) {
			// Use SYSTEM namespace which always exists, avoiding init-repo deadlock
			// Use constant lockName (e.g., "projectformulationjob") to avoid alf_lock_resource pollution
			this.lockQName = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, lockName);
		}

		@Override
		public void lock() {
			super.lock();
			if (getHoldCount() == 1) {
				try {
					lockToken = jobLockService.getLock(lockQName, LOCK_TIMEOUT, 500, 120); // wait up to 1min (120 * 500ms)
				} catch (LockAcquisitionException e) {
					logger.error("Failed to acquire JobLockService lock for " + lockQName, e);
				}
			}
		}

		@Override
		public boolean tryLock() {
			boolean localLocked = super.tryLock();
			if (localLocked) {
				if (getHoldCount() == 1) {
					try {
						lockToken = jobLockService.getLock(lockQName, LOCK_TIMEOUT);
						return true;
					} catch (LockAcquisitionException e) {
						super.unlock();
						return false;
					}
				}
				return true;
			}
			return false;
		}

		@Override
		public void unlock() {
			try {
				if ((getHoldCount() == 1) && (lockToken != null)) {
					jobLockService.releaseLock(lockToken, lockQName);
					lockToken = null;
				}
			} finally {
				if (isHeldByCurrentThread()) {
					super.unlock();
				}
			}
		}
	}
}
