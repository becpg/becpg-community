/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG. 
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.StopWatch;

import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.mail.BeCPGMailService;
import fr.becpg.repo.report.entity.EntityReportAsyncGenerator;
import fr.becpg.repo.report.entity.EntityReportService;

/**
 * 
 * @author matthieu
 * 
 */
public class EntityReportAsyncGeneratorImpl implements EntityReportAsyncGenerator {

	@Autowired
	private BeCPGMailService beCPGMailService;

	@Autowired
	private PersonService personService;
	
	@Autowired
	private NodeService nodeService;

	private ThreadPoolExecutor threadExecuter;

	private EntityReportService entityReportService;

	private static final Log logger = LogFactory.getLog(EntityReportAsyncGeneratorImpl.class);

	public void setThreadExecuter(ThreadPoolExecutor threadExecuter) {
		this.threadExecuter = threadExecuter;
	}

	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}

	@Override
	public void queueNodes(List<NodeRef> pendingNodes) {
		logger.debug("Queue nodes for async report generation");

		StopWatch watch = new StopWatch();
		watch.start();
		boolean runState = false;
		String userName = AuthenticationUtil.getFullyAuthenticatedUser();

		for (NodeRef entityNodeRef : pendingNodes) {

			Runnable command = new ProductReportGenerator(entityNodeRef);
			if (!threadExecuter.getQueue().contains(command)) {
				threadExecuter.execute(command);
			} else {
				logger.warn("Report job already in queue for " + entityNodeRef);
				logger.info("Report active task size " + threadExecuter.getActiveCount());
				logger.info("Report queue size " + threadExecuter.getTaskCount());
			}
		}

		threadExecuter.shutdown();
		try {
			while (!threadExecuter.awaitTermination(5, TimeUnit.SECONDS)) {
				logger.debug("Wait generate reports...");
			}
			runState = true;

		} catch (InterruptedException e) {
			logger.error("Unable to send email", e);

		} finally {
			// Send Mail after reports refresh
			watch.stop();
			if (watch.getTotalTimeSeconds() > 0) {
				Map<String, Object> templateArgs = new HashMap<>();
				templateArgs.put(RepoConsts.ARG_ACTION_BODY, I18NUtil.getMessage("message.async-mail.generate-reports.body"));
				templateArgs.put(RepoConsts.ARG_ACTION_URL, "page/document-details?nodeRef=" + getEntityTplNodeRef(pendingNodes.get(0)));
				templateArgs.put(RepoConsts.ARG_ACTION_STATE, runState);
				templateArgs.put(RepoConsts.ARG_ACTION_RUN_TIME, watch.getTotalTimeSeconds());

				List<NodeRef> recipientNodeRefs = new ArrayList<>();
				recipientNodeRefs.add(personService.getPerson(userName));
				Map<String, Object> templateModel = new HashMap<>();
				templateModel.put("args", templateArgs);
				String subject = "[Notification]" + I18NUtil.getMessage("message.async-mail.generate-reports.subject");

				Runnable mailTask = () -> {
					AuthenticationUtil.runAs(() -> {
						beCPGMailService.sendMail(recipientNodeRefs, subject, RepoConsts.EMAIL_ASYNC_ACTIONS_TEMPLATE, templateModel, true);
						return null;
					}, userName);
				};

				new Thread(mailTask).start();
			}

		}

	}
	
	private NodeRef getEntityTplNodeRef(NodeRef entityNodeRef){
		List<AssociationRef> targetReports = nodeService.getTargetAssocs(entityNodeRef, ReportModel.ASSOC_REPORTS);
		List<AssociationRef> targetReportTpls = nodeService.getTargetAssocs( targetReports.get(0).getTargetRef(), ReportModel.ASSOC_REPORT_TPL);
		return targetReportTpls.get(0).getTargetRef();
	}

	private class ProductReportGenerator implements Runnable {

		private final NodeRef entityNodeRef;

		private ProductReportGenerator(NodeRef entityNodeRef) {
			this.entityNodeRef = entityNodeRef;
		}

		@Override
		public void run() {
			try {
				entityReportService.generateReports(entityNodeRef);
			} catch (Exception e) {
				if (e instanceof ConcurrencyFailureException) {
					throw (ConcurrencyFailureException) e;
				}
				logger.error("Unable to generate product reports ", e);

			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((entityNodeRef == null) ? 0 : entityNodeRef.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ProductReportGenerator other = (ProductReportGenerator) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (entityNodeRef == null) {
				if (other.entityNodeRef != null)
					return false;
			} else if (!entityNodeRef.equals(other.entityNodeRef))
				return false;
			return true;
		}

		private EntityReportAsyncGeneratorImpl getOuterType() {
			return EntityReportAsyncGeneratorImpl.this;
		}

	}
}
