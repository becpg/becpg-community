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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.ibm.icu.util.Calendar;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.LabelingRuleType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

/**
 * 
 * @author matthieu
 *
 */
public class LabelingFormulationTest extends AbstractFinishedProductTest {

	protected static Log logger = LogFactory.getLog(LabelingFormulationTest.class);

	@Resource
	private AssociationService associationService;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
	}

	private NodeRef createTestProduct(final List<LabelingRuleListDataItem> labelingRuleList) {
		return transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/**
				 * Finished product 1
				 */
				logger.debug("/*************************************/");
				logger.debug("/*-- Create Finished product 1--*/");
				logger.debug("/*************************************/");
				FinishedProductData finishedProduct1 = new FinishedProductData();
				finishedProduct1.setName("Finished product "+Calendar.getInstance().getTimeInMillis());
				finishedProduct1.setLegalName("Legal Finished product 1");
				finishedProduct1.setQty(2d);
				finishedProduct1.setUnit(ProductUnit.kg);
				finishedProduct1.setDensity(1d);
				List<CompoListDataItem> compoList1 = new ArrayList<CompoListDataItem>();
				compoList1.add(new CompoListDataItem(null, (CompoListDataItem) null, 1d, null, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF11NodeRef));
				compoList1.add(new CompoListDataItem(null, compoList1.get(0), 1d, null, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial11NodeRef));
				compoList1.add(new CompoListDataItem(null, compoList1.get(0), 2d, null, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial12NodeRef));
				compoList1.add(new CompoListDataItem(null, (CompoListDataItem) null, 1d, null, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF12NodeRef));
				compoList1.add(new CompoListDataItem(null, compoList1.get(3), 3d, null, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial13NodeRef));
				compoList1.add(new CompoListDataItem(null, compoList1.get(3), 3d, null, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial14NodeRef));

				finishedProduct1.getCompoListView().setCompoList(compoList1);

				finishedProduct1.getLabelingListView().setLabelingRuleList(labelingRuleList);


				return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();
			}
		}, false, true);
	}

	// private String detailsDefaultFormat = "{0} {1,number,0.#%} ({2})";
	
   @Test
	public void testNullIng() throws Exception {
		
        NodeRef finishedProductNodeRef1 =   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
				FinishedProductData finishedProduct1 = new FinishedProductData();
				finishedProduct1.setName("Finished product "+Calendar.getInstance().getTimeInMillis());
				finishedProduct1.setLegalName("Legal Finished product 1");
				finishedProduct1.setQty(2d);
				finishedProduct1.setUnit(ProductUnit.kg);
				finishedProduct1.setDensity(1d);
				List<CompoListDataItem> compoList1 = new ArrayList<CompoListDataItem>();
				compoList1.add(new CompoListDataItem(null, (CompoListDataItem) null, 1d, null, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial16NodeRef));

				finishedProduct1.getCompoListView().setCompoList(compoList1);



				return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();
			}
		}, false, true);
        

		
		
//		└──[root - 0.0 (1.0)]
//			    ├──[ing1 french - null]
//			    ├──[ing3 french - 0.55]
//			    └──[ing2 french - null]
       
		//Declare
		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Declare", null, LabelingRuleType.Declare, Arrays.asList(rawMaterial16NodeRef), null));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, null, null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));

		
		checkILL(finishedProductNodeRef1, labelingRuleList, "ing3 french 55%, ing1 french, ing2 french", Locale.FRENCH);
		
		labelingRuleList = new ArrayList<>();
		
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Detail", null, LabelingRuleType.Detail, Arrays.asList(rawMaterial16NodeRef), null));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, null, null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));
		
		
		checkILL(finishedProductNodeRef1, labelingRuleList, "Legal Raw material 16 100% (ing1 french, ing3 french 55%, ing2 french)", Locale.FRENCH);
		
		
		final NodeRef finishProduct1 = createTestProduct(null);
		
		final NodeRef finishProduct2 = createTestProduct(null);
		

		 finishedProductNodeRef1 = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
				FinishedProductData finishedProduct1 = new FinishedProductData();
				finishedProduct1.setName("Finished product "+Calendar.getInstance().getTimeInMillis());
				finishedProduct1.setLegalName("Legal Finished product 1");
				finishedProduct1.setQty(2d);
				finishedProduct1.setUnit(ProductUnit.kg);
				finishedProduct1.setDensity(1d);
				List<CompoListDataItem> compoList1 = new ArrayList<CompoListDataItem>();
				compoList1.add(new CompoListDataItem(null, (CompoListDataItem) null, 1d, null, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF11NodeRef));
				compoList1.add(new CompoListDataItem(null, compoList1.get(0), 1d, null, CompoListUnit.kg, 0d, DeclarationType.Group, finishProduct1));
				compoList1.add(new CompoListDataItem(null, compoList1.get(0), 2d, null, CompoListUnit.kg, 0d, DeclarationType.Group, finishProduct2));
				compoList1.add(new CompoListDataItem(null, (CompoListDataItem) null, 1d, null, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF12NodeRef));
				compoList1.add(new CompoListDataItem(null, compoList1.get(3), 3d, null, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial13NodeRef));
				compoList1.add(new CompoListDataItem(null, compoList1.get(3), 3d, null, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial16NodeRef));

				finishedProduct1.getCompoListView().setCompoList(compoList1);
				
				return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();
			}
		}, false, true);
		 
		 
		labelingRuleList = new ArrayList<>(); 
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, null, null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));
		labelingRuleList.add(new LabelingRuleListDataItem("Aggr", null, LabelingRuleType.Group,Arrays.asList(rawMaterial13NodeRef,rawMaterial16NodeRef),null));
		
		checkILL(finishedProductNodeRef1, labelingRuleList, "<b>Aggr (38,7%):</b> ing3 french 100%, ing1 french, ing2 french<br/><b>Legal Finished product 1 (33,3%):</b> Pâte french 50% (Legal Raw material 12 66,7% (ing2 french 75%, ing1 french 25%), ing2 french 22,2%, ing1 french 11,1%), Garniture french 50% (ing3 french 83,3%, ing4 french 16,7%)<br/><b>Legal Finished product 1 (16,7%):</b> Pâte french 50% (Legal Raw material 12 66,7% (ing2 french 75%, ing1 french 25%), ing2 french 22,2%, ing1 french 11,1%), Garniture french 50% (ing3 french 83,3%, ing4 french 16,7%)<br/>Garniture french 11,3%", Locale.FRENCH);
		
		checkError(finishedProductNodeRef1,labelingRuleList, "Impossible de déclarer ou d'aggreger l'ingrédient ing2 sans quantité, changer le type de déclaration du composant");


