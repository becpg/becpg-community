/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.alfresco.repo.cache.DefaultSimpleCache;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.util.cache.AsynchronouslyRefreshedCacheRegistry;
import org.alfresco.util.cache.RefreshableCacheEvent;
import org.alfresco.util.cache.RefreshableCacheListener;
import org.alfresco.util.transaction.TransactionListenerAdapter;
import org.alfresco.util.transaction.TransactionSupportUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import fr.becpg.repo.cache.BeCPGCacheService;

/**
 * <p>BeCPGCacheServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class BeCPGCacheServiceImpl implements BeCPGCacheService, InitializingBean, RefreshableCacheListener {

	private static final Log logger = LogFactory.getLog(BeCPGCacheServiceImpl.class);

	Map<String, Integer> cacheSizes = new ConcurrentHashMap<>();

	private boolean isDebugEnable = false;

	private boolean disableAllCache = false;

	private AsynchronouslyRefreshedCacheRegistry registry;

	private static final int INITIAL_CACHE_MAP_SIZE = 16;

	private Map<String, SimpleCache<String, ?>> caches = new ConcurrentHashMap<>(INITIAL_CACHE_MAP_SIZE);

	/** {@inheritDoc} */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void afterPropertiesSet() throws Exception {
		isDebugEnable = logger.isDebugEnabled();

		for (Map.Entry<String, Integer> cacheEntry : cacheSizes.entrySet()) {

			Integer cacheSize = cacheEntry.getValue();

			DefaultSimpleCache<String, ?> cache = new DefaultSimpleCache(cacheSize, cacheEntry.getKey());

			caches.put(cacheEntry.getKey(), cache);
		}

		registry.register(this);

	}

	/**
	 * <p>Setter for the field <code>disableAllCache</code>.</p>
	 *
	 * @param disableAllCache a boolean.
	 */
	public void setDisableAllCache(boolean disableAllCache) {
		this.disableAllCache = disableAllCache;
	}

	/**
	 * <p>Setter for the field <code>cacheSizes</code>.</p>
	 *
	 * @param cacheSizes a {@link java.util.Map} object.
	 */
	public void setCacheSizes(Map<String, Integer> cacheSizes) {
		this.cacheSizes = cacheSizes;
	}

	/**
	 * <p>Setter for the field <code>registry</code>.</p>
	 *
	 * @param registry a {@link org.alfresco.util.cache.AsynchronouslyRefreshedCacheRegistry} object
	 */
	public void setRegistry(AsynchronouslyRefreshedCacheRegistry registry) {
		this.registry = registry;
	}

	/** {@inheritDoc} */
	@Override
	public <T> T getFromCache(String cacheName, String cacheKey, Supplier<T> cacheDataProviderCallBack) {
		return getFromCache(cacheName, cacheKey, cacheDataProviderCallBack, false);
	}

	/** {@inheritDoc} */
	@Override
	public <T> void storeInCache(String cacheName, String cacheKey, T data) {
		if (!disableAllCache) {
			cacheKey = computeCacheKey(cacheKey);
			@SuppressWarnings("unchecked")
			SimpleCache<String, T> cache = (SimpleCache<String, T>) caches.get(cacheName);
			cache.put(cacheKey, data);
		}
	}

	/** {@inheritDoc} */
	@Override
	public <T> T getFromCache(String cacheName, String cacheKey) {
		return getFromCache(cacheName, cacheKey, () -> null, false);
	}

	/** {@inheritDoc} */
	@Override
	public <T> T getFromCache(final String cacheName, String cacheKey, Supplier<T> cacheDataProviderCallBack, boolean deleteOnTxRollback) {

		cacheKey = computeCacheKey(cacheKey);
		@SuppressWarnings("unchecked")
		SimpleCache<String, T> cache = (SimpleCache<String, T>) caches.get(cacheName);

		if (cache == null) {
			logger.error(caches.keySet().toString() + " doesn't contains: " + cacheName);
			return null;
		}
		T ret = null;
		try {
			ret = disableAllCache ? null : cache.get(cacheKey);
		} catch (Exception e) {
			logger.error("Cannot get " + cacheKey + " from cache " + cacheName, e);
		}

		if (ret == null) {
			if (isDebugEnable) {
				logger.error("Cache miss " + cacheKey);
			}

			ret = cacheDataProviderCallBack.get();
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
			} else if (isDebugEnable && (ret == null)) {
				logger.error("Data provider is null for " + cacheKey);
			}
		} else if (isDebugEnable) {
			logger.debug("Cache Hit " + cacheKey);
		}

		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public void removeFromCache(String cacheName, String cacheKey) {
		cacheKey = computeCacheKey(cacheKey);
		if (isDebugEnable) {
			logger.debug("Delete from cache " + cacheKey);
		}
		SimpleCache<String, ?> cache = caches.get(cacheName);
		if (isDebugEnable && (cache.get(cacheKey) == null)) {
			logger.debug("Cache " + cacheKey + " object doesn't exists");
		}
		cache.remove(cacheKey);

	}

	/** {@inheritDoc} */
	@Override
	public <T> T getFromTransactionCache(String cacheName, String itemKey, Supplier<T> valueSupplier) {
		// Apply same key computation as regular cache methods
		itemKey = computeCacheKey(itemKey);

		// Skip computation if cache is disabled
		if (disableAllCache) {
			return valueSupplier.get();
		}

		// Create a composite transaction resource key
		final String txResourceKey = BeCPGCacheService.class.getName() + "." + cacheName;

		// Get the cache map from the transaction
		Map<String, Object> resourceMap = TransactionalResourceHelper.getMap(txResourceKey);

		// Check if our item exists in the cache
		T value = null;
		try {
			@SuppressWarnings("unchecked")
			T cachedValue = (T) resourceMap.get(itemKey);
			value = cachedValue;
		} catch (Exception e) {
			logger.error("Cannot get " + itemKey + " from transaction cache " + cacheName, e);
		}

		if (value == null) {
			if (isDebugEnable) {
				logger.debug("Transaction cache miss " + itemKey + " in " + cacheName);
			}

			// Not in cache, calculate and store it
			value = valueSupplier.get();

			if (value != null) {
				resourceMap.put(itemKey, value);
			} else if (isDebugEnable) {
				logger.debug("Transaction data provider returned null for " + itemKey + " in " + cacheName);
			}
		} else if (isDebugEnable) {
			logger.debug("Transaction cache hit " + itemKey + " in " + cacheName);
		}

		return value;
	}

	/** {@inheritDoc} */
	@Override
	public void clearCache(String cacheName) {
		registry.broadcastEvent(new BeCPGRefreshableCacheEvent(getCacheId(), cacheName), false);
	}

	/** {@inheritDoc} */
	@Override
	public void clearAllCaches() {
		registry.broadcastEvent(new BeCPGRefreshableCacheEvent(getCacheId(), "all"), true);
	}

	private String computeCacheKey(String cacheKey) {

		final String tenantDomain = TenantUtil.getCurrentDomain();
		if (!TenantService.DEFAULT_DOMAIN.equals(tenantDomain) && !cacheKey.endsWith("@" + tenantDomain)) {
			return cacheKey + "@" + tenantDomain;
		}
		return cacheKey;
	}

	/** {@inheritDoc} */
	@Override
	public void printCacheInfos() {
		for (Map.Entry<String, SimpleCache<String, ?>> cacheEntry : caches.entrySet()) {
			logger.info("Cache - " + cacheEntry.getKey());
			logger.info(" - Elements - " + cacheEntry.getValue().getKeys().size());
			logger.info(" - Capacity - " + ((DefaultSimpleCache<?, ?>) cacheEntry.getValue()).getMaxItems());

		}

	}

	/** {@inheritDoc} */
	@Override
	public void onRefreshableCacheEvent(RefreshableCacheEvent refreshableCacheEvent) {
		if (getCacheId().equals(refreshableCacheEvent.getCacheId())) {
			if ("all".equals(refreshableCacheEvent.getKey())) {
				logger.debug("Clear all cache");
				for (SimpleCache<String, ?> cache : caches.values()) {
					cache.clear();
				}
			} else {
				logger.debug("Clear specific cache: " + refreshableCacheEvent.getKey());
				SimpleCache<String, ?> cache = caches.get(refreshableCacheEvent.getKey());
				cache.clear();
			}
		}

	}

	/** {@inheritDoc} */
	@Override
	public String getCacheId() {
		return BeCPGCacheServiceImpl.class.getName();
	}

}
