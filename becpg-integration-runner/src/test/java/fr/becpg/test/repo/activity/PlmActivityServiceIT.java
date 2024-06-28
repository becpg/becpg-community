package fr.becpg.test.repo.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.forum.CommentService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
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
import fr.becpg.repo.activity.data.ActivityType;
import fr.becpg.repo.activity.helper.AuditActivityHelper;
import fr.becpg.repo.audit.model.AuditQuery;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.impl.ActivityAuditPlugin;
import fr.becpg.repo.audit.service.BeCPGAuditService;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class PlmActivityServiceIT extends AbstractFinishedProductTest {

	private static Log logger = LogFactory.getLog(PlmActivityServiceIT.class);
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
	protected BeCPGAuditService beCPGAuditService;

	protected NodeRef getActivityList(NodeRef productNodeRef) {
		NodeRef listNodeRef = null;
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(productNodeRef);
		if (listContainerNodeRef != null) {
			listNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);
		}
		return listNodeRef;

	}

	protected List<ActivityListDataItem> getActivities(NodeRef entityNodeRef, Map<String, Boolean> sortMap) {
		AuditQuery auditFilter = AuditQuery.createQuery().sortBy(ActivityAuditPlugin.PROP_CM_CREATED)
				.filter(ActivityAuditPlugin.ENTITY_NODEREF, entityNodeRef.toString());

		return inWriteTx(() -> beCPGAuditService.listAuditEntries(AuditType.ACTIVITY, auditFilter).stream()
				.map(json -> AuditActivityHelper.parseActivity(json)).collect(Collectors.toList()));

	}

	protected List<ActivityListDataItem> getActivityListDataItems(NodeRef entityNodeRef) {

		return inWriteTx(() -> {

			AuditQuery auditFilter = AuditQuery.createQuery().asc(false).dbAsc(false)
					.sortBy(ActivityAuditPlugin.PROP_CM_CREATED).filter("entityNodeRef", entityNodeRef.toString());

			return beCPGAuditService.listAuditEntries(AuditType.ACTIVITY, auditFilter).stream()
					.map(json -> AuditActivityHelper.parseActivity(json)).collect(Collectors.toList());

		});
	}

	private NodeRef createFinishedProduct() {
		// Create finished composite product with ActivityList
		return inWriteTx(() -> {
			// Create product
			FinishedProductData productData = new FinishedProductData();
			productData.setParentNodeRef(getTestFolderNodeRef());
			productData.setName("finished-product");
			alfrescoRepository.save(productData);
			// Add Activity List to product
			NodeRef listContainerNodeRef = entityListDAO.getListContainer(productData.getNodeRef());
			entityListDAO.createList(listContainerNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);

			return productData.getNodeRef();

		});
	}

	@Test
	public void checkEntityCommentActivity() {

		NodeRef finishedProductNodeRef = createFinishedProduct();

		// Check if just one activity was created
		assertEquals("Check if No Activity", 1, getActivities(finishedProductNodeRef, null).size());

		// Add comment to finished product
		NodeRef commentOnFinishedProductNodeRef = inWriteTx(() -> {
			NodeRef commentNodeRef = commentService.createComment(finishedProductNodeRef, COMMENT_TITLE_TEXT,
					COMMENT_DATA_TEXT, false);
			return commentNodeRef;
		});

		// Check if comment activity was created
		assertEquals("Activity 3: comment creation", 2, getActivities(finishedProductNodeRef, null).size());

		// Update comment on finished product
		inWriteTx(() -> {
			commentService.updateComment(commentOnFinishedProductNodeRef, COMMENT_TITLE_TEXT, COMMENT_DATA_TEXT);
			return null;
		});

		// Check if comment activity was updated
		assertEquals("Activity 4: update comment", 3, getActivities(finishedProductNodeRef, null).size());

		// Delete finished product comment
		inWriteTx(() -> {
			commentService.deleteComment(commentOnFinishedProductNodeRef);
			return null;
		});

		// Check if comment activity was deleted
		Assert.assertEquals("Activity 5: delete comment", 4, getActivities(finishedProductNodeRef, null).size());

	}

	@Test
	public void checkEntityVersionActivity() {

		AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

		final NodeRef productNodeRef = createFinishedProduct();

		// Check if No Activity was created
		assertEquals("Check create Activity", 1, getActivities(productNodeRef, null).size());

		if (!nodeService.hasAspect(productNodeRef, ContentModel.ASPECT_VERSIONABLE)) {
			inWriteTx(() -> {
				logger.debug("Add versionnable aspect");
				Map<QName, Serializable> aspectProperties = new HashMap<>();
				aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
				nodeService.addAspect(productNodeRef, ContentModel.ASPECT_VERSIONABLE, aspectProperties);
				return productNodeRef;
			});

		}

		// Check update product
		assertEquals("Check create Activity", 2, getActivities(productNodeRef, null).size());

		final NodeRef workingCopyNodeRef = inWriteTx(() -> {

			NodeRef destNodeRef = nodeService.getPrimaryParent(productNodeRef).getParentRef();

			return entityVersionService.createBranch(productNodeRef, destNodeRef);

		});

		// No activity on working copy
		assertEquals("Check if No Activity on working copy", 0, getActivities(workingCopyNodeRef, null).size());

		final NodeRef newRawMaterialNodeRef = inWriteTx(() -> {
			return entityVersionService.mergeBranch(workingCopyNodeRef, productNodeRef, VersionType.MAJOR,
					"This is a test version");
		});

		// Version activity
		assertEquals("Check version activity", 4, getActivities(newRawMaterialNodeRef, null).size());

	}

	@Test
	public void checkEntityBranchActivity() throws InterruptedException {

		AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

		final NodeRef productNodeRef = createFinishedProduct();

		final NodeRef branchNodeRef = inWriteTx(() -> {

			return entityVersionService.createBranch(productNodeRef, getTestFolderNodeRef());

		});

		// No Activity on branch
		assertEquals("Check if No Activity on branch", 0, getActivities(branchNodeRef, null).size());

		inWriteTx(() -> {
			nodeService.setProperty(branchNodeRef, PLMModel.PROP_PRODUCT_UNIT, ProductUnit.mL.toString());
			return null;
		});

		// Activity recorded on branch
		assertEquals("Check update Activity", 1, getActivities(branchNodeRef, null).size());

		inWriteTx(() -> {
			return entityVersionService.mergeBranch(branchNodeRef, productNodeRef, VersionType.MAJOR, "Tests");
		});

		// Merge activities
		assertEquals("Check Merge Activities", 5, getActivities(productNodeRef, null).size());

	}

	@Test
	public void checkChangeStateActivity() throws InterruptedException {

		AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

		final NodeRef productNodeRef = createFinishedProduct();

		// Activity recorded on entity
		assertEquals("Check if No Activity", 1, getActivities(productNodeRef, null).size());

		inWriteTx(() -> {
			nodeService.setProperty(productNodeRef, PLMModel.PROP_PRODUCT_STATE, SystemState.Valid);
			return null;
		});

		// Activity recorded on branch
		assertEquals("Check update Activity", 1, (int) getActivityListDataItems(productNodeRef).stream()
				.filter(el -> el.getActivityType().equals(ActivityType.State)).count());

	}

	@Test
	public void checkEntityDatalistActivity() {

		final NodeRef lSF1NodeRef = inWriteTx(() -> {
			LocalSemiFinishedProductData lSF1 = new LocalSemiFinishedProductData();
			lSF1.setName("Local semi finished 1");
			return alfrescoRepository.create(getTestFolderNodeRef(), lSF1).getNodeRef();

		});

		final NodeRef finishedProductNodeRef = createFinishedProduct();

		assertEquals("Check create Activity", 1, getActivities(finishedProductNodeRef, null).size());

		inWriteTx(() -> {
			List<CompoListDataItem> compoList = new ArrayList<>();
			/*
			 * compoList.add( new CompoListDataItem(null, null, 1d, 1d, ProductUnit.P, 0d,
			 * DeclarationType.Declare, lSF1NodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQty(1d).withQtyUsed(1d).withUnit(ProductUnit.P).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(lSF1NodeRef));
			FinishedProductData finishedProduct;
			finishedProduct = ((FinishedProductData) alfrescoRepository.findOne(finishedProductNodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);
			alfrescoRepository.save(finishedProduct);
			return null;
		});

		assertEquals("Check update Activity", 2, getActivities(finishedProductNodeRef, null).size());
	}

}
