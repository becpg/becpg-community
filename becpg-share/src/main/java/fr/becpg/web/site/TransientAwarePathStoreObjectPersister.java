/*
 * Copyright (C) 2010-2026 beCPG.
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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package fr.becpg.web.site;

import org.springframework.extensions.surf.ModelObject;
import org.springframework.extensions.surf.ModelPersistenceContext;
import org.springframework.extensions.surf.exception.ModelObjectPersisterException;
import org.springframework.extensions.surf.persister.PathStoreObjectPersister;

/**
 * Path store persister that keeps the model objects a request creates in memory.
 * <p>
 * Surf publishes a cache invalidation not only when an object is written to the store, but also
 * when one is created in memory. On a cluster that message reaches the other nodes only, so the
 * node that created the object keeps it. Once {@link SingleNodeClusterService} delivers those
 * messages inside the JVM, the creating node drops its own brand new object, and an object that is
 * never persisted is then lost for good: the admin console binds its tool region with
 * <code>sitedata.newComponent(...)</code> followed by <code>save(false)</code>, so the binding only
 * ever lives in the cache and every console tool renders as an unbound region.
 * <p>
 * Suppressing the creation message keeps the invalidation that matters - the one published when an
 * object is written or removed - and leaves the other partitions to expire their sentinel for a
 * path that has just come into existence.
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class TransientAwarePathStoreObjectPersister extends PathStoreObjectPersister {

	private static final ThreadLocal<Boolean> CREATING_OBJECT = new ThreadLocal<>();

	/** {@inheritDoc} */
	@Override
	protected ModelObject newObject(ModelPersistenceContext context, String objectTypeId, String objectId, boolean addToCache)
			throws ModelObjectPersisterException {
		CREATING_OBJECT.set(Boolean.TRUE);
		try {
			return super.newObject(context, objectTypeId, objectId, addToCache);
		} finally {
			CREATING_OBJECT.remove();
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void updateClusterCachePath(String path) {
		if (Boolean.TRUE.equals(CREATING_OBJECT.get())) {
			return;
		}
		super.updateClusterCachePath(path);
	}
}
