package fr.becpg.repo.variant.model;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

public interface VariantDataItem {

	public List<NodeRef> getVariants();

	public void setVariants(List<NodeRef> nodeRefs);
	
}
