package fr.becpg.repo.entity.datalist;

import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;

public interface MultiLevelDataListService {

	
	public MultiLevelListData getMultiLevelListData(DataListFilter dataListFilter);

	
}
