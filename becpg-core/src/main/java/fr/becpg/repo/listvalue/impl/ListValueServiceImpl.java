/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.listvalue.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.listvalue.ListValuePage;
import fr.becpg.repo.listvalue.ListValuePlugin;
import fr.becpg.repo.listvalue.ListValueService;

/**
 * The Class ListValueServiceImpl.
 *
 * @author Matthieu
 */
@Service("listValueService")
public class ListValueServiceImpl implements ListValueService {
	
	
	private Map<String,ListValuePlugin> plugins;
	

	@Autowired
	private ListValuePlugin[] listValuePlugins;
	
	
	private static Log logger = LogFactory.getLog(ListValueServiceImpl.class);
	


	@Override
	public ListValuePage suggestBySourceType(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String,Serializable> extraProps) {
		
		ListValuePlugin plugin = getListValuePluginBySourceType(sourceType);
		if(plugin!=null){
			if(logger.isDebugEnabled()){
				logger.debug("Use plugin to suggest : "+plugin.getClass().getSimpleName());
			}
			return plugin.suggest(sourceType, query,  pageNum, pageSize, extraProps);
			
		}
		logger.warn("No plugin found for sourceType :"+sourceType);
		//TODO better to throw exception here
		return null;
	}

	
	private ListValuePlugin getListValuePluginBySourceType(String sourceType) {
		if(plugins == null || plugins.isEmpty()){
			plugins = new HashMap<>();
			for(ListValuePlugin plugin : listValuePlugins){
				for(String tmp : plugin.getHandleSourceTypes()){
					plugins.put(tmp, plugin);
				}
			}
		}
		
		return plugins.get(sourceType);
	}

}
