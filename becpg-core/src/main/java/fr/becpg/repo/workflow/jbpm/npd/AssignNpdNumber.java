/*
 * 
 */
package fr.becpg.repo.workflow.jbpm.npd;

import java.util.Calendar;

import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.graph.exe.ExecutionContext;
import org.springframework.beans.factory.BeanFactory;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public class AssignNpdNumber extends JBPMSpringActionHandler {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -5350531116399906801L;
	
	private static Log logger = LogFactory.getLog(AssignNpdNumber.class);

	

	private String NPD_WF_NUMBER = "npdwf_npdNumber";	
	private String NPD_WF_NUMBER_INST = "npdwf:npdNumber";	
	
	private String NPD_WF_DESCRIPTION= "bpm_workflowDescription";	
	private String NPD_WF_DESCRIPTION_INST = "bpm:workflowDescription";
	
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
				
		String number = "NPD-"+Calendar.getInstance().get(Calendar.YEAR)+"-"+executionContext.getContextInstance().getId();
		String description = number+" "+ (String) executionContext.getContextInstance().getVariable("npdwf_npdProductName");
		
		
		logger.debug("Assign npdNumber : "+number);
		logger.debug("Assign description : "+description);
		
		executionContext.getContextInstance().setVariable(NPD_WF_NUMBER , number);
		executionContext.getContextInstance().setVariable(NPD_WF_NUMBER_INST , number);
		
		executionContext.getContextInstance().setVariable(NPD_WF_DESCRIPTION , description);
		executionContext.getContextInstance().setVariable(NPD_WF_DESCRIPTION_INST , description);
		
	}

}
