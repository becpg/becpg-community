package fr.becpg.repo.helper;

import java.util.Arrays;
import java.util.Collection;

import javax.annotation.Nonnull;

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
	@Nonnull
	public String extractPropName(@Nonnull QName type, @Nonnull NodeRef nodeRef) {
		if (nodeRef == null) {
			return type != null ? type.toPrefixString(namespaceService) : "";
		}

		NodeRef grp = PLMModel.TYPE_LABELINGRULELIST.equals(type) ? 
			nodeRef : 
			associationService.getTargetAssoc(nodeRef, PLMModel.ASSOC_ILL_GRP);

		if (grp != null) {
			// Try to get the label from various properties
			String title = (String) nodeService.getProperty(grp, PLMModel.PROP_LABELINGRULELIST_LABEL);

			if (title == null || title.isBlank()) {
				title = (String) nodeService.getProperty(grp, ContentModel.PROP_TITLE);
			}

			if (title == null || title.isBlank()) {
				title = (String) nodeService.getProperty(grp, ContentModel.PROP_NAME);
			}

			if (title != null && !title.isBlank()) {
				return title;
			}
		}

		// Fallback to type prefix if no title found
		return type != null ? type.toPrefixString(namespaceService) : "";
	}

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public String extractMetadata(@Nonnull QName type, @Nonnull NodeRef nodeRef) {
		String[] parts = PLMModel.TYPE_INGLABELINGLIST.toPrefixString(namespaceService).split(":");
		return parts.length > 1 ? parts[1] : "";
	}

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public Collection<QName> getMatchingTypes() {
		return Arrays.asList(PLMModel.TYPE_INGLABELINGLIST, PLMModel.TYPE_LABELINGRULELIST);
	}

	/** {@inheritDoc} */
	@Override
	public Integer getPriority() {
		return HIGH_PRIORITY;
	}

}
