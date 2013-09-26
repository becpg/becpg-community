package fr.becpg.repo.helper.extractors;

import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

public interface NodeDataExtractor {

	
	public Map<String,Object> extract(NodeRef noderef);
	
	
}
