package fr.becpg.repo.web.scripts.product;

import java.io.IOException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.BeCPGModel;

/**
 *
 * @author steven
 *
 */
public class ReqCtrlWebScript extends AbstractProductWebscript {

	private static final Log logger = LogFactory.getLog(ReqCtrlWebScript.class);

	NodeService nodeService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		NodeRef productNodeRef = getProductNodeRef(req);

		try {
			JSONObject ret = new JSONObject();

			String entityScore = (String) nodeService.getProperty(productNodeRef, BeCPGModel.PROP_ENTITY_SCORE);
			
			// might be null if product has never been formulated, if not put it
			// in res
			if (entityScore != null) {
				JSONObject scores = new JSONObject(entityScore);
				ret.put("scores", scores);
				if (logger.isDebugEnabled()) {
					logger.debug("\n\nret : " + ret);
				}
			}

			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			ret.write(res.getWriter());
		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		}

		
	}

}
