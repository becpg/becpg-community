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
package fr.becpg.repo.quality.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.MPMModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.QualityModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityTplService;
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
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * <p>QualityControlServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class QualityControlServiceImpl implements QualityControlService {

	private static final Log logger = LogFactory.getLog(QualityControlServiceImpl.class);
	private static final long HOUR = 3600L * 1000L; // in milli-seconds.
	private NodeService nodeService;
	private EntityTplService entityTplService;
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	private EntityListDAO entityListDAO;
	private RepositoryEntityDefReader<RepositoryEntity> repositoryEntityDefReader;
	private NamespaceService namespaceService;
	private SystemConfigurationService systemConfigurationService;
	
	/**
	 * <p>Setter for the field <code>systemConfigurationService</code>.</p>
	 *
	 * @param systemConfigurationService a {@link fr.becpg.repo.system.SystemConfigurationService} object
	 */
	public void setSystemConfigurationService(SystemConfigurationService systemConfigurationService) {
		this.systemConfigurationService = systemConfigurationService;
	}
	
	private String sampleIdPattern() {
		return systemConfigurationService.confValue("beCPG.quality.sampleId.format");
	}

	private static final Set<QName> datalistsToCopy = new HashSet<>();

	static {
		datalistsToCopy.add(PLMModel.TYPE_COMPOLIST);
		datalistsToCopy.add(MPMModel.TYPE_PROCESSLIST);
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>entityTplService</code>.</p>
	 *
	 * @param entityTplService a {@link fr.becpg.repo.entity.EntityTplService} object.
	 */
	public void setEntityTplService(EntityTplService entityTplService) {
		this.entityTplService = entityTplService;
	}

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/**
	 * <p>Setter for the field <code>entityListDAO</code>.</p>
	 *
	 * @param entityListDAO a {@link fr.becpg.repo.entity.EntityListDAO} object.
	 */
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	/**
	 * <p>Setter for the field <code>repositoryEntityDefReader</code>.</p>
	 *
	 * @param repositoryEntityDefReader a {@link fr.becpg.repo.repository.RepositoryEntityDefReader} object.
	 */
	public void setRepositoryEntityDefReader(RepositoryEntityDefReader<RepositoryEntity> repositoryEntityDefReader) {
		this.repositoryEntityDefReader = repositoryEntityDefReader;
	}

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/** {@inheritDoc} */
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

				String freqText = sdl.getFreqText();
				Date sampleDateTime = (batchStart != null ? batchStart : new Date());
				List<Date> sampleDates = new ArrayList<>();
				Map<Date, String> sampleDateText = new HashMap<>();

				// create samples to take

				if(freqText != null && !freqText.isBlank()){
					String[] freqs = freqText.split(",");
					for(String freq : freqs){
						freq = freq.trim().toLowerCase();
						Double freqDigit = null;
						String freqUnit = null;
						Integer timeToAdd = null;
						QName referenceDurationProp = null;
						Pattern p = Pattern.compile("([0-9]+(\\.[0-9]+)?)([A-Za-z]+)");
						Matcher m = p.matcher(freq);
						if (m.matches()) {
							freqDigit = Double.parseDouble(m.group(1));
							freqUnit = m.group(3);
							switch(freqUnit) {
							case "d":
								timeToAdd = Calendar.DAY_OF_YEAR;
								break;
							case "w":
								timeToAdd = Calendar.WEEK_OF_YEAR;
								break;
							case "m":
								timeToAdd = Calendar.MONTH;
								break;
							case "y":
								timeToAdd = Calendar.YEAR;
								break;
							case "bbd":
								referenceDurationProp = PLMModel.PROP_BEST_BEFORE_DATE;
								break;
							case "ubd":
								referenceDurationProp = PLMModel.PROP_USE_BY_DATE;
								break;
							case "pao":
								referenceDurationProp = PLMModel.PROP_PERIOD_AFTER_OPENING;
								break;
							default:
								break;
							}
							if (sampleDateTime != null && freqDigit != null && freqUnit != null) {
								logger.debug("Update sample time add: " + freqDigit + " " + freqUnit + " to " + sampleDateTime);
								Date newDate = null;
								if (timeToAdd != null) {
									Calendar cal = Calendar.getInstance();
									cal.setTime(sampleDateTime);
									cal.add(timeToAdd, freqDigit.intValue());
									newDate = cal.getTime();
								} else if (referenceDurationProp != null) {
									Integer referenceDuration = (Integer) nodeService.getProperty(qcNodeRef, referenceDurationProp);
									if (referenceDuration == null && qualityControlData.getProduct() != null) {
										referenceDuration = (Integer) nodeService.getProperty(qualityControlData.getProduct(), referenceDurationProp);
									}
									if (referenceDuration != null) {
										newDate = new Date((long) (sampleDateTime.getTime() + freqDigit * referenceDuration * 24 * 3600 * 1000));
									}
								}
								if (newDate != null) {
									sampleDates.add(newDate);
									sampleDateText.put(newDate, freq.toUpperCase());
								}
							}
						}
					}					
				} else {
					String freq = sdl.getFreqUnit();
					int samplesToTake = 1;
					int freqInHour = 1;

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
					default: 
						freqInHour = 1;
						break;
					}

					if ((batchDuration != null) && !"/batch".equals(freq)) {
						samplesToTake = (batchDuration / freqInHour)+1;
					}

					for (int z_idx = 0; z_idx < samplesToTake; z_idx++) {                        
						// calculate next time
						if (sampleDateTime != null) {
							logger.debug("Update sample time add: " + freqInHour + " hour to " + sampleDateTime);
							sampleDates.add(sampleDateTime);
							sampleDateTime = new Date(sampleDateTime.getTime() + (freqInHour * HOUR));
						}
					}
				}
				if (!sampleDates.isEmpty()) {
					Date nextAnalysisDate = qualityControlData.getNextAnalysisDate();
					for(Date sampleDate : sampleDates){
						if (nextAnalysisDate == null || nextAnalysisDate.after(sampleDate)) {
							nextAnalysisDate = sampleDate;
						}
						// several samples must be taken
						for (int z_idx2 = 0; z_idx2 < sdl.getQty(); z_idx2++) {

							samplingList.add(new SamplingListDataItem(sampleDate, sampleDateText.get(sampleDate), null, sdl.getControlPoint(), sdl.getControlStep(),
									sdl.getSamplingGroup(), sdl.getControlingGroup(), sdl.getFixingGroup(), sdl.getReaction()));
						}
					}
					if (nextAnalysisDate != null && !nextAnalysisDate.equals(qualityControlData.getNextAnalysisDate())) {
						qualityControlData.setNextAnalysisDate(nextAnalysisDate);
					}
				}
			}

			alfrescoRepository.save(qualityControlData);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void createSamplingListId(NodeRef sampleListNodeRef) {
		if(nodeService.getProperty(sampleListNodeRef, QualityModel.PROP_SL_SAMPLE_ID) == null){
			NodeRef qcNodeRef = entityListDAO.getEntity(sampleListNodeRef);
			RepositoryEntity entity = alfrescoRepository.findOne(qcNodeRef);
			if(entity instanceof QualityControlData){
				QualityControlData qualityControlData = (QualityControlData) entity;
				Integer samplesCounter = qualityControlData.getSamplesCounter();
				if (samplesCounter == null) {
					samplesCounter = 0;
				}

				Matcher patternMatcher = Pattern.compile("\\{([^}]+)\\}").matcher(sampleIdPattern());
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
				nodeService.setProperty(sampleListNodeRef, QualityModel.PROP_SL_SAMPLE_ID, sb.toString().trim());

				samplesCounter++;
				qualityControlData.setSamplesCounter(samplesCounter);
				alfrescoRepository.save(qualityControlData);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void createControlList(NodeRef sampleListNodeRef) {
		NodeRef entityNodeRef = entityListDAO.getEntity(sampleListNodeRef);
		RepositoryEntity entity = alfrescoRepository.findOne(entityNodeRef);
		ProductData productData = null;
		QualityControlData qualityControlData = null;

		if(entity instanceof QualityControlData){
			qualityControlData = (QualityControlData) entity;
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
		} else{
			productData = (ProductData)entity;
		}

		SamplingListDataItem sl = (SamplingListDataItem) alfrescoRepository.findOne(sampleListNodeRef);
		NodeRef listContainerNodeRef = alfrescoRepository.getOrCreateDataListContainer(entity);
		NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, QualityModel.TYPE_CONTROL_LIST);
		if(listNodeRef == null){
			listNodeRef = entityListDAO.createList(listContainerNodeRef, QualityModel.TYPE_CONTROL_LIST);
		}

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
						MLText textCriteria = null;

						if (productData != null && cdl.getType().startsWith("bcpg_")) {

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

						if (cdl.getTarget() != null) {
							target = cdl.getTarget();

							if (cdl.getMini() != null) {
								mini = cdl.getMini();
							}
							if (cdl.getMaxi() != null) {
								maxi = cdl.getMaxi();
							}

						} else if( target!=null  && (cdl.getMini() != null || cdl.getMaxi()!=null)) {

							if (cdl.getMini() != null) {
								mini =  target - cdl.getMini();
							}

							if (cdl.getMaxi() != null) {
								maxi = target +  cdl.getMaxi();
							}

						} else {
							if (cdl.getMini() != null) {
								mini = cdl.getMini();
							}
							if (cdl.getMaxi() != null) {
								maxi = cdl.getMaxi();
							}
						}



						if ((cdl.getUnit() != null) && !cdl.getUnit().isEmpty()) {
							unit = cdl.getUnit();
						}
						if ((cdl.getTextCriteria() != null) && !cdl.getTextCriteria().isEmpty()) {
							textCriteria = cdl.getTextCriteria();
						}
						alfrescoRepository.create(listNodeRef, new ControlListDataItem(null, cdl.getType(), mini, maxi, cdl.getRequired(), sl.getSampleId(), null, target,
								unit, textCriteria, null, cdl.getTemperature(), sl.getTimePeriod(), cdl.getMethod(), Arrays.asList(n), controlPointData.getNodeRef()));
					}
				}
			}
		}
	}

	/** {@inheritDoc} */
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
				RepositoryEntity entity = alfrescoRepository.findOne(entityNodeRef);
				if(entity instanceof QualityControlData){
					QualityControlData qualityControlData = (QualityControlData) entity;

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
			}

		} else {
			logger.warn("SampleId is null");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void deleteSamplingListId(NodeRef sampleListNodeRef) {
		NodeRef entityNodeRef = entityListDAO.getEntity(sampleListNodeRef);

		if (!nodeService.hasAspect(entityNodeRef, ContentModel.ASPECT_PENDING_DELETE)) {
			RepositoryEntity entity = alfrescoRepository.findOne(entityNodeRef);
			if(entity instanceof QualityControlData){
				QualityControlData qualityControlData = (QualityControlData) alfrescoRepository.findOne(entityNodeRef);
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
				nodeService.setProperty(sampleListNodeRef, QualityModel.PROP_SL_SAMPLE_STATE, QualityControlState.Compliant);
				updateQualityControlState(qualityControlData);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void updateQualityControlState(NodeRef sampleNodeRef) {
		if (nodeService.exists(sampleNodeRef)) {
			NodeRef entityNodeRef = entityListDAO.getEntity(sampleNodeRef);
			if (!nodeService.hasAspect(entityNodeRef, ContentModel.ASPECT_PENDING_DELETE)) {
				RepositoryEntity entity = alfrescoRepository.findOne(entityNodeRef);
				if(entity instanceof QualityControlData){
					QualityControlData qualityControlData = (QualityControlData) alfrescoRepository.findOne(entityNodeRef);
					updateQualityControlState(qualityControlData);
				}
			}
		}

	}

	private void updateQualityControlState(QualityControlData qualityControlData) {
		boolean isQCControled = true;
		boolean isQCCompliant = true;
		Date nextAnalysisDate = null;

		for (SamplingListDataItem sl : qualityControlData.getSamplingList()) {

			if (sl.getSampleState() == null) {
				isQCControled = false;
				if (nextAnalysisDate == null || (nextAnalysisDate != null && nextAnalysisDate.after(sl.getDateTime()))) {
					nextAnalysisDate = sl.getDateTime();
				}
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

		} else if (nextAnalysisDate != null){
			qualityControlData.setNextAnalysisDate(nextAnalysisDate);
		}

		alfrescoRepository.save(qualityControlData);

	}

	/** {@inheritDoc} */
	@Override
	public void copyProductDataList(NodeRef qcNodeRef, NodeRef productNodeRef) {
		NodeRef entityTplNodeRef = entityTplService.getEntityTpl(QualityModel.TYPE_QUALITY_CONTROL);
		for (QName datalistToCopy : datalistsToCopy) {
			NodeRef productDatalistNoderef = getDataListNodeRef(productNodeRef, datalistToCopy);
			if (productDatalistNoderef != null && (getDataListNodeRef(qcNodeRef, datalistToCopy) != null
					|| getDataListNodeRef(entityTplNodeRef, datalistToCopy) != null)) {
				NodeRef listContainerNodeRef = entityListDAO.getListContainer(qcNodeRef);
				if (listContainerNodeRef == null) {
					listContainerNodeRef = entityListDAO.createListContainer(qcNodeRef);
				}
				NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, datalistToCopy);
				if (listNodeRef == null) {
					listNodeRef = entityListDAO.createList(listContainerNodeRef, datalistToCopy);
				}
				entityListDAO.copyDataList(productDatalistNoderef, qcNodeRef, true);
			}
		}
	}

	private NodeRef getDataListNodeRef(NodeRef entityNodeRef, QName datalistQName) {
		NodeRef listNodeRef = null;
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
		if (listContainerNodeRef != null) {
			listNodeRef = entityListDAO.getList(listContainerNodeRef, datalistQName);
		}
		return listNodeRef;

	}

}
