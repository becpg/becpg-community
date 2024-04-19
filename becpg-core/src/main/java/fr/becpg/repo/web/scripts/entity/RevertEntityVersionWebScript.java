package fr.becpg.repo.web.scripts.entity;

import java.io.IOException;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.entity.version.EntityVersionService;

public class RevertEntityVersionWebScript extends AbstractWebScript {

	private static final String PARAM_STORE_TYPE = "store_type";
	private static final String PARAM_STORE_ID = "store_id";
	private static final String PARAM_ID = "id";

	private EntityVersionService entityVersionService;

	private SiteService siteService;
	
	private NodeService nodeService;
	
	private NamespaceService namespaceService;
	
	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}
	
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		NodeRef versionNodeRef = null;

		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();

		if (templateArgs != null) {
			String storeType = templateArgs.get(PARAM_STORE_TYPE);
			String storeId = templateArgs.get(PARAM_STORE_ID);
			String nodeId = templateArgs.get(PARAM_ID);
			if (storeType != null && storeId != null && nodeId != null) {
				versionNodeRef = new NodeRef(storeType, storeId, nodeId);
			}
		}
		
		NodeRef newEntityNodeRef = null;
		try {
			newEntityNodeRef = entityVersionService.revertVersion(versionNodeRef);
		} catch (IllegalAccessException e1) {
			throw new WebScriptException(e1.getMessage());
		}
		
		try {
			JSONObject ret = new JSONObject();

			if (newEntityNodeRef != null) {
				ret.put("persistedObject", newEntityNodeRef);
				ret.put("type", nodeService.getType(newEntityNodeRef).getPrefixedQName(namespaceService));
				ret.put("siteId", siteService.getSite(newEntityNodeRef).getShortName());
			}

			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			ret.write(res.getWriter());
		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		}

	}

}
