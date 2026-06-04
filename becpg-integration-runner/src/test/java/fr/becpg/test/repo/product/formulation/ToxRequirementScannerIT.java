package fr.becpg.test.repo.product.formulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.ToxListDataItem;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.regulatory.RequirementType;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.model.ToxType;
import fr.becpg.test.PLMBaseTestCase;

public class ToxRequirementScannerIT extends PLMBaseTestCase {

	@Autowired
	protected ProductService productService;

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	@Autowired
	protected Repository repositoryHelper;

	private NodeRef toxNodeRef;
	private NodeRef otherToxNodeRef;
	private NodeRef ingNodeRef;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();
		NodeRef systemNodeRef = repoService.getFolderByPath(companyHomeNodeRef, RepoConsts.PATH_SYSTEM);
		NodeRef charactsNodeRef = repoService.getFolderByPath(systemNodeRef, RepoConsts.PATH_CHARACTS);
		NodeRef listContainer = nodeService.getChildByName(charactsNodeRef, BeCPGModel.ASSOC_ENTITYLISTS, RepoConsts.CONTAINER_DATALISTS);
		NodeRef toxFolder = nodeService.getChildByName(listContainer, ContentModel.ASSOC_CONTAINS, PlmRepoConsts.PATH_TOXICITIES);

		inWriteTx(() -> {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "Tox Test 1");
			properties.put(PLMModel.PROP_TOX_CALCULATE_SYSTEMIC, true);
			properties.put(PLMModel.PROP_TOX_CALCULATE_MAX, true);
			properties.put(PLMModel.PROP_TOX_VALUE, 100);
			properties.put(PLMModel.PROP_TOX_TYPES, new ArrayList<>(List.of(ToxType.SkinIrritationRinseOff, ToxType.Sensitization, ToxType.SystemicIngredient)));
			toxNodeRef = nodeService.createNode(toxFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "ToxTest1"),
					PLMModel.TYPE_TOX, properties).getChildRef();

			properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "Tox Test 2");
			properties.put(PLMModel.PROP_TOX_CALCULATE_SYSTEMIC, true);
			properties.put(PLMModel.PROP_TOX_CALCULATE_MAX, true);
			properties.put(PLMModel.PROP_TOX_VALUE, 100);
			properties.put(PLMModel.PROP_TOX_TYPES, new ArrayList<>(List.of(ToxType.SkinIrritationRinseOff, ToxType.Sensitization, ToxType.SystemicIngredient)));
			otherToxNodeRef = nodeService.createNode(toxFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "ToxTest2"),
					PLMModel.TYPE_TOX, properties).getChildRef();

			properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "Ing Test");
			properties.put(PLMModel.PROP_ING_TOX_DATA, true);
			properties.put(PLMModel.PROP_ING_TOX_POD_SYSTEMIC, 1000);
			properties.put(PLMModel.PROP_ING_TOX_DERMAL_ABSORPTION, 10);
			properties.put(PLMModel.PROP_ING_TOX_MOS_MOE, 100);
			properties.put(PLMModel.PROP_ING_TOX_MAX_SKIN_IRRITATION_RINSE_OFF, 20); // 20% limit for Tox Test 1
			properties.put(PLMModel.PROP_ING_TOX_MAX_SENSITIZATION, 80); // 80% limit for Tox Test 2
			ingNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "IngTest"),
					PLMModel.TYPE_ING, properties).getChildRef();

			return null;
		});
	}

	@Test
	public void testToxRequirementScanner() {
		FinishedProductData product = inWriteTx(() -> {
			FinishedProductData productData = new FinishedProductData();
			productData.setName("Product test tox scanner");

			// Add 2 toxes to the product
			List<ToxListDataItem> toxList = new ArrayList<>();
			ToxListDataItem tox1 = new ToxListDataItem();
			tox1.setTox(toxNodeRef);
			toxList.add(tox1);

			ToxListDataItem tox2 = new ToxListDataItem();
			tox2.setTox(otherToxNodeRef);
			toxList.add(tox2);

			productData.setToxList(toxList);

			// Add ingredients (total 100%)
			List<IngListDataItem> ingList = new ArrayList<>();
			IngListDataItem ing1 = new IngListDataItem();
			ing1.setIng(ingNodeRef);
			ing1.setQtyPerc(50.0); // We put 50% ingredient
			ingList.add(ing1);
			productData.setIngList(ingList);

			productData = (FinishedProductData) alfrescoRepository.create(getTestFolderNodeRef(), productData);

			// Specifications: Only require Tox 1, with a maximum threshold of 30%
			ProductSpecificationData spec = new ProductSpecificationData();
			spec.setName("Spec limit Tox 1");
			List<ToxListDataItem> specToxList = new ArrayList<>();
			ToxListDataItem specTox1 = new ToxListDataItem();
			specTox1.setTox(toxNodeRef);
			specTox1.setMaxi(30.0); // Set max limit to 30.0 so that value=40.0 exceeds 30.0 and triggers an alert
			specToxList.add(specTox1);
			spec.setToxList(specToxList);

			spec = (ProductSpecificationData) alfrescoRepository.create(getTestFolderNodeRef(), spec);

			if (!nodeService.hasAspect(productData.getNodeRef(), PLMModel.ASPECT_TRANSFORMATION)) {
				nodeService.addAspect(productData.getNodeRef(), PLMModel.ASPECT_TRANSFORMATION, null);
			}
			nodeService.createAssociation(productData.getNodeRef(), spec.getNodeRef(), PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);

			return productData;
		});

		// Formulate the product
		inWriteTx(() -> {
			productService.formulate(product.getNodeRef());
			return null;
		});

		inReadTx(() -> {
			FinishedProductData formulated = (FinishedProductData) alfrescoRepository.findOne(product.getNodeRef());

			// Verify that the toxList contains both toxes (we do NOT delete anymore, they are filtered by raising alerts)
			Assert.assertNotNull(formulated.getToxList());
			Assert.assertEquals(2, formulated.getToxList().size());

			// Tox Test 1 maximum allowed relative quantity is calculated as: maxQuantity * 100 / qtyPerc
			// maxQuantity = 20%, qtyPerc = 50%
			// Value = 20 * 100 / 50 = 40.0% (which exceeds 30.0%)
			Assert.assertEquals(40.0, formulated.getToxList().get(0).getValue(), 0.01);

			// Check that requirement controls have alerts
			Assert.assertNotNull(formulated.getReqCtrlList());
			Assert.assertFalse(formulated.getReqCtrlList().isEmpty());

			boolean foundRangeAlert = false;
			for (RequirementListDataItem reqCtrl : formulated.getReqCtrlList()) {
				if (toxNodeRef.equals(reqCtrl.getCharact())) {
					Assert.assertEquals(RequirementType.Forbidden, reqCtrl.getReqType());
					Assert.assertNotNull(reqCtrl.getReqMlMessage());
					String message = reqCtrl.getReqMlMessage().getDefaultValue();
					Assert.assertTrue("Message should contain actual value '40%'", message.contains("40"));
					Assert.assertTrue("Message should contain expected limit '30%'", message.contains("30"));
					foundRangeAlert = true;
				}
			}
			Assert.assertTrue("Should find a requirement control alert for the exceeded tox", foundRangeAlert);

			return null;
		});
	}

	@Test
	public void testToxRequirementScannerAuthorizedMode() {
		FinishedProductData product = inWriteTx(() -> {
			FinishedProductData productData = new FinishedProductData();
			productData.setName("Product test tox authorized scanner");

			// Add 2 toxes to the product
			List<ToxListDataItem> toxList = new ArrayList<>();
			ToxListDataItem tox1 = new ToxListDataItem();
			tox1.setTox(toxNodeRef);
			toxList.add(tox1);

			ToxListDataItem tox2 = new ToxListDataItem();
			tox2.setTox(otherToxNodeRef);
			toxList.add(tox2);

			productData.setToxList(toxList);

			// Add ingredients (total 100%)
			List<IngListDataItem> ingList = new ArrayList<>();
			IngListDataItem ing1 = new IngListDataItem();
			ing1.setIng(ingNodeRef);
			ing1.setQtyPerc(50.0);
			ingList.add(ing1);
			productData.setIngList(ingList);

			productData = (FinishedProductData) alfrescoRepository.create(getTestFolderNodeRef(), productData);

			// Specifications: Only authorize Tox 1
			ProductSpecificationData spec = new ProductSpecificationData();
			spec.setName("Spec authorize Tox 1");
			List<ToxListDataItem> specToxList = new ArrayList<>();
			ToxListDataItem specTox1 = new ToxListDataItem();
			specTox1.setTox(toxNodeRef);
			specTox1.setRegulatoryType(RequirementType.Authorized); // Mark as Authorized/Whitelisted!
			specToxList.add(specTox1);
			spec.setToxList(specToxList);

			spec = (ProductSpecificationData) alfrescoRepository.create(getTestFolderNodeRef(), spec);

			if (!nodeService.hasAspect(productData.getNodeRef(), PLMModel.ASPECT_TRANSFORMATION)) {
				nodeService.addAspect(productData.getNodeRef(), PLMModel.ASPECT_TRANSFORMATION, null);
			}
			nodeService.createAssociation(productData.getNodeRef(), spec.getNodeRef(), PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);

			return productData;
		});

		// Formulate the product
		inWriteTx(() -> {
			productService.formulate(product.getNodeRef());
			return null;
		});

		inReadTx(() -> {
			FinishedProductData formulated = (FinishedProductData) alfrescoRepository.findOne(product.getNodeRef());

			// In Authorized/Whitelist mode, we do NOT filter out unauthorized toxes from the product list (they remain so we can show the alert)
			Assert.assertNotNull(formulated.getToxList());
			Assert.assertEquals(2, formulated.getToxList().size());

			// Check that we got a Forbidden alert for Tox 2 (since only Tox 1 is authorized)
			Assert.assertNotNull(formulated.getReqCtrlList());
			Assert.assertFalse(formulated.getReqCtrlList().isEmpty());

			boolean foundAuthorizedToxAlert = false;
			boolean foundUnauthorizedToxAlert = false;

			for (RequirementListDataItem reqCtrl : formulated.getReqCtrlList()) {
				if (toxNodeRef.equals(reqCtrl.getCharact())) {
					// Tox 1 is authorized and its value (40) is within standard limit (100), so no range alert should be present
					foundAuthorizedToxAlert = true;
				} else if (otherToxNodeRef.equals(reqCtrl.getCharact())) {
					// Tox 2 is NOT authorized, so we expect a Forbidden alert
					Assert.assertEquals(RequirementType.Forbidden, reqCtrl.getReqType());
					Assert.assertNotNull(reqCtrl.getReqMlMessage());
					String message = reqCtrl.getReqMlMessage().getDefaultValue();
					Assert.assertTrue(message.contains("not authorized") || message.contains("autoris"));
					foundUnauthorizedToxAlert = true;
				}
			}

			Assert.assertFalse("Should not raise range alert for the authorized tox 1", foundAuthorizedToxAlert);
			Assert.assertTrue("Should raise an unauthorized alert for tox 2", foundUnauthorizedToxAlert);

			return null;
		});
	}

}