//	TODO    Auxiliaires technologiques
//
//        J'ai un aux tech dans ma MP sel. J'utilise la MP sel dans le SF pâte. Si je dis de masquer lex aux tech, je vois l'aux tech du sel car l'info est perdu au niveau du SF
//        J'ai un aux tech dans ma MP sel. J'utilise ce même aux tech dans le SF pâte qui utilise aussi le sel. Si je dis de masquer lex aux tech, il faut prendre dans le calcul slmt la qté MeO dans le SF.


		
	}
	
	@Test
	public void testMultiLevelSFGroup() throws Exception {
		


		final NodeRef finishProduct1 = createTestProduct(null);
			
		final NodeRef finishProduct2 = createTestProduct(null);
		
		
		
		NodeRef finishedProductNodeRef1 = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
				FinishedProductData finishedProduct1 = new FinishedProductData();
				finishedProduct1.setName("Finished product "+Calendar.getInstance().getTimeInMillis());
				finishedProduct1.setLegalName("Legal Finished product 1");
				finishedProduct1.setQty(2d);
				finishedProduct1.setUnit(ProductUnit.kg);
				finishedProduct1.setDensity(1d);
				List<CompoListDataItem> compoList1 = new ArrayList<CompoListDataItem>();
				compoList1.add(new CompoListDataItem(null, (CompoListDataItem) null, 1d, null, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF11NodeRef));
				compoList1.add(new CompoListDataItem(null, compoList1.get(0), 1d, null, CompoListUnit.kg, 0d, DeclarationType.Group, finishProduct1));
				compoList1.add(new CompoListDataItem(null, compoList1.get(0), 2d, null, CompoListUnit.kg, 0d, DeclarationType.Group, finishProduct2));
				compoList1.add(new CompoListDataItem(null, (CompoListDataItem) null, 1d, null, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF12NodeRef));
				compoList1.add(new CompoListDataItem(null, compoList1.get(3), 3d, null, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial13NodeRef));
				compoList1.add(new CompoListDataItem(null, compoList1.get(3), 3d, null, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial14NodeRef));

				finishedProduct1.getCompoListView().setCompoList(compoList1);
				
				return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();
			}
		}, false, true);
		
		
			//Declare
			List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

			labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
			labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, null, null));
			labelingRuleList.add(new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));

			
//			└──[root - 0.0 (2.0) ]
//				    ├──[Garniture french - 1.0 (6.0) Detail]
//				    │   ├──[Legal Raw material 13 - 3.0 (3.0) Detail]
//				    │   │   └──[ing3 french - 3.0]
//				    │   ├──[ing3 french - 2.0]
//				    │   └──[ing4 french - 1.0]
//				    ├──[Legal Finished product 1 - 0.3333333333333333 (2.0) Group]
//				    │   ├──[Pâte french - 1.0 (3.0) Detail]
//				    │   │   ├──[ing1 french - 0.33333333333333337]
//				    │   │   ├──[ing2 french - 0.6666666666666667]
//				    │   │   └──[Legal Raw material 12 - 2.0 (2.0) Detail]
//				    │   │       ├──[ing1 french - 0.5]
//				    │   │       └──[ing2 french - 1.5]
//				    │   └──[Garniture french - 1.0 (6.0) Detail]
//				    │       ├──[ing3 french - 5.0]
//				    │       └──[ing4 french - 1.0]
//				    └──[Legal Finished product 1 - 0.6666666666666666 (2.0) Group]
//				        ├──[Pâte french - 1.0 (3.0) Detail]
//				        │   ├──[ing1 french - 0.33333333333333337]
//				        │   ├──[ing2 french - 0.6666666666666667]
//				        │   └──[Legal Raw material 12 - 2.0 (2.0) Detail]
//				        │       ├──[ing1 french - 0.5]
//				        │       └──[ing2 french - 1.5]
//				        └──[Garniture french - 1.0 (6.0) Detail]
//				            ├──[ing3 french - 5.0]
//				            └──[ing4 french - 1.0]

			
			
			checkILL(finishedProductNodeRef1, labelingRuleList, "<b>Legal Finished product 1 (33,3%):</b> Pâte french 50% (Legal Raw material 12 66,7% (ing2 french 75%, ing1 french 25%), ing2 french 22,2%, ing1 french 11,1%), Garniture french 50% (ing3 french 83,3%, ing4 french 16,7%)<br/><b>Legal Finished product 1 (16,7%):</b> Pâte french 50% (Legal Raw material 12 66,7% (ing2 french 75%, ing1 french 25%), ing2 french 22,2%, ing1 french 11,1%), Garniture french 50% (ing3 french 83,3%, ing4 french 16,7%)<br/>Garniture french 50% (Legal Raw material 13 50% (ing3 french 100%), ing3 french 33,3%, ing4 french 16,7%)", Locale.FRENCH);
		
		
			labelingRuleList = new ArrayList<>();

			labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
			labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, null, null));
			labelingRuleList.add(new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));
			labelingRuleList.add(new LabelingRuleListDataItem("Aggr", null, LabelingRuleType.Group,Arrays.asList(rawMaterial13NodeRef,rawMaterial14NodeRef),null));
			
