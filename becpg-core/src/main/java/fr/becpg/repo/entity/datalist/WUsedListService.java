package fr.becpg.repo.entity.datalist;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.entity.datalist.data.MultiLevelListData;

public interface WUsedListService {

	/**
     * Calculate the WUsed entities of the item
     * @param entityNodeRef item associated to datalists
     * @param associationName
     * @param maxDepthLevel
     */
    public MultiLevelListData getWUsedEntity(NodeRef entityNodeRef, QName associationName, int maxDepthLevel);
    
    /**
     * Calculate the WUsed entities of the items (compute AND Operator)
     * @param entityNodeRef item associated to datalists
     * @param associationName
     * @param maxDepthLevel
     */
    public MultiLevelListData getWUsedEntity(List<NodeRef> entityNodeRefs, QName associationQName, int maxDepthLevel);
    
    /**
     * Evaluate the WUsed associations
     * @param targetNodeRef
     * @return
     */
    public List<QName> evaluateWUsedAssociations(NodeRef targetAssocNodeRef);
    
    /**
     * Evaluate the list of the association
     * @param associationName
     * @return
     */
    public QName evaluateListFromAssociation(QName associationName);

	
    
}
