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
