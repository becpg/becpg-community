package fr.becpg.test.project;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.ProjectRepoConsts;
import fr.becpg.repo.project.CalendarService;
import fr.becpg.repo.project.data.PlanningMode;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.impl.CalendarWorkingDayProvider;
import fr.becpg.repo.project.impl.ProjectHelper;

public class CalendarServiceIT extends AbstractProjectTestCase {

	@Autowired
	@Qualifier("projectCalendarService")
	private CalendarService calendarService;

	@Test
	public void testCalendarServiceWithHolidays() {
		NodeRef calendarRef = createCalendar("Test Calendar", "2023/12/25;2024/01/01", "2023/08/01-2023/08/15", null);
		assertNotNull(calendarRef);

		Calendar cal = Calendar.getInstance();

		// 2023/12/25 is Monday, but holiday
		cal.set(2023, Calendar.DECEMBER, 25, 0, 0, 0);
		assertFalse("Christmas should be holiday", calendarService.isWorkingDay(cal.getTime(), calendarRef));

		// 2023/12/26 is Tuesday, working day
		cal.set(2023, Calendar.DECEMBER, 26, 0, 0, 0);
		assertTrue("Day after Christmas should be working day", calendarService.isWorkingDay(cal.getTime(), calendarRef));

		// 2023/08/10 is in summer holidays range
		cal.set(2023, Calendar.AUGUST, 10, 0, 0, 0);
		assertFalse("Summer holiday should be non-working", calendarService.isWorkingDay(cal.getTime(), calendarRef));

		// Test weekend (Saturday)
		cal.set(2023, Calendar.DECEMBER, 23, 0, 0, 0);
		assertFalse("Saturday should be non-working by default", calendarService.isWorkingDay(cal.getTime(), calendarRef));

		// Test weekend (Sunday)
		cal.set(2023, Calendar.DECEMBER, 24, 0, 0, 0);
		assertFalse("Sunday should be non-working by default", calendarService.isWorkingDay(cal.getTime(), calendarRef));
	}

	@Test
	public void testCalendarWithDefaultNonWorkingDays() {
		NodeRef calendarRef = createCalendar("Default Calendar", null, null, null);
		assertNotNull(calendarRef);

		Calendar cal = Calendar.getInstance();

		// Test default weekend (Saturday)
		cal.set(2024, Calendar.FEBRUARY, 3, 0, 0, 0); // Saturday
		assertFalse("Saturday should be non-working by default", calendarService.isWorkingDay(cal.getTime(), calendarRef));

		// Test default weekend (Sunday)
		cal.set(2024, Calendar.FEBRUARY, 4, 0, 0, 0); // Sunday
		assertFalse("Sunday should be non-working by default", calendarService.isWorkingDay(cal.getTime(), calendarRef));

		// Test weekday (Monday)
		cal.set(2024, Calendar.FEBRUARY, 5, 0, 0, 0); // Monday
		assertTrue("Monday should be working day", calendarService.isWorkingDay(cal.getTime(), calendarRef));

		// Test weekday (Friday)
		cal.set(2024, Calendar.FEBRUARY, 9, 0, 0, 0); // Friday
		assertTrue("Friday should be working day", calendarService.isWorkingDay(cal.getTime(), calendarRef));
	}

	@Test
	public void testCalendarWithCustomNonWorkingDays() {
		List<Integer> nonWorkingDays = new ArrayList<>();
		nonWorkingDays.add(Calendar.SATURDAY); // Saturday only

		NodeRef calendarRef = createCalendar("Custom Calendar", null, null, nonWorkingDays);
		assertNotNull(calendarRef);

		Calendar cal = Calendar.getInstance();

		// Test Saturday (configured as non-working)
		cal.set(2024, Calendar.FEBRUARY, 3, 0, 0, 0); // Saturday
		assertFalse("Saturday should be non-working", calendarService.isWorkingDay(cal.getTime(), calendarRef));

		// Test Sunday (NOT configured as non-working)
		cal.set(2024, Calendar.FEBRUARY, 4, 0, 0, 0); // Sunday
		assertTrue("Sunday should be working day with custom config", calendarService.isWorkingDay(cal.getTime(), calendarRef));

		// Test Monday
		cal.set(2024, Calendar.FEBRUARY, 5, 0, 0, 0); // Monday
		assertTrue("Monday should be working day", calendarService.isWorkingDay(cal.getTime(), calendarRef));
	}

