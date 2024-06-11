package fr.becpg.repo.repository.model;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.lang.NonNull;

/**
 * <p>SourceableDataItem interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface SourceableDataItem {

	/**
	 * <p>getSources.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	@NonNull
	public List<NodeRef> getSources();
}
