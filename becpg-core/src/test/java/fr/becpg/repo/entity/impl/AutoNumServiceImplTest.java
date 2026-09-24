package fr.becpg.repo.entity.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicBoolean;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * Checks that a write on a committed counter runs in its own transaction, so that a caller
 * transaction never keeps the counter row locked while it waits for the counter lock, and that
 * the other writes stay in the caller transaction, which the inner one would wait for (#36870).
 *
 * @author matthieu
 */
public class AutoNumServiceImplTest {

    private static final String PREFIX = "P";

    private static final QName CLASS_NAME = QName.createQName(BeCPGModel.BECPG_URI, "product");

    private static final NodeRef COUNTER_NODE_REF = new NodeRef("workspace://SpacesStore/counter");

    private static final NodeRef ENTITY_NODE_REF = new NodeRef("workspace://SpacesStore/entity");

    private static final NodeRef FOLDER_NODE_REF = new NodeRef("workspace://SpacesStore/folder");

    private static final Long COUNTER_NODE_ID = 12L;

    @Mock
    private NodeService nodeService;

    @Mock
    private BeCPGCacheService beCPGCacheService;

    @Mock
    private BehaviourFilter policyBehaviourFilter;

    @Mock
    private TransactionService transactionService;

    @Mock
    private RetryingTransactionHelper transactionHelper;

    @Mock
    private NodeDAO nodeDAO;

    @Mock
    private RepoService repoService;

    @Mock
    private Repository repositoryHelper;

    @InjectMocks
    private AutoNumServiceImpl autoNumService;

    private final AtomicBoolean inCounterTransaction = new AtomicBoolean();

    private final AtomicBoolean counterWrittenInItsTransaction = new AtomicBoolean();

    private AutoCloseable mocks;

    @Before
    public void setUp() throws Throwable {
        mocks = MockitoAnnotations.openMocks(this);
        when(transactionService.getRetryingTransactionHelper()).thenReturn(transactionHelper);
        when(transactionHelper.doInTransaction(any(), eq(false), eq(true))).thenAnswer(this::runInCounterTransaction);
        when(beCPGCacheService.getFromCache(eq(AutoNumServiceImpl.class.getName()), anyString(), any())).thenReturn(COUNTER_NODE_REF);
        when(nodeService.exists(COUNTER_NODE_REF)).thenReturn(true);
        when(nodeService.getProperty(COUNTER_NODE_REF, BeCPGModel.PROP_AUTO_NUM_VALUE)).thenReturn(5L);
        when(nodeService.getProperty(COUNTER_NODE_REF, BeCPGModel.PROP_AUTO_NUM_PREFIX)).thenReturn(PREFIX);
        when(nodeDAO.getNodeRefStatus(COUNTER_NODE_REF)).thenReturn(new NodeRef.Status(COUNTER_NODE_ID, COUNTER_NODE_REF, null, 1L, false));
        recordWhereCounterIsWritten();
    }

    @After
    public void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    public void testGetAutoNumValueIncrementsCounterInItsOwnTransaction() {
        assertEquals(PREFIX + "6", autoNumService.getAutoNumValue(CLASS_NAME, BeCPGModel.PROP_CODE));

        verify(nodeService).setProperty(COUNTER_NODE_REF, BeCPGModel.PROP_AUTO_NUM_VALUE, 6L);
        assertTrue(counterWrittenInItsTransaction.get());
    }

    @Test
    public void testCounterWriteDisablesAuditingInItsTransaction() {
        autoNumService.getAutoNumValue(CLASS_NAME, BeCPGModel.PROP_CODE);

        verify(policyBehaviourFilter).disableBehaviour(ContentModel.ASPECT_AUDITABLE);
    }

    @Test
    public void testGetAutoNumValueKeepsCounterWrittenByCallerInCallerTransaction() {
        when(nodeDAO.isInCurrentTxn(COUNTER_NODE_ID)).thenReturn(true);

        assertEquals(PREFIX + "6", autoNumService.getAutoNumValue(CLASS_NAME, BeCPGModel.PROP_CODE));

        verify(nodeService).setProperty(COUNTER_NODE_REF, BeCPGModel.PROP_AUTO_NUM_VALUE, 6L);
        assertFalse(counterWrittenInItsTransaction.get());
    }

    @Test
    public void testGetAutoNumValueCreatesMissingCounterInCallerTransaction() {
        when(nodeService.exists(COUNTER_NODE_REF)).thenReturn(false);
        when(repoService.getOrCreateFolderByPath(any(), anyString(), any())).thenReturn(FOLDER_NODE_REF);
        when(nodeService.createNode(eq(FOLDER_NODE_REF), any(), any(), eq(BeCPGModel.TYPE_AUTO_NUM), any()))
                .thenAnswer(invocation -> recordCounterWrite(new ChildAssociationRef(null, FOLDER_NODE_REF, null, COUNTER_NODE_REF)));

        assertEquals("1", autoNumService.getAutoNumValue(CLASS_NAME, BeCPGModel.PROP_CODE));

        verify(nodeService).createNode(eq(FOLDER_NODE_REF), any(), any(), eq(BeCPGModel.TYPE_AUTO_NUM), any());
        assertFalse(counterWrittenInItsTransaction.get());
    }

    @Test
    public void testGetOrCreateCodeSetsEntityCodeInCallerTransaction() {
        when(nodeService.getType(ENTITY_NODE_REF)).thenReturn(CLASS_NAME);

        assertEquals(PREFIX + "6", autoNumService.getOrCreateBeCPGCode(ENTITY_NODE_REF));

        verify(nodeService).setProperty(ENTITY_NODE_REF, BeCPGModel.PROP_CODE, PREFIX + "6");
        assertTrue(counterWrittenInItsTransaction.get());
    }

    @Test
    public void testGetOrCreateCodeRaisesCounterFromExistingCodeInItsOwnTransaction() {
        when(nodeService.getType(ENTITY_NODE_REF)).thenReturn(CLASS_NAME);
        when(nodeService.getProperty(ENTITY_NODE_REF, BeCPGModel.PROP_CODE)).thenReturn(PREFIX + "12");

        try (MockedStatic<BeCPGQueryBuilder> queryBuilder = mockStatic(BeCPGQueryBuilder.class)) {
            queryBuilder.when(BeCPGQueryBuilder::createQuery).thenReturn(mock(BeCPGQueryBuilder.class, RETURNS_SELF));

            assertEquals(PREFIX + "12", autoNumService.getOrCreateBeCPGCode(ENTITY_NODE_REF));
        }

        verify(nodeService).setProperty(COUNTER_NODE_REF, BeCPGModel.PROP_AUTO_NUM_VALUE, 12L);
        assertTrue(counterWrittenInItsTransaction.get());
    }

    @Test
    public void testSetAutoNumValueWritesCounterInItsOwnTransaction() {
        assertTrue(autoNumService.setAutoNumValue(CLASS_NAME, BeCPGModel.PROP_CODE, 42L));

        verify(nodeService).setProperty(COUNTER_NODE_REF, BeCPGModel.PROP_AUTO_NUM_VALUE, 42L);
        assertTrue(counterWrittenInItsTransaction.get());
    }

    @Test
    public void testSetAutoNumValueReportsMissingCounter() {
        when(nodeService.exists(COUNTER_NODE_REF)).thenReturn(false);

        assertFalse(autoNumService.setAutoNumValue(CLASS_NAME, BeCPGModel.PROP_CODE, 42L));
    }

    @Test
    public void testDeleteAutoNumValueDeletesCounterInItsOwnTransaction() {
        autoNumService.deleteAutoNumValue(CLASS_NAME, BeCPGModel.PROP_CODE);

        verify(nodeService).deleteNode(COUNTER_NODE_REF);
        assertTrue(counterWrittenInItsTransaction.get());
    }

    private Object runInCounterTransaction(InvocationOnMock invocation) throws Throwable {
        RetryingTransactionCallback<?> callback = invocation.getArgument(0);
        inCounterTransaction.set(true);
        try {
            return callback.execute();
        } finally {
            inCounterTransaction.set(false);
        }
    }

    private void recordWhereCounterIsWritten() {
        doAnswer(invocation -> checkWrittenNode(invocation.getArgument(0))).when(nodeService)
                .setProperty(any(NodeRef.class), any(QName.class), any());
        doAnswer(invocation -> recordCounterWrite(null)).when(nodeService).deleteNode(COUNTER_NODE_REF);
    }

    private Object checkWrittenNode(NodeRef nodeRef) {
        if (COUNTER_NODE_REF.equals(nodeRef)) {
            return recordCounterWrite(null);
        }
        assertFalse("Entity written in the counter transaction", inCounterTransaction.get());
        return null;
    }

    private <T> T recordCounterWrite(T result) {
        counterWrittenInItsTransaction.set(inCounterTransaction.get());
        return result;
    }
}
