/*
 * 
 */
package fr.becpg.repo.listvalue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProduct;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.test.RepoBaseTestCase;

// TODO: Auto-generated Javadoc
/**
 * The Class ListValueServiceTest.
 * 
 * @author querephi
 */
public class CompoListValuePluginTest extends RepoBaseTestCase {

	/** The Constant PATH_TEMPFOLDER. */
	private static final String PATH_TEMPFOLDER = "TempFolder";

	/** The logger. */
	private static Log logger = LogFactory.getLog(CompoListValuePluginTest.class);

	private CompoListValuePlugin compoListValuePlugin;

	/** The file folder service. */
	private FileFolderService fileFolderService;

	/** The repository helper. */
	private Repository repositoryHelper;

	private NodeRef rawMaterial1NodeRef;
	private NodeRef rawMaterial2NodeRef;
	private NodeRef rawMaterial3NodeRef;
	private NodeRef localSF1NodeRef;
	private NodeRef localSF2NodeRef;
	private NodeRef finishedProductNodeRef;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.util.BaseAlfrescoTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		fileFolderService = (FileFolderService) ctx.getBean("FileFolderService");
		repoService = (RepoService) ctx.getBean("repoService");
		repositoryHelper = (Repository) ctx.getBean("repositoryHelper");
		compoListValuePlugin = (CompoListValuePlugin) ctx.getBean("compoListValuePlugin");

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				initProducts();

				return null;

			}
		}, false, true);
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

	public void initProducts() {

		logger.debug("look for temp folder");
		NodeRef folderNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(),
				ContentModel.ASSOC_CONTAINS, PATH_TEMPFOLDER);
		if (folderNodeRef != null) {
			fileFolderService.delete(folderNodeRef);
		}
		folderNodeRef = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_TEMPFOLDER,
				ContentModel.TYPE_FOLDER).getNodeRef();

		rawMaterial1NodeRef = createRawMaterial(folderNodeRef, "RM1");
		rawMaterial2NodeRef = createRawMaterial(folderNodeRef, "RM2");
		rawMaterial3NodeRef = createRawMaterial(folderNodeRef, "RM3");

		Collection<QName> dataLists = productDictionaryService.getDataLists();

		/*-- Local semi finished product 1 --*/
		LocalSemiFinishedProduct localSF1 = new LocalSemiFinishedProduct();
		localSF1.setName("Local semi finished 1");
		localSF1.setLegalName("Legal Local semi finished 1");
		localSF1NodeRef = productDAO.create(folderNodeRef, localSF1, dataLists);

		/*-- Local semi finished product 1 --*/
		LocalSemiFinishedProduct localSF2 = new LocalSemiFinishedProduct();
		localSF2.setName("Local semi finished 2");
		localSF2.setLegalName("Legal Local semi finished 2");
		localSF2NodeRef = productDAO.create(folderNodeRef, localSF2, dataLists);

		FinishedProductData finishedProduct = new FinishedProductData();
		finishedProduct.setName("Produit fini 1");
		finishedProduct.setLegalName("Legal Produit fini 1");
		finishedProduct.setUnit(ProductUnit.kg);
		finishedProduct.setQty(2d);
		finishedProduct.setUnitPrice(12.4d);
		List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
		compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DETAIL_FR,
				localSF1NodeRef));
		compoList.add(new CompoListDataItem(null, 2, 1d, 0d, 0d, CompoListUnit.kg, 0d, null,
				DeclarationType.DECLARE_FR, rawMaterial1NodeRef));
		compoList.add(new CompoListDataItem(null, 2, 2d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DETAIL_FR,
				rawMaterial2NodeRef));
		compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.DETAIL_FR,
				localSF2NodeRef));
		compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, null,
				DeclarationType.DECLARE_FR, rawMaterial3NodeRef));
		finishedProduct.setCompoList(compoList);

		finishedProductNodeRef = productDAO.create(folderNodeRef, finishedProduct, dataLists);
	}

	/**
	 * Test suggest supplier.
	 */
	public void testCompoListValuePlugin() {

		Map<String, Serializable> props = new HashMap<String, Serializable>();
		props.put(ListValueService.PROP_LOCALE, Locale.FRENCH);
		props.put(ListValueService.PROP_NODEREF, finishedProductNodeRef.toString());
		props.put(ListValueService.PROP_CLASS_NAME, "bcpg:compoList");

		ListValuePage listValuePage = compoListValuePlugin.suggest("compoListFather", "", null, new Integer(
				ListValueService.SUGGEST_PAGE_SIZE), props);

		for (ListValueEntry listValueEntry : listValuePage.getResults()) {
			logger.info("listValueEntry: " + listValueEntry.getName() + " - " + listValueEntry.getValue());
		}
	}

}
