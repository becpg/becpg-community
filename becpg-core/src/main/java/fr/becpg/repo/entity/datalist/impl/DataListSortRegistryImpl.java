/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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
