package fr.becpg.repo.designer;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import fr.becpg.test.RepoBaseTestCase;

/**
 * 
 * @author matthieu
 * 
 */
public class DesignerInitServiceTest extends RepoBaseTestCase{

	/** The logger. */
	private static Log logger = LogFactory.getLog(DesignerInitServiceTest.class);

	@Autowired
	private DesignerInitService designerInitService;

	@Autowired
	@Qualifier("NodeService")
	private NodeService nodeService;

	@Autowired
	@Qualifier("TransactionService")
	private TransactionService transactionService;

	@Before
	public void setUp() throws Exception {

		logger.debug("DesignerInitServiceTest:setUp");
		AuthenticationUtil.setRunAsUserSystem();

	}

	@Test
	public void testInitDesigner() {

		logger.info("testInitDesigner");
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				NodeRef workflowFolder = designerInitService.getWorkflowsNodeRef();

				Assert.assertNotNull(workflowFolder);
				Assert.assertNotNull(designerInitService.getModelsNodeRef());
				Assert.assertNotNull(designerInitService.getConfigsNodeRef());

				Assert.assertNotNull(nodeService.getChildByName(designerInitService.getModelsNodeRef(), ContentModel.ASSOC_CONTAINS, "extCustomModel.xml"));
				Assert.assertNotNull(nodeService.getChildByName(designerInitService.getConfigsNodeRef(), ContentModel.ASSOC_CONTAINS, "extCustomForm.xml"));

				return null;

			}
		}, false, true);

	}

}
