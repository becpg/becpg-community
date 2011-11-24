package fr.becpg.repo.designer;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import fr.becpg.model.DesignerModel;
import fr.becpg.repo.designer.data.ModelTree;
import fr.becpg.repo.designer.impl.MetaModelVisitor;
import fr.becpg.test.RepoBaseTestCase;

public class DesignerServiceTest extends RepoBaseTestCase {

	

	/** The logger. */
	private static Log logger = LogFactory.getLog(DesignerServiceTest.class);

	/** The app ctx. */
	private static ApplicationContext appCtx = ApplicationContextHelper
			.getApplicationContext();

	/** The node service. */
	private NodeService nodeService;

	/** The file folder service. */
	private FileFolderService fileFolderService;
	
	private NamespaceService namespaceService;
	
	private MetaModelVisitor metaModelVisitor;
	
	private DesignerService designerService;

	private static String PATH_TESTFOLDER = "DesignerTestFolder";
	
	


	
	/** The repository helper. */
	private Repository repositoryHelper;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.util.BaseAlfrescoTestCase#setUp()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		logger.debug("SecurityServiceTest:setUp");

		nodeService = (NodeService) appCtx.getBean("nodeService");
		designerService = (DesignerService) appCtx.getBean("designerService");
		fileFolderService = (FileFolderService) appCtx
				.getBean("fileFolderService");
		repositoryHelper = (Repository) appCtx.getBean("repositoryHelper");
		namespaceService = (NamespaceService) appCtx.getBean("namespaceService");
		
		metaModelVisitor = (MetaModelVisitor) appCtx.getBean("metaModelVisitor");
		
		authenticationComponent.setSystemUserAsCurrentUser();
		
		

	

	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.util.BaseAlfrescoTestCase#tearDown()
	 */
	@Override
	public void tearDown() throws Exception {
		try {
			authenticationComponent.clearCurrentSecurityContext();
		} catch (Throwable e) {
			e.printStackTrace();
			// Don't let this mask any previous exceptions
		}
		super.tearDown();

	}

	public void testMetaModelVisitor() {

		
		transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {

						/*-- Create test folder --*/
						NodeRef folderNodeRef = nodeService.getChildByName(
								repositoryHelper.getCompanyHome(),
								ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);
						if (folderNodeRef != null) {
							fileFolderService.delete(folderNodeRef);
						}
						folderNodeRef = fileFolderService.create(
								repositoryHelper.getCompanyHome(),
								PATH_TESTFOLDER, ContentModel.TYPE_FOLDER)
								.getNodeRef();
						
						

						InputStream in = ClassLoader.getSystemResourceAsStream("beCPG/designer/testModel.xml");
						assertNotNull(in);
						
						M2Model m2Model = M2Model.createModel(in);
						
						NodeRef modelNodeRef = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN, DesignerModel.TYPE_M2_MODEL).getChildRef();
						
						//Try to parse becpgModel 
						metaModelVisitor.visitModelNodeRef(modelNodeRef,
								m2Model);
						
						
						
						
						ModelTree tree = metaModelVisitor.visitModelTreeNodeRef(modelNodeRef);
						assertNotNull(tree);
						logger.debug(tree);

						//To Xml
						metaModelVisitor.visitModelXml(modelNodeRef, System.out);
						
						
						
						
						return null;

					}
				}, false, true);


	}
	
	public void testDesignerService() {

		
		transactionService.getRetryingTransactionHelper().doInTransaction(
				new RetryingTransactionCallback<NodeRef>() {
					@Override
					public NodeRef execute() throws Throwable {

						/*-- Create test folder --*/
						NodeRef folderNodeRef = nodeService.getChildByName(
								repositoryHelper.getCompanyHome(),
								ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);
						if (folderNodeRef != null) {
							fileFolderService.delete(folderNodeRef);
						}
						folderNodeRef = fileFolderService.create(
								repositoryHelper.getCompanyHome(),
								PATH_TESTFOLDER, ContentModel.TYPE_FOLDER)
								.getNodeRef();
						
						

						InputStream in = ClassLoader.getSystemResourceAsStream("beCPG/designer/testModel.xml");
						assertNotNull(in);
						
						M2Model m2Model = M2Model.createModel(in);
						
						NodeRef modelNodeRef = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN, DesignerModel.TYPE_M2_MODEL).getChildRef();
						
						//Try to parse becpgModel 
						metaModelVisitor.visitModelNodeRef(modelNodeRef,
								m2Model);
						
						Map<QName, Serializable> props = new HashMap<QName, Serializable>();
						props.put(DesignerModel.PROP_M2_NAME,"bcpg:test");
						
					
						NodeRef elNodeRef =  designerService.createModelElement(modelNodeRef, DesignerModel.TYPE_M2_TYPE, DesignerModel.ASSOC_M2_TYPES, props, "STARTTASK");
						
						assertEquals("bcpg:test", (String)nodeService.getProperty(elNodeRef, DesignerModel.PROP_M2_NAME));
						assertEquals("bpm:startTask", (String)nodeService.getProperty(elNodeRef, DesignerModel.PROP_M2_PARENT_NAME));
						
						return null;

					}
				}, false, true);


	}
	
	
	
}
