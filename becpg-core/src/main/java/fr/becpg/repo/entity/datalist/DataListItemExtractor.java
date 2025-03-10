package fr.becpg.repo.entity.datalist;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>DataListItemExtractor interface.</p>
 *
 * @author matthieu
 */
public interface DataListItemExtractor {

	/**
	 * <p>extractItems.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.util.List} object
	 */
	List<NodeRef> extractItems(NodeRef nodeRef);

}
