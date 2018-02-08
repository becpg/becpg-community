package fr.becpg.repo.ecm.listvalue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.listvalue.ListValueEntry;
import fr.becpg.repo.listvalue.ListValueExtractor;

@Service("calculatedCharactsValueExtractor")
public class CalculatedCharactsValueExtractor implements ListValueExtractor<NodeRef> {

	@Autowired
	private NodeService nodeService;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private AttributeExtractorService attributeExtractorService;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Override
	public List<ListValueEntry> extract(List<NodeRef> nodeRefs) {
		List<ListValueEntry> suggestions = new ArrayList<>();
		if (nodeRefs != null) {
			for (NodeRef nodeRef : nodeRefs) {
				QName type = nodeService.getType(nodeRef);
				String name = attributeExtractorService.extractPropName(type, nodeRef);
				String cssClass = attributeExtractorService.extractMetadata(type, nodeRef);
				Map<String, String> props = new HashMap<>(2);
				props.put("type", type.toPrefixString(namespaceService));

				String parentName = "";
				if (PLMModel.TYPE_LABELINGRULELIST.equals(nodeService.getType(nodeRef))) {
					parentName = getParentOfLabellingListValue(nodeRef) + " > ";
				}

				if (nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_COLOR)) {
					props.put("color", (String) nodeService.getProperty(nodeRef, BeCPGModel.PROP_COLOR));
				}
				
				if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TITLED)) {
					props.put("title", (String) nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE));
					props.put("description", (String) nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION));
				} else {
					name = parentName + name;
				}

				ListValueEntry entry = new ListValueEntry(nodeRef.toString(), name, cssClass, props);

				suggestions.add(entry);
			}
		}
		return suggestions;
	}

	private String getParentOfLabellingListValue(NodeRef value) {
		String ret = null;

		NodeRef parent = nodeService.getPrimaryParent(value).getParentRef();
		while (!entityDictionaryService.isSubClass(nodeService.getType(parent), PLMModel.TYPE_PRODUCT)) {
			parent = nodeService.getPrimaryParent(parent).getParentRef();
		}

		if (nodeService.getProperty(parent, ContentModel.PROP_TITLE) != null) {
			ret = (String) nodeService.getProperty(parent, ContentModel.PROP_TITLE);
		} else {
			ret = (String) nodeService.getProperty(parent, ContentModel.PROP_NAME);
		}

		return ret;
	}

}
