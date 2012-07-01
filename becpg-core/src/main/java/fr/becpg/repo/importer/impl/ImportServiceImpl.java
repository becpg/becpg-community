/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.importer.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.common.csv.CSVReader;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.importer.ImportContext;
import fr.becpg.repo.importer.ImportService;
import fr.becpg.repo.importer.ImportType;
import fr.becpg.repo.importer.ImportVisitor;
import fr.becpg.repo.importer.ImporterException;

/**
 * Import service.
 *
 * @author querephi
 */
public class ImportServiceImpl implements ImportService {
	
		
	
	/** The Constant PFX_COMMENT. */
	private static final String PFX_COMMENT	= "#";
	
	/** The Constant PFX_MAPPING. */
	private static final String PFX_MAPPING = "MAPPING";
	
	/** The Constant PFX_PATH. */
	private static final String PFX_PATH = "PATH";
	
	/** The Constant PFX_TYPE. */
	private static final String PFX_TYPE = "TYPE";
	
	private static final String PFX_STOP_ON_FIRST_ERROR = "STOP_ON_FIRST_ERROR";
	
	/** The Constant PFX_COLUMS. */
	private static final String PFX_COLUMS = "COLUMNS";
	
	/** The Constant PFX_VALUES. */
	private static final String PFX_VALUES = "VALUES";
	
	private static final String PFX_IMPORT_TYPE = "IMPORT_TYPE";
	
	private static final String PFX_DISABLED_POLICIES = "DISABLED_POLICIES";
	
	private static final String PATH_SITES = "st:sites";
	
	private static final String FORMAT_DATE_FRENCH = "dd/MM/yyyy";
	private static final String FORMAT_DATE_ENGLISH = "yyyy/MM/dd";	
	
	private static final String MSG_INFO_IMPORT_BATCH = "import_service.info.import_batch";
	private static final String MSG_INFO_IMPORT_LINE = "import_service.info.import_line";
	
	private static final String MSG_ERROR_IMPORT_LINE = "import_service.error.err_import_line";
	private static final String MSG_ERROR_UNSUPPORTED_PREFIX = "import_service.error.err_unsupported_prefix";
	private static final String MSG_ERROR_MAPPING_NOT_FOUND = "import_service.error.err_mapping_not_found";
	private static final String MSG_ERROR_READING_MAPPING = "import_service.error.err_reading_mapping";
	private static final String MSG_ERROR_UNDEFINED_LINE = "import_service.error.err_undefined_line";
	private static final String MSG_ERROR_UNKNOWN_TYPE = "import_service.error.err_unknown_type";
	
	/** The Constant SEPARATOR. */
	private static final char SEPARATOR = ';';	
	
	private static final int COLUMN_PREFIX = 0;
	private static final int COLUMN_MAPPING = 1;
	private static final int COLUMN_PATH = 1;
	private static final int COLUMN_TYPE = 1;
	private static final int COLUMN_IMPORT_TYPE = 1;
	private static final int COLUMN_DISABLED_POLICIES = 1;
	
