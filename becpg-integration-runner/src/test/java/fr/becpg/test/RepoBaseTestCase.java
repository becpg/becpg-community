/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.MethodRule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestExecutionListeners;
import org.subethamail.wiser.Wiser;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.admin.InitVisitorService;
import fr.becpg.repo.audit.service.BeCPGAuditService;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntitySystemService;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.entity.datalist.WUsedListService;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.hierarchy.HierarchyService;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.publication.PublicationChannelService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.system.SystemConfigurationService;
import junit.framework.TestCase;

/**
 * base class of test cases for product classes.
 *
 * @author matthieu
 */
@RunWith(value = BeCPGTestRunner.class)
@TestExecutionListeners({ BeCPSpringTestListener.class })
public abstract class RepoBaseTestCase extends TestCase implements InitializingBean {

	private static final Log logger = LogFactory.getLog(RepoBaseTestCase.class);

	/** The tracker polls the repository every 10s, so a node is never searchable straight away. */
	private static final long SOLR_POLL_INTERVAL_MS = 500;

	/** A class churning thousands of nodes leaves a backlog the tracker has to drain first. */
	private static final long SOLR_TIMEOUT_MS = 240_000;

	/** How often a batch is polled while it runs. */
	private static final long BATCH_POLL_INTERVAL_MS = 500;

	/** How long a batch is given to run to its end. */
	private static final long BATCH_TIMEOUT_MS = 300_000;

	/** How long the queue is given to let go of a batch that has completed. */
	private static final long BATCH_RELEASE_TIMEOUT_MS = 30_000;

	private Map<String, NodeRef> testFolders = new HashMap<>();

	@Rule
	public TestName name = new TestName();

	public NodeRef getTestFolderNodeRef() {
		return testFolders.get(getTestFolderName());
	}

	private String getTestFolderName() {
		return getClassName().replaceAll("\\.", "_") + "_" + name.getMethodName();
	}
	
	protected String toTestName(String product) {
		return  name.getMethodName()+" -  "+ product;
	}


	protected NodeRef systemFolderNodeRef;

	public static RepoBaseTestCase INSTANCE;

	public static final Wiser wiser = Wiser.port(2500);

	/**
	 * Print the test we are currently running, useful if the test is running
	 * remotely and we don't see the server logs
	 */
	@Rule
	public MethodRule testAnnouncer = (base, method, target) -> {
		logger.info("Running " + getClassName() + " Integration Test: " + method.getName() + "()");
		return base;
	};

	protected String getClassName() {
		Class<?> enclosingClass = getClass().getEnclosingClass();
		if (enclosingClass != null) {
			return enclosingClass.getName();
		} else {
			return getClass().getName();
		}
	}

	static {
		try {
			logger.debug("setupBeforeClass : Start wiser");
			wiser.start();
		} catch (Exception e) {
			logger.debug("cannot open wiser!", e);
		}
	}

	@Autowired
	protected MimetypeService mimetypeService;

	@Autowired
	protected Repository repositoryHelper;

	@Autowired
	protected NodeService nodeService;

	@Autowired
	protected RepoService repoService;

	@Autowired
	protected FileFolderService fileFolderService;

	@Autowired
	protected DictionaryDAO dictionaryDAO;

	@Autowired
	protected EntitySystemService entitySystemService;

	@Autowired
	protected ServiceRegistry serviceRegistry;

	@Autowired
	protected InitVisitorService initRepoVisitorService;

	@Autowired
	protected HierarchyService hierarchyService;

	@Autowired
	protected AuthenticationComponent authenticationComponent;

	@Autowired
	protected ContentService contentService;

	@Autowired
	protected TransactionService transactionService;

	@Autowired
	protected RetryingTransactionHelper retryingTransactionHelper;

	@Autowired
	protected AuthorityService authorityService;

	@Autowired
	protected MutableAuthenticationDao authenticationDAO;

	@Autowired
	protected MutableAuthenticationService authenticationService;

	@Autowired
	protected PersonService personService;

	@Autowired
	protected EntityTplService entityTplService;

	@Autowired
	protected PermissionService permissionService;

	@Autowired
	protected AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	
	@Autowired
	protected ProductService productService;

	@Autowired
	protected BeCPGCacheService beCPGCacheService;

	@Autowired
	protected RuleService ruleService;

