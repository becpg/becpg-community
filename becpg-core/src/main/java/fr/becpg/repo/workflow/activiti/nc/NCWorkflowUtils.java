package fr.becpg.repo.workflow.activiti.nc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.QualityModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.helper.impl.AssociationServiceImpl;

public class NCWorkflowUtils {

	private static Log logger = LogFactory.getLog(NCWorkflowUtils.class);

	public interface NCWorkflowUtilsTask {

		Object getVariable(String name);

	}

	public static void updateNC(NodeRef ncNodeRef, NCWorkflowUtilsTask task, ServiceRegistry serviceRegistry) throws FileExistsException, FileNotFoundException {

		// Crapy
		AssociationServiceImpl associationService = new AssociationServiceImpl();
		associationService.setNodeService(serviceRegistry.getNodeService());

		Map<QName, Serializable> properties = new HashMap<QName, Serializable>(2);
		if (task.getVariable("ncwf_ncState") != null) {
			properties.put(QualityModel.PROP_NC_STATE, (String) task.getVariable("ncwf_ncState"));
		}
		if (task.getVariable("bpm_comment") != null) {
			properties.put(QualityModel.PROP_NC_COMMENT, (String) task.getVariable("bpm_comment"));
		}

		for (QName aspectQname : new QName[] { QualityModel.ASPECT_BATCH, QualityModel.ASPECT_CLAIM_RESPONSE, QualityModel.ASPECT_CLAIM_TREATEMENT,
				QualityModel.ASPECT_CLAIM_CLOSING, QualityModel.ASPECT_CLAIM, BeCPGModel.ASPECT_CLIENTS, BeCPGModel.ASPECT_SUPPLIERS }) {

			AspectDefinition aspectDef = serviceRegistry.getDictionaryService().getAspect(aspectQname);
			for (QName propQname : aspectDef.getProperties().keySet()) {
				Serializable attr = (Serializable) task.getVariable(propQname.toPrefixString(serviceRegistry.getNamespaceService()).replaceFirst(":", "_"));
				if (attr != null) {
					properties.put(propQname, attr);
				}
			}

			for (QName assocQname : aspectDef.getAssociations().keySet()) {

				if (task.getVariable(assocQname.toPrefixString(serviceRegistry.getNamespaceService()).replaceFirst(":", "_")) instanceof ActivitiScriptNode) {
					ActivitiScriptNode node = (ActivitiScriptNode) task.getVariable(assocQname.toPrefixString(serviceRegistry.getNamespaceService()).replaceFirst(":", "_"));
					if (node != null) {
						associationService.update(ncNodeRef, assocQname, node.getNodeRef());
					}
				} else {
					@SuppressWarnings("unchecked")
					List<NodeRef> nodes = (ArrayList<NodeRef>) task.getVariable(assocQname.toPrefixString(serviceRegistry.getNamespaceService()).replaceFirst(":", "_"));
					if (nodes != null) {
						associationService.update(ncNodeRef, assocQname, nodes);
					}
				}
			}

		}

		if (logger.isDebugEnabled()) {
			logger.debug("UpdateNC: " + ncNodeRef + " - " + properties);
		}
		serviceRegistry.getNodeService().addProperties(ncNodeRef, properties);

		// Move documents from pkgNodeRef
		NodeRef briefNodeRef = getDocumentsFolder(ncNodeRef, serviceRegistry);

		NodeRef pkgNodeRef = ((ActivitiScriptNode) task.getVariable("bpm_package")).getNodeRef();

		List<ChildAssociationRef> childAssocs = serviceRegistry.getNodeService().getChildAssocs(pkgNodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS, RegexQNamePattern.MATCH_ALL);
		for (ChildAssociationRef childAssoc : childAssocs) {
			String name = (String) serviceRegistry.getNodeService().getProperty(childAssoc.getChildRef(), ContentModel.PROP_NAME);
			if (briefNodeRef != null && serviceRegistry.getNodeService().getType(childAssoc.getChildRef()).equals(ContentModel.TYPE_CONTENT)) {
				serviceRegistry.getFileFolderService().move(childAssoc.getChildRef(), briefNodeRef, name);
				serviceRegistry.getNodeService().removeChild(pkgNodeRef, childAssoc.getChildRef());
			}
		}

	}

	public static NodeRef getDocumentsFolder(NodeRef productNodeRef, ServiceRegistry serviceRegistry) {

		for (FileInfo file : serviceRegistry.getFileFolderService().listFolders(productNodeRef)) {
			if (file.getName().equals(TranslateHelper.getTranslatedPath(RepoConsts.PATH_DOCUMENTS))) {
				return file.getNodeRef();
			}
		}

		return null;
	}

}
