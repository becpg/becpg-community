package fr.becpg.repo.comment.policy;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.forum.CommentService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.GUID;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import fr.becpg.repo.comment.CommentPermissionService;

/**
 * Unit tests for {@link CommentPermissionPolicy}.
 */
public class CommentPermissionPolicyTest {

	private NodeRef commentNodeRef;

	private NodeRef archivedCommentNodeRef;

	private CommentPermissionService commentPermissionService;

	private CommentPermissionPolicy policy;

	@BeforeClass
	public static void initAuthenticationUtil() throws Exception {
		new AuthenticationUtil().afterPropertiesSet();
	}

	@Before
	public void setUp() {
		TransactionSynchronizationManager.initSynchronization();
		// Alfresco keeps the transaction resources in its own thread local, so each test uses its own nodes
		String commentId = GUID.generate();
		commentNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, commentId);
		archivedCommentNodeRef = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, commentId);
		commentPermissionService = mock(CommentPermissionService.class);
		policy = new CommentPermissionPolicy();
		policy.setCommentPermissionService(commentPermissionService);
		policy.setCommentService(mock(CommentService.class));
		AuthenticationUtil.setFullyAuthenticatedUser("user");
	}

	@After
	public void tearDown() {
		AuthenticationUtil.clearCurrentSecurityContext();
		TransactionSynchronizationManager.clearSynchronization();
	}

	@Test
	public void testRefusedCommentEditionThrows() {
		when(commentPermissionService.canModify(commentNodeRef)).thenReturn(false);

		assertThrows(AccessDeniedException.class,
				() -> policy.onUpdateProperties(commentNodeRef, Collections.emptyMap(), Collections.emptyMap()));
	}

	@Test
	public void testRefusedCommentDeletionThrows() {
		when(commentPermissionService.canModify(commentNodeRef)).thenReturn(false);

		assertThrows(AccessDeniedException.class, () -> policy.beforeDeleteNode(commentNodeRef));
	}

	@Test
	public void testCommentCreatedInTransactionCanBeEdited() {
		when(commentPermissionService.canModify(commentNodeRef)).thenReturn(false);
		policy.onCreateNode(new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, null, ForumModel.TYPE_POST, commentNodeRef));

		policy.onUpdateProperties(commentNodeRef, Collections.emptyMap(), Collections.emptyMap());

		verify(commentPermissionService, never()).canModify(commentNodeRef);
	}

	@Test
	public void testArchivedCommentCanBeMovedOutOfTheArchive() {
		when(commentPermissionService.canModify(archivedCommentNodeRef)).thenReturn(false);

		policy.beforeDeleteNode(archivedCommentNodeRef);

		verify(commentPermissionService, never()).canModify(archivedCommentNodeRef);
	}

	@Test
	public void testSystemUserIsNotChecked() {
		when(commentPermissionService.canModify(commentNodeRef)).thenReturn(false);

		AuthenticationUtil.runAsSystem(() -> {
			policy.onUpdateProperties(commentNodeRef, Collections.emptyMap(), Collections.emptyMap());
			return null;
		});

		verify(commentPermissionService, never()).canModify(commentNodeRef);
	}

}
