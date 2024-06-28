package fr.becpg.test.repo.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.audit.AuditComponent;
import org.alfresco.repo.audit.model.AuditApplication;
import org.alfresco.repo.audit.model.AuditModelRegistry;
import org.alfresco.repo.domain.audit.AuditDAO;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.rest.api.Audit;
import org.alfresco.rest.api.model.AuditEntry;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.activity.data.ActivityEvent;
import fr.becpg.repo.activity.data.ActivityListDataItem;
import fr.becpg.repo.activity.data.ActivityType;
import fr.becpg.repo.audit.plugin.DatabaseAuditPlugin;
import fr.becpg.repo.audit.plugin.impl.ActivityAuditPlugin;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;

/**
 * This <code>class</code> is a test case of the purge functionality
 *
 * @see EntityActivityService#cleanActivities()
 *
 * @since beCPG-PLM 2.2.2
 *
 * @author rabah.
 */
public class PurgeActivityIT extends PlmActivityServiceIT {

	private static Log logger = LogFactory.getLog(PurgeActivityIT.class);

	ContentService contentService;

	@Autowired
	@Qualifier("auditApi")
	@Lazy
	private Audit audit;

	@Autowired
	private AuditComponent auditComponent;

	@Autowired
	private AuditDAO auditDAO;

	@Autowired
	private AuditModelRegistry auditModelRegistry;

	@Autowired
	private ActivityAuditPlugin activityAuditPlugin;

	private static final int MAX_PAGE = 50;

	private static final Map<String, Boolean> SORT_MAP;
	static {
		SORT_MAP = new LinkedHashMap<>();
		SORT_MAP.put("@cm:created", true);
	}

	private static final NodeRef ENTITY_NODEREF = new NodeRef("workspace://SpacesStore/8b573");

	/**
	 * @Goals: merge same activities which are one after the other.
	 *
	 * @Steps: - Create finished product. - Formulate finished product. - After 3
	 *         hours, formulate again the finished product. - After 6 hours,
	 *         formulate again the finished product.
	 *
	 * @Results: System will just keep one formulation activity.
	 */
	@Test
	public void mergeActivitySuccessorTest() {

		// Create FP
		NodeRef finishedProductNodeRef = createFinishedProduct();

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, -13);

		List<ActivityListDataItem> customActivities = new ArrayList<>();
		List<ActivityListDataItem> activities = new ArrayList<>();

		for (int i = 0; i < 3; i++) {
			// Formulate finished product
			inWriteTx(() -> {
				productService.formulate(finishedProductNodeRef);
				return null;
			});

			activities = getActivities(finishedProductNodeRef, SORT_MAP);

			activities.forEach((nodeRef) -> {
				if (!customActivities.contains(nodeRef)) {
					cal.add(Calendar.HOUR, +3);
					changeCreatedNodeDate(nodeRef, cal.getTime());
					customActivities.add(nodeRef);
				}
			});
		}

		List<ActivityType> activityTypes = new ArrayList<>();
		getActivities(finishedProductNodeRef, SORT_MAP)
				.forEach((activity) -> activityTypes.add(activity.getActivityType()));

		// Make sure that we kept one formulation activity
		assertEquals("Formulation Activities number = 1", 1,
				Collections.frequency(activityTypes, ActivityType.Formulation));

