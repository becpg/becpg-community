/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.QualityModel;
import fr.becpg.repo.entity.EntityListDAO;
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

public class QualityControlTest extends PLMBaseTestCase {
	
	private static Log logger = LogFactory.getLog(QualityControlTest.class);

	private static final long HOUR = 3600 * 1000; // in milli-seconds.

	@Resource
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	@Resource
	private EntityListDAO entityListDAO;

	private NodeRef controlStepNodeRef;
	private NodeRef methodNodeRef;
	private NodeRef controlPointNodeRef;
	private NodeRef qualityControlNodeRef;
	private NodeRef controlPlanNodeRef;


	private void createControlPlan() {

		// create method
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		String name = "Method";
		properties.put(ContentModel.PROP_NAME, name);
		methodNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
				QualityModel.TYPE_CONTROL_METHOD, properties).getChildRef();

		// create control step
		properties.clear();
		name = "Step";
		properties.put(ContentModel.PROP_NAME, name);
		controlStepNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
				QualityModel.TYPE_CONTROL_STEP, properties).getChildRef();

		// create control point
		ControlPointData controlPointData = new ControlPointData();
		controlPointData.setName("Control point");
		List<ControlDefListDataItem> controlDefList = new ArrayList<ControlDefListDataItem>();
		controlDefList.add(new ControlDefListDataItem(null, "bcpg_nutList", null, null, true, methodNodeRef, nuts));
		controlPointData.setControlDefList(controlDefList);
		controlPointNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), controlPointData).getNodeRef();

		// create control plan
		ControlPlanData controlPlanData = new ControlPlanData();
		controlPlanData.setName("Control plan");
		List<SamplingDefListDataItem> samplingDefList = new ArrayList<SamplingDefListDataItem>();
		samplingDefList.add(new SamplingDefListDataItem(2, 1, "/4hours", controlPointNodeRef, controlStepNodeRef, null, null, null, "Reaction"));
		controlPlanData.setSamplingDefList(samplingDefList);
		controlPlanNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), controlPlanData).getNodeRef();
	}

	private void createQualityControl( List<NodeRef> controlPlansNodeRef, NodeRef productNodeRef) {

		QualityControlData qualityControlData = new QualityControlData();
		qualityControlData.setName("Quality control");
		qualityControlData.setBatchStart(new Date());
		qualityControlData.setBatchDuration(8);
		qualityControlData.setBatchId("12247904");
		qualityControlData.setOrderId("2394744");
		qualityControlData.setProduct(productNodeRef);
		qualityControlData.setControlPlans(controlPlansNodeRef);

		qualityControlNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), qualityControlData).getNodeRef();

		// TODO : add a policy...
		// qualityControlService.createSamplingList(qualityControlNodeRef,
		// controlPlansNodeRef.get(0));

	}

	
	@Test
	public void testCreateQualityControl() {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				createControlPlan();
				List<NodeRef> controlPlansNodeRef = new ArrayList<NodeRef>();
				controlPlansNodeRef.add(controlPlanNodeRef);

				NodeRef productNodeRef = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(), "Raw material");

				createQualityControl(controlPlansNodeRef, productNodeRef);

				return null;

			}
		}, false, true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			
			public NodeRef execute() throws Throwable {

				// check samples
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
				
				// check controlList
				//List<ControlListDataItem> controlList = (List)alfrescoRepository.loadDataList(qualityControlNodeRef, QualityModel.TYPE_CONTROL_LIST, QualityModel.TYPE_CONTROL_LIST);
				NodeRef controlListNodeRef = entityListDAO.getList(entityListDAO.getListContainer(qualityControlNodeRef), "Analyses");
				List<FileInfo> fileInfos = fileFolderService.listFiles(controlListNodeRef);
				logger.info("fileInfos.size() " + fileInfos.size());
				assertEquals(6*10, fileInfos.size());
				boolean isFirstCLOfSample3 = true;

				// fill controlList (sample1)
				logger.info("fill controlList");
				for(FileInfo fileInfo : fileInfos){
					ControlListDataItem cl = (ControlListDataItem)alfrescoRepository.findOne(fileInfo.getNodeRef());
					if(cl.getSampleId().equals("12247904/1")){
						cl.setState(QualityControlState.Compliant);
						alfrescoRepository.save(cl);
					}
					else if(cl.getSampleId().equals("12247904/2")){
						cl.setState(QualityControlState.NonCompliant);
						alfrescoRepository.save(cl);
					}
					else if(cl.getSampleId().equals("12247904/3")){
						if(isFirstCLOfSample3){
							cl.setState(QualityControlState.NonCompliant);
						}
						else{
							cl.setState(QualityControlState.Compliant);
						}
						alfrescoRepository.save(cl);
					}					
				}
				
				return null;

			}
		}, false, true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				// check samples
				QualityControlData qualityControlData = (QualityControlData) alfrescoRepository.findOne(qualityControlNodeRef);
				int checks = 0;

				for (SamplingListDataItem sl : qualityControlData.getSamplingList()) {
					if (sl.getSampleId().equals("12247904/1")) {						
						assertEquals("check state", QualityControlState.Compliant, sl.getSampleState());
						checks++;
					} else if (sl.getSampleId().equals("12247904/2")) {
						assertEquals("check state", QualityControlState.NonCompliant, sl.getSampleState());
						checks++;
					} else if (sl.getSampleId().equals("12247904/3")) {
						assertEquals("check state", QualityControlState.NonCompliant, sl.getSampleState());
						checks++;
					}
					else if (sl.getSampleId().equals("12247904/4")) {						
						assertEquals("check state", null, sl.getSampleState());
						checks++;
					}
				}

				assertEquals(4, checks);
				
				
				return null;

			}
		}, false, true);
		
		
	}
}
