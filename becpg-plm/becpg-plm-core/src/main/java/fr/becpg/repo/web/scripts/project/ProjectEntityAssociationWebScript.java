/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG.
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
package fr.becpg.repo.web.scripts.project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 * Create a project and associate it to the selected entity for the advanced search result
 * 
 */
public class ProjectEntityAssociationWebScript extends AbstractWebScript {

	private static final Log logger = LogFactory.getLog(ProjectEntityAssociationWebScript.class);
	
	private static final String PARAM_NODEREFS = "entities";

	private static final String PARAM_TPL_NODEREF = "tplNodeRef";



	private AssociationService associationService;

	private RepoService repoService;

	private AlfrescoRepository<ProjectData> alfrescoRepository;

	
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<ProjectData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}
	

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws WebScriptException, IOException {

		logger.debug("Calling ProjectEntity ws");

		String projectTemplateParam = req.getParameter(PARAM_TPL_NODEREF);

		NodeRef projectTemplateNodeRef = null;
		if (projectTemplateParam == null) {

			JSONObject json = (JSONObject) req.parseContent();
			logger.info("json : "+json);

			try {
				if ((json != null) && json.has("projectTpl") && (json.getString("projectTpl") != null) && !json.getString("projectTpl").isEmpty()) {
					projectTemplateNodeRef = new NodeRef(json.getString("projectTpl"));
				}
			} catch (JSONException e) {
				throw new IllegalStateException(I18NUtil.getMessage("message.supplier.project-template"));
			}

		} else {
			projectTemplateNodeRef = new NodeRef(projectTemplateParam);
		}

		if (projectTemplateNodeRef == null) {
			throw new IllegalStateException(I18NUtil.getMessage("message.supplier.project-template"));
		}

		String nodeRefsParam = req.getParameter(PARAM_NODEREFS);

		List<NodeRef> nodeRefs = new ArrayList<>();

		if ((nodeRefsParam != null) && !nodeRefsParam.isEmpty()) {
			for (String nodeRefItem : nodeRefsParam.split(",")) {
				nodeRefs.add(new NodeRef(nodeRefItem));
			}
		}

		NodeRef projectNodeRef = null;

		if (!nodeRefs.isEmpty()) {
			projectNodeRef = createProject(nodeRefs, projectTemplateNodeRef);

		} else {
			throw new IllegalStateException(I18NUtil.getMessage("message.incorrect.paramater"));
		}

		try {
			JSONObject ret = new JSONObject();

			if (projectNodeRef != null) {
				ret.put("persistedObject", projectNodeRef);
			}

			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			ret.write(res.getWriter());
		} catch (JSONException e) {
			throw new IllegalStateException("Unable to serialize JSON", e);
		}

	}

	public NodeRef createProject(List<NodeRef> entities, NodeRef projectTemplateNodeRef) {

		NodeRef destNodeRef = associationService.getTargetAssoc(projectTemplateNodeRef, BeCPGModel.PROP_ENTITY_TPL_DEFAULT_DEST);
		if (destNodeRef == null) {
			throw new IllegalStateException(I18NUtil.getMessage("message.project-template.destination.missed"));
		}

		ProjectData projectData = new ProjectData();
		projectData.setName(repoService.getAvailableName(destNodeRef, "associate-project", false));
		projectData.setParentNodeRef(destNodeRef);
		projectData.setState(ProjectState.InProgress.toString());
		projectData.setEntities(entities);
		projectData.setProjectTpl(projectTemplateNodeRef);

		return alfrescoRepository.save(projectData).getNodeRef();

	}



}
