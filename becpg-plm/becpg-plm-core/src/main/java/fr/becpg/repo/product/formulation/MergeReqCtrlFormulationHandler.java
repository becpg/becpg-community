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
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.model.FormulatedCharactDataItem;

/**
 * Merge ReqCtrlListDataItem to avoid duplication of items and sort them
 *
 * @author quere
 *
 */
public class MergeReqCtrlFormulationHandler extends FormulationBaseHandler<ProductData> {

	protected static Log logger = LogFactory.getLog(MergeReqCtrlFormulationHandler.class);

	private AlfrescoRepository<ProductData> alfrescoRepository;

	private NodeService nodeService;

	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@Override
	public boolean process(ProductData productData) throws FormulateException {

		// Add child requirements
		appendChildReq(productData, productData.getCompoListView().getReqCtrlList(), productData.getCompoListView().getCompoList());

		mergeReqCtrlList(productData.getCompoListView().getReqCtrlList());
		mergeReqCtrlList(productData.getPackagingListView().getReqCtrlList());
		mergeReqCtrlList(productData.getProcessListView().getReqCtrlList());

		updateFormulatedCharactInError(productData, productData.getCompoListView().getReqCtrlList());

		return true;
	}

	private void appendChildReq(ProductData productData, List<ReqCtrlListDataItem> reqCtrlList, List<CompoListDataItem> compoList) {
		for (CompoListDataItem compoListDataItem : compoList) {
			NodeRef productNodeRef = compoListDataItem.getProduct();
			if (productNodeRef != null) {
				ProductData componentProductData = alfrescoRepository.findOne(productNodeRef);
				if (!productNodeRef.equals(productData.getNodeRef()) && (componentProductData instanceof SemiFinishedProductData)
						|| (componentProductData instanceof FinishedProductData) || (componentProductData instanceof RawMaterialData)) {
					if ((componentProductData.getCompoListView() != null) && (componentProductData.getCompoListView().getReqCtrlList() != null)) {
						for (ReqCtrlListDataItem tmp : componentProductData.getCompoListView().getReqCtrlList()) {
							// mandatory fields rclDataItem aren't put in parent
							if (tmp.getReqDataType() != RequirementDataType.Completion) {
								reqCtrlList.add(new ReqCtrlListDataItem(null, tmp.getReqType(), tmp.getReqMlMessage(), tmp.getCharact(),
										tmp.getSources(), tmp.getReqDataType() != null ? tmp.getReqDataType() : RequirementDataType.Nutrient));
							}
						}
					}
				}
			}
		}

	}

	private void mergeReqCtrlList(List<ReqCtrlListDataItem> reqCtrlList) {

		if (reqCtrlList != null) {
			Map<String, ReqCtrlListDataItem> dbReqCtrlList = new HashMap<>();
			Map<String, ReqCtrlListDataItem> newReqCtrlList = new HashMap<>();
			List<ReqCtrlListDataItem> duplicates = new ArrayList<>();

			for (ReqCtrlListDataItem r : reqCtrlList) {
				if (r.getNodeRef() != null) {
					if (dbReqCtrlList.containsKey(r.getKey())) {
						duplicates.add(r);
						// Merge sources
						for (NodeRef tmpref : r.getSources()) {
							if (!dbReqCtrlList.get(r.getKey()).getSources().contains(tmpref)) {
								dbReqCtrlList.get(r.getKey()).getSources().add(tmpref);
							}
						}
					} else {
						dbReqCtrlList.put(r.getKey(), r);
					}
				} else {
					if (newReqCtrlList.containsKey(r.getKey())) {
						duplicates.add(r);
						// Merge sources
						for (NodeRef tmpref : r.getSources()) {
							if (!newReqCtrlList.get(r.getKey()).getSources().contains(tmpref)) {
								newReqCtrlList.get(r.getKey()).getSources().add(tmpref);
							}
						}
					} else {
						newReqCtrlList.put(r.getKey(), r);
					}
				}
			}

			for (ReqCtrlListDataItem dup : duplicates) {
				reqCtrlList.remove(dup);
			}

			for (Map.Entry<String, ReqCtrlListDataItem> dbKV : dbReqCtrlList.entrySet()) {
				if (!newReqCtrlList.containsKey(dbKV.getKey())) {
					// remove
					reqCtrlList.remove(dbKV.getValue());
				} else {
					// update
					ReqCtrlListDataItem newReqCtrlListDataItem = newReqCtrlList.get(dbKV.getKey());
					dbKV.getValue().setReqType(newReqCtrlListDataItem.getReqType());
					dbKV.getValue().setSources(newReqCtrlListDataItem.getSources());
					dbKV.getValue().setCharact(newReqCtrlListDataItem.getCharact());
					dbKV.getValue().setReqDataType(newReqCtrlListDataItem.getReqDataType());
					reqCtrlList.remove(newReqCtrlListDataItem);
				}
			}

			// sort
			sort(reqCtrlList);
		}
	}

