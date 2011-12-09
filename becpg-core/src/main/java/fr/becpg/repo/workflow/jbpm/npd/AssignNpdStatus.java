/*
 * 
 */
package fr.becpg.repo.workflow.jbpm.npd;

import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.workflow.jbpm.JBPMNode;
import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
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

	private NodeService nodeService;
	
	private FileFolderService fileFolderService;
	
	/** The add duration. */
	protected String status;

	private String NPD_WF_STATUS = "npdwf_npdStatus";	
	private String NPD_WF_STATUS_INST = "npdwf:npdStatus";	
	
	/* (non-Javadoc)
	 * @see org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler#initialiseHandler(org.springframework.beans.factory.BeanFactory)
	 */
	@Override
	protected void initialiseHandler(BeanFactory factory) {
		
		nodeService = (NodeService)factory.getBean("nodeService");
		fileFolderService = (FileFolderService)factory.getBean("fileFolderService");
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
		
		/*
		 * Modify WF status
		 */
		
		executionContext.getContextInstance().setVariable(NPD_WF_STATUS , status);
		executionContext.getContextInstance().setVariable(NPD_WF_STATUS_INST , status);
		
		/*
		 * Modify Product status
		 */
		
		final JBPMNode jBPMNode = (JBPMNode) executionContext.getContextInstance().getVariable("bpm_package");		
		final NodeRef pkgNodeRef = jBPMNode.getNodeRef();
		
		RunAsWork<Object> actionRunAs = new RunAsWork<Object>()
        {
            @Override
			public Object doWork() throws Exception
            {
            	try{
        			//change state
            		List<FileInfo> files = fileFolderService.listFiles(pkgNodeRef);
            		for(FileInfo file : files){
            			
            			NodeRef productNodeRef = file.getNodeRef();
            			nodeService.setProperty(productNodeRef, NPDModel.PROP_NPD_STATUS, status);		            			
            		}
        		}
        		catch(Exception e){
        			logger.error("Failed to change product NPD status", e);
        			throw e;
        		}
        		
        		return null;
            }
        };
        AuthenticationUtil.runAs(actionRunAs, AuthenticationUtil.getAdminUserName());	
		
	}

}
