/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.listvalue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

import fr.becpg.common.RepoConsts;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.entity.AutoNumService;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.repo.search.BeCPGSearchService;

// TODO: Auto-generated Javadoc
/**
 * The Class ListValueServiceImpl.
 *
 * @author Quere, Laborie
 */
public class ListValueServiceImpl implements ListValueService {
	
	/** The Constant SUFFIX_ALL. */
	private static final String SUFFIX_ALL = "*";
	
	/** The Constant SUFFIX_SPACE. */
	private static final String SUFFIX_SPACE = " ";
	
	/** The Constant SUFFIX_DOUBLE_QUOTE. */
	private static final String SUFFIX_DOUBLE_QUOTE = "\"";
	
	/** The Constant SUFFIX_SIMPLE_QUOTE. */
	private static final String SUFFIX_SIMPLE_QUOTE = "'";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ListValueServiceImpl.class);
	

	/** The node service. */
	private NodeService nodeService;
	
	/** The product report service. */
	private ReportTplService reportTplService;
	
	
	private BeCPGSearchService beCPGSearchService;
	
	private DictionaryService dictionaryService;
	
	private DictionaryDAO dictionaryDAO;
	
	private AutoNumService autoNumService;
	
	private Analyzer luceneAnaLyzer = null;
	
	
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
    @Override
	public Map<String, String> suggestTargetAssoc(QName type, String query, Locale locale){			
        
    	logger.debug("suggestTargetAssoc");
    	
    	String queryPath = "";
    	
    	//Is code or name search
    	if(isQueryCode(query,type)){
			query = prepareQueryCode(query,type);
			queryPath = String.format(RepoConsts.QUERY_SUGGEST_TARGET_BY_CODE, type, query);
		}
		else{
			query = prepareQuery(query);
			queryPath = String.format(RepoConsts.QUERY_SUGGEST_TARGET_BY_NAME, type, query);
		}
    	
		logger.debug("repository : " + queryPath);

       List<NodeRef> ret = beCPGSearchService.suggestSearch(queryPath, new String[]{"@" + ContentModel.PROP_NAME},locale);
        
        return extractSuggest(ret,ContentModel.PROP_NAME);
       
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
    @Override
	public Map<String, String> suggestLinkedValue(String path, String parent, String query, Locale locale){			
        
    	logger.debug("suggestLinkedValue");  
    	
    	path = encodePath(path);    	
    	query = prepareQuery(query);    	    	
    	String queryPath = String.format(RepoConsts.PATH_QUERY_SUGGEST_LKV_VALUE, path, parent, query);    			
	      
         List<NodeRef> ret = beCPGSearchService.suggestSearch(queryPath, new String[]{"@" + RepoConsts.PATH_QUERY_SUGGEST_LKV_VALUE},locale);
        
        return extractSuggest(ret, BeCPGModel.PROP_LINKED_VALUE_VALUE);
 
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
    @Override
	public Map<String, String> suggestListValue(String path, String query, Locale locale){			
        
    	logger.debug("suggestListValue");  
    	
    	path = encodePath(path);    	
    	query = prepareQuery(query);
    	String queryPath = String.format(RepoConsts.PATH_QUERY_SUGGEST_VALUE, path, query);
	
        List<NodeRef> ret = beCPGSearchService.suggestSearch(queryPath, new String[]{"@" + ContentModel.PROP_NAME},locale);
       
       return extractSuggest(ret, ContentModel.PROP_NAME);
      
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
    @Override
	public Map<String, String> suggestProduct(String query, Locale locale){			
        
    	logger.debug("suggestProduct");  
    	String queryPath = "";    	
    	
		//Is code or name search, test if query is an interger ?
		if(isQueryCode(query, BeCPGModel.TYPE_PRODUCT)){
			query = prepareQueryCode(query,BeCPGModel.TYPE_PRODUCT);
			queryPath += String.format(RepoConsts.QUERY_SUGGEST_PRODUCT_BY_CODE, query, SystemState.Archived, SystemState.Refused);
		}
		else{
			query = prepareQuery(query);
			queryPath += String.format(RepoConsts.QUERY_SUGGEST_PRODUCT_BY_NAME, query, SystemState.Archived, SystemState.Refused);
		}
					
		List<NodeRef> ret = beCPGSearchService.suggestSearch(queryPath, new String[]{"@" + ContentModel.PROP_NAME}, locale);
	       
	    return extractSuggest(ret, ContentModel.PROP_NAME);
	  
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
	@Override
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
	@Override
	public Map<String, String> suggestProductReportTemplates(QName nodeType, String query) {
		
		Map<String, String> suggestions = new HashMap<String, String>();
		
		query = prepareQuery(query);
		List<NodeRef> tplsNodeRef = reportTplService.suggestUserReportTemplates(ReportType.Document, nodeType, query);
		
		for(NodeRef tplNodeRef : tplsNodeRef){
			String name = (String)nodeService.getProperty(tplNodeRef, ContentModel.PROP_NAME);
            suggestions.put(tplNodeRef.toString(), name);
		}
		
		return suggestions;
	}
	
	/**
	 * Prepare query.
	 *
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
        	
    		try {
				return  (Analyzer) Class.forName(def.getAnalyserClassName()).newInstance();
			} catch (Exception e) {
				logger.error(e,e);
				return  new fr.becpg.repo.search.lucene.analysis.FrenchSnowballAnalyserThatRemovesAccents();
			}
    	}
    	return luceneAnaLyzer;
	}

	private Map<String, String> extractSuggest(List<NodeRef> nodeRefs, QName propName) {
    	Map<String, String> suggestions = new HashMap<String, String>();
    	if(nodeRefs!=null){
    		for(NodeRef nodeRef : nodeRefs){
    			
    			String name = (String)nodeService.getProperty(nodeRef, propName);
                suggestions.put(nodeRef.toString(), name); 			
    		}
    	}
		return suggestions;
	}

}
