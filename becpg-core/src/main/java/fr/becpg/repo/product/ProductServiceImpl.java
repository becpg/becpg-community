/*
 * 
 */
package fr.becpg.repo.product;

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
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
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
    
    
    //TODO add it during formulation instead
    @Deprecated
	private ProductData markDetaillable(ProductData productData) {

	    	for(CostListDataItem costListDataItem : productData.getCostList()){
	    		costListDataItem.getAspects().add(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM);
	    	}
	    	
	    	for(IngListDataItem ingListDataItem : productData.getIngList()){
	    		ingListDataItem.getAspects().add(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM);
	    	}
	    	
	    	for(NutListDataItem nutListDataItem : productData.getNutList()){
	    		nutListDataItem.getAspects().add(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM);
	    	}

	    	for(PhysicoChemListDataItem physicoChemDataItem : productData.getPhysicoChemList()){
	    		physicoChemDataItem.getAspects().add(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM);
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
		NodeRef productsNodeRef = repoService.getOrCreateFolderByPath(containerNodeRef, RepoConsts.PATH_PRODUCTS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_PRODUCTS));
		
		// product type		
		SystemProductType systemProductType = SystemProductType.valueOf(nodeService.getType(productNodeRef));
		String productTypeFolderName = productDictionaryService.getFolderName(systemProductType);
		NodeRef productTypeNodeRef = repoService.getOrCreateFolderByPath(productsNodeRef, systemProductType.toString(), productTypeFolderName);
		

		if(productData.getHierarchy2()!=null){
			destinationNodeRef = getOrCreateHierachyFolder( productData.getHierarchy2(),productTypeNodeRef);
		} else{
			logger.debug("Cannot classify product since it doesn't have a productHierarchy2.");
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

	private NodeRef getOrCreateHierachyFolder(NodeRef hierarchyNodeRef, NodeRef parentNodeRef) {
		NodeRef destinationNodeRef = null;
		
		NodeRef parent = HierarchyHelper.getParentHierachy(hierarchyNodeRef, nodeService);
		if(parent != null ){
			parentNodeRef = getOrCreateHierachyFolder(parent, parentNodeRef);		
		}
		String name = HierarchyHelper.getHierachyName(hierarchyNodeRef, nodeService);
		if(name!=null){
			destinationNodeRef = repoService.getOrCreateFolderByPath(parentNodeRef,name, name);
		}
		else{
			if(logger.isDebugEnabled()){
				logger.debug("Cannot create folder for productHierarchy since hierarchyName is null. productHierarchy: " + hierarchyNodeRef);
			}
		}		
		
		return destinationNodeRef;
	}


}
