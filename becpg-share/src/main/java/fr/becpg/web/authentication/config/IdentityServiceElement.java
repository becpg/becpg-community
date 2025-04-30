package fr.becpg.web.authentication.config;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.element.ConfigElementAdapter;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Class to hold configuration for the Identity Service.
 *
 * @author matthieu
 */
public class IdentityServiceElement extends ConfigElementAdapter {
	/**
	 *
	 */
	private static final long serialVersionUID = 1527353183643015426L;

	public static final String CONFIG_ELEMENT_ID = "oauth2";

	private static final String REALMS = "realms";

	private int clientConnectionTimeout = 2000;
	private int clientSocketTimeout = 2000;
	private String issuerUrl;
	private String audience;
	// client id
	private String resource;
	private String clientSecret;
	private String authServerUrl;
	private String realm;
	private int connectionPoolSize = 20;
	private boolean allowAnyHostname = false;
	private boolean disableTrustManager = false;
	private String truststore;
	private String truststorePassword;
	private String clientKeystore;
	private String clientKeystorePassword;
	private String clientKeyPassword;
	private String realmKey;
	private int publicKeyCacheTtl = 86400;
	private boolean publicClient = false;
	private String principalAttribute = "preferred_username";
	private boolean clientIdValidationDisabled = true;
	private String adminConsoleRedirectPath;
	private String signatureAlgorithms = "RS256,PS256";

	public IdentityServiceElement() {
		super(CONFIG_ELEMENT_ID);
	}

	/**
	 *
	 * @return Client connection timeout in milliseconds.
	 */
	public int getClientConnectionTimeout() {
		return clientConnectionTimeout;
	}

	/**
	 *
	 * @param clientConnectionTimeout Client connection timeout in milliseconds.
	 */
	public void setClientConnectionTimeout(int clientConnectionTimeout) {
		this.clientConnectionTimeout = clientConnectionTimeout;
	}

	/**
	 *
	 * @return Client socket timeout in milliseconds.s
	 */
	public int getClientSocketTimeout() {
		return clientSocketTimeout;
	}

	/**
	 *
	 * @param clientSocketTimeout Client socket timeout in milliseconds.
	 */
	public void setClientSocketTimeout(int clientSocketTimeout) {
		this.clientSocketTimeout = clientSocketTimeout;
	}

	public void setConnectionPoolSize(int connectionPoolSize) {
		this.connectionPoolSize = connectionPoolSize;
	}

	public int getConnectionPoolSize() {
		return connectionPoolSize;
	}

	public String getIssuerUrl() {
		return issuerUrl;
	}

	public void setIssuerUrl(String issuerUrl) {
		this.issuerUrl = issuerUrl;
	}

	public String getAudience() {
		return audience;
	}

	public void setAudience(String audience) {
		this.audience = audience;
	}

	public String getAuthServerUrl() {
		return Optional.ofNullable(realm).filter(StringUtils::isNotBlank).filter(realm -> StringUtils.isNotBlank(authServerUrl))
				.map(realm -> UriComponentsBuilder.fromUriString(authServerUrl).pathSegment(REALMS, realm).build().toString()).orElse(authServerUrl);
	}

	public void setAuthServerUrl(String authServerUrl) {
		this.authServerUrl = authServerUrl;
	}

