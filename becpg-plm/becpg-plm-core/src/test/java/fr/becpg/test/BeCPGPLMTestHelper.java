/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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

import fr.becpg.model.PLMGroup;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.OrganoListDataItem;

public class BeCPGPLMTestHelper {

	private static final Log logger = LogFactory.getLog(BeCPGPLMTestHelper.class);

	public static String PRODUCT_NAME = "Finished Product";

	public static final String USER_ONE = "matthieuWF";
	public static final String USER_TWO = "philippeWF";

	protected static final String[] groups = { PLMGroup.QualityUser.toString(), PLMGroup.QualityMgr.toString() };

	/** The PAT h_ testfolder. */
	public static final String PATH_TESTFOLDER = "TestFolder";

	public static NodeRef createTestFolder() {
		return createTestFolder(PATH_TESTFOLDER);
	}

	public static NodeRef createTestFolder(String folderName) {

		NodeRef folderNodeRef = PLMBaseTestCase.INSTANCE2.nodeService.getChildByName(PLMBaseTestCase.INSTANCE2.repositoryHelper.getCompanyHome(),
				ContentModel.ASSOC_CONTAINS, folderName);

		if (folderNodeRef == null) {
			folderNodeRef = PLMBaseTestCase.INSTANCE2.fileFolderService.create(PLMBaseTestCase.INSTANCE2.repositoryHelper.getCompanyHome(),
					folderName, ContentModel.TYPE_FOLDER).getNodeRef();
		}

		return folderNodeRef;
	}

	public static void createUsers() {

		/*
		 * Matthieu : user Philippe : validators
		 */

		for (String group : groups) {

			if (!PLMBaseTestCase.INSTANCE2.authorityService.authorityExists(PermissionService.GROUP_PREFIX + group)) {
				logger.debug("create group: " + group);
				PLMBaseTestCase.INSTANCE2.authorityService.createAuthority(AuthorityType.GROUP, group);
			}
		}

		// USER_ONE
		createUser(USER_ONE);
		createUser(USER_TWO);
		if(!PLMBaseTestCase.INSTANCE2.authorityService.getAuthoritiesForUser(USER_TWO).contains(PermissionService.GROUP_PREFIX + PLMGroup.QualityUser.toString())){
		
			PLMBaseTestCase.INSTANCE2.authorityService.addAuthority(PermissionService.GROUP_PREFIX + PLMGroup.QualityUser.toString(), USER_TWO);
		}

		for (String s : PLMBaseTestCase.INSTANCE2.authorityService.getAuthoritiesForUser(USER_ONE)) {
			logger.debug("user in group: " + s);
		}

	}

	public static NodeRef createUser(String userName) {

		if (!PLMBaseTestCase.INSTANCE2.authenticationService.authenticationExists(userName)) {
			PLMBaseTestCase.INSTANCE2.authenticationService.createAuthentication(userName, "PWD".toCharArray());

			NodeRef userOne = PLMBaseTestCase.INSTANCE2.personService.getPerson(userName);
			if (userOne == null) {
				PropertyMap ppOne = new PropertyMap(4);
				ppOne.put(ContentModel.PROP_USERNAME, userName);
				ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
				ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
				ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
				ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");
				return PLMBaseTestCase.INSTANCE2.personService.createPerson(ppOne);
			} else {
				return userOne;
			}
		} else {
			return PLMBaseTestCase.INSTANCE2.personService.getPerson(userName);
		}

	}

