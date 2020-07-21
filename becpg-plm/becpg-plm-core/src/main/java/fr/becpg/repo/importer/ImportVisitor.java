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
 * @author querephi
 */
public interface ImportVisitor {

	NodeRef importNode(ImportContext importContext, List<String> values) throws ParseException, ImporterException;

	ImportContext loadClassMapping(Object mapping, ImportContext importContext, MappingLoader mappingLoader) throws MappingException;

	ImportContext loadMappingColumns(Element mappingElt, List<String> columns, ImportContext importContext) throws MappingException;
}
