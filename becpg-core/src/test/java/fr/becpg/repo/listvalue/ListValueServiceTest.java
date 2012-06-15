/*
 * 
 */
package fr.becpg.repo.listvalue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseAlfrescoTestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;

// TODO: Auto-generated Javadoc
/**
 * The Class ListValueServiceTest.
 * 
 * @author querephi
 */
public class ListValueServiceTest extends BaseAlfrescoTestCase {

	/** The Constant PATH_TEMPFOLDER. */
	private static final String PATH_TEMPFOLDER = "TempFolder";

	/** The logger. */
	private static Log logger = LogFactory.getLog(ListValueServiceTest.class);

	/** The list value service. */
	private EntityListValuePlugin entityListValuePlugin;

	/** The file folder service. */
	private FileFolderService fileFolderService;

	/** The repository helper. */
	private Repository repositoryHelper;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.util.BaseAlfrescoTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		entityListValuePlugin = (EntityListValuePlugin) ctx.getBean("entityListValuePlugin");
		fileFolderService = (FileFolderService) ctx.getBean("FileFolderService");
		repositoryHelper = (Repository) ctx.getBean("repositoryHelper");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.util.BaseAlfrescoTestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		try {
			authenticationComponent.clearCurrentSecurityContext();
		} catch (Throwable e) {
			e.printStackTrace();
			// Don't let this mask any previous exceptions
		}
		super.tearDown();

	}

	/**
	 * Test suggest supplier.
	 */
	public void testSuggestSupplier() {

		logger.debug("look for temp folder");
		NodeRef tempFolder = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TEMPFOLDER);
		if (tempFolder != null) {
			fileFolderService.delete(tempFolder);
		}
		tempFolder = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_TEMPFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();

		// Create supplier 1 with allowed constraint
		logger.debug("create temp supplier 1");
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, "Supplier 1");
		NodeRef supplierNodeRef = nodeService.createNode(tempFolder, ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_SUPPLIER, properties).getChildRef();

		String[] arrClassNames = { "bcpg:supplier" };
		List<ListValueEntry> suggestions = entityListValuePlugin.suggestTargetAssoc(BeCPGModel.TYPE_SUPPLIER, "Supplier 1", 0, 10, arrClassNames).getResults();

		boolean containsSupplier = false;
		for (ListValueEntry s : suggestions) {
			logger.debug("supplier: " + s.getName());
			if (s.getValue().equals(supplierNodeRef.toString()) && s.getName().equals("Supplier 1")) {
				containsSupplier = true;
			}
		}

		assertEquals("1 suggestion", 1, suggestions.size());
		assertTrue("check supplier key", containsSupplier);

		// filter by client : no results
		String[] arrClassNames2 = { "bcpg:client" };
		suggestions = entityListValuePlugin.suggestTargetAssoc(BeCPGModel.TYPE_SUPPLIER, "Supplier 1", 0, 10, arrClassNames2).getResults();

		assertEquals("0 suggestion", 0, suggestions.size());
	}

}
