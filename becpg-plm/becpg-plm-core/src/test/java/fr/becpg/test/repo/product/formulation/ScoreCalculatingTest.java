package fr.becpg.test.repo.product.formulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.JsonScoreHelper;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.PackagingListUnit;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.ForbiddenIngListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class ScoreCalculatingTest extends AbstractFinishedProductTest {

	protected static final Log logger = LogFactory.getLog(ScoreCalculatingTest.class);

	@Resource
	private AssociationService associationService;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
	}

	@Test
	public void testScore() {

		// create FP
		NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			FinishedProductData finishedProduct = new FinishedProductData();

			/**
			 * Raw Material part
			 */
			ProductData rawMaterial1 = alfrescoRepository.findOne(rawMaterial1NodeRef);
			rawMaterial1.setState(SystemState.Valid);
			alfrescoRepository.save(rawMaterial1);

			ProductData rawMaterial5 = alfrescoRepository.findOne(rawMaterial5NodeRef);
			rawMaterial5.setState(SystemState.ToValidate);
			alfrescoRepository.save(rawMaterial5);

			ProductData rawMaterial6 = alfrescoRepository.findOne(rawMaterial6NodeRef);
			rawMaterial6.setState(SystemState.Refused);
			alfrescoRepository.save(rawMaterial6);

			ProductData rawMaterial11 = alfrescoRepository.findOne(rawMaterial11NodeRef);
			rawMaterial11.setState(SystemState.Archived);
			alfrescoRepository.save(rawMaterial11);

			ProductData rawMaterial12 = alfrescoRepository.findOne(rawMaterial12NodeRef);
			rawMaterial12.setState(SystemState.Valid);
			alfrescoRepository.save(rawMaterial12);

			List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
			compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
			compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial5NodeRef));
			compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial6NodeRef));
			compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial11NodeRef));
			compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial12NodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);

			/**
			 * Packaging part
			 */
			ProductData packaging1 = alfrescoRepository.findOne(packagingMaterial1NodeRef);
			packaging1.setState(SystemState.Valid);
			alfrescoRepository.save(packaging1);

			ProductData packaging2 = alfrescoRepository.findOne(packagingMaterial2NodeRef);
			packaging2.setState(SystemState.ToValidate);
			alfrescoRepository.save(packaging2);

			ProductData packagingKit = alfrescoRepository.findOne(packagingKit1NodeRef);
			packagingKit.setState(SystemState.Simulation);
			alfrescoRepository.save(packagingKit);

			List<PackagingListDataItem> packagingList = new ArrayList<PackagingListDataItem>();
			packagingList.add(new PackagingListDataItem(null, 1d, PackagingListUnit.kg, PackagingLevel.Primary, false, packagingMaterial1NodeRef));
			packagingList.add(new PackagingListDataItem(null, 1d, PackagingListUnit.kg, PackagingLevel.Primary, false, packagingMaterial2NodeRef));
			packagingList.add(new PackagingListDataItem(null, 1d, PackagingListUnit.kg, PackagingLevel.Secondary, true, packagingKit1NodeRef));
			finishedProduct.getPackagingListView().setPackagingList(packagingList);

			finishedProduct.setLegalName("Finished Product 1");

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/**
			 * Spec
			 */
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, "Spec1");
			NodeRef productSpecificationNodeRef1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_PRODUCT_SPECIFICATION, properties).getChildRef();

			ProductSpecificationData specifications = (ProductSpecificationData) alfrescoRepository.findOne(productSpecificationNodeRef1);
			List<ForbiddenIngListDataItem> forbiddenIngList2 = new ArrayList<>();

			ings = new ArrayList<>();
			List<NodeRef> geoOrigins = new ArrayList<NodeRef>();
			ings.add(ing2);
			geoOrigins.add(geoOrigin2);
			forbiddenIngList2.add(new ForbiddenIngListDataItem(null, RequirementType.Forbidden, "Ing2 geoOrigin2 interdit sur charcuterie", null,
					null, null, ings, geoOrigins, new ArrayList<>()));

			specifications.setForbiddenIngList(forbiddenIngList2);
			alfrescoRepository.save(specifications);

			nodeService.createAssociation(finishedProductNodeRef, productSpecificationNodeRef1, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);
			return null;

		}, false, true);

		// formulate and check FP score
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			productService.formulate(finishedProductNodeRef);
			ProductData finishedProduct = alfrescoRepository.findOne(finishedProductNodeRef);

			assertNotNull(finishedProduct.getEntityScore());
			JSONObject scoresObject = new JSONObject(finishedProduct.getEntityScore());
			logger.info("Scores JSON object=" + scoresObject);

			int validationScore = (int) (scoresObject.getJSONObject("details").getDouble("componentsValidation"));
			int specificationsScore = scoresObject.getJSONObject("details").getInt("specifications");
			int mandatoryFieldsScore = (int) (scoresObject.getJSONObject("details").getDouble("mandatoryFields"));
			int globalScore = (int) (scoresObject.getDouble("global"));

			logger.info("ValidationScore=" + validationScore);
			logger.info("SpecificationsScore=" + specificationsScore);
			logger.info("MandatoryFieldsScore=" + mandatoryFieldsScore);

			assertEquals(37, validationScore);
			assertEquals(90, specificationsScore);
			assertEquals(33, mandatoryFieldsScore);
			assertEquals(53, globalScore);

			JSONArray catalogs = scoresObject.getJSONArray(JsonScoreHelper.PROP_CATALOGS);
			assertNotNull(catalogs);
			// TODO tests catalogs

			return null;
		}, false, true);
	}

}
