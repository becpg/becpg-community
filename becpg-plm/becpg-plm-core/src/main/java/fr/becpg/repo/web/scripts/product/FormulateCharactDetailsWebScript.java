/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG.
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
package fr.becpg.repo.web.scripts.product;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.webdav.WebDAVHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.helper.AttachmentHelper;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.product.data.CharactDetails;

/**
 * return List details
 * 
 * @author matthieu
 *
 */
public class FormulateCharactDetailsWebScript extends AbstractProductWebscript {

	private static final Log logger = LogFactory.getLog(FormulateCharactDetailsWebScript.class);

	private static final String PARAM_DATA_LIST_NAME = "dataListName";

	private static final String PARAM_ITEM_TYPE = "itemType";

	private static final String PARAM_DATALISTITEMS = "dataListItems";

	private static final String PARAM_ENTITY_NODEREF = "entityNodeRef";

	private static final String PARAM_ITEM_LEVEL = "level";

	private NodeService nodeService;

	private NamespaceService namespaceService;

	private AttributeExtractorService attributeExtractorService;

	public void setAttributeExtractorService(AttributeExtractorService attributeExtractorService) {
		this.attributeExtractorService = attributeExtractorService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		String dataListName = req.getParameter(PARAM_DATA_LIST_NAME);
		String itemType = req.getParameter(PARAM_ITEM_TYPE);
		String entityNodeRef = req.getParameter(PARAM_ENTITY_NODEREF);
		QName dataType = QName.createQName(itemType, namespaceService);

		String dataListItems = req.getParameter(PARAM_DATALISTITEMS);
		String itemLevel = req.getParameter(PARAM_ITEM_LEVEL);

		Integer level = null;

		if (itemLevel != null) {
			if ("All".equals(itemLevel)) {
				level = -1;
			} else {
				level = Integer.parseInt(itemLevel) - 1;
			}
		}

		List<NodeRef> elements = new ArrayList<>();
		if ((dataListItems != null) && (dataListItems.length() > 0)) {

			for (String nodeRef : dataListItems.split(",")) {
				elements.add(new NodeRef(nodeRef));
			}

		}

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		NodeRef productNodeRef = new NodeRef(entityNodeRef);

		try {

			CharactDetails ret = productService.formulateDetails(productNodeRef, dataType, dataListName, elements, level);

			if (elements.size() == 1 && dataListName.equals(PLMModel.TYPE_COSTLIST.getLocalName())) {
				List<ChildAssociationRef> childAssocs = nodeService.getChildAssocsByPropertyValue(
						nodeService.getPrimaryParent(elements.get(0)).getParentRef(), BeCPGModel.PROP_PARENT_LEVEL, elements.get(0));

				if (childAssocs.size() > 0) {
					ret = new CharactDetails(new ArrayList<NodeRef>());
				}
			}

			if ("csv".equals(req.getFormat())) {
				res.setContentType("application/vnd.ms-excel");
				AttachmentHelper.setAttachment(req, res, (String) nodeService.getProperty(productNodeRef, ContentModel.PROP_NAME) + ".xlsx");

				CharactDetailsHelper.writeXLS(ret, nodeService, attributeExtractorService, res.getOutputStream());
			} else {
				res.setContentType("application/json");
				res.setContentEncoding("UTF-8");
				res.getWriter().write(CharactDetailsHelper.toJSONObject(ret, nodeService, attributeExtractorService).toString(3));
			}

		} catch (FormulateException e) {
			handleFormulationError(e);

		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		} finally {
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug("FormulateCharactDetailsWebScript execute in " + watch.getTotalTimeSeconds() + "s");
			}
		}

	}

}
