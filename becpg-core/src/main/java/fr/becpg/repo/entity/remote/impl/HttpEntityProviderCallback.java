package fr.becpg.repo.entity.remote.impl;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.common.BeCPGException;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.remote.EntityProviderCallBack;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.entity.remote.RemoteParams;

/**
 * <p>HttpEntityProviderCallback class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class HttpEntityProviderCallback implements EntityProviderCallBack {

	private static final Log logger = LogFactory.getLog(HttpEntityProviderCallback.class);

	private final String remoteServer;

	private final String remoteUser;

	private final String remotePwd;

	private final RemoteEntityService remoteEntityService;

	private Map<NodeRef, NodeRef> visitedNodes = new HashMap<>();

	/**
	 * <p>Constructor for HttpEntityProviderCallback.</p>
	 *
	 * @param remoteServer a {@link java.lang.String} object.
	 * @param remoteUser a {@link java.lang.String} object.
	 * @param remotePwd a {@link java.lang.String} object.
	 * @param remoteEntityService a {@link fr.becpg.repo.entity.remote.RemoteEntityService} object.
	 */
	public HttpEntityProviderCallback(String remoteServer, String remoteUser, String remotePwd, RemoteEntityService remoteEntityService) {
		super();
		this.remoteServer = remoteServer;
		this.remoteUser = remoteUser;
		this.remotePwd = remotePwd;
		this.remoteEntityService = remoteEntityService;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef provideNode(NodeRef nodeRef, Map<NodeRef, NodeRef> cache) {
		return provideNode(nodeRef, null, cache);
	}

	/** {@inheritDoc} */
	@SuppressWarnings("deprecation")
	@Override
	public NodeRef provideNode(NodeRef nodeRef, NodeRef destNodeRef, Map<NodeRef, NodeRef> cache) {
		try {
			String url = remoteServer + "?nodeRef=" + nodeRef.toString();
			logger.debug("Try getting nodeRef  from : " + url);

			HttpGet entityUrl = new HttpGet(url);

			HttpResponse httpResponse = getResponse(entityUrl);

			if (httpResponse.getStatusLine().getStatusCode() == 200) {

				HttpEntity responseEntity = httpResponse.getEntity();

				// case 1 : it's added to the map and has no value (being
				// visited
				// somewhere in time)
				if (visitedNodes.containsKey(nodeRef) && (visitedNodes.get(nodeRef) == null)) {
					return nodeRef;
					// case 2 : it's added and already visited
				} else if (visitedNodes.containsKey(nodeRef) && (visitedNodes.get(nodeRef) != null)) {
					return visitedNodes.get(nodeRef);
				} else {
					// case 3 : not visited yet, put in map and visit
					visitedNodes.put(nodeRef, null);

					try (InputStream entityStream = responseEntity.getContent()) {
						NodeRef res = remoteEntityService.serviceRegistry().transactionService().getRetryingTransactionHelper().doInTransaction(() -> {

							// Only for transaction do not reenable it
							remoteEntityService.serviceRegistry().policyBehaviourFilter().disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
							remoteEntityService.serviceRegistry().policyBehaviourFilter().disableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);

							return remoteEntityService.internalCreateOrUpdateEntity(nodeRef, destNodeRef, entityStream,
									new RemoteParams(RemoteEntityFormat.xml), this, cache);

						}, false, false);

						visitedNodes.put(nodeRef, res);

						return res;
					}
				}

			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("Error calling " + url + " " + EntityUtils.toString(httpResponse.getEntity(), "UTF-8") + " status "
							+ httpResponse.getStatusLine().getStatusCode());
				}

				return null;
			}

		} catch (IOException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			throw new BeCPGException(e);
		}

	}

	/** {@inheritDoc} */
	@Override
	public void provideContent(NodeRef origNodeRef, NodeRef destNodeRef) {

		try {
			String url = remoteServer + "/data?nodeRef=" + origNodeRef.toString();
			logger.debug("Try getting data  from : " + url);

			HttpGet entityUrl = new HttpGet(url);

			HttpResponse httpResponse = getResponse(entityUrl);
			HttpEntity responseEntity = httpResponse.getEntity();

			try (InputStream dataStream = responseEntity.getContent()) {
				remoteEntityService.addOrUpdateEntityData(destNodeRef, dataStream, new RemoteParams(RemoteEntityFormat.xml));
			}
		} catch (IOException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
			throw new BeCPGException(e);
		}

	}

	private HttpResponse getResponse(HttpGet entityUrl) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

		HttpClientBuilder cb = HttpClientBuilder.create();
		
		if ("true".equals(System.getProperty("remote.ssl.trustAll"))) {
			SSLContextBuilder builder = org.apache.http.ssl.SSLContexts.custom();
			builder.loadTrustMaterial(null, new TrustStrategy() {
				@Override
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return true;
				}
			});
	
			SSLContext sslContext = builder.build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, (hostname, session) -> true);
	
			PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(
					RegistryBuilder.<ConnectionSocketFactory> create().register("https", sslsf).build());
	
			cb.setConnectionManager(cm);
		}

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
