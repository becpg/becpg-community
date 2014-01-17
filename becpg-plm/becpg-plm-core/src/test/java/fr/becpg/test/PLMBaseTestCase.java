/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
package fr.becpg.test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.hierarchy.HierarchyHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.AllergenType;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 * base class of test cases for product classes.
 * 
 * @author querephi
 */

@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration("classpath:alfresco/application-context.xml")
@ContextConfiguration(locations = { "classpath:alfresco/application-context.xml", "classpath:alfresco/web-scripts-application-context.xml",
		"classpath:alfresco/web-scripts-application-context-test.xml" })
public abstract class PLMBaseTestCase extends RepoBaseTestCase  {


	public static PLMBaseTestCase INSTANCE2;
	
	private static Log logger = LogFactory.getLog(PLMBaseTestCase.class);

	protected static final String HIERARCHY1_SEA_FOOD = "Sea food";
	protected static final String HIERARCHY2_FISH = "Fish";
	protected static final String HIERARCHY2_CRUSTACEAN = "Crustacean";
	protected static final String HIERARCHY1_FROZEN = "Frozen";
	protected static final String HIERARCHY2_PIZZA = "Pizza";
	protected static final String HIERARCHY2_QUICHE = "Quiche";
	protected static final String VALUE_COST_CURRENCY = "€";

	protected NodeRef HIERARCHY1_SEA_FOOD_REF;
	protected NodeRef HIERARCHY2_FISH_REF;
	protected NodeRef HIERARCHY2_CRUSTACEAN_REF;
	protected NodeRef HIERARCHY1_FROZEN_REF;
	protected NodeRef HIERARCHY2_PIZZA_REF;
	protected NodeRef HIERARCHY2_QUICHE_REF;

	protected List<NodeRef> allergens = new ArrayList<NodeRef>();
	protected List<NodeRef> costs = new ArrayList<NodeRef>();
	protected List<NodeRef> ings = new ArrayList<NodeRef>();

	protected List<NodeRef> nuts = new ArrayList<NodeRef>();
	protected List<NodeRef> organos = new ArrayList<NodeRef>();
	protected List<NodeRef> labelClaims = new ArrayList<NodeRef>();

	protected NodeRef labelingTemplateNodeRef = null;

	
	@Autowired
	protected AlfrescoRepository<ProductData> alfrescoRepository;


	@Override
	public void afterPropertiesSet() throws Exception {
		INSTANCE = this;
		INSTANCE2 = this;
	}
	
