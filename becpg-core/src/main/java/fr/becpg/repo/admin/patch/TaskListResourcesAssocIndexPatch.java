package fr.becpg.repo.admin.patch;

import org.alfresco.service.namespace.QName;

import fr.becpg.repo.admin.AssociationIndexerService;

/**
 * <p>TaskListResourcesAssocIndexPatch class.</p>
 *
 * @author matthieu
 */
public class TaskListResourcesAssocIndexPatch extends AbstractBeCPGPatch {

	private static final String PROJECT_URI = "http://www.bcpg.fr/model/project/1.0";

	private static final QName TYPE_TASK_LIST = QName.createQName(PROJECT_URI, "taskList");

	private static final QName ASSOC_TL_RESOURCES = QName.createQName(PROJECT_URI, "tlResources");

	private AssociationIndexerService associationIndexerService;
	
	/**
	 * <p>Setter for the field <code>associationIndexerService</code>.</p>
	 *
	 * @param associationIndexerService a {@link fr.becpg.repo.admin.AssociationIndexerService} object
	 */
	public void setAssociationIndexerService(AssociationIndexerService associationIndexerService) {
		this.associationIndexerService = associationIndexerService;
	}
	
	/** {@inheritDoc} */
	@Override
	protected String applyInternal() throws Exception {
		associationIndexerService.reindexAssocs(TYPE_TASK_LIST, ASSOC_TL_RESOURCES);
		return null;
	}
	
}
