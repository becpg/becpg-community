package fr.becpg.repo.designer.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.designer.DesignerModel;
import fr.becpg.repo.designer.data.DesignerTree;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
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
				tmp = new DesignerTree(assoc.getParentRef().toString());
				tmp.setName(assocName);
				tmp.setType(assoc.getTypeQName().toPrefixString(namespaceService));
				String title = "";
				if(DesignerModel.M2_URI.equals(assoc.getTypeQName().getNamespaceURI())){
					title = I18NUtil.getMessage("m2_m2model.association.m2_" + assocName+".title");
				} else {
					title = I18NUtil.getMessage("dsg_designerModel.association.dsg_" + assocName+".title");
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

		if("dsg:configElement".equals(type)){
			name = (String) nodeService.getProperty(modelNodeRef, DesignerModel.PROP_DSG_ID); 
			String evaluator  =  (String)nodeService.getProperty(modelNodeRef, DesignerModel.PROP_DSG_CONFIGEVALUATOR);
			if(!StringUtils.isEmpty(evaluator)){
				name += " ("+evaluator+")";
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
		
		if("dsg:form".equals(type) && "-".equals(name)){
			name = "default";
		}

		tmp.setName(name);
		tmp.setTitle(title);
		tmp.setDescription(description);
		tmp.setType(type);
		
		if("dsg:form".equals(type)){
			String formType = (String) nodeService.getProperty(nodeService.getPrimaryParent(modelNodeRef).getParentRef(), DesignerModel.PROP_DSG_ID); 
			tmp.setFormType(formType);
			String evaluator  = (String) nodeService.getProperty(nodeService.getPrimaryParent(modelNodeRef).getParentRef(), DesignerModel.PROP_DSG_CONFIGEVALUATOR); 

			if(evaluator!=null){
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
		
		if(nodeService.hasAspect(modelNodeRef, DesignerModel.ASPECT_MODEL_ERROR)){
			tmp.setHasError(true);
		}
		
		return tmp;
	}



}
