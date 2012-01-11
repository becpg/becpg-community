/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.listvalue.impl;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.listvalue.ListValuePage;
import fr.becpg.repo.listvalue.ListValuePlugin;
import fr.becpg.repo.listvalue.ListValuePluginRegistry;
import fr.becpg.repo.listvalue.ListValueService;

// TODO: Move into plugins
/**
 * The Class ListValueServiceImpl.
 *
 * @author Matthieu
 */
public class ListValueServiceImpl implements ListValueService {
	


	protected ListValuePluginRegistry listValuePluginRegistry;
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ListValueServiceImpl.class);
	

	
	/**
	 * @param listValuePluginRegistry the listValuePluginRegistry to set
	 */
	public void setListValuePluginRegistry(ListValuePluginRegistry listValuePluginRegistry) {
		this.listValuePluginRegistry = listValuePluginRegistry;
	}




	@Override
	public ListValuePage suggestBySourceType(String sourceType, String query, Integer pageNum, Map<String,Serializable> extraProps) {
		
		ListValuePlugin plugin = listValuePluginRegistry.getListValuePluginBySourceType(sourceType);
		if(plugin!=null){
			if(logger.isDebugEnabled()){
				logger.debug("Use plugin to suggest : "+plugin.getClass().getSimpleName());
			}
			return plugin.suggest(sourceType, query,  pageNum, extraProps);
			
		}
		logger.warn("No plugin found for sourceType :"+sourceType);
		//TODO better to throw exception here
		return null;
	}

}