	@Test
	public void testCalendarWithFridaySaturdayWeekend() {
		List<Integer> nonWorkingDays = new ArrayList<>();
		nonWorkingDays.add(6); // Friday
		nonWorkingDays.add(Calendar.SATURDAY); // Saturday

		NodeRef calendarRef = createCalendar("Friday-Saturday Weekend", null, null, nonWorkingDays);
		assertNotNull(calendarRef);

		Calendar cal = Calendar.getInstance();

		// Test Friday (configured as non-working)
		cal.set(2024, Calendar.FEBRUARY, 2, 0, 0, 0); // Friday
		assertFalse("Friday should be non-working", calendarService.isWorkingDay(cal.getTime(), calendarRef));

		// Test Saturday (configured as non-working)
		cal.set(2024, Calendar.FEBRUARY, 3, 0, 0, 0); // Saturday
		assertFalse("Saturday should be non-working", calendarService.isWorkingDay(cal.getTime(), calendarRef));

		// Test Sunday (working day in this config)
		cal.set(2024, Calendar.FEBRUARY, 4, 0, 0, 0); // Sunday
		assertTrue("Sunday should be working day", calendarService.isWorkingDay(cal.getTime(), calendarRef));

		// Test Thursday (working day)
		cal.set(2024, Calendar.FEBRUARY, 1, 0, 0, 0); // Thursday
		assertTrue("Thursday should be working day", calendarService.isWorkingDay(cal.getTime(), calendarRef));
	}

	@Test
	public void testCalendarWithSixDayWeek() {
		List<Integer> nonWorkingDays = new ArrayList<>();
		nonWorkingDays.add(Calendar.SUNDAY); // Sunday only

		NodeRef calendarRef = createCalendar("Six Day Week", null, null, nonWorkingDays);
		assertNotNull(calendarRef);

		Calendar cal = Calendar.getInstance();

		// Test Sunday (non-working)
		cal.set(2024, Calendar.FEBRUARY, 4, 0, 0, 0); // Sunday
		assertFalse("Sunday should be non-working", calendarService.isWorkingDay(cal.getTime(), calendarRef));

		// Test Saturday (working day in 6-day week)
		cal.set(2024, Calendar.FEBRUARY, 3, 0, 0, 0); // Saturday
		assertTrue("Saturday should be working day in 6-day week", calendarService.isWorkingDay(cal.getTime(), calendarRef));

		// Test all weekdays
		cal.set(2024, Calendar.FEBRUARY, 5, 0, 0, 0); // Monday
		assertTrue("Monday should be working day", calendarService.isWorkingDay(cal.getTime(), calendarRef));

		cal.set(2024, Calendar.FEBRUARY, 9, 0, 0, 0); // Friday
		assertTrue("Friday should be working day", calendarService.isWorkingDay(cal.getTime(), calendarRef));
	}

	@Test
	public void testCalendarAssociationWithProject() {
		NodeRef calendarRef = createCalendar("Project Calendar", null, null, null);
		NodeRef projectRef = createProject(ProjectState.Planned, new Date(), null, PlanningMode.Planning);

		nodeService.createAssociation(projectRef, calendarRef, ProjectModel.ASSOC_PROJECT_CALENDAR);

		NodeRef fetchedCal = calendarService.getCalendar(projectRef);
		assertEquals("Calendar should be associated with project", calendarRef, fetchedCal);
	}

	@Test
	public void testCalendarWithNoConfiguration() {
		Calendar cal = Calendar.getInstance();

		// Test with null calendar - should use default weekend
		cal.set(2024, Calendar.FEBRUARY, 3, 0, 0, 0); // Saturday
		assertFalse("Saturday should be non-working with null calendar", calendarService.isWorkingDay(cal.getTime(), null));

		cal.set(2024, Calendar.FEBRUARY, 4, 0, 0, 0); // Sunday
		assertFalse("Sunday should be non-working with null calendar", calendarService.isWorkingDay(cal.getTime(), null));

		cal.set(2024, Calendar.FEBRUARY, 5, 0, 0, 0); // Monday
		assertTrue("Monday should be working day with null calendar", calendarService.isWorkingDay(cal.getTime(), null));
	}

	@Test
	public void testEndDateSkipsNonWorkingStartDay() throws ParseException {
		NodeRef calendarRef = createCalendar("Skip Non Working Start", null, null, null);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		formatter.setTimeZone(ProjectRepoConsts.PROJECT_TIMEZONE);
		Date startDate = formatter.parse("2024/02/03"); // Saturday
		CalendarWorkingDayProvider provider = new CalendarWorkingDayProvider(calendarService, calendarRef);

		Date endDate = ProjectHelper.calculateEndDate(startDate, 1, provider);
		assertEquals("Duration of 1 starting on non-working day should end on next working day", formatter.parse("2024/02/05"), endDate);

		Date endDateTwoDays = ProjectHelper.calculateEndDate(startDate, 2, provider);
		assertEquals("Duration of 2 should advance two working days from first working day", formatter.parse("2024/02/06"), endDateTwoDays);
	}

