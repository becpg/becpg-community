package fr.becpg.repo.listvalue;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.AutoNumService;
import fr.becpg.repo.listvalue.impl.AbstractBaseListValuePlugin;
import fr.becpg.repo.listvalue.impl.ListValueServiceImpl;
import fr.becpg.repo.listvalue.impl.NodeRefListValueExtractor;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.repo.search.BeCPGSearchService;

public class EntityListValuePlugin extends AbstractBaseListValuePlugin {
	
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ListValueServiceImpl.class);


	/** The Constant SUFFIX_ALL. */
	private static final String SUFFIX_ALL = "*";
	
	/** The Constant SUFFIX_SPACE. */
	private static final String SUFFIX_SPACE = " ";
	
	/** The Constant SUFFIX_DOUBLE_QUOTE. */
	private static final String SUFFIX_DOUBLE_QUOTE = "\"";
	
	/** The Constant SUFFIX_SIMPLE_QUOTE. */
	private static final String SUFFIX_SIMPLE_QUOTE = "'";
	
	private static final String QUERY_TYPE = " +TYPE:\"%s\"";
	
	/** The Constant SOURCE_TYPE_TARGET_ASSOC. */
	private static final String SOURCE_TYPE_TARGET_ASSOC = "targetassoc";
	
	/** The Constant SOURCE_TYPE_PRODUCT. */
	private static final String SOURCE_TYPE_PRODUCT = "product";
	
	/** The Constant SOURCE_TYPE_LINKED_VALUE. */
	private static final String SOURCE_TYPE_LINKED_VALUE = "linkedvalue";
	
	/** The Constant SOURCE_TYPE_LIST_VALUE. */
	private static final String SOURCE_TYPE_LIST_VALUE = "listvalue";
	
	/** The Constant SOURCE_TYPE_PRODUCT_REPORT. */
	private static final String SOURCE_TYPE_PRODUCT_REPORT = "productreport";
	
	
	
	

	private static final String PARAM_VALUES_SEPARATOR = ",";

	/** The node service. */
	private NodeService nodeService;
	

	/** The namespace service. */
	private NamespaceService namespaceService;
	
	/** The product report service. */
	private ReportTplService reportTplService;
	
	
	private BeCPGSearchService beCPGSearchService;
	
	private DictionaryService dictionaryService;
	
	private DictionaryDAO dictionaryDAO;
	
	private AutoNumService autoNumService;
	
	private Analyzer luceneAnaLyzer = null;
	

	/**
	 * Sets the namespace service.
	 *
	 * @param namespaceService the new namespace service
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	


	public void setDictionaryDAO(DictionaryDAO dictionaryDAO) {
		this.dictionaryDAO = dictionaryDAO;
	}

	
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}


	/**
	 * Sets the node service.
	 *
	 * @param nodeService the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}	
			
	public void setReportTplService(ReportTplService reportTplService) {
		this.reportTplService = reportTplService;
	}
	

	public void setAutoNumService(AutoNumService autoNumService) {
		this.autoNumService = autoNumService;
	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}
	


	
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_TARGET_ASSOC, SOURCE_TYPE_PRODUCT, SOURCE_TYPE_LINKED_VALUE, SOURCE_TYPE_PRODUCT_REPORT,
				SOURCE_TYPE_LIST_VALUE };
	}

	
	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Map<String, Serializable> props) {

		String path = (String) props.get(ListValueService.PROP_PATH);
		String className = (String) props.get(ListValueService.PROP_CLASS_NAME);
		String classNames = (String) props.get(ListValueService.PROP_CLASS_NAMES);
		String[] arrClassNames = classNames != null ? classNames.split(PARAM_VALUES_SEPARATOR) : null;
		String parent = (String) props.get(ListValueService.PROP_PARENT);
		String productType = (String) props.get(ListValueService.PROP_PRODUCT_TYPE);
		

		if(sourceType.equals(SOURCE_TYPE_TARGET_ASSOC)){
			QName type = QName.createQName(className, namespaceService);
			return suggestTargetAssoc(type, query,pageNum, arrClassNames);
		}
		else if(sourceType.equals(SOURCE_TYPE_PRODUCT)){
			
			return suggestProduct(query,pageNum, arrClassNames);
		}
		else if(sourceType.equals(SOURCE_TYPE_LINKED_VALUE)){
			return suggestLinkedValue(path, parent, query,pageNum);
		}
		else if(sourceType.equals(SOURCE_TYPE_LIST_VALUE)){
			return suggestListValue(path, query, pageNum);
		}
		else if(sourceType.equals(SOURCE_TYPE_PRODUCT_REPORT)){			
			
			QName productTypeQName = QName.createQName(productType, namespaceService);
			return suggestProductReportTemplates(productTypeQName, query,pageNum);

		}
		
		return null;
	}

	

	/**
	 * Suggest target class according to query
	 * 
	 * Query path :
	 * +PATH:"/app:company_home/cm:System/cm:Lists/cm:Nuts/*" +TYPE:"bcpg:nut" +@cm\:name:"Nut1*".
	 *
	 * @param type the type
	 * @param query the query
	 * @return the map
	 */
    
	public ListValuePage suggestTargetAssoc(QName type, String query, Integer pageNum, String[] arrClassNames){			
        
    	logger.debug("suggestTargetAssoc");
    	
    	String queryPath = "";
    	
    	//Is code or name search
    	if(isQueryCode(query,type)){
			query = prepareQueryCode(query,type);
			queryPath = String.format(RepoConsts.QUERY_SUGGEST_TARGET_BY_CODE, type, query);
		} else if(isAllQuery(query)){
			queryPath = String.format(RepoConsts.QUERY_SUGGEST_TARGET_ALL, type);
		} else{
			query = prepareQuery(query);
			queryPath = String.format(RepoConsts.QUERY_SUGGEST_TARGET_BY_NAME, type, query);
		}
    	
    	// filter by classNames
    	queryPath = filterByClass(queryPath, arrClassNames);
    	
		logger.debug("repository : " + queryPath);

		List<NodeRef> ret = beCPGSearchService.suggestSearch(queryPath, getSort(ContentModel.PROP_NAME));
        
        return new ListValuePage(ret, pageNum, RepoConsts.SUGGEST_PAGE_SIZE, new NodeRefListValueExtractor(ContentModel.PROP_NAME,nodeService));
       
	}
    




	/**
     * Suggest linked value according to query
     * 
     * Query path :
     * +PATH:"/app:company_home/cm:System/cm:LinkedLists/cm:Hierarchy/cm:Hierarchy1_Hierarchy2*" +TYPE:"bcpg:LinkedValue" +@cm\:lkvPrevValue:"hierar*".
     *
     * @param path the path
     * @param parent the parent
     * @param query the query
     * @return the map
     */
    
	public ListValuePage suggestLinkedValue(String path, String parent, String query, Integer pageNum){			
        
    	logger.debug("suggestLinkedValue");  
    	
    	String queryPath = "";
    	path = encodePath(path);    
		if(!isAllQuery(query)){ 
	    	query = prepareQuery(query);    	    	
	        queryPath = String.format(RepoConsts.PATH_QUERY_SUGGEST_LKV_VALUE, path, parent, query);    			
		} else {
			queryPath = String.format(RepoConsts.PATH_QUERY_SUGGEST_LKV_VALUE_ALL, path, parent);    
		}
    	
        List<NodeRef> ret = beCPGSearchService.suggestSearch(queryPath, getSort(BeCPGModel.PROP_LINKED_VALUE_VALUE));
        
        return new ListValuePage(ret, pageNum, RepoConsts.SUGGEST_PAGE_SIZE, new NodeRefListValueExtractor(BeCPGModel.PROP_LINKED_VALUE_VALUE,nodeService));
 
	}
    
    /**
     * Suggest list value according to query
     * 
     * Query path :
     * +PATH:"/app:company_home/cm:System/cm:Lists/cm:Nuts/*" +TYPE:"bcpg:nut" +@cm\:name:"Nut1*".
     *
     * @param path the path
     * @param query the query
     * @return the map
     */
    
	public ListValuePage suggestListValue(String path, String query, Integer pageNum){			
        
    	logger.debug("suggestListValue");  
    	
    	String queryPath = "";
    	path = encodePath(path);    
		if(!isAllQuery(query)){ 
	    	query = prepareQuery(query);
	        queryPath = String.format(RepoConsts.PATH_QUERY_SUGGEST_VALUE, path, query);
		} else {
	    	 queryPath = String.format(RepoConsts.PATH_QUERY_SUGGEST_VALUE_ALL, path);
		}
    	
        List<NodeRef> ret = beCPGSearchService.suggestSearch(queryPath, getSort(ContentModel.PROP_NAME));
       
        return new ListValuePage(ret, pageNum, RepoConsts.SUGGEST_PAGE_SIZE, new NodeRefListValueExtractor(ContentModel.PROP_NAME,nodeService));
      
	}
    
    /**
     * Suggest a product according to query
     * 
     * Query path :
     * +PATH:"/app:company_home//*" +TYPE:"bcpg:product"  +@cm\:name:"f* -@cm\:productState:Archived -@cm\:productState:Refused"
     * +PATH:"/app:company_home//*" +TYPE:"bcpg:product"  +@cm\:code:"f* -@cm\:productState:Archived -@cm\:productState:Refused".
     *
     * @param query the query
     * @return the map
     */
    
	public ListValuePage suggestProduct(String query, Integer pageNum, String[] arrClassNames){			
        
    	logger.debug("suggestProduct");  
    	String queryPath = "";    	
    	
		//Is code or name search, test if query is an interger ?
		if(isQueryCode(query, BeCPGModel.TYPE_PRODUCT)){
			query = prepareQueryCode(query,BeCPGModel.TYPE_PRODUCT);
			queryPath += String.format(RepoConsts.QUERY_SUGGEST_PRODUCT_BY_CODE, query, SystemState.Archived, SystemState.Refused);
		} else if(isAllQuery(query)){
			queryPath += String.format(RepoConsts.QUERY_SUGGEST_PRODUCT_ALL, SystemState.Archived, SystemState.Refused);
		}
		else{
			query = prepareQuery(query);
			queryPath += String.format(RepoConsts.QUERY_SUGGEST_PRODUCT_BY_NAME, query, SystemState.Archived, SystemState.Refused);
		}
		
		// filter by classNames
		queryPath = filterByClass(queryPath, arrClassNames);
					
		List<NodeRef> ret = beCPGSearchService.suggestSearch(queryPath, getSort(ContentModel.PROP_NAME));
	       
		 return new ListValuePage(ret, pageNum, RepoConsts.SUGGEST_PAGE_SIZE, new NodeRefListValueExtractor(ContentModel.PROP_NAME,nodeService));
	  
	}
    
    private String prepareQueryCode(String query, QName type) {
    	if(Pattern.matches(RepoConsts.REGEX_NON_NEGATIVE_INTEGER_FIELD,query)){
    		
    		if(BeCPGModel.TYPE_PRODUCT.equals(type)){
    			StringBuffer ret = new StringBuffer();
    			for(QName subType : dictionaryService.getSubTypes(type, true)){
    				if(ret.length()>0){
    					ret.append(" OR ");
    				}
    				ret.append( autoNumService.getPrefixedCode(subType, BeCPGModel.PROP_CODE,Long.parseLong(query)));
    			}
    			return "("+ret.toString()+")";
    		} else {
    		
    			return autoNumService.getPrefixedCode(type, BeCPGModel.PROP_CODE,Long.parseLong(query));
    		}
		}
		return query;
	}

	private boolean isQueryCode(String query, QName type) {
    	return Pattern.matches(RepoConsts.REGEX_NON_NEGATIVE_INTEGER_FIELD,query)
    			|| Pattern.matches(autoNumService.getAutoNumMatchPattern(type, BeCPGModel.PROP_CODE),query);
	}
	
	

	/**
     * Get the nodeRef of the item by type and name.
     *
     * @param type the type
     * @param name the name
     * @return the item by type and name
     */
	
	public NodeRef getItemByTypeAndName(QName type, String name){
		
    	String queryPath = String.format(RepoConsts.QUERY_CHARACT_BY_TYPE_AND_NAME, type, name);
					
		List<NodeRef> nodes = beCPGSearchService.unProtLuceneSearch(queryPath, null, RepoConsts.MAX_RESULTS_SINGLE_VALUE);
		
		if(nodes.size()>0){			
			return nodes.get(0);
		}
		
		return null;
	
	}
	
    /**
     * Encode path.
     *
     * @param path the path
     * @return the string
     */
    private String encodePath(String path){
    	
    	StringBuilder pathBuffer = new StringBuilder(64);
    	String[] arrPath = path.split(RepoConsts.PATH_SEPARATOR);
    	
    	for(String folder : arrPath){
    		pathBuffer.append("/cm:");
    		pathBuffer.append(ISO9075.encode(folder));    		 
    	}
    	
    	//remove 1st character '/'
    	return pathBuffer.substring(1);
    }
	
	/**
	 * Get the report templates of the product type that user can choose from UI.
	 *
	 * @param systemProductType the system product type
	 * @param query the query
	 * @return the map
	 */
	
	public ListValuePage suggestProductReportTemplates(QName nodeType, String query, Integer pageNum) {
		
		query = prepareQuery(query);
		List<NodeRef> tplsNodeRef = reportTplService.suggestUserReportTemplates(ReportType.Document, nodeType, query);		
		
		 return new ListValuePage(tplsNodeRef, pageNum, RepoConsts.SUGGEST_PAGE_SIZE, new NodeRefListValueExtractor(ContentModel.PROP_NAME,nodeService));
	}
	
	/**
	 * Prepare query.
	 * //TODO escape + - && || ! ( ) { } [ ] ^ " ~ * ? : \
	 * @param query the query
	 * @return the string
	 * @throws IOException 
	 */
	private String prepareQuery(String query){
		
		logger.debug("Query before prepare:"+query);
		if(query!=null && !(query.endsWith(SUFFIX_ALL) || query.endsWith(SUFFIX_SPACE) || query.endsWith(SUFFIX_DOUBLE_QUOTE) || query.endsWith(SUFFIX_SIMPLE_QUOTE))){
			//Query with wildcard are not getting analyzed by stemmers
			// so do it manually
			Analyzer analyzer  = getTextAnalyzer();
		
			if(logger.isDebugEnabled()){
				logger.debug("Using analyzer : "+analyzer.getClass().getName());
			}
			TokenStream source = null;
			Reader reader = null;
			try {
				
				reader =  new StringReader(query);
			
				source = analyzer.tokenStream(null,reader);
				
					StringBuffer buff = new StringBuffer();
					Token reusableToken = new Token(); 
					while((reusableToken = source.next(reusableToken))!=null){
						if(buff.length()>0){
							buff.append(' ');
						}
						buff.append(reusableToken.term());
					}
				source.reset();
				
				
				buff.append(SUFFIX_ALL);
				query =  buff.toString();
			} catch (Exception e) {
				logger.error(e,e);
			} finally {
			
					try {
						if(source!=null){
							source.close();
						}
					
						
					} catch (IOException e) {
						//Nothing todo here
						logger.error(e,e);
					}
	
			
			}
			
		}
		
		logger.debug("Query after prepare:"+query);
		
		return query;
	}
	

    private Analyzer getTextAnalyzer()  {
    	if(luceneAnaLyzer==null){
    		DataTypeDefinition def  = dictionaryDAO.getDataType(DataTypeDefinition.TEXT);
        	
    		try {								//def.getAnalyserName()
				return  (Analyzer) Class.forName(def.getAnalyserResourceBundleName()).newInstance();
			} catch (Exception e) {
				logger.error(e,e);
				return  new fr.becpg.repo.search.lucene.analysis.FrenchSnowballAnalyserThatRemovesAccents();
			}
    	}
    	return luceneAnaLyzer;
	}


	private Map<String, Boolean> getSort(QName field){
		
		Map<String, Boolean> sort = new HashMap<String, Boolean>();
		sort.put("@" + field, true);
		
		return sort;
	}
	
	private String filterByClass(String query, String [] arrClassNames){
		
		if(arrClassNames != null){
			
			String queryClassNames = "";
			
			for(String className : arrClassNames){
				
				if(queryClassNames.isEmpty()){
					queryClassNames += String.format(QUERY_TYPE, className);
				}
				else{
					queryClassNames += " OR " + String.format(QUERY_TYPE, className);
				}
			}
			
			query += " AND (" + queryClassNames + ")";
		}

		return query;
	}



	private boolean isAllQuery(String query) {
		return query!=null && query.trim().equals(SUFFIX_ALL);
	}
	
}
