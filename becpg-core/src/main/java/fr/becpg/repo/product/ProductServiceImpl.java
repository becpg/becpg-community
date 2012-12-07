/*
 * 
 */
package fr.becpg.repo.product;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.MPMModel;
import fr.becpg.model.SystemProductType;
import fr.becpg.model.SystemState;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.formulation.PhysicoChemCalculatingVisitor;
import fr.becpg.repo.product.hierarchy.HierarchyHelper;
import fr.becpg.repo.repository.AlfrescoRepository;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductServiceImpl.
 *
 * @author querephi
 */
@Service
public class ProductServiceImpl implements ProductService {

	private static Log logger = LogFactory.getLog(ProductServiceImpl.class);
	
	/** The node service. */
	private NodeService nodeService;	
	
	private AlfrescoRepository<ProductData> alfrescoRepository;
	
	/** The product dictionary service. */
	private ProductDictionaryService productDictionaryService;
	
	
	private ProductVisitor compositionCalculatingVisitor;
	
	private ProductVisitor processCalculatingVisitor;
	
	/** The allergens calculating visitor. */
	private ProductVisitor allergensCalculatingVisitor;
	
	/** The nuts calculating visitor. */
	private ProductVisitor nutsCalculatingVisitor;
	
	/** The costs calculating visitor. */
	private ProductVisitor costsCalculatingVisitor;
	
	/** The ings calculating visitor. */
	private ProductVisitor ingsCalculatingVisitor;
	
	/**  The formula visitor */
	private ProductVisitor formulaVisitor;
	
	private PhysicoChemCalculatingVisitor physicoChemCalculatingVisitor;
	
	/** The repo service. */
	private RepoService repoService;
	
	/** The ownable service. */
	private OwnableService ownableService;

	private EntityListDAO entityListDAO;
	
