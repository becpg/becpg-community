/*
 * 
 */
package fr.becpg.repo.entity.policy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.QualityModel;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.product.ProductService;

/**
 * The Class EntityFolderPolicy.
 *
 * @author querephi
 */
public class EntityFolderPolicy implements NodeServicePolicies.OnCreateNodePolicy {	
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(EntityFolderPolicy.class);
	
	/** The policy component. */
	private PolicyComponent policyComponent;		
	
	private EntityService entityService;
			
	/**
	 * Sets the policy component.
	 *
	 * @param policyComponent the new policy component
	 */
	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}
	
	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	/**
	 * Inits the.
	 */
	public void init(){
		logger.debug("Init EntityFolderPolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, BeCPGModel.TYPE_ENTITY, new JavaBehaviour(this, "onCreateNode"));
	}

	/**
	 * Create an entity folder if needed
	 */
	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {		
		
		NodeRef entityNodeRef = childAssocRef.getChildRef();
		
		entityService.initializeEntityFolder(entityNodeRef);
	}
	
}
