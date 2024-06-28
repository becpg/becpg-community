/*
 *
 */
package fr.becpg.test.project.formulation;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.impl.ProjectHelper;
import fr.becpg.test.project.AbstractProjectTestCase;

/**
 * The Class ProjectCalculatePlanningDatesTest.
 *
 * @author quere
 */
public class ProjectCalculatePlanningDatesIT extends AbstractProjectTestCase {

	private static final Log logger = LogFactory.getLog(ProjectCalculatePlanningDatesIT.class);

	@Test
	public void testCalculatePlanningDates() throws ParseException {

		final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

		final NodeRef projectNodeRef = createProject(ProjectState.OnHold, dateFormat.parse("15/11/2012"), null);
		final Date today = ProjectHelper.removeTime(new Date());
		final Date nextStartDate = ProjectHelper.calculateNextStartDate(today);

		inWriteTx(() -> {

			projectService.formulate(projectNodeRef);

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			// check initialization
			assertNotNull(projectData);
			assertNotNull(projectData.getTaskList());
			assertEquals(6, projectData.getTaskList().size());
			assertEquals(dateFormat.parse("15/11/2012"), projectData.getTaskList().get(0).getStart());
			assertEquals(dateFormat.parse("16/11/2012"), projectData.getTaskList().get(0).getEnd());
			assertEquals(dateFormat.parse("19/11/2012"), projectData.getTaskList().get(1).getStart());
			assertEquals(dateFormat.parse("20/11/2012"), projectData.getTaskList().get(1).getEnd());
			assertEquals(dateFormat.parse("21/11/2012"), projectData.getTaskList().get(2).getStart());
			assertEquals(dateFormat.parse("22/11/2012"), projectData.getTaskList().get(2).getEnd());
			assertEquals(dateFormat.parse("23/11/2012"), projectData.getTaskList().get(3).getStart());
			assertEquals(dateFormat.parse("26/11/2012"), projectData.getTaskList().get(3).getEnd());
			assertEquals(dateFormat.parse("23/11/2012"), projectData.getTaskList().get(4).getStart());
			assertEquals(dateFormat.parse("26/11/2012"), projectData.getTaskList().get(4).getEnd());
			assertEquals(dateFormat.parse("27/11/2012"), projectData.getTaskList().get(5).getStart());
			assertEquals(dateFormat.parse("28/11/2012"), projectData.getTaskList().get(5).getEnd());

			// modify some tasks
			projectData.getTaskList().get(0).setStart(dateFormat.parse("19/11/2012"));
			projectData.getTaskList().get(1).setDuration(4);
			alfrescoRepository.save(projectData);
			projectService.formulate(projectNodeRef);

			// check
			projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
			assertEquals(dateFormat.parse("19/11/2012"), projectData.getTaskList().get(0).getStart());
			assertEquals(dateFormat.parse("20/11/2012"), projectData.getTaskList().get(0).getEnd());
			assertEquals(dateFormat.parse("21/11/2012"), projectData.getTaskList().get(1).getStart());
			assertEquals(dateFormat.parse("26/11/2012"), projectData.getTaskList().get(1).getEnd());
			assertEquals(dateFormat.parse("27/11/2012"), projectData.getTaskList().get(2).getStart());
			assertEquals(dateFormat.parse("28/11/2012"), projectData.getTaskList().get(2).getEnd());

			return null;
		});

		inWriteTx(() -> {

			// start project
			nodeService.setProperty(projectNodeRef, ProjectModel.PROP_PROJECT_STATE, ProjectState.Planned);
			nodeService.setProperty(projectNodeRef, ProjectModel.PROP_PROJECT_STATE, ProjectState.InProgress);

			return null;
		});

		inWriteTx(() -> {

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			assertEquals(today, projectData.getTaskList().get(0).getStart());

			// submit 1st task
			projectService.submitTask(projectData.getTaskList().get(0).getNodeRef(), "test 1");

			return null;
		});

		inWriteTx(() -> {

			projectService.formulate(projectNodeRef);

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			// check
			assertEquals(today, projectData.getTaskList().get(0).getEnd());
			assertEquals(nextStartDate, projectData.getTaskList().get(1).getStart());

			// submit 2nd task
			logger.debug("submit 2nd task");
			projectService.submitTask(projectData.getTaskList().get(1).getNodeRef(), "test 2");

			return null;
		});

		inWriteTx(() -> {

			projectService.formulate(projectNodeRef);

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			// check
			assertEquals(today, projectData.getTaskList().get(1).getStart());
			assertEquals(today, projectData.getTaskList().get(1).getEnd());
			assertEquals(nextStartDate, projectData.getTaskList().get(2).getStart());

			// submit 3rd task
			logger.debug("submit 3rd task");
			projectService.submitTask(projectData.getTaskList().get(2).getNodeRef(), "test 3");

			return null;
		});

		inWriteTx(() -> {

			projectService.formulate(projectNodeRef);

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			// check
			assertEquals(today, projectData.getTaskList().get(2).getStart());
			assertEquals(today, projectData.getTaskList().get(2).getEnd());
			assertEquals(nextStartDate, projectData.getTaskList().get(3).getStart());

			// submit 4th task
			logger.debug("submit 4th task");
			projectService.submitTask(projectData.getTaskList().get(3).getNodeRef(), "test 4");

			return null;
		});

		inWriteTx(() -> {

			Collection<QName> dataLists = new ArrayList<>();
			dataLists.add(ProjectModel.TYPE_DELIVERABLE_LIST);
			dataLists.add(ProjectModel.TYPE_TASK_LIST);

			projectService.formulate(projectNodeRef);

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			// check
			assertEquals(today, projectData.getTaskList().get(3).getStart());
			assertEquals(today, projectData.getTaskList().get(3).getEnd());
			assertEquals(nextStartDate, projectData.getTaskList().get(4).getStart());

			return null;
		});
	}
}
