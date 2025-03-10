package fr.becpg.repo.cache.impl;

import java.util.Objects;

import org.alfresco.util.cache.RefreshableCacheEvent;

/**
 * <p>BeCPGRefreshableCacheEvent class.</p>
 *
 * @author matthieu
 */
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

	    /** {@inheritDoc} */
	    @Override
	    public String getCacheId()
	    {
	        return cacheId;
	    }

	    /** {@inheritDoc} */
	    @Override
	    public String getKey()
	    {
	        return key;
	    }

		/** {@inheritDoc} */
		@Override
		public int hashCode() {
			return Objects.hash(cacheId, key);
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
			BeCPGRefreshableCacheEvent other = (BeCPGRefreshableCacheEvent) obj;
			return Objects.equals(cacheId, other.cacheId) && Objects.equals(key, other.key);
		}

		/** {@inheritDoc} */
		@Override
		public String toString() {
			return "BeCPGRefreshableCacheEvent [cacheId=" + cacheId + ", key=" + key + "]";
		}

	  
}
