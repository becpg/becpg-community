package fr.becpg.repo.web.scripts.entity.datalist;

import java.io.IOException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.entity.datalist.MultiLevelDataListService;

public class MultiLevelDataListWebscript extends AbstractWebScript {

	private static final String PARAM_NODEREF = "nodeRef";
	private static final String PARAM_EXPAND = "expand";

	private MultiLevelDataListService multiLevelDataListService;
	
	public void setMultiLevelDataListService(MultiLevelDataListService multiLevelDataListService) {
		this.multiLevelDataListService = multiLevelDataListService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		String nodeRef = req.getParameter(PARAM_NODEREF);
		boolean expand = "true".equals(req.getParameter(PARAM_EXPAND));

		if (nodeRef != null) {

			NodeRef entityToExpand = new NodeRef(nodeRef);
		
			multiLevelDataListService.expandOrColapseNode(entityToExpand, expand);
			JSONObject ret = new JSONObject();

			try {
				ret.put("nodeRef", entityToExpand);
				ret.put("success", true);
				res.setContentType("application/json");
				res.setContentEncoding("UTF-8");
				ret.write(res.getWriter());
			} catch (JSONException e) {
				throw new WebScriptException("Unable to parse JSON", e);
			}

		} else {
			throw new WebScriptException("nodeRef is mandatory");
		}

	}

}
