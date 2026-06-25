package fr.becpg.repo.entity.datalist;

import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Contributes extra, computed attributes to a data list item JSON, keyed by data
 * list item type. Implementations register themselves so the generic extractor
 * stays free of type-specific logic.
 *
 * @author matthieu
 */
public interface DataListItemDecorator {

	/**
	 * <p>Computes additional attributes to merge into the extracted item.</p>
	 *
	 * @param nodeRef the data list item node
	 * @return a map of extra attributes, never null (empty when nothing to add)
	 */
	Map<String, Object> decorate(NodeRef nodeRef);

}
