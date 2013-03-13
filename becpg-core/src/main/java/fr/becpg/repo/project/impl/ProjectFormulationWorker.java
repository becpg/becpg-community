package fr.becpg.repo.project.impl;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.search.BeCPGSearchService;

public class ProjectFormulationWorker {

	private static Log logger = LogFactory.getLog(ProjectFormulationWorker.class);
	
	private BeCPGSearchService beCPGSearchService;
	private ProjectService projectService;
	private BehaviourFilter policyBehaviourFilter;
	private TransactionService transactionService;
	
	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}
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
        		
        		// query
        		String query = LuceneHelper.mandatory(LuceneHelper.getCondType(ProjectModel.TYPE_PROJECT)) +
        				LuceneHelper.getCondEqualValue(ProjectModel.PROP_PROJECT_STATE, ProjectState.InProgress.toString(), LuceneHelper.Operator.AND);

        		List<NodeRef> projectNodeRefs = beCPGSearchService.luceneSearch(query);
        		
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
