/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.cache.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.util.transaction.TransactionListenerAdapter;
import org.alfresco.util.transaction.TransactionSupportUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

import fr.becpg.repo.cache.BeCPGCacheDataProviderCallBack;
import fr.becpg.repo.cache.BeCPGCacheService;

/**
 *
 * @author matthieu
 *
 */
public class BeCPGCacheServiceImpl implements BeCPGCacheService, InitializingBean {

	private static final Log logger = LogFactory.getLog(BeCPGCacheServiceImpl.class);

	private int maxCacheItems = 500;

	Map<String, Integer> cacheSizes = new HashMap<>(10);

	private boolean isDebugEnable = false;

	private boolean disableAllCache = false;

	private TenantAdminService tenantAdminService;

	private final Map<String, DefaultSimpleCache<String, ?>> caches = new ConcurrentHashMap<>();

	public void setMaxCacheItems(int maxCacheItems) {
		this.maxCacheItems = maxCacheItems;
	}

	public Map<String, DefaultSimpleCache<String, ?>> getCaches() {
		return Collections.unmodifiableMap(caches);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		isDebugEnable = logger.isDebugEnabled();

	}

	public void setDisableAllCache(boolean disableAllCache) {
		this.disableAllCache = disableAllCache;
	}

	public void setTenantAdminService(TenantAdminService tenantAdminService) {
		this.tenantAdminService = tenantAdminService;
	}

	public void setCacheSizes(Map<String, Integer> cacheSizes) {
		this.cacheSizes = cacheSizes;
	}

	@Override
	public <T> T getFromCache(String cacheName, String cacheKey, BeCPGCacheDataProviderCallBack<T> cacheDataProviderCallBack) {
		return getFromCache(cacheName, cacheKey, cacheDataProviderCallBack, false);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getFromCache(final String cacheName, String cacheKey, BeCPGCacheDataProviderCallBack<T> cacheDataProviderCallBack,
			boolean deleteOnTxRollback) {

		cacheKey = computeCacheKey(cacheKey);
		SimpleCache<String, T> cache = (DefaultSimpleCache<String, T>) getCache(cacheName);
		T ret = null;
		try {
			ret = disableAllCache ? null : cache.get(cacheKey);
		} catch (Exception e) {
			logger.error("Cannot get " + cacheKey + " from cache " + cacheName, e);
		}

		if (ret == null) {
			if (isDebugEnable) {
				logger.debug("Cache miss " + cacheKey);
			}

			ret = cacheDataProviderCallBack.getData();
			if (!disableAllCache && (ret != null)) {

				if (deleteOnTxRollback && TransactionSupportUtil.isActualTransactionActive()) {
					Set<String> currentTransactionCacheKeys = TransactionSupportUtil.getResource(cacheName);
					if (currentTransactionCacheKeys == null) {
						currentTransactionCacheKeys = new LinkedHashSet<>();
						if (isDebugEnable) {
							logger.debug("Bind key to transaction : " + cacheName);
						}
						TransactionSupportUtil.bindResource(cacheName, currentTransactionCacheKeys);
					}
					currentTransactionCacheKeys.add(cacheKey);

					AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {

						@Override
						public void afterRollback() {

							Set<String> txCacheKeys = TransactionSupportUtil.getResource(cacheName);
							if (txCacheKeys != null) {
								for (String txCacheKey : txCacheKeys) {
									if (isDebugEnable) {
										logger.debug("Remove tx assoc   " + cacheName + " " + txCacheKey);
									}
									removeFromCache(cacheName, txCacheKey);
								}

							}

						}
					});
				}

				cache.put(cacheKey, ret);
			}
		} else if (isDebugEnable) {
			logger.debug("Cache Hit " + cacheKey);
		}

		return ret;
	}

	@Override
	public void removeFromCache(String cacheName, String cacheKey) {
		cacheKey = computeCacheKey(cacheKey);
		SimpleCache<String, ?> cache = getCache(cacheName);
		if (isDebugEnable && (cache.get(cacheKey) == null)) {
			logger.debug("Cache " + cacheKey + " object doesn't exists");
		}
		cache.remove(cacheKey);

	}

	@Override
	public Collection<String> getCacheKeys(String cacheName) {
		SimpleCache<String, ?> cache = getCache(cacheName);
		return cache.getKeys();
	}

	@Override
	public void clearCache(String cacheName) {
		logger.debug("Clear specific cache: "+cacheName);
		SimpleCache<String, ?> cache = getCache(cacheName);
		cache.clear();
	}
	
	@Override
	public void clearAllCaches() {
		logger.debug("Clear all cache");
		for (SimpleCache<String, ?> cache : caches.values()) {
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
	private DefaultSimpleCache<String, ?> getCache(String cacheName) {
		DefaultSimpleCache<String, ?> cache = caches.get(cacheName);

		if (cache == null) {
			Integer cacheSize = cacheSizes.get(cacheName);
			if (cacheSize == null) {
				cacheSize = maxCacheItems;
			}

			if (tenantAdminService.isEnabled()) {
				cacheSize *= tenantAdminService.getAllTenants().size();
			}

			cache = new DefaultSimpleCache(cacheSize, cacheName);

			caches.put(cacheName, cache);
		}
		return cache;
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

	public final class DefaultSimpleCache<K extends Serializable, V> implements SimpleCache<K, V> {
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
			map = new ConcurrentLinkedHashMap.Builder<K, AbstractMap.SimpleImmutableEntry<K, V>>().maximumWeightedCapacity(maxItems).build();
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
			AbstractMap.SimpleImmutableEntry<K, V> kvp = new AbstractMap.SimpleImmutableEntry<>(key, value);
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

	}

	

}
