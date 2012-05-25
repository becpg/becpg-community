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
 * Implements the OpenID OAuth Message Extension Request
 * 
 * The OpenID OAuth extension is described at
 * http://svn.openid.net/repos/specifications
 * /oauth_hybrid/1.0/trunk/openid_oauth_extension.html
 * 
 * @see OAuthMessage
 * @author Bram Walet
 * 
 */
public class OAuthRequest extends OAuthMessage {

	private static Log _log = LogFactory.getLog(OAuthRequest.class);

	private static final boolean DEBUG = _log.isDebugEnabled();

	protected final static List<String> OAUTH_FIELDS = Arrays
			.asList(new String[] { "consumer", "scope" });

	/**
	 * Constructs a OAuth Request with an empty parameter list.
	 */
	protected OAuthRequest() {
		if (DEBUG)
			_log.debug("Created empty Pape request.");
	}

	/**
	 * Constructs a OAuth Request from a parameter list.
	 * <p>
	 * The parameter list can be extracted from a received message with the
	 * getExtensionParams method of the Message class, and MUST NOT contain the
	 * "openid.<extension_alias>." prefix.
	 */
	protected OAuthRequest(ParameterList params) {
		super(params);
	}

	/**
	 * Constructs a Pape Request with an empty parameter list.
	 */
	public static OAuthRequest createOAuthRequest() {
		return new OAuthRequest();
	}

	/**
	 * Constructs a PapeRequest from a parameter list.
	 * <p>
	 * The parameter list can be extracted from a received message with the
	 * getExtensionParams method of the Message class, and MUST NOT contain the
	 * "openid.<extension_alias>." prefix.
	 */
	public static OAuthRequest createOAuthRequest(ParameterList params)
			throws MessageException {
		OAuthRequest req = new OAuthRequest(params);

		req.validate();

		if (DEBUG)
			_log.debug("Created PAPE request from parameter list:\n" + params);

		return req;
	}

	public void validate() throws MessageException {
		if (!_parameters.hasParameter("consumer")) {
			throw new MessageException(
					"consumer is required in an OAuth request.",
					OAuthMessage.OAUTH_ERROR);
		}

		Iterator<?> it = _parameters.getParameters().iterator();
		while (it.hasNext()) {
			String paramName = ((Parameter) it.next()).getKey();
			if (!OAUTH_FIELDS.contains(paramName)) {
				throw new MessageException(
						"Invalid parameter name in OAuth request: " + paramName,
						OAuthMessage.OAUTH_ERROR);
			}
		}
	}

	/**
	 * Gets the consumer parameter value
	 * 
	 * @return
	 */
	public String getConsumer() {
		return getParameterValue("consumer");
	}

	/**
	 * sets a new value for the consumer parameter the previous value will be
	 * overwritten
	 * 
	 * @param consumer
	 *            URI of consumer to be set.
	 */
	public void setConsumer(String consumer) {
		set("consumer", consumer);
	}

}
