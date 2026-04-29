package fr.becpg.repo.search;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>AdvSearchQueryFilter class.</p>
 *
 * @author matthieu
 */
public class AdvSearchQueryFilter {

	private final Set<NodeRef> includeIds = new HashSet<>();
	private final Set<NodeRef> excludeIds = new HashSet<>();
	private boolean includeIdsDefined;
	private boolean requiresPostFiltering;

	/**
	 * <p>empty.</p>
	 *
	 * @return a {@link fr.becpg.repo.search.AdvSearchQueryFilter} object
	 */
	public static AdvSearchQueryFilter empty() {
		return new AdvSearchQueryFilter();
	}

	/**
	 * <p>Getter for the field <code>includeIds</code>.</p>
	 *
	 * @return a {@link java.util.Set} object
	 */
	public Set<NodeRef> getIncludeIds() {
		return includeIds;
	}

	/**
	 * <p>Getter for the field <code>excludeIds</code>.</p>
	 *
	 * @return a {@link java.util.Set} object
	 */
	public Set<NodeRef> getExcludeIds() {
		return excludeIds;
	}

	/**
	 * <p>hasIncludeIds.</p>
	 *
	 * @return a boolean
	 */
	public boolean hasIncludeIds() {
		return includeIdsDefined;
	}

	/**
	 * <p>requiresPostFiltering.</p>
	 *
	 * @return a boolean
	 */
	public boolean requiresPostFiltering() {
		return requiresPostFiltering;
	}

	/**
	 * <p>addIncludeIds.</p>
	 *
	 * @param nodeRefs a {@link java.util.Set} object
	 */
	public void addIncludeIds(Set<NodeRef> nodeRefs) {
		includeIdsDefined = true;
		includeIds.addAll(nodeRefs);
	}

	/**
	 * <p>retainIncludeIds.</p>
	 *
	 * @param nodeRefs a {@link java.util.Set} object
	 */
	public void retainIncludeIds(Set<NodeRef> nodeRefs) {
		if (!includeIdsDefined) {
			includeIds.addAll(nodeRefs);
			includeIdsDefined = true;
		} else {
			includeIds.retainAll(nodeRefs);
		}
	}

	/**
	 * <p>addExcludeIds.</p>
	 *
	 * @param nodeRefs a {@link java.util.Set} object
	 */
	public void addExcludeIds(Set<NodeRef> nodeRefs) {
		excludeIds.addAll(nodeRefs);
	}

	/**
	 * <p>setRequiresPostFiltering.</p>
	 *
	 * @param requiresPostFiltering a boolean
	 */
	public void setRequiresPostFiltering(boolean requiresPostFiltering) {
		this.requiresPostFiltering = requiresPostFiltering;
	}

	/**
	 * <p>merge.</p>
	 *
	 * @param queryFilter a {@link fr.becpg.repo.search.AdvSearchQueryFilter} object
	 */
	public void merge(AdvSearchQueryFilter queryFilter) {
		if (queryFilter.hasIncludeIds()) {
			retainIncludeIds(queryFilter.getIncludeIds());
		}
		addExcludeIds(queryFilter.getExcludeIds());
		requiresPostFiltering = requiresPostFiltering || queryFilter.requiresPostFiltering();
	}
}
