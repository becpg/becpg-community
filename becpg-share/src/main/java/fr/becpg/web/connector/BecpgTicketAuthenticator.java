package fr.becpg.web.connector;

import org.springframework.extensions.webscripts.connector.AlfrescoAuthenticator;
import org.springframework.extensions.webscripts.connector.ConnectorSession;

public class BecpgTicketAuthenticator extends AlfrescoAuthenticator {

	@Override
	public boolean isAuthenticated(String endpoint, ConnectorSession connectorSession) {
		return true;
	}

}
