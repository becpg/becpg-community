package fr.becpg.repo.repository.model;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.lang.NonNull;

/**
 * 
 * @author matthieu
 *
 */
public interface SourceableDataItem {

	@NonNull
	public List<NodeRef> getSources();
}
