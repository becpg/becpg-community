package fr.becpg.web.experimental;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.config.RemoteConfigElement.EndpointDescriptor;
import org.springframework.extensions.surf.exception.ConnectorServiceException;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.ConnectorService;

import fr.becpg.web.connector.BecpgTicketConnector;
import jakarta.servlet.http.HttpSession;
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
 * @author matthieu
 */
@ServerEndpoint(value = "/aiws", configurator = AIWSProxyConfigurator.class)
@ClientEndpoint
public class AIWSProxyHandler {

    private static final Log logger = LogFactory.getLog(AIWSProxyHandler.class);

    private static final String AI_ENDPOINT = "ai";

    private Session remoteSession;
    private HttpSession httpSession;

    private ConnectorService connectorService;

    public void setConnectorService(ConnectorService connectorService) {
        this.connectorService = connectorService;
    }

    public void setHttpSession(HttpSession httpSession) {
        if (this.httpSession != null) {
            throw new IllegalStateException("HttpSession has already been set!");
        }

        this.httpSession = httpSession;
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        try {
            EndpointDescriptor desc = connectorService.getRemoteConfig().getEndpointDescriptor(AI_ENDPOINT);

            Connector ticketConnector = connectorService.getConnector(AI_ENDPOINT, httpSession);
            String ticket = ticketConnector.getConnectorSession().getParameter(BecpgTicketConnector.PARAM_TICKETNAME_TICKET);

            remoteSession = ContainerProvider.getWebSocketContainer().connectToServer(this, new URI(createWSURL(desc.getEndpointUrl(), ticket)));
        } catch (ConnectorServiceException | DeploymentException | IOException | URISyntaxException e) {
            logger.error("Error connecting to AI server: " + e.getMessage(), e);
            closeSession(session);
        }
    }

    @OnClose
    public void onClose(CloseReason reason) {
        if (remoteSession != null && remoteSession.isOpen()) {
            try {
                remoteSession.close();
            } catch (IOException e) {
                logger.error("Error closing remote websocket session: " + e.getMessage(), e);
            }
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            if (remoteSession != null && remoteSession.isOpen()) {
                remoteSession.getBasicRemote().sendText(message);
            } else {
                session.close();
            }
        } catch (IOException e) {
            logger.error("Error sending message to AI server: " + e.getMessage(), e);
            closeSession(session);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.error("Error in WebSocket connection: " + throwable.getMessage(), throwable);
        closeSession(session);
    }

    private String createWSURL(String endpointUrl, String ticket) {
        String protocol = endpointUrl.contains("443") ? "wss" : "ws";
        return endpointUrl.replace("http", protocol).replace("/api", "/ws") + "?ticket=" + ticket;
    }

    private void closeSession(Session session) {
        try {
            session.close();
        } catch (IOException e) {
            logger.error("Error closing session: " + e.getMessage(), e);
        }
    }
}
