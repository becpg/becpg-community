/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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

import javax.annotation.Resource;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.subethamail.wiser.Wiser;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.admin.InitVisitorService;
import fr.becpg.repo.cache.BeCPGCacheService;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:alfresco/application-context.xml", "classpath:alfresco/web-scripts-application-context.xml",
		"classpath:alfresco/web-scripts-application-context-test.xml" })
public abstract class RepoBaseTestCase extends TestCase implements InitializingBean {

	private static Log logger = LogFactory.getLog(RepoBaseTestCase.class);

	
	protected NodeRef testFolderNodeRef;
	protected NodeRef systemFolderNodeRef;

	public static RepoBaseTestCase INSTANCE;

	public static Wiser wiser = new Wiser(2500);

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
	protected QNameDAO qNameDAO;

	@Override
	public void afterPropertiesSet() throws Exception {
		INSTANCE = this;
	}

	@BeforeClass
	public static void setupBeforeClass() {
		try {
			logger.debug("setupBeforeClass : Start wiser");
			wiser.start();
		} catch (Exception e) {
			logger.warn("cannot open wiser!", e);
		}
	}

	@AfterClass
	public static void tearDownBeforeClass() {
		try {
			logger.debug("tearDownBeforeClass : Stop wiser");
			wiser.stop();
		} catch (Exception e) {
			logger.warn("cannot stop wiser!", e);
		}

	}

	@Before
	public void setUp() throws Exception {

		super.setUp();

	
		
		testFolderNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
				// As system user
				AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

				// test folder
				return BeCPGTestHelper.createTestFolder();
			}
		}, false, true);

		boolean shouldInit = shouldInit();
		
		

		if (shouldInit) {
			transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Boolean>() {
				public Boolean execute() throws Throwable {
									
					initQNames();
					
					// Init repo for test
					initRepoVisitorService.run(repositoryHelper.getCompanyHome());

					return false;

				}
			}, false, true);
		}

		logger.debug("setUp shouldInit :" + shouldInit);

		systemFolderNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
				return repoService.getOrCreateFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));

			}
		}, false, true);

		doInitRepo(shouldInit);

	}

	protected boolean shouldInit(){
		return nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM)) == null;
	}
	
	protected  void doInitRepo(boolean shouldInit) {		
	}	
	
	/**
	 * We need to do this in order QName exists (required for CMIS), they are added in DB when we create node
	 */
	protected void initQNames(){
//		qNameDAO.getOrCreateQName(BeCPGModel.PROP_SORT);
		for(QName model : dictionaryDAO.getModels()){
			for(PropertyDefinition propertyDef : dictionaryDAO.getProperties(model)){
				qNameDAO.getOrCreateQName(propertyDef.getName());
			}
		}		
	}
	
	@After
	public void tearDown() throws Exception {
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Boolean>() {
			public Boolean execute() throws Throwable {

			
				logger.debug("   - Deleting :" + nodeService.getProperty(testFolderNodeRef, ContentModel.PROP_NAME));
				nodeService.deleteNode(testFolderNodeRef);
				return true;

			}
		}, false, true);
	}
}
