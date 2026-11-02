package fr.becpg.test.project;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

import fr.becpg.repo.project.impl.DefaultWorkingDayProvider;
import fr.becpg.repo.project.impl.ProjectHelper;
import fr.becpg.repo.project.impl.WorkingDayProvider;

public class ProjectHelperTest {

	/** Monday 3 January 2022 to Tuesday 27 October 2026: 1257 working days and 502 week-end days. */
	private static final Date LONG_TASK_START = gmtDate(2022, Calendar.JANUARY, 3);
	private static final Date LONG_TASK_END = gmtDate(2026, Calendar.OCTOBER, 27);
	private static final int LONG_TASK_WORKING_DAYS = 1257;

	@Test
	public void testRemoveTimeTimeZoneIndependent() {
		Date now = new Date();
		TimeZone original = TimeZone.getDefault();
		try {
			TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
			Date date1 = ProjectHelper.removeTime(now);

			TimeZone.setDefault(TimeZone.getTimeZone("GMT+2"));
			Date date2 = ProjectHelper.removeTime(now);

			Assert.assertEquals("removeTime should be timezone independent", date1.getTime(), date2.getTime());
		} finally {
			TimeZone.setDefault(original);
		}
	}
	
	@Test
	public void testCalculateNextDate() {
		TimeZone gmt = TimeZone.getTimeZone("GMT");
		Calendar cal = Calendar.getInstance(gmt);
		cal.set(2019, Calendar.OCTOBER, 26, 10, 10, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date startDate = cal.getTime();
		
		cal.set(2021, Calendar.JANUARY, 14, 0, 0, 0);
		Date endDate = cal.getTime();
		
		startDate = ProjectHelper.removeTime(startDate);
		
		Calendar check = Calendar.getInstance(gmt);
		check.setTime(startDate);
		Assert.assertEquals(0, check.get(Calendar.HOUR_OF_DAY));
		Assert.assertEquals(0, check.get(Calendar.MINUTE));
		
		int duration = ProjectHelper.calculateTaskDuration(startDate, endDate, new DefaultWorkingDayProvider());
		
		Assert.assertEquals(319, duration);
		
		Date nextDate = ProjectHelper.calculateNextDate(startDate, duration+1, true, new DefaultWorkingDayProvider());
		
		Assert.assertNotNull(nextDate);
		Assert.assertEquals(endDate, nextDate);
	}

	@Test
	public void testCalculateTaskDurationOverMoreThanMaxNonWorkingDays() {
		int duration = ProjectHelper.calculateTaskDuration(LONG_TASK_START, LONG_TASK_END, new DefaultWorkingDayProvider());

		Assert.assertEquals(LONG_TASK_WORKING_DAYS, duration);
	}

	@Test
	public void testCalculateNextDateOverMoreThanMaxNonWorkingDays() {
		Date nextDate = ProjectHelper.calculateNextDate(LONG_TASK_START, LONG_TASK_WORKING_DAYS, true, new DefaultWorkingDayProvider());

		Assert.assertEquals(LONG_TASK_END, nextDate);
	}

	@Test
	public void testCalculateNextDateStopsOnCalendarWithoutWorkingDay() {
		Date nextDate = ProjectHelper.calculateNextDate(LONG_TASK_START, 2, true, new NoWorkingDayProvider());

		Assert.assertEquals(gmtDate(2023, Calendar.JANUARY, 5), nextDate);
	}

	@Test
	public void testCalculateTaskDurationStopsOnCalendarWithoutWorkingDay() {
		int duration = ProjectHelper.calculateTaskDuration(LONG_TASK_START, LONG_TASK_END, new NoWorkingDayProvider());

		Assert.assertEquals(1, duration);
	}

	private static Date gmtDate(int year, int month, int day) {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		cal.clear();
		cal.set(year, month, day);
		return cal.getTime();
	}

	private static class NoWorkingDayProvider implements WorkingDayProvider {

		@Override
		public boolean isWorkingDay(Date date) {
			return false;
		}

		@Override
		public Date getNextWorkingDay(Date date) {
			return date;
		}
	}

}
