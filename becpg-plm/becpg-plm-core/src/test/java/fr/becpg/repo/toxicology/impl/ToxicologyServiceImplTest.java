package fr.becpg.repo.toxicology.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;

import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.transaction.TransactionListener;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.batch.BatchStep;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * Unit tests for {@link ToxicologyServiceImpl}: updating or deleting a tox must not read the
 * deletion flag of every ingredient in the request, and the batches must leave the deleted
 * ingredients untouched.
 */
public class ToxicologyServiceImplTest {

    private static final String TOX_NAME = "Tox A";

    private static final Double TOX_VALUE = 110d;

    private static final Double POD_SYSTEMIC = 10000d;

    private static final Double MOS_MOE = 100d;

    private static final Double DERMAL_ABSORPTION = 18d;

    private static final String OUTDATED_SYSTEMIC_VALUES = "- Tox A: 1";

    private static final String RECALCULATED_SYSTEMIC_VALUES = "- Tox A: 30303";

    private static final String SYSTEMIC_VALUES_OF_TWO_TOXES = "- Tox A: 10\n- Tox B: 20";

    private static final String SYSTEMIC_VALUES_WITHOUT_REMOVED_TOX = "- Tox B: 20";

    private final NodeRef toxNodeRef = newNodeRef();

    private final NodeRef ingNodeRef = newNodeRef();

    private final NodeRef otherIngNodeRef = newNodeRef();

    @Mock
    private NodeService nodeService;

    @Mock
    private BatchQueueService batchQueueService;

    @InjectMocks
    private ToxicologyServiceImpl toxicologyService;

    private AutoCloseable mocks;

    private MockedStatic<BeCPGQueryBuilder> queryBuilderStatic;

    private MockedStatic<AlfrescoTransactionSupport> transactionSupportStatic;

    @BeforeClass
    public static void initAuthenticationUtil() throws Exception {
        new AuthenticationUtil().afterPropertiesSet();
    }

