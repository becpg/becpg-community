/*******************************************************************************
 * Copyright (C) 2010-2025 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package fr.becpg.web.authentication.config;

import org.dom4j.Element;
import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.element.ConfigElementAdapter;

import fr.becpg.web.authentication.identity.IdentityServiceConfig;

/**
 * Custom config element that stores OAuth2 configuration options
 * 
 * @author BeCPG Team
 */
public class OAuth2ConfigElement extends ConfigElementAdapter
{
    public static final String CONFIG_ELEMENT_ID = "oauth2";
    
    private static final long serialVersionUID = 1L;
    
    private String issuerUrl;
    private String audience;
    private String realm = "alfresco";
    private String authServerUrl = "http://localhost:8180/auth";
    private String resource = "alfresco";
    private String clientSecret = "";
    private boolean allowAnyHostname = false;
    private boolean disableTrustManager = false;
    private String truststore;
    private String truststorePassword;
    private String clientKeystore;
    private String clientKeystorePassword;
    private String clientKeyPassword;
    private int connectionPoolSize = 20;
    private int clientConnectionTimeout = 2000;
    private int clientSocketTimeout = 2000;
    private String realmKey;
    private int publicKeyCacheTtl = 86400;
    private boolean publicClient = true;
    private String principalAttribute = "preferred_username";
    private boolean clientIdValidationDisabled = true;
    private String signatureAlgorithms = "RS256,PS256";

    
    /**
     * Default constructor
     */
    public OAuth2ConfigElement()
    {
        super(CONFIG_ELEMENT_ID);
    }
    
    /**
     * @return the issuer URL
     */
    public String getIssuerUrl()
    {
        return this.issuerUrl;
    }
    
    /**
     * @param issuerUrl the issuer URL to set
     */
    public void setIssuerUrl(String issuerUrl)
    {
        this.issuerUrl = issuerUrl;
    }
    
    /**
     * @return the audience
     */
    public String getAudience()
    {
        return this.audience;
    }
    
    /**
     * @param audience the audience to set
     */
    public void setAudience(String audience)
    {
        this.audience = audience;
    }
    
    /**
     * @return the realm
     */
    public String getRealm()
    {
        return this.realm;
    }
    
    /**
     * @param realm the realm to set
     */
    public void setRealm(String realm)
    {
        this.realm = realm;
    }
    
    /**
     * @return the auth server URL
     */
    public String getAuthServerUrl()
    {
        return this.authServerUrl;
    }
    
    /**
     * @param authServerUrl the auth server URL to set
     */
    public void setAuthServerUrl(String authServerUrl)
    {
        this.authServerUrl = authServerUrl;
    }
    
    /**
     * @return the resource
     */
    public String getResource()
    {
        return this.resource;
    }
    
    /**
     * @param resource the resource to set
     */
    public void setResource(String resource)
    {
        this.resource = resource;
    }
    
    /**
     * @return the client secret
     */
    public String getClientSecret()
    {
        return this.clientSecret;
    }
    
    /**
     * @param clientSecret the client secret to set
     */
    public void setClientSecret(String clientSecret)
    {
        this.clientSecret = clientSecret;
    }
    
    /**
     * @return whether to allow any hostname
     */
    public boolean isAllowAnyHostname()
    {
        return this.allowAnyHostname;
    }
    
    /**
     * @param allowAnyHostname whether to allow any hostname
     */
    public void setAllowAnyHostname(boolean allowAnyHostname)
    {
        this.allowAnyHostname = allowAnyHostname;
    }
    
    /**
     * @return whether trust manager is disabled
     */
    public boolean isDisableTrustManager()
    {
        return this.disableTrustManager;
    }
    
    /**
     * @param disableTrustManager whether to disable trust manager
     */
    public void setDisableTrustManager(boolean disableTrustManager)
    {
        this.disableTrustManager = disableTrustManager;
    }
    
    /**
     * @return the truststore path
     */
    public String getTruststore()
    {
        return this.truststore;
    }
    
    /**
     * @param truststore the truststore path to set
     */
    public void setTruststore(String truststore)
    {
        this.truststore = truststore;
    }
    
    /**
     * @return the truststore password
     */
    public String getTruststorePassword()
    {
        return this.truststorePassword;
    }
    
    /**
     * @param truststorePassword the truststore password to set
     */
    public void setTruststorePassword(String truststorePassword)
    {
        this.truststorePassword = truststorePassword;
    }
    
