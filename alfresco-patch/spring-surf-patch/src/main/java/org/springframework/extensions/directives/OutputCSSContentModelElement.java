/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.springframework.extensions.directives;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

import org.springframework.extensions.surf.CssImageDataHandler;
import org.springframework.extensions.surf.DependencyAggregator;
import org.springframework.extensions.surf.extensibility.DeferredContentSourceModelElement;

/**
 * <p>This is a DeferredContentTargetModelElement that is used as a place holder in the  ExtensibilityModel so
 * that other directives can add CSS dependencies into the {@code <head>} element. This allows us to disable the double-pass
 * WebScript processing (which can be done through Surf configuration) which will enable WebScripts to add CSS dependencies
 * without needing to rely on head.ftl files.</p>
 *
 * @author David Draper
 */
public class OutputCSSContentModelElement extends DependencyDeferredContentTargetModelElement
{
    private static final String TYPE = "DependencyContent";

    /**
     * <p>Constructor for OutputCSSContentModelElement.</p>
     *
     * @param id a {@link java.lang.String} object
     * @param directiveName a {@link java.lang.String} object
     * @param dependencyAggregator a {@link org.springframework.extensions.surf.DependencyAggregator} object
     */
    public OutputCSSContentModelElement(String id, 
                                        String directiveName,
                                        DependencyAggregator dependencyAggregator)
    {
        super(id, directiveName);
        this.dependencyAggregator = dependencyAggregator;
        this.resourceControllerMapping = this.dependencyAggregator.getServletContext().getContextPath() + this.dependencyAggregator.getDependencyHandler().getResourceControllerMapping() + CssImageDataHandler.FORWARD_SLASH;
    }

    private DependencyAggregator dependencyAggregator;
    private String resourceControllerMapping;
    /**
     * <p>Getter for the field <code>resourceControllerMapping</code>.</p>
     *
     * @return a {@link java.lang.String} object
     */
    protected String getResourceControllerMapping()
    {
        return resourceControllerMapping;
    }

    /** {@inheritDoc} */
    @Override
    public String getType()
    {
        return TYPE;
    }

    /**
     * <p>A map of group id to CSS dependencies. The CSS dependencies are a map of media types to resources. Group ids
     * can be arbitrarily defined so that CSS resources get merged together in a logical fashion.</p>
     */
    private LinkedHashMap<String, HashMap<String, LinkedHashSet<String>>> cssGroupToDependencyMap = new LinkedHashMap<String, HashMap<String,LinkedHashSet<String>>>();
    
    /**
     * <p>Getter for the field <code>cssGroupToDependencyMap</code>.</p>
     *
     * @return a {@link java.util.LinkedHashMap} object
     */
    public LinkedHashMap<String, HashMap<String, LinkedHashSet<String>>> getCssGroupToDependencyMap()
    {
        return this.cssGroupToDependencyMap;
    }

    /**
     * <p>A map of group id to CSS dependencies FOR AGGREGATION. The CSS dependencies are a map of media types to resources. Group ids
     * can be arbitrarily defined so that CSS resources get merged together in a logical fashion.</p>
     */
    private LinkedHashMap<String, HashMap<String, LinkedHashSet<String>>> aggCssGroupToDependencyMap = new LinkedHashMap<String, HashMap<String,LinkedHashSet<String>>>();
    
    /**
     * <p>Getter for the field <code>aggCssGroupToDependencyMap</code>.</p>
     *
     * @return a {@link java.util.LinkedHashMap} object
     */
    public LinkedHashMap<String, HashMap<String, LinkedHashSet<String>>> getAggCssGroupToDependencyMap()
    {
        return this.aggCssGroupToDependencyMap;
    }
    
    /**
     * <p>A map of group id to CSS dependencies FOR AGGREGATION. The CSS dependencies are a map of media types to resources. Group ids
     * can be arbitrarily defined so that CSS resources get merged together in a logical fashion.</p>
     */
    private LinkedHashMap<String, HashMap<String, LinkedHashSet<String>>> dojoCssGroupToDependencyMap = new LinkedHashMap<String, HashMap<String,LinkedHashSet<String>>>();
    
    /**
     * <p>Getter for the field <code>dojoCssGroupToDependencyMap</code>.</p>
     *
     * @return a {@link java.util.LinkedHashMap} object
     */
    public LinkedHashMap<String, HashMap<String, LinkedHashSet<String>>> getDojoCssGroupToDependencyMap()
    {
        return this.dojoCssGroupToDependencyMap;
    }
    
