package fr.becpg.repo.entity.datalist;

import fr.becpg.repo.entity.datalist.data.MultiLevelListData;

public interface WUsedFilter {
	
	public enum WUsedFilterKind {
		STANDARD, TRANSVERSE;
	}

	public void  filter(MultiLevelListData multiLevelData);
	
	public WUsedFilterKind getFilterKind();
	
}
