package fr.becpg.repo.download;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.download.DownloadCancelledException;
import org.alfresco.repo.download.DownloadStatusUpdateService;
import org.alfresco.repo.download.DownloadStorage;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.repo.version.common.VersionUtil;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.download.DownloadStatus;
import org.alfresco.service.cmr.download.DownloadStatus.Status;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.view.ExporterContext;
import org.alfresco.service.cmr.view.ExporterException;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream.UnicodeExtraFieldPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.becpg.model.ReportModel;
import fr.becpg.repo.entity.EntityService;


/**
 * Handler for exporting node content to a ZIP file
 *
 * @author Alex Miller
 * @version $Id: $Id
 */
public class BeCPGZipDownloadExporter extends BaseExporter
{
    private static Logger log = LoggerFactory.getLogger(BeCPGZipDownloadExporter.class);
    
    private static final String PATH_SEPARATOR = "/";

    protected ZipArchiveOutputStream zipStream;

    private NodeRef downloadNodeRef;
    private int sequenceNumber = 1;
    private long total;
    private long done;
    private long totalFileCount;
    private long filesAddedCount;
    
    private RetryingTransactionHelper transactionHelper;
    private DownloadStorage downloadStorage;
    private DictionaryService dictionaryService;
    private DownloadStatusUpdateService updateService;
    private EntityService entityService;
    private PermissionService permissionService;

    private Deque<Pair<String, NodeRef>> path = new LinkedList<>();
    private String currentName;

    private OutputStream outputStream;
    
    private Set<NodeRef> skippedNodeRefs = new HashSet<>();

