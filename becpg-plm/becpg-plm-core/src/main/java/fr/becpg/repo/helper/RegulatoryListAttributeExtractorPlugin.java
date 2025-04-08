package fr.becpg.repo.helper;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.impl.CharactAttributeExtractorPlugin;

@Service
public class RegulatoryListAttributeExtractorPlugin extends CharactAttributeExtractorPlugin {

	@Autowired
	private AssociationService associationService;
	
	@Override
	public String extractPropName(QName type, NodeRef nodeRef) {
		String countries = associationService.getTargetAssocs(nodeRef, PLMModel.ASSOC_REGULATORY_COUNTRIES)
				.stream()
				.map(i -> nodeService.getProperty(i, BeCPGModel.PROP_CHARACT_NAME))
				.map(String.class::cast)
				.collect(Collectors.joining(", "));
		String usages = associationService.getTargetAssocs(nodeRef, PLMModel.ASSOC_REGULATORY_USAGE_REF)
				.stream()
				.map(i -> nodeService.getProperty(i, BeCPGModel.PROP_CHARACT_NAME))
				.map(String.class::cast)
				.collect(Collectors.joining(", "));
		return countries + ", " + usages;
	}

	@Override
	public Collection<QName> getMatchingTypes() {
		return Collections.singletonList(PLMModel.TYPE_REGULATORY_LIST);
	}
	
	@Override
	public Integer getPriority() {
		return HIGH_PRIORITY;
	}

}
