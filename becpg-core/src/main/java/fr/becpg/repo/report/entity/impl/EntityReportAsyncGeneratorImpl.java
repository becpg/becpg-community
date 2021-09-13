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
package fr.becpg.repo.report.entity.impl;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.mail.BeCPGMailService;
import fr.becpg.repo.report.entity.EntityReportAsyncGenerator;
import fr.becpg.repo.report.entity.EntityReportService;

/**
 * <p>
 * EntityReportAsyncGeneratorImpl class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class EntityReportAsyncGeneratorImpl implements EntityReportAsyncGenerator {

	private static final Log logger = LogFactory.getLog(EntityReportAsyncGeneratorImpl.class);

	private ThreadPoolExecutor threadExecuter;

	private EntityReportService entityReportService;

	@Autowired
	private BeCPGMailService beCPGMailService;

	@Autowired
	private TransactionService transactionService;
	
	@Autowired
	private EntityVersionService entityVersionService;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private TenantAdminService tenantAdminService;

	/**
	 * <p>
	 * Setter for the field <code>threadExecuter</code>.
	 * </p>
	 *
	 * @param threadExecuter
	 *            a {@link java.util.concurrent.ThreadPoolExecutor} object.
	 */
	public void setThreadExecuter(ThreadPoolExecutor threadExecuter) {
		this.threadExecuter = threadExecuter;
	}

	/**
	 * <p>
	 * Setter for the field <code>entityReportService</code>.
	 * </p>
	 *
	 * @param entityReportService
	 *            a {@link fr.becpg.repo.report.entity.EntityReportService}
	 *            object.
	 */
	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}

	/** {@inheritDoc} */
	@Override
	public void queueNodes(List<NodeRef> pendingNodes, boolean notify) {
		logger.debug("Queue nodes for async report generation");

		EntityReportAsyncNotificationCallback callBack = null;

		if (notify) {
			callBack = new EntityReportAsyncNotificationCallback();
		}

		for (NodeRef entityNodeRef : pendingNodes) {

			Runnable command = new ProductReportGenerator(entityNodeRef, callBack, tenantAdminService.getCurrentUserDomain());
			if (!threadExecuter.getQueue().contains(command)) {

				threadExecuter.execute(command);
			} else {

				logger.warn("Report job already in queue for " + entityNodeRef);
				logger.info("Report active task size " + threadExecuter.getActiveCount());
				logger.info("Report queue size " + threadExecuter.getTaskCount());
			}

		}

	}

	private class EntityReportAsyncNotificationCallback {

		private AtomicInteger counter;
		private Long timeStamp;
		private String userName;

		public EntityReportAsyncNotificationCallback() {

			this.counter = new AtomicInteger(0);
			this.timeStamp = Calendar.getInstance().getTimeInMillis();
			this.userName = AuthenticationUtil.getFullyAuthenticatedUser();
		}

		public void register() {
			counter.incrementAndGet();
		}

		public void notifyEnd() {

			if (counter.decrementAndGet() == 0) {
				AuthenticationUtil.runAs(() -> {
					transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

						beCPGMailService.sendMailOnAsyncAction(userName, "generate-reports", null, true,
								(Calendar.getInstance().getTimeInMillis() - timeStamp) / 1000d);

						return null;
					}, true, true);
					return null;
				}, userName);

			}
		}
	}

	private class ProductReportGenerator implements Runnable {

		private final NodeRef entityNodeRef;

		private final EntityReportAsyncNotificationCallback callback;
		
		private final String tenantDomain;
		
		private ProductReportGenerator(NodeRef entityNodeRef, EntityReportAsyncNotificationCallback callback, String tenantDomain) {
			this.entityNodeRef = entityNodeRef;
			this.callback = callback;
			if (callback != null) {
				this.callback.register();
			}
			this.tenantDomain = tenantDomain;
		}
		
		@Override
		public void run() {
			try {

				String systemUserName = tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain);
				String adminUserName = tenantAdminService.getDomainUser(AuthenticationUtil.getAdminUserName(), tenantDomain);

				final NodeRef extractedNode = AuthenticationUtil.runAs(() -> transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

					if (entityVersionService.isVersion(entityNodeRef)
							&& nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_ENTITY_FORMAT) != null) {
						return entityVersionService.extractVersion(entityNodeRef);
					}

					return entityNodeRef;

				}, false, true), systemUserName);

				AuthenticationUtil.runAs(() -> {
					transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

						entityReportService.generateReports(extractedNode, entityNodeRef);

						return null;

					}, false, true);
					return null;
				}, adminUserName);

			} catch (Exception e) {

				logger.error("Unable to generate product reports ", e);

			}
			if (callback != null) {
				callback.notifyEnd();
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = (prime * result) + getOuterType().hashCode();
			result = (prime * result) + ((entityNodeRef == null) ? 0 : entityNodeRef.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			ProductReportGenerator other = (ProductReportGenerator) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (entityNodeRef == null) {
				if (other.entityNodeRef != null) {
					return false;
				}
			} else if (!entityNodeRef.equals(other.entityNodeRef)) {
				return false;
			}
			return true;
		}

		private EntityReportAsyncGeneratorImpl getOuterType() {
			return EntityReportAsyncGeneratorImpl.this;
		}

	}
}
