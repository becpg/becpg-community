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
package fr.becpg.repo.entity.policy;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * <p>Cleans up the {@code cm:taggable} aspect when the last tag is removed from a beCPG entity.</p>
 *
 * <p>Alfresco does not automatically remove the {@code cm:taggable} aspect when the
 * {@code cm:taggable} property becomes empty. As a result the Solr {@code TAG}
 * index keeps matching the entity and filters (e.g. project list filter by tag)
 * still return the node even though it no longer carries any tag.</p>
 *
 * <p>Removing the aspect when the property is cleared forces a full re-index of the
 * node and therefore purges the {@code TAG} field from the index.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class TagCleanupPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy {

    private static final Log logger = LogFactory.getLog(TagCleanupPolicy.class);

    /** {@inheritDoc} */
    @Override
    public void doInit() {
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, BeCPGModel.TYPE_ENTITY_V2,
                new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT));
    }

    /** {@inheritDoc} */
    @Override
    public void onUpdateProperties(final NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        if (!nodeService.exists(nodeRef) || isWorkingCopyOrVersion(nodeRef)) {
            return;
        }

        if (!hadTags(before) || hasTags(after)) {
            return;
        }

        if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TAGGABLE)) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Removing cm:taggable aspect from node " + nodeRef + " after last tag removal");
        }

        AuthenticationUtil.runAsSystem(() -> {
            nodeService.removeAspect(nodeRef, ContentModel.ASPECT_TAGGABLE);
            return null;
        });
    }

    private static boolean hadTags(Map<QName, Serializable> before) {
        return isNotEmpty(before.get(ContentModel.PROP_TAGS));
    }

    private static boolean hasTags(Map<QName, Serializable> after) {
        return isNotEmpty(after.get(ContentModel.PROP_TAGS));
    }

    private static boolean isNotEmpty(Serializable value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Collection<?> collection) {
            return !collection.isEmpty();
        }
        return true;
    }

}
