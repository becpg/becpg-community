package fr.becpg.web.connector;

import org.springframework.extensions.surf.exception.AuthenticationException;
import org.springframework.extensions.webscripts.connector.AbstractAuthenticator;
import org.springframework.extensions.webscripts.connector.ConnectorSession;
import org.springframework.extensions.webscripts.connector.Credentials;

public class BecpgTicketAuthenticator extends AbstractAuthenticator {


	public static final String PARAM_TICKETNAME_TICKET = "ticket";

	@Override
	public boolean isAuthenticated(String endpoint, ConnectorSession connectorSession) {
		 return (connectorSession.getParameter(PARAM_TICKETNAME_TICKET) != null) ||
	               (connectorSession.getCookieNames().length != 0);
	}

	@Override
	public ConnectorSession authenticate(String endpoint, Credentials credentials, ConnectorSession connectorSession) throws AuthenticationException {
//		 if (connectorSession != null)
//         {
//             connectorSession.setParameter(PARAM_TICKETNAME_TICKET, ticket);
//             
//             // signal that this succeeded
//             cs = connectorSession;
//         }
		return connectorSession;
	}

}
