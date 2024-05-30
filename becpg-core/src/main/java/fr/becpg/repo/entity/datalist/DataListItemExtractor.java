package fr.becpg.repo.entity.datalist;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

public interface DataListItemExtractor {

	List<NodeRef> extractItems(NodeRef nodeRef);

}
