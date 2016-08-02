/*
 * 
 */
package fr.becpg.repo.workflow.activiti.product;

import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.repo.workflow.activiti.BaseJavaDelegate;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.ConcurrencyFailureException;

import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;

/**
 * 
 * @author matthieu
 * 
 */
public class ValidateProduct extends BaseJavaDelegate {

	private static final Log logger = LogFactory.getLog(ValidateProduct.class);

	private NodeService nodeService;
	private DictionaryService dictionaryService;
	private OwnableService ownableService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setOwnableService(OwnableService ownableService) {
		this.ownableService = ownableService;
	}

	@Override
	public void execute(final DelegateExecution task) throws Exception {

		logger.debug("start ApproveActionHandler");

		RunAsWork<Object> actionRunAs = new RunAsWork<Object>() {
			@Override
			public Object doWork() throws Exception {
				try {
					NodeRef pkgNodeRef = ((ActivitiScriptNode) task.getVariable("bpm_package")).getNodeRef();
					
					// change state and classify products
					List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(pkgNodeRef,
							WorkflowModel.ASSOC_PACKAGE_CONTAINS, RegexQNamePattern.MATCH_ALL);
					for (ChildAssociationRef childAssoc : childAssocs) {

						NodeRef nodeRef = childAssoc.getChildRef();
						QName nodeType = nodeService.getType(nodeRef);

						if (dictionaryService.isSubClass(nodeType, PLMModel.TYPE_PRODUCT)) {
							nodeService.setProperty(nodeRef, PLMModel.PROP_PRODUCT_STATE, SystemState.Valid);

							// productNodeRef : remove all owner related rights
							ownableService.setOwner(nodeRef, OwnableService.NO_OWNER);
						}
					}
				} catch (Exception e) {
					if (e instanceof ConcurrencyFailureException) {
						throw (ConcurrencyFailureException) e;
					}
					logger.error("Failed to approve product", e);
					throw e;
				}

				return null;
			}
		};
		AuthenticationUtil.runAsSystem(actionRunAs);

		logger.debug("end ApproveActionHandler");
	}
}