	private static final int BATCH_SIZE	= 10;
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ImportServiceImpl.class);
	
	/** The search service. */
	private SearchService searchService = null;
	
	/** The node service. */
	private NodeService nodeService = null;
	
	/** The content service. */
	private ContentService contentService = null;
	
	/** The service registry. */
	private ServiceRegistry serviceRegistry = null;
	
	/** The repo service. */
	private RepoService repoService = null;	
	
	/** The repository helper. */
	private Repository repositoryHelper;
	
	/** The import node visitor. */
	private ImportVisitor importNodeVisitor;
	
	/** The import product visitor. */
	private ImportVisitor importProductVisitor;
	
	private ImportVisitor importEntityListAspectVisitor;
	
	private ImportVisitor importEntityListItemVisitor;
	
	/** The dictionary service. */
	private DictionaryService dictionaryService;	
	
	private BehaviourFilter policyBehaviourFilter;
					
	/**
	 * Sets the search service.
	 *
	 * @param searchService the new search service
	 */
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}	
	
	/**
	 * Sets the node service.
	 *
	 * @param nodeService the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	/**
	 * Sets the content service.
	 *
	 * @param contentService the new content service
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}
	
	/**
	 * Sets the service registry.
	 *
	 * @param serviceRegistry the new service registry
	 */
	public void setServiceRegistry(ServiceRegistry serviceRegistry){
    	this.serviceRegistry = serviceRegistry;
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
	 * Sets the repository helper.
	 *
	 * @param repositoryHelper the new repository helper
	 */
	public void setRepositoryHelper(Repository repositoryHelper) {
		this.repositoryHelper = repositoryHelper;
	}
	
	/**
	 * Sets the import node visitor.
	 *
	 * @param importNodeVisitor the new import node visitor
	 */
	public void setImportNodeVisitor(ImportVisitor importNodeVisitor) {
		this.importNodeVisitor = importNodeVisitor;
	}
	
	/**
	 * Sets the import product visitor.
	 *
	 * @param importProductVisitor the new import product visitor
	 */
	public void setImportProductVisitor(ImportVisitor importProductVisitor) {
		this.importProductVisitor = importProductVisitor;
	}
	
	public void setImportEntityListItemVisitor(ImportVisitor importEntityListItemVisitor) {
		this.importEntityListItemVisitor = importEntityListItemVisitor;
	}

	public void setImportEntityListAspectVisitor(ImportVisitor importEntityListAspectVisitor) {
		this.importEntityListAspectVisitor = importEntityListAspectVisitor;
	}
	
	/**
	 * Sets the dictionary service.
	 *
	 * @param dictionaryService the new dictionary service
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}
	
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	/**
	 * Import a text file
	 * @throws ParseException 
	 * @throws IOException 
	 */
	@Override
	public List<String> importText(NodeRef nodeRef, boolean doUpdate, boolean requiresNewTransaction) throws ImporterException, IOException, ParseException, Exception{
		
		logger.debug("start import");

		ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
		InputStream is = null;
		try{
			is = reader.getContentInputStream();
			

			if (logger.isDebugEnabled()) {
				logger.debug("Reading Import File");
			}
			Charset charset = ImportHelper.guestCharset(is,reader.getEncoding());
			if(logger.isDebugEnabled()){
				logger.debug("reader.getEncoding() : " + reader.getEncoding());
				logger.debug("finder.getEncoding() : " + charset );
			}
			
			ImportContext importContext = new ImportContext();
			importContext.setDoUpdate(doUpdate);
			importContext.setStopOnFirstError(true);
			String dateFormat = (Locale.getDefault().equals(Locale.FRENCH) || Locale.getDefault().equals(Locale.FRANCE)) ? FORMAT_DATE_FRENCH
					: FORMAT_DATE_ENGLISH;
			importContext.getPropertyFormats().setDateFormat(new SimpleDateFormat(dateFormat));
			importContext.getPropertyFormats().setDecimalFormat(
					(DecimalFormat) NumberFormat.getNumberInstance(Locale.getDefault()));
			importContext.setImportFileName((String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
			importContext.setRequiresNewTransaction(requiresNewTransaction);
			
			return proccessUpload(is, importContext ,charset);
			
		
			
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
	
	
	private List<String> proccessUpload(InputStream input, ImportContext importContext, Charset charset) throws ImporterException, ParseException, Exception {
        if (importContext.getImportFileName() != null && importContext.getImportFileName().length() > 0)
        {
            if (importContext.getImportFileName().endsWith(".csv"))
            {
            	  return  processCSVUpload(input, importContext ,charset);
            }
            if (importContext.getImportFileName().endsWith(".xml"))
            {
                return processXMLUpload(input,importContext);
                
            }
          
        }
        // If in doubt, assume it's probably a .csv
        return  processCSVUpload(input, importContext ,charset);

}
	
	
	
	private List<String> processXMLUpload(InputStream input,ImportContext importContext) {
		return null;
		// TODO Auto-generated method stub
		
	}

	private List<String> processCSVUpload(InputStream input, ImportContext importContext, Charset charset) throws ImporterException, ParseException, Exception {
		// open file and load content
		
		CSVReader csvReader = new CSVReader(new InputStreamReader(input,charset), SEPARATOR);
		try {
			// context
			importContext.setCsvReader(csvReader);
			// import				
			return importCSV(importContext, csvReader);				
		} finally {
			if(csvReader!=null){
				csvReader.close();
			}
		}
	
	}

	@Override
	public void moveImportedFile(NodeRef nodeRef, boolean hasFailed) {			
				
		// delete files that have the same name before moving it in the succeeded or failed folder
		String queryPath = "";
		NodeRef failedFolder = null;
		NodeRef succeededFolder = null;
		ResultSet resultSet = null;
		try{
			// failed
			queryPath = RepoConsts.PATH_QUERY_IMPORT_FAILED_FOLDER;
			resultSet = searchService.query(RepoConsts.SPACES_STORE, SearchService.LANGUAGE_LUCENE, queryPath);
			failedFolder = resultSet.getNodeRef(0);
			
			if(failedFolder != null){
				NodeRef targetNodeRef = nodeService.getChildByName(failedFolder, ContentModel.ASSOC_CONTAINS, (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
				if(targetNodeRef != null){
					nodeService.deleteNode(targetNodeRef);
				}
			}
			
			// succeeded
			queryPath = RepoConsts.PATH_QUERY_IMPORT_SUCCEEDED_FOLDER;
			resultSet = searchService.query(RepoConsts.SPACES_STORE, SearchService.LANGUAGE_LUCENE, queryPath);
			succeededFolder = resultSet.getNodeRef(0);
			
			if(succeededFolder != null){
				NodeRef targetNodeRef = nodeService.getChildByName(succeededFolder, ContentModel.ASSOC_CONTAINS, (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
				if(targetNodeRef != null){
					nodeService.deleteNode(targetNodeRef);
				}
			}
			
		}
		catch(Exception e){
			logger.error("Missing folder 'Import failed' or 'Import Succeeded'. Lucene query: " + queryPath, e);
		}
		finally{
			if(resultSet != null)
				resultSet.close();
		}
		
		// move nodeRef in the right folder
		NodeRef parentNodeRef = hasFailed ? failedFolder : succeededFolder;				
		if(parentNodeRef != null){	
			nodeService.moveNode(nodeRef, parentNodeRef, ContentModel.ASSOC_CONTAINS, nodeService.getType(nodeRef));
		}
		
	}
		
	/**
	 * Import text.
	 * @param csvReader 
	 *
	 * @param CSVReader the csv reader
	 * @param doUpdate the do update
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ImporterException the be cpg exception
	 * @throws ParseException the parse exception
	 */
	private List<String> importCSV(ImportContext importContext, CSVReader csvReader) throws IOException, ImporterException, ParseException, Exception{
		
		logger.debug("importFile");				
			
		Element mappingElt = null;
		NamespaceService namespaceService = serviceRegistry.getNamespaceService();
		RetryingTransactionHelper txnHelper = serviceRegistry.getRetryingTransactionHelper();				
		String[] arrStr = null;		
		
		while((arrStr = importContext.readLine()) != null){
			
			String prefix = arrStr[COLUMN_PREFIX];						
			
			if(prefix.isEmpty()){
				// skip blank lines, nothing to do...
			}
			else if(prefix.equals(PFX_COMMENT)){
				// skip comments starting with the # character, nothing to do...
			}			
			else if(prefix.equals(PFX_PATH)){
								
				importContext.setParentNodeRef(null);
				
				String pathValue = arrStr[COLUMN_PATH];
				importContext.setPath(cleanPath(pathValue));
				
				if(pathValue.isEmpty())
					throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_UNDEFINED_LINE, PFX_PATH, importContext.getCSVLine()));
				
				if(pathValue.startsWith(PATH_SITES) || pathValue.startsWith(RepoConsts.PATH_SEPARATOR + PATH_SITES)){					
					importContext.setSiteDocLib(true);
				}
				else{
					importContext.setSiteDocLib(false);
				}					
				
				List<String> paths = new ArrayList<String>();
				String[] arrPath = pathValue.split(RepoConsts.PATH_SEPARATOR);
				for(String path : arrPath)
					paths.add(path);	
				
				// use transaction, otherwise folder is not found in the transactions when we import lines
				final List<String> finalPaths = paths;
				NodeRef parentNodeRef = txnHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>(){
					public NodeRef execute() throws Exception{
		                    	
						return repoService.createFolderByPaths(repositoryHelper.getCompanyHome(), finalPaths);
					}
				},
	   			false,    // read only flag
	   			importContext.isRequiresNewTransaction());  // requires new txn flag
				
				importContext.setParentNodeRef(parentNodeRef);																
			}
			else if(prefix.equals(PFX_IMPORT_TYPE)){
				
				importContext.setImportType(null);
				
				String importTypeValue = arrStr[COLUMN_IMPORT_TYPE];
				if(importTypeValue.isEmpty()){
					throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_UNDEFINED_LINE, PFX_IMPORT_TYPE, importContext.getCSVLine()));
				}
					
				ImportType importType = ImportType.valueOf(importTypeValue);
				importContext.setImportType(importType);
			}
			else if(prefix.equals(PFX_DISABLED_POLICIES)){
				
				importContext.getDisabledPolicies().clear();
				
				String disabledPoliciesValue = arrStr[COLUMN_DISABLED_POLICIES];
				if(!disabledPoliciesValue.isEmpty()){
					for(String disabledPolicy : disabledPoliciesValue.split(RepoConsts.MULTI_VALUES_SEPARATOR)){
						importContext.getDisabledPolicies().add(QName.createQName(disabledPolicy, namespaceService));
					}
					
				}				
			}
			else if(prefix.equals(PFX_TYPE)){
				
				importContext.setType(null);
				
				String typeValue = arrStr[COLUMN_TYPE];
				if(typeValue.isEmpty())
					throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_UNDEFINED_LINE, PFX_TYPE, importContext.getCSVLine()));
				
				QName type = QName.createQName(typeValue, namespaceService);				
				importContext.setType(type);								
				
//				// detect or use ImportType defined in CSV file
//				if(detectImportType){
//					if(dictionaryService.isSubClass(type, BeCPGModel.TYPE_PRODUCT)){
//						importContext.setImportType(ImportType.Product);
//					}
//					else if(dictionaryService.isSubClass(type, BeCPGModel.TYPE_ENTITYLIST_ITEM) && importContext.getClassMappings().get(type)!=null){
//						importContext.setImportType(ImportType.EntityListItem);
//					}
//					else{
//						
//						// look for entityListsAspect
//						boolean entityListsAspect = false;					
//						TypeDefinition typeDef = dictionaryService.getType(type);
//						if(typeDef == null){
//							throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_UNKNOWN_TYPE, type));
//						}
//						else{
//							for(AspectDefinition aspectDef : typeDef.getDefaultAspects()){
//								if(aspectDef.getName().equals(BeCPGModel.ASPECT_ENTITYLISTS)){						
//									entityListsAspect = true;
//									break;
//								}
//							}
//						}
//						
//						
//						if(entityListsAspect){
//							importContext.setImportType(ImportType.EntityListAspect);
//						}					
//						else{
//							importContext.setImportType(ImportType.Node);
//						}
//					}	
//				}							
			}
			else if(prefix.equals(PFX_STOP_ON_FIRST_ERROR)){
				
				String stopOnFirstErrorValue = arrStr[COLUMN_TYPE];
				if(!stopOnFirstErrorValue.isEmpty()){
					importContext.setStopOnFirstError(Boolean.valueOf(stopOnFirstErrorValue));
				}
			}
			else if(prefix.equals(PFX_COLUMS)){				
				
				boolean undefinedColumns = true;
				List<String> columns = new ArrayList<String>(arrStr.length);
				for(int z_idx=1; z_idx<arrStr.length ; z_idx++){	
					if(!arrStr[z_idx].isEmpty()){
						columns.add(arrStr[z_idx]);
						undefinedColumns = false;
					}       			 			
       		 	}
				
				if(undefinedColumns){
					throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_UNDEFINED_LINE, PFX_COLUMS, importContext.getCSVLine()));
				}
			
				importContext = importNodeVisitor.loadMappingColumns(mappingElt, columns, importContext);
				
			}
			else if(prefix.equals(PFX_MAPPING)){
				
				mappingElt = null;
				
				String mappingValue = arrStr[COLUMN_MAPPING];
       		 	if(mappingValue.isEmpty())
       		 		throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_UNDEFINED_LINE, PFX_MAPPING, importContext.getCSVLine()));
       		 
       		 	mappingElt = loadMapping(mappingValue);
       		 	importContext = importNodeVisitor.loadClassMapping(mappingElt, importContext);
			}
        	 else if(prefix.equals(PFX_VALUES)){
        		 
        		 // split in several batches, calculate the last index to import
        		 int firstIndex = importContext.getImportIndex();
        		 int lastIndex = importContext.goToNextLine();
        		 
        		 while((arrStr = importContext.readLine()) != null){
        			 
        			 prefix = arrStr[COLUMN_PREFIX];

        			 if(prefix.equals(PFX_VALUES) || prefix.equals(PFX_COMMENT) || prefix.isEmpty()){
        				 lastIndex = importContext.getImportIndex();
        			 }
        			 else{        		
        				 break;
        			 }         			 
        			 importContext.goToNextLine();
        		 }        		       		         		 
        		 
        		 
        		 int nbBatches = ((lastIndex - firstIndex) / BATCH_SIZE) + 1;
        		 
        		
        			 
	    		 for(int z_idx=0 ; z_idx<nbBatches ; z_idx++ ){
	    			         			 
	    			 importContext.setImportIndex(firstIndex + (z_idx * BATCH_SIZE));
	    			 int tempIndex = firstIndex + ((z_idx + 1)* BATCH_SIZE);
	    			 final int finalLastIndex =  tempIndex > lastIndex ? lastIndex : tempIndex;
	        		 final ImportContext finalImportContext = importContext;              		     		
	        		 
	        		 // add info message in log and file import            		 
	        		 String info = I18NUtil.getMessage(MSG_INFO_IMPORT_BATCH, 							 					
							 					(z_idx + 1), nbBatches,
							 					importContext.getImportFileName(),
							 					(importContext.getImportIndex() + 1),
							 					(finalLastIndex + 1));
	        		 logger.info(info);
	        		 //notifyImportFile(importContext, info);
	        			 
	        		 // use transaction
	        		 importContext = txnHelper.doInTransaction(new RetryingTransactionCallback<ImportContext>(){
	    				 public ImportContext execute() throws Exception{
	 	                    	
							try {

								// do it in transaction otherwise, not taken in account
								for (QName disabledPolicy : finalImportContext.getDisabledPolicies()) {
									logger.debug("disableBehaviour: " + disabledPolicy);
									policyBehaviourFilter.disableBehaviour(disabledPolicy);
								}
								return importInBatch(finalImportContext, finalLastIndex);

							} finally {

								for (QName disabledPolicy : finalImportContext.getDisabledPolicies()) {
									policyBehaviourFilter.enableBehaviour(disabledPolicy);
								}
							}
	    				 }
	    			 }, 
	    			 false,											// readonly
	        		 importContext.isRequiresNewTransaction());  	// requires new txn flag
	    		 }
        		 
        		 importContext.goToPreviousLine();
        		 
        	 }else{
        		 throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_UNSUPPORTED_PREFIX, importContext.getCSVLine(), prefix));
        	 }
			
			importContext.goToNextLine();
			
		}
		
		return importContext.getLog();
    }
	
	private String cleanPath(String pathValue) {
		if(pathValue.startsWith("/")){
			return pathValue.substring(1);
		}
		return pathValue;
	}

	/**
	 * Import a batch of values
	 * @param importContext
	 * @param lastIndex
	 * @return
	 * @throws Exception
	 */
	private ImportContext importInBatch(final ImportContext importContext, final int lastIndex) throws Exception{
		
		String[] arrStr = null;
		
		while(importContext.getImportIndex() <= lastIndex && (arrStr = importContext.readLine()) != null){
						
			String prefix = arrStr[COLUMN_PREFIX];
			
			// skip COMMENTS and empty lines, we just import VALUES
			if(prefix.equals(PFX_VALUES)){
			
				try{
					 
					 boolean undefinedValues = true;
			   		 List<String> values = new ArrayList<String>(arrStr.length); 				
			   		 for(int z_idx=1; z_idx<arrStr.length ; z_idx++){	  
			   			 
			   			 String value = ImportHelper.cleanValue(arrStr[z_idx]);
			   			 values.add(value);
			   			 
			   			 if(!value.isEmpty()){
			   				 // one value defined, OK
			   				 undefinedValues = false;
			   			 }        			 	
			   		 }
			   		 
			   		 if(undefinedValues){
			   			throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_UNDEFINED_LINE, PFX_VALUES, importContext.getCSVLine())); 
			   		 }
								
			   		 if(dictionaryService.isSubClass(importContext.getType(), BeCPGModel.TYPE_PRODUCT)){
			   			 importProductVisitor.importNode(importContext, values);        			
			   		 }
			   		 else if(ImportType.EntityListAspect.equals(importContext.getImportType())){
			   			 importEntityListAspectVisitor.importNode(importContext, values);
			   		 }
			   		 else if(ImportType.EntityListItem.equals(importContext.getImportType())){
			   			 importEntityListItemVisitor.importNode(importContext, values);
			   		 }
			   		 else{
			   			 importNodeVisitor.importNode(importContext, values);
			   		 }    
			   		 
			   		 logger.info(I18NUtil.getMessage(MSG_INFO_IMPORT_LINE, importContext.getCSVLine()));
				}
				catch(ImporterException e){
					 
					 if(importContext.isStopOnFirstError()){
						 throw e;
					 }
					 else{
						 // store the exception and continue import...
						 String error = I18NUtil.getMessage(MSG_ERROR_IMPORT_LINE, importContext.getImportFileName(),
								 						importContext.getCSVLine(),
								 						new Date(),
								 						e.getMessage());
						 
						 logger.error(error);
						 importContext.getLog().add(error);       				 
					 }
				 }
				 catch(Exception e){
					 
					 if(importContext.isStopOnFirstError()){
						 throw e;
					 }
					 else{
						 // store the exception and the printStack and continue import...
						 String error = I18NUtil.getMessage(MSG_ERROR_IMPORT_LINE, importContext.getImportFileName(),
								 						importContext.getCSVLine(),
								 						new Date(),
								 						e.getMessage());
						 
						 
						 logger.error(error, e);
						 importContext.getLog().add(error);       				 
					 }
				 }
			}			
			importContext.goToNextLine();						
		}
		
		return importContext;
	}
	
	/**
	 * Load the mapping definition file.
	 *
	 * @param name the name
	 * @return the element
	 */
	private Element loadMapping(String name)throws ImporterException{
		
		Element mappingElt = null;
		NodeRef mappingNodeRef = null;  
    	
    	String queryPath = String.format(RepoConsts.PATH_QUERY_IMPORT_MAPPING, name);
					
		logger.debug(queryPath);
		
		SearchParameters sp = new SearchParameters();
		//sp.addLocale(repoConfig.getSystemLocale());
        sp.addStore(RepoConsts.SPACES_STORE);
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(queryPath.toString());	        
        sp.setLimitBy(LimitBy.FINAL_SIZE);
        sp.setLimit(RepoConsts.MAX_RESULTS_SINGLE_VALUE);
        
        ResultSet resultSet =null;
        
        try{
	        resultSet = searchService.query(sp);
			
	        logger.debug("resultSet.length() : " + resultSet.length());
	        if (resultSet.length() != 0){
	        	mappingNodeRef = resultSet.getNodeRef(0); 
	        }	        
        }
        finally{
        	if(resultSet != null)
        		resultSet.close();
        }
                				
		if(mappingNodeRef == null){
			String msg = I18NUtil.getMessage(MSG_ERROR_MAPPING_NOT_FOUND, name);
			logger.error(msg);
			throw new ImporterException(msg);
		}
		
		ContentReader reader = contentService.getReader(mappingNodeRef, ContentModel.PROP_CONTENT);
		InputStream is = reader.getContentInputStream();
		SAXReader saxReader = new SAXReader();
		
		try{
			Document doc = saxReader.read(is);
			mappingElt = doc.getRootElement();
		}
		catch(DocumentException e){			
			String msg = I18NUtil.getMessage(MSG_ERROR_READING_MAPPING, name, e.getMessage());
			logger.error(msg, e);
			throw new ImporterException(msg);
		}		
		
		return mappingElt;
	}
	
	
}

