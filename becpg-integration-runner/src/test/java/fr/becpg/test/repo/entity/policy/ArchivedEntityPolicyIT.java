package fr.becpg.test.repo.entity.policy;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityFormatService;
import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.PLMBaseTestCase;


public class ArchivedEntityPolicyIT extends PLMBaseTestCase {

	private static final Log logger = LogFactory.getLog(ArchivedEntityPolicyIT.class);

	@Autowired
	private EntityFormatService entityFormatService;
	
	@Test
	public void archivedEntityTest() throws InterruptedException {
		
		NodeRef rawMaterialNodeRef = inWriteTx(() -> BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP archivedEntityTest"));
		
		inWriteTx(() -> {
			nodeService.addAspect(rawMaterialNodeRef, BeCPGModel.ASPECT_ARCHIVED_ENTITY, null);
			return null;
		});
		
		waitForEntityData(rawMaterialNodeRef, true);
		
		inReadTx(() -> {
			String entityData = entityFormatService.getEntityData(rawMaterialNodeRef);
			assertNotNull(entityData);
			return null;
		});
		
		inWriteTx(() -> {
			nodeService.removeAspect(rawMaterialNodeRef, BeCPGModel.ASPECT_ARCHIVED_ENTITY);
			return null;
		});
		
		waitForEntityData(rawMaterialNodeRef, false);
		
		inReadTx(() -> {
			String entityData = entityFormatService.getEntityData(rawMaterialNodeRef);
			assertNull(entityData);
			return null;
		});
	}
	
	private void waitForEntityData(NodeRef nodeRef, boolean created) throws InterruptedException {
		int i = 0;
		while (i < 50) {
			Thread.sleep(1000);
			logger.debug("waiting for entity data...");
			i++;
			if (inWriteTx(() -> {
				if (created) {
					return entityFormatService.getEntityData(nodeRef) != null;
				}
				return entityFormatService.getEntityData(nodeRef) == null;
			})) {
				return;
			}
		}
	}
	
}
