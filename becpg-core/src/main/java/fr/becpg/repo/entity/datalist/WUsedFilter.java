package fr.becpg.repo.entity.datalist;

import java.util.List;

import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.helper.impl.AssociationCriteriaFilter;

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

	/**
	 * <p>getCriteriaFilters.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	default List<AssociationCriteriaFilter> getCriteriaFilters() {
		return List.of();
	}
	
}
