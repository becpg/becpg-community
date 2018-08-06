package fr.becpg.repo.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.web.scripts.workflow.WorkflowModelBuilder;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.repo.web.scripts.process.EntityProcessListPlugin;



@Service("workflowProcessListPlugin")
public class WorkflowProcessListPlugin implements EntityProcessListPlugin {
	
	@Autowired
	private NamespaceService namespaceService;
	@Autowired
	private NodeService nodeService;
	@Autowired
	private PersonService personService;
	@Autowired
	private DictionaryService dictionaryService;
	@Autowired
	private AuthenticationService authenticationService;
	@Autowired
	@Qualifier("WorkflowService")
	private WorkflowService workflowService;
	
	@Override
	public List<Map<String, Object>> buildModel(NodeRef nodeRef){
		
		WorkflowModelBuilder modelBuilder = new WorkflowModelBuilder(namespaceService, nodeService, authenticationService, 
                personService, workflowService, dictionaryService);
		
		// list all active and closed workflows for nodeRef
		List<WorkflowInstance> workflows = workflowService.getWorkflowsForContent(nodeRef, true);
		workflows.addAll(workflowService.getWorkflowsForContent(nodeRef, false));
		
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>(workflows.size());
		
		for (WorkflowInstance workflow : workflows){
			Map<String, Object> tmp = modelBuilder.buildSimple(workflow);
			tmp.put(PROCESS_INSTANCE_START_DATE, FORMATER.format(workflow.getStartDate()));
			tmp.put(PROCESS_INSTANCE_DUE_DATE, FORMATER.format(workflow.getDueDate()));
			tmp.put(PROCESS_INSTANCE_TYPE, getType());
			results.add(tmp);
		}
		
		return results;
		
	}

	  
	
	@Override
	public String getType() {
		return "workflow";
	}


	
	
	
}
