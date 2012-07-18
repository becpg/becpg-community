package fr.becpg.repo.entity.policy;

import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.version.VersionServicePolicies;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * Create version of type entity
 * @author quere
 *
 */
@Service
public class EntityVersionServicePolicy extends AbstractBeCPGPolicy implements VersionServicePolicies.AfterCreateVersionPolicy {

	private static Log logger = LogFactory.getLog(EntityVersionServicePolicy.class);

	private EntityVersionService entityVersionService;
	
	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}

	/**
	 * Inits the.
	 */
	public void doInit(){
		logger.debug("Init EntityVersionServicePolicy...");
		policyComponent.bindClassBehaviour(VersionServicePolicies.AfterCreateVersionPolicy.QNAME, BeCPGModel.TYPE_ENTITY, new JavaBehaviour(this, "afterCreateVersion"));		
	}
	
	@Override
	public void afterCreateVersion(NodeRef nodeRef, Version version) {
		
		logger.debug("afterCreateVersion: " + nodeRef);
		
		//create new version
        entityVersionService.createEntityVersion(nodeRef, version);        		
	}
}
