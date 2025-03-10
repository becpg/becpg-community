package fr.becpg.test.repo.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.activity.data.ActivityListDataItem;
import fr.becpg.repo.activity.helper.AuditActivityHelper;
import fr.becpg.repo.audit.model.AuditQuery;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.impl.ActivityAuditPlugin;
import fr.becpg.repo.audit.service.BeCPGAuditService;
import fr.becpg.repo.product.data.ClientData;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class DetailActivityContentIT extends AbstractFinishedProductTest {

	private static Log logger = LogFactory.getLog(DetailActivityContentIT.class);

	@Autowired
	protected AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	
	@Autowired
	protected BeCPGAuditService beCPGAuditService;
	
	@Autowired
	protected AlfrescoRepository<ClientData> clientRepository;

	protected NodeRef getActivityList(NodeRef productNodeRef) {
		NodeRef listNodeRef = null;
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(productNodeRef);
		if (listContainerNodeRef != null) {
			listNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);
		}
		return listNodeRef;

	}

	protected List<ActivityListDataItem> getActivities(NodeRef entityNodeRef, Map<String, Boolean> sortMap) {
		AuditQuery auditFilter = AuditQuery.createQuery().asc(false).dbAsc(false)
				.sortBy(ActivityAuditPlugin.PROP_CM_CREATED).filter(ActivityAuditPlugin.ENTITY_NODEREF, entityNodeRef.toString());

		return transactionService.getRetryingTransactionHelper().doInTransaction(
				() -> beCPGAuditService.listAuditEntries(AuditType.ACTIVITY, auditFilter).stream()
						.map(json -> AuditActivityHelper.parseActivity(json)).collect(Collectors.toList()),
				false, true);

	}

	private NodeRef createFinishedProduct() {
		// Create finished composite product with ActivityList
		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			// Create product
			FinishedProductData productData = new FinishedProductData();
			productData.setParentNodeRef(getTestFolderNodeRef());
			productData.setName("Finished Product Test");
			productData.setErpCode("11111");

			alfrescoRepository.save(productData);

			// Add Activity List to product
			NodeRef listContainerNodeRef = entityListDAO.getListContainer(productData.getNodeRef());
			entityListDAO.createList(listContainerNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);

			return productData.getNodeRef();

		}, false, true);
	}

	@Test
	public void checkPropChangesActivityTest() {

		NodeRef finishedProductNodeRef = createFinishedProduct();

		// Check if just one activity was created
		assertEquals("Check if No Activity", 1, getActivities(finishedProductNodeRef, null).size());

		// Change erp code of finished product
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			nodeService.setProperty(finishedProductNodeRef, BeCPGModel.PROP_ERP_CODE, "22222");
			return null;
		}, false, true);

		// Check if an activity has been created
		assertEquals("Activity 2: erpCode modification", 2, getActivities(finishedProductNodeRef, null).size());

		// Check activity data
		ActivityListDataItem activity = getActivities(finishedProductNodeRef, null).get(0);
		String activityData = activity.getActivityData();
		assertNotNull("Activity data", activityData);

		try {
			JSONObject data = new JSONObject(activityData);
			if (data.getJSONArray("properties") != null && data.getJSONArray("properties").length() > 0) {
				JSONObject dataProp = data.getJSONArray("properties").getJSONObject(0);
				if (dataProp != null) {
					assertEquals("Check erpCode before modification", "11111", dataProp.get("before").toString());
					assertEquals("Check erpCode after modification", "22222", dataProp.get("after").toString());
				}
			}

		} catch (JSONException err) {
			logger.error(err.toString());
			fail("Error activity data for property change :" + err.getMessage());
		}

	}

	@Test
	public void checkAssocChangesActivityTest() {

		NodeRef finishedProductNodeRef = createFinishedProduct();

		// Check if just one activity was created
		assertEquals("Check if No Activity", 1, getActivities(finishedProductNodeRef, null).size());

		// Add Client to product
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			ClientData client1 = new ClientData();
			client1.setName("Client1");
			client1 = clientRepository.create(getTestFolderNodeRef(), client1);
			List<NodeRef> clients = new ArrayList<NodeRef>();
			clients.add(client1.getNodeRef());
			nodeService.setAssociations(finishedProductNodeRef, PLMModel.ASSOC_CLIENTS, clients);
			return client1.getNodeRef();
		}, false, true);

		// Check if an activity has been created
		assertEquals("Activity 2: Create client association", 2, getActivities(finishedProductNodeRef, null).size());

		// Change client of finished product
		NodeRef client2NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			ClientData client2 = new ClientData();
			client2.setName("Client2");
			client2 = clientRepository.create(getTestFolderNodeRef(), client2);
			List<NodeRef> clients2 = new ArrayList<NodeRef>();
			clients2.add(client2.getNodeRef());

			List<AssociationRef> clientAssocs = nodeService.getTargetAssocs(finishedProductNodeRef,  PLMModel.ASSOC_CLIENTS);
			for(AssociationRef clientAssoc : clientAssocs) {
				nodeService.removeAssociation(clientAssoc.getSourceRef(), clientAssoc.getTargetRef(), PLMModel.ASSOC_CLIENTS);
			}

			nodeService.setAssociations(finishedProductNodeRef, PLMModel.ASSOC_CLIENTS, clients2);
			return client2.getNodeRef();
		}, false, true);
		
		assertEquals("Activity 2: Modify client association", 2, getActivities(finishedProductNodeRef, null).size());

		// Check activity data
		ActivityListDataItem activity = getActivities(finishedProductNodeRef, null).get(0);
		String activityData = activity.getActivityData();
		assertNotNull("Activity data", activityData);

		try {
			JSONObject data = new JSONObject(activityData);
			if (data.getJSONArray("properties") != null && data.getJSONArray("properties").length() > 0) {
				JSONObject dataProp = data.getJSONArray("properties").getJSONObject(0);
				if (dataProp != null) {
					assertEquals("Check client title modification", PLMModel.ASSOC_CLIENTS.toString(), dataProp.getString("title"));
					assertEquals("Check client before modification", "[]" ,dataProp.getJSONArray("before").toString());
					assertEquals("Check client after modification", "[\"("+client2NodeRef+", Client2)\"]",dataProp.getJSONArray("after").toString());
				}
			}

		} catch (JSONException err) {
			logger.error(err.toString());
			fail("Error activity data for property change :" + err.getMessage());
		}
	}

	@Test
	public void checkEntityDatalistActivityTest() {

		final NodeRef lSF1NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			LocalSemiFinishedProductData lSF1 = new LocalSemiFinishedProductData();
			lSF1.setName("Local semi finished 1"); 
			return alfrescoRepository.create(getTestFolderNodeRef(), lSF1).getNodeRef();
		}, false, true);

		final NodeRef finishedProductNodeRef = createFinishedProduct();

		assertEquals("Check create Activity", 1, getActivities(finishedProductNodeRef, null).size());

		//Add composition to product
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			List<CompoListDataItem> compoList = new ArrayList<>(); 
			compoList.add(CompoListDataItem.build().withParent(null).withQty(1d).withQtyUsed(1d).withUnit(ProductUnit.P).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(lSF1NodeRef));
			FinishedProductData finishedProduct = ((FinishedProductData)alfrescoRepository.findOne(finishedProductNodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);
			alfrescoRepository.save(finishedProduct);
			return null;
		}, false, true);

		assertEquals("Check add compoList item", 2, getActivities(finishedProductNodeRef, null).size()); 

		//Change composition qty
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			FinishedProductData finishedProduct = ((FinishedProductData)alfrescoRepository.findOne(finishedProductNodeRef));
			finishedProduct.getCompoListView().getCompoList().get(0).setQty(2d);
			alfrescoRepository.save(finishedProduct);
			return null;
		}, false, true);
		
		assertEquals("Check update compoList item ", 3, getActivities(finishedProductNodeRef, null).size()); 

		// Check activity data
		ActivityListDataItem activity = getActivities(finishedProductNodeRef, null).get(0);
		String activityData = activity.getActivityData();
		assertNotNull("Activity data", activityData);

		try {
			JSONObject data = new JSONObject(activityData);
			if (data.getJSONArray("properties") != null && data.getJSONArray("properties").length() > 0) {
				JSONObject dataProp = data.getJSONArray("properties").getJSONObject(0);
				if (dataProp != null) {
					assertEquals("Check compo qty modification", "1",dataProp.get("before").toString());
					assertEquals("Check compo qty modification", "2", dataProp.get("after").toString());
				}
			}

		} catch (JSONException err) {
			logger.error(err.toString());
			fail("Error activity data for property change :" + err.getMessage());
		}
	}


}