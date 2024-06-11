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

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.config.ConfigService;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.ServletUtil;
import org.springframework.extensions.surf.exception.ConnectorServiceException;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.ConnectorContext;
import org.springframework.extensions.webscripts.connector.HttpMethod;
import org.springframework.extensions.webscripts.connector.Response;

import fr.becpg.repo.designer.config.SharePublishHelper;

public class CleanConfigWebscript extends AbstractWebScript {

	private static final Log logger = LogFactory.getLog(CleanConfigWebscript.class);

	private String configPath;
	
	private ConfigService configService;
	
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		
		File configDir = new File(configPath);
		if (configDir.exists()) {
			
			File[] configFiles = configDir.listFiles();
			
			if (configFiles.length > 0) {
				RequestContext rc = ThreadLocalRequestContext.getRequestContext();
				try {
					Connector conn = rc.getServiceRegistry().getConnectorService().getConnector("alfresco", rc.getUserId(), ServletUtil.getSession());
					ConnectorContext ctx = new ConnectorContext();
					ctx.setMethod(HttpMethod.GET);
					Response response = conn.call("/becpg/designer/config/list", ctx);
					if (response.getStatus().getCode() == Status.STATUS_OK) {
						JSONObject jsonResponse = new JSONObject(response.getResponse());
						JSONArray items = jsonResponse.getJSONArray("items");
						for (File configFile : configFiles) {
							String fileName = configFile.getName();
							if (items.toList().stream().noneMatch(i -> ((Map<String, Object>) i).get("displayName").equals(fileName))) {
								SharePublishHelper.unpublishConfig(configPath, fileName);
							}
						}
					} else {
						throw new WebScriptException("Response status is not OK");
					}
				} catch (ConnectorServiceException e) {
					logger.error(e.getMessage(), e);
					throw new WebScriptException(e.getMessage());
				}
			}
		}
		configService.reset();
	}

}
