package fr.becpg.repo.entity.remote.impl;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.common.BeCPGException;
import fr.becpg.repo.entity.remote.EntityProviderCallBack;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;

public class HttpEntityProviderCallback implements EntityProviderCallBack {

	private static final Log logger = LogFactory.getLog(HttpEntityProviderCallback.class);

	private final String remoteServer;

	private final String remoteUser;

	private final String remotePwd;

	private final RemoteEntityService remoteEntityService;

	private Map<NodeRef, NodeRef> visitedNodes = new HashMap<>();

	public HttpEntityProviderCallback(String remoteServer, String remoteUser, String remotePwd,
			RemoteEntityService remoteEntityService) {
		super();
		this.remoteServer = remoteServer;
		this.remoteUser = remoteUser;
		this.remotePwd = remotePwd;
		this.remoteEntityService = remoteEntityService;
	}

	@Override
	public NodeRef provideNode(NodeRef nodeRef, Map<NodeRef, NodeRef> cache) throws BeCPGException {
		return provideNode(nodeRef, null, cache);
	}

	@Override
	public NodeRef provideNode(NodeRef nodeRef, NodeRef destNodeRef,
			Map<NodeRef, NodeRef> cache) throws BeCPGException {
		try {
			String url = remoteServer + "?nodeRef=" + nodeRef.toString();
			logger.debug("Try getting nodeRef  from : " + url);
			logger.debug("User : " + remoteUser);
			logger.debug("Password : " + remotePwd);

			HttpGet entityUrl = new HttpGet(url);

			HttpResponse httpResponse = getResponse(entityUrl);
			HttpEntity responseEntity = httpResponse.getEntity();

			// case 1 : it's added to the map and has no value (being visited
			// somewhere in time)
			if (visitedNodes.containsKey(nodeRef) && visitedNodes.get(nodeRef) == null) {
				return nodeRef;
				// case 2 : it's added and already visited
			} else if (visitedNodes.containsKey(nodeRef) && visitedNodes.get(nodeRef) != null) {
				return visitedNodes.get(nodeRef);
			} else {
				// case 3 : not visited yet, put in map and visit
				visitedNodes.put(nodeRef, null);
				try (InputStream entityStream = responseEntity.getContent()) {
					NodeRef res = remoteEntityService.internalCreateOrUpdateEntity(nodeRef, destNodeRef,
							entityStream, RemoteEntityFormat.xml, this, cache);
					visitedNodes.put(nodeRef, res);

					return res;
				}
			}

		} catch (IOException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
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

			HttpGet entityUrl = new HttpGet(url);

			HttpResponse httpResponse = getResponse(entityUrl);
			HttpEntity responseEntity = httpResponse.getEntity();

			try (InputStream dataStream = responseEntity.getContent()) {
				remoteEntityService.addOrUpdateEntityData(destNodeRef, dataStream, RemoteEntityFormat.xml);
			}
		} catch (IOException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			throw new BeCPGException(e);
		}

	}

	private HttpResponse getResponse(HttpGet entityUrl) throws ClientProtocolException, IOException,
			NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

		HttpClientBuilder cb = HttpClientBuilder.create();

		HttpClientContext httpContext = HttpClientContext.create();

		HttpClient httpClient = cb.build();

		entityUrl.addHeader("Accept-Language", I18NUtil.getLocale().getLanguage());

		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(remoteUser, remotePwd);
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), creds);
		httpContext.setCredentialsProvider(credsProvider);

		return httpClient.execute(entityUrl, httpContext);
	}

}