	@Override
	protected void doInitRepo(final boolean shouldInit) {

		if (shouldInit) {
			transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Boolean>() {
				public Boolean execute() throws Throwable {
					Assert.assertEquals(5, entitySystemService.getSystemEntities().size());

					initConstraints();
					return false;

				}
			}, false, true);
		}

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
				if (shouldInit) {
					dictionaryDAO.reset();
				}
				initCharacteristics();
				if (shouldInit) {
					initEntityTemplates();
				}
				initHierarchyLists();
				// initSystemProducts();
				initLabelingTemplate();
				// reset dictionary to reload constraints on list_values
				dictionaryDAO.reset();
				return null;

			}
		}, false, true);

	}

	@After
	public void tearDown() throws Exception {
		logger.debug("TearDown :");
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Boolean>() {
			public Boolean execute() throws Throwable {

				// products
				String query = LuceneHelper.mandatory(LuceneHelper.getCondType(BeCPGModel.TYPE_PRODUCT))
						+ LuceneHelper.exclude(LuceneHelper.getCondAspect(BeCPGModel.ASPECT_ENTITY_TPL))
						+ LuceneHelper.exclude(LuceneHelper.getCondEqualValue(ContentModel.PROP_NAME, "Eau"));
				List<NodeRef> productNodeRefs = beCPGSearchService.luceneSearch(query);

				for (NodeRef productNodeRef : productNodeRefs) {
					if (nodeService.exists(productNodeRef)) {

						String path = nodeService.getPath(productNodeRef).toDisplayPath(nodeService, permissionService);
						// if(!path.contains(BeCPGTestHelper.PATH_TESTFOLDER)){
						logger.debug("   - Deleting :" + nodeService.getProperty(productNodeRef, ContentModel.PROP_NAME));
						logger.debug("   - PATH :" + path);
						nodeService.deleteNode(productNodeRef);
						// }
					}
				}
				logger.debug("   - Deleting :" + nodeService.getProperty(testFolderNodeRef, ContentModel.PROP_NAME));
				nodeService.deleteNode(testFolderNodeRef);
				return true;

			}
		}, false, true);
	}

	private void initConstraints() {

		NodeRef listsFolder = entitySystemService.getSystemEntity(systemFolderNodeRef, RepoConsts.PATH_LISTS);

		// nutGroups
		NodeRef nutGroupsFolder = entitySystemService.getSystemEntityDataList(listsFolder, RepoConsts.PATH_NUT_GROUPS);
		String[] nutGroups = { "Groupe 1", "Groupe 2", "Autre" };
		for (String nutGroup : nutGroups) {
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, nutGroup);
			nodeService.createNode(nutGroupsFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_LIST_VALUE, properties);
		}
		// nutTypes
		NodeRef nutTypesFolder = entitySystemService.getSystemEntityDataList(listsFolder, RepoConsts.PATH_NUT_TYPES);
		String[] nutTypes = { "Nutriment", "Vitamine", "Minéraux", "Valeur énergétique" };
		for (String nutType : nutTypes) {
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, nutType);
			nodeService.createNode(nutTypesFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_LIST_VALUE, properties);
		}
		// nutFactsMethods
		NodeRef nutFactsMethodsFolder = entitySystemService.getSystemEntityDataList(listsFolder, RepoConsts.PATH_NUT_FACTS_METHODS);
		String[] nutFactsMethods = { "Formulation", "CIQUAL", "USDA" };
		for (String nutFactsMethod : nutFactsMethods) {
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, nutFactsMethod);
			nodeService.createNode(nutFactsMethodsFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_LIST_VALUE, properties);
		}

		// ingTypes
		NodeRef ingTypesFolder = entitySystemService.getSystemEntityDataList(listsFolder, RepoConsts.PATH_ING_TYPES);
		String[] ingTypes = { "Epaississant" };
		for (String ingType : ingTypes) {
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, ingType);
			nodeService.createNode(ingTypesFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_LIST_VALUE, properties);
		}

		// allergenTypes
		NodeRef allergenTypesFolder = entitySystemService.getSystemEntityDataList(listsFolder, RepoConsts.PATH_ALLERGEN_TYPES);
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, AllergenType.Major.toString());
		nodeService.createNode(allergenTypesFolder, ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_LIST_VALUE, properties);

		// labelingPosition
		NodeRef labelingPositionFolder = entitySystemService.getSystemEntityDataList(listsFolder, RepoConsts.PATH_LABELING_POSITIONS);
		String[] labelingPositions = { "Côté de la boîte", "Dessus de la boite" };
		for (String labelingPosition : labelingPositions) {
			properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, labelingPosition);
			nodeService.createNode(labelingPositionFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_LIST_VALUE, properties);
		}

