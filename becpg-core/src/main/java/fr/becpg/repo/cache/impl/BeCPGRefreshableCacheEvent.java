package fr.becpg.repo.cache.impl;

import java.util.Objects;

import org.alfresco.util.cache.RefreshableCacheEvent;

public class BeCPGRefreshableCacheEvent  implements RefreshableCacheEvent
{
	  
	private static final long serialVersionUID = -6131535575464962047L;
	
	private String cacheId;
	    private String key;

	    BeCPGRefreshableCacheEvent(String cacheId, String key)
	    {
	        this.cacheId = cacheId;
	        this.key = key;
	    }

	    @Override
	    public String getCacheId()
	    {
	        return cacheId;
	    }

	    @Override
	    public String getKey()
	    {
	        return key;
	    }

		@Override
		public int hashCode() {
			return Objects.hash(cacheId, key);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			BeCPGRefreshableCacheEvent other = (BeCPGRefreshableCacheEvent) obj;
			return Objects.equals(cacheId, other.cacheId) && Objects.equals(key, other.key);
		}

		@Override
		public String toString() {
			return "BeCPGRefreshableCacheEvent [cacheId=" + cacheId + ", key=" + key + "]";
		}

	  
}
