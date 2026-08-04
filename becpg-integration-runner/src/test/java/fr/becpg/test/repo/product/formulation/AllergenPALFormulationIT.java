/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.test.repo.product.formulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.formulation.allergen.PALDatabaseService;
import fr.becpg.repo.product.formulation.allergen.PALReferenceDose;
import fr.becpg.test.PLMBaseTestCase;

/**
 * Tests the precautionary allergen labelling (PAL) assessment driven by the
 * reference dose grids of the regulatory frameworks.
 *
 * <p>The scenario is the "seeded baguette" of the VITAL guidelines: a 250 g
 * product carrying a voluntary wheat, a particulate sesame contamination and a
 * homogeneous soy contamination, assessed against two frameworks whose reference
 * doses differ by an order of magnitude.</p>
 *
 * @author matthieu
 */
public class AllergenPALFormulationIT extends PLMBaseTestCase {

	private static final String VITAL_3 = "TEST_VITAL_3";

	private static final String NL_ED05 = "TEST_NL_ED05";

	private static final String WHEAT_CODE = "WHT";

	private static final String SESAME_CODE = "SES";

	private static final String SOY_CODE = "SOY";

	/** Reference doses in mg of protein: sesame 0.1, soy 0.5, wheat 0.7 */
	private static final String VITAL_3_GRID = """
			allergenCode;rfdMg;maxActionPpm;proteinPerc
			SES;0.1;;
			SOY;0.5;;
			WHT;0.7;;
			""";

	/** Reference doses in mg of protein: sesame 2.0, soy 10.0, wheat 5.0 capped at 20 ppm */
	private static final String NL_ED05_GRID = """
			allergenCode;rfdMg;maxActionPpm;proteinPerc
			SES;2.0;;
			SOY;10.0;;
			WHT;5.0;20;
			""";

	private static final Double SERVING_SIZE_IN_GRAM = 250d;

	private static final Double SESAME_PARTICLE_WEIGHT = 0.0026d;

	private static final Double SESAME_PARTICLE_PROTEIN_PERC = 18d;

	/** 5 ppm of soy: above the VITAL 3.0 action limit of 2 ppm, below the NL one of 40 ppm */
	private static final Double SOY_CONTAMINATION_PERC = 0.0005d;

	/** Grids shipped with the product and published by the repository initialisation */
	private static final String SHIPPED_NL_ED05 = "NL_ED05";

	private static final String SHIPPED_VITAL_3 = "VITAL_3";

	private static final String SHIPPED_VITAL_4 = "VITAL_4";

	private static final String[] SHIPPED_FRAMEWORKS = { SHIPPED_NL_ED05, SHIPPED_VITAL_3, SHIPPED_VITAL_4 };

	private static final String SHIPPED_GLUTEN_CODE = "GLUTEN";

	/** The regulated allergen set the shipped grids must at least cover */
	private static final int MINIMUM_SHIPPED_ALLERGENS = 20;

	@Autowired
	private ProductService productService;

	@Autowired
	private PALDatabaseService palDatabaseService;

	private NodeRef wheatNodeRef;

	private NodeRef sesameNodeRef;

	private NodeRef soyNodeRef;

	private NodeRef rawMaterialNodeRef;

	/** {@inheritDoc} */
	@Override
	public void setUp() throws Exception {
		super.setUp();

		inWriteTx(() -> {
			wheatNodeRef = createAllergen("Wheat", WHEAT_CODE);
			sesameNodeRef = createAllergen("Sesame", SESAME_CODE);
			soyNodeRef = createAllergen("Soy", SOY_CODE);
			return null;
		});

		inWriteTx(() -> {
			createReferenceDoseGrid(VITAL_3, VITAL_3_GRID);
			createReferenceDoseGrid(NL_ED05, NL_ED05_GRID);
			return null;
		});

		rawMaterialNodeRef = inWriteTx(this::createSeedTopping);
	}

	/**
	 * A whole sesame seed carries 0.47 mg of protein: above the VITAL 3.0 reference
	 * dose of 0.1 mg, so the precautionary statement is required.
	 *
	 * @throws Exception if the formulation fails
	 */
	@Test
	public void testParticleContaminationTriggersPALUnderVital3() throws Exception {
		NodeRef productNodeRef = createBaguette(VITAL_3);

		formulate(productNodeRef);

		inReadTx(() -> {
			Assert.assertTrue("A whole sesame seed exceeds the VITAL 3.0 reference dose",
					isInVoluntary(loadProduct(productNodeRef), sesameNodeRef));
			return null;
		});
	}

