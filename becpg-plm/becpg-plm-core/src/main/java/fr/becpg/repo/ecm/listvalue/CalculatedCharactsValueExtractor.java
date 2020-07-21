package fr.becpg.repo.ecm.listvalue;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.listvalue.ListValueEntry;
import fr.becpg.repo.listvalue.impl.TargetAssocValueExtractor;

/**
 * <p>CalculatedCharactsValueExtractor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("calculatedCharactsValueExtractor")
public class CalculatedCharactsValueExtractor extends TargetAssocValueExtractor {

	@Autowired
	private NodeService nodeService;

	@Autowired
	private EntityListDAO entityListDAO;

	/** {@inheritDoc} */
	@Override
	public List<ListValueEntry> extract(List<NodeRef> nodeRefs) {
		List<ListValueEntry> suggestions = new ArrayList<>();
		if (nodeRefs != null) {
			suggestions = super.extract(nodeRefs);

			for (ListValueEntry entry : suggestions) {
				if ("labelingRuleList".equals(entry.getCssClass())) {
					entry.setName(getParentOfLabellingListValue(new NodeRef(entry.getValue())) + " > " + entry.getName());
				}
			}
		}
		return suggestions;
	}

	private String getParentOfLabellingListValue(NodeRef value) {

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