    /**
     * Construct
     *
     * @param zipFile File
     * @param checkOutCheckInService CheckOutCheckInService
     * @param nodeService NodeService
     * @param transactionHelper RetryingTransactionHelper
     * @param updateService DownloadStatusUpdateService
     * @param downloadStorage DownloadStorage
     * @param dictionaryService DictionaryService
     * @param downloadNodeRef NodeRef
     * @param total long
     * @param totalFileCount long
     * @param permissionService a {@link org.alfresco.service.cmr.security.PermissionService} object
     * @param entityService a {@link fr.becpg.repo.entity.EntityService} object
     */
    public BeCPGZipDownloadExporter(PermissionService permissionService, EntityService entityService, File zipFile, CheckOutCheckInService checkOutCheckInService, NodeService nodeService, RetryingTransactionHelper transactionHelper, DownloadStatusUpdateService updateService, DownloadStorage downloadStorage, DictionaryService dictionaryService, NodeRef downloadNodeRef, long total, long totalFileCount)
    {
        super(checkOutCheckInService, nodeService);
        this.entityService = entityService;
        this.permissionService = permissionService;
        try
        {
            this.outputStream = new FileOutputStream(zipFile);
            this.updateService = updateService;
            this.transactionHelper = transactionHelper;
            this.downloadStorage = downloadStorage;
            this.dictionaryService = dictionaryService;
            
            this.downloadNodeRef = downloadNodeRef;
            this.total = total;
            this.totalFileCount = totalFileCount;
        }
        catch (FileNotFoundException e)
        {
            throw new ExporterException("Failed to create zip file", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void start(final ExporterContext context)
    {
        zipStream = new ZipArchiveOutputStream(outputStream);
        // NOTE: This encoding allows us to workaround bug...
        //       http://bugs.sun.com/bugdatabase/view_bug.do;:WuuT?bug_id=4820807
        zipStream.setEncoding("UTF-8");
        zipStream.setCreateUnicodeExtraFields(UnicodeExtraFieldPolicy.ALWAYS);
        zipStream.setUseLanguageEncodingFlag(true);
        zipStream.setFallbackToUTF8(true);
    }

    /** {@inheritDoc} */
    @Override
    public void startNode(NodeRef nodeRef)
    {
    	// beCPG: check if version node has READ permissions
    	if (isVersionNodeRef(nodeRef) && !hasReadPermissionOnReference(nodeRef)) {
			skippedNodeRefs.add(nodeRef);
			return;
    	}
    	
        this.currentName = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
        path.push(new Pair<>(currentName, nodeRef));
        if (dictionaryService.isSubClass(nodeService.getType(nodeRef), ContentModel.TYPE_FOLDER))
        {
            String path = getPath() + PATH_SEPARATOR;
            ZipArchiveEntry archiveEntry = new ZipArchiveEntry(path);
            try
            {
                zipStream.putArchiveEntry(archiveEntry);
                zipStream.closeArchiveEntry();
            }
            catch (IOException e)
            {
                throw new ExporterException("Unexpected IOException adding folder entry", e);
            }
        }
    }
    
    private boolean hasReadPermissionOnReference(NodeRef nodeRef) {
    	NodeRef entityVersionNodeRef = entityService.getEntityNodeRef(nodeRef, nodeService.getType(nodeRef));
		if (entityVersionNodeRef != null) {
			NodeRef sourceEntityNodeRef = convertVersionNodeRefToVersionedNodeRef(VersionUtil.convertNodeRef(entityVersionNodeRef));
			if (sourceEntityNodeRef != null) {
				
				NodeRef matchingNodeRef = null;
				
				if (ReportModel.TYPE_REPORT.equals(nodeService.getType(nodeRef))) {
					List<AssociationRef> assocs = nodeService.getTargetAssocs(nodeRef, ReportModel.ASSOC_REPORT_TPL);
					if (!assocs.isEmpty()) {
						matchingNodeRef = assocs.get(0).getTargetRef();
					}
				} else {
					matchingNodeRef = findMatchingSubPath(sourceEntityNodeRef, nodeRef, entityVersionNodeRef);
				}
				
				if (matchingNodeRef != null) {
					AccessStatus readPermission = permissionService.hasPermission(matchingNodeRef, PermissionService.READ_PERMISSIONS);
		            if (!readPermission.equals(AccessStatus.ALLOWED)) {
		            	readPermission = permissionService.hasPermission(matchingNodeRef, PermissionService.CONSUMER);
		            	if (!readPermission.equals(AccessStatus.ALLOWED)) {
		            		return false;
		            	}
		            }
				}
			}
		}
		return true;
    }

	private NodeRef findMatchingSubPath(NodeRef searchParentRef, NodeRef originalRef, NodeRef originalParentRef) {
		
		if (originalRef.equals(originalParentRef)) {
			return searchParentRef;
		}
		
		int originalParentPath = nodeService.getPath(originalParentRef).size();
		Path originalPath = nodeService.getPath(originalRef);
		Path originalSubPath = originalPath.subPath(originalParentPath, originalPath.size() - 1);
		
		NodeRef nextMatchingRef = searchParentRef;
		
		while (originalSubPath.size() > 0) {
			nextMatchingRef = findMatchingChild(nextMatchingRef, originalSubPath);
			if (originalSubPath.size() == 1) {
				break;
			}
			originalSubPath = originalSubPath.subPath(1, originalSubPath.size() - 1);
		}
		
		return nextMatchingRef;
	}

	private NodeRef findMatchingChild(NodeRef parentRef, Path subPathToMatch) {
		
		String firstPartToMatch = subPathToMatch.size() == 1 ? subPathToMatch.toString() : subPathToMatch.subPath(0, 0).toString();
		
		if (nodeService.exists(parentRef)) {
			int parentPath = nodeService.getPath(parentRef).size();
			for (ChildAssociationRef childAssoc : nodeService.getChildAssocs(parentRef)) {
				NodeRef childRef = childAssoc.getChildRef();
				Path currentPath = nodeService.getPath(childRef);
				Path subPath = currentPath.subPath(parentPath, currentPath.size() - 1);
				String firstPart = subPath.size() == 1 ? subPath.toString() : subPath.subPath(0, 0).toString();
				if (firstPart.equals(firstPartToMatch)) {
					return childRef;
				}
			}
		}
		return null;
	}

    private boolean isVersionNodeRef(NodeRef nodeRef)
    {
    	return nodeRef.getStoreRef().getProtocol().equals(VersionModel.STORE_PROTOCOL) || nodeRef.getStoreRef().getIdentifier().equals(Version2Model.STORE_ID);
    }
    
	/**
	 * <p>convertVersionNodeRefToVersionedNodeRef.</p>
	 *
	 * @param versionNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
    @SuppressWarnings("deprecation")
	protected NodeRef convertVersionNodeRefToVersionedNodeRef(NodeRef versionNodeRef)
    {
        Map<QName, Serializable> properties = nodeService.getProperties(versionNodeRef);
        
        NodeRef nodeRef = null;
        
        // Switch VersionStore depending on configured impl
        if (versionNodeRef.getStoreRef().getIdentifier().equals(Version2Model.STORE_ID))
        {
            // V2 version store (eg. workspace://version2Store)
            nodeRef = (NodeRef)properties.get(Version2Model.PROP_QNAME_FROZEN_NODE_REF);
        } 
        else if (versionNodeRef.getStoreRef().getIdentifier().equals(VersionModel.STORE_ID))
        {
            // Deprecated V1 version store (eg. workspace://lightWeightVersionStore)
            nodeRef = new NodeRef((String) properties.get(VersionModel.PROP_QNAME_FROZEN_NODE_STORE_PROTOCOL),
                                  (String) properties.get(VersionModel.PROP_QNAME_FROZEN_NODE_STORE_ID),
                                  (String) properties.get(VersionModel.PROP_QNAME_FROZEN_NODE_ID));
        }
        
        return nodeRef;
    }
    
    /** {@inheritDoc} */
    @Override
    public void contentImpl(NodeRef nodeRef, QName property, InputStream content, ContentData contentData, int index)
    {
    	if (skippedNodeRefs.contains(nodeRef)) {
    		return;
    	}
    	
        // if the content stream to output is empty, then just return content descriptor as is
        if (content == null)
        {
            return;
        }
        
        try
        {
            // ALF-2016
            ZipArchiveEntry zipEntry=new ZipArchiveEntry(getPath());
            zipStream.putArchiveEntry(zipEntry);
            
            // copy export stream to zip
            copyStream(zipStream, content);
            
            zipStream.closeArchiveEntry();
            filesAddedCount = filesAddedCount + 1;
        }
        catch (IOException e)
        {
            throw new ExporterException("Failed to zip export stream", e);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void endNode(NodeRef nodeRef)
    {
    	if (skippedNodeRefs.contains(nodeRef)) {
    		return;
    	}
        path.pop();
    }

    /** {@inheritDoc} */
    @Override
    public void end()
    {
        try
        {
            zipStream.close();
        }
        catch (IOException error)
        {
            throw new ExporterException("Unexpected error closing zip stream!", error);
        }
    }

    private String getPath()
    {
        if (path.size() < 1) 
        {
            throw new IllegalStateException("No elements in path!");    
        }
        
        Iterator<Pair<String, NodeRef>> iter = path.descendingIterator();
        StringBuilder pathBuilder = new StringBuilder();
        
        while (iter.hasNext())
        {
            Pair<String, NodeRef> element = iter.next();
            
            pathBuilder.append(element.getFirst());
            if (iter.hasNext())
            {
                pathBuilder.append(PATH_SEPARATOR);
            }
        }
        
        return pathBuilder.toString();
    }

    /**
     * Copy input stream to output stream
     * 
     * @param output  output stream
     * @param in  input stream
     * @throws IOException
     */
    private void copyStream(OutputStream output, InputStream in)
        throws IOException
    {
        byte[] buffer = new byte[2048 * 10];
        int read = in.read(buffer, 0, 2048 *10);
        int i = 0;
        while (read != -1)
        {
            output.write(buffer, 0, read);
            done = done + read;
            
            // ALF-16289 - only update the status every 10MB
            if (i++%500 == 0)
            {
                updateStatus();
                checkCancelled();
            }
            
            read = in.read(buffer, 0, 2048 *10);
        }
    }
    
    private void checkCancelled()
    {
        boolean downloadCancelled = transactionHelper.doInTransaction(new RetryingTransactionCallback<Boolean>()
        {
            @Override
            public Boolean execute() throws Throwable
            {
                return downloadStorage.isCancelled(downloadNodeRef);                
            }
        }, true, true);

        if ( downloadCancelled == true)
        {
            log.debug("Download cancelled");
            throw new DownloadCancelledException();
        }
    }

    private void updateStatus()
    {
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                DownloadStatus status = new DownloadStatus(Status.IN_PROGRESS, done, total, filesAddedCount, totalFileCount);
                
                updateService.update(downloadNodeRef, status, getNextSequenceNumber());
                return null;
            }
        }, false, true);
    }

    /**
     * <p>getNextSequenceNumber.</p>
     *
     * @return a int
     */
    public int getNextSequenceNumber()
    {
        return sequenceNumber++;
    }

    /**
     * <p>Getter for the field <code>done</code>.</p>
     *
     * @return a long
     */
    public long getDone()
    {
        return done;
    }

    /**
     * <p>Getter for the field <code>total</code>.</p>
     *
     * @return a long
     */
    public long getTotal()
    {
        return total;
    }

    /**
     * <p>getFilesAdded.</p>
     *
     * @return a long
     */
    public long getFilesAdded()
    {
        return filesAddedCount;
    }

    /**
     * <p>getTotalFiles.</p>
     *
     * @return a long
     */
    public long getTotalFiles()
    {
        return totalFileCount;
    }
}
