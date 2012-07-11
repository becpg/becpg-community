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
package org.springframework.extensions.webscripts;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Extends <code>PropertyResourceBundle</code> in order to provide two new capabilities. The first is to 
 * store the path where the properties file used to create the <code>InputStream</code> is located and the second
 * is to allow additional <code>ResourceBundle</code> properties to be merged into an instance.</p>
 * <p>The rational of these capabilities is to allow a <code>WebScript</code> to locate and merge extension module
 * properties files.</p>
 * 
 * @author David Draper
 */
public class WebScriptPropertyResourceBundle extends PropertyResourceBundle
{
    private static final Log logger = LogFactory.getLog(WebScriptPropertyResourceBundle.class);
    
    /**
     * <p>The location of the properties file that was used to instantiate the <code>WebScriptPropertyResourceBundle</code>
     * instance. This field is set by the constructor.</p>
     */
    private String resourcePath = null;

    /**
     * @return The location of the properties file that was used to instantiate the <code>WebScriptPropertyResourceBundle</code>
     * instance.
     */
    public String getResourcePath()
    {
        return resourcePath;
    }

    /**
     * <p>Instantiates a new <code>WebScriptPropertyResourceBundle</code>.</p>
     * 
     * @param stream The <code>InputStream</code> passed on to the super class constructor.
     * @param resourcePath The location of the properties file used to create the <code>InputStream</code>
     * @throws IOException
     */
    public WebScriptPropertyResourceBundle(InputStream stream, String resourcePath) throws IOException
    {
        super(stream);
        this.resourcePath = resourcePath;
        merge(this);
    }

    /**
     * <p>Contains the properties of all the merged bundles on a per Thread basis. This needs to be a ThreadLocal
     * object since it is possible for multiple Threads to be merging extension module i18n updates concurrently</p>
     */
    private ThreadLocal<HashMap<String, Object>> mergedBundles = new ThreadLocal<HashMap<String, Object>>() 
    {
        @Override protected HashMap<String, Object> initialValue()
        {
            return new HashMap<String, Object>();
        }
    };
    
    public void reset()
    {
        this.mergedBundles.get().clear();
        mergedBundlePaths.set(new StringBuilder()); // Reset the record of paths merged
        merge(this);
    }
    
    /**
     * <p>Keeps track of all the bundle paths that have been merged into this bundle. The purpose is for providing a contribution
     * towards caching keys so that bundles are cached not only based on the locale but on the paths that have been merged into 
     * them. This needs to be a ThreadLocal object since it is possible for multiple Threads to be merging extension module i18n 
     * updates concurrently</p> 
     */
    private ThreadLocal<StringBuilder> mergedBundlePaths = new ThreadLocal<StringBuilder>() 
    {
        @Override protected StringBuilder initialValue()
        {
            return new StringBuilder();
        }
    }; 
    
    /**
     * @return A String containing all the paths that have been merged into the bundle delimited by a colon.
     */
    public String getMergedBundlePaths()
    {        
        return this.mergedBundlePaths.get().toString();
    }

    /**
     * <p>Merges the properties of a <code>ResourceBundle</code> into the current <code>WebScriptPropertyResourceBundle</code>
     * instance. This will override any values mapped to duplicate keys in the current merged properties.</p>
     * 
     * @param resourceBundle The <code>ResourceBundle</code> to merge the properties of.
     * @return <code>true</code> if the bundle was successfully merged and <code>false</code> otherwise. 
     */
    public boolean merge(ResourceBundle resourceBundle)
    {
        boolean merged = false;
        if (resourceBundle != null)
        {
            if ((resourceBundle.getLocale() == null && this.getLocale() == null) ||
                (resourceBundle.getLocale().equals(this.getLocale())))
            {
                // If the bundle being merged is a WebScriptPropertyResourceBundle then we need to ensure that
                // we call the super class methods of getKeys and getObject rather than the overridden methods
                // which access the mergedBundles field...
                if (resourceBundle instanceof WebScriptPropertyResourceBundle)
                {
                    Enumeration<String> keys = ((WebScriptPropertyResourceBundle) resourceBundle).getWrappedKeys();
                    while (keys.hasMoreElements())
                    {
                        String key = keys.nextElement();
                        this.mergedBundles.get().put(key, ((WebScriptPropertyResourceBundle) resourceBundle).getWrappedObject(key));
                    }
                    
                    // Add the path...
                    this.mergedBundlePaths.get().append(((WebScriptPropertyResourceBundle) resourceBundle).getResourcePath());
                    this.mergedBundlePaths.get().append(":");
                }
                else
                {
                    Enumeration<String> keys = resourceBundle.getKeys();
                    while (keys.hasMoreElements())
                    {
                        String key = keys.nextElement();
                        this.mergedBundles.get().put(key, resourceBundle.getObject(key));
                    }
                }
                
                
                merged = true;
            }
            else
            {
                if (logger.isErrorEnabled())
                {
                    logger.error("It is not possible to merge differing locales for: '" + this.resourcePath + "' (attempting to merge " + resourceBundle.getLocale() + " into " + this.getLocale() + ")");
                }
            }
        }
        
        return merged;
    }

    /**
     * <p>Overrides the super class implementation to return an object located in the merged bundles</p>
     * 
     * @return An <code>Object</code> from the merged bundles 
     */
    @Override
    public Object handleGetObject(String key)
    {
        if (key == null) {
            throw new NullPointerException();
        }
        return this.mergedBundles.get().get(key);
    }
    
    /**
     * <p>Calls the super class implementation of <code>handleGetObject</code> to retrieve an object from
     * the original properties file rather than from the merged bundles. This is required when creating
     * and merging <code>WebScriptPropertyResourceBundle</code> instances.</p>
     * 
     * @param key The key to locate in the original properties file.
     * @return An object from the original properties file.
     */
    public Object getWrappedObject(String key)
    {
        return super.handleGetObject(key);
    }
    
    /**
     * <p>Overrides the super class implementation to return an enumeration of keys from all the merged bundles</p>
     * 
     * @return An <code>Enumeration</code> of the keys across all the merged bundles. 
     */
    @Override
    public Enumeration<String> getKeys()
    {
        Vector<String> keys = new Vector<String>(this.mergedBundles.get().keySet());
        return keys.elements();
    }

    /**
     * <p>Calls the super class implementation of <code>getKeys</code> to retrieve the keys from
     * the original properties file rather than from the merged bundles. This is required when creating
     * and merging <code>WebScriptPropertyResourceBundle</code> instances.</p>
     * 
     * @return An <code>Enumeration</code> of the keys from the original properties file.
     */
    public Enumeration<String> getWrappedKeys()
    {
        return super.getKeys();
    }
    
    /**
     * <p>Overrides the super class implementation to return the <code>Set</code> of keys from all merged
     * bundles</p>
     * 
     * @return A <code>Set</code> of keys obtained from all merged bundles 
     */
    @Override
    protected Set<String> handleKeySet()
    {
        return this.mergedBundles.get().keySet();
    }

    /**
     * <p>Overrides the super class implementation to check the existence of a key across all merged
     * bundles</p>
     * 
     * @return <code>true</code> if the key is present and <code>false</code> otherwise.
     */
    @Override
    public boolean containsKey(String key)
    {
        return this.mergedBundles.get().containsKey(key);
    }

    /**
     * <p>Overrides the super class implementation to return the <code>Set</code> of keys from all merged
     * bundles</p>
     * 
     * @return A <code>Set</code> of keys obtained from all merged bundles 
     */
    @Override
    public Set<String> keySet()
    {
        return this.mergedBundles.get().keySet();
    }
}