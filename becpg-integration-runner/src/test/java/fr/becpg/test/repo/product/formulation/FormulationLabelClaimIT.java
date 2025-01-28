package fr.becpg.test.repo.product.formulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Test;

import java.util.Calendar;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class FormulationLabelClaimIT extends AbstractFinishedProductTest {

	private static final String LABEL_CLAIM_TYPE = "Test claim type";

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
	}

	private NodeRef createTestProduct(final List<LabelingRuleListDataItem> labelingRuleList) {
		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/**
			 * Finished product 1
			 */
			logger.debug("/*************************************/");
			logger.debug("/*-- Create Finished product 1--*/");
			logger.debug("/*************************************/");
			FinishedProductData finishedProduct1 = new FinishedProductData();
			finishedProduct1.setName("Finished product " + Calendar.getInstance().getTimeInMillis());
			finishedProduct1.setLegalName("legal Finished product 1");
			finishedProduct1.setQty(2d);
			finishedProduct1.setUnit(ProductUnit.kg);
			finishedProduct1.setDensity(1d);
			List<CompoListDataItem> compoList1 = new ArrayList<>();
			compoList1.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF11NodeRef));
			compoList1.add(CompoListDataItem.build().withParent(compoList1.get(0)).withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial11NodeRef));
			compoList1.add(CompoListDataItem.build().withParent(compoList1.get(0)).withQtyUsed(2d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial12NodeRef));
			compoList1.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF12NodeRef));
			compoList1.add(CompoListDataItem.build().withParent(compoList1.get(3)).withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial13NodeRef));
			compoList1.add(CompoListDataItem.build().withParent(compoList1.get(3)).withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial14NodeRef));

			List<PackagingListDataItem> packagingList = new ArrayList<>();

			packagingList.add(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.kg).withPkgLevel(PackagingLevel.Primary)
					.withIsMaster(true).withProduct(packagingMaterial1NodeRef));
			packagingList.add(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.kg).withPkgLevel(PackagingLevel.Primary)
					.withIsMaster(true).withProduct(packagingMaterial2NodeRef));

			finishedProduct1.getCompoListView().setCompoList(compoList1);
			finishedProduct1.getPackagingListView().setPackagingList(packagingList);

			finishedProduct1.getLabelingListView().setLabelingRuleList(labelingRuleList);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();
		}, false, true);
	}

	@Test
	public void testSpecificationsLabelingMerge() {

		NodeRef testProduct = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "labelClaim1");
			properties.put(ContentModel.PROP_NAME, "labelClaim1");
			NodeRef tmp = createTestProduct(null);
			NodeRef labelClaimNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_LABEL_CLAIM, properties).getChildRef();

			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "labelClaim2");
			properties.put(ContentModel.PROP_NAME, "labelClaim2");
			NodeRef labelClaimNodeRef2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_LABEL_CLAIM, properties).getChildRef();

			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "labelClaim3");
			properties.put(ContentModel.PROP_NAME, "labelClaim3");
			NodeRef labelClaimNodeRef3 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_LABEL_CLAIM, properties).getChildRef();

			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "labelClaim4");
			properties.put(ContentModel.PROP_NAME, "labelClaim4");
			NodeRef labelClaimNodeRef4 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_LABEL_CLAIM, properties).getChildRef();

			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "labelClaim5");
			properties.put(ContentModel.PROP_NAME, "labelClaim5");
			NodeRef labelClaimNodeRef5 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_LABEL_CLAIM, properties).getChildRef();

			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "labelClaim6");
			properties.put(ContentModel.PROP_NAME, "labelClaim6");
			NodeRef labelClaimNodeRef6 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_LABEL_CLAIM, properties).getChildRef();

			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "labelClaim7");
			properties.put(ContentModel.PROP_NAME, "labelClaim7");
			NodeRef labelClaimNodeRef7 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_LABEL_CLAIM, properties).getChildRef();

			properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, "Spec1");
			NodeRef productSpecificationNodeRef1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_PRODUCT_SPECIFICATION, properties).getChildRef();

			ProductSpecificationData productSpec1 = (ProductSpecificationData) alfrescoRepository.findOne(productSpecificationNodeRef1);
			productSpec1.setLabelClaimList(new ArrayList<LabelClaimListDataItem>());

			productSpec1.getLabelClaimList().add(new LabelClaimListDataItem(labelClaimNodeRef, LABEL_CLAIM_TYPE, Boolean.TRUE));
			productSpec1.getLabelClaimList().add(new LabelClaimListDataItem(labelClaimNodeRef2, LABEL_CLAIM_TYPE, Boolean.FALSE));
			productSpec1.getLabelClaimList().add(new LabelClaimListDataItem(labelClaimNodeRef3, LABEL_CLAIM_TYPE, Boolean.TRUE));
			productSpec1.getLabelClaimList().add(new LabelClaimListDataItem(labelClaimNodeRef5, LABEL_CLAIM_TYPE, Boolean.FALSE));
			productSpec1.getLabelClaimList().add(new LabelClaimListDataItem(labelClaimNodeRef6, LABEL_CLAIM_TYPE, Boolean.TRUE));
			alfrescoRepository.save(productSpec1);

			properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, "Spec2");
			NodeRef productSpecificationNodeRef2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_PRODUCT_SPECIFICATION, properties).getChildRef();

			ProductSpecificationData productSpec2 = (ProductSpecificationData) alfrescoRepository.findOne(productSpecificationNodeRef2);

			productSpec2.setLabelClaimList(new ArrayList<LabelClaimListDataItem>());
			productSpec2.getLabelClaimList().add(new LabelClaimListDataItem(labelClaimNodeRef, LABEL_CLAIM_TYPE, Boolean.FALSE));
			productSpec2.getLabelClaimList().add(new LabelClaimListDataItem(labelClaimNodeRef2, LABEL_CLAIM_TYPE, Boolean.TRUE));
			productSpec2.getLabelClaimList().add(new LabelClaimListDataItem(labelClaimNodeRef5, LABEL_CLAIM_TYPE, Boolean.FALSE));

			alfrescoRepository.save(productSpec2);

			properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, "global Spec");
			NodeRef globalProductSpecificationNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_PRODUCT_SPECIFICATION, properties).getChildRef();

			ProductSpecificationData globalSpec = (ProductSpecificationData) alfrescoRepository.findOne(globalProductSpecificationNodeRef);

			globalSpec.setProductSpecifications(new ArrayList<ProductSpecificationData>());
			globalSpec.getProductSpecifications().add(productSpec1);
			globalSpec.getProductSpecifications().add(productSpec2);

			ProductData product = (ProductData) alfrescoRepository.findOne(tmp);
			product.setProductSpecifications(new ArrayList<ProductSpecificationData>());
			product.getProductSpecifications().add(globalSpec);
			product.setLabelClaimList(new ArrayList<LabelClaimListDataItem>());
			LabelClaimListDataItem productLabelClaimFalse = new LabelClaimListDataItem(labelClaimNodeRef, LABEL_CLAIM_TYPE, Boolean.TRUE);
			LabelClaimListDataItem productLabelClaimFalse2 = new LabelClaimListDataItem(labelClaimNodeRef2, LABEL_CLAIM_TYPE, Boolean.FALSE);
			LabelClaimListDataItem productLabelClaimFalse4 = new LabelClaimListDataItem(labelClaimNodeRef4, LABEL_CLAIM_TYPE, Boolean.TRUE);
			LabelClaimListDataItem productLabelClaimFalse5 = new LabelClaimListDataItem(labelClaimNodeRef5, LABEL_CLAIM_TYPE, Boolean.TRUE);
			LabelClaimListDataItem productLabelClaimFalse6 = new LabelClaimListDataItem(labelClaimNodeRef6, LABEL_CLAIM_TYPE, Boolean.TRUE);
			LabelClaimListDataItem productLabelClaimFalse7 = new LabelClaimListDataItem(labelClaimNodeRef7, LABEL_CLAIM_TYPE, Boolean.TRUE);

			LabelClaimListDataItem subProductLabelClaim6 = new LabelClaimListDataItem(labelClaimNodeRef6, LABEL_CLAIM_TYPE, Boolean.TRUE);
			subProductLabelClaim6.setLabelClaimValue(LabelClaimListDataItem.VALUE_EMPTY);
			LabelClaimListDataItem subProductLabelClaim7 = new LabelClaimListDataItem(labelClaimNodeRef7, LABEL_CLAIM_TYPE, Boolean.TRUE);
			subProductLabelClaim7.setLabelClaimValue(LabelClaimListDataItem.VALUE_EMPTY);

			productLabelClaimFalse.setIsManual(Boolean.TRUE);
			productLabelClaimFalse2.setIsManual(Boolean.TRUE);
			productLabelClaimFalse4.setIsManual(Boolean.TRUE);
			productLabelClaimFalse5.setIsManual(Boolean.TRUE);
			subProductLabelClaim6.setIsManual(Boolean.TRUE);
			subProductLabelClaim7.setIsManual(Boolean.TRUE);

			ProductData rm12 = (ProductData) alfrescoRepository.findOne(rawMaterial12NodeRef);
			if ((rm12 != null) && (rm12.getLabelClaimList() != null)) {
				rm12.getLabelClaimList().add(subProductLabelClaim6);
				rm12.getLabelClaimList().add(subProductLabelClaim7);
				alfrescoRepository.save(rm12);
			}

			ProductData packaging1 = (ProductData) alfrescoRepository.findOne(packagingMaterial1NodeRef);
			if ((packaging1 != null) && (packaging1.getLabelClaimList() != null)) {

				LabelClaimListDataItem subPackagingProductLabelClaim6 = new LabelClaimListDataItem(labelClaimNodeRef6, LABEL_CLAIM_TYPE,
						Boolean.TRUE);
				subPackagingProductLabelClaim6.setLabelClaimValue(LabelClaimListDataItem.VALUE_EMPTY);
				LabelClaimListDataItem subPackagingProductLabelClaim7 = new LabelClaimListDataItem(labelClaimNodeRef7, LABEL_CLAIM_TYPE,
						Boolean.TRUE);

				packaging1.getLabelClaimList().add(subPackagingProductLabelClaim6);
				packaging1.getLabelClaimList().add(subPackagingProductLabelClaim7);
				alfrescoRepository.save(packaging1);
			}

			product.getLabelClaimList().add(productLabelClaimFalse);
			product.getLabelClaimList().add(productLabelClaimFalse2);
			product.getLabelClaimList().add(productLabelClaimFalse4);
			product.getLabelClaimList().add(productLabelClaimFalse5);
			product.getLabelClaimList().add(productLabelClaimFalse6);
			product.getLabelClaimList().add(productLabelClaimFalse7);

			alfrescoRepository.save(product);

			nodeService.createAssociation(globalProductSpecificationNodeRef, productSpecificationNodeRef2, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);
			nodeService.createAssociation(globalProductSpecificationNodeRef, productSpecificationNodeRef1, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);

			// create association
			nodeService.createAssociation(tmp, globalProductSpecificationNodeRef, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);

			/*-- Formulation --*/
			logger.info("/*-- Formulation --*/");
			productService.formulate(tmp);

			return tmp;
		}, false, true);

		checkRequirement(testProduct);

	}

	private void checkRequirement(NodeRef testProduct) {

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			/* -- Check formulation -- */
			ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(testProduct);

			logger.info("/*-- Formulation raised " + formulatedProduct.getReqCtrlList().size() + " rclDataItem --*/");
			int checks = 0;
			for (ReqCtrlListDataItem rclDataItem : formulatedProduct.getReqCtrlList()) {
				logger.info(rclDataItem.getReqMessage());
				if ("L'allégation 'labelClaim1' doit être revendiquée".equals(rclDataItem.getReqMessage())) {
					fail();
				} else if ("L'allégation 'labelClaim2' doit être revendiquée".equals(rclDataItem.getReqMessage())) {
					assertEquals(RequirementDataType.Specification, rclDataItem.getReqDataType());
					assertEquals(RequirementType.Forbidden, rclDataItem.getReqType());
					checks++;
				} else if ("L'allégation 'labelClaim3' doit être revendiquée".equals(rclDataItem.getReqMessage())) {
					assertEquals(RequirementDataType.Specification, rclDataItem.getReqDataType());
					assertEquals(RequirementType.Forbidden, rclDataItem.getReqType());
					checks++;
				} else if ("L'allégation 'labelClaim4' doit être revendiquée".equals(rclDataItem.getReqMessage())) {
					fail();
				} else if ("L'allégation 'labelClaim5' doit être revendiquée".equals(rclDataItem.getReqMessage())) {
					fail();
				} else if ("L'allégation 'labelClaim6' doit être revendiquée".equals(rclDataItem.getReqMessage())) {
					assertEquals(RequirementDataType.Specification, rclDataItem.getReqDataType());
					assertEquals(RequirementType.Forbidden, rclDataItem.getReqType());
					checks++;
				} else if ("Allégation 'labelClaim6' non renseignée".equals(rclDataItem.getReqMessage())) {
					assertEquals(RequirementDataType.Labelclaim, rclDataItem.getReqDataType());
					assertEquals(RequirementType.Info, rclDataItem.getReqType());
					assertTrue(rclDataItem.getSources().contains(packagingMaterial1NodeRef));
					assertTrue(rclDataItem.getSources().contains(rawMaterial12NodeRef));
					checks++;
				} else if ("Allégation 'labelClaim7' non renseignée".equals(rclDataItem.getReqMessage())) {
					assertEquals(RequirementDataType.Labelclaim, rclDataItem.getReqDataType());
					assertEquals(RequirementType.Info, rclDataItem.getReqType());
					assertFalse(rclDataItem.getSources().contains(packagingMaterial1NodeRef));
					assertTrue(rclDataItem.getSources().contains(rawMaterial12NodeRef));
					checks++;
				}

			}
			logger.info("Checks: " + checks + " (should be 5)");
			assertEquals(5, checks);

			return null;
		}, false, true);

	}
}
