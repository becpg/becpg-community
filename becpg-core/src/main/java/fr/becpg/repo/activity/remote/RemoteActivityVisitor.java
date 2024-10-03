package fr.becpg.repo.activity.remote;

import java.io.OutputStream;
import java.util.List;

import org.alfresco.repo.domain.activities.ActivityFeedEntity;

import fr.becpg.common.BeCPGException;

/**
 * <p>RemoteActivityVisitor interface.</p>
 *
 * @author matthieu
 */
public interface RemoteActivityVisitor {

	/**
	 * <p>visit.</p>
	 *
	 * @param feedEntries a {@link java.util.List} object
	 * @param result a {@link java.io.OutputStream} object
	 * @throws fr.becpg.common.BeCPGException if any.
	 */
	public void visit(List<ActivityFeedEntity> feedEntries, OutputStream result) throws BeCPGException;

	/**
	 * <p>getContentType.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getContentType();

}
