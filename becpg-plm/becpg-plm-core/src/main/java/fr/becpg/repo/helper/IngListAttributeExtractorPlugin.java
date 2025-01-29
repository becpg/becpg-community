package fr.becpg.repo.helper;

import java.util.Collection;
import java.util.Collections;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.impl.CharactAttributeExtractorPlugin;

/**
 * <p>IngListAttributeExtractorPlugin class.</p>
 *
 * @author matthieu
 */
@Service
public class IngListAttributeExtractorPlugin extends CharactAttributeExtractorPlugin {

	@Autowired
	private AssociationService associationService;
	
	/** {@inheritDoc} */
	@Override
	public String extractPropName(QName type, NodeRef nodeRef) {
		NodeRef ing = associationService.getTargetAssoc(nodeRef, PLMModel.ASSOC_INGLIST_ING);
		return super.extractPropName(type, ing);
	}

	/** {@inheritDoc} */
	@Override
	public Collection<QName> getMatchingTypes() {
		return Collections.singletonList(PLMModel.TYPE_INGLIST);
	}
	
	/** {@inheritDoc} */
	@Override
	public Integer getPriority() {
		return 1;
	}

	/** {@inheritDoc} */
	@Override
	public String extractMetadata(QName type, NodeRef nodeRef) {
		NodeRef product = associationService.getTargetAssoc(nodeRef, PLMModel.TYPE_INGLIST);
		return super.extractMetadata(nodeService.getType(product), product);
	}

}
