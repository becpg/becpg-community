/*
 * #%L
 * Alfresco Repository
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
package fr.becpg.repo.download;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.download.DownloadStatusUpdateService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.download.DownloadRequest;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.view.ExporterCrawlerParameters;
import org.alfresco.service.cmr.view.ExporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityService;

/**
 * {@link org.alfresco.repo.action.executer.ActionExecuterAbstractBase} for creating an archive (ie. zip) file containing
 * content from the repository.
 *
 * The maximum total size of the content which can be downloaded is controlled
 * by the maximumContentSie property. -1 indicates no limit.
 *
 * @author Alex Miller
 * @version $Id: $Id
 */
public class BeCPGCreateDownloadArchiveAction extends AbstractDownloadArchiveAction
{
    /** Constant <code>log</code> */
    private static final Logger log = LoggerFactory.getLogger(BeCPGCreateDownloadArchiveAction.class);
    
    
    /** Constant <code>CREATION_ERROR="Unexpected error creating archive file "{trunked}</code> */
    private static final String CREATION_ERROR = "Unexpected error creating archive file for download";
    /** Constant <code>TEMP_FILE_PREFIX="download"</code> */
    private static final String TEMP_FILE_PREFIX = "download";
    /** Constant <code>TEMP_FILE_SUFFIX=".zip"</code> */
    private static final String TEMP_FILE_SUFFIX = ".zip"; 
    
    // Dependencies
    private CheckOutCheckInService checkOutCheckInService;
    private DictionaryService dictionaryService;
    private EntityService entityService;
    private PermissionService permissionService;

    private long maximumContentSize = -1l;
    
    private static class SizeEstimator extends BaseExporter 
    {
        /**
         * @param checkOutCheckInService CheckOutCheckInService
         * @param nodeService NodeService
         */
        SizeEstimator(CheckOutCheckInService checkOutCheckInService, NodeService nodeService)
        {
            super(checkOutCheckInService, nodeService);
        }

        private long size = 0;
        private long fileCount = 0;

        @Override
        protected void contentImpl(NodeRef nodeRef, QName property, InputStream content, ContentData contentData, int index)
        {
            size = size + contentData.getSize();
            fileCount = fileCount + 1;
        }

        public long getSize()
        {
            return size;
        }

        public long getFileCount()
        {
            return fileCount;
        }

    }
    
    // Dependency setters
    /**
     * <p>setCheckOutCheckInSerivce.</p>
     *
     * @param checkOutCheckInService a {@link org.alfresco.service.cmr.coci.CheckOutCheckInService} object
     */
    public void setCheckOutCheckInSerivce(CheckOutCheckInService checkOutCheckInService)
    {
        this.checkOutCheckInService = checkOutCheckInService;
    }
    
    /**
     * <p>Setter for the field <code>permissionService</code>.</p>
     *
     * @param permissionService a {@link org.alfresco.service.cmr.security.PermissionService} object
     */
    public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}
    
    /**
     * <p>Setter for the field <code>entityService</code>.</p>
     *
     * @param entityService a {@link fr.becpg.repo.entity.EntityService} object
     */
    public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}
    
    
    
    
    /**
     * Set the maximum total size of content that can be added to a single
     * download. -1 indicates no limit.
     *
     * @param maximumContentSize a long
     */
    public void setMaximumContentSize(long maximumContentSize)
    {
        this.maximumContentSize = maximumContentSize;
    }
    
    
    /**
     * <p>Setter for the field <code>transactionHelper</code>.</p>
     *
     * @param transactionHelper a {@link org.alfresco.repo.transaction.RetryingTransactionHelper} object
     */
    public void setTransactionHelper(RetryingTransactionHelper transactionHelper)
    {
        this.transactionHelper = transactionHelper;
    }
    
    /**
     * <p>Setter for the field <code>updateService</code>.</p>
     *
     * @param updateService a {@link org.alfresco.repo.download.DownloadStatusUpdateService} object
     */
    public void setUpdateService(DownloadStatusUpdateService updateService)
    {
        this.updateService = updateService;
    }
    
    

    /** {@inheritDoc} */
    @Override
    public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

    /**
     * {@inheritDoc}
     *
     * Create an archive file containing content from the repository.
     *
     * Uses the {@link ExporterService} with custom exporters to create the
     * archive files.
     */
    @Override
    protected void executeImpl(Action action, final NodeRef actionedUponNodeRef)
    {
        // Get the download request data and set up the exporter crawler parameters.
        final DownloadRequest downloadRequest = downloadStorage.getDownloadRequest(actionedUponNodeRef);

        AuthenticationUtil.runAs(() -> {

            NodeRef[] nodeRefs = downloadRequest.getRequetedNodeRefs();
            if (completeIfEmpty(actionedUponNodeRef, nodeRefs)) {
                return null;
            }

            ExporterCrawlerParameters crawlerParameters = createArchiveCrawlerParameters(nodeRefs);

            // Get an estimate of the size for statuses
            SizeEstimator estimator = new SizeEstimator(checkOutCheckInService, nodeService);
            exporterService.exportView(estimator, crawlerParameters, null);

            if ((maximumContentSize > 0) && (estimator.getSize() > maximumContentSize)) {
                maximumContentSizeExceeded(actionedUponNodeRef, maximumContentSize, estimator.getSize(), estimator.getFileCount());
                return null;
            }

            createDownload(actionedUponNodeRef, crawlerParameters, estimator);

            return null;
        }, downloadRequest.getOwner());

    }

    /**
     * Build the crawler parameters of a repository archive: the nodes are crawled with their content
     * but without their renditions, discussions, versioned associations nor entity lists.
     *
     * @param nodeRefs an array of {@link org.alfresco.service.cmr.repository.NodeRef} objects
     * @return a {@link org.alfresco.service.cmr.view.ExporterCrawlerParameters} object
     */
    private ExporterCrawlerParameters createArchiveCrawlerParameters(NodeRef[] nodeRefs)
    {
        ExporterCrawlerParameters crawlerParameters = new ExporterCrawlerParameters();

        crawlerParameters.setExportFrom(new Location(nodeRefs));
        crawlerParameters.setCrawlSelf(true);
        crawlerParameters.setExcludeChildAssocs(new QName[] { RenditionModel.ASSOC_RENDITION, ForumModel.ASSOC_DISCUSSION,
                Version2Model.CHILD_QNAME_VERSIONED_ASSOCS, BeCPGModel.ASSOC_ENTITYLISTS });
        crawlerParameters.setExcludeAspects(new QName[] { ContentModel.ASPECT_WORKING_COPY, BeCPGModel.ASPECT_ENTITY_FORMAT });

        return crawlerParameters;
    }

    /** {@inheritDoc} */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
    	//empty
    }

    private void createDownload(final NodeRef actionedUponNodeRef, ExporterCrawlerParameters crawlerParameters, SizeEstimator estimator)
    {
        // perform the actual export
        final File tempFile = TempFileProvider.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
        final BeCPGZipDownloadExporter handler = new BeCPGZipDownloadExporter(permissionService, entityService, tempFile, checkOutCheckInService,
                nodeService, transactionHelper, updateService, downloadStorage, dictionaryService, actionedUponNodeRef, estimator.getSize(),
                estimator.getFileCount());

        runExport(actionedUponNodeRef, crawlerParameters, handler, tempFile);
    }

}
