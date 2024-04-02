package fr.becpg.repo.activity.remote;

import java.io.OutputStream;
import java.util.List;

import org.alfresco.repo.domain.activities.ActivityFeedEntity;

import fr.becpg.common.BeCPGException;

/**
 * 
 */
public interface RemoteActivityVisitor {

	public void visit(List<ActivityFeedEntity> feedEntries, OutputStream result) throws BeCPGException;

	public String getContentType();

}