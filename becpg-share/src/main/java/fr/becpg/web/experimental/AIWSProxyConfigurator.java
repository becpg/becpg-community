package fr.becpg.web.experimental;

import java.text.MessageFormat;

import org.springframework.extensions.webscripts.connector.ConnectorService;

import jakarta.websocket.server.ServerEndpointConfig;

/**
 * <p>AIWSProxyConfigurator class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class AIWSProxyConfigurator extends ServerEndpointConfig.Configurator{

	private static ConnectorService connectorService;

	/**
	 * <p>Setter for the field <code>connectorService</code>.</p>
	 *
	 * @param connectorService a {@link org.springframework.extensions.webscripts.connector.ConnectorService} object
	 */
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
	
	/** {@inheritDoc} */
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
