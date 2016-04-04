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
package fr.becpg.test.repo.product.formulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import javax.annotation.Resource;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;


import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.LabelingRuleType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.test.BeCPGTestHelper;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

/**
 *
 * @author matthieu
 *
 */
public class LabelingFormulationTest extends AbstractFinishedProductTest {

	protected static final Log logger = LogFactory.getLog(LabelingFormulationTest.class);

	@Resource
	private AssociationService associationService;

	@Resource
	@Qualifier("mlAwareNodeService")
	private NodeService mlNodeService;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
	}

	private NodeRef createTestProduct(final List<LabelingRuleListDataItem> labelingRuleList) {
		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

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
			compoList1.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF11NodeRef));
			compoList1.add(
					new CompoListDataItem(null, compoList1.get(0), null, 1d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial11NodeRef));
			compoList1.add(
					new CompoListDataItem(null, compoList1.get(0), null, 2d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial12NodeRef));
			compoList1.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF12NodeRef));
			compoList1.add(
					new CompoListDataItem(null, compoList1.get(3), null, 3d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial13NodeRef));
			compoList1.add(
					new CompoListDataItem(null, compoList1.get(3), null, 3d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial14NodeRef));

			finishedProduct1.getCompoListView().setCompoList(compoList1);

			finishedProduct1.getLabelingListView().setLabelingRuleList(labelingRuleList);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();
		}, false, true);
	}

	// private String detailsDefaultFormat = "{0} {1,number,0.#%} ({2})";

	@Test
	public void testNullIng() throws Exception {

		NodeRef finishedProductNodeRef1 = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			FinishedProductData finishedProduct1 = new FinishedProductData();
			finishedProduct1.setName("Finished product " + Calendar.getInstance().getTimeInMillis());
			finishedProduct1.setLegalName("legal Finished product 1");
			finishedProduct1.setQty(2d);
			finishedProduct1.setUnit(ProductUnit.kg);
			finishedProduct1.setDensity(1d);
			List<CompoListDataItem> compoList1 = new ArrayList<>();
			compoList1.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial16NodeRef));

			finishedProduct1.getCompoListView().setCompoList(compoList1);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();
		}, false, true);

		// └──[root - 0.0 (1.0)]
		// ├──[ing1 french - null]
		// ├──[ing3 french - 0.55]
		// └──[ing2 french - null]

		// Declare
		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList
				.add(new LabelingRuleListDataItem("Declare", null, LabelingRuleType.Declare, Collections.singletonList(rawMaterial16NodeRef), null));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, null, null));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));

		checkILL(finishedProductNodeRef1, labelingRuleList, "ing3 french 55%, ing1 french, ing2 french", Locale.FRENCH);

		labelingRuleList = new ArrayList<>();

		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList
				.add(new LabelingRuleListDataItem("Detail", null, LabelingRuleType.Detail, Collections.singletonList(rawMaterial16NodeRef), null));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, null, null));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));

		checkILL(finishedProductNodeRef1, labelingRuleList, "legal Raw material 16 100% (ing1 french, ing3 french 55%, ing2 french)", Locale.FRENCH);

		final NodeRef finishProduct1 = createTestProduct(null);

		final NodeRef finishProduct2 = createTestProduct(null);

		finishedProductNodeRef1 = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			FinishedProductData finishedProduct1 = new FinishedProductData();
			finishedProduct1.setName("Finished product " + Calendar.getInstance().getTimeInMillis());
			finishedProduct1.setLegalName("legal Finished product 1");
			finishedProduct1.setQty(2d);
			finishedProduct1.setUnit(ProductUnit.kg);
			finishedProduct1.setDensity(1d);
			List<CompoListDataItem> compoList1 = new ArrayList<>();
			compoList1.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF11NodeRef));
			compoList1.add(new CompoListDataItem(null, compoList1.get(0), null, 1d, CompoListUnit.kg, 0d, DeclarationType.Kit, finishProduct1));
			compoList1.add(new CompoListDataItem(null, compoList1.get(0), null, 2d, CompoListUnit.kg, 0d, DeclarationType.Kit, finishProduct2));
			compoList1.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF12NodeRef));
			compoList1.add(
					new CompoListDataItem(null, compoList1.get(3), null, 3d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial13NodeRef));
			compoList1.add(
					new CompoListDataItem(null, compoList1.get(3), null, 3d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial16NodeRef));

			finishedProduct1.getCompoListView().setCompoList(compoList1);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();
		}, false, true);

		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, null, null));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Aggr", null, LabelingRuleType.Group, Arrays.asList(rawMaterial13NodeRef, rawMaterial16NodeRef), null));

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"<b>aggr (38,7%):</b> ing3 french 38,7%, ing1 french, ing2 french<br/><b>legal Finished product 1 (33,3%):</b> pâte french 50% (legal Raw material 12 33,3% (ing2 french 25%, ing1 french 8,3%), ing2 french 11,1%, ing1 french 5,6%), garniture french 50% (ing3 french 41,7%, ing4 french 8,3%)<br/><b>legal Finished product 1 (16,7%):</b> pâte french 50% (legal Raw material 12 33,3% (ing2 french 25%, ing1 french 8,3%), ing2 french 11,1%, ing1 french 5,6%), garniture french 50% (ing3 french 41,7%, ing4 french 8,3%)<br/>garniture french 11,3%",
				Locale.FRENCH);

		checkError(finishedProductNodeRef1, labelingRuleList,
				"Impossible de déclarer ou d'aggreger l'ingrédient ing2 sans quantité, changer le type de déclaration du composant");

		// TODO Auxiliaires technologiques
		//
		// J'ai un aux tech dans ma MP sel. J'utilise la MP sel dans le SF pâte.
		// Si je dis de masquer lex aux tech, je vois l'aux tech du sel car
		// l'info est perdu au niveau du SF
		// J'ai un aux tech dans ma MP sel. J'utilise ce même aux tech dans le
		// SF pâte qui utilise aussi le sel. Si je dis de masquer lex aux tech,
		// il faut prendre dans le calcul slmt la qté MeO dans le SF.

	}
	
	
