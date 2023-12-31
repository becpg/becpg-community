/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
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
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import fr.becpg.repo.search.AdvSearchService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>Search class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public final class Search extends BaseScopableProcessorExtension{

	private static final Log logger = LogFactory.getLog(Search.class);
	
    protected ServiceRegistry services;
    
	private AdvSearchService advSearchService;
	
	/**
	 * <p>setServiceRegistry.</p>
	 *
	 * @param serviceRegistry a {@link org.alfresco.service.ServiceRegistry} object.
	 */
	public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
    	this.services = serviceRegistry;
    }
		
	/**
	 * <p>Setter for the field <code>advSearchService</code>.</p>
	 *
	 * @param advSearchService a {@link fr.becpg.repo.search.AdvSearchService} object.
	 */
	public void setAdvSearchService(AdvSearchService advSearchService) {
		this.advSearchService = advSearchService;
	}

	

	/**
	 * <p>queryAdvSearch.</p>
	 *
	 * @param datatype a {@link java.lang.String} object.
	 * @param term a {@link java.lang.String} object.
	 * @param tag a {@link java.lang.String} object.
	 * @param criteria a {@link java.lang.Object} object.
	 * @param isRepo a boolean.
	 * @param siteId a {@link java.lang.String} object.
	 * @param containerId a {@link java.lang.String} object.
	 * @param sort a {@link java.lang.Object} object.
	 * @param maxResults a int.
	 * @return a {@link org.mozilla.javascript.Scriptable} object.
	 */
	public Scriptable queryAdvSearch(String datatype, 
			String term, String tag, Object criteria, 
			boolean isRepo, String siteId, String containerId, Object sort,int maxResults){
		return queryAdvSearch(null, datatype, term, tag, criteria, isRepo, siteId, containerId, sort, maxResults);
		
	}
	

	/**
	 * <p>queryAdvSearch.</p>
	 *
	 * @param query a {@link java.lang.String} object.
	 * @param datatype a {@link java.lang.String} object.
	 * @param criteria a {@link java.lang.Object} object.
	 * @param sort a {@link java.lang.Object} object.
	 * @param maxResults a int.
	 * @return a {@link org.mozilla.javascript.Scriptable} object.
	 */
	public Scriptable queryAdvSearch(String query, String datatype, Object criteria,  Object sort, int maxResults){
		return queryAdvSearch(query, datatype, null, null, criteria, true, null, null, sort, maxResults);
		
	}
	

	/**
	 * Method a do the query for the advanced search
	 *
	 * @param query a {@link java.lang.String} object.
	 * @param datatype a {@link java.lang.String} object.
	 * @param term a {@link java.lang.String} object.
	 * @param tag a {@link java.lang.String} object.
	 * @param criteria a {@link java.lang.Object} object.
	 * @param isRepo a boolean.
	 * @param siteId a {@link java.lang.String} object.
	 * @param containerId a {@link java.lang.String} object.
	 * @param sort a {@link java.lang.Object} object.
	 * @param maxResults a int.
	 * @return a {@link org.mozilla.javascript.Scriptable} object.
	 */
	public Scriptable queryAdvSearch(String query, String datatype, 
			String term, String tag, Object criteria,  
			boolean isRepo, String siteId, String containerId, Object sort, int maxResults){


		
		Collection<ScriptNode> set = null;        		
		Map<String, String> criteriaMap = null;
		Map<String, Boolean> sortMap = null;
		QName datatypeQName = (datatype != null && !datatype.isEmpty()) ? QName.createQName(datatype, services.getNamespaceService()) : null;
		
		if (criteria instanceof ScriptableObject){
			
			criteriaMap = new HashMap<>(4, 1.0f);
            extractCriteriaAttributes((ScriptableObject)criteria, criteriaMap);
        }
		
		if (sort instanceof ScriptableObject){
			
			sortMap = new HashMap<>();
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
		
		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().andFTSQuery(query);
		
		if(query==null){
			queryBuilder = advSearchService.createSearchQuery(datatypeQName, term, tag, isRepo, siteId, containerId);
		}
		
		queryBuilder.addSort(sortMap);
		
        List<NodeRef> nodes =advSearchService.queryAdvSearch(datatypeQName, queryBuilder, criteriaMap, maxResults);

        
        if(!nodes.isEmpty()){
        	
        	set = new LinkedHashSet<>(nodes.size(), 1.0f);
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
		for (Object propId : propIds) {
			// work on each key in turn
			// we are only interested in keys that are formed of Strings i.e. QName.toString()
			if (propId instanceof String) {
				// get the value out for the specified key - it must be Serializable
				String key = (String) propId;
				Object value = scriptable.get(key, scriptable);
				if (value instanceof String) {
					map.put(key, (String) value);
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
		for (Object propId : propIds) {
			// work on each key in turn
			if (propId instanceof Integer) {
				ScriptableObject array = (ScriptableObject) scriptable.get(0, scriptable);
				String column = (String) array.get("column", scriptable);
				Boolean asc = (Boolean) array.get("ascending", scriptable);
				map.put(column, asc);
			}

		}
    }
    
}
