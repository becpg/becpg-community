package fr.becpg.repo.helper;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;

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
	@Nonnull
	public String extractPropName(@Nonnull QName type, @Nonnull NodeRef nodeRef, String characNameFormat) {
		// get properties value, then reset, get resetted value, then reupdate with last value
		NodeRef ing = associationService.getTargetAssoc(nodeRef, PLMModel.ASSOC_INGLIST_ING);
		return super.extractPropName(type, ing, characNameFormat);
	}

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public Collection<QName> getMatchingTypes() {
		return Collections.singletonList(PLMModel.TYPE_INGLIST);
	}
	
	/** {@inheritDoc} */
	@Override
	public Integer getPriority() {
		return HIGH_PRIORITY;
	}

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public String extractMetadata(@Nonnull QName type, @Nonnull NodeRef nodeRef) {
		NodeRef product = associationService.getTargetAssoc(nodeRef, PLMModel.ASSOC_INGLIST_ING);
		return super.extractMetadata(nodeService.getType(product), product);
	}

}
