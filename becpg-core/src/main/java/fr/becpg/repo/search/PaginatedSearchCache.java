package fr.becpg.repo.search;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.entity.datalist.data.MultiLevelListData;

/**
 * <p>PaginatedSearchCache interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface PaginatedSearchCache {
	
	/**
	 * <p>getSearchResults.</p>
	 *
	 * @param queryId a {@link java.lang.String} object.
	 * @return a {@link java.util.List} object.
	 */
	public List<NodeRef> getSearchResults(String queryId);
	
	/**
	 * <p>storeSearchResults.</p>
	 *
	 * @param results a {@link java.util.List} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String storeSearchResults(List<NodeRef> results);

	/**
	 * <p>getSearchMultiLevelResults.</p>
	 *
	 * @param queryExecutionId a {@link java.lang.String} object.
	 * @return a {@link fr.becpg.repo.entity.datalist.data.MultiLevelListData} object.
	 */
	public MultiLevelListData getSearchMultiLevelResults(String queryExecutionId);

	/**
	 * <p>storeMultiLevelSearchResults.</p>
	 *
	 * @param listData a {@link fr.becpg.repo.entity.datalist.data.MultiLevelListData} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String storeMultiLevelSearchResults(MultiLevelListData listData);

}
