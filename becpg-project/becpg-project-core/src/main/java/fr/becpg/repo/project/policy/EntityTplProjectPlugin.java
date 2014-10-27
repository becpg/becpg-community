package fr.becpg.repo.project.policy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityTplPlugin;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.project.ProjectService;

@Service
public class EntityTplProjectPlugin implements EntityTplPlugin {

	private static Log logger = LogFactory.getLog(EntityTplProjectPlugin.class);

	@Autowired
	private NodeService nodeService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private EntityListDAO entityListDAO;
	@Autowired
	private AssociationService associationService;

	@Override
	public void synchronizeEntity(NodeRef projectNodeRef, NodeRef projectTplNodeRef) {

		
		if (ProjectModel.TYPE_PROJECT.equals(nodeService.getType(projectNodeRef))) {

			// copy folders
			// already done by entity policy

			// copy datalist from Tpl to project
			Collection<QName> dataLists = new ArrayList<QName>();
			dataLists.add(ProjectModel.TYPE_TASK_LIST);
			dataLists.add(ProjectModel.TYPE_DELIVERABLE_LIST);
			
			if(logger.isDebugEnabled()){
				logger.debug("Copy project template datalist '"+ nodeService.getProperty(projectTplNodeRef, ContentModel.PROP_NAME)
					+ "' for entity "+nodeService.getProperty(projectNodeRef, ContentModel.PROP_NAME));
			}
			
			entityListDAO.copyDataLists(projectTplNodeRef, projectNodeRef, dataLists, false);
			
			// we wait files are copied by entity policy
			initializeNodeRefsAfterCopy(projectNodeRef);

			// initialize
			try {
				logger.debug("Project policy formulate");
				projectService.formulate(projectNodeRef);
			} catch (FormulateException e) {
				logger.error(e, e);
			}

		}

	}

	// TODO : do it in a generic way
	public void initializeNodeRefsAfterCopy(NodeRef projectNodeRef) {

		
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(projectNodeRef);
		if (listContainerNodeRef != null) {

			// Deliverables
			NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, ProjectModel.TYPE_DELIVERABLE_LIST);
			if (listNodeRef != null) {
				List<NodeRef> listItems = entityListDAO.getListItems(listNodeRef, ProjectModel.TYPE_DELIVERABLE_LIST);
				for (NodeRef listItem : listItems) {
					updateDelieverableDocument(projectNodeRef, listItem);
				}
			}
		}
	}

	private void updateDelieverableDocument(NodeRef projectNodeRef, NodeRef listItem) {

		Stack<String> stack = new Stack<String>();
		NodeRef documentNodeRef = associationService.getTargetAssoc(listItem, ProjectModel.ASSOC_DL_CONTENT);

		if (documentNodeRef != null) {
			NodeRef folderNodeRef = nodeService.getPrimaryParent(documentNodeRef).getParentRef();

			while (folderNodeRef != null && !nodeService.hasAspect(folderNodeRef, BeCPGModel.ASPECT_ENTITYLISTS)) {
				String name = (String) nodeService.getProperty(folderNodeRef, ContentModel.PROP_NAME);
				logger.debug("folderNodeRef: " + folderNodeRef + " name: " + name);
				stack.push(name);
				folderNodeRef = nodeService.getPrimaryParent(folderNodeRef).getParentRef();
			}

			logger.debug("stack: " + stack);

			folderNodeRef = projectNodeRef;
			Iterator<String> iterator = stack.iterator();
			while (iterator.hasNext() && folderNodeRef != null) {
				folderNodeRef = nodeService.getChildByName(folderNodeRef, ContentModel.ASSOC_CONTAINS, iterator.next());
			}

			if (folderNodeRef != null) {
				NodeRef newDocumentNodeRef = nodeService.getChildByName(folderNodeRef, ContentModel.ASSOC_CONTAINS,
						(String) nodeService.getProperty(documentNodeRef, ContentModel.PROP_NAME));
				logger.debug("Update dlContent with doc " + newDocumentNodeRef);
				associationService.update(listItem, ProjectModel.ASSOC_DL_CONTENT, newDocumentNodeRef);
			}

		}
	}

}
