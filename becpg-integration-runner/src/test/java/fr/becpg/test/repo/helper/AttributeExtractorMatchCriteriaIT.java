/*
 *
 */
package fr.becpg.test.repo.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.test.PLMBaseTestCase;

/**
 * Checks the criteria matching behind the datalist and nested search filters.
 *
 * @author matthieu
 */
public class AttributeExtractorMatchCriteriaIT extends PLMBaseTestCase {

	private static final String PLANTS_CRITERION = "bcpg:plants";

	private static final String ERP_CODE_CRITERION = "bcpg:erpCode";

	@Autowired
	private AttributeExtractorService attributeExtractorService;

	private NodeRef plant1NodeRef;

	private NodeRef plant2NodeRef;

	private NodeRef plant3NodeRef;

	/**
	 * A filter on a multiple association sends every selected value in a single criterion (#30631):
	 * filtering on two plants keeps the products made in either of them.
	 */
	@Test
	public void testMatchMultipleAssociationCriterion() {

		final NodeRef bothPlantsNodeRef = inWriteTx(() -> {
			createPlants();
			return createProduct("Product made in plant 1 and plant 2", List.of(plant1NodeRef, plant2NodeRef));
		});

		final NodeRef firstPlantNodeRef = inWriteTx(() -> createProduct("Product made in plant 1", List.of(plant1NodeRef)));

		inReadTx(() -> {
			String bothPlants = plant1NodeRef + "," + plant2NodeRef;

			Assert.assertTrue("A product made in both selected plants matches",
					matches(bothPlantsNodeRef, PLANTS_CRITERION, bothPlants));
			Assert.assertTrue("A product made in one of the selected plants matches",
					matches(firstPlantNodeRef, PLANTS_CRITERION, bothPlants));
			Assert.assertFalse("A product made in none of the selected plants does not match",
					matches(firstPlantNodeRef, PLANTS_CRITERION, plant2NodeRef + "," + plant3NodeRef));

			Assert.assertTrue("A single selected plant still matches the products made in it",
					matches(bothPlantsNodeRef, PLANTS_CRITERION, plant1NodeRef.toString()));
			Assert.assertFalse("A single selected plant does not match a product made elsewhere",
					matches(bothPlantsNodeRef, PLANTS_CRITERION, plant3NodeRef.toString()));

			Assert.assertTrue("Plants selected by name match as well", matches(firstPlantNodeRef, PLANTS_CRITERION, "plant 1,plant 2"));
			Assert.assertFalse("Names of plants the product is not made in do not match",
					matches(firstPlantNodeRef, PLANTS_CRITERION, "plant 2,plant 3"));

			return null;
		});
	}

	/**
	 * A criterion on a text property is a partial search, but a product whose code is only a
	 * substring of the searched one must not be kept (#34682).
	 */
	@Test
	public void testMatchTextCriterion() {

		final NodeRef longCodeNodeRef = inWriteTx(() -> createProductWithErpCode("Product with a long erp code", "TESTXYZ"));

		final NodeRef shortCodeNodeRef = inWriteTx(() -> createProductWithErpCode("Product with a short erp code", "TEST"));

		inReadTx(() -> {
			Assert.assertTrue("A partial code matches the product carrying it", matches(longCodeNodeRef, ERP_CODE_CRITERION, "TEST"));
			Assert.assertTrue("An exact code matches", matches(shortCodeNodeRef, ERP_CODE_CRITERION, "TEST"));
			Assert.assertFalse("A product whose code is only a substring of the searched one does not match",
					matches(shortCodeNodeRef, ERP_CODE_CRITERION, "TESTXYZ"));

			return null;
		});
	}

	private boolean matches(NodeRef nodeRef, String criterion, String value) {
		Map<String, String> criteriaMap = new HashMap<>();
		criteriaMap.put(criterion, value);
		return attributeExtractorService.matchCriteria(nodeRef, criteriaMap);
	}

	private void createPlants() {
		plant1NodeRef = createPlant("Plant 1");
		plant2NodeRef = createPlant("Plant 2");
		plant3NodeRef = createPlant("Plant 3");
	}

	private NodeRef createPlant(String name) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(ContentModel.PROP_NAME, name);
		return nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), PLMModel.TYPE_PLANT, properties).getChildRef();
	}

	private NodeRef createProduct(String name, List<NodeRef> plants) {
		FinishedProductData finishedProduct = new FinishedProductData();
		finishedProduct.setName(name);
		finishedProduct.setPlants(new ArrayList<>(plants));
		return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
	}

	private NodeRef createProductWithErpCode(String name, String erpCode) {
		FinishedProductData finishedProduct = new FinishedProductData();
		finishedProduct.setName(name);
		finishedProduct.setErpCode(erpCode);
		return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
	}

}
