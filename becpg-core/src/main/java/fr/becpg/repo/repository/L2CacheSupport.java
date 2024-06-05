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
package fr.becpg.repo.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.RepoConsts;

/**
 * <p>L2CacheSupport class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class L2CacheSupport {

	private static final ThreadLocal<L2CacheThreadInfo> threadLocalCache = ThreadLocal.withInitial(L2CacheThreadInfo::new);

	public interface Action {
		void run();
	}

	/**
	 * <p>getCurrentThreadCache.</p>
	 *
	 * @param <T> a T object.
	 * @return a {@link java.util.Map} object.
	 */
	public static <T> Map<NodeRef, RepositoryEntity> getCurrentThreadCache() {
		if (threadLocalCache.get().isThreadCacheEnable()) {
			return threadLocalCache.get().getCache();
		}
		return new HashMap<>(500);
	}

	/**
	 * <p>isCacheOnlyEnable.</p>
	 *
	 * @return a boolean.
	 */
	public static boolean isCacheOnlyEnable() {
		return threadLocalCache.get().isThreadCacheEnable() && threadLocalCache.get().isCacheOnlyEnable();
	}

	/**
	 * <p>generateNodeRef.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public static NodeRef generateNodeRef() {
		return new NodeRef(RepoConsts.SPACES_STORE, "simu-" + UUID.randomUUID().toString());
	}

	/**
	 * <p>isThreadCacheEnable.</p>
	 *
	 * @return a boolean.
	 */
	public static boolean isThreadCacheEnable() {
		return threadLocalCache.get().isThreadCacheEnable();
	}
	/**
	 * <p>isThreadLockEnable.</p>
	 *
	 * @return a boolean.
	 */
	public static boolean isThreadLockEnable() {
		return threadLocalCache.get().isThreadLockEnable();
	}
	
	/**
	 * <p>isSilentModeEnable.</p>
	 *
	 * @return a boolean
	 */
	public static boolean isSilentModeEnable() {
		return threadLocalCache.get().isSilentModeEnable();
	}

	/**
	 * <p>doInCacheContext.</p>
	 *
	 * @param action a {@link fr.becpg.repo.repository.L2CacheSupport.Action} object.
	 */
	public static void doInCacheOnly(Action action) {
		L2CacheThreadInfo previousContext = threadLocalCache.get();
		try {
			threadLocalCache.set(new L2CacheThreadInfo(true, true, false, false));
			action.run();
		} finally {
			threadLocalCache.remove();
			threadLocalCache.set(previousContext);
		}
	}

	/**
	 * <p>doInCacheContext.</p>
	 *
	 * @param action a {@link fr.becpg.repo.repository.L2CacheSupport.Action} object.
	 * @param isCacheOnlyEnable a boolean.
	 * @param isThreadLockEnable a boolean.
	 */
	public static void doInCacheContext(Action action, boolean isCacheOnlyEnable, boolean isThreadLockEnable) {
		L2CacheThreadInfo previousContext = threadLocalCache.get();
		try {
			threadLocalCache.set(new L2CacheThreadInfo(isCacheOnlyEnable, true, isThreadLockEnable, false));
			action.run();
		} finally {
			threadLocalCache.remove();
			threadLocalCache.set(previousContext);
		}
	}
	
	/**
	 * <p>doInCacheContext.</p>
	 *
	 * @param action a {@link fr.becpg.repo.repository.L2CacheSupport.Action} object
	 * @param isCacheOnlyEnable a boolean
	 * @param isThreadLockEnable a boolean
	 * @param isSilentModeEnable a boolean
	 */
	public static void doInCacheContext(Action action, boolean isCacheOnlyEnable, boolean isThreadLockEnable, boolean isSilentModeEnable) {
		L2CacheThreadInfo previousContext = threadLocalCache.get();
		try {
			threadLocalCache.set(new L2CacheThreadInfo(isCacheOnlyEnable, true, isThreadLockEnable, isSilentModeEnable));
			action.run();
		} finally {
			threadLocalCache.remove();
			threadLocalCache.set(previousContext);
		}
	}

}
