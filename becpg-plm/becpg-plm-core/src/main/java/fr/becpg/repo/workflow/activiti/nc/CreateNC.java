/*
 * 
 */
package fr.becpg.repo.workflow.activiti.nc;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
import org.springframework.dao.ConcurrencyFailureException;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.QualityModel;
import fr.becpg.repo.entity.AutoNumService;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.quality.NonConformityService;
import fr.becpg.repo.workflow.activiti.nc.NCWorkflowUtils.NCWorkflowUtilsTask;

/**
 * Create the NC based on NC WF data
 *
 * @author "Philippe QUÉRÉ"
 * @version $Id: $Id
 */
public class CreateNC extends BaseJavaDelegate {

	private static final Log logger = LogFactory.getLog(CreateNC.class);

	private NodeService nodeService;
	private ServiceRegistry serviceRegistry;
	private NonConformityService nonConformityService;
	private AutoNumService autoNumService;
	private EntityService entityService;

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/** {@inheritDoc} */
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	/**
	 * <p>Setter for the field <code>nonConformityService</code>.</p>
	 *
	 * @param nonConformityService a {@link fr.becpg.repo.quality.NonConformityService} object.
	 */
	public void setNonConformityService(NonConformityService nonConformityService) {
		this.nonConformityService = nonConformityService;
	}

	/**
	 * <p>Setter for the field <code>autoNumService</code>.</p>
	 *
	 * @param autoNumService a {@link fr.becpg.repo.entity.AutoNumService} object.
	 */
	public void setAutoNumService(AutoNumService autoNumService) {
		this.autoNumService = autoNumService;
	}

	/**
	 * <p>Setter for the field <code>entityService</code>.</p>
	 *
	 * @param entityService a {@link fr.becpg.repo.entity.EntityService} object.
	 */
	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(final DelegateExecution task) throws Exception {

		RunAsWork<NodeRef> actionRunAs = new RunAsWork<NodeRef>() {
			@Override
			public NodeRef doWork() throws Exception {
				try {

					// product
					NodeRef productNodeRef = null;

					String ncType = (String) task.getVariable("ncwf_ncType");

					if (ncType == null) {
						ncType = QualityModel.NC_TYPE_NONCONFORMITY;
					}

					NodeRef parentNodeRef = nonConformityService.getStorageFolder(productNodeRef);

					String code = autoNumService.getAutoNumValue(QualityModel.TYPE_NC, BeCPGModel.PROP_CODE);

					// create nc
					// force name YYYY-NCCode, is there a better way ?
					String ncName = Calendar.getInstance().get(Calendar.YEAR) + "-" + (QualityModel.NC_TYPE_CLAIM.equals(ncType) ? "RC-" : "NC-")
							+ code;

					Map<QName, Serializable> properties = new HashMap<>();
					properties.put(ContentModel.PROP_NAME, ncName);
					properties.put(QualityModel.PROP_NC_TYPE, ncType);

					String description = (String) task.getVariable("bpm_workflowDescription");
					Integer priority = (Integer) task.getVariable("bpm_priority");
					if (description != null) {
						properties.put(ContentModel.PROP_DESCRIPTION, description);
					}
					if (priority != null) {
						properties.put(QualityModel.PROP_NC_PRIORITY, priority);
					} else {
						properties.put(QualityModel.PROP_NC_PRIORITY, 2);
					}
					properties.put(BeCPGModel.PROP_CODE, code);

					NodeRef ncNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(ncName)), QualityModel.TYPE_NC,
							properties).getChildRef();

					NodeRef briefNodeRef = entityService.getOrCreateDocumentsFolder(ncNodeRef);

					Map<QName, Serializable> emailableProperties = new HashMap<>();
					emailableProperties.put(EmailServerModel.PROP_ALIAS, ncName);
					nodeService.addAspect(briefNodeRef, EmailServerModel.ASPECT_ALIASABLE, emailableProperties);

					NCWorkflowUtils.updateNC(ncNodeRef, new NCWorkflowUtilsTask() {

						public Object getVariable(String name) {
							return task.getVariable(name);
						}

						@Override
						public Set<String> getVariableNames() {
							return task.getVariableNames();
						}
					}, serviceRegistry);

					NodeRef pkgNodeRef = ((ActivitiScriptNode) task.getVariable("bpm_package")).getNodeRef();

					String localName = QName.createValidLocalName(ncName);
					QName qName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, localName);

					nodeService.addChild(pkgNodeRef, ncNodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS, qName);

				} catch (Exception e) {
					if (e instanceof ConcurrencyFailureException) {
						throw (ConcurrencyFailureException) e;
					}
					logger.error("Failed to create nc", e);
					throw e;
				}

				return null;
			}

		};
		AuthenticationUtil.runAsSystem(actionRunAs);
	}
}
