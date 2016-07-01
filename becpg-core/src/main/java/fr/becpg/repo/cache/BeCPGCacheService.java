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
package fr.becpg.repo.cache;

import java.util.Collection;

/**
 * 
 * @author matthieu Tenant Aware Cache
 */
public interface BeCPGCacheService {

	<T> T getFromCache(String cacheName, String cacheKey, BeCPGCacheDataProviderCallBack<T> cacheDataProviderCallBack);

	void clearAllCaches();

	void removeFromCache(String name, String cacheKey);
	
	Collection<String> getCacheKeys(String cacheName);

	void printCacheInfos();

	<T> T getFromCache(String cacheName, String cacheKey, BeCPGCacheDataProviderCallBack<T> cacheDataProviderCallBack, boolean deleteOnTxRollback);

	void clearCache(String cacheName);

	
}
