package fr.becpg.repo.web.scripts.workflow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.expressions.ExpressionService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>
 * StartProcessWebscript class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 * 
 * Parse process.json and exec script
 *  <code>
 *  {
 *			"title": "Validation",
 *			"itemId": "cm:content",
 *			"itemKind": "type",
 *			"formId": "content-validation",
 *			"condition": "js( items.length > 0 && items.every( (n) =>  n.isDocument ))",
 *			"submissionUrl": "/becpg/workflow/start-process?script=start-validation-workflow.js&nodeRefs={selectedItems}",
 *			"formFields": {
 *				"prop_cm_name": "js(bcpg.getMessage(\"plm.script.start-validation-workflow.js.message\",items.map( (n) =>  n.name ).join(\", \")))"
 *			}
 *		}
 *	<code>
**/
public class StartProcessWebscript extends AbstractWebScript {

	private static final String IN_GROUP = "inGroup";
	
	private static final String IN_GROUP_OR_EMPTY = "inGroupOrEmpty";

	private static final String PARAM_NODEREFS = "nodeRefs";

	private static final String PARAM_DEFS = "defs";

	private static final String PARAM_SCRIPT = "script";

	private static final String CONFIG_PATH = "/app:company_home/cm:System/cm:Config/cm:process.json";

	private static final String SCRIPT_PATH =  "/app:company_home/cm:System/cm:WorkflowScripts";

	private static Log logger = LogFactory.getLog(StartProcessWebscript.class);

	private Repository repository;
	
	private NodeService nodeService;

	private ScriptService scriptService;

	private ContentService contentService;

	private AuthorityService authorityService;

	private WorkflowService workflowService;
	
	private ServiceRegistry serviceRegistry;
	
	private ExpressionService expressionService;
	
	private AuthorityDAO authorityDAO;
	
	private AssociationService associationService;
	
