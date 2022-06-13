package fr.becpg.repo.search.data;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

public class SearchRuleResult {

	Map<NodeRef, Map<String, NodeRef>> itemVersions;
	List<NodeRef> results;
	
	
	public Map<NodeRef, Map<String, NodeRef>> getItemVersions() {
		return itemVersions;
	}
	public void setItemVersions(Map<NodeRef, Map<String, NodeRef>> itemVersions) {
		this.itemVersions = itemVersions;
	}
	public List<NodeRef> getResults() {
		return results;
	}
	public void setResults(List<NodeRef> results) {
		this.results = results;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(itemVersions, results);
	}
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
