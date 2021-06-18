package fr.becpg.repo.repository;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

class L2CacheThreadInfo implements Serializable{


	private static final long serialVersionUID = -7958215882212282888L;
	private boolean isCacheOnlyEnable = false;
	private boolean isThreadLockEnable = false;
	private boolean isThreadCacheEnable = false;
	private Map<NodeRef, RepositoryEntity> cache = new HashMap<>(500);
	
	/**
	 * <p>Constructor for L2CacheThreadInfo.</p>
	 *
	 * @param isCacheOnlyEnable a boolean.
	 * @param isThreadCacheEnable a boolean.
	 * @param isThreadLockEnable a boolean.
	 */
	public L2CacheThreadInfo(boolean isCacheOnlyEnable, boolean isThreadCacheEnable, boolean isThreadLockEnable ) {
		super();
		this.isCacheOnlyEnable = isCacheOnlyEnable;
		this.isThreadCacheEnable = isThreadCacheEnable;
		this.isThreadLockEnable = isThreadLockEnable;
	}

	
	public Map<NodeRef, RepositoryEntity> getCache() {
		return cache;
	}

	
	public boolean isCacheOnlyEnable() {
		return isCacheOnlyEnable;
	}


	public boolean isThreadCacheEnable() {
		return isThreadCacheEnable;
	}

	public boolean isThreadLockEnable() {
		return isThreadLockEnable;
	}


	/**
	 * <p>Constructor for L2CacheThreadInfo.</p>
	 */
	public L2CacheThreadInfo() {
		super();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "L2CacheThreadInfo [isCacheOnlyEnable=" + isCacheOnlyEnable + ", isThreadCacheEnable=" + isThreadCacheEnable + ", isThreadLockEnable=" + isThreadLockEnable + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(cache, isCacheOnlyEnable, isThreadCacheEnable, isThreadLockEnable);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		L2CacheThreadInfo other = (L2CacheThreadInfo) obj;
		return Objects.equals(cache, other.cache) && isCacheOnlyEnable == other.isCacheOnlyEnable && isThreadCacheEnable == other.isThreadCacheEnable
				&& isThreadLockEnable == other.isThreadLockEnable;
	}
	
	


}

