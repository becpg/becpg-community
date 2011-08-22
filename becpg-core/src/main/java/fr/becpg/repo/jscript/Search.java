package fr.becpg.repo.jscript;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.jscript.ScriptNode.NodeValueConverter;
import org.alfresco.repo.jscript.ValueConverter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
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
	public Scriptable queryAdvSearch(String datatype, 
			String term, String tag, Object criteria, String sort, 
			boolean isRepo, String siteId, String containerId){

		logger.debug("Start Scriptable queryAdvSearch");
		
		logger.debug("queryAdvSearch, criteria: " + criteria);		
		
		Collection<ScriptNode> set = null;        		
		Map<String, String> criteriaMap = null;
		QName datatypeQName = (datatype != null && !datatype.isEmpty()) ? QName.createQName(datatype, services.getNamespaceService()) : null;
		
		if (criteria instanceof ScriptableObject){
			
			criteriaMap = new HashMap<String, String>(4, 1.0f);
            extractAttributes((ScriptableObject)criteria, criteriaMap);
        }
		
		logger.debug("queryAdvSearch, criteriaMap: " + criteriaMap);
		
        List<NodeRef> nodes = advSearchService.queryAdvSearch(datatypeQName, term, tag, criteriaMap, sort, isRepo, siteId, containerId);
        
        logger.debug("queryAdvSearch, nodes" + nodes);
        
        if(!nodes.isEmpty()){
        	
        	set = new LinkedHashSet<ScriptNode>(nodes.size(), 1.0f);
        	for (NodeRef node : nodes)
            {
                set.add(new ScriptNode(node, this.services, getScope()));
            }
        }
        
        Object[] results = set != null ? set.toArray(new Object[(set.size())]) : new Object[0];
        
        logger.debug("queryAdvSearch, results" + results);
        
        return Context.getCurrentContext().newArray(getScope(), results);
	}
	
	/**
     * Extract a map of properties and associations from a scriptable object (generally an associative array)
     * 
     * @param scriptable    The scriptable object to extract name/value pairs from.
     * @param map           The map to add the converted name/value pairs to.
     */
    private void extractAttributes(ScriptableObject scriptable, Map<String, String> map)
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
    
}
