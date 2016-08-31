/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG. 
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.hierarchy.HierarchyHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.AllergenType;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 * base class of test cases for product classes.
 * 
 * @author querephi
 */

public abstract class PLMBaseTestCase extends RepoBaseTestCase {

	public static PLMBaseTestCase INSTANCE2;

	private static final Log logger = LogFactory.getLog(PLMBaseTestCase.class);

	protected static final String HIERARCHY1_SEA_FOOD = "Sea food";
	protected static final String HIERARCHY2_FISH = "Fish";
	protected static final String HIERARCHY2_CRUSTACEAN = "Crustacean";
	protected static final String HIERARCHY1_FROZEN = "Frozen";
	protected static final String HIERARCHY2_PIZZA = "Pizza";
	protected static final String HIERARCHY2_QUICHE = "Quiche";
	protected static final String VALUE_COST_CURRENCY = "€";
	protected static final String HIERARCHY_RAWMATERIAL_PATH = PlmRepoConsts.PATH_PRODUCT_HIERARCHY + "cm:" + HierarchyHelper.getHierarchyPathName(PLMModel.TYPE_RAWMATERIAL);
	protected static final String HIERARCHY_FINISHEDPRODUCT_PATH = PlmRepoConsts.PATH_PRODUCT_HIERARCHY + "cm:" + HierarchyHelper.getHierarchyPathName(PLMModel.TYPE_FINISHEDPRODUCT);

	protected NodeRef HIERARCHY1_SEA_FOOD_REF;
	protected NodeRef HIERARCHY2_FISH_REF;
	protected NodeRef HIERARCHY2_CRUSTACEAN_REF;
	protected NodeRef HIERARCHY1_FROZEN_REF;
	protected NodeRef HIERARCHY2_PIZZA_REF;
	protected NodeRef HIERARCHY2_QUICHE_REF;

	protected List<NodeRef> allergens = new ArrayList<>();
	protected List<NodeRef> costs = new ArrayList<>();
	protected List<NodeRef> ings = new ArrayList<>();

	protected List<NodeRef> nuts = new ArrayList<>();
	protected List<NodeRef> organos = new ArrayList<>();
	protected List<NodeRef> labelClaims = new ArrayList<>();

	protected NodeRef labelingTemplateNodeRef = null;

	@Autowired
	protected AlfrescoRepository<ProductData> alfrescoRepository;
	

	@Override
	public void afterPropertiesSet() throws Exception {
		INSTANCE2 = this;
		super.afterPropertiesSet();
	}

	@Override
	protected boolean shouldInit(){
		return super.shouldInit() || hierarchyService.getHierarchyByPath(HIERARCHY_FINISHEDPRODUCT_PATH, null, HIERARCHY1_FROZEN) == null;
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

				getOrInitCharacteristics();
				if (shouldInit) {
					initEntityTemplates();
				}
				getOrInitHierarchyLists();

				// initSystemProducts();
				getOrInitLabelingTemplate();
				

				return null;

			}
		}, false, true);

	}

