package fr.becpg.repo.entity.policy;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

public class EffectivityAspectPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnAddAspectPolicy, CopyServicePolicies.OnCopyNodePolicy {

	private EffectivityAspectCopyBehaviourCallback effectivityAspectCopyBehaviourCallback;
	private DictionaryService dictionaryService;
	private EntityDictionaryService entityDictionaryService;

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
		effectivityAspectCopyBehaviourCallback = new EffectivityAspectCopyBehaviourCallback(dictionaryService);
	}

	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	@Override
	public void doInit() {
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME, BeCPGModel.ASPECT_EFFECTIVITY, new JavaBehaviour(this, "onAddAspect"));
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME, BeCPGModel.ASPECT_EFFECTIVITY, new JavaBehaviour(this, "getCopyCallback"));
	}

	@Override
	public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName) {
		queueNode(nodeRef);
	}

	@Override
	protected void doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		for (NodeRef nodeRef : pendingNodes) {
			if (isNotLocked(nodeRef) && !isWorkingCopyOrVersion(nodeRef)) {
				if (nodeService.getProperty(nodeRef, BeCPGModel.PROP_START_EFFECTIVITY) == null) {
					Date startEffectivity = new Date();
					if (dictionaryService.isSubClass(nodeService.getType(nodeRef), BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
						QName dataListItemType = nodeService.getType(nodeRef);
						QName pivotCharactAssoc = entityDictionaryService.getDefaultPivotAssoc(dataListItemType);
						if (pivotCharactAssoc != null) {
							List<AssociationRef> compoAssocRefs = nodeService.getTargetAssocs(nodeRef, pivotCharactAssoc);
							NodeRef charactNodeRef = compoAssocRefs.size() > 0 ? (compoAssocRefs.get(0)).getTargetRef() : null;
							if (charactNodeRef != null && nodeService.hasAspect(charactNodeRef, BeCPGModel.ASPECT_EFFECTIVITY)) {
								startEffectivity = (Date) nodeService.getProperty(charactNodeRef, BeCPGModel.PROP_START_EFFECTIVITY);
								if (nodeService.getProperty(nodeRef, BeCPGModel.PROP_END_EFFECTIVITY) == null) {
									nodeService.setProperty(nodeRef, BeCPGModel.PROP_END_EFFECTIVITY, nodeService.getProperty(charactNodeRef, BeCPGModel.PROP_END_EFFECTIVITY));
								}
							}
						}
					}
					nodeService.setProperty(nodeRef, BeCPGModel.PROP_START_EFFECTIVITY, startEffectivity);
				}
			}
		}
	}

	@Override
	public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails) {
		return effectivityAspectCopyBehaviourCallback;
	}

	private static class EffectivityAspectCopyBehaviourCallback extends DefaultCopyBehaviourCallback {

		private DictionaryService dictionaryService;

		public EffectivityAspectCopyBehaviourCallback(DictionaryService dictionaryService) {
			this.dictionaryService = dictionaryService;
		}

		/**
		 * Don't copy certain auditable p
		 */
		@Override
		public Map<QName, Serializable> getCopyProperties(QName classQName, CopyDetails copyDetails, Map<QName, Serializable> properties) {

			if (classQName.equals(BeCPGModel.ASPECT_EFFECTIVITY)) {
				if (!dictionaryService.isSubClass(copyDetails.getSourceNodeTypeQName(), BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
					// Have the key properties reset by the aspect
					properties.remove(BeCPGModel.PROP_START_EFFECTIVITY);
				} 

			}

			return properties;
		}

		/**
		 * Do copy the aspects
		 * 
		 * @return Returns <tt>true</tt> always
		 */
		@Override
		public boolean getMustCopy(QName classQName, CopyDetails copyDetails) {
			return true;
		}
	}

}
