package fr.becpg.web.experimental;

import java.text.MessageFormat;

import org.springframework.extensions.webscripts.connector.ConnectorService;

import jakarta.servlet.http.HttpSession;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

public class AIWSProxyConfigurator extends ServerEndpointConfig.Configurator {

	private HttpSession httpSession;

	private static ConnectorService connectorService;

	public  void setConnectorService(ConnectorService connectorService) {
		AIWSProxyConfigurator.connectorService = connectorService;
	}

	@Override
	public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
		httpSession = (HttpSession) request.getHttpSession();
		super.modifyHandshake(sec, request, response);
	}

	@Override
	public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
		T endpoint = super.getEndpointInstance(endpointClass);

		if (endpoint instanceof AIWSProxyHandler) {
			// The injection point:
			((AIWSProxyHandler) endpoint).setHttpSession(httpSession);
			((AIWSProxyHandler) endpoint).setConnectorService(connectorService);
		} else {
			throw new InstantiationException(
					MessageFormat.format("Expected instanceof \"{0}\". Got instanceof \"{1}\".", AIWSProxyHandler.class, endpoint.getClass()));
		}

		return endpoint;
	}

}
