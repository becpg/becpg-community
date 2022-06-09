package fr.becpg.repo.supplier;

import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

public interface SupplierPortalService {

	NodeRef createSupplierProject(NodeRef entityNodeRef, NodeRef projectTemplateNodeRef, List<NodeRef> supplierAccountNodeRefs);
	NodeRef getSupplierNodeRef(NodeRef entityNodeRef);
	NodeRef getSupplierDestFolder(NodeRef supplierNodeRef);
	NodeRef getOrCreateSupplierDestFolder(NodeRef supplierNodeRef, List<NodeRef> resources);
	String createName(NodeRef entityNodeRef, NodeRef supplierNodeRef, String nameFormat, Date currentDate);
	
	String getProjectNameTpl();
	String getEntityNameTpl();
}
