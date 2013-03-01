package org.saiku.web.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.saiku.service.ISessionService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import fr.becpg.olap.authentication.AlfrescoUserDetails;

public class AlfrescoSessionService implements ISessionService {

	private static final Log logger = LogFactory.getLog(SessionService.class);

	private AuthenticationManager authenticationManager;

	Map<Object, Map<String, Object>> sessionHolder = new HashMap<Object, Map<String, Object>>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.saiku.web.service.ISessionService#setAuthenticationManager(org.
	 * springframework.security.authentication.AuthenticationManager)
	 */
	public void setAuthenticationManager(AuthenticationManager auth) {
		this.authenticationManager = auth;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.saiku.web.service.ISessionService#login(javax.servlet.http.
	 * HttpServletRequest, java.lang.String, java.lang.String)
	 */
	public Map<String, Object> login(HttpServletRequest req, String username, String password) {
		if (authenticationManager != null) {
			authenticate(req, username, password);
		}

		return getSession();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.saiku.web.service.ISessionService#logout(javax.servlet.http.
	 * HttpServletRequest)
	 */
	public void logout(HttpServletRequest req) {
		if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null) {
			Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			if (sessionHolder.containsKey(p)) {
				sessionHolder.remove(p);
			}
		}
		SecurityContextHolder.clearContext();
		HttpSession session = req.getSession(true);
		session.invalidate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.saiku.web.service.ISessionService#authenticate(javax.servlet.http
	 * .HttpServletRequest, java.lang.String, java.lang.String)
	 */
	public void authenticate(HttpServletRequest req, String username, String password) {
		try {
			UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
			// token.setDetails(new WebAuthenticationDetails(req));
			Authentication authentication = this.authenticationManager.authenticate(token);
			logger.debug("Logging in with ["+ authentication.getPrincipal()+"]");
			SecurityContextHolder.getContext().setAuthentication(authentication);
		} catch (BadCredentialsException bd) {
			throw new RuntimeException("Authentication failed for: " + username, bd);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.saiku.web.service.ISessionService#getSession(javax.servlet.http.
	 * HttpServletRequest)
	 */
	public Map<String, Object> getSession() {
		return getAllSessionObjects();
	}

	public Map<String, Object> getAllSessionObjects() {
		Map<String, Object> ret = new HashMap<String, Object>();

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth != null && auth.getPrincipal() != null && (auth.getPrincipal() instanceof AlfrescoUserDetails)) {
			AlfrescoUserDetails userDetails = (AlfrescoUserDetails) auth.getPrincipal();

			ret.put("username", userDetails.getUsername());
			ret.put("sessionid", userDetails.getSessionId());
			List<String> roles = new ArrayList<String>();
			for (GrantedAuthority ga : userDetails.getAuthorities()) {
				roles.add(ga.getAuthority());
			}
			ret.put("roles", roles);
		}
		
		return ret;

	}
}