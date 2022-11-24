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

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.project.ProjectService;

/**
 * return Project Module Info
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class CompleteProjectTaskWebScript extends AbstractWebScript {

	private static final String PARAM_NODEREF = "nodeRef";

	private ProjectService projectService;

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

		String nodeRef = req.getParameter(PARAM_NODEREF);
		
		if(nodeRef==null) {
			throw new IllegalStateException("Mandatory nodeRef parameter");
		}

		NodeRef taskNodeRef = new NodeRef(nodeRef);
		
		try {
			projectService.submitTask(taskNodeRef, null);
			
			JSONObject obj = new JSONObject();
		
			obj.put(PARAM_NODEREF, taskNodeRef);
			obj.put("succees", true);

			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			res.getWriter().write(obj.toString(3));

		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		} 
	}

}
