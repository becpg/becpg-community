package fr.becpg.repo.project.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.model.SystemGroup;
import fr.becpg.repo.comment.CommentPermissionPlugin;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;

/**
 * Governs the comments posted on project tasks and on project entities ({@code pjt:projectEntity}).
 *
 * Only the author of a comment and the administrators can edit or delete it. An external author loses this right
 * once a related project has been formulated after the comment was posted.
 *
 * @author valentin
 */
@Service
public class ProjectCommentPermissionPlugin implements CommentPermissionPlugin {

	private static final String EXTERNAL_USER_GROUP = PermissionService.GROUP_PREFIX + SystemGroup.ExternalUser.toString();

	@Autowired
	private NodeService nodeService;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private EntityListDAO entityListDAO;

	@Autowired
	private AuthorityService authorityService;

	/** {@inheritDoc} */
	@Override
	public boolean isApplicable(NodeRef discussableNodeRef) {
		return isTask(discussableNodeRef) || !getProjectsOfEntity(discussableNodeRef).isEmpty();
	}

	/** {@inheritDoc} */
	@Override
	public boolean canModify(NodeRef commentNodeRef, NodeRef discussableNodeRef) {
		// cm:creator holds the fully authenticated user, so the current user is compared on the same identity
		String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
		if (authorityService.isAdminAuthority(currentUser)) {
			return true;
		}
		if (!Objects.equals(currentUser, nodeService.getProperty(commentNodeRef, ContentModel.PROP_CREATOR))) {
			return false;
		}
		return !isExternalUser(currentUser) || !isFormulatedSince(commentNodeRef, getProjects(discussableNodeRef));
	}

	private boolean isTask(NodeRef nodeRef) {
		return ProjectModel.TYPE_TASK_LIST.equals(nodeService.getType(nodeRef));
	}

	private List<NodeRef> getProjects(NodeRef discussableNodeRef) {
		if (isTask(discussableNodeRef)) {
			NodeRef projectNodeRef = entityListDAO.getEntity(discussableNodeRef);
			return projectNodeRef != null ? Collections.singletonList(projectNodeRef) : Collections.emptyList();
		}
		return getProjectsOfEntity(discussableNodeRef);
	}

	private List<NodeRef> getProjectsOfEntity(NodeRef entityNodeRef) {
		return associationService.getSourcesAssocs(entityNodeRef, ProjectModel.ASSOC_PROJECT_ENTITY);
	}

	private boolean isExternalUser(String userName) {
		return authorityService.getAuthoritiesForUser(userName).contains(EXTERNAL_USER_GROUP);
	}

	private boolean isFormulatedSince(NodeRef commentNodeRef, List<NodeRef> projectNodeRefs) {
		Date commentCreated = (Date) nodeService.getProperty(commentNodeRef, ContentModel.PROP_CREATED);
		for (NodeRef projectNodeRef : projectNodeRefs) {
			Date projectFormulated = (Date) nodeService.getProperty(projectNodeRef, BeCPGModel.PROP_FORMULATED_DATE);
			if ((projectFormulated != null) && (commentCreated != null) && projectFormulated.after(commentCreated)) {
				return true;
			}
		}
		return false;
	}

}
