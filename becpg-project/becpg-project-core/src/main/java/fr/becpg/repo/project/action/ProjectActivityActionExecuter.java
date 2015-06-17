/*
Copyright (C) 2010-2015 beCPG. 
 
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

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.project.ProjectActivityService;
import fr.becpg.repo.project.data.projectList.ActivityEvent;

/**
 * @author matthieu
 * 
 */
public class ProjectActivityActionExecuter extends ActionExecuterAbstractBase {

	public static final String NAME = "project-activity";
	
	public static final String PARAM_ACTIVITY_EVENT = "activityEvent";

	private final Log logger = LogFactory.getLog(ProjectActivityActionExecuter.class);

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
		if (nodeService.exists(actionedUponNodeRef) && !nodeService.hasAspect(actionedUponNodeRef, ContentModel.ASPECT_WORKING_COPY)
				&& !nodeService.hasAspect(actionedUponNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)) {
			QName type = nodeService.getType(actionedUponNodeRef);
			String activityEvent = (String) ruleAction.getParameterValue(PARAM_ACTIVITY_EVENT);
			if(activityEvent!=null){
				if(ForumModel.TYPE_POST.equals(type)){
					logger.debug("Action upon comment, post activity");
					projectActivityService.postCommentActivity(actionedUponNodeRef, ActivityEvent.valueOf(activityEvent));
				} else if(ContentModel.TYPE_CONTENT.equals(type)){
					logger.debug("Action upon content, post activity");
					projectActivityService.postContentActivity(actionedUponNodeRef, ActivityEvent.valueOf(activityEvent));
				}
			}
			
		}
	}

	
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		paramList.add(new ParameterDefinitionImpl(PARAM_ACTIVITY_EVENT, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_ACTIVITY_EVENT)));
		
	}

}
