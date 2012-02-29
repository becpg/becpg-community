package fr.becpg.repo.entity.policy;

import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.version.VersionServicePolicies;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.version.EntityVersionService;

/**
 * Create version of type entity
 * @author quere
 *
 */
public class EntityVersionServicePolicy implements VersionServicePolicies.AfterCreateVersionPolicy {

	private static Log logger = LogFactory.getLog(EntityVersionServicePolicy.class);

	private PolicyComponent policyComponent;
	private BehaviourFilter policyBehaviourFilter;
	private EntityVersionService entityVersionService;
	
	
	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}

	/**
	 * Inits the.
	 */
	public void init(){
		logger.debug("Init EntityVersionServicePolicy...");
		policyComponent.bindClassBehaviour(VersionServicePolicies.AfterCreateVersionPolicy.QNAME, BeCPGModel.TYPE_ENTITY, new JavaBehaviour(this, "afterCreateVersion"));		
	}
	
	@Override
	public void afterCreateVersion(NodeRef nodeRef, Version version) {
		
		logger.debug("afterCreateVersion: " + nodeRef);
		
		// disable policy to avoid the creation of a new code
        policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_CODE);
        policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITY); 
		
        try{
        	//create new version
            entityVersionService.createEntityVersion(nodeRef, version);
        }
        finally{
        	//enable policies
    		policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_CODE);
            policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITY);	
        }
		
	}
}
