package fr.becpg.repo.repository;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

class L2CacheThreadInfo implements Serializable{


	private static final long serialVersionUID = -7958215882212282888L;
	boolean isCacheOnlyEnable = false;
	boolean isThreadCacheEnable = false;
	boolean isThreadLockEnable = false;
	final Map<NodeRef, RepositoryEntity> cache = new HashMap<>();
	
	public L2CacheThreadInfo(boolean isCacheOnlyEnable, boolean isThreadCacheEnable, boolean isThreadLockEnable ) {
		super();
		this.isCacheOnlyEnable = isCacheOnlyEnable;
		this.isThreadCacheEnable = isThreadCacheEnable;
		this.isThreadLockEnable = isThreadLockEnable;
	}

	public L2CacheThreadInfo() {
		super();
	}

	@Override
	public String toString() {
		return "L2CacheThreadInfo [isCacheOnlyEnable=" + isCacheOnlyEnable + ", isThreadCacheEnable=" + isThreadCacheEnable + ", isThreadLockEnable=" + isThreadLockEnable + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cache == null) ? 0 : cache.hashCode());
		result = prime * result + (isCacheOnlyEnable ? 1231 : 1237);
		result = prime * result + (isThreadCacheEnable ? 1231 : 1237);
		result = prime * result + (isThreadLockEnable ? 1231 : 1237);
		return result;
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
		if (cache == null) {
			if (other.cache != null)
				return false;
		} else if (!cache.equals(other.cache))
			return false;
		if (isCacheOnlyEnable != other.isCacheOnlyEnable)
			return false;
		if (isThreadCacheEnable != other.isThreadCacheEnable)
			return false;
		if (isThreadLockEnable != other.isThreadLockEnable)
			return false;
		return true;
	}
	
	


}

