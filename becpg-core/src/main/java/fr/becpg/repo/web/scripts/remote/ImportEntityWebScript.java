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
package fr.becpg.repo.web.scripts.remote;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.common.BeCPGException;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.remote.EntityProviderCallBack;
import fr.becpg.repo.entity.remote.impl.HttpEntityProviderCallback;

/**
 * Import nodeRef List from default remote
 * 
 * @author matthieu
 * 
 */
public class ImportEntityWebScript extends AbstractEntityWebScript implements InitializingBean {

	private String remoteServer;

	private String remoteUser;

	private String remotePwd;

	private EntityProviderCallBack entityProviderCallBack;

	public void setRemoteServer(String remoteServer) {
		this.remoteServer = remoteServer;
	}

	public void setRemoteUser(String remoteUser) {
		this.remoteUser = remoteUser;
	}

	public void setRemotePwd(String remotePwd) {
		this.remotePwd = remotePwd;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		entityProviderCallBack = new HttpEntityProviderCallback(remoteServer + "/service/becpg/remote/entity", remoteUser, remotePwd,
				remoteEntityService);

	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {

		try {

			JSONObject json = (JSONObject) req.parseContent();
			String entities = "";
			if (json != null && json.has("entities")) {
				entities = (String) json.get("entities");
			}
			NodeRef destNodeRef = null;
			String destination = req.getParameter("destination");
			if (destination != null) {
				destNodeRef = new NodeRef(destination);
			}

			Map<QName, Serializable> props = new HashMap<>();
			props.put(BeCPGModel.PROP_CODE, null);
			props.put(ContentModel.PROP_OWNER, AuthenticationUtil.getFullyAuthenticatedUser());
			JSONArray ret = new JSONArray();
			for (final String entity : entities.split(",")) {
				NodeRef entityNodeRef = null;
				try {
					entityNodeRef = entityProviderCallBack.provideNode(new NodeRef(entity), destNodeRef, props);
				} catch (BeCPGException e) {
					logger.error("Cannot import entity ", e);
					throw new WebScriptException(e.getMessage());
				}

				ret.put(entityNodeRef);
			}

			resp.setContentType("application/json");
			resp.setContentEncoding("UTF-8");
			resp.getWriter().write(ret.toString(3));
		} catch (JSONException e) {
			throw new WebScriptException(e.getMessage());
		}

	}
}
