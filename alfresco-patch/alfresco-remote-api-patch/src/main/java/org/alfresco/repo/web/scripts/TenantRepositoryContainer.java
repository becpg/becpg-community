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
package org.alfresco.repo.web.scripts;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Registry;

import org.alfresco.repo.cache.AsynchronouslyRefreshedCache;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantDeployer;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;

/**
 * Tenant-aware Repository (server-tier) container for Web Scripts
 * 
 * @author davidc
 */
public class TenantRepositoryContainer extends RepositoryContainer implements TenantDeployer
{
    // Logger
    protected static final Log logger = LogFactory.getLog(TenantRepositoryContainer.class);

    /* Component Dependencies */
    protected TenantAdminService tenantAdminService;
    protected TransactionService transactionService;
    private AsynchronouslyRefreshedCache<Registry> registryCache;

    // beCPG: tenants whose registry has already been built by this container
    private final Set<String> builtTenants = ConcurrentHashMap.newKeySet();

    /**
     * @param registryCache
     *            asynchronously maintained cache for script registries
     */
    public void setWebScriptsRegistryCache(AsynchronouslyRefreshedCache<Registry> registryCache)
    {
        this.registryCache = registryCache;
    }

    /**
     * @param tenantAdminService
     *            service to sort out tenant context
     */
    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }

    /**
     * @param transactionService
     *            service to give transactions when reading from the container
     */
    public void setTransactionService(TransactionService transactionService)
    {
        super.setTransactionService(transactionService);
        this.transactionService = transactionService;
    }

    @Override
    public Registry getRegistry()
    {
        Registry registry = registryCache.get();
        boolean isUpToDate = registryCache.isUpToDate();
        if (!isUpToDate && logger.isDebugEnabled())
        {
            logger.debug("Retrieved out of date web script registry for tenant " + tenantAdminService.getCurrentUserDomain());
        }
        return registry;
    }

    @Override
    public void onEnableTenant()
    {
        init();
    }

    @Override
    public void onDisableTenant()
    {
        destroy();
    }

    @Override
    public void init()
    {
        tenantAdminService.register(this);

        // beCPG: the first initialisation of a tenant builds its registry once. Refreshing the cache, which
        // rebuilds it again after commit, then resetting the registry it just built, cost two extra builds of
        // every web script at each startup and for each tenant.
        if (builtTenants.add(tenantAdminService.getCurrentUserDomain()))
        {
            buildRegistryOnce();
            return;
        }

        registryCache.refresh();

        super.reset();
    }

    /**
     * beCPG: resets the container the way {@code AbstractRuntimeContainer.reset()} does, except that the
     * registry is built once by the cache instead of being rebuilt in place.
     */
    private void buildRegistryOnce()
    {
        long startTime = System.nanoTime();
        transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
            getScriptProcessorRegistry().reset();
            getTemplateProcessorRegistry().reset();
            getRegistry();
            getConfigService().reset();
            return null;
        }, true, false);
        if (logger.isInfoEnabled())
        {
            logger.info("Initialised " + getName() + " Web Script Container once (in " + (System.nanoTime() - startTime) / 1000000f + "ms)");
        }
    }

    @Override
    public void destroy()
    {
        registryCache.refresh();
    }

    @Override
    public void reset()
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>() {
            public Object execute() throws Exception
            {
                // beCPG: nothing to refresh before the first build of the tenant registry
                if (builtTenants.contains(tenantAdminService.getCurrentUserDomain()))
                {
                    destroy();
                }
                init();

                return null;
            }
        }, true, false);
    }
}
