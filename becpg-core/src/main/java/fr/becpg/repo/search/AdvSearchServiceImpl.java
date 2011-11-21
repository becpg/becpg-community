package fr.becpg.repo.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.search.impl.NodeSearcher;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.mozilla.javascript.Scriptable;

import fr.becpg.common.RepoConsts;
import fr.becpg.model.BeCPGModel;

/**
 * This class do a search on the repository (association, properties and productLists), for the UI so it respects rights.
 * @author querephi
 *
 */
public class AdvSearchServiceImpl implements AdvSearchService {

	/** The Constant SITES_SPACE_QNAME_PATH. */
	private static final String SITES_SPACE_QNAME_PATH = "/app:company_home/st:sites/";	
	
	/** The Constant PRODUCTS_TO_EXCLUDE. */
	private static final String PRODUCTS_TO_EXCLUDE = " AND -ASPECT:\"bcpg:compositeVersion\" AND -ASPECT:\"eco:simulationEntityAspect\" ";
	
	private static final String DEFAULT_FIELD_NAME = "keywords";
	
	private static final String QUERY_TEMPLATES = "%(cm:name cm:title cm:description ia:whatEvent ia:descriptionEvent lnk:title lnk:description TEXT)";
	
	private static final String CRITERIA_ING = "assoc_bcpg_ingListIng_added";
	
	private static final String CRITERIA_GEO_ORIGIN = "assoc_bcpg_ingListGeoOrigin_added";
	
	private static final String CRITERIA_BIO_ORIGIN = "assoc_bcpg_ingListBioOrigin_added";
	
	private static Log logger = LogFactory.getLog(AdvSearchServiceImpl.class);
		
	private SearchService searchService;
	private NodeService nodeService;
	private DictionaryService dictionaryService;
	private NamespaceService namespaceService;
		
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	@Override
	public List<NodeRef> queryAdvSearch(QName datatype, String term, String tag,
										Map<String, String> criteria, String sort, boolean isRepo,
										String siteId, String containerId) {
		
				
		String searchQuery = getSearchQueryByProperties(datatype, term, tag, criteria, sort, isRepo, siteId, containerId);
		List<NodeRef> nodes = getSearchNodes(searchQuery); 
		
		nodes = getSearchNodesByAssociations(nodes, criteria);
		
		if(datatype != null && dictionaryService.isSubClass(datatype, BeCPGModel.TYPE_PRODUCT)){
			
			nodes = getSearchNodesByIngListCriteria(nodes, criteria);
		}
		
		return nodes; 				
	}
	
	/**
	 * Parse the criteria and construct a FTS-ALFRESCO query.
	 *
	 * @param term the term
	 * @param tag the tag
	 * @param query the query
	 * @param sort the sort
	 * @param repo the repo
	 * @param siteId the site id
	 * @param containerId the container id
	 * @return the form query
	 */
	