//			└──[root - 0.0 (2.0) ]
//				    ├──[Aggr - 1.0 (6.0) Group]
//				    │   ├──[Legal Raw material 13 - 3.0 (3.0) Detail]
//				    │   │   └──[ing3 french - 3.0]
//				    │   ├──[ing3 french - 2.0]
//				    │   └──[ing4 french - 1.0]
//				    ├──[Legal Finished product 1 - 0.16666666666666666 (1.0) Group]
//				    │   └──[Pâte french - 1.0 (3.0) Detail]
//				    │       ├──[ing1 french - 0.33333333333333337]
//				    │       ├──[ing2 french - 0.6666666666666667]
//				    │       └──[Legal Raw material 12 - 2.0 (2.0) Detail]
//				    │           ├──[ing1 french - 0.5]
//				    │           └──[ing2 french - 1.5]
//				    └──[Legal Finished product 1 - 0.3333333333333333 (1.0) Group]
//				        └──[Pâte french - 1.0 (3.0) Detail]
//				            ├──[ing1 french - 0.33333333333333337]
//				            ├──[ing2 french - 0.6666666666666667]
//				            └──[Legal Raw material 12 - 2.0 (2.0) Detail]
//				                ├──[ing1 french - 0.5]
//				                └──[ing2 french - 1.5]


				
			checkILL(finishedProductNodeRef1, labelingRuleList,"<b>Aggr (50%):</b> Legal Raw material 13 50% (ing3 french 100%), ing3 french 33,3%, ing4 french 16,7%<br/><b>Legal Finished product 1 (16,7%):</b> Pâte french 100% (Legal Raw material 12 66,7% (ing2 french 75%, ing1 french 25%), ing2 french 22,2%, ing1 french 11,1%)<br/><b>Legal Finished product 1 (8,3%):</b> Pâte french 100% (Legal Raw material 12 66,7% (ing2 french 75%, ing1 french 25%), ing2 french 22,2%, ing1 french 11,1%)", Locale.FRENCH);
		
			
		}
	
	
	@Test
	public void testReconstitutionLabeling() throws Exception {
//		1. Liste d'ingrédients par ordre pondéral	
//		
//		Les sucres et l'acide :	les pourcentages pondéraux du saccharose et de l'acide citrique sont calculés sur la matière sèche (mise en œuvre de sucre sec et d'acide citrique en poudre)
//			le pourcentage pondéral du glucose est calculé sur le glucose liquide (mise en œuvre d'un sirop de glucose)
//			 (voir colonne %P/P avant ou après reconstitution)
//			
//		Les jus :	Les jus mis en œuvre sont des jus concentrés, le pourcentage pondéral est calculé après reconstitution des jus (voir colonne %P/P après reconstitution des jus)
//			
//		2. Déclaration des jus	La réglementation des sirops impose une déclaration de la liste d'ingrédients par ordre pondéral avec reconstitution des jus, 
//			et déclaration de la quantité de jus reconstitués totale et de la quantité du jus reconstitué de la dénomination (ici la fraise) en pourcentage volume / volume
//			(voir colonne %P/P avant reconstitution des jus)
//			Avec l'exemple du cas pratique sirop de fraise :
//			sucre, sirop de glucose-fructose, eau,  jus de fruits à base de concentrés 13% ( fraise 10%, sureau), acidifiant: acide citrique, arôme, colorant: E 129.
//	

		
		 final NodeRef finishedProductNodeRef1 =   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
				public NodeRef execute() throws Throwable {
					logger.debug("/*-- Create finished product --*/");
					FinishedProductData finishedProduct = new FinishedProductData();
					finishedProduct.setName("Produit fini 1");
					finishedProduct.setLegalName("Legal Produit fini 1");
					finishedProduct.setUnit(ProductUnit.kg);
					finishedProduct.setQty(4d);
					finishedProduct.setDensity(1d);
					List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
					
					compoList.add(new CompoListDataItem(null, (CompoListDataItem)null, null, 10d, CompoListUnit.kg, 0d, DeclarationType.DoNotDetails, rawMaterial7NodeRef)); 
					compoList.add(new CompoListDataItem(null, (CompoListDataItem) null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
					
					Map<QName,Serializable> props = new HashMap<>();
					props.put(PLMModel.PROP_RECONSTITUTION_RATE,5d);
					nodeService.addAspect(rawMaterial1NodeRef, PLMModel.ASPECT_RECONSTITUTABLE, props);
					nodeService.addAspect(rawMaterial7NodeRef,PLMModel.ASPECT_DILUENT,null);
					
					finishedProduct.getCompoListView().setCompoList(compoList);
					return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();				
				}
			}, false, true);
		
		 
			//Declare
			List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		//	labelingRuleList.add(new LabelingRuleListDataItem("Pref1", "useVolume = true", LabelingRuleType.Prefs));
			labelingRuleList.add(new LabelingRuleListDataItem("Pref2", "ingDefaultFormat = \"{0} {1,number,0.#%}\"", LabelingRuleType.Prefs));
			labelingRuleList.add(new LabelingRuleListDataItem("Pref3", "groupDefaultFormat = \"<b>{0} ({1,number,0.#%}):</b> {2}\"", LabelingRuleType.Prefs));
			labelingRuleList.add(new LabelingRuleListDataItem("Pref4", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs));
			labelingRuleList.add(new LabelingRuleListDataItem("Pref5", "ingTypeDefaultFormat = \"{0}: {2}\"", LabelingRuleType.Prefs));
			labelingRuleList.add(new LabelingRuleListDataItem("Pref6", "subIngsDefaultFormat = \"{0} ({2})\"", LabelingRuleType.Prefs));
			
			labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
			labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, null, null));
			labelingRuleList.add(new LabelingRuleListDataItem("Juice", null, LabelingRuleType.Detail,Arrays.asList(ing1,ing2),null));

			
			checkILL(finishedProductNodeRef1, labelingRuleList, "Legal Raw material 7 54,5%, Juice 45,5% (ing2 french 66,7%, ing1 french 33,3%)", Locale.FRENCH);
			
			NodeRef finishedProductNodeRef2 =   transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
				public NodeRef execute() throws Throwable {
					logger.debug("/*-- Create finished product --*/");
					FinishedProductData finishedProduct = new FinishedProductData();
					finishedProduct.setName("Produit fini 2");
					finishedProduct.setLegalName("Legal Produit fini 2");
					finishedProduct.setUnit(ProductUnit.kg);
					finishedProduct.setQty(4d);
					finishedProduct.setDensity(1d);
					List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
					
					compoList.add(new CompoListDataItem(null, (CompoListDataItem)null, null, 10d, CompoListUnit.kg, 0d, DeclarationType.DoNotDetails, rawMaterial7NodeRef)); 
					compoList.add(new CompoListDataItem(null, (CompoListDataItem) null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.DoNotDetails, rawMaterial1NodeRef));
					compoList.add(new CompoListDataItem(null, (CompoListDataItem) null, null, 5d, CompoListUnit.kg, 0d, DeclarationType.Group, finishedProductNodeRef1));
					
					Map<QName,Serializable> props = new HashMap<>();
					props.put(PLMModel.PROP_RECONSTITUTION_RATE,5d);
					nodeService.addAspect(rawMaterial1NodeRef, PLMModel.ASPECT_RECONSTITUTABLE, props);
					nodeService.addAspect(rawMaterial7NodeRef,PLMModel.ASPECT_DILUENT,null);
					
					finishedProduct.getCompoListView().setCompoList(compoList);
					return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();				
				}
			}, false, true);
		
		 
			
			
			
			//Declare
			labelingRuleList = new ArrayList<>();

		//	labelingRuleList.add(new LabelingRuleListDataItem("Pref1", "useVolume = false", LabelingRuleType.Prefs));
			labelingRuleList.add(new LabelingRuleListDataItem("Pref2", "ingDefaultFormat = \"{0}\"", LabelingRuleType.Prefs));
			labelingRuleList.add(new LabelingRuleListDataItem("Pref3", "groupDefaultFormat = \"<b>{0} ({1,number,0.#%}):</b> {2}\"", LabelingRuleType.Prefs));
			labelingRuleList.add(new LabelingRuleListDataItem("Pref4", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs));
			labelingRuleList.add(new LabelingRuleListDataItem("Pref5", "ingTypeDefaultFormat = \"{0}: {2})\"", LabelingRuleType.Prefs));
			labelingRuleList.add(new LabelingRuleListDataItem("Pref6", "subIngsDefaultFormat = \"{0} ({2})\"", LabelingRuleType.Prefs));
			labelingRuleList.add(new LabelingRuleListDataItem("DoNotDetails", null, LabelingRuleType.DoNotDetails, Arrays.asList(rawMaterial1NodeRef),null));
			
			labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render(false)", LabelingRuleType.Render));
			labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, null, null));

			checkILL(finishedProductNodeRef2, labelingRuleList, "Legal Raw material 7 54,5%, Legal Raw material 1 45,5%", Locale.FRENCH);
			
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
	//		└──[root - 0.0 (2.0)]
		//	    ├──[Pâte french - 1.0 (3.0)]
		//	    │   ├──[ing1 french - 0.33333333333333337]
		//	    │   ├──[ing2 french - 0.6666666666666667]
		//	    │   └──[Legal Raw material 12 - 2.0 (2.0)]
		//	    │       ├──[ing1 french - 0.5]
		//	    │       └──[ing2 french - 1.5]
		//	    └──[Garniture french - 1.0 (6.0)]
		//	        ├──[ing3 french - 5.0]
		//	        └──[ing4 french - 1.0]

		final NodeRef finishedProductNodeRef1 = createTestProduct(null);
		
		//Declare
		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Declare", null, LabelingRuleType.Declare, Arrays.asList(localSF11NodeRef, rawMaterial12NodeRef, localSF12NodeRef), null));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, Arrays.asList(ing1 ,ing2, ing3, ing4), null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));
		
	//		└──[root - 0.0 (9.0)]
	//			    ├──[ing1 french - 0.8333333333333334]
	//			    ├──[ing2 french - 2.166666666666667]
	//			    ├──[ing3 french - 5.0]
	//			    └──[ing4 french - 1.0]

		
		checkILL(finishedProductNodeRef1, labelingRuleList, "ing3 french 55,6%, ing2 french 24,1%, ing4 french 11,1%, ing1 french 9,3%", Locale.FRENCH);

			
		//Omit
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Omit", null, LabelingRuleType.Omit, Arrays.asList(localSF12NodeRef), null));
		//Ing2 dans rawMaterial 12 est un auxiliare
		labelingRuleList.add(new LabelingRuleListDataItem("Auxiliare", "ingListDataItem.isProcessingAid == true", LabelingRuleType.Omit, null, null));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, Arrays.asList(ing1 ,ing2, ing3, ing4), null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));
		
		
