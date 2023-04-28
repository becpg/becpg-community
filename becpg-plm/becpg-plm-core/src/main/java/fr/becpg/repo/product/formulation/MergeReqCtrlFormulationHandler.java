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
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.helper.LargeTextHelper;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.PackagingKitData;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.ResourceProductData;
import fr.becpg.repo.product.data.ScorableEntity;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.model.FormulatedCharactDataItem;

/**
 * Merge ReqCtrlListDataItem to avoid duplication of items and sort them
 *
 * @author quere
 * @version $Id: $Id
 */
public class MergeReqCtrlFormulationHandler extends FormulationBaseHandler<ScorableEntity> {

	/** Constant <code>logger</code> */
	protected static Log logger = LogFactory.getLog(MergeReqCtrlFormulationHandler.class);

	private AlfrescoRepository<ScorableEntity> alfrescoRepository;

	private NodeService nodeService;

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<ScorableEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ScorableEntity scorableEntity) {

		// Add child requirements
		if (scorableEntity.getReqCtrlList() != null) {
			if(scorableEntity instanceof ProductData) {
				appendChildReq((ProductData)scorableEntity, scorableEntity.getReqCtrlList());
			}
	
			scorableEntity.merge();
			
			if(scorableEntity instanceof ProductData) {
				updateFormulatedCharactInError((ProductData)scorableEntity, scorableEntity.getReqCtrlList());
			}
		}

		return true;
	}

	private void appendChildReq(ProductData productData, List<ReqCtrlListDataItem> reqCtrlList) {
		for (CompoListDataItem compoListDataItem : productData.getCompoListView().getCompoList()) {
			NodeRef componentProductNodeRef = compoListDataItem.getProduct();
			if (componentProductNodeRef != null) {
				ProductData componentProductData = (ProductData) alfrescoRepository.findOne(componentProductNodeRef);
				if (((!componentProductNodeRef.equals(productData.getNodeRef()) && (componentProductData instanceof SemiFinishedProductData))
						|| (componentProductData instanceof FinishedProductData) || (componentProductData instanceof RawMaterialData))
					&& ((componentProductData.getCompoListView() != null) && (componentProductData.getReqCtrlList() != null))) {					
					reqCtrlList.addAll(reqCtrlToAdd(componentProductData));
				}
			}
		}
		
		for (PackagingListDataItem packagingListDataItem : productData.getPackagingListView().getPackagingList()) {
			NodeRef componentProductNodeRef = packagingListDataItem.getProduct();
			if (componentProductNodeRef != null) {
				ProductData componentProductData = (ProductData) alfrescoRepository.findOne(componentProductNodeRef);
				if (((!componentProductNodeRef.equals(productData.getNodeRef()) && (componentProductData instanceof PackagingKitData))
						|| (componentProductData instanceof PackagingMaterialData)) 
					&& ((componentProductData.getPackagingListView() != null) && (componentProductData.getReqCtrlList() != null))) {					
					reqCtrlList.addAll(reqCtrlToAdd(componentProductData));
				}
			}
		}
		
		for (ProcessListDataItem processListDataItem : productData.getProcessListView().getProcessList()) {
			NodeRef componentProductNodeRef = processListDataItem.getResource();
			if (componentProductNodeRef != null) {
				ProductData componentProductData = (ProductData) alfrescoRepository.findOne(componentProductNodeRef);
				if (componentProductData instanceof ResourceProductData
					&& ((componentProductData.getProcessList() != null) && (componentProductData.getReqCtrlList() != null))) {					
					reqCtrlList.addAll(reqCtrlToAdd(componentProductData));
				}
			}
		}
	}
	
	private Set<ReqCtrlListDataItem> reqCtrlToAdd(ProductData componentProductData) {
		Set<ReqCtrlListDataItem> toAdd = new HashSet<>();
		for (ReqCtrlListDataItem tmp : componentProductData.getReqCtrlList()) {
			if (tmp.getReqDataType() != RequirementDataType.Completion) {
				ReqCtrlListDataItem reqCtl = new ReqCtrlListDataItem(null, tmp.getReqType(), tmp.getReqMlMessage(), tmp.getCharact(),
						tmp.getSources(), tmp.getReqDataType() != null ? tmp.getReqDataType() : RequirementDataType.Nutrient);
				reqCtl.setRegulatoryCode(tmp.getRegulatoryCode());
				toAdd.add(reqCtl);
			}
		}
		return toAdd;
	}

	@SuppressWarnings("unchecked")
	private void updateFormulatedCharactInError(ProductData productData, List<ReqCtrlListDataItem> reqCtrlList) {
		for (ReqCtrlListDataItem r : reqCtrlList) {
			if (logger.isDebugEnabled()) {
				logger.debug("r " + r.getReqMessage() + " " + r.getCharact());
			}
			if (r.getCharact() != null && !RequirementDataType.Specification.equals(r.getReqDataType())) {
				QName type = nodeService.getType(r.getCharact());
				List<FormulatedCharactDataItem> simpleList = new ArrayList<>();
				if (PLMModel.TYPE_NUT.equals(type)) {
					simpleList = (List<FormulatedCharactDataItem>) (List<?>) productData.getNutList();
				} else if (PLMModel.TYPE_COST.equals(type)) {
					simpleList = (List<FormulatedCharactDataItem>) (List<?>) productData.getCostList();
				} else if (PLMModel.TYPE_LCA.equals(type)) {
					simpleList = (List<FormulatedCharactDataItem>) (List<?>) productData.getLcaList();
				}
				for (FormulatedCharactDataItem sl : simpleList) {
					if (r.getCharact().equals(sl.getCharactNodeRef())) {
						StringBuilder message = new StringBuilder(r.getReqMessage());
						if ((r.getSources() != null) && !r.getSources().isEmpty()) {
							int i = 0;
							message.append( " : ");
							for (NodeRef n : r.getSources()) {
								if (i >= 5) {
									message.append("...");
									break;
								} else if(i > 0) {
									message.append(", ");
								}
								message.append(nodeService.getProperty(n, ContentModel.PROP_NAME));
								i++;
							}
						}
						sl.setErrorLog(LargeTextHelper.elipse((sl.getErrorLog() != null ? sl.getErrorLog() + ". " : "") + message.toString()));
						if (logger.isDebugEnabled()) {
							logger.debug("setErrorLog " + sl.getErrorLog());
						}
						break;
					}
				}
			}
		}
	}
	
	@Override
	public void onError(ScorableEntity repositoryEntity) {
		repositoryEntity.merge();
		super.onError(repositoryEntity);
	}

}
