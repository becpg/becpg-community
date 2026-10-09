package fr.becpg.repo.comment.policy;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ForumModel;
import org.alfresco.repo.forum.CommentService;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.comment.CommentPermissionService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * Refuses the edition and the deletion of a comment when the {@link CommentPermissionService} does not allow it,
 * whatever the way the comment is reached (Share, REST API, script).
 *
 * The system user, the creation of the comment itself, the cascade deletion of the commented node and the moves out
 * of the archive store (restoration or purge from the trashcan) are let through.
 *
 * @author valentin
 */
public class CommentPermissionPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnCreateNodePolicy,
		NodeServicePolicies.OnUpdatePropertiesPolicy, NodeServicePolicies.BeforeDeleteNodePolicy {

	private static final String KEY_COMMENTS_CREATED_IN_TXN = CommentPermissionPolicy.class.getName() + ".commentsCreatedInTxn";

	private CommentPermissionService commentPermissionService;

	private CommentService commentService;

	/**
	 * <p>Setter for the field <code>commentPermissionService</code>.</p>
	 *
	 * @param commentPermissionService a {@link fr.becpg.repo.comment.CommentPermissionService} object
	 */
	public void setCommentPermissionService(CommentPermissionService commentPermissionService) {
		this.commentPermissionService = commentPermissionService;
	}

	/**
	 * <p>Setter for the field <code>commentService</code>.</p>
	 *
	 * @param commentService a {@link org.alfresco.repo.forum.CommentService} object
	 */
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}

	/** {@inheritDoc} */
	@Override
	public void doInit() {
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, ForumModel.TYPE_POST,
				new JavaBehaviour(this, "onCreateNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ForumModel.TYPE_POST,
				new JavaBehaviour(this, "onUpdateProperties"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, ForumModel.TYPE_POST,
				new JavaBehaviour(this, "beforeDeleteNode"));
	}

	/** {@inheritDoc} */
	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		TransactionalResourceHelper.getSet(KEY_COMMENTS_CREATED_IN_TXN).add(childAssocRef.getChildRef());
	}

	/** {@inheritDoc} */
	@Override
	public void onUpdateProperties(NodeRef commentNodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		if (!isCreatedInCurrentTransaction(commentNodeRef)) {
			checkCanModify(commentNodeRef, "Cannot edit comment");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void beforeDeleteNode(NodeRef commentNodeRef) {
		if (!isArchived(commentNodeRef) && !isCreatedInCurrentTransaction(commentNodeRef)
				&& !isDiscussableNodeBeingDeleted(commentNodeRef)) {
			checkCanModify(commentNodeRef, "Cannot delete comment");
		}
	}

	private void checkCanModify(NodeRef commentNodeRef, String refusalMessage) {
		if (!AuthenticationUtil.isRunAsUserTheSystemUser() && !commentPermissionService.canModify(commentNodeRef)) {
			throw new AccessDeniedException(refusalMessage);
		}
	}

	private boolean isArchived(NodeRef commentNodeRef) {
		// Restoring a node moves it out of the archive store, which fires beforeDeleteNode on every descendant
		return StoreRef.STORE_REF_ARCHIVE_SPACESSTORE.equals(commentNodeRef.getStoreRef());
	}

	private boolean isCreatedInCurrentTransaction(NodeRef commentNodeRef) {
		Set<NodeRef> createdComments = TransactionalResourceHelper.getSet(KEY_COMMENTS_CREATED_IN_TXN);
		return createdComments.contains(commentNodeRef);
	}

	private boolean isDiscussableNodeBeingDeleted(NodeRef commentNodeRef) {
		NodeRef discussableNodeRef = commentService.getDiscussableAncestor(commentNodeRef);
		return (discussableNodeRef != null) && isPendingDelete(discussableNodeRef);
	}

}
