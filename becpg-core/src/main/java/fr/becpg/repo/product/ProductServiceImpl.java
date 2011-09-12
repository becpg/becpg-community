/*
 * 
 */
package fr.becpg.repo.product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.common.RepoConsts;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.model.ReportModel;
import fr.becpg.model.SystemProductType;
import fr.becpg.model.SystemState;
import fr.becpg.repo.NodeVisitor;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListUnit;
import fr.becpg.repo.product.report.ProductReportService;
import fr.becpg.repo.report.entity.EntityReportService;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductServiceImpl.
 *
 * @author querephi
 */
public class ProductServiceImpl implements ProductService {
	
	/** The Constant PERMISSION_NOT_COPY_GROUP_SYSTEMMGR. */
	private static final String PERMISSION_NOT_COPY_GROUP_SYSTEMMGR = "GROUP_SystemMgr";
	
	/** The Constant PERMISSION_NOT_COPY_GROUP_EVERYONE. */
	private static final String PERMISSION_NOT_COPY_GROUP_EVERYONE = "GROUP_EVERYONE";	

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
	
	/** The product report visitor. */
	private NodeVisitor productReportVisitor;
	
	private ProductVisitor compositionCalculatingVisitor;
	
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
	
	/** The permission service. */
	private PermissionService permissionService;
	
	/** The ownable service. */
	private OwnableService ownableService;
	
	/** The policy behaviour filter. */
	private BehaviourFilter policyBehaviourFilter;
	
	private LockService lockService;
	
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
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.product.ProductService#setProductReportVisitor(fr.becpg.repo.product.NodeVisitor)
	 */
	@Override
	public void setProductReportVisitor(NodeVisitor productReportVisitor){
    	this.productReportVisitor = productReportVisitor;
    }	
		
	public void setCompositionCalculatingVisitor(
			ProductVisitor compositionCalculatingVisitor) {
		this.compositionCalculatingVisitor = compositionCalculatingVisitor;
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
	 * Sets the permission service.
	 *
	 * @param permissionService the new permission service
	 */
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}
	
	/**
	 * Sets the ownable service.
	 *
	 * @param ownableService the new ownable service
	 */
	public void setOwnableService(OwnableService ownableService) {
		this.ownableService = ownableService;
	}	
	
