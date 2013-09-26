/*
 * 
 */
package fr.becpg.repo.workflow.activiti.product;

import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.repo.workflow.activiti.BaseJavaDelegate;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.product.ProductService;

/**
 * 
 * @author matthieu
 *
 */
public class ValidateProduct extends BaseJavaDelegate {

	private static Log logger = LogFactory.getLog(ValidateProduct.class);

	private ProductService productService;
	private NodeService nodeService;
	private Repository repositoryHelper;
	private DictionaryService dictionaryService;
	
	
	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setRepositoryHelper(Repository repositoryHelper) {
		this.repositoryHelper = repositoryHelper;
	}


	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}



	@Override
	public void execute(final DelegateExecution task) throws Exception {

		 final NodeRef pkgNodeRef = ((ActivitiScriptNode) task.getVariable("bpm_package")).getNodeRef();

		
	  logger.debug("start ApproveActionHandler");
		
		RunAsWork<Object> actionRunAs = new RunAsWork<Object>()
        {
            @Override
			public Object doWork() throws Exception
            {
            	try{
        			//change state and classify products
            		List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(pkgNodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS, RegexQNamePattern.MATCH_ALL);										
					for (ChildAssociationRef childAssoc : childAssocs) {
            			
						NodeRef nodeRef = childAssoc.getChildRef();
						QName nodeType = nodeService.getType(nodeRef);
						
						if(dictionaryService.isSubClass(nodeType, BeCPGModel.TYPE_PRODUCT)){
	            			nodeService.setProperty(nodeRef, BeCPGModel.PROP_PRODUCT_STATE, SystemState.Valid);            			
	            			productService.classifyProduct(repositoryHelper.getCompanyHome(), nodeRef);
						}            			
            		}
        		}
        		catch(Exception e){
        			logger.error("Failed to approve product", e);
        			throw e;
        		}
        		
        		return null;
            }
        };
        AuthenticationUtil.runAs(actionRunAs, AuthenticationUtil.getAdminUserName());
        
        logger.debug("end ApproveActionHandler");
	}
}
