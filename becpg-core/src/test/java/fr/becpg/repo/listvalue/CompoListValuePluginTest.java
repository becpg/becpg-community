/*
 * 
 */
package fr.becpg.repo.listvalue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.test.RepoBaseTestCase;

/**
 * The Class ListValueServiceTest.
 * 
 * @author querephi
 */
public class CompoListValuePluginTest extends RepoBaseTestCase {

	@Resource
	private CompoListValuePlugin compoListValuePlugin;

	private NodeRef rawMaterial1NodeRef;
	private NodeRef rawMaterial2NodeRef;
	private NodeRef rawMaterial3NodeRef;
	private NodeRef localSF1NodeRef;
	private NodeRef localSF2NodeRef;
	private NodeRef finishedProductNodeRef;

	@Override
	public void setUp() throws Exception {
		super.setUp();


		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				initProducts();

				return null;

			}
		}, false, true);
	}


	public void initProducts() {

		rawMaterial1NodeRef = createRawMaterial(testFolderNodeRef, "RM1");
		rawMaterial2NodeRef = createRawMaterial(testFolderNodeRef, "RM2");
		rawMaterial3NodeRef = createRawMaterial(testFolderNodeRef, "RM3");


		/*-- Local semi finished product 1 --*/
		LocalSemiFinishedProductData localSF1 = new LocalSemiFinishedProductData();
		localSF1.setName("Local semi finished 1");
		localSF1.setLegalName("Legal Local semi finished 1");
		localSF1NodeRef = alfrescoRepository.create(testFolderNodeRef, localSF1).getNodeRef();

		/*-- Local semi finished product 1 --*/
		LocalSemiFinishedProductData localSF2 = new LocalSemiFinishedProductData();
		localSF2.setName("Local semi finished 2");
		localSF2.setLegalName("Legal Local semi finished 2");
		localSF2NodeRef = alfrescoRepository.create(testFolderNodeRef, localSF2).getNodeRef();

		FinishedProductData finishedProduct = new FinishedProductData();
		finishedProduct.setName("Produit fini 1");
		finishedProduct.setLegalName("Legal Produit fini 1");
		finishedProduct.setUnit(ProductUnit.kg);
		finishedProduct.setQty(2d);
		finishedProduct.setUnitPrice(12.4d);
		List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
		CompoListDataItem item = new CompoListDataItem(null, (CompoListDataItem)null, 1d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Detail,
				localSF1NodeRef);
	
		compoList.add(item);
		compoList.add(new CompoListDataItem(null, item, 1d, 0d, 0d, CompoListUnit.kg, 0d, null,
				DeclarationType.Declare, rawMaterial1NodeRef));
		compoList.add(new CompoListDataItem(null, item, 2d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Detail,
				rawMaterial2NodeRef));
		item = new CompoListDataItem(null, (CompoListDataItem)null, 1d, 0d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Detail,
				localSF2NodeRef);

		compoList.add(item);
		compoList.add(new CompoListDataItem(null,item, 3d, 0d, 0d, CompoListUnit.kg, 0d, null,
				DeclarationType.Declare, rawMaterial3NodeRef));
		finishedProduct.getCompoListView().setCompoList(compoList);

		finishedProductNodeRef = alfrescoRepository.create(testFolderNodeRef, finishedProduct).getNodeRef();
	}

	/**
	 * Test suggest supplier.
	 */
	@Test
	public void testCompoListValuePlugin() {

		Map<String, Serializable> props = new HashMap<String, Serializable>();
		props.put(ListValueService.PROP_LOCALE, Locale.FRENCH);
		props.put(ListValueService.PROP_NODEREF, finishedProductNodeRef.toString());
		props.put(ListValueService.PROP_CLASS_NAME, "bcpg:compoList");

		ListValuePage listValuePage = compoListValuePlugin.suggest("compoListParentLevel", "", null, new Integer(
				ListValueService.SUGGEST_PAGE_SIZE), props);

//		for (ListValueEntry listValueEntry : listValuePage.getResults()) {
//			logger.info("listValueEntry: " + listValueEntry.getName() + " - " + listValueEntry.getValue());
//		}
		
		assertEquals(2, listValuePage.getResults().size());
		
		listValuePage = compoListValuePlugin.suggest("compoListParentLevel", "Local semi finished 2", null, new Integer(
				ListValueService.SUGGEST_PAGE_SIZE), props);

		assertEquals(1, listValuePage.getResults().size());
		
		// Check cycle detection (exclude localSF1NodeRef)
		ProductData finishedProduct = alfrescoRepository.findOne(finishedProductNodeRef);
		
		HashMap<String, String> extras = new HashMap<String, String>();
		extras.put("itemId", finishedProduct.getCompoListView().getCompoList().get(0).getNodeRef().toString());
		props.put(ListValueService.EXTRA_PARAM, extras);
		
		listValuePage = compoListValuePlugin.suggest("compoListParentLevel", "", null, new Integer(
				ListValueService.SUGGEST_PAGE_SIZE), props);

		assertEquals(1, listValuePage.getResults().size());
	}
	
	@Test
	public void testIsQueryMatch(){
		assertTrue(compoListValuePlugin.isQueryMath("*","Pâte de riz" ));
		assertTrue(compoListValuePlugin.isQueryMath("Pâte*","Pâte de riz" ));
		assertTrue(compoListValuePlugin.isQueryMath("Pâte","Pâte de riz" ));
		assertTrue(compoListValuePlugin.isQueryMath("Pâte*","Pate de riz" ));
		assertTrue(compoListValuePlugin.isQueryMath("Pâte*","Pates de riz" ));
		assertTrue(compoListValuePlugin.isQueryMath("Pâte*","pate de riz" ));
		assertTrue(compoListValuePlugin.isQueryMath("Pat*","Patisserie de riz" ));
		assertTrue(compoListValuePlugin.isQueryMath("Riz*","Pâte de riz" ));
		assertTrue(compoListValuePlugin.isQueryMath("Riz*","Riz au lait" ));
		assertFalse(compoListValuePlugin.isQueryMath("Pâto*","Patisserie" ));
		assertFalse(compoListValuePlugin.isQueryMath("Pâte*","DesPates" ));
		
	}

}
