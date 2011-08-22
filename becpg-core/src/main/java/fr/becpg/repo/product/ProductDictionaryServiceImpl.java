/*
 * 
 */
package fr.becpg.repo.product;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.lucene.LuceneFunction;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.common.RepoConsts;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemProductType;
import fr.becpg.model.SystemState;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.helper.LuceneHelper.Operator;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.data.ProductData;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductDictionaryServiceImpl.
 *
 * @author querephi
 */
public class ProductDictionaryServiceImpl implements ProductDictionaryService {

	/** The Constant LOCALIZATION_PFX_PATH. */
	private static final String LOCALIZATION_PFX_PATH	= "path";
	
	/** The Constant LOCALIZATION_PFX_STATE. */
	private static final String LOCALIZATION_PFX_STATE	= "state";
	
	/** The Constant LOCALIZATION_PFX_PRODUCT. */
	private static final String LOCALIZATION_PFX_PRODUCT	= "product";
	
	/** The Constant LOCALIZATION_PFX_PRODUCT_TYPE. */
	private static final String LOCALIZATION_PFX_PRODUCT_TYPE	= "product-type";	
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ProductDictionaryServiceImpl.class);
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The dictionary service. */
	private DictionaryService dictionaryService;
	
	/** The search service. */
	private SearchService searchService;
	
	/** The file folder service. */
	private FileFolderService fileFolderService;
	
	/** The repository helper. */
	private org.alfresco.repo.model.Repository repositoryHelper;
	
	/** The repo service. */
	private RepoService repoService;
		
	/**
	 * Sets the node service.
	 *
	 * @param nodeService the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	/**
	 * Sets the dictionary service.
	 *
	 * @param dictionaryService the new dictionary service
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}
	
	/**
	 * Sets the search service.
	 *
	 * @param searchService the new search service
	 */
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
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
	 * Sets the repository helper.
	 *
	 * @param repositoryHelper the new repository helper
	 */
	public void setRepositoryHelper(org.alfresco.repo.model.Repository repositoryHelper) {
		this.repositoryHelper = repositoryHelper;
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
	 * Get the data lists defined in the model.
	 *
	 * @return the data lists
	 */
	@Override
	public Collection<QName> getDataLists(){

		Collection<QName> productDataLists = dictionaryService.getSubTypes(BeCPGModel.TYPE_PRODUCTLIST_ITEM, true);
		productDataLists.remove(BeCPGModel.TYPE_PRODUCTLIST_ITEM);
		
		return productDataLists;
	}	
	
	/**
	 * Get the template of the product.
	 *
	 * @param productNodeRef the product node ref
	 * @return the product template
	 */
	@Override
	public NodeRef getProductTemplate(NodeRef productNodeRef) {
    	
    	NodeRef templateNodeRef = null;
    	QName typeQName = nodeService.getType(productNodeRef);
		SystemProductType systemProductType = SystemProductType.valueOf(typeQName);
		
		if(systemProductType == SystemProductType.Unknown){
			logger.error("Unknown product type. typeQName: " + typeQName);
			return null;
		}		
    	TypeDefinition typeDef = dictionaryService.getType(BeCPGModel.TYPE_PRODUCTTEMPLATE);
    	
    	logger.debug("getProductTemplate");
    	
    	List<QName> templateProperties = new ArrayList<QName>();
    	templateProperties.add(BeCPGModel.ASPECT_PRODUCT_TYPE);
    	templateProperties.add(BeCPGModel.PROP_PRODUCT_HIERARCHY1);
    	templateProperties.add(BeCPGModel.PROP_PRODUCT_HIERARCHY2);        
    	
    	StringBuilder queryPath = new StringBuilder(128);
    	queryPath.append(RepoConsts.PATH_QUERY_PRODUCT_TEMPLATES);
    	
		for(QName qName : templateProperties){						
			
			//associations, properties
			if(typeDef.getAssociations().containsKey(qName) || typeDef.getProperties().containsKey(qName)){
									
				// +@cm\\:localName:%s
				queryPath.append(LuceneHelper.getCondEqualValue(qName, (String)nodeService.getProperty(productNodeRef, qName), Operator.AND));
			}

			//aspects
			if(typeDef.getDefaultAspectNames().contains(qName)){				
				Set<QName> aspectProperties = dictionaryService.getPropertyDefs(qName).keySet();
				for(QName aspectProperty : aspectProperties){
					
					if(aspectProperty.equals(BeCPGModel.PROP_PRODUCT_TYPE)){
						//productType, get constraint value
						// +@cm\\:localName:%s					
						queryPath.append(LuceneHelper.getCondEqualValue(aspectProperty, systemProductType.toString(), Operator.AND));
					}
					else{
						// +@cm\\:localName:%s													
						queryPath.append(LuceneHelper.getCondEqualValue(aspectProperty, (String)nodeService.getProperty(productNodeRef, aspectProperty), Operator.AND));
					}
					
					
				}
			}
		}				
		
		logger.debug("queryPath : " + queryPath);
		
		SearchParameters sp = new SearchParameters();
        sp.addStore(RepoConsts.SPACES_STORE);
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(queryPath.toString());	        
        sp.setLimitBy(LimitBy.FINAL_SIZE);
        sp.setLimit(RepoConsts.MAX_RESULTS_SINGLE_VALUE);
        
        ResultSet resultSet = null;
        
        try{
	        resultSet = searchService.query(sp);
			
	        
	        if (resultSet.length() != 0){
	        	templateNodeRef = resultSet.getNodeRef(0);
	        	logger.debug("resultSet.length() : " + resultSet.length());
	        }
	        else{
	        	logger.error("Failed to get product template. queryPath: " + queryPath + " - resultSet.length() : " + resultSet.length());
	        }
	        
			return templateNodeRef;
        }
        finally{
        	if(resultSet != null){
        		resultSet.close();
        	}
        }
	}
		
	
	/**
	 * Initialize the product folders according to the product types, states and hierarchy.
	 *
	 * @param productsNodeRef : products folder
	 */
	@Override
	public void initializeRepoHierarchy(NodeRef productsNodeRef){
				
		NodeRef systemNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
		String productHierarchyFolderName =  TranslateHelper.getTranslatedPath(RepoConsts.PATH_PRODUCT_HIERARCHY);
		logger.debug("productHierarchyFolderName: " + productHierarchyFolderName);
		NodeRef productHierarchyNodeRef = nodeService.getChildByName(systemNodeRef, ContentModel.ASSOC_CONTAINS, productHierarchyFolderName);
						
		/*-- Create product state folders --*/
		for(SystemState systemState : SystemState.values()){
			
			String folderName = getFolderName(systemState);
			NodeRef productStateNodeRef = repoService.createFolderByPath(productsNodeRef, systemState.toString(), folderName);
			
			/*-- Create product type folders --*/
			for(SystemProductType systemProductType : SystemProductType.values()){
				
				if(systemProductType.equals(SystemProductType.Unknown))
					continue;
				
				folderName =  getFolderName(systemProductType);
				NodeRef productTypeFolder = repoService.createFolderByPath(productStateNodeRef, systemProductType.toString(), folderName);				
				
				/*-- Create hierarchy1 and hierarchy2 folders --*/
				String hierarchy1FolderName = TranslateHelper.getTranslatedPath(String.format("%s_%s", systemProductType, RepoConsts.PATH_HIERARCHY_SFX_HIERARCHY1));
				NodeRef hierarchy1FolderNodeRef = nodeService.getChildByName(productHierarchyNodeRef, ContentModel.ASSOC_CONTAINS, hierarchy1FolderName);							
				List<FileInfo> hierarchy1FileInfos = new ArrayList<FileInfo>();
				if(hierarchy1FolderNodeRef != null)
					hierarchy1FileInfos = fileFolderService.listFiles(hierarchy1FolderNodeRef);
				
				String hierarchy2FolderName = TranslateHelper.getTranslatedPath(String.format("%s_%s", systemProductType, RepoConsts.PATH_HIERARCHY_SFX_HIERARCHY2));
				NodeRef hierarchy2FolderNodeRef = nodeService.getChildByName(productHierarchyNodeRef, ContentModel.ASSOC_CONTAINS, hierarchy2FolderName);
				List<FileInfo> hierarchy2FileInfos = new ArrayList<FileInfo>();
				if(hierarchy2FolderNodeRef != null)
					hierarchy2FileInfos = fileFolderService.listFiles(hierarchy2FolderNodeRef);
				
				for(FileInfo file : hierarchy1FileInfos){
					
					String hierarchy1 = (String)nodeService.getProperty(file.getNodeRef(), ContentModel.PROP_NAME);
					NodeRef hierarchy1Folder = nodeService.getChildByName(productTypeFolder, ContentModel.ASSOC_CONTAINS, hierarchy1);
					
					if(hierarchy1Folder == null){
						logger.debug("Create  folder hierarchy1 : " + hierarchy1);
						hierarchy1Folder = fileFolderService.create(productTypeFolder, hierarchy1, ContentModel.TYPE_FOLDER).getNodeRef();
					}
					
					//Hierarchy2
					for(FileInfo file2 : hierarchy2FileInfos){
						
						String prevValue = (String)nodeService.getProperty(file2.getNodeRef(), BeCPGModel.PROP_LINKED_VALUE_PREV_VALUE);
						
						if(prevValue.equals(hierarchy1)){
							
							String hierarchy2 = (String)nodeService.getProperty(file2.getNodeRef(), BeCPGModel.PROP_LINKED_VALUE_VALUE);
							NodeRef hierarchy2Folder = nodeService.getChildByName(hierarchy1Folder, ContentModel.ASSOC_CONTAINS, hierarchy2);
							
							if(hierarchy2Folder == null){
								logger.debug("Create  folder hierarchy2 : " + hierarchy2);
								hierarchy2Folder = fileFolderService.create(hierarchy1Folder, hierarchy2, ContentModel.TYPE_FOLDER).getNodeRef();
							}
						}
						
					}
				}					
			}
		}
	}
	
	/**
	 * Gets the system state.
	 *
	 * @param systemState the system state
	 * @return the system state
	 */
	public static SystemState getSystemState(String systemState) {
		
		return (systemState != null && systemState != "") ? SystemState.valueOf(systemState) : SystemState.ToValidate;		
	}
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.product.ProductDictionaryService#getDisplayName(fr.becpg.model.SystemState)
	 */
	@Override
	public String getDisplayName(SystemState systemState) {
		
		String messageId = String.format("%s.%s.%s", LOCALIZATION_PFX_STATE, LOCALIZATION_PFX_PRODUCT, systemState).toLowerCase();
		String folderName = I18NUtil.getMessage(messageId, Locale.getDefault());
		
		if(folderName == null)
			logger.error("Failed to get the display name of the system state: " + systemState + " - messageId: " + messageId);
		
		return folderName;
	}
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.product.ProductDictionaryService#getDisplayName(fr.becpg.model.SystemProductType)
	 */
	@Override
	public String getDisplayName(SystemProductType systemProductType) {
		
		String messageId = String.format("%s.%s", LOCALIZATION_PFX_PRODUCT, systemProductType).toLowerCase();
		String folderName = I18NUtil.getMessage(messageId, Locale.getDefault());
		
		if(folderName == null)
			logger.error("Failed to get the display name of the system product type: " + systemProductType + " - messageId: " + messageId);
		
		return folderName;
	}	
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.product.ProductDictionaryService#getFolderName(fr.becpg.model.SystemState)
	 */
	@Override
	public String getFolderName(SystemState systemState) {
		
		return TranslateHelper.getTranslatedPath(String.format("%s.%s", LOCALIZATION_PFX_PRODUCT, systemState));				
	}
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.product.ProductDictionaryService#getFolderName(fr.becpg.model.SystemProductType)
	 */
	@Override
	public String getFolderName(SystemProductType systemProductType) {
		
		return TranslateHelper.getTranslatedPath(systemProductType.toString());
	}
}