	@Test
	public void testEndDateMultiDaysWithThreeDaysWeekend() throws Exception {

		List<Integer> nonWorkingDays = Arrays.asList(Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY);

		NodeRef calendarRef = createCalendar("3-days-off", null, null, nonWorkingDays);

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		formatter.setTimeZone(ProjectRepoConsts.PROJECT_TIMEZONE);

		CalendarWorkingDayProvider provider = new CalendarWorkingDayProvider(calendarService, calendarRef);

		Date startThursday = formatter.parse("2024/02/01");

		assertEnd(formatter, provider, startThursday, 1, "2024/02/01");
		assertEnd(formatter, provider, startThursday, 2, "2024/02/05");
		assertEnd(formatter, provider, startThursday, 3, "2024/02/06");
		assertEnd(formatter, provider, startThursday, 4, "2024/02/07");
		assertEnd(formatter, provider, startThursday, 6, "2024/02/12");

		Date startFriday = formatter.parse("2024/02/02");

		assertEnd(formatter, provider, startFriday, 1, "2024/02/05");
		assertEnd(formatter, provider, startFriday, 2, "2024/02/06");
		assertEnd(formatter, provider, startFriday, 3, "2024/02/07");
		assertEnd(formatter, provider, startFriday, 4, "2024/02/08");
		assertEnd(formatter, provider, startFriday, 6, "2024/02/13");
	}

	@Test
	public void testEndDateAlwaysWorkingDay() throws Exception {

		List<Integer> nonWorkingDays = Arrays.asList(Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY);

		NodeRef calendarRef = createCalendar("invariant", null, null, nonWorkingDays);

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		formatter.setTimeZone(ProjectRepoConsts.PROJECT_TIMEZONE);

		CalendarWorkingDayProvider provider = new CalendarWorkingDayProvider(calendarService, calendarRef);

		Date start = formatter.parse("2024/02/01");

		for (int duration : Arrays.asList(1, 2, 3, 4, 5, 6, 7, 10)) {
			Date end = ProjectHelper.calculateEndDate(start, duration, provider);

			assertTrue("End date must be working day for duration=" + duration, calendarService.isWorkingDay(end, calendarRef));
		}
	}
	
	@Test
	public void testStartDateNotShiftedWhenNonWorking() throws Exception {

	    List<Integer> nonWorkingDays = Arrays.asList(
	            Calendar.FRIDAY,
	            Calendar.SATURDAY,
	            Calendar.SUNDAY
	    );

	    NodeRef calendarRef = createCalendar("no-shift-start", null, null, nonWorkingDays);

	    SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
	    formatter.setTimeZone(ProjectRepoConsts.PROJECT_TIMEZONE);

	    CalendarWorkingDayProvider provider =
	            new CalendarWorkingDayProvider(calendarService, calendarRef);

	    Date startFriday = formatter.parse("2024/02/02"); 

	    Date end = ProjectHelper.calculateEndDate(startFriday, 1, provider);
	    assertEquals(formatter.parse("2024/02/05"), end);

	    assertFalse(calendarService.isWorkingDay(startFriday, calendarRef));
	}

	@Test
	public void testEndDateDurationZero() throws Exception {

		NodeRef calendarRef = createCalendar("zero-duration", null, null, null);

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		formatter.setTimeZone(ProjectRepoConsts.PROJECT_TIMEZONE);

		CalendarWorkingDayProvider provider = new CalendarWorkingDayProvider(calendarService, calendarRef);

		Date start = formatter.parse("2024/02/05");

		Date end = ProjectHelper.calculateEndDate(start, 0, provider);

		assertEquals("Duration 0 should return start date", start, end);
	}

	@Test
	public void testEndDateWithTimeComponent() {

		NodeRef calendarRef = createCalendar("time-test", null, null, null);

		Calendar cal = Calendar.getInstance(ProjectRepoConsts.PROJECT_TIMEZONE);
		cal.set(2024, Calendar.FEBRUARY, 5, 15, 30, 45);

		CalendarWorkingDayProvider provider = new CalendarWorkingDayProvider(calendarService, calendarRef);

		Date end = ProjectHelper.calculateEndDate(cal.getTime(), 1, provider);

		assertTrue(calendarService.isWorkingDay(end, calendarRef));
	}


	private void assertEnd(SimpleDateFormat formatter, CalendarWorkingDayProvider provider, Date start, int duration, String expected)
			throws ParseException {

		Date end = ProjectHelper.calculateEndDate(start, duration, provider);

		assertEquals("start=" + start + " duration=" + duration, formatter.parse(expected), end);
	}

	private NodeRef createCalendar(String name, String publicHolidays, String holidays, List<Integer> nonWorkingDays) {
		Map<QName, Serializable> props = new HashMap<>();
		props.put(BeCPGModel.PROP_CHARACT_NAME, name);
		if (publicHolidays != null) {
			props.put(ProjectModel.PROP_CAL_PUBLIC_HOLYDAYS_DATES, publicHolidays);
		}
		if (holidays != null) {
			props.put(ProjectModel.PROP_CAL_HOLYDAYS_DATES, holidays);
		}
		if (nonWorkingDays != null) {
			props.put(ProjectModel.PROP_CAL_NON_WORKING_DAYS, (Serializable) nonWorkingDays);
		}

		NodeRef folder = getTestFolderNodeRef();
		return nodeService.createNode(folder, ContentModel.ASSOC_CONTAINS, ProjectModel.TYPE_CALENDAR, ProjectModel.TYPE_CALENDAR, props)
				.getChildRef();
	}

}
