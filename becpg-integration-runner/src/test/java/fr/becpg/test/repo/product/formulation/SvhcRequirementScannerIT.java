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
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.SvhcListDataItem;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.regulatory.RequirementType;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.test.PLMBaseTestCase;

public class SvhcRequirementScannerIT extends PLMBaseTestCase {

	@Autowired
	protected ProductService productService;

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	@Autowired
	protected Repository repositoryHelper;

	private NodeRef ingNodeRef;
	private NodeRef otherIngNodeRef;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		inWriteTx(() -> {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "Substance Test 1");
			ingNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "SubstanceTest1"),
					PLMModel.TYPE_ING, properties).getChildRef();
			nodeService.addAspect(ingNodeRef, QName.createQName(BeCPGModel.BECPG_URI, "svhcAspect"), null);
			nodeService.setProperty(ingNodeRef, PLMModel.PROP_IS_SVHC, true);

			properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "Substance Test 2");
			otherIngNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "SubstanceTest2"),
					PLMModel.TYPE_ING, properties).getChildRef();
			nodeService.addAspect(otherIngNodeRef, QName.createQName(BeCPGModel.BECPG_URI, "svhcAspect"), null);
			nodeService.setProperty(otherIngNodeRef, PLMModel.PROP_IS_SVHC, true);

			return null;
		});
	}

	@Test
	public void testSvhcRequirementScanner() {
		RawMaterialData product = inWriteTx(() -> {
			RawMaterialData productData = new RawMaterialData();
			productData.setName("Product test svhc scanner");

			List<IngListDataItem> ingList = new ArrayList<>();
			IngListDataItem ing1 = new IngListDataItem();
			ing1.setIng(ingNodeRef);
			ing1.setQtyPerc(40.0);
			ingList.add(ing1);

			IngListDataItem ing2 = new IngListDataItem();
			ing2.setIng(otherIngNodeRef);
			ing2.setQtyPerc(10.0);
			ingList.add(ing2);

			productData.setIngList(ingList);
			productData.setSvhcList(new ArrayList<>());

			productData = (RawMaterialData) alfrescoRepository.create(getTestFolderNodeRef(), productData);

			ProductSpecificationData spec = new ProductSpecificationData();
			spec.setName("Spec limit Svhc 1");
			List<SvhcListDataItem> specSvhcList = new ArrayList<>();
			SvhcListDataItem specSvhc1 = new SvhcListDataItem();
			specSvhc1.setIng(ingNodeRef);
			specSvhc1.setQtyPerc(30.0);
			specSvhcList.add(specSvhc1);
			spec.setSvhcList(specSvhcList);

			spec = (ProductSpecificationData) alfrescoRepository.create(getTestFolderNodeRef(), spec);

			System.out.println("CREATED SPEC - name: " + spec.getName() + ", nodeRef: " + spec.getNodeRef());
			if (spec.getSvhcList() != null) {
				System.out.println("CREATED SPEC - svhcList size: " + spec.getSvhcList().size());
				for (SvhcListDataItem item : spec.getSvhcList()) {
					System.out.println("CREATED SPEC - item nodeRef: " + item.getNodeRef());
					System.out.println("CREATED SPEC - item qtyPerc: " + item.getQtyPerc());
					System.out.println("CREATED SPEC - item properties from nodeService: " + nodeService.getProperties(item.getNodeRef()));
				}
			}

			if (!nodeService.hasAspect(productData.getNodeRef(), PLMModel.ASPECT_TRANSFORMATION)) {
				nodeService.addAspect(productData.getNodeRef(), PLMModel.ASPECT_TRANSFORMATION, null);
			}
			nodeService.createAssociation(productData.getNodeRef(), spec.getNodeRef(), PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);

			return productData;
		});

		inReadTx(() -> {
			RawMaterialData p = (RawMaterialData) alfrescoRepository.findOne(product.getNodeRef());
			System.out.println("BEFORE FORMULATE - SVHC List size: " + (p.getSvhcList() != null ? p.getSvhcList().size() : "null"));
			if (p.getIngList() != null) {
				for (IngListDataItem ing : p.getIngList()) {
					IngItem ingItem = (IngItem) alfrescoRepository.findOne(ing.getIng());
					System.out.println("BEFORE FORMULATE - Ingredient: " + ingItem.getCharactName() + ", isSVHC: " + ingItem.getIsSubstanceOfVeryHighConcern());
				}
			}
			if (p.getSvhcList() != null) {
				for (SvhcListDataItem item : p.getSvhcList()) {
					System.out.println("BEFORE FORMULATE - Item: " + item.getIng() + ", isManual: " + item.getIsManual() + ", aspects: " + item.getAspects());
				}
			}
			return null;
		});

		inWriteTx(() -> {
			productService.formulate(product.getNodeRef());
			return null;
		});

		inReadTx(() -> {
			RawMaterialData formulated = (RawMaterialData) alfrescoRepository.findOne(product.getNodeRef());
			System.out.println("AFTER FORMULATE - SVHC List size: " + (formulated.getSvhcList() != null ? formulated.getSvhcList().size() : "null"));
			if (formulated.getSvhcList() != null) {
				for (SvhcListDataItem item : formulated.getSvhcList()) {
					System.out.println("AFTER FORMULATE - Item: " + item.getIng() + ", isManual: " + item.getIsManual() + ", aspects: " + item.getAspects() + ", value: " + item.getValue());
				}
			}
			System.out.println("AFTER FORMULATE - Product specifications size: " + (formulated.getProductSpecifications() != null ? formulated.getProductSpecifications().size() : "null"));
			if (formulated.getProductSpecifications() != null) {
				for (ProductSpecificationData spec : formulated.getProductSpecifications()) {
					System.out.println("AFTER FORMULATE - Spec name: " + spec.getName() + ", svhcList size: " + (spec.getSvhcList() != null ? spec.getSvhcList().size() : "null"));
					if (spec.getSvhcList() != null) {
						for (SvhcListDataItem sItem : spec.getSvhcList()) {
							System.out.println("AFTER FORMULATE - Spec SvhcItem: ing=" + sItem.getIng() + ", maxi=" + sItem.getMaxi() + ", value=" + sItem.getValue());
						}
					}
				}
			}
			return null;
		});

		inReadTx(() -> {
			RawMaterialData formulated = (RawMaterialData) alfrescoRepository.findOne(product.getNodeRef());

			Assert.assertNotNull(formulated.getSvhcList());
			Assert.assertEquals(2, formulated.getSvhcList().size());

			Assert.assertNotNull(formulated.getReqCtrlList());
			System.out.println("REQ_CTRL_LIST size: " + formulated.getReqCtrlList().size());
			for (RequirementListDataItem reqCtrl : formulated.getReqCtrlList()) {
				System.out.println("REQ_CTRL: type=" + reqCtrl.getReqType() + ", charact=" + reqCtrl.getCharact() + ", message=" + (reqCtrl.getReqMlMessage() != null ? reqCtrl.getReqMlMessage().getDefaultValue() : "null"));
			}
			Assert.assertFalse(formulated.getReqCtrlList().isEmpty());

			boolean foundRangeAlert = false;
			for (RequirementListDataItem reqCtrl : formulated.getReqCtrlList()) {
				if (ingNodeRef.equals(reqCtrl.getCharact())) {
					Assert.assertEquals(RequirementType.Forbidden, reqCtrl.getReqType());
					Assert.assertNotNull(reqCtrl.getReqMlMessage());
					String message = reqCtrl.getReqMlMessage().getDefaultValue();
					Assert.assertTrue("Message should contain actual value '40%'", message.contains("40"));
					Assert.assertTrue("Message should contain expected limit '30%'", message.contains("30"));
					foundRangeAlert = true;
				}
			}
			Assert.assertTrue("Should find a requirement control alert for the exceeded svhc", foundRangeAlert);

			return null;
		});
	}

	@Test
	public void testSvhcRequirementScannerAuthorizedMode() {
		RawMaterialData product = inWriteTx(() -> {
			RawMaterialData productData = new RawMaterialData();
			productData.setName("Product test svhc authorized scanner");

			List<IngListDataItem> ingList = new ArrayList<>();
			IngListDataItem ing1 = new IngListDataItem();
			ing1.setIng(ingNodeRef);
			ing1.setQtyPerc(40.0);
			ingList.add(ing1);

			IngListDataItem ing2 = new IngListDataItem();
			ing2.setIng(otherIngNodeRef);
			ing2.setQtyPerc(10.0);
			ingList.add(ing2);

			productData.setIngList(ingList);
			productData.setSvhcList(new ArrayList<>());

			productData = (RawMaterialData) alfrescoRepository.create(getTestFolderNodeRef(), productData);

			ProductSpecificationData spec = new ProductSpecificationData();
			spec.setName("Spec authorize Svhc 1");
			List<SvhcListDataItem> specSvhcList = new ArrayList<>();
			SvhcListDataItem specSvhc1 = new SvhcListDataItem();
			specSvhc1.setIng(ingNodeRef);
			specSvhc1.setRegulatoryType(RequirementType.Authorized);
			specSvhcList.add(specSvhc1);
			spec.setSvhcList(specSvhcList);

			spec = (ProductSpecificationData) alfrescoRepository.create(getTestFolderNodeRef(), spec);

			if (!nodeService.hasAspect(productData.getNodeRef(), PLMModel.ASPECT_TRANSFORMATION)) {
				nodeService.addAspect(productData.getNodeRef(), PLMModel.ASPECT_TRANSFORMATION, null);
			}
			nodeService.createAssociation(productData.getNodeRef(), spec.getNodeRef(), PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);

			return productData;
		});

		inWriteTx(() -> {
			productService.formulate(product.getNodeRef());
			return null;
		});

		inReadTx(() -> {
			RawMaterialData formulated = (RawMaterialData) alfrescoRepository.findOne(product.getNodeRef());

			Assert.assertNotNull(formulated.getSvhcList());
			Assert.assertEquals(2, formulated.getSvhcList().size());

			Assert.assertNotNull(formulated.getReqCtrlList());
			Assert.assertFalse(formulated.getReqCtrlList().isEmpty());

			boolean foundAuthorizedSvhcAlert = false;
			boolean foundUnauthorizedSvhcAlert = false;

			for (RequirementListDataItem reqCtrl : formulated.getReqCtrlList()) {
				if (ingNodeRef.equals(reqCtrl.getCharact())) {
					foundAuthorizedSvhcAlert = true;
				} else if (otherIngNodeRef.equals(reqCtrl.getCharact())) {
					Assert.assertEquals(RequirementType.Forbidden, reqCtrl.getReqType());
					Assert.assertNotNull(reqCtrl.getReqMlMessage());
					String message = reqCtrl.getReqMlMessage().getDefaultValue();
					Assert.assertTrue(message.contains("not authorized") || message.contains("autoris"));
					foundUnauthorizedSvhcAlert = true;
				}
			}

			Assert.assertFalse("Should not raise range alert for the authorized svhc 1", foundAuthorizedSvhcAlert);
			Assert.assertTrue("Should raise an unauthorized alert for svhc 2", foundUnauthorizedSvhcAlert);

			return null;
		});
	}

}
