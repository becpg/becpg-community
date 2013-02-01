package fr.becpg.repo.project;

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.test.RepoBaseTestCase;

/**
 * Test for checkout checkin
 * @author quere
 *
 */
public class ProjectCOCITest extends AbstractProjectTest {

	/** The logger. */
	private static Log logger = LogFactory.getLog(ProjectCOCITest.class);

	@Resource
	private CheckOutCheckInService checkOutCheckInService;

	@Test
	public void testCheckOutCheckIn(){
		
		initTest();
	
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {
				
				Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
				aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
				nodeService.addAspect(projectTplNodeRef, ContentModel.ASPECT_VERSIONABLE, aspectProperties);

				// Check out
				NodeRef workingCopyNodeRef = checkOutCheckInService.checkout(projectTplNodeRef);
				
				// Check in
				Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
				versionProperties.put(Version.PROP_DESCRIPTION, "This is a test version");
				NodeRef newRawMaterialNodeRef = checkOutCheckInService.checkin(workingCopyNodeRef, versionProperties);
				
				assertNotNull(newRawMaterialNodeRef);
				logger.info("### versionLabel: " + nodeService.getProperty(newRawMaterialNodeRef,ContentModel.PROP_VERSION_LABEL));
				
				return null;
			}
		}, false, true);
	}
	
}