	/**
	 * The very same seed stays below the Dutch ED05 reference dose of 2 mg, so no
	 * precautionary statement is required.
	 *
	 * @throws Exception if the formulation fails
	 */
	@Test
	public void testParticleContaminationStaysUnderNlEd05() throws Exception {
		NodeRef productNodeRef = createBaguette(NL_ED05);

		formulate(productNodeRef);

		inReadTx(() -> {
			Assert.assertFalse("A whole sesame seed stays below the ED05 reference dose",
					isInVoluntary(loadProduct(productNodeRef), sesameNodeRef));
			return null;
		});
	}

	/**
	 * 5 ppm of soy exceed the VITAL 3.0 action limit of 2 ppm derived from a 250 g
	 * serving, but stay below the 40 ppm of the Dutch grid.
	 *
	 * @throws Exception if the formulation fails
	 */
	@Test
	public void testHomogeneousContaminationFollowsTheActionLimit() throws Exception {
		NodeRef vitalProductNodeRef = createBaguette(VITAL_3);
		NodeRef dutchProductNodeRef = createBaguette(NL_ED05);

		formulate(vitalProductNodeRef);
		formulate(dutchProductNodeRef);

		inReadTx(() -> {
			Assert.assertTrue("5 ppm of soy exceed the VITAL 3.0 action limit of 2 ppm",
					isInVoluntary(loadProduct(vitalProductNodeRef), soyNodeRef));
			Assert.assertFalse("5 ppm of soy stay below the ED05 action limit of 40 ppm",
					isInVoluntary(loadProduct(dutchProductNodeRef), soyNodeRef));
			return null;
		});
	}

	/**
	 * Wheat is an ingredient of the recipe: it belongs to the ingredient list and
	 * never gets a precautionary statement, whatever the reference dose says.
	 *
	 * @throws Exception if the formulation fails
	 */
	@Test
	public void testVoluntaryAllergenNeverGetsPAL() throws Exception {
		NodeRef productNodeRef = createBaguette(VITAL_3);

		formulate(productNodeRef);

		inReadTx(() -> {
			ProductData product = loadProduct(productNodeRef);
			Assert.assertTrue("Wheat is intentionally used in the recipe", isVoluntary(product, wheatNodeRef));
			Assert.assertFalse("A voluntary allergen never gets a precautionary statement", isInVoluntary(product, wheatNodeRef));
			return null;
		});
	}

	/**
	 * Without a regulatory framework the fixed thresholds carried by the allergens
	 * keep driving the assessment, which guarantees that existing products are left
	 * untouched.
	 *
	 * @throws Exception if the formulation fails
	 */
	@Test
	public void testNoFrameworkKeepsTheLegacyBehaviour() throws Exception {
		NodeRef productNodeRef = createBaguette(null);

		formulate(productNodeRef);

		inReadTx(() -> {
			ProductData product = loadProduct(productNodeRef);
			Assert.assertTrue("Without a framework the homogeneous presence declared by the raw material is kept",
					isInVoluntary(product, soyNodeRef));
			Assert.assertTrue("Without a framework the particulate presence is left to the raw material declaration",
					isInVoluntary(product, sesameNodeRef));
			return null;
		});
	}

