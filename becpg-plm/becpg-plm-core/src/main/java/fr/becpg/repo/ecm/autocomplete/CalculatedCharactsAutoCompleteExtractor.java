package fr.becpg.repo.ecm.autocomplete;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.autocomplete.AutoCompleteEntry;
import fr.becpg.repo.autocomplete.impl.extractors.TargetAssocAutoCompleteExtractor;
import fr.becpg.repo.entity.EntityListDAO;

/**
 * <p>CalculatedCharactsValueExtractor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("calculatedCharactsValueExtractor")
public class CalculatedCharactsAutoCompleteExtractor extends TargetAssocAutoCompleteExtractor {

	@Autowired
	private NodeService nodeService;

	@Autowired
	private EntityListDAO entityListDAO;

	/** {@inheritDoc} */
	@Override
	public List<AutoCompleteEntry> extract(List<NodeRef> nodeRefs) {
		List<AutoCompleteEntry> suggestions = new ArrayList<>();
		if (nodeRefs != null) {
			suggestions = super.extract(nodeRefs);

			for (AutoCompleteEntry entry : suggestions) {
				if ("labelingRuleList".equals(entry.getCssClass())) {
					entry.setName(getParentOfLabelingListValue(new NodeRef(entry.getValue())) + " > " + entry.getName());
				}
			}
		}
		return suggestions;
	}
	
	

	private String getParentOfLabelingListValue(NodeRef value) {

		String ret = "";
		NodeRef parent = entityListDAO.getEntity(value);

		if (parent != null) {
			if (nodeService.getProperty(parent, ContentModel.PROP_TITLE) != null) {
				ret = (String) nodeService.getProperty(parent, ContentModel.PROP_TITLE);
			} else {
				ret = (String) nodeService.getProperty(parent, ContentModel.PROP_NAME);
			}
		}

		return ret;
	}

}
