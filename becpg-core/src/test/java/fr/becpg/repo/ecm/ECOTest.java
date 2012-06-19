package fr.becpg.repo.ecm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ECMModel;
import fr.becpg.repo.BeCPGDao;
import fr.becpg.repo.ecm.data.ChangeOrderData;
import fr.becpg.repo.ecm.data.ChangeOrderType;
import fr.becpg.repo.ecm.data.RevisionType;
import fr.becpg.repo.ecm.data.dataList.ChangeUnitDataItem;
import fr.becpg.repo.ecm.data.dataList.ReplacementListDataItem;
import fr.becpg.repo.ecm.data.dataList.SimulationListDataItem;
import fr.becpg.repo.ecm.data.dataList.WUsedListDataItem;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProduct;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.test.RepoBaseTestCase;

/**
 * ECO test class
 * 
 * @author quere
 * 
 */
public class ECOTest extends RepoBaseTestCase {

	/** The logger. */
	private static Log logger = LogFactory.getLog(ECOTest.class);

	/** The product service. */
	private ProductService productService;

	private BeCPGDao<ChangeOrderData> changeOrderDAO;

	private ECOService ecoService;

	/** The PAT h_ productfolder. */
	private static String PATH_PRODUCTFOLDER = "TestProductFolder";

	public static final String Double_FORMAT = "0.0000";

	/** The folder node ref. */
	private NodeRef folderNodeRef;

	/** The local s f1 node ref. */
	private NodeRef localSF1NodeRef;

	/** The raw material1 node ref. */
	private NodeRef rawMaterial1NodeRef;

	/** The raw material2 node ref. */
	private NodeRef rawMaterial2NodeRef;

	/** The local s f2 node ref. */
	private NodeRef localSF2NodeRef;

	/** The raw material3 node ref. */
	private NodeRef rawMaterial3NodeRef;

	/** The raw material4 node ref. */
	private NodeRef rawMaterial4NodeRef;

	/** The raw material5 node ref. */
	private NodeRef rawMaterial5NodeRef;

	/** The cost1. */
	private NodeRef cost1;

	/** The cost2. */
	private NodeRef cost2;

	/** The nut1. */
	private NodeRef nut1;

	/** The nut2. */
	private NodeRef nut2;

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.becpg.test.RepoBaseTestCase#setUp()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		logger.debug("ProductMgrTest:setUp");

		productService = (ProductService) ctx.getBean("productService");

		changeOrderDAO = (BeCPGDao<ChangeOrderData>) ctx.getBean("changeOrderDAO");
		ecoService = (ECOService) ctx.getBean("ecoService");

		cost1 = costs.get(0);
		cost2 = costs.get(1);

		nut1 = nuts.get(0);
		nut2 = nuts.get(1);
	
		// create RM and lSF
		initParts();
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.becpg.test.RepoBaseTestCase#tearDown()
	 */
	@Override
	public void tearDown() throws Exception {
		super.tearDown();

	}

	/**
	 * Inits the parts.
	 */
	private void initParts() {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*-- Create test folder --*/
				folderNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_PRODUCTFOLDER);
				if (folderNodeRef != null) {
					nodeService.deleteNode(folderNodeRef);
				}
				folderNodeRef = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_PRODUCTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();

				/*-- Create raw materials --*/
				logger.debug("/*-- Create raw materials --*/");
				Collection<QName> dataLists = productDictionaryService.getDataLists();
				/*-- Raw material 1 --*/
				RawMaterialData rawMaterial1 = new RawMaterialData();
				rawMaterial1.setName("Raw material 1");
				rawMaterial1.setLegalName("Legal Raw material 1");
				rawMaterial1.setHierarchy1(HIERARCHY1_SEA_FOOD_REF);
				rawMaterial1.setHierarchy2(HIERARCHY2_CRUSTACEAN_REF);
				// costList
				List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
				costList.add(new CostListDataItem(null, 3d, "€/kg", null, cost1, false));
				costList.add(new CostListDataItem(null, 2d, "€/kg", null, cost2, false));
				rawMaterial1.setCostList(costList);
				// nutList
				List<NutListDataItem> nutList = new ArrayList<NutListDataItem>();
				nutList.add(new NutListDataItem(null, 1d, "g/100g", 0d, 0d, "Groupe 1", nut1, false));
				nutList.add(new NutListDataItem(null, 2d, "g/100g", 0d, 0d, "Groupe 1", nut2, false));
				rawMaterial1.setNutList(nutList);
				rawMaterial1NodeRef = productDAO.create(folderNodeRef, rawMaterial1, dataLists);

