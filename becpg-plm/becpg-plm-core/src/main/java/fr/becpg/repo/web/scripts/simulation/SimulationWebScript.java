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
package fr.becpg.repo.web.scripts.simulation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.PLMModel;
import fr.becpg.model.PLMWorkflowModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.AbstractProductDataView;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompositionDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 *
 * @author matthieu
 *
 */
public class SimulationWebScript extends AbstractWebScript {

	private static final String PARAM_ENTITY_NODEREF = "entityNodeRef";

	private static final String PARAM_DATALISTITEMS = "dataListItems";

	private AssociationService associationService;

	private NodeService nodeService;

	private EntityVersionService entityVersionService;

	private AlfrescoRepository<ProductData> alfrescoRepository;

	private CopyService copyService;

	private static Log logger = LogFactory.getLog(SimulationWebScript.class);

	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setCopyService(CopyService copyService) {
		this.copyService = copyService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		String entityNodeRefParam = req.getParameter(PARAM_ENTITY_NODEREF);
		String dataListItems = req.getParameter(PARAM_DATALISTITEMS);

		List<NodeRef> dataListItemsNodeRefs = new ArrayList<>();
		if ((dataListItems != null) && !dataListItems.isEmpty()) {
			for (String dataListItem : dataListItems.split(",")) {
				dataListItemsNodeRefs.add(new NodeRef(dataListItem));
			}
		}

		NodeRef simulationNodeRef = null;

		NodeRef entityNodeRef = null;
		if ((entityNodeRefParam != null) && !entityNodeRefParam.isEmpty()) {
			entityNodeRef = new NodeRef(entityNodeRefParam);
		}

		if (!dataListItemsNodeRefs.isEmpty()) {

			recurSimule(entityNodeRef, null, dataListItemsNodeRefs);

		} else if (entityNodeRef != null) {
			simulationNodeRef = createSimulationNodeRef(entityNodeRef, nodeService.getPrimaryParent(entityNodeRef).getParentRef());
		}

		try {
			JSONObject ret = new JSONObject();

			if (simulationNodeRef != null) {
				ret.put("persistedObject", simulationNodeRef);
			}

			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			ret.write(res.getWriter());
		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		}

	}

	private NodeRef recurSimule(NodeRef entityNodeRef, CompositionDataItem dataListItem, List<NodeRef> dataListItemsNodeRefs) {

		NodeRef parentNodeRef = dataListItem != null ? dataListItem.getComponent() : entityNodeRef;

		ProductData productData = alfrescoRepository.findOne(parentNodeRef);

		if (productData.getCompoList() != null) {

			for (AbstractProductDataView view : productData.getViews()) {
				for (CompositionDataItem item : view.getMainDataList()) {

					NodeRef simulationNodeRef = recurSimule(entityNodeRef, item, dataListItemsNodeRefs);
					if (simulationNodeRef != null) {
						if (dataListItem == null) {
							logger.debug("Update root " + productData.getName());
							associationService.update(item.getNodeRef(), item.getComponentAssocName(), simulationNodeRef);
						} else {
							NodeRef parentSimulationNodeRef = createCopyNodeRef(parentNodeRef,
									nodeService.getPrimaryParent(entityNodeRef).getParentRef());
							ProductData newProductData = alfrescoRepository.findOne(parentSimulationNodeRef);
							logger.debug("Create new SF " + newProductData.getName());

							for (AbstractProductDataView newView : newProductData.getViews()) {
								if (newView.getClass().getName().equals(view.getClass().getName())) {
									for (CompositionDataItem newItem : newView.getMainDataList()) {
										NodeRef origNodeRef = associationService.getTargetAssoc(newItem.getNodeRef(), ContentModel.ASSOC_ORIGINAL);
										if ((origNodeRef != null) && origNodeRef.equals(item.getNodeRef())) {
											associationService.update(newItem.getNodeRef(), item.getComponentAssocName(), simulationNodeRef);
											logger.debug("Update new SF " + newProductData.getName());
											return newProductData.getNodeRef();
										}
									}
								}
							}
						}
					}
				}
			}

		}

		if ((dataListItem != null) && dataListItemsNodeRefs.contains(dataListItem.getNodeRef())) {
			logger.debug("Found item to simulate:" + dataListItem.getNodeRef());
			return createCopyNodeRef(dataListItem.getComponent(), nodeService.getPrimaryParent(entityNodeRef).getParentRef());
		}

		return null;

	}

	private NodeRef createSimulationNodeRef(NodeRef entityNodeRef, NodeRef parentRef) {

		NodeRef simulationNodeRef = entityVersionService.createBranch(entityNodeRef, parentRef);

		nodeService.setProperty(simulationNodeRef, PLMModel.PROP_PRODUCT_STATE, SystemState.Simulation);
		if (nodeService.hasAspect(simulationNodeRef, PLMWorkflowModel.ASPECT_PRODUCT_VALIDATION_ASPECT)) {
			nodeService.removeAspect(simulationNodeRef, PLMWorkflowModel.ASPECT_PRODUCT_VALIDATION_ASPECT);
		}

		return simulationNodeRef;
	}

	private NodeRef createCopyNodeRef(NodeRef entityNodeRef, NodeRef parentRef) {

		NodeRef simulationNodeRef = copyService.copyAndRename(entityNodeRef, parentRef, ContentModel.ASSOC_CONTAINS, null, true);
		nodeService.setProperty(simulationNodeRef, PLMModel.PROP_PRODUCT_STATE, SystemState.Simulation);
		if (nodeService.hasAspect(simulationNodeRef, PLMWorkflowModel.ASPECT_PRODUCT_VALIDATION_ASPECT)) {
			nodeService.removeAspect(simulationNodeRef, PLMWorkflowModel.ASPECT_PRODUCT_VALIDATION_ASPECT);
		}
		return simulationNodeRef;

	}

}
