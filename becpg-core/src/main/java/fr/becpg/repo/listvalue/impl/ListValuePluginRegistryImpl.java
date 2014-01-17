package fr.becpg.repo.listvalue.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.becpg.repo.listvalue.ListValuePlugin;
import fr.becpg.repo.listvalue.ListValuePluginRegistry;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public class ListValuePluginRegistryImpl implements ListValuePluginRegistry{

	private Map<String,ListValuePlugin> plugins = new HashMap<String, ListValuePlugin>();
	
	
	@Override
	public List<ListValuePlugin> getListValuePlugins() {
		return new ArrayList<ListValuePlugin>(plugins.values());
	}

	@Override
	public ListValuePlugin getListValuePluginBySourceType(String sourceType) {
		return plugins.get(sourceType);
	}

	@Override
	public void addListValuePlugin(ListValuePlugin plugin) {
		for(String sourceType : plugin.getHandleSourceTypes()){
			plugins.put(sourceType, plugin);
		}
	}

	
}
