/*
 * 
 */
package fr.becpg.repo.workflow.activiti.nc;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.repo.workflow.activiti.BaseJavaDelegate;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.QualityModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.quality.NonConformityService;

/**
 * Create the NC based on NC WF data
 * 
 * @author "Philippe QUÉRÉ <philippe.quere@becpg.fr>"
 * 
 */
public class CreateNC extends BaseJavaDelegate {

	private static Log logger = LogFactory.getLog(CreateNC.class);

	private static final String CM_URL = NamespaceService.CONTENT_MODEL_1_0_URI;

	private NodeService nodeService;
	private FileFolderService fileFolderService;
	private NonConformityService nonConformityService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public void setNonConformityService(NonConformityService nonConformityService) {
		this.nonConformityService = nonConformityService;
	}

	@Override
	public void execute(final DelegateExecution task) throws Exception {

		final NodeRef pkgNodeRef = ((ActivitiScriptNode) task.getVariable("bpm_package")).getNodeRef();

		RunAsWork<Object> actionRunAs = new RunAsWork<Object>() {
			@Override
			public Object doWork() throws Exception {
				try {

					// product
					NodeRef productNodeRef = null;
					ActivitiScriptNode node = (ActivitiScriptNode) task.getVariable("ncwf_product");
					if (node != null) {
						productNodeRef = node.getNodeRef();
					}

					NodeRef parentNodeRef = nonConformityService.getStorageFolder(productNodeRef);

					// create nc
					String ncName = GUID.generate();
					Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
					properties.put(ContentModel.PROP_NAME, ncName);
					properties.put(ContentModel.PROP_DESCRIPTION, (String) task.getVariable("bpm_workflowDescription"));
					properties.put(QualityModel.PROP_NC_PRIORITY, (Integer) task.getVariable("bpm_priority"));

					// batchId
					String batchId = (String) task.getVariable("ncwf_batchId");
					if (batchId != null && !batchId.isEmpty()) {
						properties.put(QualityModel.PROP_BATCH_ID, batchId);
					}

					NodeRef ncNodeRef = nodeService.createNode(
							parentNodeRef,
							ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
									QName.createValidLocalName(ncName)), QualityModel.TYPE_NC, properties)
							.getChildRef();

					// force name YYYY-NCCode, is there a better way ?
					ncName = Calendar.getInstance().get(Calendar.YEAR) + "-"
							+ nodeService.getProperty(ncNodeRef, BeCPGModel.PROP_CODE);
					nodeService.setProperty(ncNodeRef, ContentModel.PROP_NAME, ncName);
					NodeRef entityFolderNodeRef = nodeService.getPrimaryParent(ncNodeRef).getParentRef();
					if (nodeService.getType(entityFolderNodeRef).equals(BeCPGModel.TYPE_ENTITY_FOLDER)) {
						nodeService.setProperty(entityFolderNodeRef, ContentModel.PROP_NAME, ncName);
					}

					// product
					if (productNodeRef != null) {
						nodeService.createAssociation(ncNodeRef, productNodeRef, QualityModel.ASSOC_PRODUCT);
					}

					// supplier
					node = (ActivitiScriptNode) task.getVariable("ncwf_supplier");
					if (node != null) {
						logger.debug("supplier selected");
						nodeService.createAssociation(ncNodeRef, node.getNodeRef(), BeCPGModel.ASSOC_SUPPLIERS);
					}

					// client
					node = (ActivitiScriptNode) task.getVariable("ncwf_client");
					if (node != null) {
						logger.debug("client selected");
						nodeService.createAssociation(ncNodeRef, node.getNodeRef(), BeCPGModel.ASSOC_CLIENTS);
					}

					// Move file from pkgNodeRef
					List<FileInfo> files = fileFolderService.listFiles(pkgNodeRef);
					NodeRef briefNodeRef = getDocumentsFolder(ncNodeRef);
					for (FileInfo file : files) {
						String name = (String) nodeService.getProperty(file.getNodeRef(), ContentModel.PROP_NAME);
						if (briefNodeRef != null) {
							fileFolderService.move(file.getNodeRef(), briefNodeRef, name);
							nodeService.removeChild(pkgNodeRef, file.getNodeRef());
						} else {
							logger.error("No documents folder found");
							break;
						}
					}

					String localName = QName.createValidLocalName(ncName);
					QName qName = QName.createQName(CM_URL, localName);

					nodeService.addChild(pkgNodeRef, ncNodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS, qName);

				} catch (Exception e) {
					logger.error("Failed to create nc", e);
					throw e;
				}

				return null;
			}

			private NodeRef getDocumentsFolder(NodeRef productNodeRef) {

				NodeRef parentEntityNodeRef = nodeService.getPrimaryParent(productNodeRef).getParentRef();

				for (FileInfo file : fileFolderService.listFolders(parentEntityNodeRef)) {
					if (file.getName().equals(TranslateHelper.getTranslatedPath(RepoConsts.PATH_DOCUMENTS))) {
						return file.getNodeRef();
					}
				}

				return null;
			}

		};
		AuthenticationUtil.runAs(actionRunAs, AuthenticationUtil.getSystemUserName());
	}
}
