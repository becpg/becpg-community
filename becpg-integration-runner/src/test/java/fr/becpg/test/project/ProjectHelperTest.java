package fr.becpg.test.project;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import fr.becpg.repo.project.impl.ProjectHelper;

public class ProjectHelperTest {

	
	@Test
	@SuppressWarnings("deprecation")
	public void testCalculateNextDate() {
		Date startDate = new Date(2019,9,26, 10,10);
		Date endDate = new Date(2020,12,14);
		startDate = ProjectHelper.removeTime(startDate);
		
		Assert.assertEquals(0, startDate.getHours());
		Assert.assertEquals(0, startDate.getMinutes());
		
		int duration = ProjectHelper.calculateTaskDuration(startDate, endDate);
		
		Assert.assertEquals(320,duration);
		
		Date nextDate = ProjectHelper.calculateNextDate(startDate, duration+1, true);
		
		
		
		Assert.assertNotNull(nextDate);
		Assert.assertEquals(endDate, nextDate);
		
	}
	
}