    /**
     * <p>Adds a new CSS dependency. These are organised into groups, where each group is map of media type to resource.
     * This ensures that when the CSS dependencies are requested the HTML elements are defined correctly. The grouping
     * allows merges to be made logically.</p>
     *
     * @param fileName The name of the dependency
     * @param mediaType The media type for the dependency (e.g. "screen", etc)
     * @param group The id of the group to add the dependency to (if <code>null</code> will go into the default group)
     * @param forAggregation a boolean
     */
    public void addCssDependency(String fileName, String mediaType, String group, boolean forAggregation)
    {
        fileName = this.normaliseDependency(fileName);
        if (dependencyAlreadyRequested(fileName))
        {
            // No action required. Don't add a duplicate dependency.
        }
        else
        {
            markDependencyAsRequested(fileName);
            if (forAggregation)
            {
                this.addCssDependency(fileName, mediaType, group, this.aggCssGroupToDependencyMap);
            }
            else
            {
                String checksumPath = this.dependencyAggregator.getDependencyHandler().getChecksumPath(fileName);
                this.addCssDependency(this.resourceControllerMapping + checksumPath, mediaType, group, this.cssGroupToDependencyMap);
            }
        }
    }
    
    /**
     * Adds a new CSS dependency.
     *
     * @param fileName String
     * @param mediaType String
     * @param group Stringvvv
     * @param cssGroupToDependencyMap a {@link java.util.LinkedHashMap} object
     */
    public void addCssDependency(String fileName,
                                 String mediaType, 
                                 String group, 
                                 LinkedHashMap<String, HashMap<String, LinkedHashSet<String>>> cssGroupToDependencyMap) {
        HashMap<String, LinkedHashSet<String>> mediaToDependencyMap = cssGroupToDependencyMap.get(group);
        if (mediaToDependencyMap == null)
        {
            // The requested group has not yet been defined to we will create it now...
            mediaToDependencyMap = new HashMap<String, LinkedHashSet<String>>();
            cssGroupToDependencyMap.put(group, mediaToDependencyMap); // Add the new group to the map
        }
        
        // Check if the media type already exists for the current group...
        LinkedHashSet<String> cssDependencies = mediaToDependencyMap.get(mediaType);
        if (cssDependencies == null)
        {
            // ...the media type has not yet been defined so create it now...
            cssDependencies = new LinkedHashSet<String>();
            mediaToDependencyMap.put(mediaType, cssDependencies);
        }

        // Finally, add the dependency...
        cssDependencies.add(fileName);
    }
    
    /**
     * The Dojo generated CSS dependencies need to be handled separately from the those provided by the {@code <@link>} directive.
     * The reason for this is that Dojo dependencies get added as soon as they are encountered but the {@code <@link>} directive
     * dependencies only get added after all extensions have finished processing (this is so that the {@link org.springframework.extensions.surf.extensibility.DeferredContentSourceModelElement}
     * instances can be manipulated to add/remove/change dependency requests). However, we want to only output Dojo dependencies
     * if they have <b>not</b> requested via a {@code <@link>} directive.
     *
     * @param fileName String
     * @param mediaType String
     * @param group String
     */
    public void addDojoCssDependency(String fileName, 
                                     String mediaType,
                                     String group)
    {
        fileName = this.normaliseDependency(fileName);
        this.addCssDependency(fileName, mediaType, group, this.dojoCssGroupToDependencyMap);
    }

    /**
     * Filters CSS dependencies that have already been requested from the supplied map.
     *
     * @param dependencies The dependency map to filter duplicates from.
     * @return LinkedHashMap
     */
    protected LinkedHashMap<String, HashMap<String, LinkedHashSet<String>>> filterCssDependencies(LinkedHashMap<String, HashMap<String, LinkedHashSet<String>>> dependencies) {
        
        // Iterate over the supplied map...
        LinkedHashMap<String, HashMap<String, LinkedHashSet<String>>> filteredMap = new LinkedHashMap<String, HashMap<String,LinkedHashSet<String>>>();
        for (Entry<String, HashMap<String, LinkedHashSet<String>>> groupEntry: dependencies.entrySet())
        {
            // Iterate over each group map...
            HashMap<String, LinkedHashSet<String>> filteredInnerMap = new HashMap<String, LinkedHashSet<String>>();
            filteredMap.put(groupEntry.getKey(), filteredInnerMap);
            for (Entry<String, LinkedHashSet<String>> mediaToCssResource : groupEntry.getValue().entrySet())
            {
                // Iterate over each media map...
                LinkedHashSet<String> filteredResources = new LinkedHashSet<String>();
                filteredInnerMap.put(mediaToCssResource.getKey(), filteredResources);
                for (String cssResource: mediaToCssResource.getValue())
                {
                    // Check the resource...
                    if (dependencyAlreadyRequested(cssResource))
                    {
                        // If the CSS resource has already been requested then it needs to be removed
                    }
                    else
                    {
                        // If the CSS resource hasn't been requested yet then add it to the filtered list and
                        // mark it as having been requested...
                        filteredResources.add(cssResource);
                        this.markDependencyAsRequested(cssResource);
                    }
                }
            }
        }
        return filteredMap;
    }
    
