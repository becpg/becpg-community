/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.importer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.extensions.surf.util.I18NUtil;

import com.sun.source.tree.AssertTree;

import fr.becpg.common.RepoConsts;
import fr.becpg.common.csv.CSVReader;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.RepoService;

// TODO: Auto-generated Javadoc
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
		
	/** The Constant SEPARATOR. */
	private static final char SEPARATOR = ';';	
	
	private static final int COLUMN_PREFIX = 0;
	private static final int COLUMN_MAPPING = 1;
	private static final int COLUMN_PATH = 1;
	private static final int COLUMN_TYPE = 1;
	private static final int COLUMN_STOP_ON_FIRST_ERROR	= 1;
	
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
	
	private ImportVisitor importProductListAspectVisitor;
	
	private ImportVisitor importProductListItemVisitor;
	
	/** The dictionary service. */
	private DictionaryService dictionaryService;
	
	private MimetypeService mimetypeService;
					
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
	
	public void setImportProductListItemVisitor(ImportVisitor importProductListItemVisitor) {
		this.importProductListItemVisitor = importProductListItemVisitor;
	}

	public void setImportProductListAspectVisitor(ImportVisitor importProductListAspectVisitor) {
		this.importProductListAspectVisitor = importProductListAspectVisitor;
	}
	
	/**
	 * Sets the dictionary service.
	 *
	 * @param dictionaryService the new dictionary service
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}
	
	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}

	/**
	 * Import a text file
	 * @throws ParseException 
	 * @throws IOException 
	 */
	@Override
	public List<String> importText(NodeRef nodeRef, boolean doUpdate, boolean requiresNewTransaction) throws ImporterException, IOException, ParseException, Exception{
		
		logger.debug("start import");

		// open file and load content
		ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
		InputStream is = reader.getContentInputStream();
		logger.debug("reader.getEncoding() : " + reader.getEncoding());
		
		// TEST
		ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
        Charset charset = charsetFinder.getCharset(is, reader.getMimetype());
        String encoding = charset.name();
        logger.debug("###encoding: " + encoding);        
		
		CSVReader csvReader = new CSVReader(new InputStreamReader(is, reader.getEncoding()), SEPARATOR);

		// context
		ImportContext importContext = new ImportContext(nodeRef, csvReader);
		importContext.setDoUpdate(doUpdate);
		importContext.setStopOnFirstError(true);
		String dateFormat = (Locale.getDefault().equals(Locale.FRENCH) || Locale.getDefault().equals(Locale.FRANCE)) ? FORMAT_DATE_FRENCH:FORMAT_DATE_ENGLISH;		
		importContext.getPropertyFormats().setDateFormat(new SimpleDateFormat(dateFormat));
		importContext.getPropertyFormats().setDecimalFormat((DecimalFormat)NumberFormat.getNumberInstance(Locale.getDefault()));
		importContext.setImportFileName((String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
		importContext.setRequiresNewTransaction(requiresNewTransaction);
		
		// import				
		return importCSV(importContext);				
		
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
	 *
	 * @param CSVReader the csv reader
	 * @param doUpdate the do update
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ImporterException the be cpg exception
	 * @throws ParseException the parse exception
	 */
	private List<String> importCSV(ImportContext importContext) throws IOException, ImporterException, ParseException, Exception{
		
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
			else if(prefix.equals(PFX_TYPE)){
				
				importContext.setType(null);
				
				String typeValue = arrStr[COLUMN_TYPE];
				if(typeValue.isEmpty())
					throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_UNDEFINED_LINE, PFX_TYPE, importContext.getCSVLine()));
				
				QName type = QName.createQName(typeValue, namespaceService);				
				importContext.setType(type);								
				
				if(dictionaryService.isSubClass(type, BeCPGModel.TYPE_PRODUCT)){
					importContext.setImportType(ImportType.Product);
				}
				else if(dictionaryService.isSubClass(type, BeCPGModel.TYPE_PRODUCTLIST_ITEM)){
					importContext.setImportType(ImportType.ProductListItem);
				}
				else{
					
					// look for productListsAspect
					boolean productListsAspect = false;
					for(AspectDefinition aspectDef : dictionaryService.getType(type).getDefaultAspects()){
						if(aspectDef.getName().equals(BeCPGModel.ASPECT_PRODUCTLISTS)){						
							productListsAspect = true;
							break;
						}
					}
					
					if(productListsAspect){
						importContext.setImportType(ImportType.ProductListAspect);
					}					
					else{
						importContext.setImportType(ImportType.Node);
					}
				}					
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
     	                    	
        					 return importInBatch(finalImportContext, finalLastIndex);
        				 }
        			 }, 
        			 false,											// readonly
            		 importContext.isRequiresNewTransaction());  	// requires new txn flag
            		 
            		 //importContext = importInBatch(finalImportContext, finalLastIndex);
        		 }
        		 
        		 importContext.goToPreviousLine();
        		 
        	 }else{
        		 throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_UNSUPPORTED_PREFIX, importContext.getCSVLine(), prefix));
        	 }
			
			importContext.goToNextLine();
			
		}
		
		return importContext.getLog();
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
								
			   		 if(ImportType.Product.equals(importContext.getImportType())){
			   			 importProductVisitor.importNode(importContext, values);        			
			   		 }
			   		 else if(ImportType.ProductListAspect.equals(importContext.getImportType())){
			   			 importProductListAspectVisitor.importNode(importContext, values);
			   		 }
			   		 else if(ImportType.ProductListItem.equals(importContext.getImportType())){
			   			 importProductListItemVisitor.importNode(importContext, values);
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
	
	/**
	 * NOtify the import file to display what the importer is importing
	 * @param importContext
	 */
	private void notifyImportFile(ImportContext importContext, String message){
		
		nodeService.setProperty(importContext.getImportFileNodeRef(), ContentModel.PROP_TITLE, message);
	}
	
}

