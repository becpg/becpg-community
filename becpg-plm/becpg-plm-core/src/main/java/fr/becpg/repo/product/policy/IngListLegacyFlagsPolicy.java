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
import java.util.Collections;
import java.util.Map;

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
 * Keeps the deprecated boolean properties of an ingredient line and bcpg:ingListFlags in sync, both
 * ways: ticking a flag fills the matching boolean, and a writer that still sets a boolean (custom form,
 * import, connector, script) fills the matching flag. See {@link IngListLegacyFlags} for the conflict rule.
 *
 * <p>The sync is computed when the properties change, because the rule needs the values before the
 * update. Writing the result fires this policy again, which then finds both sides in agreement.</p>
 *
 * @author matthieu
 */
public class IngListLegacyFlagsPolicy extends AbstractBeCPGPolicy implements OnUpdatePropertiesPolicy, OnCreateNodePolicy {

    private static final Log logger = LogFactory.getLog(IngListLegacyFlagsPolicy.class);

    /** {@inheritDoc} */
    @Override
    public void doInit() {
        policyComponent.bindClassBehaviour(OnUpdatePropertiesPolicy.QNAME, PLMModel.TYPE_INGLIST, new JavaBehaviour(this, "onUpdateProperties"));
        policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, PLMModel.TYPE_INGLIST, new JavaBehaviour(this, "onCreateNode"));
    }

    /** {@inheritDoc} */
    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        synchronize(nodeRef, before, after);
    }

    /** {@inheritDoc} */
    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef) {
        NodeRef nodeRef = childAssocRef.getChildRef();
        synchronize(nodeRef, Collections.emptyMap(), nodeService.getProperties(nodeRef));
    }

    private void synchronize(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        if (!nodeService.exists(nodeRef) || isVersionNode(nodeRef) || isPendingDelete(nodeRef)) {
            return;
        }
        Map<QName, Serializable> changes = IngListLegacyFlags.synchronize(before, after);
        if (!changes.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Syncing ingredient line flags and legacy booleans of " + nodeRef + ": " + changes);
            }
            nodeService.addProperties(nodeRef, changes);
        }
    }
}
