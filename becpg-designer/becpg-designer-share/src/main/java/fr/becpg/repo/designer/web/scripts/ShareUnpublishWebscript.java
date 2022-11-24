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

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
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

/**
 * Publish config files in share
 *
 * @author "Valentin Leblanc"
 * @version $Id: $Id
 */
public class ShareUnpublishWebscript extends AbstractWebScript {

	private static final Log logger = LogFactory.getLog(ShareUnpublishWebscript.class);

	private String configPath;

	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}
	
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		String nodeRef = req.getParameter("nodeRef");
		
		String fileName = req.getParameter("fileName");
		
		RequestContext rc = ThreadLocalRequestContext.getRequestContext();
		Connector conn;
		try {
			conn = rc.getServiceRegistry().getConnectorService().getConnector("alfresco", rc.getUserId(), ServletUtil.getSession());
			ConnectorContext ctx = new ConnectorContext();
			ctx.setExceptionOnError(false);
			ctx.setMethod(HttpMethod.POST);
			Response response = conn.call("/becpg/designer/model/unpublish" + "?nodeRef=" + nodeRef, ctx);
			if (response.getStatus().getCode() == Status.STATUS_OK) {

				JSONObject jsonResponse = new JSONObject(response.getResponse());
				
				if (jsonResponse.has("type")) {
					if (jsonResponse.getString("type").equals("config")) {
						SharePublishHelper.unpublishConfig(configPath, fileName);
					}
				} else {
					throw new WebScriptException("Response has no type");
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
