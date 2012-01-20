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
	 * @param datatype
	 * @param term
	 * @param tag
	 * @param criteria
	 * @param sortMap
	 * @param maxItem
	 * @param isRepo
	 * @param siteId
	 * @param containerId
	 * @return
	 * If query is pass only make query search
	 * 
	 */
	public List<NodeRef> queryAdvSearch(String query, QName datatype, String term, String tag,
			Map<String, String> criteria, boolean isRepo, String siteId, String containerId, Map<String, Boolean> sortMap, int maxItem);
}
