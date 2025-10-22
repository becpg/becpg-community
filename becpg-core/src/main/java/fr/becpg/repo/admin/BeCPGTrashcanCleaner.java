package fr.becpg.repo.admin;

/*
 * #%L
 * Alfresco Trash Can Cleaner
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

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.trashcan.TrashcanCleaner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * This class is capable of cleaning the trashcan without depending on searches
 * over the archive store. So the corresponding index core could be deactivated
 * with no impact on its execution. It will {@link #clean} the trashcan according
 * to defined {@link #deleteBatchCount} and {@link #keepPeriod} properties.
 * <p>
 * {@link #deleteBatchCount}: It will set how many nodes in trashcan to delete at
 * maximum during {@link #clean} execution. By default the value is 1000.
 * <p>
 * {@link #keepPeriod}: The time period (in {@link java.time.Duration} format) for which documents in
 * trashcan are kept. Any nodes archived less than the value specified won't be deleted during {@link #clean} execution.
 *
 * @author Rui Fernandes
 * @author sglover
 */
public class BeCPGTrashcanCleaner extends TrashcanCleaner
{
    private static final Log logger = LogFactory.getLog(BeCPGTrashcanCleaner.class);

    private final NodeService nodeService;
    private final TransactionService transactionService;

    private final String archiveStoreUrl = "archive://SpacesStore";
    private final int deleteBatchCount;
    private final Duration keepPeriod;
    private List<NodeRef> trashcanNodes;
    private NodeArchiveService nodeArchiveService;

    /**
     *
     * @param nodeService
     * @param transactionService
     * @param deleteBatchCount
     * @param keepPeriod
     */
    public BeCPGTrashcanCleaner(NodeService nodeService, TransactionService transactionService,
            int deleteBatchCount, String keepPeriod, NodeArchiveService nodeArchiveService)
    {
    	super(nodeService, transactionService, deleteBatchCount, keepPeriod);
        this.nodeService = nodeService;
        this.transactionService = transactionService;
        this.deleteBatchCount = deleteBatchCount;
        this.keepPeriod = Duration.parse(keepPeriod);
        this.nodeArchiveService = nodeArchiveService;
    }

    /**
     *
     * It deletes the {@link java.util.List List} of
     * {@link org.alfresco.service.cmr.repository.NodeRef NodeRef} received as
     * argument.
     *
     * @param nodes
     *
     * return The number of deleted nodes
     */
    private int deleteNodes(List<NodeRef> nodes)
    {
        AtomicInteger deletedNodes = new AtomicInteger();
        for (NodeRef nodeRef : nodes)
        {
            // create a new transaction for each deletion so the transactions are smaller and the progress of the
            // cleaner is not lost in case of any problems encountered during the job execution
            AuthenticationUtil.runAsSystem(() ->
            {
                RetryingTransactionCallback<Void> txnWork = () ->
                {
                	nodeArchiveService.purgeArchivedNode(nodeRef);
                    deletedNodes.getAndIncrement();
                    return null;
                };
                return transactionService.getRetryingTransactionHelper().doInTransaction(txnWork, false, true);
            });
        }
        return deletedNodes.get();
    }

    /**
     *
     * It returns the {@link java.util.List List} of
     * {@link org.alfresco.service.cmr.repository.NodeRef NodeRef} of the
     * archive store set to be deleted according to configuration for
     * <b>deleteBatchCount</b> and <b>keepPeriod</b>.
     *
     * @return
     */
    private List<NodeRef> getBatchToDelete()
    {
        return getTrashcanChildAssocs()
                .stream()
                .filter(el -> olderThanDaysToKeep(el.getChildRef()))
                .map(ChildAssociationRef::getChildRef)
                .collect(Collectors.toList());
    }

    /**
     *
     * It will return the first {@link #deleteBatchCount}
     * {@link org.alfresco.service.cmr.repository.ChildAssociationRef}s
     * of type {@link ContentModel}.ASSOC_CHILDREN
     * from the archive store set.
     *
     * @return
     */
    private List<ChildAssociationRef> getTrashcanChildAssocs()
    {
        StoreRef archiveStore = new StoreRef(archiveStoreUrl);
        NodeRef archiveRoot = nodeService.getRootNode(archiveStore);

        // ContentModel.ASSOC_CHILDREN type is an ordered association and the newly deleted nodes are added at the end
        // of the trashcan nodes list.
        // Since this method returns the nodes in the correct order, we will always get the deleteBatchCount nodes
        // in the trashcan that are in there for the longest time.
        return nodeService.getChildAssocs(
                archiveRoot, ContentModel.ASSOC_CHILDREN, RegexQNamePattern.MATCH_ALL, deleteBatchCount, false);
    }

    /**
     *
     * It checks if the archived node has been archived since longer than
     * <b>keepPeriod</b>. If <b>keepPeriod</b> is 0 or negative it will return
     * always true.
     *
     * @param node
     * @return
     */
    private boolean olderThanDaysToKeep(NodeRef node)
    {
        Date archivedDate = (Date) nodeService.getProperty(node, ContentModel.PROP_ARCHIVED_DATE);
        long archivedDateValue = 0;
        if (archivedDate != null)
        {
            archivedDateValue = archivedDate.getTime();
        }

        ZonedDateTime before = Instant.ofEpochMilli(System.currentTimeMillis()).minus(keepPeriod).atZone(ZoneId.of("UTC"));
        return Instant.ofEpochMilli(archivedDateValue).atZone(ZoneId.of("UTC")).isBefore(before);
    }

    /**
     *
     * It returns the number of nodes present on trashcan.
     *
     * @return
     */
    public long getNumberOfNodesInTrashcan()
    {
        StoreRef storeRef = new StoreRef(archiveStoreUrl);
        NodeRef archiveRoot = nodeService.getRootNode(storeRef);

        return nodeService.getChildAssocs(
                archiveRoot, ContentModel.ASSOC_CHILDREN, RegexQNamePattern.MATCH_ALL).size();
    }

    /**
     *
     * The method that will clean the specified <b>archiveStoreUrl</b> to the
     * limits defined by the values set for <b>deleteBatchCount</b> and
     * <b>keepPeriod</b>.
     *
     */
    public void clean()
    {
        logger.info("Running TrashcanCleaner");

        // Retrieve in a new read-only transaction the list of nodes to be deleted by the Trashcan Cleaner
        AuthenticationUtil.runAsSystem(() ->
        {
            RetryingTransactionCallback<Void> txnWork = () ->
            {
                trashcanNodes = getBatchToDelete();

                logger.info(String.format("Number of nodes to delete: %s", trashcanNodes.size()));

                return null;
            };
            return transactionService.getRetryingTransactionHelper().doInTransaction(txnWork, true, true);
        });

        int deletedNodes = deleteNodes(trashcanNodes);

        logger.info(String.format("Number of deleted nodes: %s", deletedNodes));
        logger.info("TrashcanCleaner finished");
    }
}
