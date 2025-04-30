/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package fr.becpg.web.authentication.identity;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import com.nimbusds.openid.connect.sdk.claims.PersonClaims;

import fr.becpg.web.authentication.config.IdentityServiceElement;
import fr.becpg.web.authentication.identity.IdentityServiceFacade.DecodedAccessToken;

/**
 * This class handles Just in Time user provisioning. It extracts {@link OIDCUserInfo}
 * from {@link IdentityServiceFacade.DecodedAccessToken} or {@link UserInfo}
 * and creates a new user if it does not exist in the repository.
 */
public class IdentityServiceJITProvisioningHandler
{
    private final IdentityServiceElement identityServiceConfig;
    private final IdentityServiceFacade identityServiceFacade;


    private final BiFunction<DecodedAccessToken, String, Optional<? extends OIDCUserInfo>> mapTokenToUserInfoResponse = (token, usernameMappingClaim) -> {
        Optional<String> firstName = Optional.ofNullable(token)
            .map(jwtToken -> jwtToken.getClaim(PersonClaims.GIVEN_NAME_CLAIM_NAME))
            .filter(String.class::isInstance)
            .map(String.class::cast);
        Optional<String> lastName = Optional.ofNullable(token)
            .map(jwtToken -> jwtToken.getClaim(PersonClaims.FAMILY_NAME_CLAIM_NAME))
            .filter(String.class::isInstance)
            .map(String.class::cast);
        Optional<String> email = Optional.ofNullable(token)
            .map(jwtToken -> jwtToken.getClaim(PersonClaims.EMAIL_CLAIM_NAME))
            .filter(String.class::isInstance)
            .map(String.class::cast);

        return Optional.ofNullable(token.getClaim(Optional.ofNullable(usernameMappingClaim)
                .filter(StringUtils::isNotBlank)
                .orElse(PersonClaims.PREFERRED_USERNAME_CLAIM_NAME)))
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .map(username -> new OIDCUserInfo(username, firstName.orElse(""), lastName.orElse(""), email.orElse("")));
    };

    public IdentityServiceJITProvisioningHandler(IdentityServiceFacade identityServiceFacade,
        IdentityServiceElement identityServiceConfig)
    {
        this.identityServiceFacade = identityServiceFacade;
        this.identityServiceConfig = identityServiceConfig;
    }

    public Optional<OIDCUserInfo> extractUserInfoAndCreateUserIfNeeded(String bearerToken)
    {
       return Optional.ofNullable(bearerToken)
            .filter(Predicate.not(String::isEmpty))
            .flatMap(token -> extractUserInfoResponseFromAccessToken(token)
                .filter(userInfo -> StringUtils.isNotEmpty(userInfo.username()))
                .or(() -> extractUserInfoResponseFromEndpoint(token)));
    }

    private Optional<OIDCUserInfo> extractUserInfoResponseFromAccessToken(String bearerToken)
    {
    	 return Optional.ofNullable(bearerToken)
    	            .map(identityServiceFacade::decodeToken)
    	            .flatMap(decodedToken -> mapTokenToUserInfoResponse.apply(decodedToken,
    	                identityServiceConfig.getPrincipalAttribute()));
    }

    private Optional<OIDCUserInfo> extractUserInfoResponseFromEndpoint(String bearerToken)
    {
        return identityServiceFacade.getUserInfo(bearerToken,
                StringUtils.isNotBlank(identityServiceConfig.getPrincipalAttribute()) ?
                    identityServiceConfig.getPrincipalAttribute() : PersonClaims.PREFERRED_USERNAME_CLAIM_NAME)
            .filter(userInfo -> userInfo.username() != null && !userInfo.username().isEmpty())
            .map(userInfo -> new OIDCUserInfo(userInfo.username(),
                Optional.ofNullable(userInfo.firstName()).orElse(""),
                Optional.ofNullable(userInfo.lastName()).orElse(""),
                Optional.ofNullable(userInfo.email()).orElse("")));
    }

   
}
