package fr.becpg.repo.project.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Calendar;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.ProjectRepoConsts;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.project.CalendarService;

/**
 * <p>CalendarServiceImpl class.</p>
 *
 * @author matthieu
 */
@Service("projectCalendarService")
public class CalendarServiceImpl implements CalendarService {

	private static final Log logger = LogFactory.getLog(CalendarServiceImpl.class);

	private static final String DATE_FORMAT = "yyyy/MM/dd";
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
	private static final String SHORT_DATE_FORMAT = "MM/dd";
	private static final DateTimeFormatter SHORT_FORMATTER = DateTimeFormatter.ofPattern(SHORT_DATE_FORMAT);

	@Autowired
	private NodeService nodeService;

	@Autowired
	private EntityService entityService;

	@Autowired
	private AssociationService associationService;

	/** {@inheritDoc} */
	@Override
	public boolean isWorkingDay(Date date, NodeRef calendarNodeRef) {
		if (date == null) {
			return false;
		}
		LocalDate localDate = asLocalDate(date);
		return isWorkingDay(localDate, getHolidays(calendarNodeRef, localDate.getYear()), getNonWorkingDays(calendarNodeRef));
	}

	private boolean isWorkingDay(LocalDate date, Set<LocalDate> holidays, Set<Integer> nonWorkingDays) {
		// DayOfWeek.getValue() returns 1 (Monday) to 7 (Sunday)
		// java.util.Calendar uses 1 (Sunday) to 7 (Saturday)
		int dayOfWeekValue = (date.getDayOfWeek().getValue() % 7) + 1;
		if (nonWorkingDays.contains(dayOfWeekValue)) {
			return false;
		}
		return !holidays.contains(date);
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getCalendar(NodeRef nodeRef) {
		if ((nodeRef == null) || !nodeService.exists(nodeRef)) {
			return null;
		}

		QName type = nodeService.getType(nodeRef);
		if (ProjectModel.TYPE_PROJECT.equals(type)) {
			return associationService.getTargetAssoc(nodeRef, ProjectModel.ASSOC_PROJECT_CALENDAR);
		} else if (ProjectModel.TYPE_TASK_LIST.equals(type)) {
			// First check task specific calendar
			NodeRef cal = associationService.getTargetAssoc(nodeRef, ProjectModel.ASSOC_TL_CALENDAR);
			if (cal != null) {
				return cal;
			}
			// Fallback to project calendar
			NodeRef projectNodeRef = entityService.getEntityNodeRef(nodeRef, ProjectModel.TYPE_TASK_LIST);
			if ((projectNodeRef != null) && nodeService.exists(projectNodeRef)) {
				return associationService.getTargetAssoc(projectNodeRef, ProjectModel.ASSOC_PROJECT_CALENDAR);
			}
		}
		return null;
	}

	private Set<LocalDate> getHolidays(NodeRef calendarNodeRef, int targetYear) {
		Set<LocalDate> holidays = new HashSet<>();
		if ((calendarNodeRef == null) || !nodeService.exists(calendarNodeRef)) {
			return holidays;
		}

		parseDates(holidays, (String) nodeService.getProperty(calendarNodeRef, ProjectModel.PROP_CAL_PUBLIC_HOLYDAYS_DATES), targetYear);
		parseDates(holidays, (String) nodeService.getProperty(calendarNodeRef, ProjectModel.PROP_CAL_HOLYDAYS_DATES), targetYear);

		return holidays;
	}

	private Set<Integer> getNonWorkingDays(NodeRef calendarNodeRef) {
		Set<Integer> nonWorkingDays = new HashSet<>();

		if ((calendarNodeRef == null) || !nodeService.exists(calendarNodeRef)) {
			addDefaultDays(nonWorkingDays);
			return nonWorkingDays;
		}

		Object configuredDays = nodeService.getProperty(calendarNodeRef, ProjectModel.PROP_CAL_NON_WORKING_DAYS);

		if (!(configuredDays instanceof List)) {
			addDefaultDays(nonWorkingDays);
			return nonWorkingDays;
		}

		@SuppressWarnings("unchecked")
		List<Object> daysList = (List<Object>) configuredDays;

		if (daysList.isEmpty()) {
			addDefaultDays(nonWorkingDays);
			return nonWorkingDays;
		}

		for (Object day : daysList) {
			if (day == null) {
				continue;
			}
			try {
				if (day instanceof Number) {
					nonWorkingDays.add(((Number) day).intValue());
				} else {
					String s = day.toString().trim();
					if (!s.isEmpty()) {
						nonWorkingDays.add(Integer.valueOf(s));
					}
				}
			} catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.debug("Failed to parse non-working day: " + day, e);
				}
			}
		}

		return nonWorkingDays;
	}

	private void addDefaultDays(Set<Integer> target) {
		target.add(Calendar.SATURDAY); // 7
		target.add(Calendar.SUNDAY); // 1
	}

	
	private void parseDates(Set<LocalDate> holidays, String dateString, int targetYear) {
		if ((dateString == null) || dateString.isBlank()) {
			return;
		}
		String[] parts = dateString.split(";");
		for (String part : parts) {
			part = part.trim();
			if (part.isEmpty()) {
				continue;
			}

			try {
				if (part.contains("-")) {
					parseDateRange(holidays, part);
				} else if (part.length() == 5) { // Format MM/dd 
					holidays.add(java.time.MonthDay.parse(part, SHORT_FORMATTER).atYear(targetYear));
				} else { // Format yyyy/MM/dd
					holidays.add(LocalDate.parse(part, FORMATTER));
				}
			} catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.debug("Failed to parse date string: " + part, e);
				}
			}
		}
	}

	private LocalDate asLocalDate(Date date) {
		return date.toInstant().atZone(ProjectRepoConsts.PROJECT_TIMEZONE.toZoneId()).toLocalDate();
	}

	private void parseDateRange(Set<LocalDate> holidays, String dateRange) {
		String[] range = dateRange.split("-");
		if (range.length == 2) {
			LocalDate start = LocalDate.parse(range[0].trim(), FORMATTER);
			LocalDate end = LocalDate.parse(range[1].trim(), FORMATTER);
			if (!start.isAfter(end)) {
				LocalDate current = start;
				while (!current.isAfter(end)) {
					holidays.add(current);
					current = current.plusDays(1);
				}
			}
		}
	}
}
