package fr.becpg.repo.designer;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import fr.becpg.repo.designer.impl.FormModelVisitor;
import fr.becpg.repo.designer.impl.MetaModelVisitor;
import fr.becpg.repo.designer.workflow.DesignerWorkflowDeployer;

public class DesignerWorkflowDeployerTest extends TestCase {

	private static Log logger = LogFactory.getLog(DesignerWorkflowDeployerTest.class);

	private FileFolderService fileFolderService;

	private MimetypeService mimetypeService;

	private MetaModelVisitor metaModelVisitor;

	private FormModelVisitor formModelVisitor;

	private DesignerWorkflowDeployer designerWorkflowDeployer;

	private DesignerService designerService;

	private NodeService nodeService;

	private ContentService contentService;

	private TransactionService transactionService;

	private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

	private static String PATH_TESTFOLDER = "DesignerTestFolder";

	private Repository repositoryHelper;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		logger.debug("DesignerServiceTest:setUp");
		designerService = (DesignerService) ctx.getBean("designerService");
		fileFolderService = (FileFolderService) ctx.getBean("fileFolderService");
		repositoryHelper = (Repository) ctx.getBean("repositoryHelper");

		metaModelVisitor = (MetaModelVisitor) ctx.getBean("metaModelVisitor");

		formModelVisitor = (FormModelVisitor) ctx.getBean("formModelVisitor");
		designerWorkflowDeployer = (DesignerWorkflowDeployer) ctx.getBean("designerWorkflowDeployer");

		mimetypeService = (MimetypeService) ctx.getBean("mimetypeService");

		nodeService = (NodeService) ctx.getBean("nodeService");
		transactionService = (TransactionService) ctx.getBean("TransactionService");

		contentService = (ContentService) ctx.getBean("contentService");

		AuthenticationUtil.setRunAsUserSystem();

	}

	public void testCreateMissingFormsAndType() {

		logger.info("testCreateMissingFormsAndType");
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				/*-- Create test folder --*/
				NodeRef folderNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);
				if (folderNodeRef != null) {
					fileFolderService.delete(folderNodeRef);
				}
				folderNodeRef = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();

				InputStream in = ClassLoader.getSystemResourceAsStream("beCPG/designer/testWorkflow.xml");
				assertNotNull(in);

				String fileName = "testWorkflow.xml";
				logger.debug("add file " + fileName);

				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				properties.put(ContentModel.PROP_NAME, fileName);
				properties.put(WorkflowModel.PROP_WORKFLOW_DEF_ENGINE_ID, ActivitiConstants.ENGINE_ID);

				NodeRef workflowNodeRef = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), WorkflowModel.TYPE_WORKFLOW_DEF, properties)
						.getChildRef();

				ContentWriter writer = contentService.getWriter(workflowNodeRef, ContentModel.PROP_CONTENT, true);

				writer.setMimetype(mimetypeService.guessMimetype(fileName));
				writer.putContent(in);
				in.close();

				designerWorkflowDeployer.createMissingFormsAndType(workflowNodeRef);
				// Do it twice to test update
				designerWorkflowDeployer.createMissingFormsAndType(workflowNodeRef);

				NodeRef modelNodeRef = designerService.findOrCreateModel(fileName, null, null);

				metaModelVisitor.visitModelXml(modelNodeRef, System.out);

				NodeRef configNodeRef = designerService.findOrCreateConfig(fileName, null, null);

				assertNotNull(configNodeRef);

				formModelVisitor.visitConfigXml(configNodeRef, System.out);

				nodeService.deleteNode(nodeService.getPrimaryParent(modelNodeRef).getParentRef());
				nodeService.deleteNode(nodeService.getPrimaryParent(configNodeRef).getParentRef());
				return null;

			}
		}, false, true);

	}

}
