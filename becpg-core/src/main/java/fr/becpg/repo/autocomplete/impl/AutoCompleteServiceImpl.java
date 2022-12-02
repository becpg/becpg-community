/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.autocomplete.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.autocomplete.AutoCompletePage;
import fr.becpg.repo.autocomplete.AutoCompletePlugin;
import fr.becpg.repo.autocomplete.AutoCompleteService;

/**
 * The Class AutoCompleteServiceImpl.
 *
 * @author Matthieu
 * @version $Id: $Id
 */
@Service("autoCompleteService")
public class AutoCompleteServiceImpl implements AutoCompleteService {
	

	private static final  Log logger = LogFactory.getLog(AutoCompleteServiceImpl.class);
	
	private Map<String,AutoCompletePlugin> plugins;
	
	@Autowired
	private AutoCompletePlugin[] autoCompletePlugins;
	

	/** {@inheritDoc} */
	@Override
	public AutoCompletePage suggestBySourceType(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String,Serializable> extraProps) {
		
		AutoCompletePlugin plugin = getListValuePluginBySourceType(sourceType);
		if(plugin!=null){
			if(logger.isDebugEnabled()){
				logger.debug("Use plugin to suggest : "+plugin.getClass().getSimpleName());
			}
			return plugin.suggest(sourceType, query,  pageNum, pageSize, extraProps);
			
		}
		throw new IllegalStateException("No plugin found for sourceType :"+sourceType);
	}

	
	private AutoCompletePlugin getListValuePluginBySourceType(String sourceType) {
		if(plugins == null || plugins.isEmpty()){
			plugins = new HashMap<>();
			for(AutoCompletePlugin plugin : autoCompletePlugins){
				for(String tmp : plugin.getHandleSourceTypes()){
					plugins.put(tmp, plugin);
				}
			}
		}
		
		return plugins.get(sourceType);
	}

}
