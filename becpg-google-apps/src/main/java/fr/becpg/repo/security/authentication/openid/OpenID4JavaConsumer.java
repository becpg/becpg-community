package fr.becpg.repo.security.authentication.openid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.util.UrlUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.association.AssociationException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.Message;
import org.openid4java.message.MessageException;
import org.openid4java.message.MessageExtension;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.springframework.security.openid.AxFetchListFactory;
import org.springframework.security.openid.NullAxFetchListFactory;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationStatus;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.security.openid.OpenIDConsumer;
import org.springframework.security.openid.OpenIDConsumerException;
import org.springframework.util.StringUtils;

import fr.becpg.repo.security.authentication.openid.oauth.OAuthMessage;
import fr.becpg.repo.security.authentication.openid.oauth.OAuthRequest;
import fr.becpg.repo.security.authentication.openid.oauth.OAuthResponse;

/**
 * @author Ray Krueger
 * @author Luke Taylor
 * @author matthieu
 */

@SuppressWarnings("unchecked")
public class OpenID4JavaConsumer implements OpenIDConsumer {

	private static final String DISCOVERY_INFO_KEY = DiscoveryInformation.class.getName();

	private static final String ATTRIBUTE_LIST_KEY = "SPRING_SECURITY_OPEN_ID_ATTRIBUTES_FETCH_LIST";


	// ~ Instance fields
	// ================================================================================================

	protected final Log logger = LogFactory.getLog(getClass());

	private final ConsumerManager consumerManager;

	private final AxFetchListFactory attributesToFetchFactory;

	private SysAdminParams sysAdminParams;
	

	private String oauthScopes = null;
	

	// ~ Constructors
	// ===================================================================================================

	public OpenID4JavaConsumer() throws ConsumerException, MessageException {
		this(new ConsumerManager(), new NullAxFetchListFactory());
	}

	public void setOauthScopes(String oauthScopes) {
		this.oauthScopes = oauthScopes;
	}

	public void setSysAdminParams(SysAdminParams sysAdminParams) {
		this.sysAdminParams = sysAdminParams;
	}

	public OpenID4JavaConsumer(List<OpenIDAttribute> attributes) throws ConsumerException, MessageException {

		this(new ConsumerManager(), attributes);

	}

	public OpenID4JavaConsumer(ConsumerManager consumerManager, AxFetchListFactory attributesToFetchFactory) throws ConsumerException, MessageException {

		// Register OAuthMessage as MessageExtensionFactory (do once)
		Message.addExtensionFactory(OAuthMessage.class);
		this.consumerManager = consumerManager;
		this.attributesToFetchFactory = attributesToFetchFactory;
	}
	
	public OpenID4JavaConsumer(ConsumerManager consumerManager, final List<OpenIDAttribute> attributes)

	throws ConsumerException, MessageException {

		this(consumerManager, new AxFetchListFactory() {
			private final List<OpenIDAttribute> fetchAttrs = Collections.unmodifiableList(attributes);

			public List<OpenIDAttribute> createAttributeList(String identifier) {

				return fetchAttrs;

			}

		});

	}

	public OpenID4JavaConsumer(AxFetchListFactory attributesToFetchFactory) throws ConsumerException, MessageException {
		this(new ConsumerManager(), attributesToFetchFactory);
	}


	
	
	
	// ~ Methods
	// ========================================================================================================

	public String beginConsumption(HttpServletRequest req, String identityUrl, String returnToUrl, String realm)

	throws OpenIDConsumerException {

		List<DiscoveryInformation> discoveries;

		try {
			discoveries = consumerManager.discover(identityUrl);

		} catch (DiscoveryException e) {
			throw new OpenIDConsumerException("Error during discovery", e);
		}

		DiscoveryInformation information = consumerManager.associate(discoveries);
		req.getSession().setAttribute(DISCOVERY_INFO_KEY, information);

	
		AuthRequest authReq;

		try {

			authReq = consumerManager.authenticate(information, returnToUrl, realm);

			logger.debug("Looking up attribute fetch list for identifier: " + identityUrl);
			List<OpenIDAttribute> attributesToFetch = attributesToFetchFactory.createAttributeList(identityUrl);

			if (!attributesToFetch.isEmpty()) {

				req.getSession().setAttribute(ATTRIBUTE_LIST_KEY, attributesToFetch);
				FetchRequest fetchRequest = FetchRequest.createFetchRequest();

				for (OpenIDAttribute attr : attributesToFetch) {

					if (logger.isDebugEnabled()) {
						logger.debug("Adding attribute " + attr.getType() + " to fetch request");
					}
					fetchRequest.addAttribute(attr.getName(), attr.getType(), attr.isRequired(), attr.getCount());

				}
				authReq.addExtension(fetchRequest);

			}

			if(oauthScopes!=null ){
				OAuthRequest oauthRequest = OAuthRequest.createOAuthRequest();
				oauthRequest.setScopes(oauthScopes);
				oauthRequest.setConsumer(sysAdminParams.getShareHost());
				authReq.addExtension(oauthRequest);
			}
			

		} catch (MessageException e) {
			throw new OpenIDConsumerException("Error processing ConsumerManager authentication", e);
		} catch (ConsumerException e) {
			throw new OpenIDConsumerException("Error processing ConsumerManager authentication", e);
		}

		return authReq.getDestinationUrl(true);

	}

