package fr.becpg.repo.project;

import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Service to manage Calendar and working days calculation.
 */
public interface CalendarService {

    /**
     * Check if a date is a working day.
     * 
     * @param date the date to check
     * @param calendarNodeRef the calendar node ref (can be null)
     * @return true if working day
     */
    boolean isWorkingDay(Date date, NodeRef calendarNodeRef);
    
    /**
     * Get the calendar associated to a project or task.
     * @param nodeRef the project or task node ref
     * @return the calendar node ref or null
     */
    NodeRef getCalendar(NodeRef nodeRef);
}
