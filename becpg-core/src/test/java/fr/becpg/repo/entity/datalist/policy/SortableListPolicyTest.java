/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.entity.datalist.policy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.test.RepoBaseTestCase;

/**
 * The Class SortableListPolicyTest.
 * 
 * @author querephi
 */
public class SortableListPolicyTest extends RepoBaseTestCase {

	/** The logger. */
	private static Log logger = LogFactory.getLog(SortableListPolicyTest.class);

	@Resource
	private EntityListDAO entityListDAO;

	/** The sf node ref. */
	private NodeRef sfNodeRef;

	/**
	 * Create a list item and check initialization
	 * 
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	@Test
	public void testInitSort() throws InterruptedException {

		logger.debug("testChangeSortListItem()");


		// create product
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				// create SF
				SemiFinishedProductData sfData = new SemiFinishedProductData();
				sfData.setName("SF");
				List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
				costList.add(new CostListDataItem(null, 3d, "€/kg", null, costs.get(0), false));
				costList.add(new CostListDataItem(null, 2d, "€/kg", null, costs.get(1), false));
				costList.add(new CostListDataItem(null, 3d, "€/kg", null, costs.get(2), false));
				costList.add(new CostListDataItem(null, 2d, "€/kg", null, costs.get(3), false));
				sfData.setCostList(costList);

				sfNodeRef = alfrescoRepository.create(testFolderNodeRef, sfData).getNodeRef();

				// simulate the UI
				NodeRef listContainerNodeRef = entityListDAO.getListContainer(sfNodeRef);
				NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_COSTLIST);

				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				ChildAssociationRef childAssocRef = nodeService.createNode(listNodeRef, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()),
						BeCPGModel.TYPE_COSTLIST, properties);
				nodeService.createAssociation(childAssocRef.getChildRef(), costs.get(3), BeCPGModel.ASSOC_COSTLIST_COST);

				childAssocRef = nodeService.createNode(listNodeRef, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()),
						BeCPGModel.TYPE_COSTLIST, properties);
				nodeService.createAssociation(childAssocRef.getChildRef(), costs.get(3), BeCPGModel.ASSOC_COSTLIST_COST);

				return null;

			}
		}, false, true);

		// create product
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				// load SF and test it
				SemiFinishedProductData sfData = (SemiFinishedProductData) alfrescoRepository.findOne(sfNodeRef);

				printSort(sfData.getCostList());

				assertEquals("Check cost order", 100,
						nodeService.getProperty(sfData.getCostList().get(0).getNodeRef(), BeCPGModel.PROP_SORT));
				assertEquals("Check cost order", 101,
						nodeService.getProperty(sfData.getCostList().get(1).getNodeRef(), BeCPGModel.PROP_SORT));
				assertEquals("Check cost order", 102,
						nodeService.getProperty(sfData.getCostList().get(2).getNodeRef(), BeCPGModel.PROP_SORT));
				assertEquals("Check cost order", 103,
						nodeService.getProperty(sfData.getCostList().get(3).getNodeRef(), BeCPGModel.PROP_SORT));
				assertEquals("Check cost order", 104,
						nodeService.getProperty(sfData.getCostList().get(4).getNodeRef(), BeCPGModel.PROP_SORT));
				assertEquals("Check cost order", 105,
						nodeService.getProperty(sfData.getCostList().get(5).getNodeRef(), BeCPGModel.PROP_SORT));

				return null;

			}
		}, false, true);

	}

	// /**
	// * change sort, 4 to 2
	// *
	// * @throws InterruptedException the interrupted exception
	// */
	// public void testChangeSortDecrease() throws InterruptedException{
	//
	// logger.debug("testChangeSortListItem()");
	//
	// // create product
	// transactionService.getRetryingTransactionHelper().doInTransaction(new
	// RetryingTransactionCallback<NodeRef>(){
	// @Override
	// public NodeRef execute() throws Throwable {
	//
	// /*-- Create test folder --*/
	// NodeRef folderNodeRef =
	// nodeService.getChildByName(repositoryHelper.getCompanyHome(),
	// ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);
	// if(folderNodeRef != null)
	// {
	// fileFolderService.delete(folderNodeRef);
	// }
	// folderNodeRef =
	// fileFolderService.create(repositoryHelper.getCompanyHome(),
	// PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
	//
	// // create SF
	// SemiFinishedProductData sfData = new SemiFinishedProductData();
	// sfData.setName("SF");
	// List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
	// costList.add(new CostListDataItem(null, 3d, "€/kg", costs.get(0)));
	// costList.add(new CostListDataItem(null, 2d, "€/kg", costs.get(1)));
	// costList.add(new CostListDataItem(null, 3d, "€/kg", costs.get(2)));
	// costList.add(new CostListDataItem(null, 2d, "€/kg", costs.get(3)));
	// sfData.setCostList(costList);
	// Collection<QName> dataLists = productDictionaryService.getDataLists();
	// sfNodeRef = productDAO.create(folderNodeRef, sfData, dataLists);
	//
	// // load SF and test it
	// sfData = (SemiFinishedProductData)productDAO.find(sfNodeRef, dataLists);
	//
	// assertNotNull("Check costs exist", sfData.getCostList());
	// assertEquals("Check cost order", costs.get(0),
	// sfData.getCostList().get(0).getCost());
	// assertEquals("Check cost order", costs.get(1),
	// sfData.getCostList().get(1).getCost());
	// assertEquals("Check cost order", costs.get(2),
	// sfData.getCostList().get(2).getCost());
	// assertEquals("Check cost order", costs.get(3),
	// sfData.getCostList().get(3).getCost());
	//
	// // change sort pos4 => pos2
	// nodeService.setProperty(sfData.getCostList().get(3).getNodeRef(),
	// BeCPGModel.PROP_SORT, 2);
	//
	// sfData = (SemiFinishedProductData)productDAO.find(sfNodeRef, dataLists);
	//
	// assertNotNull("Check costs exist", sfData.getCostList());
	// assertEquals("Check cost order", costs.get(0),
	// sfData.getCostList().get(0).getCost());
	// assertEquals("Check cost order", costs.get(1),
	// sfData.getCostList().get(1).getCost());
	// assertEquals("Check cost order", costs.get(2),
	// sfData.getCostList().get(2).getCost());
	// assertEquals("Check cost order", costs.get(3),
	// sfData.getCostList().get(3).getCost());
	//
	// assertEquals("Check cost order", 1,
	// nodeService.getProperty(sfData.getCostList().get(0).getNodeRef(),
	// BeCPGModel.PROP_SORT));
	// assertEquals("Check cost order", 3,
	// nodeService.getProperty(sfData.getCostList().get(1).getNodeRef(),
	// BeCPGModel.PROP_SORT));
	// assertEquals("Check cost order", 4,
	// nodeService.getProperty(sfData.getCostList().get(2).getNodeRef(),
	// BeCPGModel.PROP_SORT));
	// assertEquals("Check cost order", 2,
	// nodeService.getProperty(sfData.getCostList().get(3).getNodeRef(),
	// BeCPGModel.PROP_SORT));
	//
	// return null;
	//
	// }},false,true);
	//
	// }
	//
	// /**
	// * change sort, 2 to 4
	// *
	// * @throws InterruptedException the interrupted exception
	// */
	// public void testChangeSortIncrease() throws InterruptedException{
	//
	// logger.debug("testChangeSortListItem()");
	//
	// // create product
	// transactionService.getRetryingTransactionHelper().doInTransaction(new
	// RetryingTransactionCallback<NodeRef>(){
	// @Override
	// public NodeRef execute() throws Throwable {
	//
	// /*-- Create test folder --*/
	// NodeRef folderNodeRef =
	// nodeService.getChildByName(repositoryHelper.getCompanyHome(),
	// ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);
	// if(folderNodeRef != null)
	// {
	// fileFolderService.delete(folderNodeRef);
	// }
	// folderNodeRef =
	// fileFolderService.create(repositoryHelper.getCompanyHome(),
	// PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
	//
	// // create SF
	// SemiFinishedProductData sfData = new SemiFinishedProductData();
	// sfData.setName("SF");
	// List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
	// costList.add(new CostListDataItem(null, 3d, "€/kg", costs.get(0)));
	// costList.add(new CostListDataItem(null, 2d, "€/kg", costs.get(1)));
	// costList.add(new CostListDataItem(null, 3d, "€/kg", costs.get(2)));
	// costList.add(new CostListDataItem(null, 2d, "€/kg", costs.get(3)));
	// sfData.setCostList(costList);
	// Collection<QName> dataLists = productDictionaryService.getDataLists();
	// sfNodeRef = productDAO.create(folderNodeRef, sfData, dataLists);
	//
	// // load SF and test it
	// sfData = (SemiFinishedProductData)productDAO.find(sfNodeRef, dataLists);
	//
	// assertNotNull("Check costs exist", sfData.getCostList());
	// assertEquals("Check cost order", costs.get(0),
	// sfData.getCostList().get(0).getCost());
	// assertEquals("Check cost order", costs.get(1),
	// sfData.getCostList().get(1).getCost());
	// assertEquals("Check cost order", costs.get(2),
	// sfData.getCostList().get(2).getCost());
	// assertEquals("Check cost order", costs.get(3),
	// sfData.getCostList().get(3).getCost());
	//
	// // change sort pos2 => pos4
	// nodeService.setProperty(sfData.getCostList().get(1).getNodeRef(),
	// BeCPGModel.PROP_SORT, 4);
	//
	// sfData = (SemiFinishedProductData)productDAO.find(sfNodeRef, dataLists);
	//
	// assertNotNull("Check costs exist", sfData.getCostList());
	// assertEquals("Check cost order", costs.get(0),
	// sfData.getCostList().get(0).getCost());
	// assertEquals("Check cost order", costs.get(1),
	// sfData.getCostList().get(1).getCost());
	// assertEquals("Check cost order", costs.get(2),
	// sfData.getCostList().get(2).getCost());
	// assertEquals("Check cost order", costs.get(3),
	// sfData.getCostList().get(3).getCost());
	//
	// assertEquals("Check cost order", 1,
	// nodeService.getProperty(sfData.getCostList().get(0).getNodeRef(),
	// BeCPGModel.PROP_SORT));
	// assertEquals("Check cost order", 4,
	// nodeService.getProperty(sfData.getCostList().get(1).getNodeRef(),
	// BeCPGModel.PROP_SORT));
	// assertEquals("Check cost order", 2,
	// nodeService.getProperty(sfData.getCostList().get(2).getNodeRef(),
	// BeCPGModel.PROP_SORT));
	// assertEquals("Check cost order", 3,
	// nodeService.getProperty(sfData.getCostList().get(3).getNodeRef(),
	// BeCPGModel.PROP_SORT));
	//
	// return null;
	//
	// }},false,true);
	//
	// }

