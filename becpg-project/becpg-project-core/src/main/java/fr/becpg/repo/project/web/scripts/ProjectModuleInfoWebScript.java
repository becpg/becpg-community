/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.project.ProjectService;

/**
 * return Project Module Info
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ProjectModuleInfoWebScript extends AbstractWebScript {

	private static final Log logger = LogFactory.getLog(ProjectModuleInfoWebScript.class);

	private static final String PARAM_SITE = "site";

	private static final String PARAM_ENTITY_NODEREF = "entityNodeRef";

	private NodeService nodeService;

	private ProjectService projectService;

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>projectService</code>.</p>
	 *
	 * @param projectService a {@link fr.becpg.repo.project.ProjectService} object.
	 */
	public void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		String siteId = req.getParameter(PARAM_SITE);

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		String entityNodeRef = req.getParameter(PARAM_ENTITY_NODEREF);

		NodeRef projectNodeRef = null;
		if ((entityNodeRef != null) && !entityNodeRef.isEmpty()) {
			projectNodeRef = new NodeRef(entityNodeRef);
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
				
				String label = (String)nodeService.getProperty(legend,ContentModel.PROP_TITLE);
				if(label == null || label.isEmpty()) {
					label = (String)nodeService.getProperty(legend, ContentModel.PROP_NAME);
				}
				
				lObj.put("label", label);
				lObj.put("color", nodeService.getProperty(legend, BeCPGModel.PROP_COLOR));
				if (projectNodeRef == null) {
					lObj.put("nbProjects", projectService.getNbProjectsByLegend(legend, siteId));
				}
				lObj.put("sort", nodeService.getProperty(legend, BeCPGModel.PROP_SORT));

				String siteIds = (String) nodeService.getProperty(legend, ProjectModel.PROP_TASK_LEGEND_SITES);
				if (siteId == null || (siteIds == null) || siteIds.isEmpty() || contains(siteId, siteIds)) {
					jsonArray.put(lObj);
				}

			}

			obj.put("legends", jsonArray);

			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			res.getWriter().write(obj.toString(3));

		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		} finally {
			if (logger.isDebugEnabled() && watch!=null) {
				logger.debug("ProjectModuleInfoWebScript execute in " + watch.getTotalTimeSeconds() + "s");
			}
		}
	}

	private boolean contains(String siteId, String siteIds) {
		for (String tmp : siteIds.split(",")) {
			if (tmp.equals(siteId)) {
				return true;
			}
		}
		return false;
	}

}