//		// Quality
//		NodeRef qualityListsFolder = entitySystemService.getSystemEntity(systemFolderNodeRef, RepoConsts.PATH_QUALITY_LISTS);
//
//		NodeRef controlUnitsFolder = entitySystemService.getSystemEntityDataList(qualityListsFolder, RepoConsts.PATH_CONTROL_UNITS);
//		String[] controlUnits = { "kcal/100g", "mg/100g", "µg/100g", "g/100g" };
//		for (String controlUnit : controlUnits) {
//			properties = new HashMap<QName, Serializable>();
//			properties.put(ContentModel.PROP_NAME, controlUnit);
//			nodeService.createNode(controlUnitsFolder, ContentModel.ASSOC_CONTAINS,
//					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_LIST_VALUE, properties);
//		}

	}


	/**
	 * Initialize the characteristics of the repository.
	 */
	private void initCharacteristics() {

		NodeRef charactsFolder = entitySystemService.getSystemEntity(systemFolderNodeRef, RepoConsts.PATH_CHARACTS);

		// allergens
		NodeRef allergenFolder = entitySystemService.getSystemEntityDataList(charactsFolder, RepoConsts.PATH_ALLERGENS);
		List<FileInfo> allergensFileInfo = fileFolderService.listFiles(allergenFolder);
		if (allergensFileInfo.size() == 0) {
			for (int i = 0; i < 10; i++) {
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				properties.put(ContentModel.PROP_NAME, "Allergen " + i);
				properties.put(BeCPGModel.PROP_ALLERGEN_TYPE, AllergenType.Major.toString());
				ChildAssociationRef childAssocRef = nodeService.createNode(allergenFolder, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ALLERGEN, properties);
				allergens.add(childAssocRef.getChildRef());
			}
		} else {
			for (FileInfo fileInfo : allergensFileInfo) {
				allergens.add(fileInfo.getNodeRef());
			}
		}

		// costs
		NodeRef costFolder = entitySystemService.getSystemEntityDataList(charactsFolder, RepoConsts.PATH_COSTS);
		List<FileInfo> costsFileInfo = fileFolderService.listFiles(costFolder);
		if (costsFileInfo.size() == 0) {

			String[] costNames = { "Coût MP", "Coût prév MP", "Coût Emb", "Coût prév Emb" };
			for (String costName : costNames) {
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				properties.put(ContentModel.PROP_NAME, costName);
				properties.put(BeCPGModel.PROP_COSTCURRENCY, VALUE_COST_CURRENCY);
				ChildAssociationRef childAssocRef = nodeService.createNode(costFolder, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_COST, properties);
				costs.add(childAssocRef.getChildRef());
			}
		} else {
			for (FileInfo fileInfo : costsFileInfo) {
				costs.add(fileInfo.getNodeRef());
			}
		}

		// ings
		NodeRef ingFolder = entitySystemService.getSystemEntityDataList(charactsFolder, RepoConsts.PATH_INGS);
		List<FileInfo> ingsFileInfo = fileFolderService.listFiles(ingFolder);
		if (ingsFileInfo.size() == 0) {
			for (int i = 0; i < 10; i++) {
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				properties.put(ContentModel.PROP_NAME, "Ing " + i);
				ChildAssociationRef childAssocRef = nodeService.createNode(ingFolder, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ING, properties);
				ings.add(childAssocRef.getChildRef());
			}
		} else {
			for (FileInfo fileInfo : ingsFileInfo) {
				ings.add(fileInfo.getNodeRef());
			}
		}

		// nuts
		NodeRef nutFolder = entitySystemService.getSystemEntityDataList(charactsFolder, RepoConsts.PATH_NUTS);
		List<FileInfo> nutsFileInfo = fileFolderService.listFiles(nutFolder);
		if (nutsFileInfo.size() == 0) {
			for (int i = 0; i < 10; i++) {
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				properties.put(ContentModel.PROP_NAME, "Nut " + i);
				properties.put(BeCPGModel.PROP_NUTUNIT, "kcal");
				properties.put(BeCPGModel.PROP_NUTGROUP, "Groupe 1");
				ChildAssociationRef childAssocRef = nodeService.createNode(nutFolder, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_NUT, properties);
				nuts.add(childAssocRef.getChildRef());
			}
		} else {
			for (FileInfo fileInfo : nutsFileInfo) {
				nuts.add(fileInfo.getNodeRef());
			}
		}

		// organos
		NodeRef organoFolder = entitySystemService.getSystemEntityDataList(charactsFolder, RepoConsts.PATH_ORGANOS);
		List<FileInfo> organosFileInfo = fileFolderService.listFiles(organoFolder);
		if (organosFileInfo.size() == 0) {
			for (int i = 0; i < 10; i++) {
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				properties.put(ContentModel.PROP_NAME, "Organo " + i);
				ChildAssociationRef childAssocRef = nodeService.createNode(organoFolder, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_ORGANO, properties);
				organos.add(childAssocRef.getChildRef());
			}
		} else {
			for (FileInfo fileInfo : organosFileInfo) {
				organos.add(fileInfo.getNodeRef());
			}
		}

		

		// claim labelling
		NodeRef labelClaimListsFolder = entitySystemService.getSystemEntityDataList(charactsFolder, RepoConsts.PATH_LABELCLAIMS);
		List<FileInfo> labelClaimsFileInfo = fileFolderService.listFiles(labelClaimListsFolder);
		if (labelClaimsFileInfo.size() == 0) {

			String[] labelClaimNames = { "Faible valeur énergétique", "Sans apport énergétique" };
			for (String labelClaim : labelClaimNames) {
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				properties.put(ContentModel.PROP_NAME, labelClaim);
				properties.put(BeCPGModel.PROP_LABEL_CLAIM_TYPE, "Nutritionnelle");
				ChildAssociationRef childAssocRef = nodeService.createNode(labelClaimListsFolder, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), BeCPGModel.TYPE_LABEL_CLAIM, properties);
				labelClaims.add(childAssocRef.getChildRef());
			}
		} else {
			for (FileInfo fileInfo : labelClaimsFileInfo) {
				labelClaims.add(fileInfo.getNodeRef());
			}
		}

	}

	private void initEntityTemplates() {

		NodeRef rawMaterialTplNodeRef = entityTplService.getEntityTpl(BeCPGModel.TYPE_RAWMATERIAL);
		ProductData rawMaterialData = (ProductData) alfrescoRepository.findOne(rawMaterialTplNodeRef);
		rawMaterialData.getCostList().add(new CostListDataItem(null, null, null, null, costs.get(0), null));
		rawMaterialData.getNutList().add(new NutListDataItem(null, null, null, null, null, null, nuts.get(0), null));
		rawMaterialData.getNutList().add(new NutListDataItem(null, null, null, null, null, null, nuts.get(0), null));

		alfrescoRepository.save(rawMaterialData);

		NodeRef packMaterialTplNodeRef = entityTplService.getEntityTpl(BeCPGModel.TYPE_PACKAGINGMATERIAL);
		ProductData packMaterialTplData = (ProductData) alfrescoRepository.findOne(packMaterialTplNodeRef);
		packMaterialTplData.getCostList().add(new CostListDataItem(null, null, null, null, costs.get(3), null));
		alfrescoRepository.save(packMaterialTplData);

	}

	/**
	 * Init the hierarchy lists
	 */
	private void initHierarchyLists() {

		logger.debug("initHierarchyLists");

		NodeRef productHierarchyNodeRef = entitySystemService.getSystemEntity(systemFolderNodeRef, RepoConsts.PATH_PRODUCT_HIERARCHY);

		Assert.assertNotNull("Product hierarchy system entity not found", productHierarchyNodeRef);

		NodeRef rawMaterialHierarchyNodeRef = entitySystemService.getSystemEntityDataList(productHierarchyNodeRef,
				HierarchyHelper.getHierarchyPathName(BeCPGModel.TYPE_RAWMATERIAL));
		Assert.assertNotNull("raw material hierarchy dataList not found", rawMaterialHierarchyNodeRef);

		NodeRef finishedProductHierarchyNodeRef = entitySystemService.getSystemEntityDataList(productHierarchyNodeRef,
				HierarchyHelper.getHierarchyPathName(BeCPGModel.TYPE_FINISHEDPRODUCT));
		Assert.assertNotNull("Finished product hierarchy dataList not found", finishedProductHierarchyNodeRef);

		/*-- create hierarchy --*/
		// RawMaterial - Sea food
		HIERARCHY1_SEA_FOOD_REF = hierarchyService.createRootHierarchy(rawMaterialHierarchyNodeRef, HIERARCHY1_SEA_FOOD);
		HIERARCHY2_FISH_REF = hierarchyService.createHierarchy(rawMaterialHierarchyNodeRef, HIERARCHY1_SEA_FOOD_REF, HIERARCHY2_FISH);
		HIERARCHY2_CRUSTACEAN_REF = hierarchyService.createHierarchy(rawMaterialHierarchyNodeRef, HIERARCHY1_SEA_FOOD_REF, HIERARCHY2_CRUSTACEAN);

		// FinishedProduct - Frozen
		HIERARCHY1_FROZEN_REF = hierarchyService.createRootHierarchy(finishedProductHierarchyNodeRef, HIERARCHY1_FROZEN);
		HIERARCHY2_PIZZA_REF = hierarchyService.createHierarchy(finishedProductHierarchyNodeRef, HIERARCHY1_FROZEN_REF, HIERARCHY2_PIZZA);
		HIERARCHY2_QUICHE_REF = hierarchyService.createHierarchy(finishedProductHierarchyNodeRef, HIERARCHY1_FROZEN_REF, HIERARCHY2_QUICHE);



	}

	private void initLabelingTemplate() {

		NodeRef listsFolder = entitySystemService.getSystemEntity(systemFolderNodeRef, RepoConsts.PATH_CHARACTS);

		// labelingTemplate
		NodeRef labelingTemplateFolder = entitySystemService.getSystemEntityDataList(listsFolder, RepoConsts.PATH_LABELING_TEMPLATES);
		List<FileInfo> labelingTemplatesFileInfo = fileFolderService.listFiles(labelingTemplateFolder);
		if (labelingTemplatesFileInfo.size() == 0) {
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, "Marquage 1");
			properties.put(ContentModel.PROP_DESCRIPTION, "N° de lot : AAJJJ (AA : derniers chiffres de l’année ; JJJ : quantième du jour de fabrication)");
			labelingTemplateNodeRef = nodeService.createNode(labelingTemplateFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), PackModel.TYPE_LABELING_TEMPLATE, properties)
					.getChildRef();
		} else {
			labelingTemplateNodeRef = labelingTemplatesFileInfo.get(0).getNodeRef();
		}

	}

}
