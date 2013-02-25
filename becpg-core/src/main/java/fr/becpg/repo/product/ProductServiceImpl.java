/*
 * 
 */
package fr.becpg.repo.product;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemProductType;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
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
	
	private NodeService nodeService;	
	
	private AlfrescoRepository<ProductData> alfrescoRepository;
	
	private ProductDictionaryService productDictionaryService;
	
	private RepoService repoService;

	private OwnableService ownableService;

	private FormulationService<ProductData> formulationService;
	
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

	public void setCharactDetailsVisitorFactory(CharactDetailsVisitorFactory charactDetailsVisitorFactory) {
		this.charactDetailsVisitorFactory = charactDetailsVisitorFactory;
	}

	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}

	public void setOwnableService(OwnableService ownableService) {
		this.ownableService = ownableService;
	}	

    public void setFormulationService(FormulationService<ProductData> formulationService) {
		this.formulationService = formulationService;
	}


	@Override
    public void formulate(NodeRef productNodeRef) throws FormulateException {
		markDetaillable(formulationService.formulate(productNodeRef));
    }       
    
   
    @Override
    public ProductData formulate(ProductData productData) throws FormulateException {
    	return markDetaillable(formulationService.formulate(productData));
    	
    	
    }    
    
    
    /**
     * Instead a list of aspect should be on RepositoryEntity
     * this aspect are added by dao on created and by formulation
     * @param productData
     * @return
     */
    @Deprecated
	private ProductData markDetaillable(ProductData productData) {

    	for(CostListDataItem costListDataItem : productData.getCostList()){
    		nodeService.addAspect(costListDataItem.getNodeRef(), BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM, new HashMap<QName, Serializable>());
    	}
    	
    	for(IngListDataItem ingListDataItem : productData.getIngList()){
    		nodeService.addAspect(ingListDataItem.getNodeRef(), BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM, new HashMap<QName, Serializable>());
    	}
    	
    	for(NutListDataItem nutListDataItem : productData.getNutList()){
    		nodeService.addAspect(nutListDataItem.getNodeRef(), BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM, new HashMap<QName, Serializable>());
    	}
    	
    	return productData;
	}

	@Override
	public CharactDetails formulateDetails(NodeRef productNodeRef, QName datatType, String dataListName, List<NodeRef> elements) throws FormulateException {


    	ProductData productData = alfrescoRepository.findOne(productNodeRef);
    	        	
    	CharactDetailsVisitor visitor  = charactDetailsVisitorFactory.getCharactDetailsVisitor(datatType, dataListName);		
		return visitor.visit(productData, elements);		
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
		
		// product type		
		SystemProductType systemProductType = SystemProductType.valueOf(nodeService.getType(productNodeRef));
		String productTypeFolderName = productDictionaryService.getFolderName(systemProductType);
		NodeRef productTypeNodeRef = repoService.createFolderByPath(productsNodeRef, systemProductType.toString(), productTypeFolderName);
		
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

			// classify product
			repoService.moveNode(productNodeRef, destinationNodeRef);
			
			// productNodeRef : remove all owner related rights 
            ownableService.setOwner(productNodeRef, OwnableService.NO_OWNER);    			
		}
		else{
			logger.debug("Failed to classify product. productNodeRef: " + productNodeRef);
		}
    }


}
