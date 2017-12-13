package fr.becpg.test.repo.activity;

import java.io.Serializable;
import java.util.ArrayList;
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
import fr.becpg.repo.activity.data.ActivityListDataItem;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class PlmActivityServiceTest extends AbstractFinishedProductTest {

	private static Log logger = LogFactory.getLog(PlmActivityServiceTest.class);
	protected static final String COMMENT_TITLE_TEXT = "Comment subject";
	protected static final String COMMENT_DATA_TEXT = "Comment body just for test";

	@Autowired
	protected AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	@Autowired
	protected EntityActivityService entityActivityService;

	@Autowired
	protected CommentService commentService;

	@Autowired
	private EntityVersionService entityVersionService;

	@Autowired
	private CheckOutCheckInService checkOutCheckInService;

	protected NodeRef getActivityList(NodeRef productNodeRef) {
		NodeRef listNodeRef = null;
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(productNodeRef);
		if (listContainerNodeRef != null) {
			listNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);
		}
		return listNodeRef;

	}

	protected List<NodeRef> getActivities(NodeRef entityNodeRef, Map<String, Boolean> sortMap) {
		// beCPGCacheService.clearAllCaches();
		
		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			List<NodeRef> ret = new ArrayList<>();
			NodeRef activityListNodeRef = getActivityList(entityNodeRef);
			if (activityListNodeRef != null) {
				// All activities of product
				ret = sortMap !=null ? entityListDAO.getListItems(activityListNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST, sortMap) : 
					entityListDAO.getListItems(activityListNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);
				
				ret.forEach(tmp -> {
					logger.info("Data: " + nodeService.getProperty(tmp, BeCPGModel.PROP_ACTIVITYLIST_DATA));
					logger.info("User: " + nodeService.getProperty(tmp, BeCPGModel.PROP_ACTIVITYLIST_USERID));
				});
				

			} else {
				logger.error("No activity list");
			}

			return ret;
		}, true, false);
	}
	
	protected Map<NodeRef, ActivityListDataItem> getActivityListDataItems(NodeRef entityNodeRef) {

		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			Map<NodeRef, ActivityListDataItem> ret = new HashMap<>();
			NodeRef activityListNodeRef = getActivityList(entityNodeRef);
			if (activityListNodeRef != null) {
				// All activities of product
				entityListDAO.getListItems(activityListNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST)
				.forEach(tmp -> ret.put(tmp, (ActivityListDataItem) alfrescoRepository.findOne(tmp)));
				
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

		// Check if just one activity was created
		assertEquals("Check if No Activity", 1, getActivities(finishedProductNodeRef, null).size());

		// Add comment to finished product
		NodeRef commentOnFinishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			NodeRef commentNodeRef = commentService.createComment(finishedProductNodeRef, COMMENT_TITLE_TEXT, COMMENT_DATA_TEXT, false);
			return commentNodeRef;
		}, false, true);

		// Check if comment activity was created
		assertEquals("Activity 3: comment creation", 3, getActivities(finishedProductNodeRef, null).size());

		// Update comment on finished product
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			commentService.updateComment(commentOnFinishedProductNodeRef, COMMENT_TITLE_TEXT, COMMENT_DATA_TEXT);
			return null;
		}, false, true);

		// Check if comment activity was updated
		assertEquals("Activity 4: update comment", 4, getActivities(finishedProductNodeRef, null).size());

		// Delete finished product comment
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			commentService.deleteComment(commentOnFinishedProductNodeRef);
			return null;
		}, false, true);

		// Check if comment activity was deleted
		Assert.assertEquals("Activity 5: delete comment", 5, getActivities(finishedProductNodeRef, null).size());

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

		// 7 activities on creation

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
		assertEquals("Check create Activity", 7, getActivities(rawMaterialNodeRef, null).size());

		final NodeRef workingCopyNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			return checkOutCheckInService.checkout(rawMaterialNodeRef);

		}, false, true);

		assertEquals("Check if No Activity", 0, getActivities(workingCopyNodeRef, null).size());

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			nodeService.setProperty(workingCopyNodeRef, PLMModel.PROP_PRODUCT_UNIT, ProductUnit.mL.toString());
			return null;
		}, false, true);

		// No activity on working copy
		assertEquals("Check update Activity", 0, getActivities(workingCopyNodeRef, null).size());

		final NodeRef newRawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			// Check in
			Map<String, Serializable> versionProperties = new HashMap<>();
			versionProperties.put(Version.PROP_DESCRIPTION, "This is a test version");
			versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE, VersionType.MAJOR);
			return checkOutCheckInService.checkin(workingCopyNodeRef, versionProperties);
		}, false, true);

		// Version activity
		assertEquals("Check if No Activity", 8, getActivities(newRawMaterialNodeRef, null).size());

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
		assertEquals("Check if No Activity", 0, getActivities(branchNodeRef, null).size());

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			nodeService.setProperty(branchNodeRef, PLMModel.PROP_PRODUCT_UNIT, ProductUnit.mL.toString());
			return null;
		}, false, true);

		// Activity recorded on branch
		assertEquals("Check update Activity", 1, getActivities(branchNodeRef, null).size());

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			return entityVersionService.mergeBranch(branchNodeRef, rawMaterialNodeRef, VersionType.MAJOR, "Tests");
		}, false, true);

		// Version activity
		assertEquals("Check if No Activity", 9, getActivities(rawMaterialNodeRef, null).size());

	}

	@Test
	public void checkChangeStateActivity() throws InterruptedException {

		AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			/*-- Create raw material --*/
			NodeRef r = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "MP test change state activity");

			// Add Activity List to product
			NodeRef listContainerNodeRef = entityListDAO.getListContainer(r);
			entityListDAO.createList(listContainerNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);
			return r;
		}, false, true);

		// Activity recorded on entity ( by default 6)
		assertEquals("Check if No Activity", 6, getActivities(rawMaterialNodeRef, null).size());

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			nodeService.setProperty(rawMaterialNodeRef, PLMModel.PROP_PRODUCT_STATE, SystemState.Valid);
			return null;
		}, false, true);

		// Activity recorded on branch
		assertEquals("Check update Activity", 7, getActivities(rawMaterialNodeRef, null).size());

	}

	@Test
	public void checkEntityDatalistActivity() {

		final NodeRef lSF1NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			LocalSemiFinishedProductData lSF1 = new LocalSemiFinishedProductData();
			lSF1.setName("Local semi finished 1");
			return alfrescoRepository.create(getTestFolderNodeRef(), lSF1).getNodeRef();

		}, false, true);

		final NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			/*-- Create finished product --*/
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Finished Product");
			NodeRef product = alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
			// Add Activity List to product
			NodeRef listContainerNodeRef = entityListDAO.getListContainer(product);
			entityListDAO.createList(listContainerNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);
			return product;

		}, false, true);

		assertEquals("Check create Activity", 1, getActivities(finishedProductNodeRef, null).size());

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(new CompoListDataItem(null, null, 1d, 1d, CompoListUnit.P, 0d, DeclarationType.Declare, lSF1NodeRef));
			FinishedProductData finishedProduct;
			finishedProduct = ((FinishedProductData) alfrescoRepository.findOne(finishedProductNodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);
			alfrescoRepository.save(finishedProduct);
			return null;
		}, false, true);

		assertEquals("Check update Activity", 2, getActivities(finishedProductNodeRef, null).size());
	}



}
