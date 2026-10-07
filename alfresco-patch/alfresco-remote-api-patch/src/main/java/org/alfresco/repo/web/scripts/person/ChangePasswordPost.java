/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.web.scripts.person;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;

/**
 * Webscript implementation for the POST method for 'changepassword' API.
 * Overridden to validate password strength against the beCPG policy.
 * 
 * @author Kevin Roast
 * @author beCPG
 */
public class ChangePasswordPost extends DeclarativeWebScript
{
    private static final String PARAM_NEWPW = "newpw";
    private static final String PARAM_OLDPW = "oldpw";
    private MutableAuthenticationService authenticationService;
    private AuthorityService authorityService;

    /**
     * @param authenticationService
     *            the AuthenticationService to set
     */
    public void setAuthenticationService(MutableAuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    /**
     * @param authorityService
     *            the AuthorityService to set
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status) */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        // Extract user name from the URL - cannot be null or webscript desc would not match
        String userName = req.getExtensionPath();

        // Extract old and new password details from JSON POST
        Content c = req.getContent();
        if (c == null)
        {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Missing POST body.");
        }
        JSONObject json;
        try
        {
            json = new JSONObject(c.getContent());

            String oldPassword = null;
            String newPassword;

            // admin users can change/set a password without knowing the old one
            boolean isAdmin = authorityService.hasAdminAuthority();
            if (!isAdmin || (userName.equalsIgnoreCase(authenticationService.getCurrentUserName())))
            {
                if (!json.has(PARAM_OLDPW) || json.getString(PARAM_OLDPW).length() == 0)
                {
                    throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                            "Old password 'oldpw' is a required POST parameter.");
                }
                oldPassword = json.getString(PARAM_OLDPW);
            }
            if (!json.has(PARAM_NEWPW) || json.getString(PARAM_NEWPW).length() == 0)
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "New password 'newpw' is a required POST parameter.");
            }
            newPassword = json.getString(PARAM_NEWPW);

            // validate password strength against policy
            validatePasswordStrength(newPassword);

            // update the password
            // an Admin user can update without knowing the original pass - but must know their own!
            if (!isAdmin || (userName.equalsIgnoreCase(authenticationService.getCurrentUserName())))
            {
                authenticationService.updateAuthentication(userName, oldPassword.toCharArray(), newPassword.toCharArray());
            }
            else
            {
                authenticationService.setAuthentication(userName, newPassword.toCharArray());
            }
        }
        catch (AuthenticationException err)
        {
            throw new WebScriptException(Status.STATUS_UNAUTHORIZED,
                    "Do not have appropriate auth or wrong auth details provided.");
        }
        catch (JSONException jErr)
        {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR,
                    "Unable to parse JSON POST body: " + jErr.getMessage());
        }
        catch (IOException ioErr)
        {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR,
                    "Unable to retrieve POST body: " + ioErr.getMessage());
        }

        Map<String, Object> model = new HashMap<String, Object>(1, 1.0f);
        model.put("success", Boolean.TRUE);
        return model;
    }

    private void validatePasswordStrength(String password)
    {
        if (password == null || password.length() < 14)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Password does not match the security policy (at least 14 characters, one uppercase, one lowercase, one digit and one special character).");
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        String specialChars = "!@#$%^&*()-_=+[]{}.";
        for (int i = 0; i < password.length(); i++)
        {
            char c = password.charAt(i);
            if (Character.isUpperCase(c))
            {
                hasUpper = true;
            }
            else if (Character.isLowerCase(c))
            {
                hasLower = true;
            }
            else if (Character.isDigit(c))
            {
                hasDigit = true;
            }
            else if (specialChars.indexOf(c) >= 0)
            {
                hasSpecial = true;
            }
        }
        if (!hasUpper || !hasLower || !hasDigit || !hasSpecial)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Password does not match the security policy (at least 14 characters, one uppercase, one lowercase, one digit and one special character).");
        }
    }
}
