package fr.becpg.test.project;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

import fr.becpg.repo.project.impl.DefaultWorkingDayProvider;
import fr.becpg.repo.project.impl.ProjectHelper;

public class ProjectHelperTest {

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
		
		Date nextDate = ProjectHelper.calculateNextDate(startDate, duration, true, new DefaultWorkingDayProvider());
		
		Assert.assertNotNull(nextDate);
		Assert.assertEquals(endDate, nextDate);
	}
	
}
