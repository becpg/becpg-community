package fr.becpg.repo.entity.datalist;

import fr.becpg.repo.entity.datalist.data.MultiLevelListData;

public interface WUsedFilter {
	
	enum WUsedFilterKind {
		STANDARD, TRANSVERSE
	}

	void  filter(MultiLevelListData multiLevelData);
	
	WUsedFilterKind getFilterKind();
	
}
