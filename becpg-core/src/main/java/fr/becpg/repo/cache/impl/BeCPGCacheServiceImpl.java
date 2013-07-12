package fr.becpg.repo.cache.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.Weighers;

import fr.becpg.repo.cache.BeCPGCacheDataProviderCallBack;
import fr.becpg.repo.cache.BeCPGCacheService;

/**
 * 
 * @author matthieu TODO refactor with new simplecache or simply delete
 */
@Service
public class BeCPGCacheServiceImpl implements BeCPGCacheService, InitializingBean {

	private static Log logger = LogFactory.getLog(BeCPGCacheServiceImpl.class);

	private int maxCacheItems = 500;

	private boolean isDebugEnable = false;

	private Map<String, DefaultSimpleCache<Serializable, ?>> caches = new ConcurrentHashMap<String, DefaultSimpleCache<Serializable, ?>>();

	public void setMaxCacheItems(int maxCacheItems) {
		this.maxCacheItems = maxCacheItems;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		isDebugEnable = logger.isDebugEnabled();

	}

	@Override
	public <T> T getFromCache(String cacheName, String cacheKey, BeCPGCacheDataProviderCallBack<T> cacheDataProviderCallBack) {

		return getFromCache(cacheName, cacheKey, cacheDataProviderCallBack, -1);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getFromCache(String cacheName, String cacheKey, BeCPGCacheDataProviderCallBack<T> cacheDataProviderCallBack, long timeStamp) {

		cacheKey = computeCacheKey(cacheKey);
		DefaultSimpleCache<Serializable, Pair<Long, T>> cache = (DefaultSimpleCache<Serializable, Pair<Long, T>>) getCache(cacheName);
		Pair<Long, T> ret = null;
		T value = null;
		try {
			ret = cache.get(cacheKey);

		} catch (Exception e) {
			logger.error("Cannot get " + cacheKey + " from cache " + cacheName, e);
		}

		if (ret != null) {
			if (ret.getFirst() < timeStamp) {
				if (isDebugEnable) {
					logger.debug("Remove obsolete cache value " + cacheKey + " old timeStamp " + ret.getFirst() + " new timeStamp " + timeStamp);
				}
				cache.remove(cacheKey);
			} else {
				if (isDebugEnable) {
					logger.debug("Cache hit " + cacheKey);
				}
				value = ret.getSecond();
			}
		}
		if (value == null) {
			if (isDebugEnable) {
				logger.debug("Cache miss " + cacheKey);
			}

			value = cacheDataProviderCallBack.getData();
			if (value != null) {
				cache.put(cacheKey, new Pair<Long, T>(timeStamp, value));
			}
		}

		return value;
	}

	@Override
	public void removeFromCache(String cacheName, String cacheKey) {
		cacheKey = computeCacheKey(cacheKey);
		SimpleCache<Serializable, ?> cache = getCache(cacheName);
		cache.remove(cacheKey);

	}

	@Override
	public void clearAllCaches() {
		logger.info("Clear all cache");
		for (SimpleCache<Serializable, ?> cache : caches.values()) {
			cache.clear();
		}

	}

	private String computeCacheKey(String cacheKey) {

		final String tenantDomain = TenantUtil.getCurrentDomain();
		if (!tenantDomain.equals(TenantService.DEFAULT_DOMAIN)) {
			return cacheKey + "@" + tenantDomain;

		}
		return cacheKey;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private DefaultSimpleCache<Serializable, ?> getCache(String cacheName) {
		if (!caches.containsKey(cacheName)) {
			caches.put(cacheName, new DefaultSimpleCache(maxCacheItems, cacheName));
		}
		return caches.get(cacheName);
	}

	@Override
	public void printCacheInfos() {
		for (String cacheName : caches.keySet()) {
			logger.info("Cache - " + cacheName);
			logger.info(" - Elements - " + caches.get(cacheName).getBackingMap().size());
			logger.info(" - Capacity - " + caches.get(cacheName).getBackingMap().capacity());
			logger.info(" - WeightedSize - " + caches.get(cacheName).getBackingMap().weightedSize());
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);
				oos.writeObject(caches.get(cacheName).getBackingMap());
				oos.close();
				logger.info(" - Data Size: " + baos.size() + "bytes");
			} catch (IOException e) {
				logger.warn(" - Data Size: (no serializable data)");
			}
		}

	}

	public final class DefaultSimpleCache<K extends Serializable, V extends Object> implements SimpleCache<K, V> {
		private ConcurrentLinkedHashMap<K, AbstractMap.SimpleImmutableEntry<K, V>> map;

		/**
		 * Construct a cache using the specified capacity and name.
		 * 
		 * @param maxItems
		 *            The cache capacity.
		 */
		public DefaultSimpleCache(int maxItems, String cacheName) {
			if (maxItems < 1) {
				throw new IllegalArgumentException("maxItems must be a positive integer, but was " + maxItems);
			}

			// The map will have a bounded size determined by the maxItems
			// member variable.
			map = new ConcurrentLinkedHashMap.Builder<K, AbstractMap.SimpleImmutableEntry<K, V>>().maximumWeightedCapacity(maxItems).concurrencyLevel(32)
					.weigher(Weighers.singleton()).build();
		}

		@Override
		public boolean contains(K key) {
			return map.containsKey(key);
		}

		@Override
		public Collection<K> getKeys() {
			return map.keySet();
		}

		public ConcurrentLinkedHashMap<K, AbstractMap.SimpleImmutableEntry<K, V>> getBackingMap() {
			return map;
		}

		@Override
		public V get(K key) {
			AbstractMap.SimpleImmutableEntry<K, V> kvp = map.get(key);
			if (kvp == null) {
				return null;
			}
			return kvp.getValue();
		}

		@Override
		public void put(K key, V value) {
			AbstractMap.SimpleImmutableEntry<K, V> kvp = new AbstractMap.SimpleImmutableEntry<K, V>(key, value);
			map.put(key, kvp);
		}

		@Override
		public void remove(K key) {
			map.remove(key);
		}

		@Override
		public void clear() {
			map.clear();
		}

		@Override
		public String toString() {
			return "DefaultSimpleCache[maxItems=" + map.capacity() + "]";
		}

		/**
		 * Sets the maximum number of items that the cache will hold.
		 * 
		 * @param maxItems
		 */
		public void setMaxItems(int maxItems) {
			map.setCapacity(maxItems);
		}

	}

}