	private PersonService personService;
	
	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}
	
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}
	
	public void setAuthorityDAO(AuthorityDAO authorityDAO) {
		this.authorityDAO = authorityDAO;
	}


	public void setExpressionService(ExpressionService expressionService) {
		this.expressionService = expressionService;
	}

	/**
	 * <p>
	 * Setter for the field <code>repository</code>.
	 * </p>
	 *
	 * @param repository
	 *            a {@link org.alfresco.repo.model.Repository} object.
	 */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}
	
	
	

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	/**
	 * <p>
	 * Setter for the field <code>scriptService</code>.
	 * </p>
	 *
	 * @param scriptService
	 *            a {@link org.alfresco.service.cmr.repository.ScriptService}
	 *            object.
	 */
	public void setScriptService(ScriptService scriptService) {
		this.scriptService = scriptService;
	}


	/**
	 * <p>
	 * Setter for the field <code>nodeService</code>.
	 * </p>
	 *
	 * @param nodeService
	 *            a {@link org.alfresco.service.cmr.repository.NodeService}
	 *            object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>
	 * Setter for the field <code>contentService</code>.
	 * </p>
	 *
	 * @param contentService
	 *            a {@link org.alfresco.service.cmr.repository.ContentService}
	 *            object.
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	/**
	 * <p>
	 * Setter for the field <code>authorityService</code>.
	 * </p>
	 *
	 * @param authorityService
	 *            a {@link org.alfresco.service.cmr.security.AuthorityService}
	 *            object.
	 */
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	public void setWorkflowService(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		String nodeRefsParam = req.getParameter(PARAM_NODEREFS);

		List<NodeRef> nodeRefs = new ArrayList<>();

		if ((nodeRefsParam != null) && !nodeRefsParam.isEmpty()) {
			for (String nodeRefItem : nodeRefsParam.split(",")) {
				nodeRefs.add(new NodeRef(nodeRefItem));
			}
		}

		try {
			JSONObject ret = new JSONObject();

			if ("true".equals(req.getParameter(PARAM_DEFS))) {
				JSONArray defs = new JSONArray();

				NodeRef configNodeRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(repository.getCompanyHome(), CONFIG_PATH);
				if (configNodeRef != null) {
					ContentReader reader = contentService.getReader(configNodeRef, ContentModel.PROP_CONTENT);
					String content = reader.getContentString();

					JSONObject processJson = new JSONObject(content);
					JSONArray processDefinitions = processJson.getJSONArray("processDefinitions");

					for (int i = 0; i < processDefinitions.length(); i++) {

						JSONObject processDefinition = processDefinitions.getJSONObject(i);

						logger.debug("Parsing process def: " + processDefinition.getString("itemId"));

						if (processDefinition.has("permissionGroup")) {
							String permissionGroup = processDefinition.getString("permissionGroup");

							if ((permissionGroup != null) && !hasCurrentUserPermission(permissionGroup)) {

								logger.debug("Skipping has no permission");
								continue;
							}

						}

						if (processDefinition.has("condition")) {
							String condition = processDefinition.getString("condition");
							if (!Boolean.parseBoolean(expressionService.eval(condition, nodeRefs).toString())) {
								logger.debug("Skipping condition doesn't match : [" + condition + "]");
								continue;
							}
						}
						
						if (processDefinition.has("destination")) {
							processDefinition.put("destination", expressionService.eval(processDefinition.getString("destination"), nodeRefs));
						}

						if (processDefinition.has("formFields")) {
							JSONObject formFields = processDefinition.getJSONObject("formFields");

							Iterator<String> iterator = formFields.keys();

							while (iterator.hasNext()) {

								String key = iterator.next();
								String value = formFields.getString(key);
								formFields.put(key, expressionService.eval(value, nodeRefs));
							}

						}
						WorkflowDefinition workflowDefinition = null;

						if ("workflow".equals(processDefinition.getString("itemKind"))) {
							workflowDefinition = workflowService.getDefinitionByName(processDefinition.getString("itemId"));
						}
						
						
						NodeRef scriptNodeRef = null;
						if ("script".equals(processDefinition.getString("itemKind"))) {
							scriptNodeRef =  BeCPGQueryBuilder.createQuery().selectNodeByPath(repository.getCompanyHome(), SCRIPT_PATH+"/cm:"+processDefinition.getString("itemId"));
						}

						if (processDefinition.has("title")) {
							String title = I18NUtil.getMessage(processDefinition.getString("title"));
							if(title!=null) {
							processDefinition.put("title", title);
							}
						} else {
							if (workflowDefinition != null) {
								processDefinition.put("title", workflowDefinition.getTitle());
							} else if(scriptNodeRef!=null) {
								String title = (String)nodeService.getProperty(scriptNodeRef, ContentModel.PROP_TITLE);
								if(title!=null && !title.isEmpty()) {
									title = (String)nodeService.getProperty(scriptNodeRef, ContentModel.PROP_NAME);
								}
								processDefinition.put("title", title);
							}
						}

						if (processDefinition.has("description")) {
							String description = I18NUtil.getMessage(processDefinition.getString("description"));
							if(description!=null) {
							processDefinition.put("description", description);
							}
						} else {
							if (workflowDefinition != null) {
								processDefinition.put("description", workflowDefinition.getDescription());
							} else if(scriptNodeRef!=null) {
								String desc = (String)nodeService.getProperty(scriptNodeRef, ContentModel.PROP_DESCRIPTION);
								if(desc!=null && !desc.isEmpty()) {
									processDefinition.put("description", desc);
								}
							}
						}

						defs.put(processDefinition);	
					}

				}

				ret.put("processDefinitions", defs);
				
			} else if(req.getParameter(PARAM_SCRIPT)!=null){

				NodeRef processScriptNodeRef =  BeCPGQueryBuilder.createQuery().selectNodeByPath(repository.getCompanyHome(), SCRIPT_PATH+"/cm:"+req.getParameter(PARAM_SCRIPT));
				JSONObject json = (JSONObject) req.parseContent();

				Map<String, Object> model = new HashMap<>();
				model.put("items",  nodeRefs.stream().map(n -> new ActivitiScriptNode(n,serviceRegistry)).toArray(ScriptNode[]::new));
				model.put("formData", json == null ? "" : json.toString());
				
				NodeRef currentUserNodeRef = personService.getPerson(AuthenticationUtil.getFullyAuthenticatedUser());
				if(currentUserNodeRef!=null) {
					model.put("currentUserNodeRef", currentUserNodeRef.toString());
				}
				
				ret.put("persistedObject", scriptService.executeScript(processScriptNodeRef, ContentModel.PROP_CONTENT, model));
			}
			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			ret.write(res.getWriter());
		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		}

	}
	
	/**
	 * 
	 * note : returns true if the group is empty or doesn't exist
	 * 
	 * @param group
	 * @return
	 */
	private boolean hasCurrentUserPermission(String permissionGroup) {
		
		if (authorityService.hasAdminAuthority()) {
			return true;
		}
		
		String[] split = permissionGroup.split("\\|");
		
		String group = split[0];
		
		String permission = IN_GROUP;
		
		if (split.length > 1) {
			permission = split[1];
		}
		
		if (IN_GROUP_OR_EMPTY.equals(permission)) {
			NodeRef authority = authorityDAO.getAuthorityNodeRefOrNull(group);
			
			if (authority == null) {
				return true;
			}
			
			if (associationService.getChildAssocs(authority, ContentModel.ASSOC_MEMBER).isEmpty()) {
				return true;
			}
		}
		
		if (permission.contains(IN_GROUP)) {
			for (String currAuth : authorityService.getAuthorities()) {
				if (group.equals(currAuth)) {
					return true;
				}
			}
		}
		
		return false;
	}


}
