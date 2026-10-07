package fr.becpg.repo.comment.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.forum.CommentService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.SystemGroup;
import fr.becpg.repo.comment.CommentPermissionPlugin;
import fr.becpg.repo.comment.CommentPermissionService;

/**
 * Refuses an external user the comments of other users, then applies every {@link CommentPermissionPlugin}
 * governing the commented node. Otherwise comments keep the standard Alfresco behaviour.
 *
 * @author valentin
 */
@Service("commentPermissionService")
public class CommentPermissionServiceImpl implements CommentPermissionService {

	private static final Log logger = LogFactory.getLog(CommentPermissionServiceImpl.class);

	private static final String EXTERNAL_USER_GROUP = PermissionService.GROUP_PREFIX + SystemGroup.ExternalUser.toString();

	@Autowired
	private CommentService commentService;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private AuthorityService authorityService;

	private List<CommentPermissionPlugin> plugins = Collections.emptyList();

	/**
	 * <p>Setter for the field <code>plugins</code>.</p>
	 *
	 * @param plugins the registered comment permission plugins
	 */
	@Autowired(required = false)
	public void setPlugins(List<CommentPermissionPlugin> plugins) {
		this.plugins = plugins;
	}

	/** {@inheritDoc} */
	@Override
	public boolean canModify(NodeRef commentNodeRef) {
		if (isExternalUserAndNotCreator(commentNodeRef)) {
			return false;
		}
		NodeRef discussableNodeRef = commentService.getDiscussableAncestor(commentNodeRef);
		if (discussableNodeRef == null) {
			return true;
		}
		for (CommentPermissionPlugin plugin : plugins) {
			if (plugin.isApplicable(discussableNodeRef) && !plugin.canModify(commentNodeRef, discussableNodeRef)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Comment " + commentNodeRef + " cannot be modified, refused by " + plugin.getClass().getSimpleName());
				}
				return false;
			}
		}
		return true;
	}

	private boolean isExternalUserAndNotCreator(NodeRef commentNodeRef) {
		// cm:creator holds the fully authenticated user, so the current user is compared on the same identity
		String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
		return authorityService.getAuthoritiesForUser(currentUser).contains(EXTERNAL_USER_GROUP)
				&& !Objects.equals(currentUser, nodeService.getProperty(commentNodeRef, ContentModel.PROP_CREATOR));
	}

}
