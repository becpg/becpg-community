/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG.
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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.util.StopWatch;

import fr.becpg.model.ReportModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.mail.BeCPGMailService;
import fr.becpg.repo.report.entity.EntityReportAsyncGenerator;
import fr.becpg.repo.report.entity.EntityReportService;

/**
 *
 * @author matthieu
 *
 */
public class EntityReportAsyncGeneratorImpl implements EntityReportAsyncGenerator {

	private static final Log logger = LogFactory.getLog(EntityReportAsyncGeneratorImpl.class);

	private ThreadPoolExecutor threadExecuter;

	private EntityReportService entityReportService;

	@Autowired
	private BeCPGMailService beCPGMailService;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private TransactionService transactionService;

	public void setThreadExecuter(ThreadPoolExecutor threadExecuter) {
		this.threadExecuter = threadExecuter;
	}

	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}

	@Override
	public void queueNodes(List<NodeRef> pendingNodes, boolean notify) {
		logger.debug("Queue nodes for async report generation");

		EntityReportAsyncNotificationCallback callBack = null;

		if (notify) {
			callBack = new EntityReportAsyncNotificationCallback(new HashSet<>(pendingNodes));
		}

		for (NodeRef entityNodeRef : pendingNodes) {

			Runnable command = new ProductReportGenerator(entityNodeRef, callBack);
			if (!threadExecuter.getQueue().contains(command)) {
				threadExecuter.execute(command);
			} else {
				if (callBack != null) {
					callBack.notify(entityNodeRef);
				}

				logger.warn("Report job already in queue for " + entityNodeRef);
				logger.info("Report active task size " + threadExecuter.getActiveCount());
				logger.info("Report queue size " + threadExecuter.getTaskCount());
			}

		}

	}


	private class EntityReportAsyncNotificationCallback {

		Set<NodeRef> workingSet = Collections.synchronizedSet(new HashSet<NodeRef>());

		private StopWatch watch = new StopWatch();
		private String userName = AuthenticationUtil.getFullyAuthenticatedUser();

		public EntityReportAsyncNotificationCallback(Set<NodeRef> workingSet) {
			super();
			this.workingSet = workingSet;
			watch.start();
		}

		public void notify(NodeRef nodeRef) {
			workingSet.remove(nodeRef);

			if (workingSet.isEmpty()) {
				// Send mail after refresh
				watch.stop();

				AuthenticationUtil.runAs(() -> {
					transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

						beCPGMailService.sendMailOnAsyncAction(userName, "generate-reports", null ,
								true, watch.getTotalTimeSeconds());

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

		private ProductReportGenerator(NodeRef entityNodeRef, EntityReportAsyncNotificationCallback callback) {
			this.entityNodeRef = entityNodeRef;
			this.callback = callback;
		}

		@Override
		public void run() {
			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
				try {
					entityReportService.generateReports(entityNodeRef);
				} catch (Exception e) {
					if (e instanceof ConcurrencyFailureException) {
						throw (ConcurrencyFailureException) e;
					}
					logger.error("Unable to generate product reports ", e);

				} finally {

					if (callback != null) {
						callback.notify(entityNodeRef);
					}
				}
				return null;
			}, false, true);
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
