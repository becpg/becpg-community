package fr.becpg.repo.listvalue;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

public interface ListValueExtractor {

	Map<String, String> extract(List<NodeRef> nodeRefs);

}
