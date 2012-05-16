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
package org.alfresco.repo.thumbnail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.content.transform.TransformerDebug;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.thumbnail.ThumbnailException;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * Registry of all the thumbnail details available
 * 
 * @author Roy Wetherall
 * @author Neil McErlean
 */
public class ThumbnailRegistry implements ApplicationContextAware, ApplicationListener<ApplicationContextEvent>
{
    /** Logger */
    private static Log logger = LogFactory.getLog(ThumbnailRegistry.class);

    /** Content service */
    private ContentService contentService;
    
    /** Transaction service */
    private TransactionService transactionService;
    
    /** Rendition service */
    private RenditionService renditionService;
    
    private TenantAdminService tenantAdminService;
    
    /** Map of thumbnail definition */
    private Map<String, ThumbnailDefinition> thumbnailDefinitions = new HashMap<String, ThumbnailDefinition>();
    
    /** Cache to store mimetype to thumbnailDefinition mapping */
    private Map<String, List<ThumbnailDefinition>> mimetypeMap = new HashMap<String, List<ThumbnailDefinition>>(17);

    private ThumbnailRenditionConvertor thumbnailRenditionConvertor;
    
    private RegistryLifecycle lifecycle = new RegistryLifecycle();
    
    public void setThumbnailRenditionConvertor(
            ThumbnailRenditionConvertor thumbnailRenditionConvertor)
    {
        this.thumbnailRenditionConvertor = thumbnailRenditionConvertor;
    }

    public ThumbnailRenditionConvertor getThumbnailRenditionConvertor()
    {
        return thumbnailRenditionConvertor;
    }
    
