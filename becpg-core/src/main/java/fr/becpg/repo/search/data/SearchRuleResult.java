package fr.becpg.repo.search.data;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>SearchRuleResult class.</p>
 *
 * @author matthieu
 */
public class SearchRuleResult {

	Map<NodeRef, Map<String, NodeRef>> itemVersions;
	List<NodeRef> results;
	
	
	/**
	 * <p>Getter for the field <code>itemVersions</code>.</p>
	 *
	 * @return a {@link java.util.Map} object
	 */
	public Map<NodeRef, Map<String, NodeRef>> getItemVersions() {
		return itemVersions;
	}
	/**
	 * <p>Setter for the field <code>itemVersions</code>.</p>
	 *
	 * @param itemVersions a {@link java.util.Map} object
	 */
	public void setItemVersions(Map<NodeRef, Map<String, NodeRef>> itemVersions) {
		this.itemVersions = itemVersions;
	}
	/**
	 * <p>Getter for the field <code>results</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<NodeRef> getResults() {
		return results;
	}
	/**
	 * <p>Setter for the field <code>results</code>.</p>
	 *
	 * @param results a {@link java.util.List} object
	 */
	public void setResults(List<NodeRef> results) {
		this.results = results;
	}
	
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(itemVersions, results);
	}
	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SearchRuleResult other = (SearchRuleResult) obj;
		return Objects.equals(itemVersions, other.itemVersions) && Objects.equals(results, other.results);
	}
	
	
}
