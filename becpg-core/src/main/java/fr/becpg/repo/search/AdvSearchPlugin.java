package fr.becpg.repo.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.search.impl.SearchConfig;

/**
 * <p>AdvSearchPlugin interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface AdvSearchPlugin {

	/**
	 * <p>filter.</p>
	 *
	 * @param nodes a {@link java.util.List} object.
	 * @param datatype a {@link org.alfresco.service.namespace.QName} object.
	 * @param criteria a {@link java.util.Map} object.
	 * @param searchConfig a {@link fr.becpg.repo.search.impl.SearchConfig} object.
	 * @return a {@link java.util.List} object.
	 */
	List<NodeRef> filter(List<NodeRef> nodes, QName datatype, Map<String, String> criteria, SearchConfig searchConfig);

	/**
	 * <p>getIgnoredFields.</p>
	 *
	 * @param datatype a {@link org.alfresco.service.namespace.QName} object.
	 * @param searchConfig a {@link fr.becpg.repo.search.impl.SearchConfig} object.
	 * @return a {@link java.util.Set} object.
	 */
	Set<String> getIgnoredFields(QName datatype, SearchConfig searchConfig);

	/**
	 * <p>isSearchFiltered.</p>
	 *
	 * @param criteria a {@link java.util.Map} object
	 * @return a boolean
	 */
	boolean isSearchFiltered(Map<String, String> criteria);

}
