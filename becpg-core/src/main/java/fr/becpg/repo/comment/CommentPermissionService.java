package fr.becpg.repo.comment;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Decides whether the current user can edit and delete a comment: an external user can only change their own
 * comments, and the registered {@link CommentPermissionPlugin}s can restrict the comments of the nodes they govern.
 *
 * @author valentin
 */
public interface CommentPermissionService {

	/**
	 * Tells whether the current user can edit and delete a comment.
	 *
	 * @param commentNodeRef the comment
	 * @return false if the current user is an external user who did not post the comment, or if a plugin governing
	 *         the commented node refuses it, true otherwise
	 */
	boolean canModify(NodeRef commentNodeRef);

}
