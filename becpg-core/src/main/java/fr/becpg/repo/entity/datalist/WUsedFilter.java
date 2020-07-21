package fr.becpg.repo.entity.datalist;

import fr.becpg.repo.entity.datalist.data.MultiLevelListData;

/**
 * <p>WUsedFilter interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface WUsedFilter {
	
	enum WUsedFilterKind {
		STANDARD, TRANSVERSE
	}

	/**
	 * <p>filter.</p>
	 *
	 * @param multiLevelData a {@link fr.becpg.repo.entity.datalist.data.MultiLevelListData} object.
	 */
	void  filter(MultiLevelListData multiLevelData);
	
	/**
	 * <p>getFilterKind.</p>
	 *
	 * @return a {@link fr.becpg.repo.entity.datalist.WUsedFilter.WUsedFilterKind} object.
	 */
	WUsedFilterKind getFilterKind();
	
}