//	@After
//	public void tearDown() throws Exception {
//		logger.trace("TearDown :");
//		super.tearDown();
//		
//		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Boolean>() {
//			public Boolean execute() throws Throwable {
//				AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
//				// products
//				List<NodeRef> productNodeRefs = BeCPGQueryBuilder.createQuery().ofType(PLMModel.TYPE_PRODUCT).inDB().list();
//
//				for (NodeRef productNodeRef : productNodeRefs) {
//					if (nodeService.exists(productNodeRef) && !nodeService.hasAspect(productNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)) {
//                       try {
//						String path = nodeService.getPath(productNodeRef).toDisplayPath(nodeService, permissionService);
//						// if(!path.contains(BeCPGTestHelper.PATH_TESTFOLDER)){
//						logger.trace("   - Deleting :" + nodeService.getProperty(productNodeRef, BeCPGModel.PROP_CHARACT_NAME));
//						logger.trace("   - PATH :" + path);
//						nodeService.deleteNode(productNodeRef);
//						// }
//                       }catch(NodeLockedException e){
//                    	   logger.warn("node is locked",e);
//                       }
//					}
//				}
//				return true;
//
//			}
//		}, false, true);
//	}

	private void initConstraints() {

		NodeRef listsFolder = entitySystemService.getSystemEntity(systemFolderNodeRef, RepoConsts.PATH_LISTS);

		// nutGroups
		NodeRef nutGroupsFolder = entitySystemService.getSystemEntityDataList(listsFolder, PlmRepoConsts.PATH_NUT_GROUPS);
		String[] nutGroups = { "Groupe 1", "Groupe 2", "Autre" };
		for (String nutGroup : nutGroups) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_LV_VALUE, nutGroup);
			nodeService.createNode(nutGroupsFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_LV_VALUE)),
					BeCPGModel.TYPE_LIST_VALUE, properties);
		}
		// nutTypes
		NodeRef nutTypesFolder = entitySystemService.getSystemEntityDataList(listsFolder, PlmRepoConsts.PATH_NUT_TYPES);
		String[] nutTypes = { "Nutriment", "Vitamine", "Minéraux", "Valeur énergétique" };
		for (String nutType : nutTypes) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_LV_VALUE, nutType);
			nodeService.createNode(nutTypesFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_LV_VALUE)),
					BeCPGModel.TYPE_LIST_VALUE, properties);
		}
		// nutFactsMethods
		NodeRef nutFactsMethodsFolder = entitySystemService.getSystemEntityDataList(listsFolder, PlmRepoConsts.PATH_NUT_FACTS_METHODS);
		String[] nutFactsMethods = { "Formulation", "CIQUAL", "USDA" };
		for (String nutFactsMethod : nutFactsMethods) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_LV_VALUE, nutFactsMethod);
			nodeService.createNode(nutFactsMethodsFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_LV_VALUE)),
					BeCPGModel.TYPE_LIST_VALUE, properties);
		}

		// ingTypes
		NodeRef ingTypesFolder = entitySystemService.getSystemEntityDataList(listsFolder, PlmRepoConsts.PATH_ING_TYPES);
		String[] ingTypes = { "Epaississant" };
		for (String ingType : ingTypes) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_LV_VALUE, ingType);
			nodeService.createNode(ingTypesFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_LV_VALUE)),
					BeCPGModel.TYPE_LIST_VALUE, properties);
		}

		Map<QName, Serializable> properties;
		
		// labelingPosition
		NodeRef labelingPositionFolder = entitySystemService.getSystemEntityDataList(listsFolder, PlmRepoConsts.PATH_LABELING_POSITIONS);
		String[] labelingPositions = { "Côté de la boîte", "Dessus de la boite" };
		for (String labelingPosition : labelingPositions) {
			properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_LV_VALUE, labelingPosition);
			nodeService.createNode(labelingPositionFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_LV_VALUE)),
					BeCPGModel.TYPE_LIST_VALUE, properties);
		}

		// labelClaimTypes
		NodeRef labelClaimTypesFolder = entitySystemService.getSystemEntityDataList(listsFolder, PlmRepoConsts.PATH_LABELCLAIMS_TYPES);
		String[] labelClaimTypes = { "Nutritionnelle" };
		for (String labelClaimType : labelClaimTypes) {
			properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_LV_VALUE, labelClaimType);
			nodeService.createNode(labelClaimTypesFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_LV_VALUE)),
					BeCPGModel.TYPE_LIST_VALUE, properties);
		}

		
		// Quality
		NodeRef qualityListsFolder = entitySystemService.getSystemEntity(systemFolderNodeRef, PlmRepoConsts.PATH_QUALITY_LISTS);

		NodeRef controlUnitsFolder = entitySystemService.getSystemEntityDataList(qualityListsFolder, PlmRepoConsts.PATH_CONTROL_UNITS);
		String[] controlUnits = { "kcal/100g", "mg/100g", "µg/100g", "g/100g","-/100g","kJ/100g" };
		for (String controlUnit : controlUnits) {
			properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_LV_VALUE, controlUnit);
			nodeService.createNode(controlUnitsFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_LV_VALUE)),
					BeCPGModel.TYPE_LIST_VALUE, properties);
		}
		
		// physicoUnits
		NodeRef physicoUnitsFolder = entitySystemService.getSystemEntityDataList(listsFolder, PlmRepoConsts.PATH_PHYSICO_UNITS);
		String[] physicoUnits = { "%" };
		for (String physicoUnit : physicoUnits) {
			properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_LV_VALUE, physicoUnit);
			nodeService.createNode(physicoUnitsFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_LV_VALUE)),
					BeCPGModel.TYPE_LIST_VALUE, properties);
		}

	}

	/**
	 * Initialize the characteristics of the repository.
	 */
	private void getOrInitCharacteristics() {

		NodeRef charactsFolder = entitySystemService.getSystemEntity(systemFolderNodeRef, RepoConsts.PATH_CHARACTS);

		// allergens
		
		if(allergens.isEmpty()){
			NodeRef allergenFolder = entitySystemService.getSystemEntityDataList(charactsFolder, PlmRepoConsts.PATH_ALLERGENS);
			List<NodeRef> allergensNodeRef = entityListDAO.getListItems(allergenFolder,PLMModel.TYPE_ALLERGEN );
			
			if (allergensNodeRef.size() == 0) {
				for (int i = 0; i < 10; i++) {
					Map<QName, Serializable> properties = new HashMap<>();
					properties.put(BeCPGModel.PROP_CHARACT_NAME, "Allergen " + i);
					properties.put(PLMModel.PROP_ALLERGEN_TYPE, AllergenType.Major.toString());
					ChildAssociationRef childAssocRef = nodeService.createNode(allergenFolder, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
							PLMModel.TYPE_ALLERGEN, properties);
					allergens.add(childAssocRef.getChildRef());
				}
			} else {
				for (NodeRef fileInfo : allergensNodeRef) {
					if(((String)nodeService.getProperty(fileInfo,BeCPGModel.PROP_CHARACT_NAME)).startsWith("Allergen")){
						allergens.add(fileInfo);
					}
				}
			}
			
			Assert.assertEquals(10, allergens.size());
			
			allergens = Collections.unmodifiableList(allergens);
		}

		// costs
		if(costs.isEmpty()){
			NodeRef costFolder = entitySystemService.getSystemEntityDataList(charactsFolder, PlmRepoConsts.PATH_COSTS);
			List<NodeRef> costsNodeRef = entityListDAO.getListItems(costFolder,PLMModel.TYPE_COST);
			if (costsNodeRef.size() == 0) {
	
				String[] costNames = { "Coût MP", "Coût prév MP", "Coût Emb", "Coût prév Emb" };
				for (String costName : costNames) {
					Map<QName, Serializable> properties = new HashMap<>();
					properties.put(BeCPGModel.PROP_CHARACT_NAME, costName);
					properties.put(PLMModel.PROP_COSTCURRENCY, VALUE_COST_CURRENCY);
					ChildAssociationRef childAssocRef = nodeService.createNode(costFolder, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
							PLMModel.TYPE_COST, properties);
					costs.add(childAssocRef.getChildRef());
				}
			} else {
				for (NodeRef fileInfo : costsNodeRef) {
					if(((String)nodeService.getProperty(fileInfo,BeCPGModel.PROP_CHARACT_NAME)).startsWith("Coût")){
					costs.add(fileInfo);
					}
				}
			}
			
			Assert.assertEquals(4, costs.size());
			costs = Collections.unmodifiableList(costs);
		}

		// ings
		if(ings.isEmpty()){
			NodeRef ingFolder = entitySystemService.getSystemEntityDataList(charactsFolder, PlmRepoConsts.PATH_INGS);
			List<NodeRef> ingsNodeRef = entityListDAO.getListItems(ingFolder,PLMModel.TYPE_ING);
			if (ingsNodeRef.size() == 0) {
				for (int i = 0; i < 10; i++) {
					Map<QName, Serializable> properties = new HashMap<>();
					properties.put(BeCPGModel.PROP_CHARACT_NAME, "Ing " + i);
					ChildAssociationRef childAssocRef = nodeService.createNode(ingFolder, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
							PLMModel.TYPE_ING, properties);
					ings.add(childAssocRef.getChildRef());
				}
			} else {
				for (NodeRef fileInfo : ingsNodeRef) {
					if(((String)nodeService.getProperty(fileInfo,BeCPGModel.PROP_CHARACT_NAME)).startsWith("Ing")){
						ings.add(fileInfo);
					}
				}
			}
			
			Assert.assertEquals(10, ings.size());
			ings = Collections.unmodifiableList(ings);
		}

		// nuts
		if(nuts.isEmpty()){
			NodeRef nutFolder = entitySystemService.getSystemEntityDataList(charactsFolder, PlmRepoConsts.PATH_NUTS);
			List<NodeRef> nutsNodeRef = entityListDAO.getListItems(nutFolder,PLMModel.TYPE_NUT);
			if (nutsNodeRef.size() == 0) {
				for (int i = 0; i < 10; i++) {
					Map<QName, Serializable> properties = new HashMap<>();
					properties.put(BeCPGModel.PROP_CHARACT_NAME, "Nut " + i);
					properties.put(PLMModel.PROP_NUTUNIT, "kcal");
					properties.put(PLMModel.PROP_NUTGROUP, "Groupe 1");
					ChildAssociationRef childAssocRef = nodeService.createNode(nutFolder, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
							PLMModel.TYPE_NUT, properties);
					nuts.add(childAssocRef.getChildRef());
				}
			} else {
				for (NodeRef fileInfo : nutsNodeRef) {
					if(((String)nodeService.getProperty(fileInfo,BeCPGModel.PROP_CHARACT_NAME)).startsWith("Nut")){
				   	 nuts.add(fileInfo);
					}
				}
			}
			Assert.assertEquals(10, nuts.size());
			nuts = Collections.unmodifiableList(nuts);
		}

		// organos
		if(organos.isEmpty()){
			NodeRef organoFolder = entitySystemService.getSystemEntityDataList(charactsFolder, PlmRepoConsts.PATH_ORGANOS);
			List<NodeRef> organosNodeRef = entityListDAO.getListItems(organoFolder,PLMModel.TYPE_ORGANO);
			if (organosNodeRef.size() == 0) {
				for (int i = 0; i < 10; i++) {
					Map<QName, Serializable> properties = new HashMap<>();
					properties.put(BeCPGModel.PROP_CHARACT_NAME, "Organo " + i);
					ChildAssociationRef childAssocRef = nodeService.createNode(organoFolder, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
							PLMModel.TYPE_ORGANO, properties);
					organos.add(childAssocRef.getChildRef());
				}
			} else {
				for (NodeRef fileInfo : organosNodeRef) {
					if(((String)nodeService.getProperty(fileInfo,BeCPGModel.PROP_CHARACT_NAME)).startsWith("Organo")){
					  organos.add(fileInfo);
					}
				}
			}
			Assert.assertEquals(10, organos.size());
			organos = Collections.unmodifiableList(organos);
		}

		// claim labelling
		if(labelClaims.isEmpty()){
			NodeRef labelClaimListsFolder = entitySystemService.getSystemEntityDataList(charactsFolder, PlmRepoConsts.PATH_LABELCLAIMS);
			List<NodeRef> labelClaimsNodeRef = entityListDAO.getListItems(labelClaimListsFolder,PLMModel.TYPE_LABEL_CLAIM);
			if (labelClaimsNodeRef.size() == 0) {
	
				String[] labelClaimNames = { "Faible valeur énergétique", "Sans apport énergétique" };
				for (String labelClaim : labelClaimNames) {
					Map<QName, Serializable> properties = new HashMap<>();
					properties.put(BeCPGModel.PROP_CHARACT_NAME, labelClaim);
					properties.put(PLMModel.PROP_LABEL_CLAIM_TYPE, "Nutritionnelle");
					ChildAssociationRef childAssocRef = nodeService.createNode(labelClaimListsFolder, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
							PLMModel.TYPE_LABEL_CLAIM, properties);
					labelClaims.add(childAssocRef.getChildRef());
				}
			} else {
				for (NodeRef fileInfo : labelClaimsNodeRef) {
					
					if(((String)nodeService.getProperty(fileInfo,BeCPGModel.PROP_CHARACT_NAME)).startsWith( "Faible valeur énergétique")
							|| ((String)nodeService.getProperty(fileInfo,BeCPGModel.PROP_CHARACT_NAME)).startsWith( "Sans apport énergétique")){
						labelClaims.add(fileInfo);
					}
				}
			}
			Assert.assertEquals(2, labelClaims.size());
			labelClaims = Collections.unmodifiableList(labelClaims);
		}

	}

	private void initEntityTemplates() {

		NodeRef rawMaterialTplNodeRef = entityTplService.getEntityTpl(PLMModel.TYPE_RAWMATERIAL);
		ProductData rawMaterialData =  alfrescoRepository.findOne(rawMaterialTplNodeRef);
		rawMaterialData.getCostList().add(new CostListDataItem(null, null, null, null, costs.get(0), null));
		rawMaterialData.getNutList().add(new NutListDataItem(null, null, null, null, null, null, nuts.get(0), null));
		rawMaterialData.getNutList().add(new NutListDataItem(null, null, null, null, null, null, nuts.get(0), null));

		alfrescoRepository.save(rawMaterialData);

		NodeRef packMaterialTplNodeRef = entityTplService.getEntityTpl(PLMModel.TYPE_PACKAGINGMATERIAL);
		ProductData packMaterialTplData = alfrescoRepository.findOne(packMaterialTplNodeRef);
		packMaterialTplData.getCostList().add(new CostListDataItem(null, null, null, null, costs.get(3), null));
		alfrescoRepository.save(packMaterialTplData);

	}

	/**
	 * Init the hierarchy lists
	 */
	private void getOrInitHierarchyLists() {

		logger.trace("initHierarchyLists");

		NodeRef productHierarchyNodeRef = entitySystemService.getSystemEntity(systemFolderNodeRef, RepoConsts.PATH_PRODUCT_HIERARCHY);

		Assert.assertNotNull("Product hierarchy system entity not found", productHierarchyNodeRef);

		NodeRef rawMaterialHierarchyNodeRef = entitySystemService.getSystemEntityDataList(productHierarchyNodeRef,
				HierarchyHelper.getHierarchyPathName(PLMModel.TYPE_RAWMATERIAL));
		Assert.assertNotNull("raw material hierarchy dataList not found", rawMaterialHierarchyNodeRef);

		NodeRef finishedProductHierarchyNodeRef = entitySystemService.getSystemEntityDataList(productHierarchyNodeRef,
				HierarchyHelper.getHierarchyPathName(PLMModel.TYPE_FINISHEDPRODUCT));
		Assert.assertNotNull("Finished product hierarchy dataList not found", finishedProductHierarchyNodeRef);

		/*-- create hierarchy --*/
		// RawMaterial - Sea food
		HIERARCHY1_SEA_FOOD_REF = hierarchyService.getHierarchyByPath(HIERARCHY_RAWMATERIAL_PATH, null, HIERARCHY1_SEA_FOOD);
		if(HIERARCHY1_SEA_FOOD_REF == null){
			HIERARCHY1_SEA_FOOD_REF	= hierarchyService.createRootHierarchy(rawMaterialHierarchyNodeRef, HIERARCHY1_SEA_FOOD);
		}
		HIERARCHY2_FISH_REF = hierarchyService.getHierarchyByPath(HIERARCHY_RAWMATERIAL_PATH, HIERARCHY1_SEA_FOOD_REF, HIERARCHY2_FISH);
		if(HIERARCHY2_FISH_REF == null){	
			HIERARCHY2_FISH_REF = hierarchyService.createHierarchy(rawMaterialHierarchyNodeRef, HIERARCHY1_SEA_FOOD_REF, HIERARCHY2_FISH);
		}
		HIERARCHY2_CRUSTACEAN_REF = hierarchyService.getHierarchyByPath(HIERARCHY_RAWMATERIAL_PATH, HIERARCHY1_SEA_FOOD_REF, HIERARCHY2_CRUSTACEAN);
		if(HIERARCHY2_CRUSTACEAN_REF == null){	
			HIERARCHY2_CRUSTACEAN_REF = hierarchyService.createHierarchy(rawMaterialHierarchyNodeRef, HIERARCHY1_SEA_FOOD_REF, HIERARCHY2_CRUSTACEAN);
		}
		

		// FinishedProduct - Frozen
		
		HIERARCHY1_FROZEN_REF = hierarchyService.getHierarchyByPath(HIERARCHY_FINISHEDPRODUCT_PATH, null, HIERARCHY1_FROZEN);
		if(HIERARCHY1_FROZEN_REF == null){
			HIERARCHY1_FROZEN_REF	= hierarchyService.createRootHierarchy(finishedProductHierarchyNodeRef, HIERARCHY1_FROZEN);
		}
		HIERARCHY2_PIZZA_REF = hierarchyService.getHierarchyByPath(HIERARCHY_FINISHEDPRODUCT_PATH, HIERARCHY1_FROZEN_REF, HIERARCHY2_PIZZA);
		if(HIERARCHY2_PIZZA_REF == null){	
			HIERARCHY2_PIZZA_REF = hierarchyService.createHierarchy(finishedProductHierarchyNodeRef, HIERARCHY1_FROZEN_REF, HIERARCHY2_PIZZA);
		}
		HIERARCHY2_QUICHE_REF = hierarchyService.getHierarchyByPath(HIERARCHY_FINISHEDPRODUCT_PATH, HIERARCHY1_FROZEN_REF, HIERARCHY2_QUICHE);
		if(HIERARCHY2_QUICHE_REF == null){	
			HIERARCHY2_QUICHE_REF = hierarchyService.createHierarchy(finishedProductHierarchyNodeRef, HIERARCHY1_FROZEN_REF, HIERARCHY2_QUICHE);
		}
		

	}

	private void getOrInitLabelingTemplate() {

		NodeRef listsFolder = entitySystemService.getSystemEntity(systemFolderNodeRef, RepoConsts.PATH_CHARACTS);

		// labelingTemplate
		NodeRef labelingTemplateFolder = entitySystemService.getSystemEntityDataList(listsFolder, PlmRepoConsts.PATH_LABELING_TEMPLATES);
		List<NodeRef> labelingTemplatesNodeRef = entityListDAO.getListItems(labelingTemplateFolder,PackModel.TYPE_LABELING_TEMPLATE);
		if (labelingTemplatesNodeRef.size() == 0) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "Marquage 1");
			properties.put(ContentModel.PROP_DESCRIPTION,
					"N° de lot : AAJJJ (AA : derniers chiffres de l’année ; JJJ : quantième du jour de fabrication)");
			labelingTemplateNodeRef = nodeService.createNode(labelingTemplateFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PackModel.TYPE_LABELING_TEMPLATE, properties).getChildRef();
		} else {
			labelingTemplateNodeRef = labelingTemplatesNodeRef.get(0);
		}

	}

}
