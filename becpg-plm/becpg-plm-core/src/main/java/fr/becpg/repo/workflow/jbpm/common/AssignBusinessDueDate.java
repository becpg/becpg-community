/*
 * 
 */
package fr.becpg.repo.workflow.jbpm.common;

import java.util.Calendar;
import java.util.Date;

import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.jbpm.calendar.BusinessCalendar;
import org.jbpm.calendar.Duration;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.springframework.beans.factory.BeanFactory;

/**
 * A jBPM Action Handler for assigning the due date for the current
 * task to "task creation date" + the duration given.
 * 
 * Due date will always fall on a business day per the jBPM business calendar
 * included in Alfresco.
 * 
 * The configuration of this action is as follows:
 * 
 * <addDuration>2 business days</addDuration>
 * 
 * Any valid jBPM duration string may be used such as "2 minutes", "4 business hours", etc.
 * 
 * code written by Jeff D. Brown - CityTech, Inc.
 *
 * @author querephi
 */
public class AssignBusinessDueDate extends JBPMSpringActionHandler {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -5350531116399906801L;

	/** The business calendar. */
	protected static BusinessCalendar businessCalendar = new BusinessCalendar();
	
	/** The add duration. */
	protected String addDuration;	
	
	/* (non-Javadoc)
	 * @see org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler#initialiseHandler(org.springframework.beans.factory.BeanFactory)
	 */
	@Override
	protected void initialiseHandler(BeanFactory arg0) {
		
	}
	
	/* (non-Javadoc)
	 * @see org.jbpm.graph.def.ActionHandler#execute(org.jbpm.graph.exe.ExecutionContext)
	 */
	@Override
	public void execute(ExecutionContext executionContext) throws Exception {
				
		if (this.addDuration == null) {
			throw new WorkflowException("addDuration parameter has not been provided");
		}
				
		Calendar cal = Calendar.getInstance();
		BusinessCalendar businessCalendar = new BusinessCalendar();		
		Duration duration = new Duration(this.addDuration);
		Date dueDate = businessCalendar.add(cal.getTime(), duration);
		TaskInstance taskInstance = executionContext.getTaskInstance();
		taskInstance.setDueDate(dueDate);		
	}

}