//		└──[root - 0.0 (1.0)]
//			    └──[Pâte french - 1.0 (3.0)]
//			        ├──[ing1 french - 0.33333333333333337]
//			        ├──[ing2 french - 0.6666666666666667]
//			        └──[Legal Raw material 12 - 2.0 (2.0)]
//			            └──[ing1 french - 0.5]

		
		
		checkILL(finishedProductNodeRef1, labelingRuleList, "Pâte french 100% (Legal Raw material 12 66,7% (ing1 french 25%), ing2 french 22,2%, ing1 french 11,1%)", Locale.FRENCH);
		
		//Test Do not Declare IngType
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Aggregate 2", null, LabelingRuleType.DoNotDetails, Arrays.asList(ing4),Arrays.asList(ing5)));
		labelingRuleList.add(new LabelingRuleListDataItem("Do not declare", null, LabelingRuleType.DoNotDeclare, Arrays.asList(ingType1), null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));

		//		
//		└──[root - 0.0 (2.0)]
//			    ├──[Pâte french - 1.0 (3.0)]
//			    │   ├──[ing1 french - 0.33333333333333337]
//			    │   ├──[ing2 french - 0.6666666666666667]
//			    │   └──[Legal Raw material 12 - 2.0 (2.0)]
//			    │       ├──[ing1 french - 0.5]
//			    │       └──[ing2 french - 1.5]
//			    └──[Garniture french - 1.0 (6.0)]
//			        ├──[ing3 french - 5.0]
//			        └──[ing5 french - 1.0]


		checkILL(finishedProductNodeRef1, labelingRuleList, "Pâte french 50% (Legal Raw material 12 66,7% (ing2 french, ing1 french), ing2 french, ing1 french), Garniture french 50% (ing3 french, ing5 french)", Locale.FRENCH);
		
		//Test Omit IngType
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Aggregate 2", null, LabelingRuleType.DoNotDetails, Arrays.asList(ing4),Arrays.asList(ing5)));
		labelingRuleList.add(new LabelingRuleListDataItem("Omit", null, LabelingRuleType.Omit, Arrays.asList(ingType1), null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));
				//		
//				└──[root - 0.0 (2.0)]
//					    ├──[Pâte french - 1.0 (3.0)]
//					    │   ├──[ing1 french - 0.33333333333333337]
//					    │   ├──[ing2 french - 0.6666666666666667]
//					    │   └──[Legal Raw material 12 - 2.0 (2.0)]
//					    │       ├──[ing1 french - 0.5]
//					    │       └──[ing2 french - 1.5]
//					    └──[Garniture french - 1.0 (6.0)]
//					        ├──[ing3 french - 5.0]
//					        └──[ing5 french - 1.0]



				
	checkILL(finishedProductNodeRef1, labelingRuleList, "Pâte french 50% (Legal Raw material 12 66,7% (ing2 french, ing1 french), ing2 french, ing1 french), Garniture french 50% (ing3 french)", Locale.FRENCH);



		//Details
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Details", null, LabelingRuleType.Detail, Arrays.asList(rawMaterial11NodeRef), null));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, Arrays.asList(ing1 ,ing2, ing3, ing4), null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));

		
//		└──[root - 0.0 (2.0)]
//			    ├──[Pâte french - 1.0 (3.0)]
//			    │   ├──[Legal Raw material 11 - 1.0 (1.0)]
//			    │   │   ├──[ing1 french - 0.33333333333333337]
//			    │   │   └──[ing2 french - 0.6666666666666667]
//			    │   └──[Legal Raw material 12 - 2.0 (2.0)]
//			    │       ├──[ing1 french - 0.5]
//			    │       └──[ing2 french - 1.5]
//			    └──[Garniture french - 1.0 (6.0)]
//			        ├──[ing3 french - 5.0]
//			        └──[ing4 french - 1.0]
		
		checkILL(finishedProductNodeRef1, labelingRuleList,"Pâte french 50% (Legal Raw material 12 66,7% (ing2 french 75%, ing1 french 25%), Legal Raw material 11 33,3% (ing2 french 66,7%, ing1 french 33,3%)), Garniture french 50% (ing3 french 83,3%, ing4 french 16,7%)", Locale.FRENCH);
		
		
		//Do not details
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("DoNotDetails", null, LabelingRuleType.DoNotDetails, Arrays.asList(rawMaterial11NodeRef), null));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, Arrays.asList(ing1 ,ing2, ing3, ing4), null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));

		
//		└──[root - 0.0 (2.0)]
//			    ├──[Pâte french - 1.0 (3.0)]
//			    │   ├──[Legal Raw material 11 - 1.0 (1.0)]
//			    │   └──[Legal Raw material 12 - 2.0 (2.0)]
//			    │       ├──[ing1 french - 0.5]
//			    │       └──[ing2 french - 1.5]
//			    └──[Garniture french - 1.0 (6.0)]
//			        ├──[ing3 french - 5.0]
//			        └──[ing4 french - 1.0]

		checkILL(finishedProductNodeRef1, labelingRuleList,"Pâte french 50% (Legal Raw material 12 66,7% (ing2 french 75%, ing1 french 25%), Legal Raw material 11 33,3%), Garniture french 50% (ing3 french 83,3%, ing4 french 16,7%)", Locale.FRENCH);
		
		
		//Rename 
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Rename 1", null, LabelingRuleType.Rename, Arrays.asList(ing1), Arrays.asList(ing5)));
		labelingRuleList.add(new LabelingRuleListDataItem("Aggregate 2", null, LabelingRuleType.DoNotDetails, Arrays.asList(ing4),Arrays.asList(ing5)));
		labelingRuleList.add(new LabelingRuleListDataItem("Rename 2", "Test rename2", LabelingRuleType.Rename, Arrays.asList(rawMaterial12NodeRef),null));
		labelingRuleList.add(new LabelingRuleListDataItem("Rename 3", "path.allergens", LabelingRuleType.Rename, Arrays.asList(ingType1), null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));
		
		
		
