package fr.becpg.repo.entity.datalist.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.entity.datalist.DataListSortPlugin;
import fr.becpg.repo.entity.datalist.DataListSortRegistry;

public class DataListSortRegistryImpl implements DataListSortRegistry {

	private static Log logger = LogFactory.getLog(DataListSortRegistryImpl.class);

	private Map<String, DataListSortPlugin> plugins = new HashMap<String, DataListSortPlugin>();

	@Override
	public List<DataListSortPlugin> getPlugins() {
		return new ArrayList<DataListSortPlugin>(plugins.values());
	}

	@Override
	public DataListSortPlugin getPluginById(String pluginId) {
		return plugins.get(pluginId);
	}

	@Override
	public void addPlugin(DataListSortPlugin plugin) {
		logger.info("register plugin " + plugin.getPluginId());
		plugins.put(plugin.getPluginId(), plugin);
	}
}
