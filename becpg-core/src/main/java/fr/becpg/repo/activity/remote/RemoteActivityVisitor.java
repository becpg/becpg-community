package fr.becpg.repo.activity.remote;

import java.io.OutputStream;
import java.util.List;

import org.alfresco.repo.domain.activities.ActivityFeedEntity;

/**
 * 
 */
public interface RemoteActivityVisitor {

	public void visit(List<ActivityFeedEntity> feedEntries, OutputStream result) throws Exception;

	public String getContentType();

}