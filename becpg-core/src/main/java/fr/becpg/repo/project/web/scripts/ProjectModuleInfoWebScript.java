package fr.becpg.repo.project.web.scripts;

import java.io.IOException;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.project.ProjectService;

/**
 * return Project Module Info
 * 
 * @author matthieu
 * 
 */
public class ProjectModuleInfoWebScript extends AbstractWebScript {

	private static Log logger = LogFactory.getLog(ProjectModuleInfoWebScript.class);

	private static final String PARAM_SITE = "site";

	private NodeService nodeService;

	private ProjectService projectService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		String siteId = req.getParameter(PARAM_SITE);

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		try {

			NodeRef projectContainer = projectService.getProjectsContainer(siteId);
			List<NodeRef> legends = projectService.getTaskLegendList();

			JSONObject obj = new JSONObject();
			JSONArray jsonArray = new JSONArray();

			obj.put("parentNodeRef", projectContainer);

			for (NodeRef legend : legends) {
				JSONObject lObj = new JSONObject();
				lObj.put("nodeRef", legend);
				lObj.put("label", nodeService.getProperty(legend, ContentModel.PROP_NAME));
				lObj.put("color", nodeService.getProperty(legend, BeCPGModel.PROP_COLOR));
				jsonArray.put(lObj);
			}

			obj.put("legends", jsonArray);

			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			res.getWriter().write(obj.toString(3));

		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		} finally {
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug("FormulateCharactDetailsWebScript execute in " + watch.getTotalTimeSeconds() + "s");
			}
		}
	}

}
