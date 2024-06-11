package fr.becpg.web.connector;

import org.springframework.extensions.config.RemoteConfigElement.ConnectorDescriptor;
import org.springframework.extensions.webscripts.connector.AlfrescoAuthenticator;
import org.springframework.extensions.webscripts.connector.ConnectorContext;
import org.springframework.extensions.webscripts.connector.HttpConnector;
import org.springframework.extensions.webscripts.connector.RemoteClient;

/**
 * <p>BecpgTicketConnector class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class BecpgTicketConnector extends HttpConnector {

	/**
	 * <p>Constructor for BecpgTicketConnector.</p>
	 *
	 * @param descriptor a {@link org.springframework.extensions.config.RemoteConfigElement.ConnectorDescriptor} object
	 * @param endpoint a {@link java.lang.String} object
	 */
	public BecpgTicketConnector(ConnectorDescriptor descriptor, String endpoint) {
		super(descriptor, endpoint);
	}

	/** {@inheritDoc} */
	@Override
	protected void applyRequestAuthentication(RemoteClient remoteClient, ConnectorContext context) {
		String currentAuthToken = getCurrentAuthToken(context);

		if (currentAuthToken != null) {
			remoteClient.setTicket(currentAuthToken);
			remoteClient.setTicketName(BecpgTicketAuthenticator.PARAM_TICKETNAME_TICKET);
		}
	}

	/**
	 * <p>getCurrentAuthToken.</p>
	 *
	 * @param context a {@link org.springframework.extensions.webscripts.connector.ConnectorContext} object
	 * @return a {@link java.lang.String} object
	 */
	public String getCurrentAuthToken(ConnectorContext context) {
		String ticket = null;

		if (context != null) {
			//beCPG ticket is passed in URI
			ticket = context.getParameters().get(BecpgTicketAuthenticator.PARAM_TICKETNAME_TICKET);

		}

		if (getCredentials() != null) {

			// if this connector is managing session info
			if (getConnectorSession() != null) {
				// apply alfresco ticket from connector session - i.e. previous login attempt
				ticket = getConnectorSession().getParameter(AlfrescoAuthenticator.CS_PARAM_ALF_TICKET);
			}
		}

		return ticket;
	}

}
