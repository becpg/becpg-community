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
package fr.becpg.repo.quality.policy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;

import fr.becpg.model.QualityModel;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.quality.NonConformityService;

/**
 * <p>NonConformityPolicies class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class NonConformityPolicies extends AbstractBeCPGPolicy implements NodeServicePolicies.BeforeDeleteNodePolicy {


	List<NodeRef> currentDeletedNodes = new ArrayList<>(); 
	
	private NonConformityService nonConformityService;

	/**
	 * <p>Setter for the field <code>nonConformityService</code>.</p>
	 *
	 * @param nonConformityService a {@link fr.becpg.repo.quality.NonConformityService} object.
	 */
	public void setNonConformityService(NonConformityService nonConformityService) {
		this.nonConformityService = nonConformityService;
	}

	/** {@inheritDoc} */
	@Override
	public void doInit() {

		policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, QualityModel.TYPE_NC, new JavaBehaviour(this, "beforeDeleteNode"));

	}


	/** {@inheritDoc} */
	public void beforeDeleteNode(NodeRef ncNodeRef) {
		for(String instance : nonConformityService.getAssociatedWorkflow(ncNodeRef)){
			queueNode(new NodeRef(new StoreRef("tmp", "wfInstance"), instance));
		}
	}

	
	
	
	
	/** {@inheritDoc} */
	@Override
	protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		List<String> instanceIds = new ArrayList<>();
		for(NodeRef tmp : pendingNodes){
			instanceIds.add(tmp.getId());
		}
		
		nonConformityService.deleteWorkflows(instanceIds);
		return true;
	}
	
}
