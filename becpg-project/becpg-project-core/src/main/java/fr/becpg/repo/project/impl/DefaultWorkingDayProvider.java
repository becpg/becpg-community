package fr.becpg.repo.project.impl;

import java.util.Calendar;
import java.util.Date;

import fr.becpg.repo.ProjectRepoConsts;

/**
 * Default implementation that only considers weekends (Saturday and Sunday) as non-working days.
 * This maintains backward compatibility with the original ProjectHelper logic.
 */
public class DefaultWorkingDayProvider implements WorkingDayProvider {

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
        
        while (!isWorkingDay(calendar.getTime())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        return calendar.getTime();
    }
    
}
