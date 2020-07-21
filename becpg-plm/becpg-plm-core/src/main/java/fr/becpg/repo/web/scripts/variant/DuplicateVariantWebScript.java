/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG.
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
package fr.becpg.repo.web.scripts.variant;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.web.scripts.WebScriptUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.data.hierarchicalList.CompositeDataItem;
import fr.becpg.repo.product.data.AbstractProductDataView;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.ResourceParamListItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.model.CompositionDataItem;
import fr.becpg.repo.variant.filters.VariantFilters;
import fr.becpg.repo.variant.model.VariantDataItem;

/**
 * <p>DuplicateVariantWebScript class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class DuplicateVariantWebScript extends AbstractWebScript {

	/** Constant <code>PARAM_NODEREF="nodeRef"</code> */
	protected static final String PARAM_NODEREF = "nodeRef";

	private static final Log logger = LogFactory.getLog(DuplicateVariantWebScript.class);

	private NodeService nodeService;

	private ServiceRegistry serviceRegistry;

	private CopyService copyService;

	private AlfrescoRepository<ProductData> alfrescoRepository;

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>serviceRegistry</code>.</p>
	 *
	 * @param serviceRegistry a {@link org.alfresco.service.ServiceRegistry} object.
	 */
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	/**
	 * <p>Setter for the field <code>copyService</code>.</p>
	 *
	 * @param copyService a {@link org.alfresco.service.cmr.repository.CopyService} object.
	 */
	public void setCopyService(CopyService copyService) {
		this.copyService = copyService;
	}

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws WebScriptException, IOException {
		logger.debug("start duplicate variant webscript");

		/* Parse the JSON content */
		org.json.simple.JSONObject json = null;
		String contentType = req.getContentType();
		if ((contentType != null) && (contentType.indexOf(';') != -1)) {
			contentType = contentType.substring(0, contentType.indexOf(';'));
		}
		if (MimetypeMap.MIMETYPE_JSON.equals(contentType)) {
			JSONParser parser = new JSONParser();
			try {
				json = (org.json.simple.JSONObject) parser.parse(req.getContent().getContent());
			} catch (IOException io) {
				throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + io.getMessage());
			} catch (ParseException pe) {
				throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + pe.getMessage());
			}
		}

		String nodeRef = req.getParameter(PARAM_NODEREF);

		ParameterCheck.mandatoryString("nodeRef", nodeRef);
		NodeRef variantNodeRef = new NodeRef(nodeRef);
		if (nodeService.exists(variantNodeRef) && json!=null) {

			try {

				/* Parse the destination NodeRef parameter */
				String destinationNodeParam = (String) json.get("alf_destination");
				ParameterCheck.mandatoryString("destinationNodeParam", destinationNodeParam);
				NodeRef entityNodeRef = WebScriptUtil.resolveNodeReference(destinationNodeParam, serviceRegistry.getNodeLocatorService());

				String name = (String) json.get("prop_cm_name");
				ParameterCheck.mandatoryString("name", destinationNodeParam);

				ProductData productData = alfrescoRepository.findOne(entityNodeRef);

				Map<QName, Serializable> props = new HashMap<>();
				props.put(ContentModel.PROP_NAME, name);
				props.put(BeCPGModel.PROP_IS_DEFAULT_VARIANT, false);
				NodeRef newVariantNodeRef = nodeService
						.createNode(entityNodeRef, BeCPGModel.ASSOC_VARIANTS, BeCPGModel.ASSOC_VARIANTS, BeCPGModel.TYPE_VARIANT, props).getChildRef();

				VariantFilters<? extends VariantDataItem> variantFilters = new VariantFilters<>(variantNodeRef);

				for (AbstractProductDataView view : productData.getViews()) {
					if ((view.getMainDataList() != null) && !view.getMainDataList().isEmpty()) {

						@SuppressWarnings("unchecked")
						Predicate<CompositionDataItem> predicate = (Predicate<CompositionDataItem>) variantFilters.createPredicate(productData);
						duplicate(newVariantNodeRef, view.getMainDataList(),
								view.getMainDataList().stream().filter(predicate).collect(Collectors.toList()));
					}
				}

				if ((productData.getResourceParamList() != null) && !productData.getResourceParamList().isEmpty()) {

					@SuppressWarnings("unchecked")
					Predicate<ResourceParamListItem> predicate = (Predicate<ResourceParamListItem>) variantFilters.createPredicate(productData);

					duplicate(newVariantNodeRef, productData.getResourceParamList(),
							productData.getResourceParamList().stream().filter(predicate).collect(Collectors.toList()));

				}

				JSONObject ret = new JSONObject();

				ret.put("persistedObject", newVariantNodeRef);
				ret.put("status", "SUCCESS");

				res.setContentType("application/json");
				res.setContentEncoding("UTF-8");
				ret.write(res.getWriter());

			} catch (JSONException e) {
				throw new WebScriptException("Unable to serialize JSON", e);
			}

		}

	}

	@SuppressWarnings("unchecked")
	private void duplicate(NodeRef newVariantNodeRef, List<? extends VariantDataItem> origList, List<? extends VariantDataItem> listToCopy) {

		if (!origList.isEmpty() && !listToCopy.isEmpty()) {

			Integer lastSort = (Integer) nodeService.getProperty(origList.get(origList.size() - 1).getNodeRef(), BeCPGModel.PROP_SORT);
			if (lastSort == null) {
				lastSort = listToCopy.size() + 25;
			}

			Map<NodeRef, NodeRef> replacements = new HashMap<>();
			Map<NodeRef, NodeRef> parents = new HashMap<>();

			int i = 0;
			for (VariantDataItem dataListToCopy : listToCopy) {

				if (dataListToCopy.getVariants() != null) {
					Integer sort = (Integer) nodeService.getProperty(dataListToCopy.getNodeRef(), BeCPGModel.PROP_SORT);
					NodeRef newDLNodeRef = copyService.copy(dataListToCopy.getNodeRef(), dataListToCopy.getParentNodeRef(),
							ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS);

					replacements.put(dataListToCopy.getNodeRef(), newDLNodeRef);

					if ((dataListToCopy instanceof CompositeDataItem)
							&& (((CompositeDataItem<CompositionDataItem>) dataListToCopy).getParent() != null)) {
						parents.put(newDLNodeRef, ((CompositeDataItem<CompositionDataItem>) dataListToCopy).getParent().getNodeRef());
					}

					if (sort == null) {
						sort = i;
					}
					sort = sort + lastSort;
					nodeService.setProperty(newDLNodeRef, BeCPGModel.PROP_SORT, sort);
					nodeService.setProperty(newDLNodeRef, BeCPGModel.PROP_VARIANTIDS, new ArrayList<>(Arrays.asList(newVariantNodeRef)));

					i++;

				}
			}

			for (NodeRef parentToUpdate : parents.keySet()) {
				nodeService.setProperty(parentToUpdate, BeCPGModel.PROP_PARENT_LEVEL, replacements.get(parents.get(parentToUpdate)));
			}

		}

	}

}
