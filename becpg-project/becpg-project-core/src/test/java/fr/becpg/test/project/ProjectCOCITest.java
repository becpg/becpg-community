package fr.becpg.test.project;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.namespace.QName;
import org.junit.Test;

import fr.becpg.repo.project.data.ProjectData;

/**
 * Test for checkout checkin
 * @author quere
 *
 */
public class ProjectCOCITest extends AbstractProjectTestCase {

	@Resource
	private CheckOutCheckInService checkOutCheckInService;

	@Test
	public void testCheckOutCheckIn(){
		
	
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {
				
				Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
				aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
				nodeService.addAspect(projectTplNodeRef, ContentModel.ASPECT_VERSIONABLE, aspectProperties);

				// Check out
				NodeRef workingCopyNodeRef = checkOutCheckInService.checkout(projectTplNodeRef);
				
				ProjectData workingCopyData = (ProjectData)alfrescoRepository.findOne(workingCopyNodeRef);								
				assertTrue(workingCopyData.getDeliverableList().get(0).getTask().equals(workingCopyData.getTaskList().get(0).getNodeRef()));
				assertTrue(workingCopyData.getDeliverableList().get(1).getTask().equals(workingCopyData.getTaskList().get(1).getNodeRef()));
				assertTrue(workingCopyData.getDeliverableList().get(2).getTask().equals(workingCopyData.getTaskList().get(1).getNodeRef()));
				assertTrue(workingCopyData.getDeliverableList().get(3).getTask().equals(workingCopyData.getTaskList().get(2).getNodeRef()));
				
				// Check in
				Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
				versionProperties.put(Version.PROP_DESCRIPTION, "This is a test version");
				NodeRef newRawMaterialNodeRef = checkOutCheckInService.checkin(workingCopyNodeRef, versionProperties);
				
				assertNotNull(newRawMaterialNodeRef);
				
				return null;
			}
		}, false, true);
	}
	
}
