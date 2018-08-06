package fr.becpg.repo.web.scripts.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.web.scripts.workflow.AbstractWorkflowWebscript;
import org.alfresco.repo.web.scripts.workflow.WorkflowModelBuilder;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class EntityProcessListWebscript extends AbstractWorkflowWebscript {

	private static final Log logger = LogFactory.getLog(EntityProcessListWebscript.class);
	
	public static final String PARAM_STORE_TYPE = "store_type";
	public static final String PARAM_STORE_ID = "store_id";
	public static final String PARAM_NODE_ID = "id";
	public static final String PARAM_ALL_TYPE = "all";
	
	
	@Autowired
	private EntityProcessListPlugin[] processPlugins;
	
	@Override
	protected Map<String, Object> buildModel(WorkflowModelBuilder modelBuilder, WebScriptRequest req, Status status, Cache cache){
		
		Map<String, String> params = req.getServiceMatch().getTemplateVars();
		
		// get nodeRef from request
		NodeRef nodeRef = new NodeRef(params.get(PARAM_STORE_TYPE), params.get(PARAM_STORE_ID), params.get(PARAM_NODE_ID));
		
		List<Map<String, Object>> results = new ArrayList<>();
		List<String> processTypes = new ArrayList<>();
		
		processTypes.add(PARAM_ALL_TYPE);
	
		for(EntityProcessListPlugin processPlugin : processPlugins){
			List<Map<String, Object>> tmp = processPlugin.buildModel(nodeRef);
			results.addAll(tmp);
			if(tmp.size() > 0){
				processTypes.add(processPlugin.getType());
			}
			
		}
		
		Map<String, Object> model = new HashMap<String, Object>();
		// build the model for ftl
		model.put("processInstances", results);
		model.put("processTypes", processTypes);
		
		return model;
		
	}
	
}
