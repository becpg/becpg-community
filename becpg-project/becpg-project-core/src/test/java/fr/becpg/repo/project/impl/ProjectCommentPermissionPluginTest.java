package fr.becpg.repo.project.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.model.SystemGroup;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;

/**
 * Unit tests for {@link ProjectCommentPermissionPlugin}.
 */
public class ProjectCommentPermissionPluginTest {

	private static final String AUTHOR = "author";

	private static final String OTHER_USER = "otherUser";

	private static final String ADMIN = "admin";

	private static final Date COMMENT_CREATED = new Date(1_000_000L);

	private static final Date BEFORE_COMMENT = new Date(500_000L);

	private static final Date AFTER_COMMENT = new Date(2_000_000L);

	private final NodeRef commentNodeRef = new NodeRef("workspace://SpacesStore/comment");

	private final NodeRef taskNodeRef = new NodeRef("workspace://SpacesStore/task");

	private final NodeRef entityNodeRef = new NodeRef("workspace://SpacesStore/entity");

	private final NodeRef projectNodeRef = new NodeRef("workspace://SpacesStore/project");

	private final NodeRef otherProjectNodeRef = new NodeRef("workspace://SpacesStore/otherProject");

	@Mock
	private NodeService nodeService;

	@Mock
	private AssociationService associationService;

	@Mock
	private EntityListDAO entityListDAO;

	@Mock
	private AuthorityService authorityService;

	@InjectMocks
	private ProjectCommentPermissionPlugin plugin;

	private AutoCloseable mocks;

	@BeforeClass
	public static void initAuthenticationUtil() throws Exception {
		new AuthenticationUtil().afterPropertiesSet();
	}

	@Before
	public void setUp() {
		mocks = MockitoAnnotations.openMocks(this);
		when(nodeService.getType(taskNodeRef)).thenReturn(ProjectModel.TYPE_TASK_LIST);
		when(nodeService.getType(entityNodeRef)).thenReturn(BeCPGModel.TYPE_ENTITY_V2);
		when(entityListDAO.getEntity(taskNodeRef)).thenReturn(projectNodeRef);
		when(associationService.getSourcesAssocs(entityNodeRef, ProjectModel.ASSOC_PROJECT_ENTITY))
				.thenReturn(List.of(otherProjectNodeRef, projectNodeRef));
		when(nodeService.getProperty(commentNodeRef, ContentModel.PROP_CREATOR)).thenReturn(AUTHOR);
		when(nodeService.getProperty(commentNodeRef, ContentModel.PROP_CREATED)).thenReturn(COMMENT_CREATED);
		when(authorityService.isAdminAuthority(ADMIN)).thenReturn(true);
		when(authorityService.getAuthoritiesForUser(AUTHOR)).thenReturn(Collections.emptySet());
	}

	@After
	public void tearDown() throws Exception {
		AuthenticationUtil.clearCurrentSecurityContext();
		mocks.close();
	}

	@Test
	public void testTaskCommentsAreGoverned() {
		assertTrue(plugin.isApplicable(taskNodeRef));
	}

	@Test
	public void testProjectEntityCommentsAreGoverned() {
		assertTrue(plugin.isApplicable(entityNodeRef));
	}

	@Test
	public void testCommentsOfEntityOutsideProjectsAreNotGoverned() {
		when(associationService.getSourcesAssocs(entityNodeRef, ProjectModel.ASSOC_PROJECT_ENTITY)).thenReturn(Collections.emptyList());

		assertFalse(plugin.isApplicable(entityNodeRef));
	}

	@Test
	public void testInternalAuthorCanModifyCommentAfterFormulation() {
		givenProjectFormulatedAt(projectNodeRef, AFTER_COMMENT);
		givenCurrentUser(AUTHOR);

		assertTrue(plugin.canModify(commentNodeRef, taskNodeRef));
	}

	@Test
	public void testOtherUserCannotModifyComment() {
		givenCurrentUser(OTHER_USER);

		assertFalse(plugin.canModify(commentNodeRef, taskNodeRef));
	}

	@Test
	public void testAdminCanModifyOtherUserComment() {
		givenCurrentUser(ADMIN);

		assertTrue(plugin.canModify(commentNodeRef, taskNodeRef));
	}

	@Test
	public void testExternalAuthorCanModifyCommentBeforeFormulation() {
		givenExternalAuthor();
		givenProjectFormulatedAt(projectNodeRef, BEFORE_COMMENT);
		givenCurrentUser(AUTHOR);

		assertTrue(plugin.canModify(commentNodeRef, taskNodeRef));
	}

	@Test
	public void testExternalAuthorCannotModifyCommentAfterFormulation() {
		givenExternalAuthor();
		givenProjectFormulatedAt(projectNodeRef, AFTER_COMMENT);
		givenCurrentUser(AUTHOR);

		assertFalse(plugin.canModify(commentNodeRef, taskNodeRef));
	}

	@Test
	public void testExternalAuthorCannotModifyEntityCommentOnceAnyProjectIsFormulated() {
		givenExternalAuthor();
		givenProjectFormulatedAt(otherProjectNodeRef, BEFORE_COMMENT);
		givenProjectFormulatedAt(projectNodeRef, AFTER_COMMENT);
		givenCurrentUser(AUTHOR);

		assertFalse(plugin.canModify(commentNodeRef, entityNodeRef));
	}

	private void givenCurrentUser(String userName) {
		AuthenticationUtil.setFullyAuthenticatedUser(userName);
	}

	private void givenExternalAuthor() {
		when(authorityService.getAuthoritiesForUser(AUTHOR))
				.thenReturn(Set.of(PermissionService.GROUP_PREFIX + SystemGroup.ExternalUser.toString()));
	}

	private void givenProjectFormulatedAt(NodeRef project, Date formulatedDate) {
		when(nodeService.getProperty(project, BeCPGModel.PROP_FORMULATED_DATE)).thenReturn(formulatedDate);
	}

}
