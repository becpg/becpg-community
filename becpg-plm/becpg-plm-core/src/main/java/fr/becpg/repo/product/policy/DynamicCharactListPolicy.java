/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG. 
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

import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

public class DynamicCharactListPolicy extends AbstractBeCPGPolicy implements CopyServicePolicies.OnCopyNodePolicy {

	private EntityListDAO entityListDAO;
	
	
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void doInit() {
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME, PLMModel.TYPE_DYNAMICCHARACTLIST, new JavaBehaviour(this, "getCopyCallback"));
	}

	@Override
	public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails) {
		return new DynamicCharactListBehaviourCallback();
	}

	private class DynamicCharactListBehaviourCallback extends DefaultCopyBehaviourCallback {

		private DynamicCharactListBehaviourCallback() {
		}

		@Override
		public boolean getMustCopy(QName classQName, CopyDetails copyDetails) {	
			String state = (String)nodeService.getProperty(copyDetails.getSourceNodeRef(), PLMModel.PROP_DYNAMICCHARACT_SYNCHRONIZABLE_STATE);
			if(state != null && state.equals("Template") 
					&& !nodeService.hasAspect(entityListDAO.getEntityFromList(copyDetails.getTargetParentNodeRef()), BeCPGModel.ASPECT_ENTITY_TPL)){
				return false;
			}			
			return true;
		}
	}

}
