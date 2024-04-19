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

	public BecpgTicketConnector(ConnectorDescriptor descriptor, String endpoint) {
		super(descriptor, endpoint);
	}

	@Override
	protected void applyRequestAuthentication(RemoteClient remoteClient, ConnectorContext context) {
		String currentAuthToken = getCurrentAuthToken(remoteClient, context);

		if (currentAuthToken != null) {
			remoteClient.setTicket(currentAuthToken);
			remoteClient.setTicketName(PARAM_TICKETNAME_TICKET);
		}
	}

	public String getCurrentAuthToken(RemoteClient remoteClient, ConnectorContext context) {

		if (context != null) {
			//beCPG ticket is passed in URI
			String becpgTicket = context.getParameters().get(PARAM_TICKETNAME_TICKET);
			if (becpgTicket != null) {
				return becpgTicket;
			}
		}

		//Else connector are authenticating again'st Alfresco
		String alfTicket = null;
		String userName = "guest";
		String instanceName = null;

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

		return getCurrentAuthToken(alfTicket, userName, instanceName);
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
