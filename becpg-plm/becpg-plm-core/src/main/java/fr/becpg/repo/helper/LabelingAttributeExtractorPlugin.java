package fr.becpg.repo.helper;

import java.util.Collection;
import java.util.Collections;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AttributeExtractorService.AttributeExtractorPlugin;

/**
 * <p>LabelingAttributeExtractorPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class LabelingAttributeExtractorPlugin implements AttributeExtractorPlugin {

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private NodeService nodeService;

	/** {@inheritDoc} */
	@Override
	public String extractPropName(QName type, NodeRef nodeRef) {

		NodeRef grp = associationService.getTargetAssoc(nodeRef, PLMModel.ASSOC_ILL_GRP);

		if (grp != null) {
			String title = (String) nodeService.getProperty(grp, PLMModel.PROP_LABELINGRULELIST_LABEL);

			if (title == null || title.isBlank()) {
				title = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE);
			}

			if (title == null || title.isBlank()) {
				title = (String) nodeService.getProperty(grp, ContentModel.PROP_NAME);
			}

			return title;
		}

		return type.toPrefixString();
	}

	/** {@inheritDoc} */
	@Override
	public String extractMetadata(QName type, NodeRef nodeRef) {
		return PLMModel.TYPE_INGLABELINGLIST.toPrefixString(namespaceService).split(":")[1];
	}

	/** {@inheritDoc} */
	@Override
	public Collection<QName> getMatchingTypes() {
		return Collections.singletonList(PLMModel.TYPE_INGLABELINGLIST);
	}

	/** {@inheritDoc} */
	@Override
	public Integer getPriority() {
		return 0;
	}

}
