/*
 * 
 */
package fr.becpg.repo.workflow.jbpm.npd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.jbpm.JBPMNode;
import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.graph.exe.ExecutionContext;
import org.springframework.beans.factory.BeanFactory;

import fr.becpg.common.RepoConsts;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.ProductData;

/**
 * Create the product based on NPD data
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 * 
 */
public class CreateProduct extends JBPMSpringActionHandler {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -5350531116399906801L;

	private static Log logger = LogFactory.getLog(CreateProduct.class);

	/** The node service. */
	private NodeService nodeService;

	/** The file folder service. */
	private FileFolderService fileFolderService;

	
	private ProductDAO productDAO;
	

	/** The product DAO */
	private EntityService entityService;

	private ProductService productService;

	private static final String CM_URL = NamespaceService.CONTENT_MODEL_1_0_URI;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler#initialiseHandler
	 * (org.springframework.beans.factory.BeanFactory)
	 */
	@Override
	protected void initialiseHandler(BeanFactory factory) {
		entityService = (EntityService) factory.getBean("entityService");
		nodeService = (NodeService) factory.getBean("nodeService");
		fileFolderService = (FileFolderService) factory.getBean("fileFolderService");
		productService = (ProductService) factory.getBean("productService");
		productDAO = (ProductDAO) factory.getBean("productDAO");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jbpm.graph.def.ActionHandler#execute(org.jbpm.graph.exe.ExecutionContext
	 * )
	 */
	@Override
	public void execute(final ExecutionContext executionContext) throws Exception {

		final NodeRef pkgNodeRef = ((JBPMNode) executionContext.getContextInstance().getVariable("bpm_package"))
				.getNodeRef();

		RunAsWork<Object> actionRunAs = new RunAsWork<Object>() {
			@Override
			public Object doWork() throws Exception {
				try {

					QName entityType = BeCPGModel.TYPE_FINISHEDPRODUCT;
					JBPMNode destinationNode = (JBPMNode) executionContext.getContextInstance().getVariable(
							"npdwf_npdFolder");
					NodeRef parentNodeRef = destinationNode.getNodeRef();
					NodeRef sourceNodeRef = null;
					NodeRef recipeNodeRef = null;
					NodeRef packagingNodeRef = null;

					String entityName = (String) executionContext.getContextInstance().getVariable(
							"npdwf_npdProductName");
					JBPMNode productNode = (JBPMNode) executionContext.getContextInstance().getVariable(
							"npdwf_needDefinitionProduct");
					if (productNode != null) {
						logger.debug("Product node exist");
						sourceNodeRef = productNode.getNodeRef();
					}

					JBPMNode recipeNode = (JBPMNode) executionContext.getContextInstance().getVariable(
							"npdwf_needDefinitionRecipeProduct");
					if (recipeNode != null) {
						logger.debug("Recipe node exist");
						recipeNodeRef = recipeNode.getNodeRef();
					} 

					JBPMNode packagingNode = (JBPMNode) executionContext.getContextInstance().getVariable(
							"npdwf_needDefinitionPackagingKit");
					if (packagingNode != null) {
						logger.debug("Packaging node exist");
						packagingNodeRef = packagingNode.getNodeRef();
					}

					NodeRef productNodeRef = entityService.createOrCopyFrom(parentNodeRef, sourceNodeRef, entityType,
							entityName);

					// profitability
					Float unitPrice = (Float) executionContext.getContextInstance().getVariable(
							"npdwf_unitPrice");
					String priceCurrency= (String) executionContext.getContextInstance().getVariable(
							"npdwf_priceCurrency");
					if(unitPrice!=null ){
					
						Map<QName, Serializable> props = new HashMap<QName, Serializable>();
						props.put(BeCPGModel.PROP_UNIT_PRICE, unitPrice);
						props.put(BeCPGModel.PROP_PRICE_CURRENCY, priceCurrency);
						nodeService.addAspect(productNodeRef, BeCPGModel.ASPECT_PROFITABILITY, props );
					}
					
					// npd aspect
					String npdNumber = (String) executionContext.getContextInstance().getVariable(
							"npdwf_npdNumber");
					String npdType = (String) executionContext.getContextInstance().getVariable(
							"npdwf_npdType");
					String npdStatus = (String) executionContext.getContextInstance().getVariable(
							"npdwf_npdStatus");
					JBPMNode npdInitiator = (JBPMNode) executionContext.getContextInstance().getVariable(
							"initiator");					
					Map<QName, Serializable> props = new HashMap<QName, Serializable>();
					props.put(NPDModel.PROP_NPD_NUMBER, npdNumber);
					props.put(NPDModel.PROP_NPD_TYPE, npdType);
					props.put(NPDModel.PROP_NPD_STATUS, npdStatus);
					props.put(NPDModel.PROP_NPD_INITIATOR, npdInitiator.getName());
					nodeService.addAspect(productNodeRef, NPDModel.ASPECT_NPD, props );

					//Delete existing subsidiary
					for( AssociationRef subsidiaryAssoc : nodeService.getTargetAssocs(productNodeRef, BeCPGModel.ASSOC_SUBSIDIARY)){
						nodeService.removeAssociation( productNodeRef, subsidiaryAssoc.getTargetRef(), BeCPGModel.ASSOC_SUBSIDIARY);
					}
					
					
					//Copy subsidiary 
					JBPMNode subsidiaryNode = (JBPMNode) executionContext.getContextInstance().getVariable(
							"npdwf_cftSubsidiary");
					if(subsidiaryNode!=null && subsidiaryNode.getNodeRef()!=null && nodeService.exists( subsidiaryNode.getNodeRef())){
						
						nodeService.createAssociation( productNodeRef, subsidiaryNode.getNodeRef() , BeCPGModel.ASSOC_SUBSIDIARY);
					}
					
					//Delete existing clients
					for( AssociationRef clientAssoc : nodeService.getTargetAssocs(productNodeRef, BeCPGModel.ASSOC_CLIENTS)){
						nodeService.removeAssociation( productNodeRef, clientAssoc.getTargetRef(), BeCPGModel.ASSOC_CLIENTS);
					}
										
					//Copy client 
					JBPMNode clientNode = (JBPMNode) executionContext.getContextInstance().getVariable(
							"npdwf_cftClient");
					if(clientNode!=null && clientNode.getNodeRef()!=null && nodeService.exists( clientNode.getNodeRef())){
						
						nodeService.createAssociation( productNodeRef, clientNode.getNodeRef() , BeCPGModel.ASSOC_CLIENTS);
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

					// Move file from pkgNodeRef
					List<FileInfo> files = fileFolderService.listFiles(pkgNodeRef);
					NodeRef briefNodeRef = getBriefNodeRef(productNodeRef);
					for (FileInfo file : files) {
						String name = (String) nodeService.getProperty(file.getNodeRef(), ContentModel.PROP_NAME);
						if (briefNodeRef != null) {
							fileFolderService.move(file.getNodeRef(), briefNodeRef, name);
							nodeService.removeChild(pkgNodeRef, file.getNodeRef());
						} else {
							logger.error("No brief folder found");
							break;
						}
					}

					String name = (String) nodeService.getProperty(productNodeRef, ContentModel.PROP_NAME);
					if (name == null) {
						name = GUID.generate();
					}
					String localName = QName.createValidLocalName(name);
					QName qName = QName.createQName(CM_URL, localName);

					nodeService.addChild(pkgNodeRef, productNodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS, qName);

				} catch (Exception e) {
					logger.error("Failed to create product", e);
					throw e;
				}

				return null;
			}

			private NodeRef getBriefNodeRef(NodeRef productNodeRef) {
				
				NodeRef parentEntityNodeRef = nodeService.getPrimaryParent(productNodeRef).getParentRef();
				
				for (FileInfo file : fileFolderService.listFolders(parentEntityNodeRef)) {
					if (file.getName().equals(TranslateHelper.getTranslatedPath(RepoConsts.PATH_BRIEF))) {
						return file.getNodeRef();
					}
				}

				return null;
			}

		};
		AuthenticationUtil.runAs(actionRunAs, AuthenticationUtil.getAdminUserName());

	}

	private void copyDataList(NodeRef productNodeRef, NodeRef sourceNodeRef, QName typeCompolist) {
	
	    Collection<QName> dataLists = new ArrayList<QName>();
		 dataLists.add(typeCompolist);
		 ProductData productData  = productDAO.find(productNodeRef, dataLists);
		 ProductData sourceData = productDAO.find(sourceNodeRef, dataLists);
		 if (typeCompolist.equals(BeCPGModel.TYPE_PACKAGINGLIST)) {
 			productData.setPackagingList(sourceData.getPackagingList());
 		}
		 else if (typeCompolist.equals(BeCPGModel.TYPE_COMPOLIST)) {
 			productData.setCompoList(sourceData.getCompoList());
 		}
		 
		 productDAO.update(productNodeRef, productData, dataLists);

	}

}