	/**
	 * The reference dose grids shipped with the product and published by the
	 * repository initialisation must be readable, comment lines and header included.
	 * Only their structure is asserted: the doses themselves are data the customer
	 * completes and maintains.
	 *
	 * @throws Exception if the grids cannot be read
	 */
	@Test
	public void testShippedGridsArePublishedAndParsed() throws Exception {
		inReadTx(() -> {
			for (String frameworkCode : SHIPPED_FRAMEWORKS) {
				Map<String, PALReferenceDose> referenceDoses = palDatabaseService.getReferenceDoses(frameworkCode);

				Assert.assertTrue("The shipped grid " + frameworkCode + " must cover the regulated allergens",
						referenceDoses.size() >= MINIMUM_SHIPPED_ALLERGENS);
				Assert.assertNotNull("The shipped grid " + frameworkCode + " must carry a gluten reference dose",
						referenceDoses.get(SHIPPED_GLUTEN_CODE));
			}

			assertReferenceDose(SHIPPED_NL_ED05, SHIPPED_GLUTEN_CODE, 5.0d);
			assertReferenceDose(SHIPPED_NL_ED05, "MUSTARD", 0.4d);
			assertReferenceDose(SHIPPED_NL_ED05, "LUPIN", 15.0d);
			assertReferenceDose(SHIPPED_VITAL_3, SHIPPED_GLUTEN_CODE, 0.7d);
			assertReferenceDose(SHIPPED_VITAL_3, "WALNUT", 0.03d);
			assertReferenceDose(SHIPPED_VITAL_4, "CRUSTACEANS", 200.0d);
			assertReferenceDose(SHIPPED_VITAL_4, "MUSTARD", 1.0d);

			Assert.assertEquals("The Dutch grid caps gluten at the legal gluten-free threshold", Double.valueOf(20d),
					palDatabaseService.getReferenceDoses(SHIPPED_NL_ED05).get(SHIPPED_GLUTEN_CODE).maxActionPpm());
			Assert.assertNull("Molluscs carry no VITAL 3.0 reference dose",
					palDatabaseService.getReferenceDoses(SHIPPED_VITAL_3).get("MOLLUSCS"));
			return null;
		});
	}

	/**
	 * <p>assertReferenceDose.</p>
	 *
	 * @param frameworkCode the shipped grid to read
	 * @param allergenCode the allergen to look up
	 * @param expectedRfdMg the published reference dose in mg of protein
	 */
	private void assertReferenceDose(String frameworkCode, String allergenCode, double expectedRfdMg) {
		PALReferenceDose referenceDose = palDatabaseService.getReferenceDoses(frameworkCode).get(allergenCode);

		Assert.assertNotNull(frameworkCode + " must carry a reference dose for " + allergenCode, referenceDose);
		Assert.assertEquals(frameworkCode + " reference dose of " + allergenCode, Double.valueOf(expectedRfdMg), referenceDose.rfdMg());
	}

