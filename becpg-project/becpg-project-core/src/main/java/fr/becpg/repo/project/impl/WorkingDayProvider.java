package fr.becpg.repo.project.impl;

import java.util.Date;

/**
 * Interface for providing working day calculations.
 * Allows different implementations (default weekend-only or calendar-based).
 *
 * @author matthieu
 */
public interface WorkingDayProvider {

    /**
     * Check if a date is a working day.
     *
     * @param date the date to check
     * @return true if working day
     */
    boolean isWorkingDay(Date date);

    /**
     * Get the next working day after the given date.
     *
     * @param date the starting date
     * @return the next working day
     */
    Date getNextWorkingDay(Date date);

}
