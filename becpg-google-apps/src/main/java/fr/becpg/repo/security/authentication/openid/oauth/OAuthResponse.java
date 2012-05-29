package fr.becpg.repo.security.authentication.openid.oauth;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.message.MessageException;
import org.openid4java.message.Parameter;
import org.openid4java.message.ParameterList;

/**
 * Implements the OpenID OAuth Message Extension Response
 * 
 * The OpenID OAuth extension is described at
 * http://svn.openid.net/repos/specifications
 * /oauth_hybrid/1.0/trunk/openid_oauth_extension.html
 * 
 * @see OAuthMessage
 * @author Bram Walet
 * 
 */
public class OAuthResponse extends OAuthMessage {

	private static Log _log = LogFactory.getLog(OAuthResponse.class);

	private static final boolean DEBUG = _log.isDebugEnabled();

	protected final static List<String> OAUTH_FIELDS = Arrays
			.asList(new String[] { "request_token", "scope", });

	// request_token (required) & scope (optional)

	/**
	 * Constructs a OAuth Response with an empty parameter list.
	 */
	protected OAuthResponse() {
		if (DEBUG)
			_log.debug("Created empty OAuth response.");
	}

	/**
	 * Constructs a OAuth Response with an empty parameter list.
	 */
	public static OAuthResponse creatOAuthResponse() {
		return new OAuthResponse();
	}

	/**
	 * Constructs an OAuth Response from a parameter list.
	 * <p>
	 * The parameter list can be extracted from a received message with the
	 * getExtensionParams method of the Message class, and MUST NOT contain the
	 * "openid.<extension_alias>." prefix.
	 */
	protected OAuthResponse(ParameterList params) {
		super(params);
	}

	public static OAuthResponse createOAuthResponse(ParameterList params)
			throws MessageException {
		OAuthResponse resp = new OAuthResponse(params);

		resp.validate();

		if (DEBUG)
			_log
					.debug("Created OAuth response from parameter list:\n"
							+ params);

		return resp;
	}

	/**
	 * Checks the validity of the extension.
	 * <p>
	 * Used when constructing a extension from a parameter list.
	 * 
	 * @throws MessageException
	 *             if the OAuthMessage is not valid.
	 */
	private void validate() throws MessageException {
		if (!_parameters.hasParameter("request_token")) {
			throw new MessageException(
					"request_token is required in a OAuth response.",
					OAuthMessage.OAUTH_ERROR);
		}

		Iterator<?> it = _parameters.getParameters().iterator();
		while (it.hasNext()) {
			String paramName = ((Parameter) it.next()).getKey();

			if (OAUTH_FIELDS.contains(paramName))
				continue;

			throw new MessageException("Invalid parameter in OAuth response: "
					+ paramName, OAuthMessage.OAUTH_ERROR);
		}
	}

	/**
	 * Gets the value of the parameter request_token
	 * 
	 * @return String The OAuth request token that the OpenID provider has
	 *         provided. This token needs to be authorized before it is an
	 *         access token.
	 */
	public String getRequestToken() {
		return getParameterValue("request_token");
	}

	/**
	 * Sets a new value of the parameter request_token. The previous value will
	 * be overwritten.
	 * 
	 * @param requestToken
	 *            The OAuth request token that needs to be set.
	 */
	public void setRequestToken(String requestToken) {
		set("request_token", requestToken);
	}

}
