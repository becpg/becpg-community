package fr.becpg.repo.comment.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.forum.CommentService;
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

import fr.becpg.model.SystemGroup;
import fr.becpg.repo.comment.CommentPermissionPlugin;

/**
 * Unit tests for {@link CommentPermissionServiceImpl}.
 */
public class CommentPermissionServiceImplTest {

	private static final String AUTHOR = "author";

	private static final String EXTERNAL_USER = "externalUser";

	private final NodeRef commentNodeRef = new NodeRef("workspace://SpacesStore/comment");

	private final NodeRef discussableNodeRef = new NodeRef("workspace://SpacesStore/discussable");

	@Mock
	private CommentService commentService;

	@Mock
	private NodeService nodeService;

	@Mock
	private AuthorityService authorityService;

	@InjectMocks
	private CommentPermissionServiceImpl commentPermissionService;

	private AutoCloseable mocks;

	@BeforeClass
	public static void initAuthenticationUtil() throws Exception {
		new AuthenticationUtil().afterPropertiesSet();
	}

	@Before
	public void setUp() {
		mocks = MockitoAnnotations.openMocks(this);
		when(commentService.getDiscussableAncestor(commentNodeRef)).thenReturn(discussableNodeRef);
		when(nodeService.getProperty(commentNodeRef, ContentModel.PROP_CREATOR)).thenReturn(AUTHOR);
		when(authorityService.getAuthoritiesForUser(AUTHOR)).thenReturn(Collections.emptySet());
		AuthenticationUtil.setFullyAuthenticatedUser(AUTHOR);
	}

	@After
	public void tearDown() throws Exception {
		AuthenticationUtil.clearCurrentSecurityContext();
		mocks.close();
	}

	@Test
	public void testExternalUserCannotModifyOtherUserComment() {
		givenExternalUser(EXTERNAL_USER);
		AuthenticationUtil.setFullyAuthenticatedUser(EXTERNAL_USER);

		assertFalse(commentPermissionService.canModify(commentNodeRef));
	}

	@Test
	public void testExternalAuthorCanModifyUngovernedComment() {
		givenExternalUser(AUTHOR);
		commentPermissionService.setPlugins(List.of(givenPlugin(false, false)));

		assertTrue(commentPermissionService.canModify(commentNodeRef));
	}

	@Test
	public void testCommentWithoutDiscussableNodeCanBeModified() {
		when(commentService.getDiscussableAncestor(commentNodeRef)).thenReturn(null);
		commentPermissionService.setPlugins(List.of(givenPlugin(true, false)));

		assertTrue(commentPermissionService.canModify(commentNodeRef));
	}

	@Test
	public void testCommentNotGovernedByAnyPluginCanBeModified() {
		commentPermissionService.setPlugins(List.of(givenPlugin(false, false)));

		assertTrue(commentPermissionService.canModify(commentNodeRef));
	}

	@Test
	public void testCommentRefusedByGoverningPluginCannotBeModified() {
		commentPermissionService.setPlugins(List.of(givenPlugin(true, true), givenPlugin(true, false)));

		assertFalse(commentPermissionService.canModify(commentNodeRef));
	}

	@Test
	public void testCommentAllowedByGoverningPluginCanBeModified() {
		commentPermissionService.setPlugins(List.of(givenPlugin(true, true)));

		assertTrue(commentPermissionService.canModify(commentNodeRef));
	}

	private void givenExternalUser(String userName) {
		when(authorityService.getAuthoritiesForUser(userName))
				.thenReturn(Set.of(PermissionService.GROUP_PREFIX + SystemGroup.ExternalUser.toString()));
	}

	private CommentPermissionPlugin givenPlugin(boolean applicable, boolean allowed) {
		CommentPermissionPlugin plugin = mock(CommentPermissionPlugin.class);
		when(plugin.isApplicable(discussableNodeRef)).thenReturn(applicable);
		when(plugin.canModify(commentNodeRef, discussableNodeRef)).thenReturn(allowed);
		return plugin;
	}

}
