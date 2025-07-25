/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package fr.becpg.repo.multilingual;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.helper.MLTextHelper;

/**
 * Interceptor to filter out multilingual text properties from getter methods and
 * transform to multilingual text for setter methods.
 * <p>
 * This interceptor ensures that all multilingual (ML) text is transformed to the
 * locale chosen for the request
 * for getters and transformed to the default locale type for setters.
 * <p>
 * Where {@link org.alfresco.service.cmr.repository.MLText ML text} has been passed in, this
 * will be allowed to pass.
 *
 * @see org.alfresco.service.cmr.repository.NodeService#getProperty(NodeRef, QName)
 * @see org.alfresco.service.cmr.repository.NodeService#getProperties(NodeRef)
 * @see org.alfresco.service.cmr.repository.NodeService#setProperty(NodeRef, QName, Serializable)
 * @see org.alfresco.service.cmr.repository.NodeService#setProperties(NodeRef, Map)
 * @author Derek Hulley
 * @author Philippe Dubois
 * @version $Id: $Id
 */
public class BeCPGMLPropertyInterceptor implements MethodInterceptor
{
    private static Log logger = LogFactory.getLog(BeCPGMLPropertyInterceptor.class);
  
    /** Direct access to the NodeService */
    private NodeService nodeService;
    
    /** Direct access to the MultilingualContentService */
    private MultilingualContentService multilingualContentService;
    
    /** Used to access property definitions */
    private DictionaryService dictionaryService;
    
