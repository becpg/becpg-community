/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.entity.policy;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * Generate documents when product properties are updated.
 * 
 * @author querephi, matthieu
 */
@Service
public class EntityReportPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnCreateAssociationPolicy, NodeServicePolicies.OnDeleteAssociationPolicy,
		NodeServicePolicies.OnUpdatePropertiesPolicy, ContentServicePolicies.OnContentUpdatePolicy {

	private static final int BATCH_SIZE = 25;

	/** The logger. */
	private static Log logger = LogFactory.getLog(EntityReportPolicy.class);

	/** The transaction service. */
	private TransactionService transactionService;

	/** The thread pool executor. */
	private ThreadPoolExecutor threadExecuter;

	/** The entityReportService **/
	private EntityReportService entityReportService;

	private BeCPGSearchService beCPGSearchService;

	/**
	 * Sets the transaction service.
	 * 
	 * @param transactionService
	 *            the new transaction service
	 */
	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	/**
	 * @param threadExecuter
	 *            the threadExecuter to set
	 */
	public void setThreadExecuter(ThreadPoolExecutor threadExecuter) {
		this.threadExecuter = threadExecuter;
	}

	/**
	 * @param entityReportService
	 *            the entityReportService to set
	 */
	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	/**
	 * Inits the.
	 */
	public void doInit() {

		logger.debug("Init EntityReportPolicy...");

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, ReportModel.ASPECT_REPORT_ENTITY, new JavaBehaviour(this,
				"onCreateAssociation"));

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, ReportModel.ASPECT_REPORT_ENTITY, new JavaBehaviour(this,
				"onDeleteAssociation"));

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ReportModel.ASPECT_REPORT_ENTITY, new JavaBehaviour(this, "onUpdateProperties"));

		// report Tpl policies

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, ReportModel.TYPE_REPORT, ReportModel.ASSOC_REPORT_TPL, new JavaBehaviour(
				this, "onDeleteAssociation"));

		// policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME,
		// ReportModel.TYPE_REPORT_TPL, new JavaBehaviour(this,
		// "beforeDeleteNode"));

		policyComponent.bindClassBehaviour(ContentServicePolicies.OnContentUpdatePolicy.QNAME, ReportModel.TYPE_REPORT_TPL, new JavaBehaviour(this, "onContentUpdate",
				NotificationFrequency.TRANSACTION_COMMIT));
	}

	@Override
	public void onCreateAssociation(AssociationRef assocRef) {

		onUpdateProduct(assocRef.getSourceRef());
	}

	@Override
	public void onDeleteAssociation(AssociationRef assocRef) {

		if (ReportModel.ASSOC_REPORT_TPL.equals(assocRef.getTypeQName())) {
			if (!nodeService.hasAspect(assocRef.getSourceRef(), ContentModel.ASPECT_PENDING_DELETE)) {

				if (logger.isDebugEnabled()) {
					logger.debug("Policy delete report " + assocRef.getSourceRef() + " - name: " + nodeService.getProperty(assocRef.getSourceRef(), ContentModel.PROP_NAME));
				}
				nodeService.deleteNode(assocRef.getSourceRef());
			}
		} else {
			onUpdateProduct(assocRef.getSourceRef());
		}
	}

	private void onUpdateProduct(NodeRef entityNodeRef) {

		queueNode(entityNodeRef);
	}

	@Override
	public void onUpdateProperties(NodeRef entityNodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

		for (Map.Entry<QName, Serializable> kv : after.entrySet()) {

			boolean hasChanged = false;

			if (kv.getValue() == null) {
				if (before.get(kv.getKey()) != null) {
					hasChanged = true;
				}
			} else if (!kv.getValue().equals(before.get(kv.getKey()))) {
				hasChanged = true;
			}

			// generate report depending of properties updated
			if (hasChanged && !ReportModel.PROP_REPORT_ENTITY_GENERATED.equals(kv.getKey()) && !ContentModel.PROP_MODIFIED.equals(kv.getKey())
					&& !ContentModel.PROP_MODIFIER.equals(kv.getKey()) && !ContentModel.PROP_VERSION_LABEL.equals(kv.getKey())
					&& !ContentModel.PROP_LAST_THUMBNAIL_MODIFICATION_DATA.equals(kv.getKey())) {

				if (logger.isDebugEnabled()) {
					logger.debug("Generate report since prop has changed. Prop: " + kv.getKey() + " before: " + before.get(kv.getKey()) + " after: " + kv.getValue());
				}

				onUpdateProduct(entityNodeRef);
				return;
			}
		}
	}

	@Override
	protected void doAfterCommit(String key, Set<NodeRef> pendingNodes) {

			Runnable runnable = new ProductReportGenerator(pendingNodes, AuthenticationUtil.getSystemUserName());
			threadExecuter.execute(runnable);

	}

	/**
	 * The Class ProductReportGenerator.
	 * 
	 * @author querephi
	 */
	private class ProductReportGenerator implements Runnable {

		/** The product node ref. */
		private Set<NodeRef> entityNodeRefs;
		private String runAsUser;

		/**
		 * Instantiates a new product report generator.
		 * 
		 * @param entityNodeRef
		 *            the product node ref
		 */
		private ProductReportGenerator(Set<NodeRef> entityNodeRefs, String runAsUser) {
			this.entityNodeRefs = entityNodeRefs;
			this.runAsUser = runAsUser;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			try {
				RunAsWork<Object> actionRunAs = new RunAsWork<Object>() {
					@Override
					public Object doWork() throws Exception {
						RetryingTransactionCallback<Object> actionCallback = new RetryingTransactionCallback<Object>() {
							@Override
							public Object execute() {
								for (NodeRef entityNodeRef : entityNodeRefs) {
									if (nodeService.exists(entityNodeRef) && !nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_TPL) && isNotLocked(entityNodeRef)
											&& !isVersionStoreNode(entityNodeRef)) {

										try {
											// Ensure that the policy doesn't
											// refire for this node
											// on this thread
											// This won't prevent background
											// processes from
											// refiring, though
											policyBehaviourFilter.disableBehaviour(entityNodeRef, ReportModel.ASPECT_REPORT_ENTITY);
											policyBehaviourFilter.disableBehaviour(entityNodeRef, ContentModel.ASPECT_AUDITABLE);
											policyBehaviourFilter.disableBehaviour(entityNodeRef, ContentModel.ASPECT_VERSIONABLE);

											if (logger.isDebugEnabled()) {
												logger.info("generate report: " + entityNodeRef + " - " + nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME));
											}
											entityReportService.generateReport(entityNodeRef);

										} finally {
											policyBehaviourFilter.enableBehaviour(entityNodeRef, ReportModel.ASPECT_REPORT_ENTITY);
											policyBehaviourFilter.enableBehaviour(entityNodeRef, ContentModel.ASPECT_AUDITABLE);
											policyBehaviourFilter.enableBehaviour(entityNodeRef, ContentModel.ASPECT_VERSIONABLE);
										}
									}
								}
								return null;
							}
						};
						return transactionService.getRetryingTransactionHelper().doInTransaction(actionCallback);
					}
				};
				AuthenticationUtil.runAs(actionRunAs, runAsUser);
			} catch (Throwable e) {
				logger.error("Unable to generate product reports ", e);
			}
		}
	}

	@Override
	public void onContentUpdate(NodeRef nodeRef, boolean newContent) {

		if (nodeService.exists(nodeRef)) {
			Boolean isSystem = (Boolean) nodeService.getProperty(nodeRef, ReportModel.PROP_REPORT_TPL_IS_SYSTEM);
			QName classType = (QName) nodeService.getProperty(nodeRef, ReportModel.PROP_REPORT_TPL_CLASS_NAME);

			if (isSystem != null && isSystem && classType != null) {

				String query = LuceneHelper.mandatory(LuceneHelper.getCondType(classType)) + 
								LuceneHelper.mandatory(LuceneHelper.getCondAspect(ReportModel.ASPECT_REPORT_ENTITY)) +
								LuceneHelper.exclude(LuceneHelper.getCondAspect(BeCPGModel.ASPECT_COMPOSITE_VERSION));

				List<NodeRef> entityNodeRefs = beCPGSearchService.luceneSearch(query);

				for (List<NodeRef> batch : Lists.partition(entityNodeRefs,BATCH_SIZE)) {
					Runnable runnable = new ProductReportGenerator(new HashSet<>(batch), AuthenticationUtil.getSystemUserName());
					threadExecuter.execute(runnable);
				}
			}
		}
	}
}
