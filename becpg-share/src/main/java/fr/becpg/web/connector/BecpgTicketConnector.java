package fr.becpg.web.connector;

import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.RemoteConfigElement.ConnectorDescriptor;
import org.springframework.extensions.webscripts.RequestCachingConnector;
import org.springframework.extensions.webscripts.connector.AlfrescoAuthenticator;
import org.springframework.extensions.webscripts.connector.ConnectorContext;
import org.springframework.extensions.webscripts.connector.Credentials;
import org.springframework.extensions.webscripts.connector.RemoteClient;

public class BecpgTicketConnector extends RequestCachingConnector {

	public static final String PARAM_TICKETNAME_TICKET = "ticket";
	private static final String PARAM_TICKETNAME_ALF_TICKET = "alf_ticket";

	public BecpgTicketConnector(ConnectorDescriptor descriptor, String endpoint) {
		super(descriptor, endpoint);
	}

	@Override
	protected void applyRequestAuthentication(RemoteClient remoteClient, ConnectorContext context) {
		String alfTicket = null;
		String userName = "guest";
		String instanceName = null;

		if (context != null) {
			alfTicket = context.getParameters().get(PARAM_TICKETNAME_ALF_TICKET);
		}

		if (getCredentials() != null) {

			userName = (String) getCredentials().getProperty(Credentials.CREDENTIAL_USERNAME);

			// if this connector is managing session info
			if (getConnectorSession() != null) {
				// apply alfresco ticket from connector session - i.e. previous login attempt
				alfTicket = getConnectorSession().getParameter(AlfrescoAuthenticator.CS_PARAM_ALF_TICKET);
			}
		}

		Config remoteConfig = remoteClient.getConfigService().getConfig("Remote");
		if (remoteConfig != null && remoteConfig.hasConfigElement("becpg-instance")) {
			instanceName = remoteConfig.getConfigElementValue("becpg-instance");
		}

		if (alfTicket != null) {
			remoteClient.setTicket(getCurrentAuthToken(alfTicket, userName, instanceName));
			remoteClient.setTicketName(PARAM_TICKETNAME_TICKET);
		}
	}

	public String getCurrentAuthToken(String alfTicket, String userName, String instanceName) {
		String currentUserName = (instanceName != null ? instanceName.replace("|true", "") : "default") + "$" + userName;
		if (!currentUserName.contains("@") || instanceName == null || !instanceName.contains("|true")) {
			currentUserName += "@default";
		}
		currentUserName += "#" + alfTicket;

		return java.util.Base64.getEncoder().encodeToString(currentUserName.getBytes());
	}
	
	

}