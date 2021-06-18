/*
 *
 */
package fr.becpg.repo.workflow.activiti.product;

import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.repo.workflow.activiti.BaseJavaDelegate;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;

/**
 * <p>ValidateProduct class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ValidateProduct extends BaseJavaDelegate {

	private static final Log logger = LogFactory.getLog(ValidateProduct.class);

	private NodeService nodeService;
	private DictionaryService dictionaryService;

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>dictionaryService</code>.</p>
	 *
	 * @param dictionaryService a {@link org.alfresco.service.cmr.dictionary.DictionaryService} object.
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(final DelegateExecution task) throws Exception {

		logger.debug("start ApproveActionHandler");

		RunAsWork<Object> actionRunAs = () -> {
			try {
				NodeRef pkgNodeRef = ((ActivitiScriptNode) task.getVariable("bpm_package")).getNodeRef();

				// change state and classify products
				List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(pkgNodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS,
						RegexQNamePattern.MATCH_ALL);
				for (ChildAssociationRef childAssoc : childAssocs) {

					NodeRef nodeRef = childAssoc.getChildRef();
					QName nodeType = nodeService.getType(nodeRef);

					if (dictionaryService.isSubClass(nodeType, PLMModel.TYPE_PRODUCT)) {
						nodeService.setProperty(nodeRef, PLMModel.PROP_PRODUCT_STATE, SystemState.Valid);
					} else if (dictionaryService.isSubClass(nodeType, PLMModel.TYPE_CLIENT)) {
						nodeService.setProperty(nodeRef, PLMModel.PROP_CLIENT_STATE, SystemState.Valid);
					}
					if (dictionaryService.isSubClass(nodeType, PLMModel.TYPE_SUPPLIER)) {
						nodeService.setProperty(nodeRef, PLMModel.PROP_SUPPLIER_STATE, SystemState.Valid);
					}
				}
			} catch (Exception e) {
				if (RetryingTransactionHelper.extractRetryCause(e) == null) {
					logger.error("Failed to approve product", e);
				}
				throw e;
			}

			return null;
		};
		AuthenticationUtil.runAsSystem(actionRunAs);

		logger.debug("end ApproveActionHandler");
	}
}
