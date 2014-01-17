/*
 * 
 */
package fr.becpg.repo.workflow.activiti.nc;

import java.util.List;
import java.util.Set;

import org.activiti.engine.delegate.DelegateTask;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.repo.workflow.activiti.tasklistener.ScriptTaskListener;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;

import fr.becpg.model.QualityModel;

/**
 * Update the NC based on NC WF data, need a java class to avoid problem with
 * retrying transaction
 * 
 * @author "Philippe QUÉRÉ <philippe.quere@becpg.fr>"
 * 
 */
public class UpdateNC extends ScriptTaskListener {


	private NodeService nodeService;
	private TransactionService transactionService;

	@Override
	public void notify(final DelegateTask task) {

		nodeService = getServiceRegistry().getNodeService();
		transactionService = getServiceRegistry().getTransactionService();

		final NodeRef pkgNodeRef = ((ActivitiScriptNode) task.getVariable("bpm_package")).getNodeRef();

		// use retrying transaction to avoid exception
		// ConcurrencyFailureException
		RetryingTransactionCallback<List<String>> actionCallback = new RetryingTransactionCallback<List<String>>() {
			@Override
			public List<String> execute() throws Exception {
				// update state and comments
				List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(pkgNodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS, RegexQNamePattern.MATCH_ALL);
				for (ChildAssociationRef childAssoc : childAssocs) {

					if (QualityModel.TYPE_NC.equals(nodeService.getType(childAssoc.getChildRef()))) {

						NodeRef ncNodeRef = childAssoc.getChildRef();

						NCWorkflowUtils.updateNC(ncNodeRef, new fr.becpg.repo.workflow.activiti.nc.NCWorkflowUtils.NCWorkflowUtilsTask() {
							
							public Object getVariable(String name) {
								
								return task.getVariable(name);
							}
							
							public Set<String> getVariableNames(){
								return task.getVariableNames();
							}
							
							
						} , getServiceRegistry());
					}
				}

				return null;
			}
		};
		transactionService.getRetryingTransactionHelper().doInTransaction(actionCallback, false, true);
	}
}
