/*
 * 
 */
package fr.becpg.repo.workflow.jbpm.npd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.springframework.beans.factory.BeanFactory;

import fr.becpg.repo.admin.NPDGroup;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public class AssignDefaultGroup extends JBPMSpringActionHandler {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -5350531116399906801L;
	
	private static Log logger = LogFactory.getLog(AssignDefaultGroup.class);

	
	private  SearchService searchService;
	 
	private AuthorityService authorityService;
	
	private WorkflowService workflowService;
	
	
	/* (non-Javadoc)
	 * @see org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler#initialiseHandler(org.springframework.beans.factory.BeanFactory)
	 */
	@Override
	protected void initialiseHandler(BeanFactory factory) {
		workflowService = (WorkflowService) factory.getBean("WorkflowService");
		searchService = (SearchService) factory.getBean("searchService");
		authorityService = (AuthorityService) factory.getBean("authorityService");
	}
	
	/* (non-Javadoc)
	 * @see org.jbpm.graph.def.ActionHandler#execute(org.jbpm.graph.exe.ExecutionContext)
	 */
	@Override
	public void execute(ExecutionContext executionContext) throws Exception {
		
		
		
		TaskInstance taskInstance = executionContext.getTaskInstance();
		
		
		
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		

		List<NodeRef> assignees = new ArrayList<NodeRef>();
			
		for(String auth : authorityService.getContainedAuthorities(AuthorityType.GROUP,PermissionService.GROUP_PREFIX + NPDGroup.FaisabilityAssignersGroup.toString()  , true)){
			logger.debug("Assign : "+auth+" to jbpm$" +taskInstance.getId());
			assignees.add(findGroupNode(auth));
		}
		
		java.util.Map<QName, List<NodeRef>> assocs = new HashMap<QName, List<NodeRef>>();
		assocs.put(WorkflowModel.ASSOC_GROUP_ASSIGNEES, assignees);
		WorkflowTask task  = workflowService.getTaskById(  "jbpm$" +taskInstance.getId());
		workflowService.updateTask(task.getId(), properties, assocs,new HashMap<QName, List<NodeRef>>());
		
		
	}
	
	

	private NodeRef findGroupNode(String groupShortName)
    {
        //TODO Use new AuthorityService.getNode() method on HEAD
        NodeRef group = null;
        
        String query = "+TYPE:\"cm:authorityContainer\" AND @cm\\:authorityName:*" + groupShortName;
        
        ResultSet results = null;
        try
        {
            results = searchService.query(
                    new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"), 
                    SearchService.LANGUAGE_LUCENE, query);
            
            if (results.length() > 0)
            {
                group = results.getNodeRefs().get(0);
            }
        }
        finally
        {
            if (results != null)
            {
                results.close();
            }
        }
        return group;
    }

}