    /**
     * {@inheritDoc}
     *
     * <p>
     * This method will be called when the ExtensibilityModel containing
     * this  ContentModelElement is closed. At this point any nested
     * models will have already added their required JavaScript and CSS
     * dependencies. The lists of dependencies will be iterated over to generate
     * the list of import statements. If we're in debug mode then each
     * JavaScript and CSS file will be requested separately. In production mode
     * we will aim to generate the minimum set of files to improve clientside
     * performance.
     */
    @Override
    public String flushContent()
    {
        StringBuilder content = new StringBuilder();
        
        // Build the dependencies in the order requested following all extension processing...
        for (DeferredContentSourceModelElement sourceElement: this.getSourceElements())
        {
            if (sourceElement instanceof CssDependencyContentModelElement)
            {
                // Sort the elements into their respective groups...
                CssDependencyContentModelElement dependency = (CssDependencyContentModelElement) sourceElement;
                addCssDependency(dependency.getDependency(), dependency.getMedia(), dependency.getGroup(), ((CssDependencyContentModelElement) sourceElement).isAggregate());
            }
        }
        
        content.append(generateCSSDependencies(this.cssGroupToDependencyMap, false));
        content.append(generateCSSDependencies(this.aggCssGroupToDependencyMap, true));
        
        // Filter out any already requested dependencies from those requested by Dojo widgets...
        LinkedHashMap<String, HashMap<String, LinkedHashSet<String>>> filteredDojoCssDependencyMap = this.filterCssDependencies(this.dojoCssGroupToDependencyMap);
        content.append(generateCSSDependencies(filteredDojoCssDependencyMap, true));
        return content.toString();
    }
   
    /**
     * <p>Generates a {@link StringBuilder} containing all the statements required to import the supplied
     * map of media to CSS files.</p>
     * 
     * @param cssMap LinkedHashMap<String, HashMap<String, LinkedHashSet<String>>>
     * @return StringBuilder
     */
    private StringBuilder generateCSSDependencies(LinkedHashMap<String, HashMap<String, LinkedHashSet<String>>> cssMap, boolean aggregate)
    {
        StringBuilder cssDeps = new StringBuilder();
        
        // Iterate over each group...
        for (HashMap<String, LinkedHashSet<String>> group: cssMap.values())
        {
            // Iterate over all media types for the group...
            for (Entry<String, LinkedHashSet<String>> mediaToCssResource : group.entrySet())
            {
                if (aggregate)
                {
                    // Append the specific CSS dependencies as an aggregated resource
                    String checksum = dependencyAggregator.generateCSSDependencies(mediaToCssResource.getValue());
                    appendCSSLink(cssDeps, resourceControllerMapping + checksum, mediaToCssResource.getKey());
                }
                else
                {
                	
                // beCPG #9470
                	
                    // When not aggregating dependencies we need to request each CSS resource individually...
                    // Open a <style> element...
//                    cssDeps.append(DirectiveConstants.OPEN_CSS_1);
//                    cssDeps.append(mediaToCssResource.getKey());
//                    cssDeps.append(DirectiveConstants.OPEN_CSS_2);
                    
                    // IE can only handle 31 import statements per style element so we 
                    // need to make sure that we don't create to many. We'll keep track
                    // of the number of imports added and create a new style tag once
                    // we get to 31...
//                    int count = 0;
                    for (String cssResource: mediaToCssResource.getValue())
                    {
                    	
                    	
                 	    cssDeps.append(DirectiveConstants.OPEN_LINK_TAG);
                        cssDeps.append(cssResource);
                        cssDeps.append(DirectiveConstants.SET_MEDIA);
                       // cssDeps.append(mediaToCssResource.getKey());
                        if("screen" == mediaToCssResource.getKey()) {
                        	cssDeps.append("all");
                        }
                        cssDeps.append(DirectiveConstants.CLOSE_LINK_TAG);
//                    	
//                        count++;
//                        
//                        // Output each CSS resource as an "@import" statement...
//                        cssDeps.append(DirectiveConstants.CSS_IMPORT);
//                        cssDeps.append(cssResource);
//                        cssDeps.append(DirectiveConstants.DELIMIT_CSS_IMPORT);
//                        if (count == 31)
//                        {
//                            // We've hit 31 imports... close the current style element and start a new one to
//                            // avoid IE failing to load the files.
//                            cssDeps.append(DirectiveConstants.CLOSE_CSS);
//                            cssDeps.append(DirectiveConstants.OPEN_CSS_1);
//                            cssDeps.append(mediaToCssResource.getKey());
//                            cssDeps.append(DirectiveConstants.OPEN_CSS_2);
//                            count = 0;
//                        }
                    }
                 //   cssDeps.append(DirectiveConstants.CLOSE_CSS); // Close the <style> element
                }
            }
        }
        return cssDeps;
    }
    
    /**
     * <p>Appends a single HTML link statement to the supplied {@link StringBuilder} that will load
     * the supplied CSS file.</p>
     *  
     * @param cssDeps The {@link StringBuilder} to append the statement to.
     * @param file The file to be loaded.
     * @param media The media type of the link (e.g. "screen")
     */
    private void appendCSSLink(StringBuilder cssDeps, String file, String media)
    {
        cssDeps.append(DirectiveConstants.OPEN_LINK_TAG);
        cssDeps.append(file);
        cssDeps.append(DirectiveConstants.SET_MEDIA);
        cssDeps.append(media);
        cssDeps.append(DirectiveConstants.CLOSE_LINK_TAG);
    }
}
