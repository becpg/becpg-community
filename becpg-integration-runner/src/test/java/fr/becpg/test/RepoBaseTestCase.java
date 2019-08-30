/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG.
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
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
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
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
import org.w3c.dom.Document;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.admin.InitVisitorService;
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

	protected NodeRef systemFolderNodeRef;

	public static RepoBaseTestCase INSTANCE;

	public static final Wiser wiser = new Wiser(2500);

	/**
	 * Print the test we are currently running, useful if the test is running
	 * remotely and we don't see the server logs
	 */
	@Rule
	public MethodRule testAnnouncer = (base, method, target) -> {
		System.out.println("Running " + getClassName() + " Integration Test: " + method.getName() + "()");
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
	@Qualifier("qnameDAO")
	protected QNameDAO qNameDAO;

	@Override
	public void afterPropertiesSet() throws Exception {
		INSTANCE = this;

		AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

		boolean shouldInit = shouldInit();

		if (shouldInit) {
			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

				// Init repo for test
				initRepoVisitorService.run(repositoryHelper.getCompanyHome());

				return false;

			}, false, true);
		}

		systemFolderNodeRef = transactionService.getRetryingTransactionHelper()
				.doInTransaction(() -> repoService.getOrCreateFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM,
						TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM)), false, true);

		doInitRepo(shouldInit);

	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		testFolders.put(getTestFolderName(), transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			// As system user
			AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

			String testFolderName = getTestFolderName();

			Date now = new Date();
			SimpleDateFormat format = new SimpleDateFormat("dd_MM_YYYY_hh_mm_ss");

			NodeRef parentTestFolder = repoService.getOrCreateFolderByPath(repositoryHelper.getCompanyHome(), "Junit Tests", "Junit Test");

			parentTestFolder = repoService.getOrCreateFolderByPath(parentTestFolder, format.format(now), format.format(now));

			NodeRef folderNodeRef = RepoBaseTestCase.INSTANCE.fileFolderService.create(parentTestFolder, testFolderName, ContentModel.TYPE_FOLDER)
					.getNodeRef();
			return folderNodeRef;

		}, false, true));
	}

	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();

		// transactionService.getRetryingTransactionHelper().doInTransaction(new
		// RetryingTransactionCallback<Boolean>() {
		// public Boolean execute() throws Throwable {
		// ruleService.disableRules();
		// try {
		// IntegrityChecker.setWarnInTransaction();
		// nodeService.addAspect(getTestFolderNodeRef(),
		// ContentModel.ASPECT_TEMPORARY, null);
		// logger.debug("Delete test folder");
		// nodeService.deleteNode(getTestFolderNodeRef());
		// } finally {
		// ruleService.enableRules();
		// }
		// return true;
		//
		// }
		// }, false, true);

	}

	public void waitForSolr(final Date startTime) {
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef nodeRef = nodeService
					.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_CONTENT)
					.getChildRef();

			nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, "" + startTime.getTime()+"1");
			nodeService.setProperty(nodeRef, BeCPGModel.PROP_IS_MANUAL_LISTITEM, true);
			return null;

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			int j = 0;
			while ((BeCPGQueryBuilder.createQuery().andPropQuery(ContentModel.PROP_NAME, "" + startTime.getTime()+"*")
					.andPropEquals(BeCPGModel.PROP_IS_MANUAL_LISTITEM, "true").inParent(getTestFolderNodeRef()).ftsLanguage().singleValue() == null)
					&& (j < 30)) {

				logger.info("Wait for solr (2s) : serverIdx " + getLastSolrIndex() + " retry *" + j);
				Thread.sleep(2000);
				j++;
			}

			return null;

		}, false, true);

	}

	// public void waitForSolr(final Date startTime) {
	// transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
	//
	// List<Transaction> transactions =
	// solrTrackingComponent.getTransactions(null, startTime.getTime(), null,
	// null, 1000);
	//
	// logger.info("Found " + transactions.size() + " new transactions");
	//
	// Long lastIdxServer = transactions.get(transactions.size() - 1).getId();
	//
	// Long lastIdxSolr = getLastSolrIndex();
	// Long transactionInSolr = getTransactionInIndex();
	// Long transactionInServer = transactionInSolr+transactions.size();
	//
	// int j = 0;
	// while (((lastIdxSolr < lastIdxServer) || (transactionInSolr <
	// transactionInServer)) && (j < 10)) {
	// Thread.sleep(2000);
	// lastIdxSolr = getLastSolrIndex();
	// transactionInSolr = getTransactionInIndex();
	// j++;
	// logger.info("Wait for solr (2s) : serverIdx " + lastIdxServer + " solrIdx
	// " + lastIdxSolr + " serverTx " + transactionInServer + " solrTx " +
	// transactionInSolr + " retry *" + j);
	// }
	//
	// int count = 0;
	// j=0;
	// while (count <10 && (j < 10)) {
	// long curtrans = getTransactionInIndex();
	//
	// if(transactionInSolr == curtrans) {
	// count++;
	// }
	// logger.info("Wait for solr (2s) "+curtrans);
	// transactionInSolr = curtrans;
	// Thread.sleep(2000);
	// j++;
	// }
	//
	//
	// return null;
	//
	// }, false, true);
	// }

	private Long getLastSolrIndex() throws Exception {

		HttpClient httpclient = HttpClients.createDefault();
		HttpGet httpget = new HttpGet("http://solr:8983/solr/admin/cores?action=SUMMARY&wt=xml&core=alfresco");
		HttpResponse httpResponse = httpclient.execute(httpget);
		assertEquals("HTTP Response Status is not OK(200)", HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
		HttpEntity entity = httpResponse.getEntity();
		assertNotNull("Response from Web Script is null", entity);

		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true); // never forget this!
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document doc = builder.parse(entity.getContent());

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		// logger.info("Approx transaction indexing time remaining : "+ (String)
		// xpath.evaluate("//str[@name='Approx transaction indexing time
		// remaining']", doc, XPathConstants.STRING));
		// logger.info("Approx change set indexing time remaining : "+ (String)
		// xpath.evaluate("//str[@name='Approx change set indexing time
		// remaining']", doc, XPathConstants.STRING));
		// logger.info("Alfresco Transactions in Index : "+ (String)
		// xpath.evaluate("//long[@name='Alfresco Transactions in Index']", doc,
		// XPathConstants.STRING));
		//
		String strIndex = (String) xpath.evaluate("//long[@name='Id for last TX on server']", doc, XPathConstants.STRING);
		if ((strIndex == null) || strIndex.isEmpty()) {
			return getLastSolrIndex();
		}
		// <long name="Id for last TX on server">1413</long><long
		// name="Id for last TX in index">1413</long>
		return Long.valueOf(strIndex);
	}

	protected boolean shouldInit() {
		return nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS,
				TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM)) == null;
	}

	protected void doInitRepo(boolean shouldInit) {
	}
}
