package fr.becpg.test.repo.web.scripts.project;

import java.util.Date;

import org.alfresco.repo.forum.CommentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.test.BeCPGTestHelper;
import fr.becpg.test.project.AbstractProjectTestCase;
import fr.becpg.test.utils.TestWebscriptExecuters;
import fr.becpg.test.utils.TestWebscriptExecuters.GetRequest;
import fr.becpg.test.utils.TestWebscriptExecuters.Response;

/**
 * Integration tests for the project task info webscript ({@code /becpg/project/task}).
 */
public class ProjectTaskInfoWebScriptIT extends AbstractProjectTestCase {

	private static final String TASK_INFO_URL = "/becpg/project/task?nodeRef=";

	/** Password given to the test users by {@link BeCPGTestHelper#createUser(String)}. */
	private static final String TEST_USER_PASSWORD = "PWD";

	private static final String VISIBLE_COMMENT = "Visible comment";

	private static final String HIDDEN_COMMENT = "Hidden comment";

	@Autowired
	private CommentService commentService;

	/**
	 * Tests that the task comment count only includes the comments the user can read.
	 */
	@Test
	public void testCommentCountIgnoresUnreadableComments() throws Exception {
		final NodeRef projectNodeRef = createProject(ProjectState.InProgress, new Date(), null);

		final NodeRef taskNodeRef = inWriteTx(() -> {
			permissionService.setPermission(projectNodeRef, BeCPGTestHelper.USER_ONE, PermissionService.CONSUMER, true);
			NodeRef firstTaskNodeRef = ((ProjectData) alfrescoRepository.findOne(projectNodeRef)).getTaskList().get(0).getNodeRef();
			commentService.createComment(firstTaskNodeRef, "", VISIBLE_COMMENT, false);
			NodeRef hiddenCommentNodeRef = commentService.createComment(firstTaskNodeRef, "", HIDDEN_COMMENT, false);
			permissionService.setPermission(hiddenCommentNodeRef, BeCPGTestHelper.USER_ONE, PermissionService.READ, false);
			return firstTaskNodeRef;
		});

		Response response = TestWebscriptExecuters.sendRequest(new GetRequest(TASK_INFO_URL + taskNodeRef), 200, BeCPGTestHelper.USER_ONE,
				TEST_USER_PASSWORD);

		JSONObject task = new JSONObject(response.getContentAsString()).getJSONObject("task");
		assertEquals("1", task.getString("commentCount"));
	}

}
