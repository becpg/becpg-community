package fr.becpg.test.repo.product.formulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.extensions.surf.util.I18NUtil;

import com.ibm.icu.util.Calendar;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.ForbiddenIngListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.formulation.AbstractSimpleListFormulationHandler;
import fr.becpg.repo.product.formulation.AllergensCalculatingFormulationHandler;
import fr.becpg.repo.product.formulation.LabelClaimFormulationHandler;
import fr.becpg.repo.product.formulation.NutsCalculatingFormulationHandler;
import fr.becpg.repo.product.formulation.PhysicoChemCalculatingFormulationHandler;
import fr.becpg.repo.product.formulation.ScoreCalculatingFormulationHandler;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class FormulationSpecMergeTest extends AbstractFinishedProductTest {

	protected static final Log logger = LogFactory.getLog(FormulationSpecMergeTest.class);

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
	}

	private NodeRef createTestProduct(final String name) {
		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/**
			 * Finished product 1
			 */
			logger.debug("/* Creating " + name + "*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName(name + Calendar.getInstance().getTimeInMillis());
			finishedProduct.setLegalName("legal " + name);
			finishedProduct.setQty(2d);
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setDensity(1d);
			finishedProduct.setServingSize(50d);// 50g

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		}, false, true);
	}

	@Test
	public void testSpecificationsLabelClaimMerge() {

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			logger.info("/*************************************/");
			logger.info("/*--     Test LabelClaim Merge     --*/");
			logger.info("/*************************************/");

			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "labelClaim1");
			NodeRef testProduct = createTestProduct("Finished product 1");

			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF11NodeRef));
			compoList
					.add(new CompoListDataItem(null, compoList.get(0), null, 2d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial12NodeRef));
			ProductData finishedProduct = alfrescoRepository.findOne(testProduct);
			finishedProduct.getCompoListView().setCompoList(compoList);
			alfrescoRepository.save(finishedProduct);

			NodeRef labelClaimNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_LABEL_CLAIM, properties).getChildRef();

			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "labelClaim2");
			NodeRef labelClaimNodeRef2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_LABEL_CLAIM, properties).getChildRef();

			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "labelClaim3");
			NodeRef labelClaimNodeRef3 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_LABEL_CLAIM, properties).getChildRef();

			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "labelClaim4");
			NodeRef labelClaimNodeRef4 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_LABEL_CLAIM, properties).getChildRef();

			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "labelClaim5");
			NodeRef labelClaimNodeRef5 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_LABEL_CLAIM, properties).getChildRef();

			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "labelClaim6");
			NodeRef labelClaimNodeRef6 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_LABEL_CLAIM, properties).getChildRef();

			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "labelClaim7");
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

			productSpec1.getLabelClaimList().add(new LabelClaimListDataItem(labelClaimNodeRef, "toto", Boolean.TRUE));
			productSpec1.getLabelClaimList().add(new LabelClaimListDataItem(labelClaimNodeRef2, "toto", Boolean.FALSE));
			productSpec1.getLabelClaimList().add(new LabelClaimListDataItem(labelClaimNodeRef3, "toto", Boolean.TRUE));
			productSpec1.getLabelClaimList().add(new LabelClaimListDataItem(labelClaimNodeRef5, "toto", Boolean.FALSE));
			productSpec1.getLabelClaimList().add(new LabelClaimListDataItem(labelClaimNodeRef6, "toto", Boolean.TRUE));
			alfrescoRepository.save(productSpec1);

			properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, "Spec2");
			NodeRef productSpecificationNodeRef2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_PRODUCT_SPECIFICATION, properties).getChildRef();

			ProductSpecificationData productSpec2 = (ProductSpecificationData) alfrescoRepository.findOne(productSpecificationNodeRef2);

			productSpec2.setLabelClaimList(new ArrayList<LabelClaimListDataItem>());
			productSpec2.getLabelClaimList().add(new LabelClaimListDataItem(labelClaimNodeRef, "toto", Boolean.FALSE));
			productSpec2.getLabelClaimList().add(new LabelClaimListDataItem(labelClaimNodeRef2, "toto", Boolean.TRUE));
			productSpec2.getLabelClaimList().add(new LabelClaimListDataItem(labelClaimNodeRef5, "toto", Boolean.FALSE));

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

			ProductData product = alfrescoRepository.findOne(testProduct);
			product.setProductSpecifications(new ArrayList<ProductSpecificationData>());
			product.getProductSpecifications().add(globalSpec);
			product.setLabelClaimList(new ArrayList<LabelClaimListDataItem>());
			LabelClaimListDataItem productLabelClaimFalse = new LabelClaimListDataItem(labelClaimNodeRef, "toto", Boolean.TRUE);
			LabelClaimListDataItem productLabelClaimFalse2 = new LabelClaimListDataItem(labelClaimNodeRef2, "toto", Boolean.FALSE);
			LabelClaimListDataItem productLabelClaimFalse4 = new LabelClaimListDataItem(labelClaimNodeRef4, "toto", Boolean.TRUE);
			LabelClaimListDataItem productLabelClaimFalse5 = new LabelClaimListDataItem(labelClaimNodeRef5, "toto", Boolean.TRUE);
			LabelClaimListDataItem productLabelClaimFalse6 = new LabelClaimListDataItem(labelClaimNodeRef6, "toto", Boolean.TRUE);
			LabelClaimListDataItem productLabelClaimFalse7 = new LabelClaimListDataItem(labelClaimNodeRef7, "toto", Boolean.TRUE);

			LabelClaimListDataItem subProductLabelClaim6 = new LabelClaimListDataItem(labelClaimNodeRef6, "toto", Boolean.TRUE);
			subProductLabelClaim6.setLabelClaimValue(LabelClaimListDataItem.VALUE_EMPTY);
			LabelClaimListDataItem subProductLabelClaim7 = new LabelClaimListDataItem(labelClaimNodeRef7, "toto", Boolean.TRUE);
			subProductLabelClaim7.setLabelClaimValue(LabelClaimListDataItem.VALUE_EMPTY);

			productLabelClaimFalse.setIsManual(Boolean.TRUE);
			productLabelClaimFalse2.setIsManual(Boolean.TRUE);
			productLabelClaimFalse4.setIsManual(Boolean.TRUE);
			productLabelClaimFalse5.setIsManual(Boolean.TRUE);
			subProductLabelClaim6.setIsManual(Boolean.TRUE);
			subProductLabelClaim7.setIsManual(Boolean.TRUE);

			ProductData rm12 = alfrescoRepository.findOne(rawMaterial12NodeRef);
			if ((rm12 != null) && (rm12.getLabelClaimList() != null)) {
				rm12.getLabelClaimList().add(subProductLabelClaim6);
				rm12.getLabelClaimList().add(subProductLabelClaim7);
				alfrescoRepository.save(rm12);
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
			nodeService.createAssociation(testProduct, globalProductSpecificationNodeRef, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);

			/*-- Formulation --*/
			logger.info("/*-- Formulation --*/");
			productService.formulate(testProduct);

			/* -- Check formulation -- */
			ProductData formulatedProduct = alfrescoRepository.findOne(testProduct);

			logger.info("/*-- Formulation raised " + formulatedProduct.getCompoListView().getReqCtrlList().size() + " rclDataItem --*/");
			int checks = 0;
			for (ReqCtrlListDataItem rclDataItem : formulatedProduct.getCompoListView().getReqCtrlList()) {
				logger.info(rclDataItem.getReqMessage());
				if (I18NUtil.getMessage(LabelClaimFormulationHandler.MESSAGE_NOT_CLAIM, "labelClaim1").equals(rclDataItem.getReqMessage())) {
					fail();
				} else if (I18NUtil.getMessage(LabelClaimFormulationHandler.MESSAGE_NOT_CLAIM, "labelClaim2").equals(rclDataItem.getReqMessage())) {
					assertEquals(RequirementDataType.Specification, rclDataItem.getReqDataType());
					assertEquals(RequirementType.Forbidden, rclDataItem.getReqType());
					checks++;
				} else if (I18NUtil.getMessage(LabelClaimFormulationHandler.MESSAGE_NOT_CLAIM, "labelClaim3").equals(rclDataItem.getReqMessage())) {
					assertEquals(RequirementDataType.Specification, rclDataItem.getReqDataType());
					assertEquals(RequirementType.Forbidden, rclDataItem.getReqType());
					checks++;
				} else if (I18NUtil.getMessage(LabelClaimFormulationHandler.MESSAGE_NOT_CLAIM, "labelClaim4").equals(rclDataItem.getReqMessage())) {
					fail();
				} else if (I18NUtil.getMessage(LabelClaimFormulationHandler.MESSAGE_NOT_CLAIM, "labelClaim5").equals(rclDataItem.getReqMessage())) {
					fail();
				} else if (I18NUtil.getMessage(LabelClaimFormulationHandler.MESSAGE_NOT_CLAIM, "labelClaim6").equals(rclDataItem.getReqMessage())) {
					assertEquals(RequirementDataType.Specification, rclDataItem.getReqDataType());
					assertEquals(RequirementType.Forbidden, rclDataItem.getReqType());
					checks++;
				} else if (I18NUtil.getMessage(LabelClaimFormulationHandler.MESSAGE_MISSING_CLAIM, "labelClaim6")
						.equals(rclDataItem.getReqMessage())) {
					assertEquals(RequirementDataType.Labelclaim, rclDataItem.getReqDataType());
					assertEquals(RequirementType.Info, rclDataItem.getReqType());
					checks++;
				} else if (I18NUtil.getMessage(LabelClaimFormulationHandler.MESSAGE_MISSING_CLAIM, "labelClaim7")
						.equals(rclDataItem.getReqMessage())) {
					assertEquals(RequirementDataType.Labelclaim, rclDataItem.getReqDataType());
					assertEquals(RequirementType.Info, rclDataItem.getReqType());
					checks++;
				}

			}
			logger.info("Checks: " + checks + " (should be 5)");
			assertEquals(5, checks);

			return null;
		}, false, true);
	}

	@Test
	public void testNutrientsMerge() {
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			logger.info("/*************************************/");
			logger.info("/*--     Test Nutrients Merge      --*/");
			logger.info("/*************************************/");

			NodeRef fp2 = createTestProduct("Finished Product 2");
			ProductData finishedProduct = alfrescoRepository.findOne(fp2);
			final String name = finishedProduct.getName();

			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, name + " Spec Nut 1");
			NodeRef productSpecificationNodeRef1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_PRODUCT_SPECIFICATION, properties).getChildRef();

			/*
			 * ================ Specifications ================
			 */
			ProductSpecificationData productSpecification1 = (ProductSpecificationData) alfrescoRepository.findOne(productSpecificationNodeRef1);

			List<NutListDataItem> nutList = new ArrayList<>();
			nutList.add(new NutListDataItem(null, null, null, 3d, 4d, null, nut1, null));
			nutList.add(new NutListDataItem(null, null, null, 7d, null, null, nut2, null));
			nutList.add(new NutListDataItem(null, null, null, null, 10d, null, nut3, null));
			nutList.add(new NutListDataItem(null, null, null, 0.4d, 10d, null, nut4, null));
			productSpecification1.setNutList(nutList);

			properties.put(ContentModel.PROP_NAME, name + " Spec Nut 2");
			NodeRef productSpecificationNodeRef2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_PRODUCT_SPECIFICATION, properties).getChildRef();

			ProductSpecificationData productSpecification2 = (ProductSpecificationData) alfrescoRepository.findOne(productSpecificationNodeRef2);
			List<NutListDataItem> nutList2 = new ArrayList<>();
			nutList2.add(new NutListDataItem(null, null, null, 0.3d, 1.5d, null, nut4, null));
			productSpecification2.setNutList(nutList2);

			alfrescoRepository.save(productSpecification1);
			alfrescoRepository.save(productSpecification2);

			properties.put(ContentModel.PROP_NAME, name + " Spec Nut Global");
			NodeRef globalProductSpecificationNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_PRODUCT_SPECIFICATION, properties).getChildRef();

			ProductSpecificationData globalProductSpecification = (ProductSpecificationData) alfrescoRepository
					.findOne(globalProductSpecificationNodeRef);
			List<ProductSpecificationData> specList = new ArrayList<>();
			specList.add(productSpecification1);
			specList.add(productSpecification2);
			List<NutListDataItem> nutList4 = new ArrayList<>();
			nutList4.add(new NutListDataItem(null, null, null, 1d, 1.2d, null, nut4, null));
			globalProductSpecification.setProductSpecifications(specList);
			globalProductSpecification.setNutList(nutList4);
			alfrescoRepository.save(globalProductSpecification);

			/*
			 * ======= Compo =======
			 */
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF1NodeRef));
			compoList
					.add(new CompoListDataItem(null, compoList.get(0), null, 1d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
			compoList.add(new CompoListDataItem(null, compoList.get(0), null, 2d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
			compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF2NodeRef));
			compoList
					.add(new CompoListDataItem(null, compoList.get(3), null, 3d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
			compoList.add(new CompoListDataItem(null, compoList.get(3), null, 3d, CompoListUnit.kg, 0d, DeclarationType.Omit, rawMaterial4NodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);

			/*
			 * ====== Nuts ======
			 */
			List<NutListDataItem> nutList3 = new ArrayList<>();
			nutList3.add(new NutListDataItem(null, null, null, null, null, null, nut1, null));
			nutList3.add(new NutListDataItem(null, null, null, null, null, null, nut2, null));
			nutList3.add(new NutListDataItem(null, null, null, null, null, null, nut3, null));
			nutList3.add(new NutListDataItem(null, null, null, null, null, null, nut4, null));
			finishedProduct.setNutList(nutList3);

			alfrescoRepository.save(finishedProduct);

			nodeService.createAssociation(globalProductSpecificationNodeRef, productSpecificationNodeRef1, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);
			nodeService.createAssociation(globalProductSpecificationNodeRef, productSpecificationNodeRef2, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);
			nodeService.createAssociation(finishedProduct.getNodeRef(), globalProductSpecificationNodeRef, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);

			/*
			 * ========= Formulate =========
			 */
			productService.formulate(fp2);

			finishedProduct = alfrescoRepository.findOne(fp2);

			// tests rclDataItem
			String message0 = I18NUtil.getMessage(NutsCalculatingFormulationHandler.MESSAGE_NUT_NOT_IN_RANGE,
					nodeService.getProperty(nut1, BeCPGModel.PROP_CHARACT_NAME), "6", "7<= ", "");
			String message1 = I18NUtil.getMessage(NutsCalculatingFormulationHandler.MESSAGE_NUT_NOT_IN_RANGE,
					nodeService.getProperty(nut2, BeCPGModel.PROP_CHARACT_NAME), "6", "7<= ", "");
			String message2 = I18NUtil.getMessage(NutsCalculatingFormulationHandler.MESSAGE_NUT_NOT_IN_RANGE,
					nodeService.getProperty(nut3, BeCPGModel.PROP_CHARACT_NAME), "14", "", " <=10");
			String message3 = I18NUtil.getMessage(AbstractSimpleListFormulationHandler.MESSAGE_UNDEFINED_CHARACT,
					nodeService.getProperty(nut3, BeCPGModel.PROP_CHARACT_NAME));
			String message4 = I18NUtil.getMessage(NutsCalculatingFormulationHandler.MESSAGE_MAXIMAL_DAILY_VALUE,
					nodeService.getProperty(nut3, BeCPGModel.PROP_CHARACT_NAME));
			String message5 = I18NUtil.getMessage(NutsCalculatingFormulationHandler.MESSAGE_NUT_NOT_IN_RANGE,
					nodeService.getProperty(nut4, BeCPGModel.PROP_CHARACT_NAME), 1.5, "1<= ", " <=1,2");

			logger.debug("Message 1: " + message1);
			logger.debug("Message 2: " + message2);
			logger.debug("Message 3: " + message3);
			logger.debug("Message 4: " + message4);

			int checks = 0;
			logger.info("Formulation raised " + finishedProduct.getCompoListView().getReqCtrlList().size() + " rclDataItems");
			for (ReqCtrlListDataItem r : finishedProduct.getCompoListView().getReqCtrlList()) {

				logger.info("reqCtrl " + r.getReqMessage() + r.getReqType() + r.getSources());

				if (message1.equals(r.getReqMessage())) {
					assertEquals(0, r.getSources().size());
					checks++;
				} else if (message2.equals(r.getReqMessage())) {
					assertEquals(0, r.getSources().size());
					checks++;
				} else if (message0.equals(r.getReqMessage())) {
					fail();
				} else if (message3.equals(r.getReqMessage())) {
					assertEquals(1, r.getSources().size());
					assertEquals(rawMaterial4NodeRef, r.getSources().get(0));
					;
					checks++;
				} else if (message4.equals(r.getReqMessage())) {
					assertEquals(0, r.getSources().size());
					checks++;
				} else if (message5.equals(r.getReqMessage())) {
					assertEquals(0, r.getSources().size());
					checks++;
				}
			}

			logger.debug("checks: " + checks + "(should be 5)");

			assertEquals(5, checks);

			return null;
		}, false, true);
	}

	@Test
	public void testAllergenMerge() {

		logger.info("/*************************************/");
		logger.info("/*--     Test Allergens Merge      --*/");
		logger.info("/*************************************/");

		NodeRef fp3 = createTestProduct("Finished Product 3");
		ProductData finishedProduct = alfrescoRepository.findOne(fp3);
		final String name = finishedProduct.getName();

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, name + " Spec Allergen globale");
			NodeRef productSpecificationNodeRef1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_PRODUCT_SPECIFICATION, properties).getChildRef();

			ProductSpecificationData productSpecification = (ProductSpecificationData) alfrescoRepository.findOne(productSpecificationNodeRef1);

			// two catalogs
			properties.clear();
			properties.put(ContentModel.PROP_NAME, name + " Spec Allergen 1");
			NodeRef productSpecificationNodeRef2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_PRODUCT_SPECIFICATION, properties).getChildRef();

			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "allergen5");
			properties.put(PLMModel.PROP_ALLERGEN_TYPE, "Major");
			NodeRef allergen5 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ALLERGEN, properties).getChildRef();

			ProductSpecificationData productSpecification2 = (ProductSpecificationData) alfrescoRepository.findOne(productSpecificationNodeRef2);

			List<AllergenListDataItem> allergens = new ArrayList<>();

			allergens.add(new AllergenListDataItem(null, null, false, true, null, null, allergen1, false));
			allergens.add(new AllergenListDataItem(null, null, true, false, null, null, allergen2, false));

			productSpecification2.setAllergenList(allergens);
			alfrescoRepository.save(productSpecification2);

			/*-- Spec allergen 2 : allergen 1 is allowed in RM1&3 if voluntary--*/
			properties.clear();
			properties.put(ContentModel.PROP_NAME, name + " Spec Allergen 2");
			NodeRef productSpecificationNodeRef3 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_PRODUCT_SPECIFICATION, properties).getChildRef();
			ProductSpecificationData productSpecification3 = (ProductSpecificationData) alfrescoRepository.findOne(productSpecificationNodeRef3);

			allergens.clear();
			allergens.add(new AllergenListDataItem(null, null, true, false, null, null, allergen4, false));
			allergens.add(new AllergenListDataItem(null, null, true, true, null, null, allergen3, false));
			allergens.add(new AllergenListDataItem(null, null, false, true, null, null, allergen2, false));
			productSpecification3.setAllergenList(allergens);
			alfrescoRepository.save(productSpecification3);

			alfrescoRepository.save(productSpecification);

			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF1NodeRef));
			compoList
					.add(new CompoListDataItem(null, compoList.get(0), null, 1d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
			compoList.add(new CompoListDataItem(null, compoList.get(0), null, 2d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
			compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF2NodeRef));
			compoList
					.add(new CompoListDataItem(null, compoList.get(3), null, 3d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
			compoList.add(new CompoListDataItem(null, compoList.get(3), null, 3d, CompoListUnit.kg, 0d, DeclarationType.Omit, rawMaterial4NodeRef));

			// avoids rclDataItems due to invalid status
			for (CompoListDataItem compo : compoList) {
				ProductData product = alfrescoRepository.findOne(compo.getProduct());
				product.setState(SystemState.Valid);
				alfrescoRepository.save(product);

			}
			finishedProduct.getCompoListView().setCompoList(compoList);
			alfrescoRepository.save(finishedProduct);

			// putting allergens in RMs
			ProductData rm1 = alfrescoRepository.findOne(rawMaterial1NodeRef);
			if (rm1.getAllergenList() != null) {
				rm1.getAllergenList().clear();
				rm1.getAllergenList().add(new AllergenListDataItem(null, null, true, false, new ArrayList<>(), new ArrayList<>(), allergen1, false));
				rm1.getAllergenList().add(new AllergenListDataItem(null, null, true, false, new ArrayList<>(), new ArrayList<>(), allergen2, false));
				rm1.getAllergenList().add(new AllergenListDataItem(null, null, true, false, new ArrayList<>(), new ArrayList<>(), allergen3, false));
				rm1.getAllergenList().add(new AllergenListDataItem(null, null, true, false, new ArrayList<>(), new ArrayList<>(), allergen5, false));
			}
			alfrescoRepository.save(rm1);

			ProductData rm2 = alfrescoRepository.findOne(rawMaterial2NodeRef);
			if (rm2.getAllergenList() != null) {
				rm2.getAllergenList().clear();
				rm2.getAllergenList().add(new AllergenListDataItem(null, null, true, true, new ArrayList<>(), new ArrayList<>(), allergen2, false));
				rm2.getAllergenList().add(new AllergenListDataItem(null, null, false, false, new ArrayList<>(), new ArrayList<>(), allergen3, false));
				rm2.getAllergenList().add(new AllergenListDataItem(null, null, false, true, new ArrayList<>(), new ArrayList<>(), allergen4, false));

			}
			alfrescoRepository.save(rm2);

			ProductData rm3 = alfrescoRepository.findOne(rawMaterial3NodeRef);
			rm3.getAllergenList().clear();
			alfrescoRepository.save(rm3);

			ProductData rm4 = alfrescoRepository.findOne(rawMaterial4NodeRef);
			rm4.getAllergenList().clear();
			alfrescoRepository.save(rm4);

			// Binding specifications
			nodeService.createAssociation(productSpecificationNodeRef1, productSpecificationNodeRef3, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);
			nodeService.createAssociation(productSpecificationNodeRef1, productSpecificationNodeRef2, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);
			nodeService.createAssociation(finishedProduct.getNodeRef(), productSpecificationNodeRef1, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);

			/*
			 * ============= Formulation =============
			 */
			productService.formulate(fp3);

			ProductData formulatedProduct = alfrescoRepository.findOne(fp3);

			int checkMissingFields = 0;
			int checks = 0;
			for (ReqCtrlListDataItem r : formulatedProduct.getCompoListView().getReqCtrlList()) {
				logger.debug("Checking rclDataItem " + r.getReqMessage());
				if (I18NUtil
						.getMessage(ScoreCalculatingFormulationHandler.MESSAGE_MANDATORY_FIELD_MISSING, "Précautions d'emploi", "EU 1169/2011 (INCO)")
						.equals(r.getReqMessage())) {
					assertEquals(RequirementType.Forbidden, r.getReqType());
					checkMissingFields++;
				} else if (I18NUtil.getMessage(ScoreCalculatingFormulationHandler.MESSAGE_MANDATORY_FIELD_MISSING, "Conditions de conservation",
						"EU 1169/2011 (INCO)").equals(r.getReqMessage())) {
					assertEquals(RequirementType.Forbidden, r.getReqType());
					checkMissingFields++;
				} else if (I18NUtil.getMessage(AllergensCalculatingFormulationHandler.MESSAGE_FORBIDDEN_ALLERGEN,
						nodeService.getProperty(allergen2, BeCPGModel.PROP_CHARACT_NAME)).equals(r.getReqMessage())) {

					assertEquals(0, r.getSources().size());
					assertEquals(RequirementType.Forbidden, r.getReqType());
					checks++;
				} else if (I18NUtil.getMessage(AllergensCalculatingFormulationHandler.MESSAGE_FORBIDDEN_ALLERGEN,
						nodeService.getProperty(allergen1, BeCPGModel.PROP_CHARACT_NAME)).equals(r.getReqMessage())) {

					assertEquals(0, r.getSources().size());
					assertEquals(RequirementType.Forbidden, r.getReqType());
					checks++;
				} else if (I18NUtil.getMessage(AllergensCalculatingFormulationHandler.MESSAGE_FORBIDDEN_ALLERGEN,
						nodeService.getProperty(allergen4, BeCPGModel.PROP_CHARACT_NAME)).equals(r.getReqMessage())) {

					assertEquals(0, r.getSources().size());
					assertEquals(RequirementType.Forbidden, r.getReqType());
					checks++;
				} else if (!RequirementDataType.Completion.equals(r.getReqDataType())) {
					logger.debug("Unexpected rclDataItem: " + r.getReqMessage());
					fail();
				}
			}
			logger.debug("Missing fields : " + checkMissingFields + " (should be 2)");
			assertEquals(2, checkMissingFields);

			logger.debug("Checks: " + checks + " (should be 3)");
			assertEquals(3, checks);

			return null;
		}, false, true);
	}

	@Test
	public void testPhysicoChemMerge() {

		logger.info("/*************************************/");
		logger.info("/*--    Test Physico-Chem Merge    --*/");
		logger.info("/*************************************/");

		NodeRef fp4 = createTestProduct("Finished Product 4");
		ProductData finishedProduct = alfrescoRepository.findOne(fp4);
		final String name = finishedProduct.getName();

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			finishedProduct.setPhysicoChemList(new ArrayList<>());
			finishedProduct.getPhysicoChemList().add(new PhysicoChemListDataItem(null, 7d, null, null, null, physicoChem1));
			finishedProduct.getPhysicoChemList().add(new PhysicoChemListDataItem(null, 6d, null, null, null, physicoChem2));
			finishedProduct.getPhysicoChemList().add(new PhysicoChemListDataItem(null, 1.29d, null, null, null, physicoChem6));
			finishedProduct.getPhysicoChemList().add(new PhysicoChemListDataItem(null, 3.4d, null, null, null, physicoChem7));
			finishedProduct.getPhysicoChemList().add(new PhysicoChemListDataItem(null, 0.6774d, null, null, null, physicoChem8));
			alfrescoRepository.save(finishedProduct);

			// specification1
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, name + " Spec Physico-Chem 1");
			NodeRef productSpecificationNodeRef1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_PRODUCT_SPECIFICATION, properties).getChildRef();

			ProductSpecificationData productSpecification1 = (ProductSpecificationData) alfrescoRepository.findOne(productSpecificationNodeRef1);

			// physico chem must not be higher than 7
			ArrayList<PhysicoChemListDataItem> physicoChemList = new ArrayList<>();
			physicoChemList.add(new PhysicoChemListDataItem(null, null, null, 5d, 7d, physicoChem1));
			physicoChemList.add(new PhysicoChemListDataItem(null, null, null, 8d, 15d, physicoChem2));
			physicoChemList.add(new PhysicoChemListDataItem(null, null, null, null, 3d, physicoChem7));
			productSpecification1.setPhysicoChemList(physicoChemList);
			alfrescoRepository.save(productSpecification1);
			// specification2
			properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, name + " Spec Physico-Chem 2");
			NodeRef productSpecificationNodeRef2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_PRODUCT_SPECIFICATION, properties).getChildRef();

			ProductSpecificationData productSpecification2 = (ProductSpecificationData) alfrescoRepository.findOne(productSpecificationNodeRef2);

			// physico chem must not be lower than 7 (must be 7 precisely)
			physicoChemList = new ArrayList<>();
			physicoChemList.add(new PhysicoChemListDataItem(null, null, null, 7d, 9d, physicoChem1));
			physicoChemList.add(new PhysicoChemListDataItem(null, null, null, 5d, 9d, physicoChem2));
			physicoChemList.add(new PhysicoChemListDataItem(null, null, null, 3d, null, physicoChem6));
			productSpecification2.setPhysicoChemList(physicoChemList);
			alfrescoRepository.save(productSpecification2);

			properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, name + " Spec Physico-Chem 3 (global)");
			NodeRef productSpecificationNodeRef3 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_PRODUCT_SPECIFICATION, properties).getChildRef();

			ProductSpecificationData productSpecification3 = (ProductSpecificationData) alfrescoRepository.findOne(productSpecificationNodeRef3);
			physicoChemList = new ArrayList<>();
			physicoChemList.add(new PhysicoChemListDataItem(null, null, null, 5d, 8.5d, physicoChem2));
			productSpecification3.setPhysicoChemList(physicoChemList);
			alfrescoRepository.save(productSpecification3);

			// spec 3 has both spec 1 and spec 2
			nodeService.createAssociation(productSpecificationNodeRef3, productSpecificationNodeRef2, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);
			nodeService.createAssociation(productSpecificationNodeRef3, productSpecificationNodeRef1, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);

			// create association
			nodeService.createAssociation(fp4, productSpecificationNodeRef3, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);

			/*-- Formulate product --*/
			logger.info("/*-- Formulate product --*/");
			productService.formulate(fp4);

			/*-- Verify formulation --*/
			logger.info("/*-- Verify formulation --*/");
			ProductData formulatedProduct = alfrescoRepository.findOne(fp4);

			logger.debug("/*-- Formulation raised " + formulatedProduct.getCompoListView().getReqCtrlList().size() + " rclDataItems --*/");
			int checks = 0;
			for (ReqCtrlListDataItem reqCtrlList : formulatedProduct.getCompoListView().getReqCtrlList()) {
				logger.debug("/*-- Checking : \"" + reqCtrlList.getReqMessage() + "\" --*/");

				if (I18NUtil.getMessage(PhysicoChemCalculatingFormulationHandler.MESSAGE_PHYSICO_NOT_IN_RANGE, "physicoChem2", "6", "8<= ", " <=8,5")
						.equals(reqCtrlList.getReqMessage())) {
					assertEquals(RequirementType.Forbidden, reqCtrlList.getReqType());
					assertEquals(0, reqCtrlList.getSources().size());
					checks++;
				} else if (I18NUtil
						.getMessage(PhysicoChemCalculatingFormulationHandler.MESSAGE_PHYSICO_NOT_IN_RANGE, "physicoChem6", 1.29, "3<= ", "")
						.equals(reqCtrlList.getReqMessage())) {
					assertEquals(RequirementType.Forbidden, reqCtrlList.getReqType());
					assertEquals(0, reqCtrlList.getSources().size());
					checks++;
				} else if (I18NUtil.getMessage(PhysicoChemCalculatingFormulationHandler.MESSAGE_PHYSICO_NOT_IN_RANGE, "physicoChem7", 3.4, "", " <=3")
						.equals(reqCtrlList.getReqMessage())) {
					assertEquals(RequirementType.Forbidden, reqCtrlList.getReqType());
					assertEquals(0, reqCtrlList.getSources().size());
					checks++;
				} else {
					fail();
				}
			}

			logger.debug("Checks: " + checks + "(should be 3)");
			assertEquals(3, checks);

			return null;
		}, false, true);
	}

	@Test
	public void testForbiddenIngredientsMerge() {

		logger.info("/*************************************/");
		logger.info("/*--      Test Fbd Ings Merge      --*/");
		logger.info("/*************************************/");

		NodeRef fp5 = createTestProduct("Finished Product 5");
		ProductData finishedProduct = alfrescoRepository.findOne(fp5);
		final String name = finishedProduct.getName();

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			IngListDataItem gmoAndIonizedIng1 = new IngListDataItem(null, null, new ArrayList<>(), new ArrayList<>(), true, true, false, ing1, true);
			IngListDataItem ionizedIng2 = new IngListDataItem(null, null, new ArrayList<>(), new ArrayList<>(), false, true, false, ing2, true);
			IngListDataItem gmoIng3 = new IngListDataItem(null, null, new ArrayList<>(), new ArrayList<>(), true, false, false, ing3, true);
			IngListDataItem cleanIng4 = new IngListDataItem(null, null, new ArrayList<>(), new ArrayList<>(), false, false, false, ing4, true);

			RawMaterialData rawMaterial1 = new RawMaterialData();
			rawMaterial1.setName("Raw material 1b");
			rawMaterial1.setState(SystemState.Valid);
			rawMaterial1.setIngList(new ArrayList<IngListDataItem>(Arrays.asList(gmoAndIonizedIng1)));
			NodeRef rawMaterial1bNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial1).getNodeRef();

			RawMaterialData rawMaterial2 = new RawMaterialData();
			rawMaterial2.setName("Raw material 2b");
			rawMaterial2.setState(SystemState.Valid);
			rawMaterial2.setIngList(new ArrayList<IngListDataItem>(Arrays.asList(ionizedIng2)));
			NodeRef rawMaterial2bNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial2).getNodeRef();

			RawMaterialData rawMaterial3 = new RawMaterialData();
			rawMaterial3.setState(SystemState.Valid);
			rawMaterial3.setName("Raw material 3b");
			rawMaterial3.setIngList(new ArrayList<IngListDataItem>(Arrays.asList(gmoIng3)));
			NodeRef rawMaterial3bNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial3).getNodeRef();

			RawMaterialData rawMaterial4 = new RawMaterialData();
			rawMaterial4.setName("Raw material 4b");
			rawMaterial4.setState(SystemState.Valid);
			rawMaterial4.setIngList(new ArrayList<IngListDataItem>(Arrays.asList(cleanIng4)));
			NodeRef rawMaterial4bNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial4).getNodeRef();

			List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
			compoList.add(new CompoListDataItem(null, null, null, 0.80d, CompoListUnit.kg, 5d, DeclarationType.Declare, rawMaterial2bNodeRef));
			compoList.add(new CompoListDataItem(null, null, null, 0.30d, CompoListUnit.kg, 10d, DeclarationType.Detail, rawMaterial3bNodeRef));
			compoList.add(new CompoListDataItem(null, null, null, 0.80d, CompoListUnit.kg, 5d, DeclarationType.Declare, rawMaterial1bNodeRef));
			compoList.add(new CompoListDataItem(null, null, null, 0.30d, CompoListUnit.kg, 10d, DeclarationType.Detail, rawMaterial4bNodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);
			alfrescoRepository.save(finishedProduct);

			// spec

			List<ForbiddenIngListDataItem> forbiddenIngList1 = new ArrayList<>();
			forbiddenIngList1.add(new ForbiddenIngListDataItem(null, RequirementType.Forbidden, "Regulation OGM", null, Boolean.TRUE, null,
					new ArrayList<>(), null, null));
			forbiddenIngList1.add(new ForbiddenIngListDataItem(null, RequirementType.Forbidden, "Regulation Ionisation", null, null, Boolean.FALSE,
					new ArrayList<>(), null, null));

			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, name + " Spec Fbd ings 1");
			NodeRef productSpecificationNodeRef1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_PRODUCT_SPECIFICATION, properties).getChildRef();

			ProductSpecificationData productSpecification1 = (ProductSpecificationData) alfrescoRepository.findOne(productSpecificationNodeRef1);
			productSpecification1.setForbiddenIngList(forbiddenIngList1);
			alfrescoRepository.save(productSpecification1);

			properties.put(ContentModel.PROP_NAME, name + " Spec Fbd ings 2");
			NodeRef productSpecificationNodeRef2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_PRODUCT_SPECIFICATION, properties).getChildRef();

			ProductSpecificationData productSpecification2 = (ProductSpecificationData) alfrescoRepository.findOne(productSpecificationNodeRef2);
			List<ForbiddenIngListDataItem> forbiddenIngList2 = new ArrayList<>();
			forbiddenIngList2.add(new ForbiddenIngListDataItem(null, RequirementType.Forbidden, "Regulation OGM", null, Boolean.FALSE, null,
					new ArrayList<>(), null, null));
			forbiddenIngList2.add(new ForbiddenIngListDataItem(null, RequirementType.Forbidden, "Regulation Ionisation", null, null, Boolean.TRUE,
					new ArrayList<>(), null, null));
			productSpecification2.setForbiddenIngList(forbiddenIngList2);
			alfrescoRepository.save(productSpecification2);

			properties.put(ContentModel.PROP_NAME, name + " Spec Fbd ings global");
			NodeRef productSpecificationNodeRef3 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_PRODUCT_SPECIFICATION, properties).getChildRef();

			nodeService.createAssociation(productSpecificationNodeRef3, productSpecificationNodeRef2, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);
			nodeService.createAssociation(productSpecificationNodeRef3, productSpecificationNodeRef1, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);
			nodeService.createAssociation(fp5, productSpecificationNodeRef3, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);

			/*-- Formulate product --*/
			logger.info("/*-- Formulate product --*/");
			productService.formulate(fp5);

			/*-- Verify formulation --*/
			logger.info("/*-- Verify formulation --*/");
			ProductData formulatedProduct = alfrescoRepository.findOne(fp5);

			logger.debug("/*-- Formulation raised " + formulatedProduct.getCompoListView().getReqCtrlList().size() + " rclDataItems --*/");
			int checks = 0, checkMissingFields = 0;
			for (ReqCtrlListDataItem reqCtrlList : formulatedProduct.getCompoListView().getReqCtrlList()) {
				logger.debug("/*-- Checking : \"" + reqCtrlList.getReqMessage() + "\" --*/");

				if (I18NUtil
						.getMessage(ScoreCalculatingFormulationHandler.MESSAGE_MANDATORY_FIELD_MISSING, "Précautions d'emploi", "EU 1169/2011 (INCO)")
						.equals(reqCtrlList.getReqMessage())) {
					assertEquals(RequirementType.Forbidden, reqCtrlList.getReqType());
					checkMissingFields++;
				} else if (I18NUtil.getMessage(ScoreCalculatingFormulationHandler.MESSAGE_MANDATORY_FIELD_MISSING, "Conditions de conservation",
						"EU 1169/2011 (INCO)").equals(reqCtrlList.getReqMessage())) {
					assertEquals(RequirementType.Forbidden, reqCtrlList.getReqType());
					checkMissingFields++;
				} else if ("Regulation OGM".equals(reqCtrlList.getReqMessage())) {
					// last spec visited should have gmo to false
					fail();
					assertEquals(2, reqCtrlList.getSources().size());
					assertTrue(reqCtrlList.getSources().contains(rawMaterial1bNodeRef));
					assertTrue(reqCtrlList.getSources().contains(rawMaterial3bNodeRef));
					checks++;
				} else if ("Regulation Ionisation".equals(reqCtrlList.getReqMessage())) {
					assertEquals(2, reqCtrlList.getSources().size());
					assertTrue(reqCtrlList.getSources().contains(rawMaterial1bNodeRef));
					assertTrue(reqCtrlList.getSources().contains(rawMaterial2bNodeRef));
					checks++;
				} else {
					fail();
				}

			}

			logger.debug("Checks: " + checks + " (should be 1)");
			assertEquals(1, checks);
			assertEquals(2, checkMissingFields);

			return null;
		}, false, true);
	}
}
