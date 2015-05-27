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
