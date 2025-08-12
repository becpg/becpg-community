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
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * <p>CompoListAttributeExtractorPlugin class.</p>
 *
 * @author matthieu
 */
@Service
public class CompoListAttributeExtractorPlugin extends CharactAttributeExtractorPlugin {
	
	@Autowired
	private AssociationService associationService;
	
	@Autowired
	private SystemConfigurationService systemConfigurationService;
	
	/** {@inheritDoc} */
	@Override
	@Nonnull
	public String extractPropName(@Nonnull QName type, @Nonnull NodeRef nodeRef) {
		NodeRef product = associationService.getTargetAssoc(nodeRef, PLMModel.ASSOC_COMPOLIST_PRODUCT);
		return super.extractExpr(product, systemConfigurationService.confValue("beCPG.product.name.format"));
	}

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public Collection<QName> getMatchingTypes() {
		return Collections.singletonList(PLMModel.TYPE_COMPOLIST);
	}
	
	/** {@inheritDoc} */
	@Override
	public Integer getPriority() {
		return 2;
	}

}
