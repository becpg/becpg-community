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

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.PLMBaseTestCase;

/**
 *
 * @author matthieu
 *
 */
public class AlfrescoRepositoryIT extends PLMBaseTestCase {

	private static final Log logger = LogFactory.getLog(AlfrescoRepositoryIT.class);

	@Autowired
	@Qualifier("mlAwareNodeService")
	private NodeService mlNodeServiceImpl;

	@Autowired
	private EntityListDAO entityListDAO;

	/**
	 * Test allergen list dao.
	 */
	@Test
	public void testAllergenListDAO() {

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

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
			allergenList.add(new AllergenListDataItem(null, null, true, true, allSources, null, allergens.get(0), false));
			allergenList.add(new AllergenListDataItem(null, null, false, true, null, allSources, allergens.get(1), false));
			allergenList.add(new AllergenListDataItem(null, null, true, false, null, allSources, allergens.get(2), false));
			allergenList.add(new AllergenListDataItem(null, null, false, false, allSources, null, allergens.get(3), false));
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

		}, false, true);

	}

	/**
	 * Test get link.
	 */
	@Test
	public void testGetListItem() {

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

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

		}, false, true);

	}

	@Test
	public void testNullProperties() {
		inWriteTx(() -> {

			NodeRef rawMaterialNodeRef = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test properties");
			ProductData rawMaterial = alfrescoRepository.findOne(rawMaterialNodeRef);

			Assert.assertFalse(rawMaterial.getAspects().contains(PLMModel.ASPECT_NUTRIENT_PROFILING_SCORE));

			MLText title = new MLText();
			title.addValue(Locale.getDefault(), "Test update property");

			rawMaterial.setTitle(title);

			alfrescoRepository.save(rawMaterial);

			Assert.assertFalse(rawMaterial.getAspects().contains(PLMModel.ASPECT_NUTRIENT_PROFILING_SCORE));

			rawMaterial.setNutrientClass("A");

			alfrescoRepository.save(rawMaterial);

			Assert.assertTrue(rawMaterial.getAspects().contains(PLMModel.ASPECT_NUTRIENT_PROFILING_SCORE));

			return true;

		});

	}

	/**
	 * Test ing labeling list.
	 */
	@Test
	public void testIngLabelingList() {

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			// create node
			MLText mlTextILL = new MLText();
			mlTextILL.addValue(Locale.ENGLISH, "English value");
			mlTextILL.addValue(Locale.FRENCH, "French value");

			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(PLMModel.PROP_ILL_VALUE, mlTextILL);

			NodeRef illNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN,
					PLMModel.TYPE_INGLABELINGLIST, properties).getChildRef();

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

		}, false, true);

	}

	@Test
	public void test16958() {
		MLTextHelper.setSupportedLocales(
				"ar, ar_DZ, ar_EG, ar_MA, ar_SA, ar_JO, bg, bn_BD, cs, en, en_US, en_PH, en_AU, en_ZA, da_DK, de, de_AT, el, el_CY, es, fi, fr, fr_CA, hi_IN, hr_HR, hu, it, iw_IL, lt, lv, ja_JP, ko_KR, ms_MY, nl, no, ro, ru, pl, pt, pt_BR, sk, sl_SI, sr_RS, sv_SE, th, tr, ur_PK, vi_VN, zh_CN, mt_MT, et_EE, in_ID, ne_NP)");

		Locale defaultLocale = Locale.getDefault();


		Locale.setDefault(MLTextHelper.parseLocale("en_GB"));
		I18NUtil.setLocale(Locale.getDefault());
		I18NUtil.setContentLocale(null);

		MLText mlTextILL = new MLText();
		mlTextILL.addValue(MLTextHelper.parseLocale("en"), "keep in original unopened packaging stored well closed in a cool dry place.");
		mlTextILL.addValue(MLTextHelper.parseLocale("ar_EG"), "يحفظ في العبوة الأصلية غير المفتوحة المخزنة جيدًا في مكان بارد وجاف.");
		mlTextILL.addValue(MLTextHelper.parseLocale("en_US"), "keep in original u./ru	nopened packaging stored well closed in a cool dry place.");
		

		
		MLTextHelper.getClosestValue(mlTextILL, MLTextHelper.parseLocale("en"));

		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(PLMModel.PROP_ILL_VALUE, mlTextILL);

		NodeRef illNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN,
				PLMModel.TYPE_INGLABELINGLIST, properties).getChildRef();

		nodeService.setProperty(illNodeRef, PLMModel.PROP_ILL_VALUE, "TEST");
		
		
		logger.debug("get property : " + mlNodeServiceImpl.getProperty(illNodeRef, PLMModel.PROP_ILL_VALUE));
		logger.debug("get property en : " + nodeService.getProperty(illNodeRef, PLMModel.PROP_ILL_VALUE));
		logger.debug("get properties : " + mlNodeServiceImpl.getProperties(illNodeRef));
		logger.debug("get property 2 : " + mlNodeServiceImpl.getProperties(illNodeRef).get(PLMModel.PROP_ILL_VALUE));

		String toSave = (String) nodeService.getProperty(illNodeRef, PLMModel.PROP_ILL_VALUE);
		nodeService.setProperty(illNodeRef, PLMModel.PROP_ILL_VALUE, toSave);
		MLText mlTextILLSaved = (MLText) mlNodeServiceImpl.getProperty(illNodeRef, PLMModel.PROP_ILL_VALUE);

		logger.debug(" Locale.getDefault(): " +  Locale.getDefault());
		logger.debug(" getClosestValue: " +  MLTextHelper.getClosestValue(mlTextILLSaved, I18NUtil.getContentLocale()));
		logger.debug(" getValueOrDefault: " +  MLTextHelper.getValueOrDefault(nodeService,illNodeRef, PLMModel.PROP_ILL_VALUE));
		

		logger.debug("get property : " + mlNodeServiceImpl.getProperty(illNodeRef, PLMModel.PROP_ILL_VALUE));

		
		
	
		assertNotNull("MLText exist", mlTextILLSaved);
		assertEquals("MLText exist has 3 Locales", 3, mlTextILL.getLocales().size());
		assertEquals("Check english value", mlTextILL.getValue(MLTextHelper.parseLocale("ar_EG")),
				mlTextILLSaved.getValue(MLTextHelper.parseLocale("ar_EG")));
		assertEquals("Check ar value", mlTextILL.getValue(MLTextHelper.parseLocale("en")), mlTextILLSaved.getValue(MLTextHelper.parseLocale("en")));
		assertEquals("Check en_US value", mlTextILL.getValue(MLTextHelper.parseLocale("en_US")),
				mlTextILLSaved.getValue(MLTextHelper.parseLocale("en_US")));

		Locale.setDefault(defaultLocale);
		MLTextHelper.flushCache();

	}

}
