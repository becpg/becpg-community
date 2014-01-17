/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.report.entity.EntityReportAsyncGenerator;
import fr.becpg.repo.report.entity.EntityReportService;

/**
 * 
 * @author matthieu
 * 
 */
public class EntityReportAsyncGeneratorImpl implements EntityReportAsyncGenerator {

	private ThreadPoolExecutor threadExecuter;

	private EntityReportService entityReportService;

	private static Log logger = LogFactory.getLog(EntityReportAsyncGeneratorImpl.class);

	public void setThreadExecuter(ThreadPoolExecutor threadExecuter) {
		this.threadExecuter = threadExecuter;
	}

	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}

	@Override
	public void queueNodes(List<NodeRef> pendingNodes) {
		for(NodeRef entityNodeRef : pendingNodes){
			
			Runnable command = new ProductReportGenerator(entityNodeRef);
			if(!threadExecuter.getQueue().contains(command)){
				threadExecuter.execute(command);
			} else {
				logger.warn("Report job already in queue for "+entityNodeRef);
				logger.info("Report active task size "+threadExecuter.getActiveCount());
				logger.info("Report queue size "+threadExecuter.getTaskCount());
			}
		}
	}

	private class ProductReportGenerator implements Runnable {

		private NodeRef entityNodeRef;

		private ProductReportGenerator(NodeRef entityNodeRef) {
			this.entityNodeRef = entityNodeRef;
		}

		@Override
		public void run() {
			try {
				entityReportService.generateReport(entityNodeRef);

			} catch (Exception e) {
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
