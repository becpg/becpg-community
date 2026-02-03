package fr.becpg.repo.project.impl;

import java.util.Calendar;
import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.project.CalendarService;

/**
 * Implementation that uses CalendarService to consider custom holidays and calendars.
 */
public class CalendarWorkingDayProvider extends DefaultWorkingDayProvider implements WorkingDayProvider  {

    private final CalendarService calendarService;
    private final NodeRef calendarNodeRef;

    /**
     * Constructor.
     *
     * @param calendarService the calendar service
     * @param calendarNodeRef the calendar node reference (can be null for default behavior)
     */
    public CalendarWorkingDayProvider(CalendarService calendarService, NodeRef calendarNodeRef) {
        this.calendarService = calendarService;
        this.calendarNodeRef = calendarNodeRef;
    }

    @Override
    public boolean isWorkingDay(Date date) {
        return calendarNodeRef != null ? calendarService.isWorkingDay(date, calendarNodeRef) : super.isWorkingDay(date);
    }

    @Override
    public Date getNextWorkingDay(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        
        while (!isWorkingDay(calendar.getTime())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        return calendar.getTime();
    }

}
