/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG.
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
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * <p>EffectivityAspectPolicy class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class EffectivityAspectPolicy extends AbstractBeCPGPolicy
		implements NodeServicePolicies.OnAddAspectPolicy, CopyServicePolicies.OnCopyNodePolicy {

	private EffectivityAspectCopyBehaviourCallback effectivityAspectCopyBehaviourCallback = new EffectivityAspectCopyBehaviourCallback();
	private EntityDictionaryService entityDictionaryService;

	/**
	 * <p>Setter for the field <code>entityDictionaryService</code>.</p>
	 *
	 * @param entityDictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object.
	 */
	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	/** {@inheritDoc} */
	@Override
	public void doInit() {
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME, BeCPGModel.ASPECT_EFFECTIVITY,
				new JavaBehaviour(this, "onAddAspect"));
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME, BeCPGModel.ASPECT_EFFECTIVITY,
				new JavaBehaviour(this, "getCopyCallback"));
	}

	/** {@inheritDoc} */
	@Override
	public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName) {
		queueNode(nodeRef);
	}

	/** {@inheritDoc} */
	@Override
	protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		for (NodeRef nodeRef : pendingNodes) {
			if (isNotLocked(nodeRef) && !isWorkingCopyOrVersion(nodeRef)) {
				if (nodeService.getProperty(nodeRef, BeCPGModel.PROP_START_EFFECTIVITY) == null) {
					Date startEffectivity = new Date();
					nodeService.setProperty(nodeRef, BeCPGModel.PROP_START_EFFECTIVITY, startEffectivity);
				}
			}
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails) {
		return effectivityAspectCopyBehaviourCallback;
	}

	private class EffectivityAspectCopyBehaviourCallback extends DefaultCopyBehaviourCallback {

		/**
		 * Don't copy certain auditable p
		 */
		@Override
		public Map<QName, Serializable> getCopyProperties(QName classQName, CopyDetails copyDetails, Map<QName, Serializable> properties) {

			if (classQName.equals(BeCPGModel.ASPECT_EFFECTIVITY)) {
				if (!entityDictionaryService.isSubClass(copyDetails.getSourceNodeTypeQName(), BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
					// Have the key properties reset by the aspect
					properties.remove(BeCPGModel.PROP_START_EFFECTIVITY);
				}
			}

			return properties;
		}

		/**
		 * Do copy the aspects
		 *
		 * @return Returns <tt>true</tt> always
		 */
		@Override
		public boolean getMustCopy(QName classQName, CopyDetails copyDetails) {
			return true;
		}
	}

}
