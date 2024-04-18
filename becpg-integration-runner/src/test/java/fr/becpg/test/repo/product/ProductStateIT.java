/*
 *
 */
package fr.becpg.test.repo.product;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;
import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.PLMBaseTestCase;

/**
 * The Class ProductVersionServiceTest.
 *
 * @author matthieu
 */
public class ProductStateIT extends PLMBaseTestCase {

	@Autowired
	private CopyService copyService;

	@Test
	public void testCopyProduct() throws InterruptedException {

		NodeRef rawMaterialNodeRef = inWriteTx(() -> {

			/*-- Create raw material --*/
			NodeRef ret = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP state test");

			// Valid it
			nodeService.setProperty(ret, PLMModel.PROP_PRODUCT_STATE, SystemState.Valid);

			nodeService.setProperty(ret, BeCPGModel.PROP_ERP_CODE, "0001");

			return ret;
		});

		NodeRef copiedNodeRef = inWriteTx(() -> {

			NodeRef ret = copyService.copy(rawMaterialNodeRef, getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					ContentModel.ASSOC_CONTAINS);

			return ret;
		});

		inWriteTx(() -> {

			assertEquals("Check state", SystemState.Simulation.toString(),
					nodeService.getProperty(copiedNodeRef, PLMModel.PROP_PRODUCT_STATE));

			assertNull("Check Erp Code", nodeService.getProperty(copiedNodeRef, BeCPGModel.PROP_ERP_CODE));

			assertEquals("Check state", SystemState.Valid.toString(),
					nodeService.getProperty(rawMaterialNodeRef, PLMModel.PROP_PRODUCT_STATE));

			return rawMaterialNodeRef;
		});

	}

}