	public String getRealm() {
		return realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getClientSecret() {
		return Optional.ofNullable(clientSecret).orElse("");
	}

	public void setAllowAnyHostname(boolean allowAnyHostname) {
		this.allowAnyHostname = allowAnyHostname;
	}

	public boolean isAllowAnyHostname() {
		return allowAnyHostname;
	}

	public void setDisableTrustManager(boolean disableTrustManager) {
		this.disableTrustManager = disableTrustManager;
	}

	public boolean isDisableTrustManager() {
		return disableTrustManager;
	}

	public void setTruststore(String truststore) {
		this.truststore = truststore;
	}

	public String getTruststore() {
		return truststore;
	}

	public void setTruststorePassword(String truststorePassword) {
		this.truststorePassword = truststorePassword;
	}

	public String getTruststorePassword() {
		return truststorePassword;
	}

	public void setClientKeystore(String clientKeystore) {
		this.clientKeystore = clientKeystore;
	}

	public String getClientKeystore() {
		return clientKeystore;
	}

	public void setClientKeystorePassword(String clientKeystorePassword) {
		this.clientKeystorePassword = clientKeystorePassword;
	}

	public String getClientKeystorePassword() {
		return clientKeystorePassword;
	}

	public void setClientKeyPassword(String clientKeyPassword) {
		this.clientKeyPassword = clientKeyPassword;
	}

	public String getClientKeyPassword() {
		return clientKeyPassword;
	}

	public void setRealmKey(String realmKey) {
		this.realmKey = realmKey;
	}

	public String getRealmKey() {
		return realmKey;
	}

	public void setPublicKeyCacheTtl(int publicKeyCacheTtl) {
		this.publicKeyCacheTtl = publicKeyCacheTtl;
	}

	public int getPublicKeyCacheTtl() {
		return publicKeyCacheTtl;
	}

	public void setPublicClient(boolean publicClient) {
		this.publicClient = publicClient;
	}

	public boolean isPublicClient() {
		return publicClient;
	}

	public String getPrincipalAttribute() {
		return principalAttribute;
	}

	public void setPrincipalAttribute(String principalAttribute) {
		this.principalAttribute = principalAttribute;
	}

	public boolean isClientIdValidationDisabled() {
		return clientIdValidationDisabled;
	}

	public void setClientIdValidationDisabled(boolean clientIdValidationDisabled) {
		this.clientIdValidationDisabled = clientIdValidationDisabled;
	}

	public String getAdminConsoleRedirectPath() {
		return adminConsoleRedirectPath;
	}

	public void setAdminConsoleRedirectPath(String adminConsoleRedirectPath) {
		this.adminConsoleRedirectPath = adminConsoleRedirectPath;
	}

	public Set<SignatureAlgorithm> getSignatureAlgorithms() {
		return Stream.of(signatureAlgorithms.split(",")).map(String::trim).map(SignatureAlgorithm::from).filter(Objects::nonNull)
				.collect(Collectors.toUnmodifiableSet());
	}

	public void setSignatureAlgorithms(String signatureAlgorithms) {
		this.signatureAlgorithms = signatureAlgorithms;
	}

	/**
	 * Constructs a new instance from an XML Element.
	 * 
	 * @param elem the XML element
	 * @return the IdentityServiceConfig element
	 */
	protected static IdentityServiceElement newInstance(Element elem) {
		IdentityServiceElement configElement = new IdentityServiceElement();

		String authServerUrl = elem.elementTextTrim("authServerUrl");
		if (authServerUrl != null) {
			configElement.setAuthServerUrl(authServerUrl);
		}

		String realm = elem.elementTextTrim("realm");
		if (realm != null) {
			configElement.setRealm(realm);
		}

		String resource = elem.elementTextTrim("resource");
		if (resource != null) {
			configElement.setResource(resource);
		}

		String clientSecret = elem.elementTextTrim("clientSecret");
		if (clientSecret != null) {
			configElement.setClientSecret(clientSecret);
		}

		String publicClient = elem.elementTextTrim("publicClient");
		if (publicClient != null) {
			configElement.setPublicClient(Boolean.parseBoolean(publicClient));
		}

		String allowAnyHostname = elem.elementTextTrim("allowAnyHostname");
		if (allowAnyHostname != null) {
			configElement.setAllowAnyHostname(Boolean.parseBoolean(allowAnyHostname));
		}

		String disableTrustManager = elem.elementTextTrim("disableTrustManager");
		if (disableTrustManager != null) {
			configElement.setDisableTrustManager(Boolean.parseBoolean(disableTrustManager));
		}

		String truststore = elem.elementTextTrim("truststore");
		if ((truststore != null) && !truststore.isEmpty()) {
			configElement.setTruststore(truststore);
		}

		String truststorePassword = elem.elementTextTrim("truststorePassword");
		if ((truststorePassword != null) && !truststorePassword.isEmpty()) {
			configElement.setTruststorePassword(truststorePassword);
		}

		String clientKeystore = elem.elementTextTrim("clientKeystore");
		if (clientKeystore != null) {
			configElement.setClientKeystore(clientKeystore);
		}

		String clientKeystorePassword = elem.elementTextTrim("clientKeystorePassword");
		if (clientKeystorePassword != null) {
			configElement.setClientKeystorePassword(clientKeystorePassword);
		}

		String clientKeyPassword = elem.elementTextTrim("clientKeyPassword");
		if (clientKeyPassword != null) {
			configElement.setClientKeyPassword(clientKeyPassword);
		}

		String connectionPoolSize = elem.elementTextTrim("connectionPoolSize");
		if ((connectionPoolSize != null) && !connectionPoolSize.isEmpty()) {
			configElement.setConnectionPoolSize(Integer.parseInt(connectionPoolSize));
		}

		String clientConnectionTimeout = elem.elementTextTrim("clientConnectionTimeout");
		if ((clientConnectionTimeout != null) && !clientConnectionTimeout.isEmpty()) {
			configElement.setClientConnectionTimeout(Integer.parseInt(clientConnectionTimeout));
		}

		String clientSocketTimeout = elem.elementTextTrim("clientSocketTimeout");
		if ((clientSocketTimeout != null) && !clientSocketTimeout.isEmpty()) {
			configElement.setClientSocketTimeout(Integer.parseInt(clientSocketTimeout));
		}

		String realmKey = elem.elementTextTrim("realmKey");
		if ((realmKey != null) && !realmKey.isEmpty()) {
			configElement.setRealmKey(realmKey);
		}

		String publicKeyCacheTtl = elem.elementTextTrim("publicKeyCacheTtl");
		if ((publicKeyCacheTtl != null) && !publicKeyCacheTtl.isEmpty()) {
			configElement.setPublicKeyCacheTtl(Integer.parseInt(publicKeyCacheTtl));
		}

		String clientIdValidationDisabled = elem.elementTextTrim("clientIdValidationDisabled");
		if (clientIdValidationDisabled != null) {
			configElement.setClientIdValidationDisabled(Boolean.parseBoolean(clientIdValidationDisabled));
		}

		String signatureAlgorithms = elem.elementTextTrim("signatureAlgorithms");
		if (signatureAlgorithms != null) {
			configElement.setSignatureAlgorithms(signatureAlgorithms);
		}

		return configElement;
	}

	@Override
	public ConfigElement combine(ConfigElement configElement) {
		if (configElement == null) {
			return this;
		}

		IdentityServiceElement otherElement = null;
		if (configElement instanceof IdentityServiceElement oAuthConfig) {
			otherElement = oAuthConfig;
		} else {
			return this;
		}

		IdentityServiceElement combinedElement = new IdentityServiceElement();

		// Use the values from the 'other' element if they exist, otherwise use our values
		combinedElement.setIssuerUrl(otherElement.getIssuerUrl() != null ? otherElement.getIssuerUrl() : this.issuerUrl);
		combinedElement.setAudience(otherElement.getAudience() != null ? otherElement.getAudience() : this.audience);
		combinedElement.setRealm(otherElement.getRealm() != null ? otherElement.getRealm() : this.realm);
		combinedElement.setAuthServerUrl(otherElement.getAuthServerUrl() != null ? otherElement.getAuthServerUrl() : this.authServerUrl);
		combinedElement.setResource(otherElement.getResource() != null ? otherElement.getResource() : this.resource);
		combinedElement.setClientSecret(otherElement.getClientSecret() != null ? otherElement.getClientSecret() : this.clientSecret);
		combinedElement.setAllowAnyHostname(otherElement.isAllowAnyHostname());
		combinedElement.setDisableTrustManager(otherElement.isDisableTrustManager());
		combinedElement.setTruststore(otherElement.getTruststore() != null ? otherElement.getTruststore() : this.truststore);
		combinedElement
				.setTruststorePassword(otherElement.getTruststorePassword() != null ? otherElement.getTruststorePassword() : this.truststorePassword);
		combinedElement.setClientKeystore(otherElement.getClientKeystore() != null ? otherElement.getClientKeystore() : this.clientKeystore);
		combinedElement.setClientKeystorePassword(
				otherElement.getClientKeystorePassword() != null ? otherElement.getClientKeystorePassword() : this.clientKeystorePassword);
		combinedElement
				.setClientKeyPassword(otherElement.getClientKeyPassword() != null ? otherElement.getClientKeyPassword() : this.clientKeyPassword);
		combinedElement
				.setConnectionPoolSize(otherElement.getConnectionPoolSize() != 0 ? otherElement.getConnectionPoolSize() : this.connectionPoolSize);
		combinedElement.setClientConnectionTimeout(
				otherElement.getClientConnectionTimeout() != 0 ? otherElement.getClientConnectionTimeout() : this.clientConnectionTimeout);
		combinedElement.setClientSocketTimeout(
				otherElement.getClientSocketTimeout() != 0 ? otherElement.getClientSocketTimeout() : this.clientSocketTimeout);
		combinedElement.setRealmKey(otherElement.getRealmKey() != null ? otherElement.getRealmKey() : this.realmKey);
		combinedElement.setPublicKeyCacheTtl(otherElement.getPublicKeyCacheTtl() != 0 ? otherElement.getPublicKeyCacheTtl() : this.publicKeyCacheTtl);
		combinedElement.setPublicClient(otherElement.isPublicClient());
		combinedElement
				.setPrincipalAttribute(otherElement.getPrincipalAttribute() != null ? otherElement.getPrincipalAttribute() : this.principalAttribute);
		combinedElement.setClientIdValidationDisabled(otherElement.isClientIdValidationDisabled());
		combinedElement
				.setSignatureAlgorithms(otherElement.signatureAlgorithms != null ? otherElement.signatureAlgorithms : this.signatureAlgorithms);
		combinedElement.setAdminConsoleRedirectPath(
				otherElement.getAdminConsoleRedirectPath() != null ? otherElement.getAdminConsoleRedirectPath() : this.adminConsoleRedirectPath);

		return combinedElement;
	}

}
