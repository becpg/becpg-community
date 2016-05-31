/*
 *
 */
package fr.becpg.repo.web.scripts.entity.datalist;

import java.io.IOException;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.web.scripts.remote.AbstractEntityWebScript;

/**
 * Webscript that copy a datalist to another entity
 *
 * @author matthieu
 */
public class CopyDataListWebScript extends AbstractEntityWebScript {

	protected static final String PARAM_STORE_TYPE = "store_type";
	protected static final String PARAM_STORE_ID = "store_id";
	protected static final String PARAM_ID = "id";

	private EntityListDAO entityListDAO;

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {

		try {

			JSONArray ret = new JSONArray();

			Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
			if (templateArgs != null) {
				String storeType = templateArgs.get(PARAM_STORE_TYPE);
				String storeId = templateArgs.get(PARAM_STORE_ID);
				String nodeId = templateArgs.get(PARAM_ID);
				if ((storeType != null) && (storeId != null) && (nodeId != null)) {
					NodeRef dataListNodeRef = new NodeRef(storeType, storeId, nodeId);
					NodeRef entityNodeRef = null;

					JSONObject json = (JSONObject) req.parseContent();
					if ((json != null) && json.has("entity")) {
						entityNodeRef = new NodeRef((String) json.get("entity"));

						entityListDAO.copyDataList(dataListNodeRef, entityNodeRef, true);

						ret.put(entityNodeRef);

					}

				}

			}

			resp.setContentType("application/json");
			resp.setContentEncoding("UTF-8");
			resp.getWriter().write(ret.toString(3));
		} catch (JSONException e) {
			throw new WebScriptException(e.getMessage());
		}

	}
}
