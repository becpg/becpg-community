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

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Create raw material --*/
			NodeRef rawMaterialNodeRef = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP state test");

			// Valid it
			nodeService.setProperty(rawMaterialNodeRef, PLMModel.PROP_PRODUCT_STATE, SystemState.Valid);

			NodeRef copiedNodeRef = copyService.copy(rawMaterialNodeRef, getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					ContentModel.ASSOC_CONTAINS);

			assertEquals("Check state", SystemState.Simulation.toString(),
					nodeService.getProperty(copiedNodeRef, PLMModel.PROP_PRODUCT_STATE));

			assertEquals("Check state", SystemState.Valid.toString(),
					nodeService.getProperty(rawMaterialNodeRef, PLMModel.PROP_PRODUCT_STATE));

			return rawMaterialNodeRef;
		}, false, true);

	}

}
