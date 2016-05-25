package fr.becpg.repo.web.scripts.product;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.NutDatabaseService;

public class NutDatabaseImportWebScript extends AbstractWebScript {

	private static final Log logger = LogFactory.getLog(NutDatabaseImportWebScript.class);

	private NutDatabaseService nutDatabaseService;

	public void setNutDatabaseService(NutDatabaseService nutDatabaseService) {
		this.nutDatabaseService = nutDatabaseService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) {
		try {

			JSONObject json = (JSONObject) req.parseContent();
			String entities = "";
			if ((json != null) && json.has("entities")) {
				entities = (String) json.get("entities");
			}
			NodeRef destNodeRef = null;
			String destination = req.getParameter("dest");
			if (destination != null) {
				destNodeRef = new NodeRef(destination);
			}

			NodeRef file = null;
			if ((json != null) && json.has("supplier")) {
				file = new NodeRef(json.getString("supplier"));
			}

			Map<QName, Serializable> props = new HashMap<>();
			props.put(BeCPGModel.PROP_CODE, null);
			props.put(ContentModel.PROP_OWNER, AuthenticationUtil.getFullyAuthenticatedUser());
			JSONArray ret = new JSONArray();
			for (final String entity : entities.split(",")) {
				NodeRef entityNodeRef;
				entityNodeRef = nutDatabaseService.createProduct(file, entity, destNodeRef);
				if (entityNodeRef == null) {
					logger.debug("createProduct returned null");
				}

				ret.put(entityNodeRef);
			}

			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			res.getWriter().write(ret.toString(3));
		} catch (JSONException e) {
			throw new WebScriptException(e.getMessage());
		} catch (IOException e) {
			throw new WebScriptException(e.getMessage());
		}
	}

}
