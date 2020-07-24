package fr.becpg.repo.activity;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.repo.activity.data.ActivityListDataItem;

/**
 * <p>EntityActivityLogListener class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class EntityActivityLogListener implements EntityActivityListener {

	private Log logger = LogFactory.getLog(EntityActivityLogListener.class);

	/** {@inheritDoc} */
	@Override
	public void notify(NodeRef entityNodeRef, ActivityListDataItem activityListDataItem) {
		if (logger.isDebugEnabled()) {
			logger.debug("Post Activity :" + activityListDataItem.toString());
		}

	}

}