	@SuppressWarnings("unchecked")
	private void updateFormulatedCharactInError(ProductData productData, List<ReqCtrlListDataItem> reqCtrlList) {
		for (ReqCtrlListDataItem r : reqCtrlList) {
			if (logger.isDebugEnabled()) {
				logger.debug("r " + r.getReqMessage() + " " + r.getCharact());
			}
			if (r.getCharact() != null) {
				QName type = nodeService.getType(r.getCharact());
				List<FormulatedCharactDataItem> simpleList = new ArrayList<>();
				if (PLMModel.TYPE_NUT.equals(type)) {
					simpleList = (List<FormulatedCharactDataItem>) (List<?>) productData.getNutList();
				} else if (PLMModel.TYPE_COST.equals(type)) {
					simpleList = (List<FormulatedCharactDataItem>) (List<?>) productData.getCostList();
				}
				for (FormulatedCharactDataItem sl : simpleList) {
					if (r.getCharact().equals(sl.getCharactNodeRef())) {
						String message = r.getReqMessage();
						if ((r.getSources() != null) && !r.getSources().isEmpty()) {
							int i = 0;
							message += " : ";
							for (NodeRef n : r.getSources()) {
								if (i > 0) {
									message += ", ";
								} else if (i >= 5) {
									message += "...";
									break;
								}
								message += nodeService.getProperty(n, ContentModel.PROP_NAME);
								i++;
							}
						}
						sl.setErrorLog((sl.getErrorLog() != null ? sl.getErrorLog() + ". " : "") + message);
						if (logger.isDebugEnabled()) {
							logger.debug("setErrorLog " + sl.getErrorLog());
						}
						break;
					}
				}
			}
		}
	}

	/**
	 * Sort by type
	 *
	 */
	private void sort(List<ReqCtrlListDataItem> reqCtrlList) {

		Collections.sort(reqCtrlList, new Comparator<ReqCtrlListDataItem>() {

			final int BEFORE = -1;
			final int EQUAL = 0;
			final int AFTER = 1;

			@Override
			public int compare(ReqCtrlListDataItem r1, ReqCtrlListDataItem r2) {

				if ((r1.getReqType() != null) && (r1.getReqType() != null)) {
					if (r1.getReqType().equals(r2.getReqType())) {
						return EQUAL;
					} else if (r1.getReqType().equals(RequirementType.Forbidden)) {
						return BEFORE;
					} else if (r2.getReqType().equals(RequirementType.Forbidden)) {
						return AFTER;
					} else if (r1.getReqType().equals(RequirementType.Tolerated)) {
						return BEFORE;
					} else {
						return AFTER;
					}
				} else if (r1.getReqType() != null) {
					return BEFORE;
				} else if (r2.getReqType() != null) {
					return AFTER;
				}

				return EQUAL;
			}
		});

		int i = 0;
		for (ReqCtrlListDataItem r : reqCtrlList) {
			r.setSort(i);
			i++;
		}
	}
}