//		└──[root - 0.0 (2.0)]
//			    ├──[Pâte french - 1.0 (3.0)]
//			    │   ├──[ing1 french - 0.33333333333333337]
//			    │   ├──[ing2 french - 0.6666666666666667]
//			    │   └──[Legal Raw material 12 - 2.0 (2.0)]
//			    │       ├──[ing1 french - 0.5]
//			    │       └──[ing2 french - 1.5]
//			    └──[Garniture french - 1.0 (6.0)]
//			        ├──[ing3 french - 5.0]
//			        └──[ing5 french - 1.0]

		
		checkILL(finishedProductNodeRef1, labelingRuleList, "Pâte french 50% (Test rename2 66,7% (ing2 french, ing5 french), ing2 french, ing5 french), Garniture french 50% (ing3 french, Allergènes: ing5 french)", Locale.FRENCH);
												
		
		//Aggregate
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Aggregate 1", null, LabelingRuleType.DoNotDetails, Arrays.asList(ing1,ing2),Arrays.asList(ing3)));
		labelingRuleList.add(new LabelingRuleListDataItem("Aggregate 2", null, LabelingRuleType.DoNotDetails, Arrays.asList(ing4),Arrays.asList(ing5)));
		labelingRuleList.add(new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));
		
		//Todo test aggregate type or MP
		
		
//		└──[root - 0.0 (2.0)]
//			    ├──[Pâte french - 1.0 (3.0)]
//			    │   ├──[ing3 french - 1.0]
//			    │   └──[Legal Raw material 12 - 2.0 (2.0)]
//			    │       └──[ing3 french - 2.0]
//			    └──[Garniture french - 1.0 (6.0)]
//			        ├──[ing3 french - 5.0]
//			        └──[ing5 french - 1.0]
		


		
		checkILL(finishedProductNodeRef1, labelingRuleList, "Pâte french 50% (Legal Raw material 12 66,7% (ing3 french), ing3 french), Garniture french 50% (ing3 french, Epaississant french: ing5 french)", Locale.FRENCH);
		
		
		// Separator tests and plural
		
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Aggregate 1", null, LabelingRuleType.DoNotDetails, Arrays.asList(ing1,ing2),Arrays.asList(ing3)));
		labelingRuleList.add(new LabelingRuleListDataItem("Aggregate 2", null, LabelingRuleType.DoNotDetails, Arrays.asList(ing4),Arrays.asList(ing5)));
		labelingRuleList.add(new LabelingRuleListDataItem("Change type", null, LabelingRuleType.Type,Arrays.asList(ing3),Arrays.asList(ingType1)));
		labelingRuleList.add(new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param2", "defaultSeparator = \"; \"", LabelingRuleType.Prefs, null, null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param3", "ingTypeDefaultSeparator = \"# \"", LabelingRuleType.Prefs, null, null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param4", "groupDefaultSeparator = \"! \"", LabelingRuleType.Prefs, null, null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param5", "subIngsSeparator = \"@ \"", LabelingRuleType.Prefs, null, null));
				
		checkILL(finishedProductNodeRef1, labelingRuleList, "Pâte french 50% (Legal Raw material 12 66,7% (Epaississant french: ing3 french); Epaississant french: ing3 french); Garniture french 50% (Epaississants french: ing3 french# ing5 french)", Locale.FRENCH);

		

		//Combine
					
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Combine 1", new MLText("Comb 1"), "20,30", LabelingRuleType.Detail, Arrays.asList(ing1,ing2),null));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, Arrays.asList(ing1 ,ing2, ing3, ing4), null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));
		
//		└──[root - 0.0 (2.0)]
//			    ├──[Pâte french - 1.0 (3.0)]
//			    │   ├──[ing1 french - 0.2666666666666667]
//			    │   ├──[ing2 french - 0.4666666666666667]
//			    │   ├──[Legal Raw material 12 - 2.0 (2.0)]
//			    │   │   ├──[ing1 french - 0.4]
//			    │   │   ├──[ing2 french - 1.05]
//			    │   │   └──[Comb 1 - 0.55 (0.55)]
//			    │   │       ├──[ing1 french - 0.1]
//			    │   │       └──[ing2 french - 0.45]
//			    │   └──[Comb 1 - 0.2666666666666667 (0.2666666666666667)]
//			    │       ├──[ing1 french - 0.06666666666666668]
//			    │       └──[ing2 french - 0.20000000000000004]
//			    └──[Garniture french - 1.0 (6.0)]
//			        ├──[ing3 french - 5.0]
//			        └──[ing4 french - 1.0]

		
		checkILL(finishedProductNodeRef1, labelingRuleList, "Pâte french 50% (Legal Raw material 12 66,7% (ing2 french 52,5%, Comb 1 27,5% (ing2 french 81,8%, ing1 french 18,2%), ing1 french 20%), ing2 french 15,6%, ing1 french 8,9%, Comb 1 8,9% (ing2 french 75%, ing1 french 25%)), Garniture french 50% (ing3 french 83,3%, ing4 french 16,7%)", Locale.FRENCH);
	

		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Combine 1", new MLText("Comb 1"), "100,30", LabelingRuleType.Detail, Arrays.asList(ing1,ing2),null));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, Arrays.asList(ing1 ,ing2, ing3, ing4), null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));