	@Autowired
	protected BehaviourFilter policyBehaviourFilter;

	@Autowired
	protected EntityListDAO entityListDAO;
	
	@Autowired
	protected BeCPGAuditService beCPGAuditService;
	
	@Autowired
	protected BatchQueueService batchQueueService;
	
	@Autowired
	protected SystemConfigurationService systemConfigurationService;
	
	@Autowired
	protected EntityDictionaryService entityDictionaryService;
	
	@Autowired
	protected WUsedListService wUsedListService;
	
	@Autowired
	protected FormulationService<FormulatedEntity> formulationService;

	@Autowired
	protected NamespacePrefixResolver namespaceService;

	@Autowired
	protected PublicationChannelService publicationChannelService;

	@Autowired
	@Qualifier("qnameDAO")
	protected QNameDAO qNameDAO;

	@Override
	public void afterPropertiesSet() throws Exception {
		INSTANCE = this;

		AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

		boolean shouldInit = shouldInit();

		if (shouldInit) {
			inWriteTx(() -> {

				// Init repo for test
				initRepoVisitorService.run(repositoryHelper.getCompanyHome());

				return false;

			});

		}

		systemFolderNodeRef = inWriteTx(() -> repoService.getOrCreateFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM,
						TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM)));

		doInitRepo(shouldInit);

	} 

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		inWriteTx(() -> {
			List<org.alfresco.service.cmr.rule.Rule> rules = ruleService.getRules(repositoryHelper.getCompanyHome(), false);
			for (org.alfresco.service.cmr.rule.Rule rule : rules) {
				if (!rule.getRuleDisabled()) {
					if ("classifyEntityRule".equals(rule.getTitle())) {
						ruleService.disableRule(rule);
					}
				}
			}
			return null;
		});

		testFolders.put(getTestFolderName(), inWriteTx(() -> {
			// As system user
			AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

			String testFolderName = getTestFolderName();

			NodeRef parentTestFolder = repoService.getOrCreateFolderByPath(repositoryHelper.getCompanyHome(), "Junit Tests", "Junit Test");

			NodeRef folderNodeRef = repoService.getFolderByPath(parentTestFolder, testFolderName);

			if (folderNodeRef != null) {

				try {
					ruleService.disableRules();
					policyBehaviourFilter.disableBehaviour();
					
					IntegrityChecker.setWarnInTransaction();
					nodeService.addAspect(folderNodeRef, ContentModel.ASPECT_TEMPORARY, null);
					logger.debug("Delete test folder");
					nodeService.deleteNode(folderNodeRef);
				} finally {
					ruleService.enableRules();
					policyBehaviourFilter.enableBehaviour();
				}

			}

			folderNodeRef = RepoBaseTestCase.INSTANCE.fileFolderService.create(parentTestFolder, testFolderName, ContentModel.TYPE_FOLDER)
					.getNodeRef();
			return folderNodeRef;

		}));
	}

	
	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}
	
	/**
	 * Blocks until a marker node created here is searchable, which means the tracker has caught up
	 * with everything this test committed. Transactions are indexed in order, so the wait also
	 * covers the backlog left behind by the classes that ran before.
	 *
	 * Each probe runs in its own short transaction: holding one open for the whole wait would keep
	 * a database connection busy for minutes and slow down the very tracker being waited for.
	 */
	public void waitForSolr() {

		Date startTime = new Date();

		inWriteTx(() -> {

			NodeRef nodeRef = nodeService
					.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_CONTENT)
					.getChildRef();

			nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, "" + startTime.getTime() + "1");
			nodeService.setProperty(nodeRef, BeCPGModel.PROP_IS_MANUAL_LISTITEM, true);
			return null;

		});

		long waitStart = System.currentTimeMillis();
		long waited = 0;

		while (!inReadTx(() -> isMarkerIndexed(startTime)) && (waited < SOLR_TIMEOUT_MS)) {
			sleepQuietly(SOLR_POLL_INTERVAL_MS);
			waited = System.currentTimeMillis() - waitStart;
		}

		if (waited >= SOLR_TIMEOUT_MS) {
			Assert.fail("Solr is taking too long! Waited " + (waited / 1000) + "s for the tracker to index the marker node - "
					+ "the tracker is most likely still draining the backlog of the previous test class");
		}

		logger.info("Waited " + waited + "ms for solr");

	}

	private void sleepQuietly(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while waiting", e);
		}
	}

	/**
	 * Blocks until the queue holds nothing any more. A test that stops on a failed assertion leaves
	 * its batch behind, and the next test would then be reading a queue it did not fill.
	 */
	protected void waitForBatchQueueToDrain() {

		long waitStart = System.currentTimeMillis();

		while (((System.currentTimeMillis() - waitStart) < BATCH_TIMEOUT_MS)
				&& (!batchQueueService.getBatchesInQueue().isEmpty() || (batchQueueService.getRunningBatchInfo() != null))) {
			logger.info("Wait for the batch queue to drain, running batch: " + batchQueueService.getRunningBatchInfo());
			sleepQuietly(BATCH_POLL_INTERVAL_MS);
		}
	}

	/**
	 * Blocks until the queue no longer holds a batch under the given identifier, whoever queued it.
	 * The scheduled jobs keep running while the campaign does, so an identifier a test is about to
	 * use may still be busy with a run of its own.
	 *
	 * @param batchInfo names the batch to wait for, by its identifier and priority
	 */
	protected void waitForBatchQueueToRelease(BatchInfo batchInfo) {

		long waitStart = System.currentTimeMillis();

		while (batchQueueService.isBatchInQueue(batchInfo) && ((System.currentTimeMillis() - waitStart) < BATCH_TIMEOUT_MS)) {
			logger.info("Wait for the queue to release batch: " + batchInfo.getBatchId());
			sleepQuietly(BATCH_POLL_INTERVAL_MS);
		}
	}

	private boolean isMarkerIndexed(Date startTime) {
		return BeCPGQueryBuilder.createQuery().andPropQuery(ContentModel.PROP_NAME, "" + startTime.getTime() + "*")
				.andPropEquals(BeCPGModel.PROP_IS_MANUAL_LISTITEM, "true").inParent(getTestFolderNodeRef()).ftsLanguage()
				.singleValue() != null;
	}
	

	/**
	 * Blocks until the batch has run to its end and the queue has let go of it. A batch the queue
	 * still holds keeps its identifier busy, so leaving before that turns into a failure of the
	 * next test rather than of this one.
	 *
	 * @param batch the batch to wait for
	 * @throws java.lang.InterruptedException if the wait is interrupted
	 */
	public void waitForBatchEnd(BatchInfo batch) throws InterruptedException {

		long waited = 0;

		while (!Boolean.TRUE.equals(batch.getIsCompleted()) && (waited < BATCH_TIMEOUT_MS)) {
			logger.info("Wait for batch: " + batch.getBatchId() + ", progress: " + (batch.getCurrentItem() + "/" + batch.getTotalItems()));
			Thread.sleep(BATCH_POLL_INTERVAL_MS);
			waited += BATCH_POLL_INTERVAL_MS;
		}

		if (waited >= BATCH_TIMEOUT_MS) {
			Assert.fail("Batch is taking too long! Progress: " + (batch.getCurrentItem() + "/" + batch.getTotalItems()) + ", running batch: "
					+ batchQueueService.getRunningBatchInfo());
		}

		waited = 0;

		while (!batchQueueService.isBatchCompleted(batch) && (waited < BATCH_RELEASE_TIMEOUT_MS)) {
			Thread.sleep(BATCH_POLL_INTERVAL_MS);
			waited += BATCH_POLL_INTERVAL_MS;
		}

		if (waited >= BATCH_RELEASE_TIMEOUT_MS) {
			Assert.fail("Batch '" + batch.getBatchId() + "' has completed but the queue still holds it, running batch: "
					+ batchQueueService.getRunningBatchInfo() + ", queue: " + batchQueueService.getBatchesInQueue());
		}
	}
	
	protected boolean shouldInit() {
		return nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS,
				TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM)) == null;
	}

	protected void doInitRepo(boolean shouldInit) {
	}
	
	protected <R> R inReadTx(RetryingTransactionCallback<R> callBack) {
		return transactionService.getRetryingTransactionHelper()
		.doInTransaction(callBack , true, true);
	}
	
	protected <R> R inWriteTx(RetryingTransactionCallback<R> callBack) {
		return transactionService.getRetryingTransactionHelper()
		.doInTransaction(callBack , false, true);
	}
	
}
