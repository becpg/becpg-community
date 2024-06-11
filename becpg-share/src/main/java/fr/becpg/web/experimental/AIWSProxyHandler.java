package fr.becpg.web.experimental;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.config.RemoteConfigElement.EndpointDescriptor;
import org.springframework.extensions.webscripts.connector.ConnectorService;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.CloseReason;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

/**
 * <p>AIWSProxyHandler class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@ServerEndpoint(value = "/aiws", configurator = AIWSProxyConfigurator.class)
public class AIWSProxyHandler {

	private static final Log logger = LogFactory.getLog(AIWSProxyHandler.class);
	private static final String AI_ENDPOINT = "ai";
	private static final String WS_PROTOCOL = "ws";
	private static final String WSS_PROTOCOL = "wss";

	private Session remoteSession;
	private ConnectorService connectorService;

	/**
	 * <p>Setter for the field <code>connectorService</code>.</p>
	 *
	 * @param connectorService a {@link org.springframework.extensions.webscripts.connector.ConnectorService} object
	 */
	public void setConnectorService(ConnectorService connectorService) {
		this.connectorService = connectorService;
	}

	@ClientEndpoint
	public class AIWSProxyClient {

		private Session localSession;
		

		public AIWSProxyClient(Session localSession) {
			super();
			this.localSession = localSession;
		}

		@OnMessage
		public void onMessage(String message, Session session) {
			try {
				if(logger.isTraceEnabled()) {
					logger.trace("Forward message to becpg-ai: ");
					logger.trace(message);
				}
				
				forwardMessage(localSession, message);
			} catch (IOException e) {
				handleError("Error sending message to AI server: " + e.getMessage(), e, session);
			}
		}

		@OnClose
		public void onClose(CloseReason reason) {
			logger.debug("Remote closing: "+reason.toString());
			if (localSession != null && localSession.isOpen()) {
				closeSession(localSession);
			}
		}

		@OnError
		public void onError(Session session, Throwable throwable) {
			handleError("Error in WebSocket connection: " + throwable.getMessage(), throwable, session);
			closeSession(localSession);
		}
	}

	/**
	 * <p>onOpen.</p>
	 *
	 * @param session a {@link jakarta.websocket.Session} object
	 * @param config a {@link jakarta.websocket.EndpointConfig} object
	 */
	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		try {
			EndpointDescriptor desc = connectorService.getRemoteConfig().getEndpointDescriptor(AI_ENDPOINT);
			String wsUrl = createWSURL(desc.getEndpointUrl(), session.getQueryString());
			
			logger.debug("Connecting to: " + wsUrl);

			remoteSession = ContainerProvider.getWebSocketContainer().connectToServer(new AIWSProxyClient(session), new URI(wsUrl));
			remoteSession.setMaxTextMessageBufferSize(5 * 1024 * 1024); // Set the buffer size to 5MB
			remoteSession.setMaxIdleTimeout(60000);
			
			
		} catch (DeploymentException | IOException | URISyntaxException e) {
			handleError("Error connecting to AI server: " + e.getMessage(), e, session);
		}
	}

	/**
	 * <p>onClose.</p>
	 *
	 * @param reason a {@link jakarta.websocket.CloseReason} object
	 */
	@OnClose
	public void onClose(CloseReason reason) {
		closeSession(remoteSession);
	}

	/**
	 * <p>onMessage.</p>
	 *
	 * @param message a {@link java.lang.String} object
	 * @param session a {@link jakarta.websocket.Session} object
	 */
	@OnMessage
	public void onMessage(String message, Session session) {
		try {
			if(logger.isTraceEnabled()) {
				logger.trace("Forward response from becpg-ai: " );
				logger.trace(message);
			}
			
			forwardMessage(remoteSession, message);
		} catch (IOException e) {
			handleError("Error sending message to AI server: " + e.getMessage(), e, session);
		}
	}

	/**
	 * <p>onError.</p>
	 *
	 * @param session a {@link jakarta.websocket.Session} object
	 * @param throwable a {@link java.lang.Throwable} object
	 */
	@OnError
	public void onError(Session session, Throwable throwable) {
		handleError("Error in WebSocket connection: " + throwable.getMessage(), throwable, session);
		closeSession(remoteSession);
	}

	private String createWSURL(String endpointUrl, String queryString) {
		String protocol = endpointUrl.contains("443") ? WSS_PROTOCOL : WS_PROTOCOL;
		return endpointUrl.replace("http", protocol) + "/ws" + (queryString!=null  ? "?" + queryString : "");
	}

	private void forwardMessage(Session session, String message) throws IOException {
		if (session != null && session.isOpen()) {
			session.getBasicRemote().sendText(message);
		} else {
			logger.debug("Session is not OPEN");
			closeSession(session);
		}
	}

	private void closeSession(Session session) {
		try {
			if (session != null) {
				session.close();
			}
		} catch (IOException e) {
			logger.error("Error closing session: " + e.getMessage(), e);
		}
	}

	private void handleError(String message, Throwable throwable, Session session) {
		logger.error(message, throwable);
		closeSession(session);
	}

}
