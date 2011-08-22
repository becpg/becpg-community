package fr.becpg.repo.search;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * do an advanced search query
 * @author querephi
 *
 */
public interface AdvSearchService {
	 
	/**
	 * Advanced search form data search.
	 * Supplied as json in the standard Alfresco Forms data structure:
	 * 		prop_<name>:value|assoc_<name>:value
	 * 		name = namespace_propertyname|pseudopropertyname
	 * 		value = string value - comma separated for multi-value, no escaping yet!
	 * - underscore represents colon character in name
	 * - pseudo property is one of any cm:content url property: mimetype|encoding|size
	 * - always string values - interogate DD for type data   
	 * 	    
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
	public List<NodeRef> queryAdvSearch(QName datatype, String term, String tag, Map<String, String> criteria, String sort, boolean isRepo, String siteId, String containerId);
}
