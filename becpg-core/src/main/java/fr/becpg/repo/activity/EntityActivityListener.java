package fr.becpg.repo.activity;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.activity.data.ActivityListDataItem;

/**
 * <p>EntityActivityListener interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface EntityActivityListener {

	/**
	 * <p>notify.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param activityListDataItem a {@link fr.becpg.repo.activity.data.ActivityListDataItem} object.
	 */
	void notify(NodeRef entityNodeRef, ActivityListDataItem activityListDataItem);

}
