/*
 * 
 */
package fr.becpg.repo.workflow.jbpm.productvalidation;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.jbpm.JBPMNode;
import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.graph.exe.ExecutionContext;
import org.springframework.beans.factory.BeanFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.product.ProductService;

/**
 * The Class ApproveActionHandler.
 *
 * @author querephi
 */
public class ApproveActionHandler extends JBPMSpringActionHandler{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3925326230084907429L;

	private static Log logger = LogFactory.getLog(ApproveActionHandler.class);
	
	/** The product service. */
	private ProductService productService;
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The file folder service. */
	private FileFolderService fileFolderService;
	
	/** The repository helper. */
	private Repository repositoryHelper;
	
	private DictionaryService dictionaryService;
	
	/* (non-Javadoc)
	 * @see org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler#initialiseHandler(org.springframework.beans.factory.BeanFactory)
	 */
	@Override
	protected void initialiseHandler(BeanFactory factory) {
		
		productService = (ProductService) factory.getBean("productService");
		nodeService = (NodeService)factory.getBean("nodeService");
		fileFolderService = (FileFolderService)factory.getBean("fileFolderService");
		repositoryHelper = (Repository)factory.getBean("repositoryHelper");
		dictionaryService = (DictionaryService)factory.getBean("dictionaryService");
	}

	/* (non-Javadoc)
	 * @see org.jbpm.graph.def.ActionHandler#execute(org.jbpm.graph.exe.ExecutionContext)
	 */
	@Override
	public void execute(ExecutionContext executionContext) throws Exception {
		
		logger.debug("start ApproveActionHandler");
		
		final JBPMNode jBPMNode = (JBPMNode) executionContext.getContextInstance().getVariable("bpm_package");		
		final NodeRef pkgNodeRef = jBPMNode.getNodeRef();		
		
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
