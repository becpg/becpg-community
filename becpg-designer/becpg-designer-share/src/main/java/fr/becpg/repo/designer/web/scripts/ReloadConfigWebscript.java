/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
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
package fr.becpg.repo.designer.web.scripts;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.config.ConfigService;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Reload share configuration files
 *
 * @author "Matthieu Laborie"
 * @version $Id: $Id
 */
public class ReloadConfigWebscript  extends DeclarativeWebScript {
	

    private static final Log logger = LogFactory.getLog(ReloadConfigWebscript.class);
    
	
	ConfigService configService;
	
	/**
	 * <p>Setter for the field <code>configService</code>.</p>
	 *
	 * @param configService the configService to set
	 */
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	
	/**
	 * {@inheritDoc}
	 *
	 * @param req a {@link org.springframework.extensions.webscripts.WebScriptRequest} object
	 * @param status a {@link org.springframework.extensions.webscripts.Status} object
	 * @return a {@link java.util.Map} object
	 */
	protected Map<String, Object> executeImpl(WebScriptRequest req,
	         Status status) {
	      Map<String, Object> model = new HashMap<>();
	      
	      logger.debug("Reload configService");
	      configService.reset();
	      
	      return model;
	   }

}
