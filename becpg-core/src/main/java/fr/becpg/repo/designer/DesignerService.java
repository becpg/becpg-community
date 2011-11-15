package fr.becpg.repo.designer;

import java.io.InputStream;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.designer.data.ModelTree;

public interface DesignerService {

	public NodeRef createModelAspectNode(NodeRef parentNode, InputStream modelXml);

	public void writeXmlFromModelAspectNode(NodeRef dictionnaryModelNode);
	
	public ModelTree getModelTree(NodeRef modelNodeRef);

	public NodeRef createModelAspectNode(NodeRef dictionaryModelNodeRef);
	
}
