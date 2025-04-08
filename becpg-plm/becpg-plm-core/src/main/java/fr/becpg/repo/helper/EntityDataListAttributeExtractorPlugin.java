package fr.becpg.repo.helper;

import java.util.Collection;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.impl.CharactAttributeExtractorPlugin;

@Service
public class EntityDataListAttributeExtractorPlugin extends CharactAttributeExtractorPlugin {
	
	@Override
	public String extractPropName(QName type, NodeRef nodeRef) {
		QName assoc = entityDictionaryService.getDefaultPivotAssoc(type);
		if (assoc != null) {
			List<AssociationRef> targetAssocs = nodeService.getTargetAssocs(nodeRef, assoc);
			if (!targetAssocs.isEmpty()) {
				AssociationRef targetAssoc = targetAssocs.get(0);
				return super.extractPropName(null, targetAssoc.getTargetRef());
			}
		}
		return (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
	}

	@Override
	public String extractMetadata(QName type, NodeRef nodeRef) {
		QName assoc = entityDictionaryService.getDefaultPivotAssoc(type);
		if (assoc != null) {
			List<AssociationRef> targetAssocs = nodeService.getTargetAssocs(nodeRef, assoc);
			if (!targetAssocs.isEmpty()) {
				AssociationRef targetAssoc = targetAssocs.get(0);
				return entityDictionaryService.toPrefixString(nodeService.getType(targetAssoc.getTargetRef())).split(":")[1];
			}
		}
		return super.extractMetadata(type, nodeRef);
	}

	@Override
	public Collection<QName> getMatchingTypes() {
		return entityDictionaryService.getSubTypes(BeCPGModel.TYPE_ENTITYLIST_ITEM);
	}
	
	@Override
	public Integer getPriority() {
		return LOW_PRIORITY;
	}

}
