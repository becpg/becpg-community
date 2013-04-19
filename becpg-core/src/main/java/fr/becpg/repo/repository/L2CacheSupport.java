package fr.becpg.repo.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.RepoConsts;


public class L2CacheSupport {

	private static ThreadLocal<L2CacheThreadInfo> threadLocalCache = new ThreadLocal<L2CacheThreadInfo>() {
		protected L2CacheThreadInfo initialValue() {
			return new L2CacheThreadInfo(false,false);
		}
	};

	public interface Action {

		public void run();
		
	}

	private static class L2CacheThreadInfo {

		boolean isCacheOnlyEnable = false;
		boolean isThreadCacheEnable = false;
		Map<NodeRef, RepositoryEntity> cache = new HashMap<NodeRef, RepositoryEntity>();
		
		public L2CacheThreadInfo(boolean isCacheOnlyEnable, boolean isThreadCacheEnable) {
			super();
			this.isCacheOnlyEnable = isCacheOnlyEnable;
			this.isThreadCacheEnable = isThreadCacheEnable;
		}

	}

	public static <T> Map<NodeRef, RepositoryEntity> getCurrentThreadCache() {
		if(threadLocalCache.get().isThreadCacheEnable){
			return threadLocalCache.get().cache;
		} 
		return new HashMap<NodeRef,RepositoryEntity>();
	}

	public static  boolean isCacheOnlyEnable() {
		return threadLocalCache.get().isThreadCacheEnable &&  threadLocalCache.get().isCacheOnlyEnable;
	}

	public static  NodeRef generateNodeRef() {
		return new NodeRef(RepoConsts.SPACES_STORE, "simu-"+UUID.randomUUID().toString());
	}

	public static boolean isThreadCacheEnable() {
		return threadLocalCache.get().isThreadCacheEnable;
	}

	public static void doInCacheContext(Action action, boolean isCacheOnlyEnable) {
		try {
			threadLocalCache.set(new L2CacheThreadInfo(isCacheOnlyEnable, true));
			action.run();
		}
		finally {
			threadLocalCache.remove();
		}
		
		
	}

}
