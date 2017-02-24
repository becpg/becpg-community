package fr.becpg.repo.designer.policy;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.designer.DesignerModel;
import fr.becpg.repo.designer.DesignerService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

public class DesignerContentUpdatePolicy extends AbstractBeCPGPolicy implements ContentServicePolicies.OnContentUpdatePolicy,
		NodeServicePolicies.OnUpdatePropertiesPolicy, NodeServicePolicies.BeforeDeleteNodePolicy{

	private static final Log logger = LogFactory.getLog(DesignerContentUpdatePolicy.class);

	private DesignerService designerService;

	public void setDesignerService(DesignerService designerService) {
		this.designerService = designerService;
	}

	@Override
	public void doInit() {
		policyComponent.bindClassBehaviour(ContentServicePolicies.OnContentUpdatePolicy.QNAME, DesignerModel.ASPECT_CONFIG, new JavaBehaviour(this,
				"onContentUpdate"));

		policyComponent.bindClassBehaviour(ContentServicePolicies.OnContentUpdatePolicy.QNAME, DesignerModel.ASPECT_MODEL, new JavaBehaviour(this,
				"onContentUpdate"));

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, DesignerModel.ASPECT_CONFIG, new JavaBehaviour(this,
				"onUpdateProperties"));

		policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, DesignerModel.ASPECT_CONFIG, new JavaBehaviour(this,
				"beforeDeleteNode"));

	}

	@Override
	public void onContentUpdate(NodeRef nodeRef, boolean newContent) {
		queueNode(nodeRef);
	}

	@Override
	protected void doBeforeCommit(String key, Set<NodeRef> pendingNodes) {

		try {
			policyBehaviourFilter.disableBehaviour(DesignerModel.ASPECT_CONFIG);
			policyBehaviourFilter.disableBehaviour(DesignerModel.ASPECT_MODEL);

			for (NodeRef pendingNode : pendingNodes) {
				if (nodeService.exists(pendingNode)) {
					designerService.createAndPublishConfig(pendingNode);
				}

			}

		} finally {
			policyBehaviourFilter.enableBehaviour(DesignerModel.ASPECT_CONFIG);
			policyBehaviourFilter.enableBehaviour(DesignerModel.ASPECT_MODEL);
		}
	}

	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		// handles rename actions
		Serializable nameBefore = before.get(ContentModel.PROP_NAME);
		Serializable nameAfter = after.get(ContentModel.PROP_NAME);

		logger.debug("Rename on config, before=" + nameBefore + ", after=" + nameAfter + ", equals ? " + nameBefore.equals(nameAfter));
		if (nameBefore != null && !nameBefore.equals(nameAfter)) {
			designerService.unpublish((String)nameBefore);
			queueNode(nodeRef);
		}
	}
	
	@Override
	public void beforeDeleteNode(NodeRef nodeRef) {
		logger.debug("Delete, oldNode = "+nodeRef+", exists ? "+nodeService.exists(nodeRef));
		
		if(nodeService.exists(nodeRef)){
			designerService.unpublish(nodeRef);
		}
	};

}