    /**
     * @return the client keystore path
     */
    public String getClientKeystore()
    {
        return this.clientKeystore;
    }
    
    /**
     * @param clientKeystore the client keystore path to set
     */
    public void setClientKeystore(String clientKeystore)
    {
        this.clientKeystore = clientKeystore;
    }
    
    /**
     * @return the client keystore password
     */
    public String getClientKeystorePassword()
    {
        return this.clientKeystorePassword;
    }
    
    /**
     * @param clientKeystorePassword the client keystore password to set
     */
    public void setClientKeystorePassword(String clientKeystorePassword)
    {
        this.clientKeystorePassword = clientKeystorePassword;
    }
    
    /**
     * @return the client key password
     */
    public String getClientKeyPassword()
    {
        return this.clientKeyPassword;
    }
    
    /**
     * @param clientKeyPassword the client key password to set
     */
    public void setClientKeyPassword(String clientKeyPassword)
    {
        this.clientKeyPassword = clientKeyPassword;
    }
    
    /**
     * @return the connection pool size
     */
    public int getConnectionPoolSize()
    {
        return this.connectionPoolSize;
    }
    
    /**
     * @param connectionPoolSize the connection pool size to set
     */
    public void setConnectionPoolSize(int connectionPoolSize)
    {
        this.connectionPoolSize = connectionPoolSize;
    }
    
    /**
     * @return the client connection timeout in milliseconds
     */
    public int getClientConnectionTimeout()
    {
        return this.clientConnectionTimeout;
    }
    
    /**
     * @param clientConnectionTimeout the client connection timeout to set
     */
    public void setClientConnectionTimeout(int clientConnectionTimeout)
    {
        this.clientConnectionTimeout = clientConnectionTimeout;
    }
    
    /**
     * @return the client socket timeout in milliseconds
     */
    public int getClientSocketTimeout()
    {
        return this.clientSocketTimeout;
    }
    
    /**
     * @param clientSocketTimeout the client socket timeout to set
     */
    public void setClientSocketTimeout(int clientSocketTimeout)
    {
        this.clientSocketTimeout = clientSocketTimeout;
    }
    
    /**
     * @return the realm key
     */
    public String getRealmKey()
    {
        return this.realmKey;
    }
    
    /**
     * @param realmKey the realm key to set
     */
    public void setRealmKey(String realmKey)
    {
        this.realmKey = realmKey;
    }
    
    /**
     * @return the public key cache TTL in seconds
     */
    public int getPublicKeyCacheTtl()
    {
        return this.publicKeyCacheTtl;
    }
    
    /**
     * @param publicKeyCacheTtl the public key cache TTL to set
     */
    public void setPublicKeyCacheTtl(int publicKeyCacheTtl)
    {
        this.publicKeyCacheTtl = publicKeyCacheTtl;
    }
    
    /**
     * @return whether this is a public client
     */
    public boolean isPublicClient()
    {
        return this.publicClient;
    }
    
    /**
     * @param publicClient whether this is a public client
     */
    public void setPublicClient(boolean publicClient)
    {
        this.publicClient = publicClient;
    }
    
    /**
     * @return the principal attribute
     */
    public String getPrincipalAttribute()
    {
        return this.principalAttribute;
    }
    
    /**
     * @param principalAttribute the principal attribute to set
     */
    public void setPrincipalAttribute(String principalAttribute)
    {
        this.principalAttribute = principalAttribute;
    }
    
    /**
     * @return whether client ID validation is disabled
     */
    public boolean isClientIdValidationDisabled()
    {
        return this.clientIdValidationDisabled;
    }
    
    /**
     * @param clientIdValidationDisabled whether to disable client ID validation
     */
    public void setClientIdValidationDisabled(boolean clientIdValidationDisabled)
    {
        this.clientIdValidationDisabled = clientIdValidationDisabled;
    }
    
    /**
     * @return the signature algorithms
     */
    public String getSignatureAlgorithms()
    {
        return this.signatureAlgorithms;
    }
    
