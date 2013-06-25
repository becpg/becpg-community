/*
 * 
 */
package fr.becpg.repo.workflow.activiti.nc;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.alfresco.email.server.EmailServerModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.repo.workflow.activiti.BaseJavaDelegate;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.QualityModel;
import fr.becpg.repo.entity.AutoNumService;
import fr.becpg.repo.quality.NonConformityService;
import fr.becpg.repo.workflow.activiti.nc.NCWorkflowUtils.NCWorkflowUtilsTask;

/**
 * Create the NC based on NC WF data
 * 
 * @author "Philippe QUÉRÉ <philippe.quere@becpg.fr>"
 * 
 */
public class CreateNC extends BaseJavaDelegate {

	private static Log logger = LogFactory.getLog(CreateNC.class);

	private NodeService nodeService;

	private ServiceRegistry serviceRegistry;

	private NonConformityService nonConformityService;
	private AutoNumService autoNumService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}


	
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}



	public void setNonConformityService(NonConformityService nonConformityService) {
		this.nonConformityService = nonConformityService;
	}

	public void setAutoNumService(AutoNumService autoNumService) {
		this.autoNumService = autoNumService;
	}

	@Override
	public void execute(final DelegateExecution task) throws Exception {

		RunAsWork<Object> actionRunAs = new RunAsWork<Object>() {
			@Override
			public Object doWork() throws Exception {
				try {

					// product
					NodeRef productNodeRef = null;
				

					String ncType = (String) task.getVariable("ncwf_ncType");

					if (ncType == null) {
						ncType = QualityModel.NC_TYPE_NONCONFORMITY;
					}

					NodeRef parentNodeRef = nonConformityService.getStorageFolder(productNodeRef);

					// create nc
					// force name YYYY-NCCode, is there a better way ?
					String ncName = Calendar.getInstance().get(Calendar.YEAR) + "-" + (QualityModel.NC_TYPE_CLAIM.equals(ncType) ? "RC-" : "NC-")
							+ autoNumService.getAutoNumValue(QualityModel.TYPE_NC, BeCPGModel.PROP_CODE);

					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(ContentModel.PROP_NAME, ncName);
					properties.put(QualityModel.PROP_NC_TYPE, ncType);
					properties.put(ContentModel.PROP_DESCRIPTION, (String) task.getVariable("bpm_workflowDescription"));
					properties.put(QualityModel.PROP_NC_PRIORITY, (Integer) task.getVariable("bpm_priority"));

					NodeRef ncNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(ncName)), QualityModel.TYPE_NC, properties).getChildRef();

					NodeRef briefNodeRef = NCWorkflowUtils.getDocumentsFolder(ncNodeRef, serviceRegistry);
					
					if(briefNodeRef!=null){
						Map<QName, Serializable> emailableProperties = new HashMap<QName, Serializable>();
						emailableProperties.put(EmailServerModel.PROP_ALIAS, ncName);
						nodeService.addAspect(briefNodeRef, EmailServerModel.ASPECT_ALIASABLE, emailableProperties);
					}

					NCWorkflowUtils.updateNC(ncNodeRef, new NCWorkflowUtilsTask() {
						
						public Object getVariable(String name) {
							return task.getVariable(name);
						}
					}, serviceRegistry);

					NodeRef pkgNodeRef = ((ActivitiScriptNode) task.getVariable("bpm_package")).getNodeRef();

					String localName = QName.createValidLocalName(ncName);
					QName qName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, localName);

					nodeService.addChild(pkgNodeRef, ncNodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS, qName);

				} catch (Exception e) {
					logger.error("Failed to create nc", e);
					throw e;
				}

				return null;
			}

		};
		AuthenticationUtil.runAs(actionRunAs, AuthenticationUtil.getSystemUserName());
	}
}
