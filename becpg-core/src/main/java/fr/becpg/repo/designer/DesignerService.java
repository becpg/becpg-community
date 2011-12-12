package fr.becpg.repo.designer;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.designer.data.FormControl;
import fr.becpg.repo.designer.data.ModelTree;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public interface DesignerService {

	public NodeRef createModelAspectNode(NodeRef parentNode, InputStream modelXml);

	public void writeXmlFromModelAspectNode(NodeRef dictionnaryModelNode);
	
	public ModelTree getModelTree(NodeRef modelNodeRef);

	public NodeRef createModelAspectNode(NodeRef dictionaryModelNodeRef);
	
	public NodeRef createModelElement(NodeRef parentNodeRef, QName typeName, QName assocName, Map<QName,Serializable> props, String modelTemplate);

	public String prefixName(NodeRef elementRef, String name);

	public NodeRef findModelNodeRef(NodeRef nodeRef);

	void publish(NodeRef dictionaryModelNodeRef);

	public List<FormControl> getFormControls();

	public NodeRef moveElement(NodeRef from, NodeRef to);

	
}