//		└──[root - 0.0 (2.0)]
//			    ├──[Pâte french - 1.0 (3.0)]
//			    │   ├──[ing2 french - 0.4666666666666667]
//			    │   ├──[Legal Raw material 12 - 2.0 (2.0)]
//			    │   │   ├──[ing2 french - 1.05]
//			    │   │   └──[Comb 1 - 0.95 (0.95)]
//			    │   │       ├──[ing1 french - 0.5]
//			    │   │       └──[ing2 french - 0.45]
//			    │   └──[Comb 1 - 0.5333333333333334 (0.5333333333333334)]
//			    │       ├──[ing1 french - 0.33333333333333337]
//			    │       └──[ing2 french - 0.20000000000000004]
//			    └──[Garniture french - 1.0 (6.0)]
//			        ├──[ing3 french - 5.0]
//			        └──[ing4 french - 1.0]

		
		checkILL(finishedProductNodeRef1, labelingRuleList, "Pâte french 50% (Legal Raw material 12 66,7% (ing2 french 52,5%, Comb 1 47,5% (ing1 french 52,6%, ing2 french 47,4%)), Comb 1 17,8% (ing1 french 62,5%, ing2 french 37,5%), ing2 french 15,6%), Garniture french 50% (ing3 french 83,3%, ing4 french 16,7%)", Locale.FRENCH);

		
		
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Combine 2", new MLText("Decors 1"), null, LabelingRuleType.Group, Arrays.asList(localSF11NodeRef,localSF12NodeRef),null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));
		
//		└──[root - 0.0 (2.0)]
//			    └──[Decors 1 - 2.0 (2.0)]
//			        ├──[Pâte french - 1.0 (3.0)]
//			        │   ├──[ing1 french - 0.33333333333333337]
//			        │   ├──[ing2 french - 0.6666666666666667]
//			        │   └──[Legal Raw material 12 - 2.0 (2.0)]
//			        │       ├──[ing1 french - 0.5]
//			        │       └──[ing2 french - 1.5]
//			        └──[Garniture french - 1.0 (6.0)]
//			            ├──[ing3 french - 5.0]
//			            └──[ing4 french - 1.0]

		
		
		checkILL(finishedProductNodeRef1, labelingRuleList, "<b>Decors 1 (100%):</b> Pâte french 50% (Legal Raw material 12 66,7% (ing2 french, ing1 french), ing2 french, ing1 french), Garniture french 50% (ing3 french, ing4 french)", Locale.FRENCH);
	
		
		
		// Group
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "renderGroupList()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Group 1", null, LabelingRuleType.Group, Arrays.asList(localSF11NodeRef),null));
		labelingRuleList.add(new LabelingRuleListDataItem("Group 2", null, LabelingRuleType.Group, Arrays.asList(localSF12NodeRef),null));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, Arrays.asList(ing1 ,ing2, ing3, ing4), null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));

		
//		└──[root - 0.0 (2.0)]
//			    ├──[Pâte french - 1.0 (3.0)]
//			    │   ├──[ing1 french - 0.33333333333333337]
//			    │   ├──[ing2 french - 0.6666666666666667]
//			    │   └──[Legal Raw material 12 - 2.0 (2.0)]
//			    │       ├──[ing1 french - 0.5]
//			    │       └──[ing2 french - 1.5]
//			    └──[Garniture french - 1.0 (6.0)]
//			        ├──[ing3 french - 5.0]
//			        └──[ing4 french - 1.0]


		
		checkILL(finishedProductNodeRef1, labelingRuleList, "<b>Pâte french 50%</b>, <b>Garniture french 50%</b>", Locale.FRENCH);
		
		
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu 1", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Group 1", null, LabelingRuleType.Group, Arrays.asList(localSF11NodeRef),null));
		labelingRuleList.add(new LabelingRuleListDataItem("Group 2", null, LabelingRuleType.Group, Arrays.asList(localSF12NodeRef),null));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, Arrays.asList(ing1 ,ing2, ing3, ing4), null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));

		
//		└──[root - 0.0 (2.0)]
//			    ├──[Pâte french - 1.0 (3.0)]
//			    │   ├──[ing1 french - 0.33333333333333337]
//			    │   ├──[ing2 french - 0.6666666666666667]
//			    │   └──[Legal Raw material 12 - 2.0 (2.0)]
//			    │       ├──[ing1 french - 0.5]
//			    │       └──[ing2 french - 1.5]
//			    └──[Garniture french - 1.0 (6.0)]
//			        ├──[ing3 french - 5.0]
//			        └──[ing4 french - 1.0]


														
		checkILL(finishedProductNodeRef1, labelingRuleList,"<b>Pâte french (50%):</b> Legal Raw material 12 66,7% (ing2 french 75%, ing1 french 25%), ing2 french 22,2%, ing1 french 11,1%<br/><b>Garniture french (50%):</b> ing3 french 83,3%, ing4 french 16,7%", Locale.FRENCH);
		
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu 2", "render(false)", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Group 1", null, LabelingRuleType.Group, Arrays.asList(localSF11NodeRef),null));
		labelingRuleList.add(new LabelingRuleListDataItem("Group 2", null, LabelingRuleType.Group, Arrays.asList(localSF12NodeRef),null));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, Arrays.asList(ing1 ,ing2, ing3, ing4), null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));



//		└──[root - 0.0 (2.0)]
//			    ├──[ing1 french - 0.11111111111111112]
//			    ├──[ing2 french - 0.22222222222222224]
//			    ├──[Legal Raw material 12 - 0.6666666666666666 (2.0)]
//			    │   ├──[ing1 french - 0.5]
//			    │   └──[ing2 french - 1.5]
//			    ├──[ing3 french - 0.8333333333333334]
//			    └──[ing4 french - 0.16666666666666666]

		checkILL(finishedProductNodeRef1, labelingRuleList,"ing3 french 41,7%, Legal Raw material 12 33,3% (ing2 french 75%, ing1 french 25%), ing2 french 11,1%, ing4 french 8,3%, ing1 french 5,6%", Locale.FRENCH);
	
		
		
		

		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu 2", "render(false)", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Aggregate 1", null, LabelingRuleType.DoNotDetails, Arrays.asList(ing1),Arrays.asList(ing3)));
		labelingRuleList.add(new LabelingRuleListDataItem("Group 1", null, LabelingRuleType.Group, Arrays.asList(localSF11NodeRef),null));
		labelingRuleList.add(new LabelingRuleListDataItem("Group 2", null, LabelingRuleType.Group, Arrays.asList(localSF12NodeRef),null));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, Arrays.asList(ing1 ,ing2, ing3, ing4), null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));



