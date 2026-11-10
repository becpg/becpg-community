package fr.becpg.repo.entity.policy;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.transaction.TransactionListener;
import org.alfresco.util.transaction.TransactionSupportUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.impl.EntityTplServiceImpl;

/**
 * Checks that a template change drops the template cache entry of its type once the transaction
 * has committed, and not before (#14498).
 *
 * @author matthieu
 */
public class EntityTplRefAspectPolicyTest {

    private static final String LATE_LISTENERS_RESOURCE_KEY = "AlfrescoTransactionSupport.preListeners";

    private static final String TPL_CACHE_NAME = EntityTplServiceImpl.class.getName();

    private static final QName TEMPLATE_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "finishedProduct");

    private static final NodeRef TEMPLATE_NODE_REF = new NodeRef("workspace://SpacesStore/template");

    private final BeCPGCacheService beCPGCacheService = mock(BeCPGCacheService.class);

    private final EntityTplRefAspectPolicy policy = new EntityTplRefAspectPolicy();

    @Before
    public void setUp() {
        NodeService nodeService = mock(NodeService.class);
        when(nodeService.exists(TEMPLATE_NODE_REF)).thenReturn(true);
        when(nodeService.getType(TEMPLATE_NODE_REF)).thenReturn(TEMPLATE_TYPE);
        policy.setNodeService(nodeService);
        policy.setBeCPGCacheService(beCPGCacheService);
        TransactionSynchronizationManager.initSynchronization();
    }

    @After
    public void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.clear();
        for (Object key : TransactionSynchronizationManager.getResourceMap().keySet()) {
            TransactionSynchronizationManager.unbindResource(key);
        }
    }

    @Test
    public void templateChangeKeepsCacheEntryUntilCommit() {
        enableTemplate();

        verify(beCPGCacheService, never()).removeFromCache(anyString(), anyString());
    }

    @Test
    public void templateChangeDropsCacheEntryAfterCommit() {
        enableTemplate();

        commit();

        verify(beCPGCacheService).removeFromCache(TPL_CACHE_NAME, TEMPLATE_TYPE.toString());
    }

    private void enableTemplate() {
        Map<QName, Serializable> before = Map.of(BeCPGModel.PROP_ENTITY_TPL_ENABLED, false);
        Map<QName, Serializable> after = Map.of(BeCPGModel.PROP_ENTITY_TPL_ENABLED, true);
        policy.onUpdateProperties(TEMPLATE_NODE_REF, before, after);
    }

    /**
     * Replays what the patched transaction support does on commit: the late listeners run once the
     * synchronization is over.
     */
    private static void commit() {
        Set<TransactionListener> lateListeners = TransactionSupportUtil.getResource(LATE_LISTENERS_RESOURCE_KEY);
        TransactionSynchronizationManager.clearSynchronization();
        for (TransactionListener lateListener : lateListeners) {
            lateListener.afterCommit();
        }
    }

}
