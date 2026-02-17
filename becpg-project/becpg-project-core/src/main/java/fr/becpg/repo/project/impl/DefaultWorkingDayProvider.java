package fr.becpg.repo.project.impl;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.ProjectRepoConsts;

/**
 * Default implementation that only considers weekends (Saturday and Sunday) as non-working days.
 * This maintains backward compatibility with the original ProjectHelper logic.
 */
public class DefaultWorkingDayProvider implements WorkingDayProvider {

    private static final Log logger = LogFactory.getLog(DefaultWorkingDayProvider.class);

    private static final int MAX_ITERATIONS = 366;

    @Override
    public boolean isWorkingDay(Date date) {
        if (date == null) {
            return false;
        }
        Calendar calendar = Calendar.getInstance(ProjectRepoConsts.PROJECT_TIMEZONE);
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return (dayOfWeek != Calendar.SATURDAY) && (dayOfWeek != Calendar.SUNDAY);
    }

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
                logger.warn("No working day found within " + MAX_ITERATIONS + " days");
                return calendar.getTime();
            }
        }

        return calendar.getTime();
    }
    
}
