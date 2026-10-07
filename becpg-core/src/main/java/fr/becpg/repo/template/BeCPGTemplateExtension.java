package fr.becpg.repo.template;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.template.BaseTemplateProcessorExtension;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.cmr.security.AuthorityService;

import fr.becpg.repo.comment.CommentPermissionService;
import fr.becpg.repo.helper.AuthorityHelper;

/**
 * <p>BeCPGTemplateExtension class.</p>
 *
 * @author matthieu
 */
public class BeCPGTemplateExtension extends BaseTemplateProcessorExtension {

    private AuthorityService authorityService;

    private CommentPermissionService commentPermissionService;
    
	/**
	 * <p>Setter for the field <code>authorityService</code>.</p>
	 *
	 * @param authorityService a {@link org.alfresco.service.cmr.security.AuthorityService} object
	 */
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	/**
	 * <p>Setter for the field <code>commentPermissionService</code>.</p>
	 *
	 * @param commentPermissionService a {@link fr.becpg.repo.comment.CommentPermissionService} object
	 */
	public void setCommentPermissionService(CommentPermissionService commentPermissionService) {
		this.commentPermissionService = commentPermissionService;
	}

	/**
	 * <p>isAccountEnabled.</p>
	 *
	 * @param person a {@link org.alfresco.repo.template.TemplateNode} object
	 * @return a boolean
	 */
	public boolean isAccountEnabled(TemplateNode person) {
		// Only admins have rights to check authentication enablement
		if (this.authorityService.isAdminAuthority(AuthenticationUtil.getFullyAuthenticatedUser())) {
			String userName = (String) person.getProperties().get(ContentModel.PROP_USERNAME.toString());
			return AuthorityHelper.isAccountEnabled(userName);
		}
		return true;
	}
	
	public boolean isCurrentUserExternal() {
		return AuthorityHelper.isCurrentUserExternal();
	}

	/**
	 * Tells whether the current user can edit and delete a comment, see {@link CommentPermissionService}.
	 *
	 * @param comment the comment node
	 * @return true if no comment permission plugin refuses it
	 */
	public boolean canModifyComment(TemplateNode comment) {
		return commentPermissionService.canModify(comment.getNodeRef());
	}
}