    private NamespaceService namespaceService;
    

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}
	

	/**
	 * <p>isMLAware.</p>
	 *
	 * @return Returns true if the current thread has marked itself
	 *      as being able to handle {@link MLText d:mltext} types properly.
	 */
	public static boolean isMLAware()
    {
       return MLPropertyInterceptor.isMLAware();
    }

    /**
     * <p>Setter for the field <code>nodeService</code>.</p>
     *
     * @param bean a {@link org.alfresco.service.cmr.repository.NodeService} object.
     */
    public void setNodeService(NodeService bean)
    {
        this.nodeService = bean;
    }

    /**
     * <p>Setter for the field <code>multilingualContentService</code>.</p>
     *
     * @param multilingualContentService a {@link org.alfresco.service.cmr.ml.MultilingualContentService} object.
     */
    public void setMultilingualContentService(MultilingualContentService multilingualContentService)
    {
        this.multilingualContentService = multilingualContentService;
    }

    /**
     * <p>Setter for the field <code>dictionaryService</code>.</p>
     *
     * @param dictionaryService a {@link org.alfresco.service.cmr.dictionary.DictionaryService} object.
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    
    
    
    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public Object invoke(final @Nonnull MethodInvocation invocation) throws Throwable
    {
       
        
        // If isMLAware then no treatment is done, just return
        if (isMLAware())
        {    
            // Don't interfere
            return invocation.proceed();
        }
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Intercepting method " + invocation.getMethod().getName() + " using content filter " + I18NUtil.getContentLocale());
        }
        
        Object ret = null;
        
        final String methodName = invocation.getMethod().getName();
        final Object[] args = invocation.getArguments();
        
        if (methodName.equals("getProperty"))
        {
            NodeRef nodeRef = (NodeRef) args[0];
            QName propertyQName = (QName) args[1];
            
            // Get the pivot translation, if appropriate
            NodeRef pivotNodeRef = getPivotNodeRef(nodeRef);
            
            // What locale must be used for filtering - ALF-3756 fix, ignore the country and variant
            Serializable value = (Serializable) invocation.proceed();
            ret = convertOutboundProperty(nodeRef, pivotNodeRef, propertyQName, value);
        }
        else if (methodName.equals("getProperties"))
        {
            NodeRef nodeRef = (NodeRef) args[0];
            
            // Get the pivot translation, if appropriate
            NodeRef pivotNodeRef = getPivotNodeRef(nodeRef);
            
            Map<QName, Serializable> properties = (Map<QName, Serializable>) invocation.proceed();
            Map<QName, Serializable> convertedProperties = null;
            if (properties != null) {
                convertedProperties = new HashMap<>(properties.size() * 2);
                // Check each return value type
                for (Map.Entry<QName, Serializable> entry : properties.entrySet())
                {
                    QName propertyQName = entry.getKey();
                    Serializable value = entry.getValue();
                    Serializable convertedValue = convertOutboundProperty(nodeRef, pivotNodeRef, propertyQName, value);
                    // Add it to the return map
                    convertedProperties.put(propertyQName, convertedValue);
                }
            }
            ret = convertedProperties;
            // Done
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Converted getProperties return value: \n" +
                        "   initial:   " + properties + "\n" +
                        "   converted: " + convertedProperties);
            }
        }
        else if (methodName.equals("setProperties"))
        {
            NodeRef nodeRef = (NodeRef) args[0];
            Map<QName, Serializable> newProperties =(Map<QName, Serializable>) args[1];
            
            // Get the pivot translation, if appropriate
            NodeRef pivotNodeRef = getPivotNodeRef(nodeRef);

            // Get the current properties for the node
            Map<QName, Serializable> currentProperties = nodeService.getProperties(nodeRef);
            // Convert all properties
            Map<QName, Serializable> convertedProperties = convertInboundProperties(
                    currentProperties,
                    newProperties,
                    nodeRef,
                    pivotNodeRef);
            // Now complete the call by passing the converted properties
            nodeService.setProperties(nodeRef, convertedProperties);
            // Done
        }
        else if (methodName.equals("addProperties"))
        {
            NodeRef nodeRef = (NodeRef) args[0];
            Map<QName, Serializable> newProperties =(Map<QName, Serializable>) args[1];
            
            // Get the pivot translation, if appropriate
            NodeRef pivotNodeRef = getPivotNodeRef(nodeRef);

            // Get the current properties for the node
            Map<QName, Serializable> currentProperties = nodeService.getProperties(nodeRef);
            // Convert all properties
            Map<QName, Serializable> convertedProperties = convertInboundProperties(
                    currentProperties,
                    newProperties,
                    nodeRef,
                    pivotNodeRef);
            // Now complete the call by passing the converted properties
            nodeService.addProperties(nodeRef, convertedProperties);
            // Done
        }
        else if (methodName.equals("setProperty"))
        {
            NodeRef nodeRef = (NodeRef) args[0];
            QName propertyQName = (QName) args[1];
            Serializable inboundValue = (Serializable) args[2];
            
            // Get the pivot translation, if appropriate
            NodeRef pivotNodeRef = getPivotNodeRef(nodeRef);
            
            // Convert the property
            inboundValue = convertInboundProperty(nodeRef, pivotNodeRef, propertyQName, inboundValue, null);
            
            // Pass this through to the node service
            nodeService.setProperty(nodeRef, propertyQName, inboundValue);
            // Done
        }
        else if (methodName.equals("createNode") && args.length > 4)
        {
            NodeRef parentNodeRef = (NodeRef) args[0];
            QName assocTypeQName = (QName) args[1];
            QName assocQName = (QName) args[2];
            QName nodeTypeQName = (QName) args[3];
            Map<QName, Serializable> newProperties =(Map<QName, Serializable>) args[4];
            if (newProperties == null)
            {
                newProperties = Collections.emptyMap();
            }
            NodeRef nodeRef = null;                 // Not created yet
            
            // No pivot
            NodeRef pivotNodeRef = null;

            // Convert all properties
            Map<QName, Serializable> convertedProperties = convertInboundProperties(
                    null,
                    newProperties,
                    nodeRef,
                    pivotNodeRef);
            // Now complete the call by passing the converted properties
            ret = nodeService.createNode(parentNodeRef, assocTypeQName, assocQName, nodeTypeQName, convertedProperties);
            // Done
        }
        else if (methodName.equals("addAspect") && args[2] != null)
        {
            NodeRef nodeRef = (NodeRef) args[0];
            QName aspectTypeQName = (QName) args[1];
            
            // Get the pivot translation, if appropriate
            NodeRef pivotNodeRef = getPivotNodeRef(nodeRef);

            Map<QName, Serializable> newProperties =(Map<QName, Serializable>) args[2];
            // Get the current properties for the node
            Map<QName, Serializable> currentProperties = nodeService.getProperties(nodeRef);
            // Convert all properties
            Map<QName, Serializable> convertedProperties = convertInboundProperties(
                    currentProperties,
                    newProperties,
                    nodeRef,
                    pivotNodeRef);
            // Now complete the call by passing the converted properties
            nodeService.addAspect(nodeRef, aspectTypeQName, convertedProperties);
            // Done
        }
        else
        {
            ret = invocation.proceed();
        }
        // done
        return ret;
    }
    
    /**
     * @param nodeRef
     *      a potential empty translation
     * @return
     *      the pivot translation node or <tt>null</tt>
     */
    private NodeRef getPivotNodeRef(NodeRef nodeRef)
    {
        if (nodeRef == null)
        {
            throw new IllegalArgumentException("NodeRef may not be null for calls to NodeService.  Check client code.");
        }
        if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION))
        {
            return multilingualContentService.getPivotTranslation(nodeRef);
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Ensure that content is spoofed for empty translations.
     */
    private Serializable convertOutboundProperty(
            NodeRef nodeRef,
            NodeRef pivotNodeRef,
            QName propertyQName,
            Serializable outboundValue)
    {
        Serializable ret = null;
        if (outboundValue instanceof MLText mlText)
        {
            // It is MLText
            ret = MLTextHelper.getClosestValue(mlText, getLocale(propertyQName));
        }
        else if(isCollectionOfMLText(outboundValue))
        {
            Collection<?> col = (Collection<?>)outboundValue; 
            ArrayList<String> answer = new ArrayList<>(col.size());
            Locale closestLocale = getClosestLocale(col);
            for(Object o : col)
            {
                MLText mlText = (MLText) o;
                String value = mlText.get(closestLocale);
                if(value != null)
                {
                    answer.add(value);
                }
            }
            ret = answer;
        }
        else if (pivotNodeRef != null)       // It is an empty translation
        {
           if (propertyQName.equals(ContentModel.PROP_MODIFIED))
           {
              // An empty translation's modified date must be the later of its own
              // modified date and the pivot translation's modified date
              Date emptyLastModified = (Date) outboundValue;
              Date pivotLastModified = (Date) nodeService.getProperty(pivotNodeRef, ContentModel.PROP_MODIFIED);
              if (emptyLastModified!=null && emptyLastModified.compareTo(pivotLastModified) < 0)
              {
                 ret = pivotLastModified;
              }
              else
              {
                 ret = emptyLastModified;
              }
           }
           else if (propertyQName.equals(ContentModel.PROP_CONTENT))
           {
              // An empty translation's cm:content must track the cm:content of the
              // pivot translation.
              ret = nodeService.getProperty(pivotNodeRef, ContentModel.PROP_CONTENT);
           }
           else
           {
              ret = outboundValue;
           }
        }
        else
        {
            ret = outboundValue;
        }
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Converted outbound property: \n" +
                    "   NodeRef:        " + nodeRef + "\n" +
                    "   Property:       " + propertyQName + "\n" +
                    "   Before:         " + outboundValue + "\n" +
                    "   After:          " + ret);
        }
        return ret;
    }
    
   
    /**
     * <p>getClosestLocale.</p>
     *
     * @param collection a {@link java.util.Collection} object.
     * @return a {@link java.util.Locale} object.
     */
    public Locale getClosestLocale(Collection<?> collection)
    {
        if (collection.isEmpty())
        {
            return null;
        }
        // Use the available keys as options
        HashSet<Locale> locales = new HashSet<>();
        for(Object o : collection)
        {
            MLText mlText = (MLText)o;
            locales.addAll(mlText.keySet());
        }
        // Try the content locale
        Locale locale = I18NUtil.getContentLocale();
        Locale match = I18NUtil.getNearestLocale(locale, locales);
        if (match == null)
        {
            // Try just the content locale language
            locale = I18NUtil.getContentLocaleLang();
            match = MLTextHelper.getNearestLocale(locale, locales);
            if (match == null)
            {
                // No close matches for the locale - go for the default locale
                locale = I18NUtil.getLocale();
                match = MLTextHelper.getNearestLocale(locale, locales);
             
                if (match == null)
                {
                    // just get any locale
                    match = MLTextHelper.getNearestLocale(Locale.getDefault(), locales);
                }
               
            }
        }
        return match;
    }
    
    /**
     * @param outboundValue Serializable
     * @return boolean
     */
    private boolean isCollectionOfMLText(Serializable outboundValue)
    {
        if(outboundValue instanceof Collection<?>)
        {
            for(Object o : (Collection<?>)outboundValue)
            {
                if(!(o instanceof MLText))
                {
                    return false;
                }
            }
            return true;
        }
        else
        {
            return false;
        }
    }

    private Map<QName, Serializable> convertInboundProperties(
            Map<QName, Serializable> currentProperties,
            Map<QName, Serializable> newProperties,
            NodeRef nodeRef,
            NodeRef pivotNodeRef)
    {
        Map<QName, Serializable> convertedProperties = new HashMap<>(newProperties.size() * 2);
        for (Map.Entry<QName, Serializable> entry : newProperties.entrySet())
        {
             QName propertyQName = entry.getKey();
             Serializable inboundValue = entry.getValue();
             // Get the current property value
             Serializable currentValue = currentProperties == null ? null : currentProperties.get(propertyQName);
             // Convert the inbound property value
             inboundValue = convertInboundProperty( nodeRef, pivotNodeRef, propertyQName, inboundValue, currentValue);
             // Put the value into the map
             convertedProperties.put(propertyQName, inboundValue);
        }
        return convertedProperties;
    }
    
    /**
     * 
     * @param inboundValue      The value that must be set
     * @param currentValue      The current value of the property or <tt>null</tt> if not known
     * @return                  Returns a potentially converted property that conforms to the model
     */
    private Serializable convertInboundProperty(
            NodeRef nodeRef,
            NodeRef pivotNodeRef,
            QName propertyQName,
            Serializable inboundValue,
            Serializable currentValue)
    {
        Serializable ret = null;
        PropertyDefinition propertyDef = this.dictionaryService.getProperty(propertyQName);
        //if no type definition associated to the name then just proceed
        if (propertyDef == null)
        {
           ret = inboundValue;
        }
        else if (propertyDef.getDataType().getName().equals(DataTypeDefinition.MLTEXT))
        {
            // Don't mess with multivalued properties or instances already of type MLText
            if (inboundValue instanceof MLText)
            {
                ret = inboundValue;
            }
            else if(propertyDef.isMultiValued())
            {
                // leave collectios of ML text alone
                if(isCollectionOfMLText(inboundValue))
                {
                    ret = inboundValue;
                }
                else
                {
                    // Anything else we assume is localised
                    if (currentValue == null && nodeRef != null)
                    {
                        currentValue = nodeService.getProperty(nodeRef, propertyQName);
                    }
                    ArrayList<MLText> returnMLList = new ArrayList<>();
                    if (currentValue != null)
                    {
                        Collection<MLText> currentCollection = DefaultTypeConverter.INSTANCE.getCollection(MLText.class, currentValue);  
                        returnMLList.addAll(currentCollection);
                    }
                    Collection<String> inboundCollection = DefaultTypeConverter.INSTANCE.getCollection(String.class, inboundValue);
                    int count = 0;
                    for(String current : inboundCollection)
                    {
                        MLText newMLValue;
                        if(count < returnMLList.size())
                        { 
                            MLText currentMLValue = returnMLList.get(count);
                            newMLValue = new MLText();
                            if (currentMLValue != null)
                            {
                                newMLValue.putAll(currentMLValue);
                            }                
                        }
                        else
                        {
                            newMLValue = new MLText();
                        }
                        MLTextHelper.replaceTextForLanguage(getLocale(propertyQName ), current, newMLValue);
                        if(count < returnMLList.size())
                        {
                            returnMLList.set(count, newMLValue);
                        }
                        else
                        {
                            returnMLList.add(newMLValue);
                        }
                        count++;
                    }
                    // remove locale settings for anything after
                    for(int i = count; i < returnMLList.size(); i++)
                    {
                        MLText currentMLValue = returnMLList.get(i);
                        MLText newMLValue = new MLText();
                        if (currentMLValue != null)
                        {
                            newMLValue.putAll(currentMLValue);
                        }
                        newMLValue.remove(getLocale(propertyQName ));
                        returnMLList.set(i, newMLValue);
                    }
                    // tidy up empty locales
                    ArrayList<MLText> tidy = new ArrayList<>();
                    for(MLText mlText : returnMLList)
                    {
                        if(!mlText.keySet().isEmpty())
                        {
                            tidy.add(mlText);
                        }
                    }
                    ret = tidy;
                }
            }
            else
            {
                // This is a multilingual single-valued property
                // Get the current value from the node service, if not provided
                if (currentValue == null && nodeRef != null)
                {
                    currentValue = nodeService.getProperty(nodeRef, propertyQName);
                }
                MLText returnMLValue = new MLText();
                if (currentValue != null)
                {
                    MLText currentMLValue = DefaultTypeConverter.INSTANCE.convert(MLText.class, currentValue);
                    returnMLValue.putAll(currentMLValue);                   
                }
                // Force the inbound value to be a String (it isn't MLText)
                String inboundValueStr = DefaultTypeConverter.INSTANCE.convert(String.class, inboundValue);
                // Update the text for the appropriate language.
                MLTextHelper.replaceTextForLanguage(getLocale(propertyQName ), inboundValueStr, returnMLValue);
                // Done
                ret = returnMLValue;
            }
        }
        else if (pivotNodeRef != null && propertyQName.equals(ContentModel.PROP_CONTENT))
        {
           // It is an empty translation.  The content must not change if it matches
           // the content of the pivot translation
           ContentData pivotContentData = (ContentData) nodeService.getProperty(pivotNodeRef, ContentModel.PROP_CONTENT);
           ContentData emptyContentData = (ContentData) inboundValue;
           String pivotContentUrl = pivotContentData == null ? null : pivotContentData.getContentUrl();
           String emptyContentUrl = emptyContentData == null ? null : emptyContentData.getContentUrl();
           if (EqualsHelper.nullSafeEquals(pivotContentUrl, emptyContentUrl))
           {
              // They are a match.  So the empty translation must be reset to it's original value
              ret = nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
           }
           else
           {
              ret = inboundValue;
           }
        }
        else
        {
            ret = inboundValue;
        }
        // Done
        if (logger.isDebugEnabled() && ret != inboundValue)
        {
            logger.debug("Converted inbound property: \n" +
                    "   NodeRef:    " + nodeRef + "\n" +
                    "   Property:   " + propertyQName + "\n" +
                    "   Before:     " + inboundValue + "\n" +
                    "   After:      " + ret);
        }
        return ret;
    }

    private Locale getLocale( QName propertyQName) {
    	if(propertyQName!=null && MLTextHelper.isDisabledMLTextField(propertyQName.toPrefixString(namespaceService))){
    		return Locale.getDefault();
    	}
		return I18NUtil.getContentLocale();
	}

	
}
