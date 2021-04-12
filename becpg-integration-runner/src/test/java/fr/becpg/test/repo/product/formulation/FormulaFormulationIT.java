/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ClientData;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
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

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
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

		inWriteTx(() -> {

			FinishedProductData finishedProductData2 = (FinishedProductData) alfrescoRepository.findOne(finishedProductData2NodeRef);
			FinishedProductData finishedProductData1 = (FinishedProductData) alfrescoRepository.findOne(finishedProductData1NodeRef);

			assertEquals("Client 1", finishedProductData1.getClients().get(0).getName());

			productService.formulate(finishedProductData2);

			logger.debug(finishedProductData2.toString());

			assertEquals("Test Spel Formula 1", MLTextHelper.getClosestValue(finishedProductData2.getLegalName(), Locale.FRENCH));
			assertEquals(1, finishedProductData2.getCostList().size());
			assertEquals(2, finishedProductData2.getCompoList().size());
			assertEquals("Client 1", finishedProductData2.getClients().get(0).getName());
			assertEquals(2, associationService.getTargetAssocs(finishedProductData2.getNodeRef(), PLMModel.ASSOC_SUPPLIERS).size());
			assertEquals(1, associationService.getTargetAssocs(finishedProductData2.getNodeRef(), PLMModel.ASSOC_CLIENTS).size());

			return finishedProductData2.getNodeRef();

		});

	}

}
