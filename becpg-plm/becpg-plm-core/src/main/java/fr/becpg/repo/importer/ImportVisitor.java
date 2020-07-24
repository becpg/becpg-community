/*
 *
 */
package fr.becpg.repo.importer;

import java.text.ParseException;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.dom4j.Element;

import fr.becpg.config.mapping.MappingException;

/**
 * <p>ImportVisitor interface.</p>
 *
 * @author querephi
 * @version $Id: $Id
 */
public interface ImportVisitor {

	/**
	 * <p>importNode.</p>
	 *
	 * @param importContext a {@link fr.becpg.repo.importer.ImportContext} object.
	 * @param values a {@link java.util.List} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @throws java.text.ParseException if any.
	 * @throws fr.becpg.repo.importer.ImporterException if any.
	 */
	NodeRef importNode(ImportContext importContext, List<String> values) throws ParseException, ImporterException;

	/**
	 * <p>loadClassMapping.</p>
	 *
	 * @param mapping a {@link java.lang.Object} object.
	 * @param importContext a {@link fr.becpg.repo.importer.ImportContext} object.
	 * @param mappingLoader a {@link fr.becpg.repo.importer.MappingLoader} object.
	 * @return a {@link fr.becpg.repo.importer.ImportContext} object.
	 * @throws fr.becpg.config.mapping.MappingException if any.
	 */
	ImportContext loadClassMapping(Object mapping, ImportContext importContext, MappingLoader mappingLoader) throws MappingException;

	/**
	 * <p>loadMappingColumns.</p>
	 *
	 * @param mappingElt a {@link org.dom4j.Element} object.
	 * @param columns a {@link java.util.List} object.
	 * @param importContext a {@link fr.becpg.repo.importer.ImportContext} object.
	 * @return a {@link fr.becpg.repo.importer.ImportContext} object.
	 * @throws fr.becpg.config.mapping.MappingException if any.
	 */
	ImportContext loadMappingColumns(Element mappingElt, List<String> columns, ImportContext importContext) throws MappingException;
}
