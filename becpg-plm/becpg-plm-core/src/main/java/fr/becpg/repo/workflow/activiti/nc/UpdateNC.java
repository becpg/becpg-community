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
import org.alfresco.service.namespace.RegexQNamePattern;

import fr.becpg.model.QualityModel;

/**
 * Update the NC based on NC WF data, need a java class to avoid problem with
 * retrying transaction
 *
 * @author "Philippe QUÉRÉ"
 * @version $Id: $Id
 */
public class UpdateNC extends ScriptTaskListener {

	private static final long serialVersionUID = 7309540412175915634L;

	/** {@inheritDoc} */
	@Override
	public void notify(final DelegateTask task) {

		final NodeRef pkgNodeRef = ((ActivitiScriptNode) task.getVariable("bpm_package")).getNodeRef();

		// use retrying transaction to avoid exception
		RetryingTransactionCallback<List<String>> actionCallback = () -> {
			// update state and comments
			List<ChildAssociationRef> childAssocs = getServiceRegistry().getNodeService().getChildAssocs(pkgNodeRef,
					WorkflowModel.ASSOC_PACKAGE_CONTAINS, RegexQNamePattern.MATCH_ALL);
			for (ChildAssociationRef childAssoc : childAssocs) {

				if (QualityModel.TYPE_NC.equals(getServiceRegistry().getNodeService().getType(childAssoc.getChildRef()))) {

					NodeRef ncNodeRef = childAssoc.getChildRef();

					NCWorkflowUtils.updateNC(ncNodeRef, new fr.becpg.repo.workflow.activiti.nc.NCWorkflowUtils.NCWorkflowUtilsTask() {

						@Override
						public Object getVariable(String name) {

							return task.getVariable(name);
						}

						@Override
						public Set<String> getVariableNames() {
							return task.getVariableNames();
						}

					}, getServiceRegistry());
				}
			}

			return null;
		};
		getServiceRegistry().getRetryingTransactionHelper().doInTransaction(actionCallback, false, true);
	}
}
