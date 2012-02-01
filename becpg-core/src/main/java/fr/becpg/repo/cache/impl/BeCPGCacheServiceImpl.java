package fr.becpg.repo.cache.impl;

import java.io.IOException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

import fr.becpg.repo.cache.BeCPGCacheDataProviderCallBack;
import fr.becpg.repo.cache.BeCPGCacheService;

/**
 * 
 * @author matthieu
 */
public class BeCPGCacheServiceImpl implements InitializingBean, DisposableBean, BeCPGCacheService {

	private static Log logger = LogFactory.getLog(BeCPGCacheServiceImpl.class);

	private CacheManager cacheManager;
	private Resource configLocation;

	public void setConfigLocation(Resource configLocation) {
		this.configLocation = configLocation;
	}

	public void afterPropertiesSet() throws IOException, CacheException {
		PropertyCheck.mandatory(this, "configLocation", configLocation);

		logger.info("Init beCPG Cache");
		cacheManager = new CacheManager(this.configLocation.getURL());
	}

	@SuppressWarnings("unchecked")
	public <T> T getFromUserCache(String cacheName, String cacheKey,
			BeCPGCacheDataProviderCallBack<T> sigedCacheDataProviderCallBack) {
		
		Cache cache = getCache(cacheName);
		T ret = null;
		try {
			Element el = cache.get(cacheKey);
			if (el != null) {
				logger.debug("Getting values from " + cacheKey);
				ret = (T) el.getObjectValue();
			}
		} catch (Exception e) {
			logger.error("Cannot get " + cacheKey + " from cache " + cacheName, e);
		}

		if (ret == null) {
			ret = sigedCacheDataProviderCallBack.getData();
			logger.debug("Store values to " + cacheKey);
			cache.put(new Element(cacheKey, ret));
		}

		return ret;
	}

	private Cache getCache(String cacheName) {
		if(!cacheManager.cacheExists(cacheName)){
			cacheManager.addCache(cacheName);
		}
		return cacheManager.getCache(cacheName);
	}

	public void destroy() {
		logger.info("Close beCPG cache");
		cacheManager.shutdown();
	}

	public void clearAllCaches() {
		logger.info("Clear all cache");
		cacheManager.clearAll();
	}

}
