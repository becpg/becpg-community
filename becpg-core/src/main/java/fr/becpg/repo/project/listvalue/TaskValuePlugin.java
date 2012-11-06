package fr.becpg.repo.project.listvalue;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.listvalue.EntityListValuePlugin;
import fr.becpg.repo.listvalue.ListValuePage;
import fr.becpg.repo.listvalue.ListValueService;

@Service
public class TaskValuePlugin extends EntityListValuePlugin {

	private static Log logger = LogFactory.getLog(TaskValuePlugin.class);

	private static final String SOURCE_TYPE_TASK_VALUE = "TaskValue";

	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_TASK_VALUE };
	}

	@Override
	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize,
			Map<String, Serializable> props) {

		NodeRef entityNodeRef = new NodeRef((String) props.get(ListValueService.PROP_NODEREF));
		logger.debug("TaskValue sourceType: " + sourceType + " - entityNodeRef: " + entityNodeRef);

		String className = (String) props.get(ListValueService.PROP_CLASS_NAME);
		QName type = QName.createQName(className, namespaceService);
		
		return suggestDatalistItem(entityNodeRef, type, ProjectModel.PROP_TL_TASK_NAME, query, pageNum, pageSize);				
	}
}