    /**
     * @param signatureAlgorithms the signature algorithms to set
     */
    public void setSignatureAlgorithms(String signatureAlgorithms)
    {
        this.signatureAlgorithms = signatureAlgorithms;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigElement combine(ConfigElement configElement)
    {
        if (configElement == null)
        {
            return this;
        }
        
        OAuth2ConfigElement otherElement = null;
        if (configElement instanceof OAuth2ConfigElement oAuthConfig)
        {
            otherElement = oAuthConfig;
        }
        else
        {
            return this;
        }
        
        OAuth2ConfigElement combinedElement = new OAuth2ConfigElement();
        
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
        combinedElement.setTruststorePassword(otherElement.getTruststorePassword() != null ? otherElement.getTruststorePassword() : this.truststorePassword);
        combinedElement.setClientKeystore(otherElement.getClientKeystore() != null ? otherElement.getClientKeystore() : this.clientKeystore);
        combinedElement.setClientKeystorePassword(otherElement.getClientKeystorePassword() != null ? otherElement.getClientKeystorePassword() : this.clientKeystorePassword);
        combinedElement.setClientKeyPassword(otherElement.getClientKeyPassword() != null ? otherElement.getClientKeyPassword() : this.clientKeyPassword);
        combinedElement.setConnectionPoolSize(otherElement.getConnectionPoolSize() != 0 ? otherElement.getConnectionPoolSize() : this.connectionPoolSize);
        combinedElement.setClientConnectionTimeout(otherElement.getClientConnectionTimeout() != 0 ? otherElement.getClientConnectionTimeout() : this.clientConnectionTimeout);
        combinedElement.setClientSocketTimeout(otherElement.getClientSocketTimeout() != 0 ? otherElement.getClientSocketTimeout() : this.clientSocketTimeout);
        combinedElement.setRealmKey(otherElement.getRealmKey() != null ? otherElement.getRealmKey() : this.realmKey);
        combinedElement.setPublicKeyCacheTtl(otherElement.getPublicKeyCacheTtl() != 0 ? otherElement.getPublicKeyCacheTtl() : this.publicKeyCacheTtl);
        combinedElement.setPublicClient(otherElement.isPublicClient());
        combinedElement.setPrincipalAttribute(otherElement.getPrincipalAttribute() != null ? otherElement.getPrincipalAttribute() : this.principalAttribute);
        combinedElement.setClientIdValidationDisabled(otherElement.isClientIdValidationDisabled());
        combinedElement.setSignatureAlgorithms(otherElement.getSignatureAlgorithms() != null ? otherElement.getSignatureAlgorithms() : this.signatureAlgorithms);
        
        return combinedElement;
    }
    
    /**
     * Constructs a new instance from an XML Element.
     * 
     * @param elem
     *            the XML element
     * @return the Kerberos configuration element
     */
    protected static OAuth2ConfigElement newInstance(Element elem)
    {
        OAuth2ConfigElement configElement = new OAuth2ConfigElement();
        
        // Support for new identity-service prefixed configuration format
        String authServerUrl = elem.elementTextTrim("identity-service.auth-server-url");
        if (authServerUrl != null) {
            configElement.setAuthServerUrl(authServerUrl);
            configElement.setRealm(elem.elementTextTrim("identity-service.realm"));
            configElement.setResource(elem.elementTextTrim("identity-service.resource"));
            configElement.setClientSecret(elem.elementTextTrim("identity-service.credentials.secret"));
            
            String publicClient = elem.elementTextTrim("identity-service.public-client");
            if (publicClient != null) {
                configElement.setPublicClient(Boolean.parseBoolean(publicClient));
            }
            
            // Set issuer URL based on auth server URL if not explicitly set
            if (authServerUrl != null && !authServerUrl.isEmpty()) {
                if (authServerUrl.endsWith("/")) {
                    configElement.setIssuerUrl(authServerUrl + "realms/" + configElement.getRealm());
                } else {
                    configElement.setIssuerUrl(authServerUrl + "/realms/" + configElement.getRealm());
                }
            }
            
            // Handle other properties that might have identity-service prefix
            String principalAttr = elem.elementTextTrim("identity-service.principal-attribute");
            if (principalAttr != null) {
                configElement.setPrincipalAttribute(principalAttr);
            }
            
            String createUser = elem.elementTextTrim("identity-service.create-user.enabled");
            if (createUser != null) {
                // This would require adding a new property to the config element
                // configElement.setCreateUserEnabled(Boolean.parseBoolean(createUser));
            }
        } 
        // Fallback to old format properties if new format not found
        else {
            configElement.setIssuerUrl(elem.elementTextTrim("issuerUrl"));
            configElement.setAudience(elem.elementTextTrim("audience"));
            configElement.setRealm(elem.elementTextTrim("realm"));
            configElement.setAuthServerUrl(elem.elementTextTrim("authServerUrl"));
            configElement.setResource(elem.elementTextTrim("resource"));
            configElement.setClientSecret(elem.elementTextTrim("clientSecret"));
            configElement.setPrincipalAttribute(elem.elementTextTrim("principalAttribute"));
            configElement.setPublicClient(Boolean.parseBoolean(elem.elementTextTrim("publicClient")));
        }
        
        // Common properties that should be parsed regardless of format
        String allowAnyHostname = elem.elementTextTrim("allowAnyHostname");
        if (allowAnyHostname != null) {
            configElement.setAllowAnyHostname(Boolean.parseBoolean(allowAnyHostname));
        }
        
        String disableTrustManager = elem.elementTextTrim("disableTrustManager");
        if (disableTrustManager != null) {
            configElement.setDisableTrustManager(Boolean.parseBoolean(disableTrustManager));
        }
        
        configElement.setTruststore(elem.elementTextTrim("truststore"));
        configElement.setTruststorePassword(elem.elementTextTrim("truststorePassword"));
        configElement.setClientKeystore(elem.elementTextTrim("clientKeystore"));
        configElement.setClientKeystorePassword(elem.elementTextTrim("clientKeystorePassword"));
        configElement.setClientKeyPassword(elem.elementTextTrim("clientKeyPassword"));
        
        String connPoolSize = elem.elementTextTrim("connectionPoolSize");
        if (connPoolSize != null && !connPoolSize.isEmpty()) {
            configElement.setConnectionPoolSize(Integer.parseInt(connPoolSize));
        }
        
        String connTimeout = elem.elementTextTrim("clientConnectionTimeout");
        if (connTimeout != null && !connTimeout.isEmpty()) {
            configElement.setClientConnectionTimeout(Integer.parseInt(connTimeout));
        }
        
        String socketTimeout = elem.elementTextTrim("clientSocketTimeout");
        if (socketTimeout != null && !socketTimeout.isEmpty()) {
            configElement.setClientSocketTimeout(Integer.parseInt(socketTimeout));
        }
        
        configElement.setRealmKey(elem.elementTextTrim("realmKey"));
        
        String keyCacheTtl = elem.elementTextTrim("publicKeyCacheTtl");
        if (keyCacheTtl != null && !keyCacheTtl.isEmpty()) {
            configElement.setPublicKeyCacheTtl(Integer.parseInt(keyCacheTtl));
        }
        
        String clientIdValidation = elem.elementTextTrim("clientIdValidationDisabled");
        if (clientIdValidation != null) {
            configElement.setClientIdValidationDisabled(Boolean.parseBoolean(clientIdValidation));
        }
        
        configElement.setSignatureAlgorithms(elem.elementTextTrim("signatureAlgorithms"));

        return configElement;
    }

	public IdentityServiceConfig toConfig() {
		IdentityServiceConfig config = new IdentityServiceConfig();
        config.setIssuerUrl(getIssuerUrl());
        config.setAudience(getAudience());
        config.setRealm(getRealm());
        config.setAuthServerUrl(getAuthServerUrl());
        config.setResource(getResource());
        config.setClientSecret(getClientSecret());
        config.setAllowAnyHostname(isAllowAnyHostname());
        config.setDisableTrustManager(isDisableTrustManager());     
        config.setTruststore(getTruststore());
        config.setTruststorePassword(getTruststorePassword());
        config.setClientKeystore(getClientKeystore());
        config.setClientKeystorePassword(getClientKeystorePassword());
        config.setClientKeyPassword(getClientKeyPassword());
        config.setConnectionPoolSize(getConnectionPoolSize());
        config.setClientConnectionTimeout(getClientConnectionTimeout());
        config.setClientSocketTimeout(getClientSocketTimeout());
        config.setRealmKey(getRealmKey());
        config.setPublicKeyCacheTtl(getPublicKeyCacheTtl());
        config.setPublicClient(isPublicClient());
        config.setPrincipalAttribute(getPrincipalAttribute());
        config.setClientIdValidationDisabled(isClientIdValidationDisabled());
        config.setSignatureAlgorithms(getSignatureAlgorithms());
        return config;
	}
}
