/*
 *
 */
package fr.becpg.test.repo.web.scripts.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.PLMBaseTestCase;
import fr.becpg.test.utils.TestWebscriptExecuters;
import fr.becpg.test.utils.TestWebscriptExecuters.GetRequest;
import fr.becpg.test.utils.TestWebscriptExecuters.Response;

/**
 * The Class VersionHistoryWebScriptTest.
 *
 * @author querephi
 */
public class EntityVersionWebScriptIT extends PLMBaseTestCase {

	/** The logger. */
	private static final Log logger = LogFactory.getLog(EntityVersionWebScriptIT.class);

	@Resource
	private CheckOutCheckInService checkOutCheckInService;

	@Test
	public void testGetVersionHistory() throws Exception {

		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper()
				.doInTransaction(() -> BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test report"), false, true);

		if (!nodeService.hasAspect(rawMaterialNodeRef, ContentModel.ASPECT_VERSIONABLE)) {
			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
				logger.debug("Add versionnable aspect");
				Map<QName, Serializable> aspectProperties = new HashMap<>();
				aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
				nodeService.addAspect(rawMaterialNodeRef, ContentModel.ASPECT_VERSIONABLE, aspectProperties);
				return rawMaterialNodeRef;
			}, false, true);

		}

		final NodeRef checkedOutNodeRef = transactionService.getRetryingTransactionHelper()
				.doInTransaction(() -> checkOutCheckInService.checkout(rawMaterialNodeRef), false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			Map<String, Serializable> versionProperties = new HashMap<>();
			versionProperties.put(Version.PROP_DESCRIPTION, "This is a test version");
			versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE, VersionType.MAJOR);

			NodeRef checkedInNodeRef = checkOutCheckInService.checkin(checkedOutNodeRef, versionProperties);

			NodeRef checkedOutNodeRef2 = checkOutCheckInService.checkout(checkedInNodeRef);
			checkOutCheckInService.checkin(checkedOutNodeRef2, null);

			return null;

		}, false, true);

		// Call webscript on raw material to check out
		String url = "/api/version?nodeRef=" + rawMaterialNodeRef;
		logger.debug("url : " + url);

		Response response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");
		logger.debug("version history: " + response.getContentAsString());

	}

}
