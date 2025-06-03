package fr.becpg.web.authentication;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.web.site.servlet.config.AIMSConfig;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import jakarta.servlet.http.HttpServletRequest;

public class BeCPGCustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver
{

    private OAuth2AuthorizationRequestResolver defaultResolver;
    private AIMSConfig aimsConfig;
    private static final  String AUDIENCE="audience";

    public BeCPGCustomAuthorizationRequestResolver(ClientRegistrationRepository repo, String authorizationRequestBaseUri,
                                              AIMSConfig aimsConfig)
    {
        defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(repo, authorizationRequestBaseUri);
        this.aimsConfig = aimsConfig;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest httpServletRequest)
    {
        OAuth2AuthorizationRequest oAuth2AuthorizationRequest = defaultResolver.resolve(httpServletRequest);
        if (oAuth2AuthorizationRequest != null)
        {
            oAuth2AuthorizationRequest = customizeAuthorizationRequest(oAuth2AuthorizationRequest, httpServletRequest);
        }
        return oAuth2AuthorizationRequest;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest httpServletRequest, String clientRegistrationId)
    {
        OAuth2AuthorizationRequest oAuth2AuthorizationRequest =
            defaultResolver.resolve(httpServletRequest, clientRegistrationId);
        if (oAuth2AuthorizationRequest != null)
        {
            oAuth2AuthorizationRequest = customizeAuthorizationRequest(oAuth2AuthorizationRequest, httpServletRequest);
        }
        return oAuth2AuthorizationRequest;
    }

    private OAuth2AuthorizationRequest customizeAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                                                     HttpServletRequest request)
    {
        Map<String, Object> extraParams = new HashMap<>();
        String requestURI =
            this.aimsConfig.getRedirectURI() != null ? this.aimsConfig.getRedirectURI() : String.valueOf(
                request.getRequestURL());
        
        //beCPG
        if ("true".equalsIgnoreCase(request.getParameter("prompt"))) {
            extraParams.put("prompt", "login");
        }

        if (this.aimsConfig.getAudience() != null)
        {
            extraParams.put(AUDIENCE, this.aimsConfig.getAudience());
        }
        
        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .redirectUri(requestURI)
                .additionalParameters(extraParams)
                .build();
    }
}

