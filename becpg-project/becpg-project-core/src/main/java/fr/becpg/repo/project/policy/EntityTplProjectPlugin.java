package fr.becpg.repo.project.policy;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
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
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.project.impl.ProjectHelper;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 * <p>EntityTplProjectPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class EntityTplProjectPlugin implements EntityTplPlugin {

	private static final Log logger = LogFactory.getLog(EntityTplProjectPlugin.class);

	@Autowired
	private NodeService nodeService;
	@Autowired
	private EntityListDAO entityListDAO;
	@Autowired
	private AssociationService associationService;
	@Autowired
	private BehaviourFilter policyBehaviourFilter;

	/** {@inheritDoc} */
	@Override
	public void beforeSynchronizeEntity(NodeRef projectNodeRef, NodeRef entityTplNodeRef) {
		if (ProjectModel.TYPE_PROJECT.equals(nodeService.getType(projectNodeRef))
				&& !nodeService.hasAspect(projectNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)
				&& policyBehaviourFilter.isEnabled(BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
			NodeRef listContainerNodeRef = entityListDAO.getListContainer(projectNodeRef);
			if (listContainerNodeRef != null) {
				Integer completionPerc = (Integer) nodeService.getProperty(projectNodeRef, ProjectModel.PROP_COMPLETION_PERCENT);
				logger.debug("beforeSynchronizeEntity check completion perc:" + completionPerc);
				if (completionPerc == null || completionPerc == 0) {
					try {
						policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
						policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_VERSIONABLE);
						policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
						nodeService.deleteNode(listContainerNodeRef);
						logger.debug("beforeSynchronizeEntity deleting datalist container");
					} finally {
						policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
						policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_VERSIONABLE);
						policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
					}
				}
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void synchronizeEntity(NodeRef projectNodeRef, NodeRef projectTplNodeRef) {

		if (ProjectModel.TYPE_PROJECT.equals(nodeService.getType(projectNodeRef))) {

			if (logger.isDebugEnabled()) {
				logger.debug("Copy project template datalist '" + nodeService.getProperty(projectTplNodeRef, ContentModel.PROP_NAME) + "' for entity "
						+ nodeService.getProperty(projectNodeRef, ContentModel.PROP_NAME));
			}

			initializeNodeRefsAfterCopy(projectNodeRef);

		}

	}

	/**
	 * <p>initializeNodeRefsAfterCopy.</p>
	 *
	 * @param projectNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void initializeNodeRefsAfterCopy(NodeRef projectNodeRef) {

		NodeRef listContainerNodeRef = entityListDAO.getListContainer(projectNodeRef);
		if (listContainerNodeRef != null) {

			// Deliverables
			NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, ProjectModel.TYPE_DELIVERABLE_LIST);
			if (listNodeRef != null && nodeService.exists(listNodeRef)) {
				List<NodeRef> listItems = entityListDAO.getListItems(listNodeRef, ProjectModel.TYPE_DELIVERABLE_LIST);
				for (NodeRef listItem : listItems) {
					updateDelieverableDocument(projectNodeRef, listItem);
				}
			}

			// Tasks
			listNodeRef = entityListDAO.getList(listContainerNodeRef, ProjectModel.TYPE_TASK_LIST);
			if (listNodeRef != null && nodeService.exists(listNodeRef)) {
				List<NodeRef> listItems = entityListDAO.getListItems(listNodeRef, ProjectModel.TYPE_TASK_LIST);
				for (NodeRef listItem : listItems) {
					nodeService.setProperties(listItem,
							ProjectHelper.resetProperties(ProjectModel.TYPE_TASK_LIST, nodeService.getProperties(listItem)));
				}
			}
		}
	}

	private void updateDelieverableDocument(NodeRef projectNodeRef, NodeRef listItem) {

		Deque<String> stack = new ArrayDeque<>();
		NodeRef documentNodeRef = associationService.getTargetAssoc(listItem, ProjectModel.ASSOC_DL_CONTENT);

		if (documentNodeRef != null) {
			NodeRef folderNodeRef = nodeService.getPrimaryParent(documentNodeRef).getParentRef();

			while (folderNodeRef != null && !nodeService.hasAspect(folderNodeRef, BeCPGModel.ASPECT_ENTITYLISTS)) {
				String name = (String) nodeService.getProperty(folderNodeRef, ContentModel.PROP_NAME);
				logger.debug("folderNodeRef: " + folderNodeRef + " name: " + name);
				stack.push(name);
				folderNodeRef = nodeService.getPrimaryParent(folderNodeRef).getParentRef();
			}

			if (logger.isDebugEnabled()) {
				logger.debug("stack: " + stack);
			}

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

	/** {@inheritDoc} */
	@Override
	public boolean shouldSynchronizeDataList(RepositoryEntity entity, QName dataListQName) {
		return ProjectModel.TYPE_PROJECT.equals(nodeService.getType(entity.getNodeRef())) && ProjectModel.TYPE_TASK_LIST.equals(dataListQName);
	}

	/** {@inheritDoc} */
	@Override
	public <T extends RepositoryEntity> void synchronizeDataList(RepositoryEntity entity, List<T> dataListItems, List<T> tplDataListItems) {
		//Do Nothing

	}

}
