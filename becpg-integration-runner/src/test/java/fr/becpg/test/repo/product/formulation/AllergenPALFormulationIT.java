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
import org.alfresco.service.cmr.repository.ChildAssociationRef;
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
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
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

	private static final String VITAL_3 = "VITAL_3";

	private static final String NL_ED05 = "NL_ED05";

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

	@Autowired
	private ProductService productService;

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
			registerRegulatoryFramework(VITAL_3);
			registerRegulatoryFramework(NL_ED05);
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
	 * Registers a framework code in the list of values backing the product property,
	 * which is what an administrator does when publishing a new grid.
	 *
	 * @param frameworkCode the framework code to allow on the products
	 */
	private void registerRegulatoryFramework(String frameworkCode) {
		NodeRef frameworksFolder = getOrCreateFrameworksList();

		if (findFrameworkValue(frameworksFolder, frameworkCode) != null) {
			return;
		}

		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_LV_VALUE, frameworkCode);

		nodeService.createNode(frameworksFolder, ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, frameworkCode), BeCPGModel.TYPE_LIST_VALUE, properties);
	}

	/**
	 * Returns the list of values backing the regulatory framework property, creating
	 * it when the repository predates the feature.
	 *
	 * @return the node reference of the list container
	 */
	private NodeRef getOrCreateFrameworksList() {
		Map<String, QName> entityLists = new HashMap<>();
		entityLists.put(PlmRepoConsts.PATH_ALLERGEN_REGULATORY_FRAMEWORKS, BeCPGModel.TYPE_LIST_VALUE);

		NodeRef listsFolder = entitySystemService.createSystemEntity(systemFolderNodeRef, RepoConsts.PATH_LISTS, entityLists);

		return entitySystemService.getSystemEntityDataList(listsFolder, PlmRepoConsts.PATH_ALLERGEN_REGULATORY_FRAMEWORKS);
	}

	/**
	 * <p>findFrameworkValue.</p>
	 *
	 * @param frameworksFolder the list container
	 * @param frameworkCode the framework code to look up
	 * @return the matching list value, or null when it is not registered yet
	 */
	private NodeRef findFrameworkValue(NodeRef frameworksFolder, String frameworkCode) {
		for (ChildAssociationRef childAssoc : nodeService.getChildAssocs(frameworksFolder)) {
			if (frameworkCode.equals(nodeService.getProperty(childAssoc.getChildRef(), BeCPGModel.PROP_LV_VALUE))) {
				return childAssoc.getChildRef();
			}
		}

		return null;
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
