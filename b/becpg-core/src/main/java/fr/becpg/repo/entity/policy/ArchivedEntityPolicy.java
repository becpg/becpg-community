package fr.becpg.repo.entity.policy;

import org.alfresco.repo.node.NodeServicePolicies.OnAddAspectPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnRemoveAspectPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityFormatService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

public class ArchivedEntityPolicy extends AbstractBeCPGPolicy implements OnAddAspectPolicy, OnRemoveAspectPolicy {

	private EntityFormatService entityFormatService;
	
	public void setEntityFormatService(EntityFormatService entityFormatService) {
		this.entityFormatService = entityFormatService;
	}
	
	@Override
	public void doInit() {
		this.policyComponent.bindClassBehaviour(OnAddAspectPolicy.QNAME, BeCPGModel.ASPECT_ARCHIVED_ENTITY, new JavaBehaviour(this, "onAddAspect"));
		this.policyComponent.bindClassBehaviour(OnRemoveAspectPolicy.QNAME, BeCPGModel.ASPECT_ARCHIVED_ENTITY, new JavaBehaviour(this, "onRemoveAspect"));
	}
	
	@Override
	public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName) {
		
	}

	@Override
	public void onRemoveAspect(NodeRef nodeRef, QName aspectTypeQName) {
		
	}
	
}
