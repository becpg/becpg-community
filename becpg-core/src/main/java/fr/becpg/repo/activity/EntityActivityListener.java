package fr.becpg.repo.activity;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.activity.data.ActivityListDataItem;

public interface EntityActivityListener {

	void notify(NodeRef entityNodeRef, ActivityListDataItem activityListDataItem);

}
