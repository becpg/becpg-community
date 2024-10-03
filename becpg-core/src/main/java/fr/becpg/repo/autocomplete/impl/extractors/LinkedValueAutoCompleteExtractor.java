package fr.becpg.repo.autocomplete.impl.extractors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.autocomplete.AutoCompleteEntry;
import fr.becpg.repo.autocomplete.AutoCompleteExtractor;

/**
 * <p>
 * LinkedValueValueExtractor class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class LinkedValueAutoCompleteExtractor implements AutoCompleteExtractor<NodeRef> {

	private NodeService nodeService;

	/**
	 * <p>Constructor for LinkedValueAutoCompleteExtractor.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	public LinkedValueAutoCompleteExtractor(NodeService nodeService) {
		super();
		this.nodeService = nodeService;
	}

	/** {@inheritDoc} */
	@Override
	public List<AutoCompleteEntry> extract(List<NodeRef> hierarchies) {
		List<AutoCompleteEntry> suggestions = new ArrayList<>();
		if (hierarchies != null) {
			for (NodeRef hierarchy : hierarchies) {
				String currentHierarchyPath = extractHierarchyFullName(hierarchy, new HashSet<>());
				suggestions.add(new AutoCompleteEntry(hierarchy.toString(), currentHierarchyPath, ""));
			}
		}

		return suggestions;
	}

	private String extractHierarchyFullName(NodeRef hierarchy, Set<NodeRef> visited) {
		visited.add(hierarchy);

		String res = extractHierarchyName(hierarchy);
		NodeRef parent = (NodeRef) nodeService.getProperty(hierarchy, BeCPGModel.PROP_PARENT_LEVEL);
		if ((parent != null) && !visited.contains(parent)) {

			res = extractHierarchyFullName(parent, visited) + " > " + res;
		}

		return res;
	}

	private String extractHierarchyName(NodeRef hierarchy) {
		return (String) nodeService.getProperty(hierarchy, BeCPGModel.PROP_LKV_VALUE);
	}

}
