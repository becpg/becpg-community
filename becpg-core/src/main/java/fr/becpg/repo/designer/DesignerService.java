package fr.becpg.repo.designer;

import java.io.InputStream;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.designer.data.ModelTree;

public interface DesignerService {

	public void createModelAspectNode(NodeRef parentNode, InputStream modelXml);

	public InputStream getXmlFromModelAspectNode(NodeRef dictionnaryModelNode);
	
	public ModelTree getModelTree(NodeRef modelNodeRef);
	
}
