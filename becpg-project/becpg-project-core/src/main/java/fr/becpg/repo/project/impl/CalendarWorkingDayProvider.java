package fr.becpg.repo.project.impl;

import java.util.Calendar;
import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.ProjectRepoConsts;
import fr.becpg.repo.project.CalendarService;

/**
 * Implementation that uses CalendarService to consider custom holidays and calendars.
 *
 * @author matthieu
 */
public class CalendarWorkingDayProvider extends DefaultWorkingDayProvider implements WorkingDayProvider  {

    private static final Log logger = LogFactory.getLog(CalendarWorkingDayProvider.class);

    private static final int MAX_ITERATIONS = 366;

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

    /** {@inheritDoc} */
    @Override
    public boolean isWorkingDay(Date date) {
        return calendarNodeRef != null ? calendarService.isWorkingDay(date, calendarNodeRef) : super.isWorkingDay(date);
    }

    /** {@inheritDoc} */
    @Override
    public Date getNextWorkingDay(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance(ProjectRepoConsts.PROJECT_TIMEZONE);
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        int iterations = 0;
        while (!isWorkingDay(calendar.getTime())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            iterations++;
            if (iterations > MAX_ITERATIONS) {
                logger.warn("No working day found within " + MAX_ITERATIONS + " days for calendar: " + calendarNodeRef);
                return calendar.getTime();
            }
        }

        return calendar.getTime();
    }

}