	/**
	 * Sets the policy behaviour filter.
	 *
	 * @param policyBehaviourFilter the new policy behaviour filter
	 */
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}	
	
	public void setLockService(LockService lockService) {
		this.lockService = lockService;
	}

	/**
	 * Formulate the product.
	 *
	 * @param productNodeRef the product node ref
	 */
    @Override
    public void formulate(NodeRef productNodeRef){

		//Load product 
    	Collection<QName> dataLists = new ArrayList<QName>();				
		dataLists.add(BeCPGModel.TYPE_COMPOLIST);
		dataLists.add(BeCPGModel.TYPE_PACKAGINGLIST);
		dataLists.add(BeCPGModel.TYPE_NUTLIST); // TODO keep min/max
    	ProductData productData = productDAO.find(productNodeRef, dataLists);     	    
    	
    	// do the formulation if the product has a composition, or packaging list defined
    	if(productData.getCompoList() != null || productData.getPackagingList() != null){
    		
    		//Call visitors   
    		productData = compositionCalculatingVisitor.visit(productData);
	    	productData = allergensCalculatingVisitor.visit(productData);
	    	productData = nutsCalculatingVisitor.visit(productData);
	    	productData = costsCalculatingVisitor.visit(productData);
	    	productData = ingsCalculatingVisitor.visit(productData);
	    	    	
	    	dataLists.add(BeCPGModel.TYPE_ALLERGENLIST);
	    	dataLists.add(BeCPGModel.TYPE_NUTLIST);
	    	dataLists.add(BeCPGModel.TYPE_COSTLIST);
	    	dataLists.add(BeCPGModel.TYPE_INGLIST);
	    	dataLists.add(BeCPGModel.TYPE_INGLABELINGLIST);
	    	dataLists.add(BeCPGModel.TYPE_REQCTRLLIST);
	    	productDAO.update(productNodeRef, productData, dataLists);
    	}    	    	    
    }        
    
    /**
	 * Check if the system should generate the report for this product
	 * @param productNodeRef
	 * @return
	 */
    @Override
	public boolean IsReportable(NodeRef productNodeRef) {
		
    	if(nodeService.exists(productNodeRef)){
    		
    		// do not generate report for product version
    		if(!nodeService.hasAspect(productNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)){
    			return true;
    		}
    	}
		return false;			
	}
    
    /**
     * Generate product reports.
     *
     * @param productNodeRef the product node ref
     */
    @Override
    public void generateReport(NodeRef productNodeRef){    
    	    	
    	if(lockService.getLockStatus(productNodeRef) == LockStatus.NO_LOCK){
    	
    		try{
        		// Ensure that the policy doesn't refire for this node
				// on this thread
				// This won't prevent background processes from
				// refiring, though
	            policyBehaviourFilter.disableBehaviour(productNodeRef, BeCPGModel.TYPE_PRODUCT);	
	            policyBehaviourFilter.disableBehaviour(productNodeRef, ContentModel.ASPECT_AUDITABLE);
	            
	            // generate reports
	            productReportVisitor.visitNode(productNodeRef);				            
	        }
	        finally{
	        	policyBehaviourFilter.enableBehaviour(productNodeRef, ContentModel.ASPECT_AUDITABLE);
	        	policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_PRODUCT);			        	
	        }	         
    	}    	
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
			
			logger.debug(String.format("Classify product '%s' in folder '%s' ", name, nodeService.getPath(destionationNodeRef)));

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
		List<AssociationRef> associationRefs = nodeService.getSourceAssocs(productNodeRef, BeCPGModel.ASSOC_COMPOLIST_PRODUCT);

		logger.debug("associationRefs size" + associationRefs.size());
		
		for(AssociationRef associationRef : associationRefs){
						
			NodeRef nodeRef = associationRef.getSourceRef();									
			
			//we display nodes that are in workspace
			if(nodeRef != null && nodeRef.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_WORKSPACE)){
				NodeRef compoListNodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();

				if(compoListNodeRef != null){
					NodeRef dataListsNodeRef = nodeService.getPrimaryParent(compoListNodeRef).getParentRef();
					
					if(dataListsNodeRef != null){
						NodeRef rootNodeRef = nodeService.getPrimaryParent(dataListsNodeRef).getParentRef();
						logger.debug("rootNodeRef: " + rootNodeRef);
						
						//we don't display history version
						if(!nodeService.hasAspect(rootNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)){
							
							Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
							//int wUsedLevel = 1;
							int level = (Integer)properties.get(BeCPGModel.PROP_DEPTH_LEVEL);
							CompoListUnit compoListUnit = CompoListUnit.valueOf((String)properties.get(BeCPGModel.PROP_COMPOLIST_UNIT));
							
							CompoListDataItem compoListDataItem = new CompoListDataItem(nodeRef, level, 
														(Float)properties.get(BeCPGModel.PROP_COMPOLIST_QTY), 
														(Float)properties.get(BeCPGModel.PROP_COMPOLIST_QTY_SUB_FORMULA), 
														(Float)properties.get(BeCPGModel.PROP_COMPOLIST_QTY_AFTER_PROCESS), 
														compoListUnit, 
														(Float)properties.get(BeCPGModel.PROP_COMPOLIST_LOSS_PERC), 
														(String)properties.get(BeCPGModel.PROP_COMPOLIST_DECL_GRP), 
														(String)properties.get(BeCPGModel.PROP_COMPOLIST_DECL_TYPE), 
														rootNodeRef);
							
							wUsedList.add(compoListDataItem);
				    		
//				    		//load recipe fathers
//							while(level > wUsedLevel){
//							
//								wUsedLevel++;								
//								List<AssociationRef> compoAssocRefs = nodeService.getTargetAssocs(nodeRef, BeCPGModel.ASSOC_COMPOLIST_FATHER);				
//								
//								if(compoAssocRefs.size() > 0){
//									
//									NodeRef fatherNodeRef = (compoAssocRefs.get(0)).getTargetRef();				
//									compoAssocRefs = nodeService.getTargetAssocs(fatherNodeRef, BeCPGModel.ASSOC_COMPOLIST_PRODUCT);
//						    		NodeRef part = (compoAssocRefs.get(0)).getTargetRef();
//									compoListDataItem.setProduct(part);
//									
//									properties = nodeService.getProperties(fatherNodeRef);
//									compoListUnit = CompoListUnit.valueOf((String)properties.get(BeCPGModel.PROP_COMPOLIST_UNIT));
//									compoListDataItem = new CompoListDataItem(fatherNodeRef, wUsedLevel, (Float)properties.get(BeCPGModel.PROP_COMPOLIST_QTY), (Float)properties.get(BeCPGModel.PROP_COMPOLIST_QTY_SUB_FORMULA), (Float)properties.get(BeCPGModel.PROP_COMPOLIST_QTY_AFTER_PROCESS), compoListUnit, (Float)properties.get(BeCPGModel.PROP_COMPOLIST_LOSS_PERC), (String)properties.get(BeCPGModel.PROP_COMPOLIST_DECL_GRP), (String)properties.get(BeCPGModel.PROP_COMPOLIST_DECL_TYPE), null);
//									wUsedList.add(compoListDataItem);
//									
//									nodeRef = fatherNodeRef;
//								}
//							}
//						
//							compoListDataItem.setProduct(rootNodeRef);
						}
					}
				}
			}
		}
		
		logger.debug("wUsedList size" + wUsedList.size());
		
		return wUsedList;
	}
	
	@Override
	public List<PackagingListDataItem> getWUsedPackagingList(NodeRef productNodeRef) {
		
		logger.debug("getWUsedProduct");
		
		List<PackagingListDataItem> wUsedList = new ArrayList<PackagingListDataItem>();
		List<AssociationRef> associationRefs = nodeService.getSourceAssocs(productNodeRef, BeCPGModel.ASSOC_PACKAGINGLIST_PRODUCT);

		logger.debug("associationRefs size" + associationRefs.size());
		
		for(AssociationRef associationRef : associationRefs){
						
			NodeRef nodeRef = associationRef.getSourceRef();									
			
			//we display nodes that are in workspace
			if(nodeRef != null && nodeRef.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_WORKSPACE)){
				NodeRef compoListNodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();

				if(compoListNodeRef != null){
					NodeRef dataListsNodeRef = nodeService.getPrimaryParent(compoListNodeRef).getParentRef();
					
					if(dataListsNodeRef != null){
						NodeRef rootNodeRef = nodeService.getPrimaryParent(dataListsNodeRef).getParentRef();
						logger.debug("rootNodeRef: " + rootNodeRef);
						
						//we don't display history version
						if(!nodeService.hasAspect(rootNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)){
							
							Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);			
							PackagingListUnit packagingListUnit = PackagingListUnit.valueOf((String)properties.get(BeCPGModel.PROP_PACKAGINGLIST_UNIT));							
							
							PackagingListDataItem packagingListDataItem = new PackagingListDataItem(nodeRef, 									
										(Float)properties.get(BeCPGModel.PROP_PACKAGINGLIST_QTY), 
										packagingListUnit, 
										(String)properties.get(BeCPGModel.PROP_PACKAGINGLIST_PKG_LEVEL), rootNodeRef);
							
							wUsedList.add(packagingListDataItem);
						}
					}
				}
			}
		}
		
		logger.debug("wUsedList size" + wUsedList.size());
		
		return wUsedList;
	}
}
