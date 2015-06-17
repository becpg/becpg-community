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
package fr.becpg.repo.project.impl;

import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.search.BeCPGQueryBuilder;

public class ProjectFormulationWorker {

	private static final Log logger = LogFactory.getLog(ProjectFormulationWorker.class);
	
	private ProjectService projectService;
	private BehaviourFilter policyBehaviourFilter;
	private TransactionService transactionService;
	
	public void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}
	
	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}
	
	public void executeFormulation(){
		
		final RetryingTransactionHelper txHelper = transactionService.getRetryingTransactionHelper();
        txHelper.setMaxRetries(1);
        txHelper.doInTransaction(new RetryingTransactionCallback<Object>(){
        	
        	public Object execute() throws Throwable{
        		
        		
        		
        		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery()
        				.ofType(ProjectModel.TYPE_PROJECT)
        				.andPropEquals(ProjectModel.PROP_PROJECT_STATE, ProjectState.InProgress.toString());
        		
        		List<NodeRef> projectNodeRefs = queryBuilder.list();
        		
        		// query
        		 queryBuilder = BeCPGQueryBuilder.createQuery()
         				.ofType(ProjectModel.TYPE_PROJECT)
         				.andPropEquals(ProjectModel.PROP_PROJECT_STATE, ProjectState.Planned.toString())
         				.andBetween(ProjectModel.PROP_PROJECT_START_DATE, "MIN", ISO8601DateFormat.format(new Date()));
        		
        		 projectNodeRefs.addAll(queryBuilder.list());
        		 
        		try{
        			policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
        			
        			for(NodeRef projectNodeRef : projectNodeRefs){
        				try {
        					if(logger.isDebugEnabled()){
        						logger.debug("Formulate project " + projectNodeRef);
        					}
        					projectService.formulate(projectNodeRef);
        				} catch (FormulateException e) {
        					logger.error("Failed to formulate project " + projectNodeRef, e);
        				}
        			}
        		}
        		finally{
        			
        			policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
        		}
        		return null;
            }
        }, false, true);		
	}
}