    @Before
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        BeCPGQueryBuilder queryBuilder = mock(BeCPGQueryBuilder.class, RETURNS_SELF);
        when(queryBuilder.list()).thenReturn(List.of(ingNodeRef, otherIngNodeRef));
        queryBuilderStatic = mockStatic(BeCPGQueryBuilder.class);
        queryBuilderStatic.when(BeCPGQueryBuilder::createQuery).thenReturn(queryBuilder);
        transactionSupportStatic = mockStatic(AlfrescoTransactionSupport.class);
        when(nodeService.exists(any(NodeRef.class))).thenReturn(true);
        givenTox();
    }

    @After
    public void tearDown() throws Exception {
        transactionSupportStatic.close();
        queryBuilderStatic.close();
        mocks.close();
    }

    @Test
    public void shouldNotReadIngredientDeletionFlagWhenToxIsUpdated() {
        toxicologyService.updateIngredientsFromTox(toxNodeRef);
        commitTransaction();

        verify(batchQueueService).queueBatch(any(BatchInfo.class), any());
        verify(nodeService, never()).getProperty(ingNodeRef, BeCPGModel.PROP_IS_DELETED);
        verify(nodeService, never()).getProperty(otherIngNodeRef, BeCPGModel.PROP_IS_DELETED);
    }

    @Test
    public void shouldNotUpdateDeletedIngredientWhenToxIsUpdated() throws Throwable {
        givenIngredientToRecalculate();
        givenIngredientDeleted(Boolean.TRUE);

        updateWorker().process(ingNodeRef);

        verifyIngredientNotWritten();
    }

    @Test
    public void shouldRecalculateUndeletedIngredientWhenToxIsUpdated() throws Throwable {
        givenIngredientToRecalculate();

        updateWorker().process(ingNodeRef);

        assertEquals(RECALCULATED_SYSTEMIC_VALUES, writtenSystemicValues());
    }

    @Test
    public void shouldSkipIngredientWithoutToxValuesWhenToxIsUpdated() throws Throwable {
        updateWorker().process(ingNodeRef);

        verifyIngredientNotWritten();
    }

    @Test
    public void shouldSkipMissingIngredientWhenToxIsUpdated() throws Throwable {
        givenIngredientToRecalculate();
        BatchProcessWorker<NodeRef> worker = updateWorker();
        when(nodeService.exists(ingNodeRef)).thenReturn(false);

        worker.process(ingNodeRef);

        verifyIngredientNotWritten();
    }

    @Test
    public void shouldSkipIngredientsWhenToxNoLongerExists() throws Throwable {
        givenIngredientToRecalculate();
        BatchProcessWorker<NodeRef> worker = updateWorker();
        when(nodeService.exists(toxNodeRef)).thenReturn(false);

        worker.process(ingNodeRef);

        verifyIngredientNotWritten();
    }

    @Test
    public void shouldNotReadIngredientDeletionFlagWhenToxIsDeleted() {
        toxicologyService.removeToxFromIngredients(toxNodeRef);
        when(nodeService.exists(toxNodeRef)).thenReturn(false);
        commitTransaction();

        verify(batchQueueService).queueBatch(any(BatchInfo.class), any());
        verify(nodeService, never()).getProperty(ingNodeRef, BeCPGModel.PROP_IS_DELETED);
        verify(nodeService, never()).getProperty(otherIngNodeRef, BeCPGModel.PROP_IS_DELETED);
    }

    @Test
    public void shouldNotUpdateDeletedIngredientWhenToxIsDeleted() throws Throwable {
        givenSystemicValues(SYSTEMIC_VALUES_OF_TWO_TOXES);
        givenIngredientDeleted(Boolean.TRUE);

        removalWorker().process(ingNodeRef);

        verifyIngredientNotWritten();
    }

    @Test
    public void shouldRemoveToxLineFromUndeletedIngredientWhenToxIsDeleted() throws Throwable {
        givenSystemicValues(SYSTEMIC_VALUES_OF_TWO_TOXES);
        givenIngredientDeleted(Boolean.FALSE);

        removalWorker().process(ingNodeRef);

        assertEquals(SYSTEMIC_VALUES_WITHOUT_REMOVED_TOX, writtenSystemicValues());
    }

    @Test
    public void shouldSkipMissingIngredientWhenToxIsDeleted() throws Throwable {
        givenSystemicValues(SYSTEMIC_VALUES_OF_TWO_TOXES);
        BatchProcessWorker<NodeRef> worker = removalWorker();
        when(nodeService.exists(ingNodeRef)).thenReturn(false);

        worker.process(ingNodeRef);

        verifyIngredientNotWritten();
    }

    @Test
    public void shouldNotQueueRemovalWhenToxStillExistsAfterCommit() {
        toxicologyService.removeToxFromIngredients(toxNodeRef);
        commitTransaction();

        verify(batchQueueService, never()).queueBatch(any(BatchInfo.class), any());
    }

    private BatchProcessWorker<NodeRef> updateWorker() {
        toxicologyService.updateIngredientsFromTox(toxNodeRef);
        commitTransaction();
        return queuedWorker();
    }

    private BatchProcessWorker<NodeRef> removalWorker() {
        toxicologyService.removeToxFromIngredients(toxNodeRef);
        when(nodeService.exists(toxNodeRef)).thenReturn(false);
        commitTransaction();
        return queuedWorker();
    }

    /**
     * Runs the after commit callback of the transaction listener bound by the service.
     */
    private void commitTransaction() {
        ArgumentCaptor<TransactionListener> listenerCaptor = ArgumentCaptor.forClass(TransactionListener.class);
        transactionSupportStatic.verify(() -> AlfrescoTransactionSupport.bindListener(listenerCaptor.capture()));
        listenerCaptor.getValue().afterCommit();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private BatchProcessWorker<NodeRef> queuedWorker() {
        ArgumentCaptor<List> stepsCaptor = ArgumentCaptor.forClass(List.class);
        verify(batchQueueService).queueBatch(any(BatchInfo.class), stepsCaptor.capture());
        BatchStep<NodeRef> step = (BatchStep<NodeRef>) stepsCaptor.getValue().get(0);
        return step.getProcessWorker();
    }

    private String writtenSystemicValues() {
        ArgumentCaptor<String> valuesCaptor = ArgumentCaptor.forClass(String.class);
        verify(nodeService).setProperty(eq(ingNodeRef), eq(PLMModel.PROP_ING_TOX_SYSTEMIC_VALUES), valuesCaptor.capture());
        return valuesCaptor.getValue();
    }

    private void verifyIngredientNotWritten() {
        verify(nodeService, never()).setProperty(any(NodeRef.class), any(QName.class), any());
    }

    private void givenTox() {
        when(nodeService.getProperty(toxNodeRef, BeCPGModel.PROP_CHARACT_NAME)).thenReturn(new MLText(Locale.getDefault(), TOX_NAME));
        when(nodeService.getProperty(toxNodeRef, PLMModel.PROP_TOX_CALCULATE_SYSTEMIC)).thenReturn(Boolean.TRUE);
        when(nodeService.getProperty(toxNodeRef, PLMModel.PROP_TOX_CALCULATE_MAX)).thenReturn(Boolean.FALSE);
        when(nodeService.getProperty(toxNodeRef, PLMModel.PROP_TOX_VALUE)).thenReturn(TOX_VALUE);
    }

    private void givenIngredientToRecalculate() {
        givenSystemicValues(OUTDATED_SYSTEMIC_VALUES);
        when(nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_POD_SYSTEMIC)).thenReturn(POD_SYSTEMIC);
        when(nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_MOS_MOE)).thenReturn(MOS_MOE);
        when(nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_DERMAL_ABSORPTION)).thenReturn(DERMAL_ABSORPTION);
    }

    private void givenSystemicValues(String systemicValues) {
        when(nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TOX_SYSTEMIC_VALUES)).thenReturn(systemicValues);
    }

    private void givenIngredientDeleted(Boolean deleted) {
        when(nodeService.getProperty(ingNodeRef, BeCPGModel.PROP_IS_DELETED)).thenReturn(deleted);
    }

    private static NodeRef newNodeRef() {
        return new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate());
    }
}
