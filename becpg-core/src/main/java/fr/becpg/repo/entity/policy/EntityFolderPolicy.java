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
	
	/** The node service. */
	private NodeService nodeService;
			
	/**
	 * Sets the policy component.
	 *
	 * @param policyComponent the new policy component
	 */
	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}
	
	/**
	 * Sets the node service.
	 *
	 * @param nodeService the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}		
	
	/**
	 * Inits the.
	 */
	public void init(){
		logger.debug("Init EntityFolderPolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, BeCPGModel.TYPE_SUPPLIER, new JavaBehaviour(this, "onCreateNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, BeCPGModel.TYPE_CLIENT, new JavaBehaviour(this, "onCreateNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, QualityModel.TYPE_QUALITY_CONTROL, new JavaBehaviour(this, "onCreateNode"));
	}

	/**
	 * Create an entity folder if needed
	 */
	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		
		NodeRef parentNodeRef = childAssocRef.getParentRef();
		NodeRef nodeNodeRef = childAssocRef.getChildRef();		
		QName parentType = nodeService.getType(parentNodeRef);
		
		if(!parentType.equals(BeCPGModel.TYPE_ENTITY_FOLDER)){
			
			String nodeName = (String)nodeService.getProperty(nodeNodeRef, ContentModel.PROP_NAME);
			nodeService.setProperty(nodeNodeRef, ContentModel.PROP_NAME, GUID.generate());
			
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, nodeName);
			
			NodeRef newFolderNodeRef = nodeService.createNode(parentNodeRef, 
									ContentModel.ASSOC_CONTAINS,
									QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, nodeName),
									BeCPGModel.TYPE_ENTITY_FOLDER, properties).getChildRef();			
			
			//move node in newfolder and rename node
			nodeService.moveNode(nodeNodeRef, newFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, nodeName));
			nodeService.setProperty(nodeNodeRef, ContentModel.PROP_NAME, nodeName);
		}
	}
	
}
