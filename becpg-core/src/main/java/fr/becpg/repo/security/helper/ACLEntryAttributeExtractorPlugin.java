package fr.becpg.repo.security.helper;

import java.util.Collection;
import java.util.Collections;

import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.stereotype.Service;

import fr.becpg.model.SecurityModel;
import fr.becpg.repo.helper.MessageHelper;
import fr.becpg.repo.helper.impl.CharactAttributeExtractorPlugin;

/**
 * <p>ACLEntryAttributeExtractorPlugin class.</p>
 *
 * @author matthieu
 */
@Service
public class ACLEntryAttributeExtractorPlugin extends CharactAttributeExtractorPlugin {
	
	/** {@inheritDoc} */
	@Override
	public String extractPropName(QName type, NodeRef nodeRef) {
		String permission = MessageHelper.getMessage("listconstraint.sec_aclPermission." + nodeService.getProperty(nodeRef, SecurityModel.PROP_ACL_PERMISSION));
		String qname = (String) nodeService.getProperty(nodeRef, SecurityModel.PROP_ACL_PROPNAME);
		if (qname != null && !qname.isBlank()) {
			QName createQName = QName.createQName(qname, namespaceService);
			TypeDefinition qnameType = entityDictionaryService.getType(createQName);
			if (qnameType != null) {
				String title = qnameType.getTitle(entityDictionaryService);
				return permission + " - " + title;
			}
			ClassAttributeDefinition propDef = entityDictionaryService.getPropDef(createQName);
			if (propDef != null) {
				String title = entityDictionaryService.getTitle(propDef, type);
				return permission + " - " + title;
			}
			return permission + " - " + qname;
		}
		return permission;
	}

	/** {@inheritDoc} */
	@Override
	public Collection<QName> getMatchingTypes() {
		return Collections.singletonList(SecurityModel.TYPE_ACL_ENTRY);
	}
	
	/** {@inheritDoc} */
	@Override
	public Integer getPriority() {
		return HIGH_PRIORITY;
	}

}
