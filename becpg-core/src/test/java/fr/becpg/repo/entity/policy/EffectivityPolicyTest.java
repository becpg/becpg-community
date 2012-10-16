/*
 * 
 */
package fr.becpg.repo.entity.policy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.repo.product.data.productList.PriceListDataItem;
import fr.becpg.test.RepoBaseTestCase;

/**
 * The Class EffectivityPolicyTest.
 * 
 * @author querephi
 */
public class EffectivityPolicyTest extends RepoBaseTestCase {

	/** The logger. */
	private static Log logger = LogFactory.getLog(EffectivityPolicyTest.class);

	/** The sf node ref. */
	private NodeRef sfNodeRef;

	private CopyService copyService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		logger.debug("EffectivityPolicyTest:setUp");

		copyService = (CopyService) ctx.getBean("copyService");

	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test supplier code.
	 */
	public void testEffectivity() {
		final Date start = new Date();
		final Date end = new Date();

		final Date nowplus1h = new Date(start.getTime() + 1000 * 60 * 60);
		final Date nowplus2h = new Date(start.getTime() + 2000 * 60 * 60);

		final Collection<QName> dataLists = new ArrayList<QName>();
		final Collection<QName> dataListSf = new ArrayList<QName>();
		dataLists.add(BeCPGModel.TYPE_PRICELIST);
		dataListSf.add(BeCPGModel.TYPE_COMPOLIST);

		sfNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*-- Create raw material --*/
				logger.debug("/*-- Create raw material --*/");

				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				// Costs
				properties.put(ContentModel.PROP_NAME, "cost1");
				properties.put(BeCPGModel.PROP_COSTCURRENCY, "€");
				NodeRef cost = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_COST, properties).getChildRef();

				PriceListDataItem priceListDataItem = new PriceListDataItem();
				priceListDataItem.setStartEffectivity(start);
				priceListDataItem.setEndEffectivity(end);
				priceListDataItem.setCost(cost);

				RawMaterialData rawMaterial1 = new RawMaterialData();
				rawMaterial1.setName("Raw material 1");
				rawMaterial1.setStartEffectivity(nowplus1h);
				rawMaterial1.setEndEffectivity(nowplus2h);

				List<PriceListDataItem> priceList = new LinkedList<PriceListDataItem>();
				priceList.add(priceListDataItem);
				rawMaterial1.setPriceList(priceList);

				NodeRef rawMaterialNodeRef = productDAO.create(testFolderNodeRef, rawMaterial1, dataLists);

				// load SF and test it
				rawMaterial1 = (RawMaterialData) productDAO.find(rawMaterialNodeRef, dataLists);
				assertNotNull("check priceList", rawMaterial1.getPriceList());

				for (PriceListDataItem p : rawMaterial1.getPriceList()) {

					assertEquals(start.getTime(), p.getStartEffectivity().getTime());
					assertEquals(end.getTime(), p.getEndEffectivity().getTime());
				}

				return rawMaterialNodeRef;

			}
		}, false, true);

		final NodeRef fp1NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				FinishedProductData fp1 = new FinishedProductData();
				fp1.setName("FP 1");

				List<CompoListDataItem> compoList = new LinkedList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, 1d, 1d, 0d, CompoListUnit.P, 0d, null, DeclarationType.Declare, sfNodeRef));

				fp1.setCompoList(compoList);

				return productDAO.create(testFolderNodeRef, fp1, dataListSf);

			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				FinishedProductData fp1 = (FinishedProductData) productDAO.find(fp1NodeRef, dataListSf);
				assertNotNull("check compoList", fp1.getCompoList());

				for (CompoListDataItem p : fp1.getCompoList()) {

					System.out.println("PWet:" + p.getProduct());

					assertEquals(nowplus1h.getTime(), p.getStartEffectivity().getTime());
					assertEquals(nowplus2h.getTime(), p.getEndEffectivity().getTime());
				}
				return null;
			}
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				NodeRef rawMaterialNodeRef = copyService.copyAndRename(sfNodeRef, testFolderNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN, true);

				RawMaterialData rawMaterial1 = (RawMaterialData) productDAO.find(rawMaterialNodeRef, dataLists);
				assertNotNull("check priceList", rawMaterial1.getPriceList());

				for (PriceListDataItem p : rawMaterial1.getPriceList()) {

					assertEquals(start.getTime(), p.getStartEffectivity().getTime());
					assertEquals(end.getTime(), p.getEndEffectivity().getTime());
				}

				return rawMaterialNodeRef;
			}
		}, false, true);

	}

}
