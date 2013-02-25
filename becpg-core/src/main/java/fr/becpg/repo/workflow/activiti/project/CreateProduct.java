/*
 * 
 */
package fr.becpg.repo.workflow.activiti.project;

import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.repo.workflow.activiti.BaseJavaDelegate;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.star.lang.NullPointerException;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.filters.EffectiveFilters;

/**
 * Create the product based on data
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 * 
 */
public class CreateProduct extends BaseJavaDelegate {


	private final static Log logger = LogFactory.getLog(CreateProduct.class);

	/** The node service. */
	private NodeService nodeService;

	protected AlfrescoRepository<ProductData> alfrescoRepository;

	/** The product DAO */
	private EntityService entityService;

	private ProductService productService;
	
	private BehaviourFilter policyBehaviourFilter;

	private RepoService repoService;


	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}

	@Override
	public void execute(final DelegateExecution task) throws Exception {

		 final NodeRef pkgNodeRef = ((ActivitiScriptNode) task.getVariable("bpm_package")).getNodeRef();

		RunAsWork<Object> actionRunAs = new RunAsWork<Object>() {
			@Override
			public Object doWork() throws Exception {
				try {
				
					NodeRef projectNodeRef = null;
					
					List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(pkgNodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS, RegexQNamePattern.MATCH_ALL);
					for (ChildAssociationRef childAssoc : childAssocs) {
						if ( nodeService.getType(childAssoc.getChildRef()).equals(ProjectModel.TYPE_PROJECT)) {
							projectNodeRef = childAssoc.getChildRef();
							break;
						}
					}
					
					if(projectNodeRef==null){
						throw new NullPointerException("Cannot find project into package");
					}
					
					NodeRef sourceNodeRef = null;
					NodeRef recipeNodeRef = null;
					NodeRef packagingNodeRef = null;

					String entityName = (String) task.getVariable(
							"pjtwf_npdProductName");
					ActivitiScriptNode productNode = (ActivitiScriptNode) task.getVariable(
							"pjtwf_needDefinitionProduct");
					if (productNode != null) {
						logger.debug("Product node exist");
						sourceNodeRef = productNode.getNodeRef();
					}

					ActivitiScriptNode recipeNode = (ActivitiScriptNode) task.getVariable(
							"pjtwf_needDefinitionRecipeProduct");
					if (recipeNode != null) {
						logger.debug("Recipe node exist");
						recipeNodeRef = recipeNode.getNodeRef();
					} 

					ActivitiScriptNode packagingNode = (ActivitiScriptNode) task.getVariable(
							"pjtwf_needDefinitionPackagingKit");
					if (packagingNode != null) {
						logger.debug("Packaging node exist");
						packagingNodeRef = packagingNode.getNodeRef();
					}

					NodeRef productNodeRef = null;
					
					try{
						//disable classify
						policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_PRODUCT);
						
						
						productNodeRef = entityService.createOrCopyFrom(sourceNodeRef, projectNodeRef, nodeService.getType(sourceNodeRef),
								repoService.getAvailableName(projectNodeRef, entityName));
						
						// change state: ToValidate
						nodeService.setProperty(productNodeRef, BeCPGModel.PROP_PRODUCT_STATE, "ToValidate");
					}
					finally{
						policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_PRODUCT);
					}					

	
					// Copy datalist
					if (recipeNodeRef != null && nodeService.exists(recipeNodeRef)) {
						logger.debug("Copy composition dataList");
						copyDataList(productNodeRef, recipeNodeRef, BeCPGModel.TYPE_COMPOLIST);
					} else {
						logger.debug("No recipe node found");
					}

					if (packagingNodeRef != null && nodeService.exists(packagingNodeRef)) {
						logger.debug("Copy packaging dataList");
						copyDataList(productNodeRef, packagingNodeRef, BeCPGModel.TYPE_PACKAGINGLIST);
					} else {
						logger.debug("No packaging node found");
					}

					
					logger.debug("Formulate product");
					
					try {

						productService.formulate(productNodeRef);
					} catch (Exception e) {
						logger.debug(e,e); // newly created cannot be formulate
					}

					//Assoc product to project
					nodeService.createAssociation(projectNodeRef, productNodeRef, ProjectModel.ASSOC_PROJECT_ENTITY);
					
					NodeRef projectTask = ((ActivitiScriptNode) task.getVariable("pjt_workflowTask")).getNodeRef();
					nodeService.setProperty(projectTask, ProjectModel.PROP_TL_STATE, "Completed");
					
					
					
				} catch (Exception e) {
					logger.error("Failed to create product", e);
					throw e;
				}

				return null;
			}


		};
		AuthenticationUtil.runAs(actionRunAs, AuthenticationUtil.getAdminUserName());

	}

	private void copyDataList(NodeRef productNodeRef, NodeRef sourceNodeRef, QName typeCompolist) {
	
		 ProductData productData  = alfrescoRepository.findOne(productNodeRef);
		 ProductData sourceData = alfrescoRepository.findOne(sourceNodeRef);
		 if (typeCompolist.equals(BeCPGModel.TYPE_PACKAGINGLIST)) {
 			productData.getPackagingListView().setPackagingList(sourceData.getPackagingList(EffectiveFilters.FUTUR));
 		 }
		 else if (typeCompolist.equals(BeCPGModel.TYPE_COMPOLIST)) {
 			productData.getCompoListView().setCompoList(sourceData.getCompoList(EffectiveFilters.FUTUR));
 		 }
		 alfrescoRepository.save(productData);

	}

}
