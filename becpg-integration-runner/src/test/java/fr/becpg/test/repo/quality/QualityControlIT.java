/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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
package fr.becpg.test.repo.quality;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.QualityModel;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.quality.data.ControlPlanData;
import fr.becpg.repo.quality.data.ControlPointData;
import fr.becpg.repo.quality.data.QualityControlData;
import fr.becpg.repo.quality.data.QualityControlState;
import fr.becpg.repo.quality.data.dataList.ControlDefListDataItem;
import fr.becpg.repo.quality.data.dataList.ControlListDataItem;
import fr.becpg.repo.quality.data.dataList.SamplingDefListDataItem;
import fr.becpg.repo.quality.data.dataList.SamplingListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.PLMBaseTestCase;

public class QualityControlIT extends PLMBaseTestCase {

	private static final Log logger = LogFactory.getLog(QualityControlIT.class);

	private static final long HOUR = 3600 * 1000; // in milli-seconds.

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	private NodeRef controlStepNodeRef;
	private NodeRef methodNodeRef;
	private NodeRef controlPointNodeRef;
	private NodeRef controlPointNodeRef2;
	private NodeRef qualityControlNodeRef;
	private NodeRef controlPlanNodeRef;

	private void createControlPlan() {

		// create method
		Map<QName, Serializable> properties = new HashMap<>();
		String name = "Method";
		properties.put(BeCPGModel.PROP_CHARACT_NAME, name);
		methodNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), QualityModel.TYPE_CONTROL_METHOD,
				properties).getChildRef();

		// create control step
		properties.clear();
		name = "Step";
		properties.put(BeCPGModel.PROP_CHARACT_NAME, name);
		controlStepNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), QualityModel.TYPE_CONTROL_STEP,
				properties).getChildRef();

		// create control point
		ControlPointData controlPointData = new ControlPointData();
		controlPointData.setName("Control point");
		List<ControlDefListDataItem> controlDefList = new ArrayList<>();
		controlDefList.add(new ControlDefListDataItem(null, "bcpg_nutList", null, null, true, methodNodeRef, nuts));
		controlPointData.setControlDefList(controlDefList);
		controlPointNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), controlPointData).getNodeRef();

		ControlPointData controlPointData2 = new ControlPointData();
		controlPointData.setName("Control point2");
		List<ControlDefListDataItem> controlDefList2 = new ArrayList<>();
		controlDefList2.add(new ControlDefListDataItem(null, "bcpg_ingList", null, null, true, methodNodeRef, ings));
		controlPointData2.setControlDefList(controlDefList2);
		controlPointNodeRef2 = alfrescoRepository.create(getTestFolderNodeRef(), controlPointData).getNodeRef();

		// create control plan
		ControlPlanData controlPlanData = new ControlPlanData();
		controlPlanData.setName("Control plan");
		List<SamplingDefListDataItem> samplingDefList = new ArrayList<>();
		samplingDefList.add(new SamplingDefListDataItem(2, 1, "/4hours", controlPointNodeRef, controlStepNodeRef, null,
				null, null, "Reaction"));
		SamplingDefListDataItem samplingDefListDataItem2 = new SamplingDefListDataItem(2, 1, "/4hours",
				controlPointNodeRef2, controlStepNodeRef, null, null, null, "Reaction");
		samplingDefListDataItem2.setFreqText("1M,3M,8M");
		samplingDefListDataItem2.setQty(1);
		samplingDefList.add(samplingDefListDataItem2);

		controlPlanData.setSamplingDefList(samplingDefList);
		controlPlanNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), controlPlanData).getNodeRef();
	}

	private void createQualityControl(List<NodeRef> controlPlansNodeRef, NodeRef productNodeRef) {

		QualityControlData qualityControlData = new QualityControlData();
		qualityControlData.setName("Quality control");
		qualityControlData.setBatchStart(new Date());
		qualityControlData.setBatchDuration(8);
		qualityControlData.setBatchId("12247904");
		qualityControlData.setOrderId("2394744");
		qualityControlData.setProduct(productNodeRef);
		qualityControlData.setControlPlans(controlPlansNodeRef);

		qualityControlNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), qualityControlData).getNodeRef();

	}

	@Test
	public void testCreateQualityControl() {

		inWriteTx(() -> {

			createControlPlan();
			List<NodeRef> controlPlansNodeRef = new ArrayList<>();
			controlPlansNodeRef.add(controlPlanNodeRef);

			NodeRef productNodeRef = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "Raw material");

			createQualityControl(controlPlansNodeRef, productNodeRef);

			return null;

		});

		inWriteTx(() -> {

			// check samples
			QualityControlData qualityControlData = (QualityControlData) alfrescoRepository
					.findOne(qualityControlNodeRef);
			assertNotNull("Check QC exists", qualityControlData);
			assertNotNull("Check Sample list", qualityControlData.getSamplingList());
			assertEquals("9 samples", 9, qualityControlData.getSamplingList().size());
			assertSame("9 samples", 9, qualityControlData.getSamplesCounter());
			int checks = 0;

			for (SamplingListDataItem sl : qualityControlData.getSamplingList()) {
				Calendar cal = Calendar.getInstance();
				switch (sl.getSampleId()) {
				case "12247904/0":
					assertEquals("check control point", controlPointNodeRef, sl.getControlPoint());
					assertEquals("check control step", controlStepNodeRef, sl.getControlStep());
					assertEquals("check state", null, sl.getSampleState());
					assertEquals("check date", qualityControlData.getBatchStart(), sl.getDateTime());
					checks++;
					break;
				case "12247904/1":
					assertEquals("check control point", controlPointNodeRef, sl.getControlPoint());
					assertEquals("check control step", controlStepNodeRef, sl.getControlStep());
					assertEquals("check state", null, sl.getSampleState());
					assertEquals("check date", qualityControlData.getBatchStart(), sl.getDateTime());
					checks++;
					break;
				case "12247904/2":
					assertEquals("check control point", controlPointNodeRef, sl.getControlPoint());
					assertEquals("check control step", controlStepNodeRef, sl.getControlStep());
					assertEquals("check state", null, sl.getSampleState());
					assertEquals("check date", new Date(qualityControlData.getBatchStart().getTime() + (4 * HOUR)),
							sl.getDateTime());
					checks++;
					break;
				case "12247904/3":
					assertEquals("check control point", controlPointNodeRef, sl.getControlPoint());
					assertEquals("check control step", controlStepNodeRef, sl.getControlStep());
					assertEquals("check state", null, sl.getSampleState());
					assertEquals("check date", new Date(qualityControlData.getBatchStart().getTime() + (4 * HOUR)),
							sl.getDateTime());
					checks++;
					break;
				case "12247904/6":
					assertEquals("check control point", controlPointNodeRef2, sl.getControlPoint());
					assertEquals("check control step", controlStepNodeRef, sl.getControlStep());
					assertEquals("check state", null, sl.getSampleState());
					cal.setTime(qualityControlData.getBatchStart());
					cal.add(Calendar.MONTH, 1);
					assertEquals("check date", cal.getTime(), sl.getDateTime());
					checks++;
					break;
				case "12247904/7":
					assertEquals("check control point", controlPointNodeRef2, sl.getControlPoint());
					assertEquals("check control step", controlStepNodeRef, sl.getControlStep());
					assertEquals("check state", null, sl.getSampleState());
					cal.setTime(qualityControlData.getBatchStart());
					cal.add(Calendar.MONTH, 3);
					assertEquals("check date", cal.getTime(), sl.getDateTime());
					checks++;
					break;
				case "12247904/8":
					assertEquals("check control point", controlPointNodeRef2, sl.getControlPoint());
					assertEquals("check control step", controlStepNodeRef, sl.getControlStep());
					assertEquals("check state", null, sl.getSampleState());
					cal.setTime(qualityControlData.getBatchStart());
					cal.add(Calendar.MONTH, 8);
					assertEquals("check date", cal.getTime(), sl.getDateTime());
					checks++;
					break;
				}

			}

			assertEquals(7, checks);

			// check controlList
			List<ControlListDataItem> controlList = qualityControlData.getControlList();

			assertEquals(9 * 10, controlList.size());
			boolean isFirstCLOfSample3 = true;

			// fill controlList (sample1)
			logger.info("fill controlList");
			for (ControlListDataItem cl : controlList) {
				switch (cl.getSampleId()) {
				case "12247904/0":
					cl.setState(QualityControlState.Compliant);
					alfrescoRepository.save(cl);
					break;
				case "12247904/1":
					cl.setState(QualityControlState.NonCompliant);
					alfrescoRepository.save(cl);
					break;
				case "12247904/2":
					if (isFirstCLOfSample3) {
						cl.setState(QualityControlState.NonCompliant);
					} else {
						cl.setState(QualityControlState.Compliant);
					}
					alfrescoRepository.save(cl);
					break;
				}
			}

			return null;

		});

		inWriteTx(() -> {

			// check samples
			QualityControlData qualityControlData = (QualityControlData) alfrescoRepository
					.findOne(qualityControlNodeRef);
			int checks = 0;
			Date nextAnalysisDate = null;

			for (SamplingListDataItem sl : qualityControlData.getSamplingList()) {
				switch (sl.getSampleId()) {
				case "12247904/0":
					assertEquals("check state", QualityControlState.Compliant, sl.getSampleState());
					checks++;
					break;
				case "12247904/1":
					assertEquals("check state", QualityControlState.NonCompliant, sl.getSampleState());
					checks++;
					break;
				case "12247904/2":
					assertEquals("check state", QualityControlState.NonCompliant, sl.getSampleState());
					checks++;
					break;
				case "12247904/3":
					assertEquals("check state", null, sl.getSampleState());
					nextAnalysisDate = sl.getDateTime();
					checks++;
					break;
				}
			}

			assertEquals(4, checks);

			// check next analysis data
			assertEquals("check next analysis date", nextAnalysisDate, qualityControlData.getNextAnalysisDate());
			return null;

		});

	}

	@Test
	public void testCreateQualityControlOnProduct() {

		logger.info("testCreateQualityControl");

		final NodeRef productNodeRef = inWriteTx(() -> {

			NodeRef rawMaterialNodeRef = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "Raw material");
			RawMaterialData rawMaterialData = (RawMaterialData) alfrescoRepository.findOne(rawMaterialNodeRef);

			// create control point
			ControlPointData controlPointData = new ControlPointData();
			controlPointData.setName("Control point");
			List<ControlDefListDataItem> controlDefList = new ArrayList<>();
			controlDefList.add(new ControlDefListDataItem(null, "bcpg_nutList", null, null, true, methodNodeRef, nuts));
			controlPointData.setControlDefList(controlDefList);
			NodeRef controlPointNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), controlPointData)
					.getNodeRef();

			SamplingListDataItem sl = new SamplingListDataItem();
			sl.setDateTime(new Date());
			sl.setControlPoint(controlPointNodeRef);
			sl.setSampleId("Test1");
			NodeRef listContainerNodeRef = alfrescoRepository.getOrCreateDataListContainer(rawMaterialData);
			NodeRef listNodeRef = entityListDAO.createList(listContainerNodeRef, QualityModel.TYPE_SAMPLING_LIST);
			alfrescoRepository.create(listNodeRef, sl);
			return rawMaterialNodeRef;

		});

		inWriteTx(() -> {

			NodeRef listContainerNodeRef = entityListDAO.getListContainer(productNodeRef);
			NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, QualityModel.TYPE_SAMPLING_LIST);
			List<NodeRef> listItems = entityListDAO.getListItems(listNodeRef, QualityModel.TYPE_SAMPLING_LIST);
			assertEquals(1, listItems.size());
			SamplingListDataItem sl = (SamplingListDataItem) alfrescoRepository.findOne(listItems.get(0));
			logger.info(sl);

			listNodeRef = entityListDAO.getList(listContainerNodeRef, QualityModel.TYPE_CONTROL_LIST);
			listItems = entityListDAO.getListItems(listNodeRef, QualityModel.TYPE_CONTROL_LIST);
			assertEquals(10, listItems.size());
			ControlListDataItem cl = (ControlListDataItem) alfrescoRepository.findOne(listItems.get(0));
			logger.info(cl);

			return null;

		});
	}
}