		// Confirm if the last activity is of type formulation
		assertEquals("Last activity type is formulation: ", ActivityType.Formulation,
				getActivities(finishedProductNodeRef, SORT_MAP).get(1).getActivityType());

	}

	/**
	 * @Goals: merge same activity in last hour
	 *
	 * @Steps: - Create finished product. - Change finished product props. -
	 *         Formulate the finished product. - Modify again finished product
	 *         props.
	 *
	 * @Results: System will generate 3 activities : one for FP creation and second
	 *           for formulation and last for props modification.
	 */
	@Test
	public void mergeSameActivityInLastHourTest() {

		NodeRef finishedProductNodeRef = createFinishedProduct();

		inWriteTx(() -> {
			// Modify finished product props (unit total cost)
			FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(finishedProductNodeRef);
			productData.setUnitTotalCost(1.0);
			alfrescoRepository.save(productData);

			// Formulate finished product
			productService.formulate(productData.getNodeRef());
			return null;
		});

		inWriteTx(() -> {
			// Again modify finished product name props (previous unit total
			// cost)
			FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(finishedProductNodeRef);
			productData.setPreviousUnitTotalCost(2.0);
			alfrescoRepository.save(productData);
			return null;
		});

		List<ActivityListDataItem> actListDataItems = getActivityListDataItems(finishedProductNodeRef);
		actListDataItems.forEach((key) -> {
			key.getActivityData();
		});

		List<ActivityType> activityTypes = new ArrayList<>();
		actListDataItems.forEach((activity) -> {
			activityTypes.add(activity.getActivityType());
		});

		// Make sure that we just generate 3 activities
		assertEquals("Activities number is 3", 3, getActivities(finishedProductNodeRef, null).size());

		// Make sure that we kept one formulation activity
		assertEquals("Formulation Activities number = 1", 1,
				Collections.frequency(activityTypes, ActivityType.Formulation));

		// Make sure that we kept two activities of Entity type
		assertEquals("Formulation Activities number = 2", 2, Collections.frequency(activityTypes, ActivityType.Entity));

	}

	/**
	 * @Goals: Don't merge activities of the first page
	 *
	 * @Steps: - Create finished product. - Add raw-material to finished product. -
	 *         After 2 hours, generate report and formulate. - Change in
	 *         raw-material. - Repeat 50 times the steps 3 and 4.
	 * 
	 *
	 * @Results: System will keep the same 50 first activities.
	 */
	@Test
	public void alwaysKeepSameLastFiftyActivitiesTest() throws InterruptedException {

		NodeRef finishedProductNodeRef = createFinishedProduct();

		inWriteTx(() -> {
			FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(finishedProductNodeRef);
			// generate change activity of type state
			entityActivityService.postStateChangeActivity(productData.getNodeRef(), null, "simulate", "validate");

			return null;
		});

		Calendar cal = Calendar.getInstance();
		for (int i = 0; i < 50; i++) {
			inWriteTx(() -> {
				// Generate report
				entityActivityService.postEntityActivity(finishedProductNodeRef, ActivityType.Report,
						ActivityEvent.Update, null);
				// Formulation
				entityActivityService.postEntityActivity(finishedProductNodeRef, ActivityType.Formulation,
						ActivityEvent.Update, null);

				return null;
			});
			getActivities(finishedProductNodeRef, null).forEach((nodeRef) -> {
				cal.add(Calendar.HOUR, -2);
				changeCreatedNodeDate(nodeRef, cal.getTime());
			});
		}

		List<ActivityListDataItem> activities = getActivities(finishedProductNodeRef, SORT_MAP);
		Collections.reverse(activities);

		List<ActivityListDataItem> firstPageBeforeClean = activities.subList(0, MAX_PAGE);

		// clean activities
		BatchInfo batch = entityActivityService.cleanActivities();
		waitForBatchEnd(batch);

		activities = getActivities(finishedProductNodeRef, SORT_MAP);
		Collections.reverse(activities);
		List<ActivityListDataItem> firstPageAfterClean = activities.subList(0, MAX_PAGE);

		// Make sure that we have more than one page
		assertTrue(activities.size() > MAX_PAGE);

		// Make sure that we kept the same first 50 activities
		assertEquals("First page always the same ", firstPageBeforeClean, firstPageAfterClean);

	}

	/**
	 * @Goals: Keep at least one report/formulation per page from the second page
	 *         Also insure that we keep the last one.
	 *
	 * @Steps: - Create finished product - Generate report - After 2 hours,
	 *         Formulate the product - Repeat the steps 2 and 3 many times
	 *
	 * @Results: System will keep one report activity and another formulation
	 *           activity in the second page.
	 */
	@Test
	public void keepOneActivityOfTypeReportAndFormulationPerPageTest() throws InterruptedException {

		// Create FP
		NodeRef finishedProductNodeRef = createFinishedProduct();

		Calendar cal = Calendar.getInstance();
		for (int i = 0; i < 100; i++) {
			inWriteTx(() -> {
				// Generate report
				entityActivityService.postEntityActivity(finishedProductNodeRef, ActivityType.Report,
						ActivityEvent.Update, null);
				// Formulation
				entityActivityService.postEntityActivity(finishedProductNodeRef, ActivityType.Formulation,
						ActivityEvent.Update, null);

				return null;
			});

			getActivities(finishedProductNodeRef, null).forEach((nodeRef) -> {
				cal.add(Calendar.HOUR, -2);
				changeCreatedNodeDate(nodeRef, cal.getTime());

			});
		}

		// activities number before clean
		assertEquals("number activities = 201", 201, getActivities(finishedProductNodeRef, null).size());

		BatchInfo batch = entityActivityService.cleanActivities();
		waitForBatchEnd(batch);

		List<ActivityListDataItem> activities = getActivities(finishedProductNodeRef, SORT_MAP);

		// activities number after clean
		assertEquals("number formulation activities in second page = 53", 53, activities.size());

		Collections.reverse(activities);
		activities = activities.subList(50, 52);

		List<ActivityType> activityTypes = new ArrayList<>();
		activities.forEach((activity) -> activityTypes.add(activity.getActivityType()));

		// Make sure that we keep one formulation activity in the second page
		assertEquals("formulation number in the second page ", 1,
				Collections.frequency(activityTypes, ActivityType.Formulation));
		// Make sure that we keep one formulation activity in second page
		assertEquals("formulation number in the second page ", 1,
				Collections.frequency(activityTypes, ActivityType.Report));

	}

	/**
	 * @Goals: Don't merge activities of type : comments, version, merge, content
	 *         and state.
	 *
	 * @Steps: - Create finished product - Change date time for each activity -
	 *         Generate one action of ( comments or version or merge or content or
	 *         state) - Repeat the step 2 and 3 till you create 50 activities. -
	 *         Change date another time - Do random action of
	 *         formulation/report/data-list till we get more than 100 activities -
	 *         Assert that we keep activities after clean
	 *
	 * @Results: System will keep all comments, merge, version, content and state
	 *           activities.
	 */
	@Test
	public void dontMergeCommentAndVersionAndStateAndContentActivitiesTest() throws InterruptedException {

		// Create FP
		NodeRef finishedProductNodeRef = createFinishedProduct();

		Calendar cal = Calendar.getInstance();

		inWriteTx(() -> {
			// Comments
			commentService.createComment(finishedProductNodeRef, COMMENT_TITLE_TEXT, COMMENT_DATA_TEXT, false);
			commentService.createComment(finishedProductNodeRef, COMMENT_TITLE_TEXT, COMMENT_DATA_TEXT, false);
			// Version
			entityActivityService.postVersionActivity(finishedProductNodeRef, ENTITY_NODEREF, "1.0");
			entityActivityService.postVersionActivity(finishedProductNodeRef, ENTITY_NODEREF, "2.0");
			// state
			entityActivityService.postStateChangeActivity(finishedProductNodeRef, null, "simulate", "validate");
			entityActivityService.postStateChangeActivity(finishedProductNodeRef, null, "validate", "refused");
			// Content
			entityActivityService.postContentActivity(finishedProductNodeRef, finishedProductNodeRef,
					ActivityEvent.Update);
			entityActivityService.postContentActivity(finishedProductNodeRef, finishedProductNodeRef,
					ActivityEvent.Update);

			return null;
		});

		for (int i = 0; i < 25; i++) {
			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
				// Generate report
				entityActivityService.postEntityActivity(finishedProductNodeRef, ActivityType.Report,
						ActivityEvent.Update, null);
				// Formulation
				entityActivityService.postEntityActivity(finishedProductNodeRef, ActivityType.Formulation,
						ActivityEvent.Update, null);
				return null;
			}, false, true);

			int[] hour = { 0 };
			getActivities(finishedProductNodeRef, SORT_MAP).forEach((nodeRef) -> {
				cal.add(Calendar.HOUR_OF_DAY, -hour[0]++);
				changeCreatedNodeDate(nodeRef, cal.getTime());
			});
		}

		List<ActivityType> activityTypesBeforeClean = new ArrayList<>();
		getActivities(finishedProductNodeRef, null)
				.forEach((activity) -> activityTypesBeforeClean.add(activity.getActivityType()));

		// Make sure that we have all comments activities
		assertEquals("Comment activities number = 2", 2,
				Collections.frequency(activityTypesBeforeClean, ActivityType.Comment));
		// Make sure that we have all versions activities
		assertEquals("Version activities number = 2", 2,
				Collections.frequency(activityTypesBeforeClean, ActivityType.Version));
		// Make sure that w have all state activities
		assertEquals("State activities number = 2", 2,
				Collections.frequency(activityTypesBeforeClean, ActivityType.State));
		// Make sure that we have all content activities
		assertEquals("Content activities number = 2", 2,
				Collections.frequency(activityTypesBeforeClean, ActivityType.Content));

		BatchInfo batch = entityActivityService.cleanActivities();
		waitForBatchEnd(batch);

		List<ActivityType> activityTypesAfterClean = new ArrayList<>();
		getActivities(finishedProductNodeRef, null)
				.forEach((activity) -> activityTypesAfterClean.add(activity.getActivityType()));

		// Make sure that we keep comments activities even if they are in the
		// second page
		assertEquals("Comment activities number = 2", 2,
				Collections.frequency(activityTypesAfterClean, ActivityType.Comment));
		// Make sure that we keep version activities
		assertEquals("Version activities number = 2", 2,
				Collections.frequency(activityTypesAfterClean, ActivityType.Version));
		// Make sure that we keep state activities
		assertEquals("State activities number = 2", 2,
				Collections.frequency(activityTypesBeforeClean, ActivityType.State));
		// Make sure that we keep content activities
		assertEquals("Content activities number = 2", 2,
				Collections.frequency(activityTypesBeforeClean, ActivityType.Content));

	}

	/**
	 * @Goals: merge activities by day for the last week.
	 *
	 * @Steps: - Create finished product. - Add SF to finished-product composition
	 *         list. - Generate multiples actions and change their create time.
	 *         which will be between 08h and 18h. - Generate 50 activities of type
	 *         comment which will be activities of the first page.
	 * 
	 * 
	 * @Results: System will keep for each day during the current week the last
	 *           activity in the day taht's mean 18h.
	 * 
	 *           NB. we talk here about activities after the first page (>50).
	 */
	@Test
	public void mergeActivitiesByDayForTheLastWeekTest() throws InterruptedException {
		// Create FP
		NodeRef finishedProductNodeRef = createFinishedProduct();

		List<ActivityListDataItem> activityWithCustomDate = new ArrayList<>();
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		cal.set(Calendar.MINUTE, 0);

		// Create SF
		final NodeRef lSF1NodeRef = inWriteTx(() -> {
			LocalSemiFinishedProductData lSF1 = new LocalSemiFinishedProductData();
			lSF1.setName("SF-1");
			return alfrescoRepository.create(getTestFolderNodeRef(), lSF1).getNodeRef();
		});

		// Generate different actions
		for (int i = 0; i < 20; i++) {
			for (int j = 8; j < 19; j++) {
				// Add SF to finished product (data-list activity)
				inWriteTx(() -> {
					List<CompoListDataItem> compoList = new ArrayList<>();
					/*
					 * compoList.add(new CompoListDataItem(null, null, 1d, 1d, ProductUnit.P, 0d,
					 * DeclarationType.Declare, lSF1NodeRef));
					 */
					compoList.add(CompoListDataItem.build().withQty(1d).withQtyUsed(1d).withUnit(ProductUnit.P)
							.withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(lSF1NodeRef));
					FinishedProductData finishedProduct;
					finishedProduct = ((FinishedProductData) alfrescoRepository.findOne(finishedProductNodeRef));
					finishedProduct.getCompoListView().setCompoList(compoList);
					alfrescoRepository.save(finishedProduct);
					return null;
				});

				inWriteTx(() -> {
					// Generate report
					entityActivityService.postEntityActivity(finishedProductNodeRef, ActivityType.Report,
							ActivityEvent.Update, null);
					// Formulation
					entityActivityService.postEntityActivity(finishedProductNodeRef, ActivityType.Formulation,
							ActivityEvent.Update, null);
					return null;
				});

				cal.set(Calendar.HOUR_OF_DAY, j);

				getActivities(finishedProductNodeRef, null).forEach((activity) -> {
					if (!activityWithCustomDate.contains(activity)) {
						activityWithCustomDate.add(activity);
						changeCreatedNodeDate(activity, cal.getTime());
					}
				});
			}

			cal.add(Calendar.DAY_OF_YEAR, -1);
		}

		// Generate multiple activities of type comment
		for (int i = 0; i < MAX_PAGE; i++) {
			inWriteTx(() -> {
				commentService.createComment(finishedProductNodeRef, COMMENT_TITLE_TEXT, COMMENT_DATA_TEXT, false);
				return null;
			});
		}

		waitForSolr();
		logger.info("Activities number before clean : " + getActivities(finishedProductNodeRef, null).size());

		// clean activities
		BatchInfo batch = entityActivityService.cleanActivities();
		waitForBatchEnd(batch);

		List<ActivityListDataItem> activityNodeRefs = getActivities(finishedProductNodeRef, SORT_MAP);
		Collections.reverse(activityNodeRefs);

		logger.info("Activities number after clean : " + activityNodeRefs.size());

		activityNodeRefs.subList(MAX_PAGE, activityNodeRefs.size()).forEach((activity) -> {
			Date customCreationDate = activity.getCreatedDate();
			Calendar time = Calendar.getInstance();
			time.setTime(customCreationDate);
			ActivityType activityType = activity.getActivityType();
			// make sure that we keep only activities of the last day

			if (activityType.equals(ActivityType.Datalist)) {
				// Make sure we keep the last activity per day
				assertEquals("activity time creation at 18h", 18, time.get(Calendar.HOUR_OF_DAY));
			}

		});

	}

	/**
	 * The legacy Generals tests cases of purge functionality
	 *
	 */

	@Test
	@Deprecated
	public void cleanActivityServiceTest() throws InterruptedException {

		// Create semiFinished product
		NodeRef semiFinishedProductNodeRef = inWriteTx(() -> {
			SemiFinishedProductData semiFinishedProductData = new SemiFinishedProductData();
			semiFinishedProductData.setName("semiFinished-product");
			return alfrescoRepository.create(getTestFolderNodeRef(), semiFinishedProductData).getNodeRef();
		});

		// Create product with ActivityList
		NodeRef productNodeRef = inWriteTx(() -> {
			// Create product
			FinishedProductData productData = new FinishedProductData();
			productData.setParentNodeRef(getTestFolderNodeRef());
			productData.setName("activity-test-product");
			List<CompoListDataItem> compoList = new ArrayList<>();
			/*
			 * compoList.add(new CompoListDataItem(null, null, 1d, 1d, ProductUnit.P, 0d,
			 * DeclarationType.Declare, semiFinishedProductNodeRef));
			 */
			compoList.add(CompoListDataItem.build().withQty(1d).withQtyUsed(1d).withUnit(ProductUnit.P).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(semiFinishedProductNodeRef));
			productData.getCompoListView().setCompoList(compoList);
			alfrescoRepository.save(productData);

			// Add Activity List to product
			NodeRef listContainerNodeRef = entityListDAO.getListContainer(productData.getNodeRef());
			entityListDAO.createList(listContainerNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);

			return productData.getNodeRef();
		});

		assertEquals("Check update Activity", 2, getActivities(productNodeRef, null).size());

		// Add some activities to product
		inWriteTx(() -> {
			// Get product
			FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(productNodeRef);

			for (int i = 0; i < MAX_PAGE; i++) {
				commentService.createComment(productNodeRef, COMMENT_TITLE_TEXT, COMMENT_DATA_TEXT, false);
			}

			productData.getCostList().add(new CostListDataItem(null, 10d, "kg", null, costs.get(0), false));
			alfrescoRepository.save(productData);

			return null;
		});

		assertEquals("Check generated Activity", MAX_PAGE + 3, getActivities(productNodeRef, null).size());

		BatchInfo batch = entityActivityService.cleanActivities();
		waitForBatchEnd(batch);

		assertEquals("Check generated Activity", MAX_PAGE + 3, getActivities(productNodeRef, null).size());

		for (int i = 0; i < 50; i++) {
			inWriteTx(() -> {
				FinishedProductData productData = (FinishedProductData) alfrescoRepository.findOne(productNodeRef);
				// Formulation
				productService.formulate(productNodeRef);
				// DataList
				productData.getCostList().add(new CostListDataItem(null, 10d, "kg", null, costs.get(0), false));
				// State
				if (productData.getState().equals(SystemState.Simulation)) {
					productData.setState(SystemState.Refused);
				} else {
					productData.setState(SystemState.Simulation);
				}

				alfrescoRepository.save(productData);
				return null;
			});

			changeCreatedDate(productNodeRef);
		}

		int activitiesBeforeClean = getActivities(productNodeRef, null).size();

		assertNotNull("Activities before clean not null ", activitiesBeforeClean);
		assertTrue("activities number before clean > 50 : ", activitiesBeforeClean > MAX_PAGE);

		waitForSolr();
		// Clean Activities
		batch = entityActivityService.cleanActivities();
		waitForBatchEnd(batch);

		int activitiesAfterClean = getActivities(productNodeRef, null).size();
		assertTrue(activitiesBeforeClean >= activitiesAfterClean);
		assertTrue(activitiesAfterClean > MAX_PAGE);

	}

	/**
	 * Create finished-product with an activity list
	 *
	 * @return finished-product nodeRef
	 *
	 */
	private NodeRef createFinishedProduct() {

		NodeRef productNodeRef = inWriteTx(() -> {
			// Create finished product
			FinishedProductData productData = new FinishedProductData();
			productData.setParentNodeRef(getTestFolderNodeRef());
			productData.setName("finished-product");
			alfrescoRepository.save(productData);

			// Add Activity List to product
			NodeRef listContainerNodeRef = entityListDAO.getListContainer(productData.getNodeRef());
			entityListDAO.createList(listContainerNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);

			return productData.getNodeRef();
		});

		return productNodeRef;
	}

	/**
	 * Change node time creation to a custom date
	 *
	 * @param activity   nodeRef of entity
	 *
	 * @param customDate new date creation of entity
	 *
	 */
	private void changeCreatedNodeDate(ActivityListDataItem activity, Date customDate) {
		inWriteTx(() -> {

			Map<String, Serializable> values = new HashMap<>();

			values.put(ActivityAuditPlugin.PROP_CM_CREATED, ISO8601DateFormat.format(customDate));

			updateAuditEntry(activityAuditPlugin, activity.getId(), (Long) customDate.getTime(), values);

			return null;
		});
	}

	public void deleteAuditEntries(DatabaseAuditPlugin plugin, Long fromId, Long toId) {
		auditComponent.deleteAuditEntriesByIdRange(plugin.getAuditApplicationId(), fromId, toId);
	}

	private void updateAuditEntry(DatabaseAuditPlugin plugin, Long id, Long time, Map<String, Serializable> values) {

		AuditEntry auditEntry = audit.getAuditEntry(plugin.getAuditApplicationId(), id, null);

		AuditApplication application = auditModelRegistry.getAuditApplicationByKey(plugin.getAuditApplicationId());

		Long applicationId = application.getApplicationId();

		deleteAuditEntries(plugin, auditEntry.getId(), auditEntry.getId() + 1);

		for (Entry<String, Serializable> entry : values.entrySet()) {
			auditEntry.getValues().put("/" + plugin.getAuditApplicationId() + "/" + plugin.getAuditApplicationPath()
					+ "/" + entry.getKey() + "/value", entry.getValue());
		}

		auditDAO.createAuditEntry(applicationId, time, AuthenticationUtil.getFullyAuthenticatedUser(),
				auditEntry.getValues());

	}

	/**
	 * Legacy method to change <code>@cm:created</code> of activities for the passed
	 * entity
	 *
	 * @param nodeRef the entity nodeRef
	 *
	 */
	@Deprecated
	private void changeCreatedDate(NodeRef nodeRef) {

		Calendar thisMonth = Calendar.getInstance();
		thisMonth.setTime(new Date());

		// Change activity created time
		inWriteTx(() -> {
			policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
			NodeRef activityListNodeRef = getActivityList(nodeRef);
			if (activityListNodeRef != null) {
				for (NodeRef activityListItemNodeRef : entityListDAO.getListItems(activityListNodeRef,
						BeCPGModel.TYPE_ACTIVITY_LIST)) {
					nodeService.setProperty(activityListItemNodeRef, ContentModel.PROP_CREATED, thisMonth.getTime());
					thisMonth.add(Calendar.DAY_OF_MONTH, -1);
				}
			}
			policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
			return null;
		});

	}

}
