/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
package fr.becpg.repo.project.impl;

import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.ProjectWorkflowService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.DeliverableState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * Project service that manage project
 * 
 * @author quere
 * 
 */

@Service("projectService")
public class ProjectServiceImpl implements ProjectService {

	private static final String QUERY_TASK_LEGEND = "+TYPE:\"pjt:taskLegend\"";

	private static Log logger = LogFactory.getLog(ProjectServiceImpl.class);

	@Autowired
	private AlfrescoRepository<ProjectData> alfrescoRepository;	
	@Autowired
	private AssociationService associationService;
	@Autowired
	private NodeService nodeService;
	@Autowired
	private BeCPGSearchService beCPGSearchService;
	@Autowired	
	private RepoService repoService;
	@Autowired
	private SiteService siteService;
	@Autowired
	private FormulationService<ProjectData> formulationService;
	@Autowired
	private ProjectWorkflowService projectWorkflowService;
	

	@Override
	public void openDeliverable(NodeRef deliverableNodeRef) {

		logger.debug("open Deliverable " + deliverableNodeRef);
		NodeRef taskNodeRef = associationService.getTargetAssoc(deliverableNodeRef, ProjectModel.ASSOC_DL_TASK);
		if (taskNodeRef != null) {
			nodeService.setProperty(taskNodeRef, ProjectModel.PROP_TL_STATE, TaskState.InProgress.toString());
		} else {
			logger.warn("Task is not defined for the deliverable. nodeRef: " + deliverableNodeRef);
		}
	}

	@Override
	public void openTask(NodeRef taskNodeRef) {
		
		logger.debug("open Task " + taskNodeRef);
		List<AssociationRef> sourceAssocs = nodeService.getSourceAssocs(taskNodeRef, ProjectModel.ASSOC_DL_TASK);		
		for(AssociationRef sourceAssoc : sourceAssocs){
			nodeService.setProperty(sourceAssoc.getSourceRef(), ProjectModel.PROP_DL_STATE, DeliverableState.InProgress.toString());
		}
	}
	
	@Override
	public List<NodeRef> getTaskLegendList() {
		return beCPGSearchService.luceneSearch(QUERY_TASK_LEGEND, LuceneHelper.getSort(BeCPGModel.PROP_SORT, true));
	}

	@Override
	public NodeRef getProjectsContainer(String siteId) {
		if(siteId!=null && siteId.length()>0){
			return siteService.getContainer(siteId,SiteService.DOCUMENT_LIBRARY);
		}
		return repoService.getFolderByPath(RepoConsts.PATH_PROJECTS);
	}

	@Override
	public void cancel(NodeRef projectNodeRef) {

		logger.debug("cancel project: " + projectNodeRef + " exists ? " + nodeService.exists(projectNodeRef));		
		if(nodeService.exists(projectNodeRef)){
			ProjectData projectData = alfrescoRepository.findOne(projectNodeRef);
	         
			for (TaskListDataItem taskListDataItem : projectData.getTaskList()) {				
				if (projectWorkflowService.isWorkflowActive(taskListDataItem)){
					projectWorkflowService.cancelWorkflow(taskListDataItem);					
				}					
			}    
			
			alfrescoRepository.save(projectData);
		}		
	}

	@Override
	public void formulate(NodeRef projectNodeRef) throws  FormulateException {

		if (nodeService.getType(projectNodeRef).equals(ProjectModel.TYPE_PROJECT)) {			
			formulationService.formulate(projectNodeRef);			
		}
	}

	@Override
	public void deleteTask(NodeRef taskListNodeRef) {
		
		// update prevTasks assoc of next tasks		
		List<NodeRef> deleteTaskPrevTaskNodeRefs = associationService.getTargetAssocs(taskListNodeRef, ProjectModel.ASSOC_TL_PREV_TASKS);
		List<AssociationRef> nextTaskAssociationRefs = nodeService.getSourceAssocs(taskListNodeRef, ProjectModel.ASSOC_TL_PREV_TASKS);
		
		for(AssociationRef nextTaskAssociationRef : nextTaskAssociationRefs){
			
			List<NodeRef> nextTaskPrevTaskNodeRefs = associationService.getTargetAssocs(nextTaskAssociationRef.getSourceRef(), ProjectModel.ASSOC_TL_PREV_TASKS);
			if(nextTaskAssociationRefs.contains(taskListNodeRef)){
				nextTaskPrevTaskNodeRefs.remove(taskListNodeRef);
			}			
			
			for(NodeRef deleteTaskPrevTaskNodeRef : deleteTaskPrevTaskNodeRefs){
				nextTaskPrevTaskNodeRefs.add(deleteTaskPrevTaskNodeRef);
			}
			
			associationService.update(nextTaskAssociationRef.getSourceRef(), nextTaskAssociationRef.getTypeQName(), nextTaskPrevTaskNodeRefs);
		}
		
//		// delete dl (not the document associated to dl -> user must delete them)
//		List<AssociationRef> dlAssociationRefs = nodeService.getSourceAssocs(taskListNodeRef, ProjectModel.ASSOC_DL_TASK);
//		for(AssociationRef dlAssociationRef : dlAssociationRefs){
//			logger.debug("###delete assoc dlAssociationRef.getSourceRef() : " + dlAssociationRef.getSourceRef());
//			nodeService.deleteNode(dlAssociationRef.getSourceRef());
//		}			
	}

	@Override
	public void submitTask(NodeRef nodeRef) {
		
		Date startDate = (Date)nodeService.getProperty(nodeRef, ProjectModel.PROP_TL_START);
		Date endDate = ProjectHelper.removeTime(new Date());
		
		nodeService.setProperty(nodeRef, ProjectModel.PROP_TL_STATE, TaskState.Completed.toString());
		// we want to keep the planned duration to calculate overdue				
		nodeService.setProperty(nodeRef, ProjectModel.PROP_TL_END, endDate);
		//milestone duration is maximum 1 day or startDate is after endDate
		Boolean isMileStone = (Boolean)nodeService.getProperty(nodeRef, ProjectModel.PROP_TL_IS_MILESTONE);
		if((isMileStone != null && isMileStone.booleanValue()) || 
				(startDate == null || startDate.after(endDate))){
			nodeService.setProperty(nodeRef, ProjectModel.PROP_TL_START, endDate);
		}		
	}
	
}
