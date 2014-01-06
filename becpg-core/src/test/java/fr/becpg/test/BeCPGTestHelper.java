package fr.becpg.test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.admin.SystemGroup;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.OrganoListDataItem;

public class BeCPGTestHelper {

	public static final String USER_ONE = "matthieuWF";
	public static final String USER_TWO = "philippeWF";
	

	protected static String[] groups = { SystemGroup.QualityUser.toString(), SystemGroup.QualityMgr.toString() };

	
	
	private static Log logger = LogFactory.getLog(BeCPGTestHelper.class);
	
	/** The PAT h_ testfolder. */
	public static String PATH_TESTFOLDER = "TestFolder";
	
	public static String PRODUCT_NAME = "Finished Product";
	
	public static NodeRef createTestFolder() {
		return createTestFolder( PATH_TESTFOLDER);
	}


	public static NodeRef createTestFolder( String folderName) {

		NodeRef folderNodeRef = RepoBaseTestCase.INSTANCE.nodeService.getChildByName(
				RepoBaseTestCase.INSTANCE.repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, folderName);
		
		if(folderNodeRef == null){
			folderNodeRef = RepoBaseTestCase.INSTANCE.fileFolderService.create(RepoBaseTestCase.INSTANCE.repositoryHelper.getCompanyHome(),
					folderName, ContentModel.TYPE_FOLDER).getNodeRef();
		}
		
		return folderNodeRef;
	}
	
	
	public static NodeRef createMultiLevelProduct(NodeRef testFolder){

		/*-- Create raw material --*/		
		logger.debug("/*-- Create raw material --*/");
		RawMaterialData rawMaterial1 = new RawMaterialData();
		rawMaterial1.setName("Raw material 1");
		NodeRef rawMaterial1NodeRef = RepoBaseTestCase.INSTANCE.alfrescoRepository.create(testFolder, rawMaterial1).getNodeRef();
		RawMaterialData rawMaterial2 = new RawMaterialData();
		rawMaterial2.setName("Raw material 2");
		NodeRef rawMaterial2NodeRef = RepoBaseTestCase.INSTANCE.alfrescoRepository.create(testFolder, rawMaterial2).getNodeRef();
		LocalSemiFinishedProductData lSF1 = new LocalSemiFinishedProductData();
		lSF1.setName("Local semi finished 1");
		NodeRef lSF1NodeRef = RepoBaseTestCase.INSTANCE.alfrescoRepository.create(testFolder, lSF1).getNodeRef();

		LocalSemiFinishedProductData lSF2 = new LocalSemiFinishedProductData();
		lSF2.setName("Local semi finished 2");
		NodeRef lSF2NodeRef = RepoBaseTestCase.INSTANCE.alfrescoRepository.create(testFolder, lSF2).getNodeRef();
		
		LocalSemiFinishedProductData lSF3 = new LocalSemiFinishedProductData();
		lSF3.setName("Local semi finished 3");
		NodeRef lSF3NodeRef = RepoBaseTestCase.INSTANCE.alfrescoRepository.create(testFolder, lSF3).getNodeRef();
		
		LocalSemiFinishedProductData lSF4 = new LocalSemiFinishedProductData();
		lSF4.setName("Local semi finished 4");
		NodeRef lSF4NodeRef = RepoBaseTestCase.INSTANCE.alfrescoRepository.create(testFolder, lSF4).getNodeRef();

		/*-- Create finished product --*/
		logger.debug("/*-- Create finished product --*/");
		FinishedProductData finishedProduct = new FinishedProductData();
		finishedProduct.setName("Finished Product");
		finishedProduct.setHierarchy1(RepoBaseTestCase.INSTANCE.HIERARCHY1_FROZEN_REF);
		finishedProduct.setHierarchy2(RepoBaseTestCase.INSTANCE.HIERARCHY2_PIZZA_REF);
		List<CompoListDataItem> compoList = new LinkedList<CompoListDataItem>();
		CompoListDataItem parent1 = new CompoListDataItem(null, (CompoListDataItem)null, 1d, 1d, CompoListUnit.P, 0d, DeclarationType.Declare, lSF1NodeRef);
		CompoListDataItem child1 =new CompoListDataItem(null,parent1, 1d, 4d, CompoListUnit.P, 0d, DeclarationType.Declare, lSF2NodeRef);
		CompoListDataItem child12 =new CompoListDataItem(null,child1, 3d, 0d, CompoListUnit.kg, 0d, DeclarationType.Omit, rawMaterial1NodeRef);
		CompoListDataItem parent2 =new CompoListDataItem(null,(CompoListDataItem) null, 1d, 4d, CompoListUnit.P, 0d, DeclarationType.Declare, lSF3NodeRef);
		CompoListDataItem child2 =new CompoListDataItem(null, parent2, 3d, 0d, CompoListUnit.kg, 0d, DeclarationType.Omit, rawMaterial2NodeRef);
		CompoListDataItem child21 =new CompoListDataItem(null, parent2, 3d, 0d, CompoListUnit.kg, 0d, DeclarationType.Omit, lSF4NodeRef);
		CompoListDataItem parent3 = new CompoListDataItem(null, (CompoListDataItem)null, 3d, 0d, CompoListUnit.kg, 0d, DeclarationType.Omit, rawMaterial1NodeRef);
		
		compoList.add(parent1);
		compoList.add(child1);
		compoList.add(child12);
		compoList.add(parent2);
		compoList.add(child2);
		compoList.add(child21);
		compoList.add(parent3);
		

		finishedProduct.getCompoListView().setCompoList(compoList);
		return RepoBaseTestCase.INSTANCE.alfrescoRepository.create(testFolder, finishedProduct).getNodeRef();
		
	}
	
	
	
