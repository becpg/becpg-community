package fr.becpg.repo.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.search.permission.BeCPGPermissionFilter;
import fr.becpg.repo.search.permission.impl.ReadPermissionFilter;

/**
 * This class do a search on the repository (association, properties and productLists), for the UI so it respects rights.
 * @author querephi
 *
 */
public class AdvSearchServiceImpl implements AdvSearchService {

	/** The Constant SITES_SPACE_QNAME_PATH. */
	private static final String SITES_SPACE_QNAME_PATH = "/app:company_home/st:sites/";	
	
	/** The Constant PRODUCTS_TO_EXCLUDE. */
	private static final String PRODUCTS_TO_EXCLUDE = " AND -ASPECT:\"bcpg:compositeVersion\" AND -ASPECT:\"ecm:simulationEntityAspect\" ";
	
	
	private static final String CRITERIA_ING = "assoc_bcpg_ingListIng_added";
	
	private static final String CRITERIA_GEO_ORIGIN = "assoc_bcpg_ingListGeoOrigin_added";
	
	private static final String CRITERIA_BIO_ORIGIN = "assoc_bcpg_ingListBioOrigin_added";
	
	private static final int MAX_RESULTS = 250;
	
	private final int SIZE_UNLIMITED = -1;
	
	private static Log logger = LogFactory.getLog(AdvSearchServiceImpl.class);
		

	private NodeService nodeService;
	private DictionaryService dictionaryService;
	private NamespaceService namespaceService;

	
	private BeCPGSearchService beCPGSearchService;
	

	private PermissionService permissionService;
	
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
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
	
	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	@Override
	public List<NodeRef> queryAdvSearch(String searchQuery, QName datatype, String term, String tag,
										Map<String, String> criteria, boolean isRepo,
										String siteId, String containerId, Map<String, Boolean> sortMap, int maxResults) {
		
		String language = SearchService.LANGUAGE_FTS_ALFRESCO;
				
		if(maxResults<=0){
			maxResults = MAX_RESULTS;
		}
		
		if(searchQuery==null || searchQuery.isEmpty()){
			searchQuery = getSearchQueryByProperties(datatype, term, tag, criteria, isRepo, siteId, containerId);
		} else {
			language  = SearchService.LANGUAGE_LUCENE;
		}
		boolean isAssocSearch =  isAssocSearch(criteria);
		
		List<NodeRef> nodes = beCPGSearchService.search(searchQuery, sortMap,  isAssocSearch ?  SIZE_UNLIMITED :maxResults  , language);
				
		
		
		if(isAssocSearch){
			nodes = getSearchNodesByAssociations(nodes, criteria);
			
			if(datatype != null && dictionaryService.isSubClass(datatype, BeCPGModel.TYPE_PRODUCT)){
				
				nodes = getSearchNodesByIngListCriteria(nodes, criteria);
			}
			nodes =  filterWithPermissions(nodes,new ReadPermissionFilter() ,maxResults);
		}
		
		
		 return nodes;			
	}
	
	private boolean isAssocSearch(Map<String, String> criteria) {
		if(criteria!=null){
			for(Map.Entry<String, String>criterion : criteria.entrySet()){
					String key = criterion.getKey();
					// association
					if(key.startsWith("assoc_")){	
						
						return true;
					}
			}
		}
		return false;
	}


	private List<NodeRef> filterWithPermissions(List<NodeRef> nodes, BeCPGPermissionFilter filter, int maxResults){
		
		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		
		nodes = filter.filter(nodes, permissionService, maxResults);
		
		if (logger.isDebugEnabled()) {
			watch.stop();
			logger.debug("filterWithPermissions executed in  "
					+ watch.getTotalTimeSeconds() + " seconds ");
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
	
	private String getSearchQueryByProperties(QName datatype, String term, String tag, Map<String, String> criteria 
			, boolean isRepo, String siteId, String containerId){
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
			
			
		}
		
				
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
			
			    if (path != null)
				{
					ftsQuery = "PATH:\"" + path + "/*\""+(ftsQuery.length()!=0?" AND " + ftsQuery:"");
				 }		
			    
			 }
			  
		
			String typeQuery = "";
			if(datatype!=null){
				typeQuery = "+TYPE:\"" + datatype + "\"";
			} else {
				typeQuery = "-TYPE:\"cm:thumbnail\"";
			}						
			 
	         // extract data type for this search - advanced search query is type specific
	         ftsQuery = typeQuery +
	                    (formQuery.length() != 0 ? " AND (" + formQuery + ")" : "") +
	                    (ftsQuery.length() != 0 ? " AND (" + ftsQuery + ")" : "");
	         
	         //beCPG : now, exclude always product history
	         ftsQuery += PRODUCTS_TO_EXCLUDE;

		
		return ftsQuery;
	}
	
	/**
	 * Take in account criteria on associations (ie : assoc_bcpg_supplierAssoc_added)
	 * @return
	 */
	private List<NodeRef> getSearchNodesByAssociations(List<NodeRef> nodes, Map<String, String> criteria){
		
		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		
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
		
		 
        if (logger.isDebugEnabled()) {
			watch.stop();
			logger.debug("getSearchNodesByAssociations executed in  "
					+ watch.getTotalTimeSeconds() + " seconds ");
		}
		
		return nodes;
	}
	
	/**
	 * Take in account criteria on ing list criteria
	 * @return
	 */
	private List<NodeRef> getSearchNodesByIngListCriteria(List<NodeRef> nodes, Map<String, String> criteria){								
		
		List<NodeRef> ingListItems = null;
		
		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		
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
		
		 
        if (logger.isDebugEnabled()) {
			watch.stop();
			logger.debug("getSearchNodesByIngListCriteria executed in  "
					+ watch.getTotalTimeSeconds() + " seconds ");
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
