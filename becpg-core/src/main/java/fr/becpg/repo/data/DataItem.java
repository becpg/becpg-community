package fr.becpg.repo.data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Interface for data item
 * @author quere
 *
 */
public interface DataItem {

    public abstract NodeRef getNodeRef();
    public abstract void setNodeRef(NodeRef nodeRef);
    public abstract Map<QName, Serializable> getProperties();
    public abstract Map<QName, NodeRef> getSingleAssociations();	
    public abstract Map<QName, List<NodeRef>> getMultipleAssociations();
}