	public static void createUsers() {

		/*
		 * Matthieu : user Philippe : validators
		 */

		for (String group : groups) {

			if (!RepoBaseTestCase.INSTANCE.authorityService.authorityExists(PermissionService.GROUP_PREFIX + group)) {
				logger.debug("create group: " + group);
				RepoBaseTestCase.INSTANCE.authorityService.createAuthority(AuthorityType.GROUP, group);
			}
		}

		// USER_ONE
		NodeRef userOne = RepoBaseTestCase.INSTANCE.personService.getPerson(USER_ONE);
		if (userOne != null) {
			RepoBaseTestCase.INSTANCE.personService.deletePerson(userOne);
		}

		if (!RepoBaseTestCase.INSTANCE.authenticationDAO.userExists(USER_ONE)) {
			createUser(USER_ONE);
		}

		// USER_TWO
		NodeRef userTwo = RepoBaseTestCase.INSTANCE.personService.getPerson(USER_TWO);
		if (userTwo != null) {
			RepoBaseTestCase.INSTANCE.personService.deletePerson(userTwo);
		}

		if (!RepoBaseTestCase.INSTANCE.authenticationDAO.userExists(USER_TWO)) {
			createUser(USER_TWO);

			RepoBaseTestCase.INSTANCE.authorityService
					.addAuthority(PermissionService.GROUP_PREFIX + SystemGroup.QualityUser.toString(), USER_TWO);
		}

		for (String s : RepoBaseTestCase.INSTANCE.authorityService.getAuthoritiesForUser(USER_ONE)) {
			logger.debug("user in group: " + s);
		}

	}

	public static NodeRef createUser(String userName) {
		if (RepoBaseTestCase.INSTANCE.authenticationService.authenticationExists(userName) == false) {
			RepoBaseTestCase.INSTANCE.authenticationService.createAuthentication(userName, "PWD".toCharArray());

			PropertyMap ppOne = new PropertyMap(4);
			ppOne.put(ContentModel.PROP_USERNAME, userName);
			ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
			ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
			ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
			ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");

			return RepoBaseTestCase.INSTANCE.personService.createPerson(ppOne);
		} else {
			return RepoBaseTestCase.INSTANCE.personService.getPerson(userName);
		}
	}
	
	/**
	 * Create a raw material.
	 * 
	 * @param parentNodeRef
	 *            the parent node ref
	 * @param name
	 *            the name
	 * @return the node ref
	 */
	public static NodeRef createRawMaterial(NodeRef parentNodeRef, String name) {

		logger.debug("createRawMaterial");

		logger.debug("Create MP");
		RawMaterialData rawMaterial = new RawMaterialData();
		rawMaterial.setName(name);
		rawMaterial.setHierarchy1(RepoBaseTestCase.INSTANCE.HIERARCHY1_SEA_FOOD_REF);
		rawMaterial.setHierarchy2(RepoBaseTestCase.INSTANCE.HIERARCHY2_FISH_REF);
		

		// Allergens
		List<AllergenListDataItem> allergenList = new ArrayList<AllergenListDataItem>();
		for (int j = 0; j < RepoBaseTestCase.INSTANCE.allergens.size(); j++) {
			AllergenListDataItem allergenListItemData = new AllergenListDataItem(null, false, false, null, null, RepoBaseTestCase.INSTANCE.allergens.get(j), false);
			allergenList.add(allergenListItemData);
		}
		rawMaterial.setAllergenList(allergenList);

		// Costs
		List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
		for (int j = 0; j < RepoBaseTestCase.INSTANCE.costs.size(); j++) {
			CostListDataItem costListItemData = new CostListDataItem(null, 12.2d, "€/kg", null, RepoBaseTestCase.INSTANCE.costs.get(j), false);
			costList.add(costListItemData);
		}
		rawMaterial.setCostList(costList);

		// Ings
		List<IngListDataItem> ingList = new ArrayList<IngListDataItem>();
		for (int j = 0; j < RepoBaseTestCase.INSTANCE.ings.size(); j++) {
			IngListDataItem ingListItemData = new IngListDataItem(null, 12.2d, null, null, false, false,false, RepoBaseTestCase.INSTANCE.ings.get(j), false);
			ingList.add(ingListItemData);
		}
		rawMaterial.setIngList(ingList);

		// Nuts
		List<NutListDataItem> nutList = new ArrayList<NutListDataItem>();
		for (int j = 0; j < RepoBaseTestCase.INSTANCE.nuts.size(); j++) {
			NutListDataItem nutListItemData = new NutListDataItem(null, 2d, "kJ/100g", 0d, 0d, "Groupe 1", RepoBaseTestCase.INSTANCE.nuts.get(j), false);
			nutList.add(nutListItemData);
		}
		rawMaterial.setNutList(nutList);

		// Organos
		List<OrganoListDataItem> organoList = new ArrayList<OrganoListDataItem>();
		for (int j = 0; j < RepoBaseTestCase.INSTANCE.organos.size(); j++) {
			OrganoListDataItem organoListItemData = new OrganoListDataItem(null, "Descr organo....", RepoBaseTestCase.INSTANCE.organos.get(j));
			organoList.add(organoListItemData);
		}
		rawMaterial.setOrganoList(organoList);

		rawMaterial.setParentNodeRef(parentNodeRef);
		rawMaterial = (RawMaterialData) RepoBaseTestCase.INSTANCE.alfrescoRepository.save(rawMaterial);

		return rawMaterial.getNodeRef();

	}


	
	
}
