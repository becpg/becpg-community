/*
 *
 */
package fr.becpg.test.project.formulation;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.project.CalendarService;
import fr.becpg.repo.ProjectRepoConsts;
import fr.becpg.repo.project.data.PlanningMode;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.impl.CalendarWorkingDayProvider;
import fr.becpg.repo.project.impl.DefaultWorkingDayProvider;
import fr.becpg.repo.project.impl.ProjectHelper;
import fr.becpg.test.project.AbstractProjectTestCase;

/**
 * The Class ProjectCalculatePlanningDatesTest.
 *
 * @author quere
 */
public class ProjectCalculatePlanningDatesIT extends AbstractProjectTestCase {

	private static final Log logger = LogFactory.getLog(ProjectCalculatePlanningDatesIT.class);

	@Autowired
	@Qualifier("projectCalendarService")
	private CalendarService calendarService;

	@Test
	public void testCalculatePlanningDates() throws ParseException {

		final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));

		final NodeRef projectNodeRef = createProject(ProjectState.OnHold, dateFormat.parse("15/11/2012"), null);
		final Date today = ProjectHelper.removeTime(new Date());
		final Date nextStartDate = ProjectHelper.calculateNextStartDate(today, new DefaultWorkingDayProvider());

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			projectService.formulate(projectNodeRef);

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);


			// check initialization
			assertNotNull(projectData);
			assertNotNull(projectData.getTaskList());
			assertEquals(6, projectData.getTaskList().size());
			assertEquals(dateFormat.parse("15/11/2012"), projectData.getTaskList().get(0).getStart());
			assertEquals(dateFormat.parse("16/11/2012"), projectData.getTaskList().get(0).getEnd());
			assertEquals(ProjectHelper.calculateNextStartDate(projectData.getTaskList().get(0).getTargetEnd(), new DefaultWorkingDayProvider()),
					projectData.getTaskList().get(1).getTargetStart());
			assertEquals(dateFormat.parse("19/11/2012"), projectData.getTaskList().get(1).getStart());
			assertEquals(dateFormat.parse("20/11/2012"), projectData.getTaskList().get(1).getEnd());
			assertEquals(ProjectHelper.calculateNextStartDate(projectData.getTaskList().get(1).getTargetEnd(), new DefaultWorkingDayProvider()),
					projectData.getTaskList().get(2).getTargetStart());
			assertEquals(dateFormat.parse("21/11/2012"), projectData.getTaskList().get(2).getStart());
			assertEquals(dateFormat.parse("22/11/2012"), projectData.getTaskList().get(2).getEnd());
			assertEquals(ProjectHelper.calculateNextStartDate(projectData.getTaskList().get(2).getTargetEnd(), new DefaultWorkingDayProvider()),
					projectData.getTaskList().get(3).getTargetStart());
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
			assertEquals(ProjectHelper.calculateNextStartDate(projectData.getTaskList().get(0).getTargetEnd(), new DefaultWorkingDayProvider()),
					projectData.getTaskList().get(1).getTargetStart());
			assertEquals(dateFormat.parse("21/11/2012"), projectData.getTaskList().get(1).getStart());
			assertEquals(dateFormat.parse("26/11/2012"), projectData.getTaskList().get(1).getEnd());
			assertEquals(ProjectHelper.calculateNextStartDate(projectData.getTaskList().get(1).getTargetEnd(), new DefaultWorkingDayProvider()),
					projectData.getTaskList().get(2).getTargetStart());
			assertEquals(dateFormat.parse("27/11/2012"), projectData.getTaskList().get(2).getStart());
			assertEquals(dateFormat.parse("28/11/2012"), projectData.getTaskList().get(2).getEnd());

			return null;
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

		        	// start project
				nodeService.setProperty(projectNodeRef, ProjectModel.PROP_PROJECT_STATE, ProjectState.Planned);
				nodeService.setProperty(projectNodeRef, ProjectModel.PROP_PROJECT_STATE, ProjectState.InProgress);

			return null;
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			assertEquals(today, projectData.getTaskList().get(0).getStart());

			// submit 1st task
			projectService.submitTask(projectData.getTaskList().get(0).getNodeRef(), "test 1");

			return null;
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			projectService.formulate(projectNodeRef);

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			// check
			assertEquals(today, projectData.getTaskList().get(0).getEnd());
			assertEquals(nextStartDate, projectData.getTaskList().get(1).getStart());

			// submit 2nd task
			logger.debug("submit 2nd task");
			projectService.submitTask(projectData.getTaskList().get(1).getNodeRef(), "test 2");

			return null;
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

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
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

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
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

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
		}, false, true);
	}

	/**
	 * Verifies that an in-progress project keeps its planned start aligned with the actual start when duration changes.
	 *
	 * @throws ParseException if the test dates cannot be parsed.
	 */
	@Test
	public void testInProgressProjectKeepsPlannedStartAligned() throws ParseException {
		final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		dateFormat.setTimeZone(ProjectRepoConsts.PROJECT_TIMEZONE);

		final NodeRef projectNodeRef = createProject(ProjectState.InProgress, dateFormat.parse("26/03/2026"), null,
				PlanningMode.Planning);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			projectService.formulate(projectNodeRef);

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			assertNotNull(projectData);
			assertNotNull(projectData.getTaskList());
			assertFalse(projectData.getTaskList().isEmpty());
			assertEquals(projectData.getStartDate(), projectData.getTaskList().get(0).getTargetStart());

			projectData.getTaskList().get(0).setDuration(4);
			alfrescoRepository.save(projectData);
			projectService.formulate(projectNodeRef);

			projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
			assertEquals(projectData.getStartDate(), projectData.getTaskList().get(0).getTargetStart());

			return null;
		}, false, true);
	}

	/**
	 * Verifies that a project without explicit startDate is planned to today (or next working day),
	 * not to tomorrow. Regression for #33571.
	 */
	@Test
	public void testProjectWithoutStartDateUsesToday() {

		final NodeRef projectNodeRef = createProject(ProjectState.OnHold, null, null, PlanningMode.Planning);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			projectService.formulate(projectNodeRef);

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);

			assertNotNull(projectData);
			assertNotNull(projectData.getStartDate());

			Date expected = ProjectHelper.findNextWorkingDay(ProjectHelper.removeTime(projectData.getCreated()), new DefaultWorkingDayProvider());
			assertEquals("Project startDate must not be shifted to tomorrow when no explicit startDate is provided", expected,
					ProjectHelper.removeTime(projectData.getStartDate()));

			return null;
		}, false, true);
	}

	/**
	 * Verifies that task start dates use the previous task calendar and task end dates use the current task calendar.
	 *
	 * @throws ParseException if the test dates cannot be parsed.
	 */
	@Test
	public void testTaskStartUsesPreviousTaskCalendar() throws ParseException {
		final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		dateFormat.setTimeZone(ProjectRepoConsts.PROJECT_TIMEZONE);

		final NodeRef projectNodeRef = createProject(ProjectState.Planned, dateFormat.parse("29/01/2024"), null, PlanningMode.Planning);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
			assertNotNull(projectData);
			assertNotNull(projectData.getTaskList());
			assertTrue(projectData.getTaskList().size() >= 2);

			List<Integer> predecessorNonWorkingDays = new ArrayList<>();
			predecessorNonWorkingDays.add(Calendar.TUESDAY);
			NodeRef predecessorCalendarRef = createCalendar("previousTaskCalendar", predecessorNonWorkingDays);

			List<Integer> currentNonWorkingDays = new ArrayList<>();
			currentNonWorkingDays.add(Calendar.THURSDAY);
			NodeRef currentCalendarRef = createCalendar("currentTaskCalendar", currentNonWorkingDays);

			nodeService.createAssociation(projectData.getTaskList().get(0).getNodeRef(), predecessorCalendarRef, ProjectModel.ASSOC_TL_CALENDAR);
			nodeService.createAssociation(projectData.getTaskList().get(1).getNodeRef(), currentCalendarRef, ProjectModel.ASSOC_TL_CALENDAR);
			List<NodeRef> prevTasks = new ArrayList<>();
			prevTasks.add(projectData.getTaskList().get(0).getNodeRef());
			projectData.getTaskList().get(1).setPrevTasks(prevTasks);
			projectData.getTaskList().get(1).setDuration(2);
			alfrescoRepository.save(projectData);

			projectService.formulate(projectNodeRef);

			projectData = (ProjectData) alfrescoRepository.findOne(projectNodeRef);
			Date predecessorEnd = projectData.getTaskList().get(0).getEnd();
			assertNotNull(predecessorEnd);

			CalendarWorkingDayProvider predecessorProvider = new CalendarWorkingDayProvider(calendarService, predecessorCalendarRef);
			Date expectedStart = ProjectHelper.calculateNextStartDate(predecessorEnd, predecessorProvider);

			assertEquals(expectedStart, projectData.getTaskList().get(1).getStart());

			CalendarWorkingDayProvider currentProvider = new CalendarWorkingDayProvider(calendarService, currentCalendarRef);
			Date expectedEnd = ProjectHelper.calculateEndDate(projectData.getTaskList().get(1).getStart(), projectData.getTaskList().get(1).getDuration(),
					currentProvider);
			assertEquals(expectedEnd, projectData.getTaskList().get(1).getEnd());

			return null;
		}, false, true);
	}

	private NodeRef createCalendar(String name, List<Integer> nonWorkingDays) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_CHARACT_NAME, name);
		properties.put(ProjectModel.PROP_CAL_NON_WORKING_DAYS, (Serializable) nonWorkingDays);

		return nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), ProjectModel.TYPE_CALENDAR, properties).getChildRef();
	}
}
