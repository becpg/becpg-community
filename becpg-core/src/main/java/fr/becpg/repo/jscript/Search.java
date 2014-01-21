/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.jscript;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import fr.becpg.repo.search.AdvSearchService;

public final class Search extends BaseScopableProcessorExtension{

	private static Log logger = LogFactory.getLog(Search.class);
	
    protected ServiceRegistry services;
    
	private AdvSearchService advSearchService;
	
	public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
    	this.services = serviceRegistry;
    }
		
	public void setAdvSearchService(AdvSearchService advSearchService) {
		this.advSearchService = advSearchService;
	}

	
	/**
	 * 
	 * @param datatype
	 * @param term
	 * @param tag
	 * @param criteria
	 * @param isRepo
	 * @param siteId
	 * @param containerId
	 * @param sort
	 * @param maxResults
	 * @return
	 */
	public Scriptable queryAdvSearch(String datatype, 
			String term, String tag, Object criteria, 
			boolean isRepo, String siteId, String containerId, Object sort,int maxResults){
		return queryAdvSearch(null, datatype, term, tag, criteria, isRepo, siteId, containerId, sort, maxResults);
		
	}
	
	/**
	 * 
	 * @param query
	 * @param criteria
	 * @param sort
	 * @param maxResults
	 * @return
	 */
	public Scriptable queryAdvSearch(String query, String datatype, Object criteria,  Object sort, int maxResults){
		return queryAdvSearch(query, datatype, null, null, criteria, true, null, null, sort, maxResults);
		
	}
	

	/**
	 * Method a do the query for the advanced search
	 * @param datatype
	 * @param term
	 * @param tag
	 * @param criteria
	 * @param sort
	 * @param isRepo
	 * @param siteId
	 * @param containerId
	 * @return
	 */
	public Scriptable queryAdvSearch(String query, String datatype, 
			String term, String tag, Object criteria,  
			boolean isRepo, String siteId, String containerId, Object sort, int maxResults){


		
		Collection<ScriptNode> set = null;        		
		Map<String, String> criteriaMap = null;
		Map<String, Boolean> sortMap = null;
		QName datatypeQName = (datatype != null && !datatype.isEmpty()) ? QName.createQName(datatype, services.getNamespaceService()) : null;
		
		if (criteria instanceof ScriptableObject){
			
			criteriaMap = new HashMap<String, String>(4, 1.0f);
            extractCriteriaAttributes((ScriptableObject)criteria, criteriaMap);
        }
		
		if (sort instanceof ScriptableObject){
			
			sortMap = new HashMap<String, Boolean>();
			extractSortAttributes((ScriptableObject)sort, sortMap);
        }
		
		if(logger.isDebugEnabled()){
			logger.debug("Start Scriptable queryAdvSearch");
			if( datatypeQName!=null){
				logger.debug("Filter on dataType: "+datatypeQName.toString());
			}

			logger.debug("queryAdvSearch, criteriaMap: " + criteriaMap);
			logger.debug("queryAdvSearch, sortMap: " + sortMap);
		}
		
		String language = SearchService.LANGUAGE_FTS_ALFRESCO;
		if(query==null){
			query = advSearchService.getSearchQueryByProperties(datatypeQName, term, tag, isRepo, siteId, containerId);
		} else {
			language = SearchService.LANGUAGE_LUCENE;
		}
		
        List<NodeRef> nodes = advSearchService.queryAdvSearch(query,language , datatypeQName , criteriaMap, sortMap, maxResults);     
        
        if(!nodes.isEmpty()){
        	
        	set = new LinkedHashSet<ScriptNode>(nodes.size(), 1.0f);
        	for (NodeRef node : nodes)
            {
                set.add(new ScriptNode(node, this.services, getScope()));
            }
        }
        
        Object[] results = set != null ? set.toArray(new Object[(set.size())]) : new Object[0];
        
        logger.debug("queryAdvSearch, results: " + results.length);
        
        return Context.getCurrentContext().newArray(getScope(), results);
	}
	
	/**
     * Extract a map of properties and associations from a scriptable object (generally an associative array)
     * 
     * @param scriptable    The scriptable object to extract name/value pairs from.
     * @param map           The map to add the converted name/value pairs to.
     */
    private void extractCriteriaAttributes(ScriptableObject scriptable, Map<String, String> map)
    {
        // we need to get all the keys to the properties provided
        // and convert them to a Map of QName to Serializable objects
        Object[] propIds = scriptable.getIds();
        for (int i = 0; i < propIds.length; i++)
        {
            // work on each key in turn
            Object propId = propIds[i];
            
            // we are only interested in keys that are formed of Strings i.e. QName.toString()
            if (propId instanceof String)
            {
                // get the value out for the specified key - it must be Serializable
                String key = (String)propId;
                Object value = scriptable.get(key, scriptable);
                if (value instanceof String)
                {
                    map.put(key, (String)value);
                }
            }
        }
    }
    
	/**
     * Extract a map of properties and associations from a scriptable object (generally an associative array)
     * 
     * @param scriptable    The scriptable object to extract name/value pairs from.
     * @param map           The map to add the converted name/value pairs to.
     */
    private void extractSortAttributes(ScriptableObject scriptable, Map<String, Boolean> map)
    {

        Object[] propIds = scriptable.getIds();
        for (int i = 0; i < propIds.length; i++)
        {
            // work on each key in turn
            Object propId = propIds[i];
            if (propId instanceof Integer)
            {
            	ScriptableObject array = (ScriptableObject) scriptable.get(0, scriptable);
            	String column = (String) array.get("column", scriptable);
            	Boolean asc =  (Boolean) array.get("ascending", scriptable);
            	map.put(column, asc);
            } 
           
        }
    }
    
}