//	@Test
//	public void testIngsLabelingWithYield() throws Exception {
//		final NodeRef finishedProductNodeRef1 = createTestProduct(null);
//		
//		finishedProductNodeRef1
//
//		// Declare
//		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();
//
//		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
//		labelingRuleList.add(new LabelingRuleListDataItem("Declare", null, LabelingRuleType.Declare,
//				Arrays.asList(localSF11NodeRef, rawMaterial12NodeRef, localSF12NodeRef), null));
//		labelingRuleList
//				.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, Arrays.asList(ing1, ing2, ing3, ing4), null));
//		labelingRuleList.add(
//				new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));
//
//		// └──[root - 0.0 (9.0)]
//		// ├──[ing1 french - 0.8333333333333334]
//		// ├──[ing2 french - 2.166666666666667]
//		// ├──[ing3 french - 5.0]
//		// └──[ing4 french - 1.0]
//
//		checkILL(finishedProductNodeRef1, labelingRuleList, "ing3 french 55,6%, ing2 french 24,1%, ing4 french 11,1%, ing1 french 9,3%",
//				Locale.FRENCH);
//
//		
//		
//	}

	@Test
	public void testMultiLevelSFGroup() throws Exception {

		final NodeRef finishProduct1 = createTestProduct(null);

		final NodeRef finishProduct2 = createTestProduct(null);

		NodeRef finishedProductNodeRef1 = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			FinishedProductData finishedProduct1 = new FinishedProductData();
			finishedProduct1.setName("Finished product " + Calendar.getInstance().getTimeInMillis());
			finishedProduct1.setLegalName("legal Finished product 1");
			finishedProduct1.setQty(2d);
			finishedProduct1.setUnit(ProductUnit.kg);
			finishedProduct1.setDensity(1d);
			List<CompoListDataItem> compoList1 = new ArrayList<>();
			compoList1.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF11NodeRef));
			compoList1.add(new CompoListDataItem(null, compoList1.get(0), null, 1d, CompoListUnit.kg, 0d, DeclarationType.Group, finishProduct1));
			compoList1.add(new CompoListDataItem(null, compoList1.get(0), null, 2d, CompoListUnit.kg, 0d, DeclarationType.Group, finishProduct2));
			compoList1.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF12NodeRef));
			compoList1.add(
					new CompoListDataItem(null, compoList1.get(3), null, 3d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial13NodeRef));
			compoList1.add(
					new CompoListDataItem(null, compoList1.get(3), null, 3d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial14NodeRef));

			finishedProduct1.getCompoListView().setCompoList(compoList1);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();
		}, false, true);

		// Declare
		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, null, null));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));

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
				"<b>legal Finished product 1 (33,3%):</b> pâte french 16,7% (legal Raw material 12 11,1% (ing2 french 8,3%, ing1 french 2,8%), ing2 french 3,7%, ing1 french 1,9%), garniture french 16,7% (ing3 french 13,9%, ing4 french 2,8%)<br/><b>legal Finished product 1 (16,7%):</b> pâte french 8,3% (legal Raw material 12 5,6% (ing2 french 4,2%, ing1 french 1,4%), ing2 french 1,9%, ing1 french 0,9%), garniture french 8,3% (ing3 french 6,9%, ing4 french 1,4%)<br/>garniture french 50% (legal Raw material 13 25% (ing3 french 25%), ing3 french 16,7%, ing4 french 8,3%)",
				Locale.FRENCH);

		labelingRuleList = new ArrayList<>();

		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, null, null));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Aggr", null, LabelingRuleType.Group, Arrays.asList(rawMaterial13NodeRef, rawMaterial14NodeRef), null));

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
				"<b>aggr (50%):</b> legal Raw material 13 25% (ing3 french 25%), ing3 french 16,7%, ing4 french 8,3%<br/><b>legal Finished product 1 (16,7%):</b> pâte french 16,7% (legal Raw material 12 11,1% (ing2 french 8,3%, ing1 french 2,8%), ing2 french 3,7%, ing1 french 1,9%)<br/><b>legal Finished product 1 (8,3%):</b> pâte french 8,3% (legal Raw material 12 5,6% (ing2 french 4,2%, ing1 french 1,4%), ing2 french 1,9%, ing1 french 0,9%)",
				Locale.FRENCH);

	}

	@Test
	public void testReconstitutionLabeling() throws Exception {
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

		final NodeRef finishedProductNodeRef1 = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setLegalName("Legal Produit fini 1");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(4d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();

			compoList.add(new CompoListDataItem(null, null, null, 10d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial7NodeRef));
			compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));

			Map<QName, Serializable> props = new HashMap<>();
			props.put(PLMModel.PROP_RECONSTITUTION_RATE, 5d);
			nodeService.addAspect(rawMaterial1NodeRef, PLMModel.ASPECT_RECONSTITUTABLE, props);
			associationService.update(rawMaterial1NodeRef, PLMModel.ASSOC_DILUENT_REF, ing5);
			associationService.update(rawMaterial1NodeRef, PLMModel.ASSOC_TARGET_RECONSTITUTION_REF, ing6);

			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		}, false, true);

		// Declare
		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		// labelingRuleList.add(new LabelingRuleListDataItem("Pref1",
		// "useVolume = true", LabelingRuleType.Prefs));
		labelingRuleList.add(new LabelingRuleListDataItem("Pref2", "ingDefaultFormat = \"{0} {1,number,0.#%}\"", LabelingRuleType.Prefs));
		labelingRuleList
				.add(new LabelingRuleListDataItem("Pref3", "groupDefaultFormat = \"<b>{0} ({1,number,0.#%}):</b> {2}\"", LabelingRuleType.Prefs));
		labelingRuleList.add(new LabelingRuleListDataItem("Pref4", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs));
		labelingRuleList.add(new LabelingRuleListDataItem("Pref5", "ingTypeDefaultFormat = \"{0}: {2}\"", LabelingRuleType.Prefs));
		labelingRuleList.add(new LabelingRuleListDataItem("Pref6", "subIngsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs));

		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, null, null));

		// └──[root - 0.0 (11.0, vol: 11.0) ]
		// ├──[ing5 french - 5.0 (10.0, vol: 10.0) Detail]
		// │ ├──[ing1 french - 7.0 ( vol : 7.0) ]
		// │ └──[ing4 french - 3.0 ( vol : 3.0) ]
		// └──[Juice - 6.0 (6.0, vol: 6.0) Detail]
		// ├──[ing1 french - 2.0 ( vol : 2.0) ]
		// └──[ing2 french - 4.0 ( vol : 4.0) ]

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"epaississant french: ing5 french 54,5% (ing1 french 70%, ing4 french 30%), epices french: ing6 french 45,5%", Locale.FRENCH);

		/** Partial Reconstitution **/

		final NodeRef finishedProductNodeRef2 = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 2");
			finishedProduct.setLegalName("Legal Produit fini 2");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(4d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();

			compoList.add(new CompoListDataItem(null, null, null, 3d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial7NodeRef));
			compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));

			Map<QName, Serializable> props = new HashMap<>();
			props.put(PLMModel.PROP_RECONSTITUTION_RATE, 5d);
			nodeService.addAspect(rawMaterial1NodeRef, PLMModel.ASPECT_RECONSTITUTABLE, props);
			associationService.update(rawMaterial1NodeRef, PLMModel.ASSOC_DILUENT_REF, ing5);
			associationService.update(rawMaterial1NodeRef, PLMModel.ASSOC_TARGET_RECONSTITUTION_REF, ing6);

			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		}, false, true);

		// └──[root - 0.0 (11.0, vol: 11.0) ]
		// ├──[ing5 french - 5.0 (10.0, vol: 10.0) Detail]
		// │ ├──[ing1 french - 7.0 ( vol : 7.0) ]
		// │ └──[ing4 french - 3.0 ( vol : 3.0) ]
		// └──[Juice - 6.0 (6.0, vol: 6.0) Detail]
		// ├──[ing1 french - 2.0 ( vol : 2.0) ]
		// └──[ing2 french - 4.0 ( vol : 4.0) ]

		checkILL(finishedProductNodeRef2, labelingRuleList, "epices french: ing6 french 95%, legal Raw material 1 (<b>allergen1</b>) 5%",
				Locale.FRENCH);

		/** Test with priority **/

		final NodeRef finishedProductNodeRef3 = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 3");
			finishedProduct.setLegalName("Legal Produit fini 3");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(4d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();

			compoList.add(new CompoListDataItem(null, null, null, 5d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial7NodeRef));
			compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
			compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial2NodeRef));

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
		}, false, true);

		checkILL(finishedProductNodeRef3, labelingRuleList, "ing4 french 71,4%, epices french, legal Raw material 1 (<b>allergen1</b>) 8,6%",
				Locale.FRENCH);
	}

	@Test
	public void testRenderAllergens() throws Exception {

		final NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 2");
			finishedProduct.setLegalName("Legal Produit fini 2");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(4d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();

			compoList.add(new CompoListDataItem(null, null, null, 3d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial7NodeRef));
			compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));

			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		}, false, true);

		// Declare
		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "renderAllergens()", LabelingRuleType.Render));

		checkILL(finishedProductNodeRef, labelingRuleList, "allergen1", Locale.FRENCH);

	}

	@Test
	public void testRawMaterialIngType() throws Exception {

		final NodeRef finishedProductNodeRef1 = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setLegalName("Legal Produit fini 1");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(4d);
			finishedProduct.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();

			compoList.add(new CompoListDataItem(null, null, null, 10d, CompoListUnit.kg, 0d, DeclarationType.DoNotDetails, rawMaterial7NodeRef));
			compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));

			Map<QName, Serializable> props = new HashMap<>();
			props.put(PLMModel.PROP_ING_TYPE_V2, ingType2);
			nodeService.addAspect(rawMaterial7NodeRef, PLMModel.ASPECT_ING_TYPE, props);

			finishedProduct.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		}, false, true);

		// Declare
		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		labelingRuleList.add(new LabelingRuleListDataItem("Pref2", "ingDefaultFormat = \"{0} {1,number,0.#%}\"", LabelingRuleType.Prefs));
		labelingRuleList
				.add(new LabelingRuleListDataItem("Pref3", "groupDefaultFormat = \"<b>{0} ({1,number,0.#%}):</b> {2}\"", LabelingRuleType.Prefs));
		labelingRuleList.add(new LabelingRuleListDataItem("Pref4", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs));
		labelingRuleList.add(new LabelingRuleListDataItem("Pref5", "ingTypeDefaultFormat = \"{0}: {2};\"", LabelingRuleType.Prefs));
		labelingRuleList.add(new LabelingRuleListDataItem("Pref6", "subIngsDefaultFormat = \"{0} ({2})\"", LabelingRuleType.Prefs));

		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, null, null));

		checkILL(finishedProductNodeRef1, labelingRuleList, "epices french: legal Raw material 7 90,9%;, ing2 french 6,1%, ing1 french 3%",
				Locale.FRENCH);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			nodeService.removeAspect(rawMaterial7NodeRef, PLMModel.ASPECT_ING_TYPE);
			return null;
		}, false, true);
	}

	/**
	 * Test ingredients calculating.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testCalculateILL() throws Exception {

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

		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Declare", null, LabelingRuleType.Declare,
				Arrays.asList(localSF11NodeRef, rawMaterial12NodeRef, localSF12NodeRef), null));
		labelingRuleList
				.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, Arrays.asList(ing1, ing2, ing3, ing4), null));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));

		// └──[root - 0.0 (9.0)]
		// ├──[ing1 french - 0.8333333333333334]
		// ├──[ing2 french - 2.166666666666667]
		// ├──[ing3 french - 5.0]
		// └──[ing4 french - 1.0]

		checkILL(finishedProductNodeRef1, labelingRuleList, "ing3 french 55,6%, ing2 french 24,1%, ing4 french 11,1%, ing1 french 9,3%",
				Locale.FRENCH);

		// Omit
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Omit", null, LabelingRuleType.Omit, Collections.singletonList(localSF12NodeRef), null));
		// Ing2 dans rawMaterial 12 est un auxiliare
		labelingRuleList.add(new LabelingRuleListDataItem("Auxiliare", "ingListDataItem.isProcessingAid == true", LabelingRuleType.Omit, null, null));
		labelingRuleList
				.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, Arrays.asList(ing1, ing2, ing3, ing4), null));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));

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
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Aggregate 2", null, LabelingRuleType.DoNotDetails, Collections.singletonList(ing4),
				Collections.singletonList(ing5)));
		labelingRuleList
				.add(new LabelingRuleListDataItem("Do not declare", null, LabelingRuleType.DoNotDeclare, Collections.singletonList(ingType1), null));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));

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
				"pâte french 50% (legal Raw material 12 33,3% (ing2 french, ing1 french), ing2 french, ing1 french), garniture french 50% (ing3 french, ing5 french)",
				Locale.FRENCH);

		// Test Omit IngType
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Aggregate 2", null, LabelingRuleType.DoNotDetails, Collections.singletonList(ing4),
				Collections.singletonList(ing5)));
		labelingRuleList.add(new LabelingRuleListDataItem("Omit", null, LabelingRuleType.Omit, Collections.singletonList(ingType1), null));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));
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
				"pâte french 50% (legal Raw material 12 33,3% (ing2 french, ing1 french), ing2 french, ing1 french), garniture french 50% (ing3 french)",
				Locale.FRENCH);

		// Details
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList
				.add(new LabelingRuleListDataItem("Details", null, LabelingRuleType.Detail, Collections.singletonList(rawMaterial11NodeRef), null));
		labelingRuleList
				.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, Arrays.asList(ing1, ing2, ing3, ing4), null));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));

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
				"pâte french 50% (legal Raw material 12 33,3% (ing2 french 25%, ing1 french 8,3%), legal Raw material 11 16,7% (ing2 french 11,1%, ing1 french 5,6%)), garniture french 50% (ing3 french 41,7%, ing4 french 8,3%)",
				Locale.FRENCH);

		// Do not details
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("DoNotDetails", null, LabelingRuleType.DoNotDetails,
				Collections.singletonList(rawMaterial11NodeRef), null));
		labelingRuleList
				.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, Arrays.asList(ing1, ing2, ing3, ing4), null));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));

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
				"pâte french 50% (legal Raw material 12 33,3% (ing2 french 25%, ing1 french 8,3%), legal Raw material 11), garniture french 50% (ing3 french 41,7%, ing4 french 8,3%)",

				Locale.FRENCH);

		// Rename
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Rename 1", null, LabelingRuleType.Rename, Collections.singletonList(ing1),
				Collections.singletonList(ing5)));
		labelingRuleList.add(new LabelingRuleListDataItem("Aggregate 2", null, LabelingRuleType.DoNotDetails, Collections.singletonList(ing4),
				Collections.singletonList(ing5)));
		labelingRuleList.add(new LabelingRuleListDataItem("Rename 2", "Test rename2", LabelingRuleType.Rename,
				Collections.singletonList(rawMaterial12NodeRef), null));
		labelingRuleList
				.add(new LabelingRuleListDataItem("Rename 3", "path.allergens", LabelingRuleType.Rename, Collections.singletonList(ingType1), null));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));

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
				"pâte french 50% (Test rename2 33,3% (ing2 french, ing5 french), ing2 french, ing5 french), garniture french 50% (ing3 french, Allergènes: ing5 french)",
				Locale.FRENCH);

		// Aggregate
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Aggregate 1", null, LabelingRuleType.DoNotDetails, Arrays.asList(ing1, ing2),
				Collections.singletonList(ing3)));
		labelingRuleList.add(new LabelingRuleListDataItem("Aggregate 2", null, LabelingRuleType.DoNotDetails, Collections.singletonList(ing4),
				Collections.singletonList(ing5)));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));

		// Todo test aggregate type or MP

		// └──[root - 0.0 (2.0)]
		// ├──[pâte french - 1.0 (3.0)]
		// │ ├──[ing3 french - 1.0]
		// │ └──[legal Raw material 12 - 2.0 (2.0)]
		// │ └──[ing3 french - 2.0]
		// └──[garniture french - 1.0 (6.0)]
		// ├──[ing3 french - 5.0]
		// └──[ing5 french - 1.0]

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"pâte french 50% (legal Raw material 12 33,3% (ing3 french), ing3 french), garniture french 50% (ing3 french, epaississant french: ing5 french)",
				Locale.FRENCH);

		// Separator tests and plural

		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Aggregate 1", null, LabelingRuleType.DoNotDetails, Arrays.asList(ing1, ing2),
				Collections.singletonList(ing3)));
		labelingRuleList.add(new LabelingRuleListDataItem("Aggregate 2", null, LabelingRuleType.DoNotDetails, Collections.singletonList(ing4),
				Collections.singletonList(ing5)));
		labelingRuleList.add(new LabelingRuleListDataItem("Change type", null, LabelingRuleType.Type, Collections.singletonList(ing3),
				Collections.singletonList(ingType1)));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param2", "defaultSeparator = \"; \"", LabelingRuleType.Prefs, null, null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param3", "ingTypeDefaultSeparator = \"# \"", LabelingRuleType.Prefs, null, null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param4", "groupDefaultSeparator = \"! \"", LabelingRuleType.Prefs, null, null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param5", "subIngsSeparator = \"@ \"", LabelingRuleType.Prefs, null, null));

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"pâte french 50% (legal Raw material 12 33,3% (epaississant french: ing3 french); epaississant french: ing3 french); garniture french 50% (epaississants french: ing3 french# ing5 french)",
				Locale.FRENCH);

		// Combine

		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Combine 1", new MLText("Comb 1"), "20,30", LabelingRuleType.Detail, Arrays.asList(ing1, ing2), null));
		labelingRuleList
				.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, Arrays.asList(ing1, ing2, ing3, ing4), null));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));

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
				"pâte french 50% (legal Raw material 12 33,3% (ing2 french 17,5%, comb 1 9,2% (ing2 french 7,5%, ing1 french 1,7%), ing1 french 6,7%), ing2 french 7,8%, ing1 french 4,4%, comb 1 4,4% (ing2 french 3,3%, ing1 french 1,1%)), garniture french 50% (ing3 french 41,7%, ing4 french 8,3%)",
				Locale.FRENCH);

		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Combine 1", new MLText("Comb 1"), "100,30", LabelingRuleType.Detail, Arrays.asList(ing1, ing2), null));
		labelingRuleList
				.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, Arrays.asList(ing1, ing2, ing3, ing4), null));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));

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
				"pâte french 50% (legal Raw material 12 33,3% (ing2 french 17,5%, comb 1 15,8% (ing1 french 8,3%, ing2 french 7,5%)), comb 1 8,9% (ing1 french 5,6%, ing2 french 3,3%), ing2 french 7,8%), garniture french 50% (ing3 french 41,7%, ing4 french 8,3%)",
				Locale.FRENCH);

		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Combine 2", new MLText("Decors 1"), null, LabelingRuleType.Group,
				Arrays.asList(localSF11NodeRef, localSF12NodeRef), null));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));

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
				"<b>decors 1 (100%):</b> pâte french 50% (legal Raw material 12 33,3% (ing2 french, ing1 french), ing2 french, ing1 french), garniture french 50% (ing3 french, ing4 french)",
				Locale.FRENCH);

		// Group
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "renderGroupList()", LabelingRuleType.Render));
		labelingRuleList
				.add(new LabelingRuleListDataItem("Group 1", null, LabelingRuleType.Group, Collections.singletonList(localSF11NodeRef), null));
		labelingRuleList
				.add(new LabelingRuleListDataItem("Group 2", null, LabelingRuleType.Group, Collections.singletonList(localSF12NodeRef), null));
		labelingRuleList
				.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, Arrays.asList(ing1, ing2, ing3, ing4), null));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));

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

		checkILL(finishedProductNodeRef1, labelingRuleList, "<b>pâte french 50%</b>, <b>garniture french 50%</b>", Locale.FRENCH);

		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu 1", "render()", LabelingRuleType.Render));
		labelingRuleList
				.add(new LabelingRuleListDataItem("Group 1", null, LabelingRuleType.Group, Collections.singletonList(localSF11NodeRef), null));
		labelingRuleList
				.add(new LabelingRuleListDataItem("Group 2", null, LabelingRuleType.Group, Collections.singletonList(localSF12NodeRef), null));
		labelingRuleList
				.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, Arrays.asList(ing1, ing2, ing3, ing4), null));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));

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
				"<b>pâte french (50%):</b> legal Raw material 12 33,3% (ing2 french 25%, ing1 french 8,3%), ing2 french 11,1%, ing1 french 5,6%<br/><b>garniture french (50%):</b> ing3 french 41,7%, ing4 french 8,3%",
				Locale.FRENCH);

		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu 2", "render(false)", LabelingRuleType.Render));
		labelingRuleList
				.add(new LabelingRuleListDataItem("Group 1", null, LabelingRuleType.Group, Collections.singletonList(localSF11NodeRef), null));
		labelingRuleList
				.add(new LabelingRuleListDataItem("Group 2", null, LabelingRuleType.Group, Collections.singletonList(localSF12NodeRef), null));
		labelingRuleList
				.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, Arrays.asList(ing1, ing2, ing3, ing4), null));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));

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
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu 2", "render(false)", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Aggregate 1", null, LabelingRuleType.DoNotDetails, Collections.singletonList(ing1),
				Collections.singletonList(ing3)));
		labelingRuleList
				.add(new LabelingRuleListDataItem("Group 1", null, LabelingRuleType.Group, Collections.singletonList(localSF11NodeRef), null));
		labelingRuleList
				.add(new LabelingRuleListDataItem("Group 2", null, LabelingRuleType.Group, Collections.singletonList(localSF12NodeRef), null));
		labelingRuleList
				.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, Arrays.asList(ing1, ing2, ing3, ing4), null));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));

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
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef1);
			formulatedProduct.getCompoListView().getCompoList().get(2).setProduct(rawMaterial7NodeRef);
			alfrescoRepository.save(formulatedProduct);
			return null;
		}, false, true);

		// └──[root - 0.0 (2.0)]
		// ├──[pâte french - 1.0 (3.0)]
		// │ ├──[ing1 french - 0.33333333333333337]
		// │ ├──[ing2 french - 0.6666666666666667]
		// │ └──[legal Raw material 7 - 2.0 (2.0)]
		// │ └──[ing5 french - 2.0]
		// └──[garniture french - 1.0 (6.0)]
		// ├──[ing3 french - 5.0]
		// └──[ing4 french - 1.0]

		// #814
		// checkILL(finishedProductNodeRef1, labelingRuleList,
		// "pâte french 50% (legal Raw material 7 33,3% (epaississant french:
		// ing5 french (ing1 french, ing4 french)), ing2 french, ing1 french),
		// garniture french 50% (ing3 french, ing4 french)",
		// Locale.FRENCH);
		checkILL(finishedProductNodeRef1, labelingRuleList,
				"pâte french 50% (legal Raw material 7 33,3% (epaississant french: ing5 french (ing1 french, ing4 french)), ing2 french, ing1 french), garniture french 50% (ing3 french, ing4 french)",
				Locale.FRENCH);

		// MultiLevel

		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render(false)", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, null, null));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));
		final NodeRef finishProduct2 = createTestProduct(null);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			productService.formulate(finishProduct2);
			ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef1);
			formulatedProduct.setQty(7d);
			formulatedProduct.getCompoListView().getCompoList().get(2).setProduct(finishProduct2);
			formulatedProduct.getCompoListView().getCompoList()
					.add(new CompoListDataItem(null, null, 5d, null, CompoListUnit.kg, 0d, DeclarationType.Group, finishProduct2));
			alfrescoRepository.save(formulatedProduct);
			return null;
		}, false, true);

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
				"pâte french 50% (legal Finished product 1 33,3% (pâte french 16,7% (legal Raw material 12 11,1% (ing2 french 8,3%, ing1 french 2,8%), ing2 french 3,7%, ing1 french 1,9%), garniture french 16,7% (ing3 french 13,9%, ing4 french 2,8%)), ing2 french 11,1%, ing1 french 5,6%), garniture french 50% (ing3 french 41,7%, ing4 french 8,3%)",
				Locale.FRENCH);

		// Declare multilevel
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render(false)", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, null, null));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));
		labelingRuleList
				.add(new LabelingRuleListDataItem("Declare", null, LabelingRuleType.Declare, Collections.singletonList(finishProduct2), null));

		// └──[root - 0.0 (7.0, vol: null) ]
		// ├──[pâte french - 3.5 (6.0, vol: null) Detail]
		// │ ├──[ing1 french - 0.6666666666666667 ( vol : null) ]
		// │ ├──[ing2 french - 1.3333333333333335 ( vol : null) ]
		// │ ├──[pâte french - 1.0 (3.0, vol: null) Detail]
		// │ │ ├──[ing1 french - 0.33333333333333337 ( vol : null) ]
		// │ │ ├──[ing2 french - 0.6666666666666667 ( vol : null) ]
		// │ │ └──[legal Raw material 12 - 2.0 (2.0, vol: null) Detail]
		// │ │ ├──[ing1 french - 0.5 ( vol : null) ]
		// │ │ └──[ing2 french - 1.5 ( vol : null) ]
		// │ ├──[garniture french - 1.0 (6.0, vol: null) Detail]
		// │ │ ├──[ing3 french - 5.0 ( vol : null) ]
		// │ │ └──[ing4 french - 1.0 ( vol : null) ]
		// │ └──[legal Raw material 12 - 2.0 (2.0, vol: null) Detail]
		// │ ├──[ing1 french - 0.5 ( vol : null) ]
		// │ └──[ing2 french - 1.5 ( vol : null) ]
		// └──[garniture french - 3.5 (12.0, vol: null) Detail]
		// ├──[ing3 french - 10.0 ( vol : null) ]
		// └──[ing4 french - 2.0 ( vol : null) ]

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"pâte french 50% (legal Raw material 12 16,7% (ing2 french 12,5%, ing1 french 4,2%), ing2 french 11,1%, pâte french 8,3% (legal Raw material 12 5,6% (ing2 french 4,2%, ing1 french 1,4%), ing2 french 1,9%, ing1 french 0,9%), garniture french 8,3% (ing3 french 6,9%, ing4 french 1,4%), ing1 french 5,6%), garniture french 50% (ing3 french 41,7%, ing4 french 8,3%)",
				Locale.FRENCH);
			// TODO //Do not Declare ????

	}

	private void checkILL(final NodeRef productNodeRef, final List<LabelingRuleListDataItem> labelingRuleList, final String ill, Locale locale) {

		logger.info("checkILL : " + ill);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			ProductData formulatedProduct = alfrescoRepository.findOne(productNodeRef);

			formulatedProduct.getLabelingListView().setLabelingRuleList(labelingRuleList);

			productService.formulate(formulatedProduct);

			Assert.assertTrue(formulatedProduct.getLabelingListView().getLabelingRuleList().size() > 0);

			// verify IngLabelingList

			try {

				Assert.assertNotNull("IngLabelingList is null", formulatedProduct.getLabelingListView().getIngLabelingList());
				Assert.assertTrue(formulatedProduct.getLabelingListView().getIngLabelingList().size() > 0);

				for (IngLabelingListDataItem illDataItem : formulatedProduct.getLabelingListView().getIngLabelingList()) {
					String formulatedIll = illDataItem.getValue().getValue(locale);
					Assert.assertEquals("Incorrect label :" + formulatedIll + "\n   - compare to " + ill, ill, formulatedIll);
					Assert.assertNotNull(illDataItem.getLogValue());

				}
			} catch (Throwable e) {
				logger.error(e);
				throw e;
			}

			return null;

		}, false, true);

	}

	private void checkError(final NodeRef productNodeRef, final List<LabelingRuleListDataItem> labelingRuleList, final String errorMessage) {
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			ProductData formulatedProduct = alfrescoRepository.findOne(productNodeRef);

			formulatedProduct.getLabelingListView().setLabelingRuleList(labelingRuleList);

			productService.formulate(formulatedProduct);

			assertFalse(formulatedProduct.getCompoListView().getReqCtrlList().isEmpty());

			for (ReqCtrlListDataItem reqCtrlListDataItem : formulatedProduct.getCompoListView().getReqCtrlList()) {
				if (RequirementType.Forbidden.equals(reqCtrlListDataItem.getReqType())) {

					String error = reqCtrlListDataItem.getReqMessage();
					if (RequirementDataType.Validation.equals(reqCtrlListDataItem.getReqDataType())) {
						Assert.assertEquals("Composant non validé", error);
					} else if (RequirementDataType.Completion.equals(reqCtrlListDataItem.getReqDataType())) {
						assertTrue(error.equals("Champ obligatoire 'Libellé légal' manquant (catalogue 'EU 1169/2011 (INCO)')")
								|| error.equals("Champ obligatoire 'Poids net (kg)' manquant (catalogue 'EU 1169/2011 (INCO)')")
								|| error.equals("Champ obligatoire 'Précautions d'emploi' manquant (catalogue 'EU 1169/2011 (INCO)')")
								|| error.equals("Champ obligatoire 'Conditions de conservation' manquant (catalogue 'EU 1169/2011 (INCO)')")
								|| error.equals("Champ obligatoire 'Origine géographique' manquant (catalogue 'EU 1169/2011 (INCO)')"));
					} else {
						Assert.assertEquals("Incorrect label :" + error + "\n   - compare to " + errorMessage, error, errorMessage);
					}
				} else if (RequirementType.Info.equals(reqCtrlListDataItem.getReqType())) {
					logger.info("Info rclDataItem, message: " + reqCtrlListDataItem.getReqMessage());
					Assert.assertEquals("Etat de l'allégation {0} indéfini", reqCtrlListDataItem.getReqMessage());
				}
			}

			return null;

		}, false, true);

	}

	@Test
	public void testMultiLingualLabelingFormulation() throws Exception {

		logger.info("testLabelingFormulation");

		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		// Test locale + format %
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Langue", "fr,en", LabelingRuleType.Locale));
		labelingRuleList
				.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, Arrays.asList(ing2, ing3, ing4), null));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));

		final NodeRef finishedProductNodeRef1 = createTestProduct(labelingRuleList);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Formulate product --*/
			logger.debug("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef1);

			/*-- Verify formulation --*/
			logger.debug("/*-- Verify formulation --*/");
			ProductData formulatedProduct1 = alfrescoRepository.findOne(finishedProductNodeRef1);

			assertTrue(formulatedProduct1.getLabelingListView().getLabelingRuleList().size() > 0);

			// verify IngLabelingList
			assertNotNull("IngLabelingList is null", formulatedProduct1.getLabelingListView().getIngLabelingList());
			assertTrue(formulatedProduct1.getLabelingListView().getIngLabelingList().size() > 0);

			for (IngLabelingListDataItem illDataItem : formulatedProduct1.getLabelingListView().getIngLabelingList()) {

				checkILL("garniture french 50% (ing3 french 41,7%, ing4 french 8,3%)",
						"pâte french 50% (legal Raw material 12 33,3% (ing2 french 25%, ing1 french), ing2 french 11,1%, ing1 french)",
						illDataItem.getValue().getValue(Locale.FRENCH));

				checkILL("garniture english 50% (ing3 english 41,7%, ing4 english 8,3%)",
						"pâte english 50% (legal Raw material 12 33,3% (ing2 english 25%, ing1 english), ing2 english 11,1%, ing1 english)",
						illDataItem.getValue().getValue(Locale.ENGLISH));

			}

			return null;

		}, false, true);
	}

	@Test
	public void testIncTypeThreshold() throws Exception {

		List<LabelingRuleListDataItem> labelingRuleList;
		// Aggregate
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Aggregate 1", null, LabelingRuleType.DoNotDetails, Arrays.asList(ing1, ing2),
				Collections.singletonList(ing3)));
		labelingRuleList.add(new LabelingRuleListDataItem("Aggregate 2", null, LabelingRuleType.DoNotDetails, Collections.singletonList(ing4),
				Collections.singletonList(ing6)));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));
		labelingRuleList
				.add(new LabelingRuleListDataItem("Param1bis", "ingDefaultFormat = \"{0} {1,number,0.#%}\"", LabelingRuleType.Prefs, null, null));
		labelingRuleList.add(
				new LabelingRuleListDataItem("Param2", "ingTypeDefaultFormat = \"{0} {1,number,0.#%}: ({2})\"", LabelingRuleType.Prefs, null, null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param3", "ingTypeDecThresholdFormat = \"{0} {1,number,0.#%} [{2}] \"",
				LabelingRuleType.Prefs, null, null));

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
				"pâte french 50% (legal Raw material 12 33,3% (ing3 french 33,3%), ing3 french 16,7%), garniture french 50% (ing3 french 41,7%, epices french 8,3% [ing6 french 8,3%])",
				Locale.FRENCH);

		labelingRuleList.add(new LabelingRuleListDataItem("Param4", "showIngCEECode = true", LabelingRuleType.Prefs, null, null));

		checkILL(finishedProductNodeRef1, labelingRuleList,
				"pâte french 50% (legal Raw material 12 33,3% (ing3 french 33,3%), ing3 french 16,7%), garniture french 50% (ing3 french 41,7%, epices french 8,3% [CEE6 8,3%])",
				Locale.FRENCH);

	}

	@Test
	public void testMultiThreadFormulation() throws Exception {
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			BeCPGTestHelper.createUser("labellingUser1");

			return null;
		});
		try {
			authenticationComponent.setCurrentUser("labellingUser1");

			final NodeRef finishProduct1 = createTestProduct(null);
			final NodeRef finishProduct2 = createTestProduct(null);

			NodeRef finishedProductNodeRef1 = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

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
				compoList1.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF11NodeRef));
				compoList1.add(
						new CompoListDataItem(null, compoList1.get(0), null, 1d, CompoListUnit.kg, 0d, DeclarationType.DoNotDetails, finishProduct1));
				compoList1.add(
						new CompoListDataItem(null, compoList1.get(0), null, 2d, CompoListUnit.kg, 0d, DeclarationType.DoNotDetails, finishProduct2));
				compoList1.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF12NodeRef));
				compoList1.add(
						new CompoListDataItem(null, compoList1.get(3), null, 3d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial13NodeRef));
				compoList1.add(new CompoListDataItem(null, compoList1.get(3), null, 3d, CompoListUnit.kg, 0d, DeclarationType.Declare,
						rawMaterial14NodeRef));

				finishedProduct1.getCompoListView().setCompoList(compoList1);

				// Declare
				List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

				labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
				labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, null, null));
				labelingRuleList.add(new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"",
						LabelingRuleType.Prefs, null, null));

				finishedProduct1.getLabelingListView().setLabelingRuleList(labelingRuleList);

				return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();
			}, false, true);

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

				String ill = "pâte french 50% (legal Finished product 1 50%), garniture french 50% (legal Raw material 13 25% (ing3 french 25%), ing3 french 16,7%, ing4 french 8,3%)";

				transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

					productService.formulate(finishedProductNodeRef1);

					ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef1);

					Assert.assertTrue(formulatedProduct.getLabelingListView().getLabelingRuleList().size() > 0);

					// verify IngLabelingList

					Assert.assertNotNull("IngLabelingList is null", formulatedProduct.getLabelingListView().getIngLabelingList());
					Assert.assertTrue(formulatedProduct.getLabelingListView().getIngLabelingList().size() > 0);

					for (IngLabelingListDataItem illDataItem : formulatedProduct.getLabelingListView().getIngLabelingList()) {
						String formulatedIll = illDataItem.getValue().getValue(Locale.FRENCH);
						Assert.assertEquals("Incorrect label :" + formulatedIll + "\n   - compare to " + ill, ill, formulatedIll);
						Assert.assertNotNull(illDataItem.getLogValue());
						logger.info("Finished labeling in thread " +
						 threadName );
					}

					return null;

				}, false, true);

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