//		└──[root - 0.0 (2.0)]
//			    ├──[ing1 french - 0.11111111111111112]
//			    ├──[ing2 french - 0.22222222222222224]
//			    ├──[Legal Raw material 12 - 0.6666666666666666 (2.0)]
//			    │   ├──[ing1 french - 0.5]
//			    │   └──[ing2 french - 1.5]
//			    ├──[ing3 french - 0.8333333333333334]
//			    └──[ing4 french - 0.16666666666666666]

		checkILL(finishedProductNodeRef1, labelingRuleList,"ing3 french 47,2%, Legal Raw material 12 33,3% (ing2 french 75%, ing3 french 25%), ing2 french 11,1%, ing4 french 8,3%", Locale.FRENCH);

	    //Sub Ingredients ing5 (ing1,ing4)
													
		
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));
		
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
				ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef1);
				formulatedProduct.getCompoListView().getCompoList().get(2).setProduct(rawMaterial7NodeRef);
				 alfrescoRepository.save(formulatedProduct);
				 return null;
			}
		}, false, true);
		
		
//		└──[root - 0.0 (2.0)]
//			    ├──[Pâte french - 1.0 (3.0)]
//			    │   ├──[ing1 french - 0.33333333333333337]
//			    │   ├──[ing2 french - 0.6666666666666667]
//			    │   └──[Legal Raw material 7 - 2.0 (2.0)]
//			    │       └──[ing5 french - 2.0]
//			    └──[Garniture french - 1.0 (6.0)]
//			        ├──[ing3 french - 5.0]
//			        └──[ing4 french - 1.0]
		
		//#814
		//checkILL(finishedProductNodeRef1, labelingRuleList, "Pâte french 50% (Legal Raw material 7 66,7% (Epaississant french: ing5 french (ing1 french, ing4 french)), ing2 french, ing1 french), Garniture french 50% (ing3 french, ing4 french)", Locale.FRENCH);
		checkILL(finishedProductNodeRef1, labelingRuleList, "Pâte french 50% (Legal Raw material 7 66,7% (ing5 french 100% (ing1 french, ing4 french)), ing2 french, ing1 french), Garniture french 50% (ing3 french, ing4 french)", Locale.FRENCH);
		
		
		//MultiLevel
		
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render(false)", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, null, null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));
		final NodeRef finishProduct2 = createTestProduct(null);
		
		
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
				productService.formulate(finishProduct2);
				ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef1);
				formulatedProduct.setQty(7d);
				formulatedProduct.getCompoListView().getCompoList().get(2).setProduct(finishProduct2);
				formulatedProduct.getCompoListView().getCompoList().add(new CompoListDataItem(null, null, 5d, null, CompoListUnit.kg, 0d, DeclarationType.Group, finishProduct2));
				 alfrescoRepository.save(formulatedProduct);
				 return null;
			}
		}, false, true);
		
		

//		└──[root - 0.0 (7.0, vol: null) ]
//			    ├──[Pâte french - 1.0 (3.0, vol: null) Detail]
//			    │   ├──[ing1 french - 0.33333333333333337 ( vol : null) ]
//			    │   ├──[ing2 french - 0.6666666666666667 ( vol : null) ]
//			    │   └──[Legal Finished product 1 - 2.0 (2.0, vol: null) Detail]
//			    │       ├──[Pâte french - 1.0 (3.0, vol: null) Detail]
//			    │       │   ├──[ing1 french - 0.33333333333333337 ( vol : null) ]
//			    │       │   ├──[ing2 french - 0.6666666666666667 ( vol : null) ]
//			    │       │   └──[Legal Raw material 12 - 2.0 (2.0, vol: null) Detail]
//			    │       │       ├──[ing1 french - 0.5 ( vol : null) ]
//			    │       │       └──[ing2 french - 1.5 ( vol : null) ]
//			    │       └──[Garniture french - 1.0 (6.0, vol: null) Detail]
//			    │           ├──[ing3 french - 5.0 ( vol : null) ]
//			    │           └──[ing4 french - 1.0 ( vol : null) ]
//			    ├──[Garniture french - 1.0 (6.0, vol: null) Detail]
//			    │   ├──[ing3 french - 5.0 ( vol : null) ]
//			    │   └──[ing4 french - 1.0 ( vol : null) ]
//			    └──[Legal Finished product 1 - 5.0 (2.0, vol: null) Group]
//			        ├──[Pâte french - 1.0 (3.0, vol: null) Detail]
//			        │   ├──[ing1 french - 0.33333333333333337 ( vol : null) ]
//			        │   ├──[ing2 french - 0.6666666666666667 ( vol : null) ]
//			        │   └──[Legal Raw material 12 - 2.0 (2.0, vol: null) Detail]
//			        │       ├──[ing1 french - 0.5 ( vol : null) ]
//			        │       └──[ing2 french - 1.5 ( vol : null) ]
//			        └──[Garniture french - 1.0 (6.0, vol: null) Detail]
//			            ├──[ing3 french - 5.0 ( vol : null) ]
//			            └──[ing4 french - 1.0 ( vol : null) ]


		
		checkILL(finishedProductNodeRef1, labelingRuleList,"Pâte french 50% (Legal Finished product 1 66,7% (Pâte french 50% (Legal Raw material 12 66,7% (ing2 french 75%, ing1 french 25%), ing2 french 22,2%, ing1 french 11,1%), Garniture french 50% (ing3 french 83,3%, ing4 french 16,7%)), ing2 french 22,2%, ing1 french 11,1%), Garniture french 50% (ing3 french 83,3%, ing4 french 16,7%)", Locale.FRENCH);
	
		
		//Declare multilevel
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render(false)", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, null, null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));
		labelingRuleList.add(new LabelingRuleListDataItem("Declare", null, LabelingRuleType.Declare, Arrays.asList(finishProduct2), null));

		
