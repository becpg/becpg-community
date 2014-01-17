package fr.becpg.repo.listvalue;

import java.util.List;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public interface ListValuePluginRegistry {
	
	public List<ListValuePlugin> getListValuePlugins();
	
	public ListValuePlugin getListValuePluginBySourceType(String sourceType);
	
	void addListValuePlugin(ListValuePlugin plugin);
}
