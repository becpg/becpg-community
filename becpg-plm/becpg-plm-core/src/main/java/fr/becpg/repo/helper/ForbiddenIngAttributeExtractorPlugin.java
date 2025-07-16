package fr.becpg.repo.helper;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.impl.CharactAttributeExtractorPlugin;

/**
 * <p>ForbiddenIngAttributeExtractorPlugin class.</p>
 *
 * @author matthieu
 */
@Service
public class ForbiddenIngAttributeExtractorPlugin extends CharactAttributeExtractorPlugin {

	@Autowired
	private AssociationService associationService;
	
	/** {@inheritDoc} */
	@Override
	@Nonnull
	public String extractPropName(@Nonnull QName type, @Nonnull NodeRef nodeRef) {
		return associationService.getTargetAssocs(nodeRef, PLMModel.ASSOC_FIL_INGS).stream()
		.map(i -> nodeService.getProperty(i, BeCPGModel.PROP_CHARACT_NAME))
		.map(String.class::cast)
		.collect(Collectors.joining(", "));
	}

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public Collection<QName> getMatchingTypes() {
		return Collections.singletonList(PLMModel.TYPE_FORBIDDENINGLIST);
	}
	
	/** {@inheritDoc} */
	@Override
	public Integer getPriority() {
		return HIGH_PRIORITY;
	}

}
