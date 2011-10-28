/*
 * 
 */
package fr.becpg.repo.workflow.jbpm.npd;

import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.graph.exe.ExecutionContext;
import org.springframework.beans.factory.BeanFactory;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public class AssignNpdStatus extends JBPMSpringActionHandler {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -5350531116399906801L;
	
	private static Log logger = LogFactory.getLog(AssignNpdStatus.class);

	
	/** The add duration. */
	protected String status;

	private String NPD_WF_STATUS = "npdwf_npdStatus";	
	private String NPD_WF_STATUS_INST = "npdwf:npdStatus";	
	
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
				
		if (this.status == null) {
			throw new WorkflowException("status parameter has not been provided");
		}
				
		logger.debug("assign npd status:"+status);
		
		executionContext.getContextInstance().setVariable(NPD_WF_STATUS , status);
		executionContext.getContextInstance().setVariable(NPD_WF_STATUS_INST , status);
		
	}

}
