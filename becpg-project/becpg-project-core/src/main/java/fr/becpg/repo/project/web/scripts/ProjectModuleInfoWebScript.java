/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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
				logger.debug("ProjectModuleInfoWebScript execute in " + watch.getTotalTimeSeconds() + "s");
			}
		}
	}

}
