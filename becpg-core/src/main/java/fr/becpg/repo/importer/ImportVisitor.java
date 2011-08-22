/*
 * 
 */
package fr.becpg.repo.importer;

import java.text.ParseException;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.dom4j.Element;

import fr.becpg.config.mapping.MappingException;


// TODO: Auto-generated Javadoc
/**
 * The Interface ImportVisitor.
 *
 * @author querephi
 */
public interface ImportVisitor {

	/**
	 * Import node.
	 *
	 * @param importContext the import context
	 * @param values the values
	 * @return the node ref
	 * @throws ParseException the parse exception
	 */
	NodeRef importNode(ImportContext importContext, List<String>values) throws ParseException, ImporterException;
	
	/**
	 * Load class mapping.
	 *
	 * @param mappingElt the mapping elt
	 * @param importContext the import context
	 * @return the import context
	 * @throws ImporterException the be cpg exception
	 */
	ImportContext loadClassMapping(Element mappingElt, ImportContext importContext) throws MappingException ;
	
	/**
	 * Load mapping columns.
	 *
	 * @param mappingElt the mapping elt
	 * @param columns the columns
	 * @param importContext the import context
	 * @return the import context
	 * @throws ImporterException the be cpg exception
	 */
	ImportContext loadMappingColumns(Element mappingElt, List<String> columns, ImportContext importContext) throws MappingException;
}
