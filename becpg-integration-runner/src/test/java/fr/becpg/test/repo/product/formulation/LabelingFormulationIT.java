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
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.LabelingRuleType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.product.formulation.labeling.LabelingFormulaContext;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.regulatory.RequirementType;
import fr.becpg.test.BeCPGTestHelper;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

/**
 *
 * @author matthieu
 *
 */
public class LabelingFormulationIT extends AbstractFinishedProductTest {

	protected static final Log logger = LogFactory.getLog(LabelingFormulationIT.class);

	@Autowired
	private AssociationService associationService;

	@Autowired
	@Qualifier("mlAwareNodeService")
	private NodeService mlNodeService;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
	}

	private NodeRef createTestProduct(final List<LabelingRuleListDataItem> labelingRuleList) {
		return inWriteTx(() -> {

			/**
			 * Finished product 1
			 */
			logger.debug("/*************************************/");
			logger.debug("/*-- Create Finished product 1--*/");
			logger.debug("/*************************************/");
			FinishedProductData finishedProduct1 = new FinishedProductData();
			finishedProduct1.setName("Finished product " + Calendar.getInstance().getTimeInMillis());
			finishedProduct1.setLegalName("legal Finished product 1");
			finishedProduct1.setQty(2d);
			finishedProduct1.setUnit(ProductUnit.kg);
			finishedProduct1.setDensity(1d);
			List<CompoListDataItem> compoList1 = new ArrayList<>();
			compoList1.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF11NodeRef));
			compoList1.add(CompoListDataItem.build().withParent(compoList1.get(0)).withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial11NodeRef));
			compoList1.add(CompoListDataItem.build().withParent(compoList1.get(0)).withQtyUsed(2d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial12NodeRef));
			compoList1.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF12NodeRef));
			compoList1.add(CompoListDataItem.build().withParent(compoList1.get(3)).withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial13NodeRef));
			compoList1.add(CompoListDataItem.build().withParent(compoList1.get(3)).withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial14NodeRef));

			finishedProduct1.getCompoListView().setCompoList(compoList1);

			finishedProduct1.getLabelingListView().setLabelingRuleList(labelingRuleList);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();
		});
	}

	@Test
	public void testNullIng() {

		NodeRef finishedProductNodeRef1 = inWriteTx(() -> {
			FinishedProductData finishedProduct1 = new FinishedProductData();
			finishedProduct1.setName("Finished product " + Calendar.getInstance().getTimeInMillis());
			finishedProduct1.setLegalName("legal Finished product 1");
			finishedProduct1.setQty(2d);
			finishedProduct1.setUnit(ProductUnit.kg);
			finishedProduct1.setDensity(1d);
			List<CompoListDataItem> compoList1 = new ArrayList<>();
			compoList1.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial16NodeRef));

			finishedProduct1.getCompoListView().setCompoList(compoList1);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();
		});

		// └──[root - 0.0 (1.0)]
		// ├──[ing1 french - null]
		// ├──[ing3 french - 0.55]
		// └──[ing2 french - null]

		// Declare
		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Declare").withLabelingRuleType(LabelingRuleType.Declare)
				.withComponents(Collections.singletonList(rawMaterial16NodeRef)).withReplacements(null));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format));

		checkILL(finishedProductNodeRef1, labelingRuleList, "ing3 french 55%, ing1 french, ing2 french", Locale.FRENCH);

		labelingRuleList = new ArrayList<>();

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Declare").withLabelingRuleType(LabelingRuleType.Declare)
				.withComponents(Collections.singletonList(rawMaterial16NodeRef)).withReplacements(null));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format));
		labelingRuleList.add(
				LabelingRuleListDataItem.build().withName("Param1").withFormula("forceKeepOrder=true").withLabelingRuleType(LabelingRuleType.Prefs));
		checkILL(finishedProductNodeRef1, labelingRuleList, "ing1 french, ing3 french 55%, ing2 french", Locale.FRENCH);

		labelingRuleList = new ArrayList<>();

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Detail").withLabelingRuleType(LabelingRuleType.Detail)
				.withComponents(Collections.singletonList(rawMaterial16NodeRef)).withReplacements(null));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format));

		checkILL(finishedProductNodeRef1, labelingRuleList, "legal Raw material 16 100% (ing1 french, ing3 french 55%, ing2 french)", Locale.FRENCH);

		final NodeRef finishProduct1 = createTestProduct(null);

		final NodeRef finishProduct2 = createTestProduct(null);

		inWriteTx(() -> {

			FinishedProductData finishedProduct2 = (FinishedProductData) alfrescoRepository.findOne(finishProduct2);
			finishedProduct2.setLegalName("legal Finished product 2");
			alfrescoRepository.save(finishedProduct2);
			return null;
		});

		finishedProductNodeRef1 = inWriteTx(() -> {
			FinishedProductData finishedProduct1 = new FinishedProductData();
			finishedProduct1.setName("Finished product " + Calendar.getInstance().getTimeInMillis());
			finishedProduct1.setLegalName("legal Finished product 1");
			finishedProduct1.setQty(2d);
			finishedProduct1.setUnit(ProductUnit.kg);
			finishedProduct1.setDensity(1d);
			List<CompoListDataItem> compoList1 = new ArrayList<>();
			compoList1.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF11NodeRef));
			compoList1.add(CompoListDataItem.build().withParent(compoList1.get(0)).withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Kit).withProduct(finishProduct1));
			compoList1.add(CompoListDataItem.build().withParent(compoList1.get(0)).withQtyUsed(2d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Kit).withProduct(finishProduct2));
			compoList1.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF12NodeRef));
			compoList1.add(CompoListDataItem.build().withParent(compoList1.get(3)).withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial13NodeRef));
			compoList1.add(CompoListDataItem.build().withParent(compoList1.get(3)).withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial16NodeRef));

			finishedProduct1.getCompoListView().setCompoList(compoList1);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();
		});

		labelingRuleList = new ArrayList<>();
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("% group")
				.withFormula("groupDefaultFormat=\"<up>{0} ({1,number,0.#%}):</up> {2}\"").withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Aggr").withLabelingRuleType(LabelingRuleType.Group)
				.withComponents(Arrays.asList(rawMaterial13NodeRef, rawMaterial16NodeRef)));

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"AGGR (38,7%) : ing3 french 38,7%, ing1 french, ing2 french<br/>LEGAL FINISHED PRODUCT 2 (33,3%) : garniture french 50% (ing3 french 41,7%, ing4 french 8,3%), pâte french 50% (legal Raw material 12 33,3% (ing2 french 25%, ing1 french 8,3%), ing2 french 11,1%, ing1 french 5,6%)<br/>LEGAL FINISHED PRODUCT 1 (16,7%) : garniture french 50% (ing3 french 41,7%, ing4 french 8,3%), pâte french 50% (legal Raw material 12 33,3% (ing2 french 25%, ing1 french 8,3%), ing2 french 11,1%, ing1 french 5,6%)<br/>garniture french 11,2%",
				Locale.FRENCH);

		checkError(finishedProductNodeRef1, labelingRuleList,
				"Impossible de déclarer ou d'aggreger l'ingrédient ing2 sans quantité, changer le type de déclaration du composant");

	}

	@Test
	public void testIngsLabelingWithYield() {

		// En libellé légal:
		// A -> Rdm1
		// --- EAU
		// --- MP1
		// --- MP2
		//
		//

		NodeRef finishedProductNodeRef1 = inWriteTx(() -> {
			FinishedProductData finishedProduct1 = new FinishedProductData();
			finishedProduct1.setName("Test Yield 1 " + Calendar.getInstance().getTimeInMillis());
			finishedProduct1.setQty(2d);
			finishedProduct1.setUnit(ProductUnit.kg);

			List<CompoListDataItem> compoList1 = new ArrayList<>();
			compoList1.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.DoNotDetails).withProduct(rawMaterialWaterNodeRef));
			compoList1.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.DoNotDetails).withProduct(rawMaterial1NodeRef));
			compoList1.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.DoNotDetails).withProduct(rawMaterial2NodeRef));

			finishedProduct1.getCompoListView().setCompoList(compoList1);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();
		});

		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("ingsLabelingWithYield=true")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"legal Raw material 1 (<b>allergen1</b>) 50%, legal Raw material 2 (<b>allergen1</b>) 50%", Locale.FRENCH);

		finishedProductNodeRef1 = inWriteTx(() -> {
			FinishedProductData finishedProduct1 = new FinishedProductData();
			finishedProduct1.setName("Test Yield 1b " + Calendar.getInstance().getTimeInMillis());
			finishedProduct1.setQty(2d);
			finishedProduct1.setUnit(ProductUnit.kg);

			List<CompoListDataItem> compoList1 = new ArrayList<>();

			compoList1.add(CompoListDataItem.build().withQtyUsed(2d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF11NodeRef));
			compoList1.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.DoNotDetails).withProduct(rawMaterialWaterNodeRef));
			compoList1.add(CompoListDataItem.build().withParent(compoList1.get(0)).withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.DoNotDetails).withProduct(rawMaterial1NodeRef));
			compoList1.add(CompoListDataItem.build().withParent(compoList1.get(0)).withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.DoNotDetails).withProduct(rawMaterial2NodeRef));

			finishedProduct1.getCompoListView().setCompoList(compoList1);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();
		});

		labelingRuleList = new ArrayList<>();

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("ingsLabelingWithYield=true")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1bis").withFormula("detailsDefaultFormat = \"{0} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"pâte french 100% (legal Raw material 1 (<b>allergen1</b>) 50%, legal Raw material 2 (<b>allergen1</b>) 50%)", Locale.FRENCH);

		// En ingrédient:
		//		Qty	Meo	netQty	Rdmt	Qty perc	Qty with yield
		//		B	3	3	2	66,6666666666667	100	150
		//		MP2	2	3	2	44,4444444444444	66,6666666666667	150
		//		--- ing1	25,00 %			44,4	16,6666666666667	37,5375375375375
		//		--- ing2	75,00 %			44,4	50	112,612612612613
		//		C	1	3	2	44,4444444444444	33,3333333333333	75
		//		MP1	1	2	2	44,4444444444444	16,6666666666667	37,5
		//		-- ing1	33,00 %			44,4	5,5	12,3873873873874
		//		-- ing2	66,00 %			44,4	11	24,7747747747748
		//		EAU	1	2	2	44,4444444444444	16,6666666666667	37,5

		//

		finishedProductNodeRef1 = inWriteTx(() -> {
			FinishedProductData finishedProduct1 = new FinishedProductData();
			finishedProduct1.setName("Test Yield 2 " + Calendar.getInstance().getTimeInMillis());
			finishedProduct1.setQty(2d);
			finishedProduct1.setUnit(ProductUnit.kg);

			List<CompoListDataItem> compoList1 = new ArrayList<>();
			compoList1.add(CompoListDataItem.build().withQtyUsed(0.5d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterialWaterNodeRef));
			compoList1.add(CompoListDataItem.build().withQtyUsed(0.5d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterialWaterNodeRef));
			compoList1.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			compoList1.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial2NodeRef));

			finishedProduct1.getCompoListView().setCompoList(compoList1);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();
		});

		labelingRuleList = new ArrayList<>();

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("ingsLabelingWithYield=true")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		checkILL(finishedProductNodeRef1, labelingRuleList, "ing2 french 70,8%, ing1 french 29,2%", Locale.FRENCH);

		labelingRuleList = new ArrayList<>();
		//#
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("ingsLabelingWithYield=true")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Detail").withLabelingRuleType(LabelingRuleType.Detail)
				.withComponents(Collections.singletonList(rawMaterial1NodeRef)).withReplacements(null));

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"legal Raw material 1 50% (ing2 french 33,3%, ing1 french 16,7%), ing2 french 37,5%, ing1 french 12,5%", Locale.FRENCH);

		//
		// En ingrédient:
		// A -> Rdm1 Decl.
		// -- B -> Rdm2 Decl.
		// --- C
		// ---- MP1
		// ---- EAU
		// --- MP2

		NodeRef finishedProductNodeRefC = inWriteTx(() -> {
			FinishedProductData finishedProduct1 = new FinishedProductData();
			finishedProduct1.setName("Test yield Sub - C " + Calendar.getInstance().getTimeInMillis());
			finishedProduct1.setLegalName("Test yield Sub - C");
			finishedProduct1.setQty(2d);
			finishedProduct1.setUnit(ProductUnit.kg);

			List<CompoListDataItem> compoList1 = new ArrayList<>();
			compoList1.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterialWaterNodeRef));
			compoList1.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));

			finishedProduct1.getCompoListView().setCompoList(compoList1);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();
		});

		NodeRef finishedProductNodeRefB = inWriteTx(() -> {
			FinishedProductData finishedProduct1 = new FinishedProductData();
			finishedProduct1.setName("Test yield Sub - B " + Calendar.getInstance().getTimeInMillis());
			finishedProduct1.setLegalName("Test yield Sub - B");
			finishedProduct1.setQty(2d);
			finishedProduct1.setUnit(ProductUnit.kg);

			List<CompoListDataItem> compoList1 = new ArrayList<>();
			compoList1.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(finishedProductNodeRefC));
			compoList1.add(CompoListDataItem.build().withQtyUsed(2d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial2NodeRef));

			finishedProduct1.getCompoListView().setCompoList(compoList1);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();
		});

		finishedProductNodeRef1 = inWriteTx(() -> {
			FinishedProductData finishedProduct1 = new FinishedProductData();
			finishedProduct1.setName("Test yield 3 " + Calendar.getInstance().getTimeInMillis());
			finishedProduct1.setQty(2d);
			finishedProduct1.setUnit(ProductUnit.kg);

			List<CompoListDataItem> compoList1 = new ArrayList<>();
			compoList1.add(CompoListDataItem.build().withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(finishedProductNodeRefB));

			finishedProduct1.getCompoListView().setCompoList(compoList1);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();
		});

		labelingRuleList = new ArrayList<>();

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("ingsLabelingWithYield=true")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		// └──[root - 0.0 (200.00000000000006, vol: 200.00000000000006) ]
		// ├──[Test yield Sub - C 1476452333120 - 150.0 (88.8888888888889, vol:
		// 88.8888888888889) Detail]
		// │ ├──[eau - -11.111111111111112 ( vol : 100.0) ]
		// │ ├──[ing1 french - 33.333333333333336 ( vol : 33.333333333333336) ]
		// │ └──[ing2 french - 66.66666666666667 ( vol : 66.66666666666667) ]
		// ├──[ing1 french - 75.0 ( vol : 75.0) ]
		// └──[ing2 french - 225.0 ( vol : 225.0) ]

		checkILL(finishedProductNodeRef1, labelingRuleList, "ing2 french 137,5%, ing1 french 50%", Locale.FRENCH);

		labelingRuleList = new ArrayList<>();

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("ingsLabelingWithYield=true")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(
				LabelingRuleListDataItem.build().withName("Param2").withFormula("force100Perc=true").withLabelingRuleType(LabelingRuleType.Prefs));

		checkILL(finishedProductNodeRef1, labelingRuleList, "ing2 french 73,3%, ing1 french 26,7%", Locale.FRENCH);

		//
		// En group:
		// #2640

		// └──[root - 0.0 (19.0, vol: 20.9) ]
		// ├──[Test yield Sub - C - 16.0 (200.0, vol: 190.0) Group]
		// │ ├──[eau - 89.99999999999999 ( vol : 100.0) ]

		// │ ├──[ing1 french - 33.333333333333336 ( vol : 33.333333333333336) ]
		// │ └──[ing2 french - 66.66666666666667 ( vol : 66.66666666666667) ]
		// └──[Test yield Sub - B - 4.0 (200.0, vol: 190.0) Group]
		// ├──[eau - 13.333333333333329 ( vol : 50.0) ]
		// ├──[ing1 french - 66.66666666666667 ( vol : 66.66666666666667) ]
		// └──[ing2 french - 183.33333333333334 ( vol : 183.33333333333334) ]

		// C
		// - ing1 = 200÷190×160÷200×0.333333333÷2 --> 0,140350877
		// - ing2 = 200÷190×160÷200×0.666666666÷2 --> 0.28070
		// B
		// - ing1 = 200÷190×40÷200×0.666666666÷2 --> 0,070
		// - ing2 = 200÷190×40÷200×1.83÷2 --> 0.192

		finishedProductNodeRef1 = inWriteTx(() -> {
			FinishedProductData finishedProduct1 = new FinishedProductData();
			finishedProduct1.setName("Test yield 4 " + Calendar.getInstance().getTimeInMillis());
			finishedProduct1.setQty(190d);
			finishedProduct1.setUnit(ProductUnit.g);

			List<CompoListDataItem> compoList1 = new ArrayList<>();
			compoList1.add(CompoListDataItem.build().withQtyUsed(160d).withUnit(ProductUnit.g).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Group).withProduct(finishedProductNodeRefC));
			compoList1.add(CompoListDataItem.build().withQtyUsed(40d).withUnit(ProductUnit.g).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Group).withProduct(finishedProductNodeRefB));

			finishedProduct1.getCompoListView().setCompoList(compoList1);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();
		});

		labelingRuleList = new ArrayList<>();

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("ingsLabelingWithYield=true")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("% group")
				.withFormula("groupDefaultFormat=\"<b>{0} ({1,number,0.#%}):</b> {2}\"").withLabelingRuleType(LabelingRuleType.Prefs));

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"<b>test yield Sub - C (80%) :</b> eau 37,9%, ing2 french 28,1%, ing1 french 14%<br/><b>test yield Sub - B (20%) :</b> ing2 french 19,3%, ing1 french 7%",
				Locale.FRENCH);
		//<b>test yield Sub - C (80%) :</b> eau 35,8%, ing2 french 28,1%, ing1 french 14%<br/><b>test yield Sub - B (20%) :</b> ing2 french 19,3%, ing1 french 7%
		//<b>test yield Sub - C (84,2%) :</b> eau 35,8%, ing2 french 28,1%, ing1 french 14%<br/><b>test yield Sub - B (21,1%) :</b> ing2 french 19,3%, ing1 french 7%

		// #2944

		NodeRef finishedProductNodeRefD = inWriteTx(() -> {
			FinishedProductData finishedProduct1 = new FinishedProductData();
			finishedProduct1.setName("Test yield Sub - D " + Calendar.getInstance().getTimeInMillis());
			finishedProduct1.setLegalName("Test yield Sub - D");
			finishedProduct1.setQty(2d);
			finishedProduct1.setUnit(ProductUnit.kg);

			List<CompoListDataItem> compoList1 = new ArrayList<>();
			compoList1.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterialWaterNodeRef));
			compoList1.add(CompoListDataItem.build().withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));

			finishedProduct1.getCompoListView().setCompoList(compoList1);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();
		});

		finishedProductNodeRef1 = inWriteTx(() -> {
			FinishedProductData finishedProduct1 = new FinishedProductData();
			finishedProduct1.setName("Test yield 5 " + Calendar.getInstance().getTimeInMillis());
			finishedProduct1.setQty(190d);
			finishedProduct1.setUnit(ProductUnit.g);

			List<CompoListDataItem> compoList1 = new ArrayList<>();
			compoList1.add(CompoListDataItem.build().withQtyUsed(160d).withUnit(ProductUnit.g).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Group).withProduct(finishedProductNodeRefD));
			compoList1.add(CompoListDataItem.build().withQtyUsed(40d).withUnit(ProductUnit.g).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Group).withProduct(finishedProductNodeRefB));

			finishedProduct1.getCompoListView().setCompoList(compoList1);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();
		});

		labelingRuleList = new ArrayList<>();

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("ingsLabelingWithYield=true")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("% group")
				.withFormula("groupDefaultFormat=\"<b>{0} ({1,number,0.#%}):</b> {2}\"").withLabelingRuleType(LabelingRuleType.Prefs));

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"<b>test yield Sub - D (80%) :</b> ing2 french 84,2%, ing1 french 42,1%<br/><b>test yield Sub - B (20%) :</b> ing2 french 19,3%, ing1 french 7%",
				Locale.FRENCH);

		//#16323

		NodeRef sfProductNodeRef1 = inWriteTx(() -> {
			SemiFinishedProductData finishedProduct1 = new SemiFinishedProductData();
			finishedProduct1.setName("#16323 SF with yield " + Calendar.getInstance().getTimeInMillis());
			finishedProduct1.setUnit(ProductUnit.kg);

			List<CompoListDataItem> compoList1 = new ArrayList<>();
			compoList1.add(CompoListDataItem.build().withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.DoNotDetails).withProduct(rawMaterial1NodeRef));
			compoList1.add(CompoListDataItem.build().withQtyUsed(6d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.DoNotDetails).withProduct(rawMaterial2NodeRef));
			compoList1.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.DoNotDetails).withProduct(rawMaterialWaterNodeRef));

			finishedProduct1.getCompoListView().setCompoList(compoList1);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();
		});

		NodeRef sfProductNodeRef2 = inWriteTx(() -> {
			SemiFinishedProductData finishedProduct1 = new SemiFinishedProductData();
			finishedProduct1.setName("#16323 SF with no yield " + Calendar.getInstance().getTimeInMillis());
			finishedProduct1.setUnit(ProductUnit.kg);

			List<CompoListDataItem> compoList1 = new ArrayList<>();
			compoList1.add(CompoListDataItem.build().withQtyUsed(10d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.DoNotDetails).withProduct(rawMaterial3NodeRef));
			finishedProduct1.getCompoListView().setCompoList(compoList1);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();
		});

		finishedProductNodeRef1 = inWriteTx(() -> {
			FinishedProductData finishedProduct1 = new FinishedProductData();
			finishedProduct1.setName("Test #16323 " + Calendar.getInstance().getTimeInMillis());
			finishedProduct1.setUnit(ProductUnit.kg);

			List<CompoListDataItem> compoList1 = new ArrayList<>();

			CompoListDataItem yieldCompoListDataItem = CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(sfProductNodeRef1);
			yieldCompoListDataItem.setYieldPerc(90d);
			compoList1.add(yieldCompoListDataItem);
			compoList1.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(sfProductNodeRef2));

			finishedProduct1.getCompoListView().setCompoList(compoList1);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();
		});

		labelingRuleList = new ArrayList<>();

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("").withLabelingRuleType(LabelingRuleType.ShowPerc));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("ingsLabelingWithYield=true")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"legal Raw material 3 (<b>allergen3</b>) 50%, legal Raw material 2 (<b>allergen1</b>) 33,3%, legal Raw material 1 (<b>allergen1</b>) 16,7%",
				Locale.FRENCH);

	}

	@Test
	public void testMultiLevelSFGroup() {

		final NodeRef finishProduct1 = createTestProduct(null);

		final NodeRef finishProduct2 = createTestProduct(null);
		inWriteTx(() -> {

			FinishedProductData finishedProduct2 = (FinishedProductData) alfrescoRepository.findOne(finishProduct2);
			finishedProduct2.setLegalName("legal Finished product 2");
			alfrescoRepository.save(finishedProduct2);
			return null;
		});

		NodeRef finishedProductNodeRef1 = inWriteTx(() -> {
			FinishedProductData finishedProduct1 = new FinishedProductData();
			finishedProduct1.setName("Finished product " + Calendar.getInstance().getTimeInMillis());
			finishedProduct1.setLegalName("legal Finished product 1");
			finishedProduct1.setQty(2d);
			finishedProduct1.setUnit(ProductUnit.kg);
			finishedProduct1.setDensity(1d);
			List<CompoListDataItem> compoList1 = new ArrayList<>();
			compoList1.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF11NodeRef));
			compoList1.add(CompoListDataItem.build().withParent(compoList1.get(0)).withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Group).withProduct(finishProduct1));
			compoList1.add(CompoListDataItem.build().withParent(compoList1.get(0)).withQtyUsed(2d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Group).withProduct(finishProduct2));
			compoList1.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF12NodeRef));
			compoList1.add(CompoListDataItem.build().withParent(compoList1.get(3)).withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial13NodeRef));
			compoList1.add(CompoListDataItem.build().withParent(compoList1.get(3)).withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial14NodeRef));
			finishedProduct1.getCompoListView().setCompoList(compoList1);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();
		});

		// Declare
		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("% group")
				.withFormula("groupDefaultFormat=\"<b>{0} ({1,number,0.#%}):</b> {2}\"").withLabelingRuleType(LabelingRuleType.Prefs));

		// └──[root - 0.0 (2.0) ]
		// ├──[garniture french - 1.0 (6.0) Detail]
		// │ ├──[legal Raw material 13 - 3.0 (3.0) Detail]
		// │ │ └──[ing3 french - 3.0]
		// │ ├──[ing3 french - 2.0]
		// │ └──[ing4 french - 1.0]
		// ├──[legal Finished product 1 - 0.3333333333333333 (2.0) Group]
		// │ ├──[pâte french - 1.0 (3.0) Detail]
		// │ │ ├──[ing1 french - 0.33333333333333337]
		// │ │ ├──[ing2 french - 0.6666666666666667]
		// │ │ └──[legal Raw material 12 - 2.0 (2.0) Detail]
		// │ │ ├──[ing1 french - 0.5]
		// │ │ └──[ing2 french - 1.5]
		// │ └──[garniture french - 1.0 (6.0) Detail]
		// │ ├──[ing3 french - 5.0]
		// │ └──[ing4 french - 1.0]
		// └──[legal Finished product 1 - 0.6666666666666666 (2.0) Group]
		// ├──[pâte french - 1.0 (3.0) Detail]
		// │ ├──[ing1 french - 0.33333333333333337]
		// │ ├──[ing2 french - 0.6666666666666667]
		// │ └──[legal Raw material 12 - 2.0 (2.0) Detail]
		// │ ├──[ing1 french - 0.5]
		// │ └──[ing2 french - 1.5]
		// └──[garniture french - 1.0 (6.0) Detail]
		// ├──[ing3 french - 5.0]
		// └──[ing4 french - 1.0]

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"<b>legal Finished product 2 (33,3%) :</b> garniture french 16,7% (ing3 french 13,9%, ing4 french 2,8%), pâte french 16,7% (legal Raw material 12 11,1% (ing2 french 8,3%, ing1 french 2,8%), ing2 french 3,7%, ing1 french 1,9%)<br/><b>legal Finished product 1 (16,7%) :</b> garniture french 8,3% (ing3 french 6,9%, ing4 french 1,4%), pâte french 8,3% (legal Raw material 12 5,6% (ing2 french 4,2%, ing1 french 1,4%), ing2 french 1,9%, ing1 french 0,9%)<br/>garniture french 50% (legal Raw material 13 25% (ing3 french 25%), ing3 french 16,7%, ing4 french 8,3%)",
				Locale.FRENCH);

		labelingRuleList = new ArrayList<>();

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format));

		labelingRuleList.add(LabelingRuleListDataItem.build().withName("% group")
				.withFormula("groupDefaultFormat=\"<b>{0} ({1,number,0.#%}):</b> {2}\"").withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Aggr").withLabelingRuleType(LabelingRuleType.Group)
				.withComponents(Arrays.asList(rawMaterial13NodeRef, rawMaterial14NodeRef)).withReplacements(null));

		// └──[root - 0.0 (2.0) ]
		// ├──[Aggr - 1.0 (6.0) Group]
		// │ ├──[legal Raw material 13 - 3.0 (3.0) Detail]
		// │ │ └──[ing3 french - 3.0]
		// │ ├──[ing3 french - 2.0]
		// │ └──[ing4 french - 1.0]
		// ├──[legal Finished product 1 - 0.16666666666666666 (1.0) Group]
		// │ └──[pâte french - 1.0 (3.0) Detail]
		// │ ├──[ing1 french - 0.33333333333333337]
		// │ ├──[ing2 french - 0.6666666666666667]
		// │ └──[legal Raw material 12 - 2.0 (2.0) Detail]
		// │ ├──[ing1 french - 0.5]
		// │ └──[ing2 french - 1.5]
		// └──[legal Finished product 1 - 0.3333333333333333 (1.0) Group]
		// └──[pâte french - 1.0 (3.0) Detail]
		// ├──[ing1 french - 0.33333333333333337]
		// ├──[ing2 french - 0.6666666666666667]
		// └──[legal Raw material 12 - 2.0 (2.0) Detail]
		// ├──[ing1 french - 0.5]
		// └──[ing2 french - 1.5]

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"<b>aggr (50%) :</b> legal Raw material 13 25% (ing3 french 25%), ing3 french 16,7%, ing4 french 8,3%<br/><b>legal Finished product 2 (16,7%) :</b> pâte french 16,7% (legal Raw material 12 11,1% (ing2 french 8,3%, ing1 french 2,8%), ing2 french 3,7%, ing1 french 1,9%)<br/><b>legal Finished product 1 (8,3%) :</b> pâte french 8,3% (legal Raw material 12 5,6% (ing2 french 4,2%, ing1 french 1,4%), ing2 french 1,9%, ing1 french 0,9%)",
				Locale.FRENCH);

	}

	@Test
	public void testIngTypeDecl() {

		/** Do not detail ingType */

		final NodeRef finishedProductNodeRef = inWriteTx(() -> {
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 4");
			finishedProduct.setLegalName("Legal Produit fini 4");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(4d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();

			compoList.add(CompoListDataItem.build().withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial7NodeRef));
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));

			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		});

		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("DoNotDetail").withLabelingRuleType(LabelingRuleType.DoNotDetails)
				.withComponents(Collections.singletonList(ingType1)).withReplacements(null));

		// └──[root - 0.0 (11.0, vol: 11.0) ]
		// ├──[ing5 french - 5.0 (10.0, vol: 10.0) Detail]
		// │ ├──[ing1 french - 7.0 ( vol : 7.0) ]
		// │ └──[ing4 french - 3.0 ( vol : 3.0) ]
		// └──[Juice - 6.0 (6.0, vol: 6.0) Detail]
		// ├──[ing1 french - 2.0 ( vol : 2.0) ]
		// └──[ing2 french - 4.0 ( vol : 4.0) ]

		checkILL(finishedProductNodeRef, labelingRuleList, "epaississant french, ing2 french 16,7%, ing1 french 8,3%", Locale.FRENCH);

	}

	@Test
	public void testShowPerc() {

		/** Do not detail ingType */

		final NodeRef finishedProductNodeRef = inWriteTx(() -> {
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 4");
			finishedProduct.setLegalName("Legal Produit fini 4");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(4d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();

			compoList.add(CompoListDataItem.build().withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial7NodeRef));
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));

			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		});

		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("%").withFormula("#.#%|HALF_DOWN").withLabelingRuleType(LabelingRuleType.ShowPerc));

		// └──[root - 0.0 (11.0, vol: 11.0) ]
		// ├──[ing5 french - 5.0 (10.0, vol: 10.0) Detail]
		// │ ├──[ing1 french - 7.0 ( vol : 7.0) ]
		// │ └──[ing4 french - 3.0 ( vol : 3.0) ]
		// └──[Juice - 6.0 (6.0, vol: 6.0) Detail]
		// ├──[ing1 french - 2.0 ( vol : 2.0) ]
		// └──[ing2 french - 4.0 ( vol : 4.0) ]

		checkILL(finishedProductNodeRef, labelingRuleList,
				"epaississant french : ing5 french 75% (ing1 french 52,5%, ing4 french 22,5%), ing2 french 16,7%, ing1 french 8,3%", Locale.FRENCH);

		labelingRuleList = new ArrayList<>();
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("#.#%|UP").withLabelingRuleType(LabelingRuleType.ShowPerc));

		checkILL(finishedProductNodeRef, labelingRuleList,
				"epaississant french : ing5 french 75% (ing1 french 52,5%, ing4 french 22,5%), ing2 french 16,7%, ing1 french 8,4%", Locale.FRENCH);

		labelingRuleList = new ArrayList<>();
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("#.#%").withLabelingRuleType(LabelingRuleType.ShowPerc));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Pref1").withFormula("defaultRoundingMode=T(java.math.RoundingMode).UP")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		checkILL(finishedProductNodeRef, labelingRuleList,
				"epaississant french : ing5 french 75% (ing1 french 52,5%, ing4 french 22,5%), ing2 french 16,7%, ing1 french 8,4%", Locale.FRENCH);

		labelingRuleList = new ArrayList<>();
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("#.#%|DOWN").withLabelingRuleType(LabelingRuleType.ShowPerc)
				.withComponents(Arrays.asList(ing2)));

		checkILL(finishedProductNodeRef, labelingRuleList,
				"epaississant french : ing5 french (ing1 french, ing4 french), ing2 french 16,6%, ing1 french", Locale.FRENCH);

		labelingRuleList = new ArrayList<>();
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("#.#%|HALF_UP||NEAREST_HALF_DOWN")
				.withLabelingRuleType(LabelingRuleType.ShowPerc).withComponents(Arrays.asList(ing2)));

		checkILL(finishedProductNodeRef, labelingRuleList,
				"epaississant french : ing5 french (ing1 french, ing4 french), ing2 french 16,5%, ing1 french", Locale.FRENCH);

		labelingRuleList = new ArrayList<>();
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("#.#%|HALF_UP||NEAREST_HALF_UP")
				.withLabelingRuleType(LabelingRuleType.ShowPerc).withComponents(Arrays.asList(ing2)));

		checkILL(finishedProductNodeRef, labelingRuleList,
				"epaississant french : ing5 french (ing1 french, ing4 french), ing2 french 17%, ing1 french", Locale.FRENCH);

	}

	@Test
	public void testIngDeclType() {

		/** Do not detail ingType */

		final NodeRef finishedProductNodeRef = inWriteTx(() -> {
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 4");
			finishedProduct.setLegalName("Legal Produit fini 4");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(4d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();

			compoList.add(CompoListDataItem.build().withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial7NodeRef));
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));

			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		});

		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("%").withFormula("#.#%|HALF_DOWN").withLabelingRuleType(LabelingRuleType.ShowPerc));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Decl ing5 ").withLabelingRuleType(LabelingRuleType.Declare)
				.withComponents(Arrays.asList(ing5)));

		// └──[root - 0.0 (11.0, vol: 11.0) ]
		// ├──[ing5 french - 5.0 (10.0, vol: 10.0) Detail]
		// │ ├──[ing1 french - 7.0 ( vol : 7.0) ]
		// │ └──[ing4 french - 3.0 ( vol : 3.0) ]
		// └──[Juice - 6.0 (6.0, vol: 6.0) Detail]
		// ├──[ing1 french - 2.0 ( vol : 2.0) ]
		// └──[ing2 french - 4.0 ( vol : 4.0) ]

		checkILL(finishedProductNodeRef, labelingRuleList, "ing1 french 60,8%, ing4 french 22,5%, ing2 french 16,7%", Locale.FRENCH);
		labelingRuleList = new ArrayList<>();

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("%").withFormula("#.#%|HALF_DOWN").withLabelingRuleType(LabelingRuleType.ShowPerc));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Decl ing5 ").withLabelingRuleType(LabelingRuleType.Declare)
				.withComponents(Arrays.asList(ing5)).withReplacements(null));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("DoNotDecl ing1 ").withFormula("ingListDataItem.isProcessingAid == true")
				.withLabelingRuleType(LabelingRuleType.DoNotDeclare));

		checkILL(finishedProductNodeRef, labelingRuleList, "ing4 french 22,5%, ing2 french 16,7%, ing1 french 8,3%", Locale.FRENCH);

		labelingRuleList = new ArrayList<>();

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("%").withFormula("#.#%|HALF_DOWN").withLabelingRuleType(LabelingRuleType.ShowPerc));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Decl ing5 ").withLabelingRuleType(LabelingRuleType.Declare)
				.withComponents(Arrays.asList(ing5)).withReplacements(null));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Omit ing1 ").withFormula("ingListDataItem.isProcessingAid == true")
				.withLabelingRuleType(LabelingRuleType.Omit));

		checkILL(finishedProductNodeRef, labelingRuleList, "ing4 french 75%, ing2 french 16,7%, ing1 french 8,3%", Locale.FRENCH);
	}

	@Test
	public void testReconstitutionLabeling() {
		// 1. Liste d'ingrédients par ordre pondéral
		//
		// Les sucres et l'acide : les pourcentages pondéraux du saccharose et
		// de l'acide citrique sont calculés sur la matière sèche (mise en œuvre
		// de sucre sec et d'acide citrique en poudre)
		// le pourcentage pondéral du glucose est calculé sur le glucose liquide
		// (mise en œuvre d'un sirop de glucose)
		// (voir colonne %P/P avant ou après reconstitution)
		//
		// Les jus : Les jus mis en œuvre sont des jus concentrés, le
		// pourcentage pondéral est calculé après reconstitution des jus (voir
		// colonne %P/P après reconstitution des jus)
		//
		// 2. Déclaration des jus La réglementation des sirops impose une
		// déclaration de la liste d'ingrédients par ordre pondéral avec
		// reconstitution des jus,
		// et déclaration de la quantité de jus reconstitués totale et de la
		// quantité du jus reconstitué de la dénomination (ici la fraise) en
		// pourcentage volume / volume
		// (voir colonne %P/P avant reconstitution des jus)
		// Avec l'exemple du cas pratique sirop de fraise :
		// sucre, sirop de glucose-fructose, eau, jus de fruits à base de
		// concentrés 13% ( fraise 10%, sureau), acidifiant: acide citrique,
		// arôme, colorant: E 129.
		//

		/** FULL Reconstitution **/

		final NodeRef finishedProductNodeRef1 = inWriteTx(() -> {
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setLegalName("Legal Produit fini 1");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(4d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();

			compoList.add(CompoListDataItem.build().withQtyUsed(10d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial7NodeRef));
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));

			Map<QName, Serializable> props = new HashMap<>();
			props.put(PLMModel.PROP_RECONSTITUTION_RATE, 5d);
			nodeService.addAspect(rawMaterial1NodeRef, PLMModel.ASPECT_RECONSTITUTABLE, props);
			associationService.update(rawMaterial1NodeRef, PLMModel.ASSOC_DILUENT_REF, ing5);
			associationService.update(rawMaterial1NodeRef, PLMModel.ASSOC_TARGET_RECONSTITUTION_REF, ing6);

			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		});

		// Declare
		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Pref2").withFormula("ingDefaultFormat = \"{0} {1,number,0.#%}\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Pref3")
				.withFormula("groupDefaultFormat = \"<b>{0} ({1,number,0.#%}):</b> {2}\"").withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Pref4").withFormula("detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Pref5").withFormula("ingTypeDefaultFormat = \"{0}: {2}\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Pref6").withFormula("subIngsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format));

		// └──[root - 0.0 (11.0, vol: 11.0) ]
		// ├──[ing5 french - 5.0 (10.0, vol: 10.0) Detail]
		// │ ├──[ing1 french - 7.0 ( vol : 7.0) ]
		// │ └──[ing4 french - 3.0 ( vol : 3.0) ]
		// └──[Juice - 6.0 (6.0, vol: 6.0) Detail]
		// ├──[ing1 french - 2.0 ( vol : 2.0) ]
		// └──[ing2 french - 4.0 ( vol : 4.0) ]

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"epaississant french : ing5 french 54,5% (ing1 french 38,2%, ing4 french 16,4%), epices french : ing6 french 45,5%", Locale.FRENCH);

		/** Partial Reconstitution **/

		final NodeRef finishedProductNodeRef2 = inWriteTx(() -> {
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 2");
			finishedProduct.setLegalName("Legal Produit fini 2");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(4d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();

			compoList.add(CompoListDataItem.build().withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial7NodeRef));
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));

			Map<QName, Serializable> props = new HashMap<>();
			props.put(PLMModel.PROP_RECONSTITUTION_RATE, 5d);
			nodeService.addAspect(rawMaterial1NodeRef, PLMModel.ASPECT_RECONSTITUTABLE, props);
			associationService.update(rawMaterial1NodeRef, PLMModel.ASSOC_DILUENT_REF, ing5);
			associationService.update(rawMaterial1NodeRef, PLMModel.ASSOC_TARGET_RECONSTITUTION_REF, ing6);

			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		});

		// └──[root - 0.0 (11.0, vol: 11.0) ]
		// ├──[ing5 french - 5.0 (10.0, vol: 10.0) Detail]
		// │ ├──[ing1 french - 7.0 ( vol : 7.0) ]
		// │ └──[ing4 french - 3.0 ( vol : 3.0) ]
		// └──[Juice - 6.0 (6.0, vol: 6.0) Detail]
		// ├──[ing1 french - 2.0 ( vol : 2.0) ]
		// └──[ing2 french - 4.0 ( vol : 4.0) ]

		checkILL(finishedProductNodeRef2, labelingRuleList, "epices french : ing6 french 93,7%, legal Raw material 1 (<b>allergen1</b>) 6,2%",
				Locale.FRENCH);

		/** Test with priority **/

		final NodeRef finishedProductNodeRef3 = inWriteTx(() -> {
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 3");
			finishedProduct.setLegalName("Legal Produit fini 3");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(4d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();

			compoList.add(CompoListDataItem.build().withQtyUsed(5d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial7NodeRef));
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial2NodeRef));

			Map<QName, Serializable> props = new HashMap<>();
			props.put(PLMModel.PROP_RECONSTITUTION_RATE, 5d);
			nodeService.addAspect(rawMaterial1NodeRef, PLMModel.ASPECT_RECONSTITUTABLE, props);
			associationService.update(rawMaterial1NodeRef, PLMModel.ASSOC_DILUENT_REF, ing5);
			associationService.update(rawMaterial1NodeRef, PLMModel.ASSOC_TARGET_RECONSTITUTION_REF, ing6);
			props = new HashMap<>();
			props.put(PLMModel.PROP_RECONSTITUTION_RATE, 5d);
			props.put(PLMModel.PROP_RECONSTITUTION_PRIORITY, 2);
			nodeService.addAspect(rawMaterial2NodeRef, PLMModel.ASPECT_RECONSTITUTABLE, props);
			associationService.update(rawMaterial2NodeRef, PLMModel.ASSOC_DILUENT_REF, ing5);
			associationService.update(rawMaterial2NodeRef, PLMModel.ASSOC_TARGET_RECONSTITUTION_REF, ing4);

			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		});

		checkILL(finishedProductNodeRef3, labelingRuleList, "ing4 french 71,4%, epices french, legal Raw material 1 (<b>allergen1</b>) 10,7%",
				Locale.FRENCH);

		// Test allergen rules
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("PrefAllergen").withFormula("disableAllergensForLocales = \"fr\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Langue").withFormula("fr,en").withLabelingRuleType(LabelingRuleType.Locale));

		checkILL(finishedProductNodeRef3, labelingRuleList, "ing4 french 71,4%, epices french, legal Raw material 1 10,7%", Locale.FRENCH);
		checkILL(finishedProductNodeRef3, labelingRuleList, "ing4 english 71.4%, epices english, legal Raw material 1 (<b>allergen1</b>) 10.7%",
				Locale.ENGLISH);

		labelingRuleList.add(LabelingRuleListDataItem.build().withName("PrefAllergen2").withFormula("disableAllergensForLocales = \"*\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		checkILL(finishedProductNodeRef3, labelingRuleList, "ing4 english 71.4%, epices english, legal Raw material 1 10.7%", Locale.ENGLISH);
	}

	@Test
	public void testRenderAllergens() {

		final NodeRef finishedProductNodeRef = inWriteTx(() -> {
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 2");
			finishedProduct.setLegalName("Legal Produit fini 2");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(4d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();

			compoList.add(CompoListDataItem.build().withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial7NodeRef));
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial2NodeRef));

			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		});

		// Declare
		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();
		labelingRuleList.add(
				LabelingRuleListDataItem.build().withName("Rendu").withFormula("renderAllergens()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Rendu2").withFormula("renderInvoluntaryAllergens()")
				.withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Rendu3").withFormula("renderInvoluntaryAllergenInProcess()")
				.withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Rendu4").withFormula("renderInvoluntaryInRawMaterial()")
				.withLabelingRuleType(LabelingRuleType.Render));

		checkILL(finishedProductNodeRef, labelingRuleList, "allergen1", Locale.FRENCH, "Rendu");
		checkILL(finishedProductNodeRef, labelingRuleList, "allergen2", Locale.FRENCH, "Rendu2");
		checkILL(finishedProductNodeRef, labelingRuleList, "", Locale.FRENCH, "Rendu3");
		checkILL(finishedProductNodeRef, labelingRuleList, "allergen2", Locale.FRENCH, "Rendu4");

	}

	@Test
	public void testSPELFormula() {

		final NodeRef finishedProductNodeRef = inWriteTx(() -> {
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 2");
			finishedProduct.setLegalName("Legal Produit fini 2");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(4d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();

			compoList.add(CompoListDataItem.build().withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial7NodeRef));
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial2NodeRef));

			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		});

		// Declare
		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("entity.name+\"-\"+renderAllergens()")
				.withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Rendu2").withFormula("var a=3;\n#a+\"-\"+renderInvoluntaryAllergens()")
				.withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Rendu3").withFormula("locale").withLabelingRuleType(LabelingRuleType.Render));

		checkILL(finishedProductNodeRef, labelingRuleList, "Produit fini 2-allergen1", Locale.FRENCH, "Rendu");
		checkILL(finishedProductNodeRef, labelingRuleList, "3-allergen2", Locale.FRENCH, "Rendu2");
		checkILL(finishedProductNodeRef, labelingRuleList, "fr", Locale.FRENCH, "Rendu3");
	}

	@Test
	public void testRawMaterialIngType() {

		final NodeRef finishedProductNodeRef1 = inWriteTx(() -> {
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setLegalName("Legal Produit fini 1");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(4d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();

			compoList.add(CompoListDataItem.build().withQtyUsed(10d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.DoNotDetails).withProduct(rawMaterial7NodeRef));
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));

			Map<QName, Serializable> props = new HashMap<>();
			props.put(PLMModel.PROP_ING_TYPE_V2, ingType2);
			nodeService.addAspect(rawMaterial7NodeRef, PLMModel.ASPECT_ING_TYPE, props);

			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		});

		// Declare
		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Pref2").withFormula("ingDefaultFormat = \"{0} {1,number,0.#%}\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Pref3")
				.withFormula("groupDefaultFormat = \"<b>{0} ({1,number,0.#%}):</b> {2}\"").withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Pref4").withFormula("detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Pref5").withFormula("ingTypeDefaultFormat = \"{0}: {2};\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Pref6").withFormula("subIngsDefaultFormat = \"{0} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format));

		checkILL(finishedProductNodeRef1, labelingRuleList, "epices french : legal Raw material 7 90,9% ;, ing2 french 6,1%, ing1 french 3%",
				Locale.FRENCH);

		inWriteTx(() -> {
			nodeService.removeAspect(rawMaterial7NodeRef, PLMModel.ASPECT_ING_TYPE);
			return null;
		});
	}

	@Test
	public void testThresholdRules() {

		final NodeRef finishedProductNodeRef1 = inWriteTx(() -> {
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setLegalName("Legal Produit fini 1");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(4d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();

			compoList.add(CompoListDataItem.build().withQtyUsed(10d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.DoNotDetails).withProduct(rawMaterial7NodeRef));
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));

			Map<QName, Serializable> props = new HashMap<>();
			props.put(PLMModel.PROP_ING_TYPE_V2, ingType2);
			nodeService.addAspect(rawMaterial7NodeRef, PLMModel.ASPECT_ING_TYPE, props);

			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		});

		// Declare
		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Pref2").withFormula("ingDefaultFormat = \"{0} {1,number,0.#%}\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Pref3")
				.withFormula("groupDefaultFormat = \"<b>{0} ({1,number,0.#%}):</b> {2}\"").withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Pref4").withFormula("detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Pref5").withFormula("ingTypeDefaultFormat = \"{0}: {2};\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Pref6").withFormula("subIngsDefaultFormat = \"{0} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format));

		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Threshold").withFormula("6.2")
				.withLabelingRuleType(LabelingRuleType.DeclareThreshold).withComponents(Arrays.asList(ing2)));

		checkILL(finishedProductNodeRef1, labelingRuleList, "epices french : legal Raw material 7 90,9% ;, ing1 french 3%", Locale.FRENCH);

		labelingRuleList = new ArrayList<>();

		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Pref2").withFormula("ingDefaultFormat = \"{0} {1,number,0.#%}\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Pref3")
				.withFormula("groupDefaultFormat = \"<b>{0} ({1,number,0.#%}):</b> {2}\"").withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Pref4").withFormula("detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Pref5").withFormula("ingTypeDefaultFormat = \"{0}: {2};\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Pref6").withFormula("subIngsDefaultFormat = \"{0} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format));

		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Threshold").withFormula("6.0")
				.withLabelingRuleType(LabelingRuleType.DeclareThreshold).withComponents(Arrays.asList(ing2)));

		checkILL(finishedProductNodeRef1, labelingRuleList, "epices french : legal Raw material 7 90,9% ;, ing2 french 6,1%, ing1 french 3%",
				Locale.FRENCH);

		labelingRuleList = new ArrayList<>();

		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Pref2").withFormula("ingDefaultFormat = \"{0} {1,number,0.#%}\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Pref3")
				.withFormula("groupDefaultFormat = \"<b>{0} ({1,number,0.#%}):</b> {2}\"").withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Pref4").withFormula("detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Pref5").withFormula("ingTypeDefaultFormat = \"{0}: {2};\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Pref6").withFormula("subIngsDefaultFormat = \"{0} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format));

		labelingRuleList.add(
				LabelingRuleListDataItem.build().withName("Threshold").withFormula("6.2").withLabelingRuleType(LabelingRuleType.DeclareThreshold));

		checkILL(finishedProductNodeRef1, labelingRuleList, "epices french : legal Raw material 7 90,9% ;", Locale.FRENCH);

		inWriteTx(() -> {
			nodeService.removeAspect(rawMaterial7NodeRef, PLMModel.ASPECT_ING_TYPE);
			return null;
		});
	}

	@Test
	public void testAggregateAndRename() {

		// Par défaut
		// └──[root - 0.0 (2.0)]
		// ├──[pâte french - 1.0 (3.0)]
		// │ ├──[ing1 french - 0.33333333333333337]
		// │ ├──[ing2 french - 0.6666666666666667]
		// │ └──[legal Raw material 12 - 2.0 (2.0)]
		// │ ├──[ing1 french - 0.5]
		// │ └──[ing2 french - 1.5]
		// └──[garniture french - 1.0 (6.0)]
		// ├──[ing3 french - 5.0]
		// └──[ing4 french - 1.0]

		final NodeRef finishedProductNodeRef1 = inWriteTx(() -> {
			logger.debug("/*-- Create finished product --*/");

			// Create finished product using builders
			FinishedProductData finishedProduct = FinishedProductData.build().withName("Aggr Etiq Test 1").withLegalName("Aggr Etiq Test 1")
					.withUnit(ProductUnit.kg).withQty(3d).withDensity(1d)
					.withCompoList(Arrays.asList(
							CompoListDataItem.build().withQtyUsed(2d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.DoNotDetails)
									.withProduct(rawMaterial1NodeRef),
							CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.DoNotDetails)
									.withProduct(rawMaterial2NodeRef)));

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		});

		// Declare
		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));

		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format).withComponents(Arrays.asList(rawMaterial1NodeRef, rawMaterial2NodeRef)));

		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Aggregate 1").withLabelingRuleType(LabelingRuleType.DoNotDetails)
				.withComponents(Arrays.asList(rawMaterial1NodeRef, rawMaterial2NodeRef))
				.withReplacements(Collections.singletonList(rawMaterial2NodeRef)));

		checkILL(finishedProductNodeRef1, labelingRuleList, "legal Raw material 2 (<b>allergen1</b>) 100%", Locale.FRENCH);

		labelingRuleList.clear();

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));

		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format).withComponents(Arrays.asList(rawMaterial1NodeRef, rawMaterial2NodeRef)));

		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Rename 1").withLabelingRuleType(LabelingRuleType.Rename)
				.withComponents(Arrays.asList(rawMaterial1NodeRef)).withReplacements(Collections.singletonList(rawMaterial2NodeRef)));

		checkILL(finishedProductNodeRef1, labelingRuleList, "legal Raw material 2 (<b>allergen1</b>) 100%", Locale.FRENCH);

		labelingRuleList.clear();

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));

		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format).withComponents(Arrays.asList(ing2)));

		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Declare 1").withLabelingRuleType(LabelingRuleType.Declare)
				.withComponents(Arrays.asList(rawMaterial1NodeRef, rawMaterial2NodeRef)));

		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Aggregate 1").withLabelingRuleType(LabelingRuleType.DoNotDetails)
				.withComponents(Arrays.asList(ing1, ing2)).withReplacements(Collections.singletonList(ing2)));

		checkILL(finishedProductNodeRef1, labelingRuleList, "ing2 french 100%", Locale.FRENCH);
	}

	@Test
	public void testAggregateWithYield() {

		final NodeRef semiFinishedProductNodeRef1 = inWriteTx(() -> {
			List<CompoListDataItem> compoList = Arrays.asList(
					CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
							.withProduct(rawMaterial1NodeRef),
					CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
							.withProduct(rawMaterial2NodeRef));

			SemiFinishedProductData semiFinishedProduct = SemiFinishedProductData.build().withName("SF with yield").withUnit(ProductUnit.kg)
					.withQty(1.8d).withDensity(1d).withCompoList(compoList);

			return alfrescoRepository.create(getTestFolderNodeRef(), semiFinishedProduct).getNodeRef();
		});

		final NodeRef finishedProductNodeRef1 = inWriteTx(() -> {
			List<CompoListDataItem> compoList = Collections.singletonList(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg)
					.withDeclarationType(DeclarationType.Declare).withProduct(semiFinishedProductNodeRef1));

			FinishedProductData finishedProduct = FinishedProductData.build().withName("Aggr Etiq Test 1").withLegalName("Aggr Etiq Test 1")
					.withUnit(ProductUnit.kg).withQty(1d).withDensity(1d).withCompoList(compoList);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		});

		// Declare
		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));

		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Show Perc").withLabelingRuleType(LabelingRuleType.ShowPerc));

		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("ingsLabelingWithYield=true")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Aggregate 1").withFormula("100").withLabelingRuleType(LabelingRuleType.Detail)
				.withComponents(Arrays.asList(ing1)).withReplacements(Collections.singletonList(ing3)));

		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Aggregate 1 - 2").withFormula("37.8")
				.withLabelingRuleType(LabelingRuleType.Detail).withComponents(Arrays.asList(ing2)).withReplacements(Collections.singletonList(ing3)));

		//32,4+ 78,7*0.38

		checkILL(finishedProductNodeRef1, labelingRuleList, "ing3 french 62,2% (ing1 french 32,4%, ing2 french 29,7%), ing2 french 49%",
				Locale.FRENCH);
	}

	/**
	 * Test ingredients calculating.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testCalculateILL() {

		// Par défaut
		// └──[root - 0.0 (2.0)]
		// ├──[pâte french - 1.0 (3.0)]
		// │ ├──[ing1 french - 0.33333333333333337]
		// │ ├──[ing2 french - 0.6666666666666667]
		// │ └──[legal Raw material 12 - 2.0 (2.0)]
		// │ ├──[ing1 french - 0.5]
		// │ └──[ing2 french - 1.5]
		// └──[garniture french - 1.0 (6.0)]
		// ├──[ing3 french - 5.0]
		// └──[ing4 french - 1.0]

		final NodeRef finishedProductNodeRef1 = createTestProduct(null);

		// Declare
		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Declare").withLabelingRuleType(LabelingRuleType.Declare)
				.withComponents(Arrays.asList(localSF11NodeRef, rawMaterial12NodeRef, localSF12NodeRef)).withReplacements(null));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format).withComponents(Arrays.asList(ing1, ing2, ing3, ing4)));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		// └──[root - 0.0 (9.0)]
		// ├──[ing1 french - 0.8333333333333334]
		// ├──[ing2 french - 2.166666666666667]
		// ├──[ing3 french - 5.0]
		// └──[ing4 french - 1.0]

		checkILL(finishedProductNodeRef1, labelingRuleList, "ing3 french 55,6%, ing2 french 24,1%, ing4 french 11,1%, ing1 french 9,3%",
				Locale.FRENCH);

		// Omit
		labelingRuleList = new ArrayList<>();
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Omit").withLabelingRuleType(LabelingRuleType.Omit)
				.withComponents(Collections.singletonList(localSF12NodeRef)).withReplacements(null));
		// Ing2 dans rawMaterial 12 est un auxiliare
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Auxiliare").withFormula("ingListDataItem.isProcessingAid == true")
				.withLabelingRuleType(LabelingRuleType.Omit));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format).withComponents(Arrays.asList(ing1, ing2, ing3, ing4)));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		// └──[root - 0.0 (1.0)]
		// └──[pâte french - 1.0 (3.0)]
		// ├──[ing1 french - 0.33333333333333337]
		// ├──[ing2 french - 0.6666666666666667]
		// └──[legal Raw material 12 - 2.0 (2.0)]
		// └──[ing1 french - 0.5]

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"pâte french 100% (legal Raw material 12 66,7% (ing1 french 66,7%), ing2 french 22,2%, ing1 french 11,1%)", Locale.FRENCH);

		// Do not declare
		labelingRuleList = new ArrayList<>();
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Omit").withLabelingRuleType(LabelingRuleType.Omit)
				.withComponents(Collections.singletonList(localSF12NodeRef)).withReplacements(null));
		// Ing2 dans rawMaterial 12 est un auxiliare
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Auxiliare").withFormula("ingListDataItem.isProcessingAid == true")
				.withLabelingRuleType(LabelingRuleType.DoNotDeclare));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format).withComponents(Arrays.asList(ing1, ing2, ing3, ing4)));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		// └──[root - 0.0 (1.0)]
		// └──[pâte french - 1.0 (3.0)]
		// ├──[ing1 french - 0.33333333333333337]
		// ├──[ing2 french - 0.6666666666666667]
		// └──[legal Raw material 12 - 2.0 (2.0)]
		// └──[ing1 french - 0.5]

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"pâte french 100% (legal Raw material 12 66,7% (ing1 french 16,7%), ing2 french 22,2%, ing1 french 11,1%)", Locale.FRENCH);

		// Test Do not Declare IngType
		labelingRuleList = new ArrayList<>();
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Aggregate 2").withLabelingRuleType(LabelingRuleType.DoNotDetails)
				.withComponents(Collections.singletonList(ing4)).withReplacements(Collections.singletonList(ing5)));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Do not declare").withLabelingRuleType(LabelingRuleType.DoNotDeclare)
				.withComponents(Collections.singletonList(ingType1)).withReplacements(null));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		//
		// └──[root - 0.0 (2.0)]
		// ├──[pâte french - 1.0 (3.0)]
		// │ ├──[ing1 french - 0.33333333333333337]
		// │ ├──[ing2 french - 0.6666666666666667]
		// │ └──[legal Raw material 12 - 2.0 (2.0)]
		// │ ├──[ing1 french - 0.5]
		// │ └──[ing2 french - 1.5]
		// └──[garniture french - 1.0 (6.0)]
		// ├──[ing3 french - 5.0]
		// └──[ing5 french - 1.0]

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"garniture french 50% (ing3 french, ing5 french), pâte french 50% (legal Raw material 12 33,3% (ing2 french, ing1 french), ing2 french, ing1 french)",
				Locale.FRENCH);

		// Test Omit IngType
		labelingRuleList = new ArrayList<>();
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Aggregate 2").withLabelingRuleType(LabelingRuleType.DoNotDetails)
				.withComponents(Collections.singletonList(ing4)).withReplacements(Collections.singletonList(ing5)));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Omit").withLabelingRuleType(LabelingRuleType.Omit)
				.withComponents(Collections.singletonList(ingType1)).withReplacements(null));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		//
		// └──[root - 0.0 (2.0)]
		// ├──[pâte french - 1.0 (3.0)]
		// │ ├──[ing1 french - 0.33333333333333337]
		// │ ├──[ing2 french - 0.6666666666666667]
		// │ └──[legal Raw material 12 - 2.0 (2.0)]
		// │ ├──[ing1 french - 0.5]
		// │ └──[ing2 french - 1.5]
		// └──[garniture french - 1.0 (6.0)]
		// ├──[ing3 french - 5.0]
		// └──[ing5 french - 1.0]

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"garniture french 50% (ing3 french), pâte french 50% (legal Raw material 12 33,3% (ing2 french, ing1 french), ing2 french, ing1 french)",
				Locale.FRENCH);

		// Details
		labelingRuleList = new ArrayList<>();
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Details").withLabelingRuleType(LabelingRuleType.Detail)
				.withComponents(Collections.singletonList(rawMaterial11NodeRef)).withReplacements(null));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format).withComponents(Arrays.asList(ing1, ing2, ing3, ing4)));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		// └──[root - 0.0 (2.0)]
		// ├──[pâte french - 1.0 (3.0)]
		// │ ├──[legal Raw material 11 - 1.0 (1.0)]
		// │ │ ├──[ing1 french - 0.33333333333333337]
		// │ │ └──[ing2 french - 0.6666666666666667]
		// │ └──[legal Raw material 12 - 2.0 (2.0)]
		// │ ├──[ing1 french - 0.5]
		// │ └──[ing2 french - 1.5]
		// └──[garniture french - 1.0 (6.0)]
		// ├──[ing3 french - 5.0]
		// └──[ing4 french - 1.0]

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"garniture french 50% (ing3 french 41,7%, ing4 french 8,3%), pâte french 50% (legal Raw material 12 33,3% (ing2 french 25%, ing1 french 8,3%), legal Raw material 11 16,7% (ing2 french 11,1%, ing1 french 5,6%))",
				Locale.FRENCH);

		// Do not details
		labelingRuleList = new ArrayList<>();
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("DoNotDetails").withLabelingRuleType(LabelingRuleType.DoNotDetails)
				.withComponents(Collections.singletonList(rawMaterial11NodeRef)).withReplacements(null));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format).withComponents(Arrays.asList(ing1, ing2, ing3, ing4)));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		// └──[root - 0.0 (2.0)]
		// ├──[pâte french - 1.0 (3.0)]
		// │ ├──[legal Raw material 11 - 1.0 (1.0)]
		// │ └──[legal Raw material 12 - 2.0 (2.0)]
		// │ ├──[ing1 french - 0.5]
		// │ └──[ing2 french - 1.5]
		// └──[garniture french - 1.0 (6.0)]
		// ├──[ing3 french - 5.0]
		// └──[ing4 french - 1.0]

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"garniture french 50% (ing3 french 41,7%, ing4 french 8,3%), pâte french 50% (legal Raw material 12 33,3% (ing2 french 25%, ing1 french 8,3%), legal Raw material 11)",

				Locale.FRENCH);

		// Rename
		labelingRuleList = new ArrayList<>();
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Rename 1").withLabelingRuleType(LabelingRuleType.Rename)
				.withComponents(Collections.singletonList(ing1)).withReplacements(Collections.singletonList(ing5)));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Aggregate 2").withLabelingRuleType(LabelingRuleType.DoNotDetails)
				.withComponents(Collections.singletonList(ing4)).withReplacements(Collections.singletonList(ing5)));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Rename 2").withFormula("Test rename2")
				.withLabelingRuleType(LabelingRuleType.Rename).withComponents(Collections.singletonList(rawMaterial12NodeRef)));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Rename 3").withFormula("path.allergens")
				.withLabelingRuleType(LabelingRuleType.Rename).withComponents(Collections.singletonList(ingType1)));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		// └──[root - 0.0 (2.0)]
		// ├──[pâte french - 1.0 (3.0)]
		// │ ├──[ing1 french - 0.33333333333333337]
		// │ ├──[ing2 french - 0.6666666666666667]
		// │ └──[legal Raw material 12 - 2.0 (2.0)]
		// │ ├──[ing1 french - 0.5]
		// │ └──[ing2 french - 1.5]
		// └──[garniture french - 1.0 (6.0)]
		// ├──[ing3 french - 5.0]
		// └──[ing5 french - 1.0]

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"garniture french 50% (ing3 french, allergènes : ing5 french), pâte french 50% (test rename2 33,3% (ing2 french, ing5 french), ing2 french, ing5 french)",
				Locale.FRENCH);

		// Aggregate
		labelingRuleList = new ArrayList<>();
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Aggregate 1").withLabelingRuleType(LabelingRuleType.DoNotDetails)
				.withComponents(Arrays.asList(ing1, ing2)).withReplacements(Collections.singletonList(ing3)));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Aggregate 2").withLabelingRuleType(LabelingRuleType.DoNotDetails)
				.withComponents(Collections.singletonList(ing4)).withReplacements(Collections.singletonList(ing5)));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		// └──[root - 0.0 (2.0)]
		// ├──[pâte french - 1.0 (3.0)]
		// │ ├──[ing3 french - 1.0]
		// │ └──[legal Raw material 12 - 2.0 (2.0)]
		// │ └──[ing3 french - 2.0]
		// └──[garniture french - 1.0 (6.0)]
		// ├──[ing3 french - 5.0]
		// └──[ing5 french - 1.0]

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"garniture french 50% (ing3 french, epaississant french : ing5 french), pâte french 50% (legal Raw material 12 33,3% (ing3 french), ing3 french)",
				Locale.FRENCH);

		// Separator tests and plural

		labelingRuleList = new ArrayList<>();
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Aggregate 1").withLabelingRuleType(LabelingRuleType.DoNotDetails)
				.withComponents(Arrays.asList(ing1, ing2)).withReplacements(Collections.singletonList(ing3)));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Aggregate 2").withLabelingRuleType(LabelingRuleType.DoNotDetails)
				.withComponents(Collections.singletonList(ing4)).withReplacements(Collections.singletonList(ing5)));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Change type").withLabelingRuleType(LabelingRuleType.Type)
				.withComponents(Collections.singletonList(ing3)).withReplacements(Collections.singletonList(ingType1)));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param2").withFormula("defaultSeparator = \"; \"")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param3").withFormula("ingTypeDefaultSeparator = \"# \"")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param4").withFormula("groupDefaultSeparator = \"! \"")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"garniture french 50% (epaississants french : ing3 french# ing5 french) ; pâte french 50% (legal Raw material 12 33,3% (epaississant french : ing3 french) ; epaississant french : ing3 french)",
				Locale.FRENCH);

		// Combine

		labelingRuleList = new ArrayList<>();
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Combine 1").withLabel(new MLText("Comb 1")).withFormula("20,30")
				.withLabelingRuleType(LabelingRuleType.Detail).withComponents(Arrays.asList(ing1, ing2)));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format).withComponents(Arrays.asList(ing1, ing2, ing3, ing4)));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		// └──[root - 0.0 (2.0)]
		// ├──[pâte french - 1.0 (3.0)]
		// │ ├──[ing1 french - 0.2666666666666667]
		// │ ├──[ing2 french - 0.4666666666666667]
		// │ ├──[legal Raw material 12 - 2.0 (2.0)]
		// │ │ ├──[ing1 french - 0.4]
		// │ │ ├──[ing2 french - 1.05]
		// │ │ └──[Comb 1 - 0.55 (0.55)]
		// │ │ ├──[ing1 french - 0.1]
		// │ │ └──[ing2 french - 0.45]
		// │ └──[Comb 1 - 0.2666666666666667 (0.2666666666666667)]
		// │ ├──[ing1 french - 0.06666666666666668]
		// │ └──[ing2 french - 0.20000000000000004]
		// └──[garniture french - 1.0 (6.0)]
		// ├──[ing3 french - 5.0]
		// └──[ing4 french - 1.0]

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"garniture french 50% (ing3 french 41,7%, ing4 french 8,3%), pâte french 50% (legal Raw material 12 33,3% (ing2 french 17,5%, comb 1 9,2% (ing2 french 7,5%, ing1 french 1,7%), ing1 french 6,7%), ing2 french 7,8%, comb 1 4,4% (ing2 french 3,3%, ing1 french 1,1%), ing1 french 4,4%)",
				Locale.FRENCH);

		labelingRuleList = new ArrayList<>();
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));

		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Combine 1").withLabel(new MLText("Comb 1")).withFormula("100,30")
				.withLabelingRuleType(LabelingRuleType.Detail).withComponents(Arrays.asList(ing1, ing2)));

		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format).withComponents(Arrays.asList(ing1, ing2, ing3, ing4)));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		// └──[root - 0.0 (2.0)]
		// ├──[pâte french - 1.0 (3.0)]
		// │ ├──[ing2 french - 0.4666666666666667]
		// │ ├──[legal Raw material 12 - 2.0 (2.0)]
		// │ │ ├──[ing2 french - 1.05]
		// │ │ └──[Comb 1 - 0.95 (0.95)]
		// │ │ ├──[ing1 french - 0.5]
		// │ │ └──[ing2 french - 0.45]
		// │ └──[Comb 1 - 0.5333333333333334 (0.5333333333333334)]
		// │ ├──[ing1 french - 0.33333333333333337]
		// │ └──[ing2 french - 0.20000000000000004]
		// └──[garniture french - 1.0 (6.0)]
		// ├──[ing3 french - 5.0]
		// └──[ing4 french - 1.0]

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"garniture french 50% (ing3 french 41,7%, ing4 french 8,3%), pâte french 50% (legal Raw material 12 33,3% (ing2 french 17,5%, comb 1 15,8% (ing1 french 8,3%, ing2 french 7,5%)), comb 1 8,9% (ing1 french 5,6%, ing2 french 3,3%), ing2 french 7,8%)",
				Locale.FRENCH);

		labelingRuleList = new ArrayList<>();
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));

		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Combine 2").withLabel(new MLText("Decors 1"))
				.withLabelingRuleType(LabelingRuleType.Group).withComponents(Arrays.asList(localSF11NodeRef, localSF12NodeRef)));

		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("% group")
				.withFormula("groupDefaultFormat=\"<b>{0} ({1,number,0.#%}):</b> {2}\"").withLabelingRuleType(LabelingRuleType.Prefs));

		// └──[root - 0.0 (2.0)]
		// └──[Decors 1 - 2.0 (2.0)]
		// ├──[pâte french - 1.0 (3.0)]
		// │ ├──[ing1 french - 0.33333333333333337]
		// │ ├──[ing2 french - 0.6666666666666667]
		// │ └──[legal Raw material 12 - 2.0 (2.0)]
		// │ ├──[ing1 french - 0.5]
		// │ └──[ing2 french - 1.5]
		// └──[garniture french - 1.0 (6.0)]
		// ├──[ing3 french - 5.0]
		// └──[ing4 french - 1.0]

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"<b>decors 1 (100%) :</b> garniture french 50% (ing3 french, ing4 french), pâte french 50% (legal Raw material 12 33,3% (ing2 french, ing1 french), ing2 french, ing1 french)",
				Locale.FRENCH);

		// Group
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(
				LabelingRuleListDataItem.build().withName("Rendu").withFormula("renderGroupList()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Group 1").withLabelingRuleType(LabelingRuleType.Group)
				.withComponents(Collections.singletonList(localSF11NodeRef)).withReplacements(null));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Group 2").withLabelingRuleType(LabelingRuleType.Group)
				.withComponents(Collections.singletonList(localSF12NodeRef)).withReplacements(null));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format).withComponents(Arrays.asList(ing1, ing2, ing3, ing4)));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("% grouplist")
				.withFormula("groupListDefaultFormat = \"<b>{0} {1,number,0.#%}</b>\"").withLabelingRuleType(LabelingRuleType.Prefs));

		// └──[root - 0.0 (2.0)]
		// ├──[pâte french - 1.0 (3.0)]
		// │ ├──[ing1 french - 0.33333333333333337]
		// │ ├──[ing2 french - 0.6666666666666667]
		// │ └──[legal Raw material 12 - 2.0 (2.0)]
		// │ ├──[ing1 french - 0.5]
		// │ └──[ing2 french - 1.5]
		// └──[garniture french - 1.0 (6.0)]
		// ├──[ing3 french - 5.0]
		// └──[ing4 french - 1.0]

		checkILL(finishedProductNodeRef1, labelingRuleList, "<b>garniture french 50%</b>, <b>pâte french 50%</b>", Locale.FRENCH);

		labelingRuleList = new ArrayList<>();
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu 1").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Group 1").withLabelingRuleType(LabelingRuleType.Group)
				.withComponents(Collections.singletonList(localSF11NodeRef)).withReplacements(null));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Group 2").withLabelingRuleType(LabelingRuleType.Group)
				.withComponents(Collections.singletonList(localSF12NodeRef)).withReplacements(null));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format).withComponents(Arrays.asList(ing1, ing2, ing3, ing4)));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("% group")
				.withFormula("groupDefaultFormat=\"<b>{0} ({1,number,0.#%}):</b> {2}\"").withLabelingRuleType(LabelingRuleType.Prefs));

		// └──[root - 0.0 (2.0)]
		// ├──[pâte french - 1.0 (3.0)]
		// │ ├──[ing1 french - 0.33333333333333337]
		// │ ├──[ing2 french - 0.6666666666666667]
		// │ └──[legal Raw material 12 - 2.0 (2.0)]
		// │ ├──[ing1 french - 0.5]
		// │ └──[ing2 french - 1.5]
		// └──[garniture french - 1.0 (6.0)]
		// ├──[ing3 french - 5.0]
		// └──[ing4 french - 1.0]

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"<b>garniture french (50%) :</b> ing3 french 41,7%, ing4 french 8,3%<br/><b>pâte french (50%) :</b> legal Raw material 12 33,3% (ing2 french 25%, ing1 french 8,3%), ing2 french 11,1%, ing1 french 5,6%",
				Locale.FRENCH);

		labelingRuleList = new ArrayList<>();
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu 2").withFormula("render(false)").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Group 1").withLabelingRuleType(LabelingRuleType.Group)
				.withComponents(Collections.singletonList(localSF11NodeRef)).withReplacements(null));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Group 2").withLabelingRuleType(LabelingRuleType.Group)
				.withComponents(Collections.singletonList(localSF12NodeRef)).withReplacements(null));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format).withComponents(Arrays.asList(ing1, ing2, ing3, ing4)));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		// └──[root - 0.0 (2.0)]
		// ├──[ing1 french - 0.11111111111111112]
		// ├──[ing2 french - 0.22222222222222224]
		// ├──[legal Raw material 12 - 0.6666666666666666 (2.0)]
		// │ ├──[ing1 french - 0.5]
		// │ └──[ing2 french - 1.5]
		// ├──[ing3 french - 0.8333333333333334]
		// └──[ing4 french - 0.16666666666666666]

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"ing3 french 41,7%, legal Raw material 12 33,3% (ing2 french 25%, ing1 french 8,3%), ing2 french 11,1%, ing4 french 8,3%, ing1 french 5,6%",
				Locale.FRENCH);

		labelingRuleList = new ArrayList<>();
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu 2").withFormula("render(false)").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Aggregate 1").withLabelingRuleType(LabelingRuleType.DoNotDetails)
				.withComponents(Collections.singletonList(ing1)).withReplacements(Collections.singletonList(ing3)));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Group 1").withLabelingRuleType(LabelingRuleType.Group)
				.withComponents(Collections.singletonList(localSF11NodeRef)).withReplacements(null));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Group 2").withLabelingRuleType(LabelingRuleType.Group)
				.withComponents(Collections.singletonList(localSF12NodeRef)).withReplacements(null));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format).withComponents(Arrays.asList(ing1, ing2, ing3, ing4)));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		// └──[root - 0.0 (2.0)]
		// ├──[ing1 french - 0.11111111111111112]
		// ├──[ing2 french - 0.22222222222222224]
		// ├──[legal Raw material 12 - 0.6666666666666666 (2.0)]
		// │ ├──[ing1 french - 0.5]
		// │ └──[ing2 french - 1.5]
		// ├──[ing3 french - 0.8333333333333334]
		// └──[ing4 french - 0.16666666666666666]

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"ing3 french 47,2%, legal Raw material 12 33,3% (ing2 french 25%, ing3 french 8,3%), ing2 french 11,1%, ing4 french 8,3%",
				Locale.FRENCH);

		// Sub Ingredients ing5 (ing1,ing4)

		labelingRuleList = new ArrayList<>();
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		inWriteTx(() -> {
			ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef1);
			formulatedProduct.getCompoListView().getCompoList().get(2).setProduct(rawMaterial7NodeRef);
			alfrescoRepository.save(formulatedProduct);
			return null;
		});

		// └──[root - 0.0 (2.0)]
		// ├──[pâte french - 1.0 (3.0)]
		// │ ├──[ing1 french - 0.33333333333333337]
		// │ ├──[ing2 french - 0.6666666666666667]
		// │ └──[legal Raw material 7 - 2.0 (2.0)]
		// │ └──[ing5 french - 2.0]
		// └──[garniture french - 1.0 (6.0)]
		// ├──[ing3 french - 5.0]
		// └──[ing4 french - 1.0]

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"garniture french 50% (ing3 french, ing4 french), pâte french 50% (legal Raw material 7 33,3% (epaississant french : ing5 french (ing1 french, ing4 french)), ing2 french, ing1 french)",
				Locale.FRENCH);

		// MultiLevel

		labelingRuleList = new ArrayList<>();
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render(false)").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format));
		final NodeRef finishProduct2 = createTestProduct(null);

		inWriteTx(() -> {
			productService.formulate(finishProduct2);
			ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef1);
			formulatedProduct.setQty(7d);
			formulatedProduct.getCompoListView().getCompoList().get(2).setProduct(finishProduct2);
			formulatedProduct.getCompoListView().getCompoList().add(CompoListDataItem.build().withParent(null).withQty(5d).withQtyUsed(null)
					.withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Group).withProduct(finishProduct2));
			alfrescoRepository.save(formulatedProduct);
			return null;
		});

		// └──[root - 0.0 (7.0, vol: null) ]
		// ├──[pâte french - 1.0 (3.0, vol: null) Detail]
		// │ ├──[ing1 french - 0.33333333333333337 ( vol : null) ]
		// │ ├──[ing2 french - 0.6666666666666667 ( vol : null) ]
		// │ └──[legal Finished product 1 - 2.0 (2.0, vol: null) Detail]
		// │ ├──[pâte french - 1.0 (3.0, vol: null) Detail]
		// │ │ ├──[ing1 french - 0.33333333333333337 ( vol : null) ]
		// │ │ ├──[ing2 french - 0.6666666666666667 ( vol : null) ]
		// │ │ └──[legal Raw material 12 - 2.0 (2.0, vol: null) Detail]
		// │ │ ├──[ing1 french - 0.5 ( vol : null) ]
		// │ │ └──[ing2 french - 1.5 ( vol : null) ]
		// │ └──[garniture french - 1.0 (6.0, vol: null) Detail]
		// │ ├──[ing3 french - 5.0 ( vol : null) ]
		// │ └──[ing4 french - 1.0 ( vol : null) ]
		// ├──[garniture french - 1.0 (6.0, vol: null) Detail]
		// │ ├──[ing3 french - 5.0 ( vol : null) ]
		// │ └──[ing4 french - 1.0 ( vol : null) ]
		// └──[legal Finished product 1 - 5.0 (2.0, vol: null) Group]
		// ├──[pâte french - 1.0 (3.0, vol: null) Detail]
		// │ ├──[ing1 french - 0.33333333333333337 ( vol : null) ]
		// │ ├──[ing2 french - 0.6666666666666667 ( vol : null) ]
		// │ └──[legal Raw material 12 - 2.0 (2.0, vol: null) Detail]
		// │ ├──[ing1 french - 0.5 ( vol : null) ]
		// │ └──[ing2 french - 1.5 ( vol : null) ]
		// └──[garniture french - 1.0 (6.0, vol: null) Detail]
		// ├──[ing3 french - 5.0 ( vol : null) ]
		// └──[ing4 french - 1.0 ( vol : null) ]

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"garniture french 50% (ing3 french 41,7%, ing4 french 8,3%), pâte french 50% (legal Finished product 1 33,3% (garniture french 16,7% (ing3 french 13,9%, ing4 french 2,8%), pâte french 16,7% (legal Raw material 12 11,1% (ing2 french 8,3%, ing1 french 2,8%), ing2 french 3,7%, ing1 french 1,9%)), ing2 french 11,1%, ing1 french 5,6%)",
				Locale.FRENCH);

		// Declare multilevel
		labelingRuleList = new ArrayList<>();
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render(false)").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Declare").withLabelingRuleType(LabelingRuleType.Declare)
				.withComponents(Collections.singletonList(finishProduct2)).withReplacements(null));

		// └──[root - 0.0 (200.0, vol: 49.99999999999999) ]
		// ├──[Pâte french - 100.0 (444.44444444444446, vol: 411.1111111111111)
		// Detail]
		// │ ├──[ing1 french (false) - 66.66666666666667 ( vol :
		// 66.66666666666667) ]
		// │ ├──[ing2 french (false) - 133.33333333333334 ( vol :
		// 133.33333333333334) ]
		// │ ├──[Pâte french - 22.22222222222222 (300.0, vol: 300.0) Detail]
		// │ │ ├──[ing1 french (false) - 33.333333333333336 ( vol :
		// 33.333333333333336) ]
		// │ │ ├──[ing2 french (false) - 66.66666666666667 ( vol :
		// 66.66666666666667) ]
		// │ │ └──[Legal Raw material 12 - 200.0 (200.0, vol: 200.0) Detail]
		// │ │ ├──[ing1 french (false) - 50.0 ( vol : 50.0) ]
		// │ │ └──[ing2 french (false) - 150.0 ( vol : 150.0) ]
		// │ ├──[Garniture french - 22.22222222222222 (600.0, vol: 600.0)
		// Detail]
		// │ │ ├──[ing3 french (false) - 500.0 ( vol : 500.0) ]
		// │ │ └──[ing4 french (false) - 100.0 ( vol : 100.0) ]
		// │ └──[Legal Raw material 12 - 200.0 (200.0, vol: 200.0) Detail]
		// │ ├──[ing1 french (false) - 50.0 ( vol : 50.0) ]
		// │ └──[ing2 french (false) - 150.0 ( vol : 150.0) ]
		// └──[Garniture french - 100.0 (1200.0, vol: 1200.0) Detail]
		// ├──[ing3 french (false) - 1000.0 ( vol : 1000.0) ]
		// └──[ing4 french (false) - 200.0 ( vol : 200.0) ]

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"garniture french 50% (ing3 french 41,7%, ing4 french 8,3%), pâte french 50% (legal Raw material 12 22,5% (ing2 french 16,9%, ing1 french 5,6%), ing2 french 15%, ing1 french 7,5%, garniture french 2,5% (ing3 french 2,1%, ing4 french 0,4%), pâte french 2,5% (legal Raw material 12 1,7% (ing2 french 1,2%, ing1 french 0,4%), ing2 french 0,6%, ing1 french 0,3%))",
				Locale.FRENCH);

	}

	private void checkError(final NodeRef productNodeRef, final List<LabelingRuleListDataItem> labelingRuleList, final String errorMessage) {
		inWriteTx(() -> {

			ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(productNodeRef);

			formulatedProduct.getLabelingListView().setLabelingRuleList(labelingRuleList);

			productService.formulate(formulatedProduct);

			assertFalse(formulatedProduct.getReqCtrlList().isEmpty());

			boolean found = false;
			for (RequirementListDataItem reqCtrlListDataItem : formulatedProduct.getReqCtrlList()) {
				if (RequirementType.Forbidden.equals(reqCtrlListDataItem.getReqType())) {

					String error = reqCtrlListDataItem.getReqMessage();
					if (error.equals(errorMessage)) {
						found = true;

					}
				}
			}

			Assert.assertTrue("Error not found " + errorMessage, found);
			return null;

		});

	}

	@Test
	public void testMultiLingualLabelingFormulation() {

		logger.info("testLabelingFormulation");

		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		// Test locale + format %
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Langue").withFormula("fr,en").withLabelingRuleType(LabelingRuleType.Locale));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
				.withLabelingRuleType(LabelingRuleType.Format).withComponents(Arrays.asList(ing2, ing3, ing4)));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		final NodeRef finishedProductNodeRef1 = createTestProduct(labelingRuleList);

		inWriteTx(() -> {

			/*-- Formulate product --*/
			logger.debug("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef1);

			/*-- Verify formulation --*/
			logger.debug("/*-- Verify formulation --*/");
			ProductData formulatedProduct1 = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef1);

			assertTrue(formulatedProduct1.getLabelingListView().getLabelingRuleList().size() > 0);

			// verify IngLabelingList
			assertNotNull("IngLabelingList is null", formulatedProduct1.getLabelingListView().getIngLabelingList());
			assertTrue(formulatedProduct1.getLabelingListView().getIngLabelingList().size() > 0);

			for (IngLabelingListDataItem illDataItem : formulatedProduct1.getLabelingListView().getIngLabelingList()) {

				checkILL("Garniture french 50% (ing3 french 41,7%, ing4 french 8,3%)",
						"Pâte french 50% (Legal Raw material 12 33,3% (ing2 french 25%, ing1 french), ing2 french 11,1%, ing1 french)",
						illDataItem.getValue().getValue(Locale.FRENCH));

				checkILL("Garniture english 50% (ing3 english 41.7%, ing4 english 8.3%)",
						"Pâte english 50% (Legal Raw material 12 33.3% (ing2 english 25%, ing1 english), ing2 english 11.1%, ing1 english)",
						illDataItem.getValue().getValue(Locale.ENGLISH));

			}

			return null;

		});
	}

	@Test
	public void testIncTypeThreshold() {

		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();
		labelingRuleList
				.add(LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Aggregate 1").withLabelingRuleType(LabelingRuleType.DoNotDetails)
				.withComponents(Arrays.asList(ing1, ing2)).withReplacements(Collections.singletonList(ing3)));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Aggregate 2").withLabelingRuleType(LabelingRuleType.DoNotDetails)
				.withComponents(Collections.singletonList(ing4)).withReplacements(Collections.singletonList(ing6)));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1").withFormula("detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1bis").withFormula("ingDefaultFormat = \"{0} {1,number,0.#%}\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param2").withFormula("ingTypeDefaultFormat = \"{0} {1,number,0.#%}: ({2})\"")
				.withLabelingRuleType(LabelingRuleType.Prefs));
		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param3")
				.withFormula("ingTypeDecThresholdFormat = \"{0} {1,number,0.#%} [{2}] \"").withLabelingRuleType(LabelingRuleType.Prefs));

		final NodeRef finishedProductNodeRef1 = createTestProduct(labelingRuleList);

		// └──[root - 0.0 (2.0)]
		// ├──[pâte french - 1.0 (3.0)]
		// │ ├──[ing3 french - 1.0]
		// │ └──[legal Raw material 12 - 2.0 (2.0)]
		// │ └──[ing3 french - 2.0]
		// └──[garniture french - 1.0 (6.0)]
		// ├──[ing3 french - 5.0]
		// └──[ing5 french - 1.0]

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"garniture french 50% (ing3 french 41,7%, epices french 8,3% [ing6 french 8,3%]), pâte french 50% (legal Raw material 12 33,3% (ing3 french 33,3%), ing3 french 16,7%)",
				Locale.FRENCH);

		labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param4").withFormula("showIngCEECode = true")
				.withLabelingRuleType(LabelingRuleType.Prefs));

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"garniture french 50% (ing3 french 41,7%, epices french 8,3% [CEE6 8,3%]), pâte french 50% (legal Raw material 12 33,3% (ing3 french 33,3%), ing3 french 16,7%)",
				Locale.FRENCH);

	}

	@Test
	public void testAllergenIdentification() {
		if (!LabelingFormulaContext.ALLERGEN_DETECTION_PATTERN.matcher("SOIJAlesitiin").find()) {
			fail();
		}
		if (!LabelingFormulaContext.ALLERGEN_DETECTION_PATTERN.matcher("λεκιθίνη ΣΌΓΙΑΣ").find()) {
			fail();
		}
		if (!LabelingFormulaContext.ALLERGEN_DETECTION_PATTERN.matcher("SZÓJAlecitin").find()) {
			fail();
		}
		if (LabelingFormulaContext.ALLERGEN_DETECTION_PATTERN.matcher("SzÓjalecitin").find()) {
			fail();
		}

		if (!LabelingFormulaContext.ALLERGEN_DETECTION_PATTERN.matcher("Lecitine de SOJA").find()) {
			fail();
		}
		if (!LabelingFormulaContext.ALLERGEN_DETECTION_PATTERN.matcher("lecitine de <b>soja</b>").find()) {
			fail();
		}
		if (!LabelingFormulaContext.ALLERGEN_DETECTION_PATTERN.matcher("lecitine de <i>soja</i>").find()) {
			fail();
		}
		if (!LabelingFormulaContext.ALLERGEN_DETECTION_PATTERN.matcher("lecitine de <u>soja</u>").find()) {
			fail();
		}
		if (LabelingFormulaContext.ALLERGEN_DETECTION_PATTERN.matcher("lecitine de soja").find()) {
			fail();
		}

		if (LabelingFormulaContext.ALLERGEN_DETECTION_PATTERN.matcher("大豆卵磷脂").find()) {
			fail();
		}
		assertEquals(replaceAllergen("соевый", "какао-бобы, сахар, какао-масло, эмульгатор: соевый лецитин , натуральный ванильный экстракт"),
				"какао-бобы, сахар, какао-масло, эмульгатор: <b>соевый</b> лецитин , натуральный ванильный экстракт");

	}

	String replaceAllergen(String allergenName, String ingLegalName) {
		Matcher ma = Pattern.compile("\\b(" + Pattern.quote(allergenName) + "(s?))\\b", Pattern.CASE_INSENSITIVE).matcher(ingLegalName);
		if (ma.find() && (ma.group(1) != null)) {
			return ma.replaceAll("<b>$1</b>");
		}
		return ingLegalName + " " + allergenName.replaceFirst("(.*)", "<b>$1</b>");
	}

	@Deprecated
	// @Test Ne fonctionne pas qd lancé dans docker TODO
	public void testMultiThreadFormulation() throws InterruptedException {

		try {
			BeCPGTestHelper.createUser("labellingUser1");
			permissionService.setPermission(getTestFolderNodeRef(), "labellingUser1", PermissionService.ALL_PERMISSIONS, true);

			authenticationComponent.setCurrentUser("labellingUser1");

			final NodeRef finishProduct1 = createTestProduct(null);
			final NodeRef finishProduct2 = createTestProduct(null);

			NodeRef finishedProductNodeRef1 = inWriteTx(() -> {

				MLText legalName = (MLText) mlNodeService.getProperty(finishProduct1, BeCPGModel.PROP_LEGAL_NAME);
				legalName.addValue(Locale.FRENCH, "legal Finished product 1");
				legalName.addValue(Locale.FRANCE, "legal Finished product 1");
				legalName.addValue(Locale.CANADA_FRENCH, "legal Finished product 1 fr_CA");

				legalName = (MLText) mlNodeService.getProperty(finishProduct2, BeCPGModel.PROP_LEGAL_NAME);

				legalName.addValue(Locale.FRENCH, "legal Finished product 1");
				legalName.addValue(Locale.FRANCE, "legal Finished product 1");
				legalName.addValue(Locale.CANADA_FRENCH, "legal Finished product 1 fr_CA");

				mlNodeService.setProperty(finishProduct1, BeCPGModel.PROP_LEGAL_NAME, legalName);

				FinishedProductData finishedProduct1 = new FinishedProductData();
				finishedProduct1.setName("Finished product " + Calendar.getInstance().getTimeInMillis());
				finishedProduct1.setLegalName("legal Finished product 1");
				finishedProduct1.setQty(2d);
				finishedProduct1.setUnit(ProductUnit.kg);
				finishedProduct1.setDensity(1d);
				List<CompoListDataItem> compoList1 = new ArrayList<>();
				compoList1.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
						.withDeclarationType(DeclarationType.Detail).withProduct(localSF11NodeRef));
				compoList1.add(CompoListDataItem.build().withParent(compoList1.get(0)).withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
						.withDeclarationType(DeclarationType.DoNotDetails).withProduct(finishProduct1));
				compoList1.add(CompoListDataItem.build().withParent(compoList1.get(0)).withQtyUsed(2d).withUnit(ProductUnit.kg).withLossPerc(0d)
						.withDeclarationType(DeclarationType.DoNotDetails).withProduct(finishProduct2));
				compoList1.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
						.withDeclarationType(DeclarationType.Detail).withProduct(localSF12NodeRef));
				compoList1.add(CompoListDataItem.build().withParent(compoList1.get(3)).withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d)
						.withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial13NodeRef));
				compoList1.add(CompoListDataItem.build().withParent(compoList1.get(3)).withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d)
						.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial14NodeRef));

				finishedProduct1.getCompoListView().setCompoList(compoList1);

				// Declare
				List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

				labelingRuleList.add(
						LabelingRuleListDataItem.build().withName("Rendu").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));
				labelingRuleList.add(LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%} ({2})")
						.withLabelingRuleType(LabelingRuleType.Format));
				labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1")
						.withFormula("detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"").withLabelingRuleType(LabelingRuleType.Prefs));

				finishedProduct1.getLabelingListView().setLabelingRuleList(labelingRuleList);

				return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();
			});

			final LongAdder adder = new LongAdder();

			Callable<Void> callable = () -> {
				String threadName = Thread.currentThread().getName();

				adder.increment();

				String currentUser = "admin";
				I18NUtil.setLocale(Locale.FRENCH);

				if ((adder.intValue() % 2) == 0) {
					I18NUtil.setLocale(Locale.CANADA_FRENCH);
					currentUser = "labellingUser1";
				}
				authenticationComponent.setCurrentUser(currentUser);

				logger.info("running labeling in thread " + threadName + "for user" + currentUser);

				String ill = "Pâte french 50% (legal Finished product 1 50%), Garniture french 50% (Legal Raw material 13 25% (ing3 french 25%), ing3 french 16,7%, ing4 french 8,3%)";

				inWriteTx(() -> {

					productService.formulate(finishedProductNodeRef1);

					ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef1);

					Assert.assertTrue(formulatedProduct.getLabelingListView().getLabelingRuleList().size() > 0);

					// verify IngLabelingList

					Assert.assertNotNull("IngLabelingList is null", formulatedProduct.getLabelingListView().getIngLabelingList());
					Assert.assertTrue(formulatedProduct.getLabelingListView().getIngLabelingList().size() > 0);

					for (IngLabelingListDataItem illDataItem : formulatedProduct.getLabelingListView().getIngLabelingList()) {
						String formulatedIll = illDataItem.getValue().getValue(Locale.FRENCH);
						Assert.assertEquals("Incorrect label :" + formulatedIll + "\n   - compare to " + ill, ill, formulatedIll);
						Assert.assertNotNull(illDataItem.getLogValue());
						logger.info("Finished labeling in thread " + threadName);
					}

					return null;

				});

				return null;
			};

			ExecutorService executor = Executors.newFixedThreadPool(20);
			List<Future<Void>> results = new ArrayList<>();

			for (int i = 0; i < 20; i++) {
				results.add(executor.submit(callable));

			}

			executor.shutdown();
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

			for (Future<Void> future : results) {
				try {

					future.get();
				} catch (ExecutionException ex) {
					logger.error(ex, ex);
					Assert.fail(ex.getMessage());
				}
			}

		} finally {
			authenticationComponent.setCurrentUser("admin");
			I18NUtil.setLocale(Locale.getDefault());
		}

	}

}
