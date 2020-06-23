package fr.becpg.repo.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.search.impl.SearchConfig;

public interface AdvSearchPlugin {

	List<NodeRef> filter(List<NodeRef> nodes, QName datatype, Map<String, String> criteria, SearchConfig searchConfig);

	Set<String> getIgnoredFields(QName datatype, SearchConfig searchConfig);

}
