/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.listvalue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.MLAnalysisMode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.common.RepoConsts;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemProductType;
import fr.becpg.model.SystemState;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;

// TODO: Auto-generated Javadoc
/**
 * The Class ListValueServiceImpl.
 *
 * @author Quere
 */
public class ListValueServiceImpl implements ListValueService {
	
	/** The Constant MAX_SUGGESTIONS. */
	private static final int MAX_SUGGESTIONS = 10;
	
	/** The Constant MAX_RESULT_ITEM. */
	private static final int MAX_RESULT_ITEM = 1;
	
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
	
	/** The search service. */
	private SearchService searchService;
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The product report service. */
	private ReportTplService reportTplService;
				
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
			
	public void setReportTplService(ReportTplService reportTplService) {
		this.reportTplService = reportTplService;
	}

	/**
	 * Get allowed values according to path, type and property (Look in every site).
	 *
	 * @param path the path
	 * @param constraintType the constraint type
	 * @param constraintProp the constraint prop
	 * @return the allowed values
	 */	
	@Override
	public List<String> getAllowedValues(String path, QName constraintType, QName constraintProp) {
		
		List<String> allowedValues = new ArrayList<String>();						
		
		path = encodePath(path);    			
		
		String queryPath = String.format(RepoConsts.PATH_QUERY_LIST_CONSTRAINTS, path, constraintType);
		//logger.debug("queryPath : " + queryPath);
		ResultSet resultSet = null;
		
		try{
			resultSet = searchService.query(RepoConsts.SPACES_STORE, SearchService.LANGUAGE_LUCENE, queryPath);
	        
			//logger.debug("resultSet.length() : " + resultSet.length());
			
	        if (resultSet.length() != 0)
	        {
	            for (ResultSetRow row : resultSet)
	            {
	                NodeRef nodeRef = row.getNodeRef();
	                String value = (String)nodeService.getProperty(nodeRef, constraintProp);
	                if(!allowedValues.contains(value)){
	                	allowedValues.add(value);
	                }
	            }                   	
	        }   
	        
//	        logger.debug("allowedValues.size() : " + allowedValues.size());
//	        logger.debug("allowed values: " + allowedValues.toString());
	        		
			return allowedValues;
		}
		finally{
			if(resultSet != null)
				resultSet.close();
		}
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
	public Map<String, String> suggestTargetAssoc(QName type, String query){			
        
    	logger.debug("suggestTargetAssoc");
    	Map<String, String> suggestions = new HashMap<String, String>();
    	
    	String queryPath = "";
    	
    	//Is code or name search, test if query is an interger ?
		if(Pattern.matches(RepoConsts.REGEX_NON_NEGATIVE_INTEGER_FIELD, query)){
			queryPath = String.format(RepoConsts.PATH_QUERY_SUGGEST_TARGET_BY_CODE, type, query);
		}
		else{
			query = prepareQuery(query);
			queryPath = String.format(RepoConsts.PATH_QUERY_SUGGEST_TARGET_BY_NAME, type, query);
		}
    	
		logger.debug("repository : " + queryPath);
		
		SearchParameters sp = new SearchParameters();
		//sp.addLocale(repoConfig.getSystemLocale());
        sp.addStore(RepoConsts.SPACES_STORE);
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(queryPath);	        
        sp.setLimitBy(LimitBy.FINAL_SIZE);
        sp.setLimit(MAX_SUGGESTIONS);        
        sp.setMaxItems(MAX_SUGGESTIONS);
        
        ResultSet resultSet = null;
        
        try{
	        resultSet = searchService.query(sp);
	        
	        logger.debug("resultSet.length() : " + resultSet.length());
	        
	        if (resultSet.length() != 0)
	        {
	            suggestions = new HashMap<String, String>(resultSet.length());
	            for (ResultSetRow row : resultSet)
	            {
	                NodeRef nodeRef = row.getNodeRef();
	                String name = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
	                suggestions.put(nodeRef.toString(), name);
	            }                   	
	        }
	        else{
	        	suggestions = new HashMap<String, String>();
	        }
	        
	        return suggestions;
        }
        finally{
        	if(resultSet != null)
        		resultSet.close();
        }
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
	public Map<String, String> suggestLinkedValue(String path, String parent, String query){			
        
    	logger.debug("suggestLinkedValue");  
    	Map<String, String> suggestions = new HashMap<String, String>();
    	
    	path = encodePath(path);    	
    	query = prepareQuery(query);    	    	
    	String queryPath = String.format(RepoConsts.PATH_QUERY_SUGGEST_LKV_VALUE, path, parent, query);    			
		logger.debug("repository : " + queryPath);
		
		SearchParameters sp = new SearchParameters();
		//sp.addLocale(repoConfig.getSystemLocale());
		//sp.addLocale(Locale.FRENCH);
        sp.addStore(RepoConsts.SPACES_STORE);
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(queryPath);	        
        sp.setLimitBy(LimitBy.FINAL_SIZE);
        sp.setLimit(MAX_SUGGESTIONS);
        sp.setMaxItems(MAX_SUGGESTIONS);
        
        ResultSet resultSet = null;
        
        try{
	        resultSet = searchService.query(sp);
	        
	        logger.debug("resultSet.length() : " + resultSet.length());
	        
	        if (resultSet.length() != 0)
	        {
	            suggestions = new HashMap<String, String>(resultSet.length());
	            for (ResultSetRow row : resultSet)
	            {
	                NodeRef nodeRef = row.getNodeRef();                
	                String value = (String)nodeService.getProperty(nodeRef, BeCPGModel.PROP_LINKED_VALUE_VALUE);
	                suggestions.put(value, value);
	            }                   	
	        }
	        else{
	        	suggestions = new HashMap<String, String>();
	        }
	        
	        return suggestions;
        }
        finally{
        	if(resultSet != null){
        		resultSet.close();
        	}
        }
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
	public Map<String, String> suggestListValue(String path, String query){			
        
    	logger.debug("suggestListValue");  
    	Map<String, String> suggestions = new HashMap<String, String>();
    	
    	path = encodePath(path);    	
    	query = prepareQuery(query);
    	String queryPath = String.format(RepoConsts.PATH_QUERY_SUGGEST_VALUE, path, query);
		logger.debug("repository : " + queryPath);
		
		SearchParameters sp = new SearchParameters();
		//sp.addLocale(repoConfig.getSystemLocale());
		//sp.addLocale(Locale.FRENCH);
        sp.addStore(RepoConsts.SPACES_STORE);
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(queryPath);	             
        sp.setLimitBy(LimitBy.FINAL_SIZE);
        sp.setLimit(MAX_SUGGESTIONS);
        sp.setMaxItems(MAX_SUGGESTIONS);
        
        sp.addSort("@" + ContentModel.PROP_NAME, true);
        sp.setMlAnalaysisMode(MLAnalysisMode.ALL_ONLY);
        //sp.setBulkFetch(false);
        sp.excludeDataInTheCurrentTransaction(false);        
        
        ResultSet resultSet = null;
        
        try{
	        resultSet = searchService.query(sp);
	        
	        logger.debug("resultSet.length() : " + resultSet.length());
	        
	        if (resultSet.length() != 0)
	        {
	            suggestions = new HashMap<String, String>(resultSet.length());
	            for (ResultSetRow row : resultSet)
	            {
	                NodeRef nodeRef = row.getNodeRef();
	                String name = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
	                suggestions.put(name, name);
	            }                   	
	        }
	        else{
	        	suggestions = new HashMap<String, String>();
	        }
	        
	        return suggestions;
        }
        finally{
        	if(resultSet != null)
        		resultSet.close();
        }
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
	public Map<String, String> suggestProduct(String query){			
        
    	logger.debug("suggestProduct");  
    	Map<String, String> suggestions = new HashMap<String, String>();
    	   	
    	String queryPath = "";    	
    	
		//Is code or name search, test if query is an interger ?
		if(Pattern.matches(RepoConsts.REGEX_NON_NEGATIVE_INTEGER_FIELD, query)){
			queryPath += String.format(RepoConsts.PATH_QUERY_SUGGEST_PRODUCT_BY_CODE, query, SystemState.Archived, SystemState.Refused);
		}
		else{
			query = prepareQuery(query);
			queryPath += String.format(RepoConsts.PATH_QUERY_SUGGEST_PRODUCT_BY_NAME, query, SystemState.Archived, SystemState.Refused);
		}
			
		logger.debug("queryPath : " + queryPath);
		
		SearchParameters sp = new SearchParameters();
		//sp.addLocale(repoConfig.getSystemLocale());
		//sp.addLocale(Locale.FRENCH);
        sp.addStore(RepoConsts.SPACES_STORE);
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(queryPath);	        
        sp.setLimitBy(LimitBy.FINAL_SIZE);
        sp.setLimit(MAX_SUGGESTIONS);
        sp.setMaxItems(MAX_SUGGESTIONS);
        
        ResultSet resultSet = null;
        
        try{
	        resultSet = searchService.query(sp);
	        
	        logger.debug("resultSet.length() : " + resultSet.length());
	        
	        if (resultSet.length() != 0)
	        {
	            suggestions = new HashMap<String, String>(resultSet.length());
	            for (ResultSetRow row : resultSet)
	            {
	                NodeRef nodeRef = row.getNodeRef();
	                String name = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
	                suggestions.put(nodeRef.toString(), name);
	            }                   	
	        }
	        
	        return suggestions;
        }
        finally{
        	if(resultSet != null)
        		resultSet.close();
        }
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
		
		NodeRef charactNodeRef = null;  
    	
    	String queryPath = String.format(RepoConsts.PATH_QUERY_CHARACT_BY_TYPE_AND_NAME, type, name);
					
		logger.debug(queryPath);
		
		SearchParameters sp = new SearchParameters();
		//sp.addLocale(repoConfig.getSystemLocale());
        sp.addStore(RepoConsts.SPACES_STORE);
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(queryPath.toString());	        
        sp.setLimitBy(LimitBy.FINAL_SIZE);
        sp.setLimit(MAX_RESULT_ITEM);
        
        ResultSet resultSet =null;
        
        try{
	        resultSet = searchService.query(sp);
			
	        logger.debug("resultSet.length() : " + resultSet.length());
	        if (resultSet.length() != 0){
	        	charactNodeRef = resultSet.getNodeRef(0); 
	        }
	        
			return charactNodeRef;
        }
        finally{
        	if(resultSet != null)
        		resultSet.close();
        }
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
	 */
	private String prepareQuery(String query){
		
		if(!(query.endsWith(SUFFIX_ALL) || query.endsWith(SUFFIX_SPACE) || query.endsWith(SUFFIX_DOUBLE_QUOTE) || query.endsWith(SUFFIX_SIMPLE_QUOTE))){
			query += SUFFIX_ALL;
		}
		
		return query;
	}

}
