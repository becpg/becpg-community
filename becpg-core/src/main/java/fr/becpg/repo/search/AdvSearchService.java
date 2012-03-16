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
	 * 
	 * @param query
	 * @param criteria
	 * @param sortMap
	 * @param maxItem
	 * @return
	 */
	public List<NodeRef> queryAdvSearch(String query, String language, QName datatype,
			Map<String, String> criteria, Map<String, Boolean> sortMap, int maxItem);

	
	/**
	 * @param datatype
	 * @param term
	 * @param tag
	 * @param isRepo
	 * @param siteId
	 * @param containerId
	 * @return
	 */
	public String getSearchQueryByProperties(QName datatype, String term, String tag, boolean isRepo, String siteId, String containerId);

}
