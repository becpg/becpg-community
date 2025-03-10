package fr.becpg.repo.activity;

import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.activity.data.ActivityListDataItem;
import fr.becpg.repo.activity.data.ActivityType;

/**
 * <p>ContentActivityListener class.</p>
 *
 * @author matthieu
 */
@Service
public class ContentActivityListener implements EntityActivityListener {

	private static final Log logger = LogFactory.getLog(ContentActivityListener.class);
	
	@Autowired
	private NodeService nodeService;

	/** {@inheritDoc} */
	@Override
	public void notify(NodeRef entityNodeRef, ActivityListDataItem activityListDataItem) {
		if (ActivityType.Content.equals(activityListDataItem.getActivityType())) {
			if (logger.isDebugEnabled()) {
				logger.debug("Update auditable properties for entity: " + entityNodeRef + ", as some content within it was updated");
			}
			nodeService.setProperty(entityNodeRef, ContentModel.PROP_MODIFIED, new Date());
			nodeService.setProperty(entityNodeRef, ContentModel.PROP_MODIFIER, activityListDataItem.getUserId());
		}
	}
}
