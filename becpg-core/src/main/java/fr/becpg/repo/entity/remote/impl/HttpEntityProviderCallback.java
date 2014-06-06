package fr.becpg.repo.entity.remote.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;

import fr.becpg.common.BeCPGException;
import fr.becpg.repo.entity.remote.EntityProviderCallBack;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;

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
		try {
			String url = remoteServer + "?nodeRef=" + nodeRef.toString();
			logger.debug("Try getting nodeRef  from : " + url);
			logger.debug("User : " + remoteUser);
			logger.debug("Password : " + remotePwd);

			HttpClient httpClient = new DefaultHttpClient();

			Header authHeader = BasicScheme.authenticate(new UsernamePasswordCredentials(remoteUser, remotePwd), "UTF-8", false);

			HttpGet entityUrl = new HttpGet(url);

			entityUrl.addHeader(authHeader);

			HttpResponse httpResponse = httpClient.execute(entityUrl);
			HttpEntity responseEntity = httpResponse.getEntity();


			

			try (InputStream entityStream = responseEntity.getContent()){
				return remoteEntityService.createOrUpdateEntity(nodeRef, entityStream, RemoteEntityFormat.xml, this);
			} 
		} catch (MalformedURLException e) {
			throw new BeCPGException(e);
		} catch (IOException e) {
			throw new BeCPGException(e);
		}

	}

	@Override
	public void provideContent(NodeRef origNodeRef, NodeRef destNodeRef) throws BeCPGException {
		
		try {
			String url = remoteServer + "/data?nodeRef=" + origNodeRef.toString();
			logger.debug("Try getting data  from : " + url);
			logger.debug("User : " + remoteUser);
			logger.debug("Password : " + remotePwd);

			HttpClient httpClient = new DefaultHttpClient();

			Header authHeader = BasicScheme.authenticate(new UsernamePasswordCredentials(remoteUser, remotePwd), "UTF-8", false);

			HttpGet entityUrl = new HttpGet(url);

			entityUrl.addHeader(authHeader);

			HttpResponse httpResponse = httpClient.execute(entityUrl);
			HttpEntity responseEntity = httpResponse.getEntity();

			

			try (InputStream dataStream = responseEntity.getContent()){

				remoteEntityService.addOrUpdateEntityData(destNodeRef, dataStream, RemoteEntityFormat.xml);

			} 
		} catch (MalformedURLException e) {
			throw new BeCPGException(e);
		} catch (IOException e) {
			throw new BeCPGException(e);
		}

	}

}
