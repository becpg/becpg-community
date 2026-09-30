/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.product.policy;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.product.helper.IngListLegacyFlags;

/**
 * Folds the deprecated boolean properties of an ingredient line into bcpg:ingListFlags when a writer
 * still sets them: imports, remote clients or scripts written before the flags existed.
 *
 * @author matthieu
 */
public class IngListLegacyFlagsPolicy extends AbstractBeCPGPolicy implements OnUpdatePropertiesPolicy, OnCreateNodePolicy {

    private static final Log logger = LogFactory.getLog(IngListLegacyFlagsPolicy.class);

    private static final String KEY_LEGACY_FLAGS = "IngListLegacyFlagsPolicy.legacyFlags";

    /** {@inheritDoc} */
    @Override
    public void doInit() {
        policyComponent.bindClassBehaviour(OnUpdatePropertiesPolicy.QNAME, PLMModel.TYPE_INGLIST, new JavaBehaviour(this, "onUpdateProperties"));
        policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, PLMModel.TYPE_INGLIST, new JavaBehaviour(this, "onCreateNode"));
    }

    /** {@inheritDoc} */
    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        if (IngListLegacyFlags.containsLegacyProperty(after)) {
            queueNode(KEY_LEGACY_FLAGS, nodeRef);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef) {
        NodeRef nodeRef = childAssocRef.getChildRef();
        if (IngListLegacyFlags.containsLegacyProperty(nodeService.getProperties(nodeRef))) {
            queueNode(KEY_LEGACY_FLAGS, nodeRef);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
        if (!KEY_LEGACY_FLAGS.equals(key)) {
            return false;
        }
        for (NodeRef nodeRef : pendingNodes) {
            migrateLegacyFlags(nodeRef);
        }
        return true;
    }

    private void migrateLegacyFlags(NodeRef nodeRef) {
        if (!nodeService.exists(nodeRef) || isPendingDelete(nodeRef) || isVersionNode(nodeRef)) {
            return;
        }
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        if (IngListLegacyFlags.containsLegacyProperty(properties)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Folding legacy ingredient line flags into bcpg:ingListFlags for: " + nodeRef);
            }
            nodeService.setProperties(nodeRef, IngListLegacyFlags.migrate(properties));
        }
    }
}