//		└──[root - 0.0 (7.0, vol: null) ]
//			    ├──[Pâte french - 3.5 (6.0, vol: null) Detail]
//			    │   ├──[ing1 french - 0.6666666666666667 ( vol : null) ]
//			    │   ├──[ing2 french - 1.3333333333333335 ( vol : null) ]
//			    │   ├──[Pâte french - 1.0 (3.0, vol: null) Detail]
//			    │   │   ├──[ing1 french - 0.33333333333333337 ( vol : null) ]
//			    │   │   ├──[ing2 french - 0.6666666666666667 ( vol : null) ]
//			    │   │   └──[Legal Raw material 12 - 2.0 (2.0, vol: null) Detail]
//			    │   │       ├──[ing1 french - 0.5 ( vol : null) ]
//			    │   │       └──[ing2 french - 1.5 ( vol : null) ]
//			    │   ├──[Garniture french - 1.0 (6.0, vol: null) Detail]
//			    │   │   ├──[ing3 french - 5.0 ( vol : null) ]
//			    │   │   └──[ing4 french - 1.0 ( vol : null) ]
//			    │   └──[Legal Raw material 12 - 2.0 (2.0, vol: null) Detail]
//			    │       ├──[ing1 french - 0.5 ( vol : null) ]
//			    │       └──[ing2 french - 1.5 ( vol : null) ]
//			    └──[Garniture french - 3.5 (12.0, vol: null) Detail]
//			        ├──[ing3 french - 10.0 ( vol : null) ]
//			        └──[ing4 french - 2.0 ( vol : null) ]

		
		checkILL(finishedProductNodeRef1, labelingRuleList,"Pâte french 50% (Legal Raw material 12 33,3% (ing2 french 75%, ing1 french 25%), ing2 french 22,2%, Pâte french 16,7% (Legal Raw material 12 66,7% (ing2 french 75%, ing1 french 25%), ing2 french 22,2%, ing1 french 11,1%), Garniture french 16,7% (ing3 french 83,3%, ing4 french 16,7%), ing1 french 11,1%), Garniture french 50% (ing3 french 83,3%, ing4 french 16,7%)", Locale.FRENCH);
		
		//	TODO	//Do not Declare ????

	}

	private void checkILL(final NodeRef productNodeRef, final List<LabelingRuleListDataItem> labelingRuleList, final String ill, Locale french) {
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				ProductData formulatedProduct = alfrescoRepository.findOne(productNodeRef);

				formulatedProduct.getLabelingListView().setLabelingRuleList(labelingRuleList);

				productService.formulate(formulatedProduct);

				assertTrue(formulatedProduct.getLabelingListView().getLabelingRuleList().size() > 0);

				// verify IngLabelingList
				assertNotNull("IngLabelingList is null", formulatedProduct.getLabelingListView().getIngLabelingList());
				assertTrue(formulatedProduct.getLabelingListView().getIngLabelingList().size() > 0);

				for (IngLabelingListDataItem illDataItem : formulatedProduct.getLabelingListView().getIngLabelingList()) {
					String formulatedIll = illDataItem.getValue().getValue(Locale.FRENCH);
					assertEquals("Incorrect label :"+formulatedIll+"\n   - compare to "+ill, ill,formulatedIll );

				}
				
				
				return null;

			}
		}, false, true);

	}

	private void checkError(final NodeRef productNodeRef, final List<LabelingRuleListDataItem> labelingRuleList, final String errorMessage) {
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				ProductData formulatedProduct = alfrescoRepository.findOne(productNodeRef);


				formulatedProduct.getLabelingListView().setLabelingRuleList(labelingRuleList);

				productService.formulate(formulatedProduct);
				
				assertFalse(formulatedProduct.getCompoListView().getReqCtrlList().isEmpty());
				
				for (ReqCtrlListDataItem reqCtrlListDataItem : formulatedProduct.getCompoListView().getReqCtrlList()) {
					if(RequirementType.Forbidden.equals(reqCtrlListDataItem.getReqType())){
						String error = reqCtrlListDataItem.getReqMessage();
						assertEquals("Incorrect label :"+error+"\n   - compare to "+errorMessage, error, errorMessage );
					}
				}

				return null;

			}
		}, false, true);

	}
	


	@Test
	public void testMultiLingualLabelingFormulation() throws Exception {

		logger.info("testLabelingFormulation");

		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		//Test locale + format %
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Langue", "fr,en", LabelingRuleType.Locale));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format, Arrays.asList(ing2, ing3, ing4), null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));

		final NodeRef finishedProductNodeRef1 = createTestProduct(labelingRuleList);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

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

					checkILL("Garniture french 50% (ing3 french 83,3%, ing4 french 16,7%)",
							"Pâte french 50% (Legal Raw material 12 66,7% (ing2 french 75%, ing1 french), ing2 french 22,2%, ing1 french)",
							illDataItem.getValue().getValue(Locale.FRENCH));

					checkILL("Garniture english 50% (ing3 english 83,3%, ing4 english 16,7%)",
							"Pâte english 50% (Legal Raw material 12 66,7% (ing2 english 75%, ing1 english), ing2 english 22,2%, ing1 english)",
							illDataItem.getValue().getValue(Locale.ENGLISH));
					
				}

				return null;

			}
		}, false, true);
	 }

	
	

	@Test
	public void testIncTypeThreshold() throws Exception {
		
		

		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();
		//Aggregate
		labelingRuleList = new ArrayList<>();
		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("Aggregate 1", null, LabelingRuleType.DoNotDetails, Arrays.asList(ing1,ing2),Arrays.asList(ing3)));
		labelingRuleList.add(new LabelingRuleListDataItem("Aggregate 2", null, LabelingRuleType.DoNotDetails, Arrays.asList(ing4),Arrays.asList(ing6)));
		labelingRuleList.add(new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param1bis", "ingDefaultFormat = \"{0} {1,number,0.#%}\"", LabelingRuleType.Prefs, null, null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param2", "ingTypeDefaultFormat = \"{0} {1,number,0.#%}: ({2})\"", LabelingRuleType.Prefs, null, null));
		labelingRuleList.add(new LabelingRuleListDataItem("Param3", "ingTypeDecThresholdFormat = \"{0} {1,number,0.#%} [{2}] \"", LabelingRuleType.Prefs, null, null));
		
		
		
		final NodeRef finishedProductNodeRef1 = createTestProduct(labelingRuleList);
		
	//	└──[root - 0.0 (2.0)]
	//		    ├──[Pâte french - 1.0 (3.0)]
	//		    │   ├──[ing3 french - 1.0]
	//		    │   └──[Legal Raw material 12 - 2.0 (2.0)]
	//		    │       └──[ing3 french - 2.0]
	//		    └──[Garniture french - 1.0 (6.0)]
	//		        ├──[ing3 french - 5.0]
	//		        └──[ing5 french - 1.0]
		
		checkILL(finishedProductNodeRef1, labelingRuleList, "Pâte french 50% (Legal Raw material 12 66,7% (ing3 french 100%), ing3 french 33,3%), Garniture french 50% (ing3 french 83,3%, Epices french 16,7% [ing6 french 16,7%])", Locale.FRENCH);

		labelingRuleList.add(new LabelingRuleListDataItem("Param4", "showIngCEECode = true", LabelingRuleType.Prefs, null, null));
		
		checkILL(finishedProductNodeRef1, labelingRuleList, "Pâte french 50% (Legal Raw material 12 66,7% (ing3 french 100%), ing3 french 33,3%), Garniture french 50% (ing3 french 83,3%, Epices french 16,7% [CEE6 16,7%])", Locale.FRENCH);

		
	}
	
	

}
