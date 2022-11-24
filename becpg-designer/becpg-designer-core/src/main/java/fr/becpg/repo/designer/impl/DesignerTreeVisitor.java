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
package fr.becpg.repo.designer.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.designer.DesignerModel;
import fr.becpg.repo.designer.data.DesignerTree;

/**
 * <p>DesignerTreeVisitor class.</p>
 *
 * @author "Matthieu Laborie"
 * @version $Id: $Id
 */
public class DesignerTreeVisitor {

	private NodeService nodeService;

	private NamespaceService namespaceService;

	/**
	 * <p>Getter for the field <code>nodeService</code>.</p>
	 *
	 * @return the nodeService
	 */
	public NodeService getNodeService() {
		return nodeService;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService
	 *            the nodeService to set
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Getter for the field <code>namespaceService</code>.</p>
	 *
	 * @return the namespaceService
	 */
	public NamespaceService getNamespaceService() {
		return namespaceService;
	}

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService
	 *            the namespaceService to set
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * <p>visitModelTreeNodeRef.</p>
	 *
	 * @param modelNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link fr.becpg.repo.designer.data.DesignerTree} object.
	 */
	public DesignerTree visitModelTreeNodeRef(NodeRef modelNodeRef) {
		DesignerTree ret = extractModelTreeNode(modelNodeRef);
		List<ChildAssociationRef> assocs = nodeService.getChildAssocs(modelNodeRef);
		DesignerHelper.sort(assocs,nodeService);
		Map<String, DesignerTree> assocRoots = new HashMap<>();
		for (ChildAssociationRef assoc : assocs) {
			String assocName = assoc.getQName().getLocalName();
			DesignerTree tmp;
			if (assocRoots.containsKey(assocName)) {
				tmp = assocRoots.get(assocName);
			} else {
				tmp = new DesignerTree(assoc.getParentRef().toString());
				tmp.setName(assocName);
				tmp.setType(assoc.getTypeQName().toPrefixString(namespaceService));
				String title;
				if (DesignerModel.M2_URI.equals(assoc.getTypeQName().getNamespaceURI())) {
					title = I18NUtil.getMessage("m2_m2model.association.m2_" + assocName + ".title");
				} else {
					title = I18NUtil.getMessage("dsg_designerModel.association.dsg_" + assocName + ".title");
				}

				tmp.setTitle(title);

				tmp.setFormId("assoc");
				tmp.setSubType(nodeService.getType(assoc.getChildRef()).toPrefixString(namespaceService));

				assocRoots.put(assocName, tmp);
				ret.getChildrens().add(tmp);
			}
			tmp.getChildrens().add(visitModelTreeNodeRef(assoc.getChildRef()));

		}

		return ret;
	}

	private DesignerTree extractModelTreeNode(NodeRef modelNodeRef) {
		DesignerTree tmp = new DesignerTree(modelNodeRef.toString());
		String type = nodeService.getType(modelNodeRef).toPrefixString(namespaceService);
		String name = (String) nodeService.getProperty(modelNodeRef, DesignerModel.PROP_M2_NAME);
		String title = (String) nodeService.getProperty(modelNodeRef, DesignerModel.PROP_M2_TITLE);
		String description = (String) nodeService.getProperty(modelNodeRef, DesignerModel.PROP_M2_DESCRIPTION);

		if ("dsg:configElement".equals(type)) {
			name = (String) nodeService.getProperty(modelNodeRef, DesignerModel.PROP_DSG_ID);
			String evaluator = (String) nodeService.getProperty(modelNodeRef, DesignerModel.PROP_DSG_CONFIGEVALUATOR);
			if (!StringUtils.isEmpty(evaluator)) {
				name += " (" + evaluator + ")";
			}
		}

		if (name == null) {
			name = (String) nodeService.getProperty(modelNodeRef, DesignerModel.PROP_M2_URI);
		}

		if (name == null || name.isEmpty()) {
			name = (String) nodeService.getProperty(modelNodeRef, DesignerModel.PROP_M2_REF);
		}

		if (name == null || name.isEmpty()) {
			name = (String) nodeService.getProperty(modelNodeRef, DesignerModel.PROP_DSG_ID);
		}

		if (name == null || name.isEmpty()) {
			name = "-";
		}

		if ("dsg:form".equals(type) && "-".equals(name)) {
			name = "default";
		}

		tmp.setName(name);
		tmp.setTitle(title);
		tmp.setDescription(description);
		tmp.setType(type);

		if ("dsg:form".equals(type)) {
			String formType = (String) nodeService.getProperty(nodeService.getPrimaryParent(modelNodeRef).getParentRef(), DesignerModel.PROP_DSG_ID);
			tmp.setFormType(formType);
			String evaluator = (String) nodeService.getProperty(nodeService.getPrimaryParent(modelNodeRef).getParentRef(),
					DesignerModel.PROP_DSG_CONFIGEVALUATOR);

			if (evaluator != null) {
				switch (evaluator) {
				case "string-compare":
					tmp.setFormKind("workflow");
					break;
				case "task-type":
					tmp.setFormKind("task");
					break;
				case "model-type":
					tmp.setFormKind("type");
					break;
				case "node-type":
					tmp.setFormKind("node");
					break;
				default:
					break;
				}

			}

		}

		if (nodeService.hasAspect(modelNodeRef, DesignerModel.ASPECT_MODEL_ERROR)) {
			tmp.setHasError(true);
		}

		return tmp;
	}

}
