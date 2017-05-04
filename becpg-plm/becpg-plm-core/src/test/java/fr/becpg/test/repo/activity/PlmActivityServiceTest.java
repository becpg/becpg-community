package fr.becpg.test.repo.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.forum.CommentService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class PlmActivityServiceTest extends AbstractFinishedProductTest {

	private static Log logger = LogFactory.getLog(PlmActivityServiceTest.class);
	private static final String COMMENT_TITLE_TEXT = "Comment subject";
	private static final String COMMENT_DATA_TEXT = "Comment body just for test";

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	@Autowired
	private ProductService productService;

	@Autowired
	private EntityActivityService entityActivityService;

	@Autowired
	private CommentService commentService;
	
	@Autowired
	private EntityVersionService entityVersionService;

	
	@Autowired
	private CheckOutCheckInService checkOutCheckInService;

	private NodeRef getActivityList(NodeRef productNodeRef) {
		NodeRef listNodeRef = null;
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(productNodeRef);
		if (listContainerNodeRef != null) {
			listNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);
		}
		return listNodeRef;

	}

	private List<NodeRef> getActivities(NodeRef entityNodeRef) {
		//beCPGCacheService.clearAllCaches();
		
		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			List<NodeRef> ret = new ArrayList<>();
			NodeRef activityListNodeRef = getActivityList(entityNodeRef);
			if (activityListNodeRef != null) {
				// All activities of product
				ret =  entityListDAO.getListItems(activityListNodeRef,
						BeCPGModel.TYPE_ACTIVITY_LIST);
				for(NodeRef tmp : ret){
					logger.info("Data: "+nodeService.getProperty(tmp, BeCPGModel.PROP_ACTIVITYLIST_DATA));
					logger.info("User: "+nodeService.getProperty(tmp, BeCPGModel.PROP_ACTIVITYLIST_USERID));
				}
			} else {
				logger.error("No activity list");
			}
			
			return ret;
		}, true, false);
	}

	@Test
	public void checkEntityCommentActivity() {

		// Create finished composite product with ActivityList
		NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			// Create product
			FinishedProductData productData = new FinishedProductData();
			productData.setParentNodeRef(getTestFolderNodeRef());
			productData.setName("finished-product");
			alfrescoRepository.save(productData);

			// Add Activity List to product
			NodeRef listContainerNodeRef = entityListDAO.getListContainer(productData.getNodeRef());
			entityListDAO.createList(listContainerNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);

			return productData.getNodeRef();
		}, false, true);

		// Check if No Activity was created
		assertEquals("Check if No Activity", 1, getActivities(finishedProductNodeRef).size());

		// Add comment to finished product
		NodeRef commentOnFinishedProductNodeRef = transactionService.getRetryingTransactionHelper()
				.doInTransaction(() -> {
					NodeRef commentNodeRef = commentService.createComment(finishedProductNodeRef, COMMENT_TITLE_TEXT,
							COMMENT_DATA_TEXT, false);
					return commentNodeRef;
				}, false, true);

		// Check if comment activity was created
		assertEquals("Activity 2: comment creation", 2, getActivities(finishedProductNodeRef).size());

		// Update comment on finished product
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			commentService.updateComment(commentOnFinishedProductNodeRef, COMMENT_TITLE_TEXT, COMMENT_DATA_TEXT);
			return null;
		}, false, true);

		// Check if comment activity was updated
		assertEquals("Activity 3: update comment", 3, getActivities(finishedProductNodeRef).size());

		// Delete finished product comment
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			commentService.deleteComment(commentOnFinishedProductNodeRef);
			return null;
		}, false, true);

		// Check if comment activity was deleted
		Assert.assertEquals("Activity 4: delete comment", 4, getActivities(finishedProductNodeRef).size());

	}
	
	

	@Test
	public void checkEntityVersionActivity() {
		
		AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
		
		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Create raw material --*/
			NodeRef r = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test version activity");
			
			// Add Activity List to product
			NodeRef listContainerNodeRef = entityListDAO.getListContainer(r);
			entityListDAO.createList(listContainerNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);
			
			return r;
		}, false, true);

		// 6 activities on creation

		if (!nodeService.hasAspect(rawMaterialNodeRef, ContentModel.ASPECT_VERSIONABLE)) {
			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
				logger.debug("Add versionnable aspect");
				Map<QName, Serializable> aspectProperties = new HashMap<>();
				aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
				nodeService.addAspect(rawMaterialNodeRef, ContentModel.ASPECT_VERSIONABLE, aspectProperties);
				return rawMaterialNodeRef;
			}, false, true);

		}

		
		// Check if No Activity was created
 		assertEquals("Check create Activity", 6, getActivities(rawMaterialNodeRef).size());

		
		final NodeRef workingCopyNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			return checkOutCheckInService.checkout(rawMaterialNodeRef);

		}, false, true);

		
		assertEquals("Check if No Activity", 0, getActivities(workingCopyNodeRef).size());
		
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			nodeService.setProperty(workingCopyNodeRef, PLMModel.PROP_PRODUCT_UNIT, ProductUnit.mL.toString());
			return null;
		}, false, true);
		
		// No activity on working copy
		assertEquals("Check update Activity", 0, getActivities(workingCopyNodeRef).size());

		final NodeRef newRawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			// Check in
			Map<String, Serializable> versionProperties = new HashMap<>();
			versionProperties.put(Version.PROP_DESCRIPTION, "This is a test version");
			versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE, VersionType.MAJOR);
			return checkOutCheckInService.checkin(workingCopyNodeRef, versionProperties);
		}, false, true);

		//Version activity
		assertEquals("Check if No Activity", 8, getActivities(newRawMaterialNodeRef).size());
		
	}
	
	
	@Test
	public void checkEntityBranchActivity() throws InterruptedException {
		
		AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			/*-- Create raw material --*/
			NodeRef r = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test branch activity");
			
			// Add Activity List to product
			NodeRef listContainerNodeRef = entityListDAO.getListContainer(r);
			entityListDAO.createList(listContainerNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);
			return r;
		}, false, true);


		final NodeRef branchNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			return entityVersionService.createBranch(rawMaterialNodeRef, getTestFolderNodeRef());
			
		}, false, true);
		
		// Activity reset on branch
		assertEquals("Check if No Activity", 0, getActivities(branchNodeRef).size());
		
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			nodeService.setProperty(branchNodeRef, PLMModel.PROP_PRODUCT_UNIT, ProductUnit.mL.toString());
			return null;
		}, false, true);
		
		// Activity recorded on branch
		assertEquals("Check update Activity", 1, getActivities(branchNodeRef).size());

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			return entityVersionService.mergeBranch(branchNodeRef, rawMaterialNodeRef, VersionType.MAJOR, "Tests");
		}, false, true);

		//Version activity
		assertEquals("Check if No Activity", 9, getActivities(rawMaterialNodeRef).size());

	}

	@Test
	public void checkChangeStateActivity() throws InterruptedException {
		
		AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			/*-- Create raw material --*/
			NodeRef r = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test branch activity");
			
			// Add Activity List to product
			NodeRef listContainerNodeRef = entityListDAO.getListContainer(r);
			entityListDAO.createList(listContainerNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);
			return r;
		}, false, true);


		// Activity reset on branch
		assertEquals("Check if No Activity", 6, getActivities(rawMaterialNodeRef).size());
		
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			nodeService.setProperty(rawMaterialNodeRef, PLMModel.PROP_PRODUCT_STATE, SystemState.Valid);
			return null;
		}, false, true);
		
		// Activity recorded on branch
		assertEquals("Check update Activity", 7, getActivities(rawMaterialNodeRef).size());
		
		

	}
	

	//@Test
	public void cleanActivityServiceTest() {

		// Init Variables
		Calendar thisMonth = Calendar.getInstance();
		thisMonth.setTime(new Date());

		// Create semiFinished product and add it to product
		NodeRef semiFinishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			SemiFinishedProductData semiFinishedProductData = new SemiFinishedProductData();
			semiFinishedProductData.setName("semiFinished-product");
			return alfrescoRepository.create(getTestFolderNodeRef(), semiFinishedProductData).getNodeRef();
		}, false, true);

		// Create product with ActivityList
		NodeRef productNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			// Create product
			FinishedProductData productData = new FinishedProductData();
			productData.setParentNodeRef(getTestFolderNodeRef());
			productData.setName("activity-test-product");
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(new CompoListDataItem(null, null, 1d, 1d, CompoListUnit.P, 0d, DeclarationType.Declare,
					semiFinishedProductNodeRef));
			productData.getCompoListView().setCompoList(compoList);
			alfrescoRepository.save(productData);

			// Add Activity List to product
			NodeRef listContainerNodeRef = entityListDAO.getListContainer(productData.getNodeRef());
			entityListDAO.createList(listContainerNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);

			return productData.getNodeRef();
		}, false, true);

		// Add some activities to product
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			// Get product
			FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(productNodeRef);

			// Add formulation activity
			for (int i = 0; i < 200; i++) {
				productService.formulate(productNodeRef);
			}

			// Add Comment activity
			for (int i = 0; i < 50; i++) {
				commentService.createComment(productNodeRef, COMMENT_TITLE_TEXT, COMMENT_DATA_TEXT, false);
			}

			// // Add State Activity, but..!! it doesn't work !!??
			 for (int i = 0; i < 50; i++) {
			 productData.setState(SystemState.Refused);
			 }
			//
			// // Add DataList activity
			 for (int i = 0; i < 4; i++) {
			 productData.getCostList().add(new CostListDataItem(null, 10d,
			 "kg", null, costs.get(0), false));
			 }

			// Save product
			alfrescoRepository.save(productData);

			// Return Product Reference
			return productData.getNodeRef();
		}, false, true);

		// Change activity creation time
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			// Get Activity list
			NodeRef activityListNodeRef = getActivityList(productNodeRef);
			if (activityListNodeRef != null) {
				// For all activities of product
				for (NodeRef activityListItemNodeRef : entityListDAO.getListItems(activityListNodeRef,
						BeCPGModel.TYPE_ACTIVITY_LIST)) {
					policyBehaviourFilter.disableBehaviour(activityListItemNodeRef, ContentModel.ASPECT_AUDITABLE);
					nodeService.setProperty(activityListItemNodeRef, ContentModel.PROP_CREATED, thisMonth.getTime());
					policyBehaviourFilter.enableBehaviour(activityListItemNodeRef, ContentModel.ASPECT_AUDITABLE);
					thisMonth.add(Calendar.DAY_OF_MONTH, -1);
				}
			}
			return null;
		}, false, true);

		int activitiesBeforeClean = 0;
		int activitiesAfterClean = 0;

		NodeRef activityListNodeRef = getActivityList(productNodeRef);
		if (activityListNodeRef != null) {
			// All activities of product
			List<NodeRef> productListactivity = entityListDAO.getListItems(activityListNodeRef,
					BeCPGModel.TYPE_ACTIVITY_LIST);
			activitiesBeforeClean = productListactivity.size();
			// Activities number
			logger.info("Activities number before clean : " + activitiesBeforeClean);
		}

		// Clean Activities
		entityActivityService.cleanActivities();

		activityListNodeRef = getActivityList(productNodeRef);
		if (activityListNodeRef != null) {
			List<NodeRef> productListactivity = entityListDAO.getListItems(activityListNodeRef,
					BeCPGModel.TYPE_ACTIVITY_LIST);
			activitiesAfterClean = productListactivity.size();
			// Activities number
			logger.info("Activities number after clean : " + activitiesAfterClean);
		}

		// Check Clean activityService
		if (activitiesBeforeClean > 50) {
			assertTrue(activitiesBeforeClean > activitiesAfterClean);
			assertTrue(activitiesAfterClean > 0);
			assertFalse(activitiesAfterClean == 50);
		} else {
			assertTrue(activitiesBeforeClean == activitiesAfterClean);
		}


	}

}
