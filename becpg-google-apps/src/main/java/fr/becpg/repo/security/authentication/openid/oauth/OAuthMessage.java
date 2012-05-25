package fr.becpg.repo.security.authentication.openid.oauth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.message.MessageException;
import org.openid4java.message.MessageExtension;
import org.openid4java.message.MessageExtensionFactory;
import org.openid4java.message.Parameter;
import org.openid4java.message.ParameterList;

/**
 * 
 * Base class for OpenID OAuth Message Extension
 * 
 * This Message Extension allows support for the OpenID OAuth extension as
 * described on
 * http://svn.openid.net/repos/specifications/oauth_hybrid/1.0/trunk
 * /openid_oauth_extension.html
 * 
 * @see Message, MessageExtension
 * @author Bram Walet
 * 
 */
public class OAuthMessage implements MessageExtension, MessageExtensionFactory {

	private static Log _log = LogFactory.getLog(OAuthMessage.class);

	private static final boolean DEBUG = _log.isDebugEnabled();

	public static final int OAUTH_ERROR = 0x0C40;

	/**
	 * The OAuth authentication Type URI.
	 */

	public static final String OPENID_NS_OAUTH = "http://specs.openid.net/extensions/oauth/1.0";

	protected static final String SCOPE_DELIMITER = " ";

	/**
	 * The OAuth authentication extension-specific parameters.
	 * <p>
	 * The openid.<extension_alias> prefix is not part of the parameter names
	 */
	protected ParameterList _parameters;

	/**
	 * Constructs an empty (no parameters) OAuth authentication extension.
	 */
	public OAuthMessage() {
		_parameters = new ParameterList();

		if (DEBUG)
			_log.debug("Created empty OAuthMessage.");
	}

	/**
	 * Constructs an OAuth authentication extension with a specified list of
	 * parameters.
	 * <p>
	 * The parameter names in the list should not contain the
	 * openid.<extension_alias>.
	 */
	public OAuthMessage(ParameterList params) {
		_parameters = params;

		if (DEBUG)
			_log.debug("Created OAuthMessage from parameter list:\n" + params);
	}

	/**
	 * Gets a the value of the parameter with the specified name.
	 * 
	 * @param name
	 *            The name of the parameter, without the
	 *            openid.<extension_alias> prefix.
	 * @return The parameter value, or null if not found.
	 */
	public ParameterList getParameters() {
		return _parameters;
	}

	/**
	 * Gets the Type URI that identifies the OAuth authentication extension.
	 */
	public String getTypeUri() {
		return OPENID_NS_OAUTH;
	}

	/**
	 * OAuth authentication doesnt' implement authentication services.
	 * 
	 * @return false
	 */
	public boolean providesIdentifier() {
		return false;
	}

	/**
	 * Sets the extension's parameters to the supplied list.
	 * <p>
	 * The parameter names in the list should not contain the
	 * openid.<extension_alias> prefix.
	 */
	public void setParameters(ParameterList params) {
		_parameters = params;

		if (DEBUG)
			_log.debug("Created AXMessage from parameter list:\n" + params);

	}

	/**
	 * OAuth authentication parameters are not REQUIRED to be signed.
	 * 
	 * @return false
	 */
	public boolean signRequired() {
		return false;
	}

	/**
	 * Instantiates the apropriate OAuth authentication object ( request /
	 * response) for the supplied parameter list.
	 * 
	 * @param parameterList
	 *            The Attribute Exchange specific parameters (without the
	 *            openid.<ext_alias> prefix) extracted from the openid message.
	 * @param isRequest
	 *            Indicates whether the parameters were extracted from an OpenID
	 *            request (true), or from an OpenID response.
	 * @return MessageExtension implementation for the supplied extension
	 *         parameters.
	 * @throws MessageException
	 *             If a Attribute Exchange object could not be instantiated from
	 *             the supplied parameter list.
	 */
	public MessageExtension getExtension(ParameterList parameterList,
			boolean isRequest) throws MessageException {

		// if (parameterList.hasParameter("consumer")) {
		if (isRequest) {
			return OAuthRequest.createOAuthRequest(parameterList);

		} else {
			return OAuthResponse.createOAuthResponse(parameterList);
		}

		// throw new MessageException(
		// "Unable to determine the mode for the OAuth Extension");

	}

	/**
	 * Gets a the value of the parameter with the specified name.
	 * 
	 * @param name
	 *            The name of the parameter, without the
	 *            openid.<extension_alias> prefix.
	 * @return The parameter value, or null if not found.
	 */
	public String getParameterValue(String name) {
		return _parameters.getParameterValue(name);
	}

	/**
	 * Sets the value for the parameter with the specified name.
	 * 
	 * @param name
	 *            The name of the parameter, without the
	 *            openid.<extension_alias> prefix.
	 */
	protected void set(String name, String value) {
		Parameter param = new Parameter(name, value);
		_parameters.set(param);
	}

	/**
	 * Get the value of the parameter scope as string (separated by ',').
	 * 
	 * @return String String of URI's (separated by ',') for which the OAuth
	 *         token is valid for.
	 */
	public String getScopes() {
		return getParameterValue("scope");
	}

	/**
	 * Get the value of the parameter scope as list.
	 * 
	 * @return List List of URI's for which the OAuth token is valid for.
	 */
	public List<String> getScopesList() {
		String scopes = getScopes();
		if (scopes == null)
			return new ArrayList<String>();
		return Arrays.asList(scopes.split(SCOPE_DELIMITER));
	}

	/**
	 * Sets a new value of the parameter scope. The previous value will be
	 * overwritten.
	 * 
	 * @param scopes
	 *            The value of the parameter scope that needs to be set.
	 */
	public void setScopes(String scopes) {
		set("scope", scopes);
	}

	public void addScope(String scope) {
		String scopes = getScopes();

		if (scopes == null)
			setScopes(scopes);
		else
			setScopes(scopes + SCOPE_DELIMITER + scope);
	}
}
