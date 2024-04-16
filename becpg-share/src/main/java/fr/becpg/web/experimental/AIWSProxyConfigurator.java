package fr.becpg.web.experimental;

import java.text.MessageFormat;
import java.util.Arrays;

import org.springframework.extensions.webscripts.connector.ConnectorService;

import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

public class AIWSProxyConfigurator extends ServerEndpointConfig.Configurator{

	private static ConnectorService connectorService;

	public  void setConnectorService(ConnectorService connectorService) {
		AIWSProxyConfigurator.connectorService = connectorService;
	}

//	 @Override
//	    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
//	        
//
//	        response.getHeaders().put(HandshakeRequest.SEC_WEBSOCKET_PROTOCOL, Arrays.asList("v10.stomp", "v11.stomp","v12.stomp"));
//	        
//	        // Call super method to proceed with the default handshake modification
//	        super.modifyHandshake(sec, request, response);
//	    }
	
	@Override
	public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
		T endpoint = super.getEndpointInstance(endpointClass);

		if (endpoint instanceof AIWSProxyHandler) {
			((AIWSProxyHandler) endpoint).setConnectorService(connectorService);
		} else {
			throw new InstantiationException(
					MessageFormat.format("Expected instanceof \"{0}\". Got instanceof \"{1}\".", AIWSProxyHandler.class, endpoint.getClass()));
		}

		return endpoint;
	}
	
}
