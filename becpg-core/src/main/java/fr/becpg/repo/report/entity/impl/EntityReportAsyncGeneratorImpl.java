package fr.becpg.repo.report.entity.impl;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import fr.becpg.repo.report.entity.EntityReportAsyncGenerator;
import fr.becpg.repo.report.entity.EntityReportService;

/**
 * 
 * @author matthieu
 *
 */
public class EntityReportAsyncGeneratorImpl implements EntityReportAsyncGenerator {

	private TransactionService transactionService;

	private ThreadPoolExecutor threadExecuter;

	private EntityReportService entityReportService;

	private ConcurrentLinkedQueue<NodeRef> reportsQueue = new ConcurrentLinkedQueue<>();

	private static Log logger = LogFactory.getLog(EntityReportAsyncGeneratorImpl.class);

	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	public void setThreadExecuter(ThreadPoolExecutor threadExecuter) {
		this.threadExecuter = threadExecuter;
	}

	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}

	@Override
	public void queueNodes(List<NodeRef> pendingNodes) {

		for (List<NodeRef> subList : Lists.partition(pendingNodes, 100)) {
			Runnable runnable = new ProductReportGenerator(subList);
			threadExecuter.execute(runnable);
		}

	}

	private class ProductReportGenerator implements Runnable {

		private List<NodeRef> entityNodeRefs;

		private ProductReportGenerator(List<NodeRef> entityNodeRefs) {
			this.entityNodeRefs = entityNodeRefs;
		}

		@Override
		public void run() {
			for (final NodeRef entityNodeRef : entityNodeRefs) {

				if (!reportsQueue.contains(entityNodeRef)) {
					try {
						reportsQueue.add(entityNodeRef);
						RunAsWork<Object> actionRunAs = new RunAsWork<Object>() {
							@Override
							public Object doWork() throws Exception {
								RetryingTransactionCallback<Object> actionCallback = new RetryingTransactionCallback<Object>() {
									@Override
									public Object execute() {
										entityReportService.generateReport(entityNodeRef);
										return null;
									}
								};
								return transactionService.getRetryingTransactionHelper().doInTransaction(actionCallback, false, true);
							}
						};
						AuthenticationUtil.runAs(actionRunAs, AuthenticationUtil.getSystemUserName());

					} catch (Exception e) {
						logger.error("Unable to generate product reports ", e);
					} finally {
						reportsQueue.remove(entityNodeRef);
					}

				} else {
					logger.warn("NodeRef already in queue: " + entityNodeRef);
				}
			}

		}
	}
}
