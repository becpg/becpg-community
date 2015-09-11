/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
package fr.becpg.repo.policy;

import java.util.Map;

import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DisableBehaviourOnCopy implements CopyServicePolicies.OnCopyNodePolicy,
CopyServicePolicies.OnCopyCompletePolicy {

	private final QName type;
	
	private final BehaviourFilter policyBehaviourFilter;
	
	private static final Log logger = LogFactory.getLog(DisableBehaviourOnCopy.class);
	
	public DisableBehaviourOnCopy(QName type, BehaviourFilter policyBehaviourFilter) {
		super();
		this.type = type;
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails) {
		return new EntityCopyBehaviourCallback();
	}

	private class EntityCopyBehaviourCallback extends DefaultCopyBehaviourCallback {

		@Override
		public boolean getMustCopy(QName classQName, CopyDetails copyDetails) {
			
			if(logger.isDebugEnabled()){
				logger.debug("DisableBehaviourOnCopy :"+classQName.toPrefixString());
				logger.debug("Force enable for transaction  :"+BeCPGPolicyHelper.isCopyBehaviourEnableForTransaction());
			}
			
			if(!BeCPGPolicyHelper.isCopyBehaviourEnableForTransaction()){
				NodeRef targetNodeRef = copyDetails.getTargetNodeRef();
				policyBehaviourFilter.disableBehaviour(targetNodeRef, type);
			}

			// Always copy
			return true;
		}
	}

	/**
	 * Re-enable aspect behaviour for the source node
	 */
	public void onCopyComplete(QName classRef, NodeRef sourceNodeRef, NodeRef destinationRef, boolean copyToNewNode, Map<NodeRef, NodeRef> copyMap) {
		if(!BeCPGPolicyHelper.isCopyBehaviourEnableForTransaction()){
			policyBehaviourFilter.enableBehaviour(destinationRef, type);
		}
	}

	
}
