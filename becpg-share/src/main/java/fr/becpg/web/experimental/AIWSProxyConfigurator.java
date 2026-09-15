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
public class AIWSProxyConfigurator extends ServerEndpointConfig.Configurator {

	/** Constant <code>connectorService</code> */
	private static ConnectorService connectorService;

	/**
	 * <p>Setter for the field <code>connectorService</code>.</p>
	 *
	 * @param connectorService a {@link org.springframework.extensions.webscripts.connector.ConnectorService} object
	 */
	public void setConnectorService(ConnectorService connectorService) {
		AIWSProxyConfigurator.connectorService = connectorService;
	}

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
