package fr.becpg.util;

import java.util.concurrent.locks.ReentrantLock;

import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ConcurrentReferenceHashMap;

import fr.becpg.model.BeCPGModel;

/**
 * <p>MutexFactory class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Component
public class MutexFactory {

	private static final Long LOCK_TIMEOUT = 5 * 60 * 1000L;

	private static final Log logger = LogFactory.getLog(MutexFactory.class);

	private ConcurrentReferenceHashMap<String, JobLockWrapper> map;

	@Autowired
	private JobLockService jobLockService;

	/**
	 * <p>Constructor for MutexFactory.</p>
	 */
	public MutexFactory() {
		this.map = new ConcurrentReferenceHashMap<>();
	}

	/**
	 * <p>getMutex.</p>
	 *
	 * @param key a {@link java.lang.String} object
	 * @return a {@link java.util.concurrent.locks.ReentrantLock} object
	 */
	public ReentrantLock getMutex(String key) {
		return this.map.compute(key, (k, v) -> v == null ? new JobLockWrapper(k) : v);
	}

	/**
	 * <p>removeMutex.</p>
	 *
	 * @param key a {@link java.lang.String} object
	 * @param value a {@link java.lang.Object} object
	 */
	public void removeMutex(String key, Object value) {
		this.map.remove(key, value);
	}

	private class JobLockWrapper extends ReentrantLock {

		private static final long serialVersionUID = 1L;
		private String lockToken;
		private QName lockQName;

		public JobLockWrapper(String key) {
			this.lockQName = QName.createQName(BeCPGModel.BECPG_URI, QName.createValidLocalName(key));
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
