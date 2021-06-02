/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
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
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * <p>CostListPolicy class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class CostListPolicy extends AbstractBeCPGPolicy implements CopyServicePolicies.OnCopyNodePolicy {

	private EntityListDAO entityListDAO;
	
	private AssociationService associationService;

	/**
	 * <p>Setter for the field <code>entityListDAO</code>.</p>
	 *
	 * @param entityListDAO a {@link fr.becpg.repo.entity.EntityListDAO} object.
	 */
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object.
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	/**
	 * <p>doInit.</p>
	 */
	public void doInit() {
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME, PLMModel.TYPE_COSTLIST, new JavaBehaviour(this, "getCopyCallback"));
	}


	/** {@inheritDoc} */
	@Override
	public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails) {
		return new CostListBehaviourCallback();
	}

	private class CostListBehaviourCallback extends DefaultCopyBehaviourCallback {

		private CostListBehaviourCallback() {
		}

		@Override
		public boolean getMustCopy(QName classQName, CopyDetails copyDetails) {			
			NodeRef entityNodeRef = entityListDAO.getEntity(copyDetails.getSourceNodeRef());
			
			if (nodeService.exists(entityNodeRef) && nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)
					&& !associationService.getTargetAssocs(copyDetails.getSourceNodeRef(), PLMModel.ASSOC_PLANTS).isEmpty()) {
				return false;
			}
			return true;
		}
	}

}