	// /**
	// * Test the fastest method to listFiles, getChildAssocs is the fastest,
	// lucene search is the slowest
	// *
	// * @throws InterruptedException the interrupted exception
	// */
	// public void testPerfLoad() throws InterruptedException{
	//
	// logger.debug("testChangeSortListItem()");
	//
	// // create product
	// transactionService.getRetryingTransactionHelper().doInTransaction(new
	// RetryingTransactionCallback<NodeRef>(){
	// @Override
	// public NodeRef execute() throws Throwable {
	//
	// int [] arrFiles = {1, 5, 20, 200, 1000};
	//
	// /*
	// * Use listFiles
	// */
	// for(int nbFiles : arrFiles){
	//
	// /*-- Create test folder --*/
	// NodeRef folderNodeRef =
	// nodeService.getChildByName(repositoryHelper.getCompanyHome(),
	// ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);
	// if(folderNodeRef != null)
	// {
	// fileFolderService.delete(folderNodeRef);
	// }
	// folderNodeRef =
	// fileFolderService.create(repositoryHelper.getCompanyHome(),
	// PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
	//
	// // create SF
	// SemiFinishedProductData sfData = new SemiFinishedProductData();
	// sfData.setName("SF");
	// List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
	//
	// for(int z_idx=0 ; z_idx<nbFiles ; z_idx++){
	//
	// costList.add(new CostListDataItem(null, 3d, "€/kg", null, costs.get(0),
	// false));
	// costList.add(new CostListDataItem(null, 2d, "€/kg", null, costs.get(1),
	// false));
	// costList.add(new CostListDataItem(null, 3d, "€/kg", null, costs.get(2),
	// false));
	// costList.add(new CostListDataItem(null, 2d, "€/kg", null, costs.get(3),
	// false));
	// }
	//
	// sfData.setCostList(costList);
	// Collection<QName> dataLists = productDictionaryService.getDataLists();
	// sfNodeRef = productDAO.create(folderNodeRef, sfData, dataLists);
	//
	// // load SF and test it
	// sfData = (SemiFinishedProductData)productDAO.find(sfNodeRef, dataLists);
	//
	// NodeRef listContainerNodeRef = entityListDAO.getListContainer(sfNodeRef);
	// NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef,
	// BeCPGModel.TYPE_COSTLIST);
	//
	// StopWatch watch = new StopWatch();
	// watch.start();
	//
	// List<FileInfo> files = fileFolderService.listFiles(listNodeRef);
	//
	// watch.stop();
	// logger.debug("listFiles nb files: " + files.size() + " - executed in  " +
	// watch.getTotalTimeSeconds() + " seconds");
	// }
	//
	// /*
	// * Use lucene search
	// */
	//
	// for(int nbFiles : arrFiles){
	//
	// /*-- Create test folder --*/
	// NodeRef folderNodeRef =
	// nodeService.getChildByName(repositoryHelper.getCompanyHome(),
	// ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);
	// if(folderNodeRef != null)
	// {
	// fileFolderService.delete(folderNodeRef);
	// }
	// folderNodeRef =
	// fileFolderService.create(repositoryHelper.getCompanyHome(),
	// PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
	//
	// // create SF
	// SemiFinishedProductData sfData = new SemiFinishedProductData();
	// sfData.setName("SF");
	// List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
	//
	// for(int z_idx=0 ; z_idx<nbFiles ; z_idx++){
	//
	// costList.add(new CostListDataItem(null, 3d, "€/kg", null, costs.get(0),
	// false));
	// costList.add(new CostListDataItem(null, 2d, "€/kg", null, costs.get(1),
	// false));
	// costList.add(new CostListDataItem(null, 3d, "€/kg", null, costs.get(2),
	// false));
	// costList.add(new CostListDataItem(null, 2d, "€/kg", null, costs.get(3),
	// false));
	// }
	//
	// sfData.setCostList(costList);
	// Collection<QName> dataLists = productDictionaryService.getDataLists();
	// sfNodeRef = productDAO.create(folderNodeRef, sfData, dataLists);
	//
	// // load SF and test it
	// sfData = (SemiFinishedProductData)productDAO.find(sfNodeRef, dataLists);
	//
	// NodeRef listContainerNodeRef = entityListDAO.getListContainer(sfNodeRef);
	// NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef,
	// BeCPGModel.TYPE_COSTLIST);
	//
	// String query = String.format("+PARENT:\"%s\"", listNodeRef);
	// Map<String, Boolean> sort = new HashMap<String, Boolean>();
	// sort.put("@" + BeCPGModel.PROP_SORT, true);
	//
	// StopWatch watch = new StopWatch();
	// watch.start();
	//
	// List<NodeRef> nodeRefs = beCPGSearchService.unProtLuceneSearch(query,
	// sort, -1);
	//
	// watch.stop();
	// logger.debug("search nb files: " + nodeRefs.size() + " - executed in  " +
	// watch.getTotalTimeSeconds() + " seconds");
	// }
	//
	// /*
	// * Use getChildAssocs
	// */
	//
	// for(int nbFiles : arrFiles){
	//
	// /*-- Create test folder --*/
	// NodeRef folderNodeRef =
	// nodeService.getChildByName(repositoryHelper.getCompanyHome(),
	// ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);
	// if(folderNodeRef != null)
	// {
	// fileFolderService.delete(folderNodeRef);
	// }
	// folderNodeRef =
	// fileFolderService.create(repositoryHelper.getCompanyHome(),
	// PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
	//
	// // create SF
	// SemiFinishedProductData sfData = new SemiFinishedProductData();
	// sfData.setName("SF");
	// List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
	//
	// for(int z_idx=0 ; z_idx<nbFiles ; z_idx++){
	//
	// costList.add(new CostListDataItem(null, 3d, "€/kg", null, costs.get(0),
	// false));
	// costList.add(new CostListDataItem(null, 2d, "€/kg", null, costs.get(1),
	// false));
	// costList.add(new CostListDataItem(null, 3d, "€/kg", null, costs.get(2),
	// false));
	// costList.add(new CostListDataItem(null, 2d, "€/kg", null, costs.get(3),
	// false));
	// }
	//
	// sfData.setCostList(costList);
	// Collection<QName> dataLists = productDictionaryService.getDataLists();
	// sfNodeRef = productDAO.create(folderNodeRef, sfData, dataLists);
	//
	// // load SF and test it
	// sfData = (SemiFinishedProductData)productDAO.find(sfNodeRef, dataLists);
	//
	// NodeRef listContainerNodeRef = entityListDAO.getListContainer(sfNodeRef);
	// NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef,
	// BeCPGModel.TYPE_COSTLIST);
	//
	// StopWatch watch = new StopWatch();
	// watch.start();
	//
	// List<NodeRef> nodeRefs = listCostListItem(listNodeRef, false, true);
	//
	// watch.stop();
	// logger.debug("getChildAssocs nb files: " + nodeRefs.size() +
	// " - executed in  " + watch.getTotalTimeSeconds() + " seconds");
	// }
	//
	//
	// return null;
	//
	// }},false,true);
	//
	// }
	//
	// private List<NodeRef> listCostListItem(NodeRef contextNodeRef, boolean
	// folders, boolean files)
	// {
	// Set<QName> searchTypeQNames = new HashSet<QName>(1);
	// searchTypeQNames.add(BeCPGModel.TYPE_COSTLIST);
	//
	// // Do the query
	// List<ChildAssociationRef> childAssocRefs =
	// nodeService.getChildAssocs(contextNodeRef, searchTypeQNames);
	// List<NodeRef> result = new ArrayList<NodeRef>(childAssocRefs.size());
	// for (ChildAssociationRef assocRef : childAssocRefs)
	// {
	// result.add(assocRef.getChildRef());
	// }
	// // Done
	// return result;
	// }

	public void printSort(List<CostListDataItem> costListDataItem) {

		for (CostListDataItem c : costListDataItem) {

			logger.info("level : " + (Integer) nodeService.getProperty(c.getNodeRef(), BeCPGModel.PROP_DEPTH_LEVEL)
					+ " - Cost " + (String) nodeService.getProperty(c.getCost(), ContentModel.PROP_NAME)
					+ " - sorted: " + (Integer) nodeService.getProperty(c.getNodeRef(), BeCPGModel.PROP_SORT));
		}
	}
}
