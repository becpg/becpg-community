package fr.becpg.repo.quality;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Test;

import fr.becpg.model.QualityModel;
import fr.becpg.repo.quality.data.ControlPlanData;
import fr.becpg.repo.quality.data.ControlPointData;
import fr.becpg.repo.quality.data.QualityControlData;
import fr.becpg.repo.quality.data.dataList.ControlDefListDataItem;
import fr.becpg.repo.quality.data.dataList.SamplingDefListDataItem;
import fr.becpg.repo.quality.data.dataList.SamplingListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.test.BeCPGTestHelper;
import fr.becpg.test.RepoBaseTestCase;

public class QualityControlTest extends RepoBaseTestCase {

	private static final long HOUR = 3600 * 1000; // in milli-seconds.

	@Resource
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	

	private NodeRef controlStepNodeRef;
	private NodeRef methodNodeRef;
	private NodeRef controlPointNodeRef;
	private NodeRef qualityControlNodeRef;
	private NodeRef controlPlanNodeRef;


	private void createControlPlan(NodeRef testFolderNodeRef) {

		// create method
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		String name = "Method";
		properties.put(ContentModel.PROP_NAME, name);
		methodNodeRef = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
				QualityModel.TYPE_CONTROL_METHOD, properties).getChildRef();

		// create control step
		properties.clear();
		name = "Step";
		properties.put(ContentModel.PROP_NAME, name);
		controlStepNodeRef = nodeService.createNode(testFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
				QualityModel.TYPE_CONTROL_STEP, properties).getChildRef();

		// create control point
		ControlPointData controlPointData = new ControlPointData();
		controlPointData.setName("Control point");
		List<ControlDefListDataItem> controlDefList = new ArrayList<ControlDefListDataItem>();
		controlDefList.add(new ControlDefListDataItem(null, "Nutritionnelle", null, null, true, methodNodeRef, nuts));
		controlPointData.setControlDefList(controlDefList);
		controlPointNodeRef = alfrescoRepository.create(testFolderNodeRef, controlPointData).getNodeRef();

		// // create group
		// Set<String> zones = new HashSet<String>();
		// zones.add(AuthorityService.ZONE_APP_DEFAULT);
		// zones.add(AuthorityService.ZONE_APP_SHARE);
		// zones.add(AuthorityService.ZONE_AUTH_ALFRESCO);
		// String group = "groupQual";
		//
		// if(!authorityService.authorityExists(PermissionService.GROUP_PREFIX +
		// group)){
		// logger.debug("create group: " + group);
		// authorityService.createAuthority(AuthorityType.GROUP, group, group,
		// zones);
		// }
		// NodeRef controlingGroup = authorityService.getA

		// create control plan
		ControlPlanData controlPlanData = new ControlPlanData();
		controlPlanData.setName("Control plan");
		List<SamplingDefListDataItem> samplingDefList = new ArrayList<SamplingDefListDataItem>();
		samplingDefList.add(new SamplingDefListDataItem(2, 1, "/4heures", controlPointNodeRef, controlStepNodeRef, null));
		controlPlanData.setSamplingDefList(samplingDefList);
		controlPlanNodeRef = alfrescoRepository.create(testFolderNodeRef, controlPlanData).getNodeRef();
	}

	private void createQualityControl(NodeRef testFolderNodeRef, List<NodeRef> controlPlansNodeRef, NodeRef productNodeRef) {

		QualityControlData qualityControlData = new QualityControlData();
		qualityControlData.setName("Quality control");
		qualityControlData.setBatchStart(new Date());
		qualityControlData.setBatchDuration(8);
		qualityControlData.setBatchId("12247904");
		qualityControlData.setOrderId("2394744");
		qualityControlData.setProduct(productNodeRef);
		qualityControlData.setControlPlans(controlPlansNodeRef);

		qualityControlNodeRef = alfrescoRepository.create(testFolderNodeRef, qualityControlData).getNodeRef();

		// TODO : add a policy...
		// qualityControlService.createSamplingList(qualityControlNodeRef,
		// controlPlansNodeRef.get(0));

	}

	@Test
	public void testCreateQualityControl() {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				createControlPlan(testFolderNodeRef);
				List<NodeRef> controlPlansNodeRef = new ArrayList<NodeRef>();
				controlPlansNodeRef.add(controlPlanNodeRef);

				NodeRef productNodeRef = BeCPGTestHelper.createRawMaterial(testFolderNodeRef, "Raw material");

				createQualityControl(testFolderNodeRef, controlPlansNodeRef, productNodeRef);

				return null;

			}
		}, false, true);

		// checks
		QualityControlData qualityControlData = (QualityControlData) alfrescoRepository.findOne(qualityControlNodeRef);
		assertNotNull("Check QC exists", qualityControlData);
		assertNotNull("Check Sample list", qualityControlData.getSamplingList());
		assertEquals("6 samples", 6, qualityControlData.getSamplingList().size());
		assertSame("6 samples", 6, qualityControlData.getSamplesCounter());
		int checks = 0;

		for (SamplingListDataItem sl : qualityControlData.getSamplingList()) {

			if (sl.getSampleId().equals("12247904/1")) {
				assertEquals("check control point", controlPointNodeRef, sl.getControlPoint());
				assertEquals("check control step", controlStepNodeRef, sl.getControlStep());
				assertEquals("check state", null, sl.getSampleState());
				assertEquals("check date", qualityControlData.getBatchStart(), sl.getDateTime());
				checks++;
			} else if (sl.getSampleId().equals("12247904/2")) {
				assertEquals("check control point", controlPointNodeRef, sl.getControlPoint());
				assertEquals("check control step", controlStepNodeRef, sl.getControlStep());
				assertEquals("check state", null, sl.getSampleState());
				assertEquals("check date", qualityControlData.getBatchStart(), sl.getDateTime());
				checks++;
			} else if (sl.getSampleId().equals("12247904/3")) {
				assertEquals("check control point", controlPointNodeRef, sl.getControlPoint());
				assertEquals("check control step", controlStepNodeRef, sl.getControlStep());
				assertEquals("check state", null, sl.getSampleState());
				assertEquals("check date", new Date(qualityControlData.getBatchStart().getTime() + 4 * HOUR), sl.getDateTime());
				checks++;
			} else if (sl.getSampleId().equals("12247904/4")) {
				assertEquals("check control point", controlPointNodeRef, sl.getControlPoint());
				assertEquals("check control step", controlStepNodeRef, sl.getControlStep());
				assertEquals("check state", null, sl.getSampleState());
				assertEquals("check date", new Date(qualityControlData.getBatchStart().getTime() + 4 * HOUR), sl.getDateTime());
				checks++;
			}
		}

		assertEquals(4, checks);
	}
}