				/*-- Raw material 2 --*/
				RawMaterialData rawMaterial2 = new RawMaterialData();
				rawMaterial2.setName("Raw material 2");
				rawMaterial2.setLegalName("Legal Raw material 2");
				rawMaterial2.setHierarchy1(HIERARCHY1_SEA_FOOD_REF);
				rawMaterial2.setHierarchy2(HIERARCHY2_CRUSTACEAN_REF);
				// costList
				costList = new ArrayList<CostListDataItem>();
				costList.add(new CostListDataItem(null, 1d, "€/kg", null, cost1, false));
				costList.add(new CostListDataItem(null, 2d, "€/kg", null, cost2, false));
				rawMaterial2.setCostList(costList);
				// nutList
				nutList = new ArrayList<NutListDataItem>();
				nutList.add(new NutListDataItem(null, 1d, "g/100g", 0d, 0d, "Groupe 1", nut1, false));
				nutList.add(new NutListDataItem(null, 2d, "g/100g", 0d, 0d, "Groupe 1", nut2, false));
				rawMaterial2.setNutList(nutList);
				rawMaterial2NodeRef = productDAO.create(folderNodeRef, rawMaterial2, dataLists);

				/*-- Raw material 3 --*/
				RawMaterialData rawMaterial3 = new RawMaterialData();
				rawMaterial3.setName("Raw material 3");
				rawMaterial3.setLegalName("Legal Raw material 3");
				rawMaterial3.setHierarchy1(HIERARCHY1_SEA_FOOD_REF);
				rawMaterial3.setHierarchy2(HIERARCHY2_CRUSTACEAN_REF);
				// costList
				costList = new ArrayList<CostListDataItem>();
				costList.add(new CostListDataItem(null, 1d, "€/kg", null, cost1, false));
				costList.add(new CostListDataItem(null, 2d, "€/kg", null, cost2, false));
				rawMaterial3.setCostList(costList);
				// nutList
				nutList = new ArrayList<NutListDataItem>();
				nutList.add(new NutListDataItem(null, 1d, "g/100g", 0d, 0d, "Groupe 1", nut1, false));
				nutList.add(new NutListDataItem(null, 2d, "g/100g", 0d, 0d, "Groupe 1", nut2, false));
				rawMaterial3.setNutList(nutList);
				rawMaterial3NodeRef = productDAO.create(folderNodeRef, rawMaterial3, dataLists);

				/*-- Raw material 4 --*/
				RawMaterialData rawMaterial4 = new RawMaterialData();
				rawMaterial4.setName("Raw material 4");
				rawMaterial4.setLegalName("Legal Raw material 4");
				rawMaterial4.setHierarchy1(HIERARCHY1_SEA_FOOD_REF);
				rawMaterial4.setHierarchy2(HIERARCHY2_CRUSTACEAN_REF);
				rawMaterial4NodeRef = productDAO.create(folderNodeRef, rawMaterial4, dataLists);

				/*-- Raw material 5 --*/
				RawMaterialData rawMaterial5 = new RawMaterialData();
				rawMaterial5.setName("Raw material 5");
				rawMaterial5.setLegalName("Legal Raw material 5");
				rawMaterial5.setHierarchy1(HIERARCHY1_SEA_FOOD_REF);
				rawMaterial5.setHierarchy2(HIERARCHY2_CRUSTACEAN_REF);
				// costList
				costList = new ArrayList<CostListDataItem>();
				costList.add(new CostListDataItem(null, 5d, "€/m", null, cost1, false));
				costList.add(new CostListDataItem(null, 6d, "€/m", null, cost2, false));
				rawMaterial5.setCostList(costList);
				// nutList
				nutList = new ArrayList<NutListDataItem>();
				nutList.add(new NutListDataItem(null, 1d, "g/100g", 0d, 0d, "Groupe 1", nut1, false));
				nutList.add(new NutListDataItem(null, 3d, "g/100g", 0d, 0d, "Groupe 1", nut2, false));
				rawMaterial5.setNutList(nutList);
				rawMaterial5NodeRef = productDAO.create(folderNodeRef, rawMaterial5, dataLists);

