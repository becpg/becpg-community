package fr.becpg.repo.entity.remote.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.HttpClientBuilder;

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
		return provideNode(nodeRef, null, null);
	}

	@Override
	public NodeRef provideNode(NodeRef nodeRef, NodeRef destNodeRef, Map<QName, Serializable> properties) throws BeCPGException {
		try {
			String url = remoteServer + "?nodeRef=" + nodeRef.toString();
			logger.debug("Try getting nodeRef  from : " + url);
			logger.debug("User : " + remoteUser);
			logger.debug("Password : " + remotePwd);

			HttpClient httpClient = HttpClientBuilder.create().build();

			BasicScheme basicScheme = new BasicScheme(Charset.forName("UTF-8"));
			HttpClientContext httpContext = HttpClientContext.create();

			HttpGet entityUrl = new HttpGet(url);
			basicScheme.authenticate(new UsernamePasswordCredentials(remoteUser, remotePwd), entityUrl, httpContext);

			HttpResponse httpResponse = httpClient.execute(entityUrl, httpContext);
			HttpEntity responseEntity = httpResponse.getEntity();

			try (InputStream entityStream = responseEntity.getContent()) {
				return remoteEntityService.createOrUpdateEntity(nodeRef, destNodeRef, properties, entityStream, RemoteEntityFormat.xml, this);
			}
		} catch (AuthenticationException | IOException e) {
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

			HttpClient httpClient = HttpClientBuilder.create().build();
			BasicScheme basicScheme = new BasicScheme(Charset.forName("UTF-8"));
			HttpClientContext httpContext = HttpClientContext.create();

			HttpGet entityUrl = new HttpGet(url);
			basicScheme.authenticate(new UsernamePasswordCredentials(remoteUser, remotePwd), entityUrl, httpContext);

			HttpResponse httpResponse = httpClient.execute(entityUrl, httpContext);
			HttpEntity responseEntity = httpResponse.getEntity();

			try (InputStream dataStream = responseEntity.getContent()) {
				remoteEntityService.addOrUpdateEntityData(destNodeRef, dataStream, RemoteEntityFormat.xml);
			}
		} catch (AuthenticationException | IOException e) {
			throw new BeCPGException(e);
		}

	}

}