	private String getSearchQueryByProperties(QName datatype, String term, String tag, Map<String, String> criteria, String sort, boolean isRepo, String siteId, String containerId){
		String formQuery = "";
		String ftsQuery = "";
		boolean first = true;				
		
		// Simple keyword search and tag specific search
		if (term != null && term.length() != 0){
			ftsQuery = "(" + term + ") PATH:\"/cm:taggable/cm:" + ISO9075.encode(term) + "/member\" ";
		}
		else if (tag != null && tag.length() != 0){
			ftsQuery = "PATH:\"/cm:taggable/cm:" + ISO9075.encode(tag) + "/member\" ";
		}
		
		// Advanced search form data search.
		// Supplied as json in the standard Alfresco Forms data structure:
		//    prop_<name>:value|assoc_<name>:value
		//    name = namespace_propertyname|pseudopropertyname
		//    value = string value - comma separated for multi-value, no escaping yet!
		// - underscore represents colon character in name
		// - pseudo property is one of any cm:content url property: mimetype|encoding|size
		// - always string values - interogate DD for type data	
		if(criteria != null && !criteria.isEmpty()){
			
			for(Map.Entry<String, String>criterion : criteria.entrySet()){
				
				String key = criterion.getKey();
				String propValue = criterion.getValue();
				
				if(!propValue.isEmpty()){
					// properties
					if(key.startsWith("prop_")){				
						
						// found a property - is it namespace_propertyname or pseudo property format?
						String propName = key.substring(5);
						if(propName.contains("_")){
							
							// property name - convert to DD property name format
							propName = propName.replace("_", ":");
							
							// special case for range packed properties
							if(propName.endsWith("-range")){
								
								// currently support text based ranges (usually numbers) or date ranges
			                    // range value is packed with a | character separator
			                     
			                    // if neither value is specified then there is no need to add the term
								if(!propValue.isEmpty()){
									
									String from = "", to = "";
									int sepindex = propValue.indexOf("|");
			                        if (propName.endsWith("-date-range"))
			                        {
			                           // date range found
			                           propName = propName.substring(0, propName.length() - "-date-range".length());
			                           
			                           // work out if "from" and/or "to" are specified - use MIN and MAX otherwise;
			                           // we only want the "YYYY-MM-DD" part of the ISO date value - so crop the strings
			                           from = (sepindex == 0 ? "MIN" : propValue.substring(0, 10));
			                           to = (sepindex == propValue.length() - 1 ? "MAX" : propValue.substring(sepindex + 1, sepindex + 11));			                           
			                        }
			                        else
			                        {
			                           // simple range found
			                           propName = propName.substring(0, propName.length() - "-range".length());
			                           
			                           // work out if "min" and/or "max" are specified - use MIN and MAX otherwise
			                           from = (sepindex == 0 ? "MIN" : propValue.substring(0, sepindex));
			                           to = (sepindex == propValue.length() - 1 ? "MAX" : propValue.substring(sepindex + 1));
			                        }
			                        formQuery += (first ? "" : " AND ") + propName + ":\"" + from + "\"..\"" + to + "\"";
								}
							}
							else{
								// beCPG - bug fix : pb with operator -, AND, OR
			                	// poivre AND -noir
								// poivre AND noir
								// sushi AND (saumon OR thon) AND -dorade
			                    //formQuery += (first ? "" : " AND ") + propName + ":\"" + propValue + "\"";
								formQuery += (first ? "" : " AND ") + propName + ":(" + propValue + ")";
			                  }
						}
						else{
							// pseudo cm:content property - e.g. mimetype, size or encoding
			                formQuery += (first ? "" : " AND ") + "cm:content." + propName + ":\"" + propValue + "\"";
						}					
						first = false;
					}
				}												
			}
			
			if (formQuery.length() != 0 || ftsQuery.length() != 0){
							
		         // extract data type for this search - advanced search query is type specific
		         ftsQuery = "TYPE:\"" + datatype + "\"" +
		                    (formQuery.length() != 0 ? " AND (" + formQuery + ")" : "") +
		                    (ftsQuery.length() != 0 ? " AND (" + ftsQuery + ")" : "");
			}
		}
		
				
		if (ftsQuery.length() != 0){
			  
			// we processed the search terms, so suffix the PATH query
			String path = null;
			if (!isRepo){
				
				path = SITES_SPACE_QNAME_PATH;
			    if (siteId != null && siteId.length() > 0)
			    {
			    	path += "cm:" + ISO9075.encode(siteId) + "/";
			    }
			    else
			    {
			    	path += "*/";
			    }
			    if (containerId != null && containerId.length() > 0)
			    {
			    	path += "cm:" + ISO9075.encode(containerId) + "/";
			    }
			    else
			    {
			    	path += "*/";
			     }
			  }
			  
			if (path != null)
			{
				ftsQuery = "PATH:\"" + path + "/*\" AND " + ftsQuery;
			 }
			 ftsQuery = "(" + ftsQuery + ") AND -TYPE:\"cm:thumbnail\"";
			  
			 //beCPG : now, exclude always product history
			 ftsQuery += PRODUCTS_TO_EXCLUDE; 
		}
		
		return ftsQuery;
	}
	
