package fr.becpg.repo.product.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.MPMModel;
import fr.becpg.model.PLMModel;

/**
 * Resolves where-used pivot associations from source item types.
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("wUsedAssociationResolver")
public class WUsedAssociationResolver {

    private final NodeService nodeService;

    @Autowired
    /**
     * <p>Constructor for WUsedAssociationResolver.</p>
     *
     * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
     */
    public WUsedAssociationResolver(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * Keeps common where-used associations for all source items.
     *
     * @param sourceList source items
     * @return common where-used associations
     */
    public List<QName> evaluateWUsedAssociations(List<NodeRef> sourceList) {
        List<QName> associationQNames = null;

        for (NodeRef sourceNodeRef : sourceList) {
            if (associationQNames == null) {
                associationQNames = evaluateWUsedAssociations(sourceNodeRef);
            } else {
                associationQNames.retainAll(evaluateWUsedAssociations(sourceNodeRef));
            }
        }

        if (associationQNames == null) {
            return Collections.emptyList();
        }

        return associationQNames;
    }

    /**
     * Resolves where-used pivot associations from one source item type.
     *
     * @param sourceNodeRef source item node reference
     * @return where-used associations
     */
    public List<QName> evaluateWUsedAssociations(NodeRef sourceNodeRef) {
        List<QName> wUsedAssociations = new ArrayList<>();

        QName nodeType = nodeService.getType(sourceNodeRef);

        if (nodeType.isMatch(PLMModel.TYPE_RAWMATERIAL) || nodeType.isMatch(PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT)
                || nodeType.isMatch(PLMModel.TYPE_SEMIFINISHEDPRODUCT) || nodeType.isMatch(PLMModel.TYPE_FINISHEDPRODUCT)
                || nodeType.isMatch(PLMModel.TYPE_LOGISTICUNIT)) {
            wUsedAssociations.add(PLMModel.ASSOC_COMPOLIST_PRODUCT);
        } else if (nodeType.isMatch(PLMModel.TYPE_PACKAGINGMATERIAL) || nodeType.isMatch(PLMModel.TYPE_PACKAGINGKIT)) {
            wUsedAssociations.add(PLMModel.ASSOC_PACKAGINGLIST_PRODUCT);
        } else if (nodeType.isMatch(PLMModel.TYPE_RESOURCEPRODUCT)) {
            wUsedAssociations.add(MPMModel.ASSOC_PL_RESOURCE);
        }

        return wUsedAssociations;
    }
}
