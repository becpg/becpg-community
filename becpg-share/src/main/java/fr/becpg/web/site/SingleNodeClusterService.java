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

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.extensions.surf.ClusterMessageAware;
import org.springframework.extensions.surf.ClusterService;

/**
 * Delivers Surf cluster messages inside the current JVM.
 * <p>
 * Surf caches its configuration objects in partitions keyed on the domain of the current user id,
 * and a write only refreshes the partition of the user performing it. On an installation whose user
 * ids are e-mail addresses that domain is not a tenant, so an administrator changing the theme, the
 * logo or another user's dashboard leaves the other partitions holding the previous configuration,
 * which is never revalidated because Share runs in production mode.
 * <p>
 * Surf already solves this with a cache invalidation message clearing the written path from every
 * partition, but only a clustered deployment provides the
 * {@link org.springframework.extensions.surf.ClusterService} that carries it. This implementation
 * delivers those messages to the local handlers so that a single node Share benefits from the same
 * invalidation.
 * <p>
 * The invalidation stays correct under Alfresco multi-tenancy: a message only removes a path from
 * the partitions, it never lets a tenant read another tenant's entry. A tenant whose entry is
 * dropped simply reads it again from its own store.
 * <p>
 * Thread-safe: handlers are held in a {@link java.util.concurrent.ConcurrentHashMap} and published
 * messages are dispatched without further synchronization.
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class SingleNodeClusterService implements ClusterService, BeanPostProcessor {

	private static final Log logger = LogFactory.getLog(SingleNodeClusterService.class);

	private final Map<String, ClusterMessageAware> handlers = new ConcurrentHashMap<>();

	/** {@inheritDoc} */
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof ClusterMessageAware clusterAware) {
			register(clusterAware);
		}
		return bean;
	}

	/**
	 * Registers a handler and hands it this service so that it starts publishing its messages.
	 *
	 * @param clusterAware the handler to register
	 */
	private void register(ClusterMessageAware clusterAware) {
		String messageType = clusterAware.getClusterMessageType();
		if (messageType == null) {
			return;
		}

		handlers.put(messageType, clusterAware);
		clusterAware.setClusterService(this);

		if (logger.isDebugEnabled()) {
			logger.debug("Registered local cluster message handler for type: " + messageType);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void publishClusterMessage(String messageType, Map<String, Serializable> payload) {
		ClusterMessageAware handler = handlers.get(messageType);
		if (handler == null) {
			return;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Delivering local cluster message of type: " + messageType);
		}

		handler.onClusterMessage(payload);
	}
}
