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

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.formulation.FormulationExecutor;
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
import fr.becpg.repo.system.SystemConfigurationService;

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

	private SystemConfigurationService systemConfigurationService;
	
	private FormulationExecutor formulationExecutor;
	
	/**
	 * <p>Setter for the field <code>formulationExecutor</code>.</p>
	 *
	 * @param formulationExecutor a {@link fr.becpg.repo.formulation.FormulationExecutor} object
	 */
	public void setFormulationExecutor(FormulationExecutor formulationExecutor) {
		this.formulationExecutor = formulationExecutor;
	}

	/**
	 * <p>Setter for the field <code>systemConfigurationService</code>.</p>
	 *
	 * @param systemConfigurationService a {@link fr.becpg.repo.system.SystemConfigurationService} object
	 */
	public void setSystemConfigurationService(SystemConfigurationService systemConfigurationService) {
		this.systemConfigurationService = systemConfigurationService;
	}

	private Integer maxRclSourcesToKeep() {
		return Integer.valueOf(systemConfigurationService.confValue("beCPG.formulation.reqCtrlList.maxRclSourcesToKeep"));
	}

	private Boolean addChildRclSources() {
		return Boolean.valueOf(systemConfigurationService.confValue("beCPG.formulation.reqCtrlList.addChildRclSources"));
	}
	
	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<ScorableEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ScorableEntity scorableEntity) {

		// Add child requirements
		if (scorableEntity.getReqCtrlList() != null) {
			if (scorableEntity instanceof ProductData) {
				appendChildReq((ProductData) scorableEntity, scorableEntity.getReqCtrlList());
			}

			scorableEntity.merge(formulationExecutor.getDisabledFormulationChainIds(scorableEntity));
			
			if (maxRclSourcesToKeep() != null && maxRclSourcesToKeep() > 0) {

				for (ReqCtrlListDataItem r : scorableEntity.getReqCtrlList()) {
					if (r.getSources() != null) {
						r.setSources(new ArrayList<>(r.getSources().subList(0, Math.min(r.getSources().size(), maxRclSourcesToKeep()))));
					}
				}
			}
		}

		return true;
	}

	private void appendChildReq(ProductData productData, List<ReqCtrlListDataItem> reqCtrlList) {
		if (productData.getCompoListView().getCompoList() != null) {
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
		}

		if (productData.getPackagingListView().getPackagingList() != null) {
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
		}
		
		if (productData.getProcessListView().getProcessList() != null) {
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
	}

	private Set<ReqCtrlListDataItem> reqCtrlToAdd(ProductData componentProductData) {
		Set<ReqCtrlListDataItem> toAdd = new HashSet<>();
		for (ReqCtrlListDataItem tmp : componentProductData.getReqCtrlList()) {
			if (tmp.getReqDataType() != RequirementDataType.Completion) {
				ReqCtrlListDataItem reqCtl = ReqCtrlListDataItem.build().ofType(tmp.getReqType())
						.withMessage(tmp.getReqMlMessage()).withCharact(tmp.getCharact())
						.withSources((addChildRclSources() == null || Boolean.TRUE.equals(addChildRclSources())) ? tmp.getSources() : new ArrayList<>())
						.ofDataType(tmp.getReqDataType() != null ? tmp.getReqDataType() : RequirementDataType.Nutrient)
						.withRegulatoryCode(tmp.getRegulatoryCode());
				toAdd.add(reqCtl);
			}
		}
		return toAdd;
	}

	/** {@inheritDoc} */
	@Override
	public void onError(ScorableEntity repositoryEntity) {
		repositoryEntity.merge(formulationExecutor.getDisabledFormulationChainIds(repositoryEntity));
		super.onError(repositoryEntity);
	}

}
