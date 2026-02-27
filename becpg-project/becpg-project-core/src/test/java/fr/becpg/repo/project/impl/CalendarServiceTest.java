package fr.becpg.repo.project.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.ProjectRepoConsts;

public class CalendarServiceTest {

    @Mock
    private NodeService nodeService;

    @InjectMocks
    private CalendarServiceImpl calendarService;

    private NodeRef calendarRef = new NodeRef("workspace://SpacesStore/calendar");

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testIsWorkingDayWithHolidays() {
        when(nodeService.exists(calendarRef)).thenReturn(true);
        // Christmas 2023 (Monday)
        when(nodeService.getProperty(calendarRef, ProjectModel.PROP_CAL_PUBLIC_HOLYDAYS_DATES)).thenReturn("2023/12/25");
        
        Calendar cal = Calendar.getInstance(ProjectRepoConsts.PROJECT_TIMEZONE);
        cal.set(2023, Calendar.DECEMBER, 25, 12, 0, 0);
        
        assertFalse("Christmas should be holiday", calendarService.isWorkingDay(cal.getTime(), calendarRef));
        
        cal.set(2023, Calendar.DECEMBER, 26, 12, 0, 0);
        assertTrue("Day after Christmas should be working day", calendarService.isWorkingDay(cal.getTime(), calendarRef));
    }

    @Test
    public void testIsWorkingDayWithDateRange() {
        when(nodeService.exists(calendarRef)).thenReturn(true);
        when(nodeService.getProperty(calendarRef, ProjectModel.PROP_CAL_HOLYDAYS_DATES)).thenReturn("2023/08/01-2023/08/15");
        
        Calendar cal = Calendar.getInstance(ProjectRepoConsts.PROJECT_TIMEZONE);
        cal.set(2023, Calendar.AUGUST, 10, 12, 0, 0);
        
        assertFalse("August 10th should be in holiday range", calendarService.isWorkingDay(cal.getTime(), calendarRef));
        
        cal.set(2023, Calendar.AUGUST, 16, 12, 0, 0);
        assertTrue("August 16th should be working day", calendarService.isWorkingDay(cal.getTime(), calendarRef));
    }

    @Test
    public void testIsWorkingDayWithCustomNonWorkingDays() {
        when(nodeService.exists(calendarRef)).thenReturn(true);
        List<String> nonWorkingDays = new ArrayList<>();
        nonWorkingDays.add("1"); // Sunday
        when(nodeService.getProperty(calendarRef, ProjectModel.PROP_CAL_NON_WORKING_DAYS)).thenReturn((java.io.Serializable) nonWorkingDays);
        
        Calendar cal = Calendar.getInstance(ProjectRepoConsts.PROJECT_TIMEZONE);
        cal.set(2024, Calendar.FEBRUARY, 4, 12, 0, 0); // Sunday
        assertFalse("Sunday should be non-working", calendarService.isWorkingDay(cal.getTime(), calendarRef));
        
        cal.set(2024, Calendar.FEBRUARY, 3, 12, 0, 0); // Saturday
        assertTrue("Saturday should be working day with custom config", calendarService.isWorkingDay(cal.getTime(), calendarRef));
    }
    
    @Test
    public void testIsWorkingDayWithIntegerNonWorkingDays() {
        when(nodeService.exists(calendarRef)).thenReturn(true);
        List<Integer> nonWorkingDays = new ArrayList<>();
        nonWorkingDays.add(1); // Sunday
        when(nodeService.getProperty(calendarRef, ProjectModel.PROP_CAL_NON_WORKING_DAYS)).thenReturn((java.io.Serializable) nonWorkingDays);
        
        Calendar cal = Calendar.getInstance(ProjectRepoConsts.PROJECT_TIMEZONE);
        cal.set(2024, Calendar.FEBRUARY, 4, 12, 0, 0); // Sunday
        assertFalse("Sunday should be non-working even if configured with Integers", calendarService.isWorkingDay(cal.getTime(), calendarRef));
    }

    @Test
    public void testIsWorkingDayWithEmptyNonWorkingDays() {
        when(nodeService.exists(calendarRef)).thenReturn(true);
        when(nodeService.getProperty(calendarRef, ProjectModel.PROP_CAL_NON_WORKING_DAYS)).thenReturn((java.io.Serializable) Collections.emptyList());
        
        Calendar cal = Calendar.getInstance(ProjectRepoConsts.PROJECT_TIMEZONE);
        cal.set(2024, Calendar.FEBRUARY, 3, 12, 0, 0); // Saturday
        assertFalse("Saturday should be non-working by default even if empty list", calendarService.isWorkingDay(cal.getTime(), calendarRef));
    }
    
    @Test
    public void testIsWorkingDayWithNullCalendar() {
        Calendar cal = Calendar.getInstance(ProjectRepoConsts.PROJECT_TIMEZONE);
        cal.set(2024, Calendar.FEBRUARY, 3, 12, 0, 0); // Saturday
        assertFalse("Saturday should be non-working by default", calendarService.isWorkingDay(cal.getTime(), null));
        
        cal.set(2024, Calendar.FEBRUARY, 5, 12, 0, 0); // Monday
        assertTrue("Monday should be working day by default", calendarService.isWorkingDay(cal.getTime(), null));
    }
}
