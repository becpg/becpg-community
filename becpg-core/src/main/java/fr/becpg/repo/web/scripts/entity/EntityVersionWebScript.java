/*
 * 
 */
package fr.becpg.repo.web.scripts.entity;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.entity.version.EntityVersion;
import fr.becpg.repo.entity.version.EntityVersionService;

/**
 * The Class VersionHistoryWebScript.
 *
 * @author querephi
 */
public class EntityVersionWebScript extends AbstractWebScript  {

	// request parameter names
	private static final String PARAM_NODEREF = "nodeRef";		
	private static final String DISPLAY_FORMAT = "dd MMM yyyy HH:mm:ss 'GMT'Z '('zzz')'";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(EntityVersionWebScript.class);		
	
	/** The entity version service. */
	private EntityVersionService entityVersionService;
	
	private NodeService nodeService;
	
	private PersonService personService;
		
	/**
	 * Sets the entity version service.
	 *
	 * @param entityVersionService the new entity version service
	 */
	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	/**
	 * Get entity version history.
	 */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		
		logger.debug("VersionWebScript executeImpl()");			
		NodeRef nodeRef = new NodeRef(req.getParameter(PARAM_NODEREF));	
		SimpleDateFormat displayFormat = new SimpleDateFormat(DISPLAY_FORMAT);
				
		List<EntityVersion> versions = entityVersionService.getAllVersions(nodeRef);
		
		try {
			JSONArray jsonVersions = new JSONArray();
			
			for(EntityVersion version : versions){
				
				logger.info("###props: " + version.getVersionProperties());
				
				JSONObject jsonVersion = new JSONObject();
				jsonVersion.put("nodeRef", version.getEntityVersionNodeRef());
				jsonVersion.put("name", nodeService.getProperty(version.getFrozenStateNodeRef(), ContentModel.PROP_NAME));
				jsonVersion.put("label", version.getVersionLabel());
				jsonVersion.put("description", version.getDescription());
				jsonVersion.put("createdDate", displayFormat.format(version.getFrozenModifiedDate()));
				jsonVersion.put("createdDateISO", ISO8601DateFormat.format(version.getFrozenModifiedDate()));
				jsonVersions.put(jsonVersion);
				
				NodeRef creatorNodeRef = personService.getPerson(version.getFrozenModifier());
				JSONObject jsonCreator = new JSONObject();
				jsonCreator.put("userName", (String)nodeService.getProperty(creatorNodeRef, ContentModel.PROP_USERNAME));
				jsonCreator.put("firstName", (String)nodeService.getProperty(creatorNodeRef, ContentModel.PROP_FIRSTNAME));
				jsonCreator.put("lastName", (String)nodeService.getProperty(creatorNodeRef, ContentModel.PROP_LASTNAME));
				jsonVersion.put("creator", jsonCreator);
				
			}		
			
			res.setContentType("application/json");
	        res.setContentEncoding("UTF-8");		
			res.getWriter().write(jsonVersions.toString(3));
			
		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON");
		}
		
	}	
	
}