	public OpenIDAuthenticationToken endConsumption(HttpServletRequest request) throws OpenIDConsumerException {

		// extract the parameters from the authentication response

		// (which comes in as a HTTP request from the OpenID provider)

		ParameterList openidResp = new ParameterList(request.getParameterMap());

		// retrieve the previously stored discovery information

		DiscoveryInformation discovered = (DiscoveryInformation) request.getSession().getAttribute(DISCOVERY_INFO_KEY);

		if (discovered == null) {
			throw new OpenIDConsumerException("DiscoveryInformation is not available. Possible causes are lost session or replay attack");
		}

		List<OpenIDAttribute> attributesToFetch = (List<OpenIDAttribute>) request.getSession().getAttribute(ATTRIBUTE_LIST_KEY);

		request.getSession().removeAttribute(DISCOVERY_INFO_KEY);
		request.getSession().removeAttribute(ATTRIBUTE_LIST_KEY);

		// extract the receiving URL from the HTTP request

		StringBuffer receivingURL = request.getRequestURL();
		if (OpenIdUtils.hasShareAuthParam(request)) {
			receivingURL = new StringBuffer();
			receivingURL.append(UrlUtil.getShareUrl(sysAdminParams));
		}

		String queryString = request.getQueryString();

		if (StringUtils.hasLength(queryString)) {
			receivingURL.append("?").append(request.getQueryString());
		}

		// verify the response

		VerificationResult verification;

		try {
			verification = consumerManager.verify(receivingURL.toString(), openidResp, discovered);
		} catch (MessageException e) {
			throw new OpenIDConsumerException("Error verifying openid response", e);
		} catch (DiscoveryException e) {
			throw new OpenIDConsumerException("Error verifying openid response", e);
		} catch (AssociationException e) {
			throw new OpenIDConsumerException("Error verifying openid response", e);
		}

		// examine the verification result and extract the verified identifier

		Identifier verified = verification.getVerifiedId();

		if (verified == null) {
			Identifier id = discovered.getClaimedIdentifier();
			return new OpenIDAuthenticationToken(OpenIDAuthenticationStatus.FAILURE, id == null ? "Unknown" : id.getIdentifier(), "Verification status message: ["
					+ verification.getStatusMsg() + "]", Collections.<OpenIDAttribute> emptyList());
		}

		Message authSuccess = verification.getAuthResponse();
		
		List<OpenIDAttribute> attributes = fetchAxAttributes(authSuccess, attributesToFetch);
		
		String oAuthToken = fetchOAuthToken(authSuccess);
		

		return new OpenIDAuthenticationToken(OpenIDAuthenticationStatus.SUCCESS, verified.getIdentifier(), oAuthToken, attributes);

	}

	private String fetchOAuthToken(Message authSuccess) throws OpenIDConsumerException {
		try {
			// Extract OAuth params from request
			if (authSuccess.hasExtension(OAuthMessage.OPENID_NS_OAUTH)) {
				OAuthResponse oauthRes = (OAuthResponse) authSuccess
						.getExtension(OAuthMessage.OPENID_NS_OAUTH);
	
				if(logger.isDebugEnabled()){
					logger.debug("Return oauth token : "+oauthRes.getRequestToken());
				}
				
				// Use a OAuth library to exchange this request-token (without
				// secret and verifier) with an access-token/secret pair
				return oauthRes.getRequestToken();
			}
		
			return null;
		} catch (MessageException e) {
			throw new OpenIDConsumerException("Oauth requestToken retrieval failed", e);
		}
	}

	List<OpenIDAttribute> fetchAxAttributes(Message authSuccess, List<OpenIDAttribute> attributesToFetch) throws OpenIDConsumerException {

		if (attributesToFetch == null || !authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
			return Collections.emptyList();

		}

		logger.debug("Extracting attributes retrieved by attribute exchange");
		List<OpenIDAttribute> attributes = Collections.emptyList();
		try {

			MessageExtension ext = authSuccess.getExtension(AxMessage.OPENID_NS_AX);
			if (ext instanceof FetchResponse) {
				FetchResponse fetchResp = (FetchResponse) ext;
				attributes = new ArrayList<OpenIDAttribute>(attributesToFetch.size());
				for (OpenIDAttribute attr : attributesToFetch) {
					List<String> values = fetchResp.getAttributeValues(attr.getName());
					if (!values.isEmpty()) {
						OpenIDAttribute fetched = new OpenIDAttribute(attr.getName(), attr.getType(), values);
						fetched.setRequired(attr.isRequired());
						attributes.add(fetched);
					}
				}

			}

		} catch (MessageException e) {
			throw new OpenIDConsumerException("Attribute retrieval failed", e);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Retrieved attributes" + attributes);
		}

		return attributes;

	}

}
