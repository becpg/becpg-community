/*
 *
 */
package fr.becpg.test.repo.product;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;
import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.PLMBaseTestCase;

/**
 * The Class ProductVersionServiceTest.
 *
 * @author matthieu
 */
public class ProductStateTest extends PLMBaseTestCase {

	@Resource
	private CopyService copyService;

	@Test
	public void testCopyProduct() throws InterruptedException {

		NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Create raw material --*/
			NodeRef ret = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP state test");

			// Valid it
			nodeService.setProperty(ret, PLMModel.PROP_PRODUCT_STATE, SystemState.Valid);

			nodeService.setProperty(ret, PLMModel.PROP_ERP_CODE, "0001");

			return ret;
		}, false, true);

		NodeRef copiedNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef ret = copyService.copy(rawMaterialNodeRef, getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS);

			return ret;
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			assertEquals("Check state", SystemState.Simulation.toString(), nodeService.getProperty(copiedNodeRef, PLMModel.PROP_PRODUCT_STATE));

			assertNull("Check Erp Code", nodeService.getProperty(copiedNodeRef, PLMModel.PROP_ERP_CODE));

			assertEquals("Check state", SystemState.Valid.toString(), nodeService.getProperty(rawMaterialNodeRef, PLMModel.PROP_PRODUCT_STATE));

			return rawMaterialNodeRef;
		}, false, true);

	}

}
