/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG.
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

public class L2CacheSupport {

	private static final ThreadLocal<L2CacheThreadInfo> threadLocalCache = ThreadLocal.withInitial(() -> {
		return new L2CacheThreadInfo();
	});

	public interface Action {
		void run();
	}

	public static <T> Map<NodeRef, RepositoryEntity> getCurrentThreadCache() {
		if (threadLocalCache.get().isThreadCacheEnable)
			return threadLocalCache.get().cache;
		return new HashMap<>();
	}

	public static boolean isCacheOnlyEnable() {
		return threadLocalCache.get().isThreadCacheEnable && threadLocalCache.get().isCacheOnlyEnable;
	}

	public static NodeRef generateNodeRef() {
		return new NodeRef(RepoConsts.SPACES_STORE, "simu-" + UUID.randomUUID().toString());
	}

	public static boolean isThreadCacheEnable() {
		return threadLocalCache.get().isThreadCacheEnable;
	}

	public static boolean isThreadLockEnable() {
		return threadLocalCache.get().isThreadLockEnable;
	}

	public static void doInCacheContext(Action action, boolean isCacheOnlyEnable) {
		L2CacheThreadInfo previousContext = threadLocalCache.get();
		try {
			threadLocalCache.set(new L2CacheThreadInfo(isCacheOnlyEnable, true, false));
			action.run();
		} finally {
			threadLocalCache.set(previousContext);
		}
	}

	public static void doInCacheContext(Action action, boolean isCacheOnlyEnable, boolean isThreadLockEnable) {
		L2CacheThreadInfo previousContext = threadLocalCache.get();
		try {
			threadLocalCache.set(new L2CacheThreadInfo(isCacheOnlyEnable, true, isThreadLockEnable));
			action.run();
		} finally {
			threadLocalCache.set(previousContext);
		}
	}

}
