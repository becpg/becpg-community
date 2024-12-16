/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntitySystemService;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.hierarchy.HierarchyService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.search.BeCPGQueryBuilder;
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

		inReadTx(() -> {
			int j = 0;
			while ((BeCPGQueryBuilder.createQuery().andPropQuery(ContentModel.PROP_NAME, "" + startTime.getTime() + "*")
					.andPropEquals(BeCPGModel.PROP_IS_MANUAL_LISTITEM, "true").inParent(getTestFolderNodeRef()).ftsLanguage().singleValue() == null)
					&& (j < 30)) {

				logger.info("Wait for solr (2s) : serverIdx retry *" + j);
				Thread.sleep(2000);
				j++;
			}
			
			if(j == 30) {
				Assert.fail("Solr is taking too long!");
			}

			return null;

		});

	}
	

	public void waitForBatchEnd(BatchInfo batch) throws InterruptedException {
		int j = 0;
		
		while(!Boolean.TRUE.equals(batch.getIsCompleted()) && (j < 60)) {
			logger.info("Wait for batch: "+ batch.getBatchId() + ", progress: " + (double) (batch.getCurrentItem() / batch.getTotalItems()) + "%");
			Thread.sleep(5000);
			j++;
		}
		
		if(j == 60) {
			Assert.fail("Batch is taking too long! Progress: " + (double) (batch.getCurrentItem() / batch.getTotalItems()) + "%");
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
