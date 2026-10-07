/**
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This file is part of the Spring Surf Extension project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.extensions.webscripts.servlet.mvc;

import java.util.Locale;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigService;
import org.springframework.extensions.config.ServerConfigElement;
import org.springframework.extensions.config.ServerProperties;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.RuntimeContainer;
import org.springframework.extensions.webscripts.servlet.ServletAuthenticatorFactory;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

/**
 * WebScript view implementation. Maintains the MVC view name as a parameter to
 * the WebScriptRuntime to ensure the correct WebScript is matched.
 *
 * @author muzquiano
 * @author kevinr
 */
public class WebScriptView extends AbstractUrlBasedView 
{   
    protected RuntimeContainer container;
    protected ServletAuthenticatorFactory authenticatorFactory;
    protected ConfigService configService;
    
    /** Host Server Configuration */
    protected static ServerProperties serverProperties;    
    
    
    /**
     * WebScriptView constructor
     *
     * @param container             WebScript RuntimeContainer
     * @param authenticatorFactory  Authenticator Factory
     * @param configService         Configuration Service
     */
    public WebScriptView(RuntimeContainer container, ServletAuthenticatorFactory authenticatorFactory, ConfigService configService) 
    {
        this.container = container;
        this.authenticatorFactory = authenticatorFactory;
        this.configService = configService;
        
        // retrieve host server configuration
        if (serverProperties == null)
        {
            Config config = configService.getConfig("Server");
            serverProperties = (ServerConfigElement)config.getConfigElement(ServerConfigElement.CONFIG_ELEMENT_ID);
        }
    }
    
    /* (non-Javadoc)
     * @see org.springframework.web.servlet.view.AbstractView#renderMergedOutputModel(java.util.Map, jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
     */
    /** {@inheritDoc} */
    protected void renderMergedOutputModel(
            Map model, HttpServletRequest request, HttpServletResponse response) throws Exception 
    {
        // the web script url
        String uri = this.getUrl();
        
        // Expose the model object as request attributes.
        exposeModelAsRequestAttributes(model, request);
        
        if (logger.isDebugEnabled())
            logger.debug("Processing request ("  + request.getMethod() + ") " + request.getRequestURL() + (request.getQueryString() != null ? "?" + request.getQueryString() : ""));
        
        // character encoding may have been set by the Spring MVC dispatcher
        if (request.getCharacterEncoding() == null)
        {
            request.setCharacterEncoding("UTF-8");
        }
        
        // locale may have been resolved by the Spring MVC dispatcher
        Locale locale = I18NUtil.getLocaleOrNull();
        if (locale == null)
        {
            setLanguageFromRequestHeader(request);
        }
    	//beCPG #17353
//        else
//        {
//            I18NUtil.setLocaleFromLanguage(locale.getLanguage());
//        }
        
        // hand off to the WebScript Servlet View runtime
        WebScriptViewRuntime runtime = new WebScriptViewRuntime(getUrl(), container, authenticatorFactory, request, response, serverProperties);
        runtime.executeScript();
    }
    
    /**
     * Apply Client and Repository language locale based on the 'Accept-Language' request header
     *
     * @param req a {@link jakarta.servlet.http.HttpServletRequest} object
     */
    public static void setLanguageFromRequestHeader(HttpServletRequest req)
    {
        // set language locale from browser header
        String acceptLang = req.getHeader("Accept-Language");
        I18NUtil.setLocaleFromLanguage(acceptLang);
    }
}
