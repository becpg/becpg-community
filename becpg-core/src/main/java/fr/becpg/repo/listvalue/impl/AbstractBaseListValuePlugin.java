package fr.becpg.repo.listvalue.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.listvalue.ListValuePlugin;
import fr.becpg.repo.listvalue.ListValuePluginRegistry;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public abstract class AbstractBaseListValuePlugin implements ListValuePlugin {

	protected ListValuePluginRegistry listValuePluginRegistry;
	
	protected Log logger = LogFactory.getLog(getClass());
	
	
	/**
	 * @param listValuePluginRegistry the listValuePluginRegistry to set
	 */
	public void setListValuePluginRegistry(ListValuePluginRegistry listValuePluginRegistry) {
		this.listValuePluginRegistry = listValuePluginRegistry;
	}


	/**
	 * Register a new plugin
	 */
	public void init(){
		listValuePluginRegistry.addListValuePlugin(this);
	}

	
	
	
	
}