	/**
	 * Creates the 250 g baguette using the whole seed topping as its single part.
	 *
	 * @param regulatoryFramework the framework code, or null to keep the legacy behaviour
	 * @return the node reference of the created product
	 * @throws Exception if the creation fails
	 */
	private NodeRef createBaguette(String regulatoryFramework) throws Exception {
		return inWriteTx(() -> {
			FinishedProductData baguette = new FinishedProductData();
			baguette.setName("Seeded baguette " + (regulatoryFramework == null ? "legacy" : regulatoryFramework));
			baguette.setUnit(ProductUnit.kg);
			baguette.setQty(1d);
			baguette.setNetWeight(1d);
			baguette.setServingSize(SERVING_SIZE_IN_GRAM);
			baguette.setServingSizeUnit(ProductUnit.g);
			baguette.setAllergenRegulatoryFramework(regulatoryFramework);

			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(rawMaterialNodeRef));
			baguette.getCompoListView().setCompoList(compoList);

			return alfrescoRepository.create(getTestFolderNodeRef(), baguette).getNodeRef();
		});
	}

	/**
	 * Creates the raw material carrying the three allergens of the scenario.
	 *
	 * @return the node reference of the created raw material
	 */
	private NodeRef createSeedTopping() {
		RawMaterialData seedTopping = new RawMaterialData();
		seedTopping.setName("Seed topping");
		seedTopping.setUnit(ProductUnit.kg);
		seedTopping.setQty(1d);

		List<AllergenListDataItem> allergenList = new ArrayList<>();
		allergenList.add(AllergenListDataItem.build().withAllergen(wheatNodeRef).withVoluntary(true).withInVoluntary(false).withQtyPerc(60d)
				.withIsManual(false));
		allergenList.add(AllergenListDataItem.build().withAllergen(sesameNodeRef).withVoluntary(false).withInVoluntary(true)
				.withParticleWeight(SESAME_PARTICLE_WEIGHT).withParticleProteinPerc(SESAME_PARTICLE_PROTEIN_PERC).withIsManual(false));
		allergenList.add(AllergenListDataItem.build().withAllergen(soyNodeRef).withVoluntary(false).withInVoluntary(true)
				.withQtyPerc(SOY_CONTAMINATION_PERC).withIsManual(false));
		seedTopping.setAllergenList(allergenList);

		return alfrescoRepository.create(getTestFolderNodeRef(), seedTopping).getNodeRef();
	}

	/**
	 * Creates an allergen characteristic carrying the code used by the grids.
	 *
	 * @param name the allergen name
	 * @param allergenCode the code used as key in the reference dose grids
	 * @return the node reference of the created allergen
	 */
	private NodeRef createAllergen(String name, String allergenCode) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_CHARACT_NAME, name);
		properties.put(PLMModel.PROP_ALLERGEN_TYPE, "Major");
		properties.put(PLMModel.PROP_ALLERGEN_CODE, allergenCode);

		return nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), PLMModel.TYPE_ALLERGEN, properties).getChildRef();
	}

	/**
	 * Drops a reference dose grid in the PAL database folder.
	 *
	 * @param frameworkCode the framework code, used as file name
	 * @param grid the CSV content of the grid
	 */
	private void createReferenceDoseGrid(String frameworkCode, String grid) {
		NodeRef databasesFolder = repoService.getOrCreateFolderByPath(systemFolderNodeRef, PlmRepoConsts.PATH_PAL_DATABASES,
				PlmRepoConsts.PATH_PAL_DATABASES);

		String fileName = frameworkCode + ".csv";
		NodeRef databaseNodeRef = fileFolderService.searchSimple(databasesFolder, fileName);

		if (databaseNodeRef == null) {
			databaseNodeRef = fileFolderService.create(databasesFolder, fileName, ContentModel.TYPE_CONTENT).getNodeRef();
		}

		ContentWriter writer = contentService.getWriter(databaseNodeRef, ContentModel.PROP_CONTENT, true);
		writer.setMimetype("text/csv");
		writer.setEncoding("UTF-8");
		writer.putContent(grid);
	}

	/**
	 * Runs the formulation of a product.
	 *
	 * @param productNodeRef the product to formulate
	 * @throws Exception if the formulation fails
	 */
	private void formulate(NodeRef productNodeRef) throws Exception {
		inWriteTx(() -> {
			productService.formulate(productNodeRef);
			return null;
		});
	}

	/**
	 * <p>loadProduct.</p>
	 *
	 * @param productNodeRef the product to load
	 * @return the formulated product
	 */
	private ProductData loadProduct(NodeRef productNodeRef) {
		return (ProductData) alfrescoRepository.findOne(productNodeRef);
	}

	/**
	 * <p>isInVoluntary.</p>
	 *
	 * @param product the formulated product
	 * @param allergenNodeRef the allergen to look up
	 * @return true when the allergen is flagged as an involuntary presence
	 */
	private boolean isInVoluntary(ProductData product, NodeRef allergenNodeRef) {
		AllergenListDataItem allergenListDataItem = findAllergen(product, allergenNodeRef);

		return (allergenListDataItem != null) && Boolean.TRUE.equals(allergenListDataItem.getInVoluntary());
	}

	/**
	 * <p>isVoluntary.</p>
	 *
	 * @param product the formulated product
	 * @param allergenNodeRef the allergen to look up
	 * @return true when the allergen is flagged as a voluntary presence
	 */
	private boolean isVoluntary(ProductData product, NodeRef allergenNodeRef) {
		AllergenListDataItem allergenListDataItem = findAllergen(product, allergenNodeRef);

		return (allergenListDataItem != null) && Boolean.TRUE.equals(allergenListDataItem.getVoluntary());
	}

	/**
	 * <p>findAllergen.</p>
	 *
	 * @param product the formulated product
	 * @param allergenNodeRef the allergen to look up
	 * @return the matching allergen line, or null when the allergen is absent
	 */
	private AllergenListDataItem findAllergen(ProductData product, NodeRef allergenNodeRef) {
		if (product.getAllergenList() == null) {
			return null;
		}

		for (AllergenListDataItem allergenListDataItem : product.getAllergenList()) {
			if (allergenNodeRef.equals(allergenListDataItem.getAllergen())) {
				return allergenListDataItem;
			}
		}

		return null;
	}

}