	public static NodeRef createMultiLevelProduct(NodeRef testFolder) {

		/*-- Create raw material --*/
		logger.debug("/*-- Create raw material --*/");
		RawMaterialData rawMaterial1 = new RawMaterialData();
		rawMaterial1.setName("Raw material 1");
		NodeRef rawMaterial1NodeRef = PLMBaseTestCase.INSTANCE2.alfrescoRepository.create(testFolder, rawMaterial1).getNodeRef();
		RawMaterialData rawMaterial2 = new RawMaterialData();
		rawMaterial2.setName("Raw material 2");
		NodeRef rawMaterial2NodeRef = PLMBaseTestCase.INSTANCE2.alfrescoRepository.create(testFolder, rawMaterial2).getNodeRef();
		LocalSemiFinishedProductData lSF1 = new LocalSemiFinishedProductData();
		lSF1.setName("Local semi finished 1");
		NodeRef lSF1NodeRef = PLMBaseTestCase.INSTANCE2.alfrescoRepository.create(testFolder, lSF1).getNodeRef();

		LocalSemiFinishedProductData lSF2 = new LocalSemiFinishedProductData();
		lSF2.setName("Local semi finished 2");
		NodeRef lSF2NodeRef = PLMBaseTestCase.INSTANCE2.alfrescoRepository.create(testFolder, lSF2).getNodeRef();

		LocalSemiFinishedProductData lSF3 = new LocalSemiFinishedProductData();
		lSF3.setName("Local semi finished 3");
		NodeRef lSF3NodeRef = PLMBaseTestCase.INSTANCE2.alfrescoRepository.create(testFolder, lSF3).getNodeRef();

		LocalSemiFinishedProductData lSF4 = new LocalSemiFinishedProductData();
		lSF4.setName("Local semi finished 4");
		NodeRef lSF4NodeRef = PLMBaseTestCase.INSTANCE2.alfrescoRepository.create(testFolder, lSF4).getNodeRef();

		/*-- Create finished product --*/
		logger.debug("/*-- Create finished product --*/");
		FinishedProductData finishedProduct = new FinishedProductData();
		finishedProduct.setName("Finished Product");
		finishedProduct.setHierarchy1(PLMBaseTestCase.INSTANCE2.HIERARCHY1_FROZEN_REF);
		finishedProduct.setHierarchy2(PLMBaseTestCase.INSTANCE2.HIERARCHY2_PIZZA_REF);
		List<CompoListDataItem> compoList = new LinkedList<>();
		CompoListDataItem parent1 = new CompoListDataItem(null, null, 1d, 1d, CompoListUnit.P, 0d, DeclarationType.Declare,
				lSF1NodeRef);
		CompoListDataItem child1 = new CompoListDataItem(null, parent1, 1d, 4d, CompoListUnit.P, 0d, DeclarationType.Declare, lSF2NodeRef);
		CompoListDataItem child12 = new CompoListDataItem(null, child1, 3d, 0d, CompoListUnit.kg, 0d, DeclarationType.Omit, rawMaterial1NodeRef);
		CompoListDataItem parent2 = new CompoListDataItem(null, null, 1d, 4d, CompoListUnit.P, 0d, DeclarationType.Declare,
				lSF3NodeRef);
		CompoListDataItem child2 = new CompoListDataItem(null, parent2, 3d, 0d, CompoListUnit.kg, 0d, DeclarationType.Omit, rawMaterial2NodeRef);
		CompoListDataItem child21 = new CompoListDataItem(null, parent2, 3d, 0d, CompoListUnit.kg, 0d, DeclarationType.Omit, lSF4NodeRef);
		CompoListDataItem parent3 = new CompoListDataItem(null,null, 3d, 0d, CompoListUnit.kg, 0d, DeclarationType.Omit,
				rawMaterial1NodeRef);

		compoList.add(parent1);
		compoList.add(child1);
		compoList.add(child12);
		compoList.add(parent2);
		compoList.add(child2);
		compoList.add(child21);
		compoList.add(parent3);

		finishedProduct.getCompoListView().setCompoList(compoList);
		return PLMBaseTestCase.INSTANCE2.alfrescoRepository.create(testFolder, finishedProduct).getNodeRef();

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
		rawMaterial.setHierarchy1(PLMBaseTestCase.INSTANCE2.HIERARCHY1_SEA_FOOD_REF);
		rawMaterial.setHierarchy2(PLMBaseTestCase.INSTANCE2.HIERARCHY2_FISH_REF);

		// Allergens
		List<AllergenListDataItem> allergenList = new ArrayList<>();
		for (int j = 0; j < PLMBaseTestCase.INSTANCE2.allergens.size(); j++) {
			AllergenListDataItem allergenListItemData = new AllergenListDataItem(null, null, false, false, null, null,
					PLMBaseTestCase.INSTANCE2.allergens.get(j), false);
			allergenList.add(allergenListItemData);
		}
		rawMaterial.setAllergenList(allergenList);

		// Costs
		List<CostListDataItem> costList = new ArrayList<>();
		for (int j = 0; j < PLMBaseTestCase.INSTANCE2.costs.size(); j++) {
			CostListDataItem costListItemData = new CostListDataItem(null, 12.2d, "€/kg", null, PLMBaseTestCase.INSTANCE2.costs.get(j), false);
			costList.add(costListItemData);
		}
		rawMaterial.setCostList(costList);

		// Ings
		List<IngListDataItem> ingList = new ArrayList<>();
		for (int j = 0; j < PLMBaseTestCase.INSTANCE2.ings.size(); j++) {
			IngListDataItem ingListItemData = new IngListDataItem(null, 12.2d, null, null, false, false, false,
					PLMBaseTestCase.INSTANCE2.ings.get(j), false);
			ingList.add(ingListItemData);
		}
		rawMaterial.setIngList(ingList);

		// Nuts
		List<NutListDataItem> nutList = new ArrayList<>();
		for (int j = 0; j < PLMBaseTestCase.INSTANCE2.nuts.size(); j++) {
			NutListDataItem nutListItemData = new NutListDataItem(null, 2d, "kJ/100g", 0d, 0d, "Groupe 1", PLMBaseTestCase.INSTANCE2.nuts.get(j),
					false);
			nutList.add(nutListItemData);
		}
		rawMaterial.setNutList(nutList);

		// Organos
		List<OrganoListDataItem> organoList = new ArrayList<>();
		for (int j = 0; j < PLMBaseTestCase.INSTANCE2.organos.size(); j++) {
			OrganoListDataItem organoListItemData = new OrganoListDataItem(null, "Descr organo....", PLMBaseTestCase.INSTANCE2.organos.get(j));
			organoList.add(organoListItemData);
		}
		rawMaterial.setOrganoList(organoList);

		rawMaterial.setParentNodeRef(parentNodeRef);
		rawMaterial = (RawMaterialData) PLMBaseTestCase.INSTANCE2.alfrescoRepository.save(rawMaterial);

		return rawMaterial.getNodeRef();

	}

}
