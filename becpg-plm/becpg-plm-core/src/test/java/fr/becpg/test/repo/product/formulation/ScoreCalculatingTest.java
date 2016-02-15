package fr.becpg.test.repo.product.formulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.LabelingRuleType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class ScoreCalculatingTest extends AbstractFinishedProductTest {

	protected static final Log logger = LogFactory.getLog(ScoreCalculatingTest.class);

	@Resource
	private AssociationService associationService;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
	}


	protected NodeRef createFullProductNodeRef(final String name) {
		return transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*-- Create finished product --*/
				logger.info("/*-- Create finished product --*/");
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName(name);
				finishedProduct.setLegalName("Legal "+name);
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setQty(2d);
				finishedProduct.setUnitPrice(22.4d);
				finishedProduct.setDensity(1d);
				finishedProduct.setServingSize(50d);//50g
				finishedProduct.setProjectedQty(10000l);
				List<CompoListDataItem> compoList = new ArrayList<>();
				compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(0), null, 1d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(0), null, 2d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(3), null, 3d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(3), null, 3d, CompoListUnit.kg, 0d, DeclarationType.Omit, rawMaterial4NodeRef));
				finishedProduct.getCompoListView().setCompoList(compoList);

				List<CostListDataItem> costList = new ArrayList<>();
				costList.add(new CostListDataItem(null, 4000d, "€", null, fixedCost, true));
				costList.add(new CostListDataItem(null, null, null, null, cost1, null));
				costList.add(new CostListDataItem(null, null, null, null, cost2, null));
				finishedProduct.setCostList(costList);

				List<NutListDataItem> nutList = new ArrayList<>();
				nutList.add(new NutListDataItem(null, null, null, null, null, null, nut1, null));
				nutList.add(new NutListDataItem(null, null, null, null, null, null, nut2, null));
				nutList.add(new NutListDataItem(null, null, null, null, null, null, nut3, null));
				finishedProduct.setNutList(nutList);

				List<DynamicCharactListItem> dynamicCharactListItems = new ArrayList<>();
				//Product
				dynamicCharactListItems.add(new DynamicCharactListItem("Product qty 1", "qty"));
				// Literal formula
				dynamicCharactListItems.add(new DynamicCharactListItem("Literal 1", "'Hello World'"));
				dynamicCharactListItems.add(new DynamicCharactListItem("Literal 2", "6.0221415E+23"));
				dynamicCharactListItems.add(new DynamicCharactListItem("Literal 3", "1+1+10-(4/100)"));
				dynamicCharactListItems.add(new DynamicCharactListItem("Literal 4", "0x7dFFFFFF"));
				dynamicCharactListItems.add(new DynamicCharactListItem("Literal 5", "true"));
				dynamicCharactListItems.add(new DynamicCharactListItem("Literal 6", "null"));
				// Properties formulae
				dynamicCharactListItems.add(new DynamicCharactListItem("Property  1", "costList[0].value"));
				dynamicCharactListItems.add(new DynamicCharactListItem("Property  1Bis", "costList[1].value"));
				dynamicCharactListItems.add(new DynamicCharactListItem("Property  2", "costList[0].unit"));
				dynamicCharactListItems.add(new DynamicCharactListItem("Property  3", "costList[0].value / costList[1].value"));
				dynamicCharactListItems.add(new DynamicCharactListItem("Property  4", "profitability"));
				dynamicCharactListItems.add(new DynamicCharactListItem("Collection Selection  1", "costList.?[value == 4.0][0].unit"));
				dynamicCharactListItems.add(new DynamicCharactListItem("Collection Selection  2", "costList.?[value < 5.0][0].value"));
				dynamicCharactListItems.add(new DynamicCharactListItem("Collection Projection  1", "costList.![value]"));
				// Variables
				dynamicCharactListItems.add(new DynamicCharactListItem("Variable  1", "compoListView.dynamicCharactList.?[title == 'Property  1' ][0].value"));
				// Template need Template Context
				// dynamicCharactListItems.add(new
				// DynamicCharactListItem("Template  1","Cost1/Cost2 : #{costList[1].value / costList[2].value}% Profitability : #{profitability}"
				// ));
				// Elvis
				dynamicCharactListItems.add(new DynamicCharactListItem("Elvis  1", "null?:'Unknown'"));
				// Boolean
				dynamicCharactListItems.add(new DynamicCharactListItem("Boolean  1", "costList[1].value > 1"));
				// Assignment
				dynamicCharactListItems.add(new DynamicCharactListItem("Assignement  1", "nutList.?[nut.toString() == '" + nut1 + "' ][0].value = 4d"));
				
				//Spel method
				dynamicCharactListItems.add(new DynamicCharactListItem(" beCPG findOne","@beCPG.findOne(nodeRef).qty"));
				
				dynamicCharactListItems.add(new DynamicCharactListItem(" beCPG propValue","@beCPG.propValue(nodeRef,'bcpg:productQty')"));

				//Formulate twice
				dynamicCharactListItems.add(new DynamicCharactListItem("Formulate twice","reformulateCount=1"));
				
				// DynamicColumn

				DynamicCharactListItem dynCol = new DynamicCharactListItem("Col Dyn 1", "entity.costList[0].value + dataListItem.qty");
				dynCol.setColumnName("bcpg_dynamicCharactColumn1");
				dynamicCharactListItems.add(dynCol);

				dynCol = new DynamicCharactListItem(
						"Col Dyn 2",
						"dataListItem.parent!=null ? entity.costList[0].value + dataListItem.qty : sum(entity.compoListView.compoList.?[parent == #root.dataListItem],\"entity.costList[0].value + dataListItem.qty\" )");

				// "dataListItem.parent!=null ? entity.costList[0].value + dataListItem.qty : sum(children(dataListItem),\"entity.costList[0].value + dataListItem.qty\" )"
				// "dataListItem.parent!=null ? entity.costList[0].value + dataListItem.qty : sum(entity.compoListView.compoList.?[parent == #root.dataListItem],\"entity.costList[0].value + dataListItem.qty\" )"

				dynCol.setColumnName("bcpg_dynamicCharactColumn2");
				dynamicCharactListItems.add(dynCol);
				
			    dynCol = new DynamicCharactListItem("Col Dyn 3", "entity.costList[0].value + dataListItem.qty");
				dynCol.setColumnName("bcpg_dynamicCharactColumn3");
				dynCol.setMultiLevelFormula(true);
				dynamicCharactListItems.add(dynCol);

				finishedProduct.getCompoListView().setDynamicCharactList(dynamicCharactListItems);

				// Claim List

				List<LabelClaimListDataItem> labelClaimListDataItems = new ArrayList<>();

				nodeService.setProperty(labelClaims.get(0), PLMModel.PROP_LABEL_CLAIM_FORMULA, "((nutList.?[nut.toString() == '" + nut1
						+ "'][0].value < 40 and unit != T(fr.becpg.repo.product.data.constraints.ProductUnit).L and unit != T(fr.becpg.repo.product.data.constraints.ProductUnit).mL )"
						+ " or (nutList.?[nut.toString() == '" + nut1
						+ "'][0].value < 20 and (unit == T(fr.becpg.repo.product.data.constraints.ProductUnit).L or unit== T(fr.becpg.repo.product.data.constraints.ProductUnit).mL )))"
						+ " and (nutList.?[nut.toString() == '" + nut1 + "'][0].value > 4 )");
				nodeService.setProperty(labelClaims.get(1), PLMModel.PROP_LABEL_CLAIM_FORMULA, "nutList.?[nut.toString() == '" + nut1 + "'][0].value <= 4");

				labelClaimListDataItems.add(new LabelClaimListDataItem(labelClaims.get(0), "Nutritionnelle", false));
				labelClaimListDataItems.add(new LabelClaimListDataItem(labelClaims.get(1), "Nutritionnelle", false));

				finishedProduct.setLabelClaimList(labelClaimListDataItems);

				List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

				labelingRuleList.add(new LabelingRuleListDataItem("Test", "render()", LabelingRuleType.Render));

				LabelingRuleListDataItem percRule = new LabelingRuleListDataItem("%", "{0} {1,number,0.#%}", LabelingRuleType.Format);
				percRule.setComponents(Arrays.asList(ing2, ing3, ing4));
				labelingRuleList.add(percRule);
				labelingRuleList.add(new LabelingRuleListDataItem("Param1", "detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"", LabelingRuleType.Prefs, null, null));
				

				labelingRuleList.add(new LabelingRuleListDataItem("Langue", "fr,en", LabelingRuleType.Locale));
				
				finishedProduct.getLabelingListView().setLabelingRuleList(labelingRuleList);

				return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

			}
		}, false, true);
	}
}
