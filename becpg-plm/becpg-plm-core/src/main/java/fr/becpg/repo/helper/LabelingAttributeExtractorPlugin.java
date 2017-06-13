package fr.becpg.repo.helper;

import java.util.Collection;
import java.util.Collections;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AttributeExtractorService.AttributeExtractorPlugin;

@Service
public class LabelingAttributeExtractorPlugin implements AttributeExtractorPlugin {

	
	@Autowired
	private  NamespaceService namespaceService;	
	
	
	@Autowired
	private AssociationService associationService;
	

	@Autowired
	private NodeService nodeService;
	
	
	
	@Override
	public String extractPropName(QName type, NodeRef nodeRef) {
		
		NodeRef grp = associationService.getTargetAssoc(nodeRef, PLMModel.ASSOC_ILL_GRP);
		
		if(grp!=null){
			return (String) nodeService.getProperty(grp, ContentModel.PROP_NAME);
			
		}
		
		return type.toPrefixString();
	}

	@Override
	public String extractMetadata(QName type, NodeRef nodeRef) {
		return PLMModel.TYPE_INGLABELINGLIST.toPrefixString(namespaceService).split(":")[1];
	}

	@Override
	public Collection<QName> getMatchingTypes() {
		return Collections.singletonList(PLMModel.TYPE_INGLABELINGLIST);
	}

	@Override
	public Integer getPriority() {
		return 0;
	}

}
