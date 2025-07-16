package fr.becpg.repo.helper;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.impl.CharactAttributeExtractorPlugin;

/**
 * <p>EntityDataListAttributeExtractorPlugin class.</p>
 *
 * @author matthieu
 */
@Service
public class EntityDataListAttributeExtractorPlugin extends CharactAttributeExtractorPlugin {
	
	/** {@inheritDoc} */
	@Override
	@Nonnull
	public String extractPropName(@Nonnull QName type, @Nonnull NodeRef nodeRef) {
		QName assoc = entityDictionaryService.getDefaultPivotAssoc(type);
		if (assoc != null) {
			List<AssociationRef> targetAssocs = nodeService.getTargetAssocs(nodeRef, assoc);
			if (!targetAssocs.isEmpty()) {
				AssociationRef targetAssoc = targetAssocs.get(0);
				// Use the original type since we know it's not null (enforced by @Nonnull)
				return super.extractPropName(type, targetAssoc.getTargetRef());
			}
		}
		return (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
	}

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public String extractMetadata(@Nonnull QName type, @Nonnull NodeRef nodeRef) {
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

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public Collection<QName> getMatchingTypes() {
		return entityDictionaryService.getSubTypes(BeCPGModel.TYPE_ENTITYLIST_ITEM);
	}
	
	/** {@inheritDoc} */
	@Override
	public Integer getPriority() {
		return LOW_PRIORITY;
	}

}
