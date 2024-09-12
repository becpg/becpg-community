package fr.becpg.repo.designer.policy;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.designer.DesignerModel;
import fr.becpg.repo.designer.DesignerService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * <p>DesignerContentUpdatePolicy class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class DesignerContentUpdatePolicy extends AbstractBeCPGPolicy implements ContentServicePolicies.OnContentUpdatePolicy,
		NodeServicePolicies.OnUpdatePropertiesPolicy, NodeServicePolicies.BeforeDeleteNodePolicy, NodeServicePolicies.OnAddAspectPolicy{

	private static final Log logger = LogFactory.getLog(DesignerContentUpdatePolicy.class);

	private DesignerService designerService;

	/**
	 * <p>Setter for the field <code>designerService</code>.</p>
	 *
	 * @param designerService a {@link fr.becpg.repo.designer.DesignerService} object.
	 */
	public void setDesignerService(DesignerService designerService) {
		this.designerService = designerService;
	}

	/** {@inheritDoc} */
	@Override
	public void doInit() {
		
		policyComponent.bindClassBehaviour(ContentServicePolicies.OnContentUpdatePolicy.QNAME, DesignerModel.ASPECT_CONFIG, new JavaBehaviour(this,
				"onContentUpdate"));
		
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME, DesignerModel.ASPECT_CONFIG, new JavaBehaviour(this,
				"onAddAspect"));
		

		policyComponent.bindClassBehaviour(ContentServicePolicies.OnContentUpdatePolicy.QNAME, DesignerModel.ASPECT_MODEL, new JavaBehaviour(this,
				"onContentUpdate"));

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, DesignerModel.ASPECT_CONFIG, new JavaBehaviour(this,
								"onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT));
		
		
		policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, DesignerModel.ASPECT_CONFIG, new JavaBehaviour(this,
				"beforeDeleteNode"));
		
	}

	/** {@inheritDoc} */
	@Override
	public void onContentUpdate(NodeRef nodeRef, boolean newContent) {
		if(!isWorkingCopy(nodeRef)) {
			queueNode(nodeRef);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		try {
			policyBehaviourFilter.disableBehaviour(DesignerModel.ASPECT_CONFIG);
			policyBehaviourFilter.disableBehaviour(DesignerModel.ASPECT_MODEL);

			for (NodeRef pendingNode : pendingNodes) {
				if (nodeService.exists(pendingNode)) {
					designerService.createAndPublishConfig(pendingNode);
					if (nodeService.hasAspect(pendingNode, DesignerModel.ASPECT_CONFIG)) {
						nodeService.setProperty(pendingNode, ContentModel.PROP_DESCRIPTION, MLTextHelper.getI18NMessage("designer.not-published"));
					}
				}

			}

		} finally {
			policyBehaviourFilter.enableBehaviour(DesignerModel.ASPECT_CONFIG);
			policyBehaviourFilter.enableBehaviour(DesignerModel.ASPECT_MODEL);
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		// handles rename actions
		Serializable nameBefore = before.get(ContentModel.PROP_NAME);
		Serializable nameAfter = after.get(ContentModel.PROP_NAME);

		logger.debug("Rename on config, before=" + nameBefore + ", after=" + nameAfter + ", equals ? " + nameBefore.equals(nameAfter));
		if (nameBefore != null && !nameBefore.equals(nameAfter) && nodeService.exists(nodeRef)) {
			if (nodeService.hasAspect(nodeRef, DesignerModel.ASPECT_MODEL)) {
				queueNode(nodeRef);
			} else if (nodeService.hasAspect(nodeRef, DesignerModel.ASPECT_CONFIG)) {
				nodeService.setProperty(nodeRef, ContentModel.PROP_DESCRIPTION, MLTextHelper.getI18NMessage("designer.to-republish"));
			}
		}
		
	}
	
	
	/** {@inheritDoc} */
	@Override
	public void beforeDeleteNode(NodeRef nodeRef) {
		if(nodeService.exists(nodeRef) && !isWorkingCopy(nodeRef)){
			logger.debug("Delete, oldNode = "+nodeRef+", exists ? "+nodeService.exists(nodeRef));
			designerService.unpublish(nodeRef);
		}
	}
	

	private boolean isWorkingCopy(NodeRef nodeRef) {
		Set<QName> aspects = nodeService.getAspects(nodeRef);
		return aspects.contains(ContentModel.ASPECT_WORKING_COPY);
	}

	/** {@inheritDoc} */
	@Override
	public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName) {
		if(!isWorkingCopy(nodeRef)) {
			queueNode(nodeRef);
		}
		
	}
}