	/**
	 * Excecute the search.
	 *
	 * @param searchQueryPath the search query path
	 * @return the search nodes
	 */
	private List<NodeRef> getSearchNodes(String searchQueryPath){
		
		List<NodeRef> searchResults = new ArrayList<NodeRef>();
		
		SearchParameters sp = new SearchParameters();
        sp.addStore(RepoConsts.SPACES_STORE);
        sp.setLanguage(org.alfresco.service.cmr.search.SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.setQuery(searchQueryPath.toString());	        
        sp.setLimitBy(LimitBy.UNLIMITED);
        sp.setDefaultFieldName(DEFAULT_FIELD_NAME);
        sp.addQueryTemplate(DEFAULT_FIELD_NAME, QUERY_TEMPLATES);
        
        logger.debug("getSearchNodes, query: " + searchQueryPath);
        
        if(searchQueryPath != null && !searchQueryPath.isEmpty()){
        	
        	ResultSet resultSet = null;
            
            try{
            	
    	        resultSet = searchService.query(sp);			
    	        searchResults = new ArrayList<NodeRef>(resultSet.getNodeRefs());	        	        	        	        	        	      
            }
            catch(Exception e){
            	logger.debug("Failed to get search nodes", e);
            }
            finally{
            	if(resultSet != null){
            		resultSet.close();
            	}
            }        	
        }
        
        logger.debug("searchResults : " + searchResults.size());        
        
        return searchResults;
	}

	/**
	 * Take in account criteria on associations (ie : assoc_bcpg_supplierAssoc_added)
	 * @return
	 */
	private List<NodeRef> getSearchNodesByAssociations(List<NodeRef> nodes, Map<String, String> criteria){
		
		for(Map.Entry<String, String>criterion : criteria.entrySet()){
			
			String key = criterion.getKey();
			String propValue = criterion.getValue();
			
			// association
			if(key.startsWith("assoc_")){	
				
				String assocName = key.substring(6);					
				if (assocName.endsWith("_added"))
				{
					//TODO : should be generic
					if(!key.equals(CRITERIA_ING) &&
							!key.equals(CRITERIA_GEO_ORIGIN) &&
							!key.equals(CRITERIA_BIO_ORIGIN)){
						
						assocName = assocName.substring(0, assocName.length() - 6);
						assocName = assocName.replace("_", ":");
						QName assocQName = QName.createQName(assocName, namespaceService);
						
						if (!propValue.isEmpty())
						{						
							String[] arrValues = propValue.split(RepoConsts.MULTI_VALUES_SEPARATOR);
							for(String strNodeRef : arrValues){
								
								NodeRef nodeRef = new NodeRef(strNodeRef);
								
								if(nodeService.exists(nodeRef)){
								
									List<AssociationRef> assocRefs = nodeService.getSourceAssocs(nodeRef, assocQName);
									
									// remove nodes that don't respect  the assoc_ criteria
									List<NodeRef> nodesToKeep = new ArrayList<NodeRef>();
									
									for(AssociationRef assocRef : assocRefs){
										
										nodesToKeep.add(assocRef.getSourceRef());
									}
									
									nodes.retainAll(nodesToKeep);								
								}
								
							}
						}
					}					
				}		
			}
		}		
		
		return nodes;
	}
	
	/**
	 * Take in account criteria on ing list criteria
	 * @return
	 */
	private List<NodeRef> getSearchNodesByIngListCriteria(List<NodeRef> nodes, Map<String, String> criteria){								
		
		List<NodeRef> ingListItems = null;
		
		for(Map.Entry<String, String>criterion : criteria.entrySet()){
			
			String key = criterion.getKey();
			String propValue = criterion.getValue();
			
			// criteria on ing
			if(key.equals(CRITERIA_ING) && !propValue.isEmpty()){	
				
				NodeRef nodeRef = new NodeRef(propValue);							
				
				if(nodeService.exists(nodeRef)){
					
					List<AssociationRef> assocRefs = nodeService.getSourceAssocs(nodeRef, BeCPGModel.ASSOC_INGLIST_ING);
					ingListItems = new ArrayList<NodeRef>(assocRefs.size());
					
					for(AssociationRef assocRef : assocRefs){	
						
						NodeRef n = assocRef.getSourceRef();
						if(isWorkSpaceProtocol(n)){
							ingListItems.add(n);
						}						
					}
				}				
			}
			
			// criteria on geo origin, we query as an OR operator
			if(key.equals(CRITERIA_GEO_ORIGIN) && !propValue.isEmpty()){	
				
				List<NodeRef> ingListGeoOrigins = new ArrayList<NodeRef>();
				
				String[] arrValues = propValue.split(RepoConsts.MULTI_VALUES_SEPARATOR);
				for(String strNodeRef : arrValues){
					
					NodeRef nodeRef = new NodeRef(strNodeRef);
					
					if(nodeService.exists(nodeRef)){
					
						List<AssociationRef> assocRefs = nodeService.getSourceAssocs(nodeRef, BeCPGModel.ASSOC_INGLIST_GEO_ORIGIN);
						
						for(AssociationRef assocRef : assocRefs){
							
							NodeRef n = assocRef.getSourceRef();
							if(isWorkSpaceProtocol(n)){
								ingListGeoOrigins.add(n);
							}							
						}
					}				
				}
				
				if(ingListItems == null){
					ingListItems = ingListGeoOrigins;
				}
				else{
					ingListItems.retainAll(ingListGeoOrigins);
				}	
			}
			
			// criteria on bio origin, we query as an OR operator
			if(key.equals(CRITERIA_BIO_ORIGIN) && !propValue.isEmpty()){	
				
				List<NodeRef> ingListBioOrigins = new ArrayList<NodeRef>();
				
				String[] arrValues = propValue.split(RepoConsts.MULTI_VALUES_SEPARATOR);
				for(String strNodeRef : arrValues){
					
					NodeRef nodeRef = new NodeRef(strNodeRef);
					
					if(nodeService.exists(nodeRef)){
					
						List<AssociationRef> assocRefs = nodeService.getSourceAssocs(nodeRef, BeCPGModel.ASSOC_INGLIST_BIO_ORIGIN);
						
						for(AssociationRef assocRef : assocRefs){
							
							NodeRef n = assocRef.getSourceRef();
							if(isWorkSpaceProtocol(n)){
								ingListBioOrigins.add(n);
							}
						}
					}				
				}
				
				if(ingListItems == null){
					ingListItems = ingListBioOrigins;
				}
				else{
					ingListItems.retainAll(ingListBioOrigins);
				}						
			}			
		}						
		
		// determine the product WUsed of the ing list items
		if(ingListItems != null){
					
			List<NodeRef> productNodeRefs = new ArrayList<NodeRef>();
			for(NodeRef ingListItem : ingListItems){
			
				if(isWorkSpaceProtocol(ingListItem)){
				
					NodeRef ingListNodeRef = nodeService.getPrimaryParent(ingListItem).getParentRef();
					if(ingListNodeRef != null){
						
						NodeRef dataListsNodeRef = nodeService.getPrimaryParent(ingListNodeRef).getParentRef();					
						if(dataListsNodeRef != null){
							
							NodeRef rootNodeRef = nodeService.getPrimaryParent(dataListsNodeRef).getParentRef();
							
							//we don't display history version
							if(rootNodeRef!=null && !nodeService.hasAspect(rootNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)){
								productNodeRefs.add(rootNodeRef);
							}
						}
					}
				}			
			}			
			
			if(productNodeRefs != null){
				nodes.retainAll(productNodeRefs);
			}
		}
		
		return nodes;
	}

	private boolean isWorkSpaceProtocol(NodeRef nodeRef){
		
		if(nodeRef.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_WORKSPACE)){
			return true;
		}
		else{
			return false;
		}
	}
}
