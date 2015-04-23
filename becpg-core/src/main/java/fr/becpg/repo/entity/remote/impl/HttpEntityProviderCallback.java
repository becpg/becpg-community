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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. 
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.entity.remote.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import fr.becpg.common.BeCPGException;
import fr.becpg.repo.entity.remote.EntityProviderCallBack;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;

/**
 * 
 * @author matthieu
 *
 */
public class HttpEntityProviderCallback implements EntityProviderCallBack {

	private static Log logger = LogFactory.getLog(HttpEntityProviderCallback.class);

	private String remoteServer;

	private String remoteUser;

	private String remotePwd;

	private RemoteEntityService remoteEntityService;

	public HttpEntityProviderCallback(String remoteServer, String remoteUser, String remotePwd, RemoteEntityService remoteEntityService) {
		super();
		this.remoteServer = remoteServer;
		this.remoteUser = remoteUser;
		this.remotePwd = remotePwd;
		this.remoteEntityService = remoteEntityService;
	}

	@Override
	public NodeRef provideNode(NodeRef nodeRef) throws BeCPGException {
		return provideNode(nodeRef, null, null);
	}

	@Override
	public NodeRef provideNode(NodeRef nodeRef, NodeRef destNodeRef, Map<QName, Serializable> properties) throws BeCPGException {
		try {
			String url = remoteServer + "?nodeRef=" + nodeRef.toString();
			if (logger.isDebugEnabled()) {
				logger.debug("Try getting nodeRef  from : " + url);
				logger.debug("User : " + remoteUser);
				logger.debug("Password : " + remotePwd);
			}
			HttpGet entityUrl = new HttpGet(url);

			try (CloseableHttpResponse httpResponse = getResponse(entityUrl)) {
				HttpEntity responseEntity = httpResponse.getEntity();
				try (InputStream entityStream = responseEntity.getContent()) {
					return remoteEntityService.createOrUpdateEntity(nodeRef, destNodeRef, properties, entityStream, RemoteEntityFormat.xml, this);
				}
			}

		} catch (IOException e) {
			throw new BeCPGException(e);
		}

	}

	@Override
	public void provideContent(NodeRef origNodeRef, NodeRef destNodeRef) throws BeCPGException {

		try {
			String url = remoteServer + "/data?nodeRef=" + origNodeRef.toString();
			if (logger.isDebugEnabled()) {
				logger.debug("Try getting data  from : " + url);
				logger.debug("User : " + remoteUser);
				logger.debug("Password : " + remotePwd);
			}
			HttpGet entityUrl = new HttpGet(url);

			try (CloseableHttpResponse httpResponse = getResponse(entityUrl)) {
				HttpEntity responseEntity = httpResponse.getEntity();
				try (InputStream dataStream = responseEntity.getContent()) {
					remoteEntityService.addOrUpdateEntityData(destNodeRef, dataStream, RemoteEntityFormat.xml);
				}
			}
		} catch (IOException e) {
			throw new BeCPGException(e);
		}

	}

	private CloseableHttpResponse getResponse(HttpGet entityUrl) throws ClientProtocolException, IOException {
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpClientContext httpContext = HttpClientContext.create();

			UsernamePasswordCredentials creds = new UsernamePasswordCredentials(remoteUser, remotePwd);
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), creds);
			httpContext.setCredentialsProvider(credsProvider);

			return httpClient.execute(entityUrl, httpContext);
		}
	}

}
