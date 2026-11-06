package fr.becpg.repo.admin;

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;

/**
 * Checks that an association reindex downgrades the integrity check in its own batch transactions
 * only, instead of switching it off for every transaction of the server (#37968).
 *
 * @author matthieu
 */
public class AssociationIndexerServiceTest {

	private static final QName SOURCE_TYPE = QName.createQName(BeCPGModel.BECPG_URI, "taskList");

	private static final QName ASSOC_QNAME = QName.createQName(BeCPGModel.BECPG_URI, "tlResources");

	private static final QName INDEX_QNAME = QName.createQName(BeCPGModel.BECPG_URI, "tlResourcesIndex");

	private static final NodeRef NODE_REF = new NodeRef("workspace://SpacesStore/task");

	@SuppressWarnings("unchecked")
	private final BatchProcessor<NodeRef> batchProcessor = mock(BatchProcessor.class);

	@Mock
	private EntityDictionaryService entityDictionaryService;

	@Mock
	private NodeService nodeService;

	@Mock
	private RuleService ruleService;

	@InjectMocks
	private TestableAssociationIndexerService associationIndexerService;

	private AutoCloseable mocks;

	@Before
	public void setUp() {
		mocks = MockitoAnnotations.openMocks(this);
		associationIndexerService.batchProcessor = batchProcessor;
		when(entityDictionaryService.getAssocIndexQName(ASSOC_QNAME)).thenReturn(INDEX_QNAME);
		TypeDefinition sourceTypeDefinition = mock(TypeDefinition.class);
		when(entityDictionaryService.getType(SOURCE_TYPE)).thenReturn(sourceTypeDefinition);
		when(entityDictionaryService.getSubTypes(SOURCE_TYPE, true)).thenReturn(Collections.emptyList());
	}

	@After
	public void tearDown() throws Exception {
		mocks.close();
	}

	@Test
	public void testReindexRunsTheBatchOnTheSourceType() {
		associationIndexerService.reindexAssocs(SOURCE_TYPE, ASSOC_QNAME);

		assertSame(SOURCE_TYPE, associationIndexerService.processedType);
		verify(batchProcessor).processLong(any(), eq(true));
	}

	@Test
	public void testWorkerDowngradesIntegrityInItsOwnTransaction() throws Throwable {
		BatchProcessWorker<NodeRef> worker = captureWorker();

		try (MockedStatic<IntegrityChecker> integrityChecker = mockStatic(IntegrityChecker.class)) {
			worker.process(NODE_REF);

			integrityChecker.verify(IntegrityChecker::setWarnInTransaction);
		}
	}

	@SuppressWarnings("unchecked")
	private BatchProcessWorker<NodeRef> captureWorker() {
		associationIndexerService.reindexAssocs(SOURCE_TYPE, ASSOC_QNAME);
		ArgumentCaptor<BatchProcessWorker<NodeRef>> workerCaptor = ArgumentCaptor.forClass(BatchProcessWorker.class);
		verify(batchProcessor).processLong(workerCaptor.capture(), eq(true));
		return workerCaptor.getValue();
	}

	/**
	 * Replaces the batch processor, which needs a database, with a mock.
	 */
	static class TestableAssociationIndexerService extends AssociationIndexerService {

		private BatchProcessor<NodeRef> batchProcessor;

		private QName processedType;

		@Override
		protected BatchProcessor<NodeRef> createBatchTypeProcessor(QName type) {
			processedType = type;
			return batchProcessor;
		}
	}
}
