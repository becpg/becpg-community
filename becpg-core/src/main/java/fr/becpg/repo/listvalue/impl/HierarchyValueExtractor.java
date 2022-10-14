package fr.becpg.repo.listvalue.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.listvalue.ListValueEntry;
import fr.becpg.repo.listvalue.ListValueExtractor;

/**
 * <p>
 * HierarchyValueExtractor class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class HierarchyValueExtractor implements ListValueExtractor<NodeRef> {

	private NodeService nodeService;

	public HierarchyValueExtractor(NodeService nodeService) {
		super();
		this.nodeService = nodeService;
	}

	/** {@inheritDoc} */
	@Override
	public List<ListValueEntry> extract(List<NodeRef> hierarchies) {
		List<ListValueEntry> suggestions = new ArrayList<>();
		if (hierarchies != null) {
			for (NodeRef hierarchy : hierarchies) {
				String currentHierarchyPath = extractHierarchyFullName(hierarchy, new HashSet<>());
				suggestions.add(new ListValueEntry(hierarchy.toString(), currentHierarchyPath, ""));
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
