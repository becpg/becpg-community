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
package fr.becpg.repo.cache;

import java.util.function.Supplier;

/**
 * <p>BeCPGCacheService interface.</p>
 *
 * @author matthieu Tenant Aware Cache
 * @version $Id: $Id
 */
public interface BeCPGCacheService {

	/**
	 * <p>getFromCache.</p>
	 *
	 * @param cacheName a {@link java.lang.String} object.
	 * @param cacheKey a {@link java.lang.String} object.
	 * @param cacheDataProviderCallBack a Supplier object.
	 * @return a T object.
	 * @param <T> a T class
	 */
	<T> T getFromCache(String cacheName, String cacheKey, Supplier<T> cacheDataProviderCallBack);
	
	/**
	 * Gets a value from the transaction cache, computing it if not present
	 * 
	 * @param <T> The type of the value
	 * @param cacheName The cache name (used as part of the transaction resource key)
	 * @param itemKey The specific item key
	 * @param valueSupplier The supplier to compute the value if not in cache
	 * @return The cached or computed value
	 */
    <T> T getFromTransactionCache(String cacheName, String cacheKey, Supplier<T> cacheDataProviderCallBack);
	

	/**
	 * <p>clearAllCaches.</p>
	 */
	void clearAllCaches();

	/**
	 * <p>removeFromCache.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 * @param cacheKey a {@link java.lang.String} object.
	 */
	void removeFromCache(String name, String cacheKey);
	

	/**
	 * <p>printCacheInfos.</p>
	 */
	void printCacheInfos();

	/**
	 * <p>getFromCache.</p>
	 *
	 * @param cacheName a {@link java.lang.String} object.
	 * @param cacheKey a {@link java.lang.String} object.
	 * @param cacheDataProviderCallBack a Supplier object.
	 * @param deleteOnTxRollback a boolean.
	 * @return a T object.
	 * @param <T> a T class
	 */
	<T> T getFromCache(String cacheName, String cacheKey, Supplier<T> cacheDataProviderCallBack, boolean deleteOnTxRollback);

	/**
	 * <p>clearCache.</p>
	 *
	 * @param cacheName a {@link java.lang.String} object.
	 */
	void clearCache(String cacheName);

	/**
	 * <p>getFromCache.</p>
	 *
	 * @param cacheName a {@link java.lang.String} object.
	 * @param cacheKey a {@link java.lang.String} object..
	 * @return a T object.
	 * @param <T> a T class
	 */
	<T> T getFromCache(String cacheName, String cacheKey);

	/**
	 * <p>storeInCache.</p>
	 *
	 * @param cacheName a {@link java.lang.String} object.
	 * @param cacheKey a {@link java.lang.String} object.
	 * @param data a T object.
	 * @param <T> a T class
	 */
	<T> void storeInCache(String cacheName, String cacheKey, T data);

	
}
