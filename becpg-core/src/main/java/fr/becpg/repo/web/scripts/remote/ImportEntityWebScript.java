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
package fr.becpg.repo.web.scripts.remote;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Arrays;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.common.BeCPGException;
import fr.becpg.repo.entity.remote.EntityProviderCallBack;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;

/**
 * Import nodeRef List from default remote
 * 
 * @author matthieu
 * 
 */
public class ImportEntityWebScript extends AbstractEntityWebScript implements InitializingBean {

	private String remoteServer;

	private String remoteUser;

	private char[] remotePwd;

	private EntityProviderCallBack entityProviderCallBack;

	public void setRemoteServer(String remoteServer) {
		this.remoteServer = remoteServer;
	}

	public void setRemoteUser(String remoteUser) {
		this.remoteUser = remoteUser;
	}

	public void setRemotePwd(char[] remotePwd) {
		if (remotePwd != null) {
			this.remotePwd = Arrays.copyOf(remotePwd, remotePwd.length);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		entityProviderCallBack = new EntityProviderCallBack() {

			@Override
			public NodeRef provideNode(NodeRef nodeRef) throws BeCPGException {

				Authenticator.setDefault(new Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(remoteUser, remotePwd);
					}
				});

				try {
					URL entityUrl = new URL(remoteServer + "/service/becpg/remote/entity?nodeRef=" + nodeRef.toString());
					URL dataUrl = new URL(remoteServer + "/service/becpg/remote/entity/data?nodeRef=" + nodeRef.toString());
					InputStream entityStream = null;
					InputStream dataStream = null;

					try {
						entityStream = entityUrl.openStream();

						NodeRef entityNodeRef = remoteEntityService.createOrUpdateEntity(nodeRef, entityStream, RemoteEntityFormat.xml, this);

						dataStream = dataUrl.openStream();

						remoteEntityService.addOrUpdateEntityData(entityNodeRef, dataStream, RemoteEntityFormat.xml);

						return entityNodeRef;

					} finally {
						IOUtils.closeQuietly(entityStream);
						IOUtils.closeQuietly(dataStream);
					}
				} catch (MalformedURLException e) {
					throw new BeCPGException(e);
				} catch (IOException e) {
					throw new BeCPGException(e);
				}

			}
		};

	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {

		try {

			JSONObject json = (JSONObject) req.parseContent();
			String entities = "";
			if (json != null && json.has("entities")) {
				entities = (String) json.get("entities");
			}

			JSONArray ret = new JSONArray();
			for (final String entity : entities.split(",")) {
				NodeRef entityNodeRef = AuthenticationUtil.runAsSystem(new RunAsWork<NodeRef>() {
					@Override
					public NodeRef doWork() throws Exception {
						try {
							return entityProviderCallBack.provideNode(new NodeRef(entity));
						} catch (BeCPGException e) {
							logger.error("Cannot import entity ", e);
							throw new WebScriptException(e.getMessage());
						}
					}
				});
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
