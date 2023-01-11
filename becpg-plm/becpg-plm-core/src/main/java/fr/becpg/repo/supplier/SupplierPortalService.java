package fr.becpg.repo.supplier;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public interface SupplierPortalService {

	NodeRef createSupplierProject(NodeRef entityNodeRef, NodeRef projectTemplateNodeRef, List<NodeRef> supplierAccountNodeRefs);
	
	NodeRef getSupplierNodeRef(NodeRef entityNodeRef);
	
	NodeRef getOrCreateSupplierDestFolder(NodeRef supplierNodeRef, List<NodeRef> resources);
	
	String createName(NodeRef entityNodeRef, NodeRef supplierNodeRef, String nameFormat, Date currentDate);
	
	String getProjectNameTpl();
	
	String getEntityNameTpl();
	
	NodeRef createExternalUser(String email, String firstName, String lastName, boolean notify, Map<QName, Serializable> extraProps);
	
	NodeRef prepareSignatureProject(NodeRef project, List<NodeRef> documentNodeRefs);
	
	NodeRef prepareSupplierSignatures(NodeRef projectNodeRef, NodeRef taskNodeRef);
}
