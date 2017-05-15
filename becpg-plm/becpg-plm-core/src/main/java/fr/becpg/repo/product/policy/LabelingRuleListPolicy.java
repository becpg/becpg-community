/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG.
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

import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

public class LabelingRuleListPolicy extends AbstractBeCPGPolicy implements CopyServicePolicies.OnCopyNodePolicy {

	private EntityListDAO entityListDAO;

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	@Override
	public void doInit() {
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME, PLMModel.TYPE_LABELING_RULE_LIST,
				new JavaBehaviour(this, "getCopyCallback"));
	}

	@Override
	public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails) {
		return new LabelingRuleListBehaviourCallback();
	}

	private class LabelingRuleListBehaviourCallback extends DefaultCopyBehaviourCallback {

		private LabelingRuleListBehaviourCallback() {
		}

		@Override
		public boolean getMustCopy(QName classQName, CopyDetails copyDetails) {
			if (nodeService.hasAspect(entityListDAO.getEntity(copyDetails.getSourceNodeRef()), BeCPGModel.ASPECT_ENTITY_TPL)
					&& !Boolean.TRUE.equals(copyDetails.getSourceNodeProperties().get(BeCPGModel.PROP_IS_MANUAL_LISTITEM))) {
				NodeRef targetNodeRef = copyDetails.getTargetParentNodeRef();
				if ((targetNodeRef != null) && nodeService.exists(targetNodeRef)) {
					NodeRef targetEntity = entityListDAO.getEntityFromList(targetNodeRef);
					if ((targetEntity != null) && nodeService.hasAspect(targetEntity, BeCPGModel.ASPECT_ENTITY_TPL)) {
						return true;
					}
				}

				return false;
			}
			return true;
		}

		@Override
		public Map<QName, Serializable> getCopyProperties(QName classQName, CopyDetails copyDetails, Map<QName, Serializable> properties) {
			properties.put(BeCPGModel.PROP_IS_MANUAL_LISTITEM, Boolean.TRUE);
			return properties;
		}
	}

}