	private CharactDetailsVisitorFactory charactDetailsVisitorFactory;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}	
	
	
	
	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}



	public void setProductDictionaryService(ProductDictionaryService productDictionaryService) {
		this.productDictionaryService = productDictionaryService;
	}
	
	public void setCompositionCalculatingVisitor(
			ProductVisitor compositionCalculatingVisitor) {
		this.compositionCalculatingVisitor = compositionCalculatingVisitor;
	}

	public void setProcessCalculatingVisitor(ProductVisitor processCalculatingVisitor) {
		this.processCalculatingVisitor = processCalculatingVisitor;
	}

	public void setCharactDetailsVisitorFactory(CharactDetailsVisitorFactory charactDetailsVisitorFactory) {
		this.charactDetailsVisitorFactory = charactDetailsVisitorFactory;
	}

	/**
	 * Sets the allergens calculating visitor.
	 *
	 * @param allergensCalculatingVisitor the new allergens calculating visitor
	 */
	public void setAllergensCalculatingVisitor(ProductVisitor allergensCalculatingVisitor){
    	this.allergensCalculatingVisitor = allergensCalculatingVisitor;
    }
	
	/**
	 * Sets the nuts calculating visitor.
	 *
	 * @param nutsCalculatingVisitor the new nuts calculating visitor
	 */
	public void setNutsCalculatingVisitor(ProductVisitor nutsCalculatingVisitor){
    	this.nutsCalculatingVisitor = nutsCalculatingVisitor;
    }
	
	/**
	 * Sets the costs calculating visitor.
	 *
	 * @param costsCalculatingVisitor the new costs calculating visitor
	 */
	public void setCostsCalculatingVisitor(ProductVisitor costsCalculatingVisitor){
    	this.costsCalculatingVisitor = costsCalculatingVisitor;
    }
	
	/**
	 * Sets the ings calculating visitor.
	 *
	 * @param ingsCalculatingVisitor the new ings calculating visitor
	 */
	public void setIngsCalculatingVisitor(ProductVisitor ingsCalculatingVisitor){
    	this.ingsCalculatingVisitor = ingsCalculatingVisitor;
    }	
	
	/**
	 * Sets the formula calculating visitor.
	 * @param formulaVisitor
	 */
	public void setFormulaVisitor(ProductVisitor formulaVisitor) {
		this.formulaVisitor = formulaVisitor;
	}
	
	public void setPhysicoChemCalculatingVisitor(PhysicoChemCalculatingVisitor physicoChemCalculatingVisitor) {
		this.physicoChemCalculatingVisitor = physicoChemCalculatingVisitor;
	}

	/**
	 * Sets the repo service.
	 *
	 * @param repoService the new repo service
	 */
	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}
	
	/**
	 * Sets the ownable service.
	 *
	 * @param ownableService the new ownable service
	 */
	public void setOwnableService(OwnableService ownableService) {
		this.ownableService = ownableService;
	}	

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}
	

	/**
	 * Formulate the product (update DB)
	 *
	 * @param productNodeRef the product node ref
	 */
    @Override
    public void formulate(NodeRef productNodeRef) throws FormulateException {
    	try {
    		//Load product 
        
    		
        	ProductData productData = alfrescoRepository.findOne(productNodeRef); 
        	        	
        	// do the formulation if the product has a composition, or packaging list defined
        	if((productData.getCompoList() != null && productData.getCompoList().size() != 0) || 
        			(productData.getPackagingList() != null && productData.getPackagingList().size() != 0) || 
        			(productData.getProcessList() != null && productData.getProcessList().size() != 0)){
        	
        		productData = formulate(productData);

				
				alfrescoRepository.save( productData);
        	}    	    	    
    	} catch (Exception e) {
			if(e instanceof FormulateException){
				throw (FormulateException)e;
			} 
			throw new FormulateException("message.formulate.failure",e);
			
		}
    }       
    
    /**
	 * Formulate the product (don't update DB)
	 *
	 * @param productNodeRef the product node ref
	 */
    @Override
    public ProductData formulate(ProductData productData) throws FormulateException {
    	try {  	    
        	// do the formulation if the product has a composition, or packaging list or process list defined
    		if((productData.getCompoList() != null && productData.getCompoList().size() != 0) || 
        			(productData.getPackagingList() != null && productData.getPackagingList().size() != 0) || 
        			(productData.getProcessList() != null && productData.getProcessList().size() != 0)){
        		
        		productData.setReqCtrlList(new ArrayList<ReqCtrlListDataItem>());
        
        		
        		//Call visitors         		
        		productData = compositionCalculatingVisitor.visit(productData);
        		productData = processCalculatingVisitor.visit(productData);
    	    	productData = allergensCalculatingVisitor.visit(productData);
    	    	productData = nutsCalculatingVisitor.visit(productData);
    	    	productData = costsCalculatingVisitor.visit(productData);
    	    	productData = ingsCalculatingVisitor.visit(productData);  
    	    	productData = physicoChemCalculatingVisitor.visit(productData);
    	    	productData = formulaVisitor.visit(productData);
    	    	
    	    	if(productData.getReqCtrlList().isEmpty()){
    	    		productData.setReqCtrlList(null);
    	    	}
        	}    	    	    
    	} catch (Exception e) {
			if(e instanceof FormulateException){
				throw (FormulateException)e;
			} 
			throw new FormulateException("message.formulate.failure",e);
			
		}
    	
    	return productData;
    }    
    

  
    
    /**
     * Move the product in a folder according to the hierarchy.
     *
     * @param containerNodeRef : companyHome, or documentLibrary of site
     * @param productNodeRef : product
     */
    @Override
    public void classifyProduct(NodeRef containerNodeRef, NodeRef productNodeRef){
    	
    	NodeRef destinationNodeRef = null;
    	ProductData productData = alfrescoRepository.findOne(productNodeRef);
    	
    	// products
		NodeRef productsNodeRef = repoService.createFolderByPath(containerNodeRef, RepoConsts.PATH_PRODUCTS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_PRODUCTS));
		
		// state
		SystemState systemState = productData.getState();
		String stateFolderName = productDictionaryService.getFolderName(systemState);
		NodeRef stateNodeRef = repoService.createFolderByPath(productsNodeRef, systemState.toString(), stateFolderName);
		
		// product type		
		SystemProductType systemProductType = SystemProductType.valueOf(nodeService.getType(productNodeRef));
		String productTypeFolderName = productDictionaryService.getFolderName(systemProductType);
		NodeRef productTypeNodeRef = repoService.createFolderByPath(stateNodeRef, systemProductType.toString(), productTypeFolderName);
		
		// hierarchy 1
		if(productData.getHierarchy1() != null){
			
			String name = HierarchyHelper.getHierachyName(productData.getHierarchy1(), nodeService);
			if(name!=null){
				NodeRef hierarchy1NodeRef = repoService.createFolderByPath(productTypeNodeRef,name, name);

				// hierarchy 2				
				if(productData.getHierarchy2() != null){
					name = HierarchyHelper.getHierachyName(productData.getHierarchy2(), nodeService);
					if(name!=null){
						destinationNodeRef = repoService.createFolderByPath(hierarchy1NodeRef,name, name);
					}
					else{
						logger.debug("Cannot create folder for productHierarchy2 since hierarchyName is null. productHierarchy2: " + productData.getHierarchy2());
					}					
	    		}
				else{
					logger.debug("Cannot classify product since it doesn't have a productHierarchy2.");
				}
			}
			else{
				logger.debug("Cannot create folder for productHierarchy1 since hierarchyName is null. productHierarchy1: " + productData.getHierarchy1());
			}
		}
		else{
			logger.debug("Cannot classify product since it doesn't have a productHierarchy1.");
		}
		
		if(destinationNodeRef != null){
			
			//Product has a product folder ? yes, we move the product folder - no, we move the product
			NodeRef nodeRefToMove = productNodeRef;
			NodeRef parentProductNodeRef = nodeService.getPrimaryParent(productNodeRef).getParentRef();
			QName parentProductType = nodeService.getType(parentProductNodeRef);
			
			if(parentProductType.equals(BeCPGModel.TYPE_ENTITY_FOLDER)){
				nodeRefToMove = parentProductNodeRef;							
			}				    	
			
			// classify product
			repoService.moveNode(nodeRefToMove, destinationNodeRef);
			
			// productNodeRef : remove all owner related rights 
            ownableService.setOwner(productNodeRef, OwnableService.NO_OWNER);    			
		}
		else{
			logger.debug("Failed to classify product. productNodeRef: " + productNodeRef);
		}
    }

	@Override
	public CharactDetails formulateDetails(NodeRef productNodeRef, QName datatType, String dataListName, List<NodeRef> elements) throws FormulateException {

		Collection<QName> dataLists = new ArrayList<QName>();				
		dataLists.add(BeCPGModel.TYPE_COMPOLIST);
		dataLists.add(BeCPGModel.TYPE_PACKAGINGLIST);
		dataLists.add(MPMModel.TYPE_PROCESSLIST);
    	ProductData productData = alfrescoRepository.findOne(productNodeRef);
    	        	
    	CharactDetailsVisitor visitor  = charactDetailsVisitorFactory.getCharactDetailsVisitor(datatType, dataListName);		
		return visitor.visit(productData, elements);		
	}
}
