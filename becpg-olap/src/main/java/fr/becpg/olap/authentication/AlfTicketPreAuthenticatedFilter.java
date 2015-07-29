package fr.becpg.olap.authentication;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;

public class AlfTicketPreAuthenticatedFilter extends RequestHeaderAuthenticationFilter {

	private String alfTicketParam = "ticket";
	

	protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {

		String principal = (String) request.getParameter(alfTicketParam);

		if (principal == null || principal.isEmpty()) {
			return super.getPreAuthenticatedPrincipal(request);
		}

		return principal;
	}

}
