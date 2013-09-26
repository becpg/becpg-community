package fr.becpg.repo.entity.datalist;

import java.util.List;


public interface DataListSortRegistry {
	
	public List<DataListSortPlugin> getPlugins();
	
	public DataListSortPlugin getPluginById(String pluginId);
	
	void addPlugin(DataListSortPlugin plugin);
}
