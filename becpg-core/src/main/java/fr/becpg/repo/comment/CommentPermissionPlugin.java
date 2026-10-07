package fr.becpg.repo.comment;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Restricts who can edit or delete the comments posted on some kinds of nodes.
 *
 * Plugins only narrow the standard Alfresco permissions: a user still needs the permission to write or
 * delete the comment node itself.
 *
 * @author valentin
 */
public interface CommentPermissionPlugin {

	/**
	 * Tells whether this plugin governs the comments posted on a node.
	 *
	 * @param discussableNodeRef the node the comments are posted on
	 * @return true if this plugin decides who can edit or delete these comments
	 */
	boolean isApplicable(NodeRef discussableNodeRef);

	/**
	 * Tells whether the current user can edit and delete a comment.
	 *
	 * @param commentNodeRef the comment
	 * @param discussableNodeRef the node the comment is posted on
	 * @return true if the current user can edit and delete the comment
	 */
	boolean canModify(NodeRef commentNodeRef, NodeRef discussableNodeRef);

}