				/*-- Local semi finished product 1 --*/
				LocalSemiFinishedProduct localSF1 = new LocalSemiFinishedProduct();
				localSF1.setName("Local semi finished 1");
				localSF1.setLegalName("Legal Local semi finished 1");
				localSF1.setHierarchy1(HIERARCHY1_SEA_FOOD_REF);
				localSF1.setHierarchy2(HIERARCHY2_CRUSTACEAN_REF);
				localSF1NodeRef = productDAO.create(folderNodeRef, localSF1, dataLists);

				/*-- Local semi finished product 2 --*/
				LocalSemiFinishedProduct localSF2 = new LocalSemiFinishedProduct();
				localSF2.setName("Local semi finished 2");
				localSF2.setLegalName("Legal Local semi finished 2");
				localSF2.setHierarchy1(HIERARCHY1_SEA_FOOD_REF);
				localSF2.setHierarchy2(HIERARCHY2_CRUSTACEAN_REF);
				localSF2NodeRef = productDAO.create(folderNodeRef, localSF2, dataLists);

				return null;

			}
		}, false, true);
	}

	/**
	 * Create a finished product
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public NodeRef createFinishedProduct(final String finishedProductName) throws Exception {

		return transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*-- Create finished product --*/
				logger.debug("/*-- Create finished product --*/");
				Collection<QName> dataLists = productDictionaryService.getDataLists();
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName(finishedProductName);
				finishedProduct.setLegalName("Legal name");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setHierarchy1(HIERARCHY1_FROZEN_REF);
				finishedProduct.setHierarchy2(HIERARCHY2_PIZZA_REF);
				finishedProduct.setQty(2d);
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 1d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 2d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, 1, 1d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
				compoList.add(new CompoListDataItem(null, 2, 3d, 0d, 0d, CompoListUnit.kg, 0d, DeclarationType.Omit, rawMaterial4NodeRef));
				finishedProduct.setCompoList(compoList);
				NodeRef finishedProductNodeRef = productDAO.create(folderNodeRef, finishedProduct, dataLists);

				logger.debug("unit of product to formulate: " + finishedProduct.getUnit());

				/*-- Formulate product --*/
				logger.debug("/*-- Formulate product --*/");
				productService.formulate(finishedProductNodeRef);

				/*-- Verify formulation --*/
				logger.debug("/*-- Verify formulation --*/");

				ProductData formulatedProduct = productDAO.find(finishedProductNodeRef, dataLists);

				logger.debug("unit of product formulated: " + finishedProduct.getUnit());
				logger.debug("Finish product: " + formulatedProduct.toString());
				// costs
				assertNotNull("CostList is null", formulatedProduct.getCostList());
				for (CostListDataItem costListDataItem : formulatedProduct.getCostList()) {
					String trace = "cost: " + nodeService.getProperty(costListDataItem.getCost(), ContentModel.PROP_NAME) + " - value: " + costListDataItem.getValue()
							+ " - unit: " + costListDataItem.getUnit();
					logger.debug(trace);
					if (costListDataItem.getCost().equals(cost1)) {
						assertEquals("cost1.getValue() == 4.0, actual values: " + trace, 4.0d, costListDataItem.getValue());
						assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
					}
					if (costListDataItem.getCost().equals(cost2)) {
						assertEquals("cost1.getValue() == 6.0, actual values: " + trace, 6.0d, costListDataItem.getValue());
						assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
					}
				}
				// nuts
				assertNotNull("NutList is null", formulatedProduct.getNutList());
				for (NutListDataItem nutListDataItem : formulatedProduct.getNutList()) {
					String trace = "nut: " + nodeService.getProperty(nutListDataItem.getNut(), ContentModel.PROP_NAME) + " - value: " + nutListDataItem.getValue() + " - unit: "
							+ nutListDataItem.getUnit();
					logger.debug(trace);
					if (nutListDataItem.getNut().equals(nut1)) {
						assertEquals("nut1.getValue() == 3, actual values: " + trace, 3d, nutListDataItem.getValue());
					}
					if (nutListDataItem.getNut().equals(nut2)) {
						assertEquals("nut2.getValue() == 6, actual values: " + trace, 6d, nutListDataItem.getValue());
					}
				}

				return finishedProductNodeRef;

			}
		}, false, true);

	}

	/**
	 * Test ecoService
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void xtestECOService() throws Exception {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				NodeRef finishedProduct1NodeRef = createFinishedProduct("PF1");
				NodeRef finishedProduct2NodeRef = createFinishedProduct("PF2");

				/*
				 * Create a change order to replace RM4 by RM5
				 */

				logger.debug("Create Change order to replace RM4: " + rawMaterial4NodeRef + " by RM5: " + rawMaterial5NodeRef);

				List<NodeRef> calculatedCharacts = new ArrayList<NodeRef>();
				calculatedCharacts.add(cost1);
				calculatedCharacts.add(cost2);
				calculatedCharacts.add(nut1);
				calculatedCharacts.add(nut2);
				ChangeOrderData changeOrderData = new ChangeOrderData(null, "ECO", null, ECOState.ToValidate, ChangeOrderType.Simulation, calculatedCharacts);

				List<ReplacementListDataItem> replacementList = new ArrayList<ReplacementListDataItem>();
				replacementList.add(new ReplacementListDataItem(null, RevisionType.Minor, rawMaterial4NodeRef, rawMaterial5NodeRef));
				changeOrderData.setReplacementList(replacementList);

				NodeRef ecoNodeRef = changeOrderDAO.create(folderNodeRef, changeOrderData);

				// calculate WUsed
				ecoService.calculateWUsedList(ecoNodeRef);

				// verify WUsed
				int checks = 0;
				ChangeOrderData dbECOData = changeOrderDAO.find(ecoNodeRef);
				assertNotNull("check ECO exist in DB", dbECOData);
				assertNotNull("Check WUsed list", dbECOData.getWUsedList());
				assertEquals("Check 2 WUsed are impacted", 3, dbECOData.getWUsedList().size());

				for (WUsedListDataItem wul : dbECOData.getWUsedList()) {

					assertNotNull(wul.getSourceItem());
					ChangeUnitDataItem changeUnitData = dbECOData.getChangeUnitMap().get(wul.getSourceItem());
					assertNotNull(changeUnitData);

					if (changeUnitData.getSourceItem().equals(rawMaterial4NodeRef)) {

						checks++;
						assertEquals(RevisionType.Minor, changeUnitData.getRevision());
					} else if (changeUnitData.getSourceItem().equals(finishedProduct1NodeRef)) {

						checks++;
						assertEquals(RevisionType.Minor, changeUnitData.getRevision());
					} else if (changeUnitData.getSourceItem().equals(finishedProduct2NodeRef)) {

						checks++;
						assertEquals(RevisionType.Minor, changeUnitData.getRevision());
					}
				}
				assertEquals(3, checks);

				// simulation
				ecoService.doSimulation(ecoNodeRef);

				// verify Simulation
				checks = 0;
				dbECOData = changeOrderDAO.find(ecoNodeRef);
				assertNotNull("check ECO exist in DB", dbECOData);
				assertNotNull("Check Simulation list", dbECOData.getSimulationList());
				assertEquals("Check SchangeUnitDataimulation list", 8, dbECOData.getSimulationList().size());

				for (SimulationListDataItem sim : dbECOData.getSimulationList()) {

					if (sim.getSourceItem().equals(finishedProduct1NodeRef)) {

						if (sim.getCharact().equals(cost1)) {

							checks++;
							assertEquals("check cost1 PF1", 4.0d, sim.getSourceValue());
							assertEquals("check cost1 PF1", 11.5d, sim.getTargetValue());
						} else if (sim.getCharact().equals(cost2)) {

							checks++;
							assertEquals("check cost2 PF1", 6.0d, sim.getSourceValue());
							assertEquals("check cost2 PF1", 15d, sim.getTargetValue());
						} else if (sim.getCharact().equals(nut1)) {

							checks++;
							assertEquals("check nut1 PF1", 3.0d, sim.getSourceValue());
							assertEquals("check nut1 PF1", 4.5d, sim.getTargetValue());
						} else if (sim.getCharact().equals(nut2)) {

							checks++;
							assertEquals("check nut2 PF1", 6.0d, sim.getSourceValue());
							assertEquals("check nut2 PF1", 10.5d, sim.getTargetValue());
						}
					} else if (sim.getSourceItem().equals(finishedProduct2NodeRef)) {

						if (sim.getCharact().equals(cost1)) {

							checks++;
							assertEquals("check cost1 PF2", 4.0d, sim.getSourceValue());
							assertEquals("check cost1 PF2", 11.5d, sim.getTargetValue());
						} else if (sim.getCharact().equals(cost2)) {

							checks++;
							assertEquals("check cost2 PF2", 6.0d, sim.getSourceValue());
							assertEquals("check cost2 PF2", 15d, sim.getTargetValue());
						} else if (sim.getCharact().equals(nut1)) {

							checks++;
							assertEquals("check nut1 PF2", 3.0d, sim.getSourceValue());
							assertEquals("check nut1 PF2", 4.5d, sim.getTargetValue());
						} else if (sim.getCharact().equals(nut2)) {

							checks++;
							assertEquals("check nut2 PF2", 6.0d, sim.getSourceValue());
							assertEquals("check nut2 PF2", 10.5d, sim.getTargetValue());
						}
					}
				}
				assertEquals(8, checks);

				// apply
				// ecoService.apply(ecoNodeRef);

				return null;

			}
		}, false, true);

	}

	/**
	 * Test ecoService
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void xtestECOPolicy() throws Exception {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				NodeRef finishedProduct1NodeRef = createFinishedProduct("PF1");
				NodeRef finishedProduct2NodeRef = createFinishedProduct("PF2");

				/*
				 * Create a change order to replace RM4 by RM5
				 */

				logger.debug("Create Change order to replace RM4: " + rawMaterial4NodeRef + " by RM5: " + rawMaterial5NodeRef);

				List<NodeRef> calculatedCharacts = new ArrayList<NodeRef>();
				calculatedCharacts.add(cost1);
				calculatedCharacts.add(cost2);
				calculatedCharacts.add(nut1);
				calculatedCharacts.add(nut2);
				ChangeOrderData changeOrderData = new ChangeOrderData(null, "ECO", null, ECOState.ToValidate, ChangeOrderType.Simulation, calculatedCharacts);

				List<ReplacementListDataItem> replacementList = new ArrayList<ReplacementListDataItem>();
				replacementList.add(new ReplacementListDataItem(null, RevisionType.Minor, rawMaterial4NodeRef, rawMaterial5NodeRef));
				changeOrderData.setReplacementList(replacementList);

				NodeRef ecoNodeRef = changeOrderDAO.create(folderNodeRef, changeOrderData);

				// calculate WUsed
				nodeService.setProperty(ecoNodeRef, ECMModel.PROP_ECO_STATE, ECOState.ToCalculateWUsed);

				// verify WUsed
				int checks = 0;
				ChangeOrderData dbECOData = changeOrderDAO.find(ecoNodeRef);
				assertNotNull("check ECO exist in DB", dbECOData);
				assertNotNull("Check WUsed list", dbECOData.getWUsedList());
				assertEquals("Check impacted WUsed", 3, dbECOData.getWUsedList().size());

				for (WUsedListDataItem wul : dbECOData.getWUsedList()) {

					assertNotNull(wul.getSourceItem());
					ChangeUnitDataItem changeUnitData = dbECOData.getChangeUnitMap().get(wul.getSourceItem());
					assertNotNull(changeUnitData);

					if (changeUnitData.getSourceItem().equals(rawMaterial4NodeRef)) {

						checks++;
						assertEquals(RevisionType.Minor, changeUnitData.getRevision());
					} else if (changeUnitData.getSourceItem().equals(finishedProduct1NodeRef)) {

						checks++;
						assertEquals(RevisionType.Minor, changeUnitData.getRevision());
					} else if (changeUnitData.getSourceItem().equals(finishedProduct2NodeRef)) {

						checks++;
						assertEquals(RevisionType.Minor, changeUnitData.getRevision());
					}
				}
				assertEquals(3, checks);

				// simulation
				nodeService.setProperty(ecoNodeRef, ECMModel.PROP_ECO_STATE, ECOState.ToSimulate);

				// verify Simulation
				checks = 0;
				dbECOData = changeOrderDAO.find(ecoNodeRef);
				assertNotNull("check ECO exist in DB", dbECOData);
				assertNotNull("Check Simulation list", dbECOData.getSimulationList());
				assertEquals("Check SchangeUnitDataimulation list", 8, dbECOData.getSimulationList().size());

				for (SimulationListDataItem sim : dbECOData.getSimulationList()) {

					if (sim.getSourceItem().equals(finishedProduct1NodeRef)) {

						if (sim.getCharact().equals(cost1)) {

							checks++;
							assertEquals("check cost1 PF1", 4.0d, sim.getSourceValue());
							assertEquals("check cost1 PF1", 11.5d, sim.getTargetValue());
						} else if (sim.getCharact().equals(cost2)) {

							checks++;
							assertEquals("check cost2 PF1", 6.0d, sim.getSourceValue());
							assertEquals("check cost2 PF1", 15d, sim.getTargetValue());
						} else if (sim.getCharact().equals(nut1)) {

							checks++;
							assertEquals("check nut1 PF1", 3.0d, sim.getSourceValue());
							assertEquals("check nut1 PF1", 4.5d, sim.getTargetValue());
						} else if (sim.getCharact().equals(nut2)) {

							checks++;
							assertEquals("check nut2 PF1", 6.0d, sim.getSourceValue());
							assertEquals("check nut2 PF1", 10.5d, sim.getTargetValue());
						}
					} else if (sim.getSourceItem().equals(finishedProduct2NodeRef)) {

						if (sim.getCharact().equals(cost1)) {

							checks++;
							assertEquals("check cost1 PF2", 4.0d, sim.getSourceValue());
							assertEquals("check cost1 PF2", 11.5d, sim.getTargetValue());
						} else if (sim.getCharact().equals(cost2)) {

							checks++;
							assertEquals("check cost2 PF2", 6.0d, sim.getSourceValue());
							assertEquals("check cost2 PF2", 15d, sim.getTargetValue());
						} else if (sim.getCharact().equals(nut1)) {

							checks++;
							assertEquals("check nut1 PF2", 3.0d, sim.getSourceValue());
							assertEquals("check nut1 PF2", 4.5d, sim.getTargetValue());
						} else if (sim.getCharact().equals(nut2)) {

							checks++;
							assertEquals("check nut2 PF2", 6.0d, sim.getSourceValue());
							assertEquals("check nut2 PF2", 10.5d, sim.getTargetValue());
						}
					}
				}
				assertEquals(8, checks);

				// apply
				// nodeService.setProperty(ecoNodeRef, ECOModel.PROP_ECO_STATE,
				// ECOState.ToApply);

				return null;

			}
		}, false, true);

	}

	/**
	 * Test ecoService in multi level compo
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void testECOInMultiLeveCompo() throws Exception {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				NodeRef finishedProduct1NodeRef = createFinishedProduct("PF1");
				NodeRef finishedProduct2NodeRef = createFinishedProduct("PF2");

				/*
				 * Create multi level compo
				 */

				FinishedProductData finishedProduct3 = new FinishedProductData();
				finishedProduct3.setName("PF3");
				finishedProduct3.setLegalName("Legal name");
				finishedProduct3.setUnit(ProductUnit.kg);
				finishedProduct3.setQty(2d);
				finishedProduct3.setHierarchy1(HIERARCHY1_SEA_FOOD_REF);
				finishedProduct3.setHierarchy2(HIERARCHY2_CRUSTACEAN_REF);
				List<CompoListDataItem> compoList = new ArrayList<CompoListDataItem>();
				compoList.add(new CompoListDataItem(null, 1, 1d, 1d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Declare, finishedProduct1NodeRef));
				compoList.add(new CompoListDataItem(null, 1, 2d, 2d, 0d, CompoListUnit.kg, 0d, null, DeclarationType.Declare, finishedProduct2NodeRef));
				finishedProduct3.setCompoList(compoList);
				Collection<QName> dataLists = new ArrayList<QName>();
				dataLists.add(BeCPGModel.TYPE_COMPOLIST);
				NodeRef finishedProduct3NodeRef = productDAO.create(folderNodeRef, finishedProduct3, dataLists);

				/*-- Formulate product --*/
				try {
					logger.debug("/*-- Formulate product PF3 --*/");
					productService.formulate(finishedProduct3NodeRef);
				} catch (Exception e) {
					logger.error("!error when formulating.", e);
				}

				/*-- Verify formulation --*/
				logger.debug("/*-- Verify formulation --*/");
				ProductData formulatedProduct3 = productDAO.find(finishedProduct3NodeRef, productDictionaryService.getDataLists());

				logger.debug("unit of product formulated: " + formulatedProduct3.getUnit());

				// costs
				assertNotNull("CostList is null", formulatedProduct3.getCostList());
				for (CostListDataItem costListDataItem : formulatedProduct3.getCostList()) {
					String trace = "cost: " + nodeService.getProperty(costListDataItem.getCost(), ContentModel.PROP_NAME) + " - value: " + costListDataItem.getValue()
							+ " - unit: " + costListDataItem.getUnit();
					logger.debug(trace);
					if (costListDataItem.getCost().equals(cost1)) {
						assertEquals("cost1.getValue() == 6.0, actual values: " + trace, 6.0d, costListDataItem.getValue());
						assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
					}
					if (costListDataItem.getCost().equals(cost2)) {
						assertEquals("cost1.getValue() == 9.0, actual values: " + trace, 9.0d, costListDataItem.getValue());
						assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
					}
				}
				// nuts
				assertNotNull("NutList is null", formulatedProduct3.getNutList());
				for (NutListDataItem nutListDataItem : formulatedProduct3.getNutList()) {
					String trace = "nut: " + nodeService.getProperty(nutListDataItem.getNut(), ContentModel.PROP_NAME) + " - value: " + nutListDataItem.getValue() + " - unit: "
							+ nutListDataItem.getUnit();
					logger.debug(trace);
					if (nutListDataItem.getNut().equals(nut1)) {
						assertEquals("nut1.getValue() == 4.5, actual values: " + trace, 4.5d, nutListDataItem.getValue());
					}
					if (nutListDataItem.getNut().equals(nut2)) {
						assertEquals("nut2.getValue() == 9, actual values: " + trace, 9d, nutListDataItem.getValue());
					}
				}

				/*
				 * Create a change order to replace RM4 by RM5
				 */

				logger.debug("Create Change order to replace RM4: " + rawMaterial4NodeRef + " by RM5: " + rawMaterial5NodeRef);

				List<NodeRef> calculatedCharacts = new ArrayList<NodeRef>();
				calculatedCharacts.add(cost1);
				calculatedCharacts.add(cost2);
				calculatedCharacts.add(nut1);
				calculatedCharacts.add(nut2);
				ChangeOrderData changeOrderData = new ChangeOrderData(null, "ECO", null, ECOState.ToValidate, ChangeOrderType.Simulation, calculatedCharacts);

				List<ReplacementListDataItem> replacementList = new ArrayList<ReplacementListDataItem>();
				replacementList.add(new ReplacementListDataItem(null, RevisionType.Minor, rawMaterial4NodeRef, rawMaterial5NodeRef));
				changeOrderData.setReplacementList(replacementList);

				NodeRef ecoNodeRef = changeOrderDAO.create(folderNodeRef, changeOrderData);

				// calculate WUsed
				ecoService.calculateWUsedList(ecoNodeRef);

				// verify WUsed
				int checks = 0;
				ChangeOrderData dbECOData = changeOrderDAO.find(ecoNodeRef);
				assertNotNull("check ECO exist in DB", dbECOData);
				assertNotNull("Check WUsed list", dbECOData.getWUsedList());
				assertEquals("Check WUsed impacted", 5, dbECOData.getWUsedList().size());

				for (WUsedListDataItem wul : dbECOData.getWUsedList()) {

					assertNotNull(wul.getSourceItem());
					ChangeUnitDataItem changeUnitData = dbECOData.getChangeUnitMap().get(wul.getSourceItem());
					assertNotNull(changeUnitData);

					if (changeUnitData.getSourceItem().equals(rawMaterial4NodeRef)) {

						checks++;
						assertEquals(RevisionType.Minor, changeUnitData.getRevision());
					} else if (changeUnitData.getSourceItem().equals(finishedProduct1NodeRef)) {

						checks++;
						assertEquals(RevisionType.Minor, changeUnitData.getRevision());
					} else if (changeUnitData.getSourceItem().equals(finishedProduct2NodeRef)) {

						checks++;
						assertEquals(RevisionType.Minor, changeUnitData.getRevision());
					} else if (changeUnitData.getSourceItem().equals(finishedProduct3NodeRef)) {

						checks++;
						assertEquals(RevisionType.Minor, changeUnitData.getRevision());
					}
				}
				assertEquals(5, checks);

				// simulation
				ecoService.doSimulation(ecoNodeRef);

				// verify Simulation
				checks = 0;
				dbECOData = changeOrderDAO.find(ecoNodeRef);
				assertNotNull("check ECO exist in DB", dbECOData);
				assertNotNull("Check Simulation list", dbECOData.getSimulationList());
				assertEquals("Check changeUnitDataSimulation list", 12, dbECOData.getSimulationList().size());

				for (SimulationListDataItem sim : dbECOData.getSimulationList()) {

					if (sim.getSourceItem().equals(finishedProduct1NodeRef)) {

						if (sim.getCharact().equals(cost1)) {

							checks++;
							assertEquals("check cost1 PF1", 4.0d, sim.getSourceValue());
							assertEquals("check cost1 PF1", 11.5d, sim.getTargetValue());
						} else if (sim.getCharact().equals(cost2)) {

							checks++;
							assertEquals("check cost2 PF1", 6.0d, sim.getSourceValue());
							assertEquals("check cost2 PF1", 15d, sim.getTargetValue());
						} else if (sim.getCharact().equals(nut1)) {

							checks++;
							assertEquals("check nut1 PF1", 3.0d, sim.getSourceValue());
							assertEquals("check nut1 PF1", 4.5d, sim.getTargetValue());
						} else if (sim.getCharact().equals(nut2)) {

							checks++;
							assertEquals("check nut2 PF1", 6.0d, sim.getSourceValue());
							assertEquals("check nut2 PF1", 10.5d, sim.getTargetValue());
						}
					} else if (sim.getSourceItem().equals(finishedProduct2NodeRef)) {

						if (sim.getCharact().equals(cost1)) {

							checks++;
							assertEquals("check cost1 PF2", 4.0d, sim.getSourceValue());
							assertEquals("check cost1 PF2", 11.5d, sim.getTargetValue());
						} else if (sim.getCharact().equals(cost2)) {

							checks++;
							assertEquals("check cost2 PF2", 6.0d, sim.getSourceValue());
							assertEquals("check cost2 PF2", 15d, sim.getTargetValue());
						} else if (sim.getCharact().equals(nut1)) {

							checks++;
							assertEquals("check nut1 PF2", 3.0d, sim.getSourceValue());
							assertEquals("check nut1 PF2", 4.5d, sim.getTargetValue());
						} else if (sim.getCharact().equals(nut2)) {

							checks++;
							assertEquals("check nut2 PF2", 6.0d, sim.getSourceValue());
							assertEquals("check nut2 PF2", 10.5d, sim.getTargetValue());
						}
					} else if (sim.getSourceItem().equals(finishedProduct3NodeRef)) {

						logger.debug("PF3 caract: " + nodeService.getProperty(sim.getCharact(), ContentModel.PROP_NAME) + "sourceValue: " + sim.getSourceValue() + "targetValue: "
								+ sim.getTargetValue());

						if (sim.getCharact().equals(cost1)) {

							checks++;
							assertEquals("check cost1 PF3", 6.0d, sim.getSourceValue());
							assertEquals("check cost1 PF3", 17.25d, sim.getTargetValue());
						} else if (sim.getCharact().equals(cost2)) {

							checks++;
							assertEquals("check cost2 PF3", 9.0d, sim.getSourceValue());
							assertEquals("check cost2 PF3", 22.5d, sim.getTargetValue());
						} else if (sim.getCharact().equals(nut1)) {

							checks++;
							assertEquals("check nut1 PF3", 4.5d, sim.getSourceValue());
							assertEquals("check nut1 PF3", 6.75d, sim.getTargetValue());
						} else if (sim.getCharact().equals(nut2)) {

							checks++;
							assertEquals("check nut2 PF3", 9.0d, sim.getSourceValue());
							assertEquals("check nut2 PF3", 15.75d, sim.getTargetValue());
						}
					}
				}
				assertEquals(12, checks);

				// apply
				// ecoService.apply(ecoNodeRef);

				return null;

			}
		}, false, true);

	}

}
