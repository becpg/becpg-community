/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