    /**
     * Content service
     * 
     * @param contentService    content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    /**
     * Transaction service
     * 
     * @param transactionService    transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
    
    /**
     * Rendition service
     * 
     * @param renditionService    rendition service
     */
    public void setRenditionService(RenditionService renditionService)
    {
        this.renditionService = renditionService;
    }
    
    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }

    /**
     * This method is used to inject the thumbnail definitions.
     * @param thumbnailDefinitions
     */
    public void setThumbnailDefinitions(final List<ThumbnailDefinition> thumbnailDefinitions)
    {
        for (ThumbnailDefinition td : thumbnailDefinitions)
        {
            String thumbnailName = td.getName();
            if (thumbnailName == null)
            {
                throw new ThumbnailException("When adding a thumbnail details object make sure the name is set.");
            }
            
            this.thumbnailDefinitions.put(thumbnailName, td);
        }
    }
    
    // Otherwise we should go ahead and persist the thumbnail definitions.
    // This is done during system startup. It needs to be done as the system user (see callers) to ensure the thumbnail definitions get saved
    // and also needs to be done within a transaction in order to support concurrent startup. See ALF-6271 for details.
    public void initThumbnailDefinitions()
    {
        RetryingTransactionHelper transactionHelper = transactionService.getRetryingTransactionHelper();
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                for (String thumbnailDefName : thumbnailDefinitions.keySet())
                {
                    final ThumbnailDefinition thumbnailDefinition = thumbnailDefinitions.get(thumbnailDefName);
                    
                    // Built-in thumbnailDefinitions do not provide any non-standard values
                    // for the ThumbnailParentAssociationDetails object. Hence the null
                    RenditionDefinition renditionDef = thumbnailRenditionConvertor.convert(thumbnailDefinition, null);
                    
                    // Thumbnail definitions are saved into the repository as actions
                    renditionService.saveRenditionDefinition(renditionDef);
                }
                return null;
            }
        });
    }
    
    /**
     * Get a list of all the thumbnail definitions
     * 
     * @return Collection<ThumbnailDefinition>  collection of thumbnail definitions
     */
    public List<ThumbnailDefinition> getThumbnailDefinitions()
    {
        return new ArrayList<ThumbnailDefinition>(this.thumbnailDefinitions.values());
    }
    
    /**
     * @deprecated use overloaded version with sourceSize parameter.
     */
    public List<ThumbnailDefinition> getThumbnailDefinitions(String mimetype)
    {
        return getThumbnailDefinitions(null, mimetype, -1);
    }
    
    public List<ThumbnailDefinition> getThumbnailDefinitions(String sourceUrl, String mimetype, long sourceSize)
    {
        List<ThumbnailDefinition> result = this.mimetypeMap.get(mimetype);
        
        if (result == null)
        {
            boolean foundAtLeastOneTransformer = false;
            result = new ArrayList<ThumbnailDefinition>(7);
            
            for (ThumbnailDefinition thumbnailDefinition : this.thumbnailDefinitions.values())
            {
                if (isThumbnailDefinitionAvailable(sourceUrl, mimetype, sourceSize, thumbnailDefinition))
                {
                    result.add(thumbnailDefinition);
                    foundAtLeastOneTransformer = true;
                }
            }
            
            // If we have found no transformers for the given MIME type then we do
            // not cache the empty list. We prevent this because we want to allow for
            // transformers only coming online *during* system operation - as opposed
            // to coming online during startup.
            //
            // An example of such a transient transformer would be those that use OpenOffice.org.
            // It is possible that the system might start without OOo-based transformers
            // being available. Therefore we must not cache an empty list for the relevant
            // MIME types - otherwise this class would hide the fact that OOo (soffice) has
            // been launched and that new transformers are available.
            if (foundAtLeastOneTransformer)
            {
                this.mimetypeMap.put(mimetype, result);
            }
        }
        
        return result;
    }
    
    /**
     * 
     * @param mimetype
     * @return
     * @deprecated Use {@link #getThumbnailDefinitions(String)} instead.
     */
    @Deprecated
    public List<ThumbnailDefinition> getThumnailDefintions(String mimetype)
    {
        return this.getThumbnailDefinitions(mimetype);
    }
    
    /**
     * Checks to see if at this moment in time, the specified {@link ThumbnailDefinition}
     *  is able to thumbnail the source mimetype. Typically used with Thumbnail Definitions
     *  retrieved by name, and/or when dealing with transient {@link ContentTransformer}s.
     * @param sourceUrl The URL of the source (optional)
     * @param sourceMimeType The source mimetype
     * @param sourceSize the size (in bytes) of the source. Use -1 if unknown.
     * @param thumbnailDefinition The {@link ThumbnailDefinition} to check for
     */
    public boolean isThumbnailDefinitionAvailable(String sourceUrl, String sourceMimeType, long sourceSize, ThumbnailDefinition thumbnailDefinition)
    {
        // Log the following getTransform() as trace so we can see the wood for the trees
        boolean orig = TransformerDebug.setDebugOutput(false);
        try
        {
            return this.contentService.getTransformer(
                    sourceUrl, 
                    sourceMimeType,
                    sourceSize, 
                    thumbnailDefinition.getMimetype(), thumbnailDefinition.getTransformationOptions()
              ) != null;
        }
        finally
        {
            TransformerDebug.setDebugOutput(orig);
        }
    }
    
    /**
     * Add a thumbnail details
     * 
     * @param thumbnailDetails  thumbnail details
     */
    public void addThumbnailDefinition(ThumbnailDefinition thumbnailDetails)
    {
        String thumbnailName = thumbnailDetails.getName();
        if (thumbnailName == null)
        {
            throw new ThumbnailException("When adding a thumbnail details object make sure the name is set.");
        }
        
        this.thumbnailDefinitions.put(thumbnailName, thumbnailDetails);
    }
    
    /**
     * Get the definition of a named thumbnail
     * 
     * @param  thumbnailNam         the thumbnail name
     * @return ThumbnailDetails     the details of the thumbnail
     */
    public ThumbnailDefinition getThumbnailDefinition(String thumbnailName)
    {
        return this.thumbnailDefinitions.get(thumbnailName);
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        lifecycle.setApplicationContext(applicationContext);
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    public void onApplicationEvent(ApplicationContextEvent event)
    {
        lifecycle.onApplicationEvent(event);
    }
    
    /**
     * This class hooks in to the spring application lifecycle and ensures that any
     * ThumbnailDefinitions injected by spring are converted to RenditionDefinitions
     * and saved.
     * 
     * beCPG : patch for multi-tenant
     */
    private class RegistryLifecycle extends AbstractLifecycleBean
    {
        /* (non-Javadoc)
         * @see org.alfresco.util.AbstractLifecycleBean#onBootstrap(org.springframework.context.ApplicationEvent)
         */
        @Override
        protected void onBootstrap(ApplicationEvent event)
        {
            long start = System.currentTimeMillis();
            
            // If the database is in read-only mode, then do not persist the thumbnail definitions.
            if (transactionService.isReadOnly())
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("TransactionService is in read-only mode. Therefore no thumbnail definitions have been initialised.");
                }
                return;
            }
            
            AuthenticationUtil.runAs(new RunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    initThumbnailDefinitions();
                    return null;
                }
            }, AuthenticationUtil.getSystemUserName());
            
            if (tenantAdminService.isEnabled())
            {
                List<Tenant> tenants = tenantAdminService.getAllTenants();
                for (Tenant tenant : tenants)
                {
                    AuthenticationUtil.runAs(new RunAsWork<Object>()
                    {
                        public Object doWork() throws Exception
                        {
                            initThumbnailDefinitions();
                            return null;
                        }
                    }, tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenant.getTenantDomain()));
                }
            }
            
            if (logger.isInfoEnabled())
            {
                logger.info("Init'ed thumbnail defs in "+(System.currentTimeMillis()-start)+" ms");
            }
        }
    
        /* (non-Javadoc)
         * @see org.alfresco.util.AbstractLifecycleBean#onShutdown(org.springframework.context.ApplicationEvent)
         */
        @Override
        protected void onShutdown(ApplicationEvent event)
        {
            // Intentionally empty
        }
    }
}
