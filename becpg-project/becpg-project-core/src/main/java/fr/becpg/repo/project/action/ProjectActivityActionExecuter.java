/*
Copyright (C) 2010-2014 beCPG. 
 
This file is part of beCPG 
 
beCPG is free software: you can redistribute it and/or modify 
it under the terms of the GNU Lesser General Public License as published by 
the Free Software Foundation, either version 3 of the License, or 
(at your option) any later version. 
 
beCPG is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 

MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
GNU Lesser General Public License for more details. 
 
You should have received a copy of the GNU Lesser General Public License 
along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.becpg.repo.project.action;

import java.util.List;

import org.alfresco.model.ForumModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.project.ProjectActivityService;

/**
 * @author matthieu
 * 
 */
public class ProjectActivityActionExecuter extends ActionExecuterAbstractBase {

	public static final String NAME = "project-activity";

	private Log logger = LogFactory.getLog(ProjectActivityActionExecuter.class);

	private ProjectActivityService projectActivityService;
	
	private NodeService nodeService;
	

	public void setProjectActivityService(ProjectActivityService projectActivityService) {
		this.projectActivityService = projectActivityService;
	}


	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}


	/**
	 * @see org.alfresco.repo.action.executer.ActionExecuter#execute(org.alfresco.repo.ref.NodeRef,
	 *      org.alfresco.repo.ref.NodeRef)
	 */
	@Override
	public void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef) {
		if (nodeService.exists(actionedUponNodeRef)) {
			if(ForumModel.TYPE_POST.equals(nodeService.getType(actionedUponNodeRef))){
				logger.debug("Action upon comment, post activity");
				projectActivityService.postCommentActivity(actionedUponNodeRef);
			}
		}
	}

	
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		//paramList.add(new ParameterDefinitionImpl(PARAM_VERSION_TYPE, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_VERSION_TYPE)));
		
	}

}
