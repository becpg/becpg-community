/*
 * 
 */
package fr.becpg.repo.product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.MPMModel;
import fr.becpg.model.SystemProductType;
import fr.becpg.model.SystemState;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.wused.WUsedListService;
import fr.becpg.repo.entity.wused.data.WUsedData;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListUnit;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.formulation.FormulateException;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductServiceImpl.
 *
 * @author querephi
 */
public class ProductServiceImpl implements ProductService {

	
	private static final int WUSED_LEVEL = 1;	

	/** The logger. */
	private static Log logger = LogFactory.getLog(ProductServiceImpl.class);	
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The file folder service. */
	private FileFolderService fileFolderService;	
	
	/** The product dao. */
	private ProductDAO productDAO;
	
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
	
	/** The repo service. */
	private RepoService repoService;
	
	
	/** The ownable service. */
	private OwnableService ownableService;
	
	private WUsedListService wUsedListService;
	
	/**
	 * Sets the node service.
	 *
	 * @param nodeService the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	/**
	 * Sets the file folder service.
	 *
	 * @param fileFolderService the new file folder service
	 */
	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}		
	
	/**
	 * Sets the product dao.
	 *
	 * @param productDAO the new product dao
	 */
	public void setProductDAO(ProductDAO productDAO){
		this.productDAO = productDAO;
	}
	
	/**
	 * Sets the product dictionary service.
	 *
	 * @param productDictionaryService the new product dictionary service
	 */
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
	
	

	public void setwUsedListService(WUsedListService wUsedListService) {
		this.wUsedListService = wUsedListService;
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
        	Collection<QName> dataLists = new ArrayList<QName>();				
    		dataLists.add(BeCPGModel.TYPE_COMPOLIST);
    		dataLists.add(BeCPGModel.TYPE_PACKAGINGLIST);
    		dataLists.add(MPMModel.TYPE_PROCESSLIST);
    		dataLists.add(BeCPGModel.TYPE_NUTLIST); // TODO keep min/max
    		dataLists.add(BeCPGModel.TYPE_COSTLIST); // TODO keep max
        	ProductData productData = productDAO.find(productNodeRef, dataLists);     	    
        	
        	// do the formulation if the product has a composition, or packaging list defined
        	if(productData.getCompoList() != null || productData.getPackagingList() != null || productData.getProcessList() != null){
        		
        		productData = formulate(productData);
    	    	    	
    	    	dataLists.add(BeCPGModel.TYPE_ALLERGENLIST);
    	    	dataLists.add(BeCPGModel.TYPE_COSTDETAILSLIST);
    	    	dataLists.add(BeCPGModel.TYPE_INGLIST);
    	    	dataLists.add(BeCPGModel.TYPE_INGLABELINGLIST);
    	    	dataLists.add(BeCPGModel.TYPE_REQCTRLLIST);
    	    	
    	    	productDAO.update(productNodeRef, productData, dataLists);
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
        	if(productData.getCompoList() != null || productData.getPackagingList() != null || productData.getProcessList() != null){
        		
        		productData.setReqCtrlList(new ArrayList<ReqCtrlListDataItem>());
        		
        		//Call visitors         		
        		productData = compositionCalculatingVisitor.visit(productData);
        		productData = processCalculatingVisitor.visit(productData);
    	    	productData = allergensCalculatingVisitor.visit(productData);
    	    	productData = nutsCalculatingVisitor.visit(productData);
    	    	productData = costsCalculatingVisitor.visit(productData);
    	    	productData = ingsCalculatingVisitor.visit(productData);  
    	    	
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
    	
    	NodeRef destionationNodeRef = null;
    	ProductData productData = productDAO.find(productNodeRef, null);
    	
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
			NodeRef hierarchy1NodeRef = repoService.createFolderByPath(productTypeNodeRef, productData.getHierarchy1(), productData.getHierarchy1());
		
			// hierarchy 2
			if(productData.getHierarchy2() != null){
				destionationNodeRef = repoService.createFolderByPath(hierarchy1NodeRef, productData.getHierarchy2(), productData.getHierarchy2());
    		}
		}		
		
		if(destionationNodeRef != null){
			
			//Product has a product folder ? yes, we move the product folder - no, we move the product
			NodeRef nodeRefToMove = productNodeRef;
			NodeRef parentProductNodeRef = nodeService.getPrimaryParent(productNodeRef).getParentRef();
			QName parentProductType = nodeService.getType(parentProductNodeRef);
			
			if(parentProductType.equals(BeCPGModel.TYPE_ENTITY_FOLDER)){
				nodeRefToMove = parentProductNodeRef;							
			}				    	
			
			// check the product is not already classified !
			NodeRef parentOfNodeRefToMove = nodeService.getPrimaryParent(nodeRefToMove).getParentRef();
			if(destionationNodeRef.equals(parentOfNodeRefToMove)){
				// nothing to do...
				logger.debug("product already classified, nothing to do...");
				return;
			}
			
			//Check there is not a node with the same name, then rename node
			String name = (String)nodeService.getProperty(nodeRefToMove, ContentModel.PROP_NAME);
			List<FileInfo> fileInfos = fileFolderService.list(destionationNodeRef);
			if(fileInfos.size() > 0){
				boolean fileAlreadyExists = false;
				int count = 0;
				for(FileInfo fileInfo : fileInfos){						
					if(fileInfo.getName().toLowerCase().equals(name.toLowerCase())){
						fileAlreadyExists = true;							
					}
				}
				
				while(fileAlreadyExists){
					count++;
					String nameWithCounter = String.format("%s (%d)", name, count);
					fileAlreadyExists = false;
					for(FileInfo fileInfo : fileInfos){						
						if(fileInfo.getName().toLowerCase().equals(nameWithCounter.toLowerCase())){
							fileAlreadyExists = true;							
						}
					}
				}
				
				if(count > 0){
					name = String.format("%s (%d)", name, count);
					//nodeService.setProperty(nodeRefToMove, ContentModel.PROP_NAME, name);
				}
			}
			
			if(logger.isDebugEnabled()){
				logger.debug(String.format("Classify product '%s' in folder '%s'", name, destionationNodeRef));
			}			

			try{
				nodeRefToMove = fileFolderService.move(nodeRefToMove, destionationNodeRef, name).getNodeRef();
			}
			catch(Exception e){
				logger.error("classifyProduct : Failed to move product", e);
			}
			
			// productNodeRef : remove all owner related rights 
            ownableService.setOwner(productNodeRef, OwnableService.NO_OWNER);    			
		}
    }
    
    
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.product.ProductService#getWUsedProduct(org.alfresco.service.cmr.repository.NodeRef)
	 */
	@Override
	public List<CompoListDataItem> getWUsedCompoList(NodeRef productNodeRef) {
		
		logger.debug("getWUsedProduct");
		
		List<CompoListDataItem> wUsedList = new ArrayList<CompoListDataItem>();		
		WUsedData wUsedData = wUsedListService.getWUsedEntity(productNodeRef, BeCPGModel.ASSOC_COMPOLIST_PRODUCT, WUSED_LEVEL);
		
		for(Map.Entry<NodeRef, WUsedData> kv : wUsedData.getRootList().entrySet()){
			
			Map<QName, Serializable> properties = nodeService.getProperties(kv.getKey());

			CompoListUnit compoListUnit = CompoListUnit.valueOf((String)properties.get(BeCPGModel.PROP_COMPOLIST_UNIT));
			
			CompoListDataItem compoListDataItem = new CompoListDataItem(kv.getKey(), WUSED_LEVEL, 
										(Float)properties.get(BeCPGModel.PROP_COMPOLIST_QTY), 
										(Float)properties.get(BeCPGModel.PROP_COMPOLIST_QTY_SUB_FORMULA), 
										(Float)properties.get(BeCPGModel.PROP_COMPOLIST_QTY_AFTER_PROCESS), 
										compoListUnit, 
										(Float)properties.get(BeCPGModel.PROP_COMPOLIST_LOSS_PERC), 
										(String)properties.get(BeCPGModel.PROP_COMPOLIST_DECL_GRP), 
										(String)properties.get(BeCPGModel.PROP_COMPOLIST_DECL_TYPE), 
										kv.getValue().getEntityNodeRef());
			
			wUsedList.add(compoListDataItem);
		}
		
		logger.debug("wUsedList size" + wUsedList.size());
		
		return wUsedList;
	}
	
	@Override
	public List<PackagingListDataItem> getWUsedPackagingList(NodeRef productNodeRef) {
		
		logger.debug("getWUsedProduct");
		
		List<PackagingListDataItem> wUsedList = new ArrayList<PackagingListDataItem>();
		WUsedData wUsedData = wUsedListService.getWUsedEntity(productNodeRef, BeCPGModel.ASSOC_PACKAGINGLIST_PRODUCT, WUSED_LEVEL);
		
		for(Map.Entry<NodeRef, WUsedData> kv : wUsedData.getRootList().entrySet()){
			
			Map<QName, Serializable> properties = nodeService.getProperties(kv.getKey());			
			PackagingListUnit packagingListUnit = PackagingListUnit.valueOf((String)properties.get(BeCPGModel.PROP_PACKAGINGLIST_UNIT));							
			
			PackagingListDataItem packagingListDataItem = new PackagingListDataItem(kv.getKey(), 									
						(Float)properties.get(BeCPGModel.PROP_PACKAGINGLIST_QTY), 
						packagingListUnit, 
						(String)properties.get(BeCPGModel.PROP_PACKAGINGLIST_PKG_LEVEL), 
						kv.getValue().getEntityNodeRef());
			
			wUsedList.add(packagingListDataItem);
		}
		
		logger.debug("wUsedList size" + wUsedList.size());
		
		return wUsedList;
	}

}
