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
package fr.becpg.repo.web.scripts.remote;

import java.io.IOException;
import java.util.HashMap;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
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
 * @version $Id: $Id
 */
@Deprecated
public class ImportEntityWebScript extends AbstractEntityWebScript implements InitializingBean {

	private String remoteServer;

	private String remoteUser;

	private String remotePwd;

	private EntityProviderCallBack entityProviderCallBack;

	/**
	 * <p>Setter for the field <code>remoteServer</code>.</p>
	 *
	 * @param remoteServer a {@link java.lang.String} object.
	 */
	public void setRemoteServer(String remoteServer) {
		this.remoteServer = remoteServer;
	}

	/**
	 * <p>Setter for the field <code>remoteUser</code>.</p>
	 *
	 * @param remoteUser a {@link java.lang.String} object.
	 */
	public void setRemoteUser(String remoteUser) {
		this.remoteUser = remoteUser;
	}

	/**
	 * <p>Setter for the field <code>remotePwd</code>.</p>
	 *
	 * @param remotePwd a {@link java.lang.String} object.
	 */
	public void setRemotePwd(String remotePwd) {
		this.remotePwd = remotePwd;
	}

	/** {@inheritDoc} */
	@Override
	public void afterPropertiesSet() throws Exception {
		entityProviderCallBack = new HttpEntityProviderCallback(remoteServer + "/service/becpg/remote/entity", remoteUser, remotePwd,
				remoteEntityService);

	}

	/** {@inheritDoc} */
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

			JSONArray ret = new JSONArray();
			for (final String entity : entities.split(",")) {
				NodeRef entityNodeRef;
				try {
					entityNodeRef = entityProviderCallBack.provideNode(new NodeRef(entity), destNodeRef, new HashMap<>());
					nodeService.setProperty(entityNodeRef,BeCPGModel.PROP_CODE, null);
					nodeService.setProperty(entityNodeRef,ContentModel.PROP_OWNER, AuthenticationUtil.getFullyAuthenticatedUser());
					
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
