/*
 * 
 */
package fr.becpg.test.repo.product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.PLMBaseTestCase;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductDAOTest.
 * 
 * @author querephi
 */
public class AlfrescoRepositoryTest extends PLMBaseTestCase {

	/** The logger. */
	private static final Log logger = LogFactory.getLog(AlfrescoRepositoryTest.class);

	/** The ml node service impl. */
	@Resource(name = "mlAwareNodeService")
	private NodeService mlNodeServiceImpl;

	@Resource
	private EntityListDAO entityListDAO;

	/**
	 * Test allergen list dao.
	 */
	@Test
	public void testAllergenListDAO() {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				// create RM
				RawMaterialData rmData = new RawMaterialData();
				rmData.setName("RM");
				NodeRef rmNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rmData).getNodeRef();

				// create SF
				SemiFinishedProductData sfData = new SemiFinishedProductData();
				sfData.setName("SF");
				List<NodeRef> allSources = new ArrayList<>();
				allSources.add(rmNodeRef);
				List<AllergenListDataItem> allergenList = new ArrayList<>();
				allergenList.add(new AllergenListDataItem(null,null, true, true, allSources, null, allergens.get(0), false));
				allergenList.add(new AllergenListDataItem(null,null, false, true, null, allSources, allergens.get(1), false));
				allergenList.add(new AllergenListDataItem(null,null, true, false, null, allSources, allergens.get(2), false));
				allergenList.add(new AllergenListDataItem(null,null, false, false, allSources, null, allergens.get(3), false));
				sfData.setAllergenList(allergenList);

				NodeRef sfNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), sfData).getNodeRef();

				// load SF and test it
				sfData = (SemiFinishedProductData) alfrescoRepository.findOne(sfNodeRef);
				assertNotNull("check allergenList", sfData.getAllergenList());
				assertFalse(nodeService.hasAspect(sfData.getAllergenList().get(0).getNodeRef(), BeCPGModel.ASPECT_ENTITYLISTS));

				for (AllergenListDataItem d : sfData.getAllergenList()) {

					if (d.getAllergen().equals(allergens.get(0))) {
						assertEquals(true, d.getVoluntary().booleanValue());
						assertEquals(true, d.getInVoluntary().booleanValue());
						assertEquals(1, d.getVoluntarySources().size());
						assertEquals(rmNodeRef, d.getVoluntarySources().get(0));
						assertEquals(true, d.getInVoluntarySources().isEmpty());
					}

					if (d.getAllergen().equals(allergens.get(1))) {
						assertEquals(false, d.getVoluntary().booleanValue());
						assertEquals(true, d.getInVoluntary().booleanValue());
						assertEquals(true, d.getVoluntarySources().isEmpty());
						assertEquals(1, d.getInVoluntarySources().size());
						assertEquals(rmNodeRef, d.getInVoluntarySources().get(0));
					}

					if (d.getAllergen().equals(allergens.get(2))) {
						assertEquals(true, d.getVoluntary().booleanValue());
						assertEquals(false, d.getInVoluntary().booleanValue());
						assertEquals(true, d.getVoluntarySources().isEmpty());
						assertEquals(1, d.getInVoluntarySources().size());
						assertEquals(rmNodeRef, d.getInVoluntarySources().get(0));
					}

					if (d.getAllergen().equals(allergens.get(3))) {
						assertEquals(false, d.getVoluntary().booleanValue());
						assertEquals(false, d.getInVoluntary().booleanValue());
						assertEquals(1, d.getVoluntarySources().size());
						assertEquals(rmNodeRef, d.getVoluntarySources().get(0));
						assertEquals(true, d.getInVoluntarySources().isEmpty());
					}
				}

				return null;

			}
		}, false, true);

	}

	/**
	 * Test get link.
	 */
	@Test
	public void testGetListItem() {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				NodeRef rawMaterialNodeRef = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test report");
				ProductData rawMaterial = alfrescoRepository.findOne(rawMaterialNodeRef);

				NodeRef costNodeRef = costs.get(3);

				NodeRef costListDataItemNodeRef = null;

				for (CostListDataItem c : rawMaterial.getCostList()) {
					if (costNodeRef.equals(c.getCost())) {
						costListDataItemNodeRef = c.getNodeRef();
					}
				}

				NodeRef listContainerNodeRef = entityListDAO.getListContainer(rawMaterialNodeRef);
				NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, PLMModel.TYPE_COSTLIST);
				NodeRef nodeRef = entityListDAO.getListItem(listNodeRef, PLMModel.ASSOC_COSTLIST_COST, costNodeRef);

				assertEquals("Cost list data item should be the same", costListDataItemNodeRef, nodeRef);

				return null;

			}
		}, false, true);

	}

	/**
	 * Test ing labeling list.
	 */
	@Test
	public void testIngLabelingList() {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				// create node
				MLText mlTextILL = new MLText();
				mlTextILL.addValue(Locale.ENGLISH, "English value");
				mlTextILL.addValue(Locale.FRENCH, "French value");

				Map<QName, Serializable> properties = new HashMap<>();
				properties.put(PLMModel.PROP_ILL_VALUE, mlTextILL);

				NodeRef illNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN, PLMModel.TYPE_INGLABELINGLIST,
						properties).getChildRef();

				nodeService.setProperty(illNodeRef, PLMModel.PROP_ILL_VALUE, mlTextILL);

				// check node saved
				logger.debug("get property : " + mlNodeServiceImpl.getProperty(illNodeRef, PLMModel.PROP_ILL_VALUE));
				logger.debug("get property fr : " + mlNodeServiceImpl.getProperty(illNodeRef, QName.createQName(BeCPGModel.BECPG_PREFIX, "illValue_fr")));
				logger.debug("get properties : " + mlNodeServiceImpl.getProperties(illNodeRef));
				logger.debug("get property 2 : " + mlNodeServiceImpl.getProperties(illNodeRef).get(PLMModel.PROP_ILL_VALUE));
				MLText mlTextILLSaved = (MLText) mlNodeServiceImpl.getProperty(illNodeRef, PLMModel.PROP_ILL_VALUE);

				assertNotNull("MLText exist", mlTextILLSaved);
				assertEquals("MLText exist has 2 Locales", 2, mlTextILL.getLocales().size());
				assertEquals("Check english value", mlTextILL.getValue(Locale.ENGLISH), mlTextILLSaved.getValue(Locale.ENGLISH));
				assertEquals("Check french value", mlTextILL.getValue(Locale.FRENCH), mlTextILLSaved.getValue(Locale.FRENCH));

				return null;

			}
		}, false, true);

	}

}
