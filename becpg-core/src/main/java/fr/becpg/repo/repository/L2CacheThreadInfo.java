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
	private boolean isSilentModeEnable = false;
	private Map<NodeRef, RepositoryEntity> cache = new HashMap<>(500);
	
	/**
	 * <p>Constructor for L2CacheThreadInfo.</p>
	 *
	 * @param isCacheOnlyEnable a boolean.
	 * @param isThreadCacheEnable a boolean.
	 * @param isThreadLockEnable a boolean.
	 * @param isSilentModeEnable a boolean
	 */
	public L2CacheThreadInfo(boolean isCacheOnlyEnable, boolean isThreadCacheEnable, boolean isThreadLockEnable, boolean isSilentModeEnable) {
		super();
		this.isCacheOnlyEnable = isCacheOnlyEnable;
		this.isThreadCacheEnable = isThreadCacheEnable;
		this.isThreadLockEnable = isThreadLockEnable;
		this.isSilentModeEnable = isSilentModeEnable;
	}

	
	/**
	 * <p>Getter for the field <code>cache</code>.</p>
	 *
	 * @return a {@link java.util.Map} object
	 */
	public Map<NodeRef, RepositoryEntity> getCache() {
		return cache;
	}

	
	/**
	 * <p>isCacheOnlyEnable.</p>
	 *
	 * @return a boolean
	 */
	public boolean isCacheOnlyEnable() {
		return isCacheOnlyEnable;
	}


	/**
	 * <p>isThreadCacheEnable.</p>
	 *
	 * @return a boolean
	 */
	public boolean isThreadCacheEnable() {
		return isThreadCacheEnable;
	}

	/**
	 * <p>isThreadLockEnable.</p>
	 *
	 * @return a boolean
	 */
	public boolean isThreadLockEnable() {
		return isThreadLockEnable;
	}
	
	/**
	 * <p>isSilentModeEnable.</p>
	 *
	 * @return a boolean
	 */
	public boolean isSilentModeEnable() {
		return isSilentModeEnable;
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
		return "L2CacheThreadInfo [isCacheOnlyEnable=" + isCacheOnlyEnable + ", isThreadCacheEnable=" + isThreadCacheEnable + ", isThreadLockEnable=" + isThreadLockEnable + ", isSilentModeEnable=" + isSilentModeEnable + "]";
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(cache, isCacheOnlyEnable, isThreadCacheEnable, isThreadLockEnable, isSilentModeEnable);
	}

	/** {@inheritDoc} */
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
				&& isThreadLockEnable == other.isThreadLockEnable && isSilentModeEnable == other.isSilentModeEnable;
	}
	
	


}

