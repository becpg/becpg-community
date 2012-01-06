package fr.becpg.repo.designer.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.designer.DesignerModel;
import fr.becpg.repo.designer.data.DesignerTree;

public class DesignerTreeVisitor {
	
	private NodeService nodeService;

	private NamespaceService namespaceService;

	/**
	 * @return the nodeService
	 */
	public NodeService getNodeService() {
		return nodeService;
	}

	/**
	 * @param nodeService
	 *            the nodeService to set
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * @return the namespaceService
	 */
	public NamespaceService getNamespaceService() {
		return namespaceService;
	}

	/**
	 * @param namespaceService
	 *            the namespaceService to set
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}


	public DesignerTree visitModelTreeNodeRef(NodeRef modelNodeRef) {
		DesignerTree ret = extractModelTreeNode(modelNodeRef);
		List<ChildAssociationRef> assocs = nodeService.getChildAssocs(modelNodeRef);
		Map<String, DesignerTree> assocRoots = new HashMap<String, DesignerTree>();
		for (ChildAssociationRef assoc : assocs) {
			String assocName = assoc.getQName().getLocalName();
			DesignerTree tmp = null;
			if (assocRoots.containsKey(assocName)) {
				tmp = assocRoots.get(assocName);
			} else {
				tmp = new DesignerTree();
				tmp.setName(assocName);
				tmp.setType(assoc.getTypeQName().toPrefixString(namespaceService));
				String title = "";
				if(DesignerModel.M2_URI.equals(assoc.getTypeQName().getNamespaceURI())){
					title = I18NUtil.getMessage("m2_m2model.association.m2_" + assocName+".title");
				} else {
					title = I18NUtil.getMessage("dsg_designerModel.association.dsg_" + assocName+".title");
				}
				
				tmp.setTitle(title);
				
				tmp.setNodeRef(assoc.getParentRef().toString());
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
		String name = (String) nodeService.getProperty(modelNodeRef, DesignerModel.PROP_M2_NAME);
		String title = (String) nodeService.getProperty(modelNodeRef, DesignerModel.PROP_M2_TITLE);
		String description = (String) nodeService.getProperty(modelNodeRef, DesignerModel.PROP_M2_DESCRIPTION);

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

		tmp.setName(name);
		tmp.setTitle(title);
		tmp.setDescription(description);
		tmp.setType(nodeService.getType(modelNodeRef).toPrefixString(namespaceService));
		
		
		if(nodeService.hasAspect(modelNodeRef, DesignerModel.ASPECT_MODEL_ERROR)){
			tmp.setHasError(true);
		}
		
		return tmp;
	}



}
