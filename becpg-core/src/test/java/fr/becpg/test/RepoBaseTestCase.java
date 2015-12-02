/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
import java.util.List;

import javax.annotation.Resource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.solr.SOLRTrackingComponent;
import org.alfresco.repo.solr.Transaction;
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
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.subethamail.wiser.Wiser;
import org.w3c.dom.Document;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

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

/**
 * base class of test cases for product classes.
 * 
 * @author matthieu
 */

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:alfresco/application-context.xml" })
public abstract class RepoBaseTestCase extends TestCase implements InitializingBean {

	private static final Log logger = LogFactory.getLog(RepoBaseTestCase.class);

	private final ThreadLocal<NodeRef> threadSafeTestFolder = new ThreadLocal<>();

	public NodeRef getTestFolderNodeRef() {
		return threadSafeTestFolder.get();
	}

	protected NodeRef systemFolderNodeRef;

	public static RepoBaseTestCase INSTANCE;

	public static final Wiser wiser = new Wiser(2500);

	static {
		try {
			logger.debug("setupBeforeClass : Start wiser");
			wiser.start();
		} catch (Exception e) {
			logger.debug("cannot open wiser!", e);
		}
	}

	@Resource
	protected MimetypeService mimetypeService;

	@Resource
	protected Repository repositoryHelper;

	@Autowired
	protected NodeService nodeService;

	@Resource
	protected RepoService repoService;

	@Resource
	protected FileFolderService fileFolderService;

	@Resource
	protected DictionaryDAO dictionaryDAO;

	@Resource
	protected EntitySystemService entitySystemService;

	@Resource
	protected ServiceRegistry serviceRegistry;

	@Resource
	protected InitVisitorService initRepoVisitorService;

	@Resource
	protected HierarchyService hierarchyService;

	@Resource
	protected AuthenticationComponent authenticationComponent;

	@Resource
	protected ContentService contentService;

	@Resource
	protected TransactionService transactionService;

	@Resource
	protected RetryingTransactionHelper retryingTransactionHelper;

	@Resource
	protected AuthorityService authorityService;

	@Resource
	protected MutableAuthenticationDao authenticationDAO;

	@Resource
	protected MutableAuthenticationService authenticationService;

	@Resource
	protected PersonService personService;

	@Resource
	protected EntityTplService entityTplService;

	@Resource
	protected PermissionService permissionService;

	@Resource
	protected AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	@Resource
	protected BeCPGCacheService beCPGCacheService;

	@Resource
	protected RuleService ruleService;

	@Resource
	protected BehaviourFilter policyBehaviourFilter;

	@Resource
	private SOLRTrackingComponent solrTrackingComponent;
	
	@Resource
	protected EntityListDAO entityListDAO;

	@Resource
	@Qualifier("qnameDAO")
	protected QNameDAO qNameDAO;

	@Override
	public void afterPropertiesSet() throws Exception {
		INSTANCE = this;

		AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

		boolean shouldInit = shouldInit();

		if (shouldInit) {
			transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Boolean>() {
				public Boolean execute() throws Throwable {

					// Init repo for test
					initRepoVisitorService.run(repositoryHelper.getCompanyHome());

					return false;

				}
			}, false, true);
		}

		systemFolderNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
				return repoService.getOrCreateFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM,
						TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));

			}
		}, false, true);

		doInitRepo(shouldInit);

	}

	@Before
	public void setUp() throws Exception {
		threadSafeTestFolder.set(transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
				// As system user
				AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

				String testFolderName = "TestFolder" + (new Date()).getTime();

				NodeRef folderNodeRef = RepoBaseTestCase.INSTANCE.fileFolderService.create(repositoryHelper.getCompanyHome(), testFolderName,
						ContentModel.TYPE_FOLDER).getNodeRef();
				return folderNodeRef;

			}
		}, false, true));
	}

	@After
	public void tearDown() throws Exception {
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Boolean>() {
			public Boolean execute() throws Throwable {
				ruleService.disableRules();
				try {
					nodeService.addAspect(threadSafeTestFolder.get(), ContentModel.ASPECT_TEMPORARY, null);
					nodeService.deleteNode(threadSafeTestFolder.get());
				} finally {
					ruleService.enableRules();
				}
				return true;

			}
		}, false, true);
	}

	public void waitForSolr(final Date startTime) {
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				List<Transaction> transactions = solrTrackingComponent.getTransactions(null, startTime.getTime(), null, null, 100);

				logger.info("Found " + transactions.size() + " new transactions");

				Long lastIdxServer = transactions.get(transactions.size() - 1).getId();

				Long lastIdxSolr = getLastSolrIndex();
				int j = 0;
				while (lastIdxSolr < lastIdxServer && j < 10) {
					Thread.sleep(2000);
					lastIdxSolr = getLastSolrIndex();
					j++;
					logger.info("Wait for solr (2s) : serverIdx " + lastIdxServer + " solrIdx " + lastIdxSolr + " retry *" + j);
				}

				return null;

			}

		}, false, true);
	}

	private Long getLastSolrIndex() throws Exception {

		HttpClient httpclient = HttpClients.createDefault();
		HttpGet httpget = new HttpGet("http://localhost:8080/solr4/admin/cores?action=SUMMARY&wt=xml");
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

		// <long name="Id for last TX on server">1413</long><long
		// name="Id for last TX in index">1413</long>

		return Long.valueOf((String) xpath.evaluate("//long[@name='Id for last TX in index']", doc, XPathConstants.STRING));
	}

	protected boolean shouldInit() {
		return nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS,
				TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM)) == null;
	}

	protected void doInitRepo(boolean shouldInit) {
	}
}
