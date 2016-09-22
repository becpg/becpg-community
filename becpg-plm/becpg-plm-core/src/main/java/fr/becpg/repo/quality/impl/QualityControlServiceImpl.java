/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG.
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
package fr.becpg.repo.quality.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.PLMModel;
import fr.becpg.model.QualityModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.quality.QualityControlService;
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
import fr.becpg.repo.repository.RepositoryEntityDefReader;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.repository.model.ControlableListDataItem;
import fr.becpg.repo.repository.model.MinMaxValueDataItem;
import fr.becpg.repo.repository.model.UnitAwareDataItem;

public class QualityControlServiceImpl implements QualityControlService {

	private static final Log logger = LogFactory.getLog(QualityControlServiceImpl.class);
	private static final long HOUR = 3600 * 1000; // in milli-seconds.

	private NodeService nodeService;
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	private EntityListDAO entityListDAO;
	private RepositoryEntityDefReader<RepositoryEntity> repositoryEntityDefReader;
	private NamespaceService namespaceService;

	private String sampleIdPattern = "{qa:batchId}/{qa:qcSamplesCounter}";

	public void setSampleIdPattern(String sampleIdPattern) {
		this.sampleIdPattern = sampleIdPattern;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
	}

	public void setRepositoryEntityDefReader(RepositoryEntityDefReader<RepositoryEntity> repositoryEntityDefReader) {
		this.repositoryEntityDefReader = repositoryEntityDefReader;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	@Override
	public void createSamplingList(NodeRef qcNodeRef, NodeRef controlPlanNodeRef) {

		QualityControlData qualityControlData = (QualityControlData) alfrescoRepository.findOne(qcNodeRef);

		List<SamplingListDataItem> samplingList = qualityControlData.getSamplingList();

		Date batchStart = qualityControlData.getBatchStart();
		Integer batchDuration = qualityControlData.getBatchDuration();

		logger.debug("load control plan");
		ControlPlanData controlPlanData = (ControlPlanData) alfrescoRepository.findOne(controlPlanNodeRef);

		if (controlPlanData.getSamplingDefList() != null) {

			logger.debug("control plan loaded, controlPlanData.getSamplingDefList(): " + controlPlanData.getSamplingDefList().size());

			for (SamplingDefListDataItem sdl : controlPlanData.getSamplingDefList()) {

				logger.debug("create sample");

				String freq = sdl.getFreqUnit();
				int samplesToTake = 1;
				int freqInHour = 0;
				Date sampleDateTime = batchStart;

				// per batch
				switch (freq) {
				case "/batch":
					break;
				case "/hour":
					freqInHour = 1;
					break;
				case "/4hours":
					freqInHour = 4;
					break;
				case "/8hours":
					freqInHour = 8;
					break;
				}

				if ((batchDuration != null) && !"/batch".equals(freq)) {
					samplesToTake = (batchDuration / freqInHour) + 1;
				}

				// create samples to take
				for (int z_idx = 0; z_idx < samplesToTake; z_idx++) {
					// several samples must be taken
					for (int z_idx2 = 0; z_idx2 < sdl.getQty(); z_idx2++) {
						samplingList.add(new SamplingListDataItem(sampleDateTime, null, sdl.getControlPoint(), sdl.getControlStep(),
								sdl.getSamplingGroup(), sdl.getControlingGroup(), sdl.getFixingGroup(), sdl.getReaction()));
					}

					// calculate next time
					if (sampleDateTime != null) {
						logger.debug("Update sample time add: " + freqInHour + " hour to " + sampleDateTime);
						sampleDateTime = new Date(sampleDateTime.getTime() + (freqInHour * HOUR));
					}
				}
			}

		}

		alfrescoRepository.save(qualityControlData);
	}

	@Override
	public void createSamplingListId(NodeRef sampleListNodeRef) {
		NodeRef qcNodeRef = entityListDAO.getEntity(sampleListNodeRef);
		QualityControlData qualityControlData = (QualityControlData) alfrescoRepository.findOne(qcNodeRef);
		Integer samplesCounter = qualityControlData.getSamplesCounter();
		if (samplesCounter == null) {
			samplesCounter = 0;
		}

		Matcher patternMatcher = Pattern.compile("\\{([^}]+)\\}").matcher(sampleIdPattern);
		StringBuffer sb = new StringBuffer();
		while (patternMatcher.find()) {

			String propQname = patternMatcher.group(1);
			String replacement = "";

			if (propQname.equals(QualityModel.PROP_QC_SAMPLES_COUNTER.toPrefixString(namespaceService))) {
				replacement = "" + samplesCounter;
			} else {
				replacement = (String) nodeService.getProperty(qcNodeRef, QName.createQName(propQname, namespaceService));
			}

			patternMatcher.appendReplacement(sb, replacement != null ? replacement.replace("$", "") : "");

		}
		patternMatcher.appendTail(sb);
		nodeService.setProperty(sampleListNodeRef, QualityModel.PROP_SL_SAMPLE_ID, sb.toString());

		samplesCounter++;
		qualityControlData.setSamplesCounter(samplesCounter);
		alfrescoRepository.save(qualityControlData);

	}

	@Override
	public void createControlList(NodeRef sampleListNodeRef) {

		NodeRef qcNodeRef = entityListDAO.getEntity(sampleListNodeRef);
		QualityControlData qualityControlData = (QualityControlData) alfrescoRepository.findOne(qcNodeRef);

		ProductData productData = null;

		if (qualityControlData.getProduct() != null) {

			logger.debug("createControlList - load product");

			productData = (ProductData) alfrescoRepository.findOne(qualityControlData.getProduct());

			// Load microbio
			if (productData.getMicrobioList().isEmpty()) {
				List<AssociationRef> productMicrobioCriteriaNodeRefs = nodeService.getTargetAssocs(qualityControlData.getProduct(),
						PLMModel.ASSOC_PRODUCT_MICROBIO_CRITERIA);

				if (!productMicrobioCriteriaNodeRefs.isEmpty()) {
					NodeRef productMicrobioCriteriaNodeRef = (productMicrobioCriteriaNodeRefs.get(0)).getTargetRef();

					ProductData productMicrobioCriteriaData = (ProductData) alfrescoRepository.findOne(productMicrobioCriteriaNodeRef);
					productData.setMicrobioList(productMicrobioCriteriaData.getMicrobioList());
				}
			}
		}

		SamplingListDataItem sl = (SamplingListDataItem) alfrescoRepository.findOne(sampleListNodeRef);

		List<ControlListDataItem> controlList = qualityControlData.getControlList();

		ControlPointData controlPointData = (ControlPointData) alfrescoRepository.findOne(sl.getControlPoint());
		if (controlPointData.getControlDefList() != null) {

			logger.debug("calculateControlList, controlPointData.getControlDefList(): " + controlPointData.getControlDefList().size());

			for (ControlDefListDataItem cdl : controlPointData.getControlDefList()) {
				if (cdl.getCharacts() != null) {
					logger.debug("characts size : " + cdl.getCharacts().size());
					for (NodeRef n : cdl.getCharacts()) {

						Double target = null;
						Double mini = null;
						Double maxi = null;
						String unit = null;
						String textCriteria = null;

						if (productData != null) {

							if (cdl.getType().startsWith("bcpg_")) {
								QName dataListQName = QName.createQName(cdl.getType().replace("_", ":"), namespaceService);

								logger.debug("Looking for list : " + dataListQName);

								Map<QName, List<? extends RepositoryEntity>> datalists = repositoryEntityDefReader.getDataLists(productData);
								@SuppressWarnings("unchecked")
								List<BeCPGDataObject> dataListItems = (List<BeCPGDataObject>) datalists.get(dataListQName);

								if (dataListItems != null) {
									for (RepositoryEntity dataListItem : dataListItems) {

										if (dataListItem instanceof ControlableListDataItem) {
											ControlableListDataItem controlableListDataItem = (ControlableListDataItem) dataListItem;
											if (n.equals(controlableListDataItem.getCharactNodeRef())) {

												logger.debug("Find matching charact in list : " + controlableListDataItem.getCharactNodeRef() + " "
														+ dataListQName);

												target = controlableListDataItem.getValue();
												textCriteria = controlableListDataItem.getTextCriteria();
												if (controlableListDataItem instanceof UnitAwareDataItem) {
													unit = ((UnitAwareDataItem) controlableListDataItem).getUnit();
												}
												if (controlableListDataItem instanceof MinMaxValueDataItem) {
													mini = ((MinMaxValueDataItem) controlableListDataItem).getMini();
													maxi = ((MinMaxValueDataItem) controlableListDataItem).getMaxi();
												}
											}
										}
									}

								}
							}

						}

						if (cdl.getTarget() != null) {
							target = cdl.getTarget();
						}
						if (cdl.getMini() != null) {
							mini = cdl.getMini();
						}
						if (cdl.getMaxi() != null) {
							maxi = cdl.getMaxi();
						}
						if ((cdl.getUnit() != null) && !cdl.getUnit().isEmpty()) {
							unit = cdl.getUnit();
						}
						if ((cdl.getTextCriteria() != null) && !cdl.getTextCriteria().isEmpty()) {
							textCriteria = cdl.getTextCriteria();
						}

						controlList.add(new ControlListDataItem(null, cdl.getType(), mini, maxi, cdl.getRequired(), sl.getSampleId(), null, target,
								unit, textCriteria, null, cdl.getMethod(), Arrays.asList(n)));
					}
				}
			}
		}

		alfrescoRepository.save(qualityControlData);

	}

	@Override
	public void updateControlListState(NodeRef controlListNodeRef) {
		Double mini = (Double) nodeService.getProperty(controlListNodeRef, QualityModel.PROP_CL_MINI);
		Double maxi = (Double) nodeService.getProperty(controlListNodeRef, QualityModel.PROP_CL_MAXI);
		Double value = (Double) nodeService.getProperty(controlListNodeRef, QualityModel.PROP_CL_VALUE);

		if ((value != null) && ((mini != null) || (maxi != null))) {

			boolean isCompliant = true;
			if ((mini != null) && (mini.compareTo(value) > 0)) {
				isCompliant = false;
			}
			if ((maxi != null) && (maxi.compareTo(value) < 0)) {
				isCompliant = false;
			}
			logger.debug("updateControlListState isCompliant: " + isCompliant + " mini " + mini + " maxi " + maxi + " value " + value);
			if (isCompliant) {
				nodeService.setProperty(controlListNodeRef, QualityModel.PROP_CL_STATE, QualityControlState.Compliant.toString());
			} else {
				nodeService.setProperty(controlListNodeRef, QualityModel.PROP_CL_STATE, QualityControlState.NonCompliant.toString());
			}
		}

		updateSampleState(controlListNodeRef);
	}

	private void updateSampleState(NodeRef controlListNodeRef) {

		String sampleId = (String) nodeService.getProperty(controlListNodeRef, QualityModel.PROP_CL_SAMPLE_ID);
		if (sampleId != null) {

			NodeRef entityNodeRef = entityListDAO.getEntity(controlListNodeRef);

			if (!nodeService.hasAspect(entityNodeRef, ContentModel.ASPECT_PENDING_DELETE)) {
				QualityControlData qualityControlData = (QualityControlData) alfrescoRepository.findOne(entityNodeRef);

				boolean isQCControled = true;
				boolean isQCCompliant = true;

				for (ControlListDataItem controlListDataItem : qualityControlData.getControlList()) {
					if (sampleId.equals(controlListDataItem.getSampleId())) {
						if (QualityControlState.NonCompliant.equals(controlListDataItem.getState())) {
							isQCCompliant = false;
						} else if ((controlListDataItem.getState() == null) && Boolean.TRUE.equals(controlListDataItem.getRequired())) {
							isQCControled = false;
						}
					}
				}

				for (SamplingListDataItem sl : qualityControlData.getSamplingList()) {
					if (sampleId.equals(sl.getSampleId())) {
						if (!isQCControled) {
							sl.setSampleState(null);
						} else {
							if (isQCCompliant) {
								sl.setSampleState(QualityControlState.Compliant);
							} else {
								sl.setSampleState(QualityControlState.NonCompliant);
							}
						}
					}
				}

				alfrescoRepository.save(qualityControlData);
			}

		} else {
			logger.warn("SampleId is null");
		}
	}

	@Override
	public void deleteSamplingListId(NodeRef sampleListNodeRef) {
		NodeRef qcNodeRef = entityListDAO.getEntity(sampleListNodeRef);

		if (!nodeService.hasAspect(qcNodeRef, ContentModel.ASPECT_PENDING_DELETE)) {
			QualityControlData qualityControlData = (QualityControlData) alfrescoRepository.findOne(qcNodeRef);

			List<ControlListDataItem> toRemove = new ArrayList<>();

			String sampleId = (String) nodeService.getProperty(sampleListNodeRef, QualityModel.PROP_SL_SAMPLE_ID);
			if (sampleId != null) {
				for (ControlListDataItem item : qualityControlData.getControlList()) {
					if (sampleId.equals(item.getSampleId())) {
						toRemove.add(item);
					}
				}

			}
			qualityControlData.getControlList().removeAll(toRemove);

			updateQualityControlState(qualityControlData);
		}
	}

	@Override
	public void updateQualityControlState(NodeRef sampleNodeRef) {
		NodeRef entityNodeRef = entityListDAO.getEntity(sampleNodeRef);
		if (!nodeService.hasAspect(entityNodeRef, ContentModel.ASPECT_PENDING_DELETE)) {
			QualityControlData qualityControlData = (QualityControlData) alfrescoRepository.findOne(entityNodeRef);
			updateQualityControlState(qualityControlData);
		}
	}

	private void updateQualityControlState(QualityControlData qualityControlData) {
		boolean isQCControled = true;
		boolean isQCCompliant = true;

		for (SamplingListDataItem sl : qualityControlData.getSamplingList()) {

			if (sl.getSampleState() == null) {
				isQCControled = false;
			} else if (sl.getSampleState().equals(QualityControlState.NonCompliant)) {
				isQCCompliant = false;
			}
		}

		logger.debug("QC isQCControled : " + isQCControled + " isQCCompliant " + isQCCompliant);
		qualityControlData.setState(null);
		if (isQCControled) {
			if (isQCCompliant) {
				qualityControlData.setState(QualityControlState.Compliant);
			} else {
				qualityControlData.setState(QualityControlState.NonCompliant);
			}
		}

		alfrescoRepository.save(qualityControlData);

	}

}
