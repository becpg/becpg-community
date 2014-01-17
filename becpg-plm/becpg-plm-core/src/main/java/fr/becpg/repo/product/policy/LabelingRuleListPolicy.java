package fr.becpg.repo.product.policy;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.namespace.QName;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

@Service
public class LabelingRuleListPolicy extends AbstractBeCPGPolicy implements CopyServicePolicies.OnCopyNodePolicy {

	private EntityListDAO entityListDAO;

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void doInit() {
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME, BeCPGModel.TYPE_LABELING_RULE_LIST, new JavaBehaviour(this, "getCopyCallback"));
	}


	@Override
	public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails) {
		return new LabelingRuleListBehaviourCallback();
	}

	private class LabelingRuleListBehaviourCallback extends DefaultCopyBehaviourCallback {

		private LabelingRuleListBehaviourCallback() {
		}

		@Override
		public boolean getMustCopy(QName classQName, CopyDetails copyDetails) {
			if (nodeService.hasAspect(entityListDAO.getEntity(copyDetails.getSourceNodeRef()), BeCPGModel.ASPECT_ENTITY_TPL)
					&& !Boolean.TRUE.equals(copyDetails.getSourceNodeProperties().get(BeCPGModel.PROP_IS_MANUAL_LISTITEM))) {
				return false;
			}
			return true;
		}

		@Override
		public Map<QName, Serializable> getCopyProperties(QName classQName, CopyDetails copyDetails, Map<QName, Serializable> properties) {
			properties.put(BeCPGModel.PROP_IS_MANUAL_LISTITEM, Boolean.TRUE);
			return properties;
		}
	}

}